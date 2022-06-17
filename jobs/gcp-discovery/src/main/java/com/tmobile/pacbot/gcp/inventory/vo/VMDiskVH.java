package com.tmobile.pacbot.gcp.inventory.vo;

public class VMDiskVH extends GCPVH {
    String name;
    long sizeInGB;
    String type;

    boolean hasSha256;

    boolean hasKmsKeyName;

    public boolean isHasKmsKeyName() {
        return hasKmsKeyName;
    }

    public void setHasKmsKeyName(boolean hasKmsKeyName) {
        this.hasKmsKeyName = hasKmsKeyName;
    }

    public boolean isHasSha256() {
        return hasSha256;
    }

    public void setHasSha256(boolean hasSha256) {
        this.hasSha256 = hasSha256;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public long getSizeInGB() {
        return sizeInGB;
    }

    public void setSizeInGB(long sizeInGB) {
        this.sizeInGB = sizeInGB;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }
}
