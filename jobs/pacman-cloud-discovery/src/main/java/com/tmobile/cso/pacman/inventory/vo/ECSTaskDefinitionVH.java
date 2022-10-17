package com.tmobile.cso.pacman.inventory.vo;

import java.util.List;

import com.amazonaws.services.ecs.model.TaskDefinition;

public class ECSTaskDefinitionVH {
	
	private TaskDefinition taskDef;
	private List<com.amazonaws.services.ecs.model.Tag> tags;
	
	public ECSTaskDefinitionVH(TaskDefinition taskDef, List<com.amazonaws.services.ecs.model.Tag> tags) {
		this.taskDef = taskDef;
		this.tags = tags;
	}

}
