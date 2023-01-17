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
                .map(this::checkValidation);
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

    private String checkValidation(Map<String, String> resource) {
        String instanceGenerationType = resource.get(PacmanRuleConstants.INSTANCE_TYPE);
        if (Objects.isNull(instanceGenerationType) || instanceGenerationType.isEmpty()) {
            return "Failed to check the instanceGenerationType " + instanceGenerationType;
        }

        if (Arrays.stream(PreviousGenerationInstanceTypes.values()).anyMatch(type ->
                type.label.equalsIgnoreCase(instanceGenerationType))) {
            return "EC2 instance has a Previous generation " + instanceGenerationType;
        }
        return null;
    }

    private enum PreviousGenerationInstanceTypes {
        M1SMALL("m1.small"),
        M1MEDIUM("m1.medium"),
        M1LARGE("m1.large"),
        M1XLARGE("m1.xlarge"),
        M3MEDIUM("m3.medium"),
        M3LARGE("m3.large"),
        M3XLARGE("m3.xlarge"),
        M32XLARGE("m3.2xlarge"),
        C1MEDIUM("c1.medium"),
        C1XLARGE("c1.xlarge"),
        C28XLARGE("c2.8xlarge"),
        C3LARGE("c3.large"),
        C3XLARGE("c3.xlarge"),
        C32XLARGE("c3.2xlarge"),
        C34XLARGE("c3.4xlarge"),
        C38XLARGE("c3.8xlarge"),
        G22XLARGE("g2.2xlarge*"),
        G28XLARGE("g2.8xlarge**"),
        M2XLARGE("m2.xlarge"),
        M22XLARGE("m2.2xlarge"),
        M24XLARGE("m2.4xlarge"),
        CR18XLARGE("cr1.8xlarge"),
        R3LARGE("r3.large"),
        R3XLARGE("r3.xlarge"),
        R32XLARGE("r3.2xlarge"),
        R34XLARGE("r3.4xlarge"),
        R38XLARGE("r3.8xlarge"),
        I2XLARGE("i2.xlarge"),
        I22XLARGE("i2.2xlarge"),
        I24XLARGE("i2.4xlarge"),
        I28XLARGE("i2.8xlarge"),
        HS18XLARGE("hs1.8xlarge"),
        T1MICRO("t1.micro");

        public final String label;

        PreviousGenerationInstanceTypes(String label) {
            this.label = label;
        }
    }
}
