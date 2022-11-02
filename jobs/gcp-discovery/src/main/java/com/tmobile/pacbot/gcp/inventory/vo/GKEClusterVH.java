package com.tmobile.pacbot.gcp.inventory.vo;

import java.util.List;

import java.util.Map;

public class GKEClusterVH extends GCPVH {
    private Map<String, Object> masterAuthorizedNetworksConfig;
    private String bootDiskKmsKey;
    private String keyName;
    private String username;
    private String password;
    private List<NodePoolVH> nodePools;
    private Boolean intraNodeVisibility;
    private boolean enableKubernetesAlpha;
    private boolean enablePrivateEndPoints;
    private boolean enablePrivateNodes;


    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public boolean isEnableKubernetesAlpha() {
        return enableKubernetesAlpha;
    }

    public void setEnableKubernetesAlpha(boolean enableKubernetesAlpha) {
        this.enableKubernetesAlpha = enableKubernetesAlpha;
    }


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
}
