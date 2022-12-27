package com.tmobile.cloud.gcprules.APIKeys;

import com.amazonaws.util.StringUtils;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.tmobile.cloud.awsrules.utils.PacmanUtils;
import com.tmobile.cloud.constants.PacmanRuleConstants;
import com.tmobile.cloud.gcprules.utils.GCPUtils;
import com.tmobile.pacman.commons.PacmanSdkConstants;
import com.tmobile.pacman.commons.exception.InvalidInputException;
import com.tmobile.pacman.commons.exception.RuleExecutionFailedExeption;
import com.tmobile.pacman.commons.policy.BasePolicy;
import com.tmobile.pacman.commons.policy.PacmanPolicy;
import com.tmobile.pacman.commons.policy.PolicyResult;
import com.tmobile.pacman.commons.policy.Annotation;
import com.tmobile.pacman.commons.utils.CommonUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;

import java.util.*;
@PacmanPolicy(key = "Check-for-API-Key-API-Restrictions", desc = "Check for API Key API Restrictions", severity = PacmanSdkConstants.SEV_MEDIUM, category = PacmanSdkConstants.SECURITY)
public class ConfigureApiRestriction extends BasePolicy {
    private static final Logger logger = LoggerFactory.getLogger(ConfigureApiRestriction.class);
    private static final String API_KEY="cloudapis.googleapis.com";

    @Override
    public PolicyResult execute(Map<String, String> ruleParam, Map<String, String> resourceAttributes) {
        logger.debug("executing ConfigureApiRestriction...");
        Annotation annotation = null;

        String resourceId = ruleParam.get(PacmanRuleConstants.RESOURCE_ID);
        String severity = ruleParam.get(PacmanRuleConstants.SEVERITY);
        String category = ruleParam.get(PacmanRuleConstants.CATEGORY);
        String vmEsURL = CommonUtils.getEnvVariableValue(PacmanSdkConstants.ES_URI_ENV_VAR_NAME);
        if (Boolean.FALSE.equals(PacmanUtils.doesAllHaveValue(severity, category, vmEsURL))) {
            logger.info(PacmanRuleConstants.MISSING_CONFIGURATION);
            throw new InvalidInputException(PacmanRuleConstants.MISSING_CONFIGURATION);
        }

        if (!StringUtils.isNullOrEmpty(vmEsURL)) {
            vmEsURL = vmEsURL + "/gcp_apikeys/_search";
        }
        logger.debug("========gcp_apikey URL after concatenation param {}  =========", vmEsURL);

        boolean isApiRestricted = false;

        MDC.put("executionId",ruleParam.get(PacmanSdkConstants.EXECUTION_ID));
        MDC.put("ruleId", ruleParam.get(PacmanSdkConstants.POLICY_ID));

        if (!StringUtils.isNullOrEmpty(resourceId)) {
            Map<String, Object> mustFilter = new HashMap<>();
            mustFilter.put(PacmanUtils.convertAttributetoKeyword(PacmanRuleConstants.RESOURCE_ID), resourceId);
            mustFilter.put(PacmanRuleConstants.LATEST, true);
            try {
                isApiRestricted= checkForRestrictAPI(vmEsURL, mustFilter);
                if (!isApiRestricted) {
                    List<LinkedHashMap<String, Object>> issueList = new ArrayList<>();
                    LinkedHashMap<String, Object> issue = new LinkedHashMap<>();

                    annotation = Annotation.buildAnnotation(ruleParam, Annotation.Type.ISSUE);
                    annotation.put(PacmanSdkConstants.DESCRIPTION, "Ensure that the usage of your Google Cloud API keys is restricted to specific APIs such as Cloud Key Management Service (KMS) API, Cloud Storage API, Cloud Monitoring API and/or Cloud Logging API.");
                    annotation.put(PacmanRuleConstants.SEVERITY, severity);
                    annotation.put(PacmanRuleConstants.CATEGORY, category);
                    issue.put(PacmanRuleConstants.VIOLATION_REASON, "API Key is not restricted");
                    issueList.add(issue);
                    annotation.put("issueDetails", issueList.toString());
                    logger.debug("======== ConfigureApiRestriction ended with an annotation {} : =========", annotation);
                    return new PolicyResult(PacmanSdkConstants.STATUS_FAILURE, PacmanRuleConstants.FAILURE_MESSAGE,
                            annotation);
                }

            } catch (Exception exception) {
                throw new RuleExecutionFailedExeption(exception.getMessage());
            }
        }
        logger.debug("ConfigureApiRestriction ended with success MSG");
        return new PolicyResult(PacmanSdkConstants.STATUS_SUCCESS, PacmanRuleConstants.SUCCESS_MESSAGE);
    }

    private boolean checkForRestrictAPI(String vmEsURL, Map<String, Object> mustFilter) throws Exception {
        logger.debug("inside restrictAPI");
        boolean validationResult=true;
        JsonArray hitsJsonArray = GCPUtils.getHitsArrayFromEs(vmEsURL, mustFilter);
        if (hitsJsonArray.size() > 0) {
            logger.info("hit array size {}",hitsJsonArray.size());
            JsonObject apiKeys = (JsonObject) ((JsonObject) hitsJsonArray.get(0))
                    .get(PacmanRuleConstants.SOURCE);

            if (apiKeys.get(PacmanRuleConstants.RESTRICTIONS).getAsJsonObject()!=null &&apiKeys.get(PacmanRuleConstants.RESTRICTIONS).getAsJsonObject().size()>0) {

                JsonArray apiTargets = apiKeys.get("apiTargetList").getAsJsonArray();
                for (JsonElement apiTarget : apiTargets) {
                    if (apiTarget.getAsString().equalsIgnoreCase(API_KEY)) {
                        validationResult = false;
                        break;
                    }
                }
            }
            else{
                validationResult=false;
            }
        }
        return validationResult;
    }

    @Override
    public String getHelpText() {
        return "Check for API Key API Restrictions.";
    }

}
