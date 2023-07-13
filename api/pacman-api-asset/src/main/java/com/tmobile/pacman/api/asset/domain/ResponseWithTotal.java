package com.tmobile.pacman.api.asset.domain;

public class ResponseWithTotal {

    private String message;

    private ResponseWithCount data;

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public ResponseWithCount getData() {
        return data;
    }

    public void setData(ResponseWithCount data) {
        this.data = data;
    }
}
