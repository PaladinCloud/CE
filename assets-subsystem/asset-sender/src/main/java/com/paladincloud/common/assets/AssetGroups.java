package com.paladincloud.common.assets;

import com.paladincloud.common.aws.Database;
import com.paladincloud.common.errors.JobException;
import com.paladincloud.common.search.ElasticSearch;
import com.paladincloud.common.search.ElasticSearch.HttpMethod;
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
    private static final Map<String, List<Map<String, String>>> databaseCache = new HashMap<>();
    private static final List<String> dataSourceCache = new ArrayList<>();
    private final ElasticSearch elasticSearch;
    private final Database database;

    @Inject
    public AssetGroups(ElasticSearch elasticSearch, Database database) {
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
                elasticSearch.invoke(HttpMethod.POST, "_aliases/", aliasQuery);
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

//        var assetGroupsList = database.executeQuery()

        LOGGER.warn("Updating aliases for {}: {}", dataSource, aliases);
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
}
