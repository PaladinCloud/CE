package com.tmobile.cso.pacman.inventory.vo;

import com.amazonaws.services.rds.model.DBSnapshot;
import com.amazonaws.services.rds.model.DBSnapshotAttribute;

import java.util.List;

public class DBSnapshotVH {

    private DBSnapshot dbSnapshot;

    private List<DBSnapshotAttributeVH> dbSnapshotAttributes;

    public DBSnapshot getDbSnapshot() {
        return dbSnapshot;
    }

    public void setDbSnapshot(DBSnapshot dbSnapshot) {
        this.dbSnapshot = dbSnapshot;
    }

    public List<DBSnapshotAttributeVH> getDbSnapshotAttributes() {
        return dbSnapshotAttributes;
    }

    public void setDbSnapshotAttributes(List<DBSnapshotAttributeVH> dbSnapshotAttributes) {
        this.dbSnapshotAttributes = dbSnapshotAttributes;
    }
}
