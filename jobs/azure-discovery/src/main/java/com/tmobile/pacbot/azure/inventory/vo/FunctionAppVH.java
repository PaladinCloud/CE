package com.tmobile.pacbot.azure.inventory.vo;

import java.util.Map;

public class FunctionAppVH extends AzureVH {
    boolean clientCertEnabled;

    private Map<String, String> tags;

    public Map<String, String> getTags() {
        return tags;
    }

    public void setTags(Map<String, String> tags) {
        this.tags = tags;
    }

    public boolean isClientCertEnabled() {
        return clientCertEnabled;
    }

    public void setClientCertEnabled(boolean clientCertEnabled) {
        this.clientCertEnabled = clientCertEnabled;
    }
}
