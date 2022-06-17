package com.tmobile.pacbot.azure.inventory.vo;

import java.util.List;
import java.util.Map;

public class ActivityLogVH extends AzureVH {
    private Map<String, Object> properties;

    public Map<String, Object> getProperties() {
        return properties;
    }

    public void setProperties(Map<String, Object> properties) {
        this.properties = properties;
    }

}
