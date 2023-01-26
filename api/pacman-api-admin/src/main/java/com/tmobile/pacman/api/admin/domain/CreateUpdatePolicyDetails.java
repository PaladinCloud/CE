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

/**
 * CreateUpdatePolicyDetails Domain Class
 */
public class CreateUpdatePolicyDetails {

	private String policyId;
	private String policyUUID;
	private String policyName;
	private String policyDisplayName;
	private String policyDesc;
	private String resolution;
	private String resolutionUrl;
	private String targetType;
	private String assetGroup;
	private String alexaKeyword;
	private String policyParams;
	private String policyFrequency;
	private String policyExecutable;
	private String policyRestUrl;
	private String policyType;
	private String policyArn;
	private String status;
	private String userId;
	private String dataSource;
	private Boolean isFileChanged;
	private String  isAutofixEnabled;
	private String severity;
	private String category;
	public String getPolicyId() {
		return policyId;
	}
	public void setPolicyId(String policyId) {
		this.policyId = policyId;
	}
	public String getPolicyUUID() {
		return policyUUID;
	}
	public void setPolicyUUID(String policyUUID) {
		this.policyUUID = policyUUID;
	}
	public String getPolicyName() {
		return policyName;
	}
	public void setPolicyName(String policyName) {
		this.policyName = policyName;
	}
	public String getPolicyDisplayName() {
		return policyDisplayName;
	}
	public void setPolicyDisplayName(String policyDisplayName) {
		this.policyDisplayName = policyDisplayName;
	}
	public String getPolicyDesc() {
		return policyDesc;
	}
	public void setPolicyDesc(String policyDesc) {
		this.policyDesc = policyDesc;
	}
	public String getResolution() {
		return resolution;
	}
	public void setResolution(String resolution) {
		this.resolution = resolution;
	}
	public String getResolutionUrl() {
		return resolutionUrl;
	}
	public void setResolutionUrl(String resolutionUrl) {
		this.resolutionUrl = resolutionUrl;
	}
	public String getTargetType() {
		return targetType;
	}
	public void setTargetType(String targetType) {
		this.targetType = targetType;
	}
	public String getAssetGroup() {
		return assetGroup;
	}
	public void setAssetGroup(String assetGroup) {
		this.assetGroup = assetGroup;
	}
	public String getAlexaKeyword() {
		return alexaKeyword;
	}
	public void setAlexaKeyword(String alexaKeyword) {
		this.alexaKeyword = alexaKeyword;
	}
	public String getPolicyParams() {
		return policyParams;
	}
	public void setPolicyParams(String policyParams) {
		this.policyParams = policyParams;
	}
	public String getPolicyFrequency() {
		return policyFrequency;
	}
	public void setPolicyFrequency(String policyFrequency) {
		this.policyFrequency = policyFrequency;
	}
	public String getPolicyExecutable() {
		return policyExecutable;
	}
	public void setPolicyExecutable(String policyExecutable) {
		this.policyExecutable = policyExecutable;
	}
	public String getPolicyRestUrl() {
		return policyRestUrl;
	}
	public void setPolicyRestUrl(String policyRestUrl) {
		this.policyRestUrl = policyRestUrl;
	}
	public String getPolicyType() {
		return policyType;
	}
	public void setPolicyType(String policyType) {
		this.policyType = policyType;
	}
	public String getPolicyArn() {
		return policyArn;
	}
	public void setPolicyArn(String policyArn) {
		this.policyArn = policyArn;
	}
	public String getStatus() {
		return status;
	}
	public void setStatus(String status) {
		this.status = status;
	}
	public String getUserId() {
		return userId;
	}
	public void setUserId(String userId) {
		this.userId = userId;
	}
	public String getDataSource() {
		return dataSource;
	}
	public void setDataSource(String dataSource) {
		this.dataSource = dataSource;
	}
	public Boolean getIsFileChanged() {
		return isFileChanged;
	}
	public void setIsFileChanged(Boolean isFileChanged) {
		this.isFileChanged = isFileChanged;
	}
	public String getIsAutofixEnabled() {
		return isAutofixEnabled;
	}
	public void setIsAutofixEnabled(String isAutofixEnabled) {
		this.isAutofixEnabled = isAutofixEnabled;
	}
	public String getSeverity() {
		return severity;
	}
	public void setSeverity(String severity) {
		this.severity = severity;
	}
	public String getCategory() {
		return category;
	}
	public void setCategory(String category) {
		this.category = category;
	}

	

}
