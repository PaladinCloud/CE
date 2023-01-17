package com.tmobile.cloud.awsrules.ami;

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

@PacmanPolicy(key = "check-unused-ami", desc = "This rule checks unused AMIs", severity = PacmanSdkConstants.SEV_HIGH, category = PacmanSdkConstants.SECURITY)
public class CheckUnusedAMIRule extends BasePolicy {

    private static final Logger logger = LoggerFactory.getLogger(CheckUnusedAMIRule.class);

    private static final String EC2_URL = "/aws/ec2/_search";

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
        logger.debug("========CheckForUnusedAMIs annotation {} :=========", annotation);
        return new PolicyResult(PacmanSdkConstants.STATUS_FAILURE, PacmanRuleConstants.FAILURE_MESSAGE, annotation);
    }

    /**
     * The method will get triggered from Rule Engine with following parameters <br>
     * Following are the Rule Parameters <br>
     * ruleKey :check-unused-ami <br>
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
        logger.debug("========CheckForUnusedAMIs started=========");
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
        logger.debug("========CheckForUnusedAMIs ended=========");
        return ruleResult;
    }

    @Override
    public String getHelpText() {
        return "Checks for any unused AMIs";
    }

    private String checkValidation(Map<String, String> resource) {

        String esCloudTrailPubAccessUrl = null;
        String pacmanHost = PacmanUtils.getPacmanHost(PacmanRuleConstants.ES_URI);
        String accountId = resource.get(PacmanRuleConstants.ACCOUNTID);
        String imageId = resource.get(PacmanRuleConstants.RESOURCE_ID);
        String publicValue = resource.get(PacmanRuleConstants.PUBLIC_VALUE);

        if (!StringUtils.isNullOrEmpty(pacmanHost)) {
            esCloudTrailPubAccessUrl = EC2_URL;
            esCloudTrailPubAccessUrl = pacmanHost + esCloudTrailPubAccessUrl;
        }

        if (!PacmanUtils.doesAllHaveValue(esCloudTrailPubAccessUrl, accountId, imageId, publicValue)) {
            logger.info(PacmanRuleConstants.MISSING_CONFIGURATION);
            throw new InvalidInputException(PacmanRuleConstants.MISSING_CONFIGURATION);
        }
        if (publicValue.equalsIgnoreCase(Boolean.TRUE.toString())) {
            return null;
        }
        if (!publicValue.equalsIgnoreCase(Boolean.FALSE.toString())) {
            return "Invalid value for publicValue" + publicValue + " for accountId " + accountId + " imageId" + imageId;
        }

        Map<String, Object> mustFilter = new HashMap<>();
        mustFilter.put(PacmanRuleConstants.ES_IMAGE_ID_ATTRIBUTE, imageId);
        HashMultimap<String, Object> shouldFilter = HashMultimap.create();
        Map<String, Object> mustTermsFilter = new HashMap<>();
        String description = "Unused AMI found,imageId:" + imageId + ",accountId:" + accountId;
        try {
            Set<String> resultSet = PacmanUtils.getValueFromElasticSearchAsSet(esCloudTrailPubAccessUrl, mustFilter,
                    shouldFilter, mustTermsFilter, "_resourceid", null);

            if (Objects.isNull(resultSet) || resultSet.isEmpty()) {
                return description;
            }
        } catch (Exception ex) {
            logger.error(description + ex.getMessage(), ex);
            return description;
        }
        return null;
    }
}
