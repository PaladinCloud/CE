package com.tmobile.cloud.awsrules.ecs;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;

import com.tmobile.cloud.awsrules.utils.PacmanUtils;
import com.tmobile.cloud.constants.PacmanRuleConstants;
import com.tmobile.pacman.commons.PacmanSdkConstants;
import com.tmobile.pacman.commons.exception.InvalidInputException;
import com.tmobile.pacman.commons.rule.Annotation;
import com.tmobile.pacman.commons.rule.BaseRule;
import com.tmobile.pacman.commons.rule.PacmanRule;
import com.tmobile.pacman.commons.rule.RuleResult;

@PacmanRule(key = "check-for-ecs-task-log-driver-in-use", desc = "checks for log driver is configured for the containers in ECS task definitions", severity = PacmanSdkConstants.SEV_HIGH, category = PacmanSdkConstants.SECURITY)
public class ECSTaskLogDriverInUseRule extends BaseRule {

	private static final Logger logger = LoggerFactory.getLogger(ECSTaskLogDriverInUseRule.class);
	
	private static final String LOG_DRIVER_NONE = "None";
	
	/**
	 * The method will get triggered from Rule Engine with following parameters
	 * 
	 * @param ruleParam
	 * 
	 ************** Following are the Rule Parameters********* <br><br>
	 * 
	 * ruleKey : check-for-ecs-task-log-driver-in-use <br><br>
	 *
	 * threadsafe : if true , rule will be executed on multiple threads <br><br>
	 *
	 * severity : Enter the value of severity <br><br>
	 * 
	 * ruleCategory : Enter the value of category <br><br>
	 * 
	 * @param resourceAttributes this is a resource in context which needs to be scanned this is provided by execution engine
	 *
	 */

	public RuleResult execute(final Map<String, String> ruleParam, Map<String, String> resourceAttributes) {

		logger.debug("========ECSTaskLogDriverInUseRule started=========");
		
		String severity = ruleParam.get(PacmanRuleConstants.SEVERITY);
		String category = ruleParam.get(PacmanRuleConstants.CATEGORY);

		MDC.put("executionId", ruleParam.get("executionId"));
		MDC.put("ruleId", ruleParam.get(PacmanSdkConstants.RULE_ID));

		Optional.ofNullable(ruleParam)
			.filter(param -> (!PacmanUtils.doesAllHaveValue(severity, category)))
			.map(param -> {logger.info(PacmanRuleConstants.MISSING_CONFIGURATION);
				throw new InvalidInputException(PacmanRuleConstants.MISSING_CONFIGURATION);
			});
		RuleResult ruleResult = Optional.ofNullable(resourceAttributes)
				.filter(resource -> LOG_DRIVER_NONE.equalsIgnoreCase(resource.get(PacmanRuleConstants.ES_LOG_DRIVER_ATTRIBUTE)))
				.map(resource -> buildFailureAnnotation(ruleParam, severity, category))
				.orElse(new RuleResult(PacmanSdkConstants.STATUS_SUCCESS, PacmanRuleConstants.SUCCESS_MESSAGE));
		
		logger.debug("========ECSTaskLogDriverInUseRule ended=========");
		return ruleResult;
		
	}
	
	private static RuleResult buildFailureAnnotation(final Map<String, String> ruleParam, String severity, String category) {
		
		Annotation annotation = null;
		LinkedHashMap<String, Object> issue = new LinkedHashMap<>();
		List<LinkedHashMap<String, Object>> issueList = new ArrayList<>();

		annotation = Annotation.buildAnnotation(ruleParam,Annotation.Type.ISSUE);
		annotation.put(PacmanSdkConstants.DESCRIPTION,"Aws ECS task definitions should be configured with a log driver");
		annotation.put(PacmanRuleConstants.SEVERITY, severity);
		annotation.put(PacmanRuleConstants.CATEGORY, category);
		annotation.put(PacmanRuleConstants.RESOURCE_ID, ruleParam.get(PacmanRuleConstants.RESOURCE_ID));
		issue.put(PacmanRuleConstants.VIOLATION_REASON, "Aws ECS task definitions should be configured with a log driver");
		issueList.add(issue);
		annotation.put("issueDetails",issueList.toString());
		logger.debug("========ECSTaskLogDriverInUseRule annotation {} :=========",annotation);
		return new RuleResult(PacmanSdkConstants.STATUS_FAILURE,PacmanRuleConstants.FAILURE_MESSAGE, annotation);
	
	
	}

	public String getHelpText() {
		return "This rule checks for log driver is configured for the containers in ECS task definitions";
	}
	
}
