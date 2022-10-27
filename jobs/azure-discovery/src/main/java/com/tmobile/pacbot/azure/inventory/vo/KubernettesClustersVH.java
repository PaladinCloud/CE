package com.tmobile.pacbot.azure.inventory.vo;

public class KubernettesClustersVH extends AzureVH{
    public boolean enableRBAC;

    public boolean isEnableRBAC() {
        return enableRBAC;
    }

    public void setEnableRBAC(boolean enableRBAC) {
        this.enableRBAC = enableRBAC;
    }
}
