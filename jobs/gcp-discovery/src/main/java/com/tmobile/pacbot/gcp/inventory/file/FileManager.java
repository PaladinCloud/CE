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
}
