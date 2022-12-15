package com.tmobile.cloud.azurerules.ActivityLog;

import com.amazonaws.util.StringUtils;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.tmobile.cloud.awsrules.utils.PacmanUtils;
import com.tmobile.cloud.azurerules.utils.AzureUtils;
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

@PacmanPolicy(key = "check-for-azure-activity-log-alert", desc = "azure activity log alert", severity = PacmanSdkConstants.SEV_HIGH, category = PacmanSdkConstants.SECURITY)
public class ActivityLogRule extends BasePolicy {
    private static final Logger logger = LoggerFactory.getLogger(ActivityLogRule.class);

    @Override
    public PolicyResult execute(Map<String, String> ruleParam, Map<String, String> resourceAttributes) {
        logger.info("Executing Azure Security rule");
        String severity = ruleParam.get(PacmanRuleConstants.SEVERITY);
        String category = ruleParam.get(PacmanRuleConstants.CATEGORY);
        String field = ruleParam.get(PacmanRuleConstants.FIELD);
        String equalsType = ruleParam.get(PacmanRuleConstants.EQUALS_STRING);
        String successMSG = ruleParam.get(PacmanRuleConstants.SUCCESS);
        String failureMsg = ruleParam.get(PacmanRuleConstants.FAILURE);
        logger.info("field: {} ", field);
        logger.info("equalsType : {} ", equalsType);
        if (Boolean.FALSE.equals(PacmanUtils.doesAllHaveValue(severity, category))) {
            logger.info(PacmanRuleConstants.MISSING_CONFIGURATION);
            throw new InvalidInputException(PacmanRuleConstants.MISSING_CONFIGURATION);
        }
        String esUrl = CommonUtils.getEnvVariableValue(PacmanSdkConstants.ES_URI_ENV_VAR_NAME);
        String url = CommonUtils.getEnvVariableValue(PacmanSdkConstants.ES_URI_ENV_VAR_NAME);
        if (!StringUtils.isNullOrEmpty(url)) {
            esUrl = url + "/azure_activitylogalert/_search";
        }
        String resourceId = ruleParam.get(PacmanRuleConstants.RESOURCE_ID);
        logger.info("resourceId: {} ", resourceId);
        boolean isValid = false;

        if (!StringUtils.isNullOrEmpty(resourceId)) {
            Map<String, Object> mustFilter = new HashMap<>();
            mustFilter.put(PacmanUtils.convertAttributetoKeyword(PacmanRuleConstants.AZURE_SUBSCRIPTION), resourceId);
            mustFilter.put(PacmanRuleConstants.LATEST, true);
            try {
                isValid = checkActivityLogAlert(esUrl, mustFilter, equalsType, field);
                if (!isValid) {
                    List<LinkedHashMap<String, Object>> issueList = new ArrayList<>();
                    LinkedHashMap<String, Object> issue = new LinkedHashMap<>();
                    Annotation annotation = null;
                    annotation = Annotation.buildAnnotation(ruleParam, Annotation.Type.ISSUE);
                    annotation.put(PacmanSdkConstants.DESCRIPTION, failureMsg);
                    annotation.put(PacmanRuleConstants.SEVERITY, severity);
                    annotation.put(PacmanRuleConstants.CATEGORY, category);
                    issue.put(PacmanRuleConstants.VIOLATION_REASON, ruleParam.get(PacmanRuleConstants.RULE_ID) + failureMsg + " Violation Found!");
                    issueList.add(issue);
                    annotation.put(PacmanRuleConstants.ISSUE_DETAILS, issueList.toString());
                    logger.debug(failureMsg);
                    return new PolicyResult(PacmanSdkConstants.STATUS_FAILURE, PacmanRuleConstants.FAILURE_MESSAGE, annotation);
                }
            } catch (Exception e) {
                throw new RuleExecutionFailedExeption("unable to determine" + e);
            }
        }

        logger.debug(successMSG);
        return new PolicyResult(PacmanSdkConstants.STATUS_SUCCESS, PacmanRuleConstants.SUCCESS_MESSAGE);
    }

    private boolean checkActivityLogAlert(String esUrl, Map<String, Object> mustFilter, String equalsType, String field) throws Exception {
        boolean validationResult = false;
        logger.info("mustFilter: {} ", mustFilter);
        JsonArray hitsJsonArray = AzureUtils.getHitsArrayFromEs(esUrl, mustFilter);

        if (hitsJsonArray.size() > 0) {

            for (int i = 0; i < hitsJsonArray.size(); i++) {
                JsonObject activityLogAlert = ((JsonObject) hitsJsonArray.get(i));

                validationResult = this.validateActivityLogAlert(activityLogAlert, equalsType, field);

                if (!validationResult) {
                    break;
                }
            }
        }

        return validationResult;
    }

    private boolean validateActivityLogAlert(JsonObject activityLogAlert, String equalsType, String field) {
        boolean result = false;
        JsonObject source = activityLogAlert.getAsJsonObject().get(PacmanRuleConstants.SOURCE).getAsJsonObject();
        JsonObject properties = source.getAsJsonObject().get(PacmanRuleConstants.PROPERTIES).getAsJsonObject();
        JsonObject condition = properties.getAsJsonObject().get("condition").getAsJsonObject();
        boolean enabled = properties.getAsJsonObject().get("enabled").getAsBoolean();
        boolean categoryAdminExists = false;
        boolean isOperationNameMatched = false;

        JsonArray allof = condition.getAsJsonObject().get(PacmanRuleConstants.ALLOF).getAsJsonArray();

        if (allof != null && allof.size() > 0) {

            for (JsonElement jsonElement : allof) {

                JsonObject allofdetails = jsonElement.getAsJsonObject();
                String jsonField = allofdetails.get(PacmanRuleConstants.FIELD).getAsString();

                String jsonEqualType = allofdetails.get(PacmanRuleConstants.EQUALS_STRING).getAsString();
                if (jsonField != null && jsonEqualType != null) {
                    categoryAdminExists = validateFields(jsonField, PacmanRuleConstants.ACTIVITY_RULE_CATEGORY, jsonEqualType, PacmanRuleConstants.ADMINISTRATIVE, categoryAdminExists);
                    isOperationNameMatched = validateFields(jsonField, field, jsonEqualType, equalsType, isOperationNameMatched);
                }
            }
        }
        if (isOperationNameMatched && categoryAdminExists && enabled) {
            result = true;
            logger.debug("Validating the SUCCESS data item: {}", activityLogAlert);
        }

        return result;
    }

    private boolean validateFields(String jsonField, String activityRuleCategory, String jsonEqualType, String administrative, boolean categoryAdminExists) {
        if (jsonField.equalsIgnoreCase(activityRuleCategory) && jsonEqualType.equalsIgnoreCase(administrative)) {
            categoryAdminExists = true;
        }
        return categoryAdminExists;
    }

    @Override
    public String getHelpText() {
        return "Checks the subscription whether a particular activity log alert rule is enabled or not.";
    }
}
