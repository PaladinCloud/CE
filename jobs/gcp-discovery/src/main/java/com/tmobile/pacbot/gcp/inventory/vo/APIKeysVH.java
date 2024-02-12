package com.tmobile.pacbot.gcp.inventory.vo;

import java.util.HashMap;
import java.util.List;

public class APIKeysVH extends GCPVH{

    private String id;
    private String name;
    private String displayName;
    private String createdTime;
    private HashMap<String,Object>restrictions;
    private List<String> apiTargetList;


    @Override
    public String getId() {
        return id;
    }

    @Override
    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public String getCreatedTime() {
        return createdTime;
    }

    public void setCreatedTime(String createdTime) {
        this.createdTime = createdTime;
    }

    public HashMap<String, Object> getRestrictions() {
        return restrictions;
    }

    public void setRestrictions(HashMap<String, Object> restrictions) {
        this.restrictions = restrictions;
    }

    public List<String> getApiTargetList() {
        return apiTargetList;
    }

    public void setApiTargetList(List<String> apiTargetList) {
        this.apiTargetList = apiTargetList;
    }

}