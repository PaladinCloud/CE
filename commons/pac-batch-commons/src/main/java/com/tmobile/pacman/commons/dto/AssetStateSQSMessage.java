package com.tmobile.pacman.commons.dto;


import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

public class AssetStateSQSMessage extends SQSBaseMessage {

    @JsonProperty("source")
    private String dataSource;
    @JsonProperty("assetTypes")
    private List<String> assetTypes;
    @JsonProperty("isFromPolicyEngine")
    private boolean fromPolicyEngine;
    @JsonProperty("jobName")
    private String jobName;

    public AssetStateSQSMessage(String tenantId, String tenantName, String dataSource, List<String> assetTypes, boolean fromPolicyEngine, String jobName) {
        super(tenantId, tenantName);
        this.dataSource = dataSource;
        this.assetTypes = assetTypes;
        this.fromPolicyEngine = fromPolicyEngine;
        this.jobName = jobName;
    }

    public String getDataSource() {
        return dataSource;
    }

    public void setDataSource(String dataSource) {
        this.dataSource = dataSource;
    }

    public List<String> getAssetTypes() {
        return assetTypes;
    }

    public void setAssetTypes(List<String> assetTypes) {
        this.assetTypes = assetTypes;
    }

    public boolean isFromPolicyEngine() {
        return fromPolicyEngine;
    }

    public void setFromPolicyEngine(boolean fromPolicyEngine) {
        this.fromPolicyEngine = fromPolicyEngine;
    }
}
