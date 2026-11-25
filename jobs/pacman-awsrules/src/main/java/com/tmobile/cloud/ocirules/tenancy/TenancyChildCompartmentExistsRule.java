package com.tmobile.cloud.ocirules.tenancy;

import com.amazonaws.util.StringUtils;
import com.google.gson.JsonObject;
import com.tmobile.cloud.awsrules.utils.PacmanUtils;
import com.tmobile.cloud.constants.PacmanRuleConstants;
import com.tmobile.pacman.commons.PacmanSdkConstants;
import com.tmobile.pacman.commons.exception.InvalidInputException;
import com.tmobile.pacman.commons.exception.RuleExecutionFailedExeption;
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
 * CIS OCI Foundations Benchmark v3.1.1 - 6.1 Create at least one compartment in your tenancy to store cloud resources.
 * Runs on the tenancy asset and fails when the tenancy has no ACTIVE compartment directly under the root
 * compartment, not counting ManagedCompartmentForPaaS which OCI creates automatically.
 */
@PacmanPolicy(key = "check-oci-tenancy-has-child-compartment", desc = "checks if OCI tenancy has at least one active compartment under the root compartment", severity = PacmanSdkConstants.SEV_HIGH, category = PacmanSdkConstants.OPERATIONS)
public class TenancyChildCompartmentExistsRule extends BasePolicy {
    private static final Logger logger = LoggerFactory.getLogger(TenancyChildCompartmentExistsRule.class);
    private static final String ES_COMPARTMENT_URL = "esIamCompartmentUrl";
    private static final String NAME = "name";
    private static final String MANAGED_PAAS_COMPARTMENT = "ManagedCompartmentForPaaS";

    @Override
    public PolicyResult execute(Map<String, String> ruleParam, Map<String, String> resourceAttributes) {
        logger.debug("========TenancyChildCompartmentExistsRule started=========");
        MDC.put("executionId", ruleParam.get("executionId"));
        MDC.put("ruleId", ruleParam.get(PacmanSdkConstants.POLICY_ID));

        String severity = ruleParam.get(PacmanRuleConstants.SEVERITY);
        String category = ruleParam.get(PacmanRuleConstants.CATEGORY);
        if (Boolean.FALSE.equals(PacmanUtils.doesAllHaveValue(severity, category))) {
            logger.info(PacmanRuleConstants.MISSING_CONFIGURATION);
            throw new InvalidInputException(PacmanRuleConstants.MISSING_CONFIGURATION);
        }

        String tenancyId = OciTenancyAssetUtil.getTenancyId(resourceAttributes);
        if (StringUtils.isNullOrEmpty(tenancyId)) {
            logger.info(PacmanRuleConstants.RESOURCE_DATA_NOT_FOUND);
            return new PolicyResult(PacmanSdkConstants.STATUS_SUCCESS, PacmanRuleConstants.SUCCESS_MESSAGE);
        }

        List<JsonObject> compartments;
        try {
            compartments = OciTenancyAssetUtil.getLatestAssets(ruleParam, ES_COMPARTMENT_URL, tenancyId);
        } catch (InvalidInputException e) {
            throw e;
        } catch (Exception e) {
            logger.error("unable to fetch OCI compartments of tenancy {}", tenancyId, e);
            throw new RuleExecutionFailedExeption("unable to fetch OCI compartments " + e);
        }

        // Pass when there is an ACTIVE compartment whose parent is the root compartment (tenancy OCID)
        int childCompartments = 0;
        for (JsonObject compartment : compartments) {
            if (tenancyId.equals(OciTenancyAssetUtil.getString(compartment, OciTenancyAssetUtil.COMPARTMENT_ID))
                    && OciTenancyAssetUtil.isActive(compartment)
                    && !MANAGED_PAAS_COMPARTMENT.equals(OciTenancyAssetUtil.getString(compartment, NAME))) {
                childCompartments++;
            }
        }

        if (childCompartments == 0) {
            String violationReason = "OCI tenancy has no active compartment under the root compartment to store cloud resources";
            List<LinkedHashMap<String, Object>> issueList = new ArrayList<>();
            LinkedHashMap<String, Object> issue = new LinkedHashMap<>();

            Annotation annotation = Annotation.buildAnnotation(ruleParam, Annotation.Type.ISSUE);
            annotation.put(PacmanSdkConstants.DESCRIPTION, violationReason);
            annotation.put(PacmanRuleConstants.SEVERITY, severity);
            annotation.put(PacmanRuleConstants.CATEGORY, category);
            issue.put(PacmanRuleConstants.VIOLATION_REASON, violationReason);
            issue.put("compartmentsFound", compartments.size());
            issueList.add(issue);
            annotation.put(PacmanRuleConstants.ISSUE_DETAILS, issueList.toString());
            logger.debug("========TenancyChildCompartmentExistsRule ended with an annotation {} : =========", annotation);
            return new PolicyResult(PacmanSdkConstants.STATUS_FAILURE, PacmanRuleConstants.FAILURE_MESSAGE, annotation);
        }

        logger.debug("========TenancyChildCompartmentExistsRule ended=========");
        return new PolicyResult(PacmanSdkConstants.STATUS_SUCCESS, PacmanRuleConstants.SUCCESS_MESSAGE);
    }

    @Override
    public String getHelpText() {
        return "Checks that the OCI tenancy has at least one active compartment under the root compartment";
    }
}
