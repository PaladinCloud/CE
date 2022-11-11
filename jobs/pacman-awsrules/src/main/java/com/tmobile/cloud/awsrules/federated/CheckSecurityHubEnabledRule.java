package com.tmobile.cloud.awsrules.federated;

import com.google.common.collect.HashMultimap;
import com.nimbusds.oauth2.sdk.util.MapUtils;
import com.tmobile.cloud.awsrules.utils.PacmanUtils;
import com.tmobile.cloud.constants.PacmanRuleConstants;
import com.tmobile.pacman.commons.PacmanSdkConstants;
import com.tmobile.pacman.commons.exception.InvalidInputException;
import com.tmobile.pacman.commons.rule.Annotation;
import com.tmobile.pacman.commons.rule.BaseRule;
import com.tmobile.pacman.commons.rule.PacmanRule;
import com.tmobile.pacman.commons.rule.RuleResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;

import java.util.*;

@PacmanRule(key = "check-security-hub-exists", desc = "This rule checks if security hub enabled for account", severity = PacmanSdkConstants.SEV_MEDIUM, category = PacmanSdkConstants.SECURITY)
public class CheckSecurityHubEnabledRule extends BaseRule {

    private static final Logger logger = LoggerFactory.getLogger(CheckSecurityHubEnabledRule.class);
    private static final String SECURITY_HUB_URL = "/aws/securityhub/_search";

    /**
     * The method will get triggered from Rule Engine with following parameters <br>
     * Following are the Rule Parameters <br>
     * ruleKey :check-cloudwatch-alarm-exists <br>
     * threadsafe : if true , rule will be executed on multiple threads <br>
     * severity : Enter the value of severity <br>
     * ruleCategory : Enter the value of category <br>
     *
     * @param ruleParam          the rule param
     * @param resourceAttributes this is a resource in context which needs to be scanned this is provided by execution engine
     * @return ruleResult
     */
    @Override
    public RuleResult execute(Map<String, String> ruleParam, Map<String, String> resourceAttributes) {
        logger.debug("========CheckForSecurityHubEnabled started=========");
        MDC.put("executionId", ruleParam.get("executionId"));
        MDC.put("ruleId", ruleParam.get(PacmanSdkConstants.RULE_ID));
        if (MapUtils.isNotEmpty(ruleParam) && !PacmanUtils.doesAllHaveValue(ruleParam.get(PacmanRuleConstants.SEVERITY),
                ruleParam.get(PacmanRuleConstants.CATEGORY))) {
            logger.info(PacmanRuleConstants.MISSING_CONFIGURATION);
            throw new InvalidInputException(PacmanRuleConstants.MISSING_CONFIGURATION);
        }
        Optional<String> opt = Optional.ofNullable(resourceAttributes)
                .map(resource -> checkValidation(ruleParam, resource));
        RuleResult ruleResult = Optional.of(ruleParam).filter(param -> opt.isPresent())
                .map(param -> buildFailureAnnotation(param, opt.get()))
                .orElse(new RuleResult(PacmanSdkConstants.STATUS_SUCCESS, PacmanRuleConstants.SUCCESS_MESSAGE));
        logger.debug("========CheckForSecurityHubEnabled ended=========");
        return ruleResult;
    }

    private String checkValidation(Map<String, String> ruleParam, Map<String, String> resource) {
        String description = null;
        String accountId = resource.get(PacmanRuleConstants.ACCOUNTID);
        String pacmanHost = PacmanUtils.getPacmanHost(PacmanRuleConstants.ES_URI);
        String regionParam = ruleParam.get(PacmanRuleConstants.REGIONS);

        if (!PacmanUtils.doesAllHaveValue(pacmanHost, accountId, regionParam)) {
            logger.info(PacmanRuleConstants.MISSING_CONFIGURATION);
            throw new InvalidInputException(PacmanRuleConstants.MISSING_CONFIGURATION);
        }
        try {
            List<String> regions = Arrays.asList((regionParam).split(","));
            String esEndPoint = pacmanHost + SECURITY_HUB_URL;
            Map<String, Object> mustFilter = new HashMap<>();
            HashMultimap<String, Object> shouldFilter = HashMultimap.create();
            Map<String, Object> mustTermsFilter = new HashMap<>();
            mustFilter.put(PacmanRuleConstants.ACCOUNTID, accountId);
            for (String region : regions) {
                mustFilter.put(PacmanRuleConstants.REGION, region);
                Set<String> resultSet = PacmanUtils.getValueFromElasticSearchAsSet(esEndPoint, mustFilter,
                        shouldFilter, mustTermsFilter, "_resourceid", null);
                if (Objects.isNull(resultSet) || resultSet.isEmpty()) {
                    return "AWS Security Hub is not enabled in region: " + region + " from the list of regions"
                            + regions + ", accountId: " + accountId;
                }
            }
        } catch (Exception ex) {
            logger.error("AWS Security Hub is not enabled" + ex.getMessage(), ex);
            description = "AWS Security Hub is not enabled";
        }
        return description;
    }

    @Override
    public String getHelpText() {
        return "Checks the AWS SecurityHub enabled";
    }

    private static RuleResult buildFailureAnnotation(final Map<String, String> ruleParam, String description) {
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
        logger.debug("========CheckForSecurityHubEnabled annotation {} :=========", annotation);
        return new RuleResult(PacmanSdkConstants.STATUS_FAILURE, PacmanRuleConstants.FAILURE_MESSAGE, annotation);
    }
}
