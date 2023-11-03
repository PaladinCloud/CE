/*******************************************************************************
 * Copyright 2023 Paladin Cloud, Inc. or its affiliates. All Rights Reserved.
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License.  You may obtain a copy
 * of the License at
 * <p>
 *   http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  See the
 * License for the specific language governing permissions and limitations under
 * the License.
 ******************************************************************************/
package com.tmobile.cso.pacman.datashipper.es;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonParser;
import com.tmobile.cso.pacman.datashipper.config.ConfigManager;
import com.tmobile.cso.pacman.datashipper.dao.RDSDBManager;
import com.tmobile.cso.pacman.datashipper.util.Constants;
import com.tmobile.cso.pacman.datashipper.util.Util;
import com.tmobile.pacman.commons.utils.CommonUtils;
import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.lang3.StringUtils;
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

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

public class ESManager implements Constants {
    private static final String ES_HOST_KEY_NAME = System.getProperty("elastic-search.host");
    private static final Integer ES_HTTP_PORT = getESPort();
    private static final Logger LOGGER = LoggerFactory.getLogger(ESManager.class);
    private static RestClient restClient;
    private static final String FETCH_IMPACTED_ALIAS_QUERY_TEMPLATE = "SELECT DISTINCT agd.groupId, agd.groupName, " +
            "agd.groupType, agd.aliasQuery FROM cf_AssetGroupDetails AS agd " +
            //Checks if data source is selected as cloudType
            "WHERE (agd.groupId NOT IN ( " +
            "    SELECT DISTINCT agcd.groupId " +
            "    FROM cf_AssetGroupCriteriaDetails AS agcd " +
            "    WHERE agcd.attributeName = 'CloudType') AND agd.groupType <> 'user' AND agd.groupType <> 'System' " +
            "AND agd.groupName <> 'all-cloud' and aliasQuery like '%s') " +
            //for all user asset groups
            "OR (agd.groupType = 'user') " +
            //for current data source
            "OR (agd.groupName = '%s' AND agd.groupType = 'System') " +
            //for current all-cloud
            "OR (agd.groupName = 'all-cloud') " +
            "OR (agd.groupType <> 'user' " +
            "    AND agd.groupType <> 'System' AND agd.groupName <> 'all-cloud' and aliasQuery like '%s')";
    private static final String UPDATE_ES_ALIAS_TEMPLATE = "{\"actions\": [{\"add\": {%s \"index\": \"%s\"," +
            "\"alias\": \"%s\"}}]}";
    private static final String FILTER_TEMPLATE = "\"filter\": %s,";
    public static final String ES_ATTRIBUTE_TAG = "tags.";
    public static final String ES_ATTRIBUTE_KEYWORD = ".keyword";
    public static final String DISTINCT_DATA_SOURCES_QUERY_TEMPLATE = "SELECT DISTINCT dataSourceName FROM cf_Target";
    public static final String CRITERIA_DETAILS_QUERY_TEMPLATE =
            "SELECT attributeName, attributeValue FROM cf_AssetGroupCriteriaDetails WHERE groupId = '%s'";
    /**
     * Gets the ES port.
     *
     * @return the ES port
     */
    private static int getESPort() {
        try {
            return Integer.parseInt(System.getProperty("elastic-search.port"));
        } catch (Exception e) {
            return 0;
        }
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
     * Upload data.
     *
     * @param index    the index
     * @param type     the type
     * @param docs     the docs
     * @param loaddate the loaddate
     * @return the map
     */
    public static Map<String, Object> uploadData(String index, String type, List<Map<String, Object>> docs, String loaddate) {

        Map<String, Object> status = new LinkedHashMap<>();
        List<String> errors = new ArrayList<>();
        String actionTemplate = "{ \"index\" : { \"_index\" : \"%s\", \"_id\" : \"%s\" } }%n";

        LOGGER.info("*********UPLOADING*** {}:::{}", type, index);

        String keys = ConfigManager.getKeyForType(index, type);
        String[] _keys = keys.split(",");
        if (null != docs && !docs.isEmpty()) {
            LOGGER.info("*********# of docs *** {}", docs.size());
            StringBuilder bulkRequest = new StringBuilder();
            int i = 0;
            for (Map<String, Object> doc : docs) {

                String id = (String) doc.get("_docid");

                String cloudType = (String) doc.get("_cloudType");
                if (cloudType != null && !cloudType.isEmpty() && cloudType.equalsIgnoreCase("Azure")) {
                    String assetIdDisplayName = null;
                    String resourceGroupName = doc.get("resourceGroupName") != null ? (String) doc.get("resourceGroupName") : "";
                    String assetName = doc.get("name") != null ? (String) doc.get("name") : "";
                    if (!resourceGroupName.isEmpty() && !assetName.isEmpty())
                        assetIdDisplayName = resourceGroupName + "/" + assetName;
                    else if (resourceGroupName.isEmpty())
                        assetIdDisplayName = assetName;
                    else
                        assetIdDisplayName = resourceGroupName;
                    //set in doc
                    doc.put("assetIdDisplayName", assetIdDisplayName.toLowerCase());

                }

                StringBuilder _doc = new StringBuilder(createESDoc(doc));
                _doc.deleteCharAt(_doc.length() - 1);
                _doc.append(",\"latest\":true,\"_loaddate\":\"" + loaddate + "\" }");
                bulkRequest.append(String.format(actionTemplate, index, id));
                bulkRequest.append(_doc + "\n");
                i++;
                if (i % 1000 == 0 || bulkRequest.toString().getBytes().length / (1024 * 1024) > 5) {
                    bulkUpload(errors, bulkRequest);
                    bulkRequest = new StringBuilder();
                }
            }
            if (index.equals("aws_internetgateway")) {
                LOGGER.info("Printing bulkrequest here:  {}", bulkRequest);
            }
            if (bulkRequest.length() > 0) {
                bulkUpload(errors, bulkRequest);
            }
            LOGGER.info("Updating status");
            refresh(index);
            updateLatestStatus(index, type, loaddate);
            status.put("uploaded_docs", i);
            if (!errors.isEmpty())
                status.put("errors", errors);
        }
        return status;

    }

    /**
     * Bulk upload.
     *
     * @param errors      the errors
     * @param bulkRequest the bulk request
     */
    private static void bulkUpload(List<String> errors, StringBuilder bulkRequest) {
        try {
            Response resp = invokeAPI("POST", "/_bulk", bulkRequest.toString());
            String responseStr = EntityUtils.toString(resp.getEntity());
            if (responseStr.contains("\"errors\":true")) {
                List<String> errRecords = Util.retrieveErrorRecords(responseStr);
                LOGGER.error("Upload failed for {}", errRecords);
                errors.addAll(errRecords);
            }
        } catch (Exception e) {
            LOGGER.error("Bulk upload failed", e);
            errors.add(e.getMessage());
        }
    }

    /**
     * Bulk upload.
     *
     * @param bulkRequest the bulk request
     */
    private static void bulkUpload(StringBuilder bulkRequest) {
        try {
            Response resp = invokeAPI("POST", "/_bulk?refresh=true", bulkRequest.toString());
            String responseStr = EntityUtils.toString(resp.getEntity());
            if (responseStr.contains("\"errors\":true")) {
                LOGGER.error(responseStr);
            }
        } catch (ParseException | IOException e) {
            LOGGER.error("Error in uploading data", e);
        }
    }

    /**
     * Refresh.
     *
     * @param index the index
     */
    public static void refresh(String index) {
        try {
            Response refrehsResponse = invokeAPI("POST", index + "/" + "_refresh", null);
            if (refrehsResponse != null && HttpStatus.SC_OK != refrehsResponse.getStatusLine().getStatusCode()) {
                LOGGER.error("Refreshing index %s failed", index, refrehsResponse);
            }
        } catch (IOException e) {
            LOGGER.error("Error in refresh ", e);
        }

    }

    /**
     * Method not used by the entity upload.But to append data to speific index
     *
     * @param index   the index
     * @param type    the type
     * @param docs    the docs
     * @param idKey   the id key
     * @param refresh the refresh
     */
    public static void uploadData(String index, String type, List<Map<String, Object>> docs, String idKey,
                                  boolean refresh) {
        String actionTemplate = "{ \"index\" : { \"_index\" : \"%s\", \"_id\" : \"%s\"} }%n";
        String endpoint = "/_bulk";
        if (refresh) {
            endpoint = endpoint + "?refresh=true";
        }
        LOGGER.info("*********UPLOADING*** {}", type);
        if (null != docs && !docs.isEmpty()) {
            StringBuilder bulkRequest = new StringBuilder();
            int i = 0;
            for (Map<String, Object> doc : docs) {
                String id = doc.get(idKey).toString();
                doc.put(Constants.DOC_TYPE, type);
                doc.put("_docid", id);
                StringBuilder _doc = new StringBuilder(createESDoc(doc));

                if (_doc != null) {
                    bulkRequest.append(String.format(actionTemplate, index, id));
                    bulkRequest.append(_doc + "\n");
                }
                i++;
                if (i % 1000 == 0 || bulkRequest.toString().getBytes().length / (1024 * 1024) > 5) {
                    LOGGER.info("Uploaded {}", i);
                    bulkUpload(endpoint, bulkRequest);
                    bulkRequest = new StringBuilder();
                }
            }
            if (bulkRequest.length() > 0) {
                LOGGER.info("Uploaded {}", i);
                bulkUpload(endpoint, bulkRequest);
            }
        }

    }

    /**
     * Bulk upload.
     *
     * @param endpoint    the endpoint
     * @param bulkRequest the bulk request
     */
    private static void bulkUpload(String endpoint, StringBuilder bulkRequest) {
        try {
            Response resp = invokeAPI("POST", endpoint, bulkRequest.toString());
            String responseStr = EntityUtils.toString(resp.getEntity());
            if (responseStr.contains("\"errors\":true")) {
                LOGGER.error(responseStr);
            }
        } catch (Exception e) {
            LOGGER.error("Bulk upload failed", e);

        }
    }

    /**
     * Update latest status.
     *
     * @param index    the index
     * @param type     the type
     * @param loaddate the loaddate
     */
    public static void updateLatestStatus(String index, String type, String loaddate) {
        String updateJson = "{\"script\":{\"inline\": \"ctx._source.latest=false\"},\"query\": {\"bool\": {\"must\": [{ \"match\": {\"latest\":true}},{ \"match\": {\"docType\":\"" + type + "\"}}], \"must_not\": [{\"match\": {\"_loaddate.keyword\":\""
                + loaddate + "\"}}]}}}";
        try {
            invokeAPI("POST", index + "/" + "_update_by_query", updateJson);
        } catch (IOException e) {
            LOGGER.error("Error in updateLatestStatus", e);
        }
    }

    /**
     * Creates the ES doc.
     *
     * @param doc the doc
     * @return the string
     */
    public static String createESDoc(Map<String, ?> doc) {
        ObjectMapper objMapper = new ObjectMapper();
        String docJson = "{}";
        try {
            docJson = objMapper.writeValueAsString(doc);
        } catch (JsonProcessingException e) {
            LOGGER.error("Error createESDoc", e);
        }
        return docJson;
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
        String uri = endpoint;
        if (!uri.startsWith("/")) {
            uri = "/" + uri;
        }
        HttpEntity entity = null;
        if (payLoad != null)
            entity = new NStringEntity(payLoad, ContentType.APPLICATION_JSON);

        return getRestClient().performRequest(method, uri, Collections.emptyMap(), entity);
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
            LOGGER.error("Error indexExists", e);
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
            LOGGER.error("Error in typeExists", e);
        }

        return false;
    }

