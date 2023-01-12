package com.tmobile.pacman.api.admin.domain;

import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class Permission {

    private String permissionName;
    private String description;
    private boolean isAdminCapability;


    public String getPermissionName() {
        return permissionName;
    }

    public void setPermissionName(String permissionName) {
        this.permissionName = permissionName;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public boolean isAdminCapability() {
        return isAdminCapability;
    }

    public void setAdminCapability(boolean adminCapability) {
        isAdminCapability = adminCapability;
    }
}
