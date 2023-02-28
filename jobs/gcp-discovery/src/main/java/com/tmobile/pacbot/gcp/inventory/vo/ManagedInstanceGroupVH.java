package com.tmobile.pacbot.gcp.inventory.vo;

import java.util.List;

public class ManagedInstanceGroupVH extends GCPVH {
    private String name;

    private List<ManagedInstanceVH> managedInstance;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<ManagedInstanceVH> getManagedInstance() {
        return managedInstance;
    }

    public void setManagedInstance(List<ManagedInstanceVH> managedInstance) {
        this.managedInstance = managedInstance;
    }
}
