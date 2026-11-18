package com.tmobile.cloud.ocirules.encryption;

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
 * CIS OCI Foundations Benchmark v3.1.1 - 5.1.2 Ensure Object Storage Buckets are encrypted with a Customer Managed Key (CMK),
 * 5.2.1 Ensure Block Volumes are encrypted with Customer Managed Keys (CMK) and 5.2.2 Ensure boot volumes are encrypted with Customer Managed Key (CMK).
 * Buckets, block volumes and boot volumes all expose kms-key-id, so the same policyKey is used for these target types.
 */
@PacmanPolicy(key = "check-oci-resource-encrypted-with-cmk", desc = "checks if OCI resource is encrypted with a customer managed key", severity = PacmanSdkConstants.SEV_MEDIUM, category = PacmanSdkConstants.SECURITY)
public class ResourceEncryptedWithCmkRule extends BasePolicy {
    private static final Logger logger = LoggerFactory.getLogger(ResourceEncryptedWithCmkRule.class);
    private static final String KMS_KEY_ID = "kmsKeyId";

    @Override
    public PolicyResult execute(Map<String, String> ruleParam, Map<String, String> resourceAttributes) {
        logger.debug("========ResourceEncryptedWithCmkRule started=========");
        MDC.put("executionId", ruleParam.get("executionId"));
        MDC.put("ruleId", ruleParam.get(PacmanSdkConstants.POLICY_ID));

        String severity = ruleParam.get(PacmanRuleConstants.SEVERITY);
        String category = ruleParam.get(PacmanRuleConstants.CATEGORY);

        if (Boolean.FALSE.equals(PacmanUtils.doesAllHaveValue(severity, category))) {
            logger.info(PacmanRuleConstants.MISSING_CONFIGURATION);
            throw new InvalidInputException(PacmanRuleConstants.MISSING_CONFIGURATION);
        }

        // Rule fails when kms-key-id is null, the resource is then encrypted with an Oracle managed key
        if (StringUtils.isNullOrEmpty(resourceAttributes.get(KMS_KEY_ID))) {
            String violationReason = "OCI resource is not encrypted with a customer managed key";
            List<LinkedHashMap<String, Object>> issueList = new ArrayList<>();
            LinkedHashMap<String, Object> issue = new LinkedHashMap<>();

            Annotation annotation = Annotation.buildAnnotation(ruleParam, Annotation.Type.ISSUE);
            annotation.put(PacmanSdkConstants.DESCRIPTION, violationReason);
            annotation.put(PacmanRuleConstants.SEVERITY, severity);
            annotation.put(PacmanRuleConstants.CATEGORY, category);
            issue.put(PacmanRuleConstants.VIOLATION_REASON, violationReason);
            issueList.add(issue);
            annotation.put(PacmanRuleConstants.ISSUE_DETAILS, issueList.toString());
            logger.debug("========ResourceEncryptedWithCmkRule ended with an annotation {} : =========", annotation);
            return new PolicyResult(PacmanSdkConstants.STATUS_FAILURE, PacmanRuleConstants.FAILURE_MESSAGE, annotation);
        }

        logger.debug("========ResourceEncryptedWithCmkRule ended=========");
        return new PolicyResult(PacmanSdkConstants.STATUS_SUCCESS, PacmanRuleConstants.SUCCESS_MESSAGE);
    }

    @Override
    public String getHelpText() {
        return "Checks that OCI buckets, block volumes and boot volumes are encrypted with a customer managed key from OCI Vault";
    }
}
