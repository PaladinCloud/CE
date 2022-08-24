package com.tmobile.cloud.azurerules.FunctionApp;

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

@PowerMockIgnore({"javax.net.ssl.*", "javax.management.*"})
@RunWith(PowerMockRunner.class)
@PrepareForTest({PacmanUtils.class, BaseRule.class, RulesElasticSearchRepositoryUtil.class})
public class IncomingClientCertificateShouldBeEnabledTest {
    @InjectMocks
    IncomingClientCertificateShouldBeEnabled incomingClientCertificateShouldBeEnabled;

    public JsonObject getFailureJsonArrayForIncomingCertificateEnabled(){
        Gson gson=new Gson();
        JsonObject jsonObject = new JsonObject();
        jsonObject.add("hits", gson.fromJson("{\n    \"hits\": [\n  {\n" +
                "        \"_index\": \"azure_functionapp\",\n" +
                "        \"_type\": \"functionapp\",\n" +
                "        \"_id\": \"subscriptions/f4d319d8-7eac-4e15-a561-400f7744aa81/resourceGroups/dev-paladin-policydata-validation-rg/providers/Microsoft.Web/sites/demo-logic-app1\",\n" +
                "        \"_score\": 0.2876821,\n" +
                "        \"_source\": {\n" +
                "          \"discoverydate\": \"2022-08-26 16:00:00+0530\",\n" +
                "          \"_cloudType\": \"Azure\",\n" +
                "          \"subscription\": null,\n" +
                "          \"region\": null,\n" +
                "          \"subscriptionName\": null,\n" +
                "          \"resourceGroupName\": null,\n" +
                "          \"id\": \"subscriptions/f4d319d8-7eac-4e15-a561-400f7744aa81/resourceGroups/dev-paladin-policydata-validation-rg/providers/Microsoft.Web/sites/demo-logic-app1\",\n" +
                "          \"clientCertEnabled\": false,\n" +
                "          \"_resourceid\": \"subscriptions/f4d319d8-7eac-4e15-a561-400f7744aa81/resourceGroups/dev-paladin-policydata-validation-rg/providers/Microsoft.Web/sites/demo-logic-app1\",\n" +
                "          \"_docid\": \"subscriptions/f4d319d8-7eac-4e15-a561-400f7744aa81/resourceGroups/dev-paladin-policydata-validation-rg/providers/Microsoft.Web/sites/demo-logic-app1\",\n" +
                "          \"_entity\": \"true\",\n" +
                "          \"_entitytype\": \"functionapp\",\n" +
                "          \"firstdiscoveredon\": \"2022-08-26 16:00:00+0530\",\n" +
                "          \"latest\": true,\n" +
                "          \"_loaddate\": \"2022-08-26 11:19:00+0000\"\n" +
                "        }\n" +
                "      }     \n  ]\n}", JsonElement.class));
        return jsonObject;
    }

    public  JsonObject getHitJsonArrayForIncomingCertificateEnabled() {
        Gson gson = new Gson();
        JsonObject jsonObject = new JsonObject();
        jsonObject.add("hits", gson.fromJson("{\n    \"hits\": [\n      {\n" +
                "        \"_index\": \"azure_functionapp\",\n" +
                "        \"_type\": \"functionapp\",\n" +
                "        \"_id\": \"subscriptions/f4d319d8-7eac-4e15-a561-400f7744aa81/resourceGroups/dev-paladin-policydata-validation-rg/providers/Microsoft.Web/sites/demo-logic-app1\",\n" +
                "        \"_score\": 0.2876821,\n" +
                "        \"_source\": {\n" +
                "          \"discoverydate\": \"2022-08-26 16:00:00+0530\",\n" +
                "          \"_cloudType\": \"Azure\",\n" +
                "          \"subscription\": null,\n" +
                "          \"region\": null,\n" +
                "          \"subscriptionName\": null,\n" +
                "          \"resourceGroupName\": null,\n" +
                "          \"id\": \"subscriptions/f4d319d8-7eac-4e15-a561-400f7744aa81/resourceGroups/dev-paladin-policydata-validation-rg/providers/Microsoft.Web/sites/demo-logic-app1\",\n" +
                "          \"clientCertEnabled\": true,\n" +
                "          \"_resourceid\": \"subscriptions/f4d319d8-7eac-4e15-a561-400f7744aa81/resourceGroups/dev-paladin-policydata-validation-rg/providers/Microsoft.Web/sites/demo-logic-app1\",\n" +
                "          \"_docid\": \"subscriptions/f4d319d8-7eac-4e15-a561-400f7744aa81/resourceGroups/dev-paladin-policydata-validation-rg/providers/Microsoft.Web/sites/demo-logic-app1\",\n" +
                "          \"_entity\": \"true\",\n" +
                "          \"_entitytype\": \"functionapp\",\n" +
                "          \"firstdiscoveredon\": \"2022-08-26 16:00:00+0530\",\n" +
                "          \"latest\": true,\n" +
                "          \"_loaddate\": \"2022-08-26 11:19:00+0000\"\n" +
                "        }\n" +
                "      }\n    ]\n}", JsonElement.class));
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
                anyObject(), anyObject(), anyInt(), anyObject(), anyObject(), anyObject())).thenReturn(getHitJsonArrayForIncomingCertificateEnabled());
        assertThat(incomingClientCertificateShouldBeEnabled.execute(CommonTestUtils.getMapString("r_123 "),
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
                anyObject(), anyObject(), anyInt(), anyObject(), anyObject(), anyObject())).thenReturn(getFailureJsonArrayForIncomingCertificateEnabled());
        assertThat(incomingClientCertificateShouldBeEnabled.execute(CommonTestUtils.getMapString("r_123 "),
                CommonTestUtils.getMapString("r_123 ")).getStatus(), is(PacmanSdkConstants.STATUS_FAILURE));
    }


    @Test
    public void getHelpTextTest() {
        assertThat(incomingClientCertificateShouldBeEnabled.getHelpText(), is(notNullValue()));
    }

}
