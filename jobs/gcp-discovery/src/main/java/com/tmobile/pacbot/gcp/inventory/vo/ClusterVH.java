package com.tmobile.pacbot.gcp.inventory.vo;

public class ClusterVH extends GCPVH{
    String kmsKeyName;

    public String getKmsKeyName() {
        return kmsKeyName;
    }

    public void setKmsKeyName(String kmsKeyName) {
        this.kmsKeyName = kmsKeyName;
    }
}
