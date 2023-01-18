package com.tmobile.pacbot.gcp.inventory.vo;

import java.util.Map;

public class CloudDNSVH extends GCPVH{
    public String dnsSecConfigState;
    public String dnsName;

    private Map<String, String> tags;

    public Map<String, String> getTags() {
        return tags;
    }

    public void setTags(Map<String, String> tags) {
        this.tags = tags;
    }

    public String getDnsName() {
        return dnsName;
    }

    public void setDnsName(String dnsName) {
        this.dnsName = dnsName;
    }

    public String getDnsSecConfigState() {
        return dnsSecConfigState;
    }

    public void setDnsSecConfigState(String dnsSecConfigState) {
        this.dnsSecConfigState = dnsSecConfigState;
    }
}
