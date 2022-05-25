/*******************************************************************************
 * Copyright 2018 T Mobile, Inc. or its affiliates. All Rights Reserved.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License.  You may obtain a copy
 * of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  See the
 * License for the specific language governing permissions and limitations under
 * the License.
 ******************************************************************************/
/**
 Copyright (C) 2017 T Mobile Inc - All Rights Reserve
 Purpose: This rule check for the elastic search exposed to public
 Author :Amisha
 Modified Date: May 25, 2022

 **/
package com.tmobile.cloud.awsrules.elb;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;

import com.amazonaws.services.ec2.model.GroupIdentifier;
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

@PacmanRule(key = "check-for-elb-v2-unrestrcted-security-group", desc = "This rule checks for elb port which is not configured in the listener security", severity = PacmanSdkConstants.SEV_HIGH, category = PacmanSdkConstants.SECURITY)
public class InsecureAppElbSecurityGroup extends BaseRule {
	private static final Logger logger = LoggerFactory.getLogger(InsecureAppElbSecurityGroup.class);

	/**
	 * The method will get triggered from Rule Engine with following parameters
	 * 
	 * @param ruleParam
	 * 
	 ************** Following are the Rule Parameters********* <br><br>
	 *
	 *	ruleKey : check-for-elb-public-access<br><br>
	 *
	 *	internetGateWay : The value 'igw' is used to identify the security group with Internet gateway <br><br>
	 *
	 *	esElbWithSGUrl : Enter the appELB/classicELB with SG URL <br><br>
     * 
     *	esRoutetableAssociationsURL : Enter the route table association ES URL <br><br>
     * 
     *	esRoutetableRoutesURL : Enter the route table routes ES URL <br><br>
     * 
     *	esRoutetableURL : Enter the route table ES URL <br><br>
     * 
     *	esSgRulesUrl : Enter the SG rules ES URL <br><br>
     *
     *	esElbV2ListenerURL : Enter the ELB listener url <br><br>
     * 
     *	cidrIp : Enter the ip as 0.0.0.0/0 <br><br>
     *
     *	cidripv6 : Enter the ip as ::/0 <br><br>
	 * 
	 *	severity : Enter the value of severity <br><br>
	 * 
	 *	ruleCategory : Enter the value of category <br><br>
	 * 
	 * @param resourceAttributes this is a resource in context which needs to be scanned this is provided by execution engine
	 *
	 */

	public RuleResult execute(final Map<String, String> ruleParam, Map<String, String> resourceAttributes) {
		logger.debug("========InsecureAppElbSecurityGroup started=========");
		Annotation annotation = null;
		String subnet = null;
		String sgRulesUrl = null;
		String elbSgUrl = null;
		Set<GroupIdentifier> securityGroupsSet = new HashSet<>();
		LinkedHashMap<String, Object> issue = new LinkedHashMap<>();
		Map<String, String> invalidSgMap = new HashMap<>();
		String esElbV2ListenerURL = null;

		String scheme = resourceAttributes.get(PacmanRuleConstants.SCHEME);
		String severity = ruleParam.get(PacmanRuleConstants.SEVERITY);
		String category = ruleParam.get(PacmanRuleConstants.CATEGORY);
		String loadBalncerId = ruleParam.get(PacmanRuleConstants.RESOURCE_ID);
		String region = resourceAttributes.get(PacmanRuleConstants.REGION_ATTR);
		String accountId = resourceAttributes.get(PacmanRuleConstants.ACCOUNTID);
		String targetType = resourceAttributes.get(PacmanRuleConstants.ENTITY_TYPE);
		String description = targetType + " Elb has publicly accessible ports which is not configured in elb security listeners";
		String elbType = resourceAttributes.get(PacmanRuleConstants.ELB_TYPE);
		String loadBalancerArn = StringUtils.trim(resourceAttributes.get(PacmanRuleConstants.APP_LOAD_BALANCER_ARN_ATTRIBUTE));

		String pacmanHost = PacmanUtils.getPacmanHost(PacmanRuleConstants.ES_URI);
		logger.debug("========pacmanHost {}  =========", pacmanHost);

		if (!StringUtils.isNullOrEmpty(pacmanHost)) {
			sgRulesUrl = ruleParam.get(PacmanRuleConstants.ES_SG_RULES_URL);
			elbSgUrl = ruleParam.get(PacmanRuleConstants.ES_ELB_WITH_SECURITYGROUP_URL);
			esElbV2ListenerURL = ruleParam.get(PacmanRuleConstants.ES_ELB_V2_LISTENER_URL);

			elbSgUrl = pacmanHost + elbSgUrl;
			sgRulesUrl = pacmanHost + sgRulesUrl;
			esElbV2ListenerURL = pacmanHost + esElbV2ListenerURL;
		}

		MDC.put("executionId", ruleParam.get("executionId"));
		MDC.put("ruleId", ruleParam.get(PacmanSdkConstants.RULE_ID));

		if (!PacmanUtils.doesAllHaveValue(severity, category, elbSgUrl, sgRulesUrl)) {
			logger.info(PacmanRuleConstants.MISSING_CONFIGURATION);
			throw new InvalidInputException(PacmanRuleConstants.MISSING_CONFIGURATION);
		}

		try {

			logger.debug("======loadBalncerId : {}", loadBalncerId);
			List<GroupIdentifier> listSecurityGroupID = PacmanUtils.getSecurityBroupIdByElb(loadBalncerId, elbSgUrl,
					accountId, region);
			securityGroupsSet.addAll(listSecurityGroupID);
			logger.info("calling Global IP method");
			if (!securityGroupsSet.isEmpty()) {

				List<Map<String,String>> listenerPorts = PacmanUtils.getListenerPortsByElbArn(esElbV2ListenerURL, loadBalancerArn);
				invalidSgMap = PacmanUtils.checkUnrestrictedSgAccess(securityGroupsSet, listenerPorts, sgRulesUrl);
			} else {
				logger.error("sg not associated to the resource");
				throw new RuleExecutionFailedExeption("sg not associated to the resource");
			}

			issue.put(PacmanRuleConstants.SEC_GRP, org.apache.commons.lang3.StringUtils.join(listSecurityGroupID, "/"));
			if (!invalidSgMap.isEmpty()) {
				//annotation = PacmanUtils.setAnnotation(invalidSgMap, ruleParam, subnet, description, issue);
				annotation.put(PacmanRuleConstants.SCHEME, scheme);
				if (null != annotation) {
					if ("appelb".equals(targetType)) {
						annotation.put(PacmanRuleConstants.TYPE_OF_ELB, elbType);
						annotation.put(PacmanRuleConstants.RESOURCE_DISPLAY_ID,
								resourceAttributes.get("loadbalancerarn"));
					}
					return new RuleResult(PacmanSdkConstants.STATUS_FAILURE, PacmanRuleConstants.FAILURE_MESSAGE,
							annotation);
				}
			}
		} catch (Exception e) {
			logger.error(e.getMessage());
			throw new RuleExecutionFailedExeption(e.getMessage());
		}
		logger.debug("========InsecureAppElbSecurityGroup ended=========");
		return new RuleResult(PacmanSdkConstants.STATUS_SUCCESS, PacmanRuleConstants.SUCCESS_MESSAGE);
	}

	@Override
	public String getHelpText() {
		return "This rule check for elb port which is not configured in the listener security";
	}
}
