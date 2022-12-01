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

import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;

@PacmanRule(key = "check-user-account-access-key-exists-during-initial-setup", desc = "This rule checks for any access key of user account during initial setup", severity = PacmanSdkConstants.SEV_HIGH, category = PacmanSdkConstants.SECURITY)
public class CheckUserAccessKeysExistsDuringInitialSetupRule extends BaseRule {

    private static final Logger logger = LoggerFactory.getLogger(CheckUserAccessKeysExistsDuringInitialSetupRule.class);

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
        logger.debug("========CheckForUserAccountAccessExistsDuringInitialSetup annotation {} :=========", annotation);
        return new RuleResult(PacmanSdkConstants.STATUS_FAILURE, PacmanRuleConstants.FAILURE_MESSAGE, annotation);
    }

    /**
     * The method will get triggered from Rule Engine with following parameters <br>
     * Following are the Rule Parameters <br>
     * ruleKey :check-user-account-access-key-exists-during-initial-setup <br>
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
        logger.debug("========CheckForUserAccountAccessExistsDuringInitialSetup started=========");
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
        logger.debug("========CheckForUserAccountAccessExistsDuringInitialSetup ended=========");
        return ruleResult;
    }

    @Override
    public String getHelpText() {
        return "Checks the user account access key exists during initial setup";
    }

    private String checkValidation(Map<String, String> ruleParam, Map<String, String> resource) {
        String esCloudTrailPubAccessUrl = null;
        String accountId = resource.get(PacmanRuleConstants.ACCOUNTID);
        String user = resource.get(PacmanRuleConstants.RESOURCE_ID);
        String pacmanHost = PacmanUtils.getPacmanHost(PacmanRuleConstants.ES_URI);
        if (!StringUtils.isNullOrEmpty(pacmanHost)) {
            esCloudTrailPubAccessUrl = CREDENTIAL_REPORT_URL;
            esCloudTrailPubAccessUrl = pacmanHost + esCloudTrailPubAccessUrl;
        }
        if (!PacmanUtils.doesAllHaveValue(esCloudTrailPubAccessUrl, accountId, user)) {
            logger.info(PacmanRuleConstants.MISSING_CONFIGURATION);
            throw new InvalidInputException(PacmanRuleConstants.MISSING_CONFIGURATION);
        }
        try {
            Map<String, Object> mustFilter = new HashMap<>();
            mustFilter.put(PacmanRuleConstants.ACCOUNTID, accountId);
            mustFilter.put(PacmanRuleConstants.USER, user);
            HashMultimap<String, Object> shouldFilter = HashMultimap.create();
            Map<String, Object> mustTermsFilter = new HashMap<>();
            Set<String> passwordEnabledSet = PacmanUtils.getValueFromElasticSearchAsSet(esCloudTrailPubAccessUrl, mustFilter,
                    shouldFilter, mustTermsFilter, "password_enabled", null);
            Set<String> accessKey1LastUsedDateSet = PacmanUtils.getValueFromElasticSearchAsSet(esCloudTrailPubAccessUrl, mustFilter,
                    shouldFilter, mustTermsFilter, "access_key_1_last_used_date", null);
            Set<String> accessKey2LastUsedDateSet = PacmanUtils.getValueFromElasticSearchAsSet(esCloudTrailPubAccessUrl, mustFilter,
                    shouldFilter, mustTermsFilter, "access_key_2_last_used_date", null);
            Set<String> accessKey1ActiveSet = PacmanUtils.getValueFromElasticSearchAsSet(esCloudTrailPubAccessUrl, mustFilter,
                    shouldFilter, mustTermsFilter, "access_key_1_active", null);
            Set<String> accessKey2ActiveSet = PacmanUtils.getValueFromElasticSearchAsSet(esCloudTrailPubAccessUrl, mustFilter,
                    shouldFilter, mustTermsFilter, "access_key_2_active", null);
            if (Objects.isNull(passwordEnabledSet) || passwordEnabledSet.isEmpty()
                    || Objects.isNull(accessKey1LastUsedDateSet) || accessKey1LastUsedDateSet.isEmpty()
                    || Objects.isNull(accessKey2LastUsedDateSet) || accessKey2LastUsedDateSet.isEmpty()
                    || Objects.isNull(accessKey1ActiveSet) || accessKey1ActiveSet.isEmpty()
                    || Objects.isNull(accessKey2ActiveSet) || accessKey2ActiveSet.isEmpty()) {
                return "Unable to retrieve user account details from ES,user:" + user;
            }
            final String passwordEnabled = passwordEnabledSet.iterator().next();
            final String accessKey1LastUsedDate = accessKey1LastUsedDateSet.iterator().next();
            final String accessKey2LastUsedDate = accessKey2LastUsedDateSet.iterator().next();
            final String accessKey1Active = accessKey1ActiveSet.iterator().next();
            final String accessKey2Active = accessKey2ActiveSet.iterator().next();
            if (passwordEnabled.equalsIgnoreCase(Boolean.TRUE.toString())
                    && ((accessKey1LastUsedDate.equalsIgnoreCase("N/A")
                    && accessKey1Active.equalsIgnoreCase(Boolean.TRUE.toString()))
                    || (accessKey2LastUsedDate.equalsIgnoreCase("N/A"))
                    && accessKey2Active.equalsIgnoreCase(Boolean.TRUE.toString()))) {
                return "The user account access key exists during initial setup,passwordEnabled:" + passwordEnabled
                        + ",accessKey1LastUsedDate:" + accessKey1LastUsedDate + ",accessKey1ActiveSet:" + accessKey1Active
                        + ",accessKey2LastUsedDate:" + accessKey2LastUsedDate + ",accessKey2ActiveSet:" + accessKey2Active
                        + ",user:" + user;
            }

        } catch (Exception ex) {
            logger.error("The user account access key exists during initial setup check failed" + ex.getMessage(), ex);
            return "The user account access key exists during initial setup";
        }
        return null;
    }
}
