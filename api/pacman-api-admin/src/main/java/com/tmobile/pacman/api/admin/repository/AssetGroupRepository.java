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
package com.tmobile.pacman.api.admin.repository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

import com.tmobile.pacman.api.admin.common.AdminConstants;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.tmobile.pacman.api.admin.repository.model.AssetGroupDetails;

/**
 * AssetGroup Repository Interface
 */
@Repository
public interface AssetGroupRepository extends JpaRepository<AssetGroupDetails, String> {

	/**
     * AssetGroup Repository function for to get all assetGroup names
     *
     * @author Nidhish
     * @return All AssetGroup Name list
     */
	@Query("SELECT groupName FROM AssetGroupDetails WHERE groupName != '' AND groupName != null GROUP BY groupName")
	public Collection<String> getAllAssetGroupNames();

	/**
     * AssetGroup Repository function for to get all assetGroup names
	 * removed stakeholder asset groups by removing user groupTypes from list
     *
     * @author NKrishn3
     * @param searchTerm - searchTerm to be searched.
     * @param pageRequest - pagination information 
     * @return All AssetGroup Details
     */
	@Query(value = "SELECT ag FROM AssetGroupDetails ag WHERE LOWER(ag.groupType) <> 'user' AND (LOWER(ag.groupId) LIKE %:searchTerm% OR LOWER(ag.groupName) LIKE %:searchTerm% OR LOWER(ag.dataSource) LIKE %:searchTerm% OR LOWER(ag.displayName) LIKE %:searchTerm% OR LOWER(ag.groupType) LIKE %:searchTerm% OR LOWER(ag.createdBy) LIKE %:searchTerm% OR LOWER(ag.createdUser) LIKE %:searchTerm% OR LOWER(ag.createdDate) LIKE %:searchTerm% OR LOWER(ag.modifiedUser) LIKE %:searchTerm% OR LOWER(ag.modifiedDate) LIKE %:searchTerm% OR LOWER(ag.description) LIKE %:searchTerm% OR LOWER(ag.aliasQuery) LIKE %:searchTerm%) GROUP BY ag.groupId",
	countQuery = "SELECT COUNT(*) FROM AssetGroupDetails ag WHERE LOWER(ag.groupType) <> 'user' AND (LOWER(ag.groupId) LIKE %:searchTerm% OR LOWER(ag.groupName) LIKE %:searchTerm% OR LOWER(ag.dataSource) LIKE %:searchTerm% OR LOWER(ag.displayName) LIKE %:searchTerm% OR LOWER(ag.groupType) LIKE %:searchTerm% OR LOWER(ag.createdBy) LIKE %:searchTerm% OR LOWER(ag.createdUser) LIKE %:searchTerm% OR LOWER(ag.createdDate) LIKE %:searchTerm% OR LOWER(ag.modifiedUser) LIKE %:searchTerm% OR LOWER(ag.modifiedDate) LIKE %:searchTerm% OR LOWER(ag.description) LIKE %:searchTerm% OR LOWER(ag.aliasQuery) LIKE %:searchTerm%) GROUP BY ag.groupId")
	public Page<AssetGroupDetails> findAll(@Param("searchTerm") String searchTerm, Pageable pageable);

	/**
     * AssetGroup Repository function for to get all assetGroup by name
     *
     * @author Nidhish
     * @param groupName - valid asset group name.
     * @return Asset Group Detail/s
     */
	public AssetGroupDetails findByGroupName(String groupName);

	public AssetGroupDetails findByGroupId(String groupId);

	/**
     * AssetGroup Repository function to fetch stakeholder group name by createdBy
     *
     * @param createdBy - user that created stakeholder.
     * @return group name of stakeholder asset group
     */
	@Query(value = "SELECT groupName FROM cf_AssetGroupDetails WHERE createdBy = ?1 AND groupType = '" +
			AdminConstants.STAKEHOLDER_GROUP_TYPE + "' and groupName like 'sh-%' limit 1", nativeQuery = true)
	Optional<String> findGroupNameByCreatedByAndGroupTypeAsStakeholder(String createdBy);

	/**
	 * @param attributeName  attributeName
	 * @param attributeValue attributeValue
	 * @param groupType      stakeholder groupType
	 * @return ownerDetails
	 */
	@Query(value = "select distinct ow.ownnerName as name, ow.ownerEmailId as email from cf_AssetGroupDetails ag" +
			" join (select * from cf_AssetGroupCriteriaDetails where attributeName = ?1 and " +
			" attributeValue in (?2)) cd on ag.groupId = cd.groupId" +
			" join cf_AssetGroupOwnerDetails ow on ag.groupName = ow.assetGroupName" +
			" where ag.groupType = ?3", nativeQuery = true)
	List<Object> findAllStakeholderByTagName(String attributeName, List<String> attributeValue, String groupType);

	@Query("SELECT distinct groupType FROM AssetGroupDetails")
	public List<String> getDistinctType();

	@Query("SELECT distinct createdBy FROM AssetGroupDetails")
	public List<String> getDistinctCreatedBy();
}
