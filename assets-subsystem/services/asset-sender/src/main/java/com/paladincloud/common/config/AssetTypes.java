package com.paladincloud.common.config;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mysql.cj.util.StringUtils;
import com.paladincloud.common.assets.AssetGroups;
import com.paladincloud.common.aws.DatabaseHelper;
import com.paladincloud.common.config.ConfigConstants.Config;
import com.paladincloud.common.errors.JobException;
import com.paladincloud.common.search.ElasticSearchHelper;
import com.paladincloud.common.search.ElasticSearchHelper.HttpMethod;
import com.paladincloud.common.util.StringHelper;
import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import javax.inject.Inject;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class AssetTypes {

    private static final Logger LOGGER = LogManager.getLogger(AssetTypes.class);
    private static Map<String, Map<String, String>> typeInfo;

    private final ElasticSearchHelper elasticSearch;
    private final DatabaseHelper database;
    private final AssetGroups assetGroups;
    private boolean hasWarnedTypeOverride = false;


    @Inject
    public AssetTypes(ElasticSearchHelper elasticSearch, DatabaseHelper database, AssetGroups assetGroups) {
        this.elasticSearch = elasticSearch;
        this.database = database;
        this.assetGroups = assetGroups;
    }

    public Set<String> getTypes(String dataSource) {
        return getTypeConfig(dataSource).keySet();
    }

    public Map<String, String> getTypesWithDisplayName(String dataSource) {
        var typeInfo = getTypeConfig(dataSource);
        var targetsWithName = new HashMap<String, String>();
        for (Map.Entry<String, Map<String, String>> typeEntry : typeInfo.entrySet()) {
            var targetType = typeEntry.getKey();
            for (Map.Entry<String, String> entry : typeEntry.getValue().entrySet()) {
                if (entry.getKey().equalsIgnoreCase("displayName")) {
                    targetsWithName.put(targetType, entry.getValue());
                }
            }
        }
        return targetsWithName;
    }

    public String getKeyForType(String ds, String type) {
        return getTypeConfig(ds).get(type).get("key");
    }

    public String getIdForType(String ds, String type) {
        return getTypeConfig(ds).get(type).get("id");
    }

    public String getResourceNameType(String ds, String type) {
        return getTypeConfig(ds).get(type).get("name");

    }

    private Map<String, Map<String, String>> getTypeConfig(String dataSource) {
        var assetTypeOverride = StringUtils.split(
            ConfigService.get(ConfigConstants.Dev.ASSET_TYPE_OVERRIDE), ",", true);
        var targetTypesInclude = StringUtils.split(ConfigService.get(Config.TARGET_TYPE_INCLUDE),
            ",", true);
        var targetTypesExclude = StringUtils.split(ConfigService.get(Config.TARGET_TYPE_EXCLUDE),
            ",", true);

        if (typeInfo == null) {
            typeInfo = new HashMap<>();

            var query =
                ConfigService.get(Config.TYPES_QUERY) + STR." and dataSourceName = '\{dataSource}'";
            var typeList = database.executeQuery(query);
            try {
                for (var type : typeList) {
                    var typeName = type.get("targetName");
                    var displayName = type.get("displayName");
                    var config = new ObjectMapper().readValue(type.get("targetConfig"),
                        new TypeReference<Map<String, String>>() {
                        });
                    config.put("displayName", displayName);
                    if (!assetTypeOverride.isEmpty()) {
                        if (assetTypeOverride.contains(typeName)) {
                            typeInfo.put(typeName, config);
                        }
                    } else if (
                        (targetTypesInclude.isEmpty() || targetTypesInclude.contains(typeName))
                            && (!targetTypesExclude.contains(typeName))) {
                        typeInfo.put(typeName, config);
                    }
                }
            } catch (IOException e) {
                throw new JobException("Failed processing type config", e);
            }
        }

        if (!assetTypeOverride.isEmpty() && !hasWarnedTypeOverride) {
            LOGGER.warn("Asset types overridden (requested = {}); actual = {}", assetTypeOverride,
                typeInfo.keySet());
            hasWarnedTypeOverride = true;
        }
        return typeInfo;
    }

    public void setupIndexAndTypes(String dataSource) {
        var newAssets = new HashSet<String>();
        var types = getTypes(dataSource);
        for (var type : types) {
            var indexName = StringHelper.indexName(dataSource, type);
            if (elasticSearch.indexMissing(indexName)) {
                newAssets.add(indexName);

                var payload = STR."""
                    {
                        "settings": {
                            "number_of_shards": 1,
                            "number_of_replicas": 1,
                            "index": {
                                "mapping.ignore_malformed": true,
                                "mapping.total_fields.limit": 2000
                            }
                        },
                        "mappings": {
                            "dynamic": true,
                            "properties": {
                                "\{type}_relations": {
                                    "type": "join",
                                    "relations": {
                                        "\{type}": ["issue_\{type}"],
                                        "issue_\{type}": [
                                            "issue_\{type}_audit",
                                            "issue_\{type}_comment",
                                            "issue_\{type}_exception"]
                                    }
                                }
                            }
                        }
                    }
                    """;

                try {

                    elasticSearch.invokeAndCheck(HttpMethod.PUT, indexName, payload);
                    elasticSearch.invokeAndCheck(HttpMethod.PUT, STR."/\{indexName}/_alias/\{dataSource}",
                        null);
                    elasticSearch.invokeAndCheck(HttpMethod.PUT, STR."/\{indexName}/_alias/ds-all", null);
                } catch (IOException e) {
                    throw new JobException(
                        STR."Error while creating the index '\{indexName}' using '\{payload}'", e);
                }
            }
        }

        assetGroups.createDefaultGroup(dataSource);
        assetGroups.updateImpactedAliases(newAssets.stream().toList(), dataSource);

        try {
            elasticSearch.createIndex("exceptions");
        } catch (IOException e) {
            throw new JobException("Error while creating the 'exceptions' index", e);
        }
    }
}
