package com.tmobile.pacbot.gcp.inventory.vo;

import java.util.List;

public class ServiceAccountVH extends GCPVH{
    private String name;
    private String displayName;
    private String email;
    private boolean isDisabled;
    private String description;
    private List<ServiceAccountKeyVH> serviceAccountKey ;

    public List<ServiceAccountKeyVH> getServiceAccountKey() {
        return serviceAccountKey;
    }

    public void setServiceAccountKey(List<ServiceAccountKeyVH> serviceAccountKey) {
        this.serviceAccountKey = serviceAccountKey;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public boolean isDisabled() {
        return isDisabled;
    }

    public void setDisabled(boolean disabled) {
        isDisabled = disabled;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }
}
