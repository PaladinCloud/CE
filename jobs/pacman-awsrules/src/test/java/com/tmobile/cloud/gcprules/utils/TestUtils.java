package com.tmobile.cloud.gcprules.utils;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

public class TestUtils {

    public static JsonArray getHitsJsonArrayForVMPublicAccess() {
        Gson gson = new Gson();
        JsonObject jsonObject = new JsonObject();
        jsonObject.add("_source", gson.fromJson("{\n" +
                "          \"_cloudType\": \"GCP\",\n" +
                "          \"region\": \"us-west1-a\",\n" +
                "          \"id\": \"8993151141438601059\",\n" +
                "          \"projectName\": \"cool-bay-349411\",\n" +
                "          \"name\": \"pacbot-demo-vm\",\n" +
                "          \"description\": \"\",\n" +
                "          \"disks\": [\n" +
                "            {\n" +
                "              \"_cloudType\": \"GCP\",\n" +
                "              \"region\": null,\n" +
                "              \"id\": \"0\",\n" +
                "              \"projectName\": \"cool-bay-349411\",\n" +
                "              \"name\": \"pacbot-demo-vm\",\n" +
                "              \"sizeInGB\": 10,\n" +
                "              \"type\": \"PERSISTENT\",\n" +
                "              \"discoverydate\": null\n" +
                "            }\n" +
                "          ],\n" +
                "          \"tags\": {\n" +
                "            \"for\": \"pacbot-demo\",\n" +
                "            \"by\": \"skchalla\"\n" +
                "          },\n" +
                "          \"machineType\": \"https://www.googleapis.com/compute/v1/projects/cool-bay-349411/zones/us-west1-a/machineTypes/e2-medium\",\n"
                +
                "          \"status\": \"TERMINATED\",\n" +
                "          \"networkInterfaces\": [\n" +
                "            {\n" +
                "              \"_cloudType\": \"GCP\",\n" +
                "              \"region\": null,\n" +
                "              \"id\": \"nic0\",\n" +
                "              \"projectName\": null,\n" +
                "              \"name\": \"nic0\",\n" +
                "              \"accessConfigs\": [\n" +
                "                {\n" +
                "                  \"_cloudType\": \"GCP\",\n" +
                "                  \"region\": null,\n" +
                "                  \"id\": \"External NAT\",\n" +
                "                  \"projectName\": \"cool-bay-349411\",\n" +
                "                  \"name\": \"External NAT\",\n" +
                "                  \"natIP\": \"\",\n" +
                "                  \"discoverydate\": null\n" +
                "                }\n" +
                "              ],\n" +
                "              \"network\": \"https://www.googleapis.com/compute/v1/projects/cool-bay-349411/global/networks/default\",\n"
                +
                "              \"discoverydate\": null\n" +
                "            }\n" +
                "          ],\n" +
                "          \"discoverydate\": \"2022-06-16 06:00:00+0000\",\n" +
                "          \"_resourceid\": \"8993151141438601059\",\n" +
                "          \"_docid\": \"8993151141438601059\",\n" +
                "          \"_entity\": \"true\",\n" +
                "          \"_entitytype\": \"vminstance\",\n" +
                "          \"firstdiscoveredon\": \"2022-06-14 10:00:00+0000\",\n" +
                "          \"latest\": true,\n" +
                "          \"_loaddate\": \"2022-06-16 06:12:00+0000\"\n" +
                "        }", JsonElement.class));

        JsonArray array = new JsonArray();
        array.add(jsonObject);
        return array;
    }

    public static JsonArray getHitsJsonArrayForVMPublicAccessFailure() {
        Gson gson = new Gson();
        JsonObject jsonObject = new JsonObject();
        jsonObject.add("_source", gson.fromJson("{\n" +
                "          \"_cloudType\": \"GCP\",\n" +
                "          \"region\": \"us-west1-a\",\n" +
                "          \"id\": \"8993151141438601059\",\n" +
                "          \"projectName\": \"cool-bay-349411\",\n" +
                "          \"name\": \"pacbot-demo-vm\",\n" +
                "          \"description\": \"\",\n" +
                "          \"disks\": [\n" +
                "            {\n" +
                "              \"_cloudType\": \"GCP\",\n" +
                "              \"region\": null,\n" +
                "              \"id\": \"0\",\n" +
                "              \"projectName\": \"cool-bay-349411\",\n" +
                "              \"name\": \"pacbot-demo-vm\",\n" +
                "              \"sizeInGB\": 10,\n" +
                "              \"type\": \"PERSISTENT\",\n" +
                "              \"discoverydate\": null\n" +
                "            }\n" +
                "          ],\n" +
                "          \"tags\": {\n" +
                "            \"for\": \"pacbot-demo\",\n" +
                "            \"by\": \"skchalla\"\n" +
                "          },\n" +
                "          \"machineType\": \"https://www.googleapis.com/compute/v1/projects/cool-bay-349411/zones/us-west1-a/machineTypes/e2-medium\",\n"
                +
                "          \"status\": \"TERMINATED\",\n" +
                "          \"networkInterfaces\": [\n" +
                "            {\n" +
                "              \"_cloudType\": \"GCP\",\n" +
                "              \"region\": null,\n" +
                "              \"id\": \"nic0\",\n" +
                "              \"projectName\": null,\n" +
                "              \"name\": \"nic0\",\n" +
                "              \"accessConfigs\": [\n" +
                "                {\n" +
                "                  \"_cloudType\": \"GCP\",\n" +
                "                  \"region\": null,\n" +
                "                  \"id\": \"External NAT\",\n" +
                "                  \"projectName\": \"cool-bay-349411\",\n" +
                "                  \"name\": \"External NAT\",\n" +
                "                  \"natIP\": \"34.148.124.52\",\n" +
                "                  \"discoverydate\": null\n" +
                "                }\n" +
                "              ],\n" +
                "              \"network\": \"https://www.googleapis.com/compute/v1/projects/cool-bay-349411/global/networks/default\",\n"
                +
                "              \"discoverydate\": null\n" +
                "            }\n" +
                "          ],\n" +
                "          \"discoverydate\": \"2022-06-16 06:00:00+0000\",\n" +
                "          \"_resourceid\": \"8993151141438601059\",\n" +
                "          \"_docid\": \"8993151141438601059\",\n" +
                "          \"_entity\": \"true\",\n" +
                "          \"_entitytype\": \"vminstance\",\n" +
                "          \"firstdiscoveredon\": \"2022-06-14 10:00:00+0000\",\n" +
                "          \"latest\": true,\n" +
                "          \"_loaddate\": \"2022-06-16 06:12:00+0000\"\n" +
                "        }", JsonElement.class));

        JsonArray array = new JsonArray();
        array.add(jsonObject);
        return array;
    }

    public static JsonArray getHitsJsonArrayForBigQuerydataset() {
        Gson gson = new Gson();
        JsonObject jsonObject = new JsonObject();
        jsonObject.add("_source", gson.fromJson("{\n" +
                "          \"_cloudType\": \"gcp\",\n" +
                "          \"region\": \"US\",\n" +
                "          \"id\": \"tesing_dataset\",\n" +
                "          \"projectName\": \"cool-bay-349411\",\n" +
                "          \"projectId\": \"cool-bay-349411\",\n" +
                "          \"datasetId\": \"tesing_dataset\",\n" +
                "          \"acl\": [\n" +
                "            {\n" +
                "              \"role\": \"WRITER\",\n" +
                "              \"specialGroup\": \"projectWriters\"\n" +
                "            },\n" +
                "            {\n" +
                "              \"role\": \"OWNER\",\n" +
                "              \"specialGroup\": \"projectOwners\"\n" +
                "            },\n" +
                "            {\n" +
                "              \"role\": \"OWNER\",\n" +
                "              \"userByEmail\": \"pacbot-demo@cool-bay-349411.iam.gserviceaccount.com\"\n" +
                "            },\n" +
                "            {\n" +
                "              \"role\": \"OWNER\",\n" +
                "              \"userByEmail\": \"santhosh.challa@zemosolabs.com\"\n" +
                "            },\n" +
                "            {\n" +
                "              \"role\": \"READER\",\n" +
                "              \"specialGroup\": \"projectReaders\"\n" +
                "            },\n" +
                "            {\n" +
                "              \"role\": \"READER\",\n" +
                "              \"userByEmail\": \"pacbot-demo@cool-bay-349411.iam.gserviceaccount.com\"\n" +
                "            }\n" +
                "          ],\n" +
                "          \"defaultTableLifetime\": null,\n" +
                "          \"description\": \"test data set\",\n" +
                "          \"etag\": \"mS16Y7CQ5o1etp5Qj0oxRg==\",\n" +
                "          \"friendlyName\": null,\n" +
                "          \"generatedId\": \"cool-bay-349411:tesing_dataset\",\n" +
                "          \"lastModified\": null,\n" +
                "          \"labels\": {\n" +
                "            \"application\": \"demo_app\"\n" +
                "          },\n" +
                "          \"kmsKeyName\": null,\n" +
                "          \"defaultPartitionExpirationMs\": null,\n" +
                "          \"discoverydate\": \"2022-06-22 13:00:00+0000\",\n" +
                "          \"_resourceid\": \"tesing_dataset\",\n" +
                "          \"_docid\": \"tesing_dataset\",\n" +
                "          \"_entity\": \"true\",\n" +
                "          \"_entitytype\": \"bigquerydataset\",\n" +
                "          \"firstdiscoveredon\": \"2022-06-22 13:00:00+0000\",\n" +
                "          \"latest\": true,\n" +
                "          \"_loaddate\": \"2022-06-22 13:43:00+0000\"\n" +
                "        }", JsonElement.class));
        JsonArray array = new JsonArray();
        array.add(jsonObject);
        return array;
    }

    public static JsonArray getHitsJsonArrayForBigQuerydatasetFailure() {
        Gson gson = new Gson();
        JsonObject jsonObject = new JsonObject();
        jsonObject.add("_source", gson.fromJson("{\n" +
                "          \"_cloudType\": \"gcp\",\n" +
                "          \"region\": \"us-east1\",\n" +
                "          \"id\": \"test_dataSet2\",\n" +
                "          \"projectName\": \"cool-bay-349411\",\n" +
                "          \"projectId\": \"cool-bay-349411\",\n" +
                "          \"datasetId\": \"test_dataSet2\",\n" +
                "          \"acl\": [\n" +
                "            {\n" +
                "              \"role\": \"WRITER\",\n" +
                "              \"specialGroup\": \"projectWriters\"\n" +
                "            },\n" +
                "            {\n" +
                "              \"role\": \"OWNER\",\n" +
                "              \"specialGroup\": \"projectOwners\"\n" +
                "            },\n" +
                "            {\n" +
                "              \"role\": \"OWNER\",\n" +
                "              \"userByEmail\": \"santhosh.challa@zemosolabs.com\"\n" +
                "            },\n" +
                "            {\n" +
                "              \"role\": \"READER\",\n" +
                "              \"specialGroup\": \"projectReaders\"\n" +
                "            },\n" +
                "            {\n" +
                "              \"role\": \"roles/viewer\",\n" +
                "              \"iamMember\": \"allUsers\"\n" +
                "            },\n" +
                "            {\n" +
                "              \"role\": \"roles/viewer\",\n" +
                "              \"specialGroup\": \"projectReaders\"\n" +
                "            }\n" +
                "          ],\n" +
                "          \"defaultTableLifetime\": null,\n" +
                "          \"description\": null,\n" +
                "          \"etag\": \"VntiAMxVlRTtmtfE09sq+w==\",\n" +
                "          \"friendlyName\": null,\n" +
                "          \"generatedId\": \"cool-bay-349411:test_dataSet2\",\n" +
                "          \"lastModified\": null,\n" +
                "          \"labels\": {},\n" +
                "          \"kmsKeyName\": null,\n" +
                "          \"defaultPartitionExpirationMs\": null,\n" +
                "          \"discoverydate\": \"2022-06-22 13:00:00+0000\",\n" +
                "          \"_resourceid\": \"test_dataSet2\",\n" +
                "          \"_docid\": \"test_dataSet2\",\n" +
                "          \"_entity\": \"true\",\n" +
                "          \"_entitytype\": \"bigquerydataset\",\n" +
                "          \"firstdiscoveredon\": \"2022-06-22 13:00:00+0000\",\n" +
                "          \"latest\": true,\n" +
                "          \"_loaddate\": \"2022-06-22 13:43:00+0000\"\n" +
                "        }", JsonElement.class));
        JsonArray array = new JsonArray();
        array.add(jsonObject);
        return array;
    }

    public static JsonArray getHitsJsonForTableEncryptCMKsSuccess() {
        Gson gson = new Gson();
        JsonObject jsonObject = new JsonObject();
        jsonObject.add("_source", gson.fromJson(
                "{\"_cloudType\":\"gcp\",\"region\":null,\"id\":\"test123\",\"projectName\":\"cool-bay-349411\",\"tableId\":\"test123\",\"description\":null,\"friendlyName\":null,\"generatedId\":\"cool-bay-349411:tesing_dataset.test123\",\"labels\":{},\"kmsKeyName\":\"key1\",\"dataSetId\":\"tesing_dataset\",\"iamResourceName\":\"projects/cool-bay-349411/datasets/tesing_dataset/tables/test123\",\"expirationTime\":null,\"creationTime\":1655789087444,\"lastModifiedTime\":null,\"etag\":null,\"requirePartitionFilter\":false,\"discoverydate\":\"2022-06-23 10:00:00+0000\",\"_resourceid\":\"test123\",\"_docid\":\"test123\",\"_entity\":\"true\",\"_entitytype\":\"bigquerytable\",\"firstdiscoveredon\":\"2022-06-23 10:00:00+0000\",\"latest\":true,\"_loaddate\":\"2022-06-23 10:59:00+0000\"}\n",
                JsonElement.class));
        JsonArray array = new JsonArray();
        array.add(jsonObject);
        return array;
    }

