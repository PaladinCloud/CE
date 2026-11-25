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
 * CIS OCI Foundations Benchmark v3.1.1 - 4.1 Ensure default tags are used on resources.
 * Runs on the tenancy asset and fails when the root compartment has no ACTIVE tag default with the value
 * ${iam.principal.name}, which records who created each resource.
 */
@PacmanPolicy(key = "check-oci-tenancy-default-tag-principal-name", desc = "checks if OCI tenancy has an active tag default with value ${iam.principal.name} in the root compartment", severity = PacmanSdkConstants.SEV_HIGH, category = PacmanSdkConstants.GOVERNANCE)
public class TenancyDefaultTagExistsRule extends BasePolicy {
    private static final Logger logger = LoggerFactory.getLogger(TenancyDefaultTagExistsRule.class);
    private static final String ES_TAG_DEFAULT_URL = "esTagDefaultUrl";
    private static final String VALUE = "value";
    private static final String PRINCIPAL_NAME_VARIABLE = "${iam.principal.name}";

    @Override
    public PolicyResult execute(Map<String, String> ruleParam, Map<String, String> resourceAttributes) {
        logger.debug("========TenancyDefaultTagExistsRule started=========");
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

        List<JsonObject> tagDefaults;
        try {
            tagDefaults = OciTenancyAssetUtil.getLatestAssets(ruleParam, ES_TAG_DEFAULT_URL, tenancyId);
        } catch (InvalidInputException e) {
            throw e;
        } catch (Exception e) {
            logger.error("unable to fetch OCI tag defaults of tenancy {}", tenancyId, e);
            throw new RuleExecutionFailedExeption("unable to fetch OCI tag defaults " + e);
        }

        // Pass when an ACTIVE tag default in the root compartment (tenancy OCID) has the value ${iam.principal.name}
        boolean found = false;
        for (JsonObject tagDefault : tagDefaults) {
            if (tenancyId.equals(OciTenancyAssetUtil.getString(tagDefault, OciTenancyAssetUtil.COMPARTMENT_ID))
                    && OciTenancyAssetUtil.isActive(tagDefault)
                    && PRINCIPAL_NAME_VARIABLE.equals(OciTenancyAssetUtil.getString(tagDefault, VALUE))) {
                found = true;
                break;
            }
        }

        if (!found) {
            String violationReason = "OCI tenancy has no active tag default with value " + PRINCIPAL_NAME_VARIABLE + " in the root compartment";
            List<LinkedHashMap<String, Object>> issueList = new ArrayList<>();
            LinkedHashMap<String, Object> issue = new LinkedHashMap<>();

            Annotation annotation = Annotation.buildAnnotation(ruleParam, Annotation.Type.ISSUE);
            annotation.put(PacmanSdkConstants.DESCRIPTION, violationReason);
            annotation.put(PacmanRuleConstants.SEVERITY, severity);
            annotation.put(PacmanRuleConstants.CATEGORY, category);
            issue.put(PacmanRuleConstants.VIOLATION_REASON, violationReason);
            issue.put("tagDefaultsFound", tagDefaults.size());
            issueList.add(issue);
            annotation.put(PacmanRuleConstants.ISSUE_DETAILS, issueList.toString());
            logger.debug("========TenancyDefaultTagExistsRule ended with an annotation {} : =========", annotation);
            return new PolicyResult(PacmanSdkConstants.STATUS_FAILURE, PacmanRuleConstants.FAILURE_MESSAGE, annotation);
        }

        logger.debug("========TenancyDefaultTagExistsRule ended=========");
        return new PolicyResult(PacmanSdkConstants.STATUS_SUCCESS, PacmanRuleConstants.SUCCESS_MESSAGE);
    }

    @Override
    public String getHelpText() {
        return "Checks that the OCI tenancy has an active tag default with value ${iam.principal.name} in the root compartment";
    }
}
