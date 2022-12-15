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
package com.tmobile.cloud.awsrules.guardduty;

import java.util.ArrayList;
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
import com.tmobile.pacman.commons.policy.Annotation;
import com.tmobile.pacman.commons.policy.BasePolicy;
import com.tmobile.pacman.commons.policy.PacmanPolicy;
import com.tmobile.pacman.commons.policy.PolicyResult;

@PacmanPolicy(key = "check-guard-duty-findings-exists", desc = "checks guard duty findings exists for a given instance", severity = PacmanSdkConstants.SEV_HIGH, category = PacmanSdkConstants.GOVERNANCE)
public class CheckGuardDutyFindingsExists extends BasePolicy {

    private static final Logger logger = LoggerFactory
            .getLogger(CheckGuardDutyFindingsExists.class);

    /**
     * The method will get triggered from Rule Engine with following parameters
     * 
     * @param ruleParam
     * 
     *            ************* Following are the Rule Parameters********* <br>
     * <br>
     * 
     *            severity : Enter the value of severity <br>
     * <br>
     * 
     *            ruleCategory : Enter the value of category <br>
     * <br>
     * 
     *            esGuardDutyUrl : Give the guard duty ES url's <br>
     * <br>
     * 
     *            ruleKey : check-guard-duty-findings-exists <br>
     * <br>
     * 
     *            threadsafe : if true , rule will be executed on multiple
     *            threads <br>
     * <br>
     * 
     * @param resourceAttributes
     *            this is a resource in context which needs to be scanned this
     *            is provided by execution engine
     *
     */

    public PolicyResult execute(final Map<String, String> ruleParam,
            Map<String, String> resourceAttributes) {

        logger.debug("========CheckGuardDutyFindingsExists started=========");
        String resourceId = null;
        Annotation annotation = null;

        String severity = ruleParam.get(PacmanRuleConstants.SEVERITY);
        String category = ruleParam.get(PacmanRuleConstants.CATEGORY);
        String guardDutyEsUrl = null;
        
        String formattedUrl = PacmanUtils.formatUrl(ruleParam,PacmanRuleConstants.ES_GUARD_DUTY_URL);
        
        if(!StringUtils.isNullOrEmpty(formattedUrl)){
            guardDutyEsUrl =  formattedUrl;
        }

        MDC.put("executionId", ruleParam.get("executionId")); 
        MDC.put("ruleId", ruleParam.get(PacmanSdkConstants.POLICY_ID)); 
        List<LinkedHashMap<String, Object>> issueList = new ArrayList();
        LinkedHashMap<String, Object> issue = new LinkedHashMap<>();

        if (!PacmanUtils.doesAllHaveValue(severity, category, guardDutyEsUrl)) {
            logger.info(PacmanRuleConstants.MISSING_CONFIGURATION);
            throw new InvalidInputException(PacmanRuleConstants.MISSING_CONFIGURATION);
        }

        if (resourceAttributes != null) {
            resourceId = StringUtils.trim(resourceAttributes
                    .get(PacmanSdkConstants.RESOURCE_ID));
            Boolean isGuardDutyFindingsExists = PacmanUtils
                    .isGuardDutyFindingsExists(resourceId, guardDutyEsUrl,
                            PacmanRuleConstants.GUARD_DUTY_INSTANCE_ATTR);

            if (isGuardDutyFindingsExists) {
                annotation = Annotation.buildAnnotation(ruleParam,
                        Annotation.Type.ISSUE);
                annotation.put(PacmanSdkConstants.DESCRIPTION,
                        "Guard Duty findings exists!!");
                annotation.put(PacmanRuleConstants.SEVERITY, severity);
                annotation.put(PacmanRuleConstants.CATEGORY, category);

                issue.put(PacmanRuleConstants.VIOLATION_REASON,
                        "Ec2 instance associated to a guard duty!!");
                issueList.add(issue);
                annotation.put("issueDetails", issueList.toString());
                logger.debug("========CheckGuardDutyFindingsExists ended with annotation {} :=========",annotation);
                return new PolicyResult(PacmanSdkConstants.STATUS_FAILURE,
                        PacmanRuleConstants.FAILURE_MESSAGE, annotation);
            }
        }

        logger.debug("========CheckGuardDutyFindingsExists ended=========");

        return new PolicyResult(PacmanSdkConstants.STATUS_SUCCESS,
                PacmanRuleConstants.SUCCESS_MESSAGE);
    }

    public String getHelpText() {
        return "This rule checks guard duty findings exists for a given instance";
    }
}
