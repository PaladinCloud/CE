package com.tmobile.cloud.gcprules.VPCFirewall.Utils;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

public class TestUtils {
    public static JsonArray getHitsJsonArrayForVPCFIreWall() {
        Gson gson = new Gson();
        JsonObject jsonObject = new JsonObject();
        jsonObject.add("_source", gson.fromJson("{\n" +
                "          \"_cloudType\" : \"GCP\",\n" +
                "          \"region\" : null,\n" +
                "          \"id\" : \"4715970846916771047\",\n" +
                "          \"projectName\" : \"Paladin Cloud\",\n" +
                "          \"projectId\" : \"central-run-349616\",\n" +
                "          \"tags\" : null,\n" +
                "          \"name\" : \"default-allow-http\",\n" +
                "          \"direction\" : \"EGRESS\",\n" +
                "          \"sourceRanges\" : [\n" +
                "            \"0.0.0.0/0\"\n" +
                "          ],\n" +
                "          \"destinationRanges\" : [  \"0.0.0.0/0\" ],\n" +
                "          \"allow\" : [\n" +
                "            {\n" +
                "              \"protocol\" : \"tcp\",\n" +
                "              \"ports\" : [\n" +
                "                \"80\"\n" +
                "              ]\n" +
                "            }\n" +
                "          ],\n" +
                "          \"disabled\" : false,\n" +
                "          \"discoverydate\" : \"2023-04-27 07:00:00+0000\",\n" +
                "          \"_resourceid\" : \"default-allow-http\",\n" +
                "          \"_docid\" : \"4715970846916771047\",\n" +
                "          \"_entity\" : \"true\",\n" +
                "          \"_entitytype\" : \"vpcfirewall\",\n" +
                "          \"docType\" : \"vpcfirewall\",\n" +
                "          \"vpcfirewall_relations\" : \"vpcfirewall\",\n" +
                "          \"firstdiscoveredon\" : \"2023-04-27 07:00:00+0000\",\n" +
                "          \"latest\" : true,\n" +
                "          \"_loaddate\" : \"2023-04-27 07:16:00+0000\"\n" +
                "        }", JsonElement.class));

        JsonArray array = new JsonArray();
        array.add(jsonObject);
        return array;
    }

    public static JsonArray getHitsJsonArrayForVPCFIreWallFailure() {
        Gson gson = new Gson();
        JsonObject jsonObject = new JsonObject();
        jsonObject.add("_source", gson.fromJson("{\n" +
                "          \"_cloudType\" : \"GCP\",\n" +
                "          \"region\" : null,\n" +
                "          \"id\" : \"4715970846916771047\",\n" +
                "          \"projectName\" : \"Paladin Cloud\",\n" +
                "          \"projectId\" : \"central-run-349616\",\n" +
                "          \"tags\" : null,\n" +
                "          \"name\" : \"default-allow-http\",\n" +
                "          \"direction\" : \"INGRESS\",\n" +
                "          \"sourceRanges\" : [\n" +
                "            \"0.0.0.0/0\"\n" +
                "          ],\n" +
                "          \"destinationRanges\" : [ ],\n" +
                "          \"allow\" : [\n" +
                "            {\n" +
                "              \"protocol\" : \"tcp\",\n" +
                "              \"ports\" : [\n" +
                "                \"1521\"\n" +
                "              ]\n" +
                "            }\n" +
                "          ],\n" +
                "          \"disabled\" : false,\n" +
                "          \"discoverydate\" : \"2023-04-27 09:00:00+0000\",\n" +
                "          \"_resourceid\" : \"default-allow-http\",\n" +
                "          \"_docid\" : \"4715970846916771047\",\n" +
                "          \"_entity\" : \"true\",\n" +
                "          \"_entitytype\" : \"vpcfirewall\",\n" +
                "          \"docType\" : \"vpcfirewall\",\n" +
                "          \"vpcfirewall_relations\" : \"vpcfirewall\",\n" +
                "          \"firstdiscoveredon\" : \"2023-04-27 09:00:00+0000\",\n" +
                "          \"latest\" : true,\n" +
                "          \"_loaddate\" : \"2023-04-27 09:10:00+0000\"\n" +
                "        }", JsonElement.class));

        JsonArray array = new JsonArray();
        array.add(jsonObject);
        return array;
    }

}
