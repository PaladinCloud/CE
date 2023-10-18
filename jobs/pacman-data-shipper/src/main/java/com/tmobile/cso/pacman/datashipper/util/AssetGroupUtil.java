/*******************************************************************************
 * Copyright 2023 Paladin Cloud, Inc. or its affiliates. All Rights Reserved.
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License.  You may obtain a copy
 * of the License at
 * <p>
 *   http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  See the
 * License for the specific language governing permissions and limitations under
 * the License.
 ******************************************************************************/
package com.tmobile.cso.pacman.datashipper.util;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

public class AssetGroupUtil {
    private static final Logger LOGGER = LoggerFactory.getLogger(AssetGroupUtil.class);

    private static final String DISTRIBUTION = "distribution";
    private static final String SEVERITY = "severity";
    private static final String COUNT = "count";
    private static final String OUTPUT = "output";
    private static final String TOTAL = "total";
    private static final String COMPLIANT = "compliant";
    private static final String NON_COMPLIANT = "noncompliant";
    private static final String DOMAIN = "domain";

    private AssetGroupUtil() {
        throw new IllegalStateException("AssetGroupUtil is a utility class");
    }

    /**
     * Fetch type counts.
     *
     * @param ag the ag
     * @return the list
     * @throws Exception
     */
    @SuppressWarnings("unchecked")
    public static List<Map<String, Object>> fetchTypeCounts(String ag) throws Exception {
        String url = HttpUtil.getAssetServiceBaseUrl() + "/count?ag=" + ag;
        String typeCountJson = HttpUtil.get(url, AuthManager.getToken());
        Map<String, Object> typeCountMap = Util.parseJson(typeCountJson);
        return  (List<Map<String, Object>>) ((Map<String, Object>) typeCountMap.get("data")).get("assetcount");
    }

    /**
     * Fetch patching compliance.
     *
     * @param api the api
     * @param ag  the ag
     * @return the map
     * @throws Exception
     */
    public static Map<String, Object> fetchPatchingCompliance(String api, String ag) throws Exception {
        Map<String, Object> patchingInfo = new HashMap<>();
        String responseJson = HttpUtil.get(api + "/patching?ag=" + ag, AuthManager.getToken());
        Map<String, Object> vulnMap = Util.parseJson(responseJson);

        @SuppressWarnings("unchecked")
        Map<String, Map<String, Object>> data = (Map<String, Map<String, Object>>) vulnMap.get("data");
        Map<String, Object> output = data.get(OUTPUT);
        if (output != null) {
            patchingInfo.putAll(data.get(OUTPUT));
        }
        return patchingInfo;
    }

    /**
     * Fetch vuln distribution.
     *
     * @param api the api
     * @param ag  the ag
     * @return the list
     * @throws Exception
     */
    @SuppressWarnings("unchecked")
    public static List<Map<String, Object>> fetchVulnDistribution(String api, String ag) throws Exception {
        List<Map<String, Object>> vulnInfo = new ArrayList<>();
        String typeCountJson = HttpUtil.get(api + "/vulnerabilities/distribution?ag=" + ag, AuthManager.getToken());
        Map<String, Object> vulnMap = Util.parseJson(typeCountJson);
        Map<String, List<Map<String, Object>>> data = (Map<String, List<Map<String, Object>>>) vulnMap.get("data");
        List<Map<String, Object>> apps = data.get("response");
        for (Map<String, Object> app : apps) {
            String application = app.get("application").toString();
            List<Map<String, Object>> envs = (List<Map<String, Object>>) app.get("applicationInfo");
            for (Map<String, Object> env : envs) {
                String environment = env.get("environment").toString();
                List<Map<String, Object>> sevinfo = (List<Map<String, Object>>) env.get("severityInfo");
                for (Map<String, Object> sev : sevinfo) {
                    Map<String, Object> vuln = new HashMap<>();
                    vuln.put("tags.Application", application);
                    vuln.put("tags.Environment", environment);
                    vuln.put("severitylevel", sev.get("severitylevel"));
                    vuln.put(SEVERITY, sev.get(SEVERITY));
                    vuln.put(COUNT, sev.get(COUNT));
                    vulnInfo.add(vuln);
                }
            }
        }

        return vulnInfo;
    }

