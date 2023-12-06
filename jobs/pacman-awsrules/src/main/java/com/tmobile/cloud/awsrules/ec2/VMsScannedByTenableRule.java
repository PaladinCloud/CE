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
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.tmobile.cloud.awsrules.utils.PacmanUtils;
import com.tmobile.cloud.constants.PacmanRuleConstants;
import com.tmobile.pacman.api.commons.Constants;
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
import java.text.SimpleDateFormat;
import java.util.*;

@PacmanPolicy(key = "check-for-vms-scanned-by-tenable", desc = "checks for VMs scanned by tenable ,if not found then its an issue", severity = PacmanSdkConstants.SEV_HIGH, category = PacmanSdkConstants.GOVERNANCE)
public class VMsScannedByTenableRule extends BasePolicy {

    private static final Logger logger = LoggerFactory.getLogger(VMsScannedByTenableRule.class);

    private static final String POLICY_NAME_FOR_LOGGER = "VMsScannedByTenableRule";
    private static final String POLICY_EXCEPTION_MESSAGE = "Unable to evaluate policy " + POLICY_NAME_FOR_LOGGER + " because of exception.";
    public static final String TERMINATED_AT_FIELD_NAME = "terminated_at";
    public static final String HAS_AGENT_FIELD_NAME = "has_agent";
    public static final String LAST_LICENSED_SCAN_DATE_FIELD_NAME = "last_licensed_scan_date";
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
        logger.debug("{}: execution started .............", POLICY_NAME_FOR_LOGGER);

        MDC.put(EXECUTION_ID_PARAM, ruleParam.get(EXECUTION_ID_PARAM)); // this is the logback Mapped Diagnostic Contex
        MDC.put(RULE_ID_PARAM, ruleParam.get(PacmanSdkConstants.POLICY_ID)); // this is the logback Mapped Diagnostic Contex

        String category = ruleParam.get(PacmanRuleConstants.CATEGORY);
        String target = ruleParam.get(PacmanRuleConstants.TARGET);
        String discoveredDaysRange = ruleParam.get(PacmanRuleConstants.DISCOVERED_DAYS_RANGE);
        String tenableEsApi = PacmanUtils.getPacmanHost(PacmanRuleConstants.ES_URI) + "/" + ESIndexConstants.TENABLE_VM_ASSETS_INDEX_NAME + "/_search";

        if (!PacmanUtils.doesAllHaveValue(category, tenableEsApi, discoveredDaysRange, target)) {
            logger.info(PacmanRuleConstants.MISSING_CONFIGURATION);
            throw new InvalidInputException(PacmanRuleConstants.MISSING_CONFIGURATION);
        }

        if (resourceAttributes != null) {
            String discoveryDate = resourceAttributes.get(PacmanRuleConstants.DISCOVEREY_DATE);
            if (!StringUtils.isNullOrEmpty(discoveryDate)) {
                discoveryDate = discoveryDate.substring(0, PacmanRuleConstants.FIRST_DISCOVERED_DATE_FORMAT_LENGTH);
            }

            if (PacmanUtils.calculateLaunchedDuration(discoveryDate) >= 0) {
                String instanceID = StringUtils.trim(resourceAttributes.get(PacmanRuleConstants.RESOURCE_ID));
                String entityType = resourceAttributes.get(PacmanRuleConstants.ENTITY_TYPE).toUpperCase();
                try {
                    return getPolicyResult(ruleParam, category, target, tenableEsApi, instanceID, entityType);
                } catch (Exception e) {
                    logger.error(POLICY_EXCEPTION_MESSAGE, e);
                    throw new RuleExecutionFailedExeption(POLICY_EXCEPTION_MESSAGE + e);
                }
            }
        }

        logger.debug("{}: completed with NULL result because resource attributes are empty.", POLICY_NAME_FOR_LOGGER);

