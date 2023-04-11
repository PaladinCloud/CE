package com.tmobile.pacman.api.compliance.dto;

import com.tmobile.pacman.api.commons.dto.ExemptionCommonDetails;

import java.util.HashMap;
import java.util.Map;

public class IndividualExNotificationRequest extends ExemptionCommonDetails {
    private String resourceId;
    private String resourceIdLink;
    private String issueId;
    private String issueIdLink;
    private String policyName;
    private String exemptionReason;
    private String exemptionGrantedDate;
    private String exemptionExpiringOn;
    private String createdBy;
    private String policyNameLink;

    private Map<String,Object> additionalInfo=new HashMap<>();

    public Map<String, Object> getAdditionalInfo() {
        return additionalInfo;
    }

    public void setAdditionalInfo(Map<String, Object> additionalInfo) {
        this.additionalInfo = additionalInfo;
    }

    public String getPolicyNameLink() {
        return policyNameLink;
    }

    public void setPolicyNameLink(String policyNameLink) {
        this.policyNameLink = policyNameLink;
    }
    public String getResourceId() {
        return resourceId;
    }

    public void setResourceId(String resourceId) {
        this.resourceId = resourceId;
    }

    public String getResourceIdLink() {
        return resourceIdLink;
    }

    public void setResourceIdLink(String resourceIdLink) {
        this.resourceIdLink = resourceIdLink;
    }

    public String getIssueId() {
        return issueId;
    }

    public void setIssueId(String issueId) {
        this.issueId = issueId;
    }

    public String getIssueIdLink() {
        return issueIdLink;
    }

    public void setIssueIdLink(String issueIdLink) {
        this.issueIdLink = issueIdLink;
    }

    public String getPolicyName() {
        return policyName;
    }

    public void setPolicyName(String policyName) {
        this.policyName = policyName;
    }

    public String getExemptionReason() {
        return exemptionReason;
    }

    public void setExemptionReason(String exemptionReason) {
        this.exemptionReason = exemptionReason;
    }

    public String getExemptionGrantedDate() {
        return exemptionGrantedDate;
    }

    public void setExemptionGrantedDate(String exemptionGrantedDate) {
        this.exemptionGrantedDate = exemptionGrantedDate;
    }

    public String getExemptionExpiringOn() {
        return exemptionExpiringOn;
    }

    public void setExemptionExpiringOn(String exemptionExpiringOn) {
        this.exemptionExpiringOn = exemptionExpiringOn;
    }

    public String getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(String createdBy) {
        this.createdBy = createdBy;
    }
}
