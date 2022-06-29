package com.tmobile.pacbot.gcp.inventory.vo;

import com.google.cloud.compute.v1.Items;

import java.util.List;
import java.util.Map;

public class VirtualMachineVH extends GCPVH {
    private String name;
    private String description;
    private List<VMDiskVH> disks;
    private Map<String, String> tags;
    private String machineType;
    private String status;
    private List<NetworkInterfaceVH> networkInterfaces;

    private List<ItemInterfaceVH> items;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public List<VMDiskVH> getDisks() {
        return disks;
    }

    public void setDisks(List<VMDiskVH> disks) {
        this.disks = disks;
    }

    public Map<String, String> getTags() {
        return tags;
    }

    public void setTags(Map<String, String> tags) {
        this.tags = tags;
    }

    public String getMachineType() {
        return machineType;
    }

    public void setMachineType(String machineType) {
        this.machineType = machineType;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public List<NetworkInterfaceVH> getNetworkInterfaces() {
        return networkInterfaces;
    }

    public void setNetworkInterfaces(List<NetworkInterfaceVH> networkInterfaces) {
        this.networkInterfaces = networkInterfaces;
    }

    public void setItems(List<ItemInterfaceVH> items) {
        this.items=items;
    }
    public List<ItemInterfaceVH> getItems() {
        return items;
    }

}
