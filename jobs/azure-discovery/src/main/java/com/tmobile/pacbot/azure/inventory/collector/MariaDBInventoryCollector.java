package com.tmobile.pacbot.azure.inventory.collector;

import com.google.gson.*;
import com.tmobile.pacbot.azure.inventory.auth.AzureCredentialProvider;
import com.tmobile.pacbot.azure.inventory.vo.AzureVH;
import com.tmobile.pacbot.azure.inventory.vo.MariaDBVH;
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
public class MariaDBInventoryCollector implements Collector {

    private static final Logger log = LoggerFactory.getLogger(MariaDBInventoryCollector.class);
    private final String apiUrlTemplate = "https://management.azure.com/subscriptions/%s/providers/Microsoft.DBforMariaDB/servers?api-version=2018-06-01-preview";
    @Autowired
    AzureCredentialProvider azureCredentialProvider;

    @Override
    public List<? extends AzureVH> collect() {
        throw new UnsupportedOperationException();
    }

    public List<MariaDBVH> collect(SubscriptionVH subscription) {

        List<MariaDBVH> mariaDBList = new ArrayList<>();
        String accessToken = azureCredentialProvider.getToken(subscription.getTenant());

        String url = String.format(apiUrlTemplate, URLEncoder.encode(subscription.getSubscriptionId()));
        try {
            String response = CommonUtils.doHttpGet(url, "Bearer", accessToken);
            JsonObject responseObj = new JsonParser().parse(response).getAsJsonObject();
            JsonArray mariaDBObjects = responseObj.getAsJsonArray("value");
            for (JsonElement mariaDBElement : mariaDBObjects) {
                MariaDBVH mariaDBVH = new MariaDBVH();
                JsonObject mariaDBObject = mariaDBElement.getAsJsonObject();
                JsonObject properties = mariaDBObject.getAsJsonObject("properties");
                JsonObject sku = mariaDBObject.getAsJsonObject("sku");
                mariaDBVH.setId(mariaDBObject.get("id").getAsString());
                mariaDBVH.setLocation(mariaDBObject.get("location").getAsString());
                mariaDBVH.setName(mariaDBObject.get("name").getAsString());
                mariaDBVH.setType(mariaDBObject.get("type").getAsString());
                mariaDBVH.setSubscription(subscription.getSubscriptionId());
                mariaDBVH.setSubscriptionName(subscription.getSubscriptionName());
                mariaDBVH.setRegion(Util.getRegionValue(subscription, mariaDBObject.get("location").getAsString()));
                mariaDBVH.setResourceGroupName(Util.getResourceGroupNameFromId(mariaDBObject.get("id").getAsString()));
                if (sku != null) {
                    HashMap<String, Object> skuMap = new Gson().fromJson(sku.toString(), HashMap.class);
                    mariaDBVH.setSkuMap(skuMap);
                }
                if (properties != null) {
                    HashMap<String, Object> propertiesMap = new Gson().fromJson(properties.toString(), HashMap.class);
                    mariaDBVH.setPropertiesMap(propertiesMap);
                }
                mariaDBList.add(mariaDBVH);
            }
        } catch (Exception e) {
            log.error("Error Collecting MariaDB", e);
            Util.eCount.getAndIncrement();
        }

        log.info("Target Type : {}  Total: {} ", "MariaDB", mariaDBList.size());
        return mariaDBList;
    }

    @Override
    public List<? extends AzureVH> collect(SubscriptionVH subscription, Map<String, Map<String, String>> tagMap) {
        throw new UnsupportedOperationException();
    }
}
