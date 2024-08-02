package com.paladincloud.common.search;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.paladincloud.common.assets.AssetDTO;
import com.paladincloud.common.config.ConfigConstants.Elastic;
import com.paladincloud.common.config.ConfigService;
import com.paladincloud.common.errors.JobException;
import com.paladincloud.common.util.JsonHelper;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.inject.Inject;
import javax.inject.Singleton;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpHost;
import org.apache.http.ParseException;
import org.apache.http.entity.ContentType;
import org.apache.http.nio.entity.NStringEntity;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.elasticsearch.client.Request;
import org.elasticsearch.client.RestClient;

@Singleton
public class ElasticSearchHelper {

    private static final int MAX_RETURNED_RESULTS = 10000;

    private static final Logger LOGGER = LogManager.getLogger(ElasticSearchHelper.class);
    private RestClient restClient;

    @Inject
    public ElasticSearchHelper() {
    }

    public boolean indexMissing(String indexName) {
        try {
            var response = invoke(HttpMethod.HEAD, indexName, null);
            if (response != null) {
                return response.getStatusCode() != 200;
            }
        } catch (IOException e) {
            throw new JobException("Failed ElasticSearch request", e);
        }
        return true;
    }

    /**
     * Invokes the ElasticSearch API with the given method, endpoint and payload. The response is
     * checked for HTTP errors and converted to a more convenient query response.
     *
     * @param clazz    - The class to transform the response body to
     * @param method   - One of PUT, POST, etc.
     * @param endpoint - The API to call, such as "_search"
     * @param payLoad  - the payload for the call; can be null.
     * @return - The converted response
     * @throws IOException - Network failures as well as HTTP errors
     */
    // ElasticQueryResponse
    public <T> T invokeCheckAndConvert(Class<T> clazz, HttpMethod method, String endpoint,
        String payLoad) throws IOException {
        var response = invokeAndCheck(method, endpoint, payLoad);
        return JsonHelper.fromString(clazz, response.getBody());
    }

    /**
     * Invokes the ElasticSearch API with the given method, endpoint and payload. The response is
     * checked for HTTP errors and the ElasticSearch response is returned.
     *
     * @param method   - One of PUT, POST, etc.
     * @param endpoint - The API to call, such as "_search"
     * @param payLoad  - the payload for the call; can be null.
     * @return - An ElasticSearch response, which will include the body of the response
     * @throws IOException - Network failures as well as HTTP errors
     */
    public ElasticResponse invokeAndCheck(HttpMethod method, String endpoint, String payLoad)
        throws IOException {
        var response = invoke(method, endpoint, payLoad);
        if (response.getStatusCode() < 200 || response.getStatusCode() > 299) {
            throw new IOException(
                STR."Failed ElasticSearch request: \{response.getStatusCode()}; \{response.getStatusPhrase()}");
        }
        return response;
    }

    /**
     * Invokes the ElasticSearch API with the given method, endpoint and payload. NO validation is
     * done on the response, that is the responsibility of the caller. Use
     * {@link #invokeAndCheck(HttpMethod, String, String)} for HTTP status validation.
     *
     * @param method   - One of PUT, POST, etc.
     * @param endpoint - The API to call, such as "_search"
     * @param payLoad  - the payload for the call; can be null.
     * @return - An ElasticSearch response, which will include the body of the response
     * @throws IOException - Network failures
     */
    public ElasticResponse invoke(HttpMethod method, String endpoint, String payLoad)
        throws IOException {
        String uri = endpoint;
        if (!uri.startsWith("/")) {
            uri = STR."/\{uri}";
        }

        var request = new Request(method.name, uri);
        if (payLoad != null) {
            request.setEntity(new NStringEntity(payLoad, ContentType.APPLICATION_JSON));
        }
        return new ElasticResponse(getRestClient().performRequest(request));
    }

    /**
     * Deletes documents that do not match the value for the field/value pair. An optional docType
     * can be specified to narrow the impacted documents.
     *
     * @param indexName  - the index to operate on
     * @param docType    - an optional docType. If null/empty, it won't be part of the query
     * @param fieldName  - the name of the field to check the value for
     * @param fieldValue - the value of the field to KEEP (all other values will be deleted)
     * @throws IOException - if the call fails
     */
    public void deleteDocumentsWithoutValue(String indexName, String docType, String fieldName,
        String fieldValue) throws IOException {
        var query = new StringBuilder(2048);
        query.append(STR."""
            {
                "query": {
                    "bool": {
                        "must_not": [{
                            "match":{
                                "\{fieldName}": "\{fieldValue}"
                            }
                        }]
            """.trim());
        if (StringUtils.isNotEmpty(docType)) {
            query.append(STR."""
                ,
                "must": [{
                    "match": { "docType.keyword": "\{docType}" }}]
                """.trim());
        }
        query.append("}}}");
        var response = invokeCheckAndConvert(ElasticSearchDeleteByQueryResponse.class,
            HttpMethod.POST, STR."\{indexName}/_delete_by_query", query.toString());
        if (!response.failures.isEmpty()) {
            LOGGER.error("ElasticSearch delete failed: {} (query={})", response.failures, query);
            throw new JobException(STR."ElasticSearch delete failed: \{response.failures}");
        }
    }

