/*******************************************************************************
 * Copyright 2018 T Mobile, Inc. or its affiliates. All Rights Reserved.
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License.  You may obtain a copy
 * of the License at
 * <p>
 *   http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  See the
 * License for the specific language governing permissions and limitations under
 * the License.
 ******************************************************************************/
package com.tmobile.pacbot.azure.inventory.file;

import com.tmobile.pacbot.azure.inventory.util.InventoryConstants;
import com.tmobile.pacbot.azure.inventory.util.TargetTypesConstants;
import com.tmobile.pacbot.azure.inventory.vo.AzureVH;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.util.List;

import static com.tmobile.pacbot.azure.inventory.util.Constants.ERROR_PREFIX;

/**
 * The Class FileManager.
 */
public class FileManager {

    private static final Logger log = LoggerFactory.getLogger(FileManager.class);

    /**
     * Instantiates a new file manager.
     */
    private FileManager() {

    }

    /**
     * Initialise.
     *
     * @param folderName the folder name
     */
    public static void initialise(String folderName) {
        FileGenerator.folderName = folderName;
        boolean isCreated = new File(folderName).mkdirs();
        if (!isCreated) {
            log.error(ERROR_PREFIX + "Failed to create file in S3 in path + {}", folderName);
            System.exit(1); // We want to exit if the S3 folder is not created
        }

        TargetTypesConstants.TARGET_TYPES_TO_COLLECT.forEach(type -> {
            try {
                FileGenerator.writeToFile(getFilenameFromTargetType(type), InventoryConstants.OPEN_ARRAY, false);
            } catch (IOException e) {
                e.printStackTrace();    // We want to continue if the file is not created
            }
        });
    }

