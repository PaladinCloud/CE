package com.tmobile.pacman.api.admin.repository.model;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;

/**
 * Policy Model Class
 */
@Entity
@Table(name = "cf_PolicyTable", uniqueConstraints = @UniqueConstraint(columnNames = "policyId"))
public class Policy {

	@Id
	@Column(name = "policyId", unique = true, nullable = false)
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
	private Date createdDate;
	private Date modifiedDate;
	private String severity;
	private String category;
	private String autoFixEnabled;
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
	public Date getCreatedDate() {
		return createdDate;
	}
	public void setCreatedDate(Date createdDate) {
		this.createdDate = createdDate;
	}
	public Date getModifiedDate() {
		return modifiedDate;
	}
	public void setModifiedDate(Date modifiedDate) {
		this.modifiedDate = modifiedDate;
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
	public String getAutoFixEnabled() {
		return autoFixEnabled;
	}
	public void setAutoFixEnabled(String autoFixEnabled) {
		this.autoFixEnabled = autoFixEnabled;
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
