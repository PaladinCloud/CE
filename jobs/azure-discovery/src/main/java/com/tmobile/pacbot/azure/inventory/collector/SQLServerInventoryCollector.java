package com.tmobile.pacbot.azure.inventory.collector;

import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.tmobile.pacbot.azure.inventory.vo.*;
import com.tmobile.pacman.commons.utils.CommonUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.microsoft.azure.PagedList;
import com.microsoft.azure.management.Azure;
import com.microsoft.azure.management.sql.SqlElasticPool;
import com.microsoft.azure.management.sql.SqlFailoverGroup;
import com.microsoft.azure.management.sql.SqlFirewallRule;
import com.microsoft.azure.management.sql.SqlServer;
import com.microsoft.azure.management.sql.SqlVirtualNetworkRule;
import com.tmobile.pacbot.azure.inventory.auth.AzureCredentialProvider;

@Component
public class SQLServerInventoryCollector {
	
	@Autowired
	AzureCredentialProvider azureCredentialProvider;
	
	private static Logger log = LoggerFactory.getLogger(SQLServerInventoryCollector.class);
	
	public List<SQLServerVH> fetchSQLServerDetails(SubscriptionVH subscription,
			Map<String, Map<String, String>> tagMap) {

		List<SQLServerVH> sqlServerList = new ArrayList<>();
		Azure azure = azureCredentialProvider.getClient(subscription.getTenant(),subscription.getSubscriptionId());
		PagedList<SqlServer> sqlServers = azure.sqlServers().list();

		for (SqlServer sqlServer : sqlServers) {
			SQLServerVH sqlServerVH = new SQLServerVH();
			sqlServerVH.setSubscription(subscription.getSubscriptionId());
			sqlServerVH.setSubscriptionName(subscription.getSubscriptionName());
			sqlServerVH.setId(sqlServer.id());
			sqlServerVH.setKind(sqlServer.kind());
			sqlServerVH.setName(sqlServer.name());
			sqlServerVH.setRegionName(sqlServer.regionName());
			sqlServerVH.setRegion(sqlServer.regionName());
			sqlServerVH.setState(sqlServer.state());
			sqlServerVH.setSystemAssignedManagedServiceIdentityPrincipalId(
					sqlServer.systemAssignedManagedServiceIdentityPrincipalId());
			sqlServerVH.setSystemAssignedManagedServiceIdentityTenantId(
					sqlServer.systemAssignedManagedServiceIdentityTenantId());
			sqlServerVH.setTags(Util.tagsList(tagMap, sqlServer.resourceGroupName(), sqlServer.tags()));
			sqlServerVH.setVersion(sqlServer.version());
			sqlServerVH.setAdministratorLogin(sqlServer.administratorLogin());
			firewallRule(sqlServer, sqlServerVH);
			getElasticPoolList(sqlServer.elasticPools().list(), sqlServerVH);
			getFailoverGroupList(sqlServer.failoverGroups().list(), sqlServerVH);
			setVulnerabilityAssessment(sqlServerVH,subscription,sqlServer);
			sqlServerList.add(sqlServerVH);
		}
		log.info("Target Type : {}  Total: {} ","SqlServer",sqlServerList.size());
		return sqlServerList;

	}

	private void setVulnerabilityAssessment(SQLServerVH sqlServerVH, SubscriptionVH subscription, SqlServer sqlServer) {
		try{
			String apiUrlTemplate="https://management.azure.com/subscriptions/%s/resourceGroups/%s/providers/Microsoft.Sql/servers/%s/vulnerabilityAssessments?api-version=2020-11-01-preview";
			String accessToken = azureCredentialProvider.getToken(subscription.getTenant());
			String url = String.format(apiUrlTemplate,
					URLEncoder.encode(subscription.getSubscriptionId(),
							java.nio.charset.StandardCharsets.UTF_8.toString()),
					URLEncoder.encode(sqlServer.resourceGroupName(),
							java.nio.charset.StandardCharsets.UTF_8.toString()),
					URLEncoder.encode(sqlServer.name(),
							java.nio.charset.StandardCharsets.UTF_8.toString()));
			log.info("The url is {}",url);

			String response = CommonUtils.doHttpGet(url, "Bearer", accessToken);
			log.info("Response is :{}",response);
			JsonObject responseObj = new JsonParser().parse(response).getAsJsonObject();
			JsonArray defenderObjects = responseObj.getAsJsonArray("value");

			for(JsonElement defenderElement:defenderObjects){
				JsonObject  defenderObject = defenderElement.getAsJsonObject();
				JsonObject properties = defenderObject.getAsJsonObject("properties");
				log.debug("Properties data{}",properties);
				if(properties!=null) {
				    if(properties.has("storageContainerPath")) {
						sqlServerVH.setStorageContainerPath(properties.get("storageContainerPath").getAsJsonPrimitive().getAsString());
					}
					if(properties.has("recurringScans")) {
						JsonObject recurringScans = properties.getAsJsonObject("recurringScans");
						sqlServerVH.setRecurringScansEnabled(recurringScans.getAsJsonPrimitive("isEnabled").getAsBoolean());
						if (properties.has("emails")) {
							sqlServerVH.setEmails(recurringScans.getAsJsonArray("emails").getAsString());
						}
						sqlServerVH.setEmailSubscriptionAdmins(recurringScans.getAsJsonPrimitive("emailSubscriptionAdmins").getAsBoolean());
					}
					}

			}
		}
		catch (Exception e) {
			e.printStackTrace();
			throw new RuntimeException(e);
		}

	}

