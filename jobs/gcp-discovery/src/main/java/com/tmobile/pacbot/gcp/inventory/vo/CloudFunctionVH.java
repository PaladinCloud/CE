package com.tmobile.pacbot.gcp.inventory.vo;


public class CloudFunctionVH extends GCPVH {

    String functionName;

    String region;

    String ingressSetting;

    String vpcConnector;

    String httpTrigger;

    public String getHttpTrigger() {
        return httpTrigger;
    }

    public String getFunctionName() {
        return functionName;
    }

    public void setFunctionName(String functionName) {
        this.functionName = functionName;
    }

    public void setHttpTrigger(String httpTrigger) {
        this.httpTrigger = httpTrigger;
    }

    public String getVpcConnector() {
        return vpcConnector;
    }

    public void setVpcConnector(String vpcConnector) {
        this.vpcConnector = vpcConnector;
    }

    @Override
    public String getRegion() {
        return region;
    }

    @Override
    public void setRegion(String region) {
        this.region = region;
    }

    public String getIngressSetting() {
        return ingressSetting;
    }

    public void setIngressSetting(String ingressSetting) {
        this.ingressSetting = ingressSetting;
    }

}
