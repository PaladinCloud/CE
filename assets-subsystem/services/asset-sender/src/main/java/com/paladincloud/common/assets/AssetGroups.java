package com.paladincloud.common.assets;

import static java.util.Map.entry;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.paladincloud.common.auth.AuthHelper;
import com.paladincloud.common.aws.DatabaseHelper;
import com.paladincloud.common.config.ConfigConstants.PaladinCloud;
import com.paladincloud.common.config.ConfigService;
import com.paladincloud.common.errors.JobException;
import com.paladincloud.common.search.ElasticAliasResponse;
import com.paladincloud.common.search.ElasticSearchHelper;
import com.paladincloud.common.search.ElasticSearchHelper.HttpMethod;
import com.paladincloud.common.util.HttpHelper;
import com.paladincloud.common.util.HttpHelper.AuthorizationType;
import com.paladincloud.common.util.JsonHelper;
import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;
import javax.inject.Inject;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class AssetGroups {

    private static final Logger LOGGER = LogManager.getLogger(AssetGroups.class);
    private static final String ASSET_GROUP_FOR_ALL_SOURCES = "all-sources";
    private static final String ASSET_SERVICE_BASE_PATH = "/asset/v1";
    private static final String COMPLIANCE_SERVICE_BASE_PATH = "/compliance/v1";
    private static final String ROW_EXISTS = "row_exists";
    private static final String ES_ATTRIBUTE_TAG = "tags.";
    private static final String ES_ATTRIBUTE_KEYWORD = ".keyword";
    private static final String UPDATE_ALIAS_TEMPLATE = """
        {
            "actions": [{
                "add":
                    {
                        %s "index": "%s",
                        "alias": "%s"
                    }
                }
            ]
        }
        """.trim();
    private static final String DOMAIN = "domain";
    private static final Map<String, List<Map<String, String>>> databaseCache = new HashMap<>();
    private static final List<String> dataSourceCache = new ArrayList<>();
    private static final Map<String, List<Map<String, String>>> assetGroupTagsCache = new HashMap<>();
    private final ElasticSearchHelper elasticSearch;
    private final DatabaseHelper database;
    private final AuthHelper authHelper;
    private final ObjectMapper objectMapper = new ObjectMapper();
    private Map<String, Integer> categoryWeightageMap = null;

    @Inject
    public AssetGroups(ElasticSearchHelper elasticSearch, DatabaseHelper database,
        AuthHelper authHelper) {
        this.elasticSearch = elasticSearch;
        this.database = database;
        this.authHelper = authHelper;
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
        @SuppressWarnings("unchecked")
        var data = (Map<String, Object>) response.get("data");
        if (data != null && data.containsKey("totalassets")) {
            return (int) data.get("totalassets");
        }
        return 0;
    }

    public void createDefaultGroup(String dataSource) {
        try {
            String queryForAllResources = STR."""
                SELECT EXISTS (SELECT 1 FROM cf_AssetGroupDetails
                WHERE groupName = '\{ASSET_GROUP_FOR_ALL_SOURCES}') AS \{ROW_EXISTS}
                """;
            var response = database.executeQuery(queryForAllResources).stream().findFirst();
            var isAllSourcesMissing =
                response.isPresent() && response.get().get(ROW_EXISTS).equals("0");
            if (isAllSourcesMissing) {
                // Creates ASSET_GROUP_FOR_ALL_SOURCES if not present
                String aliasQuery = generateDefaultAssetGroupAliasQuery(dataSource);
                insertDefaultAssetGroup(aliasQuery);
                elasticSearch.invokeCheckAndConvert(ElasticAliasResponse.class, HttpMethod.POST,
                    "_aliases/", aliasQuery);
                LOGGER.info("Created default asset group: {}", ASSET_GROUP_FOR_ALL_SOURCES);
            }
        } catch (Exception e) {
            throw new JobException("Failed creating default asset group", e);
        }
    }

    public void updateImpactedAliases(List<String> aliases, String dataSource) {
        if (aliases.isEmpty()) {
            return;
        }

        String query = STR."""
            SELECT DISTINCT agd.groupId, agd.groupName, agd.groupType, agd.aliasQuery FROM cf_AssetGroupDetails AS agd
            WHERE (agd.groupId NOT IN (
                SELECT DISTINCT agcd.groupId
                FROM cf_AssetGroupCriteriaDetails AS agcd
                WHERE agcd.attributeName = 'CloudType') AND agd.groupType <> 'user' AND agd.groupType <> 'system'
            AND agd.groupName <> '\{ASSET_GROUP_FOR_ALL_SOURCES}' and aliasQuery like '%_*%')
            OR (agd.groupType = 'user')
            OR (agd.groupName = '\{dataSource}' AND agd.groupType = 'system')
            OR (agd.groupName = '\{ASSET_GROUP_FOR_ALL_SOURCES}')
            OR (agd.groupType <> 'user'
                AND agd.groupType <> 'system' AND agd.groupName <> '\{ASSET_GROUP_FOR_ALL_SOURCES}' and aliasQuery like '\{dataSource}_*%')
            """.trim();

        var assetGroupsList = database.executeQuery(query);
        LOGGER.info("Found {} asset groups", assetGroupsList.size());
        if (assetGroupsList.isEmpty()) {
            LOGGER.error("Unable to update due to no asset groups for dataSource: {}", dataSource);
            return;
        }

        var updatedAssetGroups = new ArrayList<String>();
        var actions = new ArrayList<String>();
        var updatedAssetGroupsCount = 0;
        for (var assetGroup : assetGroupsList) {
            var alias = assetGroup.get("groupName");
            var groupType = assetGroup.get("groupType");
            var existingAliasQuery = assetGroup.get("aliasQuery");
            var groupId = assetGroup.get("groupId");
            if (alias == null || groupType == null || groupId == null) {
                LOGGER.info(
                    "Cannot update aliases due to a null field. alias={}, groupType={}, groupId={}",
                    alias, groupType, groupId);
                continue;
            }

            if ((existingAliasQuery == null || existingAliasQuery.equalsIgnoreCase("null"))
                && groupType.equalsIgnoreCase("user")) {
                var assetGroupTags = getCachedAssetGroupTagsOrFetch(groupId);
                if (assetGroupTags.isEmpty()) {
                    LOGGER.error("There are no asset group tags for groupId={}", groupId);
                    continue;
                }
                actions.add(
                    generateStakeholdersAssetGroupAliasQuery(alias, assetGroupTags, dataSource));
                updatedAssetGroupsCount += 1;
                updatedAssetGroups.add(alias);

            } else if (alias.equalsIgnoreCase(ASSET_GROUP_FOR_ALL_SOURCES)
                || alias.equalsIgnoreCase(dataSource)) {
                actions.add(String.format(UPDATE_ALIAS_TEMPLATE, "", STR."\{dataSource}_*", alias));
                updatedAssetGroupsCount += 1;
                updatedAssetGroups.add(alias);
            } else if (groupType.equalsIgnoreCase("user") && existingAliasQuery != null
                && !existingAliasQuery.equalsIgnoreCase("null") && !alias.equalsIgnoreCase(
                ASSET_GROUP_FOR_ALL_SOURCES) && !alias.equalsIgnoreCase(dataSource) || (
                existingAliasQuery != null && existingAliasQuery.contains("_*"))) {
                var filterContents = getFilterFromExistingAliasQuery(existingAliasQuery);
                var filter = filterContents.isEmpty() ? "" : STR."\"filter\": \{filterContents},";
                actions.add(
                    String.format(UPDATE_ALIAS_TEMPLATE, filter, STR."\{dataSource}_*", alias));
                updatedAssetGroupsCount += 1;
                updatedAssetGroups.add(alias);
            } else {
                throw new JobException(
                    STR."Unable to update alias \{alias} for existingAliasQuery \{existingAliasQuery}");
            }
        }

        var combinedActions = mergeActions(actions);
        if (combinedActions == null || combinedActions.toString().isEmpty()) {
            throw new JobException(STR."Update failed, nothing to do for \{dataSource}");
        }

        var payload = STR."{ \"actions\": \{combinedActions} }";
        try {
            var response = elasticSearch.invokeCheckAndConvert(ElasticAliasResponse.class,
                HttpMethod.POST, "_aliases", payload);
            if (!response.acknowledged || response.errors) {
                throw new JobException(STR."Failed creating some aliases: \{response}");
            }
        } catch (IOException e) {
            throw new JobException(STR."Error updating alias for \{dataSource}", e);
        }
        LOGGER.info("Finished updating impacted aliases for indices={} dataSource={}. "
                + "Updated {} asset groups: {}", aliases, dataSource, updatedAssetGroupsCount,
            updatedAssetGroups);
    }

    private String getFilterFromExistingAliasQuery(String existingAliasQuery) {
        try {
            JsonNode rootNode = objectMapper.readTree(existingAliasQuery);
            if (rootNode != null && rootNode.has("actions")) {
                rootNode = rootNode.get("actions");
                for (JsonNode actionNode : rootNode) {
                    JsonNode addNode = actionNode.path("add");
                    if (addNode.get("index").isMissingNode() || !addNode.get("index").toString()
                        .contains("_*")) {
                        continue;
                    }
                    JsonNode filterNode = addNode.get("filter");
                    if (filterNode != null && !filterNode.isMissingNode()) {
                        return filterNode.toString();
                    }
                }
            }
        } catch (Exception e) {
            throw new JobException(
                STR."Error while extracting filter from existingAliasQuery : \{existingAliasQuery}",
                e);
        }
        return "";
    }

    private String buildPaladinApiUrl(String servicePath, String additional) {
        return STR."\{ConfigService.get(
            PaladinCloud.BASE_PALADIN_CLOUD_API_URI)}\{servicePath}\{additional}";

    }

    private List<Map<String, String>> getCachedAssetGroupTagsOrFetch(String groupId) {
        if (assetGroupTagsCache.containsKey(groupId)) {
            return assetGroupTagsCache.get(groupId);
        } else {
            var result = database.executeQuery(
                STR."SELECT attributeName, attributeValue FROM cf_AssetGroupCriteriaDetails WHERE groupId = '\{groupId}'");
            assetGroupTagsCache.put(groupId, result);
            return result;
        }
    }

    private List<String> getCachedDataSourcesOrFetch(String dataSource) {
        if (dataSourceCache.isEmpty()) {
            dataSourceCache.addAll(getEnabledDataSources(dataSource));
        }
        return dataSourceCache;
    }

    private List<String> getEnabledDataSources(String dataSource) {
        var dataSourceList = getCachedResultOrFetch(
            "SELECT DISTINCT dataSourceName FROM cf_Target");
        if (dataSourceList.isEmpty()) {
            LOGGER.error("There are NO data sources");
            return new ArrayList<>();
        } else {
            var enabledSources = dataSourceList.stream().map(row -> {
                // Map to just the data source name
                return row.get("dataSourceName");
            }).filter(source -> {
                var response = getCachedResultOrFetch(
                    STR."SELECT `value` FROM pac_config_properties WHERE cfkey = '\{source}.enabled'").stream()
                    .findFirst();
                return response.isPresent() && response.get().get("value").equals("true");
            }).collect(Collectors.toSet());
            if (enabledSources.isEmpty()) {
                LOGGER.error("There are NO enabled data sources");
            }

            // Assume the current data source is enabled
            enabledSources.add(dataSource);
            return new ArrayList<>(enabledSources);
        }
    }

    private List<Map<String, String>> getCachedResultOrFetch(String query) {
        if (databaseCache.containsKey(query)) {
            return databaseCache.get(query);
        } else {
            var response = database.executeQuery(query);
            databaseCache.put(query, response);
            return response;
        }
    }

    private void insertDefaultAssetGroup(String aliasQuery) {
        Map<String, String> data = new HashMap<>();
        data.put("groupId", UUID.randomUUID().toString());
        data.put("groupName", ASSET_GROUP_FOR_ALL_SOURCES);
        data.put("dataSource", "");
        data.put("displayName", "All Sources");
        data.put("groupType", "system");
        data.put("createdBy", "admin@paladincloud.io");
        data.put("createdUser", "admin@paladincloud.io");
        data.put("modifiedUser", "admin@paladincloud.io");
        data.put("description", "All assets from all Sources");
        data.put("aliasQuery", aliasQuery);
        data.put("isVisible", "1");
        database.insert("cf_AssetGroupDetails", data);
    }

    private String generateDefaultAssetGroupAliasQuery(String dataSource) {
        var actions = getCachedDataSourcesOrFetch(dataSource).stream().map(sourceName -> STR."""
            {
                "add": {
                    "index": "\{sourceName.toLowerCase().trim()}_*",
                    "alias": "\{ASSET_GROUP_FOR_ALL_SOURCES}"
                }
            }
            """.trim());
        return STR."""
            {
                "actions": [\{String.join(",", actions.toList())}]
            }
            """.trim();
    }

    private String generateStakeholdersAssetGroupAliasQuery(String assetGroup,
        List<Map<String, String>> assetGroupTags, String dataSource) {

        var tagMap = generateQueryForUserAssetGroup(assetGroupTags);
        var actions = getCachedDataSourcesOrFetch(dataSource).stream().map(sourceName -> STR."""
            {
                "add": {
                    "index": "\{sourceName.toLowerCase().trim()}",
                    "alias": "\{assetGroup}",
                    "filter": {
                        "bool": \{tagMap}
                    }
                }
            }
            """.trim());
        return STR."""
            {
                "action": [\{String.join(",", actions.toList())}]
            }
            """.trim();
    }

    private String generateQueryForUserAssetGroup(List<Map<String, String>> assetGroupTags) {
        List<Object> mustList = new ArrayList<>();
        Map<String, Object> mustObj = new HashMap<>();
        Map<String, List<String>> groupedTags = assetGroupTags.stream().collect(
            Collectors.groupingBy(map -> map.get("attributeName"),
                Collectors.mapping(map -> map.get("attributeValue"), Collectors.toList())));
        groupedTags.forEach((tagName, tags) -> mustList.add(
            generateShouldMapForStakeholderAssetGroup(tagName, tags)));
        mustObj.put("must", mustList);
        try {
            return objectMapper.writeValueAsString(mustObj);
        } catch (JsonProcessingException e) {
            throw new JobException("Failed converting JSON to string", e);
        }
    }

    private Map<String, Object> generateShouldMapForStakeholderAssetGroup(String tagName,
        List<String> tags) {
        List<Object> matchList = new ArrayList<>();
        Map<String, Object> shouldObj = new HashMap<>();
        if (tags.size() == 1) {
            Map<String, Object> attributeObj = new HashMap<>();
            Map<String, Object> match = new HashMap<>();
            attributeObj.put(ES_ATTRIBUTE_TAG + tagName + ES_ATTRIBUTE_KEYWORD, tags.getFirst());
            match.put("match", attributeObj);
            return match;
        } else {
            Map<String, Object> boolObj = new HashMap<>();
            tags.forEach(value -> {
                Map<String, Object> attributeObj = new HashMap<>();
                Map<String, Object> match = new HashMap<>();
                attributeObj.put(ES_ATTRIBUTE_TAG + tagName + ES_ATTRIBUTE_KEYWORD, value);
                match.put("match", attributeObj);
                matchList.add(match);
            });
            shouldObj.put("should", matchList);
            shouldObj.put("minimum_should_match", 1);
            boolObj.put("bool", shouldObj);
            return boolObj;
        }
    }

    private JsonNode mergeActions(List<String> jsonStrings) {
        List<JsonNode> actionsList = new ArrayList<>();
        for (String jsonString : jsonStrings) {
            try {
                JsonNode jsonObj = objectMapper.readTree(jsonString);
                if (jsonObj.has("actions")) {
                    JsonNode actions = jsonObj.get("actions");
                    actionsList.addAll(convertToList(actions));
                }
            } catch (Exception e) {
                throw new JobException(STR."Error merging actions (\{jsonString})", e);
            }
        }
        return objectMapper.valueToTree(actionsList);
    }

    private List<JsonNode> convertToList(JsonNode node) {
        List<JsonNode> list = new ArrayList<>();
        if (node.isArray()) {
            node.forEach(list::add);
        } else {
            list.add(node);
        }
        return list;
    }

    private Map<String, Integer> getCategoryWeightedMap() {
        if (categoryWeightageMap == null) {
            var temp = new HashMap<String, Integer>();
            database.executeQuery("SELECT * FROM cf_PolicyCategoryWeightage").forEach(row -> {
                temp.put(row.get("policyCategory"), Integer.parseInt(row.get("weightage")));
            });
            categoryWeightageMap = temp;
        }
        return categoryWeightageMap;
    }
}
