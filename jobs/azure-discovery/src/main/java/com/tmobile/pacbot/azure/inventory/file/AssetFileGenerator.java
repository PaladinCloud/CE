package com.tmobile.pacbot.azure.inventory.file;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

import com.tmobile.pacbot.azure.inventory.collector.*;
import com.tmobile.pacbot.azure.inventory.vo.VaultVH;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.microsoft.azure.management.Azure;
import com.tmobile.pacbot.azure.inventory.auth.AzureCredentialProvider;
import com.tmobile.pacbot.azure.inventory.vo.PolicyDefinitionVH;
import com.tmobile.pacbot.azure.inventory.vo.ResourceGroupVH;
import com.tmobile.pacbot.azure.inventory.vo.SubscriptionVH;

@Component
public class AssetFileGenerator {

	@Autowired
	AzureCredentialProvider azureCredentialProvider;
	/** The target types. */
	@Value("${targetTypes:}")
	private String targetTypes;

	/** The log. */
	private static Logger log = LoggerFactory.getLogger(AssetFileGenerator.class);

	@Autowired
	VMInventoryCollector vmInventoryCollector;

	@Autowired
	DiskInventoryCollector diskInventoryCollector;

	@Autowired
	LoadBalancerInventoryCollector loadBalancerInventoryCollector;

	@Autowired
	NetworkInterfaceInventoryCollector networkInterfaceInventoryCollector;

	@Autowired
	NSGInventoryCollector networkSecurityInventoryCollector;

	@Autowired
	SQLDatabaseInventoryCollector sqlDatabaseInventoryCollector;

	@Autowired
	StorageAccountInventoryCollector storageAccountInventoryCollector;

	@Autowired
	NetworkInventoryCollector networkInventoryCollector;

	@Autowired
	SCRecommendationsCollector scRecommendationsCollector;

	@Autowired
	SQLServerInventoryCollector sqlServerInventoryCollector;

	@Autowired
	BlobContainerInventoryCollector blobContainerInventoryCollector;

	@Autowired
	ResourceGroupInventoryCollector resourceGroupInventoryCollector;

	@Autowired
	CosmosDBInventoryCollector cosmosDBInventoryCollector;

	@Autowired
	RegisteredApplicationInventoryCollector registeredApplicationInventoryCollector;

	@Autowired
	MySQLInventoryCollector mySQLInventoryCollector;

	@Autowired
	DatabricksInventoryCollector databricksInventoryCollector;

	@Autowired
	MariaDBInventoryCollector mariaDBInventoryCollector;

	@Autowired
	PostgreSQLInventoryCollector postgreSQLInventoryCollector;

	@Autowired
	SnapshotInventoryCollector snapshotInventoryCollector;

	@Autowired
	PublicIpAddressInventoryCollector publicIpAddressInventoryCollector;

	@Autowired
	RouteTableInventoryCollector routeTableInventoryCollector;

	@Autowired
	SecurityAlertsInventoryCollector securityAlertsInventoryCollector;

	@Autowired
	SecurityPricingsInventoryCollector securityPricingsInventoryCollector;

	@Autowired
	PolicyStatesInventoryCollector policyStatesInventoryCollector;

	@Autowired
	PolicyDefinitionInventoryCollector policyDefinitionInventoryCollector;

	@Autowired
	SitesInventoryCollector sitesInventoryCollector;

	@Autowired
	VaultInventoryCollector vaultInventoryCollector;

	@Autowired
	WorkflowInventoryCollector workflowInventoryCollector;

	@Autowired
	BatchAccountInventoryCollector batchAccountInventoryCollector;

	@Autowired
	NamespaceInventoryCollector namespaceInventoryCollector;

	@Autowired
	SearchServiceInventoryCollector searchServiceInventoryCollector;

	@Autowired
	SubnetInventoryCollector subnetInventoryCollector;

	@Autowired
	RedisCacheInventoryCollector redisCacheInventoryCollector;

	@Autowired
	ActivityLogsCollector activityLogsCollector;
	@Autowired
	WebAppInventoryCollector webAppInventoryCollector;

	@Autowired
	SubscriptionInventoryCollector subscriptionInventoryCollector;

	@Autowired
	FunctionAppInventoryCollector functionAppInventoryCollector;

	@Autowired
	MySQLFlexibleInventoryCollector mySQLFlexibleInventoryCollector;

	@Autowired
	BlobServiceInventoryCollector blobServiceInventoryCollector;

	@Autowired
	DiagnosticSettingsCollector diagnosticSettingsCollector;

	@Autowired
	SecurityContactsCollector securityContactsCollector;

	@Autowired
	KubernetesServicesCollector kubernetesServicesCollector;

	public void generateFiles(List<SubscriptionVH> subscriptions, String filePath) {

		try {
			FileManager.initialise(filePath);
		} catch (IOException e1) {
			e1.printStackTrace();
		}


		for (SubscriptionVH subscription : subscriptions) {
			log.info("Started Discovery for sub {}", subscription);

			try {
				String accessToken = azureCredentialProvider.getAuthToken(subscription.getTenant());
				Azure azure = azureCredentialProvider.authenticate(subscription.getTenant(),
						subscription.getSubscriptionId());
				azureCredentialProvider.putClient(subscription.getTenant(), subscription.getSubscriptionId(), azure);
				azureCredentialProvider.putToken(subscription.getTenant(), accessToken);
			} catch (Exception e) {
				log.error("Error authenticating for {}", subscription, e);
				continue;
			}

			List<ResourceGroupVH> resourceGroupList = new ArrayList<ResourceGroupVH>();
			try {
				resourceGroupList = resourceGroupInventoryCollector.fetchResourceGroupDetails(subscription);
			} catch (Exception e) {
				e.printStackTrace();
			}
			Map<String, Map<String, String>> tagMap = resourceGroupList.stream()
					.collect(Collectors.toMap(x -> x.getResourceGroupName().toLowerCase(), x -> x.getTags()));

			List<PolicyDefinitionVH> policyDefinitionList = policyDefinitionInventoryCollector
					.fetchPolicyDefinitionDetails(subscription);
			ExecutorService executor = Executors.newCachedThreadPool();

			executor.execute(() -> {
				if (!(isTypeInScope("virtualmachine"))) {
					return;
				}
				try {
					FileManager.generateVMFiles(vmInventoryCollector.fetchVMDetails(subscription, tagMap));
				} catch (Exception e) {
					e.printStackTrace();
				}
			});

			executor.execute(() -> {
				if (!(isTypeInScope("storageaccount"))) {
					return;
				}
				try {
					FileManager.generateStorageAccountFiles(
							storageAccountInventoryCollector.fetchStorageAccountDetails(subscription, tagMap));
				} catch (Exception e) {
					e.printStackTrace();
				}
			});
			executor.execute(() -> {
				if (!(isTypeInScope("sqldatabase"))) {
					return;
				}
				try {
					FileManager.generateSQLdatabaseFiles(
							sqlDatabaseInventoryCollector.fetchSQLDatabaseDetails(subscription, tagMap));
				} catch (Exception e) {
					e.printStackTrace();
				}
			});
			executor.execute(() -> {
				if (!(isTypeInScope("nsg"))) {
					return;
				}
				try {
					FileManager.generateNetworkSecurityFiles(
							networkSecurityInventoryCollector.fetchNetworkSecurityGroupDetails(subscription, tagMap));
				} catch (Exception e) {
					e.printStackTrace();
				}
			});
			executor.execute(() -> {
				if (!(isTypeInScope("disk"))) {
					return;
				}
				try {
					FileManager
							.generateDataDiskFiles(diskInventoryCollector.fetchDataDiskDetails(subscription, tagMap));
				} catch (Exception e) {
					e.printStackTrace();
				}
			});
			executor.execute(() -> {
				if (!(isTypeInScope("networkinterface"))) {
					return;
				}
				try {
					FileManager.generateNetworkInterfaceFiles(
							networkInterfaceInventoryCollector.fetchNetworkInterfaceDetails(subscription, tagMap));
				} catch (Exception e) {
					e.printStackTrace();
				}
			});

			executor.execute(() -> {
				if (!(isTypeInScope("vnet"))) {
					return;
				}
				try {
					FileManager
							.generateNetworkFiles(networkInventoryCollector.fetchNetworkDetails(subscription, tagMap));
				} catch (Exception e) {
					e.printStackTrace();
				}
			});

			executor.execute(() -> {
				if (!(isTypeInScope("loadbalancer"))) {
					return;
				}
				try {
					FileManager.generateLoadBalancerFiles(
							loadBalancerInventoryCollector.fetchLoadBalancerDetails(subscription, tagMap));
				} catch (Exception e) {
					e.printStackTrace();
				}
			});

			executor.execute(() -> {
				if (!(isTypeInScope("securitycenter"))) {
					return;
				}

				try {
					FileManager.generateSecurityCenterFiles(
							scRecommendationsCollector.fetchSecurityCenterRecommendations(subscription));
				} catch (Exception e) {
					e.printStackTrace();
				}
			});

			executor.execute(() -> {
				if (!(isTypeInScope("sqlserver"))) {
					return;
				}

				try {
					FileManager.generateSQLServerFiles(
							sqlServerInventoryCollector.fetchSQLServerDetails(subscription, tagMap));
				} catch (Exception e) {
					e.printStackTrace();
				}
			});

			executor.execute(() -> {
				if (!(isTypeInScope("blobcontainer"))) {
					return;
				}

				try {
					FileManager.generateBlobContainerFiles(
							blobContainerInventoryCollector.fetchBlobContainerDetails(subscription, tagMap));
				} catch (Exception e) {
					e.printStackTrace();
				}
			});

			executor.execute(() -> {
				if (!(isTypeInScope("resourcegroup"))) {
					return;
				}

				try {
					FileManager.generateResourceGroupFiles(
							resourceGroupInventoryCollector.fetchResourceGroupDetails(subscription));
				} catch (Exception e) {
					e.printStackTrace();
				}
			});

			executor.execute(() -> {
				if (!(isTypeInScope("cosmosdb"))) {
					return;
				}

				try {
					FileManager.generateCosmosDBFiles(
							cosmosDBInventoryCollector.fetchCosmosDBDetails(subscription, tagMap));
				} catch (Exception e) {
					e.printStackTrace();
				}
			});
			executor.execute(() -> {
				if (!(isTypeInScope("mysqlserver"))) {
					return;
				}

				try {
					FileManager.generateMySqlServerFiles(mySQLInventoryCollector.fetchMySQLServerDetails(subscription));
				} catch (Exception e) {
					e.printStackTrace();
				}
			});

			executor.execute(() -> {
				if (!(isTypeInScope("databricks"))) {
					return;
				}

				try {
					FileManager
							.generateDatabricksFiles(databricksInventoryCollector.fetchDatabricksDetails(subscription));
				} catch (Exception e) {
					e.printStackTrace();
				}
			});

			executor.execute(() -> {
				if (!(isTypeInScope("mariadb"))) {
					return;
				}

				try {
					FileManager.generateMariaDBFiles(mariaDBInventoryCollector.fetchMariaDBDetails(subscription));
				} catch (Exception e) {
					e.printStackTrace();
				}
			});

			executor.execute(() -> {
				if (!(isTypeInScope("postgresql"))) {
					return;
				}

				try {
					FileManager.generatePostgreSQLServerFiles(
							postgreSQLInventoryCollector.fetchPostgreSQLServerDetails(subscription));
				} catch (Exception e) {
					e.printStackTrace();
				}
			});

			executor.execute(() -> {
				if (!(isTypeInScope("snapshot"))) {
					return;
				}

				try {
					FileManager.generateSnapshotFiles(
							snapshotInventoryCollector.fetchSnapshotDetails(subscription, tagMap));
				} catch (Exception e) {
					e.printStackTrace();
				}
			});

			executor.execute(() -> {
				if (!(isTypeInScope("publicipaddress"))) {
					return;
				}

				try {
					FileManager.generatePublicIpAddressFiles(
							publicIpAddressInventoryCollector.fetchPublicIpAddressDetails(subscription, tagMap));
				} catch (Exception e) {
					e.printStackTrace();
				}
			});

			executor.execute(() -> {
				if (!(isTypeInScope("routetable"))) {
					return;
				}

				try {
					FileManager.generateRouteTableFiles(
							routeTableInventoryCollector.fetchRouteTableDetails(subscription, tagMap));
				} catch (Exception e) {
					e.printStackTrace();
				}
			});

			executor.execute(() -> {
				if (!(isTypeInScope("securityalerts"))) {
					return;
				}

				try {
					FileManager.generateSecurityAlertsFiles(
							securityAlertsInventoryCollector.fetchSecurityAlertsDetails(subscription));
				} catch (Exception e) {
					e.printStackTrace();
				}
			});

			executor.execute(() -> {
				if (!(isTypeInScope("policyevaluationresults"))) {
					return;
				}

				try {
					FileManager.generatePolicyStatesFiles(policyStatesInventoryCollector
							.fetchPolicyStatesDetails(subscription, policyDefinitionList));
				} catch (Exception e) {
					e.printStackTrace();
				}
			});

			executor.execute(() -> {
				if (!(isTypeInScope("policydefinitions"))) {
					return;
				}

				try {
					FileManager.generatePolicyDefinitionFiles(
							policyDefinitionInventoryCollector.fetchPolicyDefinitionDetails(subscription));
				} catch (Exception e) {
					e.printStackTrace();
				}
			});

			executor.execute(() -> {
				if (!(isTypeInScope("sites"))) {
					return;
				}

				try {
					FileManager.generateSiteFiles(
							sitesInventoryCollector.fetchSitesDetails(subscription));
				} catch (Exception e) {
					e.printStackTrace();
				}
			});

			executor.execute(() -> {
				if (!(isTypeInScope("vaults"))) {
					return;
				}

				try {
					HashMap<String,List<VaultVH>> vaults=vaultInventoryCollector.fetchVaultDetails(subscription);
					FileManager.generateVaultFiles(vaults.get("vaultList"));
					FileManager.generateVaultRABCFiles(vaults.get("vaultRBACList"));
				} catch (Exception e) {
					e.printStackTrace();
				}
			});

			executor.execute(() -> {
				if (!(isTypeInScope("workflows"))) {
					return;
				}

				try {
					FileManager.generateWorkflowFiles(
							workflowInventoryCollector.fetchWorkflowDetails(subscription));
				} catch (Exception e) {
					e.printStackTrace();
				}
			});

			executor.execute(() -> {
				if (!(isTypeInScope("batchaccounts"))) {
					return;
				}

				try {
					FileManager.generateBatchAccountFiles(
							batchAccountInventoryCollector.fetchBatchAccountDetails(subscription));
				} catch (Exception e) {
					e.printStackTrace();
				}
			});

			executor.execute(() -> {
				if (!(isTypeInScope("namespaces"))) {
					return;
				}

				try {
					FileManager.generateNamespaceFiles(
							namespaceInventoryCollector.fetchNamespaceDetails(subscription));
				} catch (Exception e) {
					e.printStackTrace();
				}
			});

			executor.execute(() -> {
				if (!(isTypeInScope("searchservices"))) {
					return;
				}

				try {
					FileManager.generateSearchServiceFiles(
							searchServiceInventoryCollector.fetchSearchServiceDetails(subscription));
				} catch (Exception e) {
					e.printStackTrace();
				}
			});

			executor.execute(() -> {
				if (!(isTypeInScope("subnets"))) {
					return;
				}

				try {
					FileManager.generateSubnetFiles(
							subnetInventoryCollector.fetchSubnetDetails(subscription));
				} catch (Exception e) {
					e.printStackTrace();
				}
			});

			executor.execute(() -> {
				if (!(isTypeInScope("rediscache"))) {
					return;
				}

				try {
					FileManager.generateRedisCacheFiles(
							redisCacheInventoryCollector.fetchRedisCacheDetails(subscription));
				} catch (Exception e) {
					e.printStackTrace();
				}
			});

			executor.execute(() -> {

				if (!(isTypeInScope("activitylog"))) {
					return;
				}

				try {
					FileManager.generateActivityLogFiles(
							activityLogsCollector.fetchActivityLogAlertDetails(subscription));
				} catch (Exception e) {
					e.printStackTrace();
				}
			});

			executor.execute(() -> {
				if (!(isTypeInScope("securitypricings"))) {
					return;
				}

				try {
					FileManager.generateSecurityPricingsFiles(
							securityPricingsInventoryCollector.fetchSecurityPricingsDetails(subscription));
				} catch (Exception e) {
					e.printStackTrace();
				}
			});

			executor.execute(() -> {
				if (!(isTypeInScope("webapp"))) {
					return;
				}
				try {

					FileManager
							.generateWebAppFiles(webAppInventoryCollector.fetchWebAppDetails(subscription));
				} catch (Exception e) {
					e.printStackTrace();
				}
			});

			executor.execute(() -> {
				if (!(isTypeInScope("subscription"))) {
					log.info("no target type found for subscription!!");
					return;
				}
				try {

					FileManager
							.generateSubscriptionFiles(subscriptionInventoryCollector.fetchSubscriptions(subscription));
					log.info("subscription data saved!");
				} catch (Exception e) {
					e.printStackTrace();
				}
			});
			executor.execute(() -> {
				if (!(isTypeInScope("functionapp"))) {
					log.info("no target type found for functionApp!!");
					return;
				}
				try {
					FileManager
							.generateFunctionAppFiles(functionAppInventoryCollector.fetchFunctionAppDetails(subscription));
					log.info("subscription data saved!");
				} catch (Exception e) {
					e.printStackTrace();
				}
			});
			executor.execute(() -> {
				if (!(isTypeInScope("mysqlflexible"))) {
					log.info("no target type found for mysqlflexible!!");
					return;
				}
				try {

					FileManager
							.generateMySQLFlexibleFiles(mySQLFlexibleInventoryCollector.fetchMySQLFlexibleServerDetails(subscription));
					log.info("subscription data saved!");
				} catch (Exception e) {
					e.printStackTrace();
				}
			});
			executor.execute(() -> {
				if (!(isTypeInScope("diagnosticsetting"))) {
					log.info("no target type found for diagnosticsetting!!");
					return;
				}
				try {

					FileManager
							.generateDiagnosticSettingFiles(diagnosticSettingsCollector.fetchDiagnosticSettings(subscription));
					log.info("diagnostic setting data saved!");
				} catch (Exception e) {
					e.printStackTrace();
				}
			});

			executor.execute(() -> {
				if (!(isTypeInScope("blobservice"))) {
					log.info("no target type found for functionApp!!");
					return;
				}
				try {

					FileManager
							.generateBlobServiceFiles(blobServiceInventoryCollector.fetchBlobServiceDetails(subscription));
					log.info("subscription data saved!");
				} catch (Exception e) {
					e.printStackTrace();
				}
			});

			executor.execute(() -> {
				if (!(isTypeInScope("defender"))) {
					log.info("no target type found for functionApp!!");
					return;
				}
				try {
					FileManager.generateSecurityContactsInfoFile(securityContactsCollector.fetchSecurityContactsInfo(subscription));
					log.info("subscription data saved!");
				} catch (Exception e) {
					e.printStackTrace();
				}
			});

			executor.execute(() -> {
				if (!(isTypeInScope("kubernetes"))) {
					log.info("no target type found for functionApp!!");
					return;
				}
				try {
					FileManager.generateKubernetesClusterDetailsInfoFile(kubernetesServicesCollector.fetchKubernetesClusterDetails(subscription));
					log.info("subscription data saved!");
				} catch (Exception e) {
					e.printStackTrace();
				}
			});

			executor.shutdown();
			while (!executor.isTerminated()) {

			}

			log.info("Finished Discovery for sub {}", subscription);
		}

		try {
			FileManager.finalise();
		} catch (IOException e) {
		}
	}

	/**
	 * function for generating registered application file
	 */
	private void generateAzureAplicationList() {

		if ((isTypeInScope("registeredApplication"))) {
			try {
				FileManager.generateRegisteredApplicationFiles(
						registeredApplicationInventoryCollector.fetchAzureRegisteredApplication());
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	private boolean isTypeInScope(String type) {
		if ("".equals(targetTypes)) {
			return true;
		} else {
			List<String> targetTypesList = Arrays.asList(targetTypes.split(","));
			return targetTypesList.contains(type);
		}
	}
}