        return null;
    }

    private PolicyResult getPolicyResult(Map<String, String> ruleParam, String category, String target, String tenableEsAPI, String instanceID, String entityType) throws ParseException {
        List<JsonObject> tenableAssets = PacmanUtils.checkInstanceIdFromElasticSearchForTenable(instanceID, tenableEsAPI, "aws_ec2_instance_id", null);

        // Get the first asset from the list in case we have more than one asset with the same instance ID stored (possible, getting such results from Tenable)
        if (tenableAssets.isEmpty()
                || !tenableAssets.get(0).get(HAS_AGENT_FIELD_NAME).getAsBoolean()
                || tenableAssets.get(0).get(TERMINATED_AT_FIELD_NAME) != null) {
            // FAIL: Tenable doesn't know about this asset, asset doesn't have Tenable agent installed or asset is terminated
            Annotation annotation = getNotScannedAnnotation(ruleParam, category, entityType);
            logger.debug("{} completed with annotation: {}", POLICY_NAME_FOR_LOGGER, annotation);

            return new PolicyResult(PacmanSdkConstants.STATUS_FAILURE, PacmanRuleConstants.FAILURE_MESSAGE, annotation);
        } else {
            JsonElement lastLicensedScanDate = tenableAssets.get(0).get(LAST_LICENSED_SCAN_DATE_FIELD_NAME);
            if (!checkLastLicensedScanDate(lastLicensedScanDate, target)) {
                // FAIL: Tenable scanned this asset more than target days ago
                Annotation annotation = getOutdatedScanAnnotation(ruleParam, category, target, entityType);
                logger.debug("{} completed with annotation:  {}", POLICY_NAME_FOR_LOGGER, annotation);

                return new PolicyResult(PacmanSdkConstants.STATUS_FAILURE, PacmanRuleConstants.FAILURE_MESSAGE, annotation);
            }
        }

        logger.debug("{} completed with no issue produced", POLICY_NAME_FOR_LOGGER);

        return null;
    }

    /**
     * Check if time passed since last licensed scan date is less than target
     *
     * @param lastLicensedScanDateJson   last licensed scan date
     * @param targetTimeDifferenceInDays target time difference in days
     * @return if time passed since last licensed scan date is less than target
     * @throws ParseException
     */
    private boolean checkLastLicensedScanDate(JsonElement lastLicensedScanDateJson, String targetTimeDifferenceInDays) throws ParseException {
        if (lastLicensedScanDateJson == null || lastLicensedScanDateJson.isJsonNull() || lastLicensedScanDateJson.getAsString().isEmpty()) {
            return false;
        }

        SimpleDateFormat dateFormat = new SimpleDateFormat(PacmanSdkConstants.DATE_FORMAT);
        Date lastLicensedScanDate = dateFormat.parse(lastLicensedScanDateJson.getAsString());
        long timeDiffInDays = (System.currentTimeMillis() - lastLicensedScanDate.getTime()) / Constants.MILLIS_ONE_DAY;

        return timeDiffInDays < Long.parseLong(targetTimeDifferenceInDays);
    }

    private static Annotation getOutdatedScanAnnotation(Map<String, String> ruleParam, String category, String target, String entityType) {
        Annotation annotation = Annotation.buildAnnotation(ruleParam, Annotation.Type.ISSUE);
        annotation.put(PacmanSdkConstants.DESCRIPTION, "" + entityType + " tenable not scanned since "
                + target + " days");
        annotation.put(PacmanRuleConstants.CATEGORY, category);
        annotation.put(ISSUE_DETAILS_ANNOTATION_KEY, getIssueDetails(entityType));

        return annotation;
    }

    private static Annotation getNotScannedAnnotation(Map<String, String> ruleParam, String category, String entityType) {
        Annotation annotation = Annotation.buildAnnotation(ruleParam, Annotation.Type.ISSUE);
        annotation.put(PacmanSdkConstants.DESCRIPTION, "" + entityType + " image not scanned  by tenable found");
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
