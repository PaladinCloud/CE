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
package com.tmobile.cloud.awsrules.ec2;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;

import com.amazonaws.util.StringUtils;
import com.tmobile.cloud.awsrules.utils.PacmanUtils;
import com.tmobile.cloud.constants.PacmanRuleConstants;
import com.tmobile.pacman.commons.PacmanSdkConstants;
import com.tmobile.pacman.commons.exception.InvalidInputException;
import com.tmobile.pacman.commons.exception.RuleExecutionFailedExeption;
import com.tmobile.pacman.commons.policy.Annotation;
import com.tmobile.pacman.commons.policy.BasePolicy;
import com.tmobile.pacman.commons.policy.PacmanPolicy;
import com.tmobile.pacman.commons.policy.PolicyResult;

@PacmanPolicy(key = "check-for-low-utilization-amazon-ec2-instance", desc = "Checks for low utilization amazon ec2 instance", severity = PacmanSdkConstants.SEV_HIGH, category = PacmanSdkConstants.GOVERNANCE)
public class LowUtilizationAmazonEC2InstancesRule extends BasePolicy {

	private static final Logger logger = LoggerFactory.getLogger(LowUtilizationAmazonEC2InstancesRule.class);
	/**
	 * The method will get triggered from Rule Engine with following parameters
	 * @param ruleParam 
	 * 
	 * ************* Following are the Rule Parameters********* <br><br>
	 * 
	 * checkId   : Mention the checkId value <br><br>
	 * 
	 * ruleKey : check-for-low-utilization-amazon-ec2-instance <br><br>
	 * 
	 * esServiceURL : Enter the Es url <br><br>
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
	public PolicyResult execute(Map<String, String> ruleParam, Map<String, String> resourceAttributes) {
		
		logger.debug("========LowUtilizationAmazonEC2InstancesRule started=========");
		Annotation annotation = null;
		String resourceId = null;
		String accountId = null;
		String checkId = StringUtils.trim(ruleParam.get(PacmanRuleConstants.CHECK_ID));
		
		String severity = ruleParam.get(PacmanRuleConstants.SEVERITY);
		String category = ruleParam.get(PacmanRuleConstants.CATEGORY);
		
		String serviceEsURL = null;
		
		String pacmanHost = PacmanUtils.getPacmanHost(PacmanRuleConstants.ES_URI);
		logger.debug("========pacmanHost {}  =========",pacmanHost);
		
		if(!StringUtils.isNullOrEmpty(pacmanHost)){
		    serviceEsURL = ruleParam.get(PacmanRuleConstants.ES_CHECK_SERVICE_SEARCH_URL_PARAM);
		    serviceEsURL = pacmanHost+serviceEsURL;
		}
		
		logger.debug("========service URL after concatination param {}  =========",serviceEsURL);
		
		MDC.put("executionId", ruleParam.get("executionId")); // this is the logback Mapped Diagnostic Contex
		MDC.put("ruleId", ruleParam.get(PacmanSdkConstants.POLICY_ID)); // this is the logback Mapped Diagnostic Contex
		
		List<LinkedHashMap<String,Object>>issueList = new ArrayList<>();
		LinkedHashMap<String,Object>issue = new LinkedHashMap<>();
		
		if (!PacmanUtils.doesAllHaveValue(checkId,severity,category,serviceEsURL)) {
			logger.info(PacmanRuleConstants.MISSING_CONFIGURATION);
			throw new InvalidInputException(PacmanRuleConstants.MISSING_CONFIGURATION);
		}
		
		if (resourceAttributes != null) {
			accountId = resourceAttributes.get(PacmanSdkConstants.ACCOUNT_ID);
			resourceId = StringUtils.trim(resourceAttributes.get(PacmanSdkConstants.RESOURCE_ID));
			Map<String, String> lowUtilEc2Map = new HashMap<>();
			try {
				lowUtilEc2Map = PacmanUtils.getLowUtilizationEc2Details(checkId,resourceId,serviceEsURL,null,accountId);
			} catch (Exception e) {
				logger.error("unable to determine",e);
				throw new RuleExecutionFailedExeption("unable to determine"+e);
			}
			if (!lowUtilEc2Map.isEmpty()) {
				annotation = Annotation.buildAnnotation(ruleParam, Annotation.Type.ISSUE);
				annotation.put(PacmanSdkConstants.DESCRIPTION,"Low utilization amazon ec2 instance found !!");
				annotation.put(PacmanRuleConstants.SEVERITY, severity); 
				annotation.put(PacmanRuleConstants.CATEGORY, category);
				annotation.put(PacmanRuleConstants.EST_MONTHLY_SAVINGS, lowUtilEc2Map.get(PacmanRuleConstants.EST_MONTHLY_SAVINGS));
				annotation.put(PacmanRuleConstants.NO_OF_LOW_UTILIZATION, lowUtilEc2Map.get(PacmanRuleConstants.NO_OF_LOW_UTILIZATION));
				
				issue.put(PacmanRuleConstants.VIOLATION_REASON, "Low utilization amazon ec2 instance found");
				issue.put(PacmanRuleConstants.CHECKID, checkId);
				issue.put(PacmanRuleConstants.SOURCE_VERIFIED, "trusted advisor");
				issueList.add(issue);
				annotation.put("issueDetails",issueList.toString());
				logger.debug("========LowUtilizationAmazonEC2InstancesRule ended with an annotation {} : =========",annotation);
				return new PolicyResult(PacmanSdkConstants.STATUS_FAILURE,PacmanRuleConstants.FAILURE_MESSAGE, annotation);
			}
			}
		logger.debug("========LowUtilizationAmazonEC2InstancesRule ended=========");
		return new PolicyResult(PacmanSdkConstants.STATUS_SUCCESS,PacmanRuleConstants.SUCCESS_MESSAGE);
	}

	@Override
	public String getHelpText() {
		return "This rule checks for low utilization amazon ec2 instance";
	}

}
