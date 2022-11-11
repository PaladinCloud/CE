package com.tmobile.pacbot.azure.inventory.vo;

public class AutoProvisioningSettingsVH extends AzureVH{

    private String name;
    private String type;
    private String autoProvision;

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

    public String getAutoProvision() {
        return autoProvision;
    }

    public void setAutoProvision(String autoProvision) {
        this.autoProvision = autoProvision;
    }


}
