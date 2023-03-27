package com.tmobile.pacman.api.admin.domain;

public class NotificationPrefsRequest {
    private String notificationType;
    private String notificationChannelName;
    private String addOrRemove;
    private String updatedBy;

    public String getNotificationType() {
        return notificationType;
    }

    public void setNotificationType(String notificationType) {
        this.notificationType = notificationType;
    }

    public String getNotificationChannelName() {
        return notificationChannelName;
    }

    public void setNotificationChannelName(String notificationChannelName) {
        this.notificationChannelName = notificationChannelName;
    }

    public String getAddOrRemove() {
        return addOrRemove;
    }

    public void setAddOrRemove(String addOrRemove) {
        this.addOrRemove = addOrRemove;
    }

    public String getUpdatedBy() {
        return updatedBy;
    }

    public void setUpdatedBy(String updatedBy) {
        this.updatedBy = updatedBy;
    }

}
