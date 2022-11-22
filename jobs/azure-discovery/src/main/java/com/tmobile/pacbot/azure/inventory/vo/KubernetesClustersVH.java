package com.tmobile.pacbot.azure.inventory.vo;

import java.util.Map;

public class KubernetesClustersVH extends AzureVH{
    private boolean enableRBAC;

    private Map<String, Object> properties;

    public boolean isEnableRBAC() {
        return enableRBAC;
    }

    public void setEnableRBAC(boolean enableRBAC) {
        this.enableRBAC = enableRBAC;
    }

    public Map<String, Object> getProperties() {
        return properties;
    }

    public void setProperties(Map<String, Object> properties) {
        this.properties = properties;
    }
}
