package com.tmobile.pacbot.gcp.inventory.vo;

import java.util.HashMap;

public class CloudAssetVH {
    String name;
    String state;
    HashMap<String, Object>config;

    public HashMap<String, Object> getConfig() {
        return config;
    }

    public void setConfig(HashMap<String, Object> config) {
        this.config = config;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }
}
