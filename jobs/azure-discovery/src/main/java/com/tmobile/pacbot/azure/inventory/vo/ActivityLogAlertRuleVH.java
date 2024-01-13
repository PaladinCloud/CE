package com.tmobile.pacbot.azure.inventory.vo;

import java.util.List;

public class ActivityLogAlertRuleVH extends AzureVH {
    private List<ActivityLogVH> activityLogAlerts;

    public List<ActivityLogVH> getActivityLogAlerts() {
        return activityLogAlerts;
    }

    public void setActivityLogAlerts(List<ActivityLogVH> activityLogAlerts) {
        this.activityLogAlerts = activityLogAlerts;
    }

}
