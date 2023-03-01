package com.tmobile.cloud.awsrules.rds;

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
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;

@PacmanPolicy(key = "check-rds-instance-transport-encryption", desc = "This rule checks if Transport Encryption is enabled for RDS Instances", severity = PacmanSdkConstants.SEV_HIGH, category = PacmanSdkConstants.SECURITY)
public class CheckForRDSTransportEncryption extends BasePolicy {

    private static final Logger logger = LoggerFactory.getLogger(CheckForRDSTransportEncryption.class);
    private static final String CONFIGURATION_VALUES = " - accountId - %s - engine - %s - dbInstanceIdentifier - %s" +
            " - dBParameterGroupName - %s";
    private static final String ERROR_MESSAGE = "unable to find parameter value for parameterName=rds.force_ssl ";
    private static final List<String> APPLICABLE_ENGINES = Arrays.asList("sqlserver-ex", "postgres");
    private static final String RDS_DB_INSTANCE_PARAM_URL = "/aws/rdsdb_parameters/_search";
    private static final String FORCE_SSL_DISABLED_VALUE = "0";
    private static final String PARAMETER_NAME = "rds.force_ssl";

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
        logger.debug("========CheckForRDSTransportEncryption annotation {} :=========", annotation);
        return new PolicyResult(PacmanSdkConstants.STATUS_FAILURE, PacmanRuleConstants.FAILURE_MESSAGE, annotation);
    }

    /**
     * The method will get triggered from Rule Engine with following parameters <br>
     * Following are the Rule Parameters <br>
     * ruleKey :check-rds-instance-transport-encryption <br>
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
        logger.debug("========CheckForRDSTransportEncryption started=========");
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
        logger.debug("========CheckForRDSTransportEncryption ended=========");
        return ruleResult;
    }

    private String checkValidation(Map<String, String> resource) {
        Map<String, Object> mustFilter = new HashMap<>();
        HashMultimap<String, Object> shouldFilter = HashMultimap.create();
        Map<String, Object> mustTermsFilter = new HashMap<>();
        String esRDSInstanceParamPubAccessUrl = null;
        String pacmanHost = PacmanUtils.getPacmanHost(PacmanRuleConstants.ES_URI);
        if (!StringUtils.isNullOrEmpty(pacmanHost)) {
            esRDSInstanceParamPubAccessUrl = RDS_DB_INSTANCE_PARAM_URL;
            esRDSInstanceParamPubAccessUrl = pacmanHost + esRDSInstanceParamPubAccessUrl;
        }
        String accountId = resource.get(PacmanRuleConstants.ACCOUNTID);
        String engine = resource.get(PacmanRuleConstants.ENGINE);
        String dbInstanceIdentifier = resource.get(PacmanRuleConstants.DB_INSTANCE_IDENTIFIER);
        String dBParameterGroupName = resource.get(PacmanRuleConstants.DB_PARAMETER_GROUP_NAME);
        String configValues = String.format(CONFIGURATION_VALUES, accountId, engine, dbInstanceIdentifier,
                dBParameterGroupName);
        if (Objects.isNull(engine) || engine.isEmpty() || Objects.isNull(accountId)
                || accountId.isEmpty() || Objects.isNull(dbInstanceIdentifier) || dbInstanceIdentifier.isEmpty()) {
            String missingConfiguration = PacmanRuleConstants.MISSING_CONFIGURATION + configValues;
            logger.info(missingConfiguration);
            throw new InvalidInputException(missingConfiguration);
        }
        if (!APPLICABLE_ENGINES.contains(engine)) {
            logger.debug("========CheckForRDSTransportEncryption - engine not supported" + configValues + "=========");
            return null;
        }
        mustFilter.put(PacmanRuleConstants.ACCOUNTID, accountId);
        mustFilter.put(PacmanRuleConstants.DB_INSTANCE_IDENTIFIER, dbInstanceIdentifier);
        mustFilter.put(PacmanRuleConstants.DB_PARAMETER_GROUP_NAME, dBParameterGroupName);
        mustFilter.put(PacmanRuleConstants.PARAMETER_NAME, PARAMETER_NAME);
        try {
            Set<String> resultSet = PacmanUtils.getValueFromElasticSearchAsSet(esRDSInstanceParamPubAccessUrl,
                    mustFilter, shouldFilter, mustTermsFilter, "parameterValue", null);
            if (Objects.isNull(resultSet) || resultSet.isEmpty()) {
                return ERROR_MESSAGE + configValues;
            }
            for (String parameterValue : resultSet) {
                if (parameterValue.equalsIgnoreCase(FORCE_SSL_DISABLED_VALUE)) {
                    return PARAMETER_NAME + " parameter is currently disabled - parameterValue " + parameterValue;
                }
            }
        } catch (Exception ex) {
            String returnMessage = "========CheckForRDSTransportEncryption - " + ERROR_MESSAGE +
                    " - " + configValues + "=========";
            logger.debug(returnMessage + ex);
            return returnMessage;
        }
        logger.debug("========CheckForRDSTransportEncryption " + configValues + "=========");
        return null;
    }

    @Override
    public String getHelpText() {
        return "This rule checks if Transport Encryption is enabled for RDS Instance";
    }
}
