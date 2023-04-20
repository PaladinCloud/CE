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

import static com.tmobile.pacman.api.admin.common.AdminConstants.*;
import static com.tmobile.pacman.api.commons.Constants.*;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpEntity;
import org.apache.http.HttpHost;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.entity.ContentType;
import org.apache.http.nio.entity.NStringEntity;
import org.apache.http.util.EntityUtils;
import org.elasticsearch.client.Response;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestClientBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.tmobile.pacman.api.admin.config.PacmanConfiguration;
import com.tmobile.pacman.api.admin.domain.AssetGroupExceptionDetailsForES;
import com.tmobile.pacman.api.admin.domain.AssetGroupExceptionDetailsRequest;
import com.tmobile.pacman.api.admin.domain.AssetGroupExceptionProjections;
import com.tmobile.pacman.api.admin.domain.DeleteAssetGroupExceptionRequest;
import com.tmobile.pacman.api.admin.domain.PolicyDetails;
import com.tmobile.pacman.api.admin.domain.PolicyProjection;
import com.tmobile.pacman.api.admin.domain.StickyExceptionResponse;
import com.tmobile.pacman.api.admin.domain.TargetTypePolicyDetails;
import com.tmobile.pacman.api.admin.domain.TargetTypePolicyViewDetails;
import com.tmobile.pacman.api.admin.exceptions.PacManException;
import com.tmobile.pacman.api.admin.repository.AssetGroupExceptionRepository;
import com.tmobile.pacman.api.admin.repository.model.AssetGroupException;
import com.tmobile.pacman.api.admin.util.AdminUtils;

/**
 * AssetGroup Exception Service Implementations
 */
@Service
public class AssetGroupExceptionServiceImpl implements AssetGroupExceptionService {
			
	private static final Logger log = LoggerFactory.getLogger(AssetGroupExceptionServiceImpl.class);
	
	@Autowired
	private PacmanConfiguration config;
	
	@Autowired
	private AssetGroupTargetDetailsService assetGroupTargetDetailsService;
	
	@Autowired
	private AssetGroupExceptionRepository assetGroupExceptionRepository;

	@Autowired
	private PolicyService policyService;

	@Autowired
	private NotificationService notificationService;
	
	private static RestClient restClient;
	
	@Autowired
	private ObjectMapper mapper;
	
	@Override
	public Page<AssetGroupExceptionProjections> getAllAssetGroupExceptions(String searchTerm, int page, int size) {
		return assetGroupExceptionRepository.findAllAssetGroupExceptions(searchTerm.toLowerCase(), PageRequest.of(page, size));
	}
	
	@Override
	public StickyExceptionResponse getAllTargetTypesByExceptionNameAndDataSource(final String exceptionName, final String dataSource) throws PacManException {
		Set<String> selectedTargetTypes = Sets.newHashSet();
		StickyExceptionResponse stickyExceptionResponse = new StickyExceptionResponse();
		List<AssetGroupExceptionProjections> allAssetGroupExceptions = assetGroupExceptionRepository.findAllAssetGroupExceptions(exceptionName, dataSource);
		List<TargetTypePolicyViewDetails> allTargetTypePolicyDetails = Lists.newArrayList();
		List<String> policyIdList = new ArrayList<String>();
		TargetTypePolicyViewDetails targetTypeRuleDetails = new TargetTypePolicyViewDetails();
		int exceptionSize = allAssetGroupExceptions.size();
		for (int index = 0; index<exceptionSize; index++) {
			AssetGroupExceptionProjections assetGroupException = allAssetGroupExceptions.get(index);
			String targetType = assetGroupException.getTargetType();
			if(StringUtils.isNotBlank(assetGroupException.getPolicyId())) {
				policyIdList.add(assetGroupException.getPolicyId());
			}

			if(StringUtils.isNotBlank(targetType)) {
				selectedTargetTypes.add(targetType);
				targetTypeRuleDetails.setTargetName(assetGroupException.getTargetType());
				targetTypeRuleDetails.setAdded(true);
				if(isNotLast(index, exceptionSize)) {
					if(!allAssetGroupExceptions.get(1+index).getTargetType().equals(targetType)){
						allTargetTypePolicyDetails.add(getTargetTypeRuleDetails(policyIdList, targetTypeRuleDetails, targetType));
						policyIdList.clear();
						targetTypeRuleDetails = new TargetTypePolicyViewDetails();
					}
				} else {
					allTargetTypePolicyDetails.add(getTargetTypeRuleDetails(policyIdList, targetTypeRuleDetails, targetType));
					policyIdList.clear();
					targetTypeRuleDetails = new TargetTypePolicyViewDetails();
				}
			}
		}
		
		stickyExceptionResponse.setExceptionName(allAssetGroupExceptions.get(0).getExceptionName()); 
		stickyExceptionResponse.setExceptionReason(allAssetGroupExceptions.get(0).getExceptionReason());
		stickyExceptionResponse.setExpiryDate(allAssetGroupExceptions.get(0).getExpiryDate());
		stickyExceptionResponse.setDataSource(allAssetGroupExceptions.get(0).getDataSource());
		
		Set<String> allSelectedTargetTypes = Sets.newHashSet();
		allSelectedTargetTypes.addAll(selectedTargetTypes);
		
		List<TargetTypePolicyViewDetails> remainingTargetTypeDetails = assetGroupTargetDetailsService.getTargetTypesByAssetGroupIdAndTargetTypeNotIn(allAssetGroupExceptions.get(0).getAssetGroup(), allSelectedTargetTypes);
		allTargetTypePolicyDetails.addAll(remainingTargetTypeDetails);
		
		stickyExceptionResponse.setTargetTypes(allTargetTypePolicyDetails);
		stickyExceptionResponse.setGroupName(allAssetGroupExceptions.get(0).getAssetGroup());
		return stickyExceptionResponse;
	}

