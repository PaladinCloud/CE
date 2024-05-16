/*******************************************************************************
 * Copyright 2023 Paladin Cloud, Inc. or its affiliates. All Rights Reserved.
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

import com.amazonaws.util.StringUtils;
import com.google.gson.JsonObject;
import com.tmobile.cloud.awsrules.utils.PacmanUtils;
import com.tmobile.cloud.constants.PacmanRuleConstants;
import com.tmobile.pacman.commons.PacmanSdkConstants;
import com.tmobile.pacman.commons.exception.InvalidInputException;
import com.tmobile.pacman.commons.exception.RuleExecutionFailedExeption;
import com.tmobile.pacman.commons.policy.Annotation;
import com.tmobile.pacman.commons.constants.ESIndexConstants;
import com.tmobile.pacman.commons.policy.BasePolicy;
import com.tmobile.pacman.commons.policy.PacmanPolicy;
import com.tmobile.pacman.commons.policy.PolicyResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;

import java.text.ParseException;
import java.util.*;

@PacmanPolicy(key = "check-for-vms-scanned-by-tenable", desc = "checks for VMs scanned by tenable ,if not found then its an issue", severity = PacmanSdkConstants.SEV_HIGH, category = PacmanSdkConstants.GOVERNANCE)
public class VMsScannedByTenableRule extends BasePolicy {

    private static final Logger logger = LoggerFactory.getLogger(VMsScannedByTenableRule.class);

    private static final String POLICY_NAME_FOR_LOGGER = "VMsScannedByTenableRule";
    private static final String POLICY_EXCEPTION_MESSAGE = "Unable to evaluate policy " + POLICY_NAME_FOR_LOGGER + " because of exception.";
    public static final String ISSUE_DETAILS_ANNOTATION_KEY = "issueDetails";
    public static final String EXECUTION_ID_PARAM = "executionId";
    public static final String RULE_ID_PARAM = "ruleId";

    /**
     * The method will get triggered from Rule Engine with following parameters.
     *
     * @param ruleParam          ************* Following are the Rule Parameters********* <br><br>
     *                           <p>
     *                           ruleKey : check-for-vms-scanned-by-tenable <br><br>
     *                           <p>
     *                           target : Enter the target days <br><br>
     *                           <p>
     *                           discoveredDaysRange : Enter the discovered days Range <br><br>
     * @param resourceAttributes this is a resource in context which needs to be scanned this is provided by execution engine
     * @return the rule result
     */
    @Override
    public PolicyResult execute(Map<String, String> ruleParam, Map<String, String> resourceAttributes) {
        logger.debug("{}: execution started", POLICY_NAME_FOR_LOGGER);

        MDC.put(EXECUTION_ID_PARAM, ruleParam.get(EXECUTION_ID_PARAM)); // this is the logback Mapped Diagnostic Contex
        MDC.put(RULE_ID_PARAM, ruleParam.get(PacmanSdkConstants.POLICY_ID)); // this is the logback Mapped Diagnostic Contex

        String category = ruleParam.get(PacmanRuleConstants.CATEGORY);
        String discoveredDaysRange = ruleParam.get(PacmanRuleConstants.DISCOVERED_DAYS_RANGE);
        String tenableEsApi = PacmanUtils.getPacmanHost(PacmanRuleConstants.ES_URI) + "/" + ESIndexConstants.TENABLE_VM_ASSETS_INDEX_NAME + "/_search";

        if (!PacmanUtils.doesAllHaveValue(category, tenableEsApi, discoveredDaysRange)) {
            logger.info(PacmanRuleConstants.MISSING_CONFIGURATION);
            throw new InvalidInputException(PacmanRuleConstants.MISSING_CONFIGURATION);
        }

        if (resourceAttributes != null) {
            String instanceId = StringUtils.trim(resourceAttributes.get(PacmanRuleConstants.RESOURCE_ID));
            String entityType = resourceAttributes.get(PacmanRuleConstants.ENTITY_TYPE).toUpperCase();
            try {
                return getPolicyResult(ruleParam, category, tenableEsApi, instanceId, entityType);
            } catch (Exception e) {
                logger.error(POLICY_EXCEPTION_MESSAGE, e);
                throw new RuleExecutionFailedExeption(POLICY_EXCEPTION_MESSAGE + e);
            }
        } else {
            logger.debug("{}: completed with NULL result: resource attributes are empty.", POLICY_NAME_FOR_LOGGER);

            return null;
        }
    }

    private PolicyResult getPolicyResult(Map<String, String> ruleParam, String category, String tenableEsApi, String instanceId, String entityType) throws ParseException {
        List<JsonObject> tenableAssets = PacmanUtils.matchAssetAgainestSourceVulnIndex(instanceId, tenableEsApi, "aws_ec2_instance_id", null);

        if (tenableAssets.isEmpty()) {
            // FAIL: Tenable doesn't do authenticated scans on this asset
            Annotation annotation = getNotScannedAnnotation(ruleParam, category, entityType);
            logger.debug("{} completed with annotation: {}", POLICY_NAME_FOR_LOGGER, annotation);

            return new PolicyResult(PacmanSdkConstants.STATUS_FAILURE, PacmanRuleConstants.FAILURE_MESSAGE, annotation);
        }

        logger.debug("{} completed with no issue produced", POLICY_NAME_FOR_LOGGER);

        return new PolicyResult(PacmanSdkConstants.STATUS_SUCCESS, PacmanRuleConstants.SUCCESS_MESSAGE);
    }

    private static Annotation getNotScannedAnnotation(Map<String, String> ruleParam, String category, String entityType) {
        Annotation annotation = Annotation.buildAnnotation(ruleParam, Annotation.Type.ISSUE);
        annotation.put(PacmanSdkConstants.DESCRIPTION, "" + entityType + " image not scanned by tenable found");
        annotation.put(PacmanRuleConstants.CATEGORY, category);
        annotation.put(ISSUE_DETAILS_ANNOTATION_KEY, getIssueDetails(entityType));

        return annotation;
    }

    private static String getIssueDetails(String entityType) {
        LinkedHashMap<String, Object> issue = new LinkedHashMap<>();
        issue.put(PacmanRuleConstants.VIOLATION_REASON, "" + entityType + " image not scanned by tenable found");
        List<LinkedHashMap<String, Object>> issueList = new ArrayList<>();
        issueList.add(issue);

        return issueList.toString();
    }

    @Override
    public String getHelpText() {
        return null;
    }
}
