package com.tmobile.pacbot.azure.inventory;

import java.text.SimpleDateFormat;
import java.util.*;

import com.tmobile.pacman.commons.database.RDSDBManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.microsoft.azure.PagedList;
import com.microsoft.azure.management.Azure.Authenticated;
import com.microsoft.azure.management.resources.Subscription;
import com.tmobile.pacbot.azure.inventory.auth.AzureCredentialProvider;
import com.tmobile.pacbot.azure.inventory.file.AssetFileGenerator;
import com.tmobile.pacbot.azure.inventory.file.S3Uploader;
import com.tmobile.pacbot.azure.inventory.vo.SubscriptionVH;

import static com.tmobile.pacbot.azure.inventory.ErrorManageUtil.triggerNotificationforPermissionDenied;

@Component
public class AzureFetchOrchestrator {
	
	@Autowired
	AssetFileGenerator fileGenerator;
	
	@Autowired
	AzureCredentialProvider azureCredentialProvider;

	@Autowired
	RDSDBManager rdsdbManager;
	
	/** The s 3 uploader. */
	@Autowired
	S3Uploader s3Uploader;
	
	@Value("${file.path}")
	private String filePath ;

	@Value("${tenants:}")
	private String tenants;
	
	@Value("${s3}")
	private String s3Bucket ;
	
	@Value("${s3.data}")
	private String s3Data ;
	
	@Value("${s3.processed}")
	private String s3Processed ;
	
	@Value("${s3.region}")
	private String s3Region ;
	
	/** The log. */
	private static Logger log = LoggerFactory.getLogger(AzureFetchOrchestrator.class);
	
	public Map<String, Object> orchestrate(){
		
		try{
			List<SubscriptionVH> subscriptions = fetchSubscriptions();
			if(subscriptions.isEmpty()){
				ErrorManageUtil.uploadError("all", "all", "all", "Error fetching subscription Info ");
				return ErrorManageUtil.formErrorCode();
			}
			
			log.info("Start : FIle Generation");
			fileGenerator.generateFiles(subscriptions,filePath);
			log.info("End : FIle Generation");
			
			log.info("Start : Backup Current Files");
			s3Uploader.backUpFiles(s3Bucket, s3Region, s3Data, s3Processed+ "/"+ new SimpleDateFormat("yyyyMMdd-HHmmss").format(new Date()));
			log.info("End : Backup Current Files");
		
			log.info("Start : Upload Files to S3");
			s3Uploader.uploadFiles(s3Bucket,s3Data,s3Region,filePath);
			log.info("End : Upload Files to S3");
		    
			
			
		}catch(Exception e){
		log.info(e.getMessage());
		}
		return null;
	}
	
	private List<SubscriptionVH> fetchSubscriptions() {

		List<SubscriptionVH> subscriptionList  = new ArrayList<>();
		String accountQuery = "SELECT accountId,accountName,accountStatus FROM cf_Accounts where platform = 'azure'";
		List<Map<String,String>> accounts=rdsdbManager.executeQuery(accountQuery);
		List<String> tenantList = new ArrayList<>();
		for (Map<String,String>account:accounts) {
			tenantList.add(account.get("accountId"));
		}
		for(String tenant : tenantList){
			try {
				Authenticated azure = azureCredentialProvider.authenticate(tenant);
				PagedList<Subscription> subscriptions = azure.subscriptions().list();
				for (Subscription subscription : subscriptions) {
					SubscriptionVH subscriptionVH = new SubscriptionVH();
					subscriptionVH.setTenant(tenant);
					subscriptionVH.setSubscriptionId(subscription.subscriptionId());
					subscriptionVH.setSubscriptionName(subscription.displayName());
					subscriptionList.add(subscriptionVH);
				}
			}catch (Exception e)
			{
				rdsdbManager.executeUpdate("UPDATE cf_AzureTenantSubscription SET subscriptionStatus='offline' WHERE tenant=?", Arrays.asList(tenant));
				ErrorManageUtil.uploadError(tenant,"all","all",e.getMessage());
				triggerNotificationforPermissionDenied();
			}
			populateTenantsSubscription(tenant,subscriptionList);
		}
		log.info("Total Subscription in Scope : {}",subscriptionList.size());
		log.info("Subscriptions : {}",subscriptionList);
		return subscriptionList;
	}

	private void populateTenantsSubscription(String tenant, List<SubscriptionVH> subscriptionList) {
		String query="INSERT IGNORE INTO cf_AzureTenantSubscription (tenant,subscription, subscriptionName) VALUES(?,?,?)";
		List tenantSubscription=new ArrayList<>();
		tenantSubscription.add(tenant);
		for(SubscriptionVH subscriptionVH:subscriptionList){
			tenantSubscription.add(subscriptionVH.getSubscriptionId());
			tenantSubscription.add(subscriptionVH.getSubscriptionName());
			rdsdbManager.executeUpdate(query,tenantSubscription);
			tenantSubscription.remove(subscriptionVH.getSubscriptionId());
			tenantSubscription.remove(subscriptionVH.getSubscriptionName());
		}
	}
}
