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

import com.amazonaws.util.CollectionUtils;
import com.amazonaws.util.StringUtils;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.tmobile.cloud.awsrules.utils.PacmanUtils;
import com.tmobile.cloud.constants.PacmanRuleConstants;
import com.tmobile.cloud.model.CveDetails;
import com.tmobile.cloud.model.VulnerabilityInfo;
import com.tmobile.pacman.commons.PacmanSdkConstants;
import com.tmobile.pacman.commons.exception.InvalidInputException;
import com.tmobile.pacman.commons.exception.RuleExecutionFailedExeption;
import com.tmobile.pacman.commons.policy.Annotation;
import com.tmobile.pacman.commons.policy.BasePolicy;
import com.tmobile.pacman.commons.policy.PacmanPolicy;
import com.tmobile.pacman.commons.policy.PolicyResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@PacmanPolicy(key = "check-vm-vulnerabilities-scanned-by-plugin-grouped", desc = "checks for VMs scanned by plugin, and report if vulnerability criteria met", severity = PacmanSdkConstants.SEV_HIGH, category = PacmanSdkConstants.GOVERNANCE)
public class AssetTypeGroupedVulnerabilitiesRule extends BasePolicy {

    private static final Logger logger = LoggerFactory.getLogger(AssetTypeGroupedVulnerabilitiesRule.class);
    private static final String POLICY_NAME_FOR_LOGGER = "AssetTypeGroupedVulnerabilitiesRule";
    private final String VULN_ASSET_LOOKUP_KEY = "asset_lookup_key";
    private final String VULNERABILITY_INDEX = "vulnerability_index";

    /**
     * The method will get triggered from Rule Engine with following parameters.
     *
     * @param ruleParam          ************* Following are the Rule Parameters********* <br><br>
     *                           <p>
     *                           ruleKey : check-vm-vulnerabilities-scanned-by-plugin-grouped <br><br>
     *                           <p>
     *                           target : Enter the target days <br><br>
     *                           <p>
     *                           discoveredDaysRange : Enter the discovered days Range <br><br>
     *                           <p>
     *                           vulnIndex : Enter the Plugin URL <br><br>
     * @param resourceAttributes this is a resource in context which needs to be scanned this is provided by execution engine
     * @return the rule result
     */
    @Override
    public PolicyResult execute(Map<String, String> ruleParam, Map<String, String> resourceAttributes) {
        logger.debug("{}: execution started", POLICY_NAME_FOR_LOGGER);
        MDC.put("executionId", ruleParam.get("executionId"));
        MDC.put("ruleId", ruleParam.get(PacmanSdkConstants.POLICY_ID));
        String category = ruleParam.get(PacmanRuleConstants.CATEGORY);
        String severity = ruleParam.get(PacmanRuleConstants.SEVERITY);
        String vulnerabilityIndex = ruleParam.get(VULNERABILITY_INDEX);
        String vulnAssetLookupKey = ruleParam.get(VULN_ASSET_LOOKUP_KEY);
        String vulnerabilitiesEndpoint = PacmanUtils.getPacmanHost(PacmanRuleConstants.ES_URI) + "/" + vulnerabilityIndex + "/_search";
        if (!PacmanUtils.doesAllHaveValue(category, severity)) {
            logger.info(PacmanRuleConstants.MISSING_CONFIGURATION);
            throw new InvalidInputException(PacmanRuleConstants.MISSING_CONFIGURATION);
        }
        PolicyResult policyResult = null;
        if (resourceAttributes != null) {
            String instanceId = StringUtils.trim(resourceAttributes.get(PacmanRuleConstants.RESOURCE_ID));
            List<JsonObject> vulnerabilityInfoList = new ArrayList<>();
            try {
                vulnerabilityInfoList = PacmanUtils.matchAssetAgainstSourceVulnIndex(instanceId, vulnerabilitiesEndpoint, vulnAssetLookupKey, null);
            } catch (Exception e) {
                logger.error("unable to determine", e);
                throw new RuleExecutionFailedExeption("unable to determine" + e);
            }
            if (!CollectionUtils.isNullOrEmpty(vulnerabilityInfoList)) {
                Annotation annotation = Annotation.buildAnnotation(ruleParam, Annotation.Type.ISSUE);
                annotation.put(PacmanSdkConstants.DESCRIPTION, "VM Instance with " + severity + " vulnerabilities found");
                annotation.put(PacmanRuleConstants.SEVERITY, severity);
                annotation.put(PacmanRuleConstants.CATEGORY, category);
                String vulnerabilityDetails = getVMVulnerabilityDetails(vulnerabilityInfoList, severity);
                annotation.put("vulnerabilityDetails", vulnerabilityDetails);

                policyResult = new PolicyResult(PacmanSdkConstants.STATUS_FAILURE, PacmanRuleConstants.FAILURE_MESSAGE, annotation);
            } else {
                policyResult = new PolicyResult(PacmanSdkConstants.STATUS_SUCCESS, PacmanRuleConstants.SUCCESS_MESSAGE);
            }
        }
        return policyResult;
    }

    private String getVMVulnerabilityDetails(List<JsonObject> vulnerabilityInfoList, String severity) {
        List<VulnerabilityInfo> vulnerabilityList = new ArrayList<>();
        for (JsonObject vulnerability : vulnerabilityInfoList) {
            for (JsonElement cveDetails : vulnerability.get(severity).getAsJsonArray()) {
                VulnerabilityInfo vul = new VulnerabilityInfo();
                String cveId = cveDetails.getAsJsonObject().get("cves").getAsString();
                CveDetails cve = new CveDetails(cveId, PacmanUtils.NIST_VULN_DETAILS_URL + cveId);
                List<CveDetails> cveList = new ArrayList<>();
                cveList.add(cve);
                vul.setCveList(cveList);
                vul.setVulnerabilityUrl(cveDetails.getAsJsonObject().get("url").getAsString());
                vul.setTitle(cveDetails.getAsJsonObject().get("title").getAsString());
                vulnerabilityList.add(vul);
            }
        }
        ObjectMapper objectMapper = new ObjectMapper();
        try {
            return objectMapper.writeValueAsString(vulnerabilityList);
        } catch (JsonProcessingException e) {
            throw new RuleExecutionFailedExeption(e.getMessage());
        }
    }

    @Override
    public String getHelpText() {
        return null;
    }
}
