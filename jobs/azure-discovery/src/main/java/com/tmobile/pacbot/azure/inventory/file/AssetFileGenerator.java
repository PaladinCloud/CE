package com.tmobile.pacbot.azure.inventory.file;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.microsoft.azure.management.Azure;
import com.tmobile.pacbot.azure.inventory.auth.AzureCredentialProvider;
import com.tmobile.pacbot.azure.inventory.collector.*;
import com.tmobile.pacbot.azure.inventory.vo.PolicyDefinitionVH;
import com.tmobile.pacbot.azure.inventory.vo.ResourceGroupVH;
import com.tmobile.pacbot.azure.inventory.vo.SubscriptionVH;
import com.tmobile.pacman.commons.database.RDSDBManager;
import com.tmobile.pacman.commons.utils.CommonUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.net.URLEncoder;
import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import static com.tmobile.pacbot.azure.inventory.util.ErrorManageUtil.triggerNotificationforPermissionDenied;
import static com.tmobile.pacbot.azure.inventory.util.Constants.ERROR_PREFIX;
import static com.tmobile.pacbot.azure.inventory.util.TargetTypesConstants.*;

@Component
public class AssetFileGenerator {

    private static final Logger log = LoggerFactory.getLogger(AssetFileGenerator.class);

    @Autowired
    AzureCredentialProvider azureCredentialProvider;
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
    @Value("${targetTypes:}")
    private String targetTypes;

    public void generateFiles(List<SubscriptionVH> subscriptions, String filePath) {

        try {
            FileManager.initialise(filePath);
        } catch (IOException e1) {
            log.error(ERROR_PREFIX + "Failed to create file in S3 in path + " + filePath, e1);
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
                rdsdbManager.executeUpdate("UPDATE cf_AzureTenantSubscription SET subscriptionStatus='configured' WHERE tenant=? AND subscription=?", Arrays.asList(subscription.getTenant(), subscription.getSubscriptionId()));
                log.info("updating account status of azure subscription- {} to online.", subscription.getSubscriptionId());
                connectedSubscriptions.add(subscription.getSubscriptionId());
            } catch (Exception e) {
                log.error("Error authenticating for {}", subscription, e);
                rdsdbManager.executeUpdate("UPDATE cf_AzureTenantSubscription SET subscriptionStatus='offline' WHERE tenant=? AND subscription=?", Arrays.asList(subscription.getTenant(), subscription.getSubscriptionId()));
                log.error("updating account status of azure subscription- {} to offline.", subscription.getSubscriptionId());
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
                if (!(isTypeInScope(VIRTUAL_MACHINE))) {
                    return;
                }
                try {
                    longRunningTargetTypeList.add(VIRTUAL_MACHINE);
                    FileManager.generateVMFiles(vmInventoryCollector.fetchVMDetails(subscription, tagMap));
                    longRunningTargetTypeList.remove(VIRTUAL_MACHINE);
                } catch (Exception e) {
                    e.printStackTrace();
                    Util.eCount.getAndIncrement();
                }
            });

            executor.execute(() -> {
                if (!(isTypeInScope(VIRTUAL_MACHINE_SCALESET))) {
                    return;
                }
                try {
                    longRunningTargetTypeList.add(VIRTUAL_MACHINE_SCALESET);
                    FileManager.generateVMSSFiles(virtualMachineScaleSetCollector.fetchVMScaleSetDetails(subscription, tagMap));
                    longRunningTargetTypeList.remove(VIRTUAL_MACHINE_SCALESET);
                } catch (Exception e) {
                    e.printStackTrace();
                    Util.eCount.getAndIncrement();
                }
            });

            executor.execute(() -> {
                if (!(isTypeInScope(STORAGE_ACCOUNT))) {
                    return;
                }
                try {
                    longRunningTargetTypeList.add(STORAGE_ACCOUNT);
                    FileManager.generateStorageAccountFiles(
                            storageAccountInventoryCollector.fetchStorageAccountDetails(subscription, tagMap));
                    longRunningTargetTypeList.remove(STORAGE_ACCOUNT);
                } catch (Exception e) {
                    e.printStackTrace();
                    Util.eCount.getAndIncrement();
                }
            });

            executor.execute(() -> {
                if (!(isTypeInScope(SQL_DATABASE))) {
                    return;
                }
                try {
                    longRunningTargetTypeList.add(SQL_DATABASE);
                    FileManager.generateSQLdatabaseFiles(
                            sqlDatabaseInventoryCollector.fetchSQLDatabaseDetails(subscription, tagMap));
                    longRunningTargetTypeList.remove(SQL_DATABASE);
                } catch (Exception e) {
                    e.printStackTrace();
                    Util.eCount.getAndIncrement();
                }
            });

