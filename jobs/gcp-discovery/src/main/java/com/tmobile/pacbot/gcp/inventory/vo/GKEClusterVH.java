package com.tmobile.pacbot.gcp.inventory.vo;

import java.util.List;
import java.util.Map;

public class GKEClusterVH extends GCPVH {
    private Map<String, Object> masterAuthorizedNetworksConfig;
    private String bootDiskKmsKey;
    private String keyName;
    private List<NodePoolVH> nodePools;
    private Boolean intraNodeVisibility;
    private boolean enableKubernetesAlpha;
    private boolean enablePrivateEndPoints;
    private boolean enablePrivateNodes;
    private  boolean legacyAuthorization;


    public List<NodePoolVH> getNodePools() {
        return nodePools;
    }

    public void setNodePools(List<NodePoolVH> nodePools) {
        this.nodePools = nodePools;
    }

    public String getBootDiskKmsKey() {
        return bootDiskKmsKey;
    }

    public void setBootDiskKmsKey(String bootDiskKmsKey) {
        this.bootDiskKmsKey = bootDiskKmsKey;
    }

    public Map<String, Object> getMasterAuthorizedNetworksConfig() {
        return masterAuthorizedNetworksConfig;
    }

    public void setMasterAuthorizedNetworksConfig(Map<String, Object> masterAuthorizedNetworksConfig) {
        this.masterAuthorizedNetworksConfig = masterAuthorizedNetworksConfig;
    }

    public String getKeyName() {
        return keyName;
    }

    public void setKeyName(String keyName) {
        this.keyName = keyName;
    }

    public boolean isLegacyAuthorization() {
        return legacyAuthorization;
    }

    public void setLegacyAuthorization(boolean legacyAuthorization) {
        this.legacyAuthorization = legacyAuthorization;
    }

    public Boolean getIntraNodeVisibility() {
        return intraNodeVisibility;
    }

    public void setIntraNodeVisibility(Boolean intraNodeVisibility) {
        this.intraNodeVisibility = intraNodeVisibility;
    }

    public boolean isEnablePrivateEndPoints() {
        return enablePrivateEndPoints;
    }

    public void setEnablePrivateEndPoints(boolean enablePrivateEndPoints) {
        this.enablePrivateEndPoints = enablePrivateEndPoints;
    }

    public boolean isEnablePrivateNodes() {
        return enablePrivateNodes;
    }

    public void setEnablePrivateNodes(boolean enablePrivateNodes) {
        this.enablePrivateNodes = enablePrivateNodes;
    }

    public boolean isEnableKubernetesAlpha() {
        return enableKubernetesAlpha;
    }

    public void setEnableKubernetesAlpha(boolean enableKubernetesAlpha) {
        this.enableKubernetesAlpha = enableKubernetesAlpha;
    }

}
