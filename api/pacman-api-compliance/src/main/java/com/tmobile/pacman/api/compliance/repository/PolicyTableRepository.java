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
package com.tmobile.pacman.api.compliance.repository;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import com.tmobile.pacman.api.compliance.repository.model.PolicyTable;

/**
 * The Interface RuleInstanceRepository.
 */
@Repository
public interface PolicyTableRepository extends
        CrudRepository<PolicyTable, String> {

    /**
     * Find PolicyTable by policy id.
     *
     * @param policyId the policy id
     * @return PolicyTable
     */
    public PolicyTable findPoicyTableByPolicyId(final String policyId);
    
    
    /**
     * Find PolicyTable by policy UUID.
     *
     * @param policyUUID the policy UUID
     * @return PolicyTable
     */
    public PolicyTable findPoicyTableByPolicyUUID(final String policyUUID);

}
