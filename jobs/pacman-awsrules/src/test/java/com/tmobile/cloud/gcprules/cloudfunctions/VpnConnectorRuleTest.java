package com.tmobile.cloud.gcprules.cloudfunctions;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.tmobile.cloud.awsrules.utils.CommonTestUtils;
import com.tmobile.cloud.awsrules.utils.PacmanUtils;
import com.tmobile.cloud.constants.PacmanRuleConstants;
import com.tmobile.cloud.gcprules.loadbalancer.EnableHttpsRule;
import com.tmobile.cloud.gcprules.utils.GCPUtils;
import com.tmobile.pacman.commons.PacmanSdkConstants;
import com.tmobile.pacman.commons.exception.InvalidInputException;
import com.tmobile.pacman.commons.policy.PolicyResult;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.powermock.core.classloader.annotations.PowerMockIgnore;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.anyObject;
import static org.mockito.Matchers.anyString;
import static org.powermock.api.mockito.PowerMockito.mockStatic;
import static org.powermock.api.mockito.PowerMockito.when;

@RunWith(PowerMockRunner.class)
@PrepareForTest({PacmanUtils.class, GCPUtils.class})
@PowerMockIgnore({"javax.net.ssl.*", "javax.management.*","jdk.internal.reflect.*"})
public class VpnConnectorRuleTest {

    @InjectMocks
    VpnConnectorRule vpnConnectorRule;

    @Before
    public void setUp() {
        mockStatic(PacmanUtils.class);
        mockStatic(GCPUtils.class);
    }


    @Test
    public void executeSuccessTest() throws Exception {
        when(PacmanUtils.getPacmanHost(anyString())).thenReturn("host");
        when(GCPUtils.validateRuleParam(anyObject())).thenReturn(true);
        when(GCPUtils.getJsonObjFromSourceData(anyObject(), anyObject())).thenReturn(getHitsJsonObjectForIngressSettingRule());
        when(PacmanUtils.createAnnotation(anyString(), anyObject(), anyString(), anyString(), anyString()))
                .thenReturn(CommonTestUtils.getAnnotation("123"));
        Map<String, String> map = getMapString("r_123 ");
        assertThat(vpnConnectorRule.execute(getMapString("r_123 "), getMapString("r_123 ")).getStatus(),
                is(PacmanSdkConstants.STATUS_SUCCESS));

    }

    private JsonObject getHitsJsonObjectForIngressSettingRule() {
        JsonArray hitsJsonArray = getHitsJsonArrayForEnableHttpsRule();
        JsonObject sourceData= null;
        if (hitsJsonArray != null && hitsJsonArray.size() > 0){
            sourceData = (JsonObject) ((JsonObject) hitsJsonArray.get(0))
                    .get(PacmanRuleConstants.SOURCE);
        }
        return sourceData;
    }

    private JsonArray getHitsJsonArrayForEnableHttpsRule(){
        Gson gson = new Gson();
        JsonObject jsonObject = new JsonObject();
        jsonObject.add("_source", gson.fromJson(
                " {\n" +
                        "          \"discoveryDate\": \"2022-12-08 17:00:00+0530\",\n" +
                        "          \"_cloudType\": \"GCP\",\n" +
                        "          \"region\": \"us-central1\",\n" +
                        "          \"id\": \"projects/central-run-349616/locations/us-central1/functions/function-2\",\n" +
                        "          \"projectName\": \"Paladin cloud\",\n" +
                        "          \"projectId\": \"central-run-349616\",\n" +
                        "          \"functionName\": \"projects/central-run-349616/locations/us-central1/functions/function-2\",\n" +
                        "          \"ingressSetting\": \"ALLOW_ALL\",\n" +
                        "          \"vpcConnector\": \"projects/central-run-349616/locations/us-central1/connectors/CONNECTOR_NAME\",\n" +
                        "          \"httpTrigger\": null,\n" +
                        "          \"discoverydate\": \"2022-12-08 11:48:00+0000\"\n" +
                        "        }\n" ,
                JsonElement.class));
        JsonArray array = new JsonArray();
        array.add(jsonObject);
        return array;
    }

