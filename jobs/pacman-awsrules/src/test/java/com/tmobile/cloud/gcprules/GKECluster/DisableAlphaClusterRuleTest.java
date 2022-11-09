package com.tmobile.cloud.gcprules.GKECluster;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.tmobile.cloud.awsrules.utils.CommonTestUtils;
import com.tmobile.cloud.awsrules.utils.PacmanUtils;
import com.tmobile.cloud.gcprules.GKEClusterRule.DisableAlphaClusterRule;
import com.tmobile.cloud.gcprules.GKEClusterRule.EnableNodeAutoUpgrade;
import com.tmobile.cloud.gcprules.utils.GCPUtils;
import com.tmobile.pacman.commons.PacmanSdkConstants;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import java.util.HashMap;
import java.util.Map;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.anyObject;
import static org.mockito.Matchers.anyString;
import static org.powermock.api.mockito.PowerMockito.mockStatic;
import static org.powermock.api.mockito.PowerMockito.when;

@RunWith(PowerMockRunner.class)
@PrepareForTest({PacmanUtils.class, GCPUtils.class})
public class DisableAlphaClusterRuleTest {

    @InjectMocks
    DisableAlphaClusterRule disableAlphaClusterRule;
    @Before
    public void setUp() {
        mockStatic(PacmanUtils.class);
        mockStatic(GCPUtils.class);
    }

    @Test
    public void executeSuccessTest() throws Exception {

        when(PacmanUtils.getPacmanHost(anyString())).thenReturn("host");
        when(GCPUtils.getHitsArrayFromEs(anyObject(), anyObject())).thenReturn(getHitsJsonForGKEDisableAlphaClusterSuccess());

        when(PacmanUtils.createAnnotation(anyString(), anyObject(), anyString(), anyString(), anyString()))
                .thenReturn(CommonTestUtils.getAnnotation("123"));
        when(PacmanUtils.doesAllHaveValue(anyString(), anyString(), anyString())).thenReturn(
                true);
        assertThat(disableAlphaClusterRule.execute(getMapString("r_123 "), getMapString("r_123 ")).getStatus(),
                is(PacmanSdkConstants.STATUS_SUCCESS));

    }

    private JsonArray getHitsJsonForGKEDisableAlphaClusterSuccess() {
        Gson gson = new Gson();
        JsonObject jsonObject = new JsonObject();
        jsonObject.add("_source", gson.fromJson(
                "{\n" +
                        "          \"discoveryDate\": \"2022-11-02 14:00:00+0530\",\n" +
                        "          \"_cloudType\": \"gcp\",\n" +
                        "          \"region\": \"us-central1-a\",\n" +
                        "          \"id\": \"d3b517c68dce42f6bd3a693d3e54be7596c51aabdf73449aaf274975e21a19db\",\n" +
                        "          \"projectName\": \"Paladin Cloud\",\n" +
                        "          \"projectId\": \"central-run-349616\",\n" +
                        "          \"masterAuthorizedNetworksConfig\": null,\n" +
                        "          \"bootDiskKmsKey\": null,\n" +
                        "          \"keyName\": null,\n" +
                        "          \"enableKubernetesAlpha\": false,\n" +
                        "          \"nodePools\": [\n" +
                        "            {\n" +
                        "              \"discoveryDate\": null,\n" +
                        "              \"_cloudType\": \"GCP\",\n" +
                        "              \"region\": null,\n" +
                        "              \"id\": null,\n" +
                        "              \"projectName\": null,\n" +
                        "              \"projectId\": null,\n" +
                        "              \"autoUpgrade\": true,\n" +
                        "              \"enableIntegrityMonitoring\": true,\n" +
                        "              \"enableSecureBoot\": false,\n" +
                        "              \"discoverydate\": null\n" +
                        "            },\n" +
                        "            {\n" +
                        "              \"discoveryDate\": null,\n" +
                        "              \"_cloudType\": \"GCP\",\n" +
                        "              \"region\": null,\n" +
                        "              \"id\": null,\n" +
                        "              \"projectName\": null,\n" +
                        "              \"projectId\": null,\n" +
                        "              \"autoUpgrade\": true,\n" +
                        "              \"enableIntegrityMonitoring\": true,\n" +
                        "              \"enableSecureBoot\": false,\n" +
                        "              \"discoverydate\": null\n" +
                        "            }\n" +
                        "          ],\n" +
                        "          \"discoverydate\": \"2022-11-02 14:00:00+0530\",\n" +
                        "          \"_resourceid\": \"d3b517c68dce42f6bd3a693d3e54be7596c51aabdf73449aaf274975e21a19db\",\n" +
                        "          \"_docid\": \"d3b517c68dce42f6bd3a693d3e54be7596c51aabdf73449aaf274975e21a19db\",\n" +
                        "          \"_entity\": \"true\",\n" +
                        "          \"_entitytype\": \"gkecluster\",\n" +
                        "          \"firstdiscoveredon\": \"2022-10-26 15:00:00+0530\",\n" +
                        "          \"latest\": true,\n" +
                        "          \"_loaddate\": \"2022-11-02 09:22:00+0000\"\n" +
                        "        }\n" ,
                JsonElement.class));
        JsonArray array = new JsonArray();
        array.add(jsonObject);
        return array;
    }

