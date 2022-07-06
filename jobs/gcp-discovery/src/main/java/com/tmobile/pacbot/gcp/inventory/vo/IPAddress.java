package com.tmobile.pacbot.gcp.inventory.vo;

public class IPAddress {
    private String ip;
    private String type;

    public IPAddress() {
    }

    public IPAddress(String ipAddress, String type) {
        this.ip = ipAddress;
        this.type = type;
    }

    public String getIp() {
        return ip;
    }

    public void setIp(String ip) {
        this.ip = ip;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }
}
