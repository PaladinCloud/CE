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
package com.tmobile.pacman.api.compliance.service;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.tmobile.pacman.api.commons.Constants;
import com.tmobile.pacman.api.commons.exception.DataException;
import com.tmobile.pacman.api.commons.utils.PacHttpUtils;
import com.tmobile.pacman.api.compliance.domain.PolicyRequestPrams;
import com.tmobile.pacman.api.compliance.repository.PolicyExemptionRepository;
import com.tmobile.pacman.api.compliance.repository.PolicyParamsRepository;
import com.tmobile.pacman.api.compliance.repository.PolicyTableRepository;
import com.tmobile.pacman.api.compliance.repository.model.PolicyExemption;
import com.tmobile.pacman.api.compliance.repository.model.PolicyParams;
import com.tmobile.pacman.api.compliance.repository.model.PolicyTable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static com.tmobile.pacman.api.compliance.util.CommonUtil.generatePolicyParamJson;
import static com.tmobile.pacman.api.compliance.util.CommonUtil.getStringDate;

/**
 * The Class RuleInstanceServiceImpl.
 */
@Service
public class PolicyTableServiceImpl implements PolicyTableService, Constants {

	private static final Logger logger = LoggerFactory.getLogger(PolicyTableServiceImpl.class);


	 /** The es host. */
    @Value("${elastic-search.host}")
    private String esHost;

    /** The es port. */
    @Value("${elastic-search.port}")
    private int esPort;

    /** The critical issue default time interval for calculating delta. */
    // @Value("${critical.issues.defaulttime}")
    private String defaultTime = "24hrs";

    /** The Constant PROTOCOL. */
    static final String PROTOCOL = "http";

    /** The es url. */
    private String esUrl;

    public static final String DISABLED_CAPS =	"DISABLED";

    public static final String POLICY_DISABLE_DESCRIPTION =	"The Policy has been disabled by %s and it will be enabled on %s";

    /**
     * Inits the.
     */
    @PostConstruct
    void init() {
        esUrl = PROTOCOL + "://" + esHost + ":" + esPort;
    }


    /** The PolicyTable repository. */
    @Autowired
    private PolicyTableRepository policyTableRepository;
    @Autowired
    private PolicyExemptionRepository policyExempRepository;
    @Autowired
    private PolicyParamsRepository policyParamsRepository;

    /* (non-Javadoc)
     * @see com.tmobile.pacman.api.compliance.service.PolicyTableServiceImpl#getPolicyTableByPolicyId(java.lang.String)
     */
    @Override
    public PolicyTable getPolicyTableByPolicyId(String policyId) {
         PolicyTable policyDetailsObj = policyTableRepository.findPoicyTableByPolicyId(policyId);
         List<PolicyExemption> policyExemptionList = policyExempRepository.findByPolicyID(policyId);
         policyDetailsObj.setPolicyExemption(policyExemptionList);
        Optional<List<PolicyParams>> policyParams = policyParamsRepository
                .findByPolicyId(policyDetailsObj.getPolicyId());
        if (policyParams.isPresent() && !policyParams.get().isEmpty()) {
            policyDetailsObj.setPolicyParams(generatePolicyParamJson(policyDetailsObj.getPolicyId(),
                    policyParams.get()));
        }
        if (policyExemptionList != null && policyExemptionList.size() > 0
				&& DISABLED_CAPS.equals(policyDetailsObj.getStatus())) {
			PolicyExemption policyExemption = policyExemptionList.get(0);
			policyDetailsObj.setDisableDesc(String.format(POLICY_DISABLE_DESCRIPTION,
                    policyExemption.getCreatedBy(), getStringDate(DATE_FORMAT_FOR_BANNER,
                            policyExemption.getExpireDate())));
		}
         return policyDetailsObj;
    }

