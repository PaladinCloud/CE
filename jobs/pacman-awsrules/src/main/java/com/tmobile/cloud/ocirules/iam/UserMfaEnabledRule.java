package com.tmobile.cloud.ocirules.iam;

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
 * CIS OCI Foundations Benchmark v3.1.1 - 1.7 Ensure MFA is enabled for all users with a console password.
 */
@PacmanPolicy(key = "check-oci-user-mfa-enabled", desc = "checks if MFA is enabled for OCI IAM user that can sign in with a console password", severity = PacmanSdkConstants.SEV_HIGH, category = PacmanSdkConstants.SECURITY)
public class UserMfaEnabledRule extends BasePolicy {
    private static final Logger logger = LoggerFactory.getLogger(UserMfaEnabledRule.class);
    private static final String CAN_USE_CONSOLE_PASSWORD = "canUseConsolePassword";
    private static final String IS_MFA_ACTIVATED = "isMfaActivated";

    @Override
    public PolicyResult execute(Map<String, String> ruleParam, Map<String, String> resourceAttributes) {
        logger.debug("========UserMfaEnabledRule started=========");
        MDC.put("executionId", ruleParam.get("executionId"));
        MDC.put("ruleId", ruleParam.get(PacmanSdkConstants.POLICY_ID));

        String severity = ruleParam.get(PacmanRuleConstants.SEVERITY);
        String category = ruleParam.get(PacmanRuleConstants.CATEGORY);

        if (Boolean.FALSE.equals(PacmanUtils.doesAllHaveValue(severity, category))) {
            logger.info(PacmanRuleConstants.MISSING_CONFIGURATION);
            throw new InvalidInputException(PacmanRuleConstants.MISSING_CONFIGURATION);
        }

        String canUseConsolePassword = resourceAttributes.get(CAN_USE_CONSOLE_PASSWORD);
        String isMfaActivated = resourceAttributes.get(IS_MFA_ACTIVATED);
        // Rule fails when capabilities.can-use-console-password is true and is-mfa-activated is false
        if (StringUtils.isNullOrEmpty(canUseConsolePassword) || StringUtils.isNullOrEmpty(isMfaActivated)) {
            logger.info(PacmanRuleConstants.RESOURCE_DATA_NOT_FOUND);
        } else if ("true".equalsIgnoreCase(canUseConsolePassword) && "false".equalsIgnoreCase(isMfaActivated)) {
            String violationReason = "OCI IAM user can sign in with a console password but MFA is not activated";
            List<LinkedHashMap<String, Object>> issueList = new ArrayList<>();
            LinkedHashMap<String, Object> issue = new LinkedHashMap<>();

            Annotation annotation = Annotation.buildAnnotation(ruleParam, Annotation.Type.ISSUE);
            annotation.put(PacmanSdkConstants.DESCRIPTION, violationReason);
            annotation.put(PacmanRuleConstants.SEVERITY, severity);
            annotation.put(PacmanRuleConstants.CATEGORY, category);
            issue.put(PacmanRuleConstants.VIOLATION_REASON, violationReason);
            issueList.add(issue);
            annotation.put(PacmanRuleConstants.ISSUE_DETAILS, issueList.toString());
            logger.debug("========UserMfaEnabledRule ended with an annotation {} : =========", annotation);
            return new PolicyResult(PacmanSdkConstants.STATUS_FAILURE, PacmanRuleConstants.FAILURE_MESSAGE, annotation);
        }

        logger.debug("========UserMfaEnabledRule ended=========");
        return new PolicyResult(PacmanSdkConstants.STATUS_SUCCESS, PacmanRuleConstants.SUCCESS_MESSAGE);
    }

    @Override
    public String getHelpText() {
        return "Checks that OCI IAM users who can sign in with a console password have MFA activated";
    }
}
