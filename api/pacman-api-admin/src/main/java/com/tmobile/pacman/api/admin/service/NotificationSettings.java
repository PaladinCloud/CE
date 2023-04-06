package com.tmobile.pacman.api.admin.service;

import com.tmobile.pacman.api.admin.domain.NotificationPrefsRequest;


import java.util.List;
import java.util.Map;

public interface NotificationSettings {
    Map<String,Object> getNotificationSettings();
    void updateNotificationSettings(List<NotificationPrefsRequest> notificationPreferencesList) throws Exception;

    Map<String,Object> getNotificationSettingsAndConfigs();
}
