package com.tmobile.pacbot.azure.inventory.vo;

import com.microsoft.azure.management.storage.StorageAccount;

import java.util.List;
import java.util.Map;

public class SubscriptionVH extends AzureVH {

    private String subscriptionId;
    private String subscriptionName;
    private String tenant;
    private List<StorageAccount> storageAccount;
    private List<StorageAccountActivityLogVH> storageAccountLogList;
    private List<RoleDefinitionVH> roleDefinitionList;
    private Map<String, String> regions;

    @Override
    public String toString() {
        return "{ subscriptionName=" + subscriptionName + ", subscriptionId=" + subscriptionId + ", tenant=" + tenant + "}";
    }

    public Map<String, String> getRegions() {
        return regions;
    }

    public void setRegions(Map<String, String> regions) {
        this.regions = regions;
    }

    public String getTenant() {
        return tenant;
    }

    public void setTenant(String tenant) {
        this.tenant = tenant;
    }

    public String getSubscriptionId() {
        return subscriptionId;
    }

    public void setSubscriptionId(String subscription) {
        this.subscriptionId = subscription;
    }

    @Override
    public String getSubscriptionName() {
        return subscriptionName;
    }

    @Override
    public void setSubscriptionName(String subscriptionName) {
        this.subscriptionName = subscriptionName;
    }

    public List<StorageAccount> getStorageAccount() {
        return storageAccount;
    }

    public void setStorageAccount(List<StorageAccount> storageAccount) {
        this.storageAccount = storageAccount;
    }

    public List<StorageAccountActivityLogVH> getStorageAccountLogList() {
        return storageAccountLogList;
    }

    public void setStorageAccountLogList(List<StorageAccountActivityLogVH> storageAccountLogList) {
        this.storageAccountLogList = storageAccountLogList;
    }

    public List<RoleDefinitionVH> getRoleDefinitionList() {
        return roleDefinitionList;
    }

    public void setRoleDefinitionList(List<RoleDefinitionVH> roleDefinitionList) {
        this.roleDefinitionList = roleDefinitionList;
    }


}
