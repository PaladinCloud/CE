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
import java.util.Optional;

@PacmanPolicy(key = "check-eks-cluster-version-update", desc = "This rule checks EKS cluster has latest version", severity = PacmanSdkConstants.SEV_HIGH, category = PacmanSdkConstants.SECURITY)
public class CheckEKSClusterVersionIsUpdatedRule extends BasePolicy {

    private static final Logger logger = LoggerFactory.getLogger(CheckEKSClusterVersionIsUpdatedRule.class);

    /**
     * The method will get triggered from Rule Engine with following parameters <br>
     * Following are the Rule Parameters <br>
     * ruleKey :check-eks-cluster-version-updated <br>
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
        logger.debug("========CheckForEKSClusterVersionUpdate started=========");
        MDC.put("executionId", ruleParam.get("executionId"));
        MDC.put("ruleId", ruleParam.get(PacmanSdkConstants.POLICY_ID));
        if (MapUtils.isNotEmpty(ruleParam) && !PacmanUtils.doesAllHaveValue(ruleParam.get(PacmanRuleConstants.SEVERITY),
                ruleParam.get(PacmanRuleConstants.CATEGORY), resourceAttributes.get(PacmanRuleConstants.ACCOUNTID))) {
            logger.info(PacmanRuleConstants.MISSING_CONFIGURATION);
            throw new InvalidInputException(PacmanRuleConstants.MISSING_CONFIGURATION);
        }
        Optional<String> opt = Optional.ofNullable(resourceAttributes)
                .map(resource -> checkValidation(ruleParam, resource));
        PolicyResult ruleResult = Optional.of(ruleParam).filter(param -> opt.isPresent())
                .map(param -> buildFailureAnnotation(param, opt.get()))
                .orElse(new PolicyResult(PacmanSdkConstants.STATUS_SUCCESS, PacmanRuleConstants.SUCCESS_MESSAGE));
        logger.debug("========CheckForEKSClusterVersionUpdate ended=========");
        return ruleResult;
    }

    @Override
    public String getHelpText() {
        return "Checks the EKS Cluster version is updated to latest or not";
    }

    private String checkValidation(Map<String, String> ruleParam, Map<String, String> resource) {
        String accountId = resource.get(PacmanRuleConstants.ACCOUNTID);
        String clusterName = resource.get(PacmanRuleConstants.CLUSTER_NAME);
        String currentClusterVersion = resource.get(PacmanRuleConstants.VERSION);
        String latestClusterVersion = ruleParam.get(PacmanRuleConstants.LATEST_VERSION);

        if (!PacmanUtils.doesAllHaveValue(accountId, clusterName, currentClusterVersion, latestClusterVersion)) {
            logger.info(PacmanRuleConstants.MISSING_CONFIGURATION);
            throw new InvalidInputException(PacmanRuleConstants.MISSING_CONFIGURATION);
        }

        if (compareVersions(currentClusterVersion, latestClusterVersion) < 0) {
            return "Update your EKS cluster version for EKS Cluster: " + clusterName + ",accountId: " + accountId
                    + ",currentClusterVersion" + currentClusterVersion + ",latestClusterVersion" + latestClusterVersion;
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
        logger.debug("========CheckForEKSClusterVersionUpdate annotation {} :=========", annotation);
        return new PolicyResult(PacmanSdkConstants.STATUS_FAILURE, PacmanRuleConstants.FAILURE_MESSAGE, annotation);
    }

    private static int compareVersions(String version1, String version2) {
        int comparisonResult = 0;

        String[] version1Splits = version1.split("\\.");
        String[] version2Splits = version2.split("\\.");
        int maxLengthOfVersionSplits = Math.max(version1Splits.length, version2Splits.length);

        for (int i = 0; i < maxLengthOfVersionSplits; i++) {
            Integer v1 = i < version1Splits.length ? Integer.parseInt(version1Splits[i]) : 0;
            Integer v2 = i < version2Splits.length ? Integer.parseInt(version2Splits[i]) : 0;
            int compare = v1.compareTo(v2);
            if (compare != 0) {
                comparisonResult = compare;
                break;
            }
        }
        return comparisonResult;
    }
}
