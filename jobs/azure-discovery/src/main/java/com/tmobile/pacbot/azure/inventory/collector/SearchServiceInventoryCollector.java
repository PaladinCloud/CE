package com.tmobile.pacbot.azure.inventory.collector;

import com.google.gson.*;
import com.tmobile.pacbot.azure.inventory.auth.AzureCredentialProvider;
import com.tmobile.pacbot.azure.inventory.vo.AzureVH;
import com.tmobile.pacbot.azure.inventory.vo.SearchServiceVH;
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
public class SearchServiceInventoryCollector implements Collector {

    private static final Logger log = LoggerFactory.getLogger(SearchServiceInventoryCollector.class);
    private final String apiUrlTemplate = "https://management.azure.com/subscriptions/%s/providers/Microsoft.Search/searchServices?api-version=2015-08-19";
    @Autowired
    AzureCredentialProvider azureCredentialProvider;

    @Override
    public List<? extends AzureVH> collect() {
        throw new UnsupportedOperationException();
    }

    public List<SearchServiceVH> collect(SubscriptionVH subscription) {

        List<SearchServiceVH> searchServiceList = new ArrayList<>();
        String accessToken = azureCredentialProvider.getToken(subscription.getTenant());
        String url = String.format(apiUrlTemplate, URLEncoder.encode(subscription.getSubscriptionId()));
        try {

            String response = CommonUtils.doHttpGet(url, "Bearer", accessToken);
            JsonObject responseObj = new JsonParser().parse(response).getAsJsonObject();
            JsonArray searchServiceObjects = responseObj.getAsJsonArray("value");
            if (searchServiceObjects != null) {
                for (JsonElement searchServiceElement : searchServiceObjects) {
                    SearchServiceVH searchServiceVH = new SearchServiceVH();
                    JsonObject searchServiceObject = searchServiceElement.getAsJsonObject();
                    searchServiceVH.setSubscription(subscription.getSubscriptionId());
                    searchServiceVH.setSubscriptionName(subscription.getSubscriptionName());
                    searchServiceVH.setRegion(Util.getRegionValue(subscription, searchServiceObject.get("location").getAsString()));
                    searchServiceVH.setResourceGroupName(Util.getResourceGroupNameFromId(searchServiceObject.get("id").getAsString()));
                    searchServiceVH.setId(searchServiceObject.get("id").getAsString());
                    searchServiceVH.setLocation(searchServiceObject.get("location").getAsString());
                    searchServiceVH.setName(searchServiceObject.get("name").getAsString());
                    searchServiceVH.setType(searchServiceObject.get("type").getAsString());
                    JsonObject properties = searchServiceObject.getAsJsonObject("properties");
                    JsonObject sku = searchServiceObject.getAsJsonObject("sku");
                    if (properties != null) {
                        HashMap<String, Object> propertiesMap = new Gson().fromJson(properties.toString(),
                                HashMap.class);
                        searchServiceVH.setProperties(propertiesMap);
                    }

                    if (sku != null) {
                        HashMap<String, Object> skuMap = new Gson().fromJson(sku.toString(), HashMap.class);
                        searchServiceVH.setSku(skuMap);
                    }

                    searchServiceList.add(searchServiceVH);
                }
            }
        } catch (Exception exception) {
            String errorMessage = String.format("Error occurred while collecting SearchService for subscriptionId: %s, subscriptionName: %s", subscription.getSubscriptionId(), subscription.getSubscriptionName());
            log.error(errorMessage, exception);
            Util.eCount.getAndIncrement();
            log.debug("Current error count after exception occurred in SearchServiceInventory collector: {}", Util.eCount.get());
        }

        log.info("Target Type : {}  Total: {} ", "Search Service", searchServiceList.size());
        return searchServiceList;
    }

    @Override
    public List<? extends AzureVH> collect(SubscriptionVH subscription, Map<String, Map<String, String>> tagMap) {
        throw new UnsupportedOperationException();
    }
}
