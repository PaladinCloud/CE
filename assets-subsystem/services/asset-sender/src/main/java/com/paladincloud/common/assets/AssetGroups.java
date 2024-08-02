package com.paladincloud.common.assets;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.paladincloud.common.aws.DatabaseHelper;
import com.paladincloud.common.errors.JobException;
import com.paladincloud.common.search.ElasticAliasResponse;
import com.paladincloud.common.search.ElasticSearchHelper;
import com.paladincloud.common.search.ElasticSearchHelper.HttpMethod;
import java.io.IOException;
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
    private static final Map<String, List<Map<String, String>>> databaseCache = new HashMap<>();
    private static final List<String> dataSourceCache = new ArrayList<>();
    private static final Map<String, List<Map<String, String>>> assetGroupTagsCache = new HashMap<>();
    private final ElasticSearchHelper elasticSearch;
    private final DatabaseHelper database;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Inject
    public AssetGroups(ElasticSearchHelper elasticSearch, DatabaseHelper database) {
        this.elasticSearch = elasticSearch;
        this.database = database;
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
}