    /**
     * Gets the type count.
     *
     * @param indexName the index name
     * @param type      the type
     * @return the type count
     */
    private static int getTypeCount(String indexName, String type) {
        try {
            Response response = invokeAPI("GET", indexName + "/_count?filter_path=count",
                    "{\"query\":{ \"match\":{\"latest\":true}}}");
            String rspJson = EntityUtils.toString(response.getEntity());
            return new ObjectMapper().readTree(rspJson).at("/count").asInt();
        } catch (IOException e) {
            LOGGER.error("Error in getTypeCount", e);
        }
        return 0;
    }

    /**
     * Configure index and types.
     *
     * @param ds        the ds
     * @param errorList the error list
     */
    public static void configureIndexAndTypes(String ds, List<Map<String, String>> errorList) {
        List<String> newAssets = new ArrayList<>();
        // new code as per the latest ES version changes.
        Set<String> types = ConfigManager.getTypes(ds);
        for (String _type : types) {
            String indexName = ds + "_" + _type;
            if (!indexExists(indexName)) {
                String _payLoad = "{\"settings\" : { \"number_of_shards\" : 1,\"number_of_replicas\" : 1 , \"index.mapping.ignore_malformed\" : true,  \"index.mapping.total_fields.limit\": 2000 },\"mappings\": {";

                StringBuilder payLoad = new StringBuilder(_payLoad);
                payLoad.append("\"dynamic\": true,");
                payLoad.append("\"properties\": {");
                payLoad.append("\"" + _type + "_relations" + "\": {");
                payLoad.append("\"type\": \"join\",");
                payLoad.append("\"relations\": {");
                payLoad.append("\"" + _type + "\"" + ":" + "[\"issue_" + _type + "\"],");
                payLoad.append("\"issue_" + _type + "\"" + ":" + "[\"issue_" + _type + "_audit\",");
                payLoad.append("\"issue_" + _type + "_comment\",");
                payLoad.append("\"issue_" + _type + "_exception\"]");
                payLoad.append("}}");
                payLoad.append("}}}");
                if (!newAssets.contains(indexName)) {
                    newAssets.add(indexName);
                }
                LOGGER.info("Printing payload before creating the index: {}", payLoad);
                try {
                    invokeAPI("PUT", indexName, payLoad.toString());
                    invokeAPI("PUT", "/" + indexName + "/_alias/" + ds, null);
                    invokeAPI("PUT", "/" + indexName + "/_alias/" + "ds-all", null);
                } catch (Exception e) {
                    LOGGER.error("Error while crating the index with payload: {}", payLoad);
                    LOGGER.error("Index creation Error: {}", e.getMessage());
                    LOGGER.error("Index creation Error Trace: {}", e.getStackTrace());
                }
            }
        }
        if (!newAssets.isEmpty()) {
            try {
                updateImpactedAliases(newAssets, ds);
            } catch (Exception e) {
                LOGGER.error("Alias update failed", e);
            }
        }
        try {
            ESManager.createIndex("exceptions", errorList);
        } catch (Exception exception) {
            LOGGER.error("Index creation Error: {}", exception.getMessage());
            LOGGER.error("Index creation Error Trace: {}", exception.getStackTrace());
        }
    }

