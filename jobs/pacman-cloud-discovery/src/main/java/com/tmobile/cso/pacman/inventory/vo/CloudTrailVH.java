package com.tmobile.cso.pacman.inventory.vo;

import com.amazonaws.services.cloudtrail.model.Trail;

public class CloudTrailVH {
	private Trail trail;
	private boolean logginEnabled;
	
	public CloudTrailVH(Trail trail, boolean logginEnabled) {
		this.trail = trail;
		this.logginEnabled = logginEnabled;
	}

}
