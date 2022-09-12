package com.tmobile.pacbot.gcp.inventory.vo;

public class NetworkVH extends GCPVH{
    private String name;
    private boolean autoCreateSubnetworks;

    public String getName() {
        return name;
    }

    public boolean isAutoCreateSubnetworks() {
        return autoCreateSubnetworks;
    }

    public void setAutoCreateSubnetworks(boolean autoCreateSubnetworks) {
        this.autoCreateSubnetworks = autoCreateSubnetworks;
    }

    public void setName(String name) {
        this.name = name;
    }
}