    public static JsonArray getHitsJsonForTableEncryptCMKsFailure() {
        Gson gson = new Gson();
        JsonObject jsonObject = new JsonObject();
        jsonObject.add("_source", gson.fromJson(
                "{\"_cloudType\":\"gcp\",\"region\":null,\"id\":\"test123\",\"projectName\":\"cool-bay-349411\",\"tableId\":\"test123\",\"description\":null,\"friendlyName\":null,\"generatedId\":\"cool-bay-349411:tesing_dataset.test123\",\"labels\":{},\"kmsKeyName\":null,\"dataSetId\":\"tesing_dataset\",\"iamResourceName\":\"projects/cool-bay-349411/datasets/tesing_dataset/tables/test123\",\"expirationTime\":null,\"creationTime\":1655789087444,\"lastModifiedTime\":null,\"etag\":null,\"requirePartitionFilter\":false,\"discoverydate\":\"2022-06-23 10:00:00+0000\",\"_resourceid\":\"test123\",\"_docid\":\"test123\",\"_entity\":\"true\",\"_entitytype\":\"bigquerytable\",\"firstdiscoveredon\":\"2022-06-23 10:00:00+0000\",\"latest\":true,\"_loaddate\":\"2022-06-23 10:59:00+0000\"}\n",
                JsonElement.class));
        JsonArray array = new JsonArray();
        array.add(jsonObject);
        return array;
    }

    public static JsonArray getHitsJsonArrayForCloudStoragePublicAccessFailure() {
        Gson gson = new Gson();
        JsonObject jsonObject = new JsonObject();
        jsonObject.add("_source", gson.fromJson("{\n" +
                "          \"_cloudType\": \"GCP\",\n" +
                "          \"region\": \"us-west1-a\",\n" +
                "          \"id\": \"staging.cool-bay-349411.appspot.com\",\n" +
                "          \"projectName\": \"cool-bay-349411\",\n" +
                "          \"name\": \"pacbot-demo-vm\",\n" +
                "          \"users\": [\n" +
                "              \"allAuthenticatedUsers\",\n" +
                "              \"allUsers\" \n" +
                "          ],\n" +
                "          \"discoverydate\": \"2022-06-16 06:00:00+0000\",\n" +
                "          \"_resourceid\": \"8993151141438601059\",\n" +
                "          \"_docid\": \"8993151141438601059\",\n" +
                "          \"_entity\": \"true\",\n" +
                "          \"_entitytype\": \"vminstance\",\n" +
                "          \"firstdiscoveredon\": \"2022-06-14 10:00:00+0000\",\n" +
                "          \"latest\": true,\n" +
                "          \"_loaddate\": \"2022-06-16 06:12:00+0000\"\n" +
                "        }", JsonElement.class));

        JsonArray array = new JsonArray();
        array.add(jsonObject);
        return array;
    }

    public static JsonArray getHitsJsonArrayForCloudStoragePublicAccessSuccess() {
        Gson gson = new Gson();
        JsonObject jsonObject = new JsonObject();
        jsonObject.add("_source", gson.fromJson("{\n" +
                "          \"_cloudType\": \"GCP\",\n" +
                "          \"region\": \"us-west1-a\",\n" +
                "          \"id\": \"staging.cool-bay-349411.appspot.com\",\n" +
                "          \"projectName\": \"cool-bay-349411\",\n" +
                "          \"name\": \"pacbot-demo-vm\",\n" +
                "          \"users\": [\n" +
                "              \"project-owners-47822473470\",\n" +
                "              \"project-viewers-47822473470\" \n" +
                "          ],\n" +
                "          \"discoverydate\": \"2022-06-16 06:00:00+0000\",\n" +
                "          \"_resourceid\": \"8993151141438601059\",\n" +
                "          \"_docid\": \"8993151141438601059\",\n" +
                "          \"_entity\": \"true\",\n" +
                "          \"_entitytype\": \"vminstance\",\n" +
                "          \"firstdiscoveredon\": \"2022-06-14 10:00:00+0000\",\n" +
                "          \"latest\": true,\n" +
                "          \"_loaddate\": \"2022-06-16 06:12:00+0000\"\n" +
                "        }", JsonElement.class));

        JsonArray array = new JsonArray();
        array.add(jsonObject);
        return array;
    }

    public static JsonArray getHitsJsonArrayForCloudStorageCMKEncryptionFailure() {
        Gson gson = new Gson();
        JsonObject jsonObject = new JsonObject();
        jsonObject.add("_source", gson.fromJson("{\n" +
                "          \"_cloudType\": \"GCP\",\n" +
                "          \"region\": \"us-west1-a\",\n" +
                "          \"id\": \"staging.cool-bay-349411.appspot.com\",\n" +
                "          \"projectName\": \"cool-bay-349411\",\n" +
                "          \"name\": \"pacbot-demo-vm\",\n" +
                "          \"users\": [\n" +
                "              \"allAuthenticatedUsers\",\n" +
                "              \"allUsers\" \n" +
                "          ],\n" +
                "          \"defaultKmsKeyName\": null,\n" +
                "          \"discoverydate\": \"2022-06-16 06:00:00+0000\",\n" +
                "          \"_resourceid\": \"8993151141438601059\",\n" +
                "          \"_docid\": \"8993151141438601059\",\n" +
                "          \"_entity\": \"true\",\n" +
                "          \"_entitytype\": \"vminstance\",\n" +
                "          \"firstdiscoveredon\": \"2022-06-14 10:00:00+0000\",\n" +
                "          \"latest\": true,\n" +
                "          \"_loaddate\": \"2022-06-16 06:12:00+0000\"\n" +
                "        }", JsonElement.class));

        JsonArray array = new JsonArray();
        array.add(jsonObject);
        return array;
    }

    public static JsonArray getHitsJsonArrayForCloudSqlHighAvailabilitySuccess() {
        Gson gson = new Gson();
        JsonObject jsonObject = new JsonObject();
        jsonObject.add("_source", gson.fromJson("{\n" +
                "          \"_cloudType\": \"gcp\",\n" +
                "          \"region\": \"us-central1\",\n" +
                "          \"id\": \"cool-bay-349411:us-central1:gcp-mysql-sever1\",\n" +
                "          \"projectName\": \"cool-bay-349411\",\n" +
                "          \"name\": \"gcp-mysql-sever1\",\n" +
                "          \"kind\": \"sql#instance\",\n" +
                "          \"createdTime\": \"2022-08-11T08:17:19.922Z\",\n" +
                "          \"masterInstanceName\": null,\n" +
                "          \"backendType\": \"SECOND_GEN\",\n" +
                "          \"state\": \"RUNNABLE\",\n" +
                "          \"databaseVersion\": \"SQLSERVER_2019_STANDARD\",\n" +
                "          \"databaseInstalledVersion\": \"SQLSERVER_2019_STANDARD_CU16\",\n" +
                "          \"instanceType\": \"CLOUD_SQL_INSTANCE\",\n" +
                "          \"eTag\": \"7d608164c64713163c83a83f5170b9af2e642287919b366faf54d605337abc6c\",\n" +
                "          \"selfLink\": \"https://sqladmin.googleapis.com/v1/projects/cool-bay-349411/instances/gcp-mysql-sever1\",\n" +
                "          \"serviceAccountEmail\": \"p47822473470-cpwrh9@gcp-sa-cloud-sql.iam.gserviceaccount.com\",\n" +
                "          \"kmsKeyVersion\": null,\n" +
                "          \"kmsKeyName\": null,\n" +
                "          \"maxDiskSize\": null,\n" +
                "          \"currentDiskSize\": null,\n" +
                "          \"ipAddress\": [\n" +
                "            {\n" +
                "              \"ip\": \"34.171.119.180\",\n" +
                "              \"type\": \"PRIMARY\"\n" +
                "            }\n" +
                "          ],\n" +
                "          \"serverCaCert\": {\n" +
                "            \"certSerialNumber\": \"0\",\n" +
                "            \"commonName\": \"C=US,O=Google\\\\, Inc,CN=Google Cloud SQL Server CA,dnQualifier=2149545f-4fd6-4ce3-9ab8-0ec282e3cd71\",\n" +
                "            \"createTime\": \"2022-08-11T08:18:03.671Z\",\n" +
                "            \"expirationTime\": \"2032-08-08T08:19:03.671Z\",\n" +
                "            \"instance\": \"gcp-mysql-sever1\",\n" +
                "            \"kind\": \"sql#sslCert\"\n" +
                "          },\n" +
                "          \"settings\": {\n" +
                "            \"activationPolicy\": \"ALWAYS\",\n" +
                "            \"activeDirectoryConfig\": {\n" +
                "              \"kind\": \"sql#activeDirectoryConfig\"\n" +
                "            },\n" +
                "            \"authorizedGaeApplications\": [],\n" +
                "            \"availabilityType\": \"REGIONAL\",\n" +
                "            \"backupConfiguration\": {\n" +
                "              \"backupRetentionSettings\": {\n" +
                "                \"retainedBackups\": 7,\n" +
                "                \"retentionUnit\": \"COUNT\"\n" +
                "              },\n" +
                "              \"enabled\": true,\n" +
                "              \"kind\": \"sql#backupConfiguration\",\n" +
                "              \"location\": \"us\",\n" +
                "              \"startTime\": \"10:00\",\n" +
                "              \"transactionLogRetentionDays\": 7\n" +
                "            },\n" +
                "            \"collation\": \"SQL_Latin1_General_CP1_CI_AS\",\n" +
                "            \"dataDiskSizeGb\": 100,\n" +
                "            \"dataDiskType\": \"PD_SSD\",\n" +
                "            \"databaseFlags\": [\n" +
                "              {\n" +
                "                \"name\": \"cross db ownership chaining\",\n" +
                "                \"value\": \"off\"\n" +
                "              }\n" +
                "            ],\n" +
                "            \"ipConfiguration\": {\n" +
                "              \"authorizedNetworks\": [\\n\" +\n" +
                "                \"            {\\n\" +\n" +
                "                \"              \\\"ip\\\": \\\"34.171.119.180\\\",\\n\" +\n" +
                "                \"              \\\"type\\\": \\\"PRIMARY\\\"\\n\" +\n" +
                "                \"            }\\n\" +],\n" +
                "              \"ipv4Enabled\": true\n" +
                "            },\n" +
                "            \"kind\": \"sql#settings\",\n" +
                "            \"locationPreference\": {\n" +
                "              \"kind\": \"sql#locationPreference\",\n" +
                "              \"zone\": \"us-central1-b\"\n" +
                "            },\n" +
                "            \"maintenanceWindow\": {\n" +
                "              \"day\": 0,\n" +
                "              \"hour\": 0,\n" +
                "              \"kind\": \"sql#maintenanceWindow\",\n" +
                "              \"updateTrack\": \"stable\"\n" +
                "            },\n" +
                "            \"pricingPlan\": \"PER_USE\",\n" +
                "            \"replicationType\": \"SYNCHRONOUS\",\n" +
                "            \"settingsVersion\": 1,\n" +
                "            \"sqlServerAuditConfig\": {\n" +
                "              \"kind\": \"sql#sqlServerAuditConfig\",\n" +
                "              \"retentionInterval\": \"0s\",\n" +
                "              \"uploadInterval\": \"0s\"\n" +
                "            },\n" +
                "            \"storageAutoResize\": true,\n" +
                "            \"storageAutoResizeLimit\": 0,\n" +
                "            \"tier\": \"db-custom-2-8192\",\n" +
                "            \"deletionProtectionEnabled\": true\n" +
                "          },\n" +
                "          \"dataBaseFlags\": null,\n" +
                "          \"discoverydate\": \"2022-08-12 00:00:00+0000\",\n" +
                "          \"_resourceid\": \"cool-bay-349411:us-central1:gcp-mysql-sever1\",\n" +
                "          \"_docid\": \"cool-bay-349411:us-central1:gcp-mysql-sever1\",\n" +
                "          \"_entity\": \"true\",\n" +
                "          \"_entitytype\": \"cloudsql\",\n" +
                "          \"firstdiscoveredon\": \"2022-08-11 08:00:00+0000\",\n" +
                "          \"latest\": true,\n" +
                "          \"_loaddate\": \"2022-08-12 06:23:00+0000\"\n" +
                "        }", JsonElement.class));

        JsonArray array = new JsonArray();
        array.add(jsonObject);
        return array;
    }

