package com.tmobile.cso.pacman.inventory.vo;

import java.util.List;

public class Route53HostedZoneVH {

    private String hostedZoneId;

    private String name;
    private String serveSignatureStatus;
    private int queryLoggingConfigSize;
    private List<Route53ResourceRecordSetVH> resourceRecordSetVHList;

    public String getHostedZoneId() {
        return hostedZoneId;
    }

    public void setHostedZoneId(String hostedZoneId) {
        this.hostedZoneId = hostedZoneId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getServeSignatureStatus() {
        return serveSignatureStatus;
    }

    public void setServeSignatureStatus(String serveSignatureStatus) {
        this.serveSignatureStatus = serveSignatureStatus;
    }

    public int getQueryLoggingConfigSize() {
        return queryLoggingConfigSize;
    }

    public void setQueryLoggingConfigSize(int queryLoggingConfigSize) {
        this.queryLoggingConfigSize = queryLoggingConfigSize;
    }

    public List<Route53ResourceRecordSetVH> getResourceRecordSetVHList() {
        return resourceRecordSetVHList;
    }

    public void setResourceRecordSetVHList(List<Route53ResourceRecordSetVH> resourceRecordSetVHList) {
        this.resourceRecordSetVHList = resourceRecordSetVHList;
    }
}
