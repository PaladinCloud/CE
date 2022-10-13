package com.tmobile.cso.pacman.inventory.vo;

import java.util.List;

import com.amazonaws.services.ecs.model.Cluster;
import com.amazonaws.services.ecs.model.Tag;

public class ECSClusterVH {
	
	private Cluster cluster;
	private List<Tag> tags;
	
	public ECSClusterVH(Cluster cluster, List<Tag> tags) {
		this.cluster = cluster;
		this.tags = tags;
	}
}