    public static void finalise() {
        TargetTypesConstants.TARGET_TYPES_TO_COLLECT.forEach(type -> {
            try {
                FileGenerator.writeToFile(getFilenameFromTargetType(type), InventoryConstants.CLOSE_ARRAY, true);
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
    }

    public static void generateTargetTypeFile(List<? extends AzureVH> map, String targetType) {
        FileGenerator.generateJson(map, getFilenameFromTargetType(targetType));
    }

//    public static void generateVMFiles(List<VirtualMachineVH> vmMap) {
//        FileGenerator.generateJson(vmMap, getFilenameFromTargetType(TargetTypesConstants.VIRTUAL_MACHINE));
//    }
//
//    public static void generateVMSSFiles(List<VirtualMachineScaleSetVH> vmssMap) {
//        FileGenerator.generateJson(vmssMap, getFilenameFromTargetType(TargetTypesConstants.VIRTUAL_MACHINE_SCALESET));
//    }
//
//    public static void generateSubscriptionFiles(List<SubscriptionVH> subscriptionVHList) {
//        FileGenerator.generateJson(subscriptionVHList, getFilenameFromTargetType(TargetTypesConstants.SUBSCRIPTION));
//    }
//
//    public static void generateStorageAccountFiles(List<StorageAccountVH> storageAccountMap) {
//        FileGenerator.generateJson(storageAccountMap, getFilenameFromTargetType(TargetTypesConstants.STORAGE_ACCOUNT));
//    }
//
//    public static void generateSQLdatabaseFiles(List<SQLDatabaseVH> sqlDatabaseMap) {
//        FileGenerator.generateJson(sqlDatabaseMap, getFilenameFromTargetType(TargetTypesConstants.SQL_DATABASE));
//    }
//
//    public static void generateNetworkSecurityFiles(List<SecurityGroupVH> securityGroupMap) {
//        FileGenerator.generateJson(securityGroupMap, getFilenameFromTargetType(TargetTypesConstants.NSG));
//    }
//
//    public static void generateDataDiskFiles(List<DataDiskVH> dataDiskMap) {
//        FileGenerator.generateJson(dataDiskMap, getFilenameFromTargetType(TargetTypesConstants.DISK));
//    }
//
//    public static void generateNetworkInterfaceFiles(List<NetworkInterfaceVH> networkInterfaceMap) {
//        FileGenerator.generateJson(networkInterfaceMap, getFilenameFromTargetType(TargetTypesConstants.NETWORK_INTERFACE));
//    }
//
//    public static void generateNetworkFiles(List<NetworkVH> networkMap) {
//        FileGenerator.generateJson(networkMap, getFilenameFromTargetType(TargetTypesConstants.VNET));
//    }
//
//    public static void generateLoadBalancerFiles(List<LoadBalancerVH> loadBalancerMap) {
//        FileGenerator.generateJson(loadBalancerMap, getFilenameFromTargetType(TargetTypesConstants.LOADBALANCER));
//    }
//
//    public static void generateSecurityCenterFiles(List<RecommendationVH> recommendations) {
//        FileGenerator.generateJson(recommendations, getFilenameFromTargetType(TargetTypesConstants.SECURITY_CENTER));
//    }
//
//    public static void generateSQLServerFiles(List<SQLServerVH> sqlServerList) {
//        FileGenerator.generateJson(sqlServerList, getFilenameFromTargetType(TargetTypesConstants.SQLSERVER));
//    }
//
//    public static void generateBlobContainerFiles(List<BlobContainerVH> blobDetailsList) {
//        FileGenerator.generateJson(blobDetailsList, getFilenameFromTargetType(TargetTypesConstants.BLOB_CONTAINER));
//    }
//
//    public static void generateResourceGroupFiles(List<ResourceGroupVH> resourceGroupList) {
//        FileGenerator.generateJson(resourceGroupList, getFilenameFromTargetType(TargetTypesConstants.RESOURCE_GROUP));
//    }
//
//    public static void generateCosmosDBFiles(List<CosmosDBVH> cosmosDBList) {
//        FileGenerator.generateJson(cosmosDBList, getFilenameFromTargetType(TargetTypesConstants.COSMOSDB));
//    }
//
//    public static void generateRegisteredApplicationFiles(List<RegisteredApplicationVH> registeredApplicationVHList) {
//        FileGenerator.generateJson(registeredApplicationVHList, TargetTypesConstants.REGISTERED_APPLICATION);
//    }
//
//    public static void generateMySqlServerFiles(List<MySQLServerVH> mySqlServerList) {
//        FileGenerator.generateJson(mySqlServerList, getFilenameFromTargetType(TargetTypesConstants.MYSQLSERVER));
//    }
//
//    public static void generateDatabricksFiles(List<DatabricksVH> databricksList) {
//        FileGenerator.generateJson(databricksList, getFilenameFromTargetType(TargetTypesConstants.DATABRICKS));
//    }
//
//    public static void generateMariaDBFiles(List<MariaDBVH> mariaDBList) {
//        FileGenerator.generateJson(mariaDBList, getFilenameFromTargetType(TargetTypesConstants.MARIADB));
//    }
//
//    public static void generatePostgreSQLServerFiles(List<PostgreSQLServerVH> postgreSQLServerList) {
//        FileGenerator.generateJson(postgreSQLServerList, getFilenameFromTargetType(TargetTypesConstants.POSTGRESQL));
//    }
//
//    public static void generateSnapshotFiles(List<SnapshotVH> snapshotList) {
//        FileGenerator.generateJson(snapshotList, getFilenameFromTargetType(TargetTypesConstants.SNAPSHOT));
//    }
//
//    public static void generatePublicIpAddressFiles(List<PublicIpAddressVH> publicIpAddressList) {
//        FileGenerator.generateJson(publicIpAddressList, getFilenameFromTargetType(TargetTypesConstants.PUBLICIP_ADDRESS));
//    }
//
//    public static void generateRouteTableFiles(List<RouteTableVH> routeTableDetailsList) {
//        FileGenerator.generateJson(routeTableDetailsList, getFilenameFromTargetType(TargetTypesConstants.ROUTE_TABLE));
//    }
//
//    public static void generateSecurityAlertsFiles(List<SecurityAlertsVH> securityAlertsList) {
//        FileGenerator.generateJson(securityAlertsList, getFilenameFromTargetType(TargetTypesConstants.SECURITY_ALERTS));
//    }
//
//    public static void generatePolicyStatesFiles(List<PolicyStatesVH> policyStatesList) {
//        FileGenerator.generateJson(policyStatesList, getFilenameFromTargetType(TargetTypesConstants.POLICY_EVALUATION_RESULTS));
//    }
//
//    public static void generatePolicyDefinitionFiles(List<PolicyDefinitionVH> policyDefinitionList) {
//        FileGenerator.generateJson(policyDefinitionList, getFilenameFromTargetType(TargetTypesConstants.POLICY_DEFINITIONS));
//    }
//
//    public static void generateSiteFiles(List<SitesVH> sitesList) {
//        FileGenerator.generateJson(sitesList, getFilenameFromTargetType(TargetTypesConstants.SITES));
//    }
//
//    public static void generateVaultFiles(List<VaultVH> vaultList) {
//        FileGenerator.generateJson(vaultList, getFilenameFromTargetType(TargetTypesConstants.VAULTS));
//    }
//
//    public static void generateWorkflowFiles(List<WorkflowVH> workflowList) {
//        FileGenerator.generateJson(workflowList, getFilenameFromTargetType(TargetTypesConstants.WORKFLOWS));
//    }
//
//    public static void generateBatchAccountFiles(List<BatchAccountVH> batchAccountList) {
//        FileGenerator.generateJson(batchAccountList, getFilenameFromTargetType(TargetTypesConstants.BATCH_ACCOUNTS));
//    }
//
//    public static void generateNamespaceFiles(List<NamespaceVH> namespaceList) {
//        FileGenerator.generateJson(namespaceList, getFilenameFromTargetType(TargetTypesConstants.NAMESPACES));
//    }
//
//    public static void generateSearchServiceFiles(List<SearchServiceVH> searchServiceList) {
//        FileGenerator.generateJson(searchServiceList, getFilenameFromTargetType(TargetTypesConstants.SEARCH_SERVICES));
//    }
//
//    public static void generateSubnetFiles(List<SubnetVH> subnetList) {
//        FileGenerator.generateJson(subnetList, getFilenameFromTargetType(TargetTypesConstants.SUBNETS));
//    }
//
//    public static void generateRedisCacheFiles(List<RedisCacheVH> redisCacheList) {
//        FileGenerator.generateJson(redisCacheList, getFilenameFromTargetType(TargetTypesConstants.REDIS_CACHE));
//    }
//
//    public static void generateActivityLogFiles(List<ActivityLogVH> activityLogVHList) {
//        FileGenerator.generateJson(activityLogVHList, getFilenameFromTargetType(TargetTypesConstants.ACTIVITY_LOG_ALERT));
//    }
//
//    public static void generateSecurityPricingsFiles(List<SecurityPricingsVH> securityPricingsVH) {
//        FileGenerator.generateJson(securityPricingsVH, getFilenameFromTargetType(TargetTypesConstants.SECURITY_PRICINGS));
//    }
//
//    public static void generateWebAppFiles(List<WebAppVH> webAppVHList) {
//        FileGenerator.generateJson(webAppVHList, getFilenameFromTargetType(TargetTypesConstants.WEBAPP));
//    }
//
//    public static void generateFunctionAppFiles(List<FunctionAppVH> functionAppVHList) {
//        FileGenerator.generateJson(functionAppVHList, getFilenameFromTargetType(TargetTypesConstants.FUNCTION_APP));
//    }
//
//    public static void generateMySQLFlexibleFiles(List<MySQLFlexibleVH> mySQLFlexibleVHListVHList) {
//        FileGenerator.generateJson(mySQLFlexibleVHListVHList, getFilenameFromTargetType(TargetTypesConstants.MYSQL_FLEXIBLE));
//    }
//
//    public static void generateBlobServiceFiles(List<BlobServiceVH> blobServiceVHList) {
//        FileGenerator.generateJson(blobServiceVHList, getFilenameFromTargetType(TargetTypesConstants.BLOB_SERVICE));
//    }
//
//    public static void generateDiagnosticSettingFiles(List<DiagnosticSettingVH> fetchDiagnosticSettingsList) {
//        FileGenerator.generateJson(fetchDiagnosticSettingsList, getFilenameFromTargetType(TargetTypesConstants.DIAGNOSTIC_SETTING));
//    }
//
//    public static void generateSecurityContactsInfoFile(List<SecurityContactsVH> securityContactsVHList) {
//        FileGenerator.generateJson(securityContactsVHList, getFilenameFromTargetType(TargetTypesConstants.DEFENDER));
//    }
//
//    public static void generateKubernetesClusterDetailsInfoFile(List<KubernetesClustersVH> kubernetesClusterList) {
//        FileGenerator.generateJson(kubernetesClusterList, getFilenameFromTargetType(TargetTypesConstants.AKS));
//    }

    private static String getFilenameFromTargetType(String targetType) {
        return "azure-" + targetType + ".data";
    }
}
