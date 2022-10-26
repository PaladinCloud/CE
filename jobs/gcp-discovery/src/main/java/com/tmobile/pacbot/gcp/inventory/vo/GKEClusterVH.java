package com.tmobile.pacbot.gcp.inventory.vo;

import java.util.List;
import java.util.Map;

public class GKEClusterVH extends GCPVH {
    private Map<String, Object> masterAuthorizedNetworksConfig;
    private String bootDiskKmsKey;

    private String keyName;

    private List<Boolean> nodePoolIntegrityMonitoring;

    public List<Boolean> getNodePoolIntegrityMonitoring() {
        return nodePoolIntegrityMonitoring;
    }

    public void setNodePoolIntegrityMonitoring(List<Boolean> nodePoolIntegrityMonitoring) {
        this.nodePoolIntegrityMonitoring = nodePoolIntegrityMonitoring;
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

}