    public static JsonArray getHitsJsonArrayForCloudSQLHighAvailabilityFailure() {
        Gson gson = new Gson();
        JsonObject jsonObject = new JsonObject();
        jsonObject.add("_source", gson.fromJson("{\n" +
                "          \"_cloudType\": \"gcp\",\n" +
                "          \"region\": \"us-central1\",\n" +
                "          \"id\": \"cool-bay-349411:us-central1:gcp-mysql-sever1\",\n" +
                "          \"projectName\": \"cool-bay-349411\",\n" +
                "          \"name\": \"gcp-mysql-sever1\",\n" +
                "          \"kind\": \"sql#instance\",\n" +
                "          \"createdTime\": \"2022-08-11T08:17:19.922Z\",\n" +
                "          \"masterInstanceName\": null,\n" +
                "          \"backendType\": \"SECOND_GEN\",\n" +
                "          \"state\": \"RUNNABLE\",\n" +
                "          \"databaseVersion\": \"SQLSERVER_2019_STANDARD\",\n" +
                "          \"databaseInstalledVersion\": \"SQLSERVER_2019_STANDARD_CU16\",\n" +
                "          \"instanceType\": \"CLOUD_SQL_INSTANCE\",\n" +
                "          \"eTag\": \"7d608164c64713163c83a83f5170b9af2e642287919b366faf54d605337abc6c\",\n" +
                "          \"selfLink\": \"https://sqladmin.googleapis.com/v1/projects/cool-bay-349411/instances/gcp-mysql-sever1\",\n" +
                "          \"serviceAccountEmail\": \"p47822473470-cpwrh9@gcp-sa-cloud-sql.iam.gserviceaccount.com\",\n" +
                "          \"kmsKeyVersion\": null,\n" +
                "          \"kmsKeyName\": null,\n" +
                "          \"maxDiskSize\": null,\n" +
                "          \"currentDiskSize\": null,\n" +
                "          \"ipAddress\": [\n" +
                "            {\n" +
                "              \"ip\": \"34.171.119.180\",\n" +
                "              \"type\": \"PRIMARY\"\n" +
                "            }\n" +
                "          ],\n" +
                "          \"serverCaCert\": {\n" +
                "            \"certSerialNumber\": \"0\",\n" +
                "            \"commonName\": \"C=US,O=Google\\\\, Inc,CN=Google Cloud SQL Server CA,dnQualifier=2149545f-4fd6-4ce3-9ab8-0ec282e3cd71\",\n" +
                "            \"createTime\": \"2022-08-11T08:18:03.671Z\",\n" +
                "            \"expirationTime\": \"2032-08-08T08:19:03.671Z\",\n" +
                "            \"instance\": \"gcp-mysql-sever1\",\n" +
                "            \"kind\": \"sql#sslCert\"\n" +
                "          },\n" +
                "          \"settings\": {\n" +
                "            \"activationPolicy\": \"ALWAYS\",\n" +
                "            \"activeDirectoryConfig\": {\n" +
                "              \"kind\": \"sql#activeDirectoryConfig\"\n" +
                "            },\n" +
                "            \"authorizedGaeApplications\": [],\n" +
                "            \"availabilityType\": \"ZONAL\",\n" +
                "            \"backupConfiguration\": {\n" +
                "              \"backupRetentionSettings\": {\n" +
                "                \"retainedBackups\": 7,\n" +
                "                \"retentionUnit\": \"COUNT\"\n" +
                "              },\n" +
                "              \"enabled\": true,\n" +
                "              \"kind\": \"sql#backupConfiguration\",\n" +
                "              \"location\": \"us\",\n" +
                "              \"startTime\": \"10:00\",\n" +
                "              \"transactionLogRetentionDays\": 7\n" +
                "            },\n" +
                "            \"collation\": \"SQL_Latin1_General_CP1_CI_AS\",\n" +
                "            \"dataDiskSizeGb\": 100,\n" +
                "            \"dataDiskType\": \"PD_SSD\",\n" +
                "            \"databaseFlags\": [\n" +
                "              {\n" +
                "                \"name\": \"cross db ownership chaining\",\n" +
                "                \"value\": \"off\"\n" +
                "              }\n" +
                "            ],\n" +
                "            \"ipConfiguration\": {\n" +
                "              \"authorizedNetworks\": [],\n" +
                "              \"ipv4Enabled\": true\n" +
                "            },\n" +
                "            \"kind\": \"sql#settings\",\n" +
                "            \"locationPreference\": {\n" +
                "              \"kind\": \"sql#locationPreference\",\n" +
                "              \"zone\": \"us-central1-b\"\n" +
                "            },\n" +
                "            \"maintenanceWindow\": {\n" +
                "              \"day\": 0,\n" +
                "              \"hour\": 0,\n" +
                "              \"kind\": \"sql#maintenanceWindow\",\n" +
                "              \"updateTrack\": \"stable\"\n" +
                "            },\n" +
                "            \"pricingPlan\": \"PER_USE\",\n" +
                "            \"replicationType\": \"SYNCHRONOUS\",\n" +
                "            \"settingsVersion\": 1,\n" +
                "            \"sqlServerAuditConfig\": {\n" +
                "              \"kind\": \"sql#sqlServerAuditConfig\",\n" +
                "              \"retentionInterval\": \"0s\",\n" +
                "              \"uploadInterval\": \"0s\"\n" +
                "            },\n" +
                "            \"storageAutoResize\": true,\n" +
                "            \"storageAutoResizeLimit\": 0,\n" +
                "            \"tier\": \"db-custom-2-8192\",\n" +
                "            \"deletionProtectionEnabled\": true\n" +
                "          },\n" +
                "          \"dataBaseFlags\": null,\n" +
                "          \"discoverydate\": \"2022-08-12 00:00:00+0000\",\n" +
                "          \"_resourceid\": \"cool-bay-349411:us-central1:gcp-mysql-sever1\",\n" +
                "          \"_docid\": \"cool-bay-349411:us-central1:gcp-mysql-sever1\",\n" +
                "          \"_entity\": \"true\",\n" +
                "          \"_entitytype\": \"cloudsql\",\n" +
                "          \"firstdiscoveredon\": \"2022-08-11 08:00:00+0000\",\n" +
                "          \"latest\": true,\n" +
                "          \"_loaddate\": \"2022-08-12 06:23:00+0000\"\n" +
                "        }", JsonElement.class));

        JsonArray array = new JsonArray();
        array.add(jsonObject);
        return array;
    }

    public static JsonArray getHitsJsonArrayForCloudStorageCMKEncryptionSuccess() {
        Gson gson = new Gson();
        JsonObject jsonObject = new JsonObject();
        jsonObject.add("_source", gson.fromJson("{\n" +
                "          \"_cloudType\": \"GCP\",\n" +
                "          \"region\": \"us-west1-a\",\n" +
                "          \"id\": \"staging.cool-bay-349411.appspot.com\",\n" +
                "          \"projectName\": \"cool-bay-349411\",\n" +
                "          \"name\": \"pacbot-demo-vm\",\n" +
                "          \"users\": [\n" +
                "              \"project-owners-47822473470\",\n" +
                "              \"project-viewers-47822473470\" \n" +
                "          ],\n" +
                "          \"defaultKmsKeyName\": \"projects/cool-bay-349411/locations/us/keyRings/cool-bay-349411-key-ring/cryptoKeys/cool-bay-bigquery-cmk\",\n"
                +
                "          \"discoverydate\": \"2022-06-16 06:00:00+0000\",\n" +
                "          \"_resourceid\": \"8993151141438601059\",\n" +
                "          \"_docid\": \"8993151141438601059\",\n" +
                "          \"_entity\": \"true\",\n" +
                "          \"_entitytype\": \"vminstance\",\n" +
                "          \"firstdiscoveredon\": \"2022-06-14 10:00:00+0000\",\n" +
                "          \"latest\": true,\n" +
                "          \"_loaddate\": \"2022-06-16 06:12:00+0000\"\n" +
                "        }", JsonElement.class));

        JsonArray array = new JsonArray();
        array.add(jsonObject);
        return array;
    }

    public static JsonArray getHitsJsonArrayForVM2FA() {
        Gson gson = new Gson();
        JsonObject jsonObject = new JsonObject();
        jsonObject.add("_source", gson.fromJson(
                "{\"_cloudType\":\"GCP\",\"region\":\"us-west1-a\",\"id\":\"8993151141438601059\",\"projectName\":\"cool-bay-349411\",\"name\":\"pacbot-demo-vm\",\"description\":\"\",\"disks\":[{\"_cloudType\":\"GCP\",\"region\":null,\"id\":\"0\",\"projectName\":\"cool-bay-349411\",\"name\":\"pacbot-demo-vm\",\"sizeInGB\":10,\"type\":\"PERSISTENT\",\"hasSha256\":false,\"hasKmsKeyName\":false,\"discoverydate\":null}],\"tags\":{\"for\":\"pacbot-demo\",\"by\":\"skchalla\"},\"machineType\":\"https://www.googleapis.com/compute/v1/projects/cool-bay-349411/zones/us-west1-a/machineTypes/e2-medium\",\"status\":\"TERMINATED\",\"networkInterfaces\":[{\"_cloudType\":\"GCP\",\"region\":null,\"id\":\"nic0\",\"projectName\":null,\"name\":\"nic0\",\"accessConfigs\":[{\"_cloudType\":\"GCP\",\"region\":null,\"id\":\"External NAT\",\"projectName\":\"cool-bay-349411\",\"name\":\"External NAT\",\"natIP\":\"\",\"discoverydate\":null}],\"network\":\"https://www.googleapis.com/compute/v1/projects/cool-bay-349411/global/networks/default\",\"discoverydate\":null}],\"items\":[{\"_cloudType\":\"GCP\",\"region\":null,\"id\":null,\"projectName\":null,\"key\":\"enable-oslogin\",\"value\":\"TRUE\",\"discoverydate\":null},{\"_cloudType\":\"GCP\",\"region\":null,\"id\":null,\"projectName\":null,\"key\":\"enable-oslogin-2fa\",\"value\":\"TRUE\",\"discoverydate\":null}],\"discoverydate\":\"2022-06-27 08:00:00+0000\",\"_resourceid\":\"8993151141438601059\",\"_docid\":\"8993151141438601059\",\"_entity\":\"true\",\"_entitytype\":\"vminstance\",\"firstdiscoveredon\":\"2022-06-14 10:00:00+0000\",\"latest\":true,\"_loaddate\":\"2022-06-27 08:42:00+0000\"}",
                JsonElement.class));

        JsonArray array = new JsonArray();
        array.add(jsonObject);
        return array;
    }

    public static JsonArray getHitsJsonArrayForVM2FAFailure() {
        Gson gson = new Gson();
        JsonObject jsonObject = new JsonObject();
        jsonObject.add("_source", gson.fromJson(
                "{\"_cloudType\":\"GCP\",\"region\":\"us-west1-a\",\"id\":\"8993151141438601059\",\"projectName\":\"cool-bay-349411\",\"name\":\"pacbot-demo-vm\",\"description\":\"\",\"disks\":[{\"_cloudType\":\"GCP\",\"region\":null,\"id\":\"0\",\"projectName\":\"cool-bay-349411\",\"name\":\"pacbot-demo-vm\",\"sizeInGB\":10,\"type\":\"PERSISTENT\",\"hasSha256\":false,\"hasKmsKeyName\":false,\"discoverydate\":null}],\"tags\":{\"for\":\"pacbot-demo\",\"by\":\"skchalla\"},\"machineType\":\"https://www.googleapis.com/compute/v1/projects/cool-bay-349411/zones/us-west1-a/machineTypes/e2-medium\",\"status\":\"TERMINATED\",\"networkInterfaces\":[{\"_cloudType\":\"GCP\",\"region\":null,\"id\":\"nic0\",\"projectName\":null,\"name\":\"nic0\",\"accessConfigs\":[{\"_cloudType\":\"GCP\",\"region\":null,\"id\":\"External NAT\",\"projectName\":\"cool-bay-349411\",\"name\":\"External NAT\",\"natIP\":\"\",\"discoverydate\":null}],\"network\":\"https://www.googleapis.com/compute/v1/projects/cool-bay-349411/global/networks/default\",\"discoverydate\":null}],\"items\":[{\"_cloudType\":\"GCP\",\"region\":null,\"id\":null,\"projectName\":null,\"key\":\"enable-oslogin\",\"value\":\"TRUE\",\"discoverydate\":null}],\"discoverydate\":\"2022-06-27 08:00:00+0000\",\"_resourceid\":\"8993151141438601059\",\"_docid\":\"8993151141438601059\",\"_entity\":\"true\",\"_entitytype\":\"vminstance\",\"firstdiscoveredon\":\"2022-06-14 10:00:00+0000\",\"latest\":true,\"_loaddate\":\"2022-06-27 08:42:00+0000\"}",
                JsonElement.class));

        JsonArray array = new JsonArray();
        array.add(jsonObject);
        return array;
    }

    public static JsonArray getHitsJsonArrayForPubSubEncryptionSuccess() {
        Gson gson = new Gson();
        JsonObject jsonObject = new JsonObject();
        jsonObject.add("_source", gson.fromJson(
                "{\"_cloudType\":\"GCP\",\"region\":null,\"id\":\"projects/cool-bay-349411/topics/test2\",\"projectName\":\"cool-bay-349411\",\"kmsKeyName\":\"projects/cool-bay-349411/locations/global/keyRings/demo-key-ring1/cryptoKeys/demo-key1\",\"discoverydate\":\"2022-07-01 06:00:00+0000\",\"_resourceid\":\"projects/cool-bay-349411/topics/test2\",\"_docid\":\"projects/cool-bay-349411/topics/test2\",\"_entity\":\"true\",\"_entitytype\":\"pubsub\",\"firstdiscoveredon\":\"2022-07-01 06:00:00+0000\",\"latest\":true,\"_loaddate\":\"2022-07-01 06:15:00+0000\"}",
                JsonElement.class));

        JsonArray array = new JsonArray();
        array.add(jsonObject);
        return array;
    }

    public static JsonArray getHitsJsonArrayForPubSubEncryptionFailure() {
        Gson gson = new Gson();
        JsonObject jsonObject = new JsonObject();
        jsonObject.add("_source", gson.fromJson(
                "{\"_cloudType\":\"GCP\",\"region\":null,\"id\":\"projects/cool-bay-349411/topics/test2\",\"projectName\":\"cool-bay-349411\",\"kmsKeyName\":null,\"discoverydate\":\"2022-07-01 06:00:00+0000\",\"_resourceid\":\"projects/cool-bay-349411/topics/test2\",\"_docid\":\"projects/cool-bay-349411/topics/test2\",\"_entity\":\"true\",\"_entitytype\":\"pubsub\",\"firstdiscoveredon\":\"2022-07-01 06:00:00+0000\",\"latest\":true,\"_loaddate\":\"2022-07-01 06:15:00+0000\"}",
                JsonElement.class));

        JsonArray array = new JsonArray();
        array.add(jsonObject);
        return array;
    }

    public static JsonArray getHitsJsonForDataProcEncryptCMKsSuccess() {
        Gson gson = new Gson();
        JsonObject jsonObject = new JsonObject();
        jsonObject.add("_source", gson.fromJson(
                "{\"_cloudType\":\"GCP\",\"region\":\"us-east1\",\"id\":\"cluster-4174\",\"projectName\":\"cool-bay-349411\",\"kmsKeyName\":\"projects/cool-bay-349411/locations/us-east1/keyRings/cool-bay-349411-cloudsql-key-ring/cryptoKeys/cool-bay-cloudsql-cmk\",\"discoverydate\":\"2022-07-19 10:00:00+0000\",\"_resourceid\":\"cluster-4174\",\"_docid\":\"cluster-4174\",\"_entity\":\"true\",\"_entitytype\":\"dataproc\",\"firstdiscoveredon\":\"2022-07-19 10:00:00+0000\",\"latest\":true,\"_loaddate\":\"2022-07-19 11:00:00+0000\"}",
                JsonElement.class));

        JsonArray array = new JsonArray();
        array.add(jsonObject);
        return array;
    }

