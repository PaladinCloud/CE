package com.tmobile.cloud.azurerules.DiagnosticSettings;

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
import com.tmobile.pacman.commons.rule.Annotation;
import com.tmobile.pacman.commons.rule.BaseRule;
import com.tmobile.pacman.commons.rule.PacmanRule;
import com.tmobile.pacman.commons.rule.RuleResult;
import com.tmobile.pacman.commons.utils.CommonUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

@PacmanRule(key = "check-if-diagnostic-setting-has-mandatory-tags", desc = "Mandatory tags must be present in diagnostic settings", severity = PacmanSdkConstants.SEV_MEDIUM, category = PacmanSdkConstants.SECURITY)
public class MandatoryTagsInDiagnosticSettingsRule extends BaseRule {
    private static final Logger logger = LoggerFactory.getLogger(MandatoryTagsInDiagnosticSettingsRule.class);
    @Override
    public RuleResult execute(Map<String, String> ruleParam, Map<String, String> resourceAttributes) {
        logger.info("Executing MandatoryTagsInDiagnosticSettingsRule rule");
        String severity = ruleParam.get(PacmanRuleConstants.SEVERITY);
        String category = ruleParam.get(PacmanRuleConstants.CATEGORY);
        String successMSG = ruleParam.get(PacmanRuleConstants.SUCCESS);
        String failureMsg = ruleParam.get(PacmanRuleConstants.FAILURE);
        if (!PacmanUtils.doesAllHaveValue(severity, category)) {
            logger.info(PacmanRuleConstants.MISSING_CONFIGURATION);
            throw new InvalidInputException(PacmanRuleConstants.MISSING_CONFIGURATION);
        }
        String esUrl = CommonUtils.getEnvVariableValue(PacmanSdkConstants.ES_URI_ENV_VAR_NAME);
        String url = CommonUtils.getEnvVariableValue(PacmanSdkConstants.ES_URI_ENV_VAR_NAME);
        if (!StringUtils.isNullOrEmpty(url)) {
            esUrl = url + "/azure_diagnosticsetting/_search";
        }
        String resourceId = ruleParam.get(PacmanRuleConstants.RESOURCE_ID);
        boolean isValid = false;

        if (!StringUtils.isNullOrEmpty(resourceId)) {
            Map<String, Object> mustFilter = new HashMap<>();
            mustFilter.put(PacmanUtils.convertAttributetoKeyword(PacmanRuleConstants.AZURE_SUBSCRIPTION_ID), resourceId);
            mustFilter.put(PacmanRuleConstants.LATEST, true);
            try {
                isValid = checkDiagnosticSettingMendatoryTag(esUrl, mustFilter);
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
                    return new RuleResult(PacmanSdkConstants.STATUS_FAILURE, PacmanRuleConstants.FAILURE_MESSAGE,
                            annotation);
                }
            } catch (Exception e) {
                throw new RuleExecutionFailedExeption("unable to determine" + e);
            }

        }

        logger.debug(successMSG);
        return new RuleResult(PacmanSdkConstants.STATUS_SUCCESS, PacmanRuleConstants.SUCCESS_MESSAGE);

    }

    private boolean checkDiagnosticSettingMendatoryTag(String esUrl, Map<String, Object> mustFilter) throws Exception {
        logger.info("Validating the resource data from elastic search. ES URL:{}, FilterMap : {}", esUrl, mustFilter);

        boolean validationResult = false;
        JsonParser parser = new JsonParser();
        JsonObject resultJson = RulesElasticSearchRepositoryUtil.getQueryDetailsFromES(esUrl, mustFilter,
                new HashMap<>(), HashMultimap.create(), null, 0, new HashMap<>(), null, null);
        logger.debug("Data fetched from elastic search. Response JSON: {}", resultJson);
        Set<String> actualEnabledCategories =new HashSet();
        if (resultJson != null && resultJson.has(PacmanRuleConstants.HITS)) {
            String hitsString = resultJson.get(PacmanRuleConstants.HITS).toString();
            logger.debug("hit content in result json: {}", hitsString);
            JsonObject hitsJson = (JsonObject) parser.parse(hitsString);
            JsonArray hitsJsonArray = hitsJson.getAsJsonObject().get(PacmanRuleConstants.HITS).getAsJsonArray();

            for (int i=0;i<hitsJsonArray.size();i++) {
                JsonObject jsonDataItem = (JsonObject) ((JsonObject) hitsJsonArray.get(i))
                        .get(PacmanRuleConstants.SOURCE);
                logger.debug("Validating the data item: {}", jsonDataItem);

                JsonArray enabledCategories = jsonDataItem.getAsJsonArray("enabledCategories");
                for(JsonElement enabledCategory:enabledCategories)
                {
                    actualEnabledCategories.add(enabledCategory.getAsString());
                }

            }

        }
        if (actualEnabledCategories.contains("Administrative")&&actualEnabledCategories.contains("Alert")&&actualEnabledCategories.contains("Policy")&&actualEnabledCategories.contains("Security")) {
                    validationResult = true;
                }
        return validationResult;
    }

    @Override
    public String getHelpText() {
        return "Include mandatory categories in the diagnostics setting";
    }
}
