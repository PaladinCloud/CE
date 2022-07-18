package com.tmobile.pacbot.gcp.inventory.vo;

import java.util.List;

public class Bindings {
    private String role;
    private List<String> members;

    public Bindings(String role, List<String> members) {
        this.role = role;
        this.members = members;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public List<String> getMembers() {
        return members;
    }

    public void setMembers(List<String> members) {
        this.members = members;
    }
}
