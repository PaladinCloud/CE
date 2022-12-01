package com.tmobile.cloud.awsrules.credentialreport;

import com.amazonaws.util.StringUtils;
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

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;

@PacmanRule(key = "check-root-account-access-key-usage", desc = "This rule checks for access key usage of root account", severity = PacmanSdkConstants.SEV_HIGH, category = PacmanSdkConstants.SECURITY)
public class CheckRootAccountUsageRule extends BaseRule {

    private static final Logger logger = LoggerFactory.getLogger(CheckRootAccountUsageRule.class);

    private static final String CREDENTIAL_REPORT_URL = "/aws/credentialreport/_search";
    private static final DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ssXXX");

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
        logger.debug("========CheckForRootAccountAccessKeyUsage annotation {} :=========", annotation);
        return new RuleResult(PacmanSdkConstants.STATUS_FAILURE, PacmanRuleConstants.FAILURE_MESSAGE, annotation);
    }

    /**
     * The method will get triggered from Rule Engine with following parameters <br>
     * Following are the Rule Parameters <br>
     * ruleKey :check-root-account-access-key-usage <br>
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
        logger.debug("========CheckForRootAccountAccessKeyUsage started=========");
        MDC.put("executionId", ruleParam.get("executionId"));
        MDC.put("ruleId", ruleParam.get(PacmanSdkConstants.RULE_ID));
        if (MapUtils.isNotEmpty(ruleParam) && !PacmanUtils.doesAllHaveValue(ruleParam.get(PacmanRuleConstants.SEVERITY),
                ruleParam.get(PacmanRuleConstants.CATEGORY), resourceAttributes.get(PacmanRuleConstants.ACCOUNTID))) {
            logger.info(PacmanRuleConstants.MISSING_CONFIGURATION);
            throw new InvalidInputException(PacmanRuleConstants.MISSING_CONFIGURATION);
        }
        Optional<String> opt = Optional.ofNullable(resourceAttributes)
                .map(resource -> checkValidation(ruleParam, resource));
        RuleResult ruleResult = Optional.of(ruleParam).filter(param -> opt.isPresent())
                .map(param -> buildFailureAnnotation(param, opt.get()))
                .orElse(new RuleResult(PacmanSdkConstants.STATUS_SUCCESS, PacmanRuleConstants.SUCCESS_MESSAGE));
        logger.debug("========CheckForRootAccountAccessKeyUsage ended=========");
        return ruleResult;
    }

    @Override
    public String getHelpText() {
        return "Checks the root account access key is being used recently";
    }

    private String checkValidation(Map<String, String> ruleParam, Map<String, String> resource) {
        int maxDays = 30;
        String esCloudTrailPubAccessUrl = null;
        String accountId = resource.get(PacmanRuleConstants.ACCOUNTID);
        String maxDaysString = ruleParam.get(PacmanRuleConstants.MAX_DAYS);
        String pacmanHost = PacmanUtils.getPacmanHost(PacmanRuleConstants.ES_URI);
        if (!StringUtils.isNullOrEmpty(pacmanHost)) {
            esCloudTrailPubAccessUrl = CREDENTIAL_REPORT_URL;
            esCloudTrailPubAccessUrl = pacmanHost + esCloudTrailPubAccessUrl;
        }
        if (!PacmanUtils.doesAllHaveValue(esCloudTrailPubAccessUrl, accountId)) {
            logger.info(PacmanRuleConstants.MISSING_CONFIGURATION);
            throw new InvalidInputException(PacmanRuleConstants.MISSING_CONFIGURATION);
        }
        try {
            if (!Objects.isNull(maxDaysString) && Integer.parseInt(maxDaysString) > 0) {
                maxDays = Integer.parseInt(maxDaysString);
            }
            Map<String, Object> mustFilter = new HashMap<>();
            mustFilter.put(PacmanRuleConstants.ACCOUNTID, accountId);
            mustFilter.put(PacmanRuleConstants.USER, "<root_account>");
            HashMultimap<String, Object> shouldFilter = HashMultimap.create();
            Map<String, Object> mustTermsFilter = new HashMap<>();
            Set<String> passwordLastUsedSet = PacmanUtils.getValueFromElasticSearchAsSet(esCloudTrailPubAccessUrl, mustFilter,
                    shouldFilter, mustTermsFilter, "password_last_used", null);
            Set<String> accessKey1LastUsedDateSet = PacmanUtils.getValueFromElasticSearchAsSet(esCloudTrailPubAccessUrl, mustFilter,
                    shouldFilter, mustTermsFilter, "access_key_1_last_used_date", null);
            Set<String> accessKey2LastUsedDateSet = PacmanUtils.getValueFromElasticSearchAsSet(esCloudTrailPubAccessUrl, mustFilter,
                    shouldFilter, mustTermsFilter, "access_key_2_last_used_date", null);
            if (Objects.isNull(passwordLastUsedSet) || passwordLastUsedSet.isEmpty()
                    || Objects.isNull(accessKey1LastUsedDateSet) || accessKey1LastUsedDateSet.isEmpty()
                    || Objects.isNull(accessKey2LastUsedDateSet) || accessKey2LastUsedDateSet.isEmpty()) {
                return "Unable to retrieve Root account details from ES";
            }
            final String passwordLastUsed = passwordLastUsedSet.iterator().next();
            final String accessKey1LastUsedDate = accessKey1LastUsedDateSet.iterator().next();
            final String accessKey2LastUsedDate = accessKey2LastUsedDateSet.iterator().next();
            if (isRecentlyUsed(passwordLastUsed, maxDays) || isRecentlyUsed(accessKey1LastUsedDate, maxDays)
                    || isRecentlyUsed(accessKey2LastUsedDate, maxDays)) {
                return "Root account is used recently maxDays:" + maxDays
                        + ",passwordLastUsed:" + passwordLastUsed + ",accessKey1LastUsedDate:" + accessKey1LastUsedDate
                        + ",accessKey2LastUsedDate:" + accessKey2LastUsedDate;
            }

        } catch (Exception ex) {
            logger.error("Root account last usage check failed" + ex.getMessage(), ex);
            return "Root account last usage check failed";
        }
        return null;
    }

    private boolean isRecentlyUsed(String date, int maxDays) {
        if (date.equalsIgnoreCase("N/A")) {
            return false;
        }
        try {
            LocalDate lastAllowedDate = LocalDate.now().minusDays(maxDays);
            LocalDate lastUsed = LocalDate.from(dateTimeFormatter.parse(date));
            return lastUsed.isAfter(lastAllowedDate);
        } catch (Exception ex) {
            logger.error("Root account last usage check,error in parsing,returning false" + ex.getMessage(), ex);
            return false;
        }
    }
}
