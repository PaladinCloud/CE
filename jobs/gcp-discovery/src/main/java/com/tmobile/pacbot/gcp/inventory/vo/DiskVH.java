package com.tmobile.pacbot.gcp.inventory.vo;

import com.google.protobuf.ProtocolStringList;

import java.util.List;

public class DiskVH extends GCPVH{

    private String name;
    private String kind;
    private long sizeGb;
    private String zone;
    private  String status;
    private  String type;
    private ProtocolStringList licenses;
    private ProtocolStringList users;
    private List<Long>licenseCodes;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getKind() {
        return kind;
    }

    public void setKind(String kind) {
        this.kind = kind;
    }

    public long getSizeGb() {
        return sizeGb;
    }

    public void setSizeGb(long sizeGb) {
        this.sizeGb = sizeGb;
    }

    public String getZone() {
        return zone;
    }

    public void setZone(String zone) {
        this.zone = zone;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public ProtocolStringList getLicenses() {
        return licenses;
    }

    public void setLicenses(ProtocolStringList licenses) {
        this.licenses = licenses;
    }

    public ProtocolStringList getUsers() {
        return users;
    }

    public void setUsers(ProtocolStringList users) {
        this.users = users;
    }

    public List<Long> getLicenseCodes() {
        return licenseCodes;
    }

    public void setLicenseCodes(List<Long> licenseCodes) {
        this.licenseCodes = licenseCodes;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }
}
