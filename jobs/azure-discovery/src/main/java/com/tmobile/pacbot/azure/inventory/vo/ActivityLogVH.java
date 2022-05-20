package com.tmobile.pacbot.azure.inventory.vo;

import java.util.List;
import java.util.Map;

public class ActivityLogVH extends AzureVH {

    private List<Map<String, Object>> allof;

    public List<Map<String, Object>> getAllof() {
        return allof;
    }

    public void setAllof(List<Map<String, Object>> allof) {
        this.allof = allof;
    }

}
