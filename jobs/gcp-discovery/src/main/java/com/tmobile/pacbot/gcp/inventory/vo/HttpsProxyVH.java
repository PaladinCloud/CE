package com.tmobile.pacbot.gcp.inventory.vo;

public class HttpsProxyVH {

    private String name;

    private boolean hasCustomPolicy;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public boolean isHasCustomPolicy() {
        return hasCustomPolicy;
    }

    public void setHasCustomPolicy(boolean hasCustomPolicy) {
        this.hasCustomPolicy = hasCustomPolicy;
    }
}
