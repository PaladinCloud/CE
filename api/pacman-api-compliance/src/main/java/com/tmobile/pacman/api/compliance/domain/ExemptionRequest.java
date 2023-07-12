package com.tmobile.pacman.api.compliance.domain;

import com.tmobile.pacman.api.commons.Constants;

public class ExemptionRequest extends IssuesException {

    private String assetGroup;

    private Constants.ExemptionActions action;

    private String approvedBy;

    public String getAssetGroup() {
        return assetGroup;
    }

    public void setAssetGroup(String assetGroup) {
        this.assetGroup = assetGroup;
    }

    public Constants.ExemptionActions getAction() {
        return action;
    }

    public void setAction(Constants.ExemptionActions action) {
        this.action = action;
    }

    public String getApprovedBy() {
        return approvedBy;
    }

    public void setApprovedBy(String approvedBy) {
        this.approvedBy = approvedBy;
    }
}
