package com.tmobile.cloud.ocirules.compartment;

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
 * CIS OCI Foundations Benchmark v3.1.1 - 6.2 Ensure no resources are created in the root compartment.
 * The root compartment OCID is the tenancy OCID, so the resource fails when compartmentId equals tenancyId.
 * Not specific to a target type, the same policyKey can be reused for any OCI asset that has both fields.
 */
@PacmanPolicy(key = "check-oci-resource-not-in-root-compartment", desc = "checks if OCI resource is created in the root compartment of the tenancy", severity = PacmanSdkConstants.SEV_MEDIUM, category = PacmanSdkConstants.OPERATIONS)
public class ResourceNotInRootCompartmentRule extends BasePolicy {
    private static final Logger logger = LoggerFactory.getLogger(ResourceNotInRootCompartmentRule.class);
    private static final String COMPARTMENT_ID = "compartmentId";
    private static final String TENANCY_ID = "tenancyId";

    @Override
    public PolicyResult execute(Map<String, String> ruleParam, Map<String, String> resourceAttributes) {
        logger.debug("========ResourceNotInRootCompartmentRule started=========");
        MDC.put("executionId", ruleParam.get("executionId"));
        MDC.put("ruleId", ruleParam.get(PacmanSdkConstants.POLICY_ID));

        String severity = ruleParam.get(PacmanRuleConstants.SEVERITY);
        String category = ruleParam.get(PacmanRuleConstants.CATEGORY);

        if (Boolean.FALSE.equals(PacmanUtils.doesAllHaveValue(severity, category))) {
            logger.info(PacmanRuleConstants.MISSING_CONFIGURATION);
            throw new InvalidInputException(PacmanRuleConstants.MISSING_CONFIGURATION);
        }

        String compartmentId = resourceAttributes.get(COMPARTMENT_ID);
        String tenancyId = resourceAttributes.get(TENANCY_ID);
        if (StringUtils.isNullOrEmpty(compartmentId) || StringUtils.isNullOrEmpty(tenancyId)) {
            logger.info(PacmanRuleConstants.RESOURCE_DATA_NOT_FOUND);
        } else if (compartmentId.equals(tenancyId)) {
            String violationReason = "OCI resource is created in the root compartment of the tenancy";
            List<LinkedHashMap<String, Object>> issueList = new ArrayList<>();
            LinkedHashMap<String, Object> issue = new LinkedHashMap<>();

            Annotation annotation = Annotation.buildAnnotation(ruleParam, Annotation.Type.ISSUE);
            annotation.put(PacmanSdkConstants.DESCRIPTION, violationReason);
            annotation.put(PacmanRuleConstants.SEVERITY, severity);
            annotation.put(PacmanRuleConstants.CATEGORY, category);
            issue.put(PacmanRuleConstants.VIOLATION_REASON, violationReason);
            issueList.add(issue);
            annotation.put(PacmanRuleConstants.ISSUE_DETAILS, issueList.toString());
            logger.debug("========ResourceNotInRootCompartmentRule ended with an annotation {} : =========", annotation);
            return new PolicyResult(PacmanSdkConstants.STATUS_FAILURE, PacmanRuleConstants.FAILURE_MESSAGE, annotation);
        }

        logger.debug("========ResourceNotInRootCompartmentRule ended=========");
        return new PolicyResult(PacmanSdkConstants.STATUS_SUCCESS, PacmanRuleConstants.SUCCESS_MESSAGE);
    }

    @Override
    public String getHelpText() {
        return "Checks that OCI resources are placed in a child compartment instead of the root compartment";
    }
}
