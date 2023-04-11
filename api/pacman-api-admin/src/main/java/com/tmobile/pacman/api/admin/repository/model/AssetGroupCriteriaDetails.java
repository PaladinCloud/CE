package com.tmobile.pacman.api.admin.repository.model;

import com.fasterxml.jackson.annotation.JsonBackReference;

import javax.persistence.*;

@Entity
@Table(name = "cf_AssetGroupCriteriaDetails", uniqueConstraints = @UniqueConstraint(columnNames = "id_"))
public class AssetGroupCriteriaDetails {

    @Id
    @Column(name = "id_", unique = true, nullable = false)
    private String id;

    @Column(name = "groupId")
    private String groupId;

    private String criteriaName;
    
    private String attributeName;
    
    private String attributeValue;

    @JsonBackReference
    @JoinColumn(name = "groupId", insertable=false, updatable=false)
    @ManyToOne(fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    private AssetGroupDetails assetGroup;


    public String getAttributeName() {
        return attributeName;
    }

    public void setAttributeName(String attributeName) {
        this.attributeName = attributeName;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getGroupId() {
        return groupId;
    }

    public void setGroupId(String groupId) {
        this.groupId = groupId;
    }

    public String getCriteriaName() {
        return criteriaName;
    }

    public void setCriteriaName(String criteriaName) {
        this.criteriaName = criteriaName;
    }

    public String getAttributeValue() {
        return attributeValue;
    }

    public void setAttributeValue(String attributeValue) {
        this.attributeValue = attributeValue;
    }

    public AssetGroupDetails getAssetGroup() {
        return assetGroup;
    }

    public void setAssetGroup(AssetGroupDetails assetGroup) {
        this.assetGroup = assetGroup;
    }
}
