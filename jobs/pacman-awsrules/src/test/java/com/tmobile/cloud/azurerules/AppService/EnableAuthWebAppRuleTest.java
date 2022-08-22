package com.tmobile.cloud.azurerules.AppService;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.tmobile.cloud.awsrules.utils.CommonTestUtils;
import com.tmobile.cloud.awsrules.utils.PacmanUtils;
import com.tmobile.cloud.awsrules.utils.RulesElasticSearchRepositoryUtil;
import com.tmobile.pacman.commons.PacmanSdkConstants;
import com.tmobile.pacman.commons.rule.BaseRule;
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
@PrepareForTest({ PacmanUtils.class, BaseRule.class, RulesElasticSearchRepositoryUtil.class })
public class EnableAuthWebAppRuleTest {
    @InjectMocks
    EnableAuthWebAppRule enableAuthWebAppRule;

    public JsonObject getFailureJsonArrayForAuthDisabled() {
        Gson gson = new Gson();
        JsonObject jsonObject = new JsonObject();
        jsonObject.add("hits", gson.fromJson(
                "{\"hits\": [\n" +
                        "      {\n" +
                        "        \"_index\": \"azure_webapp\",\n" +
                        "        \"_type\": \"webapp\",\n" +
                        "        \"_id\": \"subscriptions/f4d319d8-7eac-4e15-a561-400f7744aa81/resourceGroups/dev-paladincloud/providers/Microsoft.Web/sites/pc-webapp\",\n" +
                        "        \"_score\": 0.2876821,\n" +
                        "        \"_source\": {\n" +
                        "          \"discoverydate\": \"2022-08-16 08:00:00+0000\",\n" +
                        "          \"_cloudType\": \"Azure\",\n" +
                        "          \"subscription\": \"f4d319d8-7eac-4e15-a561-400f7744aa81\",\n" +
                        "          \"region\": null,\n" +
                        "          \"subscriptionName\": \"dev-paladincloud\",\n" +
                        "          \"resourceGroupName\": \"dev-paladincloud\",\n" +
                        "          \"id\": \"subscriptions/f4d319d8-7eac-4e15-a561-400f7744aa81/resourceGroups/dev-paladincloud/providers/Microsoft.Web/sites/pc-webapp\",\n" +
                        "          \"remoteDebuggingEnabled\": false,\n" +
                        "          \"http20Enabled\": false,\n" +
                        "          \"authEnabled\": false,\n" +
                        "          \"hostNames\": [\n" +
                        "            \"pc-webapp.azurewebsites.net\"\n" +
                        "          ],\n" +
                        "          \"_resourceid\": \"subscriptions/f4d319d8-7eac-4e15-a561-400f7744aa81/resourceGroups/dev-paladincloud/providers/Microsoft.Web/sites/pc-webapp\",\n" +
                        "          \"_docid\": \"subscriptions/f4d319d8-7eac-4e15-a561-400f7744aa81/resourceGroups/dev-paladincloud/providers/Microsoft.Web/sites/pc-webapp\",\n" +
                        "          \"_entity\": \"true\",\n" +
                        "          \"_entitytype\": \"webapp\",\n" +
                        "          \"firstdiscoveredon\": \"2022-08-05 07:00:00+0000\",\n" +
                        "          \"latest\": true,\n" +
                        "          \"_loaddate\": \"2022-08-16 08:18:00+0000\"\n" +
                        "        }\n" +
                        "      }\n" +
                        "    ]}",
                JsonElement.class));
        return jsonObject;
    }

    public JsonObject getHitJsonArrayForAuthEnabled() {
        Gson gson = new Gson();
        JsonObject jsonObject = new JsonObject();
        jsonObject.add("hits", gson.fromJson(
                "{\"hits\": [\n" +
                        "      {\n" +
                        "        \"_index\": \"azure_webapp\",\n" +
                        "        \"_type\": \"webapp\",\n" +
                        "        \"_id\": \"subscriptions/f4d319d8-7eac-4e15-a561-400f7744aa81/resourceGroups/dev-paladincloud/providers/Microsoft.Web/sites/pc-webapp\",\n" +
                        "        \"_score\": 0.2876821,\n" +
                        "        \"_source\": {\n" +
                        "          \"discoverydate\": \"2022-08-16 08:00:00+0000\",\n" +
                        "          \"_cloudType\": \"Azure\",\n" +
                        "          \"subscription\": \"f4d319d8-7eac-4e15-a561-400f7744aa81\",\n" +
                        "          \"region\": null,\n" +
                        "          \"subscriptionName\": \"dev-paladincloud\",\n" +
                        "          \"resourceGroupName\": \"dev-paladincloud\",\n" +
                        "          \"id\": \"subscriptions/f4d319d8-7eac-4e15-a561-400f7744aa81/resourceGroups/dev-paladincloud/providers/Microsoft.Web/sites/pc-webapp\",\n" +
                        "          \"remoteDebuggingEnabled\": false,\n" +
                        "          \"http20Enabled\": false,\n" +
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
                        "          \"_loaddate\": \"2022-08-16 08:18:00+0000\"\n" +
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
                .thenReturn(getHitJsonArrayForAuthEnabled());
        assertThat(enableAuthWebAppRule.execute(CommonTestUtils.getMapString("r_123 "),
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
                .thenReturn(getFailureJsonArrayForAuthDisabled());
        assertThat(enableAuthWebAppRule.execute(CommonTestUtils.getMapString("r_123 "),
                CommonTestUtils.getMapString("r_123 ")).getStatus(), is(PacmanSdkConstants.STATUS_FAILURE));
    }

    @Test
    public void getHelpTextTest() {
        assertThat(enableAuthWebAppRule.getHelpText(), is(notNullValue()));
    }


}
