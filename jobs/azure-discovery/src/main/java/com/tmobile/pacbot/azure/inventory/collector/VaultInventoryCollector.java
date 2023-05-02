package com.tmobile.pacbot.azure.inventory.collector;

import java.net.URLEncoder;
import java.util.*;

import com.microsoft.azure.PagedList;
import com.microsoft.azure.management.Azure;
import com.microsoft.azure.management.keyvault.Key;
import com.microsoft.azure.management.keyvault.Secret;
import com.microsoft.azure.management.keyvault.Vault;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.tmobile.pacbot.azure.inventory.auth.AzureCredentialProvider;
import com.tmobile.pacbot.azure.inventory.vo.SubscriptionVH;
import com.tmobile.pacbot.azure.inventory.vo.VaultVH;
import com.tmobile.pacman.commons.utils.CommonUtils;

@Component
public class VaultInventoryCollector {

	@Autowired
	AzureCredentialProvider azureCredentialProvider;

	private static Logger log = LoggerFactory.getLogger(VaultInventoryCollector.class);
	private String vaultDetailsTemplate="https://management.azure.com/%s?api-version=2022-07-01";

	public VaultVH fetchVaultDetailsById(String keyVaultId,SubscriptionVH subscription)  {
		Azure azure = azureCredentialProvider.getClient(subscription.getTenant(),subscription.getSubscriptionId());

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
			vaultVH.setRegion(vaultObject.get("location").getAsString());
			vaultVH.setResourceGroupName(Util.getResourceGroupNameFromId(vaultObject.get("id").getAsString()));
			vaultVH.setName(vaultObject.get("name").getAsString());
			vaultVH.setType(vaultObject.get("type").getAsString());
			JsonObject properties = vaultObject.getAsJsonObject("properties");
			JsonObject tags = vaultObject.getAsJsonObject("tags");
			if (properties != null) {
				log.info("********* valutInventory *****>{}", properties);
				HashMap<String, Object> propertiesMap = new Gson().fromJson(properties.toString(),
						HashMap.class);
				vaultVH.setEnabledForDeployment((boolean) propertiesMap.get("enabledForDeployment"));
				vaultVH.setEnabledForDiskEncryption((boolean) propertiesMap.get("enabledForDiskEncryption"));
				vaultVH.setEnabledForTemplateDeployment(
						(boolean) propertiesMap.get("enabledForTemplateDeployment"));
				vaultVH.setTenantId(propertiesMap.get("tenantId").toString());
				vaultVH.setProvisioningState(propertiesMap.get("provisioningState").toString());
				vaultVH.setSku((Map<String, Object>) propertiesMap.get("sku"));
				vaultVH.setVaultUri(propertiesMap.get("vaultUri").toString());
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
			log.debug("Resource group name: {}", resourceGroupName);
			vaultVH.setResourceGroupName(resourceGroupName);
			try {
				Vault azureVault = azure.vaults().getById(id);
				PagedList<Key> keys = azureVault.keys().list();
				Set<String> keyExpirationDate = new HashSet<>();
				for (Key key : keys) {
					if (key.attributes().expires() != null) {
						keyExpirationDate.add(key.attributes().expires().toString());
					} else {
						keyExpirationDate = null;
						break;
					}
				}
				vaultVH.setKeyExpirationDate(keyExpirationDate);
				PagedList<Secret> secrets = azureVault.secrets().list();
				Set<String> secretExpirationDate = new HashSet<>();
				for (Secret secret : secrets) {
					if (secret.attributes().expires() != null) {
						secretExpirationDate.add(secret.attributes().expires().toString());
					} else {
						secretExpirationDate = null;
						break;
					}
				}
				vaultVH.setSecretExpirationDate(secretExpirationDate);

			} catch (Exception e) {
				log.error(e.getMessage());
			}
		} catch (Exception e) {
			log.error(e.getMessage());

		}
		return  vaultVH;
	}

	public HashMap<String,List<VaultVH>> fetchVaultDetails(SubscriptionVH subscription) throws Exception {

		List<VaultVH> vaultList = new ArrayList();
		List<VaultVH> vaultRBACList=new ArrayList();
		HashMap<String,List<VaultVH>> vaults=new HashMap();
		String accessToken = azureCredentialProvider.getToken(subscription.getTenant());
		String vaultListTemplate = "https://management.azure.com/subscriptions/" + subscription.getSubscriptionId() + "/resources?$filter=resourceType%20eq%20'Microsoft.KeyVault/vaults'&api-version=2015-11-01";


		try {

			String response = CommonUtils.doHttpGet(vaultListTemplate, "Bearer", accessToken);
			JsonObject responseObj = new JsonParser().parse(response).getAsJsonObject();

			JsonArray vaultObjects = responseObj.getAsJsonArray("value");
			if (vaultObjects != null) {
				for (JsonElement vaultElement : vaultObjects) {
					JsonObject vaultObject = vaultElement.getAsJsonObject();
					VaultVH vault=fetchVaultDetailsById(vaultObject.get("id").getAsString(),subscription);
					if(!vault.isEnableRbacAuthorization()) {
						vaultList.add(vault);
					}else {
						vaultRBACList.add(vault);
					}
				}

			}
			vaults.put("vaultList",vaultList);
			vaults.put("vaultRBACList",vaultRBACList);
		} catch (Exception e) {
			log.error("Error Colectting vaults ",e);
		}

		log.info("Target Type : {}  Total: {} ","Vault",vaultList.size());
		return vaults;
	}

}
