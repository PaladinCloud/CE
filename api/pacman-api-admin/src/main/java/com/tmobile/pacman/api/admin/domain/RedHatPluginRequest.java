package com.tmobile.pacman.api.admin.domain;

import lombok.Data;

@Data
public class RedHatPluginRequest {

    private String redhatAccountId;
    private String redhatAccountName;
    private String redhatToken;
    private String owner;
}
