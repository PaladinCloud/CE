package com.tmobile.pacbot.azure.inventory.vo;

import java.util.List;
import java.util.Map;

public class VirtualMachineScaleSetVH extends AzureVH {
    List<String> virtualMachineIds;
    List<String> loadBalancerIds;
    private String computerName;
    private Map<String, Object> properties;

    private Map<String, String> tags;

    public String getComputerName() {
        return computerName;
    }

    public void setComputerName(String computerName) {
        this.computerName = computerName;
    }

    public List<String> getVirtualMachineIds() {
        return virtualMachineIds;
    }

    public void setVirtualMachineIds(List<String> virtualMachineIds) {
        this.virtualMachineIds = virtualMachineIds;
    }

    public List<String> getLoadBalancerIds() {
        return loadBalancerIds;
    }

    public void setLoadBalancerIds(List<String> loadBalancerIds) {
        this.loadBalancerIds = loadBalancerIds;
    }

    public Map<String, Object> getProperties() {
        return properties;
    }

    public void setProperties(Map<String, Object> properties) {
        this.properties = properties;
    }

    public Map<String, String> getTags() {
        return tags;
    }

    public void setTags(Map<String, String> tags) {
        this.tags = tags;
    }
}
