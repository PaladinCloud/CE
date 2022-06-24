package com.tmobile.pacbot.gcp.inventory.vo;

import java.util.List;
import java.util.Map;

import com.google.cloud.compute.v1.Allowed;

public class FireWallVH extends GCPVH {
    private String name;
    boolean isDisabled;
    String direction;
    List<String> sourceRanges;
    List<AllowedPortsVH> allow;

    public void setAllow(List<AllowedPortsVH> list) {
        this.allow = list;
    }

    public List<AllowedPortsVH> getAllow() {
        return allow;
    }

    public boolean isDisabled() {
        return isDisabled;
    }

    public void setDisabled(boolean isDisabled) {
        this.isDisabled = isDisabled;
    }

    public String getDirection() {
        return direction;
    }

    public void setDirection(String direction) {
        this.direction = direction;
    }

    public List<String> getSourceRanges() {
        return sourceRanges;
    }

    public void setSourceRanges(List<String> sourceRanges) {
        this.sourceRanges = sourceRanges;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

}
