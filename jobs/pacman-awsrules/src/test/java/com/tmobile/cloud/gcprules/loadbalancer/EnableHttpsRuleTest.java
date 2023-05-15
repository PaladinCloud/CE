package com.tmobile.cloud.gcprules.loadbalancer;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.tmobile.cloud.awsrules.utils.CommonTestUtils;
import com.tmobile.cloud.awsrules.utils.PacmanUtils;
import com.tmobile.cloud.gcprules.utils.GCPUtils;
import com.tmobile.pacman.commons.PacmanSdkConstants;
import com.tmobile.pacman.commons.exception.InvalidInputException;
import com.tmobile.pacman.commons.policy.Annotation;
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
@PrepareForTest({PacmanUtils.class, GCPUtils.class, Annotation.class})
@PowerMockIgnore({"javax.net.ssl.*", "javax.management.*","jdk.internal.reflect.*"})
public class EnableHttpsRuleTest {
    @InjectMocks
    EnableHttpsRule enableHttpsRule;
    @Before
    public void setUp() {
        mockStatic(PacmanUtils.class);
        mockStatic(GCPUtils.class);
        mockStatic(Annotation.class);
    }


    @Test
    public void executeSuccessTest() throws Exception {

        when(PacmanUtils.getPacmanHost(anyString())).thenReturn("host");
        when(GCPUtils.getHitsArrayFromEs(anyObject(), anyObject())).thenReturn(getHitsJsonArrayForEnableHttpsRule());

        when(Annotation.buildAnnotation(anyObject(), anyObject())).thenReturn(CommonTestUtils.getMockAnnotation());
        when(PacmanUtils.doesAllHaveValue(anyString(), anyString(), anyString())).thenReturn(
                true);
        assertThat(enableHttpsRule.execute(getMapString("r_123 "), getMapString("r_123 ")).getStatus(),
                is(PacmanSdkConstants.STATUS_SUCCESS));

    }
    private JsonArray getHitsJsonArrayForEnableHttpsRule(){
        Gson gson = new Gson();
        JsonObject jsonObject = new JsonObject();
        jsonObject.add("_source", gson.fromJson(
                " {\n" +
                        "          \"discoveryDate\": \"2022-12-08 17:00:00+0530\",\n" +
                        "          \"_cloudType\": \"GCP\",\n" +
                        "          \"region\": null,\n" +
                        "          \"id\": \"4432585077905277180\",\n" +
                        "          \"projectName\": null,\n" +
                        "          \"projectId\": null,\n" +
                        "          \"urlMap\": \"new-loadbalancer\",\n" +
                        "          \"targetHttpsProxy\": [\n" +
                        "            \"new-loadbalancer-target-proxy\"\n" +
                        "          ],\n" +
                        "          \"discoverydate\": \"2022-12-08 17:00:00+0530\",\n" +
                        "          \"_resourceid\": \"4432585077905277180\",\n" +
                        "          \"_docid\": \"4432585077905277180\",\n" +
                        "          \"_entity\": \"true\",\n" +
                        "          \"_entitytype\": \"gcploadbalancer\",\n" +
                        "          \"firstdiscoveredon\": \"2022-12-07 17:00:00+0530\",\n" +
                        "          \"latest\": true,\n" +
                        "          \"_loaddate\": \"2022-12-08 11:48:00+0000\"\n" +
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

        when(Annotation.buildAnnotation(anyObject(), anyObject())).thenReturn(CommonTestUtils.getMockAnnotation());
        when(PacmanUtils.doesAllHaveValue(anyString(), anyString(), anyString())).thenReturn(
                true);
        assertThat(enableHttpsRule.execute(getMapString("r_123 "), getMapString("r_123 ")).getStatus(),
                is(PacmanSdkConstants.STATUS_FAILURE));
    }


    private JsonArray getFailureHitsJsonArrayForEnableHttpsRule() {
        Gson gson = new Gson();
        JsonObject jsonObject = new JsonObject();
        jsonObject.add("_source", gson.fromJson(
                "{\n" +
                        "          \"_cloudType\" : \"GCP\",\n" +
                        "          \"region\" : \"\",\n" +
                        "          \"id\" : \"8754306309307615551\",\n" +
                        "          \"projectName\" : \"Paladin Cloud\",\n" +
                        "          \"projectId\" : \"central-run-349616\",\n" +
                        "          \"tags\" : null,\n" +
                        "          \"urlMap\" : \"loadbalancer2\",\n" +
                        "          \"targetHttpsProxy\" : [\n" +
                        "            \"loadbalancer2-target-proxy-2\",\n" +
                        "            \"loadbalancer2-target-proxy-4\"\n" +
                        "          ],\n" +
                        "          \"httpProxyDetailList\" : [\n" +
                        "            {\n" +
                        "              \"name\" : \"loadbalancer2-target-proxy-2\",\n" +
                        "              \"hasCustomPolicy\" : true\n" +
                        "            },\n" +
                        "            {\n" +
                        "              \"name\" : \"loadbalancer2-target-proxy-4\",\n" +
                        "              \"hasCustomPolicy\" : false\n" +
                        "            }\n" +
                        "          ],\n" +
                        "          \"logConfigEnabled\" : true,\n" +
                        "          \"quicNegotiation\" : [\n" +
                        "            true,\n" +
                        "            false\n" +
                        "          ],\n" +
                        "          \"sslPolicyList\" : [\n" +
                        "            {\n" +
                        "              \"minTlsVersion\" : \"TLS_1_2\",\n" +
                        "              \"profile\" : \"MODERN\",\n" +
                        "              \"enabledFeatures\" : [ ]\n" +
                        "            }\n" +
                        "          ],\n" +
                        "          \"discoverydate\" : \"2023-04-24 11:00:00+0000\",\n" +
                        "          \"_resourceid\" : \"8754306309307615551\",\n" +
                        "          \"_docid\" : \"8754306309307615551\",\n" +
                        "          \"_entity\" : \"true\",\n" +
                        "          \"_entitytype\" : \"gcploadbalancer\",\n" +
                        "          \"docType\" : \"gcploadbalancer\",\n" +
                        "          \"gcploadbalancer_relations\" : \"gcploadbalancer\",\n" +
                        "          \"firstdiscoveredon\" : \"2023-04-24 11:00:00+0000\",\n" +
                        "          \"latest\" : true,\n" +
                        "          \"_loaddate\" : \"2023-04-24 11:14:00+0000\"\n" +
                        "        }",
                JsonElement.class));
        JsonArray array = new JsonArray();
        array.add(jsonObject);
        return array;
    }

    @Test
    public void executeFailureTestWithInvalidInputException() throws Exception {

        when(PacmanUtils.getPacmanHost(anyString())).thenReturn("host");
        when(GCPUtils.getHitsArrayFromEs(anyObject(), anyObject())).thenReturn(getFailureHitsJsonArrayForEnableHttpsRule());

        when(Annotation.buildAnnotation(anyObject(), anyObject())).thenReturn(CommonTestUtils.getMockAnnotation());
        when(PacmanUtils.doesAllHaveValue(anyString(), anyString(), anyString())).thenReturn(
                false);
        assertThatThrownBy(() -> enableHttpsRule.execute(getMapString("r_123 "), getMapString("r_123 "))).isInstanceOf(InvalidInputException.class);
    }


    public static Map<String, String> getMapString(String passRuleResourceId) {
        Map<String, String> commonMap = new HashMap<>();
        commonMap.put("executionId", "1234");
        commonMap.put("_resourceid", passRuleResourceId);
        commonMap.put("severity", "high");
        commonMap.put("ruleCategory", "security");
        commonMap.put("accountid", "12345");
        commonMap.put("ruleId", "Enable_HTTPS_For_LoadBalancer");
        commonMap.put("policyId", "Enable_HTTPS_For_LoadBalancer");
        commonMap.put("policyVersion", "version-1");


        return commonMap;
    }

    @Test
    public void getHelpTextTest() {
        assertThat(enableHttpsRule.getHelpText(), is(notNullValue()));
    }
}
