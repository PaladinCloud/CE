package com.tmobile.cloud.azurerules.AppService;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.tmobile.cloud.awsrules.utils.CommonTestUtils;
import com.tmobile.cloud.awsrules.utils.PacmanUtils;
import com.tmobile.cloud.awsrules.utils.RulesElasticSearchRepositoryUtil;
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

@PowerMockIgnore({ "javax.net.ssl.*", "javax.management.*" })
@RunWith(PowerMockRunner.class)
@PrepareForTest({ PacmanUtils.class, BasePolicy.class, RulesElasticSearchRepositoryUtil.class, Annotation.class })
public class EnableActiveDirectoryRuleTest {
    @InjectMocks
    EnableActiveDirectoryRule enableActiveDirectoryRule;

    public JsonObject getFailureJsonArrayForActiveDirectoryDisabled() {
        Gson gson = new Gson();
        JsonObject jsonObject = new JsonObject();
        jsonObject.add("hits", gson.fromJson(
                "{\"hits\": [\n" +
                        "      {\n" +
                        "        \"_index\": \"azure_webapp\",\n" +
                        "        \"_type\": \"webapp\",\n" +
                        "        \"_id\": \"subscriptions/f4d319d8-7eac-4e15-a561-400f7744aa81/resourceGroups/dev-paladincloud/providers/Microsoft.Web/sites/pc-webapp\",\n" +
                        "        \"_score\": 0.18232156,\n" +
                        "        \"_source\": {\n" +
                        "          \"discoverydate\": \"2022-09-29 06:00:00+0000\",\n" +
                        "          \"_cloudType\": \"Azure\",\n" +
                        "          \"subscription\": \"f4d319d8-7eac-4e15-a561-400f7744aa81\",\n" +
                        "          \"region\": \"Central US\",\n" +
                        "          \"subscriptionName\": \"dev-paladincloud\",\n" +
                        "          \"resourceGroupName\": \"dev-paladincloud\",\n" +
                        "          \"id\": \"subscriptions/f4d319d8-7eac-4e15-a561-400f7744aa81/resourceGroups/dev-paladincloud/providers/Microsoft.Web/sites/pc-webapp\",\n" +
                        "          \"remoteDebuggingEnabled\": false,\n" +
                        "          \"http20Enabled\": false,\n" +
                        "          \"httpsOnly\": true,\n" +
                        "          \"ftpsState\": \"FtpsOnly\",\n" +
                        "          \"minTlsVersion\": \"1.2\",\n" +
                        "          \"clientCertEnabled\": true,\n" +
                        "          \"systemAssignedManagedServiceIdentityPrincipalId\": null,\n" +
                        "          \"authEnabled\": true,\n" +
                        "          \"hostNames\": [\n" +
                        "            \"pc-webapp.azurewebsites.net\"\n" +
                        "          ],\n" +
                        "          \"_resourceid\": \"subscriptions/f4d319d8-7eac-4e15-a561-400f7744aa81/resourceGroups/dev-paladincloud/providers/Microsoft.Web/sites/pc-webapp\",\n" +
                        "          \"_docid\": \"subscriptions/f4d319d8-7eac-4e15-a561-400f7744aa81/resourceGroups/dev-paladincloud/providers/Microsoft.Web/sites/pc-webapp\",\n" +
                        "          \"_entity\": \"true\",\n" +
                        "          \"_entitytype\": \"webapp\",\n" +
                        "          \"firstdiscoveredon\": \"2022-08-05 07:00:00+0000\",\n" +
                        "          \"latest\": true,\n" +
                        "          \"_loaddate\": \"2022-09-29 07:07:00+0000\"\n" +
                        "        }\n" +
                        "      }\n" +
                        "    ]}",
                JsonElement.class));
        return jsonObject;
    }

