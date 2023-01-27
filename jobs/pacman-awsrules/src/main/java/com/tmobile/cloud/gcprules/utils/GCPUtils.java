package com.tmobile.cloud.gcprules.utils;

import com.amazonaws.util.StringUtils;
import com.google.common.collect.HashMultimap;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.tmobile.cloud.awsrules.utils.PacmanUtils;
import com.tmobile.cloud.awsrules.utils.RulesElasticSearchRepositoryUtil;
import com.tmobile.cloud.constants.PacmanRuleConstants;
import com.tmobile.pacman.commons.PacmanSdkConstants;
import com.tmobile.pacman.commons.policy.Annotation;
import com.tmobile.pacman.commons.policy.PolicyResult;
import com.tmobile.pacman.commons.utils.CommonUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

public class GCPUtils {
    private static final Logger logger = LoggerFactory.getLogger(GCPUtils.class);

    private GCPUtils() {
    }

    public static JsonArray getHitsArrayFromEs(String esURL, Map<String, Object> mustFilter) throws Exception {
        logger.info("Validating the resource data from elastic search. ES URL:{}, FilterMap : {}", esURL, mustFilter);
        JsonArray hitsJsonArray = new JsonArray();
        JsonObject resultJson = RulesElasticSearchRepositoryUtil.getQueryDetailsFromES(esURL, mustFilter,
                new HashMap<>(),
                HashMultimap.create(), null, 0, new HashMap<>(), null, null);
        logger.debug("Data fetched from elastic search. Response JSON: {}", resultJson);

        if (resultJson.has(PacmanRuleConstants.HITS)) {
            String hitsString = resultJson.get(PacmanRuleConstants.HITS).toString();
            logger.debug("hit content in result json: {}", hitsString);
            JsonObject hitsJson = (JsonObject) JsonParser.parseString(hitsString);
            hitsJsonArray = hitsJson.getAsJsonObject().get(PacmanRuleConstants.HITS).getAsJsonArray();
        }
        return hitsJsonArray;
    }

    public static JsonObject getJsonObjFromSourceData(String esURL, String resourceId) throws Exception {
        Map<String, Object> mustFilter = new HashMap<>();
        mustFilter.put(PacmanUtils.convertAttributetoKeyword(PacmanRuleConstants.RESOURCE_ID), resourceId);
        mustFilter.put(PacmanRuleConstants.LATEST, true);
        JsonArray hitsJsonArray = getHitsArrayFromEs(esURL, mustFilter);
        JsonObject sourceData= null;
        if (hitsJsonArray != null && hitsJsonArray.size() > 0){
            logger.debug("========checkIngressSettings hit array=========");
            sourceData = (JsonObject) ((JsonObject) hitsJsonArray.get(0))
                    .get(PacmanRuleConstants.SOURCE);
            logger.debug("Data retrieved from ES: {}", sourceData);
        }
        return sourceData;
    }

    public static PolicyResult fetchPolicyResult(Map<String, String> ruleParam, String description, String violationReason){
        List<LinkedHashMap<String, Object>> issueList = new ArrayList<>();
        LinkedHashMap<String, Object> issue = new LinkedHashMap<>();

        Annotation annotation = Annotation.buildAnnotation(ruleParam, Annotation.Type.ISSUE);
        annotation.put(PacmanSdkConstants.DESCRIPTION, description);
        annotation.put(PacmanRuleConstants.SEVERITY, ruleParam.get(PacmanRuleConstants.SEVERITY));
        annotation.put(PacmanRuleConstants.CATEGORY, ruleParam.get(PacmanRuleConstants.CATEGORY));
        issue.put(PacmanRuleConstants.VIOLATION_REASON, violationReason);
        issueList.add(issue);
        annotation.put(PacmanRuleConstants.ISSUE_DETAILS, issueList.toString());
        logger.debug("========rule ended with status failure {}", annotation);
        return new PolicyResult(PacmanSdkConstants.STATUS_FAILURE, PacmanRuleConstants.FAILURE_MESSAGE,
                annotation);
    }

    public static boolean validateRuleParam(Map<String, String> ruleParam){
        String severity = ruleParam.get(PacmanRuleConstants.SEVERITY);
        String category = ruleParam.get(PacmanRuleConstants.CATEGORY);
        String esUrl = CommonUtils.getEnvVariableValue(PacmanSdkConstants.ES_URI_ENV_VAR_NAME);
        if (Boolean.FALSE.equals(PacmanUtils.doesAllHaveValue(severity, category, esUrl))) {
            logger.info(PacmanRuleConstants.MISSING_CONFIGURATION);
            return false;
        }
        return true;
    }

    public static String getEsUrl(Map<String, String> ruleParam){
        String esUrl = CommonUtils.getEnvVariableValue(PacmanSdkConstants.ES_URI_ENV_VAR_NAME);
        String collectorSpecificUrl = ruleParam.get(PacmanRuleConstants.ES_URL_PARAM);
        if (!StringUtils.isNullOrEmpty(esUrl)) {
            esUrl = esUrl + collectorSpecificUrl;
        }
        return esUrl;
    }
}
