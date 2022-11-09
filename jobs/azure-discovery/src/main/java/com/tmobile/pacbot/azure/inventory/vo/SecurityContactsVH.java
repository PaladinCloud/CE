package com.tmobile.pacbot.azure.inventory.vo;

import java.util.List;
import java.util.Map;

public class SecurityContactsVH extends AzureVH {

    private Map<String, Object> properties;
    private String name;
    private String type;

    private String etag;

    private List<AutoProvisioningSettingsVH>autoProvisioningSettingsList;

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

    public String getEtag() {
        return etag;
    }

    public void setEtag(String etag) {
        this.etag = etag;
    }

    public Map<String, Object> getProperties() {
        return properties;
    }

    public void setProperties(Map<String, Object> properties) {
        this.properties = properties;
    }
    public List<AutoProvisioningSettingsVH> getAutoProvisioningSettingsList() {
        return autoProvisioningSettingsList;
    }

    public void setAutoProvisioningSettingsList(List<AutoProvisioningSettingsVH> autoProvisioningSettingsList) {
        this.autoProvisioningSettingsList = autoProvisioningSettingsList;
    }
}

