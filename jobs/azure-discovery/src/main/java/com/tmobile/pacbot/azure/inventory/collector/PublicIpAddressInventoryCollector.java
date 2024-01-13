package com.tmobile.pacbot.azure.inventory.collector;

import com.microsoft.azure.PagedList;
import com.microsoft.azure.management.Azure;
import com.microsoft.azure.management.network.PublicIPAddress;
import com.tmobile.pacbot.azure.inventory.auth.AzureCredentialProvider;
import com.tmobile.pacbot.azure.inventory.vo.AzureVH;
import com.tmobile.pacbot.azure.inventory.vo.PublicIpAddressVH;
import com.tmobile.pacbot.azure.inventory.vo.SubscriptionVH;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Component
public class PublicIpAddressInventoryCollector implements Collector {

    private static final Logger log = LoggerFactory.getLogger(PublicIpAddressInventoryCollector.class);
    @Autowired
    AzureCredentialProvider azureCredentialProvider;

    public List<PublicIpAddressVH> collect(SubscriptionVH subscription,
                                           Map<String, Map<String, String>> tagMap) {
        List<PublicIpAddressVH> publicIpAddressList = new ArrayList<PublicIpAddressVH>();

        Azure azure = azureCredentialProvider.getClient(subscription.getTenant(), subscription.getSubscriptionId());
        PagedList<PublicIPAddress> publicIPAddresses = azure.publicIPAddresses().list();
        for (PublicIPAddress publicIPAddress : publicIPAddresses) {
            PublicIpAddressVH publicIpAddressVH = new PublicIpAddressVH();
            publicIpAddressVH.setId(publicIPAddress.id());
            publicIpAddressVH.setName(publicIPAddress.name());
            publicIpAddressVH.setResourceGroupName(publicIPAddress.resourceGroupName());
            publicIpAddressVH.setType(publicIPAddress.type());
            publicIpAddressVH
                    .setTags(Util.tagsList(tagMap, publicIPAddress.resourceGroupName(), publicIPAddress.tags()));
            publicIpAddressVH.setSubscription(subscription.getSubscriptionId());
            publicIpAddressVH.setSubscriptionName(subscription.getSubscriptionName());
            publicIpAddressVH.setIdleTimeoutInMinutes(publicIPAddress.idleTimeoutInMinutes());
            publicIpAddressVH.setFqdn(publicIPAddress.fqdn());
            publicIpAddressVH.setIpAddress(publicIPAddress.ipAddress());
            publicIpAddressVH.setKey(publicIPAddress.key());
            publicIpAddressVH.setRegionName(publicIPAddress.regionName());
            publicIpAddressVH.setRegion(Util.getRegionValue(subscription, publicIPAddress.regionName()));
            publicIpAddressVH.setReverseFqdn(publicIPAddress.reverseFqdn());
            publicIpAddressVH.setVersion(publicIPAddress.version().toString());
            publicIpAddressList.add(publicIpAddressVH);

        }

        log.info("Target Type : {}  Total: {} ", "PublicIPAddress", publicIpAddressList.size());
        return publicIpAddressList;
    }

    @Override
    public List<? extends AzureVH> collect() {
        throw new UnsupportedOperationException();
    }

    @Override
    public List<? extends AzureVH> collect(SubscriptionVH subscription) {
        throw new UnsupportedOperationException();
    }
}
