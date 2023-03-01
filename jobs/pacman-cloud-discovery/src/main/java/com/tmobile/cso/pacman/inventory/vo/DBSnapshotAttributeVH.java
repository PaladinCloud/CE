package com.tmobile.cso.pacman.inventory.vo;

import com.amazonaws.services.rds.model.DBSnapshotAttribute;

import java.util.stream.Collectors;

public class DBSnapshotAttributeVH {

    private String dBSnapshotIdentifier;

    private DBSnapshotAttribute dbSnapshotAttribute;

    private String attributeValues;

    public String getDBSnapshotIdentifier() {
        return dBSnapshotIdentifier;
    }

    public void setDBSnapshotIdentifier(String dBSnapshotIdentifier) {
        this.dBSnapshotIdentifier = dBSnapshotIdentifier;
    }

    public DBSnapshotAttribute getDbSnapshotAttribute() {
        return dbSnapshotAttribute;
    }

    public void setDbSnapshotAttribute(DBSnapshotAttribute dbSnapshotAttribute) {
        this.dbSnapshotAttribute = dbSnapshotAttribute;
        this.attributeValues = String.join(",", dbSnapshotAttribute.getAttributeValues());
    }

    public String getAttributeValues() {
        return attributeValues;
    }
}
