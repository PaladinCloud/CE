package com.tmobile.pacman.api.compliance.domain;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Map;

public class FilterRequest {
    @JsonProperty("filter")
    private Map<String, Object> apiFilter;
    private String ag;
    private String domain;
    private String type;
    private String attributeName;

    public String getAttributeName() {
        return attributeName;
    }

    public void setAttributeName(String attributeName) {
        this.attributeName = attributeName;
    }

    public String getAg() {
        return ag;
    }

    public void setAg(String ag) {
        this.ag = ag;
    }

    public String getDomain() {
        return domain;
    }

    public void setDomain(String domain) {
        this.domain = domain;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public Map<String, Object> getApiFilter() {
        return apiFilter;
    }
    @JsonProperty("filter")
    public void setApiFilter(Map<String, Object> apiFilter) {
        this.apiFilter = apiFilter;
    }
}
