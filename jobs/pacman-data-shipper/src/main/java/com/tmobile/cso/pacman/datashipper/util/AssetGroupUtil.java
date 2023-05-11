package com.tmobile.cso.pacman.datashipper.util;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

/**
 * The Class AssetGroupUtil.
 */
public class AssetGroupUtil {

    /** The Constant log. */
    private static final Logger LOGGER = LoggerFactory.getLogger(AssetGroupUtil.class);

    private static final String DISTRIBUTION = "distribution";
    private static final String SEVERITY = "severity";
    private static final String COUNT = "count";
    private static final String OUTPUT = "output" ;
    private static final String TOTAL = "total" ;
    private static final String COMPLIANT = "compliant";
    private static final String NON_COMPLIANT = "noncompliant" ;
    private static final String DOMAIN = "domain";
    private AssetGroupUtil(){
        
    }
    /**
     * Fetch asset groups.
     *
     * @param asstApiUri
     *            the asst api uri
     * @return the map
     * @throws Exception 
     */
    @SuppressWarnings("unchecked")
    public static Map<String, List<String>> fetchAssetGroups(String asstApiUri,String token) throws Exception {
        String assetGroupJson = HttpUtil.get(asstApiUri + "/list/assetgroup",token);
        Map<String, Object> assetInfoMap = Util.parseJson(assetGroupJson);
        Map<String, List<String>> assetGroups = new HashMap<>();
        if (!assetInfoMap.isEmpty()) {
            assetGroups = ((List<Map<String, Object>>) assetInfoMap.get("data")).stream().collect(
                    Collectors.toMap(obj -> obj.get("name").toString(), obj -> (List<String>) obj.get("domains")));
        }
        return assetGroups;
    }

    /**
     * Fetch type counts.
     *
     * @param asstApiUri
     *            the asst api uri
     * @param ag
     *            the ag
     * @return the list
     * @throws Exception 
     */
    @SuppressWarnings("unchecked")
    public static List<Map<String, Object>> fetchTypeCounts(String asstApiUri, String ag,String token) throws Exception {

        String typeCountJson = HttpUtil.get(asstApiUri + "/count?ag=" + ag,token);
        Map<String, Object> typeCountMap = Util.parseJson(typeCountJson);
        List<Map<String, Object>> typeCounts = new ArrayList<>();
        try {
            typeCounts = (List<Map<String, Object>>) ((Map<String, Object>) typeCountMap.get("data")).get("assetcount");
        } catch (Exception e) {
            throw e;
        }
        return typeCounts;
    }
 
    /**
     * Fetch patching compliance.
     *
     * @param api
     *            the api
     * @param ag
     *            the ag
     * @return the map
     * @throws Exception 
     */

    public static Map<String, Object> fetchPatchingCompliance(String api, String ag ,String token) throws Exception {
        Map<String, Object> patchingInfo = new HashMap<>();
        try {
            String responseJson = HttpUtil.get(api + "/patching?ag=" + ag,token);
            Map<String, Object> vulnMap = Util.parseJson(responseJson);
            @SuppressWarnings("unchecked")
            Map<String, Map<String, Object>> data = (Map<String, Map<String, Object>>) vulnMap.get("data");
            Map<String, Object> output = data.get(OUTPUT);
            if (output != null)
                patchingInfo.putAll(data.get(OUTPUT));
        } catch (Exception e) {
            throw e;
        }
        return patchingInfo;
    }

    /**
     * Fetch vuln distribution.
     *
     * @param api
     *            the api
     * @param ag
     *            the ag
     * @return the list
     * @throws Exception 
     */
    @SuppressWarnings("unchecked")
    public static List<Map<String, Object>> fetchVulnDistribution(String api, String ag ,String token) throws Exception {

        List<Map<String, Object>> vulnInfo = new ArrayList<>();
        try {
            String typeCountJson = HttpUtil.get(api + "/vulnerabilities/distribution?ag=" + ag,token);
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

        } catch (Exception e) {
            throw e;
        }
        return vulnInfo;
    }

    /**
     * Fetch compliance info.
     *
     * @param apiUrl
     *            the api url
     * @param ag
     *            the ag
     * @param domains
     *            the domains
     * @return the list
     * @throws Exception 
     */
    @SuppressWarnings("unchecked")
    public static List<Map<String, Object>> fetchComplianceInfo(String apiUrl, String ag, List<String> domains ,String token) throws Exception {
        List<Map<String, Object>> compInfo = new ArrayList<>();
        try {
            for (String domain : domains) {
                String typeCountJson = HttpUtil
                        .get(apiUrl + "/overallcompliance?ag=" + ag + "&domain=" + Util.encodeUrl(domain),token);
                Map<String, Object> complianceMap = Util.parseJson(typeCountJson);
                Map<String, Map<String, Object>> data = (Map<String, Map<String, Object>>) complianceMap.get("data");
                Map<String, Object> complianceStats = data.get(DISTRIBUTION);
                if (complianceStats != null) {
                    complianceStats.put(DOMAIN, domain);
                    compInfo.add(complianceStats);
                }
            }
        } catch (Exception e) {
            throw e;
        }
        return compInfo;
    }


