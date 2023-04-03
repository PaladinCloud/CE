package com.tmobile.pacman.dto;

import com.tmobile.pacman.common.AutoFixAction;

public class AutofixNotificationRequest {
    private String policyName;
    private String discoveredOn;
    private String autofixedOn;
    private String waitingTime;
    private String resourceId;
    private String severity;
    private AutoFixAction action;
    private String issueId;

    private String policyNameLink;
    private String issueIdLink;
    private String resourceIdLink;

    public String getPolicyNameLink() {
        return policyNameLink;
    }

    public void setPolicyNameLink(String policyNameLink) {
        this.policyNameLink = policyNameLink;
    }

    public String getIssueIdLink() {
        return issueIdLink;
    }

    public void setIssueIdLink(String issueIdLink) {
        this.issueIdLink = issueIdLink;
    }

    public String getResourceIdLink() {
        return resourceIdLink;
    }

    public void setResourceIdLink(String resourceIdLink) {
        this.resourceIdLink = resourceIdLink;
    }

    public AutofixNotificationRequest(){
    }

    public String getPolicyName() {
        return policyName;
    }

    public void setPolicyName(String policyName) {
        this.policyName = policyName;
    }

    public String getAutofixedOn() {
        return autofixedOn;
    }

    public void setAutofixedOn(String autofixedOn) {
        this.autofixedOn = autofixedOn;
    }

    public String getResourceId() {
        return resourceId;
    }

    public void setResourceId(String resourceId) {
        this.resourceId = resourceId;
    }

    public String getSeverity() {
        return severity;
    }

    public void setSeverity(String severity) {
        this.severity = severity;
    }

    public String getWaitingTime() {
        return waitingTime;
    }

    public void setWaitingTime(String waitingTime) {
        this.waitingTime = waitingTime;
    }

    public String getIssueId() {
        return issueId;
    }

    public void setIssueId(String issueId) {
        this.issueId = issueId;
    }

    public AutoFixAction getAction() {
        return action;
    }
    public void setAction(AutoFixAction action) {
        this.action = action;
    }

    public String getDiscoveredOn() {
        return discoveredOn;
    }

    public void setDiscoveredOn(String discoveredOn) {
        this.discoveredOn = discoveredOn;
    }

}