    @Test
    public void executeFailureTest() throws Exception {
        when(PacmanUtils.getPacmanHost(anyString())).thenReturn("host");
        when(GCPUtils.validateRuleParam(anyObject())).thenReturn(true);
        when(GCPUtils.getJsonObjFromSourceData(anyObject(), anyObject())).thenReturn(getFailureHitsJsonObjForIngressSettingRule());
        when(GCPUtils.fetchPolicyResult(anyObject(), anyObject(), anyObject())).thenReturn(new PolicyResult(PacmanSdkConstants.STATUS_FAILURE, PacmanRuleConstants.FAILURE_MESSAGE,
                anyObject()));
        Map<String, String> map = getMapString("r_123 ");
        assertThat(vpnConnectorRule.execute(map, map).getStatus(),
                is(PacmanSdkConstants.STATUS_FAILURE));
    }

    private JsonObject getFailureHitsJsonObjForIngressSettingRule() {
        JsonArray hitsJsonArray = getFailureHitsJsonArrayForEnableHttpsRule();
        JsonObject sourceData= null;
        if (hitsJsonArray != null && hitsJsonArray.size() > 0){
            sourceData = (JsonObject) ((JsonObject) hitsJsonArray.get(0))
                    .get(PacmanRuleConstants.SOURCE);
        }
        return sourceData;
    }


    private JsonArray getFailureHitsJsonArrayForEnableHttpsRule() {
        Gson gson = new Gson();
        JsonObject jsonObject = new JsonObject();
        jsonObject.add("_source", gson.fromJson(
                " {\n" +
                        "          \"discoveryDate\": \"2022-12-08 17:00:00+0530\",\n" +
                        "          \"_cloudType\": \"GCP\",\n" +
                        "          \"region\": \"us-central1\",\n" +
                        "          \"id\": \"projects/central-run-349616/locations/us-central1/functions/function-2\",\n" +
                        "          \"projectName\": \"Paladin cloud\",\n" +
                        "          \"projectId\": \"central-run-349616\",\n" +
                        "          \"functionName\": \"projects/central-run-349616/locations/us-central1/functions/function-2\",\n" +
                        "          \"ingressSetting\": \"ALLOW_ALL\",\n" +
                        "          \"vpcConnector\": NULL,\n" +
                        "          \"httpTrigger\": NULL,\n" +
                        "          \"discoverydate\": \"2022-12-08 11:48:00+0000\"\n" +
                        "        }\n" ,
                JsonElement.class));
        JsonArray array = new JsonArray();
        array.add(jsonObject);
        return array;
    }

    @Test
    public void executeFailureTestWithInvalidInputException() throws Exception {

        when(PacmanUtils.getPacmanHost(anyString())).thenReturn("host");
        when(GCPUtils.getHitsArrayFromEs(anyObject(), anyObject())).thenReturn(getFailureHitsJsonArrayForEnableHttpsRule());

        when(PacmanUtils.createAnnotation(anyString(), anyObject(), anyString(), anyString(), anyString())).thenReturn(CommonTestUtils.getAnnotation("123"));
        when(PacmanUtils.doesAllHaveValue(anyString(), anyString(), anyString())).thenReturn(
                false);
        Map<String, String> map = getMapString("r_123 ");
        assertThatThrownBy(() -> vpnConnectorRule.execute(map, map)).isInstanceOf(InvalidInputException.class);
    }


    public static Map<String, String> getMapString(String passRuleResourceId) {
        Map<String, String> commonMap = new HashMap<>();
        commonMap.put("executionId", "1234");
        commonMap.put("_resourceid", passRuleResourceId);
        commonMap.put("severity", "medium");
        commonMap.put("ruleCategory", "security");
        commonMap.put("accountid", "12345");
        commonMap.put("policyId", "GCP_Cloud_Function_not_enabled_with_VPC_connector");
        commonMap.put("policyVersion", "version-1");


        return commonMap;
    }

    @Test
    public void getHelpTextTest() {
        assertThat(vpnConnectorRule.getHelpText(), is(notNullValue()));
    }
}
