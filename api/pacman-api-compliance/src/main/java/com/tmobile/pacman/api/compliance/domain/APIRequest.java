package com.tmobile.pacman.api.compliance.domain;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Map;

public class APIRequest extends Request{
    @JsonProperty("filter")
    private Map<String, Object> apiFilter;

    public Map<String, Object> getApiFilter() {
        return apiFilter;
    }
    @JsonProperty("filter")
    public void setApiFilter(Map<String, Object> apiFilter) {
        this.apiFilter = apiFilter;
    }
}
