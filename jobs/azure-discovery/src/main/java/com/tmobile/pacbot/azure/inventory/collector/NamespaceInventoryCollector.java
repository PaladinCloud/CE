package com.tmobile.pacbot.azure.inventory.collector;

import com.google.gson.*;
import com.tmobile.pacbot.azure.inventory.auth.AzureCredentialProvider;
import com.tmobile.pacbot.azure.inventory.vo.AzureVH;
import com.tmobile.pacbot.azure.inventory.vo.NamespaceVH;
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
public class NamespaceInventoryCollector implements Collector {

    private static final Logger log = LoggerFactory.getLogger(NamespaceInventoryCollector.class);
    private final String apiUrlTemplate = "https://management.azure.com/subscriptions/%s/providers/Microsoft.EventHub/namespaces?api-version=2017-04-01";
    @Autowired
    AzureCredentialProvider azureCredentialProvider;

    @Override
    public List<? extends AzureVH> collect() {
        throw new UnsupportedOperationException();
    }

    public List<NamespaceVH> collect(SubscriptionVH subscription) {

        List<NamespaceVH> namespaceList = new ArrayList<NamespaceVH>();
        String accessToken = azureCredentialProvider.getToken(subscription.getTenant());

        String url = String.format(apiUrlTemplate, URLEncoder.encode(subscription.getSubscriptionId()));
        try {

            String response = CommonUtils.doHttpGet(url, "Bearer", accessToken);
            JsonObject responseObj = new JsonParser().parse(response).getAsJsonObject();
            JsonArray namespaceObjects = responseObj.getAsJsonArray("value");
            if (namespaceObjects != null) {
                for (JsonElement namespaceElement : namespaceObjects) {
                    NamespaceVH namespaceVH = new NamespaceVH();
                    JsonObject namespaceObject = namespaceElement.getAsJsonObject();
                    namespaceVH.setSubscription(subscription.getSubscriptionId());
                    namespaceVH.setSubscriptionName(subscription.getSubscriptionName());
                    namespaceVH.setRegion(Util.getRegionValue(subscription, namespaceObject.get("location").getAsString()));
                    namespaceVH.setResourceGroupName(Util.getResourceGroupNameFromId(namespaceObject.get("id").getAsString()));
                    namespaceVH.setId(namespaceObject.get("id").getAsString());
                    namespaceVH.setLocation(namespaceObject.get("location").getAsString());
                    namespaceVH.setName(namespaceObject.get("name").getAsString());
                    namespaceVH.setType(namespaceObject.get("type").getAsString());
                    JsonObject properties = namespaceObject.getAsJsonObject("properties");
                    JsonObject tags = namespaceObject.getAsJsonObject("tags");
                    JsonObject sku = namespaceObject.getAsJsonObject("sku");
                    if (properties != null) {
                        HashMap<String, Object> propertiesMap = new Gson().fromJson(properties.toString(),
                                HashMap.class);
                        namespaceVH.setProperties(propertiesMap);
                    }
                    if (tags != null) {
                        HashMap<String, Object> tagsMap = new Gson().fromJson(tags.toString(), HashMap.class);
                        namespaceVH.setTags(tagsMap);
                    }
                    if (sku != null) {
                        HashMap<String, Object> skuMap = new Gson().fromJson(sku.toString(), HashMap.class);
                        namespaceVH.setSku(skuMap);
                    }

                    namespaceList.add(namespaceVH);
                }
            }
        } catch (Exception e) {
            log.error("Error collecting namespace", e);
            Util.eCount.getAndIncrement();
        }

        log.info("Target Type : {}  Total: {} ", "Namespace", namespaceList.size());
        return namespaceList;
    }

    @Override
    public List<? extends AzureVH> collect(SubscriptionVH subscription, Map<String, Map<String, String>> tagMap) {
        throw new UnsupportedOperationException();
    }
}
