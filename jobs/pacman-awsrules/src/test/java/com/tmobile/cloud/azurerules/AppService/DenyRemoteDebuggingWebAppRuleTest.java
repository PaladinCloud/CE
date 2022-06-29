package com.tmobile.cloud.azurerules.AppService;


import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.tmobile.cloud.awsrules.utils.CommonTestUtils;
import com.tmobile.cloud.awsrules.utils.PacmanUtils;
import com.tmobile.cloud.awsrules.utils.RulesElasticSearchRepositoryUtil;
import com.tmobile.cloud.constants.PacmanRuleConstants;
import com.tmobile.pacman.commons.PacmanSdkConstants;
import com.tmobile.pacman.commons.rule.BaseRule;
import com.tmobile.pacman.commons.rule.RuleResult;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.powermock.core.classloader.annotations.PowerMockIgnore;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import java.util.HashMap;
import java.util.Map;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.anyObject;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.*;
import static org.powermock.api.mockito.PowerMockito.mockStatic;
import static org.powermock.api.mockito.PowerMockito.when;

@PowerMockIgnore({"javax.net.ssl.*", "javax.management.*"})
@RunWith(PowerMockRunner.class)
@PrepareForTest({PacmanUtils.class, BaseRule.class})
@PrepareForTest({PacmanUtils.class, BaseRule.class, RulesElasticSearchRepositoryUtil.class})

public class DenyRemoteDebuggingWebAppRuleTest {
    @InjectMocks
    DenyRemoteDebuggingWebAppRule denyRemoteDebuggingWebAppRule;

    @Test
    public void getHelpTextTest() {
        assertThat(denyRemoteDebuggingWebAppRule.getHelpText(), is(notNullValue()));
    }

    public JsonObject getFailureJsonArrayForRemoteDebugging(){
        Gson gson=new Gson();
        JsonObject jsonObject = new JsonObject();
        jsonObject.add("hits", gson.fromJson("{\n    \"hits\": [\n        {\n            \"_source\": {\n                \"discoverydate\": \"2022-06-28 06:00:00+0000\",\n                \"_cloudType\": \"Azure\",\n                \"subscription\": \"f4d319d8-7eac-4e15-a561-400f7744aa81\",\n                \"region\": \"centralus\",\n                \"subscriptionName\": \"dev-paladincloud\",\n                \"resourceGroupName\": \"dev-paladincloud\",\n                \"id\": \"subscriptions/f4d319d8-7eac-4e15-a561-400f7744aa81/resourceGroups/dev-paladincloud/providers/Microsoft.Network/networkSecurityGroups/testing-nsg\",\n                \"key\": \"ccb7e20e-47c3-478b-a960-580c7a6b9d1e\",\n                \"name\": \"testing-nsg\",\n                \"tags\": {},\n                \"excludedDetectionTypes\": [\n                    \"Access_Anomaly\",\n                    \"Data_Exfiltration\",\n                    \"Unsafe_Action\"\n                ]\n            }\n        }\n    ]\n}", JsonElement.class));
        return jsonObject;
    }
    public  JsonObject getHitJsonArrayForRemoteDebugging() {
        Gson gson = new Gson();
        JsonObject jsonObject = new JsonObject();
        jsonObject.add("hits", gson.fromJson("{\n    \"hits\": [\n        {\n            \"_source\": {\n                \"discoverydate\": \"2022-06-28 06:00:00+0000\",\n                \"_cloudType\": \"Azure\",\n                \"subscription\": \"f4d319d8-7eac-4e15-a561-400f7744aa81\",\n                \"region\": \"centralus\",\n                \"subscriptionName\": \"dev-paladincloud\",\n                \"resourceGroupName\": \"dev-paladincloud\",\n                \"id\": \"subscriptions/f4d319d8-7eac-4e15-a561-400f7744aa81/resourceGroups/dev-paladincloud/providers/Microsoft.Network/networkSecurityGroups/testing-nsg\",\n                \"key\": \"ccb7e20e-47c3-478b-a960-580c7a6b9d1e\",\n                \"name\": \"testing-nsg\",\n                \"tags\": {},\n                \"excludedDetectionTypes\": [\n           \n                ]\n            }\n        }\n    ]\n}", JsonElement.class));
        return jsonObject;
    }


