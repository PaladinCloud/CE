package com.tmobile.pacbot.azure.inventory.vo;

import com.microsoft.azure.management.appservice.FtpsState;


import java.util.Set;

public class WebAppVH extends AzureVH {
    private boolean remoteDebuggingEnabled;
    private boolean http20Enabled;
    private String resourceGroupName;
    private boolean httpsOnly;
    private FtpsState ftpsState;

    private String minTlsVersion;

    private boolean clientCertEnabled;

    private String systemAssignedManagedServiceIdentityPrincipalId;

    public String getSystemAssignedManagedServiceIdentityPrincipalId() {
        return systemAssignedManagedServiceIdentityPrincipalId;
    }

    public void setSystemAssignedManagedServiceIdentityPrincipalId(String systemAssignedManagedServiceIdentityPrincipalId) {
        this.systemAssignedManagedServiceIdentityPrincipalId = systemAssignedManagedServiceIdentityPrincipalId;
    }

    public boolean isClientCertEnabled() {
        return clientCertEnabled;
    }

    public void setClientCertEnabled(boolean clientCertEnabled) {
        this.clientCertEnabled = clientCertEnabled;
    }


    public boolean isHttpsOnly() {
        return httpsOnly;
    }

    public void setHttpsOnly(boolean httpsOnly) {
        this.httpsOnly = httpsOnly;
    }

    private boolean authEnabled;
    public boolean isAuthEnabled() {
        return authEnabled;
    }

    public void setAuthEnabled(boolean authEnabled) {
        this.authEnabled = authEnabled;
    }

    public Set<String> getHostNames() {
        return hostNames;
    }

    public boolean isHttp20Enabled() {
        return http20Enabled;
    }

    public void setHttp20Enabled(boolean http20Enabled) {
        this.http20Enabled = http20Enabled;
    }

    @Override
    public String getResourceGroupName() {
        return resourceGroupName;
    }

    @Override
    public void setResourceGroupName(String resourceGroupName) {
        this.resourceGroupName = resourceGroupName;
    }

    public void setHostNames(Set<String> hostNames) {
        this.hostNames = hostNames;
    }

    private Set<String> hostNames;
    public boolean getRemoteDebuggingEnabled() {
        return remoteDebuggingEnabled;
    }

    public void setRemoteDebuggingEnabled(boolean remoteDebuggingEnabled) {
        this.remoteDebuggingEnabled = remoteDebuggingEnabled;
    }

    public FtpsState getFtpsState() {
        return ftpsState;
    }
    public void setFtpsState(FtpsState ftpsState) {
        this.ftpsState = ftpsState;
    }

    public String getMinTlsVersion() {
        return minTlsVersion;
    }

    public void setMinTlsVersion(String minTlsVersion) {
        this.minTlsVersion = minTlsVersion;
    }
}
