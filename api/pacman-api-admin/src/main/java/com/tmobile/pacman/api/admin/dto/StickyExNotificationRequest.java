package com.tmobile.pacman.api.admin.dto;

import com.tmobile.pacman.api.commons.dto.ExemptionCommonDetails;

public class StickyExNotificationRequest extends ExemptionCommonDetails {

    private String assetGroup;
    private String exceptionName;
    private String exceptionReason;
    private String expiringOn;
    private String policyNames;

    private String userId;

    public String getAssetGroup() {
        return assetGroup;
    }

    public void setAssetGroup(String assetGroup) {
        this.assetGroup = assetGroup;
    }

    public String getExceptionName() {
        return exceptionName;
    }

    public void setExceptionName(String exceptionName) {
        this.exceptionName = exceptionName;
    }

    public String getExceptionReason() {
        return exceptionReason;
    }

    public void setExceptionReason(String exceptionReason) {
        this.exceptionReason = exceptionReason;
    }

    public String getExpiringOn() {
        return expiringOn;
    }

    public void setExpiringOn(String expiringOn) {
        this.expiringOn = expiringOn;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getPolicyNames() {
        return policyNames;
    }

    public void setPolicyNames(String policyNames) {
        this.policyNames = policyNames;
    }
}
