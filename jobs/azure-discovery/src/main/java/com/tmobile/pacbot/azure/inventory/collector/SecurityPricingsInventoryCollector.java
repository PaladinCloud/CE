package com.tmobile.pacbot.azure.inventory.collector;

import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

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
import com.tmobile.pacbot.azure.inventory.vo.SecurityPricingsVH;
import com.tmobile.pacbot.azure.inventory.vo.SubscriptionVH;
import com.tmobile.pacman.commons.utils.CommonUtils;

@Component
public class SecurityPricingsInventoryCollector {
	
	@Autowired
	AzureCredentialProvider azureCredentialProvider;

	private String apiUrlTemplate = "https://management.azure.com/subscriptions/%s/providers/Microsoft.Security/pricings?api-version=2022-03-01";
	private static Logger log = LoggerFactory.getLogger(SecurityPricingsInventoryCollector.class);
	
	public List<SecurityPricingsVH> fetchSecurityPricingsDetails(SubscriptionVH subscription) throws Exception {

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

				if (properties != null) {
					HashMap<String, Object> propertiesMap = new Gson().fromJson(properties.toString(), HashMap.class);
					securityPricingsVH.setPropertiesMap(propertiesMap);
				}
				securityPricingsList.add(securityPricingsVH);
			}
		} catch (Exception e) {
			log.error("Error collecting Security Pricings",e);
		}

		log.info("Target Type : {}  Total: {} ","Security Pricings",securityPricingsList.size());
		return securityPricingsList;
	}

}
