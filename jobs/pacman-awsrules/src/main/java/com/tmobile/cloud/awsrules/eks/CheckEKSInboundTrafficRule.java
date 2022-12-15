package com.tmobile.cloud.awsrules.eks;

import com.amazonaws.util.StringUtils;
import com.google.common.collect.HashMultimap;
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
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;

@PacmanPolicy(key = "check-eks-inbound-traffic", desc = "This rule checks EKS inbound traffic disabled or not", severity = PacmanSdkConstants.SEV_HIGH, category = PacmanSdkConstants.SECURITY)
public class CheckEKSInboundTrafficRule extends BasePolicy {

    private static final Logger logger = LoggerFactory.getLogger(CheckEKSInboundTrafficRule.class);

    private static final String SG_RULES_URL = "/aws/sg_rules/_search";
    private static final String PORT = "443";
    private static final String TYPE = "inbound";

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
        logger.debug("========CheckForEKSSecurityGroupInboundTraffic annotation {} :=========", annotation);
        return new PolicyResult(PacmanSdkConstants.STATUS_FAILURE, PacmanRuleConstants.FAILURE_MESSAGE, annotation);
    }

    /**
     * The method will get triggered from Rule Engine with following parameters <br>
     * Following are the Rule Parameters <br>
     * ruleKey :check-eks-inbound-traffic <br>
     * threadsafe : if true , rule will be executed on multiple threads <br>
     * severity : Enter the value of severity <br>
     * ruleCategory : Enter the value of category <br>
     *
     * @param ruleParam          the rule param
     * @param resourceAttributes this is a resource in context which needs to be scanned this is provided by execution engine
     * @return PolicyResult
     */
    @Override
    public PolicyResult execute(Map<String, String> ruleParam, Map<String, String> resourceAttributes) {
        logger.debug("========CheckForEKSSecurityGroupInboundTraffic started=========");
        MDC.put("executionId", ruleParam.get("executionId"));
        MDC.put("policyId", ruleParam.get(PacmanSdkConstants.POLICY_ID));
        if (MapUtils.isNotEmpty(ruleParam) && !PacmanUtils.doesAllHaveValue(ruleParam.get(PacmanRuleConstants.SEVERITY),
                ruleParam.get(PacmanRuleConstants.CATEGORY), resourceAttributes.get(PacmanRuleConstants.ACCOUNTID))) {
            logger.info(PacmanRuleConstants.MISSING_CONFIGURATION);
            throw new InvalidInputException(PacmanRuleConstants.MISSING_CONFIGURATION);
        }
        Optional<String> opt = Optional.ofNullable(resourceAttributes)
                .map(this::checkValidation);
        PolicyResult PolicyResult = Optional.of(ruleParam).filter(param -> opt.isPresent())
                .map(param -> buildFailureAnnotation(param, opt.get()))
                .orElse(new PolicyResult(PacmanSdkConstants.STATUS_SUCCESS, PacmanRuleConstants.SUCCESS_MESSAGE));
        logger.debug("========CheckForEKSSecurityGroupInboundTraffic ended=========");
        return PolicyResult;
    }

    @Override
    public String getHelpText() {
        return "Checks the EKS Inbound traffic disabled or not";
    }

    private String checkValidation(Map<String, String> resource) {

        String esSgRulesPubAccessUrl = null;
        String pacmanHost = PacmanUtils.getPacmanHost(PacmanRuleConstants.ES_URI);
        String accountId = resource.get(PacmanRuleConstants.ACCOUNTID);
        String clusterName = resource.get(PacmanRuleConstants.CLUSTER_NAME);
        String clusterSecurityGroupId = resource.get(PacmanRuleConstants.CLUSTER_SECURITY_GROUP_ID);

        if (!StringUtils.isNullOrEmpty(pacmanHost)) {
            esSgRulesPubAccessUrl = SG_RULES_URL;
            esSgRulesPubAccessUrl = pacmanHost + esSgRulesPubAccessUrl;
        }

        if (Objects.isNull(clusterSecurityGroupId)) {
            return "Eks cluster:" + clusterName + " with security groupId null found";
        }

        if (!PacmanUtils.doesAllHaveValue(accountId, clusterName, esSgRulesPubAccessUrl)) {
            logger.info(PacmanRuleConstants.MISSING_CONFIGURATION);
            throw new InvalidInputException(PacmanRuleConstants.MISSING_CONFIGURATION);
        }

        Map<String, Object> mustFilter = new HashMap<>();
        mustFilter.put(PacmanRuleConstants.ACCOUNTID, accountId);
        mustFilter.put(PacmanRuleConstants.GROUP_ID, clusterSecurityGroupId);
        mustFilter.put(PacmanRuleConstants.TYPE, TYPE);
        HashMultimap<String, Object> shouldFilter = HashMultimap.create();
        Map<String, Object> mustTermsFilter = new HashMap<>();

        try {
            Set<String> fromPortSet = PacmanUtils.getValueFromElasticSearchAsSet(esSgRulesPubAccessUrl, mustFilter,
                    shouldFilter, mustTermsFilter, PacmanRuleConstants.ES_SG_FROM_PORT_ATTRIBUTE, null);

            Set<String> toPortSet = PacmanUtils.getValueFromElasticSearchAsSet(esSgRulesPubAccessUrl, mustFilter,
                    shouldFilter, mustTermsFilter, PacmanRuleConstants.ES_SG_TO_PORT_ATTRIBUTE, null);

            if (Objects.isNull(fromPortSet) || Objects.isNull(toPortSet)
                    || (toPortSet.isEmpty() && fromPortSet.isEmpty())) {
                return "Inbound rules for Eks cluster:" + clusterName + " with security groupId:" + clusterSecurityGroupId
                        + " for accountId: " + accountId + " doesn't exists";
            }

            for (String port : fromPortSet) {
                if (!port.equalsIgnoreCase(PORT)) {
                    return "Found non compliant fromPort:" + port + " for Eks cluster:" + clusterName
                            + " with security groupId:" + clusterSecurityGroupId
                            + " for accountId: " + accountId;
                }
            }

            for (String port : toPortSet) {
                if (!port.equalsIgnoreCase(PORT)) {
                    return "Found non compliant toPort:" + port + " for Eks cluster:" + clusterName
                            + " with security groupId:" + clusterSecurityGroupId
                            + " for accountId: " + accountId;
                }
            }
        } catch (Exception ex) {
            logger.error("Inbound rules for Eks cluster:" + clusterName + " with security groupId:" + clusterSecurityGroupId
                    + " for accountId: " + accountId + " not found" + ex.getMessage(), ex);
            return "Inbound rules for Eks cluster:" + clusterName + " with security groupId:" + clusterSecurityGroupId
                    + " for accountId: " + accountId + " not found";
        }
        return null;
    }
}
