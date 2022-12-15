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

import java.util.List;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import com.tmobile.pacman.api.compliance.repository.model.PacPolicyEngineAutofixActions;
import com.tmobile.pacman.api.compliance.repository.model.PacPolicyEngineAutofixActionsIdentity;

/**
 * The Interface PacPolicyEngineAutofixActionsRepository.
 */
@Repository
public interface PacPolicyEngineAutofixActionsRepository
        extends
        CrudRepository<PacPolicyEngineAutofixActions, PacPolicyEngineAutofixActionsIdentity> {

    /**
     * Find last action by resource id.
     *
     * @param resourceId the resource id
     * @return the list
     */
    public List<PacPolicyEngineAutofixActions> findLastActionByResourceId(
            final String resourceId);
}
