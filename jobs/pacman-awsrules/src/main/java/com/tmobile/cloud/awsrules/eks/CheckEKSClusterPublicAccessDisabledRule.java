package com.tmobile.cloud.awsrules.eks;

import com.nimbusds.oauth2.sdk.util.MapUtils;
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
import java.util.Objects;
import java.util.Optional;

@PacmanPolicy(key = "check-eks-cluster-endpoint-public-access-disabled", desc = "This rule checks public access to EKS Cluster endpoint", severity = PacmanSdkConstants.SEV_HIGH, category = PacmanSdkConstants.SECURITY)
public class CheckEKSClusterPublicAccessDisabledRule extends BasePolicy {

    private static final Logger logger = LoggerFactory.getLogger(CheckEKSClusterPublicAccessDisabledRule.class);
    private static final String ACCESS_POINT = "0.0.0.0/0";

    /**
     * The method will get triggered from Rule Engine with following parameters <br>
     * Following are the Rule Parameters <br>
     * ruleKey :check-eks-cluster-endpoint-public-access-disabled <br>
     * threadsafe : if true , rule will be executed on multiple threads <br>
     * severity : Enter the value of severity <br>
     * ruleCategory : Enter the value of category <br>
     *
     * @param ruleParam          the rule param
     * @param resourceAttributes this is a resource in context which needs to be scanned this is provided by execution engine
     * @return ruleResult
     */
    @Override
    public PolicyResult execute(Map<String, String> ruleParam, Map<String, String> resourceAttributes) {
        logger.debug("========CheckForEKSClusterPublicAccessDisabled started=========");
        MDC.put("executionId", ruleParam.get("executionId"));
        MDC.put("ruleId", ruleParam.get(PacmanSdkConstants.POLICY_ID));
        if (MapUtils.isNotEmpty(ruleParam) && !PacmanUtils.doesAllHaveValue(ruleParam.get(PacmanRuleConstants.SEVERITY),
                ruleParam.get(PacmanRuleConstants.CATEGORY), resourceAttributes.get(PacmanRuleConstants.ACCOUNTID))) {
            logger.info(PacmanRuleConstants.MISSING_CONFIGURATION);
            throw new InvalidInputException(PacmanRuleConstants.MISSING_CONFIGURATION);
        }
        Optional<String> opt = Optional.ofNullable(resourceAttributes)
                .map(this::checkValidation);
        PolicyResult ruleResult = Optional.of(ruleParam).filter(param -> opt.isPresent())
                .map(param -> buildFailureAnnotation(param, opt.get()))
                .orElse(new PolicyResult(PacmanSdkConstants.STATUS_SUCCESS, PacmanRuleConstants.SUCCESS_MESSAGE));
        logger.debug("========CheckForEKSClusterPublicAccessDisabled ended=========");
        return ruleResult;
    }

    @Override
    public String getHelpText() {
        return "Checks the EKS Cluster public access is disabled";
    }

    private String checkValidation(Map<String, String> resource) {
        String accountId = resource.get(PacmanRuleConstants.ACCOUNTID);
        String clusterName = resource.get(PacmanRuleConstants.CLUSTER_NAME);
        String endpointPublicAccess = resource.get(PacmanRuleConstants.ENDPOINT_PUBLIC_ACCESS);
        String publicAccessCidrs = resource.get(PacmanRuleConstants.PUBLIC_ACCESS_CIDRS);

        if (!PacmanUtils.doesAllHaveValue(accountId, clusterName, endpointPublicAccess)) {
            logger.info(PacmanRuleConstants.MISSING_CONFIGURATION);
            throw new InvalidInputException(PacmanRuleConstants.MISSING_CONFIGURATION);
        }
        if (endpointPublicAccess.equalsIgnoreCase(Boolean.TRUE.toString()) &&
                !Objects.isNull(publicAccessCidrs) && publicAccessCidrs.contains(ACCESS_POINT)) {
            return "Public access is enabled for EKS Cluster: " + clusterName + ",accountId: " + accountId;
        }
        return null;
    }

    private static PolicyResult buildFailureAnnotation(final Map<String, String> ruleParam, String description) {
        LinkedHashMap<String, Object> issue = new LinkedHashMap<>();
        List<LinkedHashMap<String, Object>> issueList = new ArrayList<>();
        Annotation annotation = Annotation.buildAnnotation(ruleParam, Annotation.Type.ISSUE);
        annotation.put(PacmanSdkConstants.DESCRIPTION, description);
        annotation.put(PacmanRuleConstants.SEVERITY, ruleParam.get(PacmanRuleConstants.SEVERITY));
        annotation.put(PacmanRuleConstants.CATEGORY, ruleParam.get(PacmanRuleConstants.CATEGORY));
        annotation.put(PacmanRuleConstants.RESOURCE_ID, ruleParam.get(PacmanRuleConstants.RESOURCE_ID));
        issue.put(PacmanRuleConstants.VIOLATION_REASON, description);
        issueList.add(issue);
        annotation.put("issueDetails", issueList.toString());
        logger.debug("========CheckForEKSClusterPublicAccessDisabled annotation {} :=========", annotation);
        return new PolicyResult(PacmanSdkConstants.STATUS_FAILURE, PacmanRuleConstants.FAILURE_MESSAGE, annotation);
    }
}