            executor.execute(() -> {
                if (!(isTypeInScope(NSG))) {
                    return;
                }
                try {
                    longRunningTargetTypeList.add(NSG);
                    FileManager.generateNetworkSecurityFiles(
                            networkSecurityInventoryCollector.fetchNetworkSecurityGroupDetails(subscription, tagMap));
                    longRunningTargetTypeList.remove(NSG);
                } catch (Exception e) {
                    e.printStackTrace();
                    Util.eCount.getAndIncrement();
                }
            });

            executor.execute(() -> {
                if (!(isTypeInScope(DISK))) {
                    return;
                }
                try {
                    longRunningTargetTypeList.add(DISK);
                    FileManager
                            .generateDataDiskFiles(diskInventoryCollector.fetchDataDiskDetails(subscription, tagMap));
                    longRunningTargetTypeList.remove(DISK);
                } catch (Exception e) {
                    e.printStackTrace();
                    Util.eCount.getAndIncrement();
                }
            });

            executor.execute(() -> {
                if (!(isTypeInScope(NETWORK_INTERFACE))) {
                    return;
                }
                try {
                    longRunningTargetTypeList.add(NETWORK_INTERFACE);
                    FileManager.generateNetworkInterfaceFiles(
                            networkInterfaceInventoryCollector.fetchNetworkInterfaceDetails(subscription, tagMap));
                    longRunningTargetTypeList.remove(NETWORK_INTERFACE);
                } catch (Exception e) {
                    e.printStackTrace();
                    Util.eCount.getAndIncrement();
                }
            });

            executor.execute(() -> {
                if (!(isTypeInScope(VNET))) {
                    return;
                }
                try {
                    longRunningTargetTypeList.add(VNET);
                    FileManager.generateNetworkFiles(networkInventoryCollector.fetchNetworkDetails(subscription, tagMap));
                    longRunningTargetTypeList.remove(VNET);
                } catch (Exception e) {
                    e.printStackTrace();
                    Util.eCount.getAndIncrement();
                }
            });

            executor.execute(() -> {
                if (!(isTypeInScope(LOADBALANCER))) {
                    return;
                }
                try {
                    longRunningTargetTypeList.add(LOADBALANCER);
                    FileManager.generateLoadBalancerFiles(
                            loadBalancerInventoryCollector.fetchLoadBalancerDetails(subscription, tagMap));
                    longRunningTargetTypeList.remove(LOADBALANCER);
                } catch (Exception e) {
                    e.printStackTrace();
                    Util.eCount.getAndIncrement();
                }
            });

            executor.execute(() -> {
                if (!(isTypeInScope(SECURITY_CENTER))) {
                    return;
                }
                try {
                    longRunningTargetTypeList.add(SECURITY_CENTER);
                    FileManager.generateSecurityCenterFiles(
                            scRecommendationsCollector.fetchSecurityCenterRecommendations(subscription));
                    longRunningTargetTypeList.remove(SECURITY_CENTER);
                } catch (Exception e) {
                    e.printStackTrace();
                    Util.eCount.getAndIncrement();
                }
            });

            executor.execute(() -> {
                if (!(isTypeInScope(SQLSERVER))) {
                    return;
                }
                try {
                    longRunningTargetTypeList.add(SQLSERVER);
                    FileManager.generateSQLServerFiles(
                            sqlServerInventoryCollector.fetchSQLServerDetails(subscription, tagMap));
                    longRunningTargetTypeList.remove(SQLSERVER);
                } catch (Exception e) {
                    e.printStackTrace();
                    Util.eCount.getAndIncrement();
                }
            });

            executor.execute(() -> {
                if (!(isTypeInScope(BLOB_CONTAINER))) {
                    return;
                }
                try {
                    longRunningTargetTypeList.add(BLOB_CONTAINER);
                    FileManager.generateBlobContainerFiles(
                            blobContainerInventoryCollector.fetchBlobContainerDetails(subscription, tagMap));
                    longRunningTargetTypeList.remove(BLOB_CONTAINER);
                } catch (Exception e) {
                    e.printStackTrace();
                    Util.eCount.getAndIncrement();
                }
            });

