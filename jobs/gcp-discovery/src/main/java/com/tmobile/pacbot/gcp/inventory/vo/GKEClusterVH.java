package com.tmobile.pacbot.gcp.inventory.vo;

import java.util.List;
import java.util.Map;
import java.util.Objects;

public class GKEClusterVH extends GCPVH {
    private Map<String, Object> masterAuthorizedNetworksConfig;
    private String bootDiskKmsKey;
    private String keyName;
    private String name;
    private String username;
    private String password;
    private List<NodePoolVH> nodePools;
    private Boolean intraNodeVisibility;
    private boolean enableKubernetesAlpha;
    private boolean enablePrivateEndPoints;
    private boolean enablePrivateNodes;
    private  boolean legacyAuthorization;
    private String version;
    private boolean disableKubernetesDashBoard;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        GKEClusterVH that = (GKEClusterVH) o;
        return Objects.equals(name, that.name);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name);
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public boolean isDisableKubernetesDashBoard() {
        return disableKubernetesDashBoard;
    }

    public void setDisableKubernetesDashBoard(boolean disableKubernetesDashBoard) {
        this.disableKubernetesDashBoard = disableKubernetesDashBoard;
    }

    private  String cloudLogging;
    private  String cloudMonitoring;

    public String getCloudLogging() {
        return cloudLogging;
    }

    public void setCloudLogging(String cloudLogging) {
        this.cloudLogging = cloudLogging;
    }

    public String getCloudMonitoring() {
        return cloudMonitoring;
    }

    public void setCloudMonitoring(String cloudMonitoring) {
        this.cloudMonitoring = cloudMonitoring;
    }

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

    public String getClientKey() {
        return clientKey;
    }

    public void setClientKey(String clientKey) {
        this.clientKey = clientKey;
    }

    public List<NodePoolVH> getNodePools() {
        return nodePools;
    }
    private String clientKey;


    public boolean isIPAlias() {
        return IPAlias;
    }

    public void setIPAlias(boolean IPAlias) {
        this.IPAlias = IPAlias;
    }

    private  boolean IPAlias;


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

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }
}
