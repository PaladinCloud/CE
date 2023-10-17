/*******************************************************************************
 * Copyright 2023 Paladin Cloud, Inc. or its affiliates. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License.  You may obtain a copy
 * of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  See the
 * License for the specific language governing permissions and limitations under
 * the License.
 ******************************************************************************/
package com.tmobile.cso.pacman.aqua.util;

import com.google.gson.Gson;

import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.apache.commons.httpclient.HttpStatus;
import org.apache.http.HttpEntity;
import org.apache.http.HttpHost;
import org.apache.http.ParseException;
import org.apache.http.entity.ContentType;
import org.apache.http.nio.entity.NStringEntity;
import org.apache.http.util.EntityUtils;
import org.elasticsearch.client.Response;
import org.elasticsearch.client.RestClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@SuppressWarnings("unchecked")
public class ElasticSearchManager {

    private static final Logger LOGGER = LoggerFactory.getLogger(ElasticSearchManager.class);
    private static final String ES_HOST_KEY_NAME = System.getProperty("elastic-search.host");
    private static final Integer ES_HTTP_PORT = Integer.parseInt(System.getProperty("elastic-search.port"));
    private static RestClient restClient;


    private ElasticSearchManager() {
    }

    /**
     * Gets the rest client.
     *
     * @return the rest client
     */
    private static RestClient getRestClient() {
        if (restClient == null)
            restClient = RestClient.builder(new HttpHost(ES_HOST_KEY_NAME, ES_HTTP_PORT)).build();
        return restClient;

    }

    /**
     * Creates the index.
     *
     * @param index the index name
     */
    public static void createIndex(String index) {
        String indexName = "/" + index;
        if (!indexExists(indexName)) {
            String payLoad = "{\"settings\": { \"number_of_shards\" : 1,\"number_of_replicas\" : 1,\"index.mapping.ignore_malformed\": true }}";
            try {
                invokeAPI("PUT", indexName, payLoad);
            } catch (IOException e) {
                LOGGER.error("Error createIndex ", e);
            }
        }
    }

    /**
     * Creates the type.
     *
     * @param index the index name
     * @param typename  the typename
     */
    public static void createType(String index, String typename) {
        String indexName = "/" + index;
        if (!typeExists(indexName, typename)) {
            String endPoint = indexName + "/_mapping/" + typename;
            try {
                invokeAPI("PUT", endPoint, "{ \"properties\":{}}");
            } catch (IOException e) {
                LOGGER.error("Error in method createType", e);
            }
        }
    }

    /**
     * Bulk upload.
     *
     * @param bulkRequest the bulk request
     */
    private static void bulkUpload(StringBuilder bulkRequest) {
        try {
            Response resp = invokeAPI("POST", "/_bulk", bulkRequest.toString());
            String responseStr = EntityUtils.toString(resp.getEntity());
            if (responseStr.contains("\"errors\":true")) {
                LOGGER.error(responseStr);
            }
        } catch (ParseException | IOException e) {
            LOGGER.error("Error in uploading data", e);
        }
    }

    /**
     * Upload data.
     *
     * @param index the index
     * @param type  the type
     * @param docs  the docs
     * @param idKey the id key
     */
    public static void uploadData(String index, String type, List<Map<String, Object>> docs, String idKey) {
        String actionTemplate = "{ \"index\" : { \"_index\" : \"%s\", \"_id\" : \"%s\"} }%n";

        LOGGER.info("*********UPLOADING*** {}", type);
        if (null != docs && !docs.isEmpty()) {
            StringBuilder bulkRequest = new StringBuilder();
            int i = 0;
            for (Map<String, Object> doc : docs) {
                String id = doc.get(idKey).toString();
                doc.put("docType", type);
                StringBuilder _doc = new StringBuilder(createESDoc(doc));
                bulkRequest.append(String.format(actionTemplate, index, id));
                bulkRequest.append(_doc + "\n");
                i++;
                if (i % 1000 == 0 || bulkRequest.toString().getBytes().length / (1024 * 1024) > 5) {
                    LOGGER.info("Uploaded {}", i);
                    bulkUpload(bulkRequest);
                    bulkRequest = new StringBuilder();
                }
            }

            if (bulkRequest.length() > 0) {
                LOGGER.info("Uploaded {}", i);
                bulkUpload(bulkRequest);
            }

            refresh(index);
        }
    }

    /**
     * Refresh.
     *
     * @param index the index
     */
    public static void refresh(String index) {
        String indexName = "/" + index;
        try {
            Response refrehsResponse = invokeAPI("POST", indexName + "/" + "_refresh", null);
            if (refrehsResponse != null && HttpStatus.SC_OK != refrehsResponse.getStatusLine().getStatusCode()) {
                LOGGER.error("Refreshing index {} failed", index);
            }
        } catch (IOException e) {
            LOGGER.error("Error refresh ", e);
        }
    }

    /**
     * Creates the ES doc.
     *
     * @param doc the doc
     * @return the string
     */
    public static String createESDoc(Map<String, Object> doc) {
        return new Gson().toJson(doc);
    }

    /**
     * Invoke API.
     *
     * @param method   the method
     * @param endpoint the endpoint
     * @param payLoad  the pay load
     * @return the response
     * @throws IOException Signals that an I/O exception has occurred.
     */
    public static Response invokeAPI(String method, String endpoint, String payLoad) throws IOException {
        HttpEntity entity = null;
        if (payLoad != null) {
            entity = new NStringEntity(payLoad, ContentType.APPLICATION_JSON);
        }

        return getRestClient().performRequest(method, endpoint, Collections.<String, String>emptyMap(), entity);
    }

    /**
     * Index exists.
     *
     * @param indexName the index name
     * @return true, if successful
     */
    private static boolean indexExists(String indexName) {
        try {
            Response response = invokeAPI("HEAD", indexName, null);
            if (response != null) {
                return response.getStatusLine().getStatusCode() == 200;
            }
        } catch (IOException e) {
            LOGGER.error("Error indexExists ", e);
        }

        return false;
    }

    /**
     * Type exists.
     *
     * @param indexName the index name
     * @param type      the type
     * @return true, if successful
     */
    private static boolean typeExists(String indexName, String type) {
        try {
            Response response = invokeAPI("HEAD", indexName + "/_mapping/" + type, null);
            if (response != null) {
                return response.getStatusLine().getStatusCode() == 200;
            }
        } catch (IOException e) {
            LOGGER.error("Error typeExists ", e);
        }

        return false;
    }

}
