package com.tmobile.cloud.awsrules.federated;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;

import com.tmobile.cloud.awsrules.utils.PacmanUtils;
import com.tmobile.cloud.constants.PacmanRuleConstants;
import com.tmobile.pacman.commons.PacmanSdkConstants;
import com.tmobile.pacman.commons.exception.InvalidInputException;
import com.tmobile.pacman.commons.exception.RuleExecutionFailedExeption;
import com.tmobile.pacman.commons.rule.Annotation;
import com.tmobile.pacman.commons.rule.BaseRule;
import com.tmobile.pacman.commons.rule.PacmanRule;
import com.tmobile.pacman.commons.rule.RuleResult;

@PacmanRule(key = "check-for-expired-iam-certificate", desc = "This Rule should look for expired SSL(IAM) certificates", severity = PacmanSdkConstants.SEV_HIGH, category = PacmanSdkConstants.GOVERNANCE)
public class CheckIAMCertificateExpired extends BaseRule{



	private static final Logger logger = LoggerFactory.getLogger(CheckIAMCertificateExpired.class);

	/**
	 * The method will get triggered from Rule Engine with following parameters
	 *
	 * @param ruleParam
	 *
	 **************Following are the Rule Parameters********* <br><br>
	 *
	 * ruleKey : check-for-expired-iam-certificate <br><br>
	 *
	 * severity : Enter the value of severity <br><br>
	 *
	 * ruleCategory : Enter the value of category <br><br>
	 *
	 * @param resourceAttributes this is a resource in context which needs to be scanned this is provided by execution engine
	 *
	 */

	public RuleResult execute(final Map<String, String> ruleParam,Map<String, String> resourceAttributes) {

		logger.debug("========CheckIAMCertificateExpired started=========");

		MDC.put("executionId", ruleParam.get("executionId"));
		MDC.put("ruleId", ruleParam.get(PacmanSdkConstants.RULE_ID));

		Optional.ofNullable(ruleParam)
				.filter(param -> (!PacmanUtils.doesAllHaveValue(param.get(PacmanRuleConstants.SEVERITY),
						param.get(PacmanRuleConstants.CATEGORY))))
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


		logger.debug("========CheckIAMCertificateExpired ended=========");
		return ruleResult;

	}

	public String getHelpText() {
		return "This Rule should look for expired SSL(IAM) certificates";
	}
	
	/**
	 * @param ruleParam
	 * @param resourceAttributes
	 * @return
	 * 
	 * This method checks if the expiry date of the IAM certificate is before the current date. 
	 * If yes, then the certificate is expired.
	 * 
	 */
	private String checkValidation(Map<String, String> ruleParam, Map<String, String> resourceAttributes) {
		
		String description = null;
		String expiryDateStr = resourceAttributes.get(PacmanRuleConstants.ES_IAM_CERT_EXPIRY_ATTRIBUTE);
		SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		
		
		try {
			Date validTo = dateFormat.parse(expiryDateStr);
			DateTime expiryDate = new DateTime(validTo);
			boolean isExpired = new DateTime().toLocalDateTime().isAfter(expiryDate.toLocalDateTime());
			
			if(isExpired)
				description = "Expired IAM certificate found !!";
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
		logger.debug("========CheckIAMCertificateExpired annotation {} :=========",annotation);
		return new RuleResult(PacmanSdkConstants.STATUS_FAILURE,PacmanRuleConstants.FAILURE_MESSAGE, annotation);
	
	
	}

}
