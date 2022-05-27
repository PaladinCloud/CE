
package com.tmobile.cloud.awsrules.elb;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;

import com.amazonaws.services.ec2.model.GroupIdentifier;
import com.amazonaws.services.elasticloadbalancing.model.Listener;
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

@PacmanRule(key = "check-for-classic-elb-unrestrcted-security-group", desc = "This rule checks for classic elb security group port which is not configured in the listener security", severity = PacmanSdkConstants.SEV_HIGH, category = PacmanSdkConstants.SECURITY)
public class UnrestrctedClassicElbSecurityGroup extends BaseRule {
	private static final Logger logger = LoggerFactory.getLogger(UnrestrctedClassicElbSecurityGroup.class);

	/**
	 * The method will get triggered from Rule Engine with following parameters
	 * 
	 * @param ruleParam
	 * 
	 ************** Following are the Rule Parameters********* <br><br>
	 *
	 *	ruleKey : check-for-classic-elb-public-access<br><br>
	 *
	 *	esElbWithSGUrl : Enter the appELB/classicELB with SG URL <br><br>
     * 
     *	esSgRulesUrl : Enter the SG rules ES URL <br><br>
     *
     *	esElbV2ListenerURL : Enter the ELB listener url <br><br>
     * 
	 *	severity : Enter the value of severity <br><br>
	 * 
	 *	ruleCategory : Enter the value of category <br><br>
	 * 
	 * @param resourceAttributes this is a resource in context which needs to be scanned this is provided by execution engine
	 *
	 */

	public RuleResult execute(final Map<String, String> ruleParam, Map<String, String> resourceAttributes) {

		logger.debug("========UnrestrctedClassicElbSecurityGroup started=========");
		String elbSgUrl = null;
		String sgRulesUrl = null;
		Annotation annotation = null;
		String esClassicElbListenerURL = null;

		Set<GroupIdentifier> securityGroupsSet = new HashSet<>();
		List<Map<String, String>> invalidSgMap = new ArrayList<>();
		LinkedHashMap<String, Object> issue = new LinkedHashMap<>();
		List<LinkedHashMap<String, Object>> issueList = new ArrayList<>();

		String severity = ruleParam.get(PacmanRuleConstants.SEVERITY);
		String category = ruleParam.get(PacmanRuleConstants.CATEGORY);
		String scheme = resourceAttributes.get(PacmanRuleConstants.SCHEME);
		String loadBalncerId = ruleParam.get(PacmanRuleConstants.RESOURCE_ID);
		String elbType = resourceAttributes.get(PacmanRuleConstants.ELB_TYPE);
		String region = resourceAttributes.get(PacmanRuleConstants.REGION_ATTR);
		String accountId = resourceAttributes.get(PacmanRuleConstants.ACCOUNTID);
		String targetType = resourceAttributes.get(PacmanRuleConstants.ENTITY_TYPE);
		String loadBalancerName = StringUtils.trim(resourceAttributes.get(PacmanRuleConstants.LOAD_BALANCER_ID_ATTRIBUTE));

		String pacmanHost = PacmanUtils.getPacmanHost(PacmanRuleConstants.ES_URI);
		logger.debug("========pacmanHost {}  =========", pacmanHost);

		if (!StringUtils.isNullOrEmpty(pacmanHost)) {
			sgRulesUrl = ruleParam.get(PacmanRuleConstants.ES_SG_RULES_URL);
			elbSgUrl = ruleParam.get(PacmanRuleConstants.ES_CLASSIC_ELB_WITH_SECURITYGROUP_URL);
			esClassicElbListenerURL = ruleParam.get(PacmanRuleConstants.ES_CLASSIC_ELB_LISTENER_URL);

			elbSgUrl = pacmanHost + elbSgUrl;
			sgRulesUrl = pacmanHost + sgRulesUrl;
			esClassicElbListenerURL = pacmanHost + esClassicElbListenerURL;
		}

		MDC.put("executionId", ruleParam.get("executionId"));
		MDC.put("ruleId", ruleParam.get(PacmanSdkConstants.RULE_ID));

		if (!PacmanUtils.doesAllHaveValue(severity, category, elbSgUrl, sgRulesUrl, esClassicElbListenerURL)) {
			logger.info(PacmanRuleConstants.MISSING_CONFIGURATION);
			throw new InvalidInputException(PacmanRuleConstants.MISSING_CONFIGURATION);
		}

		try {

			logger.debug("======loadBalncerId : {}", loadBalncerId);
			List<GroupIdentifier> listSecurityGroupID = PacmanUtils.getSecurityBroupIdByElb(loadBalncerId, elbSgUrl, accountId, region);
			securityGroupsSet.addAll(listSecurityGroupID);

			if (!securityGroupsSet.isEmpty()) {
				List<Listener> listenerPorts = PacmanUtils.getClassicLBListenerPortsByName(esClassicElbListenerURL, loadBalancerName,accountId,region);
				invalidSgMap = PacmanUtils.checkUnrestrictedSgAccessForClassicLB(securityGroupsSet, listenerPorts, sgRulesUrl);
			} else {
				logger.error("sg not associated to the resource");
				issue.put(PacmanRuleConstants.SEC_GRP, org.apache.commons.lang3.StringUtils.join(listSecurityGroupID, "/"));
				throw new RuleExecutionFailedExeption("sg not associated to the resource");
			}

			if (!invalidSgMap.isEmpty()) {

				String description = "Classic Elb security groups with the configuration " + invalidSgMap.toString() + "  are found having unrestricted access ";
				annotation = Annotation.buildAnnotation(ruleParam, Annotation.Type.ISSUE);
				annotation.put(PacmanSdkConstants.DESCRIPTION, description);
				annotation.put(PacmanRuleConstants.SEVERITY, severity);
				annotation.put(PacmanRuleConstants.CATEGORY, category);
				annotation.put(PacmanRuleConstants.RESOURCE_DISPLAY_ID, resourceAttributes.get("loadbalancerarn"));
				annotation.put(PacmanRuleConstants.SCHEME, scheme);
				if ("appelb".equals(targetType))
					annotation.put(PacmanRuleConstants.TYPE_OF_ELB, elbType);
				issue.put(PacmanRuleConstants.VIOLATION_REASON, description);
				issueList.add(issue);
				annotation.put("issueDetails", issueList.toString());
				logger.debug("========UnrestrctedClassicElbSecurityGroup ended with an annotation {} : =========", annotation);
				return new RuleResult(PacmanSdkConstants.STATUS_FAILURE, PacmanRuleConstants.FAILURE_MESSAGE, annotation);
			}
		} catch (Exception e) {
			logger.error("error: ", e);
			throw new RuleExecutionFailedExeption(e.getMessage());
		}
		logger.debug("========UnrestrctedClassicElbSecurityGroup ended=========");
		return new RuleResult(PacmanSdkConstants.STATUS_SUCCESS, PacmanRuleConstants.SUCCESS_MESSAGE);
	}

	@Override
	public String getHelpText() {
		return "This rule check for classic elb security group port which is not configured in the listener security";
	}
}
