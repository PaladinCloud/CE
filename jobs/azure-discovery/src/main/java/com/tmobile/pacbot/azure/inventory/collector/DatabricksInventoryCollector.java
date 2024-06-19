package com.tmobile.pacbot.azure.inventory.collector;

import com.google.gson.*;
import com.tmobile.pacbot.azure.inventory.auth.AzureCredentialProvider;
import com.tmobile.pacbot.azure.inventory.vo.AzureVH;
import com.tmobile.pacbot.azure.inventory.vo.DatabricksVH;
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
public class DatabricksInventoryCollector implements Collector {

    private static final Logger log = LoggerFactory.getLogger(DatabricksInventoryCollector.class);
    private final String apiUrlTemplate = "https://management.azure.com/subscriptions/%s/providers/Microsoft.Databricks/workspaces?api-version=2018-04-01";
    @Autowired
    AzureCredentialProvider azureCredentialProvider;

    @Override
    public List<? extends AzureVH> collect() {
        throw new UnsupportedOperationException();
    }

    public List<DatabricksVH> collect(SubscriptionVH subscription) {

        List<DatabricksVH> databricksList = new ArrayList<DatabricksVH>();
        String accessToken = azureCredentialProvider.getToken(subscription.getTenant());

        String url = String.format(apiUrlTemplate, URLEncoder.encode(subscription.getSubscriptionId()));
        try {
            String response = CommonUtils.doHttpGet(url, "Bearer", accessToken);
            JsonObject responseObj = new JsonParser().parse(response).getAsJsonObject();
            JsonArray databricksObjects = responseObj.getAsJsonArray("value");
            for (JsonElement databricksElement : databricksObjects) {
                DatabricksVH databricksVH = new DatabricksVH();
                JsonObject databricksObject = databricksElement.getAsJsonObject();
                JsonObject properties = databricksObject.getAsJsonObject("properties");
                JsonObject sku = databricksObject.getAsJsonObject("sku");
                databricksVH.setId(databricksObject.get("id").getAsString());
                databricksVH.setResourceGroupName(getResourceGroupNameFromId(databricksVH.getId()));
                databricksVH.setLocation(databricksObject.get("location").getAsString());
                databricksVH.setRegion(Util.getRegionValue(subscription, databricksObject.get("location").getAsString()));
                databricksVH.setName(databricksObject.get("name").getAsString());
                databricksVH.setType(databricksObject.get("type").getAsString());
                databricksVH.setSubscription(subscription.getSubscriptionId());
                databricksVH.setSubscriptionName(subscription.getSubscriptionName());
                JsonObject tags = properties.getAsJsonObject("parameters").getAsJsonObject("resourceTags");
                if (tags != null) {
                    HashMap<String, Object> tagsMap = new Gson().fromJson(tags.get("value").toString(), HashMap.class);
                    databricksVH.setTags(tagsMap);
                }
                if (sku != null) {
                    HashMap<String, Object> skuMap = new Gson().fromJson(sku.toString(), HashMap.class);
                    databricksVH.setSkuMap(skuMap);
                }
                if (properties != null) {
                    HashMap<String, Object> propertiesMap = new Gson().fromJson(properties.toString(), HashMap.class);
                    databricksVH.setPropertiesMap(propertiesMap);
                }
                databricksList.add(databricksVH);
            }
        } catch (Exception exception) {
            String errorMessage = String.format("Error collecting Databricks for subscriptionId: %s, subscriptionName: %s", subscription.getSubscriptionId(), subscription.getSubscriptionName());
            log.error(errorMessage, exception);
            Util.eCount.getAndIncrement();
            log.debug("Current error count after exception occurred in DatabricksInventory Collector: {}", Util.eCount.get());
        }

        log.info("Target Type : {}  Total: {} ", "Databrick", databricksList.size());
        return databricksList;
    }

    @Override
    public List<? extends AzureVH> collect(SubscriptionVH subscription, Map<String, Map<String, String>> tagMap) {
        throw new UnsupportedOperationException();
    }
}
