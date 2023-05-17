package com.tmobile.cso.pacman.qualys.util;

import java.io.IOException;
import java.util.*;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.tmobile.cso.pacman.qualys.Constants;
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

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.Gson;


/**
 * The Class ElasticSearchManager.
 */
public class ElasticSearchManager {

    /** The Constant LOGGER. */
    private static final Logger LOGGER = LoggerFactory.getLogger(ElasticSearchManager.class);

    /** The Constant ES_HOST_KEY_NAME. */
    private static final String ES_HOST_KEY_NAME = System.getProperty("elastic-search.host");

    /** The Constant ES_HTTP_PORT. */
    private static final Integer ES_HTTP_PORT = Integer.parseInt(System.getProperty("elastic-search.port"));

    /** The rest client. */
    private static RestClient restClient;

    /**
     * Instantiates a new elastic search manager.
     */
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
     * @param indexName the index name
     */
    public static void createIndex(String index, String type) {
        String indexName = "/"+index;
        if (!indexExists(indexName)) {
            String _payLoad = "{\"settings\" : { \"number_of_shards\" : 1,\"number_of_replicas\" : 1 },\"mappings\": {";

            StringBuilder payLoad = new StringBuilder(_payLoad);
            payLoad.append("\"dynamic\": true,");
            payLoad.append("\"properties\": {");
            //payLoad.append("\"docType\": {");
            //payLoad.append("\"type\": \"keyword\",");
            //payLoad.append("\"index\": true");
            //payLoad.append("},");
            payLoad.append("\"" + type + "_relations" + "\": {");
            payLoad.append("\"type\": \"join\",");
            payLoad.append("\"relations\": {");
            payLoad.append("\"" + type + "\"" + ":" + "[\"issue_" + type + "\"],");
            payLoad.append("\"issue_" + type + "\"" + ":" + "[\"issue_" + type + "_audit\",");
            payLoad.append("\"issue_" + type + "_comment\",");
            payLoad.append("\"issue_" + type + "_exception\"]");
            payLoad.append("}}");
            payLoad.append("}}}");

            LOGGER.info("Creating index with payload: {}", payLoad);
            try {
                invokeAPI("PUT", indexName, payLoad.toString());
            } catch (IOException e) {
                LOGGER.error("Error createIndex ", e);
            }
        }
    }
    public static void createTypeWithParent(String index, String type, String parent) throws IOException {
//        if (!typeExists(index, type)) {
        String endPoint = "/"+index + "/_mapping";

        // Get existing children
        Map<String, Object> existingChildren = getChildRelations(index, parent);
        if(!isTypeExists(existingChildren,index,parent,type)){

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

    private static boolean isTypeExists(Map<String, Object> existingChildren, String index, String parent, String type) {

        boolean isExists=false;
        List<String> existingChildTypes;
        if (existingChildren.containsKey(parent)) {
            Object existingChildObj = existingChildren.get(parent);
            if (existingChildObj instanceof String) {
                if(type.equalsIgnoreCase((String) existingChildObj)){
                    isExists= true;
                }
            } else {
                existingChildTypes = (List<String>) existingChildObj;
                String matchingChild = existingChildTypes.stream().filter(ch -> ch.equalsIgnoreCase(type)).findAny().orElse(null);
                isExists= matchingChild!=null;
            }
        }
        LOGGER.info("Index Exists: {}",isExists);
        return isExists;
    }

    private static Map<String, Object> getChildRelations(String index, String parent) throws IOException {
        String endPoint = "/"+index + "/_mapping";
        Response response = invokeAPI("GET", endPoint, null);
        JsonNode node = new ObjectMapper().readTree(EntityUtils.toString(response.getEntity()));
        JsonNode properties = node.at("/" + index + "/mappings/properties");
        JsonNode relations = properties.get(parent + "_relations").get("relations");
        LOGGER.info("Printing relations here: {}", relations.toString());
        LOGGER.info("Printing relations JSON here: {}", new ObjectMapper().convertValue(relations, Map.class));
        return new ObjectMapper().convertValue(relations, Map.class);
    }

    /**
     * Creates the type.
     *
     * @param indexName the index name
     * @param typename the typename
     */
    public static void createType(String index, String typename) {
        String indexName = "/"+index;
        if (!typeExists(indexName, typename)) {
            String endPoint = indexName + "/_mapping/" + typename;
            try {
                invokeAPI("PUT", endPoint, "{ \"properties\":{}}");
            } catch (IOException e) {
                LOGGER.error("Error in method createType", e);
                ;
            }
        }
    }

    /**
     * Creates the type as parent.
     *
     * @param indexName the index name
     * @param typename the typename
     */
    public static void createTypeAsParent(String indexName, String typename) {
        if (!typeExists(indexName, typename)) {
            String endPoint = indexName + "/_mapping/" + typename;
            try {
                invokeAPI("PUT", endPoint, "{ \"properties\":{}, \"issue_" + typename
                        + "\":{ \"_parent\": { \"type\": \"" + typename + "\" }}	}");
            } catch (IOException e) {
                LOGGER.error("Error at createTypeAsParent", e);
            }
        }
    }

    /**
     * Creates the alias.
     *
     * @param indexName the index name
     * @param aliasName the alias name
     */
    public static void createAlias(String indexName, String aliasName) {
        try {
            invokeAPI("PUT", "/" + indexName + "/_alias/" + aliasName, null);
        } catch (IOException e) {
            LOGGER.error("Error in createAlias ", e);
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
     * @param type the type
     * @param docs the docs
     * @param idKey the id key
     */
    public static void uploadData(String index, String type, List<Map<String, Object>> docs, String idKey) {
        String actionTemplate = "{ \"index\" : { \"_index\" : \"%s\",\"_id\" : \"%s\"} }%n";

        LOGGER.info("*********UPLOADING*** {}", type);
        if (null != docs && !docs.isEmpty()) {
            StringBuilder bulkRequest = new StringBuilder();
            int i = 0;
            for (Map<String, Object> doc : docs) {
                doc.put(Constants.DOC_TYPE, type);
                doc.put(type + "_relations", type);
                String id = doc.get(idKey).toString();
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
     * added for uploading Child docs where parent id could be dervied from
     * child.
     *
     * @param index the index
     * @param type the type
     * @param docs the docs
     * @param parentKey the parent key
     */
    public static void uploadData(String index, String type, List<Map<String, Object>> docs, String parentKey,String idKey,boolean removeIdKey) {
        String actionTemplate = "{ \"index\" : { \"_index\" : \"%s\", \"_type\" : \"%s\", \"_id\" : \"%s\" , \"_parent\" : \"%s\"} }%n";

        LOGGER.info("*********UPLOADING*** {}", type);
        if (null != docs && !docs.isEmpty()) {
            StringBuilder bulkRequest = new StringBuilder();
            int i = 0;
            for (Map<String, Object> doc : docs) {


                String parent = doc.get(parentKey).toString();
                String id =  doc.get(idKey).toString();
                if(removeIdKey){
                    doc.remove(idKey);
                }
                StringBuilder _doc = new StringBuilder(new Gson().toJson(doc));
                bulkRequest.append(String.format(actionTemplate, index, type,id, parent));
                bulkRequest.append(_doc + "\n");
                i++;
                if (i % 1000 == 0 || bulkRequest.toString().getBytes().length / (1024 * 1024) > 5) {
                    LOGGER.info("Uploading {}", i);
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
        String indexName = "/"+index;
        try {
            Response refrehsResponse = invokeAPI("POST", indexName + "/" + "_refresh", null);
            if (refrehsResponse != null && HttpStatus.SC_OK != refrehsResponse.getStatusLine().getStatusCode()) {
                LOGGER.error("Refreshing index {} failed", index, refrehsResponse);
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

    public static String createESStatsDoc(Map<String, ?> doc) {
        ObjectMapper objMapper = new ObjectMapper();
        String docJson = "{}";
        try {
            docJson = objMapper.writeValueAsString(doc);
        } catch (JsonProcessingException e) {
            LOGGER.error("Error createESDoc",e);
        }
        return docJson;
    }

    /**
     * Invoke API.
     *
     * @param method the method
     * @param endpoint the endpoint
     * @param payLoad the pay load
     * @return the response
     * @throws IOException Signals that an I/O exception has occurred.
     */
    public static Response invokeAPI(String method, String endpoint, String payLoad) throws IOException {
        HttpEntity entity = null;
        if (payLoad != null)
            entity = new NStringEntity(payLoad, ContentType.APPLICATION_JSON);
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
                return response.getStatusLine().getStatusCode() == 200 ? true : false;
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
     * @param type the type
     * @return true, if successful
     */
    private static boolean typeExists(String indexName, String type) {
        String payload="{\"query\":{\"bool\":{\"must\":[{\"term\":{\"docType\":\""+type+"\"}}]}}}";
        LOGGER.info("Checking if the index type exists. Endpoint:{}/_search, payload:{}",indexName,payload);
        try {
            Response response = invokeAPI("GET", "/"+indexName + "/_search/", payload);
            if (response != null) {
                return response.getStatusLine().getStatusCode() == 200 ? true : false;
            }
        } catch (IOException e) {
            LOGGER.error("Error typeExists ", e);
        }

        return false;
    }

    /**
     * Gets the type count.
     *
     * @param indexName the index name
     * @param type the type
     * @return the type count
     */
    private static int getTypeCount(String indexName, String type) {
        String countPayload="{\"query\":{\"bool\":{\"must\":[{\"term\":{\"docType\":\""+type+"\"}}]}}}";
        try {
            LOGGER.debug("Fetching type count with endpoint:{}/_count?filter_path=count, Payload:{} ",indexName,countPayload);
            Response response = invokeAPI("GET", indexName + "/_count?filter_path=count", countPayload);
            String rspJson = EntityUtils.toString(response.getEntity());
            return new ObjectMapper().readTree(rspJson).at("/count").asInt();
        } catch (IOException e) {
            LOGGER.error("Error getTypeCount ", e);
        }
        return 0;
    }

    /**
     * Creates the type.
     *
     * @param index the index
     * @param type the type
     * @param parent the parent
     */
    public static void createType(String index, String type, String parent) {
        String indexName = "/"+index;
        if (!typeExists(indexName, type)) {
            String endPoint = indexName + "/_mapping/" + type;
            String payLoad = "{\"_parent\": { \"type\": \"" + parent + "\" } }";
            try {
                invokeAPI("PUT", endPoint, payLoad);
            } catch (IOException e) {
                LOGGER.error("Error createType ", e);
            }
        }
    }

    /**
     * Gets the existing info.
     *
     * @param indexName the index name
     * @param type      the type
     * @param filters   the filters
     * @param latest    the latest
     * @return the existing info
     */
    public static Map<String, Map<String, Object>> getExistingInfo(String index, String type, List<String> filters,
                                                                   boolean latest) {
        String indexName = "/"+index;
        LOGGER.info("Fetching existing info from indexName: {}",indexName);
        int count = getTypeCount(indexName, type);
        int _count = count;
        boolean scroll = false;
        int SCROLL_SIZE = 10000;
        if (count > SCROLL_SIZE) {
            _count = SCROLL_SIZE;
            scroll = true;
        }

        String keyField = filters.get(0);
        String filter_path = "&filter_path=hits.hits._source,_scroll_id";

        StringBuilder payLoad = new StringBuilder("{ \"_source\": [");
        for (String _filter : filters) {
            payLoad.append("\"" + _filter + "\",");
        }
        payLoad.deleteCharAt(payLoad.length() - 1);
        payLoad.append("],");
        payLoad.append("\"query\": {\n \"bool\": {\n \"must\": [{\n \"term\": {\n \"docType\": \""+type+"\"\n }\n }");
        if (latest) {
            payLoad.append(",{\n  \"term\": {\n  \"latest\": \"true\"\n  }\n }");
        }
        payLoad.append("]\n }\n }\n }");

        String endPoint = indexName + "/_search?scroll=5m" + filter_path + "&size=" + _count;

        LOGGER.info("getExistingInfo endpoint: {}",endPoint);
        Map<String, Map<String, Object>> _data = new HashMap<>();
        String scrollId = fetchDataAndScrollId(endPoint, _data, keyField, payLoad.toString());

        if (scroll) {
            count -= SCROLL_SIZE;
            do {
                endPoint = "/_search/scroll";
                String payload="{\n" +
                        "\"scroll\" : \"5m\", \n" +
                        "\"scroll_id\" : \""+scrollId+"\" \n" +
                        "}";
                scrollId = fetchScrollPost(endPoint, _data, keyField, payload);
                count -= SCROLL_SIZE;
                if (count < 0)
                    scroll = false;
            } while (scroll);
        }
        // invokeAPI("DELETE", "/_search/scroll?scroll_id="+scrollId, null);
        return _data;
    }

    /**
     * Fetch data and scroll id.
     *
     * @param endPoint the end point
     * @param _data the data
     * @param keyField the key field
     * @param payLoad the pay load
     * @return the string
     */
    private static String fetchDataAndScrollId(String endPoint, Map<String, Map<String, Object>> _data, String keyField,
                                               String payLoad) {
        LOGGER.debug("fetchDataAndScrollId >> endpoint :{}, payload: {} ", endPoint,payLoad);
        try {
            ObjectMapper objMapper = new ObjectMapper();
            Response response = invokeAPI("GET", endPoint, payLoad);
            String responseJson = EntityUtils.toString(response.getEntity());
            JsonNode _info = objMapper.readTree(responseJson).at("/hits/hits");
            String scrollId = objMapper.readTree(responseJson).at("/_scroll_id").textValue();
            Iterator<JsonNode> it = _info.elements();
            while (it.hasNext()) {
                String doc = it.next().fields().next().getValue().toString();
                Map<String, Object> docMap = new ObjectMapper().readValue(doc,
                        new TypeReference<Map<String, Object>>() {
                        });
                _data.put(docMap.get(keyField).toString(), docMap);
                docMap.remove(keyField);
            }
            return scrollId;
        } catch (ParseException | IOException e) {
            LOGGER.error("Error fetchDataAndScrollId ", e);
        }
        return "";

    }

    private static String fetchScrollPost(String endPoint, Map<String, Map<String, Object>> _data, String keyField,
                                          String payLoad) {
        LOGGER.debug("fetchScrollPost >> endpoint :{}, payload: {} ", endPoint,payLoad);
        try {
            ObjectMapper mapper = new ObjectMapper();
            Response apiResponse = invokeAPI("POST", endPoint, payLoad);
            String responseJson = EntityUtils.toString(apiResponse.getEntity());
            JsonNode _info = mapper.readTree(responseJson).at("/hits/hits");
            String scrollId = mapper.readTree(responseJson).at("/_scroll_id").textValue();
            Iterator<JsonNode> iterator = _info.elements();
            while (iterator.hasNext()) {
                Iterator<Map.Entry<String, JsonNode>> fields = iterator.next().fields();
                String doc="";
                while(fields.hasNext()){
                    Map.Entry<String, JsonNode> field = fields.next();
                    if(field.getKey().equalsIgnoreCase("_source")){
                        doc=field.getValue().toString();
                    }
                }
                Map<String, Object> documentMap = new ObjectMapper().readValue(doc,
                        new TypeReference<Map<String, Object>>() {
                        });
                _data.put(documentMap.get(keyField).toString(), documentMap);
                documentMap.remove(keyField);
            }
            return scrollId;
        } catch (ParseException | IOException e) {
            LOGGER.error("Error fetchDataAndScrollId ", e);
        }
        return "";

    }
    /**
     * Update latest status.
     *
     * @param index the index
     * @param type the type
     * @param discoveryDate the discovery date
     */
    public static void updateLatestStatus(String index, String type, String discoveryDate) {
        String indexName = "/"+index;
        String updateJson = "{\"script\":{\"inline\": \"ctx._source.latest=false\"},\"query\": {\"bool\": {\"must\": [{ \"match\": {\"latest\":true}},{ \"match\": {\"docType.keyword\":\""+type+"\"}}], \"must_not\": [{\"match\": {\"discoverydate.keyword\":\""
                + discoveryDate + "\"}}]}}}";
        try {
            invokeAPI("POST", indexName +  "/_update_by_query", updateJson);
        } catch (IOException e) {
            LOGGER.error("Error updateLatestStatus ", e);
        }
    }

    /**
     * Delete old documents.
     *
     * @param index the index
     * @param type the type
     * @param field the field
     * @param value the value
     */
    public static void deleteOldDocuments(String index, String type, String field, String value) {
        String indexName = "/"+index;
        String deleteJson = "{\"query\": {\"bool\": {\"must_not\": [{ \"match\": {\"" + field + "\":\"" + value
                + "\"}}]}}}";
        try {
            invokeAPI("POST", indexName + "/" + type + "/" + "_delete_by_query", deleteJson);
        } catch (IOException e) {
            LOGGER.error("Error deleteOldDocuments ", e);
        }
    }
}
