package com.tmobile.pacbot.gcp.inventory.vo;

import java.util.HashSet;
import java.util.List;

public class IAMUserVH extends GCPVH{
    private  String userId;
    private  String email;
    private HashSet<String> roles;

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getEmail() {
        return email;
    }

    public HashSet<String> getRoles() {
        return roles;
    }

    public void setRoles(HashSet<String> roles) {
        this.roles = roles;
    }

    public void setEmail(String email) {
        this.email = email;
    }


}