	private void getElasticPoolList(List<SqlElasticPool> sqlElasticPoolList, SQLServerVH sqlServerVH) {
		List<ElasticPoolVH> elasticPoolList = new ArrayList<>();
		for (SqlElasticPool sqlElasticPool : sqlElasticPoolList) {
			ElasticPoolVH elasticPoolVH = new ElasticPoolVH();
			elasticPoolVH.setName(sqlElasticPool.name());
			elasticPoolVH.setSize(sqlElasticPool.listDatabases().size());
			elasticPoolVH.setStorageCapacity(sqlElasticPool.storageCapacityInMB());
			elasticPoolVH.setId(sqlElasticPool.id());
			elasticPoolVH.setStorageMB(sqlElasticPool.storageMB());
			elasticPoolVH.setDtu(sqlElasticPool.dtu());
			elasticPoolVH.setEdition(sqlElasticPool.edition().toString());
			elasticPoolList.add(elasticPoolVH);

		}
		sqlServerVH.setElasticPoolList(elasticPoolList);

	}

	private void firewallRule(SqlServer sqlServer, SQLServerVH sqlServerVH) {
		List<Map<String, String>> firewallRuleList = new ArrayList<>();
		Map<String, String> firewallMap;
		for (SqlFirewallRule sqlFirewallRule : sqlServer.firewallRules().list()) {
			firewallMap = new HashMap<>();
			firewallMap.put("name", sqlFirewallRule.name());
			firewallMap.put("startIPAddress", sqlFirewallRule.startIPAddress());
			firewallMap.put("endIPAddress", sqlFirewallRule.endIPAddress());
			firewallRuleList.add(firewallMap);

		}
		for (SqlVirtualNetworkRule sqlVirtualNetworkRule : sqlServer.virtualNetworkRules().list()) {
			firewallMap = new HashMap<>();

			firewallMap.put("virtualNetworkRuleName",
					sqlVirtualNetworkRule.name() != null ? sqlVirtualNetworkRule.name() : "");
			firewallMap.put("virtualNetworkSubnetId",
					sqlVirtualNetworkRule.subnetId() != null ? sqlVirtualNetworkRule.subnetId() : "");
			firewallMap.put("virtualNetworkResourceGroupName",
					sqlVirtualNetworkRule.resourceGroupName() != null ? sqlVirtualNetworkRule.resourceGroupName() : "");
			firewallMap.put("virtualNetworkState",
					sqlVirtualNetworkRule.state() != null ? sqlVirtualNetworkRule.state() : "");

			firewallRuleList.add(firewallMap);
		}
		sqlServerVH.setFirewallRuleDetails(firewallRuleList);
	}

	private void getFailoverGroupList(List<SqlFailoverGroup> sqlFailoverGroupList, SQLServerVH sqlServerVH) {
		List<FailoverGroupVH> failoverGroupList = new ArrayList<>();
		for (SqlFailoverGroup sqlFailoverGroup : sqlFailoverGroupList) {
			FailoverGroupVH failoverGroupVH = new FailoverGroupVH();
			failoverGroupVH.setSize(sqlFailoverGroup.databases().size());
			failoverGroupVH.setId(sqlFailoverGroup.id());
			failoverGroupVH.setName(sqlFailoverGroup.name());
			failoverGroupVH.setReplicationState(sqlFailoverGroup.replicationState());
			failoverGroupVH.setReadOnlyEndpointPolicy(sqlFailoverGroup.readOnlyEndpointPolicy().toString());
			failoverGroupVH.setReadWriteEndpointPolicy(sqlFailoverGroup.readWriteEndpointPolicy().toString());
			failoverGroupVH.setGracePeriod(sqlFailoverGroup.readWriteEndpointDataLossGracePeriodMinutes());
			failoverGroupList.add(failoverGroupVH);

		}
		sqlServerVH.setFailoverGroupList(failoverGroupList);

	}

}
