package com.tmobile.cloud.gcprules.cloudfunctions;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.tmobile.cloud.awsrules.utils.CommonTestUtils;
import com.tmobile.cloud.awsrules.utils.PacmanUtils;
import com.tmobile.cloud.gcprules.utils.GCPUtils;
import com.tmobile.pacman.commons.PacmanSdkConstants;
import com.tmobile.pacman.commons.exception.InvalidInputException;
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
public class HttpTriggersTest {

    @InjectMocks
    HttpTriggers httpTriggerRule;

    @Before
    public void setUp() {
        mockStatic(PacmanUtils.class);
        mockStatic(GCPUtils.class);
    }


    @Test
    public void executeSuccessTest() throws Exception {

        when(PacmanUtils.getPacmanHost(anyString())).thenReturn("host");
        when(GCPUtils.getHitsArrayFromEs(anyObject(), anyObject())).thenReturn(getHitsJsonArrayForEnableHttpsRule());

        when(PacmanUtils.createAnnotation(anyString(), anyObject(), anyString(), anyString(), anyString()))
                .thenReturn(CommonTestUtils.getAnnotation("123"));
        when(PacmanUtils.doesAllHaveValue(anyString(), anyString(), anyString())).thenReturn(
                true);
        assertThat(httpTriggerRule.execute(getMapString("r_123 "), getMapString("r_123 ")).getStatus(),
                is(PacmanSdkConstants.STATUS_SUCCESS));

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
                        "          \"httpTrigger\": \"1\",\n" +
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
        when(GCPUtils.getHitsArrayFromEs(anyObject(), anyObject()))
                .thenReturn(getFailureHitsJsonArrayForEnableHttpsRule());

        when(PacmanUtils.createAnnotation(anyString(), anyObject(), anyString(), anyString(), anyString()))
                .thenReturn(CommonTestUtils.getAnnotation("123"));
        when(PacmanUtils.doesAllHaveValue(anyString(), anyString(), anyString())).thenReturn(
                true);
        assertThat(httpTriggerRule.execute(getMapString("r_123 "), getMapString("r_123 ")).getStatus(),
                is(PacmanSdkConstants.STATUS_FAILURE));
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
                        "          \"vpcConnector\": null,\n" +
                        "          \"httpTrigger\": \"2\",\n" +
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
        assertThatThrownBy(() -> httpTriggerRule.execute(map, map)).isInstanceOf(InvalidInputException.class);
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
        assertThat(httpTriggerRule.getHelpText(), is(notNullValue()));
    }
}
