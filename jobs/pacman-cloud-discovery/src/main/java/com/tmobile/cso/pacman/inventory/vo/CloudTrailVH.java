package com.tmobile.cso.pacman.inventory.vo;

import java.util.Date;
import java.util.List;

import com.amazonaws.services.cloudtrail.model.Trail;

public class CloudTrailVH {
	private Trail trail;
	private boolean logginEnabled;
	private List<CloudTrailEventSelectorVH> evenSelectorList;
	private String latestCloudWatchLogsDeliveryTime;
	
	public CloudTrailVH(Trail trail, boolean logginEnabled, 
			List<CloudTrailEventSelectorVH> evenSelectorList,String latestCloudWatchLogsDeliveryTime) {
		this.trail = trail;
		this.logginEnabled = logginEnabled;
		this.evenSelectorList = evenSelectorList;
		this.latestCloudWatchLogsDeliveryTime=latestCloudWatchLogsDeliveryTime;
	}

}
