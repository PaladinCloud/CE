/***************************************************************************************************
 * Copyright 2024 Paladin Cloud, Inc. or its affiliates. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 **************************************************************************************************/

package com.tmobile.pacbot.azure.inventory.file;

import com.tmobile.pacbot.azure.inventory.collector.*;
import com.tmobile.pacbot.azure.inventory.vo.AzureVH;
import com.tmobile.pacbot.azure.inventory.vo.SubscriptionVH;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static com.tmobile.pacbot.azure.inventory.util.TargetTypesConstants.*;

@Component
public class AssetDataFactory {
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

    public List<? extends AzureVH> getAssetData(SubscriptionVH subscription, Map<String, Map<String, String>> tagMap, String assetType) {
        switch (assetType) {
            case ACTIVITY_LOG_ALERT:
                return activityLogsCollector.collect(subscription);
            case AKS:
                return kubernetesServicesCollector.collect(subscription);
            case BATCH_ACCOUNTS:
                return batchAccountInventoryCollector.collect(subscription);
            case BLOB_CONTAINER:
                return blobContainerInventoryCollector.collect(subscription, tagMap);
            case BLOB_SERVICE:
                return blobServiceInventoryCollector.collect(subscription);
            case COSMOSDB:
                return cosmosDBInventoryCollector.collect(subscription, tagMap);
            case DATABRICKS:
                return databricksInventoryCollector.collect(subscription);
            case DEFENDER:
                return securityContactsCollector.collect(subscription);
            case DIAGNOSTIC_SETTING:
                return diagnosticSettingsCollector.collect(subscription);
            case DISK:
                return diskInventoryCollector.collect(subscription, tagMap);
            case FUNCTION_APP:
                return functionAppInventoryCollector.collect(subscription);
            case LOADBALANCER:
                return loadBalancerInventoryCollector.collect(subscription, tagMap);
            case MARIADB:
                return mariaDBInventoryCollector.collect(subscription);
            case MYSQL_FLEXIBLE:
                return mySQLFlexibleInventoryCollector.collect(subscription);
            case MYSQLSERVER:
                return mySQLInventoryCollector.collect(subscription);
            case NAMESPACES:
                return namespaceInventoryCollector.collect(subscription);
            case NETWORK_INTERFACE:
                return networkInterfaceInventoryCollector.collect(subscription, tagMap);
            case NSG:
                return networkSecurityInventoryCollector.collect(subscription, tagMap);
            case POLICY_DEFINITIONS:
                return policyDefinitionInventoryCollector.collect(subscription);
            case POLICY_EVALUATION_RESULTS:
                return policyStatesInventoryCollector.collect(subscription);
            case POSTGRESQL:
                return postgreSQLInventoryCollector.collect(subscription);
            case PUBLICIP_ADDRESS:
                return publicIpAddressInventoryCollector.collect(subscription, tagMap);
            case REDIS_CACHE:
                return redisCacheInventoryCollector.collect(subscription);
            case REGISTERED_APPLICATION:
                return registeredApplicationInventoryCollector.collect();
            case RESOURCE_GROUP:
                return resourceGroupInventoryCollector.collect(subscription);
            case ROUTE_TABLE:
                return routeTableInventoryCollector.collect(subscription, tagMap);
            case SEARCH_SERVICES:
                return searchServiceInventoryCollector.collect(subscription);
            case SECURITY_ALERTS:
                return securityAlertsInventoryCollector.collect(subscription);
            case SECURITY_CENTER:
                return scRecommendationsCollector.collect(subscription);
            case SECURITY_PRICINGS:
                return securityPricingsInventoryCollector.collect(subscription);
            case SITES:
                return sitesInventoryCollector.collect(subscription);
            case SNAPSHOT:
                return snapshotInventoryCollector.collect(subscription, tagMap);
            case SQL_DATABASE:
                return sqlDatabaseInventoryCollector.collect(subscription, tagMap);
            case SQLSERVER:
                return sqlServerInventoryCollector.collect(subscription, tagMap);
            case STORAGE_ACCOUNT:
                return storageAccountInventoryCollector.collect(subscription, tagMap);
            case SUBNETS:
                return subnetInventoryCollector.collect(subscription);
            case SUBSCRIPTION:
                return subscriptionInventoryCollector.collect(subscription);
            case VIRTUAL_MACHINE:
                return vmInventoryCollector.collect(subscription, tagMap);
            case VIRTUAL_MACHINE_SCALESET:
                return virtualMachineScaleSetCollector.collect(subscription, tagMap);
            case VAULTS:
                return vaultInventoryCollector.collect(subscription);
            case VNET:
                return networkInventoryCollector.collect(subscription, tagMap);
            case WEBAPP:
                return webAppInventoryCollector.collect(subscription);
            case WORKFLOWS:
                return workflowInventoryCollector.collect(subscription);
            default:
                return new ArrayList<>();
        }
    }
}
