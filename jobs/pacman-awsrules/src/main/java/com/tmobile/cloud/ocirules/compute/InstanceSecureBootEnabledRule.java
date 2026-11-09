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
 * CIS OCI Foundations Benchmark v3.1.1 - 3.2 Ensure Secure Boot is enabled on Compute Instance.
 */
@PacmanPolicy(key = "check-oci-instance-secure-boot-enabled", desc = "checks if secure boot is enabled on OCI compute instance", severity = PacmanSdkConstants.SEV_MEDIUM, category = PacmanSdkConstants.SECURITY)
public class InstanceSecureBootEnabledRule extends BasePolicy {
    private static final Logger logger = LoggerFactory.getLogger(InstanceSecureBootEnabledRule.class);
    private static final String SECURE_BOOT_ENABLED = "secureBootEnabled";

    @Override
    public PolicyResult execute(Map<String, String> ruleParam, Map<String, String> resourceAttributes) {
        logger.debug("========InstanceSecureBootEnabledRule started=========");
        MDC.put("executionId", ruleParam.get("executionId"));
        MDC.put("ruleId", ruleParam.get(PacmanSdkConstants.POLICY_ID));

        String severity = ruleParam.get(PacmanRuleConstants.SEVERITY);
        String category = ruleParam.get(PacmanRuleConstants.CATEGORY);

        if (Boolean.FALSE.equals(PacmanUtils.doesAllHaveValue(severity, category))) {
            logger.info(PacmanRuleConstants.MISSING_CONFIGURATION);
            throw new InvalidInputException(PacmanRuleConstants.MISSING_CONFIGURATION);
        }

        // Rule fails when platform-config is null or is-secure-boot-enabled is false,
        // null will also be a violation
        if (!"true".equalsIgnoreCase(resourceAttributes.get(SECURE_BOOT_ENABLED))) {
            String violationReason = "OCI compute instance does not have Secure Boot enabled";
            List<LinkedHashMap<String, Object>> issueList = new ArrayList<>();
            LinkedHashMap<String, Object> issue = new LinkedHashMap<>();

            Annotation annotation = Annotation.buildAnnotation(ruleParam, Annotation.Type.ISSUE);
            annotation.put(PacmanSdkConstants.DESCRIPTION, violationReason);
            annotation.put(PacmanRuleConstants.SEVERITY, severity);
            annotation.put(PacmanRuleConstants.CATEGORY, category);
            issue.put(PacmanRuleConstants.VIOLATION_REASON, violationReason);
            issueList.add(issue);
            annotation.put(PacmanRuleConstants.ISSUE_DETAILS, issueList.toString());
            logger.debug("========InstanceSecureBootEnabledRule ended with an annotation {} : =========", annotation);
            return new PolicyResult(PacmanSdkConstants.STATUS_FAILURE, PacmanRuleConstants.FAILURE_MESSAGE, annotation);
        }

        logger.debug("========InstanceSecureBootEnabledRule ended=========");
        return new PolicyResult(PacmanSdkConstants.STATUS_SUCCESS, PacmanRuleConstants.SUCCESS_MESSAGE);
    }

    @Override
    public String getHelpText() {
        return "Checks that OCI compute instances are Shielded Instances with Secure Boot enabled";
    }
}
