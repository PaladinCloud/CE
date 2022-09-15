package com.tmobile.pacbot.azure.inventory.collector;

import com.google.gson.*;
import com.microsoft.azure.PagedList;
import com.microsoft.azure.management.Azure;
import com.microsoft.azure.management.storage.StorageAccount;
import com.tmobile.pacbot.azure.inventory.auth.AzureCredentialProvider;
import com.tmobile.pacbot.azure.inventory.vo.BlobContainerVH;
import com.tmobile.pacbot.azure.inventory.vo.BlobServiceVH;
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
public class BlobServiceInventoryCollector {
    @Autowired
    AzureCredentialProvider azureCredentialProvider;

    private String apiUrlTemplate = "https://management.azure.com/subscriptions/%s/resourceGroups/%s/providers/Microsoft.Storage/storageAccounts/%s/blobServices?api-version=2019-04-01";
    private static Logger log = LoggerFactory.getLogger(BlobServiceInventoryCollector.class);

    public List<BlobServiceVH> fetchBlobServiceDetails(SubscriptionVH subscription) {

        List<BlobServiceVH> blobServiceVHList = new ArrayList<>();
        String accessToken = azureCredentialProvider.getToken(subscription.getTenant());
        Azure azure = azureCredentialProvider.getClient(subscription.getTenant(),subscription.getSubscriptionId());
        PagedList<StorageAccount> storageAccounts = azure.storageAccounts().list();

        for (StorageAccount storageAccount : storageAccounts) {
            String url = String.format(apiUrlTemplate, URLEncoder.encode(subscription.getSubscriptionId()),
                    URLEncoder.encode(storageAccount.resourceGroupName()), URLEncoder.encode(storageAccount.name()));
            try {
                String response = CommonUtils.doHttpGet(url, "Bearer", accessToken);
                JsonObject responseObj = new JsonParser().parse(response).getAsJsonObject();
                JsonArray blobObjects = responseObj.getAsJsonArray("value");
                for (JsonElement blobObjectElement : blobObjects) {
                    BlobServiceVH blobServiceVH = new BlobServiceVH();
                    blobServiceVH.setSubscription(subscription.getSubscriptionId());
                    blobServiceVH.setSubscriptionName(subscription.getSubscriptionName());
                    JsonObject blobObject = blobObjectElement.getAsJsonObject();
                    JsonObject properties = blobObject.getAsJsonObject("properties");
                    log.debug("Properties data{}",properties);
                    blobServiceVH.setId(blobObject.get("id").getAsString());
                    blobServiceVH.setName(blobObject.get("name").getAsString());
                    blobServiceVH.setType(blobObject.get("type").getAsString());
                    if (properties!=null) {
                        HashMap<String, Object> propertiesMap = new Gson().fromJson(properties.toString(),
                                HashMap.class);
                        blobServiceVH.setPropertiesMap(propertiesMap);
                    }
                    blobServiceVHList.add(blobServiceVH);
                }
            } catch (Exception e) {
                log.error(" Error fetching blobService for storage account {} Cause : {}" ,storageAccount.name(),e.getMessage());

            }
        }
        log.info("Target Type : {}  Total: {} ","Blob Container",blobServiceVHList.size());
        return blobServiceVHList;
    }

}
