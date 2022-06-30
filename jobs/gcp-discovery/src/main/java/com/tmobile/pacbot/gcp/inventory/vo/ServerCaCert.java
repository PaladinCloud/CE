package com.tmobile.pacbot.gcp.inventory.vo;

public class ServerCaCert {
    private String certSerialNumber;
    private String commonName;
    private String createTime;
    private String expirationTime;
    private String instance;
    private String kind;

    public String getCertSerialNumber() {
        return certSerialNumber;
    }

    public void setCertSerialNumber(String certSerialNumber) {
        this.certSerialNumber = certSerialNumber;
    }

    public String getCommonName() {
        return commonName;
    }

    public void setCommonName(String commonName) {
        this.commonName = commonName;
    }

    public String getCreateTime() {
        return createTime;
    }

    public void setCreateTime(String createTime) {
        this.createTime = createTime;
    }

    public String getExpirationTime() {
        return expirationTime;
    }

    public void setExpirationTime(String expirationTime) {
        this.expirationTime = expirationTime;
    }

    public String getInstance() {
        return instance;
    }

    public void setInstance(String instance) {
        this.instance = instance;
    }

    public String getKind() {
        return kind;
    }

    public void setKind(String kind) {
        this.kind = kind;
    }
}
