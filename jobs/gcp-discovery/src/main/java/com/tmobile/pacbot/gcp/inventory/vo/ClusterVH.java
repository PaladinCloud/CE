package com.tmobile.pacbot.gcp.inventory.vo;

import java.util.Map;

public class ClusterVH extends GCPVH{
    String kmsKeyName;
    private Map<String, String> tags;

    public Map<String, String> getTags() {
        return tags;
    }

    public void setTags(Map<String, String> tags) {
        this.tags = tags;
    }

    public String getKmsKeyName() {
        return kmsKeyName;
    }

    public void setKmsKeyName(String kmsKeyName) {
        this.kmsKeyName = kmsKeyName;
    }
}
