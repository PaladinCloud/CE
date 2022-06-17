
package com.tmobile.cloud.awsrules.federated;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

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

@PacmanRule(key = "check-for-expired-acm-certificate", desc = "This Rule look for the SSL(ACM) certificate expired", severity = PacmanSdkConstants.SEV_HIGH, category = PacmanSdkConstants.GOVERNANCE)
public class CheckACMCertificateExpired extends BaseRule {

	private static final Logger logger = LoggerFactory.getLogger(CheckACMCertificateExpired.class);

	private static final String STATUS_VALUE_EXP = "EXPIRED";

	/**
	 * The method will get triggered from Rule Engine with following parameters
	 *
	 * @param ruleParam
	 *
	 **************Following are the Rule Parameters********* <br><br>
	 *
	 * ruleKey : check-for-expired-acm-certificate <br><br>
	 *
	 *
	 * severity : Enter the value of severity <br><br>
	 *
	 * ruleCategory : Enter the value of category <br><br>
	 *
	 * @param resourceAttributes this is a resource in context which needs to be scanned this is provided by execution engine
	 *
	 */
	public RuleResult execute(final Map<String, String> ruleParam, Map<String, String> resourceAttributes) {
		logger.debug("========CheckACMCertificateExpired started=========");
		Annotation annotation = null;
		String status = resourceAttributes.get("status");
		String severity = ruleParam.get(PacmanRuleConstants.SEVERITY);
		String category = ruleParam.get(PacmanRuleConstants.CATEGORY);

		MDC.put("executionId", ruleParam.get("executionId"));
		MDC.put("ruleId", ruleParam.get(PacmanSdkConstants.RULE_ID));

		List<LinkedHashMap<String, Object>> issueList = new ArrayList<>();
		LinkedHashMap<String, Object> issue = new LinkedHashMap<>();

		if (!PacmanUtils.doesAllHaveValue(severity, category)) {
			logger.info(PacmanRuleConstants.MISSING_CONFIGURATION);
			throw new InvalidInputException(PacmanRuleConstants.MISSING_CONFIGURATION);
		}
		if (status != null && STATUS_VALUE_EXP.equalsIgnoreCase(status)) {
			annotation = Annotation.buildAnnotation(ruleParam, Annotation.Type.ISSUE);
			annotation.put(PacmanSdkConstants.DESCRIPTION, "SSL(ACM) Expired  found!!");
			annotation.put(PacmanRuleConstants.SEVERITY, severity);
			annotation.put(PacmanRuleConstants.CATEGORY, category);
			issue.put(PacmanRuleConstants.VIOLATION_REASON, "SSL(ACM) Expired found!!");
			issueList.add(issue);
			annotation.put("issueDetails", issueList.toString());
			logger.debug("========CheckACMCertificateExpired ended with annotation {} : =========", annotation);
			return new RuleResult(PacmanSdkConstants.STATUS_FAILURE, PacmanRuleConstants.FAILURE_MESSAGE, annotation);
		} else {
			logger.info("SSL(ACM) certificate not expired");
		}

		logger.debug("========CheckACMCertificateExpired ended=========");
		return new RuleResult(PacmanSdkConstants.STATUS_SUCCESS, PacmanRuleConstants.SUCCESS_MESSAGE);
	}

	public String getHelpText() {
		return "This Rule should look for the SSL(ACM) certificate expired";
	}

}
