package com.tmobile.pacbot.gcp.inventory.vo;

import java.math.BigInteger;
import java.util.Map;

public class BigQueryTableVH extends GCPVH {

    private String tableId;
    private String description;
    private String friendlyName;
    private String generatedId;
    private Map<String, String> labels;
    private String kmsKeyName;
    private String dataSetId;
    private String iamResourceName;
    private String expirationTime;
    private String creationTime;
    private String lastModifiedTime;
    private String etag;

    private String selfLink;

    private BigInteger rowNum;

    private Long numBytes;

    public String getDataSetId() {
        return dataSetId;
    }

    public String getIamResourceName() {
        return iamResourceName;
    }

    public void setIamResourceName(String iamResourceName) {
        this.iamResourceName = iamResourceName;
    }

    public void setDataSetId(String dataSetId) {
        this.dataSetId = dataSetId;
    }

    public String getTableId() {
        return tableId;
    }

    public void setTableId(String tableId) {
        this.tableId = tableId;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
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

    public String getExpirationTime() {
        return expirationTime;
    }

    public void setExpirationTime(String expirationTime) {
        this.expirationTime = expirationTime;
    }

    public String getCreationTime() {
        return creationTime;
    }

    public void setCreationTime(String creationTime) {
        this.creationTime = creationTime;
    }

    public String getLastModifiedTime() {
        return lastModifiedTime;
    }

    public void setLastModifiedTime(String lastModifiedTime) {
        this.lastModifiedTime = lastModifiedTime;
    }

    public String getEtag() {
        return etag;
    }

    public void setEtag(String etag) {
        this.etag = etag;
    }

    public String getSelfLink() {
        return selfLink;
    }

    public void setSelfLink(String selfLink) {
        this.selfLink = selfLink;
    }

    public BigInteger getRowNum() {
        return rowNum;
    }

    public void setRowNum(BigInteger rowNum) {
        this.rowNum = rowNum;
    }

    public Long getNumBytes() {
        return numBytes;
    }

    public void setNumBytes(Long numBytes) {
        this.numBytes = numBytes;
    }
}
