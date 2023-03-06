package com.tmobile.cloud.awsrules.route53;

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
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@PacmanPolicy(key = "check-route53-hosted-zone-missing-eip", desc = "This rule checks DNS entry that points to missing EIP for Hosted zones in route53", severity = PacmanSdkConstants.SEV_HIGH, category = PacmanSdkConstants.SECURITY)
public class CheckRoute53HostedZoneMissingEIP extends BasePolicy {

    private static final Logger logger = LoggerFactory.getLogger(CheckRoute53HostedZoneMissingEIP.class);

    private static final String HOSTED_ZONE_URL = "/aws/route53_hostedzone/_search";
    private static final String RESOURCE_RECORD_URL = "/aws/route53_resourcerecord/_search";
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
        logger.debug("========CheckForAmazonRoute53HostedZoneMissingEIP annotation {} :=========", annotation);
        return new PolicyResult(PacmanSdkConstants.STATUS_FAILURE, PacmanRuleConstants.FAILURE_MESSAGE, annotation);
    }

    /**
     * The method will get triggered from Rule Engine with following parameters <br>
     * Following are the Rule Parameters <br>
     * ruleKey :check-route53-hosted-zone-missing-eip <br>
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
        logger.debug("========CheckForAmazonRoute53HostedZoneMissingEIP started=========");
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
        logger.debug("========CheckForAmazonRoute53HostedZoneMissingEIP ended=========");
        return ruleResult;
    }

    @Override
    public String getHelpText() {
        return "Checks DNS entry that points to missing EIP for Hosted Zones in Route53";
    }

    private String checkValidation(Map<String, String> resource) {

        String accountId = resource.get(PacmanRuleConstants.ACCOUNTID);
        String domainName = resource.get(PacmanRuleConstants.DOMAIN_NAME);
        String esRoute53PubAccessUrl = null;
        String pacmanHost = PacmanUtils.getPacmanHost(PacmanRuleConstants.ES_URI);
        logger.debug("========pacmanHost {}  =========", pacmanHost);

        if (!StringUtils.isNullOrEmpty(pacmanHost)) {
            esRoute53PubAccessUrl = HOSTED_ZONE_URL;
            esRoute53PubAccessUrl = pacmanHost + esRoute53PubAccessUrl;
        }

        if (!PacmanUtils.doesAllHaveValue(accountId, domainName)) {
            logger.info(PacmanRuleConstants.MISSING_CONFIGURATION);
            return "Invalid values for accountId:" + accountId + " and domainName:" + domainName;
        }

        try {
            Map<String, Object> mustFilter = new HashMap<>();
            mustFilter.put(PacmanRuleConstants.ACCOUNTID, accountId);
            mustFilter.put(PacmanRuleConstants.DOMAIN_NAME, domainName);
            HashMultimap<String, Object> shouldFilter = HashMultimap.create();
            Map<String, Object> mustTermsFilter = new HashMap<>();

            Set<String> resultSet = PacmanUtils.getValueFromElasticSearchAsSet(esRoute53PubAccessUrl, mustFilter,
                    shouldFilter, mustTermsFilter, "hostedzoneid", null);

            if (Objects.isNull(resultSet) || resultSet.isEmpty()) {
                return "Hosted zone for domainName: " + domainName + " not found for accountId: " + accountId;
            }
            esRoute53PubAccessUrl = pacmanHost + RESOURCE_RECORD_URL;
            Set<String> resourceRecordSet = new HashSet<>();
            mustFilter.put(PacmanRuleConstants.TYPE, "A");
            for (String hostedZoneId : resultSet) {
                if (Objects.isNull(hostedZoneId) || (hostedZoneId.length() < 1)) {
                    return "Invalid hostedZoneId: " + hostedZoneId + " for domainName: " + domainName +
                            " accountId : " + accountId;
                }
                mustFilter.put(PacmanRuleConstants.HOSTED_ZONE_ID, hostedZoneId);
                try {
                    resourceRecordSet.addAll(Objects.requireNonNull(PacmanUtils
                            .getValueFromElasticSearchAsSet(esRoute53PubAccessUrl, mustFilter,
                                    shouldFilter, mustTermsFilter, "resourceRecords", null)));
                } catch (Exception ex) {
                    logger.error("resourceRecordSet not found for Hosted Zone :" + hostedZoneId + " in Route53 for " +
                            "domainName: " + domainName + " accountId : " + accountId + ex.getMessage(), ex);
                    return "resourceRecordSet not found for Hosted Zone :" + hostedZoneId + " in Route53 for " +
                            "domainName: " + domainName + " accountId : " + accountId;
                }
            }
            if (resourceRecordSet.isEmpty()) {
                return "resourceRecordsSet is empty for domainName: " + domainName;
            }
            List<String> ipValues = new ArrayList<>();
            for (String value : resourceRecordSet) {
                ipValues.addAll(Arrays.asList(value.split(",")));
            }
            ipValues = ipValues.stream().filter(v -> !StringUtils.isNullOrEmpty(v)).collect(Collectors.toList());
            if (ipValues.isEmpty()) {
                return "IPs in Resource record set A is empty for Hosted Zones in Route53 for domainName: " +
                        domainName + " accountId : " + accountId;
            }
            esRoute53PubAccessUrl = pacmanHost + EC2_URL;
            mustFilter = new HashMap<>();
            mustFilter.put(PacmanRuleConstants.ACCOUNTID, accountId);
            for (String ip : ipValues) {
                mustFilter.put(PacmanRuleConstants.PUBLIC_IP_ADDR, ip);
                Set<String> ec2Set = PacmanUtils.getValueFromElasticSearchAsSet(esRoute53PubAccessUrl, mustFilter,
                        shouldFilter, mustTermsFilter, "publicipaddress", null);
                if (Objects.isNull(ec2Set) || ec2Set.isEmpty()) {
                    return "DNS entry is pointing to a missing EIP " + ip + " for Hosted Zones in Route53 for domainName: " +
                            domainName + " accountId : " + accountId;
                }
            }
        } catch (Exception ex) {
            logger.error("SPF is not enabled for Hosted Zone in Route53 for domainName: " +
                    domainName + " accountId : " + accountId + ex.getMessage(), ex);
            return "SPF is not enabled for Hosted Zone in Route53 for domainName: " + domainName +
                    " accountId : " + accountId;
        }
        return null;
    }
}
