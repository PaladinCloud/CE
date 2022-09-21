package com.tmobile.pacbot.azure.inventory.collector;

import com.microsoft.azure.PagedList;
import com.microsoft.azure.management.Azure;
import com.microsoft.azure.management.storage.PublicEndpoints;
import com.microsoft.azure.management.storage.StorageAccount;
import com.tmobile.pacbot.azure.inventory.auth.AzureCredentialProvider;
import com.tmobile.pacbot.azure.inventory.vo.StorageAccountVH;
import com.tmobile.pacbot.azure.inventory.vo.SubscriptionVH;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
public class StorageAccountInventoryCollector {

	@Autowired
	AzureCredentialProvider azureCredentialProvider;

	private static Logger log = LoggerFactory.getLogger(StorageAccountInventoryCollector.class);

	public List<StorageAccountVH> fetchStorageAccountDetails(SubscriptionVH subscription,
			Map<String, Map<String, String>> tagMap) {
		List<StorageAccountVH> storageAccountList = new ArrayList<StorageAccountVH>();

		Azure azure = azureCredentialProvider.getClient(subscription.getTenant(), subscription.getSubscriptionId());
		PagedList<StorageAccount> storageAccounts = azure.storageAccounts().list();

		for (StorageAccount storageAccount : storageAccounts) {
			StorageAccountVH storageAccountVH = new StorageAccountVH();
			storageAccountVH.setResourceGroupName(storageAccount.resourceGroupName());
			storageAccountVH.setKind(storageAccount.kind().toString());
			storageAccountVH.setCanAccessFromAzureServices(storageAccount.canAccessFromAzureServices());
			storageAccountVH.setIpAddressesWithAccess(storageAccount.ipAddressesWithAccess());
			storageAccountVH.setId(storageAccount.id());
			storageAccountVH.setIpAddressRangesWithAccess(storageAccount.ipAddressRangesWithAccess());
			storageAccountVH.setAccessAllowedFromAllNetworks(storageAccount.isAccessAllowedFromAllNetworks());
			storageAccountVH.setAzureFilesAadIntegrationEnabled(storageAccount.isAzureFilesAadIntegrationEnabled());
			storageAccountVH.setHnsEnabled(storageAccount.isHnsEnabled());
			storageAccountVH.setName(storageAccount.name());
			storageAccountVH.setRegionName(storageAccount.regionName());
			storageAccountVH.setRegion(storageAccount.regionName());
			storageAccountVH.setNetworkSubnetsWithAccess(storageAccount.networkSubnetsWithAccess());
			storageAccountVH.setSystemAssignedManagedServiceIdentityPrincipalId(
					storageAccount.systemAssignedManagedServiceIdentityPrincipalId());
			storageAccountVH.setSystemAssignedManagedServiceIdentityTenantId(
					storageAccount.systemAssignedManagedServiceIdentityTenantId());
			storageAccountVH.setTags(Util.tagsList(tagMap, storageAccount.resourceGroupName(), storageAccount.tags()));
			storageAccountVH.setSubscription(subscription.getSubscriptionId());
			storageAccountVH.setSubscriptionName(subscription.getSubscriptionName());
			endPointDetails(storageAccount.endPoints(), storageAccountVH);
			if (storageAccount.inner() != null) {
				if (storageAccount.inner().encryption() != null
						&& storageAccount.inner().encryption().keyVaultProperties() != null) {
					storageAccountVH
							.setCustomerManagedKey(storageAccount.inner().encryption().keyVaultProperties().keyName());
				}
				if(storageAccount.inner().networkRuleSet().bypass()!=null){
					storageAccountVH.setNetworkRuleBypass(storageAccount.inner().networkRuleSet().bypass().toString());
					log.info("storage Account Bypass to string{}",storageAccount.inner().networkRuleSet().bypass().toString());
				}
				if(storageAccount.inner().networkRuleSet().defaultAction()!=null){
					storageAccountVH.setDefaultAction(storageAccount.inner().networkRuleSet().defaultAction().name());
				}


			}
			if(storageAccount.minimumTlsVersion()!=null){
				storageAccountVH.setMinimumTlsVersion(storageAccount.minimumTlsVersion());
				log.info("minTlsVersion",storageAccount.minimumTlsVersion());
			}

			storageAccountVH.setBlobPublicAccessAllowed(storageAccount.isBlobPublicAccessAllowed());
			storageAccountList.add(storageAccountVH);
		}
		log.info("Target Type : {}  Total: {} ", "Storage Account", storageAccountList.size());
		return storageAccountList;
	}

	private void endPointDetails(PublicEndpoints endpoints, StorageAccountVH storageAccountVH) {
		Map<String, String> endpointsMap = new HashMap<String, String>();
		endpointsMap.put("blobEndPoint", endpoints.primary().blob());
		endpointsMap.put("fileEndPoint", endpoints.primary().file());
		endpointsMap.put("queueEndPoint", endpoints.primary().queue());
		endpointsMap.put("tableEndPoint", endpoints.primary().table());
		endpointsMap.put("dfsEndPoint", endpoints.primary().dfs());
		endpointsMap.put("webEndPoint", endpoints.primary().web());
		storageAccountVH.setEndpointsMap(endpointsMap);

	}
}
