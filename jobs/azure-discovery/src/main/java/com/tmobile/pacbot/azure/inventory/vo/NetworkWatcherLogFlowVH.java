package com.tmobile.pacbot.azure.inventory.vo;

public class NetworkWatcherLogFlowVH {
    String name;
    String id;
    boolean enabled;
    Integer retentionInDays;
    boolean isRetentionEnabled;

    public boolean isRetentionEnabled() {
        return isRetentionEnabled;
    }

    public void setRetentionEnabled(boolean retentionEnabled) {
        isRetentionEnabled = retentionEnabled;
    }

    public void setRetentionInDays(Integer retentionInDays) {
        this.retentionInDays = retentionInDays;
    }


    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public Integer getRetentionInDays() {
        return retentionInDays;
    }

    public void setRetentionInDays(int retentionInDays) {
        this.retentionInDays = retentionInDays;
    }
}
