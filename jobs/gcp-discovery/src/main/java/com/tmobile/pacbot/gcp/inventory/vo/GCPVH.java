package com.tmobile.pacbot.gcp.inventory.vo;

import com.tmobile.pacbot.gcp.inventory.collector.Util;

public class GCPVH {

    private String discoveryDate;
    private String _cloudType = "GCP";
    private String subscription;

    private String region;
    private String subscriptionName;
    private String resourceGroupName;
    private String id;
    private String projectName;

    public String getSubscription() {
        return subscription;
    }

    public void setSubscription(String subscription) {
        this.subscription = subscription;
    }

    public String getRegion() {
        return region;
    }

    public void setRegion(String region) {
        this.region = region;
    }

    public String getSubscriptionName() {
        return subscriptionName;
    }

    public void setSubscriptionName(String subscriptionName) {
        this.subscriptionName = subscriptionName;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = Util.removeFirstSlash(id);
    }

    public String getResourceGroupName() {
        return resourceGroupName;
    }

    public void setResourceGroupName(String resourceGroupName) {
        this.resourceGroupName = resourceGroupName;
    }

    public String getDiscoverydate() {
        return discoveryDate;
    }

    public void setDiscoverydate(String discoverydate) {
        this.discoveryDate = discoverydate;
    }

    public String get_cloudType() {
        return _cloudType;
    }

    public void set_cloudType(String _cloudType) {
        this._cloudType = _cloudType;
    }

    public String getProjectName() {
        return projectName;
    }

    public void setProjectName(String projectName) {
        this.projectName = projectName;
    }

}
