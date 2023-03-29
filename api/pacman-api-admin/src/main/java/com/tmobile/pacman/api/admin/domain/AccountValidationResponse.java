package com.tmobile.pacman.api.admin.domain;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.*;

@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class AccountValidationResponse {
    private String projectId;

    private String accountId;

    private String accountName;
    private String tenantId;
    private String type;
    private String validationStatus;
    private String message;
    private String errorDetails;

    private String qualysApiUrl;
    private String qualysUser;

    private String aquaApiUrl;
    private String aquaUser;
}
