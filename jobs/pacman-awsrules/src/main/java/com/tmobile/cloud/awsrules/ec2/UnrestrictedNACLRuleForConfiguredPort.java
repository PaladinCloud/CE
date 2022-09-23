package com.tmobile.cloud.awsrules.ec2;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;

import com.amazonaws.util.StringUtils;
import com.nimbusds.oauth2.sdk.util.MapUtils;
import com.tmobile.cloud.awsrules.utils.PacmanUtils;
import com.tmobile.cloud.constants.PacmanRuleConstants;
import com.tmobile.pacman.commons.PacmanSdkConstants;
import com.tmobile.pacman.commons.exception.InvalidInputException;
import com.tmobile.pacman.commons.exception.RuleExecutionFailedExeption;
import com.tmobile.pacman.commons.rule.Annotation;
import com.tmobile.pacman.commons.rule.BaseRule;
import com.tmobile.pacman.commons.rule.PacmanRule;
import com.tmobile.pacman.commons.rule.RuleResult;

@PacmanRule(key = "check-for-nacl-with-public-access-configured-ports", desc = "checks network ACL has rule which allow unrestricted access to server administration ports", severity = PacmanSdkConstants.SEV_HIGH, category = PacmanSdkConstants.SECURITY)
public class UnrestrictedNACLRuleForConfiguredPort extends BaseRule {

	private static final Logger logger = LoggerFactory.getLogger(UnrestrictedNACLRuleForConfiguredPort.class);

	/**
	 * The method will get triggered from Rule Engine with following parameters
	 * 
	 * @param ruleParam
	 * 
	 ************** Following are the Rule Parameters********* <br><br>
	 *
	 * portToCheck : The port value of the security group <br><br>
	 * 
	 * ruleKey : check-for-NACL-with-public-access-configured-ports <br><br>
	 *
	 * threadsafe : if true , rule will be executed on multiple threads <br><br>
	 *
	 * severity : Enter the value of severity <br><br>
	 * 
	 * esNaclEntryUrl : Enter the network access control list entry ES URL  <br><br>
	 * 
	 * ruleCategory : Enter the value of category <br><br>
	 * 
	 * @param resourceAttributes this is a resource in context which needs to be scanned this is provided by execution engine
	 *
	 */

	@Override
	public RuleResult execute(final Map<String, String> ruleParam, Map<String, String> resourceAttributes) {

		logger.debug("========UnrestrictedNetworkACLRule started=========");

		MDC.put("executionId", ruleParam.get("executionId"));
		MDC.put("ruleId", ruleParam.get(PacmanSdkConstants.RULE_ID));

		if (MapUtils.isNotEmpty(ruleParam) && !PacmanUtils.doesAllHaveValue(ruleParam.get(PacmanRuleConstants.SEVERITY),
				ruleParam.get(PacmanRuleConstants.CATEGORY), ruleParam.get(PacmanRuleConstants.PORT_TO_CHECK),
				ruleParam.get(PacmanRuleConstants.ES_NACL_ENTRY_URL))) {
			logger.info(PacmanRuleConstants.MISSING_CONFIGURATION);
			throw new InvalidInputException(PacmanRuleConstants.MISSING_CONFIGURATION);
		}

		Optional<String> opt = Optional.ofNullable(resourceAttributes)
				.map(resource -> checkValidation(ruleParam, resource));

		RuleResult ruleResult = Optional.ofNullable(ruleParam).filter(param -> opt.isPresent())
				.map(param -> buildFailureAnnotation(param, opt.get()))
				.orElse(new RuleResult(PacmanSdkConstants.STATUS_SUCCESS, PacmanRuleConstants.SUCCESS_MESSAGE));

		logger.debug("========UnrestrictedNetworkACLRule ended=========");
		return ruleResult;

	}
    
	/**
	 * @param ruleParam
	 * @param resourceAttributes
	 * @return
	 * 
	 * Validate the nacl has rule with 
	 * cidr bloc 0.0.0.0/0 or ipv6cidr block ::/0 and
	 * action allow and 
	 * from port and to port equals portToCheck or 
	 *  portToCheck falls within the port range or 
	 *   port range is empty (indicates All) and 
	 * Egress is false (indicates inbound traffic)
	 * 
	 */
	private String checkValidation(Map<String, String> ruleParam, Map<String, String> resourceAttributes) {

		String description = null;

		String formattedNaclEntryUrl = PacmanUtils.formatUrl(ruleParam, PacmanRuleConstants.ES_NACL_ENTRY_URL);
		String esNaclEntryUrl = !StringUtils.isNullOrEmpty(formattedNaclEntryUrl) ? formattedNaclEntryUrl : "";

		try {

			boolean isUnrestrictedNAclRule = PacmanUtils.checkNaclWithInvalidRules(
					resourceAttributes.get(PacmanRuleConstants.ES_NACL_ID_ATTRIBUTE), esNaclEntryUrl,
					ruleParam.get(PacmanRuleConstants.PORT_TO_CHECK));

			if (isUnrestrictedNAclRule)
				return description = "Network Access Control List(NACL) with unrestricted access to server administration port "
						+ ruleParam.get(PacmanRuleConstants.PORT_TO_CHECK) + " found !!";
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
		logger.debug("========UnrestrictedNetworkACLRule annotation {} :=========",annotation);
		return new RuleResult(PacmanSdkConstants.STATUS_FAILURE,PacmanRuleConstants.FAILURE_MESSAGE, annotation);
	
	
	}
	
	@Override
	public String getHelpText() {
		return "Checks network ACL has rule which allow unrestricted access to server administration ports";
	}

}
