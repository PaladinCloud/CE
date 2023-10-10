package com.tmobile.cso.pacman.datashipper.dto;

import java.util.List;
import java.util.Map;

public class DatasourceData {

    private List<String> accountIds;

    private List<String> assetGroups;

    public List<String> getAccountIds() {
        return accountIds;
    }

    public void setAccountIds(List<String> accountIds) {
        this.accountIds = accountIds;
    }

    public List<String> getAssetGroups() {
        return assetGroups;
    }

    public void setAssetGroups(List<String> assetGroups) {
        this.assetGroups = assetGroups;
    }
}
