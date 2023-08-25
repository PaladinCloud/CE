package com.tmobile.pacbot.azure.inventory.file;

import java.io.IOException;
import java.net.URLEncoder;
import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.tmobile.pacbot.azure.inventory.AzureDiscoveryJob;
import com.tmobile.pacbot.azure.inventory.ErrorManageUtil;
import com.tmobile.pacbot.azure.inventory.collector.*;
import com.tmobile.pacman.commons.utils.CommonUtils;
import com.tmobile.pacman.commons.database.RDSDBManager;
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

import static com.tmobile.pacbot.azure.inventory.ErrorManageUtil.triggerNotificationforPermissionDenied;
import static com.tmobile.pacbot.azure.inventory.InventoryConstants.JOB_NAME;
import static com.tmobile.pacman.commons.PacmanSdkConstants.DATA_ALERT_ERROR_STRING;

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
	@Autowired
	VirtualMachineScaleSetCollector virtualMachineScaleSetCollector;

	@Autowired
	RDSDBManager rdsdbManager;

	private static final String TARGETTYPE_VIRTUAL_MACHINE = "virtualmachine";
	private static final String TARGETTYPE_VIRTUAL_MACHINE_SCALESET = "virtualmachinescaleset";
	private static final String TARGETTYPE_STORAGE_ACCOUNT = "storageaccount";
	private static final String TARGETTYPE_SQL_DATABASE = "sqldatabase";
	private static final String TARGETTYPE_NSG = "nsg";
	private static final String TARGETTYPE_DISK = "disk";
	private static final String TARGETTYPE_NETWORK_INTERFACE = "networkinterface";
	private static final String TARGETTYPE_VNET = "vnet";
	private static final String TARGETTYPE_LOADBALANCER = "loadbalancer";
	private static final String TARGETTYPE_SQLSERVER = "sqlserver";
	private static final String TARGETTYPE_SECURITY_CENTER = "securitycenter";
	private static final String TARGETTYPE_BLOB_CONTAINER = "blobcontainer";
	private static final String TARGETTYPE_RESOURCE_GROUP = "resourcegroup";
	private static final String TARGETTYPE_COSMOSDB = "cosmosdb";
	private static final String TARGETTYPE_MYSQLSERVER = "mysqlserver";
	private static final String TARGETTYPE_DATABRICKS = "databricks";
	private static final String TARGETTYPE_MARIADB = "mariadb";
	private static final String TARGETTYPE_POSTGRESQL = "postgresql";
	private static final String TARGETTYPE_SNAPSHOT = "snapshot";
	private static final String TARGETTYPE_PUBLICIP_ADDRESS = "publicipaddress";
	private static final String TARGETTYPE_ROUTE_TABLE = "routetable";
	private static final String TARGETTYPE_SECURITY_ALERTS = "securityalerts";
	private static final String TARGETTYPE_POLICY_EVALUATION_RESULTS = "policyevaluationresults";
	private static final String TARGETTYPE_SITES = "sites";
	private static final String TARGETTYPE_VAULTS = "vaults";
	private static final String TARGETTYPE_WORKFLOWS = "workflows";
	private static final String TARGETTYPE_BATCH_ACCOUNTS = "batchaccounts";
	private static final String TARGETTYPE_NAMESPACES = "namespaces";
	private static final String TARGETTYPE_SEARCH_SERVICES = "searchservices";
	private static final String TARGETTYPE_SUBNETS = "subnets";
	private static final String TARGETTYPE_REDIS_CACHE = "rediscache";
	private static final String TARGETTYPE_ACTIVITY_LOG = "activitylog";
	private static final String TARGETTYPE_SECURITY_PRICINGS = "securitypricings";
	private static final String TARGETTYPE_WEBAPP = "webapp";
	private static final String TARGETTYPE_SUBSCRIPTION = "subscription";
	private static final String TARGETTYPE_FUNCTION_APP = "functionapp";
	private static final String TARGETTYPE_MYSQL_FLEXIBLE = "mysqlflexible";
	private static final String TARGETTYPE_DIAGNOSTIC_SETTING = "diagnosticsetting";
	private static final String TARGETTYPE_BLOB_SERVICE = "blobservice";
	private static final String TARGETTYPE_KUBERNETES = "kubernetes";
	private static final String TARGETTYPE_POLICY_DEFINITIONS = "policydefinitions";
	private static final String TARGETTYPE_DEFENDER = "defender";
	public void generateFiles(List<SubscriptionVH> subscriptions, String filePath) {

		try {
			FileManager.initialise(filePath);
		} catch (IOException e1) {
			log.error(DATA_ALERT_ERROR_STRING + JOB_NAME+ " Failed to create file in S3 in the given path");
			System.exit(1);
		}

		List<String> connectedSubscriptions = new ArrayList<>();
		for (SubscriptionVH subscription : subscriptions) {
			log.info("Started Discovery for sub {}", subscription);
			try {
				String accessToken = azureCredentialProvider.getAuthToken(subscription.getTenant());
				Azure azure = azureCredentialProvider.authenticate(subscription.getTenant(),
						subscription.getSubscriptionId());
				azureCredentialProvider.putClient(subscription.getTenant(), subscription.getSubscriptionId(), azure);
				azureCredentialProvider.putToken(subscription.getTenant(), accessToken);
				rdsdbManager.executeUpdate("UPDATE cf_AzureTenantSubscription SET subscriptionStatus='configured' WHERE tenant=? AND subscription=?",Arrays.asList(subscription.getTenant(),subscription.getSubscriptionId()));
				log.info("updating account status of azure subscription- {} to online.",subscription.getSubscriptionId());
				connectedSubscriptions.add(subscription.getSubscriptionId());
			} catch (Exception e) {
				log.error("Error authenticating for {}", subscription, e);
				rdsdbManager.executeUpdate("UPDATE cf_AzureTenantSubscription SET subscriptionStatus='offline' WHERE tenant=? AND subscription=?",Arrays.asList(subscription.getTenant(),subscription.getSubscriptionId()));
				log.error("updating account status of azure subscription- {} to offline.",subscription.getSubscriptionId());
				continue;
			}
			subscription.setRegions(getRegionsFromAzure(subscription));
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

			List<String> longRunningTargetTypeList = new CopyOnWriteArrayList<>();

			executor.execute(() -> {
				if (!(isTypeInScope(TARGETTYPE_VIRTUAL_MACHINE))) {
					return;
				}
				try {
					longRunningTargetTypeList.add(TARGETTYPE_VIRTUAL_MACHINE);
					FileManager.generateVMFiles(vmInventoryCollector.fetchVMDetails(subscription, tagMap));
					longRunningTargetTypeList.remove(TARGETTYPE_VIRTUAL_MACHINE);
				} catch (Exception e) {
					e.printStackTrace();
					Util.eCount.getAndIncrement();
				}
			});

			executor.execute(() -> {
				if (!(isTypeInScope(TARGETTYPE_VIRTUAL_MACHINE_SCALESET))) {
					return;
				}
				try {
					longRunningTargetTypeList.add(TARGETTYPE_VIRTUAL_MACHINE_SCALESET);
					FileManager.generateVMSSFiles(virtualMachineScaleSetCollector.fetchVMScaleSetDetails(subscription, tagMap));
					longRunningTargetTypeList.remove(TARGETTYPE_VIRTUAL_MACHINE_SCALESET);
				} catch (Exception e) {
					e.printStackTrace();
					Util.eCount.getAndIncrement();
				}
			});

			executor.execute(() -> {
				if (!(isTypeInScope(TARGETTYPE_STORAGE_ACCOUNT))) {
					return;
				}
				try {
					longRunningTargetTypeList.add(TARGETTYPE_STORAGE_ACCOUNT);
					FileManager.generateStorageAccountFiles(
							storageAccountInventoryCollector.fetchStorageAccountDetails(subscription, tagMap));
					longRunningTargetTypeList.remove(TARGETTYPE_STORAGE_ACCOUNT);
				} catch (Exception e) {
					e.printStackTrace();
					Util.eCount.getAndIncrement();
				}
			});

			executor.execute(() -> {
				if (!(isTypeInScope(TARGETTYPE_SQL_DATABASE))) {
					return;
				}
				try {
					longRunningTargetTypeList.add(TARGETTYPE_SQL_DATABASE);
					FileManager.generateSQLdatabaseFiles(
							sqlDatabaseInventoryCollector.fetchSQLDatabaseDetails(subscription, tagMap));
					longRunningTargetTypeList.remove(TARGETTYPE_SQL_DATABASE);
				} catch (Exception e) {
					e.printStackTrace();
					Util.eCount.getAndIncrement();
				}
			});

			executor.execute(() -> {
				if (!(isTypeInScope(TARGETTYPE_NSG))) {
					return;
				}
				try {
					longRunningTargetTypeList.add(TARGETTYPE_NSG);
					FileManager.generateNetworkSecurityFiles(
							networkSecurityInventoryCollector.fetchNetworkSecurityGroupDetails(subscription, tagMap));
					longRunningTargetTypeList.remove(TARGETTYPE_NSG);
				} catch (Exception e) {
					e.printStackTrace();
					Util.eCount.getAndIncrement();
				}
			});

			executor.execute(() -> {
				if (!(isTypeInScope(TARGETTYPE_DISK))) {
					return;
				}
				try {
					longRunningTargetTypeList.add(TARGETTYPE_DISK);
					FileManager
							.generateDataDiskFiles(diskInventoryCollector.fetchDataDiskDetails(subscription, tagMap));
					longRunningTargetTypeList.remove(TARGETTYPE_DISK);
				} catch (Exception e) {
					e.printStackTrace();
					Util.eCount.getAndIncrement();
				}
			});

			executor.execute(() -> {
				if (!(isTypeInScope(TARGETTYPE_NETWORK_INTERFACE))) {
					return;
				}
				try {
					longRunningTargetTypeList.add(TARGETTYPE_NETWORK_INTERFACE);
					FileManager.generateNetworkInterfaceFiles(
							networkInterfaceInventoryCollector.fetchNetworkInterfaceDetails(subscription, tagMap));
					longRunningTargetTypeList.remove(TARGETTYPE_NETWORK_INTERFACE);
				} catch (Exception e) {
					e.printStackTrace();
					Util.eCount.getAndIncrement();
				}
			});

			executor.execute(() -> {
				if (!(isTypeInScope(TARGETTYPE_VNET))) {
					return;
				}
				try {
					longRunningTargetTypeList.add(TARGETTYPE_VNET);
					FileManager.generateNetworkFiles(networkInventoryCollector.fetchNetworkDetails(subscription, tagMap));
					longRunningTargetTypeList.remove(TARGETTYPE_VNET);
				} catch (Exception e) {
					e.printStackTrace();
					Util.eCount.getAndIncrement();
				}
			});

			executor.execute(() -> {
				if (!(isTypeInScope(TARGETTYPE_LOADBALANCER))) {
					return;
				}
				try {
					longRunningTargetTypeList.add(TARGETTYPE_LOADBALANCER);
					FileManager.generateLoadBalancerFiles(
							loadBalancerInventoryCollector.fetchLoadBalancerDetails(subscription, tagMap));
					longRunningTargetTypeList.remove(TARGETTYPE_LOADBALANCER);
				} catch (Exception e) {
					e.printStackTrace();
					Util.eCount.getAndIncrement();
				}
			});

			executor.execute(() -> {
				if (!(isTypeInScope(TARGETTYPE_SECURITY_CENTER))) {
					return;
				}
				try {
					longRunningTargetTypeList.add(TARGETTYPE_SECURITY_CENTER);
					FileManager.generateSecurityCenterFiles(
							scRecommendationsCollector.fetchSecurityCenterRecommendations(subscription));
					longRunningTargetTypeList.remove(TARGETTYPE_SECURITY_CENTER);
				} catch (Exception e) {
					e.printStackTrace();
					Util.eCount.getAndIncrement();
				}
			});

			executor.execute(() -> {
				if (!(isTypeInScope(TARGETTYPE_SQLSERVER))) {
					return;
				}
				try {
					longRunningTargetTypeList.add(TARGETTYPE_SQLSERVER);
					FileManager.generateSQLServerFiles(
							sqlServerInventoryCollector.fetchSQLServerDetails(subscription, tagMap));
					longRunningTargetTypeList.remove(TARGETTYPE_SQLSERVER);
				} catch (Exception e) {
					e.printStackTrace();
					Util.eCount.getAndIncrement();
				}
			});

			executor.execute(() -> {
				if (!(isTypeInScope(TARGETTYPE_BLOB_CONTAINER))) {
					return;
				}
				try {
					longRunningTargetTypeList.add(TARGETTYPE_BLOB_CONTAINER);
					FileManager.generateBlobContainerFiles(
							blobContainerInventoryCollector.fetchBlobContainerDetails(subscription, tagMap));
					longRunningTargetTypeList.remove(TARGETTYPE_BLOB_CONTAINER);
				} catch (Exception e) {
					e.printStackTrace();
					Util.eCount.getAndIncrement();
				}
			});

			executor.execute(() -> {
				if (!(isTypeInScope(TARGETTYPE_RESOURCE_GROUP))) {
					return;
				}
				try {
					longRunningTargetTypeList.add(TARGETTYPE_RESOURCE_GROUP);
					FileManager.generateResourceGroupFiles(
							resourceGroupInventoryCollector.fetchResourceGroupDetails(subscription));
					longRunningTargetTypeList.remove(TARGETTYPE_RESOURCE_GROUP);
				} catch (Exception e) {
					e.printStackTrace();
					Util.eCount.getAndIncrement();
				}
			});

			executor.execute(() -> {
				if (!(isTypeInScope(TARGETTYPE_COSMOSDB))) {
					return;
				}
				try {
					longRunningTargetTypeList.add(TARGETTYPE_COSMOSDB);
					FileManager.generateCosmosDBFiles(
							cosmosDBInventoryCollector.fetchCosmosDBDetails(subscription, tagMap));
					longRunningTargetTypeList.remove(TARGETTYPE_COSMOSDB);
				} catch (Exception e) {
					e.printStackTrace();
					Util.eCount.getAndIncrement();
				}
			});

			executor.execute(() -> {
				if (!(isTypeInScope(TARGETTYPE_MYSQLSERVER))) {
					return;
				}
				try {
					longRunningTargetTypeList.add(TARGETTYPE_MYSQLSERVER);
					FileManager.generateMySqlServerFiles(mySQLInventoryCollector.fetchMySQLServerDetails(subscription));
					longRunningTargetTypeList.remove(TARGETTYPE_MYSQLSERVER);
				} catch (Exception e) {
					e.printStackTrace();
					Util.eCount.getAndIncrement();
				}
			});

			executor.execute(() -> {
				if (!(isTypeInScope(TARGETTYPE_DATABRICKS))) {
					return;
				}
				try {
					longRunningTargetTypeList.add(TARGETTYPE_DATABRICKS);
					FileManager
							.generateDatabricksFiles(databricksInventoryCollector.fetchDatabricksDetails(subscription));
					longRunningTargetTypeList.remove(TARGETTYPE_DATABRICKS);
				} catch (Exception e) {
					e.printStackTrace();
					Util.eCount.getAndIncrement();
				}
			});

			executor.execute(() -> {
				if (!(isTypeInScope(TARGETTYPE_MARIADB))) {
					return;
				}
				try {
					longRunningTargetTypeList.add(TARGETTYPE_MARIADB);
					FileManager.generateMariaDBFiles(mariaDBInventoryCollector.fetchMariaDBDetails(subscription));
					longRunningTargetTypeList.remove(TARGETTYPE_MARIADB);
				} catch (Exception e) {
					e.printStackTrace();
					Util.eCount.getAndIncrement();
				}
			});

			executor.execute(() -> {
				if (!(isTypeInScope(TARGETTYPE_POSTGRESQL))) {
					return;
				}
				try {
					longRunningTargetTypeList.add(TARGETTYPE_POSTGRESQL);
					FileManager.generatePostgreSQLServerFiles(
							postgreSQLInventoryCollector.fetchPostgreSQLServerDetails(subscription));
					longRunningTargetTypeList.remove(TARGETTYPE_POSTGRESQL);
				} catch (Exception e) {
					e.printStackTrace();
					Util.eCount.getAndIncrement();
				}
			});

			executor.execute(() -> {
				if (!(isTypeInScope(TARGETTYPE_SNAPSHOT))) {
					return;
				}
				try {
					longRunningTargetTypeList.add(TARGETTYPE_SNAPSHOT);
					FileManager.generateSnapshotFiles(
							snapshotInventoryCollector.fetchSnapshotDetails(subscription, tagMap));
					longRunningTargetTypeList.remove(TARGETTYPE_SNAPSHOT);
				} catch (Exception e) {
					e.printStackTrace();
					Util.eCount.getAndIncrement();
				}
			});

			executor.execute(() -> {
				if (!(isTypeInScope(TARGETTYPE_PUBLICIP_ADDRESS))) {
					return;
				}
				try {
					longRunningTargetTypeList.add(TARGETTYPE_PUBLICIP_ADDRESS);
					FileManager.generatePublicIpAddressFiles(
							publicIpAddressInventoryCollector.fetchPublicIpAddressDetails(subscription, tagMap));
					longRunningTargetTypeList.remove(TARGETTYPE_PUBLICIP_ADDRESS);
				} catch (Exception e) {
					e.printStackTrace();
					Util.eCount.getAndIncrement();
				}
			});

			executor.execute(() -> {
				if (!(isTypeInScope(TARGETTYPE_ROUTE_TABLE))) {
					return;
				}
				try {
					longRunningTargetTypeList.add(TARGETTYPE_ROUTE_TABLE);
					FileManager.generateRouteTableFiles(
							routeTableInventoryCollector.fetchRouteTableDetails(subscription, tagMap));
					longRunningTargetTypeList.remove(TARGETTYPE_ROUTE_TABLE);
				} catch (Exception e) {
					e.printStackTrace();
					Util.eCount.getAndIncrement();
				}
			});

			executor.execute(() -> {
				if (!(isTypeInScope(TARGETTYPE_SECURITY_ALERTS))) {
					return;
				}
				try {
					longRunningTargetTypeList.add(TARGETTYPE_SECURITY_ALERTS);
					FileManager.generateSecurityAlertsFiles(
							securityAlertsInventoryCollector.fetchSecurityAlertsDetails(subscription));
					longRunningTargetTypeList.remove(TARGETTYPE_SECURITY_ALERTS);
				} catch (Exception e) {
					e.printStackTrace();
					Util.eCount.getAndIncrement();
				}
			});

			executor.execute(() -> {
				if (!(isTypeInScope(TARGETTYPE_POLICY_EVALUATION_RESULTS))) {
					return;
				}
				try {
					longRunningTargetTypeList.add(TARGETTYPE_POLICY_EVALUATION_RESULTS);
					FileManager.generatePolicyStatesFiles(policyStatesInventoryCollector
							.fetchPolicyStatesDetails(subscription, policyDefinitionList));
					longRunningTargetTypeList.remove(TARGETTYPE_POLICY_EVALUATION_RESULTS);
				} catch (Exception e) {
					e.printStackTrace();
					Util.eCount.getAndIncrement();
				}
			});

			executor.execute(() -> {
				if (!(isTypeInScope(TARGETTYPE_POLICY_DEFINITIONS))) {
					return;
				}
				try {
					longRunningTargetTypeList.add(TARGETTYPE_POLICY_DEFINITIONS);
					FileManager.generatePolicyDefinitionFiles(
							policyDefinitionInventoryCollector.fetchPolicyDefinitionDetails(subscription));
					longRunningTargetTypeList.remove(TARGETTYPE_POLICY_DEFINITIONS);
				} catch (Exception e) {
					e.printStackTrace();
					Util.eCount.getAndIncrement();
				}
			});

			executor.execute(() -> {
				if (!(isTypeInScope(TARGETTYPE_SITES))) {
					return;
				}
				try {
					longRunningTargetTypeList.add(TARGETTYPE_SITES);
					FileManager.generateSiteFiles(
							sitesInventoryCollector.fetchSitesDetails(subscription));
					longRunningTargetTypeList.remove(TARGETTYPE_SITES);
				} catch (Exception e) {
					e.printStackTrace();
					Util.eCount.getAndIncrement();
				}
			});

			executor.execute(() -> {
				if (!(isTypeInScope(TARGETTYPE_VAULTS))) {
					return;
				}
				try {
					longRunningTargetTypeList.add(TARGETTYPE_VAULTS);
					FileManager.generateVaultFiles(vaultInventoryCollector.fetchVaultDetails(subscription));
					longRunningTargetTypeList.remove(TARGETTYPE_VAULTS);
				} catch (Exception e) {
					e.printStackTrace();
					Util.eCount.getAndIncrement();
				}
			});

			executor.execute(() -> {
				if (!(isTypeInScope(TARGETTYPE_WORKFLOWS))) {
					return;
				}
				try {
					longRunningTargetTypeList.add(TARGETTYPE_WORKFLOWS);
					FileManager.generateWorkflowFiles(
							workflowInventoryCollector.fetchWorkflowDetails(subscription));
					longRunningTargetTypeList.remove(TARGETTYPE_WORKFLOWS);
				} catch (Exception e) {
					e.printStackTrace();
					Util.eCount.getAndIncrement();
				}
			});

			executor.execute(() -> {
				if (!(isTypeInScope(TARGETTYPE_BATCH_ACCOUNTS))) {
					return;
				}
				try {
					longRunningTargetTypeList.add(TARGETTYPE_BATCH_ACCOUNTS);
					FileManager.generateBatchAccountFiles(
							batchAccountInventoryCollector.fetchBatchAccountDetails(subscription));
					longRunningTargetTypeList.remove(TARGETTYPE_BATCH_ACCOUNTS);
				} catch (Exception e) {
					e.printStackTrace();
					Util.eCount.getAndIncrement();
				}
			});

			executor.execute(() -> {
				if (!(isTypeInScope(TARGETTYPE_NAMESPACES))) {
					return;
				}
				try {
					longRunningTargetTypeList.add(TARGETTYPE_NAMESPACES);
					FileManager.generateNamespaceFiles(
							namespaceInventoryCollector.fetchNamespaceDetails(subscription));
					longRunningTargetTypeList.remove(TARGETTYPE_NAMESPACES);
				} catch (Exception e) {
					e.printStackTrace();
					Util.eCount.getAndIncrement();
				}
			});

			executor.execute(() -> {
				if (!(isTypeInScope(TARGETTYPE_SEARCH_SERVICES))) {
					return;
				}
				try {
					longRunningTargetTypeList.add(TARGETTYPE_SEARCH_SERVICES);
					FileManager.generateSearchServiceFiles(
							searchServiceInventoryCollector.fetchSearchServiceDetails(subscription));
					longRunningTargetTypeList.remove(TARGETTYPE_SEARCH_SERVICES);
				} catch (Exception e) {
					e.printStackTrace();
					Util.eCount.getAndIncrement();
				}
			});

			executor.execute(() -> {
				if (!(isTypeInScope(TARGETTYPE_SUBNETS))) {
					return;
				}
				try {
					longRunningTargetTypeList.add(TARGETTYPE_SUBNETS);
					FileManager.generateSubnetFiles(
							subnetInventoryCollector.fetchSubnetDetails(subscription));
					longRunningTargetTypeList.remove(TARGETTYPE_SUBNETS);
				} catch (Exception e) {
					e.printStackTrace();
					Util.eCount.getAndIncrement();
				}
			});

			executor.execute(() -> {
				if (!(isTypeInScope(TARGETTYPE_REDIS_CACHE))) {
					return;
				}
				try {
					longRunningTargetTypeList.add(TARGETTYPE_REDIS_CACHE);
					FileManager.generateRedisCacheFiles(
							redisCacheInventoryCollector.fetchRedisCacheDetails(subscription));
					longRunningTargetTypeList.remove(TARGETTYPE_REDIS_CACHE);
				} catch (Exception e) {
					e.printStackTrace();
					Util.eCount.getAndIncrement();
				}
			});

			executor.execute(() -> {
				if (!(isTypeInScope(TARGETTYPE_ACTIVITY_LOG))) {
					return;
				}
				try {
					longRunningTargetTypeList.add(TARGETTYPE_ACTIVITY_LOG);
					FileManager.generateActivityLogFiles(
							activityLogsCollector.fetchActivityLogAlertDetails(subscription));
					longRunningTargetTypeList.remove(TARGETTYPE_ACTIVITY_LOG);
				} catch (Exception e) {
					e.printStackTrace();
					Util.eCount.getAndIncrement();
				}
			});

			executor.execute(() -> {
				if (!(isTypeInScope(TARGETTYPE_SECURITY_PRICINGS))) {
					return;
				}
				try {
					longRunningTargetTypeList.add(TARGETTYPE_SECURITY_PRICINGS);
					FileManager.generateSecurityPricingsFiles(
							securityPricingsInventoryCollector.fetchSecurityPricingsDetails(subscription));
					longRunningTargetTypeList.remove(TARGETTYPE_SECURITY_PRICINGS);
				} catch (Exception e) {
					e.printStackTrace();
					Util.eCount.getAndIncrement();
				}
			});

			executor.execute(() -> {
				if (!(isTypeInScope(TARGETTYPE_WEBAPP))) {
					return;
				}
				try {
					longRunningTargetTypeList.add(TARGETTYPE_WEBAPP);
					FileManager
							.generateWebAppFiles(webAppInventoryCollector.fetchWebAppDetails(subscription));
					longRunningTargetTypeList.remove(TARGETTYPE_WEBAPP);
				} catch (Exception e) {
					e.printStackTrace();
					Util.eCount.getAndIncrement();
				}
			});

			executor.execute(() -> {
				if (!(isTypeInScope(TARGETTYPE_SUBSCRIPTION))) {
					return;
				}
				try {
					longRunningTargetTypeList.add(TARGETTYPE_SUBSCRIPTION);
					FileManager
							.generateSubscriptionFiles(subscriptionInventoryCollector.fetchSubscriptions(subscription));
					longRunningTargetTypeList.remove(TARGETTYPE_SUBSCRIPTION);
				} catch (Exception e) {
					e.printStackTrace();
					Util.eCount.getAndIncrement();
				}
			});

			executor.execute(() -> {
				if (!(isTypeInScope(TARGETTYPE_FUNCTION_APP))) {
					return;
				}
				try {
					longRunningTargetTypeList.add(TARGETTYPE_FUNCTION_APP);
					FileManager
							.generateFunctionAppFiles(functionAppInventoryCollector.fetchFunctionAppDetails(subscription));
					longRunningTargetTypeList.remove(TARGETTYPE_FUNCTION_APP);
				} catch (Exception e) {
					e.printStackTrace();
					Util.eCount.getAndIncrement();
				}
			});

			executor.execute(() -> {
				if (!(isTypeInScope(TARGETTYPE_MYSQL_FLEXIBLE))) {
					return;
				}
				try {
					longRunningTargetTypeList.add(TARGETTYPE_MYSQL_FLEXIBLE);
					FileManager
							.generateMySQLFlexibleFiles(mySQLFlexibleInventoryCollector.fetchMySQLFlexibleServerDetails(subscription));
					longRunningTargetTypeList.remove(TARGETTYPE_MYSQL_FLEXIBLE);
				} catch (Exception e) {
					e.printStackTrace();
					Util.eCount.getAndIncrement();
				}
			});

			executor.execute(() -> {
				if (!(isTypeInScope(TARGETTYPE_DIAGNOSTIC_SETTING))) {
					return;
				}
				try {
					longRunningTargetTypeList.add(TARGETTYPE_DIAGNOSTIC_SETTING);
					FileManager
							.generateDiagnosticSettingFiles(diagnosticSettingsCollector.fetchDiagnosticSettings(subscription));
					longRunningTargetTypeList.remove(TARGETTYPE_DIAGNOSTIC_SETTING);
				} catch (Exception e) {
					e.printStackTrace();
					Util.eCount.getAndIncrement();
				}
			});

			executor.execute(() -> {
				if (!(isTypeInScope(TARGETTYPE_BLOB_SERVICE))) {
					log.info("no target type found for functionApp!!");
					return;
				}
				try {
					longRunningTargetTypeList.add(TARGETTYPE_BLOB_SERVICE);
					FileManager
							.generateBlobServiceFiles(blobServiceInventoryCollector.fetchBlobServiceDetails(subscription));
					longRunningTargetTypeList.remove(TARGETTYPE_BLOB_SERVICE);
				} catch (Exception e) {
					e.printStackTrace();
					Util.eCount.getAndIncrement();
				}
			});

			executor.execute(() -> {
				if (!(isTypeInScope(TARGETTYPE_DEFENDER))) {
					return;
				}
				try {
					longRunningTargetTypeList.add(TARGETTYPE_DEFENDER);
					FileManager.generateSecurityContactsInfoFile(securityContactsCollector.fetchSecurityContactsInfo(subscription));
					longRunningTargetTypeList.remove(TARGETTYPE_DEFENDER);
				} catch (Exception e) {
					e.printStackTrace();
					Util.eCount.getAndIncrement();
				}
			});

			executor.execute(() -> {
				if (!(isTypeInScope(TARGETTYPE_KUBERNETES))) {
					return;
				}
				try {
					longRunningTargetTypeList.add(TARGETTYPE_KUBERNETES);
					FileManager.generateKubernetesClusterDetailsInfoFile(kubernetesServicesCollector.fetchKubernetesClusterDetails(subscription));
					longRunningTargetTypeList.remove(TARGETTYPE_KUBERNETES);
				} catch (Exception e) {
					e.printStackTrace();
					Util.eCount.getAndIncrement();
				}
			});

			executor.shutdown();
			try {
				while (!executor.awaitTermination(1, TimeUnit.HOURS)) {
					log.error("Following target type collectors for subscription - {} have exceeded 1 hour - {}", subscription.getSubscriptionId(), longRunningTargetTypeList.stream().collect(Collectors.joining(" , ")));
				}
			} catch (InterruptedException e) {
				log.error("Interrupted exception occurred in azure collector", e);
			}

			while (!executor.isTerminated()) {
			}

			log.info("Finished Discovery for sub {}", subscription);
		}
		triggerNotificationforPermissionDenied();
		//Below logger message is used by datadog to create alert.
		if(Util.eCount.get()>0){
			log.error(DATA_ALERT_ERROR_STRING + JOB_NAME + " for at least one collector. Number of failures detected is " + Util.eCount.get());
		}
		if(connectedSubscriptions.isEmpty()){
			rdsdbManager.executeUpdate("UPDATE cf_AzureTenantSubscription SET subscriptionStatus='offline'", Collections.emptyList());
		}
		else{
			String combinedConnectedSubsStr = connectedSubscriptions.stream().map(sub -> "'"+sub+"'").collect(Collectors.joining(","));
			rdsdbManager.executeUpdate("UPDATE cf_AzureTenantSubscription SET subscriptionStatus='offline' WHERE subscription NOT IN ("+combinedConnectedSubsStr+")",Collections.emptyList());
		}
		try {
			FileManager.finalise();
		} catch (IOException e) {
			log.error(DATA_ALERT_ERROR_STRING + JOB_NAME+ " while adding closing bracket to data files.");
			System.exit(1);
		}
	}

	private Map<String, String> getRegionsFromAzure(SubscriptionVH subscription) {
		String accessToken = azureCredentialProvider.getToken(subscription.getTenant());
		String regionTemplate="https://management.azure.com/subscriptions/%s/locations?api-version=2020-01-01";
		String url=String.format(regionTemplate, URLEncoder.encode(subscription.getSubscriptionId()));
		Map<String,String> regionMap=new HashMap<>();
		try {
			String response= CommonUtils.doHttpGet(url, "Bearer", accessToken);
			JsonObject responseObj = new JsonParser().parse(response).getAsJsonObject();
			JsonArray regions=responseObj.getAsJsonArray("value");
			for(JsonElement region:regions)
			{
				JsonObject regionObj=region.getAsJsonObject();
				regionMap.put(regionObj.get("displayName").getAsString(),regionObj.get("name").getAsString());
			}
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
		return regionMap;
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
