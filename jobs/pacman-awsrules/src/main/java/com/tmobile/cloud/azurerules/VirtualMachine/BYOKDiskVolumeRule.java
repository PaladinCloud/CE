package com.tmobile.cloud.azurerules.VirtualMachine;

import com.amazonaws.util.StringUtils;
import com.tmobile.cloud.awsrules.utils.PacmanUtils;
import com.tmobile.cloud.constants.PacmanRuleConstants;
import com.tmobile.pacman.commons.PacmanSdkConstants;
import com.tmobile.pacman.commons.exception.InvalidInputException;
import com.tmobile.pacman.commons.exception.RuleExecutionFailedExeption;
import com.tmobile.pacman.commons.rule.Annotation;
import com.tmobile.pacman.commons.rule.BaseRule;
import com.tmobile.pacman.commons.rule.PacmanRule;
import com.tmobile.pacman.commons.rule.RuleResult;
import com.tmobile.pacman.commons.utils.CommonUtils;
import org.slf4j.LoggerFactory;
import org.slf4j.Logger;
import com.google.common.collect.HashMultimap;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.tmobile.cloud.awsrules.utils.RulesElasticSearchRepositoryUtil;

import java.util.*;


@PacmanRule(key = "check-encryption-enabled-with-customer-managed-keys-for-Azure_VM_disks", desc = "Use customer-managed keys for Microsoft Azure virtual machine (VM) disk volumes encryption.", severity = PacmanSdkConstants.SEV_HIGH, category = PacmanSdkConstants.SECURITY)
public class BYOKDiskVolumeRule extends BaseRule {


    private static final Logger logger = LoggerFactory.getLogger(BYOKDiskVolumeRule.class);
    public static  final   String isEncryptionEnabled="isEncryptionEnabled";
    public static  final   String encryptionSetting="encryptionSettings";
    public static  final   String keyEncryption="keyEncryptionKey";
    public static  final   String keyurl="keyUrl";

    @Override
    public RuleResult execute(Map<String, String> ruleParam, Map<String, String> resourceAttributes) {

        logger.info("Executing Encryption with customer-managed keys for App-Tier disk volumes");

        String severity = ruleParam.get(PacmanRuleConstants.SEVERITY);
        String category = ruleParam.get(PacmanRuleConstants.CATEGORY);

        if (!PacmanUtils.doesAllHaveValue(severity, category)) {
            logger.info(PacmanRuleConstants.MISSING_CONFIGURATION);
            throw new InvalidInputException(PacmanRuleConstants.MISSING_CONFIGURATION);
        }
        String esUrl = CommonUtils.getEnvVariableValue(PacmanSdkConstants.ES_URI_ENV_VAR_NAME);

        if (!StringUtils.isNullOrEmpty(esUrl)) {
            esUrl = esUrl + "/azure_virtualmachine/_search";
        }

        String resourceId = ruleParam.get(PacmanRuleConstants.RESOURCE_ID);

        boolean isValid = true;
        if (!StringUtils.isNullOrEmpty(resourceId)) {

            Map<String, Object> mustFilter = new HashMap<>();
            mustFilter.put(PacmanUtils.convertAttributetoKeyword(PacmanRuleConstants.RESOURCE_ID), resourceId);
            mustFilter.put(PacmanRuleConstants.LATEST, true);
            try {
                isValid = checkIsCustomerManagedKeyAttached(esUrl, mustFilter);
            } catch (Exception e) {
                logger.error("unable to determine", e);
                throw new RuleExecutionFailedExeption("unable to determine" + e);
            }

            if (!isValid) {
                List<LinkedHashMap<String, Object>> issueList = new ArrayList<>();
                LinkedHashMap<String, Object> issue = new LinkedHashMap<>();
                Annotation annotation = null;
                annotation = Annotation.buildAnnotation(ruleParam, Annotation.Type.ISSUE);
                annotation.put(PacmanSdkConstants.DESCRIPTION,
                        "Use customer-managed keys for Microsoft Azure virtual machine (VM) disk volumes encryption.");
                annotation.put(PacmanRuleConstants.SEVERITY, severity);
                annotation.put(PacmanRuleConstants.CATEGORY, category);
                issue.put(PacmanRuleConstants.VIOLATION_REASON,
                        ruleParam.get(PacmanRuleConstants.RULE_ID) + " Violation Found!");
                issueList.add(issue);
                annotation.put(PacmanRuleConstants.ISSUE_DETAILS, issueList.toString());
                logger.debug("  The disk volumes attached to the selected Microsoft Azure virtual machine are encrypted using a service-managed key .Rule completed with FAILURE isValid flag {} : ", isValid);
                return new RuleResult(PacmanSdkConstants.STATUS_FAILURE, PacmanRuleConstants.FAILURE_MESSAGE,
                        annotation);
            }
        }
        logger.debug(" The disk volumes attached to the selected Microsoft Azure virtual machine are encrypted using a customer-managed key (BYOK) to encrypt the disk volumes attached to the selected Microsoft Azure virtual machine.Rule completed with SUCCESS. isValid flag: {}", isValid);
        return new RuleResult(PacmanSdkConstants.STATUS_SUCCESS, PacmanRuleConstants.SUCCESS_MESSAGE);
    }

