package com.tmobile.pacman.api.admin.dto;

import lombok.Data;

@Data
public class PluginNotificationDto {
    private String action;
    private String doneBy;
    private String pluginName;
    private String summary;
}
