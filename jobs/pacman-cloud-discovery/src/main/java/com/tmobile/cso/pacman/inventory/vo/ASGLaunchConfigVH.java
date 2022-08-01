package com.tmobile.cso.pacman.inventory.vo;


import com.amazonaws.services.autoscaling.model.LaunchConfiguration;

public class ASGLaunchConfigVH {
	

	private LaunchConfiguration lauchConfig;
	
	private String securityGroups;

	public ASGLaunchConfigVH(LaunchConfiguration lauchConfig, String securityGroups) {
		this.lauchConfig = lauchConfig;
		this.securityGroups = securityGroups;
	}

	public LaunchConfiguration getLauchConfig() {
		return lauchConfig;
	}

	public void setLauchConfig(LaunchConfiguration lauchConfig) {
		this.lauchConfig = lauchConfig;
	}

	public String getSecurityGroups() {
		return securityGroups;
	}

	public void setSecurityGroups(String securityGroups) {
		this.securityGroups = securityGroups;
	}
	
	
	
}
