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
package com.tmobile.pacman.api.admin.repository.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import com.google.common.base.Strings;
import com.tmobile.pacman.api.commons.repo.PacmanRdsRepository;
import com.tmobile.pacman.api.commons.utils.PacHttpUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.configurationprocessor.json.JSONArray;
import org.springframework.boot.configurationprocessor.json.JSONObject;
import org.springframework.stereotype.Service;

import com.google.common.collect.Lists;
import com.tmobile.pacman.api.admin.domain.PolicyProjection;
import com.tmobile.pacman.api.admin.domain.TargetTypePolicyDetails;
import com.tmobile.pacman.api.admin.domain.TargetTypePolicyViewDetails;
import com.tmobile.pacman.api.admin.exceptions.PacManException;
import com.tmobile.pacman.api.admin.repository.AssetGroupTargetDetailsRepository;
import com.tmobile.pacman.api.admin.repository.model.AssetGroupDetails;
import com.tmobile.pacman.api.admin.repository.model.AssetGroupTargetDetails;
import com.tmobile.pacman.api.commons.Constants;

import javax.annotation.PostConstruct;

/**
 * AssetGroup Target Details Service Implementations
 */
@Service
public class AssetGroupTargetDetailsServiceImpl implements AssetGroupTargetDetailsService, Constants {

	private static final Logger log = LoggerFactory.getLogger(AssetGroupTargetDetailsService.class);

	@Autowired
	private AssetGroupTargetDetailsRepository assetGroupTargetDetailsRepository;

	@Autowired
	private AssetGroupService assetGroupService;

	@Autowired
	private PolicyService policyService;

	private String esUrl;

	private static final String PROTOCOL = "http";

	@Value("${elastic-search.host}")
	private String esHost;
	@Value("${elastic-search.port}")
	private int esPort;

	private static final String FORWARD_SLASH = "/";

	@PostConstruct
	void init()
	{
		esUrl = PROTOCOL + "://" + esHost + ":" + esPort;
	}

	@Autowired
	PacmanRdsRepository rdsRepository;


	@Override
	public List<TargetTypePolicyDetails> getTargetTypesByAssetGroupName(String assetGroupName) {
		AssetGroupDetails assetGroupDetails = assetGroupService.findByGroupName(assetGroupName);
		List<AssetGroupTargetDetails> allAssetGroupTargetDetails = assetGroupTargetDetailsRepository.findByGroupId(assetGroupDetails.getGroupId());
		List<TargetTypePolicyDetails> allStudentsDetails = allAssetGroupTargetDetails.parallelStream().map(fetchTargetTypeRuleDetails).collect(Collectors.toList());
		return allStudentsDetails;
	}

	Function<AssetGroupTargetDetails, TargetTypePolicyDetails> fetchTargetTypeRuleDetails = assetGroupTargetDetail -> {
		TargetTypePolicyDetails targetTypeRuleDetails = new TargetTypePolicyDetails();
		targetTypeRuleDetails.setTargetName(assetGroupTargetDetail.getTargetType());
		List<PolicyProjection> allPolicies = policyService.getAllPoliciesByTargetTypeName(assetGroupTargetDetail.getTargetType());
		targetTypeRuleDetails.setAllPolicies(allPolicies);
		targetTypeRuleDetails.setPolicies(Lists.newArrayList());
		return targetTypeRuleDetails;
	};

	@Override
	public List<TargetTypePolicyViewDetails> getTargetTypesByAssetGroupIdAndTargetTypeNotIn(String assetGroupName, Set<String> targetTypeNames) throws PacManException {
		AssetGroupDetails assetGroupDetails = assetGroupService.findByGroupName(assetGroupName);
		List<AssetGroupTargetDetails> allAssetGroupTargetDetails = Lists.newArrayList();
		if (assetGroupDetails != null) {
			if(targetTypeNames.isEmpty()) {
				allAssetGroupTargetDetails = assetGroupTargetDetailsRepository.findByGroupId(assetGroupDetails.getGroupId());
			} else {
				allAssetGroupTargetDetails = assetGroupTargetDetailsRepository.findByGroupIdAndTargetTypeNotIn(assetGroupDetails.getGroupId(), targetTypeNames);
			}
			List<TargetTypePolicyViewDetails> allStudentsDetails = allAssetGroupTargetDetails.parallelStream().map(fetchStickyExceptionTargetTypeRuleDetails).collect(Collectors.toList());
			return allStudentsDetails;
		} else {
			throw new PacManException("Asset Group does not exits");
		}
	}
	
