package com.tmobile.pacbot.gcp.inventory.vo;

import java.util.List;

public class AllowedPortsVH {
    String protocol;
    List<String> ports;

    public String getProtocol() {
        return protocol;
    }

    public void setProtocol(String protocol) {
        this.protocol = protocol;
    }

    public List<String> getPorts() {
        return ports;
    }

    public void setPorts(List<String> ports) {
        this.ports = ports;
    }

}
