package com.tmobile.pacbot.azure.inventory.collector;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.tmobile.pacbot.azure.inventory.auth.AzureCredentialProvider;
import com.tmobile.pacbot.azure.inventory.vo.AzureVH;
import com.tmobile.pacbot.azure.inventory.vo.MySQLFlexibleVH;
import com.tmobile.pacbot.azure.inventory.vo.SubscriptionVH;
import com.tmobile.pacman.commons.utils.CommonUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
public class MySQLFlexibleInventoryCollector implements Collector {
    private static final Logger logger = LoggerFactory.getLogger(MySQLFlexibleInventoryCollector.class);
    private static final String serverApiUrlTemplate = "https://management.azure.com/subscriptions/%s/providers/Microsoft.DBforMySQL/flexibleServers?api-version=2021-05-01";
    private static final String configApiUrlTemplate = "https://management.azure.com/subscriptions/%s/resourceGroups/%s/providers/Microsoft.DBforMySQL/flexibleServers/%s/configurations?api-version=2021-05-01";
    @Autowired
    AzureCredentialProvider azureCredentialProvider;

    @Override
    public List<? extends AzureVH> collect() {
        throw new UnsupportedOperationException();
    }

    public List<MySQLFlexibleVH> collect(SubscriptionVH subscription) {
        List<MySQLFlexibleVH> mySQLFlexibleVHList = new ArrayList<>();
        String accessToken = azureCredentialProvider.getToken(subscription.getTenant());
        String url = null;
        try {
            url = String.format(serverApiUrlTemplate,
                    URLEncoder.encode(subscription.getSubscriptionId(),
                            java.nio.charset.StandardCharsets.UTF_8.toString()));
            String response = CommonUtils.doHttpGet(url, "Bearer", accessToken);
            logger.info("subscriptionName: {}", subscription.getSubscriptionName());
            JsonObject responseObj = new JsonParser().parse(response).getAsJsonObject();
            logger.info("JSON Response object {}", responseObj);
            JsonArray serverNames = responseObj.getAsJsonArray("value");
            for (int i = 0; i < serverNames.size(); i++) {
                MySQLFlexibleVH mySQLFlexibleVH = new MySQLFlexibleVH();
                JsonObject tags = serverNames.get(i).getAsJsonObject().get("tags").getAsJsonObject();
                if (tags != null) {
                    HashMap<String, String> tagsMap = new Gson().fromJson(tags.toString(), HashMap.class);
                    mySQLFlexibleVH.setTags(tagsMap);
                }

                mySQLFlexibleVH.setRegion(Util.getRegionValue(subscription, serverNames.get(i).getAsJsonObject().get("location").getAsString()));
                String serverName = serverNames.get(i).getAsJsonObject().get("name").getAsString();
                String id = serverNames.get(i).getAsJsonObject().get("id").getAsString();
                int beginningIndex = id.indexOf("resourceGroups") + 15;
                String resourceGroupName = (id).substring(beginningIndex, id.indexOf('/', beginningIndex + 2));
                String configUrl = String.format(configApiUrlTemplate,
                        URLEncoder.encode(subscription.getSubscriptionId(),
                                java.nio.charset.StandardCharsets.UTF_8.toString()),
                        URLEncoder.encode(resourceGroupName,
                                java.nio.charset.StandardCharsets.UTF_8.toString()),
                        URLEncoder.encode(serverName,
                                java.nio.charset.StandardCharsets.UTF_8.toString()));
                String responseConfig = CommonUtils.doHttpGet(configUrl, "Bearer", accessToken);
                JsonObject responseConfigObj = new JsonParser().parse(responseConfig).getAsJsonObject();
                JsonArray value = responseConfigObj.getAsJsonArray("value");
                for (int j = 0; j < value.size(); j++) {
                    JsonObject properties = value.get(j).getAsJsonObject().getAsJsonObject("properties");
                    String tlsVersion = properties.get("value").getAsString();
                    if (tlsVersion.startsWith("TLS")) {
                        mySQLFlexibleVH.setTlsVersion(tlsVersion);
                        mySQLFlexibleVH.setResourceGroupName(resourceGroupName);
                        mySQLFlexibleVH.setId(id);
                        mySQLFlexibleVH.setName(serverName);
                        mySQLFlexibleVH.setSubscriptionName(subscription.getSubscriptionName());
                        mySQLFlexibleVH.setSubscription(subscription.getSubscriptionId());
                        break;
                    }
                }

                mySQLFlexibleVHList.add(mySQLFlexibleVH);
            }
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException(e);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        return mySQLFlexibleVHList;
    }

    @Override
    public List<? extends AzureVH> collect(SubscriptionVH subscription, Map<String, Map<String, String>> tagMap) {
        throw new UnsupportedOperationException();
    }
}
