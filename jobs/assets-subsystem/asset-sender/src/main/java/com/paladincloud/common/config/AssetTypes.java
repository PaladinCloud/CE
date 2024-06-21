package com.paladincloud.common.config;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mysql.cj.util.StringUtils;
import com.paladincloud.common.assets.AssetGroups;
import com.paladincloud.common.aws.Database;
import com.paladincloud.common.config.ConfigConstants.Config;
import com.paladincloud.common.errors.JobException;
import com.paladincloud.common.search.ElasticSearch;
import com.paladincloud.common.search.ElasticSearch.HttpMethod;
import com.paladincloud.common.util.StringExtras;
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

    private final ElasticSearch elasticSearch;
    private final Database database;


    @Inject
    public AssetTypes(ElasticSearch elasticSearch, Database database) {
        this.elasticSearch = elasticSearch;
        this.database = database;
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
        var targetTypesInclude = StringUtils.split(
            ConfigService.get(Config.CONFIG_TARGET_TYPE_INCLUDE), ",", true);
        var targetTypesExclude = StringUtils.split(
            ConfigService.get(Config.CONFIG_TARGET_TYPE_EXCLUDE), ",", true);

        if (typeInfo == null) {
            typeInfo = new HashMap<>();

            var query = ConfigService.get(Config.CONFIG_TYPES_QUERY)
                + STR." and dataSourceName = '\{dataSource}'";
            var typeList = database.executeQuery(query);
            try {
                for (var type : typeList) {
                    var typeName = type.get("targetName");
                    var displayName = type.get("displayName");
                    var config = new ObjectMapper().readValue(type.get("targetConfig"),
                        new TypeReference<Map<String, String>>() {
                        });
                    config.put("displayName", displayName);
                    if ((targetTypesInclude.isEmpty() || targetTypesInclude.contains(typeName))
                        && (!targetTypesExclude.contains(typeName))) {
                        typeInfo.put(typeName, config);
                    }
                }
            } catch (IOException e) {
                throw new JobException("Failed processing type config", e);
            }
        }

        return typeInfo;
    }

    public void setupIndexAndTypes(String dataSource) {
        var newAssets = new HashSet<String>();
        var types = getTypes(dataSource);
        for (var type : types) {
            var indexName = StringExtras.indexName(dataSource, type);
            if (elasticSearch.indexMissing(indexName)) {
                if (!indexName.equals("kvt_gcp_cloudfunction")) {
                    LOGGER.error("Skipping index creation for {}", indexName);
                    continue;
                }
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
                    elasticSearch.invoke(HttpMethod.PUT, indexName, payload);
                    elasticSearch.invoke(HttpMethod.PUT, STR."/\{indexName}/_alias/\{dataSource}",
                        null);
                    elasticSearch.invoke(HttpMethod.PUT, STR."/\{indexName}/_alias/ds-all", null);
                } catch (IOException e) {
                    throw new JobException(
                        STR."Error while creating the index '\{indexName}' using '\{payload}'", e);
                }
            }
        }

        AssetGroups.createDefaultGroup(dataSource);
        AssetGroups.updateImpactedAliases(newAssets.stream().toList(), dataSource);

        try {
            elasticSearch.createIndex("exceptions");
        } catch (IOException e) {
            throw new JobException("Error while creating the 'exceptions' index", e);
        }
    }
}
