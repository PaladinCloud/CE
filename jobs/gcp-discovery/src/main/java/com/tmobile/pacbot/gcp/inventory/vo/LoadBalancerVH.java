package com.tmobile.pacbot.gcp.inventory.vo;

import java.util.List;

public class LoadBalancerVH extends GCPVH{
    String urlMap;

    List<String> targetHttpProxy;

    boolean logConfigEnabled;

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

    public List<String> getTargetHttpProxy() {
        return targetHttpProxy;
    }

    public void setTargetHttpProxy(List<String> targetHttpProxy) {
        this.targetHttpProxy = targetHttpProxy;
    }
}
