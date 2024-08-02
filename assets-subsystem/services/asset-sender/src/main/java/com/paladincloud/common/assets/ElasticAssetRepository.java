package com.paladincloud.common.assets;

import com.paladincloud.common.search.ElasticBatch;
import com.paladincloud.common.search.ElasticSearchHelper;
import com.paladincloud.common.search.ElasticSearchHelper.HttpMethod;
import com.paladincloud.common.search.ElasticSearchUpdateByQueryResponse;
import com.paladincloud.common.util.JsonHelper;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class ElasticAssetRepository implements AssetRepository {

    private static final Logger LOGGER = LogManager.getLogger(ElasticAssetRepository.class);
    private final ElasticSearchHelper elasticSearch;

    public ElasticAssetRepository(ElasticSearchHelper elasticSearch) {
        this.elasticSearch = elasticSearch;
    }

    @Override
    public Map<String, AssetDTO> getLatestAssets(String indexName, List<String> filters) {
        return elasticSearch.getLatestAssets(indexName, filters);
    }

    @Override
    public void deleteAssetsWithoutValue(String indexName, String docType, String fieldName,
        String fieldValue) throws IOException {
        elasticSearch.deleteDocumentsWithoutValue(indexName, docType, fieldName, fieldValue);
    }

    @Override
    public Map<String, Object> getTypeRelations(String indexName, String parentType)
        throws IOException {
        var response = elasticSearch.invokeAndCheck(HttpMethod.GET, STR."\{indexName}/_mapping",
            null);
        var document = JsonHelper.objectMapper.readTree(response.getBody());
        var relations = document.at(
            STR."/\{indexName}/mappings/properties/\{parentType}_relations/relations");

        @SuppressWarnings("unchecked")
        Map<String, Object> result = JsonHelper.objectMapper.convertValue(relations, Map.class);
        return result;
    }

    @Override
    public void updateTypeRelations(String indexName, String parentType,
        Map<String, Object> relations) throws IOException {
        var asString = JsonHelper.toJson(relations);
        var payload = STR."""
                {
                    "properties": {
                        "\{parentType}_relations": {
                            "type": "join",
                            "relations": \{asString}
                        }
                    }
                }
                """.trim();
        LOGGER.info("Updating types relations for {} to {}", parentType, relations);
        elasticSearch.invokeAndCheck(HttpMethod.PUT, STR."\{indexName}/_mapping", payload);
    }

    public void processLoadErrors(String indexName, String type, String loadDate, Map<String, List<Map<String, Object>>> typeToError)
        throws IOException {
        if (typeToError.containsKey(type) || typeToError.containsKey("all")) {
            var errors = typeToError.get(type);
            if (errors == null) {
                errors = typeToError.get("all");
            }

            var query = new StringBuilder(2048);
            query.append(STR."""
                { "script": {"inline": "ctx._source._loaddate= '\{loadDate}'" },
                "query": {"bool": { "should": [
                """.trim());

            var shouldQuery = new ArrayList<String>();
            errors.forEach(err -> {
                var accountId = err.get("accountid").toString();
                var region = err.get("region").toString();
                if (StringUtils.isNotEmpty(accountId) && StringUtils.isNotEmpty(region)) {
                    shouldQuery.add(STR."""
                        {
                            "bool": {
                                "must": [
                                    {
                                        "term": {
                                            "accountid.keyword": "\{accountId}"
                                        }
                                    },
                                    {
                                        "term": {
                                            "region.keyword": "\{region}"
                                        }
                                    }
                                ]
                            }
                        }
                        """.trim());
                }
            });

            query.append(String.join(",", shouldQuery));
            query.append("]");

            var arrayOpen = false;
            if (StringUtils.isNotEmpty(type)) {
                query.append(STR."""
                    ,
                    "minimum_should_match": 1,
                    "must": [{ "match": { "docType.keyword": "\{type}" }}
                    """.trim());
                arrayOpen = true;
            }

            query.append("""
                ,{ "match": { "latest": true }}
                """.trim());
            if (arrayOpen) {
                query.append("]");
            }
            query.append("}}}");

            var response = elasticSearch.invokeCheckAndConvert(ElasticSearchUpdateByQueryResponse.class, HttpMethod.POST,
                STR."\{indexName}/_update_by_query", query.toString());
            LOGGER.info("Updated via load errors: {} {} - updateCount={}", indexName, type, response.updated);
        }
    }

    @Override
    public Batch createBatch() {
        return new ElasticBatch(elasticSearch);
    }

    @Override
    public void createIndex(String index) throws IOException {
        elasticSearch.createIndex(index);
    }
}
