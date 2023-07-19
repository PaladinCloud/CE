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

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.tmobile.pacman.api.commons.exception.DataException;
import com.tmobile.pacman.api.commons.utils.PacHttpUtils;
import com.tmobile.pacman.api.compliance.repository.PolicyParamsRepository;
import com.tmobile.pacman.api.compliance.repository.model.PolicyParams;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import com.tmobile.pacman.api.commons.Constants;
import com.tmobile.pacman.api.compliance.repository.PolicyExemptionRepository;
import com.tmobile.pacman.api.compliance.repository.PolicyTableRepository;
import com.tmobile.pacman.api.compliance.repository.model.PolicyExemption;
import com.tmobile.pacman.api.compliance.repository.model.PolicyTable;

import javax.annotation.PostConstruct;

import static com.tmobile.pacman.api.compliance.util.CommonUtil.generatePolicyParamJson;
import static com.tmobile.pacman.api.compliance.util.CommonUtil.getStringDate;

/**
 * The Class RuleInstanceServiceImpl.
 */
@Service
public class PolicyTableServiceImpl implements PolicyTableService, Constants {

    @Value("${elastic-search.host}")
    private String esHost;

    /** The es port. */
    @Value("${elastic-search.port}")
    private int esPort;

    /** The Constant PROTOCOL. */
    static final String PROTOCOL = "http";

    /** The es url. */
    private String esUrl;
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

    /* (non-Javadoc)
     * @see com.tmobile.pacman.api.compliance.service.PolicyTableServiceImpl#getPolicyTableByPolicyId(java.lang.String)
     */
    @Override
    public PolicyTable getPolicyTableByPolicyId(String policyId) {
        PolicyTable policy = policyTableRepository.findPoicyTableByPolicyId(policyId);
        List<PolicyExemption> policyExemptionList = policyExempRepository.findByPolicyID(policyId);
        policy.setPolicyExemption(policyExemptionList);
        return policy;
    }

    @Override
    public PolicyTable getPolicyTableByPolicyUUID(String policyUUID) {
        PolicyTable policy = policyTableRepository.findPoicyTableByPolicyUUID(policyUUID);
        List<PolicyExemption> policyExemptionList = policyExempRepository.findByPolicyID(policy.getPolicyId());
        policy.setPolicyExemption(policyExemptionList);
        return policy;
    }

}
