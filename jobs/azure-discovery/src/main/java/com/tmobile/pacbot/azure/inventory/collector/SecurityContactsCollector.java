package com.tmobile.pacbot.azure.inventory.collector;


import com.google.gson.*;
import com.tmobile.pacbot.azure.inventory.auth.AzureCredentialProvider;
import com.tmobile.pacbot.azure.inventory.vo.AutoProvisioningSettingsVH;
import com.tmobile.pacbot.azure.inventory.vo.AzureVH;
import com.tmobile.pacbot.azure.inventory.vo.SecurityContactsVH;
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
public class SecurityContactsCollector implements Collector {
    private static final String ID = "id";
    private static final String NAME = "name";
    private static final String TYPE = "type";
    private static final String PROPERTY = "properties";
    private static final String AUTO_PROVISION = "autoProvision";
    private static final Logger log = LoggerFactory.getLogger(SecurityContactsCollector.class);
    private final String apiUrlTemplate = "https://management.azure.com/subscriptions/%s/providers/Microsoft.Security/securityContacts?api-version=2020-01-01-preview";
    @Autowired
    AzureCredentialProvider azureCredentialProvider;

    @Override
    public List<? extends AzureVH> collect() {
        throw new UnsupportedOperationException();
    }

    public List<SecurityContactsVH> collect(SubscriptionVH subscription) {
        List<SecurityContactsVH> securityContactsList = new ArrayList<>();
        String accessToken = azureCredentialProvider.getToken(subscription.getTenant());
        String url = String.format(apiUrlTemplate, URLEncoder.encode(subscription.getSubscriptionId()));
        try {
            String response = CommonUtils.doHttpGet(url, "Bearer", accessToken);
            JsonParser parser = new JsonParser();
            Object obj = parser.parse(response);
            if (obj instanceof JsonArray) {
                JsonArray jsonArray = (JsonArray) parser.parse(response);
                for (JsonElement securityElement : jsonArray) {
                    SecurityContactsVH securityContactsVH = new SecurityContactsVH();
                    JsonObject securityObject = securityElement.getAsJsonObject();
                    securityContactsVH.setSubscription(subscription.getSubscriptionId());
                    securityContactsVH.setSubscriptionName(subscription.getSubscriptionName());
                    securityContactsVH.setId(securityObject.get(ID).getAsString());
                    securityContactsVH.setEtag(securityObject.get("etag").getAsString());
                    securityContactsVH.setName(securityObject.get(NAME).getAsString());
                    securityContactsVH.setRegion(Util.getRegionValue(subscription, securityObject.get("location").getAsString()));
                    securityContactsVH.setType(securityObject.get(TYPE).getAsString());
                    JsonObject propertiesJson = securityObject.get(PROPERTY).getAsJsonObject();
                    HashMap<String, Object> propertiesMap = new Gson().fromJson(propertiesJson.toString(), HashMap.class);
                    securityContactsVH.setProperties(propertiesMap);
                    securityContactsVH.setAutoProvisioningSettingsList(fetchAutoProvisioningSettingsList(subscription));
                    securityContactsList.add(securityContactsVH);
                }
            }
        } catch (Exception e) {
            log.error("Error fetching Security Contacts", e);
            Util.eCount.getAndIncrement();
        }

        log.info("Target Type : {}  Total: {} ", "Batch Account", securityContactsList.size());
        return securityContactsList;
    }

    @Override
    public List<? extends AzureVH> collect(SubscriptionVH subscription, Map<String, Map<String, String>> tagMap) {
        throw new UnsupportedOperationException();
    }

    private List<AutoProvisioningSettingsVH> fetchAutoProvisioningSettingsList(SubscriptionVH subscription) {
        List<AutoProvisioningSettingsVH> autoProvisioningSettingsVHList = new ArrayList<>();

        try {
            String apiUrlTemplate = "https://management.azure.com/%s/providers/Microsoft.Security/autoProvisioningSettings?api-version=2017-08-01-preview";
            String accessToken = azureCredentialProvider.getToken(subscription.getTenant());
            String url = String.format(apiUrlTemplate, URLEncoder.encode("/subscriptions/" + subscription.getSubscriptionId(), java.nio.charset.StandardCharsets.UTF_8.toString()));

            String response = CommonUtils.doHttpGet(url, "Bearer", accessToken);
            JsonObject responseObj = new JsonParser().parse(response).getAsJsonObject();
            JsonArray autoProvisioningObjects = responseObj.getAsJsonArray("value");

            for (JsonElement autoProvisioningElement : autoProvisioningObjects) {
                AutoProvisioningSettingsVH autoProvisioningSettingsVH = new AutoProvisioningSettingsVH();
                autoProvisioningSettingsVH.setSubscription(subscription.getSubscriptionId());
                autoProvisioningSettingsVH.setSubscriptionName(subscription.getSubscriptionName());
                JsonObject autoProvisioningObject = autoProvisioningElement.getAsJsonObject();
                String id = autoProvisioningObject.get(ID).getAsString();
                autoProvisioningSettingsVH.setId(id);
                String name = autoProvisioningObject.get(NAME).getAsString();
                autoProvisioningSettingsVH.setName(name);
                String type = autoProvisioningObject.get(TYPE).getAsString();
                autoProvisioningSettingsVH.setType(type);

                JsonObject properties = autoProvisioningObject.getAsJsonObject(PROPERTY);

                if (properties != null) {
                    String autoProvision = properties.get(AUTO_PROVISION).getAsString();
                    autoProvisioningSettingsVH.setAutoProvision(autoProvision);
                }
                autoProvisioningSettingsVHList.add(autoProvisioningSettingsVH);
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        return autoProvisioningSettingsVHList;
    }
}




