package com.tmobile.pacbot.azure.inventory.collector;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.tmobile.pacbot.azure.inventory.auth.AzureCredentialProvider;
import com.tmobile.pacbot.azure.inventory.vo.DiagnosticSettingVH;
import com.tmobile.pacbot.azure.inventory.vo.SubscriptionVH;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.net.URLEncoder;
import java.util.Set;

import com.tmobile.pacman.commons.utils.CommonUtils;
@Component
public class DiagnosticSettingsCollector {
    @Autowired
    AzureCredentialProvider azureCredentialProvider;
    private static final Logger log = LoggerFactory.getLogger(DiagnosticSettingsCollector.class);
    public List<DiagnosticSettingVH> fetchDiagnosticSettings(SubscriptionVH subscription) {
        List<DiagnosticSettingVH> diagnosticSettingVHList = new ArrayList<>();
        try {
            String apiUrlTemplate="https://management.azure.com/%s/providers/Microsoft.Insights/diagnosticSettings?api-version=2021-05-01-preview";
            String accessToken = azureCredentialProvider.getToken(subscription.getTenant());
            String url = String.format(apiUrlTemplate, URLEncoder.encode("/subscriptions/"+subscription.getSubscriptionId(),java.nio.charset.StandardCharsets.UTF_8.toString()));
            log.info("The url is {}",url);
            String response = CommonUtils.doHttpGet(url, "Bearer", accessToken);
            log.info("Response is :{}",response);
            JsonObject responseObj = new JsonParser().parse(response).getAsJsonObject();
            JsonArray diagnosticSettings = responseObj.getAsJsonArray("value");

            for(JsonElement diagnosticSetting:diagnosticSettings){
                DiagnosticSettingVH diagnosticSettingVH=new DiagnosticSettingVH();
                diagnosticSettingVH.setId(diagnosticSetting.getAsJsonObject().getAsJsonPrimitive("id").getAsString());
                diagnosticSettingVH.setName(diagnosticSetting.getAsJsonObject().getAsJsonPrimitive("name").getAsString());
                diagnosticSettingVH.setSubscriptionName(subscription.getSubscriptionName());
                diagnosticSettingVH.setSubscription(subscription.getSubscription());
                diagnosticSettingVH.setResourceGroupName(subscription.getResourceGroupName());
                diagnosticSettingVH.setSubscriptionId(subscription.getSubscriptionId());
                diagnosticSettingVH.setRegion(subscription.getRegion());
                JsonObject properties =diagnosticSetting.getAsJsonObject().getAsJsonObject("properties");
                JsonArray logs=properties.getAsJsonArray("logs");
                Set<String> enabledCategories=new HashSet();
                for(JsonElement diagnosticLog:logs) {
                    if(diagnosticLog.getAsJsonObject().getAsJsonPrimitive("enabled").getAsBoolean()) {
                        enabledCategories.add(diagnosticLog.getAsJsonObject().getAsJsonPrimitive("category").getAsString());
                    }
                }
                diagnosticSettingVH.setEnabledCategories(enabledCategories);
                diagnosticSettingVHList.add(diagnosticSettingVH);
            }

            log.info("Size of diagnostic settings: {}", diagnosticSettingVHList.size());
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        return diagnosticSettingVHList;
    }
}
