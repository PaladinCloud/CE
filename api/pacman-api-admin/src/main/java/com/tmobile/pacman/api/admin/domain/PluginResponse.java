package com.tmobile.pacman.api.admin.domain;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class PluginResponse {

    private String validationStatus;
    private String message;
    private String errorDetails;
}
