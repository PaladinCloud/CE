package com.tmobile.pacbot.azure.inventory.collector;

import com.microsoft.azure.PagedList;
import com.microsoft.azure.management.Azure;
import com.microsoft.azure.management.network.LoadBalancer;
import com.tmobile.pacbot.azure.inventory.auth.AzureCredentialProvider;
import com.tmobile.pacbot.azure.inventory.vo.AzureVH;
import com.tmobile.pacbot.azure.inventory.vo.LoadBalancerVH;
import com.tmobile.pacbot.azure.inventory.vo.SubscriptionVH;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Component
public class LoadBalancerInventoryCollector implements Collector {

    private static final Logger log = LoggerFactory.getLogger(LoadBalancerInventoryCollector.class);
    @Autowired
    AzureCredentialProvider azureCredentialProvider;

    @Override
    public List<? extends AzureVH> collect() {
        throw new UnsupportedOperationException();
    }

    @Override
    public List<? extends AzureVH> collect(SubscriptionVH subscription) {
        throw new UnsupportedOperationException();
    }

    public List<LoadBalancerVH> collect(SubscriptionVH subscription,
                                        Map<String, Map<String, String>> tagMap) {
        List<LoadBalancerVH> loadBalancerList = new ArrayList<>();

        Azure azure = azureCredentialProvider.getClient(subscription.getTenant(), subscription.getSubscriptionId());
        PagedList<LoadBalancer> loadBalancers = azure.loadBalancers().list();
        for (LoadBalancer loadBalancer : loadBalancers) {
            LoadBalancerVH loadBalancerVH = new LoadBalancerVH();
            loadBalancerVH.setHashCode(loadBalancer.hashCode());
            loadBalancerVH.setId(loadBalancer.id());
            loadBalancerVH.setKey(loadBalancer.key());
            loadBalancerVH.setPublicIPAddressIds(loadBalancer.publicIPAddressIds());
            loadBalancerVH.setName(loadBalancer.name());
            loadBalancerVH.setRegionName(loadBalancer.regionName());
            loadBalancerVH.setRegion(Util.getRegionValue(subscription, loadBalancer.regionName()));
            loadBalancerVH.setResourceGroupName(loadBalancer.resourceGroupName());
            loadBalancerVH.setTags(Util.tagsList(tagMap, loadBalancer.resourceGroupName(), loadBalancer.tags()));
            loadBalancerVH.setType(loadBalancer.type());
            loadBalancerVH.setSubscription(subscription.getSubscriptionId());
            loadBalancerVH.setSubscriptionName(subscription.getSubscriptionName());
            List<String> backendIpConfigurationIds = loadBalancer.inner().backendAddressPools().stream()
                    .flatMap(pool -> pool.backendIPConfigurations() == null ? null : pool.backendIPConfigurations().stream())
                    .map(backendIpConfig -> backendIpConfig.id() == null ? null : backendIpConfig.id())
                    .collect(Collectors.toList());
            loadBalancerVH.setBackendPoolInstances(backendIpConfigurationIds);
            loadBalancerList.add(loadBalancerVH);

        }

        log.info("Target Type : {}  Total: {} ", "LoadBalancer", loadBalancerList.size());
        return loadBalancerList;
    }
}
