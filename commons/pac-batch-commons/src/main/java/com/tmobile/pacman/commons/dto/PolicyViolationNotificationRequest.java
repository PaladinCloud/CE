package com.tmobile.pacman.commons.dto;

import com.tmobile.pacman.commons.utils.Constants;

public class PolicyViolationNotificationRequest {
    private String resourceId;
    private String resourceIdLink;
    private String issueId;
    private String issueIdLink;
    private String policyName;
    private String policyNameLink;
    private String description;
    private String scanTime;
    private Constants.Actions action;

    public String getScanTime() {
        return scanTime;
    }

    public void setScanTime(String scanTime) {
        this.scanTime = scanTime;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
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

    public String getPolicyNameLink() {
        return policyNameLink;
    }

    public void setPolicyNameLink(String policyNameLink) {
        this.policyNameLink = policyNameLink;
    }

    public Constants.Actions getAction() {
        return action;
    }

    public void setAction(Constants.Actions action) {
        this.action = action;
    }
}
