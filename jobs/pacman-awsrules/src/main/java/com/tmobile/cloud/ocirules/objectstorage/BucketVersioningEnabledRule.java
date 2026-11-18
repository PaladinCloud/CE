package com.tmobile.cloud.ocirules.objectstorage;

import com.amazonaws.util.StringUtils;
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

/**
 * CIS OCI Foundations Benchmark v3.1.1 - 5.1.3 Ensure Versioning is Enabled for Object Storage Buckets.
 */
@PacmanPolicy(key = "check-oci-bucket-versioning-enabled", desc = "checks if versioning is enabled on OCI object storage bucket", severity = PacmanSdkConstants.SEV_MEDIUM, category = PacmanSdkConstants.SECURITY)
public class BucketVersioningEnabledRule extends BasePolicy {
    private static final Logger logger = LoggerFactory.getLogger(BucketVersioningEnabledRule.class);
    private static final String VERSIONING = "versioning";
    private static final String ENABLED = "Enabled";

    @Override
    public PolicyResult execute(Map<String, String> ruleParam, Map<String, String> resourceAttributes) {
        logger.debug("========BucketVersioningEnabledRule started=========");
        MDC.put("executionId", ruleParam.get("executionId"));
        MDC.put("ruleId", ruleParam.get(PacmanSdkConstants.POLICY_ID));

        String severity = ruleParam.get(PacmanRuleConstants.SEVERITY);
        String category = ruleParam.get(PacmanRuleConstants.CATEGORY);

        if (Boolean.FALSE.equals(PacmanUtils.doesAllHaveValue(severity, category))) {
            logger.info(PacmanRuleConstants.MISSING_CONFIGURATION);
            throw new InvalidInputException(PacmanRuleConstants.MISSING_CONFIGURATION);
        }

        String versioning = resourceAttributes.get(VERSIONING);
        // Rule fails when versioning is Suspended or Disabled
        if (StringUtils.isNullOrEmpty(versioning)) {
            logger.info(PacmanRuleConstants.RESOURCE_DATA_NOT_FOUND);
        } else if (!ENABLED.equalsIgnoreCase(versioning)) {
            String violationReason = "OCI object storage bucket does not have versioning enabled, versioning is " + versioning;
            List<LinkedHashMap<String, Object>> issueList = new ArrayList<>();
            LinkedHashMap<String, Object> issue = new LinkedHashMap<>();

            Annotation annotation = Annotation.buildAnnotation(ruleParam, Annotation.Type.ISSUE);
            annotation.put(PacmanSdkConstants.DESCRIPTION, violationReason);
            annotation.put(PacmanRuleConstants.SEVERITY, severity);
            annotation.put(PacmanRuleConstants.CATEGORY, category);
            issue.put(PacmanRuleConstants.VIOLATION_REASON, violationReason);
            issueList.add(issue);
            annotation.put(PacmanRuleConstants.ISSUE_DETAILS, issueList.toString());
            logger.debug("========BucketVersioningEnabledRule ended with an annotation {} : =========", annotation);
            return new PolicyResult(PacmanSdkConstants.STATUS_FAILURE, PacmanRuleConstants.FAILURE_MESSAGE, annotation);
        }

        logger.debug("========BucketVersioningEnabledRule ended=========");
        return new PolicyResult(PacmanSdkConstants.STATUS_SUCCESS, PacmanRuleConstants.SUCCESS_MESSAGE);
    }

    @Override
    public String getHelpText() {
        return "Checks that OCI object storage buckets have versioning enabled";
    }
}
