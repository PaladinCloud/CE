package com.tmobile.pacbot.gcp.inventory.vo;

import java.util.Map;

public class GKEClusterVH extends GCPVH {

    private Map<String, Object> masterAuthorizedNetworksConfig;

    public Map<String, Object> getMasterAuthorizedNetworksConfig() {
        return masterAuthorizedNetworksConfig;
    }

    public void setMasterAuthorizedNetworksConfig(Map<String, Object> masterAuthorizedNetworksConfig) {
        this.masterAuthorizedNetworksConfig = masterAuthorizedNetworksConfig;
    }

}