	private TargetTypePolicyViewDetails getTargetTypeRuleDetails(List<String> policyIdList, TargetTypePolicyViewDetails targetTypeRuleDetails, String targetType) {
		if(StringUtils.isNotBlank(targetType)) {
			List<PolicyProjection> allPolicies = policyService.getAllPoliciesByTargetTypeAndNotInPolicyIdList(targetType, policyIdList);
			List<PolicyProjection> policies = policyService.getAllPoliciesByTargetTypeAndPolicyIdList(targetType, policyIdList);
			targetTypeRuleDetails.setAllPolicies(allPolicies);
			targetTypeRuleDetails.setPolicies(policies);
		}
		return targetTypeRuleDetails;
	}

	private boolean isNotLast(final int index, final int size) {
		return (index + 1 < size);
	}

	@Override
	public String createAssetGroupExceptions(final AssetGroupExceptionDetailsRequest assetGroupExceptionDetails, final String userId) throws PacManException {
		List<String> policyIds = Lists.newArrayList();
		try {
			List<TargetTypePolicyDetails> targetTypes = assetGroupExceptionDetails.getTargetTypes();
			String status = deleteAssetGroupExceptions(new DeleteAssetGroupExceptionRequest(assetGroupExceptionDetails.getExceptionName().trim(), assetGroupExceptionDetails.getAssetGroup().trim()), userId,"create");
			String notificationSubject = EXCEPTION_DELETEION_SUCCESS.equalsIgnoreCase(status)? UPDATE_STICKY_EXCEPTION_SUBJECT : CREATE_STICKY_EXCEPTION_SUBJECT;
			Actions action = EXCEPTION_DELETEION_SUCCESS.equalsIgnoreCase(status)? Actions.UPDATE : Actions.CREATE;
			if (updateExceptionToES(assetGroupExceptionDetails)) {
				try {
					boolean haveNoRules = true;
					List<AssetGroupException> allAssetGroupExceptions = Lists.newArrayList();
					for (int targetIndex = 0; targetIndex < targetTypes.size(); targetIndex++) {
						List<PolicyDetails> policies = targetTypes.get(targetIndex).getPolicies();
						for (int policyIndex = 0; policyIndex < policies.size(); policyIndex++) {
							AssetGroupException assetGroupException = new AssetGroupException();
							assetGroupException.setGroupName(assetGroupExceptionDetails.getAssetGroup().trim());
							assetGroupException.setTargetType(targetTypes.get(targetIndex).getTargetName().trim());
							assetGroupException.setDataSource(assetGroupExceptionDetails.getDataSource().trim());
							assetGroupException.setExceptionName(assetGroupExceptionDetails.getExceptionName().trim());
							assetGroupException.setExceptionReason(assetGroupExceptionDetails.getExceptionReason().trim());
							assetGroupException.setExpiryDate(AdminUtils.getFormatedDate("dd/MM/yyyy", assetGroupExceptionDetails.getExpiryDate()));
							assetGroupException.setPolicyId(policies.get(policyIndex).getId().trim());
							assetGroupException.setPolicyName(policies.get(policyIndex).getText().trim());
							policyIds.add(policies.get(policyIndex).getId().trim());
							allAssetGroupExceptions.add(assetGroupException);
							haveNoRules = false;
						}
						
					}
					if(targetTypes.size()==0 || haveNoRules) {
						AssetGroupException assetGroupException = new AssetGroupException();
						assetGroupException.setGroupName(assetGroupExceptionDetails.getAssetGroup().trim());
						assetGroupException.setTargetType(StringUtils.EMPTY);
						assetGroupException.setDataSource(assetGroupExceptionDetails.getDataSource().trim());
						assetGroupException.setExceptionName(assetGroupExceptionDetails.getExceptionName().trim());
						assetGroupException.setExceptionReason(assetGroupExceptionDetails.getExceptionReason().trim());
						assetGroupException.setExpiryDate(AdminUtils.getFormatedDate("dd/MM/yyyy", assetGroupExceptionDetails.getExpiryDate()));
						assetGroupException.setPolicyId(StringUtils.EMPTY);
						assetGroupException.setPolicyName(StringUtils.EMPTY);
						allAssetGroupExceptions.add(assetGroupException);
					}

					assetGroupExceptionRepository.saveAll(allAssetGroupExceptions);
					notificationService.triggerNotificationForCreateStickyEx(assetGroupExceptionDetails, userId, notificationSubject, policyIds, action);
					if(targetTypes.size()!=0 && !haveNoRules) {
						invokeAllPolices(policyIds);
					}
					return CONFIG_STICKY_EXCEPTION_SUCCESS;
				} catch (Exception e) {
					invokeAPI("DELETE", "/exceptions/sticky_exceptions/"+ assetGroupExceptionDetails.getExceptionName().replaceAll(" ", "_"), null);
					throw new PacManException("Exception while updating to table " + e.getMessage());
				}
			} else {
				throw new PacManException("Exception while updating to ES");
			}
		} catch (Exception exception) {
			log.error(UNEXPECTED_ERROR_OCCURRED, exception);
			throw new PacManException(UNEXPECTED_ERROR_OCCURRED);
		}
	}

