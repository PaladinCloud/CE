package com.tmobile.pacbot.azure.inventory.collector;

import com.google.gson.*;
import com.microsoft.azure.PagedList;
import com.microsoft.azure.keyvault.models.KeyVaultErrorException;
import com.microsoft.azure.management.Azure;
import com.microsoft.azure.management.keyvault.Key;
import com.microsoft.azure.management.keyvault.Secret;
import com.microsoft.azure.management.keyvault.Vault;
import com.tmobile.pacbot.azure.inventory.auth.AzureCredentialProvider;
import com.tmobile.pacbot.azure.inventory.util.ErrorManageUtil;
import com.tmobile.pacbot.azure.inventory.vo.AzureVH;
import com.tmobile.pacbot.azure.inventory.vo.SubscriptionVH;
import com.tmobile.pacbot.azure.inventory.vo.VaultVH;
import com.tmobile.pacman.commons.utils.CommonUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.net.URLEncoder;
import java.util.*;

@Component
public class VaultInventoryCollector implements Collector {

    private static final Logger log = LoggerFactory.getLogger(VaultInventoryCollector.class);
    private final String vaultDetailsTemplate = "https://management.azure.com/%s?api-version=2022-07-01";
    @Autowired
    AzureCredentialProvider azureCredentialProvider;

    public VaultVH fetchVaultDetailsById(String keyVaultId, SubscriptionVH subscription) {
        Azure azure = azureCredentialProvider.getClient(subscription.getTenant(), subscription.getSubscriptionId());

        VaultVH vaultVH = new VaultVH();
        String url = String.format(vaultDetailsTemplate, URLEncoder.encode(keyVaultId));
        String accessToken = azureCredentialProvider.getToken(subscription.getTenant());


        try {
            String response = CommonUtils.doHttpGet(url, "Bearer", accessToken);
            JsonObject vaultObject = new JsonParser().parse(response).getAsJsonObject();

            vaultVH.setSubscription(subscription.getSubscriptionId());
            vaultVH.setSubscriptionName(subscription.getSubscriptionName());
            vaultVH.setId(vaultObject.get("id").getAsString());
            vaultVH.setLocation(vaultObject.get("location").getAsString());
            vaultVH.setRegion(Util.getRegionValue(subscription, vaultObject.get("location").getAsString()));
            vaultVH.setResourceGroupName(Util.getResourceGroupNameFromId(vaultObject.get("id").getAsString()));
            vaultVH.setName(vaultObject.get("name").getAsString());
            vaultVH.setType(vaultObject.get("type").getAsString());
            JsonObject properties = vaultObject.getAsJsonObject("properties");
            JsonObject tags = vaultObject.getAsJsonObject("tags");
            if (properties != null) {
                log.info("********* vault Inventory *****>{}", properties);
                HashMap<String, Object> propertiesMap = new Gson().fromJson(properties.toString(),
                        HashMap.class);
                if (propertiesMap.get("enabledForDeployment") != null) {
                    vaultVH.setEnabledForDeployment((boolean) propertiesMap.get("enabledForDeployment"));
                }

                if (propertiesMap.get("enabledForDiskEncryption") != null) {
                    vaultVH.setEnabledForDeployment((boolean) propertiesMap.get("enabledForDiskEncryption"));
                }

                if (propertiesMap.get("enabledForTemplateDeployment") != null) {
                    vaultVH.setEnabledForDeployment((boolean) propertiesMap.get("enabledForTemplateDeployment"));
                }

                vaultVH.setTenantId(propertiesMap.get("tenantId") != null ? propertiesMap.get("tenantId").toString() : null);
                vaultVH.setProvisioningState(propertiesMap.get("provisioningState") != null ? propertiesMap.get("provisioningState").toString() : null);
                vaultVH.setSku(propertiesMap.get("sku") != null ? (Map<String, Object>) propertiesMap.get("sku") : null);
                vaultVH.setVaultUri(propertiesMap.get("vaultUri") != null ? propertiesMap.get("vaultUri").toString() : null);
                if (propertiesMap.get("enablePurgeProtection") != null) {
                    vaultVH.setEnablePurgeProtection((boolean) propertiesMap.get("enablePurgeProtection"));
                }
                if (propertiesMap.get("enableSoftDelete") != null) {
                    vaultVH.setEnableSoftDelete((boolean) propertiesMap.get("enableSoftDelete"));
                }
                if (propertiesMap.get("enableRbacAuthorization") != null) {
                    vaultVH.setEnableRbacAuthorization((boolean) propertiesMap.get("enableRbacAuthorization"));
                }
                if (properties.get("accessPolicies") != null) {
                    JsonArray accessPolicies = properties.getAsJsonArray("accessPolicies");
                    if (accessPolicies.size() > 0) {
                        JsonObject permissions = (JsonObject) ((JsonObject) accessPolicies.get(0)).get("permissions");
                        HashMap<String, List<String>> permissionsMap = new Gson().fromJson(permissions.toString(),
                                HashMap.class);
                        vaultVH.setPermissionForKeys(permissionsMap.get("keys"));
                        vaultVH.setPermissionForSecrets(permissionsMap.get("secrets"));
                        vaultVH.setPermissionForCertificates(permissionsMap.get("certificates"));
                    }
                }
            }

            if (tags != null) {
                HashMap<String, Object> tagsMap = new Gson().fromJson(tags.toString(), HashMap.class);
                vaultVH.setTags(tagsMap);
            }
            String id = vaultVH.getId();
            int beginningIndex = id.indexOf("resourceGroups") + 15;
            String resourceGroupName = (vaultVH.getId()).substring(beginningIndex, id.indexOf('/', beginningIndex + 2));
            vaultVH.setResourceGroupName(resourceGroupName);
            Vault azureVault = azure.vaults().getById(id);
            if (vaultVH.isEnableRbacAuthorization()) {
                setKeyExpirationDate(azureVault, vaultVH);
                setSecretExpirationDate(azureVault, vaultVH);
            }
        } catch (Exception exception) {
            String errorMessage = String.format("Error occurred while collecting VaultInventory for subscriptionId: %s, subscriptionName: %s", subscription.getSubscriptionId(), subscription.getSubscriptionName());
            log.error(errorMessage, exception);
            Util.eCount.getAndIncrement();
            log.debug("Current error count after exception occurred in VaultInventory collector: {}", Util.eCount.get());

        }

        return vaultVH;
    }

