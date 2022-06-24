package com.tmobile.cloud.awsrules.appflow;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;

import com.amazonaws.util.StringUtils;
import com.tmobile.cloud.awsrules.dms.DmsEncryptionUsingKMSCMKsRule;
import com.tmobile.cloud.awsrules.utils.PacmanUtils;
import com.tmobile.cloud.constants.PacmanRuleConstants;
import com.tmobile.pacman.commons.PacmanSdkConstants;
import com.tmobile.pacman.commons.exception.InvalidInputException;
import com.tmobile.pacman.commons.exception.RuleExecutionFailedExeption;
import com.tmobile.pacman.commons.rule.Annotation;
import com.tmobile.pacman.commons.rule.BaseRule;
import com.tmobile.pacman.commons.rule.PacmanRule;
import com.tmobile.pacman.commons.rule.RuleResult;

@PacmanRule(key = "check-for-app-flow-encrypted-with-cmks", desc = "checks for aws appflow flowas are encrypted using CMKs", severity = PacmanSdkConstants.SEV_HIGH, category = PacmanSdkConstants.SECURITY)
public class AppFlowShouldBeEncryptedWithCMK extends BaseRule {

	private static final Logger logger = LoggerFactory.getLogger(DmsEncryptionUsingKMSCMKsRule.class);
	
	public static final String DEFAULT_KEY_MANAGER = "AWS";
    
	/**
	 * The method will get triggered from Rule Engine with following parameters
	 * 
	 * @param ruleParam
	 * 
	 ************** Following are the Rule Parameters********* <br><br>
	 * 
	 * ruleKey : check-for-app-flow-encrypted-with-cmks <br><br>
	 *
	 * threadsafe : if true , rule will be executed on multiple threads <br><br>
	 *
	 * esKmsUrl : Enter the kms es api url <br><br>
	 *
	 * severity : Enter the value of severity <br><br>
	 * 
	 * ruleCategory : Enter the value of category <br><br>
	 * 
	 * @param resourceAttributes this is a resource in context which needs to be scanned this is provided by execution engine
	 *
	 */

	public RuleResult execute(final Map<String, String> ruleParam, Map<String, String> resourceAttributes) {

		logger.debug("========AppFlowShouldBeEncryptedWithCMK started=========");
		
		MDC.put("executionId", ruleParam.get("executionId"));
		MDC.put("ruleId", ruleParam.get(PacmanSdkConstants.RULE_ID));

		String formattedKmsUrl = PacmanUtils.formatUrl(ruleParam, PacmanRuleConstants.ES_KMS_URL);
		String esKmsUrl = !StringUtils.isNullOrEmpty(formattedKmsUrl)?formattedKmsUrl:"";

		Optional.ofNullable(ruleParam)
			.filter(param -> (!PacmanUtils.doesAllHaveValue(param.get(PacmanRuleConstants.SEVERITY), param.get(PacmanRuleConstants.CATEGORY), esKmsUrl)))
			.map(param -> {logger.info(PacmanRuleConstants.MISSING_CONFIGURATION);
				throw new InvalidInputException(PacmanRuleConstants.MISSING_CONFIGURATION);
			});
		RuleResult ruleResult = Optional.ofNullable(resourceAttributes)
				.filter(resource -> !checkValidation(esKmsUrl, resourceAttributes))
				.map(resource -> buildFailureAnnotation(ruleParam))
				.orElse(new RuleResult(PacmanSdkConstants.STATUS_SUCCESS, PacmanRuleConstants.SUCCESS_MESSAGE));
		logger.debug("========AppFlowShouldBeEncryptedWithCMK ended=========");
		return ruleResult;
		
	}


	private boolean checkValidation(final String esKmsUrl, Map<String, String> resourceAttributes) {

		try {
			return PacmanUtils.checkIfResourceEncryptedWithKmsCmks(resourceAttributes.get(PacmanRuleConstants.ES_KMS_ARN_ATTRIBUTE), esKmsUrl, DEFAULT_KEY_MANAGER);
		} catch (Exception e) {
			logger.error("unable to determine", e);
			throw new RuleExecutionFailedExeption("unable to determine" + e);
		}
	}
	
	
	private static RuleResult buildFailureAnnotation(final Map<String, String> ruleParam) {
		
		Annotation annotation = null;
		LinkedHashMap<String, Object> issue = new LinkedHashMap<>();
		List<LinkedHashMap<String, Object>> issueList = new ArrayList<>();

		annotation = Annotation.buildAnnotation(ruleParam,Annotation.Type.ISSUE);
		annotation.put(PacmanSdkConstants.DESCRIPTION,"Aws Appflow is encrypted with AWS managed keys instead of Customer Master Keys !!");
		annotation.put(PacmanRuleConstants.SEVERITY, ruleParam.get(PacmanRuleConstants.SEVERITY));
		annotation.put(PacmanRuleConstants.SUBTYPE, Annotation.Type.RECOMMENDATION.toString());
		annotation.put(PacmanRuleConstants.CATEGORY, ruleParam.get(PacmanRuleConstants.CATEGORY));
		annotation.put(PacmanRuleConstants.RESOURCE_ID, ruleParam.get(PacmanRuleConstants.RESOURCE_ID));
		issue.put(PacmanRuleConstants.VIOLATION_REASON, "Aws Appflow is encrypted with AWS managed keys instead of Customer Master Keys !!");
		issueList.add(issue);
		annotation.put("issueDetails",issueList.toString());
		logger.debug("========AppFlowShouldBeEncryptedWithCMK annotation {} :=========",annotation);
		return new RuleResult(PacmanSdkConstants.STATUS_FAILURE,PacmanRuleConstants.FAILURE_MESSAGE, annotation);
	
	
	}

	public String getHelpText() {
		return "This rule checks for aws appflow flowas are encrypted using CMKs";
	}
	
}