    @Test
    public void executeTest() throws Exception {
        public void executeSucessTest() throws Exception {
            mockStatic(PacmanUtils.class);
            when(PacmanUtils.doesAllHaveValue(anyString(), anyString(), anyString(), anyString(), anyString(),
                    anyString()))
            mockStatic(RulesElasticSearchRepositoryUtil.class);
            when(PacmanUtils.doesAllHaveValue(anyString(), anyString()))
                    .thenReturn(
                            true);
            when(PacmanUtils.formatUrl(anyObject(), anyString())).thenReturn("host");
            when(PacmanUtils.checkAccessibleToAll(anyObject(), anyString(), anyString(), anyString(), anyString(),
                    anyString())).thenReturn(CommonTestUtils.getMapBoolean("r_123 "));
            when(RulesElasticSearchRepositoryUtil.getQueryDetailsFromES(anyString(),anyObject(),
                    anyObject(),
                    anyObject(), anyObject(), anyInt(), anyObject(), anyObject(), anyObject())).thenReturn(getHitJsonArrayForRemoteDebugging());
            assertThat(denyRemoteDebuggingWebAppRule.execute(CommonTestUtils.getMapString("r_123 "),
                    CommonTestUtils.getMapString("r_123 ")), is(notNullValue()));
            CommonTestUtils.getMapString("r_123 ")).getStatus(), is(PacmanSdkConstants.STATUS_SUCCESS));

            when(PacmanUtils.checkAccessibleToAll(anyObject(), anyString(), anyString(), anyString(), anyString(),
                    anyString())).thenReturn(CommonTestUtils.getEmptyMapBoolean("r_123 "));
        }


    @Test
    public void appServiceRemoteDebuggingEnabled() {
        Map<String, String> ruleParam = new HashMap<>();
        ruleParam.put(PacmanSdkConstants.EXECUTION_ID, "exectionid");
        ruleParam.put(PacmanSdkConstants.RULE_ID, "denyRemoteDebuggingWebAppRule");
        ruleParam.put(PacmanRuleConstants.CATEGORY, PacmanSdkConstants.SECURITY);
        ruleParam.put(PacmanRuleConstants.SEVERITY, PacmanSdkConstants.SEV_HIGH);
        Map<String, String> resourceAttribute = getResourceForRemoteDebuggingEnabled("EMR1234");
        RuleResult ruleResult = denyRemoteDebuggingWebAppRule.execute(ruleParam, resourceAttribute);
        assertEquals(PacmanSdkConstants.STATUS_SUCCESS, ruleResult.getStatus());
    }

    @Test
    public void appServiceRemoteDebuggingDisabled() {
        Map<String, String> ruleParam = new HashMap<>();
        ruleParam.put(PacmanSdkConstants.EXECUTION_ID, "exectionid");
        ruleParam.put(PacmanSdkConstants.RULE_ID, "denyRemoteDebuggingWebAppRule");
        ruleParam.put(PacmanRuleConstants.CATEGORY, PacmanSdkConstants.SECURITY);
        ruleParam.put(PacmanRuleConstants.SEVERITY, PacmanSdkConstants.SEV_HIGH);
        Map<String, String> resourceAttribute = getResourceForRemoteDebuggingDisabled("EMR1234");
        RuleResult ruleResult = denyRemoteDebuggingWebAppRule.execute(ruleParam, resourceAttribute);
        assertEquals(PacmanSdkConstants.STATUS_FAILURE, ruleResult.getStatus());
    }
    private Map<String, String> getResourceForRemoteDebuggingEnabled(String clusterID) {
        Map<String, String> clusterObj = new HashMap<>();
        clusterObj.put("_resourceid", clusterID);
        clusterObj.put("name", "AZURE-EMR");
        clusterObj.put("creationTimestamp", "2022-05-31T13:00:38.628-08:00");
        clusterObj.put("securityconfig", "securityconfig");

        return clusterObj;
    }

    private Map<String, String> getResourceForRemoteDebuggingDisabled(String clusterID) {
        Map<String, String> clusterObj = new HashMap<>();
        clusterObj.put("_resourceid", clusterID);
        clusterObj.put("name", "AZURE-EMR");
        clusterObj.put("creationTimestamp", "2022-05-31T13:00:38.628-08:00");
        clusterObj.put("securityconfig", null);

        return clusterObj;
    }
}
