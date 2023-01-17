package com.tmobile.pacbot.azure.inventory.vo;

import java.util.Map;

public class MySQLFlexibleVH extends AzureVH{
    String tlsVersion;

    private Map<String, String> tags;

    public Map<String, String> getTags() {
        return tags;
    }

    public void setTags(Map<String, String> tags) {
        this.tags = tags;
    }

    public String getTlsVersion() {
        return tlsVersion;
    }

    public void setTlsVersion(String tlsVersion) {
        this.tlsVersion = tlsVersion;
    }
}
