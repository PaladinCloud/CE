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
package com.tmobile.cloud.gcprules.vminstance;

import com.amazonaws.util.StringUtils;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.tmobile.cloud.awsrules.utils.PacmanUtils;
import com.tmobile.cloud.constants.PacmanRuleConstants;
import com.tmobile.pacman.commons.PacmanSdkConstants;
import com.tmobile.pacman.commons.exception.InvalidInputException;
import com.tmobile.pacman.commons.exception.RuleExecutionFailedExeption;
import com.tmobile.pacman.commons.rule.Annotation;
import com.tmobile.pacman.commons.rule.BaseRule;
import com.tmobile.pacman.commons.rule.PacmanRule;
import com.tmobile.pacman.commons.rule.RuleResult;
import com.tmobile.pacman.commons.utils.CommonUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;

import java.util.*;

import com.tmobile.cloud.gcprules.utils.GCPUtils;

@PacmanRule(key = "check-for-vminstnace-public-access", desc = "checks for virtual machine instance which has public IP address", severity = PacmanSdkConstants.SEV_HIGH, category = PacmanSdkConstants.SECURITY)
public class VMInstanceWithPublicAccess extends BaseRule {
    private static final Logger logger = LoggerFactory.getLogger(VMInstanceWithPublicAccess.class);

    /**
     * The method will get triggered from Rule Engine with following parameters
     *
     * @param ruleParam          ************* Following are the Rule Parameters********* <br><br>
     *                           <p>
     *                           ruleKey : check-for-ec2-public-access <br><br>
     *                           <p>
     *                           severity : Enter the value of severity <br><br>
     *                           <p>
     *                           ruleCategory : Enter the value of category <br><br>
     *                           <p>
     *                           <br><br>
     * @param resourceAttributes this is a resource in context which needs to be scanned this is provided by execution engine
     */

    @Override
    public RuleResult execute(Map<String, String> ruleParam, Map<String, String> resourceAttributes) {
        logger.debug("========VMWithPublicIPAccess started=========");
        Annotation annotation = null;

        String resourceId = ruleParam.get(PacmanRuleConstants.RESOURCE_ID);
        String severity = ruleParam.get(PacmanRuleConstants.SEVERITY);
        String category = ruleParam.get(PacmanRuleConstants.CATEGORY);

        String vmEsURL = CommonUtils.getEnvVariableValue(PacmanSdkConstants.ES_URI_ENV_VAR_NAME);

        if (Boolean.FALSE.equals(PacmanUtils.doesAllHaveValue(severity, category, vmEsURL))) {
            logger.info(PacmanRuleConstants.MISSING_CONFIGURATION);
            throw new InvalidInputException(PacmanRuleConstants.MISSING_CONFIGURATION);
        }

        if (!StringUtils.isNullOrEmpty(vmEsURL)) {
            vmEsURL = vmEsURL + "/gcp_vminstance/_search";
        }
        logger.debug("========vmEsURL URL after concatenation param {}  =========", vmEsURL);

        boolean isVmWithPublicIp = false;

        MDC.put("executionId", ruleParam.get("executionId"));
        MDC.put("ruleId", ruleParam.get(PacmanSdkConstants.RULE_ID));

        if (!StringUtils.isNullOrEmpty(resourceId)) {

            Map<String, Object> mustFilter = new HashMap<>();
            mustFilter.put(PacmanUtils.convertAttributetoKeyword(PacmanRuleConstants.RESOURCE_ID), resourceId);
            mustFilter.put(PacmanRuleConstants.LATEST, true);

            try {
                isVmWithPublicIp = verifyVmWithPublicIp(vmEsURL, mustFilter);
                if (!isVmWithPublicIp) {
                    List<LinkedHashMap<String, Object>> issueList = new ArrayList<>();
                    LinkedHashMap<String, Object> issue = new LinkedHashMap<>();

                    annotation = Annotation.buildAnnotation(ruleParam, Annotation.Type.ISSUE);
                    annotation.put(PacmanSdkConstants.DESCRIPTION, "VM instance with public IP found");
                    annotation.put(PacmanRuleConstants.SEVERITY, severity);
                    annotation.put(PacmanRuleConstants.CATEGORY, category);

                    issue.put(PacmanRuleConstants.VIOLATION_REASON, "VM instance with public IP found");
                    issueList.add(issue);
                    annotation.put("issueDetails", issueList.toString());
                    logger.debug("========EC2WithPublicIPAccess ended with an annotation {} : =========", annotation);
                    return new RuleResult(PacmanSdkConstants.STATUS_FAILURE, PacmanRuleConstants.FAILURE_MESSAGE, annotation);
                }

            } catch (Exception exception) {
                throw new RuleExecutionFailedExeption(exception.getMessage());
            }
        }

        logger.debug("========VMWithPublicIPAccess ended=========");
        return new RuleResult(PacmanSdkConstants.STATUS_SUCCESS, PacmanRuleConstants.SUCCESS_MESSAGE);

    }

    private boolean verifyVmWithPublicIp(String vmEsURL, Map<String, Object> mustFilter) throws Exception {
        logger.debug("========verifyVmWithPublicIp started=========");
        JsonArray hitsJsonArray = GCPUtils.getHitsArrayFromEs(vmEsURL, mustFilter);
        boolean validationResult = true;
        if (hitsJsonArray.size() > 0) {
            JsonObject vmInstanceObject = (JsonObject) ((JsonObject) hitsJsonArray.get(0))
                    .get(PacmanRuleConstants.SOURCE);

            logger.debug("Validating the data item: {}", vmInstanceObject.toString());
            JsonArray networkInterfaceJsonArray = vmInstanceObject.getAsJsonObject()
                    .get(PacmanRuleConstants.GCP_NETWORK_INTERFACE).getAsJsonArray();

            if (networkInterfaceJsonArray.size() > 0) {
                for (int i = 0; i < networkInterfaceJsonArray.size(); i++) {
                    JsonObject networkInterfaceDataItem = ((JsonObject) networkInterfaceJsonArray
                            .get(i));

                    validationResult = validateAccessConfigs(validationResult, networkInterfaceDataItem);
                }

            } else {
                logger.info(PacmanRuleConstants.RESOURCE_DATA_NOT_FOUND);
            }

        } else {
            logger.info(PacmanRuleConstants.RESOURCE_DATA_NOT_FOUND);
        }

        return validationResult;
    }

    private boolean validateAccessConfigs(boolean validationResult, JsonObject networkInterfaceDataItem) {
        JsonArray accessConfigJsonArray = networkInterfaceDataItem.getAsJsonObject()
                .get(PacmanRuleConstants.GCP_ACCESS_CONFIGS).getAsJsonArray();

        for (int j = 0; j < accessConfigJsonArray.size(); j++) {
            JsonObject accessConfigDataItem = ((JsonObject) accessConfigJsonArray
                    .get(j));

            String natIp = accessConfigDataItem.getAsJsonObject()
                    .get(PacmanRuleConstants.GCP_NAT_IP).getAsString();

            if (!StringUtils.isNullOrEmpty(natIp)) {
                validationResult = false;
                break;
            }
        }
        return validationResult;
    }

    @Override
    public String getHelpText() {
        return "checks VM instance with public NAT IP address";
    }

}