    private static void updateImpactedAliases(List<String> newIndices, String datasource) {
        ObjectMapper objectMapper = new ObjectMapper();
        List<Map<String, String>> assetGroupsList = RDSDBManager.executeQuery(String
                .format(FETCH_IMPACTED_ALIAS_QUERY_TEMPLATE, "%_*%", datasource, "%" + datasource + "_*%"));
        if (assetGroupsList.isEmpty()) {
            LOGGER.error("assetGroupsList is empty for datasource : {}", datasource);
            return;
        }
        String filter;
        String aliasUpdateQuery;
        for (String index : newIndices) {
            int updatedAssetGroups = 0;
            for (Map<String, String> assetGroup : assetGroupsList) {
                String alias = assetGroup.get("groupName");
                String groupType = assetGroup.get("groupType");
                String existingAliasQuery = assetGroup.get("aliasQuery");
                String groupId = assetGroup.get("groupId");
                if (alias == null || groupType == null || groupId == null) {
                    LOGGER.info("At least one of the fields is null. Alias: " + alias + ", GroupType: " +
                            groupType + ", GroupId: " + groupId);
                    continue;
                }
                if ((existingAliasQuery == null || existingAliasQuery.equalsIgnoreCase("null"))
                        && groupType.equalsIgnoreCase("user")) {
                    List<Map<String, String>> assetGroupTags = RDSDBManager.executeQuery(String.format(
                            CRITERIA_DETAILS_QUERY_TEMPLATE, groupId));
                    try {
                        aliasUpdateQuery = objectMapper.writeValueAsString(createAliasForStakeholderAssetGroup(alias,
                                assetGroupTags));
                    } catch (JsonProcessingException e) {
                        LOGGER.error("Error while updating alias for asset group {}", alias, e);
                        continue;
                    }
                } else if (alias.equalsIgnoreCase("all-clouds") || alias.equalsIgnoreCase(datasource)) {
                    filter = StringUtils.EMPTY;
                    aliasUpdateQuery = String.format(UPDATE_ES_ALIAS_TEMPLATE, filter, index, alias);
                } else if (groupType.equalsIgnoreCase("user") || (existingAliasQuery != null &&
                        existingAliasQuery.contains("_*"))) {
                    String filterContents = getFilterFromExistingAliasQuery(existingAliasQuery);
                    filter = filterContents.isEmpty() ? StringUtils.EMPTY :
                            String.format(FILTER_TEMPLATE, filterContents);
                    aliasUpdateQuery = String.format(UPDATE_ES_ALIAS_TEMPLATE, filter, index, alias);
                } else {
                    LOGGER.error("Cannot update alias {} for existingAliasQuery {}", alias, existingAliasQuery);
                    continue;
                }
                try {
                    invokeAPI("POST", "_aliases/", aliasUpdateQuery);
                    updatedAssetGroups++;
                } catch (IOException e) {
                    LOGGER.error("Error while updating alias for new index {} and asset group {}", index, alias, e);
                }
            }
            LOGGER.info("Updated {} asset groups from {} for index {}", updatedAssetGroups,
                    assetGroupsList.size(), index);
        }
    }

