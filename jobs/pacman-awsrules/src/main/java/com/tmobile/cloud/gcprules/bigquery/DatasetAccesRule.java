package com.tmobile.cloud.gcprules.bigquery;

import com.amazonaws.util.StringUtils;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.tmobile.cloud.awsrules.utils.PacmanUtils;
import com.tmobile.cloud.constants.PacmanRuleConstants;
import com.tmobile.cloud.gcprules.utils.GCPUtils;
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
import org.slf4j.MDC;

import java.util.*;
import java.util.stream.Collectors;

@PacmanRule(key = "check-bigquery-dataset-public-access", desc = "Check for Publicly Accessible BigQuery Datasets", severity = PacmanSdkConstants.SEV_HIGH, category = PacmanSdkConstants.SECURITY)

public class DatasetAccesRule extends BaseRule {

    private static final Logger logger = LoggerFactory.getLogger(DatasetAccesRule.class);

    @Override
    public RuleResult execute(Map<String, String> ruleParam, Map<String, String> resourceAttributes) {

        logger.debug("Executing public access rule for bigquery datasets");
        Annotation annotation = null;
        String resourceId = ruleParam.get(PacmanRuleConstants.RESOURCE_ID);
        String severity = ruleParam.get(PacmanRuleConstants.SEVERITY);
        String category = ruleParam.get(PacmanRuleConstants.CATEGORY);
        String description = ruleParam.get(PacmanRuleConstants.DESCRIPTION);
        String violationReason = ruleParam.get(PacmanRuleConstants.VIOLATION_REASON);
        String esUrl = CommonUtils.getEnvVariableValue(PacmanSdkConstants.ES_URI_ENV_VAR_NAME);
        String esEndpoint = ruleParam.get(PacmanRuleConstants.ES_SG_RULES_URL);
        if (Boolean.FALSE.equals(PacmanUtils.doesAllHaveValue(severity, category, esUrl))) {
            logger.info(PacmanRuleConstants.MISSING_CONFIGURATION);
            throw new InvalidInputException(PacmanRuleConstants.MISSING_CONFIGURATION);
        }

        if (!StringUtils.isNullOrEmpty(esUrl)) {
            esUrl = esUrl + esEndpoint;
        }
        logger.debug("ES search url for gcp bigquery:  {}", esUrl);
        boolean publicAccessFlag = false;
        MDC.put(PacmanSdkConstants.EXECUTION_ID, ruleParam.get(PacmanSdkConstants.EXECUTION_ID));
        MDC.put(PacmanSdkConstants.RULE_ID, ruleParam.get(PacmanSdkConstants.RULE_ID));

        if (!StringUtils.isNullOrEmpty(resourceId)) {
            Map<String, Object> mustFilter = new HashMap<>();
            mustFilter.put(PacmanUtils.convertAttributetoKeyword(PacmanRuleConstants.RESOURCE_ID), resourceId);
            mustFilter.put(PacmanRuleConstants.LATEST, true);
            try {
                publicAccessFlag = checkPublicAccess(esUrl, mustFilter);
                if (!publicAccessFlag) {
                    List<LinkedHashMap<String, Object>> issueList = new ArrayList<>();
                    LinkedHashMap<String, Object> issue = new LinkedHashMap<>();
                    annotation = Annotation.buildAnnotation(ruleParam, Annotation.Type.ISSUE);
                    annotation.put(PacmanSdkConstants.DESCRIPTION, description);
                    annotation.put(PacmanRuleConstants.SEVERITY, severity);
                    annotation.put(PacmanRuleConstants.CATEGORY, category);
                    issue.put(PacmanRuleConstants.VIOLATION_REASON, violationReason);
                    issueList.add(issue);
                    annotation.put(PacmanRuleConstants.ISSUE_DETAILS, issueList.toString());
                    logger.debug("BigQuery dataset public access ended with failure. Annotation {} :", annotation);
                    return new RuleResult(PacmanSdkConstants.STATUS_FAILURE, PacmanRuleConstants.FAILURE_MESSAGE,
                            annotation);
                }
            } catch (Exception exception) {
                throw new RuleExecutionFailedExeption(exception.getMessage());
            }
        }
        logger.debug("BigQuery dataset public access ended with success.");
        return new RuleResult(PacmanSdkConstants.STATUS_SUCCESS, PacmanRuleConstants.SUCCESS_MESSAGE);

    }

    private boolean checkPublicAccess(String esurl, Map<String, Object> mustFilter) throws Exception {
        logger.debug("Validating public access for dataset");

        JsonArray hitsJsonArray = GCPUtils.getHitsArrayFromEs(esurl, mustFilter);
        if (hitsJsonArray.size() > 0) {
            JsonObject sourceData = (JsonObject) ((JsonObject) hitsJsonArray.get(0))
                    .get(PacmanRuleConstants.SOURCE);
            logger.debug("Data retrieved from ES: {}", sourceData);
            JsonArray accessList = sourceData.getAsJsonObject().get(PacmanRuleConstants.ACL).getAsJsonArray();
            if (!accessList.isEmpty()) {
                for (int i = 0; i < accessList.size(); i++) {
                    JsonObject accessPolicy = ((JsonObject) accessList.get(i));
                    String specialGroupValue = getGroupValue(accessPolicy, PacmanRuleConstants.SPECIAL_GROUP);
                    String iamGroupValue = getGroupValue(accessPolicy, PacmanRuleConstants.IAM_MEMBER);
                    boolean checkInSpecialGrp = isAnyMatch(specialGroupValue, PacmanRuleConstants.ALL_USERS, PacmanRuleConstants.ALL_AUTH_USERS);
                    boolean checkInIamGrp = isAnyMatch(iamGroupValue, PacmanRuleConstants.ALL_USERS, PacmanRuleConstants.ALL_AUTH_USERS);
                    if (checkInSpecialGrp || checkInIamGrp) {
                        logger.debug("Public Access found to for special/IAM group: specialGroup: {}, iamGroup:{}", specialGroupValue, iamGroupValue);
                        return false;
                    }
                }
            } else {
                logger.info(PacmanRuleConstants.RESOURCE_DATA_NOT_FOUND);
            }

        } else {
            logger.info(PacmanRuleConstants.RESOURCE_DATA_NOT_FOUND);
        }
        return true;
    }

    private String getGroupValue(JsonObject accessPolicy, String specialGroup) {
        return accessPolicy.get(specialGroup) != null ? accessPolicy.get(specialGroup).getAsString() : "";
    }

    private boolean isAnyMatch(String actualValue, String... expectedValues) {
        logger.debug("Actual value passed :{}", actualValue);
        if (org.apache.commons.lang3.StringUtils.isEmpty(actualValue)) {
            logger.debug("Actual value passed is empty. Returning false");
            return false;
        }
        return Arrays.stream(expectedValues).collect(Collectors.toList()).contains(actualValue);
    }

    @Override
    public String getHelpText() {
        return "This rule checks for Publicly Accessible BigQuery Datasets";
    }
}
