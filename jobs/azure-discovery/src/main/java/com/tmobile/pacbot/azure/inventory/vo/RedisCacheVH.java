package com.tmobile.pacbot.azure.inventory.vo;

public class RedisCacheVH extends AzureVH{
    private boolean nonSslPort;
    private String name;

    private int port;

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public boolean isNonSslPort() {
        return nonSslPort;
    }

    public void setNonSslPort(boolean nonSslPort) {
        this.nonSslPort = nonSslPort;
    }
}
