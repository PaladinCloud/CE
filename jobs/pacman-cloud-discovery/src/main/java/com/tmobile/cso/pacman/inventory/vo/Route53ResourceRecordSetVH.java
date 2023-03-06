package com.tmobile.cso.pacman.inventory.vo;

public class Route53ResourceRecordSetVH {

    private String name;
    private String type;
    private String ttl;
    private String resourceRecords;

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

    public String getTtl() {
        return ttl;
    }

    public void setTtl(String ttl) {
        this.ttl = ttl;
    }

    public String getResourceRecords() {
        return resourceRecords;
    }

    public void setResourceRecords(String resourceRecords) {
        this.resourceRecords = resourceRecords;
    }
}
