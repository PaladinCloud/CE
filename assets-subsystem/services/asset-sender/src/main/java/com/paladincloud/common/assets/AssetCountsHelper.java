package com.paladincloud.common.assets;

import static java.util.Map.entry;

import com.paladincloud.common.auth.AuthHelper;
import com.paladincloud.common.aws.DatabaseHelper;
import com.paladincloud.common.config.ConfigConstants.PaladinCloud;
import com.paladincloud.common.config.ConfigService;
import com.paladincloud.common.util.HttpHelper;
import com.paladincloud.common.util.HttpHelper.AuthorizationType;
import com.paladincloud.common.util.JsonHelper;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.inject.Inject;

public class AssetCountsHelper {
    private static final String ASSET_SERVICE_BASE_PATH = "/asset/v1";
    private static final String COMPLIANCE_SERVICE_BASE_PATH = "/compliance/v1";
    private static final String DOMAIN = "domain";

    private final AuthHelper authHelper;
    private final DatabaseHelper databaseHelper;
    private Map<String, Integer> categoryWeightageMap = null;

    @Inject
    public AssetCountsHelper (AuthHelper authHelper, DatabaseHelper databaseHelper) {
        this.authHelper = authHelper;
        this.databaseHelper = databaseHelper;
    }

    public List<Map<String, Object>> fetchTypeCounts(String assetGroup) throws Exception {
        var url = buildPaladinApiUrl(ASSET_SERVICE_BASE_PATH, STR."/count?ag=\{assetGroup}");
        var headers = HttpHelper.getBasicHeaders(AuthorizationType.BEARER, authHelper.getToken());
        headers.put("cache-control", "no-cache");
        var typeCountMap = JsonHelper.mapFromString(HttpHelper.get(url, headers));
        @SuppressWarnings("unchecked") var response = (List<Map<String, Object>>) ((Map<String, Object>) typeCountMap.get(
            "data")).get("assetcount");
        return response;
    }

    public List<Map<String, Object>> fetchPolicyCompliance(String assetGroup, List<String> domains)
        throws Exception {
        var policyCompliance = new ArrayList<Map<String, Object>>();
        var url = buildPaladinApiUrl(COMPLIANCE_SERVICE_BASE_PATH, "/noncompliancepolicy");
        for (var domain : domains) {
            var body = STR."""
                {
                    "ag": "\{assetGroup}",
                    "filter": {
                        "domain": "\{domain}"
                    }
                }
                """.trim();
            var response = JsonHelper.mapFromString(
                HttpHelper.post(url, body, AuthorizationType.BEARER, authHelper.getToken()));

            @SuppressWarnings("unchecked") var infoList = (List<Map<String, Object>>) ((Map<String, Object>) response.get(
                "data")).get("response");
            var x = infoList.stream().map(item -> new HashMap<>(
                Map.ofEntries(entry(DOMAIN, domain), entry("policyId", item.get("policyId")),
                    entry("compliance_percent", item.get("compliance_percent")),
                    entry("total", item.get("assetsScanned")),
                    entry("compliant", item.get("passed")),
                    entry("noncompliant", item.get("failed")),
                    entry("severity", item.get("severity")),
                    entry("policyCategory", item.get("policyCategory"))))).toList();
            policyCompliance.addAll(x);
        }
        return policyCompliance;
    }

