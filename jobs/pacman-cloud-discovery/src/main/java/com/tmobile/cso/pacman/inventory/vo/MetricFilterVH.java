package com.tmobile.cso.pacman.inventory.vo;

import java.util.List;

import com.amazonaws.services.logs.model.MetricFilter;

public class MetricFilterVH {
	
	private String metricName;
	private String metricNamespace;
	private String metricValue;
	private MetricFilter metricFilter;
	
	public MetricFilterVH(String metricName, String metricNamespace, String metricValue,
			MetricFilter metricFilter) {
		super();
		this.metricName = metricName;
		this.metricNamespace = metricNamespace;
		this.metricValue = metricValue;
		this.metricFilter = metricFilter;
	}
	
	

}
