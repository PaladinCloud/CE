package com.tmobile.cloud.azurerules.Defender;

import com.amazonaws.util.StringUtils;
import com.google.common.collect.HashMultimap;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.tmobile.cloud.awsrules.utils.PacmanUtils;
import com.tmobile.cloud.awsrules.utils.RulesElasticSearchRepositoryUtil;
import com.tmobile.cloud.constants.PacmanRuleConstants;
import com.tmobile.pacman.commons.PacmanSdkConstants;
import com.tmobile.pacman.commons.exception.InvalidInputException;
import com.tmobile.pacman.commons.exception.RuleExecutionFailedExeption;
import com.tmobile.pacman.commons.policy.Annotation;
import com.tmobile.pacman.commons.policy.BasePolicy;
import com.tmobile.pacman.commons.policy.PacmanPolicy;
import com.tmobile.pacman.commons.policy.PolicyResult;
import com.tmobile.pacman.commons.utils.CommonUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

@PacmanPolicy(key = "check-if-Auto-Provisioning-is-enabled", desc = "Automatic provisioning of the monitoring agent is enabled in your Microsoft Azure account to collect security data and events from your cloud compute resources in order to help you prevent, detect, and respond efficiently to security vulnerabilities and threats.", severity = PacmanSdkConstants.SEV_MEDIUM, category = PacmanSdkConstants.SECURITY)
public class AutoProvisioningSettingsRule extends BasePolicy {
    private static final Logger logger = LoggerFactory.getLogger(AutoProvisioningSettingsRule.class);
    @Override
    public PolicyResult execute(Map<String, String> ruleParam, Map<String, String> resourceAttributes) {
        logger.info("Executing  AutoProvisioningSettingsRule  rule");
        String severity = ruleParam.get(PacmanRuleConstants.SEVERITY);
        String category = ruleParam.get(PacmanRuleConstants.CATEGORY);
        String successMSG = ruleParam.get(PacmanRuleConstants.SUCCESS);
        String failureMsg = ruleParam.get(PacmanRuleConstants.VIOLATION_REASON);
        if (!PacmanUtils.doesAllHaveValue(severity, category)) {
            logger.info(PacmanRuleConstants.MISSING_CONFIGURATION);
            throw new InvalidInputException(PacmanRuleConstants.MISSING_CONFIGURATION);
        }
        String esUrl = CommonUtils.getEnvVariableValue(PacmanSdkConstants.ES_URI_ENV_VAR_NAME);
        String url = CommonUtils.getEnvVariableValue(PacmanSdkConstants.ES_URI_ENV_VAR_NAME);
        if (!StringUtils.isNullOrEmpty(url)) {
            esUrl = url + "/azure_defender/_search";
        }
        boolean isValid = false;
        Map<String, Object> mustFilter = new HashMap<>();
        mustFilter.put(PacmanRuleConstants.LATEST, true);
        try {
            isValid = checkAutoProvisionEnabled(esUrl, mustFilter);
            if (!isValid) {
                List<LinkedHashMap<String, Object>> issueList = new ArrayList<>();
                LinkedHashMap<String, Object> issue = new LinkedHashMap<>();
                Annotation annotation = null;
                annotation = Annotation.buildAnnotation(ruleParam, Annotation.Type.ISSUE);
                annotation.put(PacmanSdkConstants.DESCRIPTION,
                        failureMsg);
                annotation.put(PacmanRuleConstants.SEVERITY, severity);
                annotation.put(PacmanRuleConstants.CATEGORY, category);
                issue.put(PacmanRuleConstants.VIOLATION_REASON,
                        ruleParam.get(PacmanRuleConstants.RULE_ID) + failureMsg + " Violation Found!");
                issueList.add(issue);
                annotation.put(PacmanRuleConstants.ISSUE_DETAILS, issueList.toString());
                logger.debug(
                        failureMsg);
                return new PolicyResult(PacmanSdkConstants.STATUS_FAILURE, PacmanRuleConstants.FAILURE_MESSAGE,
                        annotation);
            }
        } catch (Exception e) {
            throw new RuleExecutionFailedExeption("unable to determine" + e);
        }
        logger.debug(successMSG);
        return new PolicyResult(PacmanSdkConstants.STATUS_SUCCESS, PacmanRuleConstants.SUCCESS_MESSAGE);

    }

    private boolean checkAutoProvisionEnabled(String esUrl, Map<String, Object> mustFilter) throws Exception {
        logger.info("Validating the resource data from elastic search. ES URL:{}, FilterMap : {}", esUrl, mustFilter);

        boolean validationResult = true;
        JsonParser parser = new JsonParser();
        JsonObject resultJson = RulesElasticSearchRepositoryUtil.getQueryDetailsFromES(esUrl, mustFilter,
                new HashMap<>(), HashMultimap.create(), null, 0, new HashMap<>(), null, null);
        logger.debug("Data fetched from elastic search. Response JSON: {}", resultJson);

        if (resultJson != null && resultJson.has(PacmanRuleConstants.HITS)) {
            String hitsString = resultJson.get(PacmanRuleConstants.HITS).toString();
            logger.debug("hit content in result json: {}", hitsString);
            JsonObject hitsJson = (JsonObject) parser.parse(hitsString);
            JsonArray hitsJsonArray = hitsJson.getAsJsonObject().get(PacmanRuleConstants.HITS).getAsJsonArray();
            for (int i=0;i<hitsJsonArray.size();i++) {
                JsonObject jsonDataItem = (JsonObject) ((JsonObject) hitsJsonArray.get(i))
                        .get(PacmanRuleConstants.SOURCE);
                logger.debug("Validating the data item: {}", jsonDataItem);
                JsonArray autoProvisioningSettingsList =jsonDataItem.getAsJsonObject().get("autoProvisioningSettingsList").getAsJsonArray();
                if(!autoProvisioningSettingsList.isEmpty()){
                    for(int j=0;j<autoProvisioningSettingsList.size();j++){
                        JsonObject autoProvisioningObj = ((JsonObject) autoProvisioningSettingsList.get(i));
                        String autoProvision=autoProvisioningObj.get(PacmanRuleConstants.AUTO_PROVISION).getAsString();
                        if(autoProvision.equalsIgnoreCase("Off")){
                            validationResult=false;
                            break;
                        }
                    }
                }
                else{
                    logger.info(PacmanRuleConstants.RESOURCE_DATA_NOT_FOUND);
                    validationResult=false;
                }
            }
        }else {
            logger.info(PacmanRuleConstants.RESOURCE_DATA_NOT_FOUND);
        }
        return validationResult;
    }

    @Override
    public String getHelpText() {
        return "This rule checks if Auto-Provisioning of Log Analytics agent for Azure VMs is Set to On";
    }
}