    public List<Map<String, Object>> fetchCompliance(String assetGroup, List<String> domains)
        throws Exception {
        List<Map<String, Object>> compInfo = new ArrayList<>();
        for (var domain : domains) {
            var url = buildPaladinApiUrl(COMPLIANCE_SERVICE_BASE_PATH,
                STR."/overallcompliance?ag=\{assetGroup}&domain=\{URLEncoder.encode(domain,
                    StandardCharsets.UTF_8)}");
            var complianceResponse = JsonHelper.mapFromString(HttpHelper.get(url,
                HttpHelper.getBasicHeaders(AuthorizationType.BEARER, authHelper.getToken())));
            @SuppressWarnings("unchecked") var complianceStats = ((Map<String, Map<String, Object>>) complianceResponse.get(
                "data")).get("distribution");
            int numerator = 0;
            int denominator = 0;
            for (var entry : complianceStats.entrySet()) {
                var weight = getCategoryWeightedMap().getOrDefault(entry.getKey(), null);
                if (weight != null && entry.getValue() != null) {
                    numerator += Integer.parseInt(entry.getValue().toString()) * weight;
                    denominator += weight;
                }
            }
            if (denominator > 0) {
                complianceStats.put("overall", numerator / denominator);
            } else {
                complianceStats.put("overall", 0);
            }
            complianceStats.put(DOMAIN, domain);
            compInfo.add(complianceStats);
        }
        return compInfo;
    }

    public Map<String, Object> fetchTaggingSummary(String assetGroup) throws Exception {
        var url = buildPaladinApiUrl(COMPLIANCE_SERVICE_BASE_PATH, STR."/tagging?ag=\{assetGroup}");
        var taggingResponse = JsonHelper.mapFromString(HttpHelper.get(url,
            HttpHelper.getBasicHeaders(AuthorizationType.BEARER, authHelper.getToken())));
        @SuppressWarnings("unchecked") var taggingStats = ((Map<String, Map<String, Object>>) taggingResponse.get(
            "data")).get("output");
        var total = (int) taggingStats.get("assets");
        var nonCompliant = (int) taggingStats.get("untagged");
        var compliant = (int) taggingStats.get("tagged");
        return new HashMap<>(
            Map.ofEntries(entry("total", total), entry("noncompliant", nonCompliant),
                entry("compliant", compliant)));
    }


    @SuppressWarnings("unchecked")
    public List<Map<String, Object>> fetchIssuesInfo(String assetGroup, List<String> domains)
        throws Exception {
        var issuesList = new ArrayList<Map<String, Object>>();
        for (var domain : domains) {
            var url = buildPaladinApiUrl(COMPLIANCE_SERVICE_BASE_PATH,
                STR."/issues/distribution?ag=\{assetGroup}&domain=\{URLEncoder.encode(domain,
                    StandardCharsets.UTF_8)}");
            var distributionResponse = JsonHelper.mapFromString(HttpHelper.get(url,
                HttpHelper.getBasicHeaders(AuthorizationType.BEARER, authHelper.getToken())));
            @SuppressWarnings("unchecked") var distribution = ((Map<String, Map<String, Object>>) distributionResponse.get(
                "data")).get("distribution");
            var issue = new HashMap<String, Object>();
            issue.put("domain", domain);
            issue.put("total", distribution.get("total_issues"));

            issue.putAll(
                (Map<String, Map<String, Object>>) distribution.get("distribution_by_severity"));
            issue.putAll(
                (Map<String, Map<String, Object>>) distribution.get("distribution_policyCategory"));
            issuesList.add(issue);
        }
        return issuesList;
    }

    @SuppressWarnings("unchecked")
    public Map<String, Object> fetchAssetCounts(String assetGroup) throws Exception {
        var url = buildPaladinApiUrl(ASSET_SERVICE_BASE_PATH, STR."/count?ag=\{assetGroup}");
        var response = JsonHelper.mapFromString(HttpHelper.get(url,
            HttpHelper.getBasicHeaders(AuthorizationType.BEARER, authHelper.getToken())));
        return (Map<String, Object>) response.get("data");
    }

    public int fetchAccountAssetCount(String platform, String accountId) throws Exception {
        var url = buildPaladinApiUrl(ASSET_SERVICE_BASE_PATH,
            STR."/count?ag=\{platform}&accountId=\{accountId}");
        var response = JsonHelper.mapFromString(HttpHelper.get(url,
            HttpHelper.getBasicHeaders(AuthorizationType.BEARER, authHelper.getToken())));
        @SuppressWarnings("unchecked") var data = (Map<String, Object>) response.get("data");
        if (data != null && data.containsKey("totalassets")) {
            return (int) data.get("totalassets");
        }
        return 0;
    }

    private String buildPaladinApiUrl(String servicePath, String additional) {
        return STR."\{ConfigService.get(
            PaladinCloud.BASE_PALADIN_CLOUD_API_URI)}\{servicePath}\{additional}";

    }

    private Map<String, Integer> getCategoryWeightedMap() {
        if (categoryWeightageMap == null) {
            var temp = new HashMap<String, Integer>();
            databaseHelper.executeQuery("SELECT * FROM cf_PolicyCategoryWeightage").forEach(
                row -> temp.put(row.get("policyCategory"), Integer.parseInt(row.get("weightage"))));
            categoryWeightageMap = temp;
        }
        return categoryWeightageMap;
    }
}
