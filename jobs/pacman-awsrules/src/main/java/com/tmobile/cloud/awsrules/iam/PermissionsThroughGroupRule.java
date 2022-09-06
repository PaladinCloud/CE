package com.tmobile.cloud.awsrules.iam;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;

import com.amazonaws.services.identitymanagement.AmazonIdentityManagementClient;
import com.tmobile.cloud.awsrules.utils.IAMUtils;
import com.tmobile.cloud.awsrules.utils.PacmanUtils;
import com.tmobile.cloud.constants.PacmanRuleConstants;
import com.tmobile.pacman.commons.AWSService;
import com.tmobile.pacman.commons.PacmanSdkConstants;
import com.tmobile.pacman.commons.exception.InvalidInputException;
import com.tmobile.pacman.commons.exception.RuleExecutionFailedExeption;
import com.tmobile.pacman.commons.exception.UnableToCreateClientException;
import com.tmobile.pacman.commons.rule.Annotation;
import com.tmobile.pacman.commons.rule.BaseRule;
import com.tmobile.pacman.commons.rule.PacmanRule;
import com.tmobile.pacman.commons.rule.RuleResult;

@PacmanRule(key = "iam-permissions-through-group", desc = "Check if an IAM user gets permission only through group", severity = PacmanSdkConstants.SEV_HIGH, category = PacmanSdkConstants.SECURITY)
public class PermissionsThroughGroupRule extends BaseRule {

	private static final Logger logger = LoggerFactory.getLogger(PermissionsThroughGroupRule.class);
	private static final String USER_NAME = "username";

	/**
	 * The method will get triggered from Rule Engine with following parameters
	 * 
	 * @param ruleParam
	 * 
	 ************** Following are the Rule Parameters********* <br><br>
	 * 
	 * ruleKey : iam-permissions-through-group <br><br>
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

	@Override
	public RuleResult execute(final Map<String, String> ruleParam, Map<String, String> resourceAttributes) {

		logger.debug("========PermissionsThroughGroupRule started=========");

		MDC.put("executionId", ruleParam.get("executionId"));
		MDC.put("ruleId", ruleParam.get(PacmanSdkConstants.RULE_ID));
		
		Optional.ofNullable(ruleParam)
				.filter(param -> (!PacmanUtils.doesAllHaveValue(param.get(PacmanRuleConstants.SEVERITY),
						param.get(PacmanRuleConstants.CATEGORY), param.get(PacmanSdkConstants.Role_IDENTIFYING_STRING))))
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


		logger.debug("========PermissionsThroughGroupRule ended=========");
		return ruleResult;

	}
    
	/**
	 * @param ruleParam
	 * @param resourceAttributes
	 * @return
	 * 
	 * Validate IAM user has attached or inline policies.
	 * 
	 */
	private String checkValidation(Map<String, String> ruleParam, Map<String, String> resourceAttributes) {

		String description = null;
		Map<String, Object> map = null;
		AmazonIdentityManagementClient iamClient = null;
		Map<String, String> ruleParamforIAM = new HashMap<>();

		ruleParamforIAM.putAll(ruleParam);
		ruleParamforIAM.put("region", "us-east-1");

		String userName = resourceAttributes.get(USER_NAME);

		try {
			map = getClientFor(AWSService.IAM, ruleParamforIAM.get(PacmanSdkConstants.Role_IDENTIFYING_STRING), ruleParamforIAM);
			iamClient = (AmazonIdentityManagementClient) map.get(PacmanSdkConstants.CLIENT);
			
			if(IAMUtils.isUserHasInvalidPermission(userName,iamClient)) {
				return description = "IAM user has permissions without group!!";
			}

		} catch (UnableToCreateClientException e) {
			logger.error("unable to get client for following input", e);
			throw new InvalidInputException(e.toString());
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
		issue.put(PacmanRuleConstants.VIOLATION_REASON, description);
		issueList.add(issue);
		annotation.put("issueDetails",issueList.toString());
		logger.debug("========PermissionsThroughGroupRule annotation {} :=========",annotation);
		return new RuleResult(PacmanSdkConstants.STATUS_FAILURE,PacmanRuleConstants.FAILURE_MESSAGE, annotation);
	
	
	}
	
	@Override
	public String getHelpText() {
		return "Check if an IAM user gets permission only through group";
	}

}