    private boolean checkIsCustomerManagedKeyAttached(String esUrl, Map<String, Object> mustFilter) throws Exception{
        logger.info("Validating the resource data from elastic search. ES URL:{}, FilterMap : {}", esUrl, mustFilter);
        boolean validationResult=false;
        JsonParser parser = new JsonParser();
        JsonObject resultJson = RulesElasticSearchRepositoryUtil.getQueryDetailsFromES(esUrl, mustFilter,
                new HashMap<>(),
                HashMultimap.create(), null, 0, new HashMap<>(), null, null);
        logger.debug("Data fetched from elastic search. Response JSON: {}", resultJson.toString());

        if(resultJson !=null && resultJson.has(PacmanRuleConstants.HITS)){
            String hitsString = resultJson.get(PacmanRuleConstants.HITS).toString();
            logger.debug("hit content in result json: {}", hitsString);
            JsonObject hitsJson = (JsonObject) parser.parse(hitsString);
            JsonArray hitsJsonArray = hitsJson.getAsJsonObject().get(PacmanRuleConstants.HITS).getAsJsonArray();
            if (hitsJsonArray.size() > 0) {
                JsonObject jsonDataItem = (JsonObject) ((JsonObject) hitsJsonArray.get(0))
                        .get(PacmanRuleConstants.SOURCE);
                logger.debug("Validating the data item: {}", jsonDataItem.toString());
                JsonArray diskJsonArray = jsonDataItem.getAsJsonObject()
                        .get(PacmanRuleConstants.DISKS).getAsJsonArray();
                if(diskJsonArray.size()>0) {
                    for (int i = 0; i < diskJsonArray.size(); i++) {
                        JsonObject diskDataItem = ((JsonObject) diskJsonArray
                                .get(i));
                        boolean encryption = diskDataItem.getAsJsonObject().get(isEncryptionEnabled).getAsBoolean();
                        if (encryption) {
                            logger.info("The attached disk volumes are  encrypted,");
                            JsonArray encryptionSettings=jsonDataItem.getAsJsonObject().get(encryptionSetting).getAsJsonArray();
                            if(encryptionSettings.size()>0){
                                JsonObject keyEncryptionKey=encryptionSettings.getAsJsonArray().get(Integer.parseInt(keyEncryption)).getAsJsonObject();
                                String keyUrl=keyEncryptionKey.getAsJsonObject().get(keyurl).getAsString();
                                if(keyUrl!=null){
                                    validationResult=true;}
                            }
                        }
                    }
                }
                else {
                    logger.debug(PacmanRuleConstants.RESOURCE_DATA_NOT_FOUND);
                }
            } else {
                logger.info(PacmanRuleConstants.RESOURCE_DATA_NOT_FOUND);
            }
        } else {
            logger.info(PacmanRuleConstants.RESOURCE_DATA_NOT_FOUND);
        }
        return validationResult;
    }


    @Override
    public String getHelpText() {
        return "This rule will check if your Azure virtual machine disk volumes are encrypted with customer-managed keys";
    }
}
