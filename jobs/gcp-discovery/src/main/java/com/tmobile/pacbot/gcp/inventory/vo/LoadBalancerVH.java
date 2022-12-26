package com.tmobile.pacbot.gcp.inventory.vo;

import java.util.List;

public class LoadBalancerVH extends GCPVH{
    String urlMap;

    List<String> targetHttpsProxy;

    boolean logConfigEnabled;

    List<SslPolicyVH>sslPolicyList;

    public boolean isLogConfigEnabled() {
        return logConfigEnabled;
    }

    public void setLogConfigEnabled(boolean logConfigEnabled) {
        this.logConfigEnabled = logConfigEnabled;
    }

    public String getUrlMap() {
        return urlMap;
    }

    public void setUrlMap(String urlMap) {
        this.urlMap = urlMap;
    }


    public List<String> getTargetHttpsProxy() {
        return targetHttpsProxy;
    }

    public void setTargetHttpsProxy(List<String> targetHttpsProxy) {
        this.targetHttpsProxy = targetHttpsProxy;
    }
    public List<SslPolicyVH> getSslPolicyList() {
        return sslPolicyList;
    }

    public void setSslPolicyList(List<SslPolicyVH> sslPolicyList) {
        this.sslPolicyList = sslPolicyList;
    }

}
