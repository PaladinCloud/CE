package com.tmobile.cloud.azurerules.KeyValts;

import com.amazonaws.util.StringUtils;
import com.google.common.collect.HashMultimap;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
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
import org.apache.commons.lang.ArrayUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

@PacmanPolicy(key = "deny-admin-privileges-azure-keyvault-rule", desc = "deny permission for administrator-level permissions for key vaults", severity = PacmanSdkConstants.SEV_HIGH, category = PacmanSdkConstants.SECURITY)
public class AdministrativeLevelPermisionsRule extends BasePolicy {

    private static final Logger logger = LoggerFactory.getLogger(AdministrativeLevelPermisionsRule.class);
    @Override
    public PolicyResult execute(Map<String, String> ruleParam, Map<String, String> resourceAttributes) {
        logger.info("Executing deny permission for administrator-level permissions for key vaults");
        String severity = ruleParam.get(PacmanRuleConstants.SEVERITY);
        String category = ruleParam.get(PacmanRuleConstants.CATEGORY);
        String successMSG = ruleParam.get(PacmanRuleConstants.SUCCESS);
        String failureMsg = ruleParam.get(PacmanRuleConstants.FAILURE);
        String[] adminForKeys = ruleParam.get(PacmanRuleConstants.ADMIN_FOR_KEYS).split(",");
        String[] adminForSecrets = ruleParam.get(PacmanRuleConstants.ADMIN_FOR_SECRETS).split(",");
        String[] adminForCertificates = ruleParam.get(PacmanRuleConstants.ADMIN_FOR_CERTIFICTAES).split(",");

        if (!PacmanUtils.doesAllHaveValue(severity, category)) {
            logger.info(PacmanRuleConstants.MISSING_CONFIGURATION);
            throw new InvalidInputException(PacmanRuleConstants.MISSING_CONFIGURATION);
        }
        String esUrl = CommonUtils.getEnvVariableValue(PacmanSdkConstants.ES_URI_ENV_VAR_NAME);
        String url = CommonUtils.getEnvVariableValue(PacmanSdkConstants.ES_URI_ENV_VAR_NAME);
        if (!StringUtils.isNullOrEmpty(url)) {
            esUrl = url + "/azure_vaults/_search";
        }

        String resourceId = ruleParam.get(PacmanRuleConstants.RESOURCE_ID);
        boolean isValid = false;

        if (!StringUtils.isNullOrEmpty(resourceId)) {
            Map<String, Object> mustFilter = new HashMap<>();
            mustFilter.put(PacmanUtils.convertAttributetoKeyword(PacmanRuleConstants.RESOURCE_ID), resourceId);
            mustFilter.put(PacmanRuleConstants.LATEST, true);
            try {
                isValid = checkkeyVaultPermissions(esUrl, mustFilter,adminForKeys,adminForSecrets,adminForCertificates);
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
                            ruleParam.get(PacmanRuleConstants.RULE_ID) + " Violation Found!");
                    issueList.add(issue);
                    annotation.put(PacmanRuleConstants.ISSUE_DETAILS, issueList.toString());
                    logger.debug(
                            failureMsg);
                    return new PolicyResult(PacmanSdkConstants.STATUS_FAILURE, PacmanRuleConstants.FAILURE_MESSAGE,
                            annotation);
                }
            } catch (Exception e) {
                logger.error("unable to determine", e);
                throw new RuleExecutionFailedExeption("unable to determine" + e);
            }

        }

        logger.debug(successMSG);
        return new PolicyResult(PacmanSdkConstants.STATUS_SUCCESS, PacmanRuleConstants.SUCCESS_MESSAGE);
    }

    private boolean checkkeyVaultPermissions(String esUrl, Map<String, Object> mustFilter, String[] adminForKeys, String[] adminForSecrets, String[] adminForCertificates) throws Exception {
        logger.info("Validating the resource data from elastic search. ES URL:{}, FilterMap : {}", esUrl, mustFilter);
        boolean validationResult = true;
        JsonObject resultJson = RulesElasticSearchRepositoryUtil.getQueryDetailsFromES(esUrl, mustFilter,
                new HashMap<>(), HashMultimap.create(), null, 0, new HashMap<>(), null, null);
        logger.debug("Data fetched from elastic search. Response JSON: {}", resultJson.toString());
        if (resultJson != null && resultJson.has(PacmanRuleConstants.HITS)) {
            String hitsString = resultJson.get(PacmanRuleConstants.HITS).toString();
            logger.debug("hit content in result json: {}", hitsString);
            JsonObject hitsJson = JsonParser.parseString(hitsString).getAsJsonObject();
            JsonArray hitsJsonArray = hitsJson.getAsJsonObject().get(PacmanRuleConstants.HITS).getAsJsonArray();
            if (hitsJsonArray.size() > 0) {
                JsonObject jsonDataItem = (JsonObject) ((JsonObject) hitsJsonArray.get(0))
                        .get(PacmanRuleConstants.SOURCE);

                JsonArray permissionForKeys = jsonDataItem.get("permissionForKeys").getAsJsonArray();
                logger.info("permissionForKeys : {}",permissionForKeys);
                JsonArray permissionForSecrets = jsonDataItem.get("permissionForSecrets").getAsJsonArray();
                logger.info("permissionForSecrets : {}",permissionForSecrets);
                JsonArray permissionForCertificates = jsonDataItem.get("permissionForCertificates").getAsJsonArray();
                logger.info("permissionForCertificates : {}",permissionForCertificates);

                for (JsonElement permissionForKey:permissionForKeys)
                {
                    String keyString=permissionForKey.getAsString();
                if (!ArrayUtils.contains(adminForKeys, keyString)) {
                    logger.info("adminForKeys : {}",adminForKeys);
                    logger.info("permissionForKey : {}",keyString);
                    logger.info("validationResult :{}",validationResult);
                    return validationResult = false;
                }
                }
                for (JsonElement permissionForSecret:permissionForSecrets)
                {
                    String secretString=permissionForSecret.getAsString();
                    if (!ArrayUtils.contains(adminForSecrets, secretString)) {
                        return validationResult = false;
                    }
                }
                for (JsonElement permissionForCertificate:permissionForCertificates)
                {
                    String certificateString=permissionForCertificate.getAsString();
                    if (!ArrayUtils.contains(adminForCertificates, certificateString)) {
                        return validationResult = false;
                    }
                }

            } else {
                logger.debug(PacmanRuleConstants.RESOURCE_DATA_NOT_FOUND);
            }
        } else {
            logger.debug(PacmanRuleConstants.RESOURCE_DATA_NOT_FOUND);
        }

        return validationResult;

    }

    @Override
    public String getHelpText() {
        return "To determine if there are any access policies with administrator-level permissions associated with Azure Key Vaults";
    }
}
