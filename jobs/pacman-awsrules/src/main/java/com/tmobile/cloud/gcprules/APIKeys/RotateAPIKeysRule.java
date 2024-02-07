package com.tmobile.cloud.gcprules.APIKeys;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;

import com.amazonaws.util.StringUtils;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.tmobile.cloud.awsrules.utils.PacmanUtils;
import com.tmobile.cloud.constants.PacmanRuleConstants;
import com.tmobile.cloud.gcprules.utils.GCPUtils;
import com.tmobile.pacman.commons.PacmanSdkConstants;
import com.tmobile.pacman.commons.exception.InvalidInputException;
import com.tmobile.pacman.commons.exception.RuleExecutionFailedExeption;
import com.tmobile.pacman.commons.policy.Annotation;
import com.tmobile.pacman.commons.policy.BasePolicy;
import com.tmobile.pacman.commons.policy.PacmanPolicy;
import com.tmobile.pacman.commons.policy.PolicyResult;
import com.tmobile.pacman.commons.utils.CommonUtils;

@PacmanPolicy(key = "rotate-API-Keys", desc = "Ensure that all your Google Cloud API keys are regularly regenerated  in order to meet security and compliance requirements", severity = PacmanSdkConstants.SEV_MEDIUM, category = PacmanSdkConstants.SECURITY)
public class RotateAPIKeysRule extends BasePolicy {

    private static final Logger logger = LoggerFactory.getLogger(RotateAPIKeysRule.class);
    @Override
    public PolicyResult execute(Map<String, String> ruleParam, Map<String, String> resourceAttributes) {
        logger.debug("executing RotateAPIKeysRule....");
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
        logger.debug("========gcp_iamusers URL after concatenation param {}  =========", vmEsURL);

        boolean isKeysRotated = false;

        MDC.put("executionId",ruleParam.get(PacmanSdkConstants.EXECUTION_ID));
        MDC.put("ruleId", ruleParam.get(PacmanSdkConstants.POLICY_ID));

        if (!StringUtils.isNullOrEmpty(resourceId)) {
            Map<String, Object> mustFilter = new HashMap<>();
            mustFilter.put(PacmanUtils.convertAttributetoKeyword(PacmanRuleConstants.RESOURCE_ID), resourceId);
            mustFilter.put(PacmanRuleConstants.LATEST, true);
            try {
                isKeysRotated= rotateApiKeys(vmEsURL, mustFilter);
                if (!isKeysRotated) {
                    List<LinkedHashMap<String, Object>> issueList = new ArrayList<>();
                    LinkedHashMap<String, Object> issue = new LinkedHashMap<>();

                    annotation = Annotation.buildAnnotation(ruleParam, Annotation.Type.ISSUE);
                    annotation.put(PacmanSdkConstants.DESCRIPTION, "Ensure that all your Google Cloud API keys are regularly regenerated  in order to meet security and compliance requirements");
                    annotation.put(PacmanRuleConstants.SEVERITY, severity);
                    annotation.put(PacmanRuleConstants.CATEGORY, category);
                    issue.put(PacmanRuleConstants.VIOLATION_REASON, "API Key Application is not restricted");
                    issueList.add(issue);
                    annotation.put("issueDetails", issueList.toString());
                    logger.debug("========EnableAPIApplicationRestriction ended with an annotation {} : =========", annotation);
                    return new PolicyResult(PacmanSdkConstants.STATUS_FAILURE, PacmanRuleConstants.FAILURE_MESSAGE,
                            annotation);
                }

            } catch (Exception exception) {
                throw new RuleExecutionFailedExeption(exception.getMessage());
            }
        }
        logger.debug("EnableAPIApplicationRestriction ended with success MSG");
        return new PolicyResult(PacmanSdkConstants.STATUS_SUCCESS, PacmanRuleConstants.SUCCESS_MESSAGE);
    }

    private boolean rotateApiKeys(String vmEsURL, Map<String, Object> mustFilter) throws Exception {
        logger.debug("inside rotateApiKeys");
        boolean validationResult=false;
        JsonArray hitsJsonArray = GCPUtils.getHitsArrayFromEs(vmEsURL, mustFilter);

        if (hitsJsonArray.size() > 0) {
            logger.info("hit array size {}", hitsJsonArray.size());
            JsonObject apiKeys = (JsonObject) ((JsonObject) hitsJsonArray.get(0)).get(PacmanRuleConstants.SOURCE);
            String createdDateStr = apiKeys.get("createdTime").getAsString();
            SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSSSSS");
            Date date = simpleDateFormat.parse(createdDateStr);
            if (TimeUnit.DAYS.convert(new Date().getTime() - date.getTime(), TimeUnit.MILLISECONDS) <= 90) {
                validationResult = true;
            }

        }
        return validationResult;
    }


    @Override
    public String getHelpText() {
        return "Ensure that all your Google Cloud API keys are regularly regenerated  in order to meet security and compliance requirements";
    }
}
