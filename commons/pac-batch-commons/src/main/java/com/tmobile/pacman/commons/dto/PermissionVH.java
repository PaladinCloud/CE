package com.tmobile.pacman.commons.dto;

import java.util.List;
import java.util.Map;

public class PermissionVH {
    Map<String, List<String>> assetPermissionIssue;
    String accountNumber;


    public Map<String, List<String>> getAssetPermissionIssue() {
        return assetPermissionIssue;
    }

    public void setAssetPermissionIssue(Map<String, List<String>> assetPermissionIssue) {
        this.assetPermissionIssue = assetPermissionIssue;
    }

    public String getAccountNumber() {
        return accountNumber;
    }

    public void setAccountNumber(String accountNumber) {
        this.accountNumber = accountNumber;
    }
}
