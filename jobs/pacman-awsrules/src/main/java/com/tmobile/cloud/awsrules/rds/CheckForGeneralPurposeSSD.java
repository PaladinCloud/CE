package com.tmobile.cloud.awsrules.rds;

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
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

@PacmanPolicy(key = "check-general-purpose-ssd-in-rds-instance", desc = "This rule checks if General purpose SSDs are used for RDS Instances", severity = PacmanSdkConstants.SEV_HIGH, category = PacmanSdkConstants.SECURITY)
public class CheckForGeneralPurposeSSD extends BasePolicy {

    private static final Logger logger = LoggerFactory.getLogger(CheckForGeneralPurposeSSD.class);

    private static final String CONFIGURATION_VALUES = " - accountId - %s - storageType - %s";
    private static final List<String> GENERAL_PURPOSE_STORAGE_TYPES = Arrays.asList("io1", "io2");

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
        logger.debug("========CheckForGeneralPurposeSSD annotation {} :=========", annotation);
        return new PolicyResult(PacmanSdkConstants.STATUS_FAILURE, PacmanRuleConstants.FAILURE_MESSAGE, annotation);
    }

    /**
     * The method will get triggered from Rule Engine with following parameters <br>
     * Following are the Rule Parameters <br>
     * ruleKey :check-general-purpose-ssd-in-rds-instance <br>
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
        logger.debug("========CheckForGeneralPurposeSSD started=========");
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
        logger.debug("========CheckForGeneralPurposeSSD ended=========");
        return ruleResult;
    }

    private String checkValidation(Map<String, String> resource) {
        String accountId = resource.get(PacmanRuleConstants.ACCOUNTID);
        String storageType = resource.get(PacmanRuleConstants.STORAGE_TYPE);
        String configValues = String.format(CONFIGURATION_VALUES, accountId, storageType);

        if (Objects.isNull(storageType) || storageType.isEmpty() || Objects.isNull(accountId) || accountId.isEmpty()) {
            String missingConfiguration = PacmanRuleConstants.MISSING_CONFIGURATION + configValues;
            logger.info(missingConfiguration);
            throw new InvalidInputException(missingConfiguration);
        }
        if (GENERAL_PURPOSE_STORAGE_TYPES.contains(storageType)) {
            return "The RDS instances are not using general purpose SSDs- " + configValues;
        }
        logger.debug("========CheckForGeneralPurposeSSD " + configValues);
        return null;
    }

    @Override
    public String getHelpText() {
        return "This rule checks if General purpose SSDs are used for RDS Instances";
    }
}
