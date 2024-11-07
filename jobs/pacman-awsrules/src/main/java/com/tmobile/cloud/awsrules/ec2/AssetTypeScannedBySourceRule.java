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
import com.google.common.collect.HashMultimap;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.tmobile.cloud.awsrules.utils.PacmanUtils;
import com.tmobile.cloud.awsrules.utils.RulesElasticSearchRepositoryUtil;
import com.tmobile.cloud.constants.PacmanRuleConstants;
import com.tmobile.pacman.commons.PacmanSdkConstants;
import com.tmobile.pacman.commons.constants.PluginDisplayName;
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

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@PacmanPolicy(key = "check-asset-scanned-by-source", desc = "checks for Assets scanned by source", severity = PacmanSdkConstants.SEV_HIGH, category = PacmanSdkConstants.GOVERNANCE)
public class AssetTypeScannedBySourceRule extends BasePolicy {

    private static final Logger logger = LoggerFactory.getLogger(AssetTypeScannedBySourceRule.class);
    private static final String VULN_ASSET_LOOKUP_KEY = "asset_lookup_key";
    private static final String SRC_ASSET_KEY = "src_asset_key";
    private static final String ASSET_LOOKUP_INDEX = "asset_lookup_index";
    private static final String VUL_SCAN_DAYS = "minDaysSinceLastVulScan";
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss'Z'");

    public static String checkAgentStatus(JsonObject json, int nDays, String plugin, String pluginService) {
        JsonObject pluginObject = json.getAsJsonObject(plugin);
        if (!pluginObject.has(pluginService) || pluginObject.get(pluginService).isJsonNull()) {
            return PluginDisplayName.getDisplayNameByString(plugin) + " Agent is not installed";
        }
        JsonObject vulnerabilityManagement = pluginObject.getAsJsonObject(pluginService);
        if (!vulnerabilityManagement.has(PacmanRuleConstants.LAST_SCAN_DATE) || vulnerabilityManagement.get(PacmanRuleConstants.LAST_SCAN_DATE).isJsonNull()) {
            return "Unable to determine as last scanned date is not available!!";
        }
        try {
            LocalDateTime lastScanDate = LocalDateTime.parse(vulnerabilityManagement.get(PacmanRuleConstants.LAST_SCAN_DATE).getAsString(), DATE_FORMATTER);
            long daysSinceLastScan = ChronoUnit.DAYS.between(lastScanDate.toLocalDate(), LocalDateTime.now().toLocalDate());
            return daysSinceLastScan > nDays ? PluginDisplayName.getDisplayNameByString(plugin) + " Not scanned since " + nDays + " days!!" : "All checks passed";
        } catch (Exception e) {
            logger.error("Date format error in getting LastScanDate: {}", e.getMessage());
            return null;
        }

    }

    @Override
    public PolicyResult execute(Map<String, String> policyParam, Map<String, String> resourceAttributes) {
        logger.debug("AssetTypeScannedBySourceRule execution started");
        initializeMDC(policyParam);
        validatePolicyConfig(policyParam);
        PolicyResult policyResult = null;
        String firstDiscoveredOn = resourceAttributes.get(PacmanRuleConstants.FIRST_DISCOVERED_ON);
        String discoveredDaysRange = policyParam.get(PacmanRuleConstants.DISCOVERED_DAYS_RANGE);
        if(!StringUtils.isNullOrEmpty(firstDiscoveredOn)){
            firstDiscoveredOn= firstDiscoveredOn.substring(0,PacmanRuleConstants.FIRST_DISCOVERED_DATE_FORMAT_LENGTH);
        }
        if ("true".equalsIgnoreCase(resourceAttributes.get("_isActive")) &&
                PacmanUtils.calculateLaunchedDuration(firstDiscoveredOn)>Long.parseLong(discoveredDaysRange)) {
            policyResult = processPolicy(policyParam, resourceAttributes);
        }
        return policyResult != null ? policyResult : createSuccessResult();
    }

    private void initializeMDC(Map<String, String> policyParam) {
        MDC.put("executionId", policyParam.get("executionId"));
        MDC.put("policyId", policyParam.get(PacmanSdkConstants.POLICY_ID));
    }

    private void validatePolicyConfig(Map<String, String> policyParam) {
        if (!PacmanUtils.doesAllHaveValue(policyParam.get(PacmanRuleConstants.CATEGORY), policyParam.get(PacmanRuleConstants.SEVERITY))) {
            logger.info(PacmanRuleConstants.MISSING_CONFIGURATION);
            throw new InvalidInputException(PacmanRuleConstants.MISSING_CONFIGURATION);
        }
    }

    private String getElasticSearchURL(String source, String targetType) {
        return PacmanUtils.getPacmanHost(PacmanRuleConstants.ES_URI) + "/" + source + "_" + targetType + "_opinions/_search";
    }

    private String getDocID(Map<String, String> resourceAttributes, String srcAssetKey) {
        return StringUtils.trim(ObjectUtils.firstNonNull(resourceAttributes.get(srcAssetKey), resourceAttributes.get("_docid")));
    }

    private String getInstanceID(Map<String, String> resourceAttributes, String srcAssetKey) {
        return StringUtils.trim(ObjectUtils.firstNonNull(resourceAttributes.get(srcAssetKey), resourceAttributes.get(PacmanRuleConstants.RESOURCE_ID)));
    }

