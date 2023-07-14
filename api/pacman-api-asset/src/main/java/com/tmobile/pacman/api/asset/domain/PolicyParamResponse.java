package com.tmobile.pacman.api.asset.domain;

public class PolicyParamResponse {
    private String message;

    private PolicyParamDto data;

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public PolicyParamDto getData() {
        return data;
    }

    public void setData(PolicyParamDto data) {
        this.data = data;
    }
}
