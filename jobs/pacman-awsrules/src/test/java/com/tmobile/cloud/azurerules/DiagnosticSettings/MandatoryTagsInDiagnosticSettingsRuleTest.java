package com.tmobile.cloud.azurerules.DiagnosticSettings;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.tmobile.cloud.awsrules.utils.CommonTestUtils;
import com.tmobile.cloud.awsrules.utils.PacmanUtils;
import com.tmobile.cloud.awsrules.utils.RulesElasticSearchRepositoryUtil;
import com.tmobile.cloud.azurerules.BlobContainer.BlobContainerImmutableRule;
import com.tmobile.pacman.commons.PacmanSdkConstants;
import com.tmobile.pacman.commons.policy.Annotation;
import com.tmobile.pacman.commons.policy.BasePolicy;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.powermock.core.classloader.annotations.PowerMockIgnore;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.*;
import static org.mockito.Matchers.anyObject;
import static org.powermock.api.mockito.PowerMockito.mockStatic;
import static org.powermock.api.mockito.PowerMockito.when;

@PowerMockIgnore({"javax.net.ssl.*", "javax.management.*"})
@RunWith(PowerMockRunner.class)
@PrepareForTest({PacmanUtils.class, BasePolicy.class, RulesElasticSearchRepositoryUtil.class,Annotation.class})
public class MandatoryTagsInDiagnosticSettingsRuleTest {
    @InjectMocks
    MandatoryTagsInDiagnosticSettingsRule mandatoryTagsInDiagnosticSettingsRule;

    public JsonObject getFailureJsonArrayForDiagnosticSettings(){
        Gson gson=new Gson();
        JsonObject jsonObject = new JsonObject();
        jsonObject.add("hits", gson.fromJson("{\"hits\": [\n" +
                "      {\n" +
                "        \"_index\": \"azure_diagnosticsetting\",\n" +
                "        \"_type\": \"diagnosticsetting\",\n" +
                "        \"_id\": \"subscriptions/f4d319d8-7eac-4e15-a561-400f7744aa81/providers/microsoft.insights/diagnosticSettings/diagnostics-setting1\",\n" +
                "        \"_score\": 0.18232156,\n" +
                "        \"_source\": {\n" +
                "          \"discoverydate\": \"2022-09-27 11:00:00+0000\",\n" +
                "          \"_cloudType\": \"Azure\",\n" +
                "          \"subscription\": null,\n" +
                "          \"region\": null,\n" +
                "          \"subscriptionName\": \"dev-paladincloud\",\n" +
                "          \"resourceGroupName\": null,\n" +
                "          \"id\": \"subscriptions/f4d319d8-7eac-4e15-a561-400f7744aa81/providers/microsoft.insights/diagnosticSettings/diagnostics-setting1\",\n" +
                "          \"name\": \"diagnostics-setting1\",\n" +
                "          \"enabledCategories\": [\n" +
                "            \"Administrative\",\n" +
                "            \"Security\"\n" +
                "          ],\n" +
                "          \"subscriptionId\": \"f4d319d8-7eac-4e15-a561-400f7744aa81\",\n" +
                "          \"_resourceid\": \"subscriptions/f4d319d8-7eac-4e15-a561-400f7744aa81/providers/microsoft.insights/diagnosticSettings/diagnostics-setting1\",\n" +
                "          \"_docid\": \"subscriptions/f4d319d8-7eac-4e15-a561-400f7744aa81/providers/microsoft.insights/diagnosticSettings/diagnostics-setting1\",\n" +
                "          \"_entity\": \"true\",\n" +
                "          \"_entitytype\": \"diagnosticsetting\",\n" +
                "          \"firstdiscoveredon\": \"2022-09-26 10:00:00+0000\",\n" +
                "          \"latest\": true,\n" +
                "          \"_loaddate\": \"2022-09-27 11:50:00+0000\"\n" +
                "        }\n" +
                "      },\n" +
                "      {\n" +
                "        \"_index\": \"azure_diagnosticsetting\",\n" +
                "        \"_type\": \"diagnosticsetting\",\n" +
                "        \"_id\": \"subscriptions/f4d319d8-7eac-4e15-a561-400f7744aa81/providers/microsoft.insights/diagnosticSettings/diagnostics-setting2\",\n" +
                "        \"_score\": 0.18232156,\n" +
                "        \"_source\": {\n" +
                "          \"discoverydate\": \"2022-09-27 11:00:00+0000\",\n" +
                "          \"_cloudType\": \"Azure\",\n" +
                "          \"subscription\": null,\n" +
                "          \"region\": null,\n" +
                "          \"subscriptionName\": \"dev-paladincloud\",\n" +
                "          \"resourceGroupName\": null,\n" +
                "          \"id\": \"subscriptions/f4d319d8-7eac-4e15-a561-400f7744aa81/providers/microsoft.insights/diagnosticSettings/diagnostics-setting2\",\n" +
                "          \"name\": \"diagnostics-setting2\",\n" +
                "          \"enabledCategories\": [\n" +
                "            \"Poicy\",\n" +
                "            \"Administrative\",\n" +
                "            \"ServiceHealth\",\n" +
                "            \"Alert\",\n" +
                "            \"Security\"\n" +
                "          ],\n" +
                "          \"subscriptionId\": \"f4d319d8-7eac-4e15-a561-400f7744aa81\",\n" +
                "          \"_resourceid\": \"subscriptions/f4d319d8-7eac-4e15-a561-400f7744aa81/providers/microsoft.insights/diagnosticSettings/diagnostics-setting2\",\n" +
                "          \"_docid\": \"subscriptions/f4d319d8-7eac-4e15-a561-400f7744aa81/providers/microsoft.insights/diagnosticSettings/diagnostics-setting2\",\n" +
                "          \"_entity\": \"true\",\n" +
                "          \"_entitytype\": \"diagnosticsetting\",\n" +
                "          \"firstdiscoveredon\": \"2022-09-26 10:00:00+0000\",\n" +
                "          \"latest\": true,\n" +
                "          \"_loaddate\": \"2022-09-27 11:50:00+0000\"\n" +
                "        }\n" +
                "      }\n" +
                "    ]}", JsonElement.class));
        return jsonObject;
    }

