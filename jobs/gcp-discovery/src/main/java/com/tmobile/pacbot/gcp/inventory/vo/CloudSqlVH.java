package com.tmobile.pacbot.gcp.inventory.vo;

import com.google.api.services.sqladmin.model.Settings;

import java.util.List;

public class CloudSqlVH extends GCPVH{
    private String name;
    private String kind;
    private String createdTime;
    private String masterInstanceName;
    private String backendType;
    private String state;
    private String databaseVersion;
    private String databaseInstalledVersion;
    private String instanceType;
    private String eTag;
    private String selfLink;
    private String serviceAccountEmail;
    private String kmsKeyVersion;
    private String kmsKeyName;
    private Long maxDiskSize;
    private Long currentDiskSize;
    private List<IPAddress> ipAddress;
    private ServerCaCert serverCaCert;
    private Settings settings;
    private Boolean backupEnabled;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getKind() {
        return kind;
    }

    public void setKind(String kind) {
        this.kind = kind;
    }

    public String getCreatedTime() {
        return createdTime;
    }

    public void setCreatedTime(String createdTime) {
        this.createdTime = createdTime;
    }

    public String getMasterInstanceName() {
        return masterInstanceName;
    }

    public void setMasterInstanceName(String masterInstanceName) {
        this.masterInstanceName = masterInstanceName;
    }

    public String getBackendType() {
        return backendType;
    }

    public void setBackendType(String backendType) {
        this.backendType = backendType;
    }

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }

    public String getDatabaseVersion() {
        return databaseVersion;
    }

    public void setDatabaseVersion(String databaseVersion) {
        this.databaseVersion = databaseVersion;
    }

    public String getDatabaseInstalledVersion() {
        return databaseInstalledVersion;
    }

    public void setDatabaseInstalledVersion(String databaseInstalledVersion) {
        this.databaseInstalledVersion = databaseInstalledVersion;
    }

    public String getInstanceType() {
        return instanceType;
    }

    public void setInstanceType(String instanceType) {
        this.instanceType = instanceType;
    }

    public String geteTag() {
        return eTag;
    }

    public void seteTag(String eTag) {
        this.eTag = eTag;
    }

    public String getSelfLink() {
        return selfLink;
    }

    public void setSelfLink(String selfLink) {
        this.selfLink = selfLink;
    }

    public String getServiceAccountEmail() {
        return serviceAccountEmail;
    }

    public void setServiceAccountEmail(String serviceAccountEmail) {
        this.serviceAccountEmail = serviceAccountEmail;
    }

    public String getKmsKeyVersion() {
        return kmsKeyVersion;
    }

    public void setKmsKeyVersion(String kmsKeyVersion) {
        this.kmsKeyVersion = kmsKeyVersion;
    }

    public String getKmsKeyName() {
        return kmsKeyName;
    }

    public void setKmsKeyName(String kmsKeyName) {
        this.kmsKeyName = kmsKeyName;
    }

    public Long getMaxDiskSize() {
        return maxDiskSize;
    }

    public void setMaxDiskSize(Long maxDiskSize) {
        this.maxDiskSize = maxDiskSize;
    }

    public Long getCurrentDiskSize() {
        return currentDiskSize;
    }

    public void setCurrentDiskSize(Long currentDiskSize) {
        this.currentDiskSize = currentDiskSize;
    }

    public List<IPAddress> getIpAddress() {
        return ipAddress;
    }

    public void setIpAddress(List<IPAddress> ipAddress) {
        this.ipAddress = ipAddress;
    }

    public ServerCaCert getServerCaCert() {
        return serverCaCert;
    }

    public void setServerCaCert(ServerCaCert serverCaCert) {
        this.serverCaCert = serverCaCert;
    }

    public Settings getSettings() {
        return settings;
    }

    public void setSettings(Settings settings) {
        this.settings = settings;
    }

    public Boolean getBackupEnabled() {
        return backupEnabled;
    }

    public void setBackupEnabled(Boolean backupEnabled) {
        this.backupEnabled = backupEnabled;
    }

}
