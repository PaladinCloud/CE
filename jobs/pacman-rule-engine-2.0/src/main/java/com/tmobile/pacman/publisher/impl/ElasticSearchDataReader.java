/**
  Copyright (C) 2017 T Mobile Inc - All Rights Reserve
  Purpose:
  Author :kkumar28
  Modified Date: Jul 2, 2019
  
**/
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
import java.net.MalformedURLException;

import org.opensearch.action.get.GetRequest;
import org.opensearch.action.get.GetResponse;
import org.opensearch.action.search.SearchRequest;
import org.opensearch.action.search.SearchResponse;
import org.opensearch.client.RequestOptions;
import org.opensearch.index.query.QueryBuilder;
import org.opensearch.index.query.QueryBuilders;
import org.opensearch.rest.RestStatus;
import org.opensearch.search.SearchHits;
import org.opensearch.search.builder.SearchSourceBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.tmobile.pacman.util.ESUtils;


/**
 * @author kkumar28
 *
 */
public class ElasticSearchDataReader extends ElasticSearchDataInterface {

    
    
    /**
     * @throws MalformedURLException 
     * 
     */
    public ElasticSearchDataReader() throws Exception {
        super();
    }
    
    
    private static final Logger logger = LoggerFactory.getLogger(ElasticSearchDataReader.class);

    
    /**
     * 
     * @param index
     * @param type
     * @param id
     * @return
     * @throws IOException
     */
    public String getDocumentById(String index,String type, String id,String parentId) throws IOException{
        GetRequest req = new GetRequest(index, id);
        req.routing(parentId);
        GetResponse response = client.get(req, RequestOptions.DEFAULT);
        return response.getSourceAsString();
    }
    
    
    /**
     * @param indexNameFromRuleParam
     * @param autoFixPlanType
     * @param resourceId
     * @return
     * @throws IOException 
     */
    public String searchDocument(String indexNameFromRuleParam, String autoFixPlanType, String resourceId) throws IOException {

        SearchSourceBuilder sourceBuilder = new SearchSourceBuilder();
        QueryBuilder termQuery1 = QueryBuilders.termQuery(ESUtils.convertAttributeToKeyword("resourceId"), resourceId);
        QueryBuilder termQuery2 = QueryBuilders.termQuery(ESUtils.convertAttributeToKeyword("docType"), autoFixPlanType);
        QueryBuilder boolQueryBuilder = QueryBuilders.boolQuery()
                .must(termQuery1)
                .must(termQuery2);
        sourceBuilder.query(boolQueryBuilder);
        SearchRequest searchRequest = new SearchRequest(indexNameFromRuleParam);
        searchRequest.source(sourceBuilder);

        logger.debug("searching auto fix plan with query ", searchRequest.toString());

        SearchResponse response = client.search(searchRequest, RequestOptions.DEFAULT);
        SearchHits hits = response.getHits();
        if (RestStatus.OK == response.status() && hits.getTotalHits().value > 0) {
            return hits.getAt(0).getSourceAsString();
        } else {
            throw new IOException(String.format("no plan found for resource %s", resourceId));
        }
    }
}
