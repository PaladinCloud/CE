package com.tmobile.pacbot.azure.inventory.vo;

import java.util.List;
import java.util.Map;
import java.util.Set;

public class VaultVH extends AzureVH {
    private String name;
    private String type;
    private String location;
    private Map<String, Object> tags;
    private Map<String, Object> sku;
    private boolean enabledForDeployment;
    private boolean enabledForDiskEncryption;
    private boolean enabledForTemplateDeployment;
    private String tenantId;
    private String provisioningState;
    private String vaultUri;
    private List<String> permissionForKeys;
    private List<String> permissionForSecrets;
    private List<String> permissionForCertificates;
    private Set<String> secretExpirationDate;

    private boolean enableRbacAuthorization;
    private Set<String> keyExpirationDate;
    private boolean enablePurgeProtection;
    private boolean enableSoftDelete;

    public boolean isEnableRbacAuthorization() {
        return enableRbacAuthorization;
    }

    public void setEnableRbacAuthorization(boolean enableRbacAuthorization) {
        this.enableRbacAuthorization = enableRbacAuthorization;
    }

    public Set<String> getSecretExpirationDate() {
        return secretExpirationDate;
    }

    public void setSecretExpirationDate(Set<String> secretExpirationDate) {
        this.secretExpirationDate = secretExpirationDate;
    }

    public Set<String> getKeyExpirationDate() {
        return keyExpirationDate;
    }

    public void setKeyExpirationDate(Set<String> keyExpirationDate) {
        this.keyExpirationDate = keyExpirationDate;
    }

    public Map<String, Object> getSku() {
        return sku;
    }

    public void setSku(Map<String, Object> sku) {
        this.sku = sku;
    }

    public boolean isEnablePurgeProtection() {
        return enablePurgeProtection;
    }

    public void setEnablePurgeProtection(boolean enablePurgeProtection) {
        this.enablePurgeProtection = enablePurgeProtection;
    }

    public boolean isEnableSoftDelete() {
        return enableSoftDelete;
    }

    public void setEnableSoftDelete(boolean enableSoftDelete) {
        this.enableSoftDelete = enableSoftDelete;
    }

    public List<String> getPermissionForKeys() {
        return permissionForKeys;
    }

    public void setPermissionForKeys(List<String> permissionForKeys) {
        this.permissionForKeys = permissionForKeys;
    }

    public List<String> getPermissionForSecrets() {
        return permissionForSecrets;
    }

    public void setPermissionForSecrets(List<String> permissionForSecrets) {
        this.permissionForSecrets = permissionForSecrets;
    }

    public List<String> getPermissionForCertificates() {
        return permissionForCertificates;
    }

    public void setPermissionForCertificates(List<String> permissionForCertificates) {
        this.permissionForCertificates = permissionForCertificates;
    }

    public boolean isEnabledForDeployment() {
        return enabledForDeployment;
    }

    public void setEnabledForDeployment(boolean enabledForDeployment) {
        this.enabledForDeployment = enabledForDeployment;
    }

    public boolean isEnabledForDiskEncryption() {
        return enabledForDiskEncryption;
    }

    public void setEnabledForDiskEncryption(boolean enabledForDiskEncryption) {
        this.enabledForDiskEncryption = enabledForDiskEncryption;
    }

    public boolean isEnabledForTemplateDeployment() {
        return enabledForTemplateDeployment;
    }

    public void setEnabledForTemplateDeployment(boolean enabledForTemplateDeployment) {
        this.enabledForTemplateDeployment = enabledForTemplateDeployment;
    }

    public String getTenantId() {
        return tenantId;
    }

    public void setTenantId(String tenantId) {
        this.tenantId = tenantId;
    }

    public String getProvisioningState() {
        return provisioningState;
    }

    public void setProvisioningState(String provisioningState) {
        this.provisioningState = provisioningState;
    }

    public String getVaultUri() {
        return vaultUri;
    }

    public void setVaultUri(String vaultUri) {
        this.vaultUri = vaultUri;
    }

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

}
