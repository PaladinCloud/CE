/*******************************************************************************
 * Copyright 2018 T Mobile, Inc. or its affiliates. All Rights Reserved.
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
package com.tmobile.pacman.publisher.impl;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.google.gson.JsonObject;
import org.apache.http.HttpHost;
import org.opensearch.action.bulk.BulkItemResponse;
import org.opensearch.action.bulk.BulkRequest;
import org.opensearch.action.bulk.BulkResponse;
import org.opensearch.action.index.IndexRequest;
import org.opensearch.client.RequestOptions;
import org.opensearch.client.RestClient;
import org.opensearch.client.RestHighLevelClient;
import org.opensearch.common.xcontent.XContentType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;
import com.tmobile.pacman.common.AutoFixAction;
import com.tmobile.pacman.common.PacmanSdkConstants;
import com.tmobile.pacman.commons.autofix.AutoFixPlan;
import com.tmobile.pacman.dto.AutoFixTransaction;
import com.tmobile.pacman.util.CommonUtils;
import com.tmobile.pacman.util.ESUtils;

import static com.tmobile.pacman.common.PacmanSdkConstants.DOC_TYPE;

// not using the old way , this is the new class to publish data to ES , all old code will be refactored to use this one

/**
 * The Class ElasticSearchDataPublisher.
 */
public class ElasticSearchDataPublisher {


    /**
     * The Constant BULK_INDEX_REQUEST_TEMPLATE.
     */
    private static final String BULK_INDEX_REQUEST_TEMPLATE = "{ \"index\" : { \"_index\" : \"%s\", \"routing\" : \"%s\"} }%n";

    /**
     * The Constant logger.
     */
    private static final Logger logger = LoggerFactory.getLogger(ElasticSearchDataPublisher.class);

    /**
     * The client.
     */
    private RestHighLevelClient client;


    /**
     * test mode flag *.
     */
    boolean testMode = false;

    /**
     * Instantiates a new elastic search data publisher.
     */
    public ElasticSearchDataPublisher() {
        client = new RestHighLevelClient(RestClient.builder(new HttpHost(ESUtils.getESHost(), ESUtils.getESPort())));
    }

    /**
     * Instantiates a new elastic search data publisher.
     *
     * @param testMode the test mode
     */
    public ElasticSearchDataPublisher(Boolean testMode) {
        this.testMode = testMode;
    }

