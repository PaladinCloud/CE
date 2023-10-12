package com.tmobile.pacman.commons.dto;

import java.util.List;
import java.util.Map;

public class PermissionVH {
    Map<String, List<String>> assetPermissionIssues;
    String accountNumber;


    public Map<String, List<String>> getAssetPermissionIssues() {
        return assetPermissionIssues;
    }

    public void setAssetPermissionIssues(Map<String, List<String>> assetPermissionIssues) {
        this.assetPermissionIssues = assetPermissionIssues;
    }

    public String getAccountNumber() {
        return accountNumber;
    }

    public void setAccountNumber(String accountNumber) {
        this.accountNumber = accountNumber;
    }
}
