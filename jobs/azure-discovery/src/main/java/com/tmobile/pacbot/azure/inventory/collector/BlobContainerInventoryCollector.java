package com.tmobile.pacbot.azure.inventory.collector;

import com.google.gson.*;
import com.microsoft.azure.PagedList;
import com.microsoft.azure.management.Azure;
import com.microsoft.azure.management.storage.StorageAccount;
import com.tmobile.pacbot.azure.inventory.auth.AzureCredentialProvider;
import com.tmobile.pacbot.azure.inventory.vo.AzureVH;
import com.tmobile.pacbot.azure.inventory.vo.BlobContainerVH;
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
public class BlobContainerInventoryCollector implements Collector {

    private static Logger log = LoggerFactory.getLogger(BlobContainerInventoryCollector.class);
    @Autowired
    AzureCredentialProvider azureCredentialProvider;
    private String apiUrlTemplate = "https://management.azure.com/subscriptions/%s/resourceGroups/%s/providers/Microsoft.Storage/storageAccounts/%s/blobServices/default/containers?api-version=2022-09-01";

    @Override
    public List<? extends AzureVH> collect() {
        throw new UnsupportedOperationException();
    }

    @Override
    public List<? extends AzureVH> collect(SubscriptionVH subscription) {
        throw new UnsupportedOperationException();
    }

    public List<BlobContainerVH> collect(SubscriptionVH subscription, Map<String, Map<String, String>> tagMap) {

        List<BlobContainerVH> blobContainerList = new ArrayList<BlobContainerVH>();
        String accessToken = azureCredentialProvider.getToken(subscription.getTenant());
        Azure azure = azureCredentialProvider.getClient(subscription.getTenant(), subscription.getSubscriptionId());
        PagedList<StorageAccount> storageAccounts = azure.storageAccounts().list();
        for (StorageAccount storageAccount : storageAccounts) {
            if (!(storageAccount.kind().toString().equals("FileStorage"))) {
                String url = String.format(apiUrlTemplate, URLEncoder.encode(subscription.getSubscriptionId()),
                        URLEncoder.encode(storageAccount.resourceGroupName()), URLEncoder.encode(storageAccount.name()));
                try {
                    String response = CommonUtils.doHttpGet(url, "Bearer", accessToken);
                    JsonObject responseObj = new JsonParser().parse(response).getAsJsonObject();
                    JsonArray blobObjects = responseObj.getAsJsonArray("value");
                    for (JsonElement blobObjectElement : blobObjects) {
                        Map<String, String> tags = new HashMap<String, String>();
                        BlobContainerVH blobContainerVH = new BlobContainerVH();
                        blobContainerVH.setSubscription(subscription.getSubscriptionId());
                        blobContainerVH.setSubscriptionName(subscription.getSubscriptionName());
                        blobContainerVH.setResourceGroupName(storageAccount.resourceGroupName());
                        blobContainerVH.setRegion(Util.getRegionValue(subscription, storageAccount.regionName()));
                        JsonObject blobObject = blobObjectElement.getAsJsonObject();
                        JsonObject properties = blobObject.getAsJsonObject("properties");
                        blobContainerVH.setId(blobObject.get("id").getAsString());
                        blobContainerVH.setName(blobObject.get("name").getAsString());
                        blobContainerVH.setType(blobObject.get("type").getAsString());
                        blobContainerVH.setTag(blobObject.get("etag").getAsString());
                        blobContainerVH.setHasImmutabilityPolicy(properties.get("hasImmutabilityPolicy").getAsBoolean());
                        blobContainerVH.setHasLegalHold(properties.get("hasLegalHold").getAsBoolean());
                        blobContainerVH.setTags(Util.tagsList(tagMap, storageAccount.resourceGroupName(), tags));
                        HashMap<String, Object> propertiesMap = new Gson().fromJson(properties.toString(),
                                HashMap.class);
                        blobContainerVH.setPropertiesMap(propertiesMap);
                        blobContainerList.add(blobContainerVH);
                    }

                } catch (Exception e) {
                    log.error(" Error fetching blob containers for storage account {} Cause : {}", storageAccount.name(), e.getMessage());
                    Util.eCount.getAndIncrement();
                }
            }
        }

        log.info("Target Type : {}  Total: {} ", "Blob Container", blobContainerList.size());
        return blobContainerList;
    }
}
