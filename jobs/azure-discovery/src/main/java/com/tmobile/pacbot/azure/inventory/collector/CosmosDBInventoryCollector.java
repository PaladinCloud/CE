package com.tmobile.pacbot.azure.inventory.collector;

import com.microsoft.azure.PagedList;
import com.microsoft.azure.management.Azure;
import com.microsoft.azure.management.cosmosdb.CosmosDBAccount;
import com.microsoft.azure.management.cosmosdb.VirtualNetworkRule;
import com.tmobile.pacbot.azure.inventory.auth.AzureCredentialProvider;
import com.tmobile.pacbot.azure.inventory.vo.AzureVH;
import com.tmobile.pacbot.azure.inventory.vo.CosmosDBVH;
import com.tmobile.pacbot.azure.inventory.vo.SubscriptionVH;
import com.tmobile.pacbot.azure.inventory.vo.VirtualNetworkRuleVH;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Component
public class CosmosDBInventoryCollector implements Collector {
	
	@Autowired
	AzureCredentialProvider azureCredentialProvider;

	private static Logger log = LoggerFactory.getLogger(CosmosDBInventoryCollector.class);

	@Override
	public List<? extends AzureVH> collect() {
		throw new UnsupportedOperationException();
	}

	@Override
	public List<? extends AzureVH> collect(SubscriptionVH subscription) {
		throw new UnsupportedOperationException();
	}

	public List<CosmosDBVH> collect(SubscriptionVH subscription, Map<String, Map<String, String>> tagMap) {
		List<CosmosDBVH> cosmosDBList = new ArrayList<>();
		Azure azure = azureCredentialProvider.getClient(subscription.getTenant(),subscription.getSubscriptionId());
		PagedList<CosmosDBAccount> CosmosDB = azure.cosmosDBAccounts().list();
		for (CosmosDBAccount cosmosDB : CosmosDB) {
			CosmosDBVH cosmosDBVH = new CosmosDBVH();
			cosmosDBVH.setSubscription(subscription.getSubscriptionId());
			cosmosDBVH.setSubscriptionName(subscription.getSubscriptionName());
			cosmosDBVH.setId(cosmosDB.id());
			cosmosDBVH.setKey(cosmosDB.key());
			cosmosDBVH.setName(cosmosDB.name());
			cosmosDBVH.setResourceGroupName(cosmosDB.resourceGroupName());
			cosmosDBVH.setRegion(Util.getRegionValue(subscription,cosmosDB.regionName()));
			cosmosDBVH.setTags(Util.tagsList(tagMap, cosmosDB.resourceGroupName(), cosmosDB.tags()));
			cosmosDBVH.setType(cosmosDB.type());
			cosmosDBVH.setIpRangeFilter(cosmosDB.ipRangeFilter());
			cosmosDBVH.setMultipleWriteLocationsEnabled(cosmosDB.multipleWriteLocationsEnabled());
			cosmosDBVH.setVirtualNetworkRuleList(getVirtualNetworkRule(cosmosDB.virtualNetworkRules()));
			cosmosDBList.add(cosmosDBVH);
		}

		log.info("Target Type : {}  Total: {} ","Cosom DB",cosmosDBList.size());
		return cosmosDBList;
	}

	private List<VirtualNetworkRuleVH> getVirtualNetworkRule(List<VirtualNetworkRule> virtualNetworkRuleList) {
		List<VirtualNetworkRuleVH> virtualNetworkRuleVHlist = new ArrayList<>();
		for (VirtualNetworkRule virtualNetworkRule : virtualNetworkRuleList) {
			VirtualNetworkRuleVH virtualNetworkRuleVH = new VirtualNetworkRuleVH();
			virtualNetworkRuleVH.setId(virtualNetworkRule.id());
			virtualNetworkRuleVH
					.setIgnoreMissingVNetServiceEndpoint(virtualNetworkRule.ignoreMissingVNetServiceEndpoint());
			virtualNetworkRuleVHlist.add(virtualNetworkRuleVH);

		}

		return virtualNetworkRuleVHlist;
	}
}
