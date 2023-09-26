package com.tmobile.pacman.api.admin.enums;

import java.util.Arrays;
import java.util.Optional;

public enum AssetGrpAttributeIndex {
    AWS("/aws_account/_search?filter_path=aggregations.alldata.buckets.key"),
    AZURE("/azure_subscription/_search?filter_path=aggregations.alldata.buckets.key"),
    GCP("/gcp_vminstance/_search?filter_path=aggregations.alldata.buckets.key");
    private final String identifier;

    private AssetGrpAttributeIndex(String identifier) {
        this.identifier = identifier;
    }

    public String toString() {
        return identifier;
    }


}
