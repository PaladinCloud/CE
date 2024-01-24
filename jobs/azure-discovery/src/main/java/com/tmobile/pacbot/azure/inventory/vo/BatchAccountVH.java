package com.tmobile.pacbot.azure.inventory.vo;

import java.util.Map;

public class BatchAccountVH extends AzureVH {

    private String name;
    private String type;
    private String location;
    private Map<String, Object> tags;
    private String provisioningState;
    private String accountEndpoint;
    private String poolQuota;
    private String dedicatedCoreQuotaPerVMFamily;
    private String poolAllocationMode;
    private String dedicatedCoreQuota;
    private String lowPriorityCoreQuota;
    private String activeJobAndJobScheduleQuota;
    private boolean dedicatedCoreQuotaPerVMFamilyEnforced;
    private Map<String, Object> autoStorage;


    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public Map<String, Object> getTags() {
        return tags;
    }

    public void setTags(Map<String, Object> tags) {
        this.tags = tags;
    }

    public String getProvisioningState() {
        return provisioningState;
    }

    public void setProvisioningState(String provisioningState) {
        this.provisioningState = provisioningState;
    }

    public String getAccountEndpoint() {
        return accountEndpoint;
    }

    public void setAccountEndpoint(String accountEndpoint) {
        this.accountEndpoint = accountEndpoint;
    }

    public String getPoolQuota() {
        return poolQuota;
    }

    public void setPoolQuota(String poolQuota) {
        this.poolQuota = poolQuota;
    }

    public String getDedicatedCoreQuotaPerVMFamily() {
        return dedicatedCoreQuotaPerVMFamily;
    }

    public void setDedicatedCoreQuotaPerVMFamily(String dedicatedCoreQuotaPerVMFamily) {
        this.dedicatedCoreQuotaPerVMFamily = dedicatedCoreQuotaPerVMFamily;
    }

    public String getPoolAllocationMode() {
        return poolAllocationMode;
    }

    public void setPoolAllocationMode(String poolAllocationMode) {
        this.poolAllocationMode = poolAllocationMode;
    }

    public String getDedicatedCoreQuota() {
        return dedicatedCoreQuota;
    }

    public void setDedicatedCoreQuota(String dedicatedCoreQuota) {
        this.dedicatedCoreQuota = dedicatedCoreQuota;
    }

    public String getLowPriorityCoreQuota() {
        return lowPriorityCoreQuota;
    }

    public void setLowPriorityCoreQuota(String lowPriorityCoreQuota) {
        this.lowPriorityCoreQuota = lowPriorityCoreQuota;
    }

    public String getActiveJobAndJobScheduleQuota() {
        return activeJobAndJobScheduleQuota;
    }

    public void setActiveJobAndJobScheduleQuota(String activeJobAndJobScheduleQuota) {
        this.activeJobAndJobScheduleQuota = activeJobAndJobScheduleQuota;
    }

    public boolean isDedicatedCoreQuotaPerVMFamilyEnforced() {
        return dedicatedCoreQuotaPerVMFamilyEnforced;
    }

    public void setDedicatedCoreQuotaPerVMFamilyEnforced(boolean dedicatedCoreQuotaPerVMFamilyEnforced) {
        this.dedicatedCoreQuotaPerVMFamilyEnforced = dedicatedCoreQuotaPerVMFamilyEnforced;
    }

    public Map<String, Object> getAutoStorage() {
        return autoStorage;
    }

    public void setAutoStorage(Map<String, Object> autoStorage) {
        this.autoStorage = autoStorage;
    }

}
