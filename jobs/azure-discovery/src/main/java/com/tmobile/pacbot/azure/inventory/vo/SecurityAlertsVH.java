package com.tmobile.pacbot.azure.inventory.vo;

import java.util.HashMap;

public class SecurityAlertsVH extends AzureVH {
    private String name;
    private String type;
    private HashMap<String, Object> propertiesMap;

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

    public HashMap<String, Object> getPropertiesMap() {
        return propertiesMap;
    }

    public void setPropertiesMap(HashMap<String, Object> propertiesMap) {
        this.propertiesMap = propertiesMap;
    }

}