    /**
     * Gets the assets with 'latest:true' in the given index
     *
     * @param indexName the index name
     * @return the asset documents
     */
    public Map<String, AssetDTO> getLatestAssets(String indexName, List<String> filters) {
        int totalDocumentCount = getDocumentCount(indexName);
        boolean scroll = totalDocumentCount > ElasticSearchHelper.MAX_RETURNED_RESULTS;

        StringBuilder filterPath = new StringBuilder("&filter_path=_scroll_id,");
        for (String _filter : filters) {
            filterPath.append("hits.hits._source.").append(_filter).append(",");
        }
        filterPath.deleteCharAt(filterPath.length() - 1);

        String endPoint = STR."\{indexName}/_search?scroll=1m\{filterPath}&size=\{Math.min(totalDocumentCount,
            ElasticSearchHelper.MAX_RETURNED_RESULTS)}";
        if (totalDocumentCount == 0) {
            endPoint = STR."\{indexName}/_search?scroll=1m";
        }

        String payLoad = """
            {"query":{"match":{"latest":true}}}
            """;
        Map<String, AssetDTO> results = new HashMap<>();
        String scrollId = fetchAssetAndScrollId(endPoint, results, payLoad);

        if (scroll) {
            totalDocumentCount -= ElasticSearchHelper.MAX_RETURNED_RESULTS;
            do {
                endPoint = STR."/_search/scroll?scroll=1m&scroll_id=\{scrollId}";
                scrollId = fetchAssetAndScrollId(endPoint, results, null);
                totalDocumentCount -= ElasticSearchHelper.MAX_RETURNED_RESULTS;
                if (totalDocumentCount <= 0) {
                    scroll = false;
                }
            } while (scroll);
        }
        return results;
    }

    /**
     * Fetch assets using and return the scroll id.
     *
     * @param endPoint the end point
     * @param results  the data from ElasticSearch
     * @param payLoad  the pay load
     * @return the string
     */
    private String fetchAssetAndScrollId(String endPoint, Map<String, AssetDTO> results,
        String payLoad) {
        try {
            var response = invokeCheckAndConvert(ElasticQueryAssetResponse.class, HttpMethod.GET,
                endPoint, payLoad);
            if (response.hits != null && response.hits.hits != null) {
                for (var hit : response.hits.hits) {
                    results.put(hit.source.getDocId(), hit.source);
                }
            }
            return response.scrollId;
        } catch (ParseException | IOException e) {
            LOGGER.error("Error in fetchDataAndScrollId", e);
            throw new JobException("Failed fetching documents", e);
        }
    }

    public void createIndex(String indexName) throws IOException {
        if (indexMissing(indexName)) {
            LOGGER.info("Creating index {}", indexName);
            var payload = """
                { "settings": { "index": { "number_of_shards": 1, "number_of_replicas": 1, "mapping.ignore_malformed": true } } }
                """;
            invoke(HttpMethod.PUT, indexName, payload);
        }
    }

    private RestClient getRestClient() {
        if (restClient == null) {
            var host = ConfigService.get(Elastic.HOST);
            var port = Integer.parseInt(ConfigService.get(Elastic.PORT));
            restClient = RestClient.builder(new HttpHost(host, port)).build();
        }
        return restClient;
    }

    /**
     * Gets the count of documents in an index.
     *
     * @param indexName the index name
     * @return the type count
     */
    private int getDocumentCount(String indexName) {
        try {
            var response = invokeAndCheck(HttpMethod.GET,
                STR."\{indexName}/_count?filter_path=count", """
                    {"query":{"match":{"latest":true}}}
                    """);
            return new ObjectMapper().readTree(response.getBody()).at("/count").asInt();
        } catch (IOException e) {
            throw new JobException(STR."Error getting document count in \{indexName}", e);
        }
    }

    public enum HttpMethod {
        GET("GET"), HEAD("HEAD"), POST("POST"), PUT("PUT"), DELETE("DELETE");

        public final String name;

        HttpMethod(String name) {
            this.name = name;
        }

        public String toString() {
            return this.name;
        }
    }
}
