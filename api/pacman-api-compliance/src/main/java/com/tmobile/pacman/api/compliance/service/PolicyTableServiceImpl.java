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

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.tmobile.pacman.api.commons.Constants;
import com.tmobile.pacman.api.compliance.repository.PolicyTableRepository;
import com.tmobile.pacman.api.compliance.repository.model.PolicyTable;

/**
 * The Class RuleInstanceServiceImpl.
 */
@Service
public class PolicyTableServiceImpl implements PolicyTableService, Constants {
    
    /** The PolicyTable repository. */
    @Autowired
    private PolicyTableRepository policyTableRepository;

    /* (non-Javadoc)
     * @see com.tmobile.pacman.api.compliance.service.PolicyTableServiceImpl#getPolicyTableByPolicyId(java.lang.String)
     */
    @Override
    public PolicyTable getPolicyTableByPolicyId(String policyId) {
        return policyTableRepository.findPoicyTableByPolicyId(policyId);
    }
    
    @Override
    public PolicyTable getPolicyTableByPolicyUUID(String policyUUID) {
        return policyTableRepository.findPoicyTableByPolicyUUID(policyUUID);
    }

}