    /**
     * Fetch compliance info.
     *
     * @param ag      the ag
     * @param domains the domains
     * @return the list
     * @throws Exception
     */
    @SuppressWarnings("unchecked")
    public static List<Map<String, Object>> fetchComplianceInfo(String ag, List<String> domains) throws Exception {
        List<Map<String, Object>> compInfo = new ArrayList<>();
        for (String domain : domains) {
            String apiUrl = HttpUtil.getComplianceServiceBaseUrl() + "/overallcompliance?ag=" + ag + "&domain=" + Util.encodeUrl(domain);
            String typeCountJson = HttpUtil
                    .get(apiUrl, AuthManager.getToken());
            Map<String, Object> complianceMap = Util.parseJson(typeCountJson);
            Map<String, Map<String, Object>> data = (Map<String, Map<String, Object>>) complianceMap.get("data");
            Map<String, Object> complianceStats = data.get(DISTRIBUTION);
            if (complianceStats != null) {
                complianceStats.put(DOMAIN, domain);
                compInfo.add(complianceStats);
            }
        }

        return compInfo;
    }

    /**
     * Fetch rule compliance info.
     *
     * @param ag      the ag
     * @param domains the domains
     * @return the list
     * @throws Exception
     */
    public static List<Map<String, Object>> fetchPolicyComplianceInfo(String ag, List<String> domains) throws Exception {
        String url = HttpUtil.getComplianceServiceBaseUrl() + "/noncompliancepolicy";
        List<Map<String, Object>> ruleInfoList = new ArrayList<>();
        for (String domain : domains) {
            String body = "{\"ag\":\"" + ag + "\",\"filter\":{\"domain\":\"" + domain + "\"}}";
            String ruleComplianceInfo = HttpUtil.post(url, body, AuthManager.getToken(), "Bearer");

            JsonObject response = new JsonParser().parse(ruleComplianceInfo).getAsJsonObject();
            JsonArray ruleInfoListJson = response.get("data").getAsJsonObject().get("response").getAsJsonArray();
            for (JsonElement ruleInfoItem : ruleInfoListJson) {
                JsonObject ruleInfoJson = ruleInfoItem.getAsJsonObject();
                Map<String, Object> ruleInfo = new HashMap<>();
                ruleInfo.put(DOMAIN, domain);
                ruleInfo.put("policyId", ruleInfoJson.get("policyId").getAsString());
                ruleInfo.put("compliance_percent", ruleInfoJson.get("compliance_percent").getAsDouble());
                ruleInfo.put(TOTAL, ruleInfoJson.get("assetsScanned").getAsLong());
                ruleInfo.put(COMPLIANT, ruleInfoJson.get("passed").getAsLong());
                ruleInfo.put(NON_COMPLIANT, ruleInfoJson.get("failed").getAsLong());
                ruleInfo.put("contribution_percent", ruleInfoJson.get("contribution_percent").getAsDouble());
                ruleInfo.put("severity", ruleInfoJson.get("severity").getAsString());
                ruleInfo.put("policyCategory", ruleInfoJson.get("policyCategory").getAsString());
                ruleInfoList.add(ruleInfo);
            }
        }

        return ruleInfoList;
    }

    /**
     * Fetch vuln summary.
     *
     * @param ag the ag
     * @return the map
     * @throws Exception
     */
    public static Map<String, Object> fetchVulnSummary(String ag) throws Exception {
        Map<String, Object> vulnSummary = new HashMap<>();
        String url = HttpUtil.getVulnerabilityServiceBaseUrl() + "/vulnerabilites?ag=" + ag;
        String vulnSummaryResponse = HttpUtil.get(url, AuthManager.getToken());
        JsonObject vulnSummaryJson = new JsonParser().parse(vulnSummaryResponse).getAsJsonObject();
        JsonObject vulnJsonObj = vulnSummaryJson.get("data").getAsJsonObject().get(OUTPUT).getAsJsonObject();
        long total = vulnJsonObj.get("hosts").getAsLong();
        long noncompliant = vulnJsonObj.get("totalVulnerableAssets").getAsLong();

        vulnSummary.put(TOTAL, total);
        vulnSummary.put(NON_COMPLIANT, noncompliant);
        vulnSummary.put(COMPLIANT, total - noncompliant);

        return vulnSummary;
    }

    /**
     * Fetch tagging summary.
     *
     * @param ag the ag
     * @return the map
     * @throws Exception
     */
    public static Map<String, Object> fetchTaggingSummary(String ag) throws Exception {
        String url = HttpUtil.getComplianceServiceBaseUrl() + "/tagging?ag=" + ag;
        Map<String, Object> taggingSummary = new HashMap<>();
        String taggingSummaryResponse = HttpUtil.get(url, AuthManager.getToken());
        JsonObject taggingSummaryJson = new JsonParser().parse(taggingSummaryResponse).getAsJsonObject();
        JsonObject taggingJsonObj = taggingSummaryJson.get("data").getAsJsonObject().get(OUTPUT).getAsJsonObject();

        long total = taggingJsonObj.get("assets").getAsLong();
        long noncompliant = taggingJsonObj.get("untagged").getAsLong();
        long compliant = taggingJsonObj.get("tagged").getAsLong();
        taggingSummary.put(TOTAL, total);
        taggingSummary.put(NON_COMPLIANT, noncompliant);
        taggingSummary.put(COMPLIANT, compliant);

        return taggingSummary;
    }

