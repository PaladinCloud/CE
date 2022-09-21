package com.tmobile.cloud.awsrules.s3;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;

import com.amazonaws.auth.policy.Policy;
import com.amazonaws.auth.policy.Statement;
import com.amazonaws.auth.policy.Statement.Effect;
import com.amazonaws.util.CollectionUtils;
import com.amazonaws.util.StringUtils;
import com.nimbusds.oauth2.sdk.util.MapUtils;
import com.tmobile.cloud.awsrules.utils.PacmanUtils;
import com.tmobile.cloud.constants.PacmanRuleConstants;
import com.tmobile.pacman.commons.PacmanSdkConstants;
import com.tmobile.pacman.commons.exception.InvalidInputException;
import com.tmobile.pacman.commons.rule.Annotation;
import com.tmobile.pacman.commons.rule.BaseRule;
import com.tmobile.pacman.commons.rule.PacmanRule;
import com.tmobile.pacman.commons.rule.RuleResult;

@PacmanRule(key = "check-for-s3-bucket-policy-denies-http", desc = "checks S3 has policy which denies HTTP requests", severity = PacmanSdkConstants.SEV_HIGH, category = PacmanSdkConstants.SECURITY)
public class CheckHTTPDeniedRule extends BaseRule {

	private static final Logger logger = LoggerFactory.getLogger(CheckHTTPDeniedRule.class);

	/**
	 * The method will get triggered from Rule Engine with following parameters
	 * 
	 * @param ruleParam
	 * 
	 ************** Following are the Rule Parameters********* <br><br>
	 *
	 * ruleKey : check-for-s3-bucket-policy-denies-http <br><br>
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

		logger.debug("========CheckHTTPDeniedRule started=========");

		MDC.put("executionId", ruleParam.get("executionId"));
		MDC.put("ruleId", ruleParam.get(PacmanSdkConstants.RULE_ID));

		if (MapUtils.isNotEmpty(ruleParam) && !PacmanUtils.doesAllHaveValue(ruleParam.get(PacmanRuleConstants.SEVERITY),
				ruleParam.get(PacmanRuleConstants.CATEGORY))) {
			logger.info(PacmanRuleConstants.MISSING_CONFIGURATION);
			throw new InvalidInputException(PacmanRuleConstants.MISSING_CONFIGURATION);
		}

		Optional<String> opt = Optional.ofNullable(resourceAttributes)
				.map(resource -> checkValidation(ruleParam, resource));

		RuleResult ruleResult = Optional.ofNullable(ruleParam).filter(param -> opt.isPresent())
				.map(param -> buildFailureAnnotation(param, opt.get()))
				.orElse(new RuleResult(PacmanSdkConstants.STATUS_SUCCESS, PacmanRuleConstants.SUCCESS_MESSAGE));

		logger.debug("========CheckHTTPDeniedRule ended=========");
		return ruleResult;

	}
    
	/**
	 * @param ruleParam
	 * @param resourceAttributes
	 * @return
	 * 
	 * Check the ES attribute bucketpolicy. Check the policy for Effect : Deny and
	 * Condition { aws:SecureTransport : false }
	 * 
	 */
	private String checkValidation(Map<String, String> ruleParam, Map<String, String> resourceAttributes) {

		String description = null;
		String bucketPolicy = resourceAttributes.get(PacmanRuleConstants.ES_BKT_POLICY_ATTRIBUTE);

		if (!StringUtils.isNullOrEmpty(bucketPolicy)) {
			Policy policy = Policy.fromJson(bucketPolicy);
			if (!isHTTPDenied(policy))
				description = "S3 bucket without a policy to deny HTTP requests found !!";
		} else
			description = "S3 bucket without a policy to deny HTTP requests found !!";
		return description;
	}

	private boolean isHTTPDenied(Policy policy) {
		for (Statement statement : policy.getStatements()) {

			if (statement.getEffect().equals(Effect.Deny)) {
				if (!CollectionUtils.isNullOrEmpty(statement.getConditions())) {
					if (statement.getConditions().stream()
							.filter(condition -> condition.getConditionKey().equalsIgnoreCase("aws:SecureTransport")
									&& condition.getValues().contains("false"))
							.count() > 0)
						return true;
				}
			}

		}
		return false;
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
		logger.debug("========CheckHTTPDeniedRule annotation {} :=========",annotation);
		return new RuleResult(PacmanSdkConstants.STATUS_FAILURE,PacmanRuleConstants.FAILURE_MESSAGE, annotation);
	
	
	}
	
	@Override
	public String getHelpText() {
		return "Checks S3 has policy which denies HTTP requests";
	}

}
