package com.tmobile.pacbot.azure.inventory.collector;

import com.google.gson.*;
import com.microsoft.azure.PagedList;
import com.microsoft.azure.management.Azure;
import com.microsoft.azure.management.network.Network;
import com.tmobile.pacbot.azure.inventory.auth.AzureCredentialProvider;
import com.tmobile.pacbot.azure.inventory.vo.AzureVH;
import com.tmobile.pacbot.azure.inventory.vo.SubnetVH;
import com.tmobile.pacbot.azure.inventory.vo.SubscriptionVH;
import com.tmobile.pacman.commons.utils.CommonUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.tmobile.pacbot.azure.inventory.collector.Util.getResourceGroupNameFromId;

@Component
public class SubnetInventoryCollector implements Collector {

    private static final Logger log = LoggerFactory.getLogger(SubnetInventoryCollector.class);
    private final String apiUrlTemplate = "https://management.azure.com/subscriptions/%s/resourceGroups/%s/providers/Microsoft.Network/virtualNetworks/%s/subnets?api-version=2019-07-01";
    @Autowired
    AzureCredentialProvider azureCredentialProvider;

    @Override
    public List<? extends AzureVH> collect() {
        throw new UnsupportedOperationException();
    }

    public List<SubnetVH> collect(SubscriptionVH subscription) {
        List<SubnetVH> subnetList = new ArrayList<>();
        String accessToken = azureCredentialProvider.getToken(subscription.getTenant());
        Azure azure = azureCredentialProvider.authenticate(subscription.getTenant(), subscription.getSubscriptionId());
        PagedList<Network> networks = azure.networks().list();
        for (Network network : networks) {
            String url = String.format(apiUrlTemplate, URLEncoder.encode(subscription.getSubscriptionId()),
                    URLEncoder.encode(network.resourceGroupName()), URLEncoder.encode(network.name()));
            try {
                String response = CommonUtils.doHttpGet(url, "Bearer", accessToken);
                JsonObject responseObj = new JsonParser().parse(response).getAsJsonObject();
                JsonArray subnetObjects = responseObj.getAsJsonArray("value");
                for (JsonElement subnetElement : subnetObjects) {
                    SubnetVH subnetVH = new SubnetVH();
                    subnetVH.setSubscription(subscription.getSubscriptionId());
                    subnetVH.setSubscriptionName(subscription.getSubscriptionName());
                    JsonObject subnetObject = subnetElement.getAsJsonObject();
                    JsonObject properties = subnetObject.getAsJsonObject("properties");
                    subnetVH.setId(subnetObject.get("id").getAsString());
                    subnetVH.setRegion(Util.getRegionValue(subscription, network.regionName()));
                    subnetVH.setResourceGroupName(getResourceGroupNameFromId(subnetVH.getId()));
                    subnetVH.setName(subnetObject.get("name").getAsString());
                    subnetVH.setType(subnetObject.get("type").getAsString());
                    subnetVH.setEtag(subnetObject.get("etag").getAsString());
                    if (properties != null) {
                        HashMap<String, Object> propertiesMap = new Gson().fromJson(properties.toString(),
                                HashMap.class);
                        if (propertiesMap.get("ipConfigurations") != null) {
                            subnetVH.setIpConfigurations((List<Map<String, Object>>) propertiesMap.get("ipConfigurations"));
                        }
                        if (propertiesMap.get("addressPrefix") != null) {
                            subnetVH.setAddressPrefix(propertiesMap.get("addressPrefix").toString());
                        }
                        if (propertiesMap.get("privateLinkServiceNetworkPolicies") != null) {
                            subnetVH.setPrivateLinkServiceNetworkPolicies(
                                    propertiesMap.get("privateLinkServiceNetworkPolicies").toString());
                        }
                        if (propertiesMap.get("provisioningState") != null) {
                            subnetVH.setProvisioningState(propertiesMap.get("provisioningState").toString());
                        }
                        if (propertiesMap.get("provisioningState") != null) {
                            subnetVH.setPrivateEndpointNetworkPolicies(
                                    propertiesMap.get("privateEndpointNetworkPolicies").toString());
                        }
                    }

                    subnetList.add(subnetVH);
                }
            } catch (Exception exception) {
                String errorMessage = String.format("Error occurred while collecting SubnetInventory for subscriptionId: %s, subscriptionName: %s", subscription.getSubscriptionId(), subscription.getSubscriptionName());
                log.error(errorMessage, exception);
                Util.eCount.getAndIncrement();
                log.debug("Current error count after exception occurred in SubnetInventory collector: {}", Util.eCount.get());
            }
        }

        log.info("Target Type : {}  Total: {} ", "Subnet", subnetList.size());
        return subnetList;
    }

    @Override
    public List<? extends AzureVH> collect(SubscriptionVH subscription, Map<String, Map<String, String>> tagMap) {
        throw new UnsupportedOperationException();
    }
}
