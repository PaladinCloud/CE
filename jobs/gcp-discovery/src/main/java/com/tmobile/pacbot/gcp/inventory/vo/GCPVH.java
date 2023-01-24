package com.tmobile.pacbot.gcp.inventory.vo;

import com.tmobile.pacbot.gcp.inventory.collector.Util;

import java.util.Map;

public class GCPVH {

    private String discoveryDate;
    private String _cloudType = "GCP";
    private String region;
    private String id;
    private String projectName;

    private String projectId;
    private Map<String, String> tags;

    public Map<String, String> getTags() {
        return tags;
    }

    public void setTags(Map<String, String> tags) {
        this.tags = tags;
    }

    public String getRegion() {
        return region;
    }

    public void setRegion(String region) {
        this.region = region;
    }
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = Util.removeFirstSlash(id);
    }

    public String getDiscoverydate() {
        return discoveryDate;
    }

    public void setDiscoverydate(String discoverydate) {
        this.discoveryDate = discoverydate;
    }

    public String get_cloudType() {
        return _cloudType;
    }

    public void set_cloudType(String _cloudType) {
        this._cloudType = _cloudType;
    }

    public String getProjectName() {
        return projectName;
    }

    public void setProjectName(String projectName) {
        this.projectName = projectName;
    }

    public String getProjectId() {
        return projectId;
    }

    public void setProjectId(String projectId) {
        this.projectId = projectId;
    }
}