    @Test
    public void executeFailureTest() throws Exception {

        when(PacmanUtils.getPacmanHost(anyString())).thenReturn("host");
        when(GCPUtils.getHitsArrayFromEs(anyObject(), anyObject()))
                .thenReturn(getHitsJsonForGKEDisableAlphaClusterFailure());

        when(PacmanUtils.createAnnotation(anyString(), anyObject(), anyString(), anyString(), anyString()))
                .thenReturn(CommonTestUtils.getAnnotation("123"));
        when(PacmanUtils.doesAllHaveValue(anyString(), anyString(), anyString())).thenReturn(
                true);
        assertThat(disableAlphaClusterRule.execute(getMapString("r_123 "), getMapString("r_123 ")).getStatus(),
                is(PacmanSdkConstants.STATUS_FAILURE));
    }

    private JsonArray getHitsJsonForGKEDisableAlphaClusterFailure() {
        Gson gson = new Gson();
        JsonObject jsonObject = new JsonObject();
        jsonObject.add("_source", gson.fromJson(
                " {\n" +
                        "          \"discoveryDate\": \"2022-11-02 14:00:00+0530\",\n" +
                        "          \"_cloudType\": \"gcp\",\n" +
                        "          \"region\": \"us-central1-c\",\n" +
                        "          \"id\": \"d9d982b84a9b4e76991375828db5cda358be6a4ce2534a7fa2946448cc3e2a5c\",\n" +
                        "          \"projectName\": \"Paladin Cloud\",\n" +
                        "          \"projectId\": \"central-run-349616\",\n" +
                        "          \"masterAuthorizedNetworksConfig\": null,\n" +
                        "          \"bootDiskKmsKey\": null,\n" +
                        "          \"keyName\": null,\n" +
                        "          \"enableKubernetesAlpha\": true,\n" +
                        "          \"nodePools\": [\n" +
                        "            {\n" +
                        "              \"discoveryDate\": null,\n" +
                        "              \"_cloudType\": \"GCP\",\n" +
                        "              \"region\": null,\n" +
                        "              \"id\": null,\n" +
                        "              \"projectName\": null,\n" +
                        "              \"projectId\": null,\n" +
                        "              \"autoUpgrade\": false,\n" +
                        "              \"enableIntegrityMonitoring\": true,\n" +
                        "              \"enableSecureBoot\": false,\n" +
                        "              \"discoverydate\": null\n" +
                        "            }\n" +
                        "          ],\n" +
                        "          \"discoverydate\": \"2022-11-02 14:00:00+0530\",\n" +
                        "          \"_resourceid\": \"d9d982b84a9b4e76991375828db5cda358be6a4ce2534a7fa2946448cc3e2a5c\",\n" +
                        "          \"_docid\": \"d9d982b84a9b4e76991375828db5cda358be6a4ce2534a7fa2946448cc3e2a5c\",\n" +
                        "          \"_entity\": \"true\",\n" +
                        "          \"_entitytype\": \"gkecluster\",\n" +
                        "          \"firstdiscoveredon\": \"2022-11-02 14:00:00+0530\",\n" +
                        "          \"latest\": true,\n" +
                        "          \"_loaddate\": \"2022-11-02 09:22:00+0000\"\n" +
                        "        }\n" ,
                JsonElement.class));
        JsonArray array = new JsonArray();
        array.add(jsonObject);
        return array;
    }

    public static Map<String, String> getMapString(String passRuleResourceId) {
        Map<String, String> commonMap = new HashMap<>();
        commonMap.put("executionId", "1234");
        commonMap.put("_resourceid", passRuleResourceId);
        commonMap.put("severity", "medium");
        commonMap.put("ruleCategory", "security");
        commonMap.put("accountid", "12345");
        commonMap.put("violationReason", "Alpha cluster is enabled");
        commonMap.put("esSgRulesUrl", "/gcp_gkecluster/_search");
        return commonMap;
    }

    @Test
    public void getHelpTextTest() {
        assertThat(disableAlphaClusterRule.getHelpText(), is(notNullValue()));
    }
}