	Function<AssetGroupTargetDetails, TargetTypePolicyViewDetails> fetchStickyExceptionTargetTypeRuleDetails = assetGroupTargetDetail -> {
		TargetTypePolicyViewDetails targetTypeRuleDetails = new TargetTypePolicyViewDetails();
		targetTypeRuleDetails.setTargetName(assetGroupTargetDetail.getTargetType());
		List<PolicyProjection> allPolicies = policyService.getAllPoliciesByTargetTypeName(assetGroupTargetDetail.getTargetType());
		targetTypeRuleDetails.setAllPolicies(allPolicies);
		targetTypeRuleDetails.setPolicies(Lists.newArrayList());
		return targetTypeRuleDetails;
	};

	public List<Map<String, Object>> getTargetTypesByAssetGroupNameFromES(String assetGroupName) {
		String query1 = "{\"size\":\"0\",\"query\":{\"bool\":{\"must\":[{\"term\":{\"latest\":\"true\"}},{\"term\":{\"_entity\":\"true\"}}]}},\"aggs\":{\"name\":{\"terms\":{\"field\":\"_type\",\"size\":1000}}}}";

		String urlToQuery = buildAggsURL(esUrl, assetGroupName, null);
		List<String> targetTypes = new ArrayList<>();
		String responseDetails = null;
		try {
			responseDetails = PacHttpUtils.doHttpPost(urlToQuery, query1);
			JSONArray jsonArray =new JSONObject(responseDetails).getJSONObject("aggregations").getJSONObject("name").getJSONArray("buckets");
			for (int i=0; i < jsonArray.length(); i++) {
				targetTypes.add(jsonArray.getJSONObject(i).get("key").toString());
			}
		} catch (Exception e) {
			log.error("Cannot fetch target types from ES");
		}
		String result = targetTypes.stream().collect(Collectors.joining("','", "'", "'"));
		String query = "select distinct targetName as type, displayName as displayName ,category as category,domain as domain from cf_Target "
				+ " where  (status = 'active' or status = 'enabled') and targetName in ("+result+") ";
		return rdsRepository.getDataFromPacman(query);
	}

	private String buildAggsURL(String url, String index, String type) {

		StringBuilder urlToQuery = new StringBuilder(url).append(FORWARD_SLASH).append(index);
		if (!Strings.isNullOrEmpty(type)) {
			urlToQuery.append(FORWARD_SLASH).append(type);
		}
		urlToQuery.append(FORWARD_SLASH).append("_search/?size=0");
		return urlToQuery.toString();
	}

	@Override
	public  String getGcpFlagValueFromDB(){

		String query = "select value from pac_config_properties "
				+ " where  cfkey = 'gcp.enabled' ";

		List<Map<String, Object>> gcpValue =rdsRepository.getDataFromPacman(query);
		String valueString="";
		for(Map<String,Object>value:gcpValue) {
			valueString= (String) value.get("value");
		}
		log.info("gcp {} ",rdsRepository.getDataFromPacman(query));
		return  valueString;
	}

	@Override
	public  String getAzureFlagValueFromDB(){

		String query = "select value from pac_config_properties "
				+ " where  cfkey = 'azure.enabled' ";

		List<Map<String, Object>> azureValue=rdsRepository.getDataFromPacman(query);
		String valueString="";
		for(Map<String,Object>value:azureValue) {
			valueString= (String) value.get("value");
		}
		log.info("azure {} ",rdsRepository.getDataFromPacman(query));
		return  valueString;
	}
}