	private void invokeAllPolices(List<String> policyIds) {
		try {	
			policyService.invokeAllPolicies(policyIds);
		} catch (Exception exception) {
			log.error(UNEXPECTED_ERROR_OCCURRED, exception);
		}
	}

	@Override
	public String updateAssetGroupExceptions(final AssetGroupExceptionDetailsRequest assetGroupExceptionDetailsRequest, String userId) throws PacManException {
		try {
			deleteAssetGroupExceptions(new DeleteAssetGroupExceptionRequest(assetGroupExceptionDetailsRequest.getExceptionName(), assetGroupExceptionDetailsRequest.getAssetGroup()), userId,"update");
			return createAssetGroupExceptions(assetGroupExceptionDetailsRequest, userId);
		} catch (Exception exception) {
			throw new PacManException("Exception while deleteing the exception "+exception.getMessage());
		}
	}

	@Override
	public String deleteAssetGroupExceptions(final DeleteAssetGroupExceptionRequest assetGroupExceptionRequest, String userId, String action) throws PacManException {
		try {
			Response exceptionResponse = invokeAPI("GET","/exceptions/sticky_exceptions/" + assetGroupExceptionRequest.getExceptionName().replaceAll(" ", "_")+"/_source", null);
			if(exceptionResponse != null) {
				String exceptionBackUp = EntityUtils.toString(exceptionResponse.getEntity());
				Response response = invokeAPI("DELETE","/exceptions/sticky_exceptions/" + assetGroupExceptionRequest.getExceptionName().replaceAll(" ", "_"), null);
				if(response != null) {
					if (response.getStatusLine().getStatusCode() == 200 || response.getStatusLine().getStatusCode() == 201) {
						try {
							List<AssetGroupException> assetGroupExceptions = assetGroupExceptionRepository.findByGroupNameAndExceptionName(assetGroupExceptionRequest.getGroupName(), assetGroupExceptionRequest.getExceptionName());
							for (AssetGroupException assetGroupException : assetGroupExceptions) {
								assetGroupExceptionRepository.delete(assetGroupException);
								if("delete".equalsIgnoreCase(action)){
									notificationService.triggerNotificationForDelStickyException(assetGroupException, userId, DELETE_STICKY_EXCEPTION_SUBJECT, assetGroupExceptionRequest.getDeletedBy());
								}
							}
						} catch (Exception exception) {
							Response exceptionBackUpResponse = invokeAPI("POST", "/exceptions/sticky_exceptions/"+ assetGroupExceptionRequest.getExceptionName().trim().replaceAll(" ", "_"), exceptionBackUp);
							if(exceptionBackUpResponse != null) {
								if (exceptionBackUpResponse.getStatusLine().getStatusCode() == 200 || exceptionBackUpResponse.getStatusLine().getStatusCode() == 201) {
									throw new PacManException("Exception while updating to table as well as updating ES:"+exception.getMessage());
								}
							}
							throw new PacManException("Exception while updating to table "+exception.getMessage());
						}
					}
				} else {
					return EXCEPTION_DELETEION_FAILURE;
				}
				return EXCEPTION_DELETEION_SUCCESS;
			} else {
				return EXCEPTION_DELETEION_FAILURE;
			}
		} catch (Exception exception) {
			throw new PacManException("Exception while deleteing the exception "+exception.getMessage());
		}
	}

