package com.tmobile.pacbot.azure.inventory.collector;

import com.google.gson.*;
import com.tmobile.pacbot.azure.inventory.auth.AzureCredentialProvider;
import com.tmobile.pacbot.azure.inventory.vo.AzureVH;
import com.tmobile.pacbot.azure.inventory.vo.SecurityPricingsVH;
import com.tmobile.pacbot.azure.inventory.vo.SubscriptionVH;
import com.tmobile.pacman.commons.utils.CommonUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.tmobile.pacbot.azure.inventory.util.InventoryConstants.REGION_GLOBAL;

@Component
public class SecurityPricingsInventoryCollector implements Collector {

    private static final Logger log = LoggerFactory.getLogger(SecurityPricingsInventoryCollector.class);
    @Autowired
    AzureCredentialProvider azureCredentialProvider;
    private final String apiUrlTemplate = "https://management.azure.com/subscriptions/%s/providers/Microsoft.Security/pricings?api-version=2022-03-01";

    @Override
    public List<? extends AzureVH> collect() {
        throw new UnsupportedOperationException();
    }

    public List<SecurityPricingsVH> collect(SubscriptionVH subscription) {

        List<SecurityPricingsVH> securityPricingsList = new ArrayList<SecurityPricingsVH>();
        String accessToken = azureCredentialProvider.getToken(subscription.getTenant());

        String url = String.format(apiUrlTemplate, URLEncoder.encode(subscription.getSubscriptionId()));
        try {
            String response = CommonUtils.doHttpGet(url, "Bearer", accessToken);
            JsonObject responseObj = new JsonParser().parse(response).getAsJsonObject();
            JsonArray securityPricingsObjects = responseObj.getAsJsonArray("value");
            for (JsonElement securityPricingsElement : securityPricingsObjects) {
                SecurityPricingsVH securityPricingsVH = new SecurityPricingsVH();
                JsonObject securityPricingsObject = securityPricingsElement.getAsJsonObject();
                JsonObject properties = securityPricingsObject.getAsJsonObject("properties");
                securityPricingsVH.setId(securityPricingsObject.get("id").getAsString());
                securityPricingsVH.setName(securityPricingsObject.get("name").getAsString());
                securityPricingsVH.setType(securityPricingsObject.get("type").getAsString());
                securityPricingsVH.setSubscription(subscription.getSubscriptionId());
                securityPricingsVH.setSubscriptionName(subscription.getSubscriptionName());
                securityPricingsVH.setRegion(Util.getRegionValue(subscription, StringUtils.defaultIfBlank(subscription.getRegion(), REGION_GLOBAL)));
                securityPricingsVH.setResourceGroupName(Util.getResourceGroupNameFromId(securityPricingsObject.get("id").getAsString()));

                if (properties != null) {
                    HashMap<String, Object> propertiesMap = new Gson().fromJson(properties.toString(), HashMap.class);
                    securityPricingsVH.setPropertiesMap(propertiesMap);
                }
                securityPricingsList.add(securityPricingsVH);
            }
        } catch (Exception e) {
            log.error("Error collecting Security Pricings", e);
            Util.eCount.getAndIncrement();
        }

        log.info("Target Type : {}  Total: {} ", "Security Pricings", securityPricingsList.size());
        return securityPricingsList;
    }

    @Override
    public List<? extends AzureVH> collect(SubscriptionVH subscription, Map<String, Map<String, String>> tagMap) {
        throw new UnsupportedOperationException();
    }
}