            executor.execute(() -> {
                if (!(isTypeInScope(RESOURCE_GROUP))) {
                    return;
                }
                try {
                    longRunningTargetTypeList.add(RESOURCE_GROUP);
                    FileManager.generateResourceGroupFiles(
                            resourceGroupInventoryCollector.fetchResourceGroupDetails(subscription));
                    longRunningTargetTypeList.remove(RESOURCE_GROUP);
                } catch (Exception e) {
                    e.printStackTrace();
                    Util.eCount.getAndIncrement();
                }
            });

            executor.execute(() -> {
                if (!(isTypeInScope(COSMOSDB))) {
                    return;
                }
                try {
                    longRunningTargetTypeList.add(COSMOSDB);
                    FileManager.generateCosmosDBFiles(
                            cosmosDBInventoryCollector.fetchCosmosDBDetails(subscription, tagMap));
                    longRunningTargetTypeList.remove(COSMOSDB);
                } catch (Exception e) {
                    e.printStackTrace();
                    Util.eCount.getAndIncrement();
                }
            });

            executor.execute(() -> {
                if (!(isTypeInScope(MYSQLSERVER))) {
                    return;
                }
                try {
                    longRunningTargetTypeList.add(MYSQLSERVER);
                    FileManager.generateMySqlServerFiles(mySQLInventoryCollector.fetchMySQLServerDetails(subscription));
                    longRunningTargetTypeList.remove(MYSQLSERVER);
                } catch (Exception e) {
                    e.printStackTrace();
                    Util.eCount.getAndIncrement();
                }
            });

            executor.execute(() -> {
                if (!(isTypeInScope(DATABRICKS))) {
                    return;
                }
                try {
                    longRunningTargetTypeList.add(DATABRICKS);
                    FileManager
                            .generateDatabricksFiles(databricksInventoryCollector.fetchDatabricksDetails(subscription));
                    longRunningTargetTypeList.remove(DATABRICKS);
                } catch (Exception e) {
                    e.printStackTrace();
                    Util.eCount.getAndIncrement();
                }
            });

            executor.execute(() -> {
                if (!(isTypeInScope(MARIADB))) {
                    return;
                }
                try {
                    longRunningTargetTypeList.add(MARIADB);
                    FileManager.generateMariaDBFiles(mariaDBInventoryCollector.fetchMariaDBDetails(subscription));
                    longRunningTargetTypeList.remove(MARIADB);
                } catch (Exception e) {
                    e.printStackTrace();
                    Util.eCount.getAndIncrement();
                }
            });

            executor.execute(() -> {
                if (!(isTypeInScope(POSTGRESQL))) {
                    return;
                }
                try {
                    longRunningTargetTypeList.add(POSTGRESQL);
                    FileManager.generatePostgreSQLServerFiles(
                            postgreSQLInventoryCollector.fetchPostgreSQLServerDetails(subscription));
                    longRunningTargetTypeList.remove(POSTGRESQL);
                } catch (Exception e) {
                    e.printStackTrace();
                    Util.eCount.getAndIncrement();
                }
            });

            executor.execute(() -> {
                if (!(isTypeInScope(SNAPSHOT))) {
                    return;
                }
                try {
                    longRunningTargetTypeList.add(SNAPSHOT);
                    FileManager.generateSnapshotFiles(
                            snapshotInventoryCollector.fetchSnapshotDetails(subscription, tagMap));
                    longRunningTargetTypeList.remove(SNAPSHOT);
                } catch (Exception e) {
                    e.printStackTrace();
                    Util.eCount.getAndIncrement();
                }
            });

            executor.execute(() -> {
                if (!(isTypeInScope(PUBLICIP_ADDRESS))) {
                    return;
                }
                try {
                    longRunningTargetTypeList.add(PUBLICIP_ADDRESS);
                    FileManager.generatePublicIpAddressFiles(
                            publicIpAddressInventoryCollector.fetchPublicIpAddressDetails(subscription, tagMap));
                    longRunningTargetTypeList.remove(PUBLICIP_ADDRESS);
                } catch (Exception e) {
                    e.printStackTrace();
                    Util.eCount.getAndIncrement();
                }
            });

            executor.execute(() -> {
                if (!(isTypeInScope(ROUTE_TABLE))) {
                    return;
                }
                try {
                    longRunningTargetTypeList.add(ROUTE_TABLE);
                    FileManager.generateRouteTableFiles(
                            routeTableInventoryCollector.fetchRouteTableDetails(subscription, tagMap));
                    longRunningTargetTypeList.remove(ROUTE_TABLE);
                } catch (Exception e) {
                    e.printStackTrace();
                    Util.eCount.getAndIncrement();
                }
            });