    /**
     * Publish auto fix transactions.
     *
     * @param autoFixTrans the auto fix trans
     * @param ruleParam
     * @return the int
     */
    public int publishAutoFixTransactions(List<AutoFixTransaction> autoFixTrans, Map<String, String> ruleParam) {

        if (autoFixTrans != null && autoFixTrans.size() == 0) {
            return 0;
        }

        BulkRequest bulkRequest = new BulkRequest();
        Gson gson = new Gson();
        StringBuffer bulkRequestBody = new StringBuffer();
        String response = "";
        List<Map<String, Map>> responseList = new ArrayList<>();
        String esUrl = ESUtils.getEsUrl();
        String bulkPostUrl = esUrl + AnnotationPublisher.BULK_WITH_REFRESH_TRUE;
        final String autoFixType = PacmanSdkConstants.TYPE_FOR_AUTO_FIX_RECORD + "_" + ruleParam.get(PacmanSdkConstants.TARGET_TYPE);
        for (AutoFixTransaction autoFixTransaction : autoFixTrans) {
            // first post auto fix as child doc of type
            if (AutoFixAction.AUTOFIX_ACTION_FIX.equals(autoFixTransaction.getAction())) {
                try {
                    if (!ESUtils.isParentChildRelationExists(ESUtils.getEsUrl(), getIndexName(ruleParam), ruleParam.get(PacmanSdkConstants.TARGET_TYPE), autoFixType)) {
                        try {
                            ESUtils.createMappingWithParent(ESUtils.getEsUrl(), getIndexName(ruleParam), autoFixType, ruleParam.get(PacmanSdkConstants.TARGET_TYPE));
                        } catch (Exception e) {
                            logger.error("unable to create child type");
                        }
                    }
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
                try {
                    // parent child document seems to have some issue
                    bulkRequestBody.append(String.format(BULK_INDEX_REQUEST_TEMPLATE, getIndexName(ruleParam), getDocId(autoFixTransaction)));

                    bulkRequestBody.append(gson.toJson(addRelationsToTransaction(autoFixTransaction, autoFixType)));
                    bulkRequestBody.append("\n");
                    if (bulkRequestBody.toString().getBytes().length
                            / (1024 * 1024) >= PacmanSdkConstants.ES_MAX_BULK_POST_SIZE) {
                        response = CommonUtils.doHttpPost(bulkPostUrl, bulkRequestBody.toString(), new HashMap());
                        responseList.add(gson.fromJson(response, Map.class));
                        bulkRequestBody.setLength(0);
                    }
                } catch (Exception e) {
                    logger.error("error occurred while indexing auto fix document", e);
                    autoFixTransaction.setAdditionalInfo("error occurred while indexing auto fix document " + e.getMessage());
                }
            }
            // build transaction log
            IndexRequest indexRequest = new IndexRequest(
                    CommonUtils.getPropValue(PacmanSdkConstants.AUTO_FIX_TRAN_INDEX_NAME_KEY));
            indexRequest.source(gson.toJson(addDocType(autoFixTransaction, CommonUtils.getPropValue(PacmanSdkConstants.AUTO_FIX_TRAN_TYPE_NAME_KEY))), XContentType.JSON);
            bulkRequest.add(indexRequest);
        }

        // post the remaining data if available
        if (bulkRequestBody.length() > 0) {
            response = CommonUtils.doHttpPost(bulkPostUrl, bulkRequestBody.toString(), new HashMap<>());
        }
        responseList.add(gson.fromJson(response, Map.class));

        // now post transaction log
        try {
            if (null != client) {
                BulkResponse bulkResponse = client.bulk(bulkRequest, RequestOptions.DEFAULT);
                if (bulkResponse.hasFailures()) {
                    if (!isIndexAvailable(bulkResponse.getItems())) {
                        logger.info("index not found to write the transaction logs, creating one");
                        // version 5.6 does not support index creation via API,
                        // hence executing a post
                        try {
                            CommonUtils
                                    .doHttpPut(
                                            ESUtils.getEsUrl() + "/"
                                                    + CommonUtils
                                                    .getPropValue(PacmanSdkConstants.AUTO_FIX_TRAN_INDEX_NAME_KEY),
                                            "");
                            publishAutoFixTransactions(autoFixTrans, ruleParam); // index should be created now
                        } catch (Exception e) {

                            logger.error("error creating index", e);
                        }
                    }
                }
            }
        } catch (IOException e) {
            logger.error("error posting auto fix transaction log", e);
            return -1;
        }
        return 0;
    }

    private String addRelationsToTransaction(AutoFixTransaction autoFixTransaction, String autoFixType) {
        Gson gson = new Gson();
        String json = gson.toJson(autoFixTransaction);
        JsonObject jsonObject = gson.fromJson(json, JsonObject.class);

        // Add the parent-child mapping
        Map<String, String> relations = new HashMap<>();
        relations.put("parent", autoFixTransaction.getParentDocId());
        relations.put("name", autoFixType);
        JsonObject relationsObject = new JsonObject();
        for (Map.Entry<String, String> entry : relations.entrySet()) {
            relationsObject.addProperty(entry.getKey(), entry.getValue());
        }
        jsonObject.addProperty(DOC_TYPE, autoFixType);
        jsonObject.add(autoFixTransaction.getTargetType() + "_relations", relationsObject);

        return jsonObject.toString();
    }

    private String addDocType(AutoFixTransaction autoFixTransaction, String type) {
        Gson gson = new Gson();
        String json = gson.toJson(autoFixTransaction);
        JsonObject jsonObject = gson.fromJson(json, JsonObject.class);
        jsonObject.addProperty(DOC_TYPE, type);

        return jsonObject.toString();
    }

    /**
     * @param autoFixTransaction
     * @return
     */
    private String getDocId(AutoFixTransaction autoFixTransaction) {
        return autoFixTransaction.getAccountId() + "_" + autoFixTransaction.getRegion() + "_" + autoFixTransaction.getResourceId();
    }

    /**
     * @param ruleParam
     * @return
     */
    public static String getIndexName(Map<String, String> ruleParam) {

        return ruleParam.get(PacmanSdkConstants.DATA_SOURCE_KEY).replace("_all", "") + "_"
                + ruleParam.get(PacmanSdkConstants.TARGET_TYPE);
    }

    /**
     * Checks if is index avaialble.
     *
     * @param bulkItemResponses the bulk item responses
     * @return the boolean
     */
    private Boolean isIndexAvailable(BulkItemResponse[] bulkItemResponses) {
        return null == Arrays.stream(bulkItemResponses)
                .filter(x -> x.getFailure().getCause().getMessage().contains("no such index")).findAny().orElse(null);
    }


    /**
     * Close.
     */
    public void close() {
        if (null != client)
            try {
                client.close();
            } catch (IOException e) {
                logger.error("error closing rest client", e);
            }

        client = null;
    }

    /**
     * @return
     */
    public <T> Boolean postDocAsChildOfType(AutoFixPlan plan, String docId, Map<String, String> ruleParam, String planTypeName, String parentType, String parentId) {

        logger.debug("posting the plan to ES plan id =  {}", plan.getPlanId());
        Gson gson = new Gson();
        try {
            if (!ESUtils.isParentChildRelationExists(ESUtils.getEsUrl(), getIndexName(ruleParam), parentType, planTypeName)) {
                try {
                    ESUtils.createMappingWithParent(ESUtils.getEsUrl(), getIndexName(ruleParam), planTypeName, parentType);
                } catch (Exception e) {
                    logger.error("unable to create child type");
                }
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        IndexRequest indexRequest = new IndexRequest(getIndexName(ruleParam));

        indexRequest.source(gson.toJson(addRelationsToPlan(plan, planTypeName, parentType, parentId)), XContentType.JSON);
        indexRequest.routing(parentId);
        // indexRequest.parent(parentId);
        try {
            client.index(indexRequest, RequestOptions.DEFAULT);
        } catch (IOException e) {
            logger.error("error indexing autofix plan", e);
            return false;
        }
        logger.debug("posts the plan to ES plan id =  {}", plan.getPlanId());

        return true;
    }

    /**
     * @param plan
     * @param planTypeName
     * @param parentType
     * @param parentId
     * @return String
     */
    private static String addRelationsToPlan(AutoFixPlan plan, String planTypeName, String parentType, String parentId) {
        Gson gson = new Gson();
        String json = gson.toJson(plan);
        JsonObject jsonObject = gson.fromJson(json, JsonObject.class);

        // Add the parent-child mapping
        Map<String, String> relations = new HashMap<>();
        relations.put("parent", parentId);
        relations.put("name", planTypeName);
        JsonObject relationsObject = new JsonObject();
        for (Map.Entry<String, String> entry : relations.entrySet()) {
            relationsObject.addProperty(entry.getKey(), entry.getValue());
        }
        jsonObject.addProperty(DOC_TYPE, planTypeName);
        jsonObject.add(parentType + "_relations", relationsObject);

        return jsonObject.toString();
    }
}
