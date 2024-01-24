package com.tmobile.pacbot.azure.inventory.collector;

import com.google.gson.*;
import com.tmobile.pacbot.azure.inventory.auth.AzureCredentialProvider;
import com.tmobile.pacbot.azure.inventory.vo.AzureVH;
import com.tmobile.pacbot.azure.inventory.vo.PostgreSQLServerVH;
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
public class PostgreSQLInventoryCollector implements Collector {

    private static final Logger log = LoggerFactory.getLogger(PostgreSQLInventoryCollector.class);
    private final String apiUrlTemplate = "https://management.azure.com/subscriptions/%s/providers/Microsoft.DBforPostgreSQL/servers?api-version=2017-12-01";
    @Autowired
    AzureCredentialProvider azureCredentialProvider;

    @Override
    public List<? extends AzureVH> collect() {
        throw new UnsupportedOperationException();
    }

    public List<PostgreSQLServerVH> collect(SubscriptionVH subscription) {
        List<PostgreSQLServerVH> postgreSQLServerList = new ArrayList<PostgreSQLServerVH>();
        String accessToken = azureCredentialProvider.getToken(subscription.getTenant());

        String url = String.format(apiUrlTemplate, URLEncoder.encode(subscription.getSubscriptionId()));
        try {
            String response = CommonUtils.doHttpGet(url, "Bearer", accessToken);
            JsonObject responseObj = new JsonParser().parse(response).getAsJsonObject();
            JsonArray postgreSQLServerObjects = responseObj.getAsJsonArray("value");
            for (JsonElement postgreSQLServerObjectElement : postgreSQLServerObjects) {
                PostgreSQLServerVH postgreSQLServerVH = new PostgreSQLServerVH();
                postgreSQLServerVH.setSubscription(subscription.getSubscriptionId());
                postgreSQLServerVH.setSubscriptionName(subscription.getSubscriptionName());
                JsonObject postgreSQLServerObject = postgreSQLServerObjectElement.getAsJsonObject();
                JsonObject properties = postgreSQLServerObject.getAsJsonObject("properties");
                JsonObject sku = postgreSQLServerObject.getAsJsonObject("sku");
                postgreSQLServerVH.setId(postgreSQLServerObject.get("id").getAsString());
                postgreSQLServerVH.setLocation(postgreSQLServerObject.get("location").getAsString());
                postgreSQLServerVH.setRegion(Util.getRegionValue(subscription, postgreSQLServerObject.get("location").getAsString()));
                postgreSQLServerVH.setResourceGroupName(Util.getResourceGroupNameFromId(postgreSQLServerObject.get("id").getAsString()));
                postgreSQLServerVH.setName(postgreSQLServerObject.get("name").getAsString());
                postgreSQLServerVH.setType(postgreSQLServerObject.get("type").getAsString());
                JsonObject tags = postgreSQLServerObject.get("tags").getAsJsonObject();
                if (tags != null) {
                    HashMap<String, String> tagsMap = new Gson().fromJson(tags.toString(), HashMap.class);
                    postgreSQLServerVH.setTags(tagsMap);
                }
                postgreSQLServerVH.setResourceGroupName(getResourceGroupNameFromId(postgreSQLServerVH.getId()));
                if (sku != null) {
                    HashMap<String, Object> skuMap = new Gson().fromJson(sku.toString(), HashMap.class);
                    postgreSQLServerVH.setSkuMap(skuMap);
                }
                if (properties != null) {
                    HashMap<String, Object> propertiesMap = new Gson().fromJson(properties.toString(), HashMap.class);
                    postgreSQLServerVH.setPropertiesMap(propertiesMap);
                }
                postgreSQLServerList.add(postgreSQLServerVH);
            }
        } catch (Exception e) {
            log.error("Error collecting Postgres", e);
            Util.eCount.getAndIncrement();
        }

        log.info("Target Type : {} Total: {} ", "Postgres DB", postgreSQLServerList.size());
        return postgreSQLServerList;
    }

    @Override
    public List<? extends AzureVH> collect(SubscriptionVH subscription, Map<String, Map<String, String>> tagMap) {
        throw new UnsupportedOperationException();
    }
}
