package com.tmobile.pacman.api.admin.domain;

import lombok.Data;

@Data
public class CognitoUserResponse {

    private String firstName;
    private String lastName;
    private String email;
}
