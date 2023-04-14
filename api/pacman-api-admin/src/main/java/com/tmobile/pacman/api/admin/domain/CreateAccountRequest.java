package com.tmobile.pacman.api.admin.domain;

import lombok.Data;

@Data
public class CreateAccountRequest {
    private String platform;
    private String accountId;
    private String accountName;
    private String roleName;
    private String projectId;
    private String projectName;
    private String secretData;

    private String tenantId;
    private String tenantName;
    private String tenantSecretData;

    private String qualysApiUrl;
    private String qualysApiUser;
    private String qualysApiPassword;

    private String aquaApiUrl;
    private String aquaClientDomainUrl;
    private String aquaApiUser;
    private String aquaApiPassword;

    private String tenableAPIUrl;
    private String tenableAccessKey;
    private String tenableSecretKey;

}
