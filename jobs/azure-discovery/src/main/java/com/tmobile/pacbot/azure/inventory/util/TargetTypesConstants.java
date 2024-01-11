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

package com.tmobile.pacbot.azure.inventory.util;

import java.util.Arrays;
import java.util.List;

public class TargetTypesConstants {

    public static final String ACTIVITY_LOG_ALERT = "activitylogalert";
    public static final String AKS = "aks";
    public static final String BATCH_ACCOUNTS = "batchaccounts";
    public static final String BLOB_CONTAINER = "blobcontainer";
    public static final String BLOB_SERVICE = "blobservice";
    public static final String COSMOSDB = "cosmosdb";
    public static final String DATABRICKS = "databricks";
    public static final String DEFENDER = "defender";
    public static final String DIAGNOSTIC_SETTING = "diagnosticsetting";
    public static final String DISK = "disk";
    public static final String FUNCTION_APP = "functionapp";
    public static final String LOADBALANCER = "loadbalancer";
    public static final String MARIADB = "mariadb";
    public static final String MYSQL_FLEXIBLE = "mysqlflexible";
    public static final String MYSQLSERVER = "mysqlserver";
    public static final String NAMESPACES = "namespaces";
    public static final String NETWORK_INTERFACE = "networkinterface";
    public static final String NSG = "nsg";
    public static final String POLICY_DEFINITIONS = "policydefinitions";
    public static final String POLICY_EVALUATION_RESULTS = "policyevaluationresults";
    public static final String POSTGRESQL = "postgresql";
    public static final String PUBLICIP_ADDRESS = "publicipaddress";
    public static final String REDIS_CACHE = "rediscache";
    public static final String REGISTERED_APPLICATION = "registeredapplication";
    public static final String RESOURCE_GROUP = "resourcegroup";
    public static final String ROUTE_TABLE = "routetable";
    public static final String SEARCH_SERVICES = "searchservices";
    public static final String SECURITY_ALERTS = "securityalerts";
    public static final String SECURITY_CENTER = "securitycenter";
    public static final String SECURITY_PRICINGS = "securitypricings";
    public static final String SITES = "sites";
    public static final String SNAPSHOT = "snapshot";
    public static final String SQL_DATABASE = "sqldatabase";
    public static final String SQLSERVER = "sqlserver";
    public static final String STORAGE_ACCOUNT = "storageaccount";
    public static final String SUBNETS = "subnets";
    public static final String SUBSCRIPTION = "subscription";
    public static final String VIRTUAL_MACHINE = "virtualmachine";
    public static final String VIRTUAL_MACHINE_SCALESET = "virtualmachinescaleset";
    public static final String VAULTS = "vaults";
    public static final String VNET = "vnet";
    public static final String WEBAPP = "webapp";
    public static final String WORKFLOWS = "workflows";


    public static final List<String> TARGET_TYPES = Arrays.asList(
            ACTIVITY_LOG_ALERT,
            AKS,
            BATCH_ACCOUNTS,
            BLOB_CONTAINER,
            BLOB_SERVICE,
            COSMOSDB,
            DATABRICKS,
            DEFENDER,
            DIAGNOSTIC_SETTING,
            DISK,
            FUNCTION_APP,
            LOADBALANCER,
            MARIADB,
            MYSQL_FLEXIBLE,
            MYSQLSERVER,
            NAMESPACES,
            NETWORK_INTERFACE,
            NSG,
            POLICY_DEFINITIONS,
            POLICY_EVALUATION_RESULTS,
            POSTGRESQL,
            PUBLICIP_ADDRESS,
            REDIS_CACHE,
            REGISTERED_APPLICATION,
            RESOURCE_GROUP,
            ROUTE_TABLE,
            SEARCH_SERVICES,
            SECURITY_ALERTS,
            SECURITY_CENTER,
            SECURITY_PRICINGS,
            SITES,
            SNAPSHOT,
            SQL_DATABASE,
            SQLSERVER,
            STORAGE_ACCOUNT,
            SUBNETS,
            SUBSCRIPTION,
            VIRTUAL_MACHINE,
            VIRTUAL_MACHINE_SCALESET,
            VAULTS,
            VNET,
            WEBAPP,
            WORKFLOWS
    );

    private TargetTypesConstants() {
    }
}
