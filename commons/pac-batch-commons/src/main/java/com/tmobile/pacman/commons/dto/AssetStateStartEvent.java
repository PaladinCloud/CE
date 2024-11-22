package com.tmobile.pacman.commons.dto;

public class AssetStateStartEvent {
    private final String tenantId;
    private final String dataSource;
    private final String[] assetTypes;
    private final boolean isFromPolicyEngine;

    public AssetStateStartEvent(String tenantId, String dataSource, String[] assetTypes, boolean isFromPolicyEngine) {
        this.tenantId = tenantId;
        this.dataSource = dataSource;
        this.assetTypes = assetTypes;
        this.isFromPolicyEngine = isFromPolicyEngine;
    }

    public String toCommandLine() {
        return String.format("--tenant_id=%s --data_source=%s --asset_types=%s --is_from_policy_engine=%s",
            tenantId, dataSource, String.join(",", assetTypes), isFromPolicyEngine);
    }
}
