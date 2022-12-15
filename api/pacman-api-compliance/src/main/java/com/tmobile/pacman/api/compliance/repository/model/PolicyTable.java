package com.tmobile.pacman.api.compliance.repository.model;

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
public class PolicyTable {

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

	
}
