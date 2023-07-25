package com.tmobile.pacbot.azure.inventory.vo;

import java.util.List;

public class VirtualMachineScaleSetVH {

    List<String>virtualMachineIds;

    List<String>loadBalancerIds;

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
}
