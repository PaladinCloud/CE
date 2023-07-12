package com.tmobile.pacman.api.compliance.repository.model;

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