    public JsonObject getHitJsonArrayForActiveDirectoryEnabled() {
        Gson gson = new Gson();
        JsonObject jsonObject = new JsonObject();
        jsonObject.add("hits", gson.fromJson(
                "{\"hits\": [\n" +
                        "      {\n" +
                        "        \"_index\": \"azure_webapp\",\n" +
                        "        \"_type\": \"webapp\",\n" +
                        "        \"_id\": \"subscriptions/f4d319d8-7eac-4e15-a561-400f7744aa81/resourceGroups/dev-paladincloud/providers/Microsoft.Web/sites/pc-webapp\",\n" +
                        "        \"_score\": 0.18232156,\n" +
                        "        \"_source\": {\n" +
                        "          \"discoverydate\": \"2022-09-29 06:00:00+0000\",\n" +
                        "          \"_cloudType\": \"Azure\",\n" +
                        "          \"subscription\": \"f4d319d8-7eac-4e15-a561-400f7744aa81\",\n" +
                        "          \"region\": \"Central US\",\n" +
                        "          \"subscriptionName\": \"dev-paladincloud\",\n" +
                        "          \"resourceGroupName\": \"dev-paladincloud\",\n" +
                        "          \"id\": \"subscriptions/f4d319d8-7eac-4e15-a561-400f7744aa81/resourceGroups/dev-paladincloud/providers/Microsoft.Web/sites/pc-webapp\",\n" +
                        "          \"remoteDebuggingEnabled\": false,\n" +
                        "          \"http20Enabled\": false,\n" +
                        "          \"httpsOnly\": true,\n" +
                        "          \"ftpsState\": \"FtpsOnly\",\n" +
                        "          \"minTlsVersion\": \"1.2\",\n" +
                        "          \"clientCertEnabled\": true,\n" +
                        "          \"systemAssignedManagedServiceIdentityPrincipalId\": \"abc\",\n" +
                        "          \"authEnabled\": true,\n" +
                        "          \"hostNames\": [\n" +
                        "            \"pc-webapp.azurewebsites.net\"\n" +
                        "          ],\n" +
                        "          \"_resourceid\": \"subscriptions/f4d319d8-7eac-4e15-a561-400f7744aa81/resourceGroups/dev-paladincloud/providers/Microsoft.Web/sites/pc-webapp\",\n" +
                        "          \"_docid\": \"subscriptions/f4d319d8-7eac-4e15-a561-400f7744aa81/resourceGroups/dev-paladincloud/providers/Microsoft.Web/sites/pc-webapp\",\n" +
                        "          \"_entity\": \"true\",\n" +
                        "          \"_entitytype\": \"webapp\",\n" +
                        "          \"firstdiscoveredon\": \"2022-08-05 07:00:00+0000\",\n" +
                        "          \"latest\": true,\n" +
                        "          \"_loaddate\": \"2022-09-29 07:07:00+0000\"\n" +
                        "        }\n" +
                        "      }\n" +
                        "    ]}",
                JsonElement.class));
        return jsonObject;
    }

    @Test
    public void executeSucessTest() throws Exception {
        mockStatic(PacmanUtils.class);
        mockStatic(RulesElasticSearchRepositoryUtil.class);
        when(PacmanUtils.doesAllHaveValue(anyString(), anyString()))
                .thenReturn(
                        true);
        when(RulesElasticSearchRepositoryUtil.getQueryDetailsFromES(anyString(), anyObject(),
                anyObject(),
                anyObject(), anyObject(), anyInt(), anyObject(), anyObject(), anyObject()))
                .thenReturn(getHitJsonArrayForActiveDirectoryEnabled());
        assertThat(enableActiveDirectoryRule.execute(CommonTestUtils.getMapString("r_123 "),
                CommonTestUtils.getMapString("r_123 ")).getStatus(), is(PacmanSdkConstants.STATUS_SUCCESS));
    }

    @Test
    public void executeFailureTest() throws Exception {
        mockStatic(PacmanUtils.class);
        mockStatic(RulesElasticSearchRepositoryUtil.class);
        when(PacmanUtils.doesAllHaveValue(anyString(), anyString()))
                .thenReturn(
                        true);
        when(RulesElasticSearchRepositoryUtil.getQueryDetailsFromES(anyString(), anyObject(),
                anyObject(),
                anyObject(), anyObject(), anyInt(), anyObject(), anyObject(), anyObject()))
                .thenReturn(getFailureJsonArrayForActiveDirectoryDisabled());
        mockStatic(Annotation.class);
        when(Annotation.buildAnnotation(anyObject(),anyObject())).thenReturn(getMockAnnotation());
        assertThat(enableActiveDirectoryRule.execute(CommonTestUtils.getMapString("r_123 "),
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
        assertThat(enableActiveDirectoryRule.getHelpText(), is(notNullValue()));
    }


}
