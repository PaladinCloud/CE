package com.tmobile.pacbot.gcp.inventory.vo;

public class VMDiskVH extends GCPVH {
    String name;
    long sizeInGB;
    String type;

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
