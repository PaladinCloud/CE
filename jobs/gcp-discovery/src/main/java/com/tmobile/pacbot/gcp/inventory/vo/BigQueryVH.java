package com.tmobile.pacbot.gcp.inventory.vo;

import java.util.List;
import java.util.Map;

public class BigQueryVH extends GCPVH{
    private String projectId;
    private String datasetId;
    private List<Map<String,String>> acl;
    private Long defaultTableLifetime;
    private String description;
    private String etag;
    private String friendlyName;
    private String generatedId;
    private Long lastModified;
    private Map<String,String> labels;
    private String kmsKeyName;
    private Long defaultPartitionExpirationMs;

    public String getProjectId() {
        return projectId;
    }

    public void setProjectId(String projectId) {
        this.projectId = projectId;
    }

    public String getDatasetId() {
        return datasetId;
    }

    public void setDatasetId(String datasetId) {
        this.datasetId = datasetId;
    }

    public List<Map<String, String>> getAcl() {
        return acl;
    }

    public void setAcl(List<Map<String, String>> acl) {
        this.acl = acl;
    }

    public Long getDefaultTableLifetime() {
        return defaultTableLifetime;
    }

    public void setDefaultTableLifetime(Long defaultTableLifetime) {
        this.defaultTableLifetime = defaultTableLifetime;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getEtag() {
        return etag;
    }

    public void setEtag(String etag) {
        this.etag = etag;
    }

    public String getFriendlyName() {
        return friendlyName;
    }

    public void setFriendlyName(String friendlyName) {
        this.friendlyName = friendlyName;
    }

    public String getGeneratedId() {
        return generatedId;
    }

    public void setGeneratedId(String generatedId) {
        this.generatedId = generatedId;
    }

    public Long getLastModified() {
        return lastModified;
    }

    public void setLastModified(Long lastModified) {
        this.lastModified = lastModified;
    }

    public Map<String, String> getLabels() {
        return labels;
    }

    public void setLabels(Map<String, String> labels) {
        this.labels = labels;
    }

    public String getKmsKeyName() {
        return kmsKeyName;
    }

    public void setKmsKeyName(String kmsKeyName) {
        this.kmsKeyName = kmsKeyName;
    }

    public Long getDefaultPartitionExpirationMs() {
        return defaultPartitionExpirationMs;
    }

    public void setDefaultPartitionExpirationMs(Long defaultPartitionExpirationMs) {
        this.defaultPartitionExpirationMs = defaultPartitionExpirationMs;
    }
}
