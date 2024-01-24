package com.tmobile.cso.pacman.inventory.vo;

import com.amazonaws.services.cloudtrail.model.Trail;

import java.util.List;

public class CloudTrailVH {

    private final Trail trail;
    private final boolean loggingEnabled;
    private final List<CloudTrailEventSelectorVH> evenSelectorList;
    private final String latestCloudWatchLogsDeliveryTime;

    public CloudTrailVH(Trail trail, boolean loggingEnabled,
                        List<CloudTrailEventSelectorVH> evenSelectorList, String latestCloudWatchLogsDeliveryTime) {
        this.trail = trail;
        this.loggingEnabled = loggingEnabled;
        this.evenSelectorList = evenSelectorList;
        this.latestCloudWatchLogsDeliveryTime = latestCloudWatchLogsDeliveryTime;
    }
}
