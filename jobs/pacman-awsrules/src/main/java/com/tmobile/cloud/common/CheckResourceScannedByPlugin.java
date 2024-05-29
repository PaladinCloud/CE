package com.tmobile.cloud.common;

import com.google.gson.JsonArray;
import com.tmobile.cloud.awsrules.utils.PacmanUtils;
import com.tmobile.cloud.constants.PacmanRuleConstants;
import com.tmobile.pacman.commons.PacmanSdkConstants;
import com.tmobile.pacman.commons.exception.InvalidInputException;
import com.tmobile.pacman.commons.policy.Annotation;
import com.tmobile.pacman.commons.policy.BasePolicy;
import com.tmobile.pacman.commons.policy.PacmanPolicy;
import com.tmobile.pacman.commons.policy.PolicyResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@PacmanPolicy(key = "check-for-resource-scanned-by-plugin", desc = "checks for resources scanned by external plugins", severity = PacmanSdkConstants.SEV_HIGH, category = PacmanSdkConstants.GOVERNANCE)
public class CheckResourceScannedByPlugin extends BasePolicy {

    private static final Logger logger = LoggerFactory.getLogger(CheckResourceScannedByPlugin.class);

    public PolicyResult execute(final Map<String, String> policyParam, Map<String, String> resourceAttributes) {
        logger.debug("========CheckResourceScannedByPlugin rule started=========");
        Annotation annotation = null;
        String severity = policyParam.get(PacmanRuleConstants.SEVERITY);
        String category = policyParam.get(PacmanRuleConstants.CATEGORY);
        String resourceKey = policyParam.get(PacmanRuleConstants.RESOURCE_KEY);
        String datasorce = policyParam.get(PacmanSdkConstants.DATA_SOURCE_KEY);
        String esEndpoint = "/" + policyParam.get(PacmanRuleConstants.RESOURCE_INDEX) + "/_search";
        String esResourceUrl = PacmanUtils.getPacmanHost(PacmanRuleConstants.ES_URI) + esEndpoint;

        MDC.put("executionId", policyParam.get("executionId")); // this is the logback Mapped Diagnostic Context
        MDC.put("ruleId", policyParam.get(PacmanSdkConstants.POLICY_ID)); // this is the logback Mapped Diagnostic Context

        if (!PacmanUtils.doesAllHaveValue(severity, category, esResourceUrl)) {
            logger.info(PacmanRuleConstants.MISSING_CONFIGURATION);
            throw new InvalidInputException(PacmanRuleConstants.MISSING_CONFIGURATION);
        }

        List<LinkedHashMap<String, Object>> issueList = new ArrayList<>();
        LinkedHashMap<String, Object> issue = new LinkedHashMap<>();
        boolean resourceScanned = true;

        String entityType = resourceAttributes.get(PacmanRuleConstants.ENTITY_TYPE);
        try {
            JsonArray resourceArray = PacmanUtils.getResultFromElasticSearch(resourceAttributes.get(PacmanRuleConstants.ACCOUNTID), resourceAttributes.get(PacmanRuleConstants.RESOURCE_ID), esResourceUrl, resourceKey, null, "true", null);
            resourceScanned = !resourceArray.isEmpty();
        } catch (Exception e) {
            logger.error("Error while fetching resource info from ES", e);
        }
        if (!resourceScanned) {
            String description = entityType + " instance not scanned  by " + datasorce + " found";
            annotation = Annotation.buildAnnotation(policyParam, Annotation.Type.ISSUE);
            annotation.put(PacmanSdkConstants.DESCRIPTION, description);
            annotation.put(PacmanRuleConstants.SEVERITY, severity);
            annotation.put(PacmanRuleConstants.CATEGORY, category);

            issue.put(PacmanRuleConstants.VIOLATION_REASON, description);
            issue.put(PacmanRuleConstants.SOURCE_VERIFIED, resourceKey);
            issue.put(PacmanRuleConstants.FAILED_REASON, datasorce + " agent not found");
            issueList.add(issue);
            annotation.put("issueDetails", issueList.toString());

            logger.debug("========CheckResourceScannedByPlugin rule ended with annotation {} : =========", annotation);
            return new PolicyResult(PacmanSdkConstants.STATUS_FAILURE, PacmanRuleConstants.FAILURE_MESSAGE, annotation);
        }

        logger.debug("========CheckResourceScannedByPlugin rule evaluation successful for resourceId {} =========", resourceAttributes.get(PacmanRuleConstants.RESOURCE_ID));
        return new PolicyResult(PacmanSdkConstants.STATUS_SUCCESS, PacmanRuleConstants.SUCCESS_MESSAGE);
    }

    public String getHelpText() {
        return "This rule checks for resources scanned by external plugins";
    }
}