    @Override
    public PolicyTable getPolicyDetailsWithExemption(String ag, String policyId) {
         PolicyTable policyDetailsObj = getPolicyTableByPolicyId(policyId);
         List<PolicyExemption> policyExemptionList = policyExempRepository.findByPolicyID(policyId);
         policyDetailsObj.setPolicyExemption(policyExemptionList);
         try {
			policyDetailsObj.setExemptionDetails(getExemptionDetailsByPolicyIdAndAG(ag,policyId));
		} catch (DataException e) {
			logger.error("error in getting exemption detail {}", e);
		}
         Optional<List<PolicyParams>> policyParams = policyParamsRepository
                 .findByPolicyId(policyDetailsObj.getPolicyId());
         if (policyParams.isPresent() && !policyParams.get().isEmpty()) {
             policyDetailsObj.setPolicyParams(generatePolicyParamJson(policyDetailsObj.getPolicyId(),
                     policyParams.get()));
         }
         if (policyExemptionList != null && policyExemptionList.size() > 0
 				&& DISABLED_CAPS.equals(policyDetailsObj.getStatus())) {
 			PolicyExemption policyExemption = policyExemptionList.get(0);
 			policyDetailsObj.setDisableDesc(String.format(POLICY_DISABLE_DESCRIPTION,
                    policyExemption.getCreatedBy(), getStringDate(DATE_FORMAT_FOR_BANNER,
                            policyExemption.getExpireDate())));
 		}
         return policyDetailsObj;
    }

    @Override
    public PolicyTable getPolicyTableByPolicyUUID(String policyUUID) {
        PolicyTable policy = policyTableRepository.findPoicyTableByPolicyUUID(policyUUID);
        if (policy != null) {
            Optional<List<PolicyParams>> policyParams = policyParamsRepository.findByPolicyId(policy.getPolicyId());
            if (policyParams.isPresent() && !policyParams.get().isEmpty()) {
                policy.setPolicyParams(generatePolicyParamJson(policy.getPolicyId(),
                        policyParams.get()));
            }
        }
        return policy;
    }

    /*
     * (non-Javadoc)
     *
     * @see com.tmobile.pacman.api.compliance.repository.ComplianceRepository#
     * getPolicyDetailsByApplicationFromES(java.lang.String, java.lang.String,
     * java.lang.String)
     */
    public Map<String,Object> getExemptionDetailsByPolicyIdAndAG(String assetGroup, String policyId)
            throws DataException {
        String responseJson = null;
        Gson gson = new GsonBuilder().create();
        StringBuilder requestBody = null;
        List<Map<String, Object>> cloudDetails = null;
        StringBuilder urlToQueryBuffer = new StringBuilder(esUrl).append("/").append("exceptions/sticky_exceptions").append("/")
                .append(SEARCH);
        requestBody = new StringBuilder(
                "{\"size\":1000,\"query\":{\"bool\":{\"must\":[{\"term\":{\"assetGroup.keyword\":{\"value\":\""+assetGroup+"\"}}},{\"term\":{\"targetTypes.policies.policyId.keyword\":{\"value\":\""
                        + policyId + "\"}}}]}}}");
        try {
            responseJson = PacHttpUtils.doHttpPost(urlToQueryBuffer.toString(), requestBody.toString());
        } catch (Exception e) {
            logger.error(ERROR_IN_US, e);
            throw new DataException(e);
        }
        Map<String, Object> responseMap = (Map<String, Object>) gson.fromJson(responseJson, Object.class);
		if (responseMap.containsKey(HITS)) {
			Map<String, Object> hits = (Map<String, Object>) responseMap.get(HITS);
			if (hits.containsKey(HITS)) {
				cloudDetails = (List<Map<String, Object>>) hits.get(HITS);
				if (cloudDetails.size() > 0) {
					Map<String, Object> cloudDetail = cloudDetails.get(0);
					Map<String, Object> sourceMap = (Map<String, Object>) cloudDetail.get("_source");
					Map<String, Object> excemptionDetails = new HashMap<>();
					excemptionDetails.put("exceptionName", sourceMap.get("exceptionName"));
					excemptionDetails.put("exceptionReason", sourceMap.get("exceptionReason"));
					excemptionDetails.put("expiryDate", sourceMap.get("expiryDate"));
					excemptionDetails.put("assetGroup", sourceMap.get("assetGroup"));
					excemptionDetails.put("dataSource", sourceMap.get("dataSource"));
					excemptionDetails.put("createdBy", sourceMap.get("createdBy"));
					excemptionDetails.put("createdOn", sourceMap.get("createdOn"));
					excemptionDetails.put("policyId", policyId);
					return excemptionDetails;
				}
			}
		}
		return new HashMap<>();
    }

