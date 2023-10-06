package com.tmobile.cso.pacman.datashipper.dto;

import java.util.List;
import java.util.Map;

public class DatasourceData {

    private List<String> accountIds;

    private Map<String, List<String>> assetGroupDomains;

    public List<String> getAccountIds() {
        return accountIds;
    }

    public void setAccountIds(List<String> accountIds) {
        this.accountIds = accountIds;
    }

    public Map<String, List<String>> getAssetGroupDomains() {
        return assetGroupDomains;
    }

    public void setAssetGroupDomains(Map<String, List<String>> assetGroupDomains) {
        this.assetGroupDomains = assetGroupDomains;
    }
}
