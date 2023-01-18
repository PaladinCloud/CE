package com.tmobile.pacbot.gcp.inventory.vo;

import java.util.Map;
import java.util.Set;

public class StorageVH extends GCPVH {
    private Set<String> users;

    private Boolean uniformBucketLevelAccess;
    private Map<String, String> tags;

    public Map<String, String> getTags() {
        return tags;
    }

    public void setTags(Map<String, String> tags) {
        this.tags = tags;
    }

    public Set<String> getUsers() {
        return users;
    }

    public void setUsers(Set<String> users) {
        this.users = users;
    }

    public Boolean getUniformBucketLevelAccess() {
        return uniformBucketLevelAccess;
    }

    public void setUniformBucketLevelAccess(Boolean uniformBucketLevelAccess) {
        this.uniformBucketLevelAccess = uniformBucketLevelAccess;
    }
}
