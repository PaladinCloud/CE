package com.tmobile.pacbot.azure.inventory.vo;

public class RedisCacheVH extends AzureVH{
    private boolean nonSslPort;

    public boolean isNonSslPort() {
        return nonSslPort;
    }

    public void setNonSslPort(boolean nonSslPort) {
        this.nonSslPort = nonSslPort;
    }
}