    public List<Map<String, String>> getPolicyDetails(PolicyRequestPrams requestPrams) throws Exception {
        List<Map<String, String>> policyDetailList;
        if (requestPrams.getSource() == null || "".equals(requestPrams.getSource())
                || requestPrams.getTargetType() == null || "".equals(requestPrams.getTargetType())) {
            throw new Exception("Both Source and Target Type are mandatory");
        }
        if (requestPrams.getPolicyUUIDs() != null && requestPrams.getPolicyUUIDs().size() > 0) {
            policyDetailList = policyTableRepository.findPolicyPolicyUUIDs(requestPrams.getPolicyUUIDs());
        } else if (requestPrams.getEnricherSource() != null && !"".equals(requestPrams.getEnricherSource())) {
            policyDetailList = policyTableRepository.findPolicyBySourceAndTargetTypeAndEnricherSource(requestPrams.getSource(), requestPrams.getTargetType(), requestPrams.getEnricherSource());
        } else {
            policyDetailList = policyTableRepository.findPolicyBySourceAndTargetType(requestPrams.getSource(), requestPrams.getTargetType());
        }
        // add policy params to policy details.
        List<Map<String, String>> resultPolicyList = new ArrayList<>();
        policyDetailList.forEach(policy -> {
            Map<String, String> updatedPolicy = new HashMap<>(policy);
            Optional<List<PolicyParams>> policyParams = policyParamsRepository.findByPolicyId(policy.get("policyId"));
            if (policyParams.isPresent()) {
                List<PolicyParams> paramList = policyParams.get();
                paramList.forEach(param -> {
                    updatedPolicy.put(param.getKey(), param.getValue() != null ? param.getValue() : param.getDefaultVal());
                });
            }
            resultPolicyList.add(updatedPolicy);
        });

        return resultPolicyList;
    }

    @Override
    public PolicyRequestPrams getAssetTypeByPolicyUUID(String policyUUID) {
        Optional<PolicyTable> optionalPolicyDetails = policyTableRepository.findByPolicyUUID(policyUUID);
        if (optionalPolicyDetails.isPresent()) {
            PolicyTable policyDetails = optionalPolicyDetails.get();
            return convertPolicyDetailsToPolicyEngineParams(Collections.singletonList(policyDetails));
        }
        return new PolicyRequestPrams();
    }

    @Override
    public List<PolicyRequestPrams> getAssetTypesBySource(String source) {
        List<PolicyTable> policies = policyTableRepository.findByAssetGroup(source);
        return getPolicyEngineParamsForPolices(policies);
    }

    @Override
    public List<PolicyRequestPrams> getAssetTypesByEnricherSource(String enricherSource) {
        List<PolicyTable> policies = policyTableRepository.findByEnricherSource(enricherSource);
        List<PolicyRequestPrams> policyEngineParamsForPolices = getPolicyEngineParamsForPolices(policies);
        policyEngineParamsForPolices.forEach(param -> {
            param.setEnricherSource(enricherSource);
        });
        return policyEngineParamsForPolices;
    }

    private List<PolicyRequestPrams> getPolicyEngineParamsForPolices(List<PolicyTable> policies) {
        List<PolicyRequestPrams> allParams = new ArrayList<>();
        Map<String, List<PolicyTable>> policiesGroupedByTargetType = new HashMap<>();
        if (policies.isEmpty()) {
            return allParams;
        }
        policies.forEach(policy -> {
            policiesGroupedByTargetType.computeIfAbsent(policy.getTargetType(), p -> new ArrayList<>()).add(policy);
        });
        policiesGroupedByTargetType.forEach((targetType, group) -> {
            PolicyRequestPrams convertedParams = convertPolicyDetailsToPolicyEngineParams(group);
            if (convertedParams != null) {
                convertedParams.setPolicyUUIDs(new ArrayList<>());
                allParams.add(convertedParams);
            }
        });
        return allParams;
    }

    private PolicyRequestPrams convertPolicyDetailsToPolicyEngineParams(List<PolicyTable> policies) {
        PolicyRequestPrams params = new PolicyRequestPrams();
        if (policies.isEmpty()) {
            return null;
        }
        params.setSource(policies.get(0).getAssetGroup());
        params.setTargetType(policies.get(0).getTargetType());
        List<String> policyUUIDs = new ArrayList<>();
        policies.forEach(policy -> {
            policyUUIDs.add(policy.getPolicyUUID());
        });
        params.setPolicyUUIDs(policyUUIDs);
        return params;
    }
}