            executor.execute(() -> {
                if (!(isTypeInScope(SECURITY_ALERTS))) {
                    return;
                }
                try {
                    longRunningTargetTypeList.add(SECURITY_ALERTS);
                    FileManager.generateSecurityAlertsFiles(
                            securityAlertsInventoryCollector.fetchSecurityAlertsDetails(subscription));
                    longRunningTargetTypeList.remove(SECURITY_ALERTS);
                } catch (Exception e) {
                    e.printStackTrace();
                    Util.eCount.getAndIncrement();
                }
            });

            executor.execute(() -> {
                if (!(isTypeInScope(POLICY_EVALUATION_RESULTS))) {
                    return;
                }
                try {
                    longRunningTargetTypeList.add(POLICY_EVALUATION_RESULTS);
                    FileManager.generatePolicyStatesFiles(policyStatesInventoryCollector
                            .fetchPolicyStatesDetails(subscription, policyDefinitionList));
                    longRunningTargetTypeList.remove(POLICY_EVALUATION_RESULTS);
                } catch (Exception e) {
                    e.printStackTrace();
                    Util.eCount.getAndIncrement();
                }
            });

            executor.execute(() -> {
                if (!(isTypeInScope(POLICY_DEFINITIONS))) {
                    return;
                }
                try {
                    longRunningTargetTypeList.add(POLICY_DEFINITIONS);
                    FileManager.generatePolicyDefinitionFiles(
                            policyDefinitionInventoryCollector.fetchPolicyDefinitionDetails(subscription));
                    longRunningTargetTypeList.remove(POLICY_DEFINITIONS);
                } catch (Exception e) {
                    e.printStackTrace();
                    Util.eCount.getAndIncrement();
                }
            });

            executor.execute(() -> {
                if (!(isTypeInScope(SITES))) {
                    return;
                }
                try {
                    longRunningTargetTypeList.add(SITES);
                    FileManager.generateSiteFiles(
                            sitesInventoryCollector.fetchSitesDetails(subscription));
                    longRunningTargetTypeList.remove(SITES);
                } catch (Exception e) {
                    e.printStackTrace();
                    Util.eCount.getAndIncrement();
                }
            });

            executor.execute(() -> {
                if (!(isTypeInScope(VAULTS))) {
                    return;
                }
                try {
                    longRunningTargetTypeList.add(VAULTS);
                    FileManager.generateVaultFiles(vaultInventoryCollector.fetchVaultDetails(subscription));
                    longRunningTargetTypeList.remove(VAULTS);
                } catch (Exception e) {
                    e.printStackTrace();
                    Util.eCount.getAndIncrement();
                }
            });

            executor.execute(() -> {
                if (!(isTypeInScope(WORKFLOWS))) {
                    return;
                }
                try {
                    longRunningTargetTypeList.add(WORKFLOWS);
                    FileManager.generateWorkflowFiles(
                            workflowInventoryCollector.fetchWorkflowDetails(subscription));
                    longRunningTargetTypeList.remove(WORKFLOWS);
                } catch (Exception e) {
                    e.printStackTrace();
                    Util.eCount.getAndIncrement();
                }
            });

            executor.execute(() -> {
                if (!(isTypeInScope(BATCH_ACCOUNTS))) {
                    return;
                }
                try {
                    longRunningTargetTypeList.add(BATCH_ACCOUNTS);
                    FileManager.generateBatchAccountFiles(
                            batchAccountInventoryCollector.fetchBatchAccountDetails(subscription));
                    longRunningTargetTypeList.remove(BATCH_ACCOUNTS);
                } catch (Exception e) {
                    e.printStackTrace();
                    Util.eCount.getAndIncrement();
                }
            });

            executor.execute(() -> {
                if (!(isTypeInScope(NAMESPACES))) {
                    return;
                }
                try {
                    longRunningTargetTypeList.add(NAMESPACES);
                    FileManager.generateNamespaceFiles(
                            namespaceInventoryCollector.fetchNamespaceDetails(subscription));
                    longRunningTargetTypeList.remove(NAMESPACES);
                } catch (Exception e) {
                    e.printStackTrace();
                    Util.eCount.getAndIncrement();
                }
            });

