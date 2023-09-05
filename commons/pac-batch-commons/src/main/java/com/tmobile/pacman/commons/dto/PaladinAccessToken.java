package com.tmobile.pacman.commons.dto;

public class  PaladinAccessToken {
    private String token;
    private long expiresAt;

    public PaladinAccessToken(String token, long expiresAt){
        this.token = token;
        this.expiresAt = expiresAt;
    }
    public String getToken() {
        return token;
    }
    public void setToken(String token) {
        this.token = token;
    }
    public long getExpiresAt() {
        return expiresAt;
    }
    public void setExpiresAt(long expiresAt) {
        this.expiresAt = expiresAt;
    }
    public String toString(){
        return "Token:"+token+" ,ExpiresIn (sec)"+ (expiresAt- System.currentTimeMillis())/1000;
    }

}
