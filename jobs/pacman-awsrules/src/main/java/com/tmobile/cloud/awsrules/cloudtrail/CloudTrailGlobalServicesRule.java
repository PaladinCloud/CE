/*******************************************************************************
  * Copyright 2019 T Mobile, Inc. or its affiliates. All Rights Reserved.
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
  Copyright (C) 2019 T Mobile Inc - All Rights Reserve
  Purpose:
  Author :Amisha
  Modified Date: May 10, 2022

 **/
package com.tmobile.cloud.awsrules.cloudtrail;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;

import com.tmobile.cloud.awsrules.utils.PacmanUtils;
import com.tmobile.cloud.constants.PacmanRuleConstants;
import com.tmobile.pacman.commons.PacmanSdkConstants;
import com.tmobile.pacman.commons.exception.InvalidInputException;
import com.tmobile.pacman.commons.policy.Annotation;
import com.tmobile.pacman.commons.policy.BasePolicy;
import com.tmobile.pacman.commons.policy.PacmanPolicy;
import com.tmobile.pacman.commons.policy.PolicyResult;

@PacmanPolicy(key = "check-cloudtrail-global-services-enabled", desc = "This rule checks for AWS CloudTrail global services enabled", severity = PacmanSdkConstants.SEV_HIGH, category = PacmanSdkConstants.SECURITY)
public class CloudTrailGlobalServicesRule extends BasePolicy {

    private static final Logger logger = LoggerFactory.getLogger(CloudTrailGlobalServicesRule.class);

    /**
     * The method will get triggered from Rule Engine with following parameters
     *
     * @param ruleParam
     *
     *            ************* Following are the Rule Parameters********* <br>
     * <br>
     *
     *            ruleKey : check-cloudtrail-global-services-enabled <br>
     * <br>
     *
     *            severity : Enter the value of severity <br>
     * <br>
     *
     *            ruleCategory : Enter the value of category <br>
     * <br>
     *
     *            inputCloudTrailName : Enter the cloud trail input  <br>
     * <br>
     *
     * @param resourceAttributes
     *            this is a resource in context which needs to be scanned this
     *            is provided y execution engine
     *
     */

    @Override
    public PolicyResult execute(Map<String, String> ruleParam,Map<String, String> resourceAttributes) {
        logger.debug("========CloudTrailGlobalServicesRule started=========");
        Annotation annotation = null;
        String severity = ruleParam.get(PacmanRuleConstants.SEVERITY);
        String category = ruleParam.get(PacmanRuleConstants.CATEGORY);
        MDC.put("executionId", ruleParam.get("executionId"));
        MDC.put("ruleId", ruleParam.get(PacmanSdkConstants.POLICY_ID));
        
        String isGlobalServicesEventEnabled = resourceAttributes.get(PacmanRuleConstants.INCLUDE_GLOBAL_SERVICE_EVENTS);
        
        List<LinkedHashMap<String,Object>>issueList = new ArrayList<>();
		LinkedHashMap<String,Object>issue = new LinkedHashMap<>();
		
        if (!PacmanUtils.doesAllHaveValue(severity, category)) {
            logger.info(PacmanRuleConstants.MISSING_CONFIGURATION);
            throw new InvalidInputException(PacmanRuleConstants.MISSING_CONFIGURATION);
		}
		if (StringUtils.isNotEmpty(isGlobalServicesEventEnabled) && !Boolean.parseBoolean(isGlobalServicesEventEnabled)) {
			annotation = Annotation.buildAnnotation(ruleParam, Annotation.Type.ISSUE);
			annotation.put(PacmanSdkConstants.DESCRIPTION, "Cloudtrail global service event is not enabled!!");
			annotation.put(PacmanRuleConstants.SEVERITY, severity);
			annotation.put(PacmanRuleConstants.CATEGORY, category);
			issue.put(PacmanRuleConstants.VIOLATION_REASON, "Cloudtrail global service event is not enabled!!");
			issueList.add(issue);
			annotation.put("issueDetails", issueList.toString());
			return new PolicyResult(PacmanSdkConstants.STATUS_FAILURE, PacmanRuleConstants.FAILURE_MESSAGE, annotation);
		}

        logger.debug("========CloudTrailGlobalServicesRule ended=========");
		return new PolicyResult(PacmanSdkConstants.STATUS_SUCCESS, PacmanRuleConstants.SUCCESS_MESSAGE);
    }

    public String getHelpText() {
		return "This rule checks for AWS CloudTrail global services enabled";
	}

}
