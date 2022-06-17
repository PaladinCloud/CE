package com.tmobile.pacbot.gcp.inventory;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.tmobile.pacbot.gcp.inventory.config.ConfigUtil;
import com.tmobile.pacman.commons.jobs.PacmanJob;

/**
 * The Class InventoryCollectionJob.
 */
@PacmanJob(methodToexecute="execute",jobName="GCP Data Collector", desc="Job to fetch gcp info and load to S3" ,priority=5)
public class GCPDiscoveryJob {
	
	/** The log. */
	private static final Logger log = LoggerFactory.getLogger(GCPDiscoveryJob.class);
	/**
	 * The main method.
	 *
04:46 PM
so npm install should be run manually
	 * @param args the arguments
	 */
	public static void main(String[] args){
		Map<String,String> params = new HashMap<>();
		Arrays.stream(args).forEach(obj-> {
			String[] keyValue = obj.split("[:]");
			params.put(keyValue[0], keyValue[1]);
		});
		execute(params);
	}
	
	/**
	 * Execute.
	 *
	 * @param params the params
	 * @return 
	 */
	public static Map<String, Object> execute(Map<String,String> params){
		try {
			ConfigUtil.setConfigProperties(params.get(InventoryConstants.CONFIG_CREDS));
			if( !(params==null || params.isEmpty())){
				params.forEach((k,v) -> System.setProperty(k, v));
			}
		} catch (Exception e) {
			log.error("Error fetching config", e);
			ErrorManageUtil.uploadError("all", "all", "all", "Error fetching config "+ e.getMessage());
			//return ErrorManageUtil.formErrorCode();
		}
		return GCPDiscoveryApplication.collect( new String[]{});
	}
}
