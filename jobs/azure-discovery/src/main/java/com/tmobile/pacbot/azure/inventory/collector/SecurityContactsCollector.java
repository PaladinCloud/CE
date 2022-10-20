package com.tmobile.pacbot.azure.inventory.collector;


import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.tmobile.pacbot.azure.inventory.auth.AzureCredentialProvider;
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

@Component
public class SecurityContactsCollector {
    @Autowired
    AzureCredentialProvider azureCredentialProvider;

    private static Logger LOGGER = LoggerFactory.getLogger(BatchAccountInventoryCollector.class);
    private String apiUrlTemplate = "https://management.azure.com/subscriptions/%s/providers/Microsoft.Security/securityContacts?api-version=2020-01-01-preview";

    public List<SecurityContactsVH> fetchSecurityContactsInfo(SubscriptionVH subscription) throws Exception {
        List<SecurityContactsVH> securityContactsList = new ArrayList<>();
        String accessToken = azureCredentialProvider.getToken(subscription.getTenant());
        String url = String.format(apiUrlTemplate, URLEncoder.encode(subscription.getSubscriptionId()));
        try {
            String response = CommonUtils.doHttpGet(url, "Bearer", accessToken);
            JsonParser parser = new JsonParser();
            JsonArray jsonArray = (JsonArray) parser.parse(response);
            SecurityContactsVH securityContactsVH = new SecurityContactsVH();
            JsonObject responseJson = jsonArray.get(0).getAsJsonObject();
            securityContactsVH.setId(responseJson.get("id").getAsString());
            securityContactsVH.setEtag(responseJson.get("etag").getAsString());
            securityContactsVH.setName(responseJson.get("name").getAsString());
            securityContactsVH.setRegion(responseJson.get("location").getAsString());
            securityContactsVH.setType(responseJson.get("type").getAsString());
            JsonObject propertiesJson = responseJson.get("properties").getAsJsonObject();
            HashMap<String, Object> propertiesMap = new Gson().fromJson(propertiesJson.toString(), HashMap.class);
            securityContactsVH.setProperties(propertiesMap);
            securityContactsList.add(securityContactsVH);

        } catch (Exception e) {
            LOGGER.error("Error fetching BatchAccount", e);
        }
        LOGGER.info("Target Type : {}  Total: {} ", "Batch Account", securityContactsList.size());
        return securityContactsList;
    }
}




