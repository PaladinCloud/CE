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
  Author :Amisha
  Modified Date: Jun 01, 2022
  
 **/
package com.tmobile.cloud.awsrules.athena;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;

import com.amazonaws.util.StringUtils;
import com.tmobile.cloud.awsrules.utils.PacmanUtils;
import com.tmobile.cloud.constants.PacmanRuleConstants;
import com.tmobile.pacman.commons.PacmanSdkConstants;
import com.tmobile.pacman.commons.exception.InvalidInputException;
import com.tmobile.pacman.commons.policy.Annotation;
import com.tmobile.pacman.commons.policy.BasePolicy;
import com.tmobile.pacman.commons.policy.PacmanPolicy;
import com.tmobile.pacman.commons.policy.PolicyResult;

@PacmanPolicy(key = "check-for-athena-query-results-encryption", desc = "checks for aws athena query results are encrypted", severity = PacmanSdkConstants.SEV_HIGH, category = PacmanSdkConstants.SECURITY)
public class AthenaQueryResultsEncryption extends BasePolicy {

	private static final Logger logger = LoggerFactory.getLogger(AthenaQueryResultsEncryption.class);
    public static final String SSE_STATUS_ENABLED = "ENABLED";
    
    /**
     * The method will get triggered from Rule Engine with following parameters
     * 
     * @param ruleParam
     * 
     * ************* Following are the Rule Parameters********* <br><br>
     * 
     * ruleKey : check-for-athena-query-results-encryption <br><br>
     * 
     * severity : Enter the value of severity <br><br>
     * 
     * ruleCategory : Enter the value of category <br><br>
     * 
     * threadsafe : if true , rule will be executed on multiple threads <br><br>
     * 
     * @param resourceAttributes this is a resource in context which needs to be scanned this is provided by execution engine
     *
     */

	public PolicyResult execute(final Map<String, String> ruleParam, Map<String, String> resourceAttributes) {

		logger.debug("========AthenaQueryResultsEncryption started=========");

		MDC.put("executionId", ruleParam.get("executionId"));
		MDC.put("ruleId", ruleParam.get(PacmanSdkConstants.POLICY_ID));

		Optional.ofNullable(ruleParam)
				.filter(param -> (!PacmanUtils.doesAllHaveValue(param.get(PacmanRuleConstants.SEVERITY), param.get(PacmanRuleConstants.CATEGORY))))
				.map(param -> {
					logger.info(PacmanRuleConstants.MISSING_CONFIGURATION);
					throw new InvalidInputException(PacmanRuleConstants.MISSING_CONFIGURATION);
				});

		PolicyResult ruleResult = Optional.ofNullable(resourceAttributes)
				.filter(resource -> StringUtils.isNullOrEmpty(resource.get(PacmanRuleConstants.ES_ENCRYPTION_OPTION_ATTRIBUTE)))
				.map(resource -> buildFailureAnnotation(ruleParam))
				.orElse(new PolicyResult(PacmanSdkConstants.STATUS_SUCCESS, PacmanRuleConstants.SUCCESS_MESSAGE));

		logger.debug("========AthenaQueryResultsEncryption ended=========");
		return ruleResult;
	}
	private static PolicyResult buildFailureAnnotation(final Map<String, String> ruleParam) {
		
		Annotation annotation = null;
		LinkedHashMap<String, Object> issue = new LinkedHashMap<>();
		List<LinkedHashMap<String, Object>> issueList = new ArrayList<>();

		annotation = Annotation.buildAnnotation(ruleParam,Annotation.Type.ISSUE);
		annotation.put(PacmanSdkConstants.DESCRIPTION,"AWS Athena query results are not encrypted");
		annotation.put(PacmanRuleConstants.SEVERITY, ruleParam.get(PacmanRuleConstants.SEVERITY));
		annotation.put(PacmanRuleConstants.SUBTYPE, Annotation.Type.RECOMMENDATION.toString());
		annotation.put(PacmanRuleConstants.CATEGORY, ruleParam.get(PacmanRuleConstants.CATEGORY));
		annotation.put(PacmanRuleConstants.RESOURCE_ID, ruleParam.get(PacmanRuleConstants.RESOURCE_ID));
		issue.put(PacmanRuleConstants.VIOLATION_REASON, "AWS Athena query results are not encrypted");
		issueList.add(issue);
		annotation.put("issueDetails",issueList.toString());
		logger.debug("========AthenaQueryResultsEncryption annotation {} :=========",annotation);
		return new PolicyResult(PacmanSdkConstants.STATUS_FAILURE,PacmanRuleConstants.FAILURE_MESSAGE, annotation);
	
	}

	public String getHelpText() {
		return "This rule checks for aws athena query results are encrypted";
	}
	
}
