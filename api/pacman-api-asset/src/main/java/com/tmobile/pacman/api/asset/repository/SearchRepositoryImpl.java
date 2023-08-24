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
package com.tmobile.pacman.api.asset.repository;

import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

import com.google.common.collect.HashMultimap;
import joptsimple.internal.Strings;

import org.apache.http.HttpEntity;
import org.apache.http.HttpHost;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.entity.ContentType;
import org.apache.http.nio.entity.NStringEntity;
import org.apache.http.util.EntityUtils;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestClientBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Repository;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.tmobile.pacman.api.asset.AssetConstants;
import com.tmobile.pacman.api.asset.domain.SearchResult;
import com.tmobile.pacman.api.asset.service.AssetService;
import com.tmobile.pacman.api.commons.Constants;
import com.tmobile.pacman.api.commons.exception.DataException;
import com.tmobile.pacman.api.commons.repo.ElasticSearchRepository;
import com.tmobile.pacman.api.commons.repo.PacmanRdsRepository;
import com.tmobile.pacman.api.commons.utils.PacHttpUtils;

/**
 * Implemented class for SearchRepository and all its method
 */
@Repository
public class SearchRepositoryImpl implements SearchRepository {

    private static final Logger LOGGER = LoggerFactory.getLogger(SearchRepositoryImpl.class);
    private static final String PROTOCOL = "http://";
    @Autowired
    private PacmanRdsRepository rdsRepository;

    @Value("${elastic-search.host}")
    private String esHost;
    @Value("${elastic-search.port}")
    private int esPort;
    @Value("${vulnerability.types}")
    private String configuredVulnTargetTypes;
    @Value("${datasource.types:aws,azure}")
    private String dataSourceTypes;

    private Gson gson = new Gson();

    @Autowired
    ElasticSearchRepository esRepository;

    @Autowired
    AssetService assetService;

    private static RestClient restClient;

    private static Map<String, Map<String, String>> categoryToRefineByMap = new HashMap<>();
    private static Map<String, Map<String, String>> categoryToReturnFieldsMap = new HashMap<>();

    private synchronized void fetchConfigFromDB() {

        if (categoryToRefineByMap.size() > 0 || categoryToReturnFieldsMap.size() > 0) {
            return;
        }

        String query = "select SEARCH_CATEGORY,RESOURCE_TYPE,REFINE_BY_FIELDS,RETURN_FIELDS FROM OmniSearch_Config";
        List<Map<String, Object>> resultsList = rdsRepository.getDataFromPacman(query);

        Iterator<Map<String, Object>> rowIterator = resultsList.iterator();

        while (rowIterator.hasNext()) {
            Map<String, Object> currentRowMap = rowIterator.next();
            String searchCategory = currentRowMap.get("SEARCH_CATEGORY") != null
                    ? currentRowMap.get("SEARCH_CATEGORY").toString().trim() : "";
            String resourceType = currentRowMap.get("RESOURCE_TYPE") != null
                    ? currentRowMap.get("RESOURCE_TYPE").toString().trim() : "";
            String refineByFields = currentRowMap.get("REFINE_BY_FIELDS") != null
                    ? currentRowMap.get("REFINE_BY_FIELDS").toString().trim() : "";
            String returnFields = currentRowMap.get("RETURN_FIELDS") != null
                    ? currentRowMap.get("RETURN_FIELDS").toString().trim() : "";

            Map<String, String> resourceTypeToRefineByMap = categoryToRefineByMap.get(searchCategory);
            if (null == resourceTypeToRefineByMap) {
                resourceTypeToRefineByMap = new HashMap<>();
            }
            resourceTypeToRefineByMap.put(resourceType, refineByFields);

            Map<String, String> resourceTypeToReturnFieldMap = categoryToReturnFieldsMap.get(searchCategory);
            if (null == resourceTypeToReturnFieldMap) {
                resourceTypeToReturnFieldMap = new HashMap<>();
            }
            resourceTypeToReturnFieldMap.put(resourceType, returnFields);

            categoryToRefineByMap.put(searchCategory, resourceTypeToRefineByMap);
            categoryToReturnFieldsMap.put(searchCategory, resourceTypeToReturnFieldMap);

        }
    }

