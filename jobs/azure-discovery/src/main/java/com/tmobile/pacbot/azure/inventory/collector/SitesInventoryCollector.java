package com.tmobile.pacbot.azure.inventory.collector;

import com.google.gson.*;
import com.tmobile.pacbot.azure.inventory.auth.AzureCredentialProvider;
import com.tmobile.pacbot.azure.inventory.vo.AzureVH;
import com.tmobile.pacbot.azure.inventory.vo.SitesVH;
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

@Component
public class SitesInventoryCollector implements Collector {

    private static final Logger log = LoggerFactory.getLogger(SitesInventoryCollector.class);
    private final String apiUrlTemplate = "https://management.azure.com/subscriptions/%s/providers/Microsoft.Network/vpnSites?api-version=2019-06-01";
    @Autowired
    AzureCredentialProvider azureCredentialProvider;

    @Override
    public List<? extends AzureVH> collect() {
        throw new UnsupportedOperationException();
    }

    public List<SitesVH> collect(SubscriptionVH subscription) {
        List<SitesVH> sitesList = new ArrayList<SitesVH>();
        String accessToken = azureCredentialProvider.getToken(subscription.getTenant());

        String url = String.format(apiUrlTemplate, URLEncoder.encode(subscription.getSubscriptionId()));
        try {

            String response = CommonUtils.doHttpGet(url, "Bearer", accessToken);
            JsonObject responseObj = new JsonParser().parse(response).getAsJsonObject();
            JsonArray sitesObjects = responseObj.getAsJsonArray("value");
            if (sitesObjects != null) {
                for (JsonElement sitesElement : sitesObjects) {
                    SitesVH sitesVH = new SitesVH();
                    JsonObject sitesObject = sitesElement.getAsJsonObject();
                    sitesVH.setSubscription(subscription.getSubscriptionId());
                    sitesVH.setSubscriptionName(subscription.getSubscriptionName());
                    sitesVH.setRegion(Util.getRegionValue(subscription, sitesObject.get("location").getAsString()));
                    sitesVH.setResourceGroupName(Util.getResourceGroupNameFromId(sitesObject.get("id").getAsString()));
                    sitesVH.setId(sitesObject.get("id").getAsString());
                    sitesVH.setEtag(sitesObject.get("etag").getAsString());
                    sitesVH.setLocation(sitesObject.get("location").getAsString());
                    sitesVH.setName(sitesObject.get("name").getAsString());
                    sitesVH.setType(sitesObject.get("type").getAsString());
                    JsonObject properties = sitesObject.getAsJsonObject("properties");
                    JsonObject tags = sitesObject.getAsJsonObject("tags");
                    if (properties != null) {
                        HashMap<String, Object> propertiesMap = new Gson().fromJson(properties.toString(), HashMap.class);
                        sitesVH.setProperties(propertiesMap);
                    }
                    if (tags != null) {
                        HashMap<String, Object> tagsMap = new Gson().fromJson(tags.toString(), HashMap.class);
                        sitesVH.setTags(tagsMap);
                    }

                    sitesList.add(sitesVH);
                }
            }
        } catch (Exception exception) {
            String errorMessage = String.format("Error occurred while collecting SitesInventory for subscriptionId: %s, subscriptionName: %s", subscription.getSubscriptionId(), subscription.getSubscriptionName());
            log.error(errorMessage, exception);
            Util.eCount.getAndIncrement();
            log.debug("Current error count after exception occurred in SitesInventory collector: {}", Util.eCount.get());
        }

        log.info("Target Type : {}  Total: {} ", "Site", sitesList.size());
        return sitesList;
    }

    @Override
    public List<? extends AzureVH> collect(SubscriptionVH subscription, Map<String, Map<String, String>> tagMap) {
        throw new UnsupportedOperationException();
    }
}
