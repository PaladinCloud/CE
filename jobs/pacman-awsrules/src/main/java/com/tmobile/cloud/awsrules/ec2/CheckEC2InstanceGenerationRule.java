package com.tmobile.cloud.awsrules.ec2;

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

@PacmanPolicy(key = "check-ec2-instance-generation", desc = "This rule checks ec2 instance generation", severity = PacmanSdkConstants.SEV_HIGH, category = PacmanSdkConstants.SECURITY)
public class CheckEC2InstanceGenerationRule extends BasePolicy {

    private static final Logger logger = LoggerFactory.getLogger(CheckEC2InstanceGenerationRule.class);

    private static final String RETURN_STATEMENT = "This is a Previous generation EC2 instance %s and it can be " +
            "upgraded to %s";

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
        logger.debug("========CheckEC2InstanceGeneration annotation {} :=========", annotation);
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
        logger.debug("========CheckEC2InstanceGeneration started=========");
        MDC.put("executionId", ruleParam.get("executionId"));
        MDC.put("ruleId", ruleParam.get(PacmanSdkConstants.POLICY_ID));
        if (MapUtils.isNotEmpty(ruleParam) && !PacmanUtils.doesAllHaveValue(ruleParam.get(PacmanRuleConstants.SEVERITY),
                ruleParam.get(PacmanRuleConstants.CATEGORY), resourceAttributes.get(PacmanRuleConstants.ACCOUNTID))) {
            logger.info(PacmanRuleConstants.MISSING_CONFIGURATION);
            throw new InvalidInputException(PacmanRuleConstants.MISSING_CONFIGURATION);
        }
        Optional<String> opt = Optional.ofNullable(resourceAttributes)
                .map(resource -> checkValidation(resource, ruleParam));
        PolicyResult ruleResult = Optional.of(ruleParam).filter(param -> opt.isPresent())
                .map(param -> buildFailureAnnotation(param, opt.get()))
                .orElse(new PolicyResult(PacmanSdkConstants.STATUS_SUCCESS, PacmanRuleConstants.SUCCESS_MESSAGE));
        logger.debug("========CheckEC2InstanceGeneration ended=========");
        return ruleResult;
    }

    @Override
    public String getHelpText() {
        return "Checks for EC2 Instance generation is latest or not";
    }

    private String checkValidation(Map<String, String> resource, Map<String, String> ruleParam) {
        String accountId = resource.get(PacmanRuleConstants.ACCOUNTID);
        String instanceGenerationType = resource.get(PacmanRuleConstants.INSTANCE_TYPE);
        String oldVersions = ruleParam.get(PacmanRuleConstants.OLD_VERSIONS);
        if (Objects.isNull(instanceGenerationType) || instanceGenerationType.isEmpty() ||
                Objects.isNull(oldVersions) || oldVersions.isEmpty() ||
                Objects.isNull(accountId) || accountId.isEmpty()) {
            return "Failed to check the instanceGenerationType " + instanceGenerationType + " with olderVersions "
                    + oldVersions + " for accountId " + accountId;
        }

        if (Arrays.stream(oldVersions.split(",")).anyMatch(type ->
                type.equalsIgnoreCase(instanceGenerationType))) {
            switch (instanceGenerationType.split("\\.")[0].toUpperCase()) {
                case "T1":
                    return String.format(RETURN_STATEMENT, instanceGenerationType, "T2");
                case "M1":
                case "M3":
                    return String.format(RETURN_STATEMENT, instanceGenerationType, "M5");
                case "C1":
                case "C3":
                    return String.format(RETURN_STATEMENT, instanceGenerationType, "C5");
                case "I2":
                    return String.format(RETURN_STATEMENT, instanceGenerationType, "I3");
                case "M2":
                case "CR1":
                case "R3":
                    return String.format(RETURN_STATEMENT, instanceGenerationType, "R4");
                case "HS1":
                    return String.format(RETURN_STATEMENT, instanceGenerationType, "D2");
                case "G2":
                    return String.format(RETURN_STATEMENT, instanceGenerationType, "G3");

                default:
                    return "EC2 instance has a Previous generation " + instanceGenerationType;
            }
        }
        return null;
    }
}