    private void setSecretExpirationDate(Vault azureVault, VaultVH vaultVH) {
        try {
            PagedList<Secret> secrets = azureVault.secrets().list();
            Set<String> secretExpirationDate = new HashSet<>();
            for (Secret secret : secrets) {
                if (secret.attributes().expires() != null) {
                    secretExpirationDate.add(secret.attributes().expires().toString());
                } else {
                    //Expiry date is set as null because violation will be raised even if one secret does not have expiry date
                    secretExpirationDate = null;
                    break;
                }
            }

            vaultVH.setSecretExpirationDate(secretExpirationDate);
        } catch (KeyVaultErrorException e) {
            //if permission is denied to get list of secrets, then do not raise any violation
            vaultVH.setSecretExpirationDate(new HashSet<>());
            ErrorManageUtil.uploadError(vaultVH.getSubscription(), vaultVH.getRegion(), "vault", e.getMessage());
            log.error(e.getMessage());
        }
    }

    private void setKeyExpirationDate(Vault azureVault, VaultVH vaultVH) {
        try {
            PagedList<Key> keys = azureVault.keys().list();
            Set<String> keyExpirationDate = new HashSet<>();
            for (Key key : keys) {
                if (key.attributes().expires() != null) {
                    keyExpirationDate.add(key.attributes().expires().toString());
                } else {
                    //Expiry date is set as null because violation will be raised even if one key does not have expiry date
                    keyExpirationDate = null;
                    break;
                }
            }
            vaultVH.setKeyExpirationDate(keyExpirationDate);
        } catch (KeyVaultErrorException e) {
            //if permission is denied to get list of keys, then do not raise any violation
            vaultVH.setKeyExpirationDate(new HashSet<>());
            ErrorManageUtil.uploadError(vaultVH.getSubscription(), vaultVH.getRegion(), "vault", e.getMessage());
            log.error(e.getMessage());
        }
    }

    @Override
    public List<? extends AzureVH> collect() {
        throw new UnsupportedOperationException();
    }

    public List<VaultVH> collect(SubscriptionVH subscription) {
        List<VaultVH> vaultList = new ArrayList<>();
        String accessToken = azureCredentialProvider.getToken(subscription.getTenant());
        String vaultListTemplate = "https://management.azure.com/subscriptions/" + subscription.getSubscriptionId() + "/resources?$filter=resourceType%20eq%20'Microsoft.KeyVault/vaults'&api-version=2015-11-01";
        try {

            String response = CommonUtils.doHttpGet(vaultListTemplate, "Bearer", accessToken);
            JsonObject responseObj = new JsonParser().parse(response).getAsJsonObject();

            JsonArray vaultObjects = responseObj.getAsJsonArray("value");
            if (vaultObjects != null) {
                for (JsonElement vaultElement : vaultObjects) {
                    JsonObject vaultObject = vaultElement.getAsJsonObject();
                    VaultVH vault = fetchVaultDetailsById(vaultObject.get("id").getAsString(), subscription);
                    vaultList.add(vault);
                }
            }
        } catch (Exception e) {
            log.error("Error Collecting Vaults ", e);
            Util.eCount.getAndIncrement();
        }

        log.info("Target Type : {}  Total: {} ", "Vault", vaultList.size());
        return vaultList;
    }

    @Override
    public List<? extends AzureVH> collect(SubscriptionVH subscription, Map<String, Map<String, String>> tagMap) {
        throw new UnsupportedOperationException();
    }
}
