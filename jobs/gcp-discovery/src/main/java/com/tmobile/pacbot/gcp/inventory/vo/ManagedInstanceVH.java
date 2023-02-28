package com.tmobile.pacbot.gcp.inventory.vo;

import java.util.List;

public class ManagedInstanceVH {
    private String name;

    private List<String> instanceHealth;

    private int instanceHealthCount;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<String> getInstanceHealth() {
        return instanceHealth;
    }

    public void setInstanceHealth(List<String> instanceHealth) {
        this.instanceHealth = instanceHealth;
    }

    public int getInstanceHealthCount() {
        return instanceHealthCount;
    }

    public void setInstanceHealthCount(int instanceHealthCount) {
        this.instanceHealthCount = instanceHealthCount;
    }
}
