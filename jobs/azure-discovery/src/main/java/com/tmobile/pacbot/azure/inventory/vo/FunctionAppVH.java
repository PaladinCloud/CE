package com.tmobile.pacbot.azure.inventory.vo;

public class FunctionAppVH extends AzureVH {
    boolean clientCertEnabled;

    public boolean isClientCertEnabled() {
        return clientCertEnabled;
    }

    public void setClientCertEnabled(boolean clientCertEnabled) {
        this.clientCertEnabled = clientCertEnabled;
    }
}
