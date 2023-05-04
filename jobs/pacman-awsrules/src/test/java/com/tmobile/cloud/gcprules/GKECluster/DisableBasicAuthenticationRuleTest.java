package com.tmobile.cloud.gcprules.GKECluster;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.tmobile.cloud.awsrules.utils.CommonTestUtils;
import com.tmobile.cloud.awsrules.utils.PacmanUtils;
import com.tmobile.cloud.gcprules.GKEClusterRule.DisableBasicAuthenticationRule;
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
public class DisableBasicAuthenticationRuleTest {

    @InjectMocks
    DisableBasicAuthenticationRule disableBasicAuthenticationRule;
    @Before
    public void setUp() {
        mockStatic(PacmanUtils.class);
        mockStatic(GCPUtils.class);
        mockStatic(Annotation.class);
    }

    @Test
    public void executeSuccessTest() throws Exception {

        when(PacmanUtils.getPacmanHost(anyString())).thenReturn("host");
        when(GCPUtils.getHitsArrayFromEs(anyObject(), anyObject())).thenReturn(getHitsJsonForGKEBasicAuthenticationSuccess());

        when(Annotation.buildAnnotation(anyObject(), anyObject())).thenReturn(CommonTestUtils.getMockAnnotation());
        when(PacmanUtils.doesAllHaveValue(anyString(), anyString(), anyString())).thenReturn(
                true);
        assertThat(disableBasicAuthenticationRule.execute(getMapString("r_123 "), getMapString("r_123 ")).getStatus(),
                is(PacmanSdkConstants.STATUS_SUCCESS));

    }

    private JsonArray getHitsJsonForGKEBasicAuthenticationSuccess() {
        Gson gson = new Gson();
        JsonObject jsonObject = new JsonObject();
        jsonObject.add("_source", gson.fromJson(
                "{\n" +
                        "          \"discoveryDate\": \"2022-11-07 17:00:00+0530\",\n" +
                        "          \"_cloudType\": \"gcp\",\n" +
                        "          \"region\": \"us-central1-c\",\n" +
                        "          \"id\": \"f0336360ca484fb581d8cd2c414ecc768dad2f0b0b8846c1a6c93871816019a2\",\n" +
                        "          \"projectName\": \"Paladin Cloud\",\n" +
                        "          \"projectId\": \"central-run-349616\",\n" +
                        "          \"masterAuthorizedNetworksConfig\": null,\n" +
                        "          \"bootDiskKmsKey\": null,\n" +
                        "          \"keyName\": null,\n" +
                        "          \"username\": \"\",\n" +
                        "          \"password\": \"\",\n" +
                        "          \"version\": \"1.23.8-gke.1900\",\n" +
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
                        "          \"discoverydate\": \"2022-11-07 17:00:00+0530\",\n" +
                        "          \"_resourceid\": \"f0336360ca484fb581d8cd2c414ecc768dad2f0b0b8846c1a6c93871816019a2\",\n" +
                        "          \"_docid\": \"f0336360ca484fb581d8cd2c414ecc768dad2f0b0b8846c1a6c93871816019a2\",\n" +
                        "          \"_entity\": \"true\",\n" +
                        "          \"_entitytype\": \"gkecluster\",\n" +
                        "          \"firstdiscoveredon\": \"2022-11-01 14:00:00+0530\",\n" +
                        "          \"latest\": true,\n" +
                        "          \"_loaddate\": \"2022-11-07 12:02:00+0000\"\n" +
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
                .thenReturn(getHitsJsonForGKEBasicAuthenticationFailure());

        when(Annotation.buildAnnotation(anyObject(), anyObject())).thenReturn(CommonTestUtils.getMockAnnotation());
        when(PacmanUtils.doesAllHaveValue(anyString(), anyString(), anyString())).thenReturn(
                true);
        assertThat(disableBasicAuthenticationRule.execute(getMapString("r_123 "), getMapString("r_123 ")).getStatus(),
                is(PacmanSdkConstants.STATUS_FAILURE));
    }

    private JsonArray getHitsJsonForGKEBasicAuthenticationFailure() {
        Gson gson = new Gson();
        JsonObject jsonObject = new JsonObject();
        jsonObject.add("_source", gson.fromJson(
                "{\n" +
                        "          \"discoveryDate\": \"2022-11-07 17:00:00+0530\",\n" +
                        "          \"_cloudType\": \"gcp\",\n" +
                        "          \"region\": \"us-central1-c\",\n" +
                        "          \"id\": \"27297f8c62ec43b5a3367b00cb2a6cacb97f7a20327a4eadb30e6d048661a545\",\n" +
                        "          \"projectName\": \"Paladin Cloud\",\n" +
                        "          \"projectId\": \"central-run-349616\",\n" +
                        "          \"masterAuthorizedNetworksConfig\": null,\n" +
                        "          \"bootDiskKmsKey\": null,\n" +
                        "          \"keyName\": null,\n" +
                        "          \"username\": \"a\",\n" +
                        "          \"password\": \"b\",\n" +
                        "          \"version\": \"1.13.8-gke.1900\",\n" +
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
                        "              \"enableSecureBoot\": true,\n" +
                        "              \"discoverydate\": null\n" +
                        "            }\n" +
                        "          ],\n" +
                        "          \"discoverydate\": \"2022-11-07 17:00:00+0530\",\n" +
                        "          \"_resourceid\": \"27297f8c62ec43b5a3367b00cb2a6cacb97f7a20327a4eadb30e6d048661a545\",\n" +
                        "          \"_docid\": \"27297f8c62ec43b5a3367b00cb2a6cacb97f7a20327a4eadb30e6d048661a545\",\n" +
                        "          \"_entity\": \"true\",\n" +
                        "          \"_entitytype\": \"gkecluster\",\n" +
                        "          \"firstdiscoveredon\": \"2022-10-27 12:00:00+0000\",\n" +
                        "          \"latest\": true,\n" +
                        "          \"_loaddate\": \"2022-11-07 12:02:00+0000\"\n" +
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
        commonMap.put("severity", "critical");
        commonMap.put("ruleCategory", "security");
        commonMap.put("accountid", "12345");
        commonMap.put("violationReason", "Basic authentication is NOT disabled");
        commonMap.put("esSgRulesUrl", "/gcp_gkecluster/_search");
        return commonMap;
    }

    @Test
    public void getHelpTextTest() {
        assertThat(disableBasicAuthenticationRule.getHelpText(), is(notNullValue()));
    }

}
