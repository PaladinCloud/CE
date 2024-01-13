package com.tmobile.pacbot.azure.inventory.collector;

import com.google.gson.*;
import com.tmobile.pacbot.azure.inventory.auth.AzureCredentialProvider;
import com.tmobile.pacbot.azure.inventory.vo.AzureVH;
import com.tmobile.pacbot.azure.inventory.vo.MySQLServerVH;
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
public class MySQLInventoryCollector implements Collector {

    private static final Logger log = LoggerFactory.getLogger(MySQLInventoryCollector.class);
    private final String apiUrlTemplate = "https://management.azure.com/subscriptions/%s/providers/Microsoft.DBforMySQL/servers?api-version=2017-12-01";
    @Autowired
    AzureCredentialProvider azureCredentialProvider;

    @Override
    public List<? extends AzureVH> collect() {
        throw new UnsupportedOperationException();
    }

    public List<MySQLServerVH> collect(SubscriptionVH subscription) {

        List<MySQLServerVH> mySqlServerList = new ArrayList<MySQLServerVH>();
        String accessToken = azureCredentialProvider.getToken(subscription.getTenant());

        String url = String.format(apiUrlTemplate, URLEncoder.encode(subscription.getSubscriptionId()));
        try {
            String response = CommonUtils.doHttpGet(url, "Bearer", accessToken);
            JsonObject responseObj = new JsonParser().parse(response).getAsJsonObject();
            JsonArray sqlServerObjects = responseObj.getAsJsonArray("value");
            for (JsonElement sqlServerObjectElement : sqlServerObjects) {
                MySQLServerVH mySQLServerVH = new MySQLServerVH();
                mySQLServerVH.setSubscription(subscription.getSubscriptionId());
                mySQLServerVH.setSubscriptionName(subscription.getSubscriptionName());
                JsonObject sqlServerObject = sqlServerObjectElement.getAsJsonObject();
                JsonObject properties = sqlServerObject.getAsJsonObject("properties");
                JsonObject sku = sqlServerObject.getAsJsonObject("sku");
                mySQLServerVH.setRegion(Util.getRegionValue(subscription, sqlServerObject.get("location").getAsString()));
                mySQLServerVH.setResourceGroupName(Util.getResourceGroupNameFromId(sqlServerObject.get("id").getAsString()));
                mySQLServerVH.setId(sqlServerObject.get("id").getAsString());
                mySQLServerVH.setLocation(sqlServerObject.get("location").getAsString());
                mySQLServerVH.setName(sqlServerObject.get("name").getAsString());
                mySQLServerVH.setType(sqlServerObject.get("type").getAsString());
                if (sku != null) {
                    HashMap<String, Object> skuMap = new Gson().fromJson(sku.toString(), HashMap.class);
                    mySQLServerVH.setSkuMap(skuMap);
                }
                if (properties != null) {
                    HashMap<String, Object> propertiesMap = new Gson().fromJson(properties.toString(), HashMap.class);
                    mySQLServerVH.setPropertiesMap(propertiesMap);
                }

                mySqlServerList.add(mySQLServerVH);
            }
        } catch (Exception e) {
            log.error("Error Collecting mysqlserver", e);
            Util.eCount.getAndIncrement();
        }

        log.info("Target Type : {}  Total: {} ", "MySQL Server", mySqlServerList.size());
        return mySqlServerList;
    }

    @Override
    public List<? extends AzureVH> collect(SubscriptionVH subscription, Map<String, Map<String, String>> tagMap) {
        throw new UnsupportedOperationException();
    }
}
