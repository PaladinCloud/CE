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
                "          \"_cloudType\": \"GCP\",\n" +
                "          \"region\": \"us-west1-a\",\n" +
                "          \"id\": \"8993151141438601059\",\n" +
                "          \"projectName\": \"cool-bay-349411\",\n" +
                "          \"name\": \"default-allow-http\",\n" +
                "          \"direction\": \"INGRESS\",\n" +
                "          \"sourceRanges\": [\n" +
                "            {\n" +
                "              \".0.0.0/0\n" +
                "            }\n" +
                "          ],\n" +
                "          \"allow\": [\n" +
                "            {\n" +
                "              \"protocol\": \"tcp\",\n" +
                "  \"ports\": [\n" +
                "            {\n" +
                "              \".80\n" +
                "            }\n" +
                "          ],\n" + "            }\n" +
                "          ],\n" +
                "          \"disabled\": \"false\",\n" +
                "              \"discoverydate\": null\n" +
                "            }\n" +
                "          ],\n" +
                "          \"discoverydate\": \"2022-06-17 00:00:00+0000\",\n" +
                "          \"_resourceid\": \"3078863054644366453\",\n" +
                "          \"_docid\": \"8993151141438601059\",\n" +
                "          \"_entity\": \"true\",\n" +
                "          \"_entitytype\": \"vpcfirewall\",\n" +
                "          \"firstdiscoveredon\": \"2022-06-16 13:00:00+0000\",\n" +
                "          \"latest\": true,\n" +
                "          \"_loaddate\": \"2022-06-17 00:12:00+0000\"\n" +
                "        }", JsonElement.class));

        JsonArray array = new JsonArray();
        array.add(jsonObject);
        return array;
    }

    public static JsonArray getHitsJsonArrayForVPCFIreWallFailure() {
        Gson gson = new Gson();
        JsonObject jsonObject = new JsonObject();
        jsonObject.add("_source", gson.fromJson("{\n" +
                "          \"_cloudType\": \"GCP\",\n" +
                "          \"region\": \"us-west1-a\",\n" +
                "          \"id\": \"8993151141438601059\",\n" +
                "          \"projectName\": \"cool-bay-349411\",\n" +
                "          \"name\": \"default-allow-http\",\n" +
                "          \"direction\": \"INGRESS\",\n" +
                "          \"allow\": [\n" +
                "            {\n" +
                "              \"protocol\": \"tcp\",\n" +
                "  \"ports\": [\n" +
                "            {\n" +
                "              \".80\n" +
                "            }\n" +
                "          ],\n" + "            }\n" +
                "          ],\n" +
                "          \"disabled\": \"false\",\n" +
                "              \"discoverydate\": null\n" +
                "            }\n" +
                "          ],\n" +
                "          \"discoverydate\": \"2022-06-17 00:00:00+0000\",\n" +
                "          \"_resourceid\": \"3078863054644366453\",\n" +
                "          \"_docid\": \"8993151141438601059\",\n" +
                "          \"_entity\": \"true\",\n" +
                "          \"_entitytype\": \"vpcfirewall\",\n" +
                "          \"firstdiscoveredon\": \"2022-06-16 13:00:00+0000\",\n" +
                "          \"latest\": true,\n" +
                "          \"_loaddate\": \"2022-06-17 00:12:00+0000\"\n" +

                "        }", JsonElement.class));

        JsonArray array = new JsonArray();
        array.add(jsonObject);
        return array;
    }

}
