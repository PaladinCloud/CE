package com.tmobile.pacbot.azure.inventory.vo;


import java.util.Set;

public class DiagnosticSettingVH extends AzureVH{
    private String name;

    private Set<String> enabledCategories;

    private String subscriptionId;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Set<String> getEnabledCategories() {
        return enabledCategories;
    }

    public void setEnabledCategories(Set<String> enabledCategories) {
        this.enabledCategories = enabledCategories;
    }

    public String getSubscriptionId() {
        return subscriptionId;
    }

    public void setSubscriptionId(String subscriptionId) {
        this.subscriptionId = subscriptionId;
    }
}