    public  JsonObject getHitJsonArrayForDiagnosticSettings() {
        Gson gson = new Gson();
        JsonObject jsonObject = new JsonObject();
        jsonObject.add("hits", gson.fromJson("{\"hits\": [\n" +
                "      {\n" +
                "        \"_index\": \"azure_diagnosticsetting\",\n" +
                "        \"_type\": \"diagnosticsetting\",\n" +
                "        \"_id\": \"subscriptions/f4d319d8-7eac-4e15-a561-400f7744aa81/providers/microsoft.insights/diagnosticSettings/diagnostics-setting1\",\n" +
                "        \"_score\": 0.18232156,\n" +
                "        \"_source\": {\n" +
                "          \"discoverydate\": \"2022-09-27 11:00:00+0000\",\n" +
                "          \"_cloudType\": \"Azure\",\n" +
                "          \"subscription\": null,\n" +
                "          \"region\": null,\n" +
                "          \"subscriptionName\": \"dev-paladincloud\",\n" +
                "          \"resourceGroupName\": null,\n" +
                "          \"id\": \"subscriptions/f4d319d8-7eac-4e15-a561-400f7744aa81/providers/microsoft.insights/diagnosticSettings/diagnostics-setting1\",\n" +
                "          \"name\": \"diagnostics-setting1\",\n" +
                "          \"enabledCategories\": [\n" +
                "            \"Administrative\",\n" +
                "            \"Security\"\n" +
                "          ],\n" +
                "          \"subscriptionId\": \"f4d319d8-7eac-4e15-a561-400f7744aa81\",\n" +
                "          \"_resourceid\": \"subscriptions/f4d319d8-7eac-4e15-a561-400f7744aa81/providers/microsoft.insights/diagnosticSettings/diagnostics-setting1\",\n" +
                "          \"_docid\": \"subscriptions/f4d319d8-7eac-4e15-a561-400f7744aa81/providers/microsoft.insights/diagnosticSettings/diagnostics-setting1\",\n" +
                "          \"_entity\": \"true\",\n" +
                "          \"_entitytype\": \"diagnosticsetting\",\n" +
                "          \"firstdiscoveredon\": \"2022-09-26 10:00:00+0000\",\n" +
                "          \"latest\": true,\n" +
                "          \"_loaddate\": \"2022-09-27 11:50:00+0000\"\n" +
                "        }\n" +
                "      },\n" +
                "      {\n" +
                "        \"_index\": \"azure_diagnosticsetting\",\n" +
                "        \"_type\": \"diagnosticsetting\",\n" +
                "        \"_id\": \"subscriptions/f4d319d8-7eac-4e15-a561-400f7744aa81/providers/microsoft.insights/diagnosticSettings/diagnostics-setting2\",\n" +
                "        \"_score\": 0.18232156,\n" +
                "        \"_source\": {\n" +
                "          \"discoverydate\": \"2022-09-27 11:00:00+0000\",\n" +
                "          \"_cloudType\": \"Azure\",\n" +
                "          \"subscription\": null,\n" +
                "          \"region\": null,\n" +
                "          \"subscriptionName\": \"dev-paladincloud\",\n" +
                "          \"resourceGroupName\": null,\n" +
                "          \"id\": \"subscriptions/f4d319d8-7eac-4e15-a561-400f7744aa81/providers/microsoft.insights/diagnosticSettings/diagnostics-setting2\",\n" +
                "          \"name\": \"diagnostics-setting2\",\n" +
                "          \"enabledCategories\": [\n" +
                "            \"Policy\",\n" +
                "            \"Administrative\",\n" +
                "            \"ServiceHealth\",\n" +
                "            \"Alert\",\n" +
                "            \"Security\"\n" +
                "          ],\n" +
                "          \"subscriptionId\": \"f4d319d8-7eac-4e15-a561-400f7744aa81\",\n" +
                "          \"_resourceid\": \"subscriptions/f4d319d8-7eac-4e15-a561-400f7744aa81/providers/microsoft.insights/diagnosticSettings/diagnostics-setting2\",\n" +
                "          \"_docid\": \"subscriptions/f4d319d8-7eac-4e15-a561-400f7744aa81/providers/microsoft.insights/diagnosticSettings/diagnostics-setting2\",\n" +
                "          \"_entity\": \"true\",\n" +
                "          \"_entitytype\": \"diagnosticsetting\",\n" +
                "          \"firstdiscoveredon\": \"2022-09-26 10:00:00+0000\",\n" +
                "          \"latest\": true,\n" +
                "          \"_loaddate\": \"2022-09-27 11:50:00+0000\"\n" +
                "        }\n" +
                "      }\n" +
                "    ]}", JsonElement.class));
        return jsonObject;
    }

