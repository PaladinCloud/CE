package com.tmobile.pacbot.gcp.inventory.vo;

import java.util.List;

public class NetworkInterfaceVH extends GCPVH {

    private String name;
    private List<AccessConfigVH> accessConfigs;
    private String network;

    public void setName(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public void setAccessConfigs(List<AccessConfigVH> accessConfigs) {
        this.accessConfigs = accessConfigs;
    }

    public List<AccessConfigVH> getAccessConfigs() {
        return accessConfigs;
    }

    public void setNetwork(String network) {
        this.network = network;
    }

    public String getNetwork() {
        return network;
    }
}
