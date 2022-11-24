package com.tmobile.pacman.api.compliance.domain;

import java.util.ArrayList;
import java.util.List;

public class PolicyPackDetails {
    private String id;
    private String name;
    private String description;

    private int topLevel;
    private List<PolicyPackDetails> childPolicyPacks = new ArrayList<>();

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
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

    public List<PolicyPackDetails> getChildPolicyPacks() {
        return childPolicyPacks;
    }

    public void setChildPolicyPacks(List<PolicyPackDetails> childPolicyPacks) {
        this.childPolicyPacks = childPolicyPacks;
    }

    public int getTopLevel() {
        return topLevel;
    }

    public void setTopLevel(int topLevel) {
        this.topLevel = topLevel;
    }

}