            executor.execute(() -> {
                if (!(isTypeInScope(SEARCH_SERVICES))) {
                    return;
                }
                try {
                    longRunningTargetTypeList.add(SEARCH_SERVICES);
                    FileManager.generateSearchServiceFiles(
                            searchServiceInventoryCollector.fetchSearchServiceDetails(subscription));
                    longRunningTargetTypeList.remove(SEARCH_SERVICES);
                } catch (Exception e) {
                    e.printStackTrace();
                    Util.eCount.getAndIncrement();
                }
            });

            executor.execute(() -> {
                if (!(isTypeInScope(SUBNETS))) {
                    return;
                }
                try {
                    longRunningTargetTypeList.add(SUBNETS);
                    FileManager.generateSubnetFiles(
                            subnetInventoryCollector.fetchSubnetDetails(subscription));
                    longRunningTargetTypeList.remove(SUBNETS);
                } catch (Exception e) {
                    e.printStackTrace();
                    Util.eCount.getAndIncrement();
                }
            });

            executor.execute(() -> {
                if (!(isTypeInScope(REDIS_CACHE))) {
                    return;
                }
                try {
                    longRunningTargetTypeList.add(REDIS_CACHE);
                    FileManager.generateRedisCacheFiles(
                            redisCacheInventoryCollector.fetchRedisCacheDetails(subscription));
                    longRunningTargetTypeList.remove(REDIS_CACHE);
                } catch (Exception e) {
                    e.printStackTrace();
                    Util.eCount.getAndIncrement();
                }
            });

            executor.execute(() -> {
                if (!(isTypeInScope(ACTIVITY_LOG))) {
                    return;
                }
                try {
                    longRunningTargetTypeList.add(ACTIVITY_LOG);
                    FileManager.generateActivityLogFiles(
                            activityLogsCollector.fetchActivityLogAlertDetails(subscription));
                    longRunningTargetTypeList.remove(ACTIVITY_LOG);
                } catch (Exception e) {
                    e.printStackTrace();
                    Util.eCount.getAndIncrement();
                }
            });

            executor.execute(() -> {
                if (!(isTypeInScope(SECURITY_PRICINGS))) {
                    return;
                }
                try {
                    longRunningTargetTypeList.add(SECURITY_PRICINGS);
                    FileManager.generateSecurityPricingsFiles(
                            securityPricingsInventoryCollector.fetchSecurityPricingsDetails(subscription));
                    longRunningTargetTypeList.remove(SECURITY_PRICINGS);
                } catch (Exception e) {
                    e.printStackTrace();
                    Util.eCount.getAndIncrement();
                }
            });

            executor.execute(() -> {
                if (!(isTypeInScope(WEBAPP))) {
                    return;
                }
                try {
                    longRunningTargetTypeList.add(WEBAPP);
                    FileManager
                            .generateWebAppFiles(webAppInventoryCollector.fetchWebAppDetails(subscription));
                    longRunningTargetTypeList.remove(WEBAPP);
                } catch (Exception e) {
                    e.printStackTrace();
                    Util.eCount.getAndIncrement();
                }
            });

            executor.execute(() -> {
                if (!(isTypeInScope(SUBSCRIPTION))) {
                    return;
                }
                try {
                    longRunningTargetTypeList.add(SUBSCRIPTION);
                    FileManager
                            .generateSubscriptionFiles(subscriptionInventoryCollector.fetchSubscriptions(subscription));
                    longRunningTargetTypeList.remove(SUBSCRIPTION);
                } catch (Exception e) {
                    e.printStackTrace();
                    Util.eCount.getAndIncrement();
                }
            });

            executor.execute(() -> {
                if (!(isTypeInScope(FUNCTION_APP))) {
                    return;
                }
                try {
                    longRunningTargetTypeList.add(FUNCTION_APP);
                    FileManager
                            .generateFunctionAppFiles(functionAppInventoryCollector.fetchFunctionAppDetails(subscription));
                    longRunningTargetTypeList.remove(FUNCTION_APP);
                } catch (Exception e) {
                    e.printStackTrace();
                    Util.eCount.getAndIncrement();
                }
            });

            executor.execute(() -> {
                if (!(isTypeInScope(MYSQL_FLEXIBLE))) {
                    return;
                }
                try {
                    longRunningTargetTypeList.add(MYSQL_FLEXIBLE);
                    FileManager
                            .generateMySQLFlexibleFiles(mySQLFlexibleInventoryCollector.fetchMySQLFlexibleServerDetails(subscription));
                    longRunningTargetTypeList.remove(MYSQL_FLEXIBLE);
                } catch (Exception e) {
                    e.printStackTrace();
                    Util.eCount.getAndIncrement();
                }
            });

