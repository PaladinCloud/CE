package com.tmobile.cloud.awsrules.asg;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;

import com.amazonaws.util.CollectionUtils;
import com.amazonaws.util.StringUtils;
import com.tmobile.cloud.awsrules.utils.PacmanUtils;
import com.tmobile.cloud.constants.PacmanRuleConstants;
import com.tmobile.pacman.commons.PacmanSdkConstants;
import com.tmobile.pacman.commons.exception.InvalidInputException;
import com.tmobile.pacman.commons.exception.RuleExecutionFailedExeption;
import com.tmobile.pacman.commons.rule.Annotation;
import com.tmobile.pacman.commons.rule.BaseRule;
import com.tmobile.pacman.commons.rule.PacmanRule;
import com.tmobile.pacman.commons.rule.RuleResult;

@PacmanRule(key = "check-for-asg-referencing-missing-ami", desc = "checks for ASG launch configuration referencing missing AMI", severity = PacmanSdkConstants.SEV_HIGH, category = PacmanSdkConstants.SECURITY)
public class AutoScalingGroupsWithMissingAMI extends BaseRule {

	private static final Logger logger = LoggerFactory.getLogger(AutoScalingGroupsWithMissingAMI.class);
	
	/**
	 * The method will get triggered from Rule Engine with following parameters
	 * 
	 * @param ruleParam
	 * 
	 ************** Following are the Rule Parameters********* <br><br>
	 * 
	 * ruleKey : check-for-asg-referencing-missing-ami <br><br>
	 * 
	 * esAmiUrl : Enter the es ami URL<br><br>
	 * 
	 * esAsgLcURL : Enter the es asg launch configuration URL<br><br>
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

		logger.debug("========AutoScalingGroupsWithMissingAMI started=========");

		MDC.put("executionId", ruleParam.get("executionId"));
		MDC.put("ruleId", ruleParam.get(PacmanSdkConstants.RULE_ID));

		Optional.ofNullable(ruleParam)
				.filter(param -> (!PacmanUtils.doesAllHaveValue(param.get(PacmanRuleConstants.SEVERITY),
						param.get(PacmanRuleConstants.CATEGORY),
						param.get(PacmanRuleConstants.ES_ASG_LC_URL),
						param.get(PacmanRuleConstants.ES_AMI_URL))))
				.map(param -> {
					logger.info(PacmanRuleConstants.MISSING_CONFIGURATION);
					throw new InvalidInputException(PacmanRuleConstants.MISSING_CONFIGURATION);
				});
		
		Optional<String> opt = Optional.ofNullable(resourceAttributes)
				.map(resource -> checkValidation(ruleParam, resource));
		
		RuleResult ruleResult = Optional.ofNullable(ruleParam)
				.filter(param -> opt.isPresent())
				.map(param -> buildFailureAnnotation(param, opt.get()))
				.orElse(new RuleResult(PacmanSdkConstants.STATUS_SUCCESS, PacmanRuleConstants.SUCCESS_MESSAGE));


		logger.debug("========AutoScalingGroupsWithMissingAMI ended=========");
		return ruleResult;

	}
	
	private String checkValidation(Map<String, String> ruleParam, Map<String, String> resourceAttributes) {
		
		String description = null;
		Set<String> imageSet = null;
		String formattedAmiUrl = PacmanUtils.formatUrl(ruleParam, PacmanRuleConstants.ES_AMI_URL);
		String esAmiUrl = !StringUtils.isNullOrEmpty(formattedAmiUrl) ? formattedAmiUrl : "";
		
		String formattedAsgConfigUrl = PacmanUtils.formatUrl(ruleParam, PacmanRuleConstants.ES_ASG_LC_URL);
		String esAsgLcUrl = !StringUtils.isNullOrEmpty(formattedAsgConfigUrl) ? formattedAsgConfigUrl : "";
		try {
			List<String> images = PacmanUtils.getImagesByAsgArn(resourceAttributes.get(PacmanRuleConstants.ES_ASG_ARN_ATTRIBUTE), esAsgLcUrl);
			if (!CollectionUtils.isNullOrEmpty(images)) {
				imageSet = new HashSet<>();
				imageSet.addAll(images);
				Set<String> missingImages = PacmanUtils.getMissingAMIs(imageSet, esAmiUrl);
				if (!CollectionUtils.isNullOrEmpty(missingImages))
					description = "Missing AMIs found in ASG launch configuration -  " + String.join(",", missingImages);
			}
		} catch (Exception e) {
			logger.error("unable to determine", e);
			throw new RuleExecutionFailedExeption("unable to determine" + e);
		}

		return description;
	}
	
	
	private static RuleResult buildFailureAnnotation(final Map<String, String> ruleParam, String description) {
		
		Annotation annotation = null;
		LinkedHashMap<String, Object> issue = new LinkedHashMap<>();
		List<LinkedHashMap<String, Object>> issueList = new ArrayList<>();

		annotation = Annotation.buildAnnotation(ruleParam,Annotation.Type.ISSUE);
		annotation.put(PacmanSdkConstants.DESCRIPTION,description);
		annotation.put(PacmanRuleConstants.SEVERITY, ruleParam.get(PacmanRuleConstants.SEVERITY));
		annotation.put(PacmanRuleConstants.CATEGORY, ruleParam.get(PacmanRuleConstants.CATEGORY));
		annotation.put(PacmanRuleConstants.RESOURCE_ID, ruleParam.get(PacmanRuleConstants.RESOURCE_ID));
		issue.put(PacmanRuleConstants.VIOLATION_REASON, "ASG launch configuration with missing AMIs found.");
		issueList.add(issue);
		annotation.put("issueDetails",issueList.toString());
		logger.debug("========AutoScalingGroupsWithMissingAMI annotation {} :=========",annotation);
		return new RuleResult(PacmanSdkConstants.STATUS_FAILURE,PacmanRuleConstants.FAILURE_MESSAGE, annotation);
	
	
	}

	public String getHelpText() {
		return "This rule checks for ASG launch configuration referencing missing AMI";
	}
	
}