    private static String getFilterFromExistingAliasQuery(String existingAliasQuery) {
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            JsonNode rootNode = objectMapper.readTree(existingAliasQuery);
            if (rootNode != null && !rootNode.get("actions").isEmpty()) {
                rootNode = rootNode.get("actions");
                for (JsonNode actionNode : rootNode) {
                    JsonNode addNode = actionNode.path("add");
                    if (addNode.get("index").isMissingNode() || !addNode.get("index").toString().contains("_*")) {
                        continue;
                    }
                    JsonNode filterNode = addNode.get("filter");
                    if (filterNode != null && !filterNode.isMissingNode()) {
                        return filterNode.toString();
                    }
                }
            }
        } catch (Exception e) {
            LOGGER.error("Error while extracting filter from existingAliasQuery : {}", existingAliasQuery, e);
        }
        return StringUtils.EMPTY;
    }

    public static Map<String, Object> createAliasForStakeholderAssetGroup(String assetGroup,
                                                                          List<Map<String, String>> assetGroupTags) {
        Map<String, Object> alias = Maps.newHashMap();
        List<Object> action = Lists.newArrayList();
        try {
            List<String> dataSourceList = RDSDBManager.executeStringQuery(DISTINCT_DATA_SOURCES_QUERY_TEMPLATE);
            if (dataSourceList.isEmpty()) {
                LOGGER.error("");
                return Maps.newHashMap();
            }
            if (assetGroupTags.isEmpty()) {
                LOGGER.error("");
                return Maps.newHashMap();
            }
            dataSourceList = dataSourceList.stream().filter(ds -> isDataStreamOrIndexOrAliasExists(ds + "_*"))
                    .collect(Collectors.toList());
            if (dataSourceList.isEmpty()) {
                LOGGER.error("");
                return Maps.newHashMap();
            }
            Map<String, Object> tagMap = buildQueryForUserAssetGroup(assetGroupTags);
            for (String datasource : dataSourceList) {
                Map<String, Object> addObj = Maps.newHashMap();
                addObj.put("index", datasource.toLowerCase().trim() + "_*");
                addObj.put("alias", assetGroup);
                Map<String, Object> filterDetails = Maps.newHashMap();
                filterDetails.put("bool", tagMap);
                addObj.put("filter", filterDetails);
                Map<String, Object> add = Maps.newHashMap();
                add.put("add", addObj);
                action.add(add);
            }
            alias.put("actions", action);
            return alias;
        } catch (Exception exception) {
            LOGGER.error("", exception);
            return Maps.newHashMap();
        }
    }

    private static Map<String, Object> buildQueryForUserAssetGroup(List<Map<String, String>> assetGroupTags) {
        List<Object> mustList = new ArrayList<>();
        Map<String, Object> mustObj = Maps.newHashMap();
        Map<String, List<String>> groupedTags = assetGroupTags.stream()
                .collect(Collectors.groupingBy(
                        map -> map.get("attributeName"),
                        Collectors.mapping(map -> map.get("attributeValue"), Collectors.toList())
                ));
        groupedTags.forEach((tagName, tags) -> {
            mustList.add(getShouldMapForStakeholderAssetGroup(tagName, tags));
        });
        mustObj.put("must", mustList);
        return mustObj;
    }

    private static Map<String, Object> getShouldMapForStakeholderAssetGroup(String tagName, List<String> tags) {
        List<Object> matchList = new ArrayList<>();
        Map<String, Object> shouldObj = Maps.newHashMap();
        if (tags.size() == 1) {
            Map<String, Object> attributeObj = Maps.newHashMap();
            Map<String, Object> match = Maps.newHashMap();
            attributeObj.put(ES_ATTRIBUTE_TAG + tagName + ES_ATTRIBUTE_KEYWORD, tags.get(0));
            match.put("match", attributeObj);
            return match;
        } else {
            Map<String, Object> boolObj = Maps.newHashMap();
            tags.forEach(value -> {
                Map<String, Object> attributeObj = Maps.newHashMap();
                Map<String, Object> match = Maps.newHashMap();
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

    public static boolean isDataStreamOrIndexOrAliasExists(String target) {
        try {
            Response response = invokeAPI("GET", target + "?allow_no_indices=false", null);
            if (Objects.isNull(response) || Objects.isNull(response.getStatusLine())) {
                return false;
            }
            return response.getStatusLine().getStatusCode() == 200;
        } catch (Exception exception) {
            LOGGER.error("", exception);
            return false;
        }
    }

    /**
     * Gets the existing info.
     *
     * @param indexName the index name
     * @param type      the type
     * @param filters   the filters
     * @return the existing info
     */
    public static Map<String, Map<String, String>> getExistingInfo(String indexName, String type, List<String> filters) {
        int count = getTypeCount(indexName, type);
        int _count = count;
        boolean scroll = false;
        if (count > 10000) {
            _count = 10000;
            scroll = true;
        }

        String keyField = filters.get(0);
        StringBuilder filter_path = new StringBuilder("&filter_path=_scroll_id,");
        for (String _filter : filters) {
            filter_path.append("hits.hits._source." + _filter + ",");
        }
        filter_path.deleteCharAt(filter_path.length() - 1);

        String endPoint = indexName + "/_search?scroll=1m" + filter_path + "&size=" + _count;
        if (count == 0) {
            endPoint = indexName + "/_search?scroll=1m" + filter_path;
        }
        String payLoad = "{ \"query\": { \"match\": {\"latest\": true}}}";
        Map<String, Map<String, String>> _data = new HashMap<>();
        String scrollId = fetchDataAndScrollId(endPoint, _data, keyField, payLoad);

        if (scroll) {
            count -= 10000;
            do {
                endPoint = "/_search/scroll?scroll=1m&scroll_id=" + scrollId + filter_path;
                scrollId = fetchDataAndScrollId(endPoint, _data, keyField, null);
                count -= 10000;
                if (count <= 0)
                    scroll = false;
            } while (scroll);
        }
        return _data;
    }

    /**
     * Fetch data and scroll id.
     *
     * @param endPoint the end point
     * @param _data    the data
     * @param keyField the key field
     * @param payLoad  the pay load
     * @return the string
     */
    private static String fetchDataAndScrollId(String endPoint, Map<String, Map<String, String>> _data, String keyField,
                                               String payLoad) {
        try {
            ObjectMapper objMapper = new ObjectMapper();
            Response response = invokeAPI("GET", endPoint, payLoad);
            String responseJson = EntityUtils.toString(response.getEntity());
            JsonNode _info = objMapper.readTree(responseJson).at("/hits/hits");
            String scrollId = objMapper.readTree(responseJson).at("/_scroll_id").textValue();
            Iterator<JsonNode> it = _info.elements();
            String doc;
            Map<String, String> docMap;
            while (it.hasNext()) {
                doc = it.next().fields().next().getValue().toString();
                docMap = objMapper.readValue(doc, new TypeReference<Map<String, String>>() {
                });
                _data.put(docMap.get(keyField), docMap);
                docMap.remove(keyField);
            }
            return scrollId;
        } catch (ParseException | IOException e) {
            LOGGER.error("Error in fetchDataAndScrollId", e);
        }
        return "";
    }

    /**
     * Fetch current count stats for asset groups.
     *
     * @param date the date
     * @return the map
     */
    @SuppressWarnings("unchecked")
    public static Map<String, Map<String, Map<String, Object>>> fetchCurrentCountStatsForAssetGroups(String date) {

        Map<String, Map<String, Map<String, Object>>> asgInfoList = new HashMap<>();
        try {
            ObjectMapper objMapper = new ObjectMapper();
//            String payLoad = "{\"query\": { \"match\": { \"date\": \"" + date + "\"} }}";
            String payLoad = "{" +
                    "  \"query\": {" +
                    "    \"bool\": {" +
                    "      \"must\": [" +
                    "        {" +
                    "          \"match\": {" +
                    "            \"docType\": \"count_type\"" +
                    "          }" +
                    "        }," +
                    "        {" +
                    "          \"match\": { \"date\": \"" + date + "\"}}" +
                    "      ]" +
                    "    }" +
                    "  }" +
                    "}";
            Response response = invokeAPI("POST", "assetgroup_stats/_search?size=10000", payLoad);
            String responseJson = EntityUtils.toString(response.getEntity());

            Map<String, Object> docMap = objMapper.readValue(responseJson, new TypeReference<Map<String, Object>>() {
            });
            List<Map<String, Object>> docs = (List<Map<String, Object>>) ((Map<String, Object>) docMap.get("hits"))
                    .get("hits");

            for (Map<String, Object> doc : docs) {
                Map<String, Object> _doc = (Map<String, Object>) doc.get("_source");

                Map<String, Map<String, Object>> typeInfo = asgInfoList.get(_doc.get("ag").toString());
                if (typeInfo == null) {
                    typeInfo = new HashMap<>();
                    asgInfoList.put(_doc.get("ag").toString(), typeInfo);
                }

                typeInfo.put(_doc.get("type").toString(), _doc);
                _doc.remove("ag");
                _doc.remove("type");

            }
        } catch (ParseException | IOException e) {
            LOGGER.error("Error in fetchCurrentCountStatsForAssetGroups", e);
        }
        return asgInfoList;
    }

    /**
     * Creates the index.
     *
     * @param indexName the index name
     * @param errorList the error list
     */
    public static void createIndex(String indexName, List<Map<String, String>> errorList) {
        if (!indexExists(indexName)) {
            String payLoad = "{\"settings\": {  \"number_of_shards\" : 1,\"number_of_replicas\" : 1,\"index.mapping.ignore_malformed\": true }}";
            try {
                invokeAPI("PUT", indexName, payLoad);
            } catch (IOException e) {
                LOGGER.error("Error in createIndex", e);
                Map<String, String> errorMap = new HashMap<>();
                errorMap.put(ERROR, "Error in createIndex " + indexName);
                errorMap.put(ERROR_TYPE, WARN);
                errorMap.put(EXCEPTION, e.getMessage());
                errorList.add(errorMap);
            }
        }
    }

    /**
     * Creates the type.
     *
     * @param indexName the index name
     * @param typename  the typename
     * @param errorList the error list
     */
    public static void createType(String indexName, String typename, List<Map<String, String>> errorList) {
        if (!typeExists(indexName, typename)) {
            String endPoint = indexName + "/_mapping/" + typename;
            try {
                invokeAPI("PUT", endPoint, "{ \"properties\":{}}");
            } catch (IOException e) {
                LOGGER.error("Error in createType", e);
                Map<String, String> errorMap = new HashMap<>();
                errorMap.put(ERROR, "Error in createType " + typename);
                errorMap.put(ERROR_TYPE, WARN);
                errorMap.put(EXCEPTION, e.getMessage());
                errorList.add(errorMap);
            }
        }
    }

    /**
     * @param index  the index
     * @param type   child type
     * @param parent parent type
     * @throws IOException ioexception
     */
    public static void createType(String index, String type, String parent) throws IOException {
        String endPoint = index + "/_mapping";

        // Get existing children
        Map<String, Object> existingChildren = getChildRelations(index, parent);

        // Check if parent already has existing children
        List<String> existingChildTypes;
        if (existingChildren.containsKey(parent)) {
            Object existingChildObj = existingChildren.get(parent);
            if (existingChildObj instanceof String) {
                existingChildTypes = new ArrayList<>();
                existingChildTypes.add((String) existingChildObj);
            } else {
                existingChildTypes = (List<String>) existingChildObj;
            }
        } else {
            existingChildTypes = new ArrayList<>();
        }

        // Add new child
        if (!existingChildTypes.contains(type)) {
            existingChildTypes.add(type);
            existingChildren.put(parent, existingChildTypes);

            // Create childMap with updated relations
            Map<String, Object> childMap = new HashMap<>();
            childMap.put("type", "join");
            childMap.put("relations", existingChildren);

            Map<String, Object> properties = new HashMap<>();
            properties.put(parent + "_relations", childMap);

            Map<String, Object> payloadMap = new HashMap<>();
            payloadMap.put("properties", properties);

            String payLoad = new ObjectMapper().writeValueAsString(payloadMap);
            LOGGER.info("creating Type: ");
            LOGGER.info("Index: {}", index);
            LOGGER.info("type: {}", type);
            LOGGER.info("parent: {}", parent);
            LOGGER.info("payLoad: {}", payLoad);
            try {
                invokeAPI("PUT", endPoint, payLoad);
            } catch (IOException e) {
                LOGGER.error("Error createType ", e);
                LOGGER.error("Index: {}", index);
                LOGGER.error("type: {}", type);
                LOGGER.error("parent: {}", parent);
                LOGGER.error("payLoad: {}", payLoad);

            }
        }
    }

    // Helper function to retrieve existing child relations
    private static Map<String, Object> getChildRelations(String index, String parent) throws IOException {
        String endPoint = index + "/_mapping";
        Response response = invokeAPI("GET", endPoint, null);
        JsonNode node = new ObjectMapper().readTree(EntityUtils.toString(response.getEntity()));
        JsonNode properties = node.at("/" + index + "/mappings/properties");
        JsonNode relations = properties.get(parent + "_relations").get("relations");
        LOGGER.info("Printing relations here: {}", relations.toString());
        LOGGER.info("Printing relations JSON here: {}", new ObjectMapper().convertValue(relations, Map.class));
        return new ObjectMapper().convertValue(relations, Map.class);
    }

    /**
     * added for uploading Child docs where parent id could be dervied from
     * child.
     *
     * @param index the index
     * @param type  the type
     * @param docs  the docs
     */
    public static void uploadData(String index, String parentType, String type, List<Map<String, Object>> docs, String[] key, String dataSource) {
        String actionTemplate = "{ \"index\" : { \"_index\" : \"%s\" , \"routing\" : \"%s\" } }";
        LOGGER.info("*********UPLOADING*** {}:::{}", type, index);
        if (null != docs && !docs.isEmpty()) {
            StringBuilder bulkRequest = new StringBuilder();
            int i = 0;
            Gson gson = new GsonBuilder().create(); // create Gson instance
            for (Map<String, Object> doc : docs) {

                String _doc = new Gson().toJson(doc);
                String parent = Util.concatenate(doc, key, "_");
                if ("aws".equalsIgnoreCase(dataSource)) {
                    if (Arrays.asList(key).contains("accountid")) {
                        parent = dataSource + "_" + parentType + "_" + parent;
                    }
                }
                bulkRequest.append(String.format(actionTemplate, index, parent)).append("\n");
                bulkRequest.append(_doc).append("\n");
                i++;
                if (i % 1000 == 0 || bulkRequest.toString().getBytes().length / (1024 * 1024) > 5) {
                    bulkUpload(bulkRequest);
                    bulkRequest = new StringBuilder();
                }
            }
            if (bulkRequest.length() > 0) {
                bulkUpload(bulkRequest);
            }
        }
    }

    public static void uploadData(String index, List<Map<String, Object>> docs, String dataSource) {
        String actionTemplate = "{ \"index\" : { \"_index\" : \"%s\" , \"_id\" : \"%s\", \"routing\" : \"%s\" } }";
        LOGGER.info("*********UPLOADING*** {}:::{}", index);
        if (null != docs && !docs.isEmpty()) {
            StringBuilder bulkRequest = new StringBuilder();
            int i = 0;
            for (Map<String, Object> doc : docs) {

                String _doc = new Gson().toJson(doc);
                String parent = (String) doc.get("_resourceid");
                String _id = (String) doc.get("annotationid");

                bulkRequest.append(String.format(actionTemplate, index, _id, parent)).append("\n");
                bulkRequest.append(_doc).append("\n");
                i++;
                if (i % 1000 == 0 || bulkRequest.toString().getBytes().length / (1024 * 1024) > 5) {
                    bulkUpload(bulkRequest);
                    bulkRequest = new StringBuilder();
                }
            }
            if (bulkRequest.length() > 0) {
                bulkUpload(bulkRequest);
            }
        }
    }

    public static void uploadAuditLogData(String index, List<Map<String, Object>> docs) {
        String actionTemplate = "{ \"index\" : { \"_index\" : \"%s\" , \"_id\" : \"%s\", \"routing\" : \"%s\" } }";
        long dateInMillSec = new Date().getTime();

        LOGGER.info("*********UPLOADING*** {}:::{}", index);
        if (null != docs && !docs.isEmpty()) {
            StringBuilder bulkRequest = new StringBuilder();
            int i = 0;
            for (Map<String, Object> doc : docs) {

                String _doc = new Gson().toJson(doc);
                String parent = (String) doc.get("annotationid");
                String _id = CommonUtils.getUniqueIdForString(parent + dateInMillSec);

                bulkRequest.append(String.format(actionTemplate, index, _id, parent)).append("\n");
                bulkRequest.append(_doc).append("\n");
                i++;
                if (i % 1000 == 0 || bulkRequest.toString().getBytes().length / (1024 * 1024) > 5) {
                    bulkUpload(bulkRequest);
                    bulkRequest = new StringBuilder();
                }
            }
            if (bulkRequest.length() > 0) {
                bulkUpload(bulkRequest);
            }
        }
    }

    private static String convertToString(Object value) {
        if (value instanceof Map) {
            Map<String, Object> map = (Map<String, Object>) value;
            StringBuilder builder = new StringBuilder("{");
            for (Map.Entry<String, Object> entry : map.entrySet()) {
                builder.append('"').append(entry.getKey()).append("\":");
                builder.append(convertToString(entry.getValue())).append(",");
            }
            if (builder.charAt(builder.length() - 1) == ',') {
                builder.setCharAt(builder.length() - 1, '}');
            } else {
                builder.append('}');
            }
            return builder.toString();
        } else if (value instanceof List) {
            List<Object> list = (List<Object>) value;
            StringBuilder builder = new StringBuilder("[");
            for (Object obj : list) {
                builder.append(convertToString(obj)).append(",");
            }
            if (builder.charAt(builder.length() - 1) == ',') {
                builder.setCharAt(builder.length() - 1, ']');
            } else {
                builder.append(']');
            }
            return builder.toString();
        } else if (value instanceof String) {
            return value.toString();
        } else {
            return value.toString();
        }
    }

    /**
     * Delete old documents.
     *
     * @param index the index
     * @param type  the type
     * @param field the field
     * @param value the value
     */
    public static void deleteOldDocuments(String index, String type, String field, String value) {
        String deleteJson = "{\"query\":{\"bool\":{\"must_not\":[{\"match\":{\"" + field + "\":\"" + value + "\"}}],"
                + "\"must\":[{\"match\":{\"docType.keyword\":\"" + type + "\"}}]}}}";
        try {
            invokeAPI("POST", index + "/" + "_delete_by_query", deleteJson);
        } catch (IOException e) {
            LOGGER.error("Error deleteOldDocuments ", e);
        }
    }

    /**
     * Update load date.
     *
     * @param index the index
     * @param query the query
     */
    public static long updateLoadDate(String index, String query) {
        LOGGER.info("Error records processed for {} ", index);
        try {
            Response updateInfo = invokeAPI("POST", index + "/" + "_update_by_query", query);
            String updateInfoJson = EntityUtils.toString(updateInfo.getEntity());
            return new JsonParser().parse(updateInfoJson).getAsJsonObject().get("updated").getAsLong();
        } catch (IOException e) {
            LOGGER.error("Error in updateLoadDate", e);
        }
        return 0L;
    }

    public static void createNestedType(String indexName, String typename, List<Map<String, String>> errorList) {
        if (!typeExists(indexName, typename)) {
            String endPoint = indexName + "/_mapping/" + typename;
            try {
                invokeAPI("PUT", endPoint, "{ \"properties\":{\"assetCount\":{\"type\":\"nested\",\"properties\":{}}}}");
            } catch (IOException e) {
                LOGGER.error("Error in createType", e);
                Map<String, String> errorMap = new HashMap<>();
                errorMap.put(ERROR, "Error in createType " + typename);
                errorMap.put(ERROR_TYPE, WARN);
                errorMap.put(EXCEPTION, e.getMessage());
                errorList.add(errorMap);
            }
        }
    }

    /**
     * Below method deletes violation records for resources which are deleted.
     *
     * @param entities
     * @param index
     */
    public static void removeViolationForDeletedAssets(List<Map<String, Object>> entities, String index) {
        try {
            if (entities != null && !entities.isEmpty()) {
                String combinedResourceIdString = entities.stream().map(entity -> "\"" + entity.get("_resourceid") + "\"").collect(Collectors.joining(","));
                String requestBody = "{\n" +
                        "    \"script\": {\n" +
                        "    \"inline\": \"ctx._source.issueStatus = 'deleted'\",\n" +
                        "    \"lang\": \"painless\"\n" +
                        "  }," +
                        "  \"query\": {\n" +
                        "    \"bool\": {\n" +
                        "      \"must\": [\n" +
                        "        {\n" +
                        "          \"term\": {\n" +
                        "            \"issueStatus.keyword\": \"open\"\n" +
                        "          }\n" +
                        "        },\n" +
                        "        {\n" +
                        "          \"term\": {\n" +
                        "            \"type.keyword\": \"issue\"\n" +
                        "          }\n" +
                        "        },\n" +
                        "        {\n" +
                        "          \"bool\": {\n" +
                        "            \"must_not\": [\n" +
                        "              {\n" +
                        "                \"terms\": {\n" +
                        "                  \"_resourceid.keyword\": [ %s ]\n" +
                        "                }\n" +
                        "              }\n" +
                        "            ]\n" +
                        "          }\n" +
                        "        }\n" +
                        "      ]\n" +
                        "    }\n" +
                        "  }\n" +
                        "}";

                requestBody = String.format(requestBody, combinedResourceIdString);
                invokeAPI("POST", "/" + index + "/_update_by_query", requestBody);
            }
        } catch (Exception exception) {
            LOGGER.info("Failed to delete violation records of deleted asssets of index " + index + ". Error Message - " + exception.getMessage());
        }
    }
}
