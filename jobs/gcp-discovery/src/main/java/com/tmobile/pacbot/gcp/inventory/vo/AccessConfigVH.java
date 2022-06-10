package com.tmobile.pacbot.gcp.inventory.vo;

public class AccessConfigVH extends GCPVH {
    private String name;
    private String natIP;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getNatIP() {
        return natIP;
    }

    public void setNatIP(String natIP) {
        this.natIP = natIP;
    }
}
