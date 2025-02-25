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
import com.google.gson.JsonArray;
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
import org.apache.commons.lang3.ObjectUtils;
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
    private static final String SRC_ASSET_KEY = "src_asset_key";
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
        String severityMatchCriteria = ruleParam.get(PacmanRuleConstants.SEVERITY_MATCH_CRITERIA);
        String vulnerabilityIndex = ruleParam.get(VULNERABILITY_INDEX);
        String vulnAssetLookupKey = ruleParam.get(VULN_ASSET_LOOKUP_KEY);
        /* this param determines the asset key in source index. for eg. for azure_virtualmachine asset key is vmId  */
        String srcAssetKey = ruleParam.get(SRC_ASSET_KEY);
        String vulnerabilitiesEndpoint = PacmanUtils.getPacmanHost(PacmanRuleConstants.ES_URI) + "/" + vulnerabilityIndex + "/_search";
        if (!PacmanUtils.doesAllHaveValue(category, severity)) {
            logger.info(PacmanRuleConstants.MISSING_CONFIGURATION);
            throw new InvalidInputException(PacmanRuleConstants.MISSING_CONFIGURATION);
        }
        PolicyResult policyResult = null;
        if (resourceAttributes != null) {
            String instanceId = StringUtils.trim(ObjectUtils.firstNonNull(resourceAttributes.get(srcAssetKey), resourceAttributes.get(PacmanRuleConstants.RESOURCE_ID)));
            List<JsonObject> vulnerabilityInfoList = new ArrayList<>();
            try {
                vulnerabilityInfoList = PacmanUtils.matchAssetAgainstSourceVulnIndex(instanceId, vulnerabilitiesEndpoint, vulnAssetLookupKey, null);
            } catch (Exception e) {
                logger.error("unable to determine", e);
                throw new RuleExecutionFailedExeption("unable to determine" + e);
            }

            if (!CollectionUtils.isNullOrEmpty(vulnerabilityInfoList)) {
                String vulnerabilityDetails = getVMVulnerabilityDetails(vulnerabilityInfoList, severityMatchCriteria);
                if (!vulnerabilityDetails.equals("[]")) {
                    Annotation annotation = Annotation.buildAnnotation(ruleParam, Annotation.Type.ISSUE);
                    annotation.put(PacmanSdkConstants.DESCRIPTION, "VM Instance with " + severity + " vulnerabilities found");
                    annotation.put(PacmanRuleConstants.SEVERITY, severity);
                    annotation.put(PacmanRuleConstants.CATEGORY, category);
                    annotation.put("vulnerabilityDetails", vulnerabilityDetails);

                    policyResult = new PolicyResult(PacmanSdkConstants.STATUS_FAILURE, PacmanRuleConstants.FAILURE_MESSAGE, annotation);
                }
            }

            if (policyResult == null) {
                policyResult = new PolicyResult(PacmanSdkConstants.STATUS_SUCCESS, PacmanRuleConstants.SUCCESS_MESSAGE);
            }
        }
        return policyResult;
    }

    private String getVMVulnerabilityDetails(List<JsonObject> vulnerabilityInfoList, String severity) {
        List<VulnerabilityInfo> vulnerabilityList = new ArrayList<>();
        for (JsonObject vulnerability : vulnerabilityInfoList) {
            if (vulnerability.get(severity) != null) {
                for (JsonElement cveDetails : vulnerability.get(severity).getAsJsonArray()) {
                    VulnerabilityInfo vul = new VulnerabilityInfo();
                    if (cveDetails.getAsJsonObject().get("url") != null) {
                        vul.setVulnerabilityUrl(cveDetails.getAsJsonObject().get("url").getAsString());
                    }
                    if (cveDetails.getAsJsonObject().get("title") != null) {
                        vul.setTitle(cveDetails.getAsJsonObject().get("title").getAsString());
                    }
                    /* If there is a list of cve IDs group it under this vulnerability info */
                    JsonElement cves = cveDetails.getAsJsonObject().get("cves");
                    if (cves != null) {
                        if (cves.isJsonArray()) {
                            JsonArray cveArray = cves.getAsJsonArray();
                            List<CveDetails> cveList = new ArrayList<>();
                            for (JsonElement e : cveArray) {
                                JsonObject vulnInfo = e.getAsJsonObject();
                                CveDetails cve = new CveDetails(vulnInfo.get("title").getAsString(), vulnInfo.get("url").getAsString());
                                cveList.add(cve);
                                vul.setCveList(cveList);
                            }
                        } else if (!StringUtils.isNullOrEmpty(cves.getAsString())) {
                            String cveId = cves.getAsString();
                            CveDetails cve = new CveDetails(cveId, PacmanUtils.NIST_VULN_DETAILS_URL + cveId);
                            List<CveDetails> cveList = new ArrayList<>(1);
                            cveList.add(cve);
                            vul.setCveList(cveList);
                        }
                    }
                    vulnerabilityList.add(vul);
                }
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
