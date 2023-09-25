package com.tmobile.pacman.api.admin.repository.model;

import lombok.Data;

import javax.persistence.*;

@Entity
@Data
@Table(name = "cf_PolicyParams", uniqueConstraints = @UniqueConstraint(columnNames = "policyParamsID"))
public class PolicyParams {

    @Id
    @Column(name = "policyParamsID", unique = true, nullable = false)
    private Long policyParamsID;
    @Column(name = "policyID")
    private String policyId;
    @Column(name = "paramKey")
    private String key;
    @Column(name = "paramValue")
    private String value;
    private String defaultVal;
    private String isEdit;
    private String isMandatory;
    private String encrypt;
    private String displayName;

    public Long getPolicyParamsID() {
        return policyParamsID;
    }

    public void setPolicyParamsID(Long policyParamsID) {
        this.policyParamsID = policyParamsID;
    }

    public String getPolicyId() {
        return policyId;
    }

    public void setPolicyId(String policyId) {
        this.policyId = policyId;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public String getDefaultVal() {
        return defaultVal;
    }

    public void setDefaultVal(String defaultVal) {
        this.defaultVal = defaultVal;
    }

    public String getIsEdit() {
        return isEdit;
    }

    public void setIsEdit(String isEdit) {
        this.isEdit = isEdit;
    }

    public String getIsMandatory() {
        return isMandatory;
    }

    public void setIsMandatory(String isMandatory) {
        this.isMandatory = isMandatory;
    }

    public String getEncrypt() {
        return encrypt;
    }

    public void setEncrypt(String encrypt) {
        this.encrypt = encrypt;
    }

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    private String description;

    @Override
    public String toString() {
        return "{\"policyId\": \"" + policyId + "\"" +
                ", \"params\": [{\"key\" : \"" + key + "\"" +
                ", \"value\": \"" + value + "\"" +
                ", \"defaultVal\": \"" + defaultVal + "\"" +
                ", \"isEdit\": \"" + isEdit + "\"" +
                ", \"isMandatory\": \"" + isMandatory + "\"" +
                ", \"encrypt\": \"" + encrypt + "\"" +
                ", \"displayName\": \"" + displayName + "\"" +
                ", \"description\": \"" + description + "\"" +
                "}]}";
    }

    public String paramsToJsonString() {
        return "{\"key\" : \"" + key + "\"" +
                ", \"value\": \"" + value + "\"" +
                ", \"defaultVal\": \"" + defaultVal + "\"" +
                ", \"isEdit\": \"" + isEdit + "\"" +
                ", \"isMandatory\": \"" + isMandatory + "\"" +
                ", \"encrypt\": \"" + encrypt + "\"" +
                ", \"displayName\": \"" + displayName + "\"" +
                ", \"description\": \"" + description + "\"" +
                "}";
    }
}
