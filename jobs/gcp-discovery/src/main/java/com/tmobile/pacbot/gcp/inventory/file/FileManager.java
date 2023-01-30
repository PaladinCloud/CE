/*******************************************************************************
 * Copyright 2018 T Mobile, Inc. or its affiliates. All Rights Reserved.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License.  You may obtain a copy
 * of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  See the
 * License for the specific language governing permissions and limitations under
 * the License.
 ******************************************************************************/
package com.tmobile.pacbot.gcp.inventory.file;

import java.io.File;
import java.io.IOException;
import java.util.List;

import com.tmobile.pacbot.gcp.inventory.InventoryConstants;
import com.tmobile.pacbot.gcp.inventory.constants.DataFileNamesConstants;
import com.tmobile.pacbot.gcp.inventory.vo.*;

/**
 * The Class FileManager.
 */
public class FileManager {

    /**
     * Instantiates a new file manager.
     */
    private FileManager() {

    }

    /**
     * Initialise.
     *
     * @param folderName
     *                   the folder name
     * @throws IOException
     *                     Signals that an I/O exception has occurred.
     */
    public static void initialise(String folderName) throws IOException {
        FileGenerator.folderName = folderName;
        new File(folderName).mkdirs();

        FileGenerator.writeToFile("gcp-vminstance.data", "[", false);
        FileGenerator.writeToFile("gcp-vpcfirewall.data", "[", false);
        FileGenerator.writeToFile("gcp-cloudstorage.data", "[", false);
        FileGenerator.writeToFile("gcp-bigquerydataset.data", "[", false);
        FileGenerator.writeToFile("gcp-bigquerytable.data", "[", false);
        FileGenerator.writeToFile(InventoryConstants.GCP_CLOUD_SQL_FILE, "[", false);
        FileGenerator.writeToFile(InventoryConstants.GCP_KMS_KEY_FILE, "[", false);
        FileGenerator.writeToFile("gcp-dataproc.data", "[", false);
        FileGenerator.writeToFile("gcp-pubsub.data", "[", false);
        FileGenerator.writeToFile("gcp-gkecluster.data", "[", false);
        FileGenerator.writeToFile("gcp-clouddns.data", "[", false);
        FileGenerator.writeToFile("gcp-cloudsql_sqlserver.data", "[", false);
        FileGenerator.writeToFile("gcp-cloudsql_mysqlserver.data", "[", false);
        FileGenerator.writeToFile("gcp-networks.data", "[", false);
        FileGenerator.writeToFile("gcp-project.data", "[", false);
        FileGenerator.writeToFile("gcp-serviceaccounts.data", "[", false);
        FileGenerator.writeToFile("gcp-cloudsql_postgres.data", "[", false);
        FileGenerator.writeToFile("gcp-iamusers.data", "[", false);
        FileGenerator.writeToFile("gcp-gcploadbalancer.data", "[", false);
        FileGenerator.writeToFile("gcp-apikeys.data", "[", false);
        FileGenerator.writeToFile(DataFileNamesConstants.CLOUD_FUNCTION, "[", false);
        FileGenerator.writeToFile(DataFileNamesConstants.CLOUD_FUNCTION_GEN1, "[", false);
    }

    public static void finalise() throws IOException {

        FileGenerator.writeToFile("gcp-vminstance.data", "]", true);
        FileGenerator.writeToFile("gcp-vpcfirewall.data", "]", true);
        FileGenerator.writeToFile("gcp-bigquerydataset.data", "]", true);
        FileGenerator.writeToFile("gcp-bigquerytable.data", "]", true);
        FileGenerator.writeToFile("gcp-cloudstorage.data", "]", true);
        FileGenerator.writeToFile(InventoryConstants.GCP_CLOUD_SQL_FILE, "]", true);
        FileGenerator.writeToFile(InventoryConstants.GCP_KMS_KEY_FILE, "]", true);
        FileGenerator.writeToFile("gcp-pubsub.data", "]", true);
        FileGenerator.writeToFile("gcp-dataproc.data", "]", true);
        FileGenerator.writeToFile("gcp-gkecluster.data", "]", true);
        FileGenerator.writeToFile("gcp-clouddns.data", "]", true);
        FileGenerator.writeToFile("gcp-cloudsql_sqlserver.data", "]", true);
        FileGenerator.writeToFile("gcp-cloudsql_mysqlserver.data", "]", true);
        FileGenerator.writeToFile("gcp-networks.data", "]", true);
        FileGenerator.writeToFile("gcp-project.data", "]", true);
        FileGenerator.writeToFile("gcp-serviceaccounts.data", "]", true);
        FileGenerator.writeToFile("gcp-cloudsql_postgres.data", "]", true);
        FileGenerator.writeToFile("gcp-iamusers.data", "]", true);
        FileGenerator.writeToFile("gcp-gcploadbalancer.data", "]", true);
        FileGenerator.writeToFile("gcp-apikeys.data", "]", true);
        FileGenerator.writeToFile(DataFileNamesConstants.CLOUD_FUNCTION, "]", true);
        FileGenerator.writeToFile(DataFileNamesConstants.CLOUD_FUNCTION_GEN1, "]", true);

    }

