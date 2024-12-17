package com.tmobile.pacman.commons.dto;


import com.fasterxml.jackson.annotation.JsonProperty;

import java.io.Serializable;

public class SQSBaseMessage implements Serializable {

    @JsonProperty("tenant_id")
    private String tenantId;
    @JsonProperty("tenant_name")
    private String tenantName;

    public SQSBaseMessage(String tenantId, String tenantName) {
        this.tenantId = tenantId;
        this.tenantName = tenantName;
    }

    public String getTenantId() {
        return tenantId;
    }

    public void setTenantId(String tenantId) {
        this.tenantId = tenantId;
    }

    public String getTenantName() {
        return tenantName;
    }

    public void setTenantName(String tenantName) {
        this.tenantName = tenantName;
    }
}
