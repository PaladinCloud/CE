package com.tmobile.pacbot.gcp.inventory.vo;

public class CloudDNSVH extends GCPVH{
    public String dnsSecConfigState;
    public String dnsName;

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
