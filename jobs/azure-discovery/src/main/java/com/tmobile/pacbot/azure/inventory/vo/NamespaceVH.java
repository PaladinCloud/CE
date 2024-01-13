package com.tmobile.pacbot.azure.inventory.vo;

import java.util.Map;

public class NamespaceVH extends AzureVH {

    private String name;
    private String type;
    private String location;
    private Map<String, Object> tags;
    private Map<String, Object> properties;
    private Map<String, Object> sku;


    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public Map<String, Object> getTags() {
        return tags;
    }

    public void setTags(Map<String, Object> tags) {
        this.tags = tags;
    }

    public Map<String, Object> getProperties() {
        return properties;
    }

    public void setProperties(Map<String, Object> properties) {
        this.properties = properties;
    }

    public Map<String, Object> getSku() {
        return sku;
    }

    public void setSku(Map<String, Object> sku) {
        this.sku = sku;
    }

}
