package com.tmobile.cso.pacman.inventory.vo;

import java.util.List;

import com.amazonaws.services.docdb.model.DBCluster;


/**
 * The Class DocumentDBVH.
 */
public class DocumentDBVH {
	

	
	/** The DB cluster list. */
	List<DBCluster> clusters;
	
	/**
	 * Instantiates a new DocumentDBVH VH.
	 *
	 * @param clusters the List of DB Cluster
	 */
	public DocumentDBVH(List<DBCluster> clusters){
		this.clusters = clusters;
	}
	
}