    @Test
    public void executeSucessTest() throws Exception {
        mockStatic(PacmanUtils.class);
        mockStatic(RulesElasticSearchRepositoryUtil.class);
        when(PacmanUtils.doesAllHaveValue(anyString(), anyString()))
                .thenReturn(
                        true);
        when(RulesElasticSearchRepositoryUtil.getQueryDetailsFromES(anyString(),anyObject(),
                anyObject(),
                anyObject(), anyObject(), anyInt(), anyObject(), anyObject(), anyObject())).thenReturn(getHitJsonArrayForDiagnosticSettings());
        assertThat(mandatoryTagsInDiagnosticSettingsRule.execute(CommonTestUtils.getMapString("r_123 "),
                CommonTestUtils.getMapString("r_123 ")).getStatus(), is(PacmanSdkConstants.STATUS_SUCCESS));
    }

    @Test
    public void executeFailureTest() throws Exception {
        mockStatic(PacmanUtils.class);
        mockStatic(RulesElasticSearchRepositoryUtil.class);
        when(PacmanUtils.doesAllHaveValue(anyString(), anyString()))
                .thenReturn(
                        true);
        when(RulesElasticSearchRepositoryUtil.getQueryDetailsFromES(anyString(),anyObject(),
                anyObject(),
                anyObject(), anyObject(), anyInt(), anyObject(), anyObject(), anyObject())).thenReturn(getFailureJsonArrayForDiagnosticSettings());
        mockStatic(Annotation.class);
        when(Annotation.buildAnnotation(anyObject(),anyObject())).thenReturn(getMockAnnotation());
        assertThat(mandatoryTagsInDiagnosticSettingsRule.execute(CommonTestUtils.getMapString("r_123 "),
                CommonTestUtils.getMapString("r_123 ")).getStatus(), is(PacmanSdkConstants.STATUS_FAILURE));
    }

    private Annotation getMockAnnotation() {
        Annotation annotation=new Annotation();
        annotation.put(PacmanSdkConstants.POLICY_NAME,"Mock policy name");
        annotation.put(PacmanSdkConstants.POLICY_ID, "Mock policy id");
        annotation.put(PacmanSdkConstants.POLICY_VERSION, "Mock policy version");
        annotation.put(PacmanSdkConstants.RESOURCE_ID, "Mock resource id");
        annotation.put(PacmanSdkConstants.TYPE, "Mock type");
        return annotation;
    }


    @Test
    public void getHelpTextTest() {
        assertThat(mandatoryTagsInDiagnosticSettingsRule.getHelpText(), is(notNullValue()));
    }
}