    private PolicyResult processPolicy(Map<String, String> policyParam, Map<String, String> resourceAttributes) {
        String agentStatusMessage = null;
        String srcAssetKey = policyParam.get(SRC_ASSET_KEY);
        String plugin = policyParam.get("pluginName");
        // @TODO: Once all plugins are converted to Opinion, remove the condition below
        if (PacmanRuleConstants.QUALYS.equalsIgnoreCase(plugin)) {
            String targetType = policyParam.get(PacmanSdkConstants.TARGET_TYPE);
            String source = policyParam.get(PacmanSdkConstants.DATA_SOURCE_KEY);
            String opinionURL = getElasticSearchURL(source, targetType);
            String docID = getDocID(resourceAttributes, srcAssetKey);
            String pluginService = policyParam.get("pluginServiceName");
            int scanDays = Integer.parseInt(policyParam.get(VUL_SCAN_DAYS));
            try {
                Map<String, Object> mustFilter = createFilterMap(docID, policyParam.get(PacmanSdkConstants.TARGET_TYPE));
                JsonObject opinionObject = getOpinionsObject(opinionURL, mustFilter);
                if (opinionObject == null || opinionObject.isJsonNull()) {
                    agentStatusMessage = PluginDisplayName.getDisplayNameByString(plugin) + " Agent is not installed";
                } else {
                    agentStatusMessage = checkAgentStatus(opinionObject, scanDays, plugin, pluginService);
                }

                if ("All checks passed".equalsIgnoreCase(agentStatusMessage)) {
                    return createSuccessResult();
                }
            } catch (Exception e) {
                logger.error("Error during policy execution", e);
                throw new RuleExecutionFailedExeption("Unable to determine: " + e.getMessage(), e);
            }
        } else {
            String assetLookupIndex = policyParam.get(ASSET_LOOKUP_INDEX);
            String instanceID = getInstanceID(resourceAttributes, srcAssetKey);
            String vulnAssetLookupKey = policyParam.get(VULN_ASSET_LOOKUP_KEY);
            return processNonQualysPlugin(instanceID, assetLookupIndex, vulnAssetLookupKey, policyParam);
        }

        return agentStatusMessage != null ? createFailureResult(policyParam, agentStatusMessage) : null;
    }

    private Map<String, Object> createFilterMap(String docID, String targetType) {
        Map<String, Object> mustFilter = new HashMap<>();
        mustFilter.put("_docId.keyword", docID);
        mustFilter.put("_docType", targetType);
        return mustFilter;
    }

    private PolicyResult processNonQualysPlugin(String instanceId, String vulnerabilitiesEndpoint, String vulnAssetLookupKey, Map<String, String> policyParam) {
        List<JsonObject> vulAssetList;
        try {
            vulAssetList = PacmanUtils.matchAssetAgainstSourceVulnIndex(instanceId, vulnerabilitiesEndpoint, vulnAssetLookupKey, null);
            if (CollectionUtils.isNullOrEmpty(vulAssetList)) {
                return createFailureResult(policyParam, PacmanRuleConstants.FAILURE_MESSAGE);
            }
        } catch (Exception e) {
            logger.error("Error processing vul plugin", e);
            throw new RuleExecutionFailedExeption("Unable to determine: " + e.getMessage(), e);
        }
        return null;
    }

    private PolicyResult createSuccessResult() {
        return new PolicyResult(PacmanSdkConstants.STATUS_SUCCESS, PacmanRuleConstants.SUCCESS_MESSAGE);
    }

    private PolicyResult createFailureResult(Map<String, String> policyParam, String failureMessage) {
        Annotation annotation = Annotation.buildAnnotation(policyParam, Annotation.Type.ISSUE);
        annotation.put(PacmanSdkConstants.DESCRIPTION, policyParam.get("targetTypeDisplayName") + " not scanned by " + PluginDisplayName.getDisplayNameByString(policyParam.get("pluginName")));
        annotation.put(PacmanRuleConstants.SEVERITY, policyParam.get(PacmanRuleConstants.SEVERITY));
        annotation.put(PacmanRuleConstants.CATEGORY, policyParam.get(PacmanRuleConstants.CATEGORY));
        return new PolicyResult(PacmanSdkConstants.STATUS_FAILURE, failureMessage, annotation);
    }

    public JsonObject getOpinionsObject(String esURL, Map<String, Object> mustFilter) throws Exception {
        logger.info("Validating resource data from Elasticsearch. URL: {}, Filter: {}", esURL, mustFilter);
        JsonObject resultJson = RulesElasticSearchRepositoryUtil.getQueryDetailsFromES(esURL, mustFilter, new HashMap<>(), HashMultimap.create(), null, 0, new HashMap<>(), null, null);
        logger.debug("Data fetched from Elasticsearch: {}", resultJson);

        JsonArray hitsJsonArray = resultJson.getAsJsonObject(PacmanRuleConstants.HITS).getAsJsonArray(PacmanRuleConstants.HITS);
        if (hitsJsonArray.size() > 0) {
            return hitsJsonArray.get(0).getAsJsonObject().getAsJsonObject(PacmanRuleConstants.SOURCE).getAsJsonObject(PacmanRuleConstants.ASSET_OPINIONS);
        }
        return null;
    }

    @Override
    public String getHelpText() {
        return null;
    }
}