    @Override
    public SearchResult fetchSearchResultsAndSetTotal(String ag, String domain, boolean includeAllAssets,
            String targetType, String searchText, Map<String, List<String>> lowLevelFilters, int from, int size,
            SearchResult result, String searchCategory) throws DataException {

        if (categoryToRefineByMap.size() == 0 || categoryToReturnFieldsMap.size() == 0) {

            fetchConfigFromDB();

        }

        Map<String, Object> mustFilter = new LinkedHashMap<>();
        Map<String, Object> mustTermsFilter = new LinkedHashMap<>();

        lowLevelFilters.forEach((displayName, valueList) -> {
            String esFieldName = getFieldMappingsForSearch(targetType, false, searchCategory).get(displayName)
                    + ".keyword";
            mustTermsFilter.put(esFieldName, valueList);
        });

        String esType = null;

        if (AssetConstants.ASSETS.equals(searchCategory)) {
            if (!includeAllAssets) {
                mustFilter.put(Constants.LATEST, Constants.TRUE);
            }
            mustFilter.put(AssetConstants.UNDERSCORE_ENTITY, Constants.TRUE);
            if (null == targetType) {
                mustTermsFilter.put(AssetConstants.UNDERSCORE_ENTITY_TYPE_KEYWORD, getTypesForDomain(ag, domain));
            }
            esType = targetType;
        }

        if (AssetConstants.POLICY_VIOLATIONS.equals(searchCategory)) {
            mustFilter.put("type.keyword", "issue");
            mustTermsFilter.put("issueStatus.keyword", Arrays.asList("open", "exempted"));
            if (null == targetType) {
                esType = null;
                mustTermsFilter.put("targetType.keyword", getTypesForDomain(ag, domain));

            } else {
                esType = "issue_" + targetType;
            }

        }

        if (AssetConstants.VULNERABILITIES.equals(searchCategory)) {
            mustFilter.put(Constants.LATEST, Constants.TRUE);
            mustTermsFilter.put(Constants.SEVEITY_LEVEL+".keyword",
                    Arrays.asList(Constants.THREE, Constants.FOUR, Constants.FIVE));

            if (null != targetType) {
                mustFilter.put("_index", "aws_" + targetType);
            }
            esType = Constants.VULN_INFO;
        }
        long start = System.currentTimeMillis();

//        if (!Strings.isNullOrEmpty(searchText)) {
//            searchText = "\"" + searchText + "\"";
//        }
        List<Map<String, Object>> results = new ArrayList<>();
        if (!AssetConstants.VULNERABILITIES.equals(searchCategory)) {

            List<Map<String, Object>> sortFieldsMapList = new ArrayList<>();
            Map<String, Object> resourceIdSort = new HashMap<>();
            resourceIdSort.put("_resourceid.keyword", "asc");
            sortFieldsMapList.add(resourceIdSort);

            StringBuilder urlToQuery = new StringBuilder(PROTOCOL + esHost + ":" + esPort + "/" + ag);
            if (!Strings.isNullOrEmpty(esType)) {
                mustFilter.put(AssetConstants.DOC_TYPE_KEYWORD, esType);
            }
            urlToQuery.append("/").append("_search?from=").append(from).append("&size=").append(size);
            String resultJson = null;
            try {
                resultJson = getSearchResults(mustFilter, searchText, mustTermsFilter, getReturnFieldsForSearch(targetType, searchCategory),ag,searchCategory,size);
            } catch (Exception e) {
                LOGGER.error("Exception occurred for search category {} with message - {}",searchCategory,e.getMessage());
                throw new RuntimeException(e);
            }
            JsonObject responseJson = null;
            if (!Objects.isNull(resultJson)) {
                responseJson = (JsonObject) JsonParser.parseString(resultJson);
            }
            if (responseJson != null && responseJson.has(AssetConstants.HITS) && responseJson.get(AssetConstants.HITS)
                    .getAsJsonObject().has(AssetConstants.TOTAL) && responseJson.get(AssetConstants.HITS)
                    .getAsJsonObject().get(AssetConstants.TOTAL).isJsonObject() && responseJson.get(AssetConstants.HITS)
                    .getAsJsonObject().get(AssetConstants.TOTAL).getAsJsonObject().has(AssetConstants.VALUE) &&
                    responseJson.get(AssetConstants.HITS).getAsJsonObject().get(AssetConstants.TOTAL).getAsJsonObject()
                            .get(AssetConstants.VALUE).isJsonPrimitive()) {
                result.setTotal(responseJson.get(AssetConstants.HITS).getAsJsonObject().get(AssetConstants.TOTAL)
                        .getAsJsonObject().get(AssetConstants.VALUE).getAsLong());
            }
            esRepository.processResponseAndSendTheScrollBack(resultJson, results);

        } else {
            List<String> returnFields = getReturnFieldsForSearch(targetType, searchCategory);
            String docQueryString = "";
            docQueryString = new StringBuilder(docQueryString).append("[").toString();
            int count = 0;
            for (String returnField : returnFields) {
                if (count == 0) {
                    docQueryString = new StringBuilder(docQueryString).append("doc['").append(returnField)
                            .append("'].value").toString();
                } else {
                    docQueryString = new StringBuilder(docQueryString).append("doc['").append(returnField)
                            .append(".keyword'].value").toString();

                }
                count++;

                if (count < returnFields.size()) {
                    docQueryString = new StringBuilder(docQueryString).append(" +'~'+").toString();
                }

            }
            docQueryString = docQueryString + "]";

            String url = PROTOCOL + esHost + ":" + esPort + "/" + ag + "/" + Constants.VULN_INFO + "/"
                    + Constants.SEARCH;
            Map<String, Object> query = new HashMap<>();
            query.put("query", esRepository.buildQuery(mustFilter, null, null, searchText, mustTermsFilter, null));

            String queryString = new Gson().toJson(query);
            String payload = queryString.substring(0, queryString.length() - 1)
                    + ", \"aggs\": {\"qids\": {\"terms\": {\"script\": \"" + docQueryString + "\",\"size\": 10000}}}}";
            String responseJson = "";
            try {
                long startAggs = System.currentTimeMillis();
                LOGGER.debug("To get vuln aggs without dups, url is: {} and payload is: {}", url, payload);
                responseJson = PacHttpUtils.doHttpPost(url, payload);
                LOGGER.debug(AssetConstants.DEBUG_RESPONSEJSON, responseJson);
                long endAggs = System.currentTimeMillis();
                LOGGER.debug("Time taken for ES call(vuln aggs sans dups) is: {}", (endAggs - startAggs));
                results = getDistFromVulnAggsResult(responseJson, returnFields);
            } catch (Exception e) {
                LOGGER.error("Failed to retrieve vuln aggs for omni search ", e);
            }

        }

        results = pruneResults(results, targetType, searchCategory);
        if (AssetConstants.VULNERABILITIES.equals(searchCategory)) {
            result.setTotal(results.size());

            int end = from + size;
            if (end > (results.size())) {
                from = 0;
                end = results.size();
            }
            results = results.subList(from, end);
        }

        result.setResults(results);
        long end = System.currentTimeMillis();
        LOGGER.debug("Time taken to perform search for Search Category {} is: {}", searchCategory, (end - start));

        return result;

    }

    private List<Map<String,Object>> getTokenListFromSearchText(String searchText) throws Exception {
        StringBuilder urlToAnalyze = new StringBuilder(PROTOCOL + esHost + ":" + esPort + "/_analyze");
        if (searchText.startsWith("\"") && searchText.endsWith("\"")) {
            searchText= searchText.substring(1, searchText.length() - 1);
        }

        String payloadForTokens = "{\n" +
                "  \"text\" : \""+searchText+"\"\n" +
                "}\n";
        String resultJson = PacHttpUtils.doHttpPost(urlToAnalyze.toString(),payloadForTokens);

        Map<String,Object> tokensMap = gson.fromJson(resultJson,Map.class);
        List<Map<String,Object>> tokenList = (List<Map<String,Object>>)tokensMap.get("tokens");
        return tokenList;
    }

    private String getResultsFromElastic(final Map<String, Object> mustFilter, final Map<String, Object> mustNotFilter,
                                         final HashMultimap<String, Object> shouldFilter, final String searchText,
                                         final Map<String, Object> mustTermsFilter, Map<String, List<String>> matchPhrasePrefix,
                                         List<String> fieldsForSearch, String searchType, List<String> returnFieldsForSearch, String ag, String searchCategory, int size) throws Exception {

        Map<String, Object> query = new HashMap<>();
        Map<String,Object> queryMap = "quick".equalsIgnoreCase(searchType)?esRepository.buildQuery(mustFilter, null, null, searchText,
                mustTermsFilter, null,fieldsForSearch):esRepository.buildQuery(mustFilter, null, null, searchText,
                mustTermsFilter, null);
        query.put("size",String.valueOf(size));
        query.put("query", queryMap);
        query.put("_source", returnFieldsForSearch);
        Map<String, Object> resourceIdSort = new HashMap<>();
        resourceIdSort.put("_docid.keyword", "asc");
        query.put("sort", resourceIdSort);
        String payload = gson.toJson(query);
        LOGGER.info("payload in data search thread for search category -{} is  {}",searchCategory,payload);
        StringBuilder url = new StringBuilder(PROTOCOL + esHost + ":" + esPort + "/"+ag+"/_search");
        return PacHttpUtils.doHttpPost(url.toString(),payload);
    }

    private String getSearchResults(Map<String, Object> mustFilter, String searchText, Map<String, Object> mustTermsFilter, List<String> returnFieldsForSearch, String ag, String searchCategory, int size) throws Exception {
        long start = System.currentTimeMillis();
        List<Map<String,Object>> tokenList = getTokenListFromSearchText(searchText);
        List<String> fieldsForSearch = AssetConstants.ASSETS.equalsIgnoreCase(searchCategory)?
                Arrays.asList("*","_docid","_entity","_entitytype","_cloudType","_resourcename","_resourceid"):
                Arrays.asList("*","_docid","_resourceid");

        String caseSensitiveStr="";
        /*
        If it is a case sensitive search, following query criteria shall be used.
        {"query_string":{"query":"*user-provided-string-without-quotes*", "default_operator":"AND", "analyzer": "keyword"}}
         */
        if((searchText.startsWith("\"") && searchText.endsWith("\"")) || (tokenList.size()==0 && searchText.length()>0)){
            if(searchText.startsWith("\"") && searchText.endsWith("\"")) {
                caseSensitiveStr = searchText.substring(1, searchText.length() - 1);
            }
            StringBuilder tempStr1 = new StringBuilder();
            for(char c: caseSensitiveStr.toCharArray()){
                String tempStr2 = Character.toString(c);
                //Escape the below characters to avoid tokenizing by analyzer
                if(Arrays.asList("-"," ","/",":","*").contains(tempStr2)){
                    tempStr1.append("\\"+tempStr2);
                }
                else{
                    tempStr1.append(tempStr2);
                }
            }
            caseSensitiveStr = tempStr1+"*";
            Map<String,Object> queryMap = esRepository.buildQuery(mustFilter, null, null, caseSensitiveStr,
                    mustTermsFilter, null);
            Optional<Map<String,Object>> optionalQueryStringMap = Optional.ofNullable(queryMap).map(obj -> (Map<String,Object>)obj.get("bool")).map(obj -> (List<Map<String,Object>>)obj.get("must"))
                    .map(obj -> obj.stream().filter(map1 -> map1.containsKey("simple_query_string")).findFirst()).map(obj -> {
                        if(obj.isPresent()){
                            return obj.get();
                        }
                        else return null;
                    });
            if(optionalQueryStringMap.isPresent()){
                Map<String,Object> queryStringOuterMap = optionalQueryStringMap.get();
                Map<String,Object> queryStringMap = (Map<String,Object>)queryStringOuterMap.get("simple_query_string");
                queryStringMap.put("analyzer","keyword");
                queryStringMap.put("fields",Arrays.asList("*.keyword"));
                Map<String, Object> query = new HashMap<>();
                query.put("size",String.valueOf(size));
                query.put("query", queryMap);
                query.put("_source", returnFieldsForSearch);
                String payload = gson.toJson(query);
                LOGGER.info("payload of case sensitive search in data search thread for search category -{} is  {}",searchCategory,payload);
                StringBuilder url = new StringBuilder(PROTOCOL + esHost + ":" + esPort + "/"+ag+"/_search");
                String caseSensitiveSearchResult = PacHttpUtils.doHttpPost(url.toString(),payload);
                LOGGER.info("case sensitive result obtained for data search thread for search category -{}",searchCategory);
                long end = System.currentTimeMillis();
                LOGGER.error("Time taken in data search thread for Search Category {} is: {}", searchCategory, (end - start));
                return caseSensitiveSearchResult;
            }
        }
        else{
            /*
            If it is case insensitive search, first step is to find tokens of search text  by /_analyze request. If number of tokens are > 2,
            first and second criteria are used, else  first and third criteria are used.
            First criteria fetches accurate and quicker results whereas second and third criterias may fetch inaccurate results with time delay.
            First criteria query is triggered initially and if 0 results are obtained, then another ES query is triggered by using second or third criteria
            based on number of tokens. Results thus obtained will be returned. If first criteria query returns non zero results, then the same is returned.

            First criteria :
            {"multi_match":{"query":"user-provided-search-text","type":"phrase_prefix","fields":["*","_docid","_entity","_docid","_entitytype","_cloudType","_resourcename"]}
            Second criteria :
            {"multi_match":{"query":"user-provided-search-text","type":"phrase_prefix","fields":["*","_docid","_entity","_docid","_entitytype","_cloudType","_resourcename"]}
            Difference between first and second criteria is first token is removed from user provided search text in second criteria.
            Third criteria :
            {"query_string":{"query":"*user-provided-search-text*", "default_operator":"AND"}}

             */
            String result = getResultsFromElastic(mustFilter, null, null, searchText, mustTermsFilter, null,fieldsForSearch,"quick",returnFieldsForSearch,ag,searchCategory,size);
            int count = getResultCount(result);
            LOGGER.info("results(1) obtained in data search thread for search category -{} , count - {}",searchCategory,count);
            if(count==0){
                if(tokenList.size()>2){
                    List<String> tokensExcludingFirstTokenList = new ArrayList<>();
                    for (int i = 1; i < tokenList.size(); i++) {
                        tokensExcludingFirstTokenList.add(tokenList.get(i).get("token").toString());
                    }
                    String secondResult = getResultsFromElastic(mustFilter, null, null, String.join(" ", tokensExcludingFirstTokenList), mustTermsFilter, null,fieldsForSearch,"quick",returnFieldsForSearch,ag, searchCategory, size);
                    LOGGER.info("results(2) obtained for data search thread for search category -{}",searchCategory);
                    long end = System.currentTimeMillis();
                    LOGGER.info("Time taken in data search thread for Search Category {} is: {}", searchCategory, (end - start));
                    return secondResult;
                }
                else{
                    String expandedSearchText = tokenList.stream().map(obj -> obj.get("token").toString()).collect(Collectors.joining(" "))+"*";
                    //   String searchTextForCaseSensitive = "";
                    String thirdResult = getResultsFromElastic(mustFilter, null, null, expandedSearchText, mustTermsFilter, null,fieldsForSearch,"slow",returnFieldsForSearch,ag, searchCategory, size);
                    LOGGER.info("results(3) obtained for data search thread for search category -{} ",searchCategory);
                    long end = System.currentTimeMillis();
                    LOGGER.info("Time taken in data search thread for Search Category {} is: {}", searchCategory, (end - start));
                    return thirdResult;
                }
            }
            else{
                long end = System.currentTimeMillis();
                LOGGER.info("Time taken in data search thread for Search Category {} is: {}", searchCategory,(end - start));
                return result;
            }
        }
        return "{}";
    }


    private int getResultCount(String value) {
        Gson gson = new Gson();
        Map<String,Object> resultMap = gson.fromJson(value,Map.class);
        Optional<Double> countOptional = Optional.ofNullable(resultMap).map(obj -> (Map<String,Object>)obj.get("hits")).map(obj -> (Map<String,Object>)obj.get("total")).map(obj -> (Double)obj.get("value"));
        if(countOptional.isPresent()){
            return countOptional.get().intValue();
        }
        return 0;
    }

    @Override
    public List<Map<String, Object>> fetchTargetTypes(String ag, String searchText, String searchCategory,
                                                      String domain, boolean includeAllAssets) throws DataException {
        List<Map<String, Object>> resourceTypeBucketList = new ArrayList<>();
        if (AssetConstants.VULNERABILITIES.equals(searchCategory)) {
            return resourceTypeBucketList;
        }

        if (categoryToRefineByMap.size() == 0 || categoryToReturnFieldsMap.size() == 0) {

            fetchConfigFromDB();

        }


        String aggStringForHighLevelEntities = "";
        if (AssetConstants.ASSETS.equals(searchCategory)) {
            aggStringForHighLevelEntities = "\"targetTypes\":{\"terms\":{\"field\":\"_entitytype.keyword\",\"size\":10000}}";
        }

        if (AssetConstants.POLICY_VIOLATIONS.equals(searchCategory)) {
            aggStringForHighLevelEntities = "\"targetTypes\":{\"terms\":{\"field\":\"targetType.keyword\",\"size\":10000}}";
        }

        /*if (AssetConstants.VULNERABILITIES.equals(searchCategory)) {
            aggStringForHighLevelEntities = "\"targetTypes\":{\"terms\":{\"field\":\"_index\",\"size\":10000},\"aggs\":{\"unique\":{\"cardinality\":{\"field\":\""
                    + getReturnFieldsForSearch(null, searchCategory).get(0) + "\"}}}}";
        }*/
        String responseJson="";
        try{
            responseJson = getSearchResultsForTargetTypes(searchText, searchCategory, includeAllAssets, aggStringForHighLevelEntities,ag, null);
            if("{}".equalsIgnoreCase(responseJson)){
                return resourceTypeBucketList;
            }
        } catch (Exception e) {
            LOGGER.error("Failed to retrieve high level entity types for omni search ", e);
            return resourceTypeBucketList;
        }
        /*if (AssetConstants.VULNERABILITIES.equals(searchCategory)) {
            firstUrl = PROTOCOL + esHost + ":" + esPort + "/" + ag + "/" + Constants.VULN_INFO + "/" + Constants.SEARCH;
        }*/

        resourceTypeBucketList = getDistributionFromAggResult(responseJson, "targetTypes");

        // Atleast one refinement should be defined for the entity. If not, kick
        // it out
        removeResourceTypeIfNoMappingDefined(resourceTypeBucketList, searchCategory);

        removeResourceTypeIfNotAttachedToDomain(ag, resourceTypeBucketList, domain);

        return resourceTypeBucketList;
    }

    private String getSearchResultsForTargetTypes(String searchText, String searchCategory, boolean includeAllAssets, String aggStringForHighLevelEntities, String ag, String resourceType) throws Exception {
        String payLoadStr = createPayLoad(aggStringForHighLevelEntities, searchText, searchCategory, resourceType,
                includeAllAssets);
        List<Map<String,Object>> tokenListFromSearchText = getTokenListFromSearchText(searchText);
        String firstUrl = PROTOCOL + esHost + ":" + esPort + "/" + ag + "/" + Constants.SEARCH;
        String searchFieldsStr = AssetConstants.ASSETS.equals(searchCategory)? "\"fields\": [\"*\",\"_docid\",\"_entity\",\"_resourcename\",\"_resourceid\",\"_entitytype\",\"_cloudType\"]\n" :
                "\"fields\": [\"*\",\"_docid\",\"_resourceid\"]\n";
        String multiMatchCondition = "{\"multi_match\":{\n" +
                "                  \"query\":\"%s\",\n" +
                "                  \"type\": \"phrase_prefix\",\n" +
                searchFieldsStr +
                "               }\n" +
                "            }";
        String queryStringCondition = "{\n" +
                "               \"simple_query_string\":{\n" +
                "                  \"query\":\"%s\",\n" +
                "                  \"default_operator\": \"AND\"\n" +
                "               }\n" +
                "            }";
        String queryStringCaseSensitiveCondition = "{\n" +
                "               \"simple_query_string\":{\n" +
                "                  \"query\":\"%s\",\n" +
                "                  \"default_operator\": \"AND\",\n" +
                "                   \"analyzer\":\"keyword\", \n" +
                "                   \"fields\" : [\"*.keyword\"] \n" +
                "               }\n" +
                "            }";
        if((searchText.startsWith("\"") && searchText.endsWith("\"")) || (tokenListFromSearchText.size()==0 && searchText.length()>0)) {
            String caseSensitiveStr="";
            if (searchText.startsWith("\"") && searchText.endsWith("\"")) {
                caseSensitiveStr = searchText.substring(1, searchText.length() - 1);
            }
            StringBuilder tempStr1 = new StringBuilder();
            for (char c : caseSensitiveStr.toCharArray()) {
                String tempStr2 = Character.toString(c);
                //Escape the below character to avoid tokenizing by
                if (Arrays.asList("-", " ", "/", ":", "*").contains(tempStr2)) {
                    tempStr1.append("\\\\" + tempStr2);
                } else {
                    tempStr1.append(tempStr2);
                }
            }
            caseSensitiveStr = tempStr1.toString()+"*";
            String caseSensitivePayload = String.format(payLoadStr,String.format(queryStringCaseSensitiveCondition,caseSensitiveStr));
            LOGGER.info("case sensitive payload in outgoing filter thread for search category -{} is  {}",searchCategory,caseSensitivePayload);
            String responseJson = PacHttpUtils.doHttpPost(firstUrl, caseSensitivePayload);
            LOGGER.info("case sensitive response in outgoing filter thread for search category -{} ",searchCategory);
            return responseJson;
        }
        else{
            String multiMatchStr1 = String.format(multiMatchCondition,searchText);
            LOGGER.info("payload(1) in outgoing filter thread for search category -{} is  {}",searchCategory,String.format(payLoadStr,multiMatchStr1));
            String responseJson = PacHttpUtils.doHttpPost(firstUrl, String.format(payLoadStr,multiMatchStr1));
            int count = getResultCount(responseJson);
            LOGGER.info("results(1) obtained for outgoing filter thread for search category -{} count -  {}",searchCategory,count);
            if(count>0){
                return responseJson;
            }
            else{
                if(tokenListFromSearchText.size()>2){
                    List<String> tokensExcludingFirstTokenList = new ArrayList<>();
                    for (int i = 1; i < tokenListFromSearchText.size(); i++) {
                        tokensExcludingFirstTokenList.add(tokenListFromSearchText.get(i).get("token").toString());
                    }
                    String multiMatchStr2 = String.format(multiMatchCondition,String.join(" ",tokensExcludingFirstTokenList));
                    LOGGER.info("payload(2) in outgoing filter thread for search category -{} is {}",searchCategory,String.format(payLoadStr,multiMatchStr2));
                    responseJson = PacHttpUtils.doHttpPost(firstUrl, String.format(payLoadStr,multiMatchStr2));
                    LOGGER.info("results(2) obtained for outgoing filter thread for search category -{}"+searchCategory);
                    return responseJson;
                }
                else{
                    String expandedSearchText = tokenListFromSearchText.stream().map(obj -> obj.get("token").toString()).collect(Collectors.joining(" "))+"*";
                    String queryStr = String.format(queryStringCondition,expandedSearchText);
                    LOGGER.info("payload(3) in outgoing filter thread for search category -{} is {}",searchCategory,String.format(payLoadStr,queryStr));
                    responseJson = PacHttpUtils.doHttpPost(firstUrl, String.format(payLoadStr,queryStr));
                    LOGGER.info("results(3) obtained for outgoing filter thread for search category -{} ",searchCategory);
                    return responseJson;
                }
            }
        }
    }

    private List<String> getTypesForDomain(String ag, String domain) {
        List<Map<String, Object>> domainData = assetService.getTargetTypesForAssetGroup(ag, domain, null);
        List<String> typesForDomain = new ArrayList<>();
        domainData.forEach(domainMap -> {
            domainMap.forEach((key, value) -> {
                if (key.equals("type")) {
                    typesForDomain.add(value.toString());
                }
            });
        });
        return typesForDomain;
    }

    private synchronized void removeResourceTypeIfNotAttachedToDomain(String ag,
            List<Map<String, Object>> resourceTypeBucketList, String domain) {

        List<String> typesForDomain = getTypesForDomain(ag, domain);

        Iterator<Map<String, Object>> resourceIterator = resourceTypeBucketList.iterator();
        while (resourceIterator.hasNext()) {
            String resourceType = resourceIterator.next().get(AssetConstants.FIELDNAME).toString();
            if (!typesForDomain.contains(resourceType)) {
                resourceIterator.remove();
            }
        }
    }

    private synchronized void removeResourceTypeIfNoMappingDefined(List<Map<String, Object>> resourceTypeBucketList,
            String searchCategory) {
        Iterator<Map<String, Object>> resourceIterator = resourceTypeBucketList.iterator();
        while (resourceIterator.hasNext()) {
            String resourceType = resourceIterator.next().get(AssetConstants.FIELDNAME).toString();
            Map<String, String> fieldMapping = getFieldMappingsForSearch(resourceType, false, searchCategory);
            if (fieldMapping.isEmpty()) {
                resourceIterator.remove();
            }
        }
    }

    @Override
    public Map<String, List<Map<String, Object>>> fetchDistributionForTargetType(String ag, String resourceType,
            String searchText, String searchCategory, boolean includeAllAssets) {

        Map<String, List<Map<String, Object>>> returnBucketMap = new LinkedHashMap<>();

        // Prepare aggregation string based on what resourceType we are
        // dealing with
        StringBuilder aggregationStrBuffer = new StringBuilder();
        Map<String, String> fieldMapping = getFieldMappingsForSearch(resourceType, false, searchCategory);
        if (fieldMapping.isEmpty()) {
            return returnBucketMap;
        }

        fieldMapping.forEach((displayName, esFieldName) -> {
            aggregationStrBuffer.append(
                    "\"" + displayName + "\":{\"terms\":{\"field\":\"" + esFieldName + ".keyword\",\"size\":10000}");

            if (AssetConstants.VULNERABILITIES.equals(searchCategory)) {

                aggregationStrBuffer.append(",\"aggs\":{\"unique\":{\"cardinality\":{\"field\":\""
                        + getReturnFieldsForSearch(resourceType, searchCategory).get(0) + "\"}}}");
            }
            aggregationStrBuffer.append("}");

            // Trailing comma
            aggregationStrBuffer.append(",");
        });

        // Remove the trailing comma, because of the iteration above
        String aggStringForLowLevelMenu = aggregationStrBuffer.toString().substring(0,
                aggregationStrBuffer.toString().length() - 1);

//        String lowLevelPayLoadStr = createPayLoad(aggStringForLowLevelMenu, searchText, searchCategory, resourceType,
//                includeAllAssets);

        /*Commented because we are not using Constants.VULN_INFO index anymore*/
        /*if (AssetConstants.VULNERABILITIES.equals(searchCategory)) {
            secondUrl = PROTOCOL + esHost + ":" + esPort + "/" + ag + "/" + Constants.VULN_INFO + "/"
                    + Constants.SEARCH;
        }*/

        try {
            long start = System.currentTimeMillis();
            final String lowLevelResponseJson = getSearchResultsForTargetTypes(searchText, searchCategory, includeAllAssets, aggStringForLowLevelMenu,ag,resourceType);
            LOGGER.debug(AssetConstants.DEBUG_RESPONSEJSON, lowLevelResponseJson);
            long end = System.currentTimeMillis();
            LOGGER.debug("Search Category {}", searchCategory);
            LOGGER.debug("Target type {}", resourceType);
            LOGGER.debug("Time taken for ES call(refineBy) is: {}", (end - start));

            fieldMapping.forEach((displayName, esFieldName) -> {
                List<Map<String, Object>> detailsBucketList = getDistributionFromAggResult(lowLevelResponseJson,
                        displayName);
                if (!detailsBucketList.isEmpty()) {
                    returnBucketMap.put(displayName, detailsBucketList);
                }

            });
        } catch (Exception e) {
            LOGGER.error("Error fetching distributions from ES:", e);
        }

        return returnBucketMap;
    }

    private List<Map<String, Object>> getDistributionFromAggResult(String responseJson, String aggName) {
        JsonParser jsonParser = new JsonParser();
        JsonObject resultJson = jsonParser.parse(responseJson).getAsJsonObject();
        JsonArray types = resultJson.get("aggregations").getAsJsonObject().get(aggName).getAsJsonObject().get("buckets")
                .getAsJsonArray();
        List<Map<String, Object>> bucketList = new ArrayList<>();
        String dsArray[] = dataSourceTypes.split(",");
        for (JsonElement type : types) {
            JsonObject typeObj = type.getAsJsonObject();
            String fieldName = typeObj.get("key").getAsString();

            // To handle vulnerabilities type
       
            for(String ds : dsArray) {
	            if (fieldName.startsWith(ds+"_")) {
	                fieldName = fieldName.substring(ds.length()+1);
	                break;
	            }
            }

            long count = typeObj.get("doc_count").getAsLong();
            JsonElement uniqueNumberElement = typeObj.get("unique");
            if (null != uniqueNumberElement) {
                count = uniqueNumberElement.getAsJsonObject().get("value").getAsLong();
            }
            Map<String, Object> typeMap = new HashMap<>();
            typeMap.put(AssetConstants.FIELDNAME, fieldName);
            typeMap.put("count", count);
            bucketList.add(typeMap);
        }
        return bucketList;
    }

    private List<Map<String, Object>> getDistFromVulnAggsResult(String responseJson, List<String> returnFields) {

        JsonParser jsonParser = new JsonParser();
        JsonObject resultJson = jsonParser.parse(responseJson).getAsJsonObject();
        JsonArray types = resultJson.get("aggregations").getAsJsonObject().get("qids").getAsJsonObject().get("buckets")
                .getAsJsonArray();
        List<Map<String, Object>> bucketList = new ArrayList<>();
        for (JsonElement type : types) {
            int count = 0;
            Map<String, Object> map = new HashMap<>();
            JsonObject typeObj = type.getAsJsonObject();
            String key = typeObj.get("key").getAsString();
            StringTokenizer vulnkeyTokenizer = new StringTokenizer(key, "~");
            while (vulnkeyTokenizer.hasMoreTokens()) {
                String token = vulnkeyTokenizer.nextToken();
                map.put(returnFields.get(count), token);
                count++;
            }
            bucketList.add(map);
        }

        return bucketList;
    }

    private String createPayLoad(String aggString, String searchText, String searchCategory, String resourceType,
            boolean includeAllAssets) {
        StringBuilder payLoad = new StringBuilder();
        String matchString = "";
        if (AssetConstants.ASSETS.equals(searchCategory) && Objects.isNull(resourceType)) {
            matchString = "{\"match\":{\"_entity\":\"true\"}}";
        }
        if (AssetConstants.ASSETS.equals(searchCategory) && !Objects.isNull(resourceType)) {
            matchString = "{\"match\":{\"_entity\":\"true\"}},{\"term\":{\"docType.keyword\":{\"value\":\"" +
                    resourceType + "\"}}}";
        }
        if (AssetConstants.POLICY_VIOLATIONS.equals(searchCategory) && !Objects.isNull(resourceType)) {
            matchString = "{\"match\":{\"type.keyword\":\"issue\"}},{\"term\":{\"docType.keyword\":{\"value\":\"issue_"
                    + resourceType + "\"}}},{\"terms\":{\"issueStatus.keyword\":[ \"open\",\"exempted\"]}}";
        }
        if (AssetConstants.POLICY_VIOLATIONS.equals(searchCategory) && Objects.isNull(resourceType)) {
            matchString = "{\"match\":{\"type.keyword\":\"issue\"}}," +
                    "{\"terms\":{\"issueStatus.keyword\":[ \"open\",\"exempted\"]}}";
        }
        /*if (AssetConstants.VULNERABILITIES.equals(searchCategory)) {
            matchString = "{\"terms\":{\"severitylevel.keyword\":[3,4,5]}}";
            if (resourceType != null) {
                matchString = matchString + ",{\"match\":{\"_index\":\"aws_" + resourceType + "\"}}";
            }
        }*/

        payLoad.append("{\"size\":1,\"query\":{\"bool\":{\"must\":[");
        payLoad.append(matchString);
        if (AssetConstants.ASSETS.equals(searchCategory) && !includeAllAssets) {
            payLoad.append(",{\"match\":{\"latest\":\"true\"}}");
        }
        if (AssetConstants.VULNERABILITIES.equals(searchCategory)) {
            payLoad.append(",{\"match\":{\"latest\":\"true\"}}");
        }
        payLoad.append(", %s");
        payLoad.append("]}}");
        payLoad.append(",\"aggs\":{");
        payLoad.append(aggString);
        payLoad.append("}}");
        return payLoad.toString();
    }

    @Override
    public Map<String, String> getFieldMappingsForSearch(String incomingResourceType, boolean flipOrder,
            String searchCategory) {

        Map<String, String> mappingList = new LinkedHashMap<>();

        String commaSepString = categoryToRefineByMap.get(searchCategory).get(incomingResourceType);

        String commaSepStringForAll = categoryToRefineByMap.get(searchCategory).get("All");

        String jointStr = commaSepStringForAll + (commaSepString != null ? (",".concat(commaSepString)) : "");

        String displayName = null;
        String esFieldName = null;

        StringTokenizer commaTokens = new StringTokenizer(jointStr, ",");
        while (commaTokens.hasMoreTokens()) {
            String pipeSeparatedStr = commaTokens.nextToken();
            if (pipeSeparatedStr.contains("|")) {
                int posOfPipe = pipeSeparatedStr.indexOf('|');
                esFieldName = pipeSeparatedStr.substring(0, posOfPipe);
                displayName = pipeSeparatedStr.substring(posOfPipe, pipeSeparatedStr.length());
            } else {
                // Assume display name is same as field name
                esFieldName = pipeSeparatedStr;
                displayName = pipeSeparatedStr;
            }
            if (flipOrder) {
                mappingList.put(esFieldName, displayName);
            } else {
                mappingList.put(displayName, esFieldName);
            }
        }
        return mappingList;
    }

    @Override
    public List<String> getReturnFieldsForSearch(String incomingResourceType, String searchCategory) {

        List<String> returnFieldList = new ArrayList<>();

        String commaSepString = categoryToReturnFieldsMap.get(searchCategory).get(incomingResourceType);
        String commaSepStringForAll = categoryToReturnFieldsMap.get(searchCategory).get("All");

        String jointStr = commaSepStringForAll + (commaSepString != null ? (",".concat(commaSepString)) : "");
        StringTokenizer commaTokens = new StringTokenizer(jointStr, ",");

        while (commaTokens.hasMoreTokens()) {
            returnFieldList.add(commaTokens.nextToken());
        }

        return returnFieldList;
    }

    private List<Map<String, Object>> pruneResults(List<Map<String, Object>> results, String targetType,
            String searchCategory) {
        List<String> returnFields = getReturnFieldsForSearch(targetType, searchCategory);
        List<Map<String, Object>> resultsAfterPruning = new ArrayList<>();
        results.forEach(result -> {
            Map<String, Object> outgoingMap = new LinkedHashMap<>();
            result.forEach((key, value) -> {
                if (returnFields.contains(key) || key.startsWith("tags.")) {
                    // The first item in the return fields from RDS is to be
                    // considered as the id
                    // field
                    if (key.equals(returnFields.get(0))) {
                        key = Constants._ID;
                    }
                    outgoingMap.put(key, value);
                }
            });

            outgoingMap.put("searchCategory", searchCategory);
            boolean removeDups = false;
            if (AssetConstants.VULNERABILITIES.equals(searchCategory)) {
                removeDups = true;
            }
            if (!removeDups || (removeDups
                    && !doesResultAlreadyContainId(resultsAfterPruning, outgoingMap.get(Constants._ID)))) {
                resultsAfterPruning.add(outgoingMap);
            }
        });
        return resultsAfterPruning;
    }

    private boolean doesResultAlreadyContainId(List<Map<String, Object>> resultsAfterPruning,
            Object idValueToBeChecked) {
        List<Object> matchedObjects = new ArrayList<>();
        resultsAfterPruning.forEach(result -> {
            result.forEach((key, value) -> {
                if (key.equals(Constants._ID)) {
                    double lhsLongValue = Double.parseDouble(value.toString());
                    double rhsLongValue = Double.parseDouble(idValueToBeChecked.toString());
                    if (lhsLongValue == rhsLongValue) {

                        LOGGER.debug("Duplicate vuln id found(Won't be adding this..): {}", idValueToBeChecked);
                        matchedObjects.add(idValueToBeChecked);
                    }

                }
            });
        });
        return !matchedObjects.isEmpty();
    }

    private RestClient getRestClient() {
        if (restClient == null) {

            RestClientBuilder builder = RestClient.builder(new HttpHost(esHost, esPort));
            builder.setRequestConfigCallback(new RestClientBuilder.RequestConfigCallback() {
                @Override
                public RequestConfig.Builder customizeRequestConfig(RequestConfig.Builder requestConfigBuilder) {
                    return requestConfigBuilder.setConnectionRequestTimeout(0);
                }
            });
            restClient = builder.build();
        }
        return restClient;

    }

    private String invokeESCall(String method, String endpoint, String payLoad) {
        HttpEntity entity = null;
        try {
            if (payLoad != null) {
                entity = new NStringEntity(payLoad, ContentType.APPLICATION_JSON);
            }
            return EntityUtils.toString(getRestClient()
                    .performRequest(method, endpoint, Collections.<String, String>emptyMap(), entity).getEntity());
        } catch (IOException e) {
            LOGGER.error("Error in invokeESCall ", e);
        }
        return null;
    }



}
