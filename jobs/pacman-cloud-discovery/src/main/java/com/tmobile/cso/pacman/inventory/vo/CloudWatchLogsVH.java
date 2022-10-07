package com.tmobile.cso.pacman.inventory.vo;

import java.util.ArrayList;
import java.util.List;

import com.amazonaws.services.logs.model.LogGroup;
import com.amazonaws.services.logs.model.MetricFilter;
import com.amazonaws.services.logs.model.MetricTransformation;

public class CloudWatchLogsVH {

	private LogGroup logGroup;
	private List<MetricFilterVH> metricFilterVH;

	
	public CloudWatchLogsVH(LogGroup logGroup, List<MetricFilter> metricFilterList) {
		 this.logGroup =  logGroup;
		 metricFilterVH = new ArrayList<>();
		setMetricFilterDetails(metricFilterList);
	}
	
	public void setMetricFilterDetails (List<MetricFilter> metricFilter) {
		
		metricFilter.forEach(filter -> {
			List<MetricTransformation> metricTransformations = filter.getMetricTransformations();
			if(metricTransformations != null && !metricTransformations.isEmpty()
					&& metricTransformations.size() > 0) {
				metricFilterVH.add( new MetricFilterVH(metricTransformations.get(0).getMetricName(),
						metricTransformations.get(0).getMetricNamespace()
						,metricTransformations.get(0).getMetricValue(),filter));

			}
		});
		
	}
}