    public static JsonArray getHitsJsonForDataProcEncryptCMKsFailure() {
        Gson gson = new Gson();
        JsonObject jsonObject = new JsonObject();
        jsonObject.add("_source", gson.fromJson("{\n" +
                "          \"_cloudType\": \"GCP\",\n" +
                "          \"region\": \"us-central1\",\n" +
                "          \"id\": \"cluster-fc36\",\n" +
                "          \"projectName\": \"cool-bay-349411\",\n" +
                "          \"kmsKeyName\": null,\n" +
                "          \"discoverydate\": \"2022-07-19 10:00:00+0000\",\n" +
                "          \"_resourceid\": \"cluster-fc36\",\n" +
                "          \"_docid\": \"cluster-fc36\",\n" +
                "          \"_entity\": \"true\",\n" +
                "          \"_entitytype\": \"dataproc\",\n" +
                "          \"firstdiscoveredon\": \"2022-07-19 10:00:00+0000\",\n" +
                "          \"latest\": true,\n" +
                "          \"_loaddate\": \"2022-07-19 11:00:00+0000\"\n" +
                "        }", JsonElement.class));

        JsonArray array = new JsonArray();
        array.add(jsonObject);
        return array;
    }
    public static JsonArray getHitsJsonForVMTerminateFailure() {
        Gson gson = new Gson();
        JsonObject jsonObject = new JsonObject();
        jsonObject.add("_source", gson.fromJson("{\n" +
                "          \"_cloudType\": \"GCP\",\n" +
                "          \"region\": \"us-west4-b\",\n" +
                "          \"id\": \"4705952002456105765\",\n" +
                "          \"projectName\": \"my-project-22-354616\",\n" +
                "          \"name\": \"paladin-instance-1\",\n" +
                "          \"description\": \"\",\n" +
                "          \"disks\": [\n" +
                "            {\n" +
                "              \"_cloudType\": \"GCP\",\n" +
                "              \"region\": null,\n" +
                "              \"id\": \"0\",\n" +
                "              \"projectName\": \"my-project-22-354616\",\n" +
                "              \"name\": \"paladin-instance-1\",\n" +
                "              \"sizeInGB\": 10,\n" +
                "              \"type\": \"PERSISTENT\",\n" +
                "              \"hasSha256\": false,\n" +
                "              \"hasKmsKeyName\": false,\n" +
                "              \"discoverydate\": null\n" +
                "            }\n" +
                "          ],\n" +
                "          \"tags\": {\n" +
                "            \"application\": \"paladincloud\",\n" +
                "            \"enviornment\": \"gcp-demo\"\n" +
                "          },\n" +
                "          \"machineType\": \"https://www.googleapis.com/compute/v1/projects/my-project-22-354616/zones/us-west4-b/machineTypes/e2-micro\",\n" +
                "          \"status\": \"TERMINATED\",\n" +
                "          \"networkInterfaces\": [\n" +
                "            {\n" +
                "              \"_cloudType\": \"GCP\",\n" +
                "              \"region\": null,\n" +
                "              \"id\": \"nic0\",\n" +
                "              \"projectName\": null,\n" +
                "              \"name\": \"nic0\",\n" +
                "              \"accessConfigs\": [\n" +
                "                {\n" +
                "                  \"_cloudType\": \"GCP\",\n" +
                "                  \"region\": null,\n" +
                "                  \"id\": \"External NAT\",\n" +
                "                  \"projectName\": \"my-project-22-354616\",\n" +
                "                  \"name\": \"External NAT\",\n" +
                "                  \"natIP\": \"\",\n" +
                "                  \"discoverydate\": null\n" +
                "                }\n" +
                "              ],\n" +
                "              \"network\": \"https://www.googleapis.com/compute/v1/projects/my-project-22-354616/global/networks/default\",\n" +
                "              \"discoverydate\": null\n" +
                "            }\n" +
                "          ],\n" +
                "          \"items\": [],\n" +
                "          \"onHostMaintainence\": \"TERMINATE\",\n" +
                "          \"discoverydate\": \"2022-08-02 10:00:00+0000\",\n" +
                "          \"_resourceid\": \"4705952002456105765\",\n" +
                "          \"_docid\": \"4705952002456105765\",\n" +
                "          \"_entity\": \"true\",\n" +
                "          \"_entitytype\": \"vminstance\",\n" +
                "          \"firstdiscoveredon\": \"2022-07-20 15:00:00+0000\",\n" +
                "          \"latest\": true,\n" +
                "          \"_loaddate\": \"2022-08-02 11:03:00+0000\"\n" +
                "        }", JsonElement.class));

        JsonArray array = new JsonArray();
        array.add(jsonObject);
        return array;
    }
    public static JsonArray getHitsJsonForVMMigrateSuccess() {
        Gson gson = new Gson();
        JsonObject jsonObject = new JsonObject();
        jsonObject.add("_source", gson.fromJson("{\n" +
                "          \"_cloudType\": \"GCP\",\n" +
                "          \"region\": \"us-west4-b\",\n" +
                "          \"id\": \"4705952002456105765\",\n" +
                "          \"projectName\": \"my-project-22-354616\",\n" +
                "          \"name\": \"paladin-instance-1\",\n" +
                "          \"description\": \"\",\n" +
                "          \"disks\": [\n" +
                "            {\n" +
                "              \"_cloudType\": \"GCP\",\n" +
                "              \"region\": null,\n" +
                "              \"id\": \"0\",\n" +
                "              \"projectName\": \"my-project-22-354616\",\n" +
                "              \"name\": \"paladin-instance-1\",\n" +
                "              \"sizeInGB\": 10,\n" +
                "              \"type\": \"PERSISTENT\",\n" +
                "              \"hasSha256\": false,\n" +
                "              \"hasKmsKeyName\": false,\n" +
                "              \"discoverydate\": null\n" +
                "            }\n" +
                "          ],\n" +
                "          \"tags\": {\n" +
                "            \"application\": \"paladincloud\",\n" +
                "            \"enviornment\": \"gcp-demo\"\n" +
                "          },\n" +
                "          \"machineType\": \"https://www.googleapis.com/compute/v1/projects/my-project-22-354616/zones/us-west4-b/machineTypes/e2-micro\",\n" +
                "          \"status\": \"TERMINATED\",\n" +
                "          \"networkInterfaces\": [\n" +
                "            {\n" +
                "              \"_cloudType\": \"GCP\",\n" +
                "              \"region\": null,\n" +
                "              \"id\": \"nic0\",\n" +
                "              \"projectName\": null,\n" +
                "              \"name\": \"nic0\",\n" +
                "              \"accessConfigs\": [\n" +
                "                {\n" +
                "                  \"_cloudType\": \"GCP\",\n" +
                "                  \"region\": null,\n" +
                "                  \"id\": \"External NAT\",\n" +
                "                  \"projectName\": \"my-project-22-354616\",\n" +
                "                  \"name\": \"External NAT\",\n" +
                "                  \"natIP\": \"\",\n" +
                "                  \"discoverydate\": null\n" +
                "                }\n" +
                "              ],\n" +
                "              \"network\": \"https://www.googleapis.com/compute/v1/projects/my-project-22-354616/global/networks/default\",\n" +
                "              \"discoverydate\": null\n" +
                "            }\n" +
                "          ],\n" +
                "          \"items\": [],\n" +
                "          \"onHostMaintainence\": \"MIGRATE\",\n" +
                "          \"discoverydate\": \"2022-08-02 10:00:00+0000\",\n" +
                "          \"_resourceid\": \"4705952002456105765\",\n" +
                "          \"_docid\": \"4705952002456105765\",\n" +
                "          \"_entity\": \"true\",\n" +
                "          \"_entitytype\": \"vminstance\",\n" +
                "          \"firstdiscoveredon\": \"2022-07-20 15:00:00+0000\",\n" +
                "          \"latest\": true,\n" +
                "          \"_loaddate\": \"2022-08-02 11:03:00+0000\"\n" +
                "        }", JsonElement.class));

        JsonArray array = new JsonArray();
        array.add(jsonObject);
        return array;
    }

    public static JsonArray getHitsJsonForGKEClusterSuccess() {
        Gson gson = new Gson();
        JsonObject jsonObject = new JsonObject();
        jsonObject.add("_source", gson.fromJson(
                "{\"_cloudType\":\"gcp\",\"region\":null,\"id\":\"test123\",\"projectName\":\"cool-bay-349411\",\"tableId\":\"test123\",\"description\":null,\"friendlyName\":null,\"generatedId\":\"cool-bay-349411:tesing_dataset.test123\",\"labels\":{},\"masterAuthorizedNetworksConfig\":{\"enable\":\"true\"},\"dataSetId\":\"tesing_dataset\",\"iamResourceName\":\"projects/cool-bay-349411/datasets/tesing_dataset/tables/test123\",\"expirationTime\":null,\"creationTime\":1655789087444,\"lastModifiedTime\":null,\"etag\":null,\"requirePartitionFilter\":false,\"discoverydate\":\"2022-06-23 10:00:00+0000\",\"_resourceid\":\"test123\",\"_docid\":\"test123\",\"_entity\":\"true\",\"_entitytype\":\"bigquerytable\",\"firstdiscoveredon\":\"2022-06-23 10:00:00+0000\",\"latest\":true,\"_loaddate\":\"2022-06-23 10:59:00+0000\"}\n",
                JsonElement.class));
        JsonArray array = new JsonArray();
        array.add(jsonObject);
        return array;
    }

