package com.tmobile.cso.pacman.inventory.vo;

import java.util.List;

import com.amazonaws.services.autoscaling.model.AutoScalingGroup;

public class ASGVH {

	private AutoScalingGroup asg;

	private List<ASGLaunchConfigVH> lauchConfigList;

	public ASGVH(AutoScalingGroup asg, List<ASGLaunchConfigVH> lauchConfigList) {
		this.asg = asg;
		this.lauchConfigList = lauchConfigList;

	}

	public AutoScalingGroup getAsg() {
		return asg;
	}

	public void setAsg(AutoScalingGroup asg) {
		this.asg = asg;
	}

	public List<ASGLaunchConfigVH> getLauchConfigList() {
		return lauchConfigList;
	}

	public void setLauchConfigList(List<ASGLaunchConfigVH> lauchConfigList) {
		this.lauchConfigList = lauchConfigList;
	}

}
