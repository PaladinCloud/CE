package com.tmobile.pacbot.gcp.inventory.vo;


import java.util.HashMap;
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

    private List<HashMap<String,Object>> serviceAccounts;
    private boolean confidentialComputing;

    public boolean isConfidentialComputing() {
        return confidentialComputing;
    }

    public void setConfidentialComputing(boolean confidentialComputing) {
        this.confidentialComputing = confidentialComputing;
    }

    public List<HashMap<String, Object>> getServiceAccounts() {
        return serviceAccounts;
    }


    public void setServiceAccounts(List<HashMap<String, Object>> serviceAccounts) {
        this.serviceAccounts = serviceAccounts;
    }

    private String onHostMaintainence;

    private ShieldedInstanceConfigVH shieldedInstanceConfig;

    private String projectNumber;
    private List scopesList;
    private List emailList;
    public String getOnHostMaintainence() {
        return onHostMaintainence;
    }

    public ShieldedInstanceConfigVH getShieldedInstanceConfig() {
        return shieldedInstanceConfig;
    }

    public void setShieldedInstanceConfig(ShieldedInstanceConfigVH shieldedInstanceConfig) {
        this.shieldedInstanceConfig = shieldedInstanceConfig;
    }

    public void setOnHostMaintainence(String onHostMaintainence) {
        this.onHostMaintainence = onHostMaintainence;
    }

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

    public List getScopesList() {
        return scopesList;
    }

    public List getEmailList() {
        return emailList;
    }

    public void setEmailList(List emailList) {
        this.emailList = emailList;
    }

    public void setScopesList(List scopesList) {
        this.scopesList = scopesList;
    }

    public String getProjectNumber() {
        return projectNumber;
    }

    public void setProjectNumber(String projectNumber) {
        this.projectNumber = projectNumber;
    }
}
