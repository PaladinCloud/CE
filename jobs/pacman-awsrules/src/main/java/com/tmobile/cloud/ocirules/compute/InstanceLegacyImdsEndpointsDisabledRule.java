package com.tmobile.cloud.ocirules.compute;

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
 * CIS OCI Foundations Benchmark v3.1.1 - 3.1 Ensure Compute Instance Legacy Metadata service endpoint is disabled.
 */
@PacmanPolicy(key = "check-oci-instance-legacy-imds-endpoints-disabled", desc = "checks if legacy metadata service (IMDSv1) endpoints are disabled on OCI compute instance", severity = PacmanSdkConstants.SEV_HIGH, category = PacmanSdkConstants.SECURITY)
public class InstanceLegacyImdsEndpointsDisabledRule extends BasePolicy {
    private static final Logger logger = LoggerFactory.getLogger(InstanceLegacyImdsEndpointsDisabledRule.class);
    private static final String LEGACY_IMDS_ENDPOINTS_DISABLED = "legacyImdsEndpointsDisabled";

    @Override
    public PolicyResult execute(Map<String, String> ruleParam, Map<String, String> resourceAttributes) {
        logger.debug("========InstanceLegacyImdsEndpointsDisabledRule started=========");
        MDC.put("executionId", ruleParam.get("executionId"));
        MDC.put("ruleId", ruleParam.get(PacmanSdkConstants.POLICY_ID));

        String severity = ruleParam.get(PacmanRuleConstants.SEVERITY);
        String category = ruleParam.get(PacmanRuleConstants.CATEGORY);

        if (Boolean.FALSE.equals(PacmanUtils.doesAllHaveValue(severity, category))) {
            logger.info(PacmanRuleConstants.MISSING_CONFIGURATION);
            throw new InvalidInputException(PacmanRuleConstants.MISSING_CONFIGURATION);
        }

        // Rule fails when are-legacy-imds-endpoints-disabled is false
        if ("false".equalsIgnoreCase(resourceAttributes.get(LEGACY_IMDS_ENDPOINTS_DISABLED))) {
            String violationReason = "OCI compute instance allows legacy metadata service (IMDSv1) endpoints";
            List<LinkedHashMap<String, Object>> issueList = new ArrayList<>();
            LinkedHashMap<String, Object> issue = new LinkedHashMap<>();

            Annotation annotation = Annotation.buildAnnotation(ruleParam, Annotation.Type.ISSUE);
            annotation.put(PacmanSdkConstants.DESCRIPTION, violationReason);
            annotation.put(PacmanRuleConstants.SEVERITY, severity);
            annotation.put(PacmanRuleConstants.CATEGORY, category);
            issue.put(PacmanRuleConstants.VIOLATION_REASON, violationReason);
            issueList.add(issue);
            annotation.put(PacmanRuleConstants.ISSUE_DETAILS, issueList.toString());
            logger.debug("========InstanceLegacyImdsEndpointsDisabledRule ended with an annotation {} : =========", annotation);
            return new PolicyResult(PacmanSdkConstants.STATUS_FAILURE, PacmanRuleConstants.FAILURE_MESSAGE, annotation);
        }

        logger.debug("========InstanceLegacyImdsEndpointsDisabledRule ended=========");
        return new PolicyResult(PacmanSdkConstants.STATUS_SUCCESS, PacmanRuleConstants.SUCCESS_MESSAGE);
    }

    @Override
    public String getHelpText() {
        return "Checks that OCI compute instances only allow Instance Metadata Service v2 (legacy IMDSv1 endpoints disabled)";
    }
}