    public static JsonArray getHitsJsonForGKEClusterFailure() {
        Gson gson = new Gson();
        JsonObject jsonObject = new JsonObject();
        jsonObject.add("_source", gson.fromJson(
                "{\"_cloudType\":\"gcp\",\"region\":null,\"id\":\"test123\",\"projectName\":\"cool-bay-349411\",\"tableId\":\"test123\",\"description\":null,\"friendlyName\":null,\"generatedId\":\"cool-bay-349411:tesing_dataset.test123\",\"labels\":{},\"masterAuthorizedNetworksConfig\":{},\"dataSetId\":\"tesing_dataset\",\"iamResourceName\":\"projects/cool-bay-349411/datasets/tesing_dataset/tables/test123\",\"expirationTime\":null,\"creationTime\":1655789087444,\"lastModifiedTime\":null,\"etag\":null,\"requirePartitionFilter\":false,\"discoverydate\":\"2022-06-23 10:00:00+0000\",\"_resourceid\":\"test123\",\"_docid\":\"test123\",\"_entity\":\"true\",\"_entitytype\":\"bigquerytable\",\"firstdiscoveredon\":\"2022-06-23 10:00:00+0000\",\"latest\":true,\"_loaddate\":\"2022-06-23 10:59:00+0000\"}\n",
                JsonElement.class));
        JsonArray array = new JsonArray();
        array.add(jsonObject);
        return array;
    }
    public static JsonArray getHitjsonArrayForDBOwnerFlagChanging(){
        Gson gson = new Gson();
        JsonObject jsonObject = new JsonObject();
        jsonObject.add("_source", gson.fromJson(
                "{\"_cloudType\":\"gcp\",\"region\":\"us-central1\",\"id\":\"cool-bay-349411:us-central1:gcp-mysql-sever1\",\"projectName\":\"cool-bay-349411\",\"name\":\"gcp-mysql-sever1\",\"kind\":\"sql#instance\",\"createdTime\":\"2022-08-11T08:17:19.922Z\",\"masterInstanceName\":null,\"backendType\":\"SECOND_GEN\",\"state\":\"RUNNABLE\",\"databaseVersion\":\"SQLSERVER_2019_STANDARD\",\"databaseInstalledVersion\":\"SQLSERVER_2019_STANDARD_CU16\",\"instanceType\":\"CLOUD_SQL_INSTANCE\",\"eTag\":\"7d608164c64713163c83a83f5170b9af2e642287919b366faf54d605337abc6c\",\"selfLink\":\"https://sqladmin.googleapis.com/v1/projects/cool-bay-349411/instances/gcp-mysql-sever1\",\"serviceAccountEmail\":\"p47822473470-cpwrh9@gcp-sa-cloud-sql.iam.gserviceaccount.com\",\"kmsKeyVersion\":null,\"kmsKeyName\":null,\"maxDiskSize\":null,\"currentDiskSize\":null,\"ipAddress\":[{\"ip\":\"34.171.119.180\",\"type\":\"PRIMARY\"}],\"serverCaCert\":{\"certSerialNumber\":\"0\",\"commonName\":\"C=US,O=Google\\\\,Inc,CN=GoogleCloudSQLServerCA,dnQualifier=2149545f-4fd6-4ce3-9ab8-0ec282e3cd71\",\"createTime\":\"2022-08-11T08:18:03.671Z\",\"expirationTime\":\"2032-08-08T08:19:03.671Z\",\"instance\":\"gcp-mysql-sever1\",\"kind\":\"sql#sslCert\"},\"settings\":{\"activationPolicy\":\"ALWAYS\",\"activeDirectoryConfig\":{\"kind\":\"sql#activeDirectoryConfig\"},\"authorizedGaeApplications\":[],\"availabilityType\":\"ZONAL\",\"backupConfiguration\":{\"backupRetentionSettings\":{\"retainedBackups\":7,\"retentionUnit\":\"COUNT\"},\"enabled\":true,\"kind\":\"sql#backupConfiguration\",\"location\":\"us\",\"startTime\":\"10:00\",\"transactionLogRetentionDays\":7},\"collation\":\"SQL_Latin1_General_CP1_CI_AS\",\"dataDiskSizeGb\":100,\"dataDiskType\":\"PD_SSD\",\"databaseFlags\":[{\"name\":\"cross db ownership chaining\",\"value\":\"off\"}],\"ipConfiguration\":{\"authorizedNetworks\":[],\"ipv4Enabled\":true},\"kind\":\"sql#settings\",\"locationPreference\":{\"kind\":\"sql#locationPreference\",\"zone\":\"us-central1-b\"},\"maintenanceWindow\":{\"day\":0,\"hour\":0,\"kind\":\"sql#maintenanceWindow\",\"updateTrack\":\"stable\"},\"pricingPlan\":\"PER_USE\",\"replicationType\":\"SYNCHRONOUS\",\"settingsVersion\":1,\"sqlServerAuditConfig\":{\"kind\":\"sql#sqlServerAuditConfig\",\"retentionInterval\":\"0s\",\"uploadInterval\":\"0s\"},\"storageAutoResize\":true,\"storageAutoResizeLimit\":0,\"tier\":\"db-custom-2-8192\",\"deletionProtectionEnabled\":true},\"dataBaseFlags\":null,\"discoverydate\":\"2022-08-1112:00:00+0000\",\"_resourceid\":\"cool-bay-349411:us-central1:gcp-mysql-sever1\",\"_docid\":\"cool-bay-349411:us-central1:gcp-mysql-sever1\",\"_entity\":\"true\",\"_entitytype\":\"cloudsql\",\"firstdiscoveredon\":\"2022-08-1108:00:00+0000\",\"latest\":true,\"_loaddate\":\"2022-08-1112:41:00+0000\"}",
                JsonElement.class));
        JsonArray array = new JsonArray();
        array.add(jsonObject);
        return array;
    }
    public static JsonArray getFailurejsonArrayForDBOwnerFlagChanging(){
        Gson gson = new Gson();
        JsonObject jsonObject = new JsonObject();
        jsonObject.add("_source", gson.fromJson(
                "{\"_cloudType\":\"gcp\",\"region\":\"us-central1\",\"id\":\"cool-bay-349411:us-central1:gcp-mysql-sever1\",\"projectName\":\"cool-bay-349411\",\"name\":\"gcp-mysql-sever1\",\"kind\":\"sql#instance\",\"createdTime\":\"2022-08-11T08:17:19.922Z\",\"masterInstanceName\":null,\"backendType\":\"SECOND_GEN\",\"state\":\"RUNNABLE\",\"databaseVersion\":\"SQLSERVER_2019_STANDARD\",\"databaseInstalledVersion\":\"SQLSERVER_2019_STANDARD_CU16\",\"instanceType\":\"CLOUD_SQL_INSTANCE\",\"eTag\":\"7d608164c64713163c83a83f5170b9af2e642287919b366faf54d605337abc6c\",\"selfLink\":\"https://sqladmin.googleapis.com/v1/projects/cool-bay-349411/instances/gcp-mysql-sever1\",\"serviceAccountEmail\":\"p47822473470-cpwrh9@gcp-sa-cloud-sql.iam.gserviceaccount.com\",\"kmsKeyVersion\":null,\"kmsKeyName\":null,\"maxDiskSize\":null,\"currentDiskSize\":null,\"ipAddress\":[{\"ip\":\"34.171.119.180\",\"type\":\"PRIMARY\"}],\"serverCaCert\":{\"certSerialNumber\":\"0\",\"commonName\":\"C=US,O=Google\\\\,Inc,CN=GoogleCloudSQLServerCA,dnQualifier=2149545f-4fd6-4ce3-9ab8-0ec282e3cd71\",\"createTime\":\"2022-08-11T08:18:03.671Z\",\"expirationTime\":\"2032-08-08T08:19:03.671Z\",\"instance\":\"gcp-mysql-sever1\",\"kind\":\"sql#sslCert\"},\"settings\":{\"activationPolicy\":\"ALWAYS\",\"activeDirectoryConfig\":{\"kind\":\"sql#activeDirectoryConfig\"},\"authorizedGaeApplications\":[],\"availabilityType\":\"ZONAL\",\"backupConfiguration\":{\"backupRetentionSettings\":{\"retainedBackups\":7,\"retentionUnit\":\"COUNT\"},\"enabled\":true,\"kind\":\"sql#backupConfiguration\",\"location\":\"us\",\"startTime\":\"10:00\",\"transactionLogRetentionDays\":7},\"collation\":\"SQL_Latin1_General_CP1_CI_AS\",\"dataDiskSizeGb\":100,\"dataDiskType\":\"PD_SSD\",\"databaseFlags\":[{\"name\":\"cross db ownership chaining\",\"value\":\"on\"}],\"ipConfiguration\":{\"authorizedNetworks\":[],\"ipv4Enabled\":true},\"kind\":\"sql#settings\",\"locationPreference\":{\"kind\":\"sql#locationPreference\",\"zone\":\"us-central1-b\"},\"maintenanceWindow\":{\"day\":0,\"hour\":0,\"kind\":\"sql#maintenanceWindow\",\"updateTrack\":\"stable\"},\"pricingPlan\":\"PER_USE\",\"replicationType\":\"SYNCHRONOUS\",\"settingsVersion\":1,\"sqlServerAuditConfig\":{\"kind\":\"sql#sqlServerAuditConfig\",\"retentionInterval\":\"0s\",\"uploadInterval\":\"0s\"},\"storageAutoResize\":true,\"storageAutoResizeLimit\":0,\"tier\":\"db-custom-2-8192\",\"deletionProtectionEnabled\":true},\"dataBaseFlags\":null,\"discoverydate\":\"2022-08-1112:00:00+0000\",\"_resourceid\":\"cool-bay-349411:us-central1:gcp-mysql-sever1\",\"_docid\":\"cool-bay-349411:us-central1:gcp-mysql-sever1\",\"_entity\":\"true\",\"_entitytype\":\"cloudsql\",\"firstdiscoveredon\":\"2022-08-1108:00:00+0000\",\"latest\":true,\"_loaddate\":\"2022-08-1112:41:00+0000\"}",
                JsonElement.class));
        JsonArray array = new JsonArray();
        array.add(jsonObject);
        return array;
    }

    public static JsonArray getHitsJsonArrayForAutomatedBackupSuccess() {
        Gson gson = new Gson();
        JsonObject jsonObject = new JsonObject();
        jsonObject.add("_source", gson.fromJson("{\n" +
                "          \"_cloudType\": \"GCP\",\n" +
                "          \"region\": \"us-west1-a\",\n" +
                "          \"id\": \"staging.cool-bay-349411.appspot.com\",\n" +
                "          \"projectName\": \"cool-bay-349411\",\n" +
                "          \"name\": \"pacbot-demo-vm\",\n" +
                "          \"users\": [\n" +
                "              \"project-owners-47822473470\",\n" +
                "              \"project-viewers-47822473470\" \n" +
                "          ],\n" +
                "          \"defaultKmsKeyName\": \"projects/cool-bay-349411/locations/us/keyRings/cool-bay-349411-key-ring/cryptoKeys/cool-bay-bigquery-cmk\",\n"+
                "          \"backupConfiguration\":\"true\",\n"
                +
                "          \"discoverydate\": \"2022-06-16 06:00:00+0000\",\n" +
                "          \"_resourceid\": \"8993151141438601059\",\n" +
                "          \"_docid\": \"8993151141438601059\",\n" +
                "          \"_entity\": \"true\",\n" +
                "          \"_entitytype\": \"vminstance\",\n" +
                "          \"firstdiscoveredon\": \"2022-06-14 10:00:00+0000\",\n" +
                "          \"latest\": true,\n" +
                "          \"_loaddate\": \"2022-06-16 06:12:00+0000\"\n" +
                "        }", JsonElement.class));

        JsonArray array = new JsonArray();
        array.add(jsonObject);
        return array;
    }

