package com.tmobile.cloud.azurerules.SQLDatabase;

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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

@PacmanPolicy(key = "check-for-threat-detection-on-sql-server", desc = "Enable all types of threat detection for your Microsoft Azure SQL database servers", severity = PacmanSdkConstants.SEV_HIGH, category = PacmanSdkConstants.SECURITY)
public class CheckThreatDetectionRule extends BasePolicy {

    private static final Logger logger = LoggerFactory.getLogger(CheckThreatDetectionRule.class);

    @Override
    public PolicyResult execute(Map<String, String> ruleParam, Map<String, String> resourceAttributes) {
        logger.info("Executing Sql Thread Detection Rule");

        String severity = ruleParam.get(PacmanRuleConstants.SEVERITY);
        String category = ruleParam.get(PacmanRuleConstants.CATEGORY);

        if (!PacmanUtils.doesAllHaveValue(severity, category)) {
            logger.info(PacmanRuleConstants.MISSING_CONFIGURATION);
            throw new InvalidInputException(PacmanRuleConstants.MISSING_CONFIGURATION);
        }

        String esUrl = CommonUtils.getEnvVariableValue(PacmanSdkConstants.ES_URI_ENV_VAR_NAME);
        String url = CommonUtils.getEnvVariableValue(PacmanSdkConstants.ES_URI_ENV_VAR_NAME);
        if (!StringUtils.isNullOrEmpty(url)) {
            esUrl = url + "/azure_sqldatabase/_search";
        }

        String resourceId = ruleParam.get(PacmanRuleConstants.RESOURCE_ID);

        boolean isValid = false;
        if (!StringUtils.isNullOrEmpty(resourceId)) {

            Map<String, Object> mustFilter = new HashMap<>();
            mustFilter.put(PacmanUtils.convertAttributetoKeyword(PacmanRuleConstants.RESOURCE_ID), resourceId);
            mustFilter.put(PacmanRuleConstants.LATEST, true);
            try {
                isValid = isSQLdbThreatExcluded(esUrl, mustFilter);
            } catch (Exception e) {
                logger.error("unable to determine", e);
                throw new RuleExecutionFailedExeption("unable to determine" + e);
            }

            if (isValid) {
                List<LinkedHashMap<String, Object>> issueList = new ArrayList<>();
                LinkedHashMap<String, Object> issue = new LinkedHashMap<>();
                Annotation annotation = null;
                annotation = Annotation.buildAnnotation(ruleParam, Annotation.Type.ISSUE);
                annotation.put(PacmanSdkConstants.DESCRIPTION, "threat detection is  excluded on sql database");
                annotation.put(PacmanRuleConstants.SEVERITY, severity);
                annotation.put(PacmanRuleConstants.CATEGORY, category);
                issue.put(PacmanRuleConstants.VIOLATION_REASON,
                        ruleParam.get(PacmanRuleConstants.RULE_ID) + " Violation Found!");
                issueList.add(issue);
                annotation.put(PacmanRuleConstants.ISSUE_DETAILS, issueList.toString());
                logger.debug("threat detection-on-sql database server Failed. isValid flag {} : ",
                        isValid);
                return new PolicyResult(PacmanSdkConstants.STATUS_FAILURE, PacmanRuleConstants.FAILURE_MESSAGE,
                        annotation);
            }
        }
        logger.debug("threat detection on sql database server  SUCCESS. isValid flag: {}", isValid);
        return new PolicyResult(PacmanSdkConstants.STATUS_SUCCESS, PacmanRuleConstants.SUCCESS_MESSAGE);
    }

    private boolean isSQLdbThreatExcluded(String esUrl, Map<String, Object> mustFilter) throws Exception {
        logger.info("Validating the resource data from elastic search. ES URL:{}, FilterMap : {}", esUrl, mustFilter);
        JsonParser parser = new JsonParser();
        JsonObject resultJson = RulesElasticSearchRepositoryUtil.getQueryDetailsFromES(esUrl, mustFilter,
                new HashMap<>(),
                HashMultimap.create(), null, 0, new HashMap<>(), null, null);
        logger.debug("Data fetched from elastic search. Response JSON: {}", resultJson.toString());

        if (resultJson != null && resultJson.has(PacmanRuleConstants.HITS)) {
            String hitsString = resultJson.get(PacmanRuleConstants.HITS).toString();
            logger.debug("hit content in result json: {}", hitsString);
            JsonObject hitsJson = (JsonObject) parser.parse(hitsString);
            JsonArray hitsJsonArray = hitsJson.getAsJsonObject().get(PacmanRuleConstants.HITS).getAsJsonArray();
            if (hitsJsonArray.size() > 0) {
                JsonObject jsonDataItem = (JsonObject) ((JsonObject) hitsJsonArray.get(0))
                        .get(PacmanRuleConstants.SOURCE);
                logger.debug("Validating the data item: {}", jsonDataItem.toString());
                JsonArray excludedDetectionTypes = jsonDataItem.getAsJsonObject()
                        .get(PacmanRuleConstants.EXCLUDEDDETECTIONTYPES).getAsJsonArray();
                if (excludedDetectionTypes.size() > 0) {
                    logger.info("excluded Detetectio types: {}", excludedDetectionTypes);
                    return true;

                }

            }

        }
        logger.info(PacmanRuleConstants.RESOURCE_DATA_NOT_FOUND);

        return false;

    }

    @Override
    public String getHelpText() {
        return "This rule will check if the Sql DataBase Server Enable all the Thread Detection";
    }

}