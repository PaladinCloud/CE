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
/**
  Copyright (C) 2017 T Mobile Inc - All Rights Reserve
  Purpose:
  Author :kkumar
  Modified Date: Oct 17, 2017

 **/
package com.tmobile.pacman.api.commons.repo;


import java.util.*;


import javax.annotation.PostConstruct;

import com.tmobile.pacman.api.commons.exception.DataException;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Repository;

import com.google.common.base.Strings;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.tmobile.pacman.api.commons.Constants;
import com.tmobile.pacman.api.commons.utils.CommonUtils;
import com.tmobile.pacman.api.commons.utils.PacHttpUtils;
import com.tmobile.pacman.api.commons.utils.ThreadLocalUtil;

@Repository
public class ElasticSearchRepository implements Constants {

	/**
	 *
	 */
	private static final String AVG = "avg";
	/**
	 *
	 */
	private static final String SUM = "sum";
	/**
	 *
	 */
	private static final String SUM_NETWORK_OUT = "Sum-NetworkOut";
	/**
	 *
	 */
	private static final String SUM_NETWORK_IN = "Sum-NetworkIn";
	/**
	 *
	 */
	private static final String AVG_CPU_UTILIZATION = "Avg-CPU-Utilization";
	/**
	 *
	 */
	private static final String NO_RECORDS_FOUND = "No records found!";
	/**
	 *
	 */
	private static final String HITS = "hits";

	/**
	 *
	 */
	private static final String EC2_UTILIZATION = "ec2_utilization";
	/**
	 *
	 */
	private static final String AGGS = "aggs";
	/**
	 *
	 */
	private static final String FORWARD_SLASH = "/";
	/**
	 *
	 */
	private static final String _COUNT = "_count";
	/**
	 *
	 */
	private static final String QUERY_STRING = "query_string";
	/**
	 *
	 */
	private static final String MATCH_PHRASE_PREFIX = "match_phrase_prefix";
	/**
	 *
	 */
	private static final String _ALL = "_all";

	public static final String UNDERSCORE_ENTITY = "_entity";

	public static final String ALL = "all";

	public static final String FIELD_NAME = "fieldName";
	/**
	 *
	 */
	private static final String MATCH = "match";
	/**
	 *
	 */
	private static final String MINIMUM_SHOULD_MATCH = "minimum_should_match";
	/**
	 *
	 */
	private static final String BOOL = "bool";
	/**
	 *
	 */
	private static final String SHOULD = "should";
	/**
	 *
	 */
	private static final String MUST_NOT = "must_not";
	/**
	 *
	 */
	private static final String MUST = "must";
	/**
	 *
	 */
	private static final String RANGE = "range";
	/**
	 *
	 */
	private static final String TERM = "term";
	/**
	 *
	 */
	private static final String TERMS = "terms";
	/**
	 *
	 */
	private static final String ERROR_RETRIEVING_INVENTORY_FROM_ES = "error retrieving inventory from ES";
	/**
	 *
	 */
	private static final String HAS_PARENT = "has_parent";
	/**
	 *
	 */
	private static final String HAS_CHILD = "has_child";
	/**
	 *
	 */
	private static final String _SOURCE = "_source";
	/**
	 *
	 */
	private static final String SORT = "sort";
	/**
	 *
	 */
	private static final String QUERY = "query";
	/**
	 *
	 */
	private static final String SIZE = "size";
	/**
	 *
	 */
	private static final String _SEARCH = "_search";
	@Value("${elastic-search.host}")
	private String esHost;
	@Value("${elastic-search.port}")
	private int esPort;

	@Value("${tagging.mandatoryTags}")
	private String mandatoryTags;

	final static String protocol = "http";

	private String esUrl;

	private static final Log LOGGER = LogFactory.getLog(ElasticSearchRepository.class);

	@PostConstruct
	void init() {
		esUrl = protocol + "://" + esHost + ":" + esPort;
	}

	/**
	 *
	 * @param dataSource
	 * @param targetType
	 * @param mustFilter
	 * @param mustNotFilter
	 * @param shouldFilter
	 * @param fields
	 * @return
	 * @throws Exception
	 * @deprecated
	 */
	public List<Map<String, Object>> getDataFromES(String dataSource, String targetType, Map<String, Object> mustFilter,
			final Map<String, Object> mustNotFilter, final HashMultimap<String, Object> shouldFilter,
			List<String> fields, Map<String, Object> mustTermsFilter) throws Exception {

		Long totalDocs = getTotalDocumentCountForIndexAndType(dataSource, targetType, mustFilter, mustNotFilter,
				shouldFilter, null, mustTermsFilter);
		// if filter is not null apply filter, this can be a multi value filter
		// also if from and size are -1 -1 send all the data back and do not
		// paginate
		StringBuilder urlToQueryBuffer = new StringBuilder(esUrl).append(FORWARD_SLASH).append(dataSource);
		if (!Strings.isNullOrEmpty(targetType)) {
			mustFilter.put("docType.keyword",targetType);
		}
		urlToQueryBuffer.append(FORWARD_SLASH).append(_SEARCH);
		// paginate for breaking the response into smaller chunks
		Map<String, Object> requestBody = new HashMap<String, Object>();
		requestBody.put(SIZE, ES_PAGE_SIZE);
		if (totalDocs < ES_PAGE_SIZE) {
			requestBody.put(SIZE, totalDocs);
		}
		requestBody.put(QUERY, buildQuery(mustFilter, mustNotFilter, shouldFilter, null, mustTermsFilter,null));
		requestBody.put(_SOURCE, fields);
		Gson serializer = new GsonBuilder().disableHtmlEscaping().create();
		String request = serializer.toJson(requestBody);

		LOGGER.info("request {} "+ request);
		return prepareResultsUsingPagination(dataSource,request,0,totalDocs);
	}

	public Map<String, Long> getAssetCountByAssetGroup(String aseetGroupName, String type, String application) {

		Map<String, Object> filter = new HashMap<>();
		filter.put(Constants.LATEST, Constants.TRUE);
		filter.put(UNDERSCORE_ENTITY, Constants.TRUE);
		if (application != null) {
			filter.put(Constants.TAGS_APPS, application);
		}

		Map<String, Long> countMap = new HashMap<>();
		try {
			if (ALL.equals(type)) {
				try {
					countMap = getTotalDistributionForIndexAndType(aseetGroupName, null, filter, null,
							null, DOC_TYPE_KEYWORD, Constants.THOUSAND, null);
				} catch (Exception e) {
					LOGGER.error("Exception in getAssetCountByAssetGroup :", e);
				}
			} else {
				long count = getTotalDocumentCountForIndexAndType(aseetGroupName, type, filter, null,
						null, null, null);
				countMap.put(type, count);
			}
		} catch (Exception e) {
			LOGGER.error("Exception in getAssetCountByAssetGroup :", e);
		}

		return countMap;
	}

	/**
	 *
	 * @param dataSource
	 * @param targetType
	 * @param mustFilter
	 * @param mustNotFilter
	 * @param shouldFilter
	 * @param fields
	 * @return
	 * @throws Exception
	 */
	public List<Map<String, Object>> getSortedDataFromES(String dataSource, String targetType,
														 Map<String, Object> mustFilter, final Map<String, Object> mustNotFilter,
														 final HashMultimap<String, Object> shouldFilter, List<String> fields, Map<String, Object> mustTermsFilter,
														 List<Map<String, Object>> sortFieldMapList) throws Exception {

		Long totalDocs = getTotalDocumentCountForIndexAndType(dataSource, targetType, mustFilter, mustNotFilter,
				shouldFilter, null, mustTermsFilter);
		// if filter is not null apply filter, this can be a multi value filter
		// also if from and size are -1 -1 send all the data back and do not
		// paginate
		StringBuilder urlToQueryBuffer = new StringBuilder(esUrl).append(FORWARD_SLASH).append(dataSource);
		urlToQueryBuffer.append(FORWARD_SLASH).append(_SEARCH);

		// paginate for breaking the response into smaller chunks
		Map<String, Object> requestBody = new HashMap<String, Object>();
		requestBody.put(SIZE, ES_PAGE_SIZE);
		if (totalDocs > 0 && totalDocs < ES_PAGE_SIZE) {
			requestBody.put(SIZE, totalDocs);
		}
		requestBody.put(QUERY, buildQuery(mustFilter, mustNotFilter, shouldFilter, null, mustTermsFilter,null));

		if (null != sortFieldMapList && !sortFieldMapList.isEmpty()) {
			requestBody.put(SORT, sortFieldMapList);
		}
		requestBody.put(_SOURCE, fields);
		Gson serializer = new GsonBuilder().disableHtmlEscaping().create();
		String request = serializer.toJson(requestBody);

		return prepareResultsUsingPagination(dataSource,request,0,totalDocs);
	}

	public List<Map<String, Object>> prepareResultsUsingPagination(String dataSource, String request,long from, long size) throws Exception {
		Gson gson = new GsonBuilder().create();
		String urlToQuery = esUrl + "/" + dataSource +"/" + _SEARCH;
		Map<String,Object> requestBodyMap = gson.fromJson(request,Map.class);
		List<Map<String, Object>> results = new ArrayList<>();
		// paginate for breaking the response into smaller chunks
		Map<String, Object> sortKey = new HashMap<>();
		Map<String, Object> sortInnerMap = new HashMap<>();
		sortInnerMap.put(UNMAPPED_TYPE,LONG);
		sortInnerMap.put(ORDER,DESC);
		sortKey.put(DOCID+KEYWORD, sortInnerMap);

		if(requestBodyMap.containsKey("sort")){
			Object sortObj = requestBodyMap.get("sort");
			if(sortObj instanceof List){
				List<Map<String, Object>> sortList = (List<Map<String, Object>>)requestBodyMap.get("sort");
				if(sortList.stream().noneMatch(p -> p.containsKey(DOCID+KEYWORD))){
					sortList.add(sortKey);
				}
			}
			else{
				List<Map<String, Object>> sortList = new ArrayList<>();
				Map<String,Object> sortMap = (Map<String,Object>)requestBodyMap.get("sort");
				sortList.add(sortMap);
				sortList.add(sortKey);
				requestBodyMap.put("sort",sortList);
			}
		}
		else{
			List<Map<String, Object>> sortList = new ArrayList<>();
			sortList.add(sortKey);
			requestBodyMap.put("sort",sortList);
		}

		for (int index = 0; index <= ((from+size) / Constants.ES_PAGE_SIZE); index++) {
			String responseDetails;
			try {
				responseDetails = PacHttpUtils.doHttpPost(urlToQuery, gson.toJson(requestBodyMap));
				LOGGER.info(" responseDetails {} "+responseDetails);
				Map<String, Object> returnObj = processResponse(responseDetails, results);
				requestBodyMap.put("search_after", returnObj.get("sortArray"));
			} catch (Exception e) {
				LOGGER.error(ERROR_RETRIEVING_INVENTORY_FROM_ES, e);
				throw e;
			}
		}

		int to = (int) (from + size);
		if (to > results.size()) {
			to = results.size();
		}
		results = results.subList((int) from, to);
		return results;
	}

	private static Map<String, Object> processResponse(String responseDetails,
													   List<Map<String, Object>> results) {
		Gson serializer = new GsonBuilder().create();
		Map<String, Object> response = (Map<String, Object>) serializer.fromJson(responseDetails, Object.class);
		Map<String, Object> lastHitDetail = new HashMap<>();
		LOGGER.info(" response {} "+response );
		if (response.containsKey("hits")) {
			Map<String, Object> hits = (Map<String, Object>) response.get("hits");
			if (hits.containsKey("hits")) {
				List<Map<String, Object>> hitDetails = (List<Map<String, Object>>) hits.get("hits");
				for (Map<String, Object> hitDetail : hitDetails) {
					Map<String, Object> sources = (Map<String, Object>) hitDetail.get(_SOURCE);
					sources.put(ES_DOC_ID_KEY, hitDetail.get(ES_DOC_ID_KEY));
					sources.put(ES_DOC_ROUTING_KEY,
							hitDetail.get(ES_DOC_ROUTING_KEY));
					results.add(CommonUtils.flatNestedMap(null, sources));
					lastHitDetail = hitDetail;
				}
			}
		}
		List<String> sortArray = (List<String>) lastHitDetail.get("sort");
		Map<String, Object> returnObject = new HashMap<>();
		returnObject.put("sortArray", sortArray);
		return returnObject;
	}

	/**
	 *
	 * @param distributionName
	 * @param size
	 * @return
	 */
	private Map<String, Object> buildAggs(String distributionName, int size) {
		Map<String, Object> name = new HashMap<String, Object>();
		if (!Strings.isNullOrEmpty(distributionName)) {
			Map<String, Object> terms = new HashMap<String, Object>();
			Map<String, Object> termDetails = new HashMap<String, Object>();
			termDetails.put("field", distributionName);
			if (size > 0) {
				termDetails.put(SIZE, size);
			}
			terms.put(TERMS, termDetails);
			name.put("name", terms);
		}
		return name;
	}
	
	/**
	 * 
	 * @param distributionName
	 * @param size
	 * @param aggsName
	 * @param nestedAggs
	 * @return
	 */
	public Map<String, Object> buildAggs(String distributionName, int size, String aggsName, Map<String, Object> nestedAggs) {
		Map<String, Object> name = new HashMap<String, Object>();
		if (!Strings.isNullOrEmpty(distributionName)) {
			Map<String, Object> terms = new HashMap<String, Object>();
			Map<String, Object> termDetails = new HashMap<String, Object>();
			termDetails.put("field", distributionName);
			if (size > 0) {
				termDetails.put(SIZE, size);
			}
			terms.put(TERMS, termDetails);
			if (nestedAggs != null && !nestedAggs.isEmpty()) {
				terms.put(AGGS, nestedAggs);
			}
			name.put(( Strings.isNullOrEmpty(aggsName) ? "name" : aggsName ), terms);
		}
		return name;
	}

	/**
	 *
	 * @param mustFilter
	 * @return elastic search query details
	 */
	public Map<String, Object> buildQuery(final Map<String, Object> mustFilter, final Map<String, Object> mustNotFilter,
			final HashMultimap<String, Object> shouldFilter, final String searchText,
			final Map<String, Object> mustTermsFilter,Map<String, List<String>> matchPhrasePrefix) {

		Map<String, Object> queryFilters = Maps.newHashMap();
		Map<String, Object> boolFilters = Maps.newHashMap();

		Map<String, Object> hasChildObject = null;
		Map<String, Object> hasParentObject = null;

		if (isNotNullOrEmpty(mustFilter)) {
			if (mustFilter.containsKey(HAS_CHILD)) {
				hasChildObject = (Map<String, Object>) mustFilter.get(HAS_CHILD);
				mustFilter.remove(HAS_CHILD);
			}
			if (mustFilter.containsKey(HAS_PARENT)) {
				hasParentObject = (Map<String, Object>) mustFilter.get(HAS_PARENT);
				mustFilter.remove(HAS_PARENT);
			}
		}

		if (isNotNullOrEmpty(mustFilter) && (!Strings.isNullOrEmpty(searchText))) {

			List<Map<String, Object>> must = getFilter(mustFilter, mustTermsFilter,matchPhrasePrefix);
			Map<String, Object> match = Maps.newHashMap();
			Map<String, Object> all = Maps.newHashMap();
			// If the string is enclosed in quotes, do a match phrase instead of
			// match
			if (searchText.startsWith("\"") && searchText.endsWith("\"")) {
				all.put(QUERY, searchText.substring(1, searchText.length() - 1));
				match.put(QUERY_STRING, all);
			} else {
				all.put(QUERY, searchText);
				match.put(QUERY_STRING, all);
			}
			must.add(match);
			boolFilters.put(MUST, must);
		} else if (isNotNullOrEmpty(mustFilter)) {
			boolFilters.put(MUST, getFilter(mustFilter, mustTermsFilter,matchPhrasePrefix));
		}

		if (isNotNullOrEmpty(mustFilter)) {

			Map<String, Object> hasChildMap = Maps.newHashMap();
			Map<String, Object> hasParentMap = Maps.newHashMap();

			List<Map<String, Object>> must = (List<Map<String, Object>>) boolFilters.get(MUST);

			if (null != hasChildObject) {
				hasChildMap.put(HAS_CHILD, hasChildObject);
				must.add(hasChildMap);
			}
			if (null != hasParentObject) {
				hasParentMap.put(HAS_PARENT, hasParentObject);
				must.add(hasParentMap);
			}

		}
		if (isNotNullOrEmpty(mustNotFilter)) {

			boolFilters.put(MUST_NOT, getFilter(mustNotFilter, null,null));
		}
		if (isNotNullOrEmpty(shouldFilter)) {

			boolFilters.put(SHOULD, getFilter(shouldFilter));
			boolFilters.put(MINIMUM_SHOULD_MATCH, "1");
		}
		queryFilters.put(BOOL, boolFilters);
		return queryFilters;
	}

	/**
	 *
	 * @param shouldFilter
	 * @return
	 */
	private boolean isNotNullOrEmpty(HashMultimap<String, Object> shouldFilter) {

		return shouldFilter != null && shouldFilter.size() > 0;
	}

	/**
	 *
	 * @param collection
	 * @return
	 */
	private boolean isNotNullOrEmpty(Map<String, Object> collection) {

		return collection != null && collection.size() > 0;
	}

	/**
	 *
	 * @param mustfilter
	 * @return
	 */
	private List<Map<String, Object>> getFilter(final Map<String, Object> mustfilter,
												final Map<String, Object> mustTerms,
												Map<String, List<String>> matchPhrasePrefix) {
		List<Map<String, Object>> finalFilter = Lists.newArrayList();
		for (Map.Entry<String, Object> entry : mustfilter.entrySet()) {
			Map<String, Object> term = Maps.newHashMap();
			Map<String, Object> termDetails = Maps.newHashMap();

			if(entry.getKey().equalsIgnoreCase("tagged")){
				List<Object>fieldValue=(List<Object>)entry.getValue();
				for(Object value:fieldValue){
					Map<String, Object> existsDetails = Maps.newHashMap();
					Map<String, Object> exists = Maps.newHashMap();
					existsDetails.put("field",value);
					exists.put("exists",existsDetails);
					finalFilter.add(exists);
				}
			}
			else if(entry.getKey().equalsIgnoreCase("untagged")){
				List<Object>fieldValue=(List<Object>)entry.getValue();
				finalFilter.add(getFilterForUntagged(fieldValue));
			}
			else if(entry.getKey().equalsIgnoreCase("exempted")){
				Map<String, Object> exemptDetails = Maps.newHashMap();
				Map<String, Object> exempt = Maps.newHashMap();
				exemptDetails.put("_resourceid.keyword",entry.getValue());
				exempt.put(TERMS,exemptDetails);
				finalFilter.add(exempt);
			}
			else{
				termDetails.put(entry.getKey(), entry.getValue());
			}

			if (RANGE.equals(entry.getKey())) {
				term.put(RANGE, entry.getValue());
			}
			else if(Arrays.asList("match","bool").contains(entry.getKey())){
				term.put(entry.getKey(), entry.getValue());
			}
			else if(entry.getKey().equalsIgnoreCase("tagged") || entry.getKey().equalsIgnoreCase("exempted") || entry.getKey().equalsIgnoreCase("untagged") ){
				continue;
			}
			else {
				term.put(TERM, termDetails);
			}
			finalFilter.add(term);
		}
		if (mustTerms != null && !mustTerms.isEmpty()) {
			if (mustTerms.size() == 1) {
				Map<String, Object> term = Maps.newHashMap();
				term.put(TERMS, mustTerms);
				finalFilter.add(term);
			} else {
				mustTerms.forEach((key, value) -> {
					Map<String, Object> innerMustTermsMap = new HashMap<>();
					innerMustTermsMap.put(key, value);
					Map<String, Object> term = Maps.newHashMap();
					term.put(TERMS, innerMustTermsMap);
					finalFilter.add(term);
				});
			}
		}
		if (matchPhrasePrefix != null && !matchPhrasePrefix.isEmpty()) {

			for (Map.Entry<String, List<String>> entry : matchPhrasePrefix
					.entrySet()) {
				List<Object> infoList = new ArrayList<Object>();
				infoList.add(entry.getValue());

				for (Object val : entry.getValue()) {
					Map<String, Object> map = new HashMap<String, Object>();
					Map<String, Object> matchPhrasePrefixMap = Maps
							.newHashMap();
					map.put(entry.getKey(), val);
					matchPhrasePrefixMap.put(MATCH_PHRASE_PREFIX, map);
					finalFilter.add(matchPhrasePrefixMap);
				}

			}
		}

		return finalFilter;
	}

	/**
	 *
	 * @param filter
	 * @return
	 */
	private List<Map<String, Object>> getFilter(final HashMultimap<String, Object> filter) {
		List<Map<String, Object>> finalFilter = Lists.newArrayList();
		for (Map.Entry<String, Object> entry : filter.entries()) {
			Map<String, Object> term = Maps.newHashMap();
			Map<String, Object> termDetails = Maps.newHashMap();
			termDetails.put(entry.getKey(), entry.getValue());
			if(!("bool".equalsIgnoreCase(entry.getKey()) || RANGE.equalsIgnoreCase(entry.getKey()))){
				term.put(TERM, termDetails);
				finalFilter.add(term);
			}
			else{
				finalFilter.add(termDetails);
			}
		}
		return finalFilter;
	}

	/**
	 *
	 * @param scrollId
	 * @param esPageScrollTtl
	 * @return
	 */
	public String buildScrollRequest(String scrollId, String esPageScrollTtl) {
		Map<String, Object> requestBody = new HashMap<String, Object>();
		requestBody.put("scroll", ES_PAGE_SCROLL_TTL);
		requestBody.put("scroll_id", scrollId);
		Gson serializer = new GsonBuilder().create();
		return serializer.toJson(requestBody);
	}

	/**
	 *
	 * @param responseDetails
	 * @param results
	 * @return
	 */
	public String processResponseAndSendTheScrollBack(String responseDetails, List<Map<String, Object>> results) {
		Gson serializer = new GsonBuilder().create();
		Map<String, Object> response = (Map<String, Object>) serializer.fromJson(responseDetails, Object.class);
		if (response.containsKey(HITS)) {
			Map<String, Object> hits = (Map<String, Object>) response.get(HITS);
			if (hits.containsKey(HITS)) {
				List<Map<String, Object>> hitDetails = (List<Map<String, Object>>) hits.get(HITS);
				for (Map<String, Object> hitDetail : hitDetails) {
					Map<String, Object> sources = (Map<String, Object>) hitDetail.get(_SOURCE);
					sources.put(ES_DOC_ID_KEY, hitDetail.get(ES_DOC_ID_KEY));
					sources.put(ES_DOC_PARENT_KEY, hitDetail.get(ES_DOC_PARENT_KEY));
					sources.put(ES_DOC_ROUTING_KEY, hitDetail.get(ES_DOC_ROUTING_KEY));
					results.add(CommonUtils.flatNestedMap(null, sources));
				}
			}
		}
		return (String) response.get("_scroll_id");
	}

	/**
	 *
	 * @param mustFilter
	 *            search url
	 * @param index
	 *            name
	 * @param type
	 *            name
	 * @param shouldFilter
	 * @param mustNotFilter
	 * @param mustFilter
	 * @return elastic search count
	 */
	@SuppressWarnings("unchecked")
	public long getTotalDocumentCountForIndexAndType(String index, String type, Map<String, Object> mustFilter,
													 Map<String, Object> mustNotFilter, HashMultimap<String, Object> shouldFilter, String searchText,
													 Map<String, Object> mustTermsFilter) throws Exception {

		String urlToQuery = buildCountURL(esUrl, index, "");

		Map<String, Object> requestBody = new HashMap<String, Object>();
		Map<String, Object> matchFilters = Maps.newHashMap();
		if (mustFilter == null && !Strings.isNullOrEmpty(type)) {
				matchFilters.put(MATCH_ALL, new HashMap<String, String>().put(Constants.DOC_TYPE_KEYWORD, type));
			}
		if (mustFilter == null && Strings.isNullOrEmpty(type)) {
				matchFilters.put(MATCH_ALL, new HashMap<String, String>());
		}
		if(mustFilter!=null &&!Strings.isNullOrEmpty(type)){
			mustFilter.put(Constants.DOC_TYPE_KEYWORD,type);
			matchFilters.putAll(mustFilter);
		}
		if(mustFilter!=null && Strings.isNullOrEmpty(type)){
			matchFilters.putAll(mustFilter);
		}
		if (null != mustFilter) {
			requestBody.put(QUERY, buildQuery(matchFilters, mustNotFilter, shouldFilter, searchText, mustTermsFilter,null));
		} else {
			requestBody.put(QUERY, matchFilters);
		}
		String responseDetails = null;
		Gson gson = new GsonBuilder().create();
		try {

			String requestJson = gson.toJson(requestBody, Object.class);
			responseDetails = PacHttpUtils.doHttpPost(urlToQuery, requestJson);
			Map<String, Object> response = (Map<String, Object>) gson.fromJson(responseDetails, Object.class);
			return (long) (Double.parseDouble(response.get(COUNT).toString()));
		} catch (Exception e) {
			LOGGER.error(ERROR_RETRIEVING_INVENTORY_FROM_ES, e);
			throw e;
		}
	}

	/**
	 *
	 * @param url
	 * @param index
	 * @param type
	 * @return
	 */
	private String buildCountURL(String url, String index, String type) {

		StringBuilder urlToQuery = new StringBuilder(url).append(FORWARD_SLASH).append(index);
		if (!Strings.isNullOrEmpty(type)) {
			urlToQuery.append(FORWARD_SLASH).append(type);
		}
		urlToQuery.append(FORWARD_SLASH).append(_COUNT);
		return urlToQuery.toString();
	}


	/**
	 *
	 * @param index
	 * @param type
	 * @param mustFilter
	 * @param mustNotFilter
	 * @param shouldFilter
	 * @param aggsFilter
	 * @param size
	 * @param mustTermsFilter
	 * @return
	 * @throws Exception
	 */
	public Map<String, Long> getTotalDistributionForIndexAndType(String index, String type,
			Map<String, Object> mustFilter, Map<String, Object> mustNotFilter,
			HashMultimap<String, Object> shouldFilter, String aggsFilter, int size, Map<String, Object> mustTermsFilter)
			throws Exception {

		String urlToQuery = buildAggsURL(esUrl, index, "");
		Map<String, Object> requestBody = new HashMap<String, Object>();
		Map<String, Object> matchFilters = Maps.newHashMap();
		Map<String, Long> distribution = new HashMap<String, Long>();
		if (!Strings.isNullOrEmpty(type)) {
			mustFilter.put(DOC_TYPE_KEYWORD, type);
		}
		if (mustFilter == null) {
			matchFilters.put(MATCH_ALL, new HashMap<String, String>());
		} else {
			matchFilters.putAll(mustFilter);
		}
		if (null != mustFilter) {
			requestBody.put(QUERY, buildQuery(matchFilters, mustNotFilter, shouldFilter, null, mustTermsFilter,null));
			requestBody.put(AGGS, buildAggs(aggsFilter, size));

			if (!Strings.isNullOrEmpty(aggsFilter)) {
				requestBody.put(SIZE, "0");
			}

		} else {
			requestBody.put(QUERY, matchFilters);
		}
		String responseDetails = null;
		Gson gson = new GsonBuilder().create();

		try {
			String requestJson = gson.toJson(requestBody, Object.class);
			responseDetails = PacHttpUtils.doHttpPost(urlToQuery, requestJson);
			Map<String, Object> response = (Map<String, Object>) gson.fromJson(responseDetails, Map.class);
			Map<String, Object> aggregations = (Map<String, Object>) response.get(AGGREGATIONS);
			Map<String, Object> name = (Map<String, Object>) aggregations.get(NAME);
			List<Map<String, Object>> buckets = (List<Map<String, Object>>) name.get(BUCKETS);

			for (int i = 0; i < buckets.size(); i++) {
				Map<String, Object> bucket = buckets.get(i);
				distribution.put(bucket.get("key").toString(), ((Double) bucket.get("doc_count")).longValue());
			}

		} catch (Exception e) {
			LOGGER.error(ERROR_RETRIEVING_INVENTORY_FROM_ES, e);
			throw e;
		}
		return distribution;
	}

	/**
	 * 
	 * @param url
	 * @param index
	 * @param type
	 * @return
	 */
	public String buildAggsURL(String url, String index, String type) {

		StringBuilder urlToQuery = new StringBuilder(url).append(FORWARD_SLASH).append(index);
		if (!Strings.isNullOrEmpty(type)) {
			urlToQuery.append(FORWARD_SLASH).append(type);
		}
		urlToQuery.append(FORWARD_SLASH).append("_search/?size=0");
		return urlToQuery.toString();
	}

	/**
	 *
	 * @param asseGroup
	 * @return
	 * @throws Exception
	 */
	public List<Map<String, Object>> getUtilizationByAssetGroup(String asseGroup) throws Exception {
		String urlToQuery = buildAggsURL(esUrl, asseGroup, EC2_UTILIZATION);
		Map<String, Object> requestBody = new HashMap<String, Object>();
		Map<String, Object> cpuUtilizationDetails = new HashMap<String, Object>();
		Map<String, Object> cpuUtilization = new HashMap<String, Object>();
		Map<String, Object> range = new HashMap<String, Object>();
		Map<String, Object> must = new HashMap<String, Object>();
		Map<String, Object> bool = new HashMap<String, Object>();
		Map<String, Object> aggs = new HashMap<String, Object>();
		Map<String, Object> histogram = new HashMap<String, Object>();
		Map<String, Object> histogramDetails = new HashMap<String, Object>();
		Map<String, Object> avgValuesperDay = new HashMap<String, Object>();
		List<Map<String, Object>> utilizationList = new ArrayList<Map<String, Object>>();
		Map<String, Object> utilization = null;
		Map<String, Object> field = null;
		Map<String, Object> sum = null;
		Map<String, Object> avg = null;
		// must Query formate
		cpuUtilizationDetails.put("gte", "now-30d");
		cpuUtilizationDetails.put("lte", "now-1d");
		cpuUtilizationDetails.put("format", "yyyy-MM-dd HH:mm:ss");
		cpuUtilization.put("#Datetime-CPU-Utilization", cpuUtilizationDetails);
		range.put(RANGE, cpuUtilization);
		must.put(MUST, range);
		bool.put(BOOL, must);
		// Aggs Query foramte

		field = new HashMap();
		avg = new HashMap();
		field.put("field", AVG_CPU_UTILIZATION);
		avg.put(AVG, field);
		aggs.put(AVG_CPU_UTILIZATION, avg);

		field = new HashMap();
		sum = new HashMap();
		field.put("field", SUM_NETWORK_IN);
		sum.put(SUM, field);
		aggs.put("Avg-NetworkIn", sum);

		field = new HashMap();
		sum = new HashMap();
		field.put("field", SUM_NETWORK_OUT);
		sum.put(SUM, field);
		aggs.put("Avg-NetworkOut", sum);

		field = new HashMap();
		sum = new HashMap();
		field.put("field", "Sum-DiskReadBytes");
		sum.put(SUM, field);
		aggs.put("Avg-DiskReadinBytes", sum);

		field = new HashMap();
		sum = new HashMap();
		field.put("field", "Sum-DiskWriteBytes");
		sum.put(SUM, field);
		aggs.put("Avg-DiskWriteinBytes", sum);

		field = new HashMap();
		field.put("_key", "desc");
		histogramDetails.put("field", "#Datetime-CPU-Utilization");
		histogramDetails.put("interval", "day");
		histogramDetails.put("format", "yyyy-MM-dd HH:mm:ss");
		histogramDetails.put(ORDER, field);
		histogram.put("date_histogram", histogramDetails);
		histogram.put(AGGS, aggs);
		avgValuesperDay.put("avg-values-per-day", histogram);

		requestBody.put(QUERY, bool);
		requestBody.put(AGGS, avgValuesperDay);
		requestBody.put(SIZE, 0);

		Gson gson = new GsonBuilder().create();

		try {
			String request = gson.toJson(requestBody, Object.class);
			String responseDetails = null;

			responseDetails = PacHttpUtils.doHttpPost(urlToQuery, request);

			JsonParser parser = new JsonParser();
			JsonObject responseDetailsjson = parser.parse(responseDetails).getAsJsonObject();
			responseDetailsjson.get("aggregations");
			Object aggregations = responseDetailsjson.get("aggregations");
			String aggregationsstr = aggregations.toString();
			parser = new JsonParser();
			JsonObject aggregationsjson = (JsonObject) parser.parse(aggregationsstr);
			Object avgvalues = aggregationsjson.get("avg-values-per-day");
			String avgvaluestr = avgvalues.toString();
			parser = new JsonParser();
			JsonObject avgvaluesjson = (JsonObject) parser.parse(avgvaluestr);
			Object bucketObj = avgvaluesjson.get(BUCKETS);
			String bucketstr = bucketObj.toString();
			parser = new JsonParser();
			JsonArray buckets = parser.parse(bucketstr).getAsJsonArray();

			for (JsonElement jsonElement : buckets) {
				utilization = new HashMap<String, Object>();

				JsonObject bucketdetails = jsonElement.getAsJsonObject();
				utilization.put(DATE, bucketdetails.get("key_as_string").getAsString());

				Object cpuUtilizationObj = bucketdetails.get(AVG_CPU_UTILIZATION);
				String cpuUtilizationStr = cpuUtilizationObj.toString();
				parser = new JsonParser();
				JsonObject cpuUtilizationjson = (JsonObject) parser.parse(cpuUtilizationStr);
				if (!cpuUtilizationjson.get("value").isJsonNull()) {
					utilization.put(CPU_UTILIZATION, cpuUtilizationjson.get("value").getAsDouble());
				} else {
					utilization.put(CPU_UTILIZATION, 0);
				}
				Object networkInUtilizationObj = bucketdetails.get("Avg-NetworkIn");
				String networkInUtilizationStr = networkInUtilizationObj.toString();
				parser = new JsonParser();
				JsonObject networkInUtilizationjson = (JsonObject) parser.parse(networkInUtilizationStr);
				if (!networkInUtilizationjson.get("value").isJsonNull()) {
					utilization.put(NETWORK_IN, networkInUtilizationjson.get("value").getAsLong());
				} else {
					utilization.put(NETWORK_IN, 0);
				}
				Object networkOutUtilizationObj = bucketdetails.get("Avg-NetworkOut");
				String networkOutUtilizationStr = networkOutUtilizationObj.toString();
				parser = new JsonParser();
				JsonObject networkOutUtilizationjson = (JsonObject) parser.parse(networkOutUtilizationStr);
				if (!networkOutUtilizationjson.get("value").isJsonNull()) {
					utilization.put(NETWORK_OUT, networkOutUtilizationjson.get("value").getAsLong());
				} else {
					utilization.put(NETWORK_OUT, 0);
				}
				Object diskReadUtilizationObj = bucketdetails.get("Avg-DiskReadinBytes");
				String diskReadUtilizationStr = diskReadUtilizationObj.toString();
				parser = new JsonParser();
				JsonObject diskReadUtilizationjson = (JsonObject) parser.parse(diskReadUtilizationStr);
				if (!diskReadUtilizationjson.get("value").isJsonNull()) {
					utilization.put(DISK_READ_IN_BYTES, diskReadUtilizationjson.get("value").getAsLong());
				} else {
					utilization.put(DISK_READ_IN_BYTES, 0);
				}
				Object diskRWriteUtilizationObj = bucketdetails.get("Avg-DiskWriteinBytes");
				String diskRWriteUtilizationStr = diskRWriteUtilizationObj.toString();
				parser = new JsonParser();
				JsonObject diskRWriteUtilizationjson = (JsonObject) parser.parse(diskRWriteUtilizationStr);
				if (!diskRWriteUtilizationjson.get("value").isJsonNull()) {
					utilization.put(DISK_WRITE_IN_BYTES, diskRWriteUtilizationjson.get("value").getAsLong());
				} else {
					utilization.put(DISK_WRITE_IN_BYTES, 0);
				}
				utilizationList.add(utilization);
			}
			return utilizationList;

		} catch (Exception e) {
			LOGGER.error(ERROR_RETRIEVING_INVENTORY_FROM_ES, e);
			throw e;
		}
	}

	/**
	 *
	 * @param dataSource
	 * @param targetType
	 * @param mustFilter
	 * @param mustNotFilter
	 * @param shouldFilter
	 * @param fields
	 * @param from
	 * @param size
	 * @param searchText
	 * @param mustTermsFilter
	 * @return
	 * @throws Exception
	 * @deprecated
	 */
	public List<Map<String, Object>> getDataFromESBySize(String dataSource, String targetType,
			final Map<String, Object> mustFilter, final Map<String, Object> mustNotFilter,
			final HashMultimap<String, Object> shouldFilter, List<String> fields, int from, int size, String searchText,
			final Map<String, Object> mustTermsFilter) throws Exception {
		if (size <= 0) {
			size = (int) getTotalDocumentCountForIndexAndType(dataSource, targetType, mustFilter, mustNotFilter, null,
					searchText, mustTermsFilter);
		}
		StringBuilder urlToQueryBuffer = new StringBuilder(esUrl).append(FORWARD_SLASH).append(dataSource);
		if (!Strings.isNullOrEmpty(targetType)) {
			urlToQueryBuffer.append(FORWARD_SLASH).append(targetType);
		}
		urlToQueryBuffer.append(FORWARD_SLASH).append(_SEARCH);

		Map<String, Object> requestBody = new HashMap<String, Object>();
		requestBody.put(SIZE, ES_PAGE_SIZE);
		if ((from + size) < ES_PAGE_SIZE) {
			requestBody.put(SIZE, (from + size));
		}
		requestBody.put(QUERY, buildQuery(mustFilter, mustNotFilter, shouldFilter, searchText, mustTermsFilter,null));
		requestBody.put(_SOURCE, fields);
		Gson serializer = new GsonBuilder().create();
		String request = serializer.toJson(requestBody);
		return prepareResultsUsingPagination(dataSource,request,from,size);
	}

	/**
	 *
	 * @param dataSource
	 * @param targetType
	 * @param mustFilter
	 * @param mustNotFilter
	 * @param shouldFilter
	 * @param fields
	 * @param from
	 * @param size
	 * @param searchText
	 * @param mustTermsFilter
	 * @return
	 * @throws Exception
	 */
	public List<Map<String, Object>> getSortedDataFromESBySize(String dataSource, String targetType,
															   final Map<String, Object> mustFilter, final Map<String, Object> mustNotFilter,
															   final HashMultimap<String, Object> shouldFilter, List<String> fields, int from, int size, String searchText,
															   final Map<String, Object> mustTermsFilter, List<Map<String, Object>> sortList) throws Exception {
		int totalDocumentCount = (int) getTotalDocumentCountForIndexAndType(dataSource, targetType, mustFilter, mustNotFilter, null,
				searchText, mustTermsFilter);
		if (size <= 0) {
			size=totalDocumentCount;
		}
		ThreadLocalUtil.count.set(totalDocumentCount);
		StringBuilder urlToQueryBuffer = new StringBuilder(esUrl).append(FORWARD_SLASH).append(dataSource);
		if (!Strings.isNullOrEmpty(targetType)) {
			mustFilter.put(DOC_TYPE_KEYWORD, targetType);
		}
		urlToQueryBuffer.append(FORWARD_SLASH).append(_SEARCH);
		Map<String, Object> requestBody = new HashMap<String, Object>();
		requestBody.put(SIZE, ES_PAGE_SIZE);
		if ((from + size) < ES_PAGE_SIZE) {
			requestBody.put(SIZE, (from + size));
		}
		requestBody.put(QUERY, buildQuery(mustFilter, mustNotFilter, shouldFilter, searchText, mustTermsFilter, null));
		List<Map<String, Object>> list = new ArrayList<Map<String, Object>>();

		String order = "";
		if(null != sortList && !sortList.isEmpty()) {
			for (Map<String, Object> sortFieldMapList : sortList) {
				Map<String, Object> sortScript = new HashMap<>();
				Map<String, Object> paramsList = new HashMap<>();
				Map<String, Object> script = new HashMap<>();
				Map<String, Object> Outerscript = new HashMap<>();
				order = sortFieldMapList.get(ORDER).toString();
				if (sortFieldMapList.get("sortOrder") != null) {
					sortScript.put("type", sortFieldMapList.get("fieldType"));
					String fieldName = (String) sortFieldMapList.get(FIELD_NAME);
					String inlineScript = "doc['%s'].value";
					script.put("params", paramsList);
					inlineScript = "params.sortOrder.indexOf(doc['%s'].value)";
					paramsList.put("sortOrder", sortFieldMapList.get("sortOrder"));

					if (sortFieldMapList.get(FIELD_NAME).equals("_uid")) {
						inlineScript = "doc['_uid'].value.substring(doc['_uid'].value.indexOf('#')+1)";
					}
					String inlineScriptString = String.format(inlineScript, fieldName);
					script.put("inline", inlineScriptString);
					sortScript.put("script", script);
					sortScript.put(ORDER, sortFieldMapList.get(ORDER));
					Outerscript.put("_script", sortScript);
					list.add(Outerscript);
				} else {
					Map<String, Object> sortMap = new HashMap<>();
					String fieldName = (String) sortFieldMapList.get(FIELD_NAME);
					if (fieldName != null) {
						String sortOrder = sortFieldMapList.get(ORDER) != null ? (String) sortFieldMapList.get(ORDER) : "asc";
						if(fieldName.equalsIgnoreCase(AUTOFIX_PLANNED_KEYWORD)){
							Map<String,String> mappedSort=new HashMap<>();
							mappedSort.put(UNMAPPED_TYPE, (String) sortFieldMapList.get("fieldType"));
							mappedSort.put(ORDER, sortOrder);
							Map<String, Object> sortAttribute = new HashMap<>();
							sortAttribute.put(fieldName, mappedSort);
							list.add(sortAttribute);
						}
						else if(fieldName.equalsIgnoreCase("_entitytype.keyword")){
							fieldName="targettypedisplayname.keyword";
							Map<String, Object> scriptMap = new HashMap<>();
							scriptMap.put("source", "doc['"+fieldName+"'].value.toLowerCase()");
							scriptMap.put( "lang", "painless");
							Map<String, Object> scriptOrderMap = new HashMap<>();
							scriptOrderMap.put("script",scriptMap);
							scriptOrderMap.put("order", sortOrder);
							scriptOrderMap.put("type","string");
							Map<String, Object> finalOrderMap = new HashMap<>();
							finalOrderMap.put("_script",scriptOrderMap);
							list.add(finalOrderMap);
						} else if(fieldName.startsWith("tags.") && fieldName.endsWith(".keyword") && Arrays.asList(mandatoryTags.split(",")).contains(((String[])(fieldName.split("\\.")))[1])){
							String returnValueForMissingTag = sortOrder.equalsIgnoreCase("asc") ? "~" : " ";
							Map<String, Object> innerScriptMap1 = new HashMap<>();
							Map<String, Object> innerScriptMap2 = new HashMap<>();
							innerScriptMap2.put("lang", "painless");
							String mTag = ((String[])(fieldName.split("\\.")))[1];
							innerScriptMap2.put("source", "if(!doc.containsKey('tags."+mTag+"') || doc['tags."+mTag+".keyword'].size()==0) return '" + returnValueForMissingTag + "'; else return doc['tags."+mTag+".keyword'].value.toLowerCase()");
							innerScriptMap1.put("script", innerScriptMap2);
							innerScriptMap1.put("type", "string");
							innerScriptMap1.put("order", sortOrder);
							sortMap.put("_script",innerScriptMap1);
							list.add(sortMap);
						}else{
							Map<String, Object> sortOrderMap = new HashMap<>();
							sortOrderMap.put(ORDER, sortOrder);
							sortMap.put(fieldName, sortOrderMap);
							list.add(sortMap);
						}
					}
				}

			}
			Map<String,String> mappedSort=new HashMap<>();
			mappedSort.put(UNMAPPED_TYPE,"long");
			String sortOrder = !order.isEmpty() ? order : "asc";
			mappedSort.put(ORDER,sortOrder);
			Map<String, Object> docIdSort = new HashMap<>();
			Map<String, Object> annotationIdSort = new HashMap<>();
			docIdSort.put(DOCID+KEYWORD, mappedSort);
			annotationIdSort.put(ANNOTATIONID+KEYWORD, mappedSort);
			list.add(docIdSort);
			if(mustFilter != null && mustFilter.containsKey(Constants.TYPE_KEYWORD) && mustFilter.get(Constants.TYPE_KEYWORD).equals(Constants.ISSUE)){
				list.add(annotationIdSort);
			}

			if (!list.isEmpty()) {
				requestBody.put(SORT, list);
			}
		}
		requestBody.put(_SOURCE, fields);
		Gson serializer = new GsonBuilder().create();
		String request = serializer.toJson(requestBody);
		LOGGER.info(request+" request ");
		return prepareResultsUsingPagination(dataSource,request,from,size);
	}

	/**
	 *
	 * @param dataSource
	 * @param targetType
	 * @param id
	 * @param routing
	 * @param parent
	 * @param partialDocument
	 * @return
	 * @throws Exception
	 */
	@SuppressWarnings("unchecked")
	public Boolean updatePartialDataToES(String dataSource, String targetType, String id, String routing, String parent,
			Map<String, Object> partialDocument) throws Exception {
		try {
			StringBuilder esQueryUrl = new StringBuilder(esUrl);
			if (!Strings.isNullOrEmpty(dataSource) && !Strings.isNullOrEmpty(targetType)) {
				esQueryUrl.append(FORWARD_SLASH).append(dataSource).append(FORWARD_SLASH).append(targetType);
				esQueryUrl.append(FORWARD_SLASH).append(id).append(FORWARD_SLASH).append("_update");
				if (!Strings.isNullOrEmpty(routing) && !Strings.isNullOrEmpty(parent)) {
					esQueryUrl.append("?routing=").append(routing).append("&parent=").append(parent);
				}
				partialDocument.put("_docid", id);
				Map<String, Object> documentToUpdate = Maps.newHashMap();
				documentToUpdate.put("doc", partialDocument);
				Gson serializer = new GsonBuilder().create();
				String request = serializer.toJson(documentToUpdate);
				String responseDetails = PacHttpUtils.doHttpPost(esQueryUrl.toString(), request);
				Map<String, Object> response = (Map<String, Object>) serializer.fromJson(responseDetails, Object.class);
				return ("updated".equalsIgnoreCase(response.get("result").toString())
						|| "noop".equalsIgnoreCase(response.get("result").toString()));
			}
		} catch (Exception exception) {
			LOGGER.error("error while updatePartialDataToES",exception);
			return false;
		}
		return false;
	}

	/**
	 *
	 * @param dataSource
	 * @param routing
	 * @param data
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public Boolean saveExceptionDataToES(String dataSource, String routing, Map<String, Object> data) {
		try {
			StringBuilder esQueryUrl = new StringBuilder(esUrl);
			if (!Strings.isNullOrEmpty(dataSource)) {
				esQueryUrl.append(FORWARD_SLASH).append(dataSource).append("/issue_").append(data.get(TARGET_TYPE))
						.append("_exception");
				if (!Strings.isNullOrEmpty(routing)) {
					esQueryUrl.append("?routing=").append(routing);
					Gson serializer = new GsonBuilder().create();
					String request = serializer.toJson(data);
					String responseDetails = PacHttpUtils.doHttpPost(esQueryUrl.toString(), request);
					Map<String, Object> response = (Map<String, Object>) serializer.fromJson(responseDetails,
							Object.class);
					if (response.get("result").toString().equalsIgnoreCase("created")) {
						return true;
					} else {
						return false;
					}
				}
			}
		} catch (Exception exception) {
			return false;
		}
		return false;
	}

	/**
	 *
	 * @param index
	 * @param type
	 * @param mustFilter
	 * @param mustNotFilter
	 * @param shouldFilter
	 * @param aggsFilter
	 * @return
	 */
	public Map<String, Long> getTotalDistributionForIndexAndTypeBySize(String index, String type,
			Map<String, Object> mustFilter, Map<String, Object> mustNotFilter,
			HashMultimap<String, Object> shouldFilter, String aggsFilter, int size, int from, String searchText)
			throws Exception {

		StringBuilder urlToQueryBuffer = new StringBuilder(esUrl).append(FORWARD_SLASH).append(index);
		if (!Strings.isNullOrEmpty(type)) {
			urlToQueryBuffer.append(FORWARD_SLASH).append(type);
		}
		urlToQueryBuffer.append(FORWARD_SLASH).append(_SEARCH);
		if (size > 0 && from >= 0) {
			urlToQueryBuffer.append("?").append("size=").append(size).append("&").append("from=").append(from);
		}

		String urlToQuery = urlToQueryBuffer.toString();
		Map<String, Object> requestBody = new HashMap<String, Object>();
		Map<String, Object> matchFilters = Maps.newHashMap();
		Map<String, Long> distribution = new HashMap<String, Long>();
		if (mustFilter == null) {
			matchFilters.put(MATCH_ALL, new HashMap<String, String>());
		} else {
			matchFilters.putAll(mustFilter);
		}
		if (null != mustFilter) {
			requestBody.put(QUERY, buildQuery(matchFilters, mustNotFilter, shouldFilter, searchText, null,null));
			requestBody.put(AGGS, buildAggs(aggsFilter, size));

		} else {
			requestBody.put(QUERY, matchFilters);
		}
		String responseDetails = null;
		Gson gson = new GsonBuilder().create();

		try {
			String requestJson = gson.toJson(requestBody, Object.class);
			responseDetails = PacHttpUtils.doHttpPost(urlToQuery, requestJson);
			Map<String, Object> response = (Map<String, Object>) gson.fromJson(responseDetails, Map.class);
			Map<String, Object> aggregations = (Map<String, Object>) response.get(AGGREGATIONS);
			Map<String, Object> name = (Map<String, Object>) aggregations.get(NAME);
			List<Map<String, Object>> buckets = (List<Map<String, Object>>) name.get(BUCKETS);
			for (int i = 0; i < buckets.size(); i++) {
				Map<String, Object> bucket = buckets.get(i);
				distribution.put(bucket.get("key").toString(), ((Double) bucket.get("doc_count")).longValue());
			}

		} catch (Exception e) {
			LOGGER.error(ERROR_RETRIEVING_INVENTORY_FROM_ES, e);
			throw e;
		}
		return distribution;
	}

	/**
	 *
	 * @param index
	 * @param type
	 * @param mustFilter
	 * @param mustNotFilter
	 * @param shouldFilter
	 * @param dateFieldName
	 * @param interval
	 * @return
	 * @throws Exception
	 */
	public Map<String, Long> getDateHistogramForIndexAndTypeByInterval(String index, String type,
			Map<String, Object> mustFilter, Map<String, Object> mustNotFilter,
			HashMultimap<String, Object> shouldFilter, String dateFieldName, String interval) throws Exception {

		String urlToQuery = buildAggsURL(esUrl, index, type);
		Map<String, Object> requestBody = new HashMap<String, Object>();
		Map<String, Object> matchFilters = Maps.newHashMap();
		Map<String, Long> distribution = new HashMap<String, Long>();
		if (mustFilter == null) {
			matchFilters.put(MATCH_ALL, new HashMap<String, String>());
		} else {
			matchFilters.putAll(mustFilter);
		}
		if (null != dateFieldName && null != interval) {

			requestBody.put(AGGS, buildHistogramAggs(dateFieldName, interval));

		}
		if (!matchFilters.isEmpty()) {
			requestBody.put(QUERY, buildQuery(matchFilters, mustNotFilter, shouldFilter, null, null,null));
		}
		String responseDetails = null;
		Gson gson = new GsonBuilder().create();

		try {
			String requestJson = gson.toJson(requestBody, Object.class);
			responseDetails = PacHttpUtils.doHttpPost(urlToQuery, requestJson);
			Map<String, Object> response = (Map<String, Object>) gson.fromJson(responseDetails, Map.class);
			Map<String, Object> aggregations = (Map<String, Object>) response.get("aggregations");

			Map<String, Object> name = (Map<String, Object>) aggregations.get("name");
			List<Map<String, Object>> buckets = (List<Map<String, Object>>) name.get("buckets");

			for (int i = 0; i < buckets.size(); i++) {
				Map<String, Object> bucket = buckets.get(i);
				distribution.put(bucket.get("key_as_string").toString(),
						((Double) bucket.get("doc_count")).longValue());
			}

		} catch (Exception e) {
			LOGGER.error(ERROR_RETRIEVING_INVENTORY_FROM_ES, e);
			throw e;
		}
		return distribution;
	}

	/**
	 *
	 * @param dateFieldName
	 * @param interval
	 * @return
	 */
	private Map<String, Object> buildHistogramAggs(String dateFieldName, String interval) {

		Map<String, Object> agg = new HashMap<String, Object>();
		Map<String, Object> name = new HashMap<String, Object>();
		Map<String, Object> histogram = new HashMap<String, Object>();

		if (!Strings.isNullOrEmpty(dateFieldName)) {

			histogram.put("field", dateFieldName);
			histogram.put("interval", interval);
		}
		name.put("date_histogram", histogram);
		agg.put("name", name);
		return agg;

	}

	/**
	 *
	 * @param index
	 * @param type
	 * @param filter
	 * @param mustNotFilter
	 * @param shouldFilter
	 * @param aggsFilter
	 * @return
	 */
	public Map<String, String> getAccountsByMultiAggs(String index, String type, Map<String, Object> filter,
			Map<String, Object> mustNotFilter, HashMultimap<String, Object> shouldFilter,
			Map<String, Object> aggsFilter, int size) throws Exception {

		StringBuilder requestBody = null;
		StringBuilder urlToQuery = null;
		Map<String, Object> matchFilters = Maps.newHashMap();
		Map<String, String> distribution = new HashMap<String, String>();
		if (filter == null) {
			matchFilters.put(MATCH_ALL, new HashMap<String, String>());
		} else {
			matchFilters.putAll(filter);
		}
		if (null != filter) {
			urlToQuery = new StringBuilder(esUrl).append(FORWARD_SLASH).append(index);
			urlToQuery.append(FORWARD_SLASH).append(_SEARCH);

			requestBody = new StringBuilder("{\"aggs\":{\"accountid\":{\"terms\":{\"field\":\"");
			requestBody.append("accountid.keyword");
			requestBody.append("\",\"size\":" + size + "},"
					+ "\"aggs\":{\"accountname\":{\"terms\":{\"field\":\"accountname.keyword\",\"size\":" + size
					+ "}}}}}}");

		}

		String responseDetails = null;
		Gson gson = new GsonBuilder().create();

		try {
			responseDetails = PacHttpUtils.doHttpPost(urlToQuery.toString(), requestBody.toString());
			Map<String, Object> response = (Map<String, Object>) gson.fromJson(responseDetails, Map.class);
			Map<String, Object> aggregations = (Map<String, Object>) response.get("aggregations");
			Map<String, Object> name = (Map<String, Object>) aggregations.get("accountid");
			List<Map<String, Object>> buckets = (List<Map<String, Object>>) name.get("buckets");

			for (int i = 0; i < buckets.size(); i++) {
				Map<String, Object> bucket = buckets.get(i);
				Map<String, Object> accName = (Map<String, Object>) bucket.get("accountname");
				List<Map<String, Object>> bucketsName = (List<Map<String, Object>>) accName.get("buckets");
				for (int j = 0; j < bucketsName.size(); j++) {
					Map<String, Object> bucketAccName = bucketsName.get(j);
					distribution.put(bucket.get("key").toString(), bucketAccName.get("key").toString());
				}
			}

		} catch (Exception e) {
			LOGGER.error(ERROR_RETRIEVING_INVENTORY_FROM_ES, e);
			throw e;
		}
		return distribution;
	}

	/**
	 *
	 * @param dataSource
	 * @param targetType
	 * @param mustFilter
	 * @param mustNotFilter
	 * @param shouldFilter
	 * @param fields
	 * @param from
	 * @param size
	 * @param searchText
	 * @param mustTermsFilter
	 * @return
	 * @throws Exception
	 */
	public List<LinkedHashMap<String, Object>> getDetailsFromESBySize(String dataSource, String targetType,
			final Map<String, Object> mustFilter, final Map<String, Object> mustNotFilter,
			final HashMultimap<String, Object> shouldFilter, List<String> fields, int from, int size, String searchText,
			final Map<String, Object> mustTermsFilter) throws Exception {

		StringBuilder urlToQueryBuffer = new StringBuilder(esUrl).append(FORWARD_SLASH).append(dataSource);
		if (!Strings.isNullOrEmpty(targetType)) {
			mustFilter.put(DOC_TYPE_KEYWORD, targetType);
		}
		urlToQueryBuffer.append(FORWARD_SLASH).append(_SEARCH);
		if (size > 0 && from >= 0) {
			urlToQueryBuffer.append("?").append("size=").append(size).append("&").append("from=").append(from);
		}
		String urlToQuery = urlToQueryBuffer.toString();
		List<LinkedHashMap<String, Object>> results = new ArrayList<LinkedHashMap<String, Object>>();

		Map<String, Object> requestBody = new HashMap<String, Object>();
		Map<String, Map<String, String>> auditDate = new HashMap<String, Map<String, String>>();
		Map<String, String> order = new HashMap<String, String>();
		order.put(ORDER, "desc");
		order.put(UNMAPPED_TYPE,"date");
		auditDate.put("auditdate", order);
		List<Map<String, Map<String, String>>> list = new ArrayList<Map<String, Map<String, String>>>();
		list.add(auditDate);
		requestBody.put(SORT, list);
		if (size > 0) {
			requestBody.put(SIZE, size);
		}

		requestBody.put(QUERY, buildQuery(mustFilter, mustNotFilter, shouldFilter, searchText, mustTermsFilter,null));
		requestBody.put(_SOURCE, fields);
		Gson serializer = new GsonBuilder().create();
		String request = serializer.toJson(requestBody);
		String responseDetails = null;
		try {
			responseDetails = PacHttpUtils.doHttpPost(urlToQuery, request);
			serializer = new GsonBuilder().create();
			Map<String, Object> response = (Map<String, Object>) serializer.fromJson(responseDetails, Object.class);
			if (response.containsKey(HITS)) {
				Map<String, Object> hits = (Map<String, Object>) response.get(HITS);
				if (hits.containsKey(HITS)) {
					List<Map<String, Object>> hitDetails = (List<Map<String, Object>>) hits.get(HITS);
					if (!hitDetails.isEmpty()) {
						for (Map<String, Object> hitDetail : hitDetails) {
							Map<String, Object> sources = (Map<String, Object>) hitDetail.get(_SOURCE);
							results.add(CommonUtils.flatNestedLinkedHashMap(null, sources));

						}
					} else {
						throw new RuntimeException(NO_RECORDS_FOUND);
					}
				}
			}
		} catch (Exception e) {
			LOGGER.error(ERROR_RETRIEVING_INVENTORY_FROM_ES, e);
			throw e;
		}
		return results;
	}

	/**
	 *
	 * @param dataSource
	 * @param targetType
	 * @param mustFilter
	 * @param mustNotFilter
	 * @param shouldFilter
	 * @param fields
	 * @param mustTermsFilter
	 * @param mustNotTermsFilter
	 * @param mustNotWildCardFilter
	 * @param sortFieldMapList
	 * @return
	 * @throws Exception
	 */
	@SuppressWarnings("unchecked")
	public List<Map<String, Object>> getSortedDataFromESWithMustNotTermsFilter(String dataSource, String targetType,
			Map<String, Object> mustFilter, final Map<String, Object> mustNotFilter,
			final HashMultimap<String, Object> shouldFilter, List<String> fields, Map<String, Object> mustTermsFilter,
			Map<String, Object> mustNotTermsFilter, Map<String, Object> mustNotWildCardFilter,
			List<Map<String, Object>> sortFieldMapList) throws Exception {

		Long totalDocs = getTotalDocumentCountForIndexAndTypeWithMustNotTermsFilter(dataSource, targetType, mustFilter,
				mustNotFilter, shouldFilter, null, mustTermsFilter, mustNotTermsFilter, mustNotWildCardFilter);
		// if filter is not null apply filter, this can be a multi value filter
		// also if from and size are -1 -1 send all the data back and do not
		// paginate
		StringBuilder urlToQueryBuffer = new StringBuilder(esUrl).append(FORWARD_SLASH).append(dataSource);
		if (!Strings.isNullOrEmpty(targetType)) {
			urlToQueryBuffer.append(FORWARD_SLASH).append(targetType);
		}
		urlToQueryBuffer.append(FORWARD_SLASH).append(_SEARCH);
		// paginate for breaking the response into smaller chunks
		Map<String, Object> requestBody = new HashMap<String, Object>();

		requestBody.put(SIZE, ES_PAGE_SIZE);
		if (totalDocs < ES_PAGE_SIZE) {
			requestBody.put(SIZE, totalDocs);
		}

		requestBody.put(QUERY, buildQueryForMustTermsFilter(mustFilter, mustNotFilter, shouldFilter, null,
				mustTermsFilter, mustNotTermsFilter, mustNotWildCardFilter));

		if (null != sortFieldMapList && !sortFieldMapList.isEmpty()) {
			requestBody.put(SORT, sortFieldMapList);
		}

		requestBody.put(_SOURCE, fields);
		Gson serializer = new GsonBuilder().create();
		String request = serializer.toJson(requestBody);
		return prepareResultsUsingPagination(dataSource,request,0,totalDocs);
	}

	/**
	 *
	 * @param index
	 * @param type
	 * @param mustFilter
	 * @param mustNotFilter
	 * @param shouldFilter
	 * @param searchText
	 * @param mustTermsFilter
	 * @param mustNotTermsFilter
	 * @param mustNotWildCardFilter
	 * @return
	 * @throws Exception
	 */
	public long getTotalDocumentCountForIndexAndTypeWithMustNotTermsFilter(String index, String type,
			Map<String, Object> mustFilter, Map<String, Object> mustNotFilter,
			HashMultimap<String, Object> shouldFilter, String searchText, Map<String, Object> mustTermsFilter,
			Map<String, Object> mustNotTermsFilter, Map<String, Object> mustNotWildCardFilter) throws Exception {

		String urlToQuery = buildCountURL(esUrl, index, type);

		Map<String, Object> requestBody = new HashMap<String, Object>();
		Map<String, Object> matchFilters = Maps.newHashMap();
		if (mustFilter == null) {
			matchFilters.put(MATCH_ALL, new HashMap<String, String>());
		} else {
			matchFilters.putAll(mustFilter);
		}
		if (null != mustFilter) {
			requestBody.put(QUERY, buildQueryForMustTermsFilter(matchFilters, mustNotFilter, shouldFilter, searchText,
					mustTermsFilter, mustNotTermsFilter, mustNotWildCardFilter));
		} else {
			requestBody.put(QUERY, matchFilters);
		}
		String responseDetails = null;
		Gson gson = new GsonBuilder().create();
		try {

			String requestJson = gson.toJson(requestBody, Object.class);
			responseDetails = PacHttpUtils.doHttpPost(urlToQuery, requestJson);
			Map<String, Object> response = (Map<String, Object>) gson.fromJson(responseDetails, Object.class);
			return (long) (Double.parseDouble(response.get(COUNT).toString()));
		} catch (Exception e) {
			LOGGER.error(ERROR_RETRIEVING_INVENTORY_FROM_ES, e);
			throw e;
		}
	}

	/**
	 *
	 * @param mustFilter
	 * @return elastic search query details
	 */
	private Map<String, Object> buildQueryForMustTermsFilter(final Map<String, Object> mustFilter,
			final Map<String, Object> mustNotFilter, final HashMultimap<String, Object> shouldFilter,
			final String searchText, final Map<String, Object> mustTermsFilter,
			final Map<String, Object> mustNotTermsFilter, final Map<String, Object> mustNotWildCardFilter) {
		Map<String, Object> queryFilters = Maps.newHashMap();
		Map<String, Object> boolFilters = Maps.newHashMap();

		Map<String, Object> hasChildObject = null;
		Map<String, Object> hasParentObject = null;

		if (isNotNullOrEmpty(mustFilter)) {
			if (mustFilter.containsKey(HAS_CHILD)) {
				hasChildObject = (Map<String, Object>) mustFilter.get(HAS_CHILD);
				mustFilter.remove(HAS_CHILD);
			}
			if (mustFilter.containsKey(HAS_PARENT)) {
				hasParentObject = (Map<String, Object>) mustFilter.get(HAS_PARENT);
				mustFilter.remove(HAS_PARENT);
			}
		}

		if (isNotNullOrEmpty(mustFilter) && (!Strings.isNullOrEmpty(searchText))) {

			List<Map<String, Object>> must = getFilter(mustFilter, mustTermsFilter,null);
			Map<String, Object> match = Maps.newHashMap();
			Map<String, Object> all = Maps.newHashMap();
			all.put(_ALL, searchText);
			match.put(MATCH, all);
			must.add(match);
			boolFilters.put(MUST, must);
		} else if (isNotNullOrEmpty(mustFilter)) {
			boolFilters.put(MUST, getFilter(mustFilter, mustTermsFilter,null));
		}

		if (isNotNullOrEmpty(mustFilter)) {

			Map<String, Object> hasChildMap = Maps.newHashMap();
			Map<String, Object> hasParentMap = Maps.newHashMap();

			List<Map<String, Object>> must = (List<Map<String, Object>>) boolFilters.get(MUST);

			if (null != hasChildObject) {
				hasChildMap.put(HAS_CHILD, hasChildObject);
				must.add(hasChildMap);
			}
			if (null != hasParentObject) {
				hasParentMap.put(HAS_PARENT, hasParentObject);
				must.add(hasParentMap);
			}

		}

		if (isNotNullOrEmpty(mustNotFilter)) {

			boolFilters.put(MUST_NOT, getFilterWithWildCard(mustNotFilter, mustNotTermsFilter, mustNotWildCardFilter));
		}
		if (isNotNullOrEmpty(shouldFilter)) {
			boolFilters.put(SHOULD, getFilter(shouldFilter));
			boolFilters.put(MINIMUM_SHOULD_MATCH, "1");
		}
		queryFilters.put(BOOL, boolFilters);
		return queryFilters;
	}

	/**
	 *
	 * @param mustfilter
	 * @return
	 */
	private List<Map<String, Object>> getFilterWithWildCard(final Map<String, Object> mustfilter,
			final Map<String, Object> mustTerms, final Map<String, Object> wildCardFilter) {

		List<Map<String, Object>> finalFilter = Lists.newArrayList();
		Map<String, Object> wildCardMap = Maps.newHashMap();
		for (Map.Entry<String, Object> entry : mustfilter.entrySet()) {
			Map<String, Object> term = Maps.newHashMap();
			Map<String, Object> termDetails = Maps.newHashMap();
			termDetails.put(entry.getKey(), entry.getValue());
			if (RANGE.equals(entry.getKey())) {
				term.put(RANGE, entry.getValue());
			} else {
				term.put(TERM, termDetails);
			}
			finalFilter.add(term);
		}

		if (isNotNullOrEmpty(wildCardFilter)) {
			wildCardMap.put("wildcard", wildCardFilter);
			finalFilter.add(wildCardMap);
		}

		if (mustTerms != null && !mustTerms.isEmpty()) {
			Map<String, Object> term = Maps.newHashMap();
			term.put(TERMS, mustTerms);
			finalFilter.add(term);
		}
		return finalFilter;
	}

	/**
	 *
	 *            search url
	 * @param index
	 *            name
	 * @param type
	 *            name
	 * @param shouldFilter
	 * @param mustNotFilter
	 * @return elastic search count
	 */
	@SuppressWarnings("unchecked")
	public long getTotalDistributionForIndexAndTypeWithMatchPhrase(String index, String type,
			Map<String, Object> mustFilter, Map<String, Object> mustNotFilter,
			HashMultimap<String, Object> shouldFilter, String searchText,
			Map<String, Object> mustTermsFilter,
			Map<String, List<String>> matchPhrasePrefix) throws Exception {

		String urlToQuery = buildESURL(esUrl, index, type,0);
		JsonParser parser = new JsonParser();
		JsonObject aggregationObject = new JsonObject();
		long totaluntagged = 0;
		Map<String, Object> requestBody = new HashMap<String, Object>();
		Map<String, Object> matchFilters = Maps.newHashMap();
		if (mustFilter == null) {
			matchFilters.put(MATCH_ALL, new HashMap<String, String>());
		} else {
			matchFilters.putAll(mustFilter);
		}
		if (null != mustFilter) {
			requestBody.put(
					QUERY,
					buildQuery(matchFilters, mustNotFilter, shouldFilter,
							searchText, mustTermsFilter, matchPhrasePrefix));
		} else {
			requestBody.put(QUERY, matchFilters);
		}
		StringBuilder aggregationQuery = new StringBuilder("{\"distinct_resourceids\": {\"cardinality\": {\"field\": \"_resourceid.keyword\"}}}");
		aggregationObject = parser.parse(aggregationQuery.toString()).getAsJsonObject();
		requestBody.put(AGGS,aggregationObject);
		String responseDetails = null;
		Gson gson = new GsonBuilder().create();
		try {

			String requestJson = gson.toJson(requestBody, Object.class);
			responseDetails = PacHttpUtils.doHttpPost(urlToQuery, requestJson);
			JsonObject responseJson = parser.parse(responseDetails).getAsJsonObject();
			JsonObject aggs = responseJson.get(AGGREGATIONS).getAsJsonObject();
			JsonObject resourceids = aggs.get("distinct_resourceids").getAsJsonObject();
			totaluntagged = resourceids.get("value").getAsLong();
			return totaluntagged;
		} catch (Exception e) {
			LOGGER.error(ERROR_RETRIEVING_INVENTORY_FROM_ES, e);
			throw e;
		}
	}
	
	/**
	 * 
	 * @param url
	 * @param index
	 * @param type
	 * @return
	 */
	public String buildESURL(String url, String index, String type, int from) {

		StringBuilder urlToQuery = new StringBuilder(url).append(FORWARD_SLASH).append(index);
		if (!Strings.isNullOrEmpty(type)) {
			urlToQuery.append(FORWARD_SLASH).append(type);
		}
		urlToQuery.append(FORWARD_SLASH).append(_SEARCH);
		return urlToQuery.toString();
	}

	/**
	 * 
	 * @param index
	 * @param type
	 * @param mustFilter
	 * @param mustNotFilter
	 * @param shouldFilter
	 * @param aggsFilter
	 * @param size
	 * @param mustTermsFilter
	 * @return
	 * @throws Exception
	 */
	public Map<String, Object> getEnvAndTotalDistributionForIndexAndType(String index, String type,
			Map<String, Object> mustFilter, Map<String, Object> mustNotFilter,
			HashMultimap<String, Object> shouldFilter, String aggsFilter, Map<String, Object> nestedaggs, int size, Map<String, Object> mustTermsFilter)
			throws Exception {

		String urlToQuery = buildAggsURL(esUrl, index, type);
		Map<String, Object> requestBody = new HashMap<String, Object>();
		Map<String, Object> matchFilters = Maps.newHashMap();
		Map<String, Object> distribution = new HashMap<>();
		Map<String, Long> countMap = new HashMap<>();
		Map<String, Object> envMap = new HashMap<>();
		
		if (mustFilter == null) {
			matchFilters.put(MATCH_ALL, new HashMap<String, String>());
		} else {
			matchFilters.putAll(mustFilter);
		}
		if (null != mustFilter) {
			requestBody.put(QUERY, buildQuery(matchFilters, mustNotFilter, shouldFilter, null, mustTermsFilter,null));
			requestBody.put(AGGS, buildAggs(aggsFilter, size, null, nestedaggs));

			if (!Strings.isNullOrEmpty(aggsFilter)) {
				requestBody.put(SIZE, "0");
			}

		} else {
			requestBody.put(QUERY, matchFilters);
		}
		String responseDetails = null;
		Gson gson = new GsonBuilder().create();

		try {
			String requestJson = gson.toJson(requestBody, Object.class);
			responseDetails = PacHttpUtils.doHttpPost(urlToQuery, requestJson);
			Map<String, Object> response = gson.fromJson(responseDetails, Map.class);
			Map<String, Object> aggregations = (Map<String, Object>) response.get(AGGREGATIONS);
			Map<String, Object> name = (Map<String, Object>) aggregations.get(NAME);
			List<Map<String, Object>> buckets = (List<Map<String, Object>>) name.get(BUCKETS);
			
			for (int i = 0; i < buckets.size(); i++) {
				Map<String, Object> bucket = buckets.get(i);
				countMap.put(bucket.get("key").toString(), ((Double) bucket.get("doc_count")).longValue());
				
				Map<String, Object> enviroments = (Map<String, Object>) bucket.get(ENVIRONMENTS);
				List<Map<String, Object>> envBuckets = (List<Map<String, Object>>) enviroments.get(BUCKETS);
				
				Map<String, Long> environments = new HashMap<>();
				for(int j=0; j< envBuckets.size(); j++) {
					Map<String, Object> env = envBuckets.get(j);
					environments.put(env.get("key").toString(), ((Double) env.get("doc_count")).longValue());
				}
				envMap.put(bucket.get("key").toString(), environments);	
			}
			distribution.put(Constants.ASSET_COUNT, countMap);
			distribution.put(Constants.ENV_COUNT, envMap);

		} catch (Exception e) {
			LOGGER.error(ERROR_RETRIEVING_INVENTORY_FROM_ES, e);
			throw e;
		}
		return distribution;
	}

	public  String getRequiredObject(String cloudType,List<String> aggsForTagsList) throws Exception {
		StringBuilder urlToQueryBuffer = new StringBuilder(esUrl).append("/")
				.append(cloudType).append("/").append(SEARCH);
		StringBuilder requestBody = null;
		String accountId;
		if(cloudType.equalsIgnoreCase("aws")){
			accountId="accountid";
		} else if(cloudType.equalsIgnoreCase("azure")){
			accountId="subscriptionId";
		} else{
			accountId="projectId";
		}
		String body = "{"
				+ "\"query\":{\"bool\":{\"must\":[{\"term\":{\"latest\":\"true\"}},{\"term\":{\"_entity\":\"true\"}}]}}"
				+ ",\"aggs\": {"
				+ "\"TargetType\": {\"terms\": {\"field\": \"docType.keyword\",\"size\":"+ES_PAGE_SIZE+"}},"
				+ "\"Id\": {\"terms\": {\"field\": \"" + accountId +".keyword\",\"size\":"+ES_PAGE_SIZE+"}},"
				+ "\"Region\": {\"terms\": {\"field\": \"region.keyword\",\"size\": "+ES_PAGE_SIZE+"}},";

		for(String agg : aggsForTagsList) {
			body += agg + ",";
		}

		// remove trailing comma
		body = body.substring(0, body.length() - 1);
		body += "}}";

		requestBody = new StringBuilder(body);
		try {
			String response = PacHttpUtils.doHttpPost(urlToQueryBuffer.toString(),
					requestBody.toString());
			LOGGER.info("response {} " + response);
			return response;
		}catch (Exception e){
			LOGGER.error(e.getMessage());
			throw new DataException(e);
		}
	}

	private Map<String,Object> getFilterForUntagged(List<Object>fieldValue){
		Map<String,Object> finalResponse=new HashMap<>();
		List<Map<String,Object>> shouldList=new ArrayList<>();
		Map<String,Object> shouldDetails=new HashMap<>();
		for(Object value:fieldValue){
			Map<String, Object> existsDetails = Maps.newHashMap();
			Map<String, Object> exists = Maps.newHashMap();
			List<Map<String,Object>> mustNotList=new ArrayList<>();
			Map<String, Object> mustNotDetails = Maps.newHashMap();
			Map<String, Object> boolDetails = Maps.newHashMap();

			existsDetails.put("field",value);
			exists.put("exists",existsDetails);
			mustNotList.add(exists);
			mustNotDetails.put(MUST_NOT,mustNotList);
			boolDetails.put(BOOL,mustNotDetails);
			shouldList.add(boolDetails);
		}
		shouldDetails.put(SHOULD,shouldList);
		finalResponse.put(BOOL,shouldDetails);
		return  finalResponse;
	}
	public long getTotalDocCountWithConditions(String dataSource,String targetType, String request) throws Exception {
		StringBuilder urlToQueryBuffer = new StringBuilder(esUrl).append(FORWARD_SLASH).append(dataSource);
		if (!Strings.isNullOrEmpty(targetType)) {
			urlToQueryBuffer.append(FORWARD_SLASH).append(targetType);
		}
		urlToQueryBuffer.append(FORWARD_SLASH).append("_count");
		String responseDetails = PacHttpUtils.doHttpPost(urlToQueryBuffer.toString(), request);
		Gson serializer = new GsonBuilder().create();
		Map<String, Object> response = (Map<String, Object>) serializer.fromJson(responseDetails,
				Object.class);
		if(response.containsKey("count")){
			return (long)(Double.parseDouble(response.get(COUNT).toString()));
		} else
			throw new Exception("Could not fetch count.");
	}

	public List<Map<String,Object>> fetchCloudNotifications(String dataSource,String targetType, String request, String requestForCount, int from, int size,int docSize) throws Exception {
		try{
			LOGGER.info("inside ElasticSearchRepository:::fetchCloudNotifications");
			long docCount=getTotalDocCountWithConditions(dataSource,"", requestForCount);
			ThreadLocalUtil.count.set((int)docCount);
			long documentCount = size==0 ? docCount : size;
			LOGGER.info("inside ElasticSearchRepository:::fetchCloudNotifications, docCount is "+docCount);
			StringBuilder urlToQueryBuffer = new StringBuilder(esUrl).append(FORWARD_SLASH).append(dataSource);
			if (!Strings.isNullOrEmpty(targetType)) {
				urlToQueryBuffer.append(FORWARD_SLASH).append(targetType);
			}
			urlToQueryBuffer.append(FORWARD_SLASH).append(_SEARCH);
			return prepareResultsUsingPagination(dataSource,request,from,documentCount);
		} catch(Exception exception){
			LOGGER.error("Exception occurred in ElasticSearchRepository:::fetchCloudNotifications");
			throw new Exception(exception.getMessage());
		}
	}

	public List<Map<String, Object>> getDataFromES(String dataSource, String targetType, Map<String, Object> queryMap) throws Exception {
		Gson gson = new GsonBuilder().create();
		Map<String, Object> mapForCount = new HashMap<>();
		mapForCount.putAll(queryMap);
		mapForCount.remove("size");
		String requestBodyForCount = gson.toJson(mapForCount);
		String requestBody = gson.toJson(queryMap);
		Long totalDocs = getTotalDocCountWithConditions(dataSource, targetType, requestBodyForCount);
		StringBuilder urlToQueryBuffer = new StringBuilder(esUrl).append(FORWARD_SLASH).append(dataSource);
		if (!Strings.isNullOrEmpty(targetType)) {
			urlToQueryBuffer.append(FORWARD_SLASH).append(targetType);
		}
		urlToQueryBuffer.append(FORWARD_SLASH).append(_SEARCH);
		return prepareResultsUsingPagination(dataSource,requestBody,0,totalDocs);
	}
}
