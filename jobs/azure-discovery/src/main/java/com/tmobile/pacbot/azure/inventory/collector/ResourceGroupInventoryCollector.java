package com.tmobile.pacbot.azure.inventory.collector;

import com.microsoft.azure.PagedList;
import com.microsoft.azure.management.Azure;
import com.microsoft.azure.management.resources.ResourceGroup;
import com.tmobile.pacbot.azure.inventory.auth.AzureCredentialProvider;
import com.tmobile.pacbot.azure.inventory.vo.AzureVH;
import com.tmobile.pacbot.azure.inventory.vo.ResourceGroupVH;
import com.tmobile.pacbot.azure.inventory.vo.SubscriptionVH;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Component
public class ResourceGroupInventoryCollector implements Collector {

    private static Logger log = LoggerFactory.getLogger(ResourceGroupInventoryCollector.class);
    @Autowired
    AzureCredentialProvider azureCredentialProvider;

    public List<ResourceGroupVH> collect(SubscriptionVH subscription) {
        List<ResourceGroupVH> resourceGroupList = new ArrayList<>();
        Azure azure = azureCredentialProvider.getClient(subscription.getTenant(), subscription.getSubscriptionId());
        PagedList<ResourceGroup> resourceGroups = azure.resourceGroups().list();
        for (ResourceGroup resourceGroup : resourceGroups) {
            ResourceGroupVH resourceGroupVH = new ResourceGroupVH();
            resourceGroupVH.setSubscription(subscription.getSubscriptionId());
            resourceGroupVH.setSubscriptionName(subscription.getSubscriptionName());
            resourceGroupVH.setId(resourceGroup.id());
            resourceGroupVH.setResourceGroupName(resourceGroup.name());
            resourceGroupVH.setKey(resourceGroup.key());
            resourceGroupVH.setType(resourceGroup.type());
            resourceGroupVH.setProvisioningState(resourceGroup.provisioningState());
            resourceGroupVH.setRegionName(resourceGroup.regionName());
            resourceGroupVH.setRegion(Util.getRegionValue(subscription, resourceGroup.regionName()));
            resourceGroupVH.setTags(resourceGroup.tags());
            resourceGroupVH.setName(resourceGroup.name());
            resourceGroupList.add(resourceGroupVH);
        }
        log.info("Target Type : {}  Total: {} ", "ResourceGroup", resourceGroupList.size());
        return resourceGroupList;
    }


    @Override
    public List<? extends AzureVH> collect() {
        throw new UnsupportedOperationException();
    }

    @Override
    public List<? extends AzureVH> collect(SubscriptionVH subscription, Map<String, Map<String, String>> tagMap) {
        throw new UnsupportedOperationException();
    }
}
