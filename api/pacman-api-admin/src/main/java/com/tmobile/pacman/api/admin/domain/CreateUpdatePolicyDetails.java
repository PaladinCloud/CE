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
	private String  autofixEnabled;
	private String severity;
	private String category;
	private String autoFixAvailable;
	private String allowList;
	private Integer waitingTime;
	private Integer maxEmailNotification;
	private String templateName;
	private String templateColumns;
	private String fixType;
	private String warningMailSubject;
	private String fixMailSubject;
	private String warningMessage;
	private String fixMessage;
	private String violationMessage;
	private Integer elapsedTime;
	
	
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
	public String getAutofixEnabled() {
		return autofixEnabled;
	}
	public void setAutofixEnabled(String autofixEnabled) {
		this.autofixEnabled = autofixEnabled;
	}
	public String getAutoFixAvailable() {
		return autoFixAvailable;
	}
	public void setAutoFixAvailable(String autoFixAvailable) {
		this.autoFixAvailable = autoFixAvailable;
	}
	public String getAllowList() {
		return allowList;
	}
	public void setAllowList(String allowList) {
		this.allowList = allowList;
	}
	public Integer getWaitingTime() {
		return waitingTime;
	}
	public void setWaitingTime(Integer waitingTime) {
		this.waitingTime = waitingTime;
	}
	public Integer getMaxEmailNotification() {
		return maxEmailNotification;
	}
	public void setMaxEmailNotification(Integer maxEmailNotification) {
		this.maxEmailNotification = maxEmailNotification;
	}
	public String getTemplateName() {
		return templateName;
	}
	public void setTemplateName(String templateName) {
		this.templateName = templateName;
	}
	public String getTemplateColumns() {
		return templateColumns;
	}
	public void setTemplateColumns(String templateColumns) {
		this.templateColumns = templateColumns;
	}
	public String getFixType() {
		return fixType;
	}
	public void setFixType(String fixType) {
		this.fixType = fixType;
	}
	public String getWarningMailSubject() {
		return warningMailSubject;
	}
	public void setWarningMailSubject(String warningMailSubject) {
		this.warningMailSubject = warningMailSubject;
	}
	public String getFixMailSubject() {
		return fixMailSubject;
	}
	public void setFixMailSubject(String fixMailSubject) {
		this.fixMailSubject = fixMailSubject;
	}
	public String getWarningMessage() {
		return warningMessage;
	}
	public void setWarningMessage(String warningMessage) {
		this.warningMessage = warningMessage;
	}
	public String getFixMessage() {
		return fixMessage;
	}
	public void setFixMessage(String fixMessage) {
		this.fixMessage = fixMessage;
	}
	public String getViolationMessage() {
		return violationMessage;
	}
	public void setViolationMessage(String violationMessage) {
		this.violationMessage = violationMessage;
	}
	public Integer getElapsedTime() {
		return elapsedTime;
	}
	public void setElapsedTime(Integer elapsedTime) {
		this.elapsedTime = elapsedTime;
	}
	
	

}
