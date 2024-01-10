package com.tmobile.pacbot.azure.inventory.collector;

import com.google.gson.*;
import com.tmobile.pacbot.azure.inventory.auth.AzureCredentialProvider;
import com.tmobile.pacbot.azure.inventory.vo.SubscriptionVH;
import com.tmobile.pacbot.azure.inventory.vo.WorkflowVH;
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
public class WorkflowInventoryCollector {
	
	@Autowired
	AzureCredentialProvider azureCredentialProvider;
	
	private static Logger log = LoggerFactory.getLogger(WorkflowInventoryCollector.class);
	private String apiUrlTemplate = "https://management.azure.com/subscriptions/%s/providers/Microsoft.Logic/workflows?api-version=2016-06-01";

	public List<WorkflowVH> fetchWorkflowDetails(SubscriptionVH subscription) throws Exception {

		List<WorkflowVH> workflowList = new ArrayList<WorkflowVH>();
		String accessToken = azureCredentialProvider.getToken(subscription.getTenant());

		String url = String.format(apiUrlTemplate, URLEncoder.encode(subscription.getSubscriptionId()));
		try {

			String response = CommonUtils.doHttpGet(url, "Bearer", accessToken);
			JsonObject responseObj = new JsonParser().parse(response).getAsJsonObject();
			JsonArray workflowObjects = responseObj.getAsJsonArray("value");
			if (workflowObjects != null) {
				for (JsonElement workflowElement : workflowObjects) {
					WorkflowVH workflowVH = new WorkflowVH();
					JsonObject workflowObject = workflowElement.getAsJsonObject();
					workflowVH.setSubscription(subscription.getSubscriptionId());
					workflowVH.setSubscriptionName(subscription.getSubscriptionName());
					workflowVH.setRegion(Util.getRegionValue(subscription,workflowObject.get("location").getAsString()));
					workflowVH.setResourceGroupName(Util.getResourceGroupNameFromId(workflowObject.get("id").getAsString()));
					workflowVH.setId(workflowObject.get("id").getAsString());
					workflowVH.setLocation(workflowObject.get("location").getAsString());
					workflowVH.setName(workflowObject.get("name").getAsString());
					workflowVH.setType(workflowObject.get("type").getAsString());
					JsonObject properties = workflowObject.getAsJsonObject("properties");
					JsonObject tags = workflowObject.getAsJsonObject("tags");
					if (properties != null) {
						HashMap<String, Object> propertiesMap = new Gson().fromJson(properties.toString(),
								HashMap.class);
						workflowVH.setProperties(propertiesMap);
					}
					if (tags != null) {
						HashMap<String, Object> tagsMap = new Gson().fromJson(tags.toString(), HashMap.class);
						workflowVH.setTags(tagsMap);
					}

					workflowList.add(workflowVH);
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
			Util.eCount.getAndIncrement();
		}

		log.info("Target Type : {}  Total: {} ","workflow",workflowList.size());
		return workflowList;
	}


}
