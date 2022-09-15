package com.tmobile.pacbot.azure.inventory.vo;

import java.util.Map;

public class BlobServiceVH extends AzureVH{
    private String name;
    private String type;
    private Map<String, Object> propertiesMap;

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

    public Map<String, Object> getPropertiesMap() {
        return propertiesMap;
    }

    public void setPropertiesMap(Map<String, Object> propertiesMap) {
        this.propertiesMap = propertiesMap;
    }
}
