package com.tmobile.pacbot.azure.inventory.vo;

import java.util.Set;

public class WebAppVH extends AzureVH {
    private boolean remoteDebuggingEnabled;
    private boolean http20Enabled;
    private String resourceGroupName;
    private boolean httpsOnly;

    public boolean isHttpsOnly() {
        return httpsOnly;
    }

    public void setHttpsOnly(boolean httpsOnly) {
        this.httpsOnly = httpsOnly;
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
}
