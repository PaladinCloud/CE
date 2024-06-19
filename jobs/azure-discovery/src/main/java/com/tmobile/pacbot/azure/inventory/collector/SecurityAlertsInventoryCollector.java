package com.tmobile.pacbot.azure.inventory.collector;

import com.google.gson.*;
import com.tmobile.pacbot.azure.inventory.auth.AzureCredentialProvider;
import com.tmobile.pacbot.azure.inventory.vo.AzureVH;
import com.tmobile.pacbot.azure.inventory.vo.SecurityAlertsVH;
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
public class SecurityAlertsInventoryCollector implements Collector {

    private static final Logger log = LoggerFactory.getLogger(SecurityAlertsInventoryCollector.class);
    private final String apiUrlTemplate = "https://management.azure.com/subscriptions/%s/providers/Microsoft.Security/alerts?api-version=2019-01-01";
    @Autowired
    AzureCredentialProvider azureCredentialProvider;

    @Override
    public List<? extends AzureVH> collect() {
        throw new UnsupportedOperationException();
    }

    public List<SecurityAlertsVH> collect(SubscriptionVH subscription) {
        List<SecurityAlertsVH> securityAlertsList = new ArrayList<>();
        String accessToken = azureCredentialProvider.getToken(subscription.getTenant());

        String url = String.format(apiUrlTemplate, URLEncoder.encode(subscription.getSubscriptionId()));
        try {
            String response = CommonUtils.doHttpGet(url, "Bearer", accessToken);
            JsonObject responseObj = new JsonParser().parse(response).getAsJsonObject();
            JsonArray securityAlertsObjects = responseObj.getAsJsonArray("value");
            for (JsonElement securityAlertsElement : securityAlertsObjects) {
                SecurityAlertsVH securityAlertsVH = new SecurityAlertsVH();
                JsonObject databricksObject = securityAlertsElement.getAsJsonObject();
                String id = databricksObject.get("id").getAsString();
                JsonObject properties = databricksObject.getAsJsonObject("properties");
                securityAlertsVH.setId(id);
                securityAlertsVH.setName(databricksObject.get("name").getAsString());
                securityAlertsVH.setType(databricksObject.get("type").getAsString());
                securityAlertsVH.setSubscription(subscription.getSubscriptionId());
                securityAlertsVH.setSubscriptionName(subscription.getSubscriptionName());
                String region = id.substring(id.indexOf("locations/") + ("locations/").length(), id.indexOf("/alerts"));
                securityAlertsVH.setRegion(Util.getRegionValue(subscription, region));
                securityAlertsVH.setResourceGroupName(Util.getResourceGroupNameFromId(databricksObject.get("id").getAsString()));
                if (properties != null) {
                    HashMap<String, Object> propertiesMap = new Gson().fromJson(properties.toString(), HashMap.class);
                    securityAlertsVH.setPropertiesMap(propertiesMap);
                }

                securityAlertsList.add(securityAlertsVH);
            }
        } catch (Exception exception) {
            String errorMessage = String.format("Error while collecting SecurityAlerts for subscriptionId: %s, subscriptionName: %s", subscription.getSubscriptionId(), subscription.getSubscriptionName());
            log.error(errorMessage, exception);
            Util.eCount.getAndIncrement();
            log.debug("Current error count after exception occurred in SecurityAlertsInventory collector: {}", Util.eCount.get());
        }

        log.info("Target Type : {}  Total: {} ", "Security Alerts", securityAlertsList.size());
        return securityAlertsList;
    }

    @Override
    public List<? extends AzureVH> collect(SubscriptionVH subscription, Map<String, Map<String, String>> tagMap) {
        throw new UnsupportedOperationException();
    }
}