    /**
     * Fetch rule compliance info.
     *
     * @param compapiuri
     *            the compapiuri
     * @param ag
     *            the ag
     * @param domains
     *            the domains
     * @return the list
     * @throws Exception 
     */
    public static List<Map<String, Object>> fetchPolicyComplianceInfo(String compapiuri, String ag,
            List<String> domains ,String token) throws Exception {

        List<Map<String, Object>> ruleInfoList = new ArrayList<>();
        try {
            for (String domain : domains) {
                String ruleCommplianceInfo = HttpUtil.post(compapiuri + "/noncompliancepolicy",
                        "{\"ag\":\"" + ag + "\",\"filter\":{\"domain\":\"" + domain + "\"}}",token,"Bearer");
                JsonObject response = new JsonParser().parse(ruleCommplianceInfo).getAsJsonObject();
                JsonArray ruleInfoListJson = response.get("data").getAsJsonObject().get("response").getAsJsonArray();
                JsonObject ruleinfoJson;
                Map<String, Object> ruleInfo;
                for (JsonElement _ruleinfo : ruleInfoListJson) {
                    ruleinfoJson = _ruleinfo.getAsJsonObject();
                    ruleInfo = new HashMap<>();
                    ruleInfo.put(DOMAIN, domain);
                    ruleInfo.put("policyId", ruleinfoJson.get("policyId").getAsString());
                    ruleInfo.put("compliance_percent", ruleinfoJson.get("compliance_percent").getAsDouble());
                    ruleInfo.put(TOTAL, ruleinfoJson.get("assetsScanned").getAsLong());
                    ruleInfo.put(COMPLIANT, ruleinfoJson.get("passed").getAsLong());
                    ruleInfo.put(NON_COMPLIANT, ruleinfoJson.get("failed").getAsLong());
                    ruleInfo.put("contribution_percent", ruleinfoJson.get("contribution_percent").getAsDouble());
                    ruleInfo.put("severity",ruleinfoJson.get("severity").getAsString());
                    ruleInfo.put("policyCategory",ruleinfoJson.get("policyCategory").getAsString());
                    ruleInfoList.add(ruleInfo);
                }
            }
        } catch (Exception e) {
            throw e;
        }
        return ruleInfoList;
    }

    /**
     * Fetch vuln summary.
     *
     * @param compapiuri
     *            the compapiuri
     * @param ag
     *            the ag
     * @return the map
     * @throws Exception 
     */
    public static Map<String, Object> fetchVulnSummary(String compapiuri, String ag ,String token) throws Exception {
        Map<String, Object> vulnSummary = new HashMap<>();
        try {
            String vulnSummaryResponse = HttpUtil.get(compapiuri + "/vulnerabilites?ag=" + ag,token);
            JsonObject vulnSummaryJson = new JsonParser().parse(vulnSummaryResponse).getAsJsonObject();
            JsonObject vulnJsonObj = vulnSummaryJson.get("data").getAsJsonObject().get(OUTPUT).getAsJsonObject();
            long total = vulnJsonObj.get("hosts").getAsLong();
            long noncompliant = vulnJsonObj.get("totalVulnerableAssets").getAsLong();
            vulnSummary.put(TOTAL, total);
            vulnSummary.put(NON_COMPLIANT, noncompliant);
            vulnSummary.put(COMPLIANT, total - noncompliant);
        } catch (Exception e) {
            throw e;
        }
        return vulnSummary;
    }

    /**
     * Fetch tagging summary.
     *
     * @param compapiuri
     *            the compapiuri
     * @param ag
     *            the ag
     * @return the map
     * @throws Exception 
     */
    public static Map<String, Object> fetchTaggingSummary(String compapiuri, String ag ,String token) throws Exception {
        Map<String, Object> taggingSummary = new HashMap<>();
        try {
            String taggingSummaryResponse = HttpUtil.get(compapiuri + "/tagging?ag=" + ag,token);
            JsonObject taggingSummaryJson = new JsonParser().parse(taggingSummaryResponse).getAsJsonObject();
            JsonObject taggingJsonObj = taggingSummaryJson.get("data").getAsJsonObject().get(OUTPUT)
                    .getAsJsonObject();
            long total = taggingJsonObj.get("assets").getAsLong();
            long noncompliant = taggingJsonObj.get("untagged").getAsLong();
            long compliant = taggingJsonObj.get("tagged").getAsLong();
            taggingSummary.put(TOTAL, total);
            taggingSummary.put(NON_COMPLIANT, noncompliant);
            taggingSummary.put(COMPLIANT, compliant);
        } catch (Exception e) {
            throw e;
        }
        return taggingSummary;
    }

