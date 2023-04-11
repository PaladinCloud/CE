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

import com.tmobile.pacman.api.admin.repository.model.AssetGroupCriteriaDetails;

import java.util.List;
import java.util.Set;

/**
 * UpdateAssetGroupDetails Domain Class
 */
public class UpdateAssetGroupDetails extends CreateUpdateAssetGroupDetails {

	private String groupId;
	private List<TargetTypesProjection> remainingTargetTypes;
	private List<TargetTypesDetails> remainingTargetTypesFullDetails;

	private Long assetCount;

	private Set<AssetGroupCriteriaDetails> criteriaDetails;

	private String groupType;

	public String getGroupType() {
		return groupType;
	}

	public void setGroupType(String groupType) {
		this.groupType = groupType;
	}

	public Set<AssetGroupCriteriaDetails> getCriteriaDetails() {
		return criteriaDetails;
	}

	public void setCriteriaDetails(Set<AssetGroupCriteriaDetails> criteriaDetails) {
		this.criteriaDetails = criteriaDetails;
	}



	public void setAssetCount(Long assetCount) {
		this.assetCount = assetCount;
	}

	public Long getAssetCount() {
		return assetCount;
	}

	public String getGroupId() {
		return groupId;
	}

	public void setGroupId(String groupId) {
		this.groupId = groupId;
	}

	public List<TargetTypesProjection> getRemainingTargetTypes() {
		return remainingTargetTypes;
	}

	public void setRemainingTargetTypes(List<TargetTypesProjection> remainingTargetTypes) {
		this.remainingTargetTypes = remainingTargetTypes;
	}

	public List<TargetTypesDetails> getRemainingTargetTypesFullDetails() {
		return remainingTargetTypesFullDetails;
	}

	public void setRemainingTargetTypesFullDetails(List<TargetTypesDetails> remainingTargetTypesFullDetails) {
		this.remainingTargetTypesFullDetails = remainingTargetTypesFullDetails;
	}
}