    public static void generateVMFiles(List<VirtualMachineVH> vmMap) throws IOException {

        FileGenerator.generateJson(vmMap, "gcp-vminstance.data");

    }

    public static void generateFireWallFiles(List<FireWallVH> vmMap) throws IOException {

        FileGenerator.generateJson(vmMap, "gcp-vpcfirewall.data");

    }

    public static void generateBigqueryFiles(List<BigQueryVH> dataMap) throws IOException {
        FileGenerator.generateJson(dataMap, "gcp-bigquerydataset.data");

    }

    public static void generateBigqueryTableFiles(List<BigQueryTableVH> dataMap) throws IOException {
        FileGenerator.generateJson(dataMap, "gcp-bigquerytable.data");

    }

    public static void generateStorageFiles(List<StorageVH> storageList) {
        FileGenerator.generateJson(storageList, "gcp-cloudstorage.data");
    }

    public static void generateCloudSqlFiles(List<CloudSqlVH> cloudsqlList) {
        FileGenerator.generateJson(cloudsqlList, InventoryConstants.GCP_CLOUD_SQL_FILE);
    }

    public static void generatePubSubFiles(List<TopicVH> pubsubList) {
        FileGenerator.generateJson(pubsubList, "gcp-pubsub.data");
    }

    public static void generateKmsKeyFiles(List<KMSKeyVH> keyList) {
        FileGenerator.generateJson(keyList, InventoryConstants.GCP_KMS_KEY_FILE);
    }

    public static void generateDataProcFiles(List<ClusterVH> clustList) {
        FileGenerator.generateJson(clustList, "gcp-dataproc.data");
    }

    public static void generateGKEClusterFiles(List<GKEClusterVH> clustList) {
        FileGenerator.generateJson(clustList, "gcp-gkecluster.data");
    }
    public static void generateCloudDnsFiles(List<CloudDNSVH> dnsList) {
        FileGenerator.generateJson(dnsList, "gcp-clouddns.data");
    }
    public static void generateCloudSqlServerFiles(List<CloudSqlVH> cloudSqlVHList) {
        FileGenerator.generateJson(cloudSqlVHList, "gcp-cloudsql_sqlserver.data");
    }
    public static void generateNetworksFiles(List<NetworkVH> networkVHS) {
        FileGenerator.generateJson(networkVHS, "gcp-networks.data");
    }
    public static void generateProjectFiles(List<ProjectVH> projectMetadataVHList){
        FileGenerator.generateJson(projectMetadataVHList, "gcp-project.data");

    }
    public static void generateServiceAccountFiles(List<ServiceAccountVH> serviceAccountVHList){
        FileGenerator.generateJson(serviceAccountVHList, "gcp-serviceaccounts.data");
    }

    public static void generateCloudMySqlServerFiles(List<CloudSqlVH> cloudSqlVHList) {
        FileGenerator.generateJson(cloudSqlVHList, "gcp-cloudsql_mysqlserver.data");
    }

    public static void generateCloudPostgresFiles(List<CloudSqlVH> cloudSqlVHList) {
        FileGenerator.generateJson(cloudSqlVHList, "gcp-cloudsql_postgres.data");
    }

    public static void generateIamUsers(List<IAMUserVH> cloudSqlVHList) {
        FileGenerator.generateJson(cloudSqlVHList, "gcp-iamusers.data");
    }
    public static void generateLoadBalancerFiles(List<LoadBalancerVH> fetchLoadBalancerInventory) {
        FileGenerator.generateJson(fetchLoadBalancerInventory, "gcp-gcploadbalancer.data");
    }
    public static void generateApiKeysFiles(List<APIKeysVH>apiKeysVHList){
        FileGenerator.generateJson(apiKeysVHList, "gcp-apikeys.data");
    }
    public static void generateCloudFunctionFile(List<CloudFunctionVH> fetchCloudFunctionInventory) {
        FileGenerator.generateJson(fetchCloudFunctionInventory, DataFileNamesConstants.CLOUD_FUNCTION);
    }

    public static void generateCloudFunctionGen1File(List<CloudFunctionVH> fetchCloudFunctionInventory) {
        FileGenerator.generateJson(fetchCloudFunctionInventory, DataFileNamesConstants.CLOUD_FUNCTION_GEN1);
    }
}