    /**
     * Fetch cert summary.
     *
     * @param compapiuri
     *            the compapiuri
     * @param ag
     *            the ag
     * @return the map
     * @throws Exception 
     */
    public static Map<String, Object> fetchCertSummary(String compapiuri, String ag ,String token) throws Exception {
        Map<String, Object> certSummary = new HashMap<>();
        try {
            String certSummaryResponse = HttpUtil.get(compapiuri + "/certificates?ag=" + ag,token);
            JsonObject certSummaryJson = new JsonParser().parse(certSummaryResponse).getAsJsonObject();
            JsonObject certJsonObj = certSummaryJson.get("data").getAsJsonObject().get(OUTPUT).getAsJsonObject();
            long total = certJsonObj.get("certificates").getAsLong();
            long noncompliant = certJsonObj.get("certificates_expiring").getAsLong();
            long compliant = total - noncompliant;
            certSummary.put(TOTAL, total);
            certSummary.put(NON_COMPLIANT, noncompliant);
            certSummary.put(COMPLIANT, compliant);
        } catch (Exception e) {
            throw e;
        }
        return certSummary;
    }

    /**
     * Fetch issues info.
     *
     * @param compapiuri
     *            the compapiuri
     * @param ag
     *            the ag
     * @param domains
     *            the domains
     * @return the list
     * @throws Exception 
     */
    public static List<Map<String, Object>> fetchIssuesInfo(String compapiuri, String ag, List<String> domains ,String token) throws Exception {
        List<Map<String, Object>> issueInfoList = new ArrayList<>();
        Map<String, Object> issuesInfo;
        try {
            for (String domain : domains) {
                String distributionResponse = HttpUtil
                        .get(compapiuri + "/issues/distribution?ag=" + ag + "&domain=" + Util.encodeUrl(domain),token);
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

        } catch (Exception e) {
            throw e;
        }
        return issueInfoList;
    }

    public static Map<String, Object> fetchAssetCounts(String asstApiUri, String ag, String token) throws Exception {

        String assetCountJson = HttpUtil.get(asstApiUri + "/count?ag=" + ag,token);
        Map<String, Object> assetCountMap = Util.parseJson(assetCountJson);
        Map<String, Object> assetCounts = new HashMap<>();
        try {
            assetCounts=(Map<String, Object>) assetCountMap.get("data");
        } catch (Exception e) {
            throw e;
        }
        return assetCounts;
    }
    public static String fetchViolationsCount(String compApiUrl, String token, String platform, String accountId) throws Exception {
        String uri = compApiUrl + "/issues/distribution?ag=" + platform + "&accountId=" + accountId;
        LOGGER.info("Fetching violation count for account:{} from compliance API: {}",accountId,uri);
        String issuesCountJson = HttpUtil.get(uri,token);
        LOGGER.info("Violation data API response:{}",issuesCountJson);
        JsonObject resultJson = new JsonParser().parse(issuesCountJson).getAsJsonObject();
        if(resultJson.getAsJsonObject("data")!=null && resultJson.getAsJsonObject("data").getAsJsonObject("distribution")!=null){
            JsonElement element = resultJson.getAsJsonObject("data").getAsJsonObject("distribution").get("total_issues");
            if(element!=null){
                String violationCount = element.getAsString();
                LOGGER.info("Violation count for account:{} is {}", accountId,violationCount);
                return violationCount;
            }
        }
        LOGGER.info("Violation data not found from API, setting count as 0");
        return "0";
    }
    @SuppressWarnings("unchecked")
    public static String fetchAssetCount(String asstApiUri, String token, String platform, String accountId) throws Exception {
        String uri = asstApiUri + "/count?ag=" + platform + "&accountId=" + accountId;
        LOGGER.info("Fetching asset count for account:{} from assets API: {}",accountId,uri);
        String assetCountJson = HttpUtil.get(uri,token);
        LOGGER.info("Asset data API response:{}",assetCountJson);
        JsonObject resultJson = new JsonParser().parse(assetCountJson).getAsJsonObject();
        if(resultJson.getAsJsonObject("data")!=null && resultJson.getAsJsonObject("data").get("totalassets")!=null){
            String assetCount =  resultJson.getAsJsonObject("data").get("totalassets").getAsString();
            LOGGER.info("Asset count for account:{} is {}", accountId,assetCount);
            return assetCount;
        }
        LOGGER.info("Asset data not found from API, setting count as 0");
        return "0";
    }
}
