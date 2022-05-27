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

@PacmanRule(key = "check-for-secured-classic-elb-listener-protocols", desc = "checks for secured listener protocols are used by classic elbs", severity = PacmanSdkConstants.SEV_HIGH, category = PacmanSdkConstants.GOVERNANCE)
public class ClassicElbListenerSecurityRule extends BaseRule {

	private static final Logger logger = LoggerFactory.getLogger(ClassicElbListenerSecurityRule.class);

	 /**
     * The method will get triggered from Rule Engine with following parameters
     * 
     * @param ruleParam
     * 
     * ************* Following are the Rule Parameters********* <br><br>
     * 
     * ruleKey : check-for-secured-classic-elb-listener-protocols <br><br>
     * 
     * severity : Enter the value of severity <br><br>
     * 
     * ruleCategory : Enter the value of category <br><br>
     * 
     * esClassicElbListenerURL : Enter the Classic ELB listener url <br><br>
     * 
     * threadsafe : if true , rule will be executed on multiple threads <br><br>
     * 
     * @param resourceAttributes this is a resource in context which needs to be scanned this is provided by execution engine
     *
     */

	public RuleResult execute(final Map<String, String> ruleParam,Map<String, String> resourceAttributes) {

		logger.debug("========ClassicElbListenerSecurityRule started=========");
		Annotation annotation = null;
		String loadBalancerName = null;
		String esElbV2ListenerURL = null;
		String region = null;
		String accountId = null;
		
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
			esElbV2ListenerURL = ruleParam.get(PacmanRuleConstants.ES_CLASSIC_ELB_LISTENER_URL);
			esElbV2ListenerURL = pacmanHost + esElbV2ListenerURL;
		}
		logger.debug("========ClassicElbListenerSecurityRule after concatination param {}  =========", esElbV2ListenerURL);
		
		try {
			if (resourceAttributes != null) {
				region = StringUtils.trim(resourceAttributes.get(PacmanRuleConstants.REGION));
				accountId = StringUtils.trim(resourceAttributes.get(PacmanSdkConstants.ACCOUNT_ID));
				loadBalancerName  = StringUtils.trim(resourceAttributes.get(PacmanRuleConstants.LOAD_BALANCER_ID_ATTRIBUTE));
				Set<String> securedProtocolList = PacmanUtils.checkClassicElbUsingSecuredProtocol(esElbV2ListenerURL, loadBalancerName, accountId, region);

				LinkedHashMap<String, Object> issue = new LinkedHashMap<>();
				List<LinkedHashMap<String, Object>> issueList = new ArrayList<>();

				if (CollectionUtils.isNullOrEmpty(securedProtocolList)) {
					annotation = Annotation.buildAnnotation(ruleParam, Annotation.Type.ISSUE);
					annotation.put(PacmanSdkConstants.DESCRIPTION, "Classic ELB with insecure listener found!!");
					annotation.put(PacmanRuleConstants.SEVERITY, severity);
					annotation.put(PacmanRuleConstants.CATEGORY, category);
					annotation.put(PacmanRuleConstants.RESOURCE_ID, resourceId);
					issue.put(PacmanRuleConstants.VIOLATION_REASON, "Classic ELB with insecure listener found!!");
					issueList.add(issue);
					annotation.put("issueDetails", issueList.toString());
					logger.debug("========ClassicElbListenerSecurityRule ended with an annotation {} : =========", annotation);
					return new RuleResult(PacmanSdkConstants.STATUS_FAILURE, PacmanRuleConstants.FAILURE_MESSAGE, annotation);
				}
			}

		}catch(Exception exception) {
			logger.error("error: ", exception);
			throw new RuleExecutionFailedExeption(exception.getMessage());
		}
		
		
		logger.debug("========ClassicElbListenerSecurityRule ended=========");
		return new RuleResult(PacmanSdkConstants.STATUS_SUCCESS,PacmanRuleConstants.SUCCESS_MESSAGE);
	}

	public String getHelpText() {
		return "This rule checks secured listener protocols are used by classic elbs";
	}
}
