package com.tmobile.cso.pacman.inventory.vo;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;

import com.amazonaws.services.appflow.model.FlowDefinition;
import com.amazonaws.services.elasticfilesystem.model.FileSystemDescription;
import com.amazonaws.services.elasticfilesystem.model.Tag;


/**
 * The Class appFlow.
 */
public class AppFlowVH {
	
	/** The efs. */
	FlowDefinition flowDef;
	
	/** The tags. */
	List<Tag> tags;
	
	/** kms arn */
	String kmsArn;
	/**
	 * Instantiates a new appflow VH.
	 *
	 * @param  the flowDef
	 * @param tags the tags
	 */
	public AppFlowVH(FlowDefinition flowDef, String kmsArn){
		this.flowDef = flowDef;
		this.tags = new ArrayList<>();
		if(flowDef != null) {
			Iterator<Entry<String, String>> it = flowDef.getTags().entrySet().iterator();
			while(it.hasNext()){
				Entry<String, String> entry = it.next();
				Tag tag = new Tag();
				tag.setKey(entry.getKey());
				tag.setValue(entry.getValue());
				tags.add(tag);
			}
		}
		
		this.kmsArn = kmsArn;
	}

}
