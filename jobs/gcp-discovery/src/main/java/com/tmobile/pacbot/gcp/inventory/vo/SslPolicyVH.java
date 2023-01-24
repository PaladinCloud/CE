package com.tmobile.pacbot.gcp.inventory.vo;

import java.util.List;


public class SslPolicyVH {

     private String minTlsVersion;
    private String profile;
    private List<String> enabledFeatures;

    public String getMinTlsVersion() {
        return minTlsVersion;
    }

    public void setMinTlsVersion(String minTlsVersion) {
        this.minTlsVersion = minTlsVersion;
    }

    public String getProfile() {
        return profile;
    }

    public void setProfile(String profile) {
        this.profile = profile;
    }


    public List<String> getEnabledFeatures() {
        return enabledFeatures;
    }

    public void setEnabledFeatures(List<String> enabledFeatures) {
        this.enabledFeatures = enabledFeatures;
    }

}
