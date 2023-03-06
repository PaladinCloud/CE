package com.tmobile.cloud.awsrules.route53;

import com.nimbusds.oauth2.sdk.util.MapUtils;
import com.tmobile.cloud.awsrules.utils.PacmanUtils;
import com.tmobile.cloud.constants.PacmanRuleConstants;
import com.tmobile.pacman.commons.PacmanSdkConstants;
import com.tmobile.pacman.commons.exception.InvalidInputException;
import com.tmobile.pacman.commons.policy.Annotation;
import com.tmobile.pacman.commons.policy.BasePolicy;
import com.tmobile.pacman.commons.policy.PacmanPolicy;
import com.tmobile.pacman.commons.policy.PolicyResult;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

@PacmanPolicy(key = "check-route53-domain-renewal", desc = "This rule checks domain names about to expire", severity = PacmanSdkConstants.SEV_HIGH, category = PacmanSdkConstants.SECURITY)
public class CheckRoute53DomainNameRenewal extends BasePolicy {

    private static final Logger logger = LoggerFactory.getLogger(CheckRoute53DomainNameRenewal.class);

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
        logger.debug("========CheckForAmazonRoute53DomainNamesAboutToExpire annotation {} :=========", annotation);
        return new PolicyResult(PacmanSdkConstants.STATUS_FAILURE, PacmanRuleConstants.FAILURE_MESSAGE, annotation);
    }

    /**
     * The method will get triggered from Rule Engine with following parameters <br>
     * Following are the Rule Parameters <br>
     * ruleKey :check-route53-domain-renewal <br>
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
        logger.debug("========CheckForAmazonRoute53DomainNamesAboutToExpire started=========");
        MDC.put("executionId", ruleParam.get("executionId"));
        MDC.put("ruleId", ruleParam.get(PacmanSdkConstants.POLICY_ID));
        if (MapUtils.isNotEmpty(ruleParam) && !PacmanUtils.doesAllHaveValue(ruleParam.get(PacmanRuleConstants.SEVERITY),
                ruleParam.get(PacmanRuleConstants.CATEGORY), resourceAttributes.get(PacmanRuleConstants.ACCOUNTID))) {
            logger.info(PacmanRuleConstants.MISSING_CONFIGURATION);
            throw new InvalidInputException(PacmanRuleConstants.MISSING_CONFIGURATION);
        }
        Optional<String> opt = Optional.ofNullable(resourceAttributes)
                .map(resource -> checkValidation(ruleParam, resource));
        PolicyResult policyResult = Optional.of(ruleParam).filter(param -> opt.isPresent())
                .map(param -> buildFailureAnnotation(param, opt.get()))
                .orElse(new PolicyResult(PacmanSdkConstants.STATUS_SUCCESS, PacmanRuleConstants.SUCCESS_MESSAGE));
        logger.debug("========CheckForAmazonRoute53DomainNamesAboutToExpire ended=========");
        return policyResult;
    }

    @Override
    public String getHelpText() {
        return "Checks for any domain names expired in Amazon Route53";
    }

    private String checkValidation(Map<String, String> ruleParam, Map<String, String> resource) {

        int maxDays = 30;
        String accountId = resource.get(PacmanRuleConstants.ACCOUNTID);
        String expiryDate = resource.get(PacmanRuleConstants.EXPIRATION_DATE);
        String maxDaysString = ruleParam.get(PacmanRuleConstants.MAX_DAYS);
        String domainName = resource.get(PacmanRuleConstants.DOMAIN_NAME);

        if (!PacmanUtils.doesAllHaveValue(accountId, expiryDate, domainName)) {
            logger.info("Invalid values for accountId:" + accountId + " and expiryDate:" + expiryDate + " and domainName:" +
                    domainName);
            return "Invalid values for accountId:" + accountId + " and expiryDate:" + expiryDate + " and domainName:" +
                    domainName;
        }
        if (Objects.isNull(maxDaysString) || StringUtils.isEmpty(maxDaysString)) {
            logger.info("using default maxDays value:" + maxDays);
        } else {
            try {
                maxDays = Integer.parseInt(maxDaysString);
            } catch (Exception ex) {
                logger.info("Unable to parse param maxDaysString:" + maxDaysString);
                return "Unable to parse param maxDaysString:" + maxDaysString;
            }
        }

        if (isExpired(expiryDate, maxDays)) {
            return "Renewal of Domain:" + domainName + " is near to expiry date " +
                    "for accountId:" + accountId + " and expiryDate:" + expiryDate;
        }

        return null;
    }

    private boolean isExpired(String date, int maxDays) {
        try {
            LocalDate lastAllowedDate = LocalDate.now().plusDays(maxDays);
            LocalDate expiryDate = LocalDate.from(PacmanRuleConstants.DATE_TIME_FORMATTER.parse(date));
            return lastAllowedDate.isAfter(expiryDate);
        } catch (Exception ex) {
            logger.error("Route53 renewal check,error in parsing,returning false" + ex.getMessage(), ex);
            return false;
        }
    }
}