    public static JsonArray getHitsJsonArrayForAutomatedBackupFailure() {
        Gson gson = new Gson();
        JsonObject jsonObject = new JsonObject();
        jsonObject.add("_source", gson.fromJson("{\n" +
                "          \"_cloudType\": \"GCP\",\n" +
                "          \"region\": \"us-west1-a\",\n" +
                "          \"id\": \"staging.cool-bay-349411.appspot.com\",\n" +
                "          \"projectName\": \"cool-bay-349411\",\n" +
                "          \"name\": \"pacbot-demo-vm\",\n" +
                "          \"users\": [\n" +
                "              \"allAuthenticatedUsers\",\n" +
                "              \"allUsers\" \n" +
                "          ],\n" +
                "          \"defaultKmsKeyName\": null,\n" +
                "          \"backupConfiguration\":\"true\",\n" +
                "          \"discoverydate\": \"2022-06-16 06:00:00+0000\",\n" +
                "          \"_resourceid\": \"8993151141438601059\",\n" +
                "          \"_docid\": \"8993151141438601059\",\n" +
                "          \"_entity\": \"true\",\n" +
                "          \"_entitytype\": \"vminstance\",\n" +
                "          \"firstdiscoveredon\": \"2022-06-14 10:00:00+0000\",\n" +
                "          \"latest\": true,\n" +
                "          \"_loaddate\": \"2022-06-16 06:12:00+0000\"\n" +
                "        }", JsonElement.class));
        JsonArray array = new JsonArray();
        array.add(jsonObject);
        return array;

    }
    public static JsonArray getHitsJsonArrayForCloudDNSSecstateRuleFailure() {
        Gson gson = new Gson();
        JsonObject jsonObject = new JsonObject();
        jsonObject.add("_source", gson.fromJson("{\n" +
                "          \"_cloudType\": \"GCP\",\n" +
                "          \"region\": \"us-west1-a\",\n" +
                "          \"id\": \"staging.cool-bay-349411.appspot.com\",\n" +
                "          \"projectName\": \"cool-bay-349411\",\n" +
                "          \"name\": \"pacbot-demo-vm\",\n" +
                "          \"dnsSecConfigState\": \"OFF\",\n" +
                "          \"discoverydate\": \"2022-06-16 06:00:00+0000\",\n" +
                "          \"_resourceid\": \"8993151141438601059\",\n" +
                "          \"_docid\": \"8993151141438601059\",\n" +
                "          \"_entity\": \"true\",\n" +
                "          \"_entitytype\": \"vminstance\",\n" +
                "          \"firstdiscoveredon\": \"2022-06-14 10:00:00+0000\",\n" +
                "          \"latest\": true,\n" +
                "          \"_loaddate\": \"2022-06-16 06:12:00+0000\"\n" +
                "        }", JsonElement.class));

        JsonArray array = new JsonArray();
        array.add(jsonObject);
        return array;
    }
    public static JsonArray getHitsJsonArrayForCloudDNSSecstateRuleSucess() {
        Gson gson = new Gson();
        JsonObject jsonObject = new JsonObject();
        jsonObject.add("_source", gson.fromJson("{\n" +
                "          \"_cloudType\": \"GCP\",\n" +
                "          \"region\": \"us-west1-a\",\n" +
                "          \"id\": \"staging.cool-bay-349411.appspot.com\",\n" +
                "          \"projectName\": \"cool-bay-349411\",\n" +
                "          \"name\": \"pacbot-demo-vm\",\n" +
                "          \"dnsSecConfigState\": \"ON\",\n" +
                "          \"discoverydate\": \"2022-06-16 06:00:00+0000\",\n" +
                "          \"_resourceid\": \"8993151141438601059\",\n" +
                "          \"_docid\": \"8993151141438601059\",\n" +
                "          \"_entity\": \"true\",\n" +
                "          \"_entitytype\": \"vminstance\",\n" +
                "          \"firstdiscoveredon\": \"2022-06-14 10:00:00+0000\",\n" +
                "          \"latest\": true,\n" +
                "          \"_loaddate\": \"2022-06-16 06:12:00+0000\"\n" +
                "        }", JsonElement.class));

        JsonArray array = new JsonArray();
        array.add(jsonObject);
        return array;
    }
    public static JsonArray getFailurejsonArrayForExternalScriptsEnabled(){
        Gson gson = new Gson();
        JsonObject jsonObject = new JsonObject();
        jsonObject.add("_source", gson.fromJson(
                "{\"_cloudType\":\"gcp\",\"region\":\"us-central1\",\"id\":\"cool-bay-349411:us-central1:gcp-mysql-sever1\",\"projectName\":\"cool-bay-349411\",\"name\":\"gcp-mysql-sever1\",\"kind\":\"sql#instance\",\"createdTime\":\"2022-08-11T08:17:19.922Z\",\"masterInstanceName\":null,\"backendType\":\"SECOND_GEN\",\"state\":\"RUNNABLE\",\"databaseVersion\":\"SQLSERVER_2019_STANDARD\",\"databaseInstalledVersion\":\"SQLSERVER_2019_STANDARD_CU16\",\"instanceType\":\"CLOUD_SQL_INSTANCE\",\"eTag\":\"7d608164c64713163c83a83f5170b9af2e642287919b366faf54d605337abc6c\",\"selfLink\":\"https://sqladmin.googleapis.com/v1/projects/cool-bay-349411/instances/gcp-mysql-sever1\",\"serviceAccountEmail\":\"p47822473470-cpwrh9@gcp-sa-cloud-sql.iam.gserviceaccount.com\",\"kmsKeyVersion\":null,\"kmsKeyName\":null,\"maxDiskSize\":null,\"currentDiskSize\":null,\"ipAddress\":[{\"ip\":\"34.171.119.180\",\"type\":\"PRIMARY\"}],\"serverCaCert\":{\"certSerialNumber\":\"0\",\"commonName\":\"C=US,O=Google\\\\,Inc,CN=GoogleCloudSQLServerCA,dnQualifier=2149545f-4fd6-4ce3-9ab8-0ec282e3cd71\",\"createTime\":\"2022-08-11T08:18:03.671Z\",\"expirationTime\":\"2032-08-08T08:19:03.671Z\",\"instance\":\"gcp-mysql-sever1\",\"kind\":\"sql#sslCert\"},\"settings\":{\"activationPolicy\":\"ALWAYS\",\"activeDirectoryConfig\":{\"kind\":\"sql#activeDirectoryConfig\"},\"authorizedGaeApplications\":[],\"availabilityType\":\"ZONAL\",\"backupConfiguration\":{\"backupRetentionSettings\":{\"retainedBackups\":7,\"retentionUnit\":\"COUNT\"},\"enabled\":true,\"kind\":\"sql#backupConfiguration\",\"location\":\"us\",\"startTime\":\"10:00\",\"transactionLogRetentionDays\":7},\"collation\":\"SQL_Latin1_General_CP1_CI_AS\",\"dataDiskSizeGb\":100,\"dataDiskType\":\"PD_SSD\",\"databaseFlags\":[{\"name\":\"external scripts enabled\",\"value\":\"on\"}],\"ipConfiguration\":{\"authorizedNetworks\":[],\"ipv4Enabled\":true},\"kind\":\"sql#settings\",\"locationPreference\":{\"kind\":\"sql#locationPreference\",\"zone\":\"us-central1-b\"},\"maintenanceWindow\":{\"day\":0,\"hour\":0,\"kind\":\"sql#maintenanceWindow\",\"updateTrack\":\"stable\"},\"pricingPlan\":\"PER_USE\",\"replicationType\":\"SYNCHRONOUS\",\"settingsVersion\":1,\"sqlServerAuditConfig\":{\"kind\":\"sql#sqlServerAuditConfig\",\"retentionInterval\":\"0s\",\"uploadInterval\":\"0s\"},\"storageAutoResize\":true,\"storageAutoResizeLimit\":0,\"tier\":\"db-custom-2-8192\",\"deletionProtectionEnabled\":true},\"dataBaseFlags\":null,\"discoverydate\":\"2022-08-1112:00:00+0000\",\"_resourceid\":\"cool-bay-349411:us-central1:gcp-mysql-sever1\",\"_docid\":\"cool-bay-349411:us-central1:gcp-mysql-sever1\",\"_entity\":\"true\",\"_entitytype\":\"cloudsql\",\"firstdiscoveredon\":\"2022-08-1108:00:00+0000\",\"latest\":true,\"_loaddate\":\"2022-08-1112:41:00+0000\"}",
                JsonElement.class));
        JsonArray array = new JsonArray();
        array.add(jsonObject);
        return array;
    }
    public static JsonArray getHitjsonArrayForExternalScriptsEnabled(){
        Gson gson = new Gson();
        JsonObject jsonObject = new JsonObject();
        jsonObject.add("_source", gson.fromJson(
                "{\"_cloudType\":\"gcp\",\"region\":\"us-central1\",\"id\":\"cool-bay-349411:us-central1:gcp-mysql-sever1\",\"projectName\":\"cool-bay-349411\",\"name\":\"gcp-mysql-sever1\",\"kind\":\"sql#instance\",\"createdTime\":\"2022-08-11T08:17:19.922Z\",\"masterInstanceName\":null,\"backendType\":\"SECOND_GEN\",\"state\":\"RUNNABLE\",\"databaseVersion\":\"SQLSERVER_2019_STANDARD\",\"databaseInstalledVersion\":\"SQLSERVER_2019_STANDARD_CU16\",\"instanceType\":\"CLOUD_SQL_INSTANCE\",\"eTag\":\"7d608164c64713163c83a83f5170b9af2e642287919b366faf54d605337abc6c\",\"selfLink\":\"https://sqladmin.googleapis.com/v1/projects/cool-bay-349411/instances/gcp-mysql-sever1\",\"serviceAccountEmail\":\"p47822473470-cpwrh9@gcp-sa-cloud-sql.iam.gserviceaccount.com\",\"kmsKeyVersion\":null,\"kmsKeyName\":null,\"maxDiskSize\":null,\"currentDiskSize\":null,\"ipAddress\":[{\"ip\":\"34.171.119.180\",\"type\":\"PRIMARY\"}],\"serverCaCert\":{\"certSerialNumber\":\"0\",\"commonName\":\"C=US,O=Google\\\\,Inc,CN=GoogleCloudSQLServerCA,dnQualifier=2149545f-4fd6-4ce3-9ab8-0ec282e3cd71\",\"createTime\":\"2022-08-11T08:18:03.671Z\",\"expirationTime\":\"2032-08-08T08:19:03.671Z\",\"instance\":\"gcp-mysql-sever1\",\"kind\":\"sql#sslCert\"},\"settings\":{\"activationPolicy\":\"ALWAYS\",\"activeDirectoryConfig\":{\"kind\":\"sql#activeDirectoryConfig\"},\"authorizedGaeApplications\":[],\"availabilityType\":\"ZONAL\",\"backupConfiguration\":{\"backupRetentionSettings\":{\"retainedBackups\":7,\"retentionUnit\":\"COUNT\"},\"enabled\":true,\"kind\":\"sql#backupConfiguration\",\"location\":\"us\",\"startTime\":\"10:00\",\"transactionLogRetentionDays\":7},\"collation\":\"SQL_Latin1_General_CP1_CI_AS\",\"dataDiskSizeGb\":100,\"dataDiskType\":\"PD_SSD\",\"databaseFlags\":[{\"name\":\"external scripts enabled\",\"value\":\"off\"}],\"ipConfiguration\":{\"authorizedNetworks\":[],\"ipv4Enabled\":true},\"kind\":\"sql#settings\",\"locationPreference\":{\"kind\":\"sql#locationPreference\",\"zone\":\"us-central1-b\"},\"maintenanceWindow\":{\"day\":0,\"hour\":0,\"kind\":\"sql#maintenanceWindow\",\"updateTrack\":\"stable\"},\"pricingPlan\":\"PER_USE\",\"replicationType\":\"SYNCHRONOUS\",\"settingsVersion\":1,\"sqlServerAuditConfig\":{\"kind\":\"sql#sqlServerAuditConfig\",\"retentionInterval\":\"0s\",\"uploadInterval\":\"0s\"},\"storageAutoResize\":true,\"storageAutoResizeLimit\":0,\"tier\":\"db-custom-2-8192\",\"deletionProtectionEnabled\":true},\"dataBaseFlags\":null,\"discoverydate\":\"2022-08-1112:00:00+0000\",\"_resourceid\":\"cool-bay-349411:us-central1:gcp-mysql-sever1\",\"_docid\":\"cool-bay-349411:us-central1:gcp-mysql-sever1\",\"_entity\":\"true\",\"_entitytype\":\"cloudsql\",\"firstdiscoveredon\":\"2022-08-1108:00:00+0000\",\"latest\":true,\"_loaddate\":\"2022-08-1112:41:00+0000\"}",
                JsonElement.class));
        JsonArray array = new JsonArray();
        array.add(jsonObject);
        return array;
    }

    public static JsonArray getHitjsonArrayForContainedDBAUTHFlag(){
        Gson gson = new Gson();
        JsonObject jsonObject = new JsonObject();
        jsonObject.add("_source", gson.fromJson(
                "{\"_cloudType\":\"gcp\",\"region\":\"us-central1\",\"id\":\"cool-bay-349411:us-central1:gcp-mysql-sever1\",\"projectName\":\"cool-bay-349411\",\"name\":\"gcp-mysql-sever1\",\"kind\":\"sql#instance\",\"createdTime\":\"2022-08-11T08:17:19.922Z\",\"masterInstanceName\":null,\"backendType\":\"SECOND_GEN\",\"state\":\"RUNNABLE\",\"databaseVersion\":\"SQLSERVER_2019_STANDARD\",\"databaseInstalledVersion\":\"SQLSERVER_2019_STANDARD_CU16\",\"instanceType\":\"CLOUD_SQL_INSTANCE\",\"eTag\":\"7d608164c64713163c83a83f5170b9af2e642287919b366faf54d605337abc6c\",\"selfLink\":\"https://sqladmin.googleapis.com/v1/projects/cool-bay-349411/instances/gcp-mysql-sever1\",\"serviceAccountEmail\":\"p47822473470-cpwrh9@gcp-sa-cloud-sql.iam.gserviceaccount.com\",\"kmsKeyVersion\":null,\"kmsKeyName\":null,\"maxDiskSize\":null,\"currentDiskSize\":null,\"ipAddress\":[{\"ip\":\"34.171.119.180\",\"type\":\"PRIMARY\"}],\"serverCaCert\":{\"certSerialNumber\":\"0\",\"commonName\":\"C=US,O=Google\\\\,Inc,CN=GoogleCloudSQLServerCA,dnQualifier=2149545f-4fd6-4ce3-9ab8-0ec282e3cd71\",\"createTime\":\"2022-08-11T08:18:03.671Z\",\"expirationTime\":\"2032-08-08T08:19:03.671Z\",\"instance\":\"gcp-mysql-sever1\",\"kind\":\"sql#sslCert\"},\"settings\":{\"activationPolicy\":\"ALWAYS\",\"activeDirectoryConfig\":{\"kind\":\"sql#activeDirectoryConfig\"},\"authorizedGaeApplications\":[],\"availabilityType\":\"ZONAL\",\"backupConfiguration\":{\"backupRetentionSettings\":{\"retainedBackups\":7,\"retentionUnit\":\"COUNT\"},\"enabled\":true,\"kind\":\"sql#backupConfiguration\",\"location\":\"us\",\"startTime\":\"10:00\",\"transactionLogRetentionDays\":7},\"collation\":\"SQL_Latin1_General_CP1_CI_AS\",\"dataDiskSizeGb\":100,\"dataDiskType\":\"PD_SSD\",\"databaseFlags\":[{\"name\":\"contained database authentication\",\"value\":\"off\"}],\"ipConfiguration\":{\"authorizedNetworks\":[],\"ipv4Enabled\":true},\"kind\":\"sql#settings\",\"locationPreference\":{\"kind\":\"sql#locationPreference\",\"zone\":\"us-central1-b\"},\"maintenanceWindow\":{\"day\":0,\"hour\":0,\"kind\":\"sql#maintenanceWindow\",\"updateTrack\":\"stable\"},\"pricingPlan\":\"PER_USE\",\"replicationType\":\"SYNCHRONOUS\",\"settingsVersion\":1,\"sqlServerAuditConfig\":{\"kind\":\"sql#sqlServerAuditConfig\",\"retentionInterval\":\"0s\",\"uploadInterval\":\"0s\"},\"storageAutoResize\":true,\"storageAutoResizeLimit\":0,\"tier\":\"db-custom-2-8192\",\"deletionProtectionEnabled\":true},\"dataBaseFlags\":null,\"discoverydate\":\"2022-08-1112:00:00+0000\",\"_resourceid\":\"cool-bay-349411:us-central1:gcp-mysql-sever1\",\"_docid\":\"cool-bay-349411:us-central1:gcp-mysql-sever1\",\"_entity\":\"true\",\"_entitytype\":\"cloudsql\",\"firstdiscoveredon\":\"2022-08-1108:00:00+0000\",\"latest\":true,\"_loaddate\":\"2022-08-1112:41:00+0000\"}",
                JsonElement.class));
        JsonArray array = new JsonArray();
        array.add(jsonObject);
        return array;
    }
    public static JsonArray getFailurejsonArrayForDForContainedDBAUTHFlag(){
        Gson gson = new Gson();
        JsonObject jsonObject = new JsonObject();
        jsonObject.add("_source", gson.fromJson(
                "{\"_cloudType\":\"gcp\",\"region\":\"us-central1\",\"id\":\"cool-bay-349411:us-central1:gcp-mysql-sever1\",\"projectName\":\"cool-bay-349411\",\"name\":\"gcp-mysql-sever1\",\"kind\":\"sql#instance\",\"createdTime\":\"2022-08-11T08:17:19.922Z\",\"masterInstanceName\":null,\"backendType\":\"SECOND_GEN\",\"state\":\"RUNNABLE\",\"databaseVersion\":\"SQLSERVER_2019_STANDARD\",\"databaseInstalledVersion\":\"SQLSERVER_2019_STANDARD_CU16\",\"instanceType\":\"CLOUD_SQL_INSTANCE\",\"eTag\":\"7d608164c64713163c83a83f5170b9af2e642287919b366faf54d605337abc6c\",\"selfLink\":\"https://sqladmin.googleapis.com/v1/projects/cool-bay-349411/instances/gcp-mysql-sever1\",\"serviceAccountEmail\":\"p47822473470-cpwrh9@gcp-sa-cloud-sql.iam.gserviceaccount.com\",\"kmsKeyVersion\":null,\"kmsKeyName\":null,\"maxDiskSize\":null,\"currentDiskSize\":null,\"ipAddress\":[{\"ip\":\"34.171.119.180\",\"type\":\"PRIMARY\"}],\"serverCaCert\":{\"certSerialNumber\":\"0\",\"commonName\":\"C=US,O=Google\\\\,Inc,CN=GoogleCloudSQLServerCA,dnQualifier=2149545f-4fd6-4ce3-9ab8-0ec282e3cd71\",\"createTime\":\"2022-08-11T08:18:03.671Z\",\"expirationTime\":\"2032-08-08T08:19:03.671Z\",\"instance\":\"gcp-mysql-sever1\",\"kind\":\"sql#sslCert\"},\"settings\":{\"activationPolicy\":\"ALWAYS\",\"activeDirectoryConfig\":{\"kind\":\"sql#activeDirectoryConfig\"},\"authorizedGaeApplications\":[],\"availabilityType\":\"ZONAL\",\"backupConfiguration\":{\"backupRetentionSettings\":{\"retainedBackups\":7,\"retentionUnit\":\"COUNT\"},\"enabled\":true,\"kind\":\"sql#backupConfiguration\",\"location\":\"us\",\"startTime\":\"10:00\",\"transactionLogRetentionDays\":7},\"collation\":\"SQL_Latin1_General_CP1_CI_AS\",\"dataDiskSizeGb\":100,\"dataDiskType\":\"PD_SSD\",\"databaseFlags\":[{\"name\":\"contained database authentication\",\"value\":\"on\"}],\"ipConfiguration\":{\"authorizedNetworks\":[],\"ipv4Enabled\":true},\"kind\":\"sql#settings\",\"locationPreference\":{\"kind\":\"sql#locationPreference\",\"zone\":\"us-central1-b\"},\"maintenanceWindow\":{\"day\":0,\"hour\":0,\"kind\":\"sql#maintenanceWindow\",\"updateTrack\":\"stable\"},\"pricingPlan\":\"PER_USE\",\"replicationType\":\"SYNCHRONOUS\",\"settingsVersion\":1,\"sqlServerAuditConfig\":{\"kind\":\"sql#sqlServerAuditConfig\",\"retentionInterval\":\"0s\",\"uploadInterval\":\"0s\"},\"storageAutoResize\":true,\"storageAutoResizeLimit\":0,\"tier\":\"db-custom-2-8192\",\"deletionProtectionEnabled\":true},\"dataBaseFlags\":null,\"discoverydate\":\"2022-08-1112:00:00+0000\",\"_resourceid\":\"cool-bay-349411:us-central1:gcp-mysql-sever1\",\"_docid\":\"cool-bay-349411:us-central1:gcp-mysql-sever1\",\"_entity\":\"true\",\"_entitytype\":\"cloudsql\",\"firstdiscoveredon\":\"2022-08-1108:00:00+0000\",\"latest\":true,\"_loaddate\":\"2022-08-1112:41:00+0000\"}",
                JsonElement.class));
        JsonArray array = new JsonArray();
        array.add(jsonObject);
        return array;
    }

