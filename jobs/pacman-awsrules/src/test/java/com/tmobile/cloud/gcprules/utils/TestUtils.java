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
                "          \"machineType\": \"https://www.googleapis.com/compute/v1/projects/cool-bay-349411/zones/us-west1-a/machineTypes/e2-medium\",\n" +
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
                "              \"network\": \"https://www.googleapis.com/compute/v1/projects/cool-bay-349411/global/networks/default\",\n" +
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
                "          \"machineType\": \"https://www.googleapis.com/compute/v1/projects/cool-bay-349411/zones/us-west1-a/machineTypes/e2-medium\",\n" +
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
                "              \"network\": \"https://www.googleapis.com/compute/v1/projects/cool-bay-349411/global/networks/default\",\n" +
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
}
