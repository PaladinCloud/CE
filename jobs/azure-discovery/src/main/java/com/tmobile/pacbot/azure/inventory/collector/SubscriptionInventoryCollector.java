package com.tmobile.pacbot.azure.inventory.collector;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.microsoft.azure.PagedList;
import com.microsoft.azure.management.Azure;
import com.microsoft.azure.management.storage.StorageAccount;
import com.tmobile.pacbot.azure.inventory.auth.AzureCredentialProvider;
import com.tmobile.pacbot.azure.inventory.vo.StorageAccountActivityLogVH;
import com.tmobile.pacbot.azure.inventory.vo.SubscriptionVH;
import com.tmobile.pacman.commons.utils.CommonUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;

@Component
public class SubscriptionInventoryCollector {

    @Autowired
    AzureCredentialProvider azureCredentialProvider;

    private static final Logger log = LoggerFactory.getLogger(SubscriptionInventoryCollector.class);

    public List<SubscriptionVH> fetchSubscriptions(SubscriptionVH subscription) throws UnsupportedEncodingException {

        List<SubscriptionVH> subscriptionList = new ArrayList<>();

        SubscriptionVH subscriptionVH = new SubscriptionVH();
        subscriptionVH.setTenant(subscription.getTenant());
        subscriptionVH.setSubscriptionId(subscription.getSubscriptionId());
        subscriptionVH.setId(subscription.getSubscriptionId());
        subscriptionVH.setSubscriptionName(subscription.getSubscriptionName());
        subscriptionVH.setSubscription(subscription.getSubscriptionId());

        subscriptionVH.setStorageAccountLogList(fetchStorageAccountActivityLog(subscriptionVH));

        subscriptionList.add(subscriptionVH);

        log.info("Size of subscriptions: {}", subscriptionList.size());
        return subscriptionList;
    }

    private List<StorageAccountActivityLogVH>fetchStorageAccountActivityLog(SubscriptionVH subscription) throws UnsupportedEncodingException {

        String apiUrlTemplate="https://management.azure.com/subscriptions/%s/providers/Microsoft.Insights/diagnosticSettings?api-version=2021-05-01-preview";

        List<StorageAccountActivityLogVH>storageAccountActivityLogVHList=new ArrayList<>();
        String accessToken = azureCredentialProvider.getToken(subscription.getTenant());
        Azure azure = azureCredentialProvider.authenticate(subscription.getTenant(),
                subscription.getSubscriptionId());

        PagedList<StorageAccount> storageAccounts = azure.storageAccounts().list();


            String url = String.format(apiUrlTemplate, URLEncoder.encode(subscription.getSubscriptionId(),java.nio.charset.StandardCharsets.UTF_8.toString()));

                try{
                    String response = CommonUtils.doHttpGet(url, "Bearer", accessToken);
                    JsonObject  responseObj = new JsonParser().parse(response).getAsJsonObject();
                    JsonArray storageObjects = responseObj.getAsJsonArray("value");

                    for(JsonElement storageObjElement:storageObjects){
                        StorageAccountActivityLogVH storageAccountActivityLogVH=new StorageAccountActivityLogVH();
                        JsonObject  storageObject = storageObjElement.getAsJsonObject();
                        JsonObject properties = storageObject.getAsJsonObject("properties");
                        log.debug("Properties data{}",properties);
                        if(properties!=null) {
                            String storageAccountId=properties.get("storageAccountId").getAsString();
                            storageAccountActivityLogVH.setStorageAccountActivityLogContainerId(storageAccountId);
                            storageAccountActivityLogVH.setStorageAccountEncryptionKeySource(checkStorageAccountEncryptionKeySource(storageAccountId,storageAccounts));
                        }

                        storageAccountActivityLogVHList.add(storageAccountActivityLogVH);
                    }
                }
            catch (Exception e){
                log.error("Error while fetching storage account for alert: {}", e);
            }

        return storageAccountActivityLogVHList;
    }

    private String checkStorageAccountEncryptionKeySource(String storageAccountId, PagedList<StorageAccount> storageAccounts) {

        String result="";

        for (StorageAccount storageAccount : storageAccounts) {
            if(storageAccountId.equalsIgnoreCase(storageAccount.id())){
                return storageAccount.encryptionKeySource().toString();
            }
        }

        return result;
    }
}

