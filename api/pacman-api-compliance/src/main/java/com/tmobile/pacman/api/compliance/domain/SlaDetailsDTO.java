package com.tmobile.pacman.api.compliance.domain;

public class SlaDetailsDTO {
    private String slaId;
    private String description;
    private String userId;
    private String remediationTimeUnit;
    private Long remediationTime;
    public String getSlaId() {
        return slaId;
    }

    public void setSlaId(String slaId) {
        this.slaId = slaId;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getRemediationTimeUnit() {
        return remediationTimeUnit;
    }

    public void setRemediationTimeUnit(String remediationTimeUnit) {
        this.remediationTimeUnit = remediationTimeUnit;
    }

    public Long getRemediationTime() {
        return remediationTime;
    }

    public void setRemediationTime(Long remediationTime) {
        this.remediationTime = remediationTime;
    }
}
