package com.tmobile.pacman.api.admin.domain;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CreateAssetGroup {

    private String datasource;

    private String groupName;
    private String type;
    private String createdBy;
    private String description;

    private List<TargetTypesDetails> targetTypes;


    private boolean isVisible = true;

    private List<HashMap<String, Object>> configuration;

    private Map<String, Object> alias;

    public boolean isVisible() {
        return isVisible;
    }

    public List<TargetTypesDetails> getTargetTypes() {
        return targetTypes;
    }

    public void setTargetTypes(List<TargetTypesDetails> targetTypes) {
        this.targetTypes = targetTypes;
    }

    public void setVisible(boolean visible) {
        isVisible = visible;
    }



    public String getDatasource() {
        return datasource;
    }

    public void setDatasource(String datasource) {
        this.datasource = datasource;
    }

    public Map<String, Object> getAlias() {
        return alias;
    }

    public void setAlias(Map<String, Object> alias) {
        this.alias = alias;
    }

    public String getGroupName() {
        return groupName;
    }

    public void setGroupName(String groupName) {
        this.groupName = groupName;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(String createdBy) {
        this.createdBy = createdBy;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public List<HashMap<String, Object>> getConfiguration() {
        return configuration;
    }

    public void setConfiguration(List<HashMap<String, Object>> configuration) {
        this.configuration = configuration;
    }

    @Override
    public String toString() {
        return "CreateAssetGroup{" +
                "datasource='" + datasource + '\'' +
                ", groupName='" + groupName + '\'' +
                ", type='" + type + '\'' +
                ", createdBy='" + createdBy + '\'' +
                ", description='" + description + '\'' +
                ", targetTypes=" + targetTypes +
                ", isVisible=" + isVisible +
                ", configuration=" + configuration +
                ", alias=" + alias +
                '}';
    }
}
