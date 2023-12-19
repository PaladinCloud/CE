package com.tmobile.pacman.api.compliance.domain;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;
import java.util.Map;

public class NotificationFilterRequest {

    @JsonProperty("filter")
    private Map<String, List<String>> apiFilter;
    private String ag;
    private String domain;
    private String type;
    private String attributeName;
    private String searchText;

    public Map<String, List<String>> getApiFilter() {
        return apiFilter;
    }

    public void setApiFilter(Map<String, List<String>> apiFilter) {
        this.apiFilter = apiFilter;
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

    public String getAttributeName() {
        return attributeName;
    }

    public void setAttributeName(String attributeName) {
        this.attributeName = attributeName;
    }

    public String getSearchText() {
        return searchText;
    }

    public void setSearchText(String searchText) {
        this.searchText = searchText;
    }
}