            executor.execute(() -> {
                if (!(isTypeInScope(DIAGNOSTIC_SETTING))) {
                    return;
                }
                try {
                    longRunningTargetTypeList.add(DIAGNOSTIC_SETTING);
                    FileManager
                            .generateDiagnosticSettingFiles(diagnosticSettingsCollector.fetchDiagnosticSettings(subscription));
                    longRunningTargetTypeList.remove(DIAGNOSTIC_SETTING);
                } catch (Exception e) {
                    e.printStackTrace();
                    Util.eCount.getAndIncrement();
                }
            });

            executor.execute(() -> {
                if (!(isTypeInScope(BLOB_SERVICE))) {
                    log.info("no target type found for functionApp!!");
                    return;
                }
                try {
                    longRunningTargetTypeList.add(BLOB_SERVICE);
                    FileManager
                            .generateBlobServiceFiles(blobServiceInventoryCollector.fetchBlobServiceDetails(subscription));
                    longRunningTargetTypeList.remove(BLOB_SERVICE);
                } catch (Exception e) {
                    e.printStackTrace();
                    Util.eCount.getAndIncrement();
                }
            });

            executor.execute(() -> {
                if (!(isTypeInScope(DEFENDER))) {
                    return;
                }
                try {
                    longRunningTargetTypeList.add(DEFENDER);
                    FileManager.generateSecurityContactsInfoFile(securityContactsCollector.fetchSecurityContactsInfo(subscription));
                    longRunningTargetTypeList.remove(DEFENDER);
                } catch (Exception e) {
                    e.printStackTrace();
                    Util.eCount.getAndIncrement();
                }
            });

            executor.execute(() -> {
                if (!(isTypeInScope(KUBERNETES))) {
                    return;
                }
                try {
                    longRunningTargetTypeList.add(KUBERNETES);
                    FileManager.generateKubernetesClusterDetailsInfoFile(kubernetesServicesCollector.fetchKubernetesClusterDetails(subscription));
                    longRunningTargetTypeList.remove(KUBERNETES);
                } catch (Exception e) {
                    e.printStackTrace();
                    Util.eCount.getAndIncrement();
                }
            });

            executor.shutdown();
            try {
                while (!executor.awaitTermination(1, TimeUnit.HOURS)) {
                    log.error("Following target type collectors for subscription - {} have exceeded 1 hour - {}", subscription.getSubscriptionId(), String.join(" , ", longRunningTargetTypeList));
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
        if (Util.eCount.get() > 0) {
            log.error(ERROR_PREFIX + "for at least one collector. Number of failures detected is " + Util.eCount.get(), new Exception("Error in at least one collector"));
        }
        if (connectedSubscriptions.isEmpty()) {
            rdsdbManager.executeUpdate("UPDATE cf_AzureTenantSubscription SET subscriptionStatus='offline'", Collections.emptyList());
        } else {
            String combinedConnectedSubsStr = connectedSubscriptions.stream().map(sub -> "'" + sub + "'").collect(Collectors.joining(","));
            rdsdbManager.executeUpdate("UPDATE cf_AzureTenantSubscription SET subscriptionStatus='offline' WHERE subscription NOT IN (" + combinedConnectedSubsStr + ")", Collections.emptyList());
        }
        try {
            FileManager.finalise();
        } catch (IOException e) {
            log.error(ERROR_PREFIX + "while adding closing bracket to data files", e);
            System.exit(1);
        }
    }

    private Map<String, String> getRegionsFromAzure(SubscriptionVH subscription) {
        String accessToken = azureCredentialProvider.getToken(subscription.getTenant());
        String regionTemplate = "https://management.azure.com/subscriptions/%s/locations?api-version=2020-01-01";
        String url = String.format(regionTemplate, URLEncoder.encode(subscription.getSubscriptionId()));
        Map<String, String> regionMap = new HashMap<>();
        try {
            String response = CommonUtils.doHttpGet(url, "Bearer", accessToken);
            JsonObject responseObj = new JsonParser().parse(response).getAsJsonObject();
            JsonArray regions = responseObj.getAsJsonArray("value");
            for (JsonElement region : regions) {
                JsonObject regionObj = region.getAsJsonObject();
                regionMap.put(regionObj.get("displayName").getAsString(), regionObj.get("name").getAsString());
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
