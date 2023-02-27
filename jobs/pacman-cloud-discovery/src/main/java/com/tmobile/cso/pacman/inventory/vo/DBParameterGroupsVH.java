package com.tmobile.cso.pacman.inventory.vo;

import com.amazonaws.services.rds.model.DBParameterGroupStatus;

import java.util.List;

public class DBParameterGroupsVH {

    private String dBInstanceIdentifier;

    private List<DBParameterGroupStatus> dBParameterGroups;

    public String getDBInstanceIdentifier() {
        return dBInstanceIdentifier;
    }

    public void setDBInstanceIdentifier(String dBInstanceIdentifier) {
        this.dBInstanceIdentifier = dBInstanceIdentifier;
    }

    public List<DBParameterGroupStatus> getdBParameterGroups() {
        return dBParameterGroups;
    }

    public void setdBParameterGroups(List<DBParameterGroupStatus> dBParameterGroups) {
        this.dBParameterGroups = dBParameterGroups;
    }
}
