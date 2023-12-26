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
package com.tmobile.pacman.api.admin.domain;

import java.util.Set;
import java.sql.Timestamp;

import com.tmobile.pacman.api.admin.repository.model.AssetGroupCriteriaDetails;
import com.tmobile.pacman.api.admin.repository.model.AssetGroupDetails;
import com.tmobile.pacman.api.admin.repository.model.AssetGroupTargetDetails;

/**
 * AssetGroupView Domain Class
 */
public class AssetGroupView {

	private String groupId;
	private String groupName;
	private String displayName;
	private String description;
	private String type;

	private String createdBy;

	private Long assetCount;

	private String groupType;

	public String getGroupType() {
		return groupType;
	}

	public Timestamp getCreatedDate() {
		return createdDate;
	}

	public void setCreatedDate(Timestamp createdDate) {
		this.createdDate = createdDate;
	}

	public Timestamp getUpdatedDate() {
		return updatedDate;
	}

	public void setUpdatedDate(Timestamp updatedDate) {
		this.updatedDate = updatedDate;
	}

	private Timestamp createdDate;
	private Timestamp updatedDate;

	public void setGroupType(String groupType) {
		this.groupType = groupType;
	}

	public void setDisplayName(String displayName) {
		this.displayName = displayName;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public String getDisplayName() {
		return displayName;
	}

	public String getDescription() {
		return description;
	}

	private Set<AssetGroupCriteriaDetails> criteriaDetails;

	private Set<AssetGroupTargetDetails> targetTypes;

	public Set<AssetGroupTargetDetails> getTargetTypes() {
		return targetTypes;
	}

	public void setTargetTypes(Set<AssetGroupTargetDetails> targetTypes) {
		this.targetTypes = targetTypes;
	}

	public String getCreatedBy() {
		return createdBy;
	}

	public void setCreatedBy(String createdBy) {
		this.createdBy = createdBy;
	}

	public Long getAssetCount() {
		return assetCount;
	}

	public void setAssetCount(Long assetCount) {
		this.assetCount = assetCount;
	}

	public AssetGroupView() {
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}


	public Set<AssetGroupCriteriaDetails> getCriteriaDetails() {
		return criteriaDetails;
	}

	public void setCriteriaDetails(Set<AssetGroupCriteriaDetails> criteriaDetails) {
		this.criteriaDetails = criteriaDetails;
	}

	public AssetGroupView(AssetGroupDetails assetGroupDetails) {
		this.groupId = assetGroupDetails.getGroupId();
		this.groupName = assetGroupDetails.getGroupName();
	}

	public String getGroupId() {
		return groupId;
	}

	public String getGroupName() {
		return groupName;
	}

	public void setGroupId(String groupId) {
		this.groupId = groupId;
	}

	public void setGroupName(String groupName) {
		this.groupName = groupName;
	}
}
