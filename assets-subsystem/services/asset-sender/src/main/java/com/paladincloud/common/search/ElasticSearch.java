package com.paladincloud.common.search;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.paladincloud.common.config.ConfigConstants.Elastic;
import com.paladincloud.common.config.ConfigService;
import com.paladincloud.common.errors.JobException;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import javax.inject.Inject;
import javax.inject.Singleton;
import org.apache.http.HttpHost;
import org.apache.http.ParseException;
import org.apache.http.entity.ContentType;
import org.apache.http.nio.entity.NStringEntity;
import org.apache.http.util.EntityUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.elasticsearch.client.Request;
import org.elasticsearch.client.Response;
import org.elasticsearch.client.RestClient;

@Singleton
public class ElasticSearch {

    private static final int MAX_RETURNED_RESULTS = 10000;

    private static final Logger LOGGER = LogManager.getLogger(ElasticSearch.class);
    private RestClient restClient;

    @Inject
    public ElasticSearch() {
    }

    public boolean indexMissing(String indexName) {
        try {
            var response = invoke(HttpMethod.HEAD, indexName, null);
            if (response != null) {
                return response.getStatusLine().getStatusCode() != 200;
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
        return transformResponse(clazz, response);
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
    public Response invokeAndCheck(HttpMethod method, String endpoint, String payLoad)
        throws IOException {
        var response = invoke(method, endpoint, payLoad);
        var status = response.getStatusLine();
        var statusCode = status.getStatusCode();
        if (statusCode < 200 || statusCode > 299) {
            throw new IOException(
                STR."Failed ElasticSearch request: \{statusCode}; \{status.getReasonPhrase()}");
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
    public Response invoke(HttpMethod method, String endpoint, String payLoad) throws IOException {
        String uri = endpoint;
        if (!uri.startsWith("/")) {
            uri = STR."/\{uri}";
        }

        var request = new Request(method.name, uri);
        if (payLoad != null) {
            request.setEntity(new NStringEntity(payLoad, ContentType.APPLICATION_JSON));
        }
        return getRestClient().performRequest(request);
    }

    /**
     * Gets the existing documents.
     *
     * @param indexName the index name
     * @param filters   the filters
     * @return the existing documents
     */
    public Map<String, Map<String, String>> getExistingDocuments(String indexName,
        List<String> filters) {
        int totalDocumentCount = getDocumentCount(indexName);
        boolean scroll = totalDocumentCount > ElasticSearch.MAX_RETURNED_RESULTS;

        String keyField = filters.getFirst();
        StringBuilder filter_path = new StringBuilder("&filter_path=_scroll_id,");
        for (String _filter : filters) {
            filter_path.append("hits.hits._source.").append(_filter).append(",");
        }
        filter_path.deleteCharAt(filter_path.length() - 1);

        String endPoint = STR."\{indexName}/_search?scroll=1m\{filter_path}&size=\{Math.min(
            totalDocumentCount, ElasticSearch.MAX_RETURNED_RESULTS)}";
        if (totalDocumentCount == 0) {
            endPoint = STR."\{indexName}/_search?scroll=1m\{filter_path}";
        }
        String payLoad = """
            {"query":{"match":{"latest":true}}}
            """;
        Map<String, Map<String, String>> results = new HashMap<>();
        String scrollId = fetchDataAndScrollId(endPoint, results, keyField, payLoad);

        if (scroll) {
            totalDocumentCount -= ElasticSearch.MAX_RETURNED_RESULTS;
            do {
                endPoint = STR."/_search/scroll?scroll=1m&scroll_id=\{scrollId}\{filter_path}";
                scrollId = fetchDataAndScrollId(endPoint, results, keyField, null);
                totalDocumentCount -= ElasticSearch.MAX_RETURNED_RESULTS;
                if (totalDocumentCount <= 0) {
                    scroll = false;
                }
            } while (scroll);
        }
        return results;
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

    private <T> T transformResponse(Class<T> clazz, Response response) throws IOException {
        var objectMapper = new ObjectMapper().configure(
            DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

        var x = EntityUtils.toString(response.getEntity());
        return objectMapper.readValue(x, clazz);
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
            Response response = invoke(HttpMethod.GET, STR."\{indexName}/_count?filter_path=count",
                """
                    {"query":{"match":{"latest":true}}}
                    """);
            String rspJson = EntityUtils.toString(response.getEntity());
            return new ObjectMapper().readTree(rspJson).at("/count").asInt();
        } catch (IOException e) {
            throw new JobException(STR."Error getting document count in \{indexName}", e);
        }
    }

    /**
     * Fetch data and scroll id.
     *
     * @param endPoint the end point
     * @param results  the data from ElasticSearch
     * @param keyField the key field
     * @param payLoad  the pay load
     * @return the string
     */
    private String fetchDataAndScrollId(String endPoint, Map<String, Map<String, String>> results,
        String keyField, String payLoad) {
        try {
            var response = invokeCheckAndConvert(ElasticQueryResponse.class, HttpMethod.GET,
                endPoint, payLoad);
            if (response.hits != null && response.hits.hits != null) {
                for (var document : response.hits.hits) {
                    // Convert from <String, Object> to <String, String>
                    var docMap = document.source.entrySet().stream()
                        .collect(Collectors.toMap(Map.Entry::getKey, e -> e.getValue().toString()));
                    results.put(docMap.get(keyField), docMap);
                    docMap.remove(keyField);
                }
            }
            return response.scrollId;
        } catch (ParseException | IOException e) {
            LOGGER.error("Error in fetchDataAndScrollId", e);
        }
        return "";
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
