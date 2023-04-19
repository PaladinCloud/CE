package com.tmobile.cloud.awsrules.ec2.model;

public class CveDetails {
    private String id;
    private String url;

    public CveDetails() {
    }

    public CveDetails(String id, String url) {
        this.id = id;
        this.url = url;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    @Override
    public String toString() {
        return "{" +
                "id='" + id + '\'' +
                ",url='" + url + '\'' +
                '}';
    }
}
