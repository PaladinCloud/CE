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
  Purpose:
  Author :u55262
  Modified Date: Sep 19, 2017
  
 **/
package com.tmobile.cloud.awsrules.elb;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
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

@PacmanRule(key = "check-for-secured-elb-v2-listener-protocols", desc = "checks for secured listener protocols are used by elbs", severity = PacmanSdkConstants.SEV_HIGH, category = PacmanSdkConstants.GOVERNANCE)
public class ElbV2ListenerSecurityRule extends BaseRule {

	private static final Logger logger = LoggerFactory.getLogger(ElbV2ListenerSecurityRule.class);

	 /**
     * The method will get triggered from Rule Engine with following parameters
     * 
     * @param ruleParam
     * 
     * ************* Following are the Rule Parameters********* <br><br>
     * 
     * ruleKey : check-for-ec2-public-access <br><br>
     * 
     * severity : Enter the value of severity <br><br>
     * 
     * ruleCategory : Enter the value of category <br><br>
     * 
     * esElbV2ListenerURL : Enter the ELB listener url <br><br>
     * 
     * threadsafe : if true , rule will be executed on multiple threads <br><br>
     * 
     * @param resourceAttributes this is a resource in context which needs to be scanned this is provided by execution engine
     *
     */

	public RuleResult execute(final Map<String, String> ruleParam,Map<String, String> resourceAttributes) {

		logger.debug("========ElbV2ListenerSecurityRule started=========");
		Annotation annotation = null;
		String loadBalancerArn = null;
		String esElbV2ListenerURL = null;
		
		String severity = ruleParam.get(PacmanRuleConstants.SEVERITY);
		String category = ruleParam.get(PacmanRuleConstants.CATEGORY);
		String resourceId = ruleParam.get(PacmanSdkConstants.RESOURCE_ID);
		String pacmanHost = PacmanUtils.getPacmanHost(PacmanRuleConstants.ES_URI);
		
		MDC.put("executionId", ruleParam.get("executionId"));
		MDC.put("ruleId", ruleParam.get(PacmanSdkConstants.RULE_ID));
		
		if (!PacmanUtils.doesAllHaveValue(severity,category)) {
			logger.info(PacmanRuleConstants.MISSING_CONFIGURATION);
			throw new InvalidInputException(PacmanRuleConstants.MISSING_CONFIGURATION);
		}
		logger.debug("========pacmanHost {}  =========", pacmanHost);
		if (!StringUtils.isNullOrEmpty(pacmanHost)) {
			esElbV2ListenerURL = ruleParam.get(PacmanRuleConstants.ES_ELB_V2_LISTENER_URL);
			esElbV2ListenerURL = pacmanHost + esElbV2ListenerURL;
		}
		logger.debug("========esElbV2ListenerURL after concatination param {}  =========", esElbV2ListenerURL);
		
		try {
			if (resourceAttributes != null) {
				loadBalancerArn = StringUtils .trim(resourceAttributes.get(PacmanRuleConstants.APP_LOAD_BALANCER_ARN_ATTRIBUTE));
				Set<String> securedProtocolList = PacmanUtils.checkElbUsingSecuredProtocol(esElbV2ListenerURL, loadBalancerArn);

				LinkedHashMap<String, Object> issue = new LinkedHashMap<>();
				List<LinkedHashMap<String, Object>> issueList = new ArrayList<>();

				if (CollectionUtils.isNullOrEmpty(securedProtocolList)) {
					annotation = Annotation.buildAnnotation(ruleParam, Annotation.Type.ISSUE);
					annotation.put(PacmanSdkConstants.DESCRIPTION, "ELB with insecure listener found!!");
					annotation.put(PacmanRuleConstants.SEVERITY, severity);
					annotation.put(PacmanRuleConstants.CATEGORY, category);
					annotation.put(PacmanRuleConstants.RESOURCE_ID, resourceId);
					issue.put(PacmanRuleConstants.VIOLATION_REASON, "ELB with insecure listener found!!");
					issueList.add(issue);
					annotation.put("issueDetails", issueList.toString());
					logger.debug("========ElbV2ListenerSecurityRule ended with an annotation {} : =========", annotation);
					return new RuleResult(PacmanSdkConstants.STATUS_FAILURE, PacmanRuleConstants.FAILURE_MESSAGE, annotation);
				}
			}

		}catch(Exception exception) {
			logger.error("error: ", exception);
			throw new RuleExecutionFailedExeption(exception.getMessage());
		}
		
		
		logger.debug("========ElbV2ListenerSecurityRule ended=========");
		return new RuleResult(PacmanSdkConstants.STATUS_SUCCESS,PacmanRuleConstants.SUCCESS_MESSAGE);
	}

	public String getHelpText() {
		return "This rule checks secured listener protocols are used by elbs";
	}
}
