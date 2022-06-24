package com.tmobile.pacbot.gcp.inventory.vo;

import java.util.Set;

public class StorageVH extends GCPVH {
    private Set<String> users;

    public Set<String> getUsers() {
        return users;
    }

    public void setUsers(Set<String> users) {
        this.users = users;
    }
}
