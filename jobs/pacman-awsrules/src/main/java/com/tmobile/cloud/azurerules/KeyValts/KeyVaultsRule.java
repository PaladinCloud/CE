package com.tmobile.cloud.azurerules.KeyValts;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import com.google.common.collect.HashMultimap;
import org.apache.commons.lang.ArrayUtils;

import com.tmobile.pacman.commons.rule.Annotation;
import com.tmobile.pacman.commons.rule.BaseRule;
import com.tmobile.pacman.commons.rule.PacmanRule;
import com.tmobile.pacman.commons.rule.RuleResult;
import com.tmobile.pacman.commons.utils.CommonUtils;
import com.amazonaws.util.StringUtils;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.tmobile.cloud.awsrules.utils.PacmanUtils;
import com.tmobile.cloud.awsrules.utils.RulesElasticSearchRepositoryUtil;
import com.tmobile.cloud.constants.PacmanRuleConstants;
import com.tmobile.pacman.commons.PacmanSdkConstants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.tmobile.pacman.commons.exception.InvalidInputException;
import com.tmobile.pacman.commons.exception.RuleExecutionFailedExeption;

@PacmanRule(key = "check-for-azure-keyvault-rule-alert", desc = "azure key vault rule", severity = PacmanSdkConstants.SEV_HIGH, category = PacmanSdkConstants.SECURITY)
public class KeyVaultsRule extends BaseRule {
    private static final Logger logger = LoggerFactory.getLogger(KeyVaultsRule.class);

    @Override
    public RuleResult execute(Map<String, String> ruleParam, Map<String, String> resourceAttributes) {
        // TODO Auto-generated method stub

        logger.info("Executing key Valuts  rule");
        String severity = ruleParam.get(PacmanRuleConstants.SEVERITY);
        String category = ruleParam.get(PacmanRuleConstants.CATEGORY);
        String[] keyValutNames = ruleParam.get(PacmanRuleConstants.KEYVAULTNAMES).split(",");
        String successMSG = ruleParam.get(PacmanRuleConstants.SUCCESS);
        String failureMsg = ruleParam.get(PacmanRuleConstants.FAILURE);
        String keyValutKey = ruleParam.get(PacmanRuleConstants.KEYVAULTKEY);
        String keyValutValue = ruleParam.get(PacmanRuleConstants.KEYVAULTVALUE);

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
                isValid = checkkeyVaultCMK(esUrl, mustFilter, keyValutNames, keyValutKey, keyValutValue);
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
                    return new RuleResult(PacmanSdkConstants.STATUS_FAILURE, PacmanRuleConstants.FAILURE_MESSAGE,
                            annotation);
                }
            } catch (Exception e) {
                logger.error("unable to determine", e);
                throw new RuleExecutionFailedExeption("unable to determine" + e);
            }

        }

        logger.debug(successMSG);
        return new RuleResult(PacmanSdkConstants.STATUS_SUCCESS, PacmanRuleConstants.SUCCESS_MESSAGE);
    }

    private boolean checkkeyVaultCMK(String esUrl, Map<String, Object> mustFilter, String[] keyValutName,
            String keyValutKey, String KeyVaultValue)
            throws Exception {
        logger.info("Validating the resource data from elastic search. ES URL:{}, FilterMap : {}", esUrl, mustFilter);
        boolean validationResult = false;
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

                String resourceName = jsonDataItem.get(PacmanRuleConstants.NAME).getAsString();
                JsonObject propertiesMap = jsonDataItem.get(PacmanRuleConstants.TAGS).getAsJsonObject();

                if (ArrayUtils.contains(keyValutName, resourceName) && propertiesMap.get(keyValutKey) != null) {
                    return validationResult = true;

                } else {
                    logger.debug(PacmanRuleConstants.RESOURCE_DATA_NOT_FOUND);

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

        return null;
    }

}