    /**
     * Fetch issues info.
     *
     * @param ag      the ag
     * @param domains the domains
     * @return the list
     * @throws Exception
     */
    public static List<Map<String, Object>> fetchIssuesInfo(String ag, List<String> domains) throws Exception {
        List<Map<String, Object>> issueInfoList = new ArrayList<>();
        Map<String, Object> issuesInfo;
        for (String domain : domains) {
            String url = HttpUtil.getComplianceServiceBaseUrl() + "/issues/distribution?ag=" + ag + "&domain=" + Util.encodeUrl(domain);
            String distributionResponse = HttpUtil
                    .get(url, AuthManager.getToken());
            JsonObject distributionJson = new JsonParser().parse(distributionResponse).getAsJsonObject();
            JsonObject distributionObj = distributionJson.get("data").getAsJsonObject().get(DISTRIBUTION)
                    .getAsJsonObject();
            issuesInfo = new HashMap<>();
            issuesInfo.put(DOMAIN, domain);
            issuesInfo.put(TOTAL, distributionObj.get("total_issues").getAsLong());
            JsonObject distributionSeverity = distributionObj.get("distribution_by_severity").getAsJsonObject();
            JsonObject distributionCategory = distributionObj.get("distribution_policyCategory").getAsJsonObject();

            Set<String> severityKeys = distributionSeverity.keySet();
            for (String severityKey : severityKeys) {
                issuesInfo.put(severityKey, distributionSeverity.get(severityKey).getAsLong());
            }

            Set<String> categoryKeys = distributionCategory.keySet();
            for (String categoryKey : categoryKeys) {
                issuesInfo.put(categoryKey, distributionCategory.get(categoryKey).getAsLong());
            }

            issueInfoList.add(issuesInfo);
        }

        return issueInfoList;
    }

    public static Map<String, Object> fetchAssetCounts(String ag) throws Exception {
        String url = HttpUtil.getAssetServiceBaseUrl() + "/count?ag=" + ag;
        String assetCountJson = HttpUtil.get(url, AuthManager.getToken());
        Map<String, Object> assetCountMap = Util.parseJson(assetCountJson);

        return (Map<String, Object>) assetCountMap.get("data");
    }

    public static String fetchViolationsCount(String platform, String accountId) throws Exception {
        String uri = HttpUtil.getComplianceServiceBaseUrl() + "/issues/distribution?ag=" + platform + "&accountId=" + accountId;
        LOGGER.info("Fetching violation count for account:{} from compliance API: {}", accountId, uri);

        String issuesCountJson = HttpUtil.get(uri, AuthManager.getToken());
        LOGGER.info("Violation data API response:{}", issuesCountJson);

        JsonObject resultJson = new JsonParser().parse(issuesCountJson).getAsJsonObject();
        if (resultJson.getAsJsonObject("data") != null && resultJson.getAsJsonObject("data").getAsJsonObject("distribution") != null) {
            JsonElement element = resultJson.getAsJsonObject("data").getAsJsonObject("distribution").get("total_issues");
            if (element != null) {
                String violationCount = element.getAsString();
                LOGGER.info("Violation count for account:{} is {}", accountId, violationCount);

                return violationCount;
            }
        }

        LOGGER.info("Violation data not found from API, setting count as 0");
        return "0";
    }

    @SuppressWarnings("unchecked")
    public static String fetchAssetCount(String platform, String accountId) throws Exception {
        String uri = HttpUtil.getAssetServiceBaseUrl() + "/count?ag=" + platform + "&accountId=" + accountId;
        LOGGER.info("Fetching asset count for account:{} from assets API: {}", accountId, uri);

        String assetCountJson = HttpUtil.get(uri, AuthManager.getToken());
        LOGGER.info("Asset data API response:{}", assetCountJson);

        JsonObject resultJson = new JsonParser().parse(assetCountJson).getAsJsonObject();
        if (resultJson.getAsJsonObject("data") != null && resultJson.getAsJsonObject("data").get("totalassets") != null) {
            String assetCount = resultJson.getAsJsonObject("data").get("totalassets").getAsString();
            LOGGER.info("Asset count for account:{} is {}", accountId, assetCount);

            return assetCount;
        }

        LOGGER.info("Asset data not found from API, setting count as 0");
        return "0";
    }
}
