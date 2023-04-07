package com.tmobile.cloud.gcprules.GKECluster;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.tmobile.cloud.awsrules.utils.CommonTestUtils;
import com.tmobile.cloud.awsrules.utils.PacmanUtils;
import com.tmobile.cloud.gcprules.GKEClusterRule.EnableIntraNodeVisibilityRule;
import com.tmobile.cloud.gcprules.utils.GCPUtils;
import com.tmobile.pacman.commons.PacmanSdkConstants;
import com.tmobile.pacman.commons.policy.Annotation;
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
@PrepareForTest({PacmanUtils.class, GCPUtils.class, Annotation.class})
public class EnableIntraNodeVisibilityRuleTest {

    @InjectMocks
    EnableIntraNodeVisibilityRule enableIntraNodeVisibilityRule;

    @Before
    public void setUp() {
        mockStatic(PacmanUtils.class);
        mockStatic(GCPUtils.class);
        mockStatic(Annotation.class);
    }
    @Test
    public void executeSuccessTest() throws Exception {

        when(PacmanUtils.getPacmanHost(anyString())).thenReturn("host");
        when(GCPUtils.getHitsArrayFromEs(anyObject(), anyObject())).thenReturn(getHitsJsonForIntraNodeVisibilitySuccess());

        when(Annotation.buildAnnotation(anyObject(), anyObject())).thenReturn(CommonTestUtils.getMockAnnotation());
        when(PacmanUtils.doesAllHaveValue(anyString(), anyString(), anyString())).thenReturn(
                true);
        assertThat(enableIntraNodeVisibilityRule.execute(getMapString("r_123 "), getMapString("r_123 ")).getStatus(),
                is(PacmanSdkConstants.STATUS_SUCCESS));

    }

    private JsonArray getHitsJsonForIntraNodeVisibilitySuccess() {
        Gson gson = new Gson();
        JsonObject jsonObject = new JsonObject();
        jsonObject.add("_source", gson.fromJson(
                "{\n" +
                        "          \"discoveryDate\": \"2022-10-27 12:00:00+0000\",\n" +
                        "          \"_cloudType\": \"gcp\",\n" +
                        "          \"region\": \"us-central1-c\",\n" +
                        "          \"id\": \"27297f8c62ec43b5a3367b00cb2a6cacb97f7a20327a4eadb30e6d048661a545\",\n" +
                        "          \"projectName\": \"Paladin Cloud\",\n" +
                        "          \"projectId\": \"central-run-349616\",\n" +
                        "          \"masterAuthorizedNetworksConfig\": null,\n" +
                        "          \"bootDiskKmsKey\": null,\n" +
                        "          \"intraNodeVisibility\": true,\n" +
                        "          \"keyName\": null,\n" +
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
                        "            }\n" +
                        "          ],\n" +
                        "          \"discoverydate\": \"2022-10-27 12:00:00+0000\",\n" +
                        "          \"_resourceid\": \"27297f8c62ec43b5a3367b00cb2a6cacb97f7a20327a4eadb30e6d048661a545\",\n" +
                        "          \"_docid\": \"27297f8c62ec43b5a3367b00cb2a6cacb97f7a20327a4eadb30e6d048661a545\",\n" +
                        "          \"_entity\": \"true\",\n" +
                        "          \"_entitytype\": \"gkecluster\",\n" +
                        "          \"firstdiscoveredon\": \"2022-10-27 12:00:00+0000\",\n" +
                        "          \"latest\": true,\n" +
                        "          \"_loaddate\": \"2022-10-27 12:30:00+0000\"\n" +
                        "        }",
                JsonElement.class));
        JsonArray array = new JsonArray();
        array.add(jsonObject);
        return array;
    }

    @Test
    public void executeFailureTest() throws Exception {

        when(PacmanUtils.getPacmanHost(anyString())).thenReturn("host");
        when(GCPUtils.getHitsArrayFromEs(anyObject(), anyObject()))
                .thenReturn(getHitsJsonForIntraNodeVisibilityFailure());

        when(Annotation.buildAnnotation(anyObject(), anyObject())).thenReturn(CommonTestUtils.getMockAnnotation());
        when(PacmanUtils.doesAllHaveValue(anyString(), anyString(), anyString())).thenReturn(
                true);
        assertThat(enableIntraNodeVisibilityRule.execute(getMapString("r_123 "), getMapString("r_123 ")).getStatus(),
                is(PacmanSdkConstants.STATUS_FAILURE));
    }

    private JsonArray getHitsJsonForIntraNodeVisibilityFailure() {
        Gson gson = new Gson();
        JsonObject jsonObject = new JsonObject();
        jsonObject.add("_source", gson.fromJson(
                "{\n" +
                        "          \"discoveryDate\": \"2022-10-27 12:00:00+0000\",\n" +
                        "          \"_cloudType\": \"gcp\",\n" +
                        "          \"region\": \"us-central1-c\",\n" +
                        "          \"id\": \"27297f8c62ec43b5a3367b00cb2a6cacb97f7a20327a4eadb30e6d048661a545\",\n" +
                        "          \"projectName\": \"Paladin Cloud\",\n" +
                        "          \"projectId\": \"central-run-349616\",\n" +
                        "          \"masterAuthorizedNetworksConfig\": null,\n" +
                        "          \"bootDiskKmsKey\": null,\n" +
                        "          \"intraNodeVisibility\": false,\n" +
                        "          \"keyName\": null,\n" +
                        "          \"nodePools\": [\n" +
                        "            {\n" +
                        "              \"discoveryDate\": null,\n" +
                        "              \"_cloudType\": \"GCP\",\n" +
                        "              \"region\": null,\n" +
                        "              \"id\": null,\n" +
                        "              \"projectName\": null,\n" +
                        "              \"projectId\": null,\n" +
                        "              \"autoUpgrade\": true,\n" +
                        "              \"enableIntegrityMonitoring\": false,\n" +
                        "              \"enableSecureBoot\": false,\n" +
                        "              \"discoverydate\": null\n" +
                        "            }\n" +
                        "          ],\n" +
                        "          \"discoverydate\": \"2022-10-27 12:00:00+0000\",\n" +
                        "          \"_resourceid\": \"27297f8c62ec43b5a3367b00cb2a6cacb97f7a20327a4eadb30e6d048661a545\",\n" +
                        "          \"_docid\": \"27297f8c62ec43b5a3367b00cb2a6cacb97f7a20327a4eadb30e6d048661a545\",\n" +
                        "          \"_entity\": \"true\",\n" +
                        "          \"_entitytype\": \"gkecluster\",\n" +
                        "          \"firstdiscoveredon\": \"2022-10-27 12:00:00+0000\",\n" +
                        "          \"latest\": true,\n" +
                        "          \"_loaddate\": \"2022-10-27 12:30:00+0000\"\n" +
                        "        }",
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
        commonMap.put("violationReason", "if intraNode Visibility flag is disabled");
        commonMap.put("esSgRulesUrl", "/gcp_gkecluster/_search");
        return commonMap;
    }

    @Test
    public void getHelpTextTest() {
        assertThat(enableIntraNodeVisibilityRule.getHelpText(), is(notNullValue()));
    }
}