	private boolean updateExceptionToES(final AssetGroupExceptionDetailsRequest assetGroupExceptionDetails) {
		try {
			AssetGroupExceptionDetailsForES exceptionDetailsForES = new AssetGroupExceptionDetailsForES();
			exceptionDetailsForES.setAssetGroup(assetGroupExceptionDetails.getAssetGroup().trim());
			exceptionDetailsForES.setExpiryDate(AdminUtils.getFormatedStringDate("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", AdminUtils.getFormatedDate("dd/MM/yyyy", assetGroupExceptionDetails.getExpiryDate())));
			exceptionDetailsForES.setDataSource(assetGroupExceptionDetails.getDataSource().trim());
			exceptionDetailsForES.setExceptionName(assetGroupExceptionDetails.getExceptionName().trim());
			exceptionDetailsForES.setExceptionReason(assetGroupExceptionDetails.getExceptionReason().trim());

			List<Object> targetTypesForES = Lists.newArrayList(); 
			List<TargetTypePolicyDetails> targetTypes = assetGroupExceptionDetails.getTargetTypes();
			for (int targetIndex = 0; targetIndex < targetTypes.size(); targetIndex++) {
				if(targetTypes.get(targetIndex).getPolicies().isEmpty()) {
					continue;
				} else {
					Map<String, Object> targetTypeForES = Maps.newHashMap();
					targetTypeForES.put("name", targetTypes.get(targetIndex).getTargetName());
					List<PolicyDetails> policies = targetTypes.get(targetIndex).getPolicies();
					List<Map<String, String>> policiesForES = Lists.newArrayList();
					for (int policyIndex = 0; policyIndex < policies.size(); policyIndex++) {
						Map<String, String> policyForES = Maps.newHashMap();
						policyForES.put("policyName", policies.get(policyIndex).getText());
						policyForES.put("policyId", policies.get(policyIndex).getId());
						policiesForES.add(policyForES);
					}
					targetTypeForES.put("policies", policiesForES);
					targetTypesForES.add(targetTypeForES);
				}
			}
			
			exceptionDetailsForES.setTargetTypes(targetTypesForES);
			createIndex("exceptions");
			Response response = invokeAPI("POST", "/exceptions/sticky_exceptions/"+ assetGroupExceptionDetails.getExceptionName().trim().replaceAll(" ", "_"), mapper.writeValueAsString(exceptionDetailsForES));
			if(response != null) {
				if (response.getStatusLine().getStatusCode() == 200 || response.getStatusLine().getStatusCode() == 201) {
					return true;
				} else {
					return false;
				}
			} else {
				return false;
			}
		} catch (Exception exception) {
			log.error(UNEXPECTED_ERROR_OCCURRED, exception);
			return false;
		}
	}
	
	private void createIndex(String indexName) {
		if (!indexExists(indexName)) {
			String payLoad = "{\"settings\": {  \"number_of_shards\" : 1,\"number_of_replicas\" : 1 }}";
			invokeAPI("PUT", indexName, payLoad);
		}
	}
	
	private boolean indexExists(String indexName){
		Response response = invokeAPI("HEAD",indexName,null);
		if(response!=null){
			return response.getStatusLine().getStatusCode() == 200?true:false;
		}
		return false;
	}
	
	private Response invokeAPI(String method, String endpoint, String payLoad) {
		HttpEntity entity = null;
        try {
            if (payLoad != null) {
                entity = new NStringEntity(payLoad, ContentType.APPLICATION_JSON);
            }
            if(!endpoint.startsWith("/")) {
            	endpoint = "/"+endpoint;
            }
            return getRestClient().performRequest(method, endpoint, Collections.<String, String>emptyMap(), entity);
        } catch (IOException exception) {
        	log.error(UNEXPECTED_ERROR_OCCURRED, exception);
        } finally {
        	if(entity != null) {
        		try {
					EntityUtils.consume(entity);
				} catch (IOException exception) {
					log.error(UNEXPECTED_ERROR_OCCURRED, exception);
				}
        	}
        }
        return null; 
	}
	
	private RestClient getRestClient() {
        if (restClient == null) {
        	String esHost = config.getElasticSearch().getDevIngestHost();
    		int esPort = config.getElasticSearch().getDevIngestPort();
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

	@Override
	public Collection<String> getAllExceptionNames() {
		return assetGroupExceptionRepository.getAllExceptionNames();
	}
}
