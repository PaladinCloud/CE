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
 * CIS OCI Foundations Benchmark v3.1.1 - 5.1.1 Ensure no Object Storage buckets are publicly visible.
 */
@PacmanPolicy(key = "check-oci-bucket-not-public", desc = "checks if OCI object storage bucket is publicly visible", severity = PacmanSdkConstants.SEV_HIGH, category = PacmanSdkConstants.SECURITY)
public class BucketNotPublicRule extends BasePolicy {
    private static final Logger logger = LoggerFactory.getLogger(BucketNotPublicRule.class);
    private static final String PUBLIC_ACCESS_TYPE = "publicAccessType";
    private static final String NO_PUBLIC_ACCESS = "NoPublicAccess";

    @Override
    public PolicyResult execute(Map<String, String> ruleParam, Map<String, String> resourceAttributes) {
        logger.debug("========BucketNotPublicRule started=========");
        MDC.put("executionId", ruleParam.get("executionId"));
        MDC.put("ruleId", ruleParam.get(PacmanSdkConstants.POLICY_ID));

        String severity = ruleParam.get(PacmanRuleConstants.SEVERITY);
        String category = ruleParam.get(PacmanRuleConstants.CATEGORY);

        if (Boolean.FALSE.equals(PacmanUtils.doesAllHaveValue(severity, category))) {
            logger.info(PacmanRuleConstants.MISSING_CONFIGURATION);
            throw new InvalidInputException(PacmanRuleConstants.MISSING_CONFIGURATION);
        }

        String publicAccessType = resourceAttributes.get(PUBLIC_ACCESS_TYPE);
        // Rule fails when public-access-type is ObjectRead or ObjectReadWithoutList
        if (StringUtils.isNullOrEmpty(publicAccessType)) {
            logger.info(PacmanRuleConstants.RESOURCE_DATA_NOT_FOUND);
        } else if (!NO_PUBLIC_ACCESS.equalsIgnoreCase(publicAccessType)) {
            String violationReason = "OCI object storage bucket is publicly visible, public access type is " + publicAccessType;
            List<LinkedHashMap<String, Object>> issueList = new ArrayList<>();
            LinkedHashMap<String, Object> issue = new LinkedHashMap<>();

            Annotation annotation = Annotation.buildAnnotation(ruleParam, Annotation.Type.ISSUE);
            annotation.put(PacmanSdkConstants.DESCRIPTION, violationReason);
            annotation.put(PacmanRuleConstants.SEVERITY, severity);
            annotation.put(PacmanRuleConstants.CATEGORY, category);
            issue.put(PacmanRuleConstants.VIOLATION_REASON, violationReason);
            issueList.add(issue);
            annotation.put(PacmanRuleConstants.ISSUE_DETAILS, issueList.toString());
            logger.debug("========BucketNotPublicRule ended with an annotation {} : =========", annotation);
            return new PolicyResult(PacmanSdkConstants.STATUS_FAILURE, PacmanRuleConstants.FAILURE_MESSAGE, annotation);
        }

        logger.debug("========BucketNotPublicRule ended=========");
        return new PolicyResult(PacmanSdkConstants.STATUS_SUCCESS, PacmanRuleConstants.SUCCESS_MESSAGE);
    }

    @Override
    public String getHelpText() {
        return "Checks that OCI object storage buckets have public access type NoPublicAccess";
    }
}