    public static JsonArray getHitsJsonForSuccessDenyPublicIp(){
        Gson gson = new Gson();
        JsonObject jsonObject = new JsonObject();
        jsonObject.add("_source", gson.fromJson("{\n" +
                "          \"_cloudType\": \"gcp\",\n" +
                "          \"region\": \"us-central1\",\n" +
                "          \"id\": \"cool-bay-349411:us-central1:gcp-mysql-instance1\",\n" +
                "          \"projectName\": \"cool-bay-349411\",\n" +
                "          \"name\": \"gcp-mysql-instance1\",\n" +
                "          \"kind\": \"sql#instance\",\n" +
                "          \"createdTime\": \"2022-06-28T03:07:43.761Z\",\n" +
                "          \"masterInstanceName\": null,\n" +
                "          \"backendType\": \"SECOND_GEN\",\n" +
                "          \"state\": \"SUSPENDED\",\n" +
                "          \"databaseVersion\": \"MYSQL_8_0\",\n" +
                "          \"databaseInstalledVersion\": \"MYSQL_8_0_26\",\n" +
                "          \"instanceType\": \"CLOUD_SQL_INSTANCE\",\n" +
                "          \"eTag\": \"44c8f7e7d3b041fb1e4f8089c1558b6dcdf4de2e127d574a98b86452990c08c6\",\n" +
                "          \"selfLink\": \"https://sqladmin.googleapis.com/v1/projects/cool-bay-349411/instances/gcp-mysql-instance1\",\n" +
                "          \"serviceAccountEmail\": \"p47822473470-0s7wz4@gcp-sa-cloud-sql.iam.gserviceaccount.com\",\n" +
                "          \"kmsKeyVersion\": null,\n" +
                "          \"kmsKeyName\": null,\n" +
                "          \"maxDiskSize\": null,\n" +
                "          \"currentDiskSize\": null,\n" +
                "          \"ipAddress\": [\n" +
                "            {\n" +
                "              \"ip\": \"34.68.114.195\",\n" +
                "              \"type\": \"SEC\"\n" +
                "            }\n" +
                "          ],\n" +
                "          \"serverCaCert\": {\n" +
                "            \"certSerialNumber\": \"0\",\n" +
                "            \"commonName\": \"C=US,O=Google\\\\, Inc,CN=Google Cloud SQL Server CA,dnQualifier=39b1af3a-6bd5-4bcf-bd3c-b76ea723538b\",\n" +
                "            \"createTime\": \"2022-06-28T03:09:24.129Z\",\n" +
                "            \"expirationTime\": \"2032-06-25T03:10:24.129Z\",\n" +
                "            \"instance\": \"gcp-mysql-instance1\",\n" +
                "            \"kind\": \"sql#sslCert\"\n" +
                "          },\n" +
                "          \"settings\": {\n" +
                "            \"activationPolicy\": \"ALWAYS\",\n" +
                "            \"authorizedGaeApplications\": [],\n" +
                "            \"availabilityType\": \"ZONAL\",\n" +
                "            \"backupConfiguration\": {\n" +
                "              \"backupRetentionSettings\": {\n" +
                "                \"retainedBackups\": 7,\n" +
                "                \"retentionUnit\": \"COUNT\"\n" +
                "              },\n" +
                "              \"binaryLogEnabled\": true,\n" +
                "              \"enabled\": true,\n" +
                "              \"kind\": \"sql#backupConfiguration\",\n" +
                "              \"location\": \"us\",\n" +
                "              \"startTime\": \"03:00\",\n" +
                "              \"transactionLogRetentionDays\": 7\n" +
                "            },\n" +
                "            \"dataDiskSizeGb\": 100,\n" +
                "            \"dataDiskType\": \"PD_SSD\",\n" +
                "            \"ipConfiguration\": {\n" +
                "              \"authorizedNetworks\": [+\n" +
                "               {\n" +
                "                \"value\": \"10.50.51.5/32\",\n"  +
                "                \"kind\": \"sql#aclEntry\"\n" +
                "                 \"name\": \"private-ip\"\n"   +
                "                }\n" +
                "                                      ],\n" +
                "              \"ipv4Enabled\": true\n" +
                "            },\n" +
                "            \"kind\": \"sql#settings\",\n" +
                "            \"locationPreference\": {\n" +
                "              \"kind\": \"sql#locationPreference\",\n" +
                "              \"zone\": \"us-central1-b\"\n" +
                "            },\n" +
                "            \"maintenanceWindow\": {\n" +
                "              \"day\": 0,\n" +
                "              \"hour\": 0,\n" +
                "              \"kind\": \"sql#maintenanceWindow\",\n" +
                "              \"updateTrack\": \"stable\"\n" +
                "            },\n" +
                "            \"pricingPlan\": \"PER_USE\",\n" +
                "            \"replicationType\": \"SYNCHRONOUS\",\n" +
                "            \"settingsVersion\": 17,\n" +
                "            \"storageAutoResize\": true,\n" +
                "            \"storageAutoResizeLimit\": 0,\n" +
                "            \"tier\": \"db-custom-2-8192\",\n" +
                "            \"userLabels\": {\n" +
                "              \"environment\": \"demo\",\n" +
                "              \"application\": \"paladincloud\",\n" +
                "              \"created_by\": \"paladin\"\n" +
                "            }\n" +
                "          },\n" +
                "          \"discoverydate\": \"2022-08-16 11:00:00+0000\",\n" +
                "          \"_resourceid\": \"cool-bay-349411:us-central1:gcp-mysql-instance1\",\n" +
                "          \"_docid\": \"cool-bay-349411:us-central1:gcp-mysql-instance1\",\n" +
                "          \"_entity\": \"true\",\n" +
                "          \"_entitytype\": \"cloudsql\",\n" +
                "          \"firstdiscoveredon\": \"2022-07-20 15:00:00+0000\",\n" +
                "          \"latest\": true,\n" +
                "          \"_loaddate\": \"2022-08-16 12:22:00+0000\"\n" +
                "        }\n" , JsonElement.class));
        JsonArray array = new JsonArray();
        array.add(jsonObject);
        return array;}

    public static JsonArray getHitjsonArrayForRemoteAccessDBFlag(){
        Gson gson = new Gson();
        JsonObject jsonObject = new JsonObject();
        jsonObject.add("_source", gson.fromJson(
                "{\n" +
                        "          \"_cloudType\": \"gcp\",\n" +
                        "          \"region\": \"us-central1\",\n" +
                        "          \"id\": \"central-run-349616:us-central1:sql-server-instance1\",\n" +
                        "          \"projectName\": \"central-run-349616\",\n" +
                        "          \"name\": \"sql-server-instance1\",\n" +
                        "          \"kind\": \"sql#instance\",\n" +
                        "          \"createdTime\": \"2022-08-18T10:03:14.182Z\",\n" +
                        "          \"masterInstanceName\": null,\n" +
                        "          \"backendType\": \"SECOND_GEN\",\n" +
                        "          \"state\": \"RUNNABLE\",\n" +
                        "          \"databaseVersion\": \"SQLSERVER_2019_STANDARD\",\n" +
                        "          \"databaseInstalledVersion\": \"SQLSERVER_2019_STANDARD_CU16\",\n" +
                        "          \"instanceType\": \"CLOUD_SQL_INSTANCE\",\n" +
                        "          \"eTag\": \"9e2191b523db7ba21d3fea8c195ace6b0062f10a5e3ec23b01ed0737d8f46c67\",\n" +
                        "          \"selfLink\": \"https://sqladmin.googleapis.com/v1/projects/central-run-349616/instances/sql-server-instance1\",\n" +
                        "          \"serviceAccountEmail\": \"p344106022091-kdzpvj@gcp-sa-cloud-sql.iam.gserviceaccount.com\",\n" +
                        "          \"kmsKeyVersion\": null,\n" +
                        "          \"kmsKeyName\": null,\n" +
                        "          \"maxDiskSize\": null,\n" +
                        "          \"currentDiskSize\": null,\n" +
                        "          \"ipAddress\": [\n" +
                        "            {\n" +
                        "              \"ip\": \"34.135.103.201\",\n" +
                        "              \"type\": \"PRIMARY\"\n" +
                        "            }\n" +
                        "          ],\n" +
                        "          \"serverCaCert\": {\n" +
                        "            \"certSerialNumber\": \"0\",\n" +
                        "            \"commonName\": \"C=US,O=Google\\\\, Inc,CN=Google Cloud SQL Server CA,dnQualifier=92d054a8-75ed-4929-aa79-fc4aa7e03d41\",\n" +
                        "            \"createTime\": \"2022-08-18T10:05:17.124Z\",\n" +
                        "            \"expirationTime\": \"2032-08-15T10:06:17.124Z\",\n" +
                        "            \"instance\": \"sql-server-instance1\",\n" +
                        "            \"kind\": \"sql#sslCert\"\n" +
                        "          },\n" +
                        "          \"settings\": {\n" +
                        "            \"activationPolicy\": \"ALWAYS\",\n" +
                        "            \"activeDirectoryConfig\": {\n" +
                        "              \"kind\": \"sql#activeDirectoryConfig\"\n" +
                        "            },\n" +
                        "            \"authorizedGaeApplications\": [],\n" +
                        "            \"availabilityType\": \"ZONAL\",\n" +
                        "            \"backupConfiguration\": {\n" +
                        "              \"backupRetentionSettings\": {\n" +
                        "                \"retainedBackups\": 7,\n" +
                        "                \"retentionUnit\": \"COUNT\"\n" +
                        "              },\n" +
                        "              \"enabled\": true,\n" +
                        "              \"kind\": \"sql#backupConfiguration\",\n" +
                        "              \"location\": \"us\",\n" +
                        "              \"startTime\": \"11:00\",\n" +
                        "              \"transactionLogRetentionDays\": 7\n" +
                        "            },\n" +
                        "            \"collation\": \"SQL_Latin1_General_CP1_CI_AS\",\n" +
                        "            \"dataDiskSizeGb\": 100,\n" +
                        "            \"dataDiskType\": \"PD_SSD\",\n" +
                        "            \"databaseFlags\": [\n" +
                        "              {\n" +
                        "                \"name\": \"contained database authentication\",\n" +
                        "                \"value\": \"off\"\n" +
                        "              },\n" +
                        "              {\n" +
                        "                \"name\": \"external scripts enabled\",\n" +
                        "                \"value\": \"off\"\n" +
                        "              },\n" +
                        "              {\n" +
                        "                \"name\": \"remote access\",\n" +
                        "                \"value\": \"off\"\n" +
                        "              },\n" +
                        "              {\n" +
                        "                \"name\": \"cross db ownership chaining\",\n" +
                        "                \"value\": \"on\"\n" +
                        "              }\n" +
                        "            ],\n" +
                        "            \"ipConfiguration\": {\n" +
                        "              \"authorizedNetworks\": [],\n" +
                        "              \"ipv4Enabled\": true\n" +
                        "            },\n" +
                        "            \"kind\": \"sql#settings\",\n" +
                        "            \"locationPreference\": {\n" +
                        "              \"kind\": \"sql#locationPreference\",\n" +
                        "              \"zone\": \"us-central1-b\"\n" +
                        "            },\n" +
                        "            \"maintenanceWindow\": {\n" +
                        "              \"day\": 0,\n" +
                        "              \"hour\": 0,\n" +
                        "              \"kind\": \"sql#maintenanceWindow\",\n" +
                        "              \"updateTrack\": \"stable\"\n" +
                        "            },\n" +
                        "            \"pricingPlan\": \"PER_USE\",\n" +
                        "            \"replicationType\": \"SYNCHRONOUS\",\n" +
                        "            \"settingsVersion\": 5,\n" +
                        "            \"sqlServerAuditConfig\": {\n" +
                        "              \"kind\": \"sql#sqlServerAuditConfig\",\n" +
                        "              \"retentionInterval\": \"0s\",\n" +
                        "              \"uploadInterval\": \"0s\"\n" +
                        "            },\n" +
                        "            \"storageAutoResize\": true,\n" +
                        "            \"storageAutoResizeLimit\": 0,\n" +
                        "            \"tier\": \"db-custom-2-8192\",\n" +
                        "            \"connectorEnforcement\": \"NOT_REQUIRED\",\n" +
                        "            \"deletionProtectionEnabled\": true\n" +
                        "          },\n" +
                        "          \"backupEnabled\": true,\n" +
                        "          \"discoverydate\": \"2022-08-25 07:00:00+0000\",\n" +
                        "          \"_resourceid\": \"central-run-349616:us-central1:sql-server-instance1\",\n" +
                        "          \"_docid\": \"central-run-349616:us-central1:sql-server-instance1\",\n" +
                        "          \"_entity\": \"true\",\n" +
                        "          \"_entitytype\": \"cloudsql_sqlserver\",\n" +
                        "          \"firstdiscoveredon\": \"2022-08-25 07:00:00+0000\",\n" +
                        "          \"latest\": true,\n" +
                        "          \"_loaddate\": \"2022-08-25 08:27:00+0000\"\n" +
                        "        }",
                JsonElement.class));
        JsonArray array = new JsonArray();
        array.add(jsonObject);
        return array;
    }

