package com.tmobile.cso.pacman.inventory.vo;

import java.util.List;

import com.amazonaws.services.cloudtrail.model.Trail;

public class CloudTrailVH {
	private Trail trail;
	private boolean logginEnabled;
	private List<CloudTrailEventSelectorVH> evenSelectorList;
	
	public CloudTrailVH(Trail trail, boolean logginEnabled, 
			List<CloudTrailEventSelectorVH> evenSelectorList) {
		this.trail = trail;
		this.logginEnabled = logginEnabled;
		this.evenSelectorList = evenSelectorList;
	}

}
