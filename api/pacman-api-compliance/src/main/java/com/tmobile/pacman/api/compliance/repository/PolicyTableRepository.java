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

import com.tmobile.pacman.api.compliance.repository.model.PolicyTable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;
import java.util.Optional;

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
    public List<PolicyTable> findPoicyTableByAssetGroup(String assetGroup);


    @Query(value = "select policyId,policyDisplayName,policyUUID,policyName,policyType,assetGroup,assetGroup as 'pac_ds',targetType,severity,p.category,"
            + " p.category as 'policyCategory',autoFixEnabled as 'autofix',"
            + " case when autoFixEnabled = 'true' then CONVERT(maxEmailNotification, SIGNED)  else 1 end as maxEmailNotification, "
            + " case when autoFixEnabled = 'true' then CONVERT(waitingTime, SIGNED)  else 24 end as waitingTime, "
            + " case when autoFixEnabled = 'true' then CONVERT(elapsedTime, SIGNED)  else 24 end as elapsedTime,"
            + " p.status,riskScore, t.displayName as 'targetTypeDisplayName' "
            + " from cf_PolicyTable p join cf_Target t on p.targetType=t.targetName  where assetgroup=:source and targettype=:targetType and enricherSource is null and policyType <> 'External';", nativeQuery = true)
    public List<Map<String, String>> findPolicyBySourceAndTargetType(final String source, final String targetType);

    @Query(value = "select policyId,policyDisplayName,policyUUID,policyName,policyType,assetGroup,assetGroup as 'pac_ds',targetType,severity,p.category,"
            + " p.category as 'policyCategory',autoFixEnabled as 'autofix',"
            + " case when autoFixEnabled = 'true' then CONVERT(maxEmailNotification, SIGNED)  else 1 end as maxEmailNotification, "
            + " case when autoFixEnabled = 'true' then CONVERT(waitingTime, SIGNED)  else 24 end as waitingTime, "
            + " case when autoFixEnabled = 'true' then CONVERT(elapsedTime, SIGNED)  else 24 end as elapsedTime,"
            + " p.status,riskScore, t.displayName as 'targetTypeDisplayName' "
            + " from cf_PolicyTable p join cf_Target t on p.targetType=t.targetName  where assetgroup=:source and targettype=:targetType and enricherSource= :enricherSource and policyType <> 'External' ;", nativeQuery = true)
    public List<Map<String, String>> findPolicyBySourceAndTargetTypeAndEnricherSource(final String source, final String targetType, final String enricherSource);

    @Query(value = "select policyId,policyDisplayName,policyUUID,policyName,policyType,assetGroup,assetGroup as 'pac_ds',targetType,severity,p.category,"
            + " p.category as 'policyCategory',autoFixEnabled as 'autofix',"
            + " case when autoFixEnabled = 'true' then CONVERT(maxEmailNotification, SIGNED)  else 1 end as maxEmailNotification, "
            + " case when autoFixEnabled = 'true' then CONVERT(waitingTime, SIGNED)  else 24 end as waitingTime, "
            + " case when autoFixEnabled = 'true' then CONVERT(elapsedTime, SIGNED)  else 24 end as elapsedTime,"
            + " p.status,riskScore, t.displayName as 'targetTypeDisplayName' "
            + " from cf_PolicyTable p join cf_Target t on p.targetType=t.targetName  where  policyType <> 'External' and policyUUID IN  :policyUUIDs  ;", nativeQuery = true)
    public List<Map<String, String>> findPolicyPolicyUUIDs(final List<String> policyUUIDs);

    Optional<PolicyTable> findByPolicyUUID(String policyUUID);

    List<PolicyTable> findByAssetGroup(String assetGroup);

    List<PolicyTable> findByEnricherSource(String enricherSource);
}