    public static JsonArray getHitsJsonForFailureDenyPublicIp(){
        Gson gson = new Gson();
        JsonObject jsonObject = new JsonObject();
        jsonObject.add("_source", gson.fromJson("{\n" +
                "          \"_cloudType\": \"gcp\",\n" +
                "          \"region\": \"us-central1\",\n" +
                "          \"id\": \"cool-bay-349411:us-central1:gcp-mysql-instance1\",\n" +
                "          \"projectName\": \"cool-bay-349411\",\n" +
                "          \"name\": \"gcp-mysql-instance1\",\n" +
                "          \"kind\": \"sql#instance\",\n" +
                "          \"createdTime\": \"2022-06-28T03:07:43.761Z\",\n" +
                "          \"masterInstanceName\": null,\n" +
                "          \"backendType\": \"SECOND_GEN\",\n" +
                "          \"state\": \"SUSPENDED\",\n" +
                "          \"databaseVersion\": \"MYSQL_8_0\",\n" +
                "          \"databaseInstalledVersion\": \"MYSQL_8_0_26\",\n" +
                "          \"instanceType\": \"CLOUD_SQL_INSTANCE\",\n" +
                "          \"eTag\": \"44c8f7e7d3b041fb1e4f8089c1558b6dcdf4de2e127d574a98b86452990c08c6\",\n" +
                "          \"selfLink\": \"https://sqladmin.googleapis.com/v1/projects/cool-bay-349411/instances/gcp-mysql-instance1\",\n" +
                "          \"serviceAccountEmail\": \"p47822473470-0s7wz4@gcp-sa-cloud-sql.iam.gserviceaccount.com\",\n" +
                "          \"kmsKeyVersion\": null,\n" +
                "          \"kmsKeyName\": null,\n" +
                "          \"maxDiskSize\": null,\n" +
                "          \"currentDiskSize\": null,\n" +
                "          \"ipAddress\": [\n" +
                "            {\n" +
                "              \"ip\": \"34.68.114.195\",\n" +
                "              \"type\": \"SEC\"\n" +
                "            }\n" +
                "          ],\n" +
                "          \"serverCaCert\": {\n" +
                "            \"certSerialNumber\": \"0\",\n" +
                "            \"commonName\": \"C=US,O=Google\\\\, Inc,CN=Google Cloud SQL Server CA,dnQualifier=39b1af3a-6bd5-4bcf-bd3c-b76ea723538b\",\n" +
                "            \"createTime\": \"2022-06-28T03:09:24.129Z\",\n" +
                "            \"expirationTime\": \"2032-06-25T03:10:24.129Z\",\n" +
                "            \"instance\": \"gcp-mysql-instance1\",\n" +
                "            \"kind\": \"sql#sslCert\"\n" +
                "          },\n" +
                "          \"settings\": {\n" +
                "            \"activationPolicy\": \"ALWAYS\",\n" +
                "            \"authorizedGaeApplications\": [],\n" +
                "            \"availabilityType\": \"ZONAL\",\n" +
                "            \"backupConfiguration\": {\n" +
                "              \"backupRetentionSettings\": {\n" +
                "                \"retainedBackups\": 7,\n" +
                "                \"retentionUnit\": \"COUNT\"\n" +
                "              },\n" +
                "              \"binaryLogEnabled\": true,\n" +
                "              \"enabled\": true,\n" +
                "              \"kind\": \"sql#backupConfiguration\",\n" +
                "              \"location\": \"us\",\n" +
                "              \"startTime\": \"03:00\",\n" +
                "              \"transactionLogRetentionDays\": 7\n" +
                "            },\n" +
                "            \"dataDiskSizeGb\": 100,\n" +
                "            \"dataDiskType\": \"PD_SSD\",\n" +
                "            \"ipConfiguration\": {\n" +
                "              \"authorizedNetworks\": [+\n" +
                "               {\n" +
                "                \"value\": \"0.0.0.0/0\",\n"  +
                "                \"kind\": \"sql#aclEntry\"\n" +
                "                 \"name\": \"public-ip\"\n"   +
                "                }\n" +
                "                                      ],\n" +
                "              \"ipv4Enabled\": true\n" +
                "            },\n" +
                "            \"kind\": \"sql#settings\",\n" +
                "            \"locationPreference\": {\n" +
                "              \"kind\": \"sql#locationPreference\",\n" +
                "              \"zone\": \"us-central1-b\"\n" +
                "            },\n" +
                "            \"maintenanceWindow\": {\n" +
                "              \"day\": 0,\n" +
                "              \"hour\": 0,\n" +
                "              \"kind\": \"sql#maintenanceWindow\",\n" +
                "              \"updateTrack\": \"stable\"\n" +
                "            },\n" +
                "            \"pricingPlan\": \"PER_USE\",\n" +
                "            \"replicationType\": \"SYNCHRONOUS\",\n" +
                "            \"settingsVersion\": 17,\n" +
                "            \"storageAutoResize\": true,\n" +
                "            \"storageAutoResizeLimit\": 0,\n" +
                "            \"tier\": \"db-custom-2-8192\",\n" +
                "            \"userLabels\": {\n" +
                "              \"environment\": \"demo\",\n" +
                "              \"application\": \"paladincloud\",\n" +
                "              \"created_by\": \"paladin\"\n" +
                "            }\n" +
                "          },\n" +
                "          \"discoverydate\": \"2022-08-16 11:00:00+0000\",\n" +
                "          \"_resourceid\": \"cool-bay-349411:us-central1:gcp-mysql-instance1\",\n" +
                "          \"_docid\": \"cool-bay-349411:us-central1:gcp-mysql-instance1\",\n" +
                "          \"_entity\": \"true\",\n" +
                "          \"_entitytype\": \"cloudsql\",\n" +
                "          \"firstdiscoveredon\": \"2022-07-20 15:00:00+0000\",\n" +
                "          \"latest\": true,\n" +
                "          \"_loaddate\": \"2022-08-16 12:22:00+0000\"\n" +
                "        }\n" , JsonElement.class));
        JsonArray array = new JsonArray();
        array.add(jsonObject);
        return array;
    }

    public static JsonArray getFailurejsonArrayForRemoteAccessDBFlag(){
        Gson gson = new Gson();
        JsonObject jsonObject = new JsonObject();
        jsonObject.add("_source", gson.fromJson(
                "{\n" +
                        "          \"_cloudType\": \"gcp\",\n" +
                        "          \"region\": \"us-central1\",\n" +
                        "          \"id\": \"central-run-349616:us-central1:sql-server-instance1\",\n" +
                        "          \"projectName\": \"central-run-349616\",\n" +
                        "          \"name\": \"sql-server-instance1\",\n" +
                        "          \"kind\": \"sql#instance\",\n" +
                        "          \"createdTime\": \"2022-08-18T10:03:14.182Z\",\n" +
                        "          \"masterInstanceName\": null,\n" +
                        "          \"backendType\": \"SECOND_GEN\",\n" +
                        "          \"state\": \"RUNNABLE\",\n" +
                        "          \"databaseVersion\": \"SQLSERVER_2019_STANDARD\",\n" +
                        "          \"databaseInstalledVersion\": \"SQLSERVER_2019_STANDARD_CU16\",\n" +
                        "          \"instanceType\": \"CLOUD_SQL_INSTANCE\",\n" +
                        "          \"eTag\": \"9e2191b523db7ba21d3fea8c195ace6b0062f10a5e3ec23b01ed0737d8f46c67\",\n" +
                        "          \"selfLink\": \"https://sqladmin.googleapis.com/v1/projects/central-run-349616/instances/sql-server-instance1\",\n" +
                        "          \"serviceAccountEmail\": \"p344106022091-kdzpvj@gcp-sa-cloud-sql.iam.gserviceaccount.com\",\n" +
                        "          \"kmsKeyVersion\": null,\n" +
                        "          \"kmsKeyName\": null,\n" +
                        "          \"maxDiskSize\": null,\n" +
                        "          \"currentDiskSize\": null,\n" +
                        "          \"ipAddress\": [\n" +
                        "            {\n" +
                        "              \"ip\": \"34.135.103.201\",\n" +
                        "              \"type\": \"PRIMARY\"\n" +
                        "            }\n" +
                        "          ],\n" +
                        "          \"serverCaCert\": {\n" +
                        "            \"certSerialNumber\": \"0\",\n" +
                        "            \"commonName\": \"C=US,O=Google\\\\, Inc,CN=Google Cloud SQL Server CA,dnQualifier=92d054a8-75ed-4929-aa79-fc4aa7e03d41\",\n" +
                        "            \"createTime\": \"2022-08-18T10:05:17.124Z\",\n" +
                        "            \"expirationTime\": \"2032-08-15T10:06:17.124Z\",\n" +
                        "            \"instance\": \"sql-server-instance1\",\n" +
                        "            \"kind\": \"sql#sslCert\"\n" +
                        "          },\n" +
                        "          \"settings\": {\n" +
                        "            \"activationPolicy\": \"ALWAYS\",\n" +
                        "            \"activeDirectoryConfig\": {\n" +
                        "              \"kind\": \"sql#activeDirectoryConfig\"\n" +
                        "            },\n" +
                        "            \"authorizedGaeApplications\": [],\n" +
                        "            \"availabilityType\": \"ZONAL\",\n" +
                        "            \"backupConfiguration\": {\n" +
                        "              \"backupRetentionSettings\": {\n" +
                        "                \"retainedBackups\": 7,\n" +
                        "                \"retentionUnit\": \"COUNT\"\n" +
                        "              },\n" +
                        "              \"enabled\": true,\n" +
                        "              \"kind\": \"sql#backupConfiguration\",\n" +
                        "              \"location\": \"us\",\n" +
                        "              \"startTime\": \"11:00\",\n" +
                        "              \"transactionLogRetentionDays\": 7\n" +
                        "            },\n" +
                        "            \"collation\": \"SQL_Latin1_General_CP1_CI_AS\",\n" +
                        "            \"dataDiskSizeGb\": 100,\n" +
                        "            \"dataDiskType\": \"PD_SSD\",\n" +
                        "            \"databaseFlags\": [\n" +
                        "              {\n" +
                        "                \"name\": \"contained database authentication\",\n" +
                        "                \"value\": \"off\"\n" +
                        "              },\n" +
                        "              {\n" +
                        "                \"name\": \"external scripts enabled\",\n" +
                        "                \"value\": \"off\"\n" +
                        "              },\n" +
                        "              {\n" +
                        "                \"name\": \"remote access\",\n" +
                        "                \"value\": \"on\"\n" +
                        "              },\n" +
                        "              {\n" +
                        "                \"name\": \"cross db ownership chaining\",\n" +
                        "                \"value\": \"on\"\n" +
                        "              }\n" +
                        "            ],\n" +
                        "            \"ipConfiguration\": {\n" +
                        "              \"authorizedNetworks\": [],\n" +
                        "              \"ipv4Enabled\": true\n" +
                        "            },\n" +
                        "            \"kind\": \"sql#settings\",\n" +
                        "            \"locationPreference\": {\n" +
                        "              \"kind\": \"sql#locationPreference\",\n" +
                        "              \"zone\": \"us-central1-b\"\n" +
                        "            },\n" +
                        "            \"maintenanceWindow\": {\n" +
                        "              \"day\": 0,\n" +
                        "              \"hour\": 0,\n" +
                        "              \"kind\": \"sql#maintenanceWindow\",\n" +
                        "              \"updateTrack\": \"stable\"\n" +
                        "            },\n" +
                        "            \"pricingPlan\": \"PER_USE\",\n" +
                        "            \"replicationType\": \"SYNCHRONOUS\",\n" +
                        "            \"settingsVersion\": 5,\n" +
                        "            \"sqlServerAuditConfig\": {\n" +
                        "              \"kind\": \"sql#sqlServerAuditConfig\",\n" +
                        "              \"retentionInterval\": \"0s\",\n" +
                        "              \"uploadInterval\": \"0s\"\n" +
                        "            },\n" +
                        "            \"storageAutoResize\": true,\n" +
                        "            \"storageAutoResizeLimit\": 0,\n" +
                        "            \"tier\": \"db-custom-2-8192\",\n" +
                        "            \"connectorEnforcement\": \"NOT_REQUIRED\",\n" +
                        "            \"deletionProtectionEnabled\": true\n" +
                        "          },\n" +
                        "          \"backupEnabled\": true,\n" +
                        "          \"discoverydate\": \"2022-08-25 07:00:00+0000\",\n" +
                        "          \"_resourceid\": \"central-run-349616:us-central1:sql-server-instance1\",\n" +
                        "          \"_docid\": \"central-run-349616:us-central1:sql-server-instance1\",\n" +
                        "          \"_entity\": \"true\",\n" +
                        "          \"_entitytype\": \"cloudsql_sqlserver\",\n" +
                        "          \"firstdiscoveredon\": \"2022-08-25 07:00:00+0000\",\n" +
                        "          \"latest\": true,\n" +
                        "          \"_loaddate\": \"2022-08-25 08:27:00+0000\"\n" +
                        "        }",
                JsonElement.class));
        JsonArray array = new JsonArray();
        array.add(jsonObject);
        return array;
    }
}
