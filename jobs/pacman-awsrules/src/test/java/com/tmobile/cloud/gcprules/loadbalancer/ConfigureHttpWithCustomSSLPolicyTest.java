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
public class ConfigureHttpWithCustomSSLPolicyTest {
    @InjectMocks
    ConfigureHttpWithCustomSSLPolicy configureHttpWithCustomSSLPolicy;
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
        assertThat(configureHttpWithCustomSSLPolicy.execute(getMapString("r_123 "), getMapString("r_123 ")).getStatus(),
                is(PacmanSdkConstants.STATUS_SUCCESS));

    }
    private JsonArray getHitsJsonArrayForEnableHttpsRule(){
        Gson gson = new Gson();
        JsonObject jsonObject = new JsonObject();
        jsonObject.add("_source", gson.fromJson(
                "{\"discoveryDate\":\"2023-01-23 11:00:00+0530\",\"_cloudType\":\"GCP\",\"region\":null,\"id\":\"8754306309307615551\",\"projectName\":null,\"projectId\":null,\"urlMap\":\"loadbalancer2\",\"targetHttpsProxy\":[\"loadbalancer2-target-proxy-2\",\"loadbalancer2-target-proxy-4\"],\"httpProxyDetailList\":[{\"name\":\"loadbalancer2-target-proxy-2\",\"hasCustomPolicy\":true},{\"name\":\"loadbalancer2-target-proxy-4\",\"hasCustomPolicy\":true}],\"logConfigEnabled\":true,\"sslPolicyList\":[{\"discoveryDate\":null,\"_cloudType\":\"GCP\",\"region\":null,\"id\":null,\"projectName\":null,\"projectId\":null,\"minTlsVersion\":\"TLS_1_2\",\"profile\":\"MODERN\",\"enabledFeatures\":[],\"discoverydate\":null}],\"discoverydate\":\"2023-01-23 11:00:00+0530\"}" ,
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
        assertThat(configureHttpWithCustomSSLPolicy.execute(getMapString("r_123 "), getMapString("r_123 ")).getStatus(),
                is(PacmanSdkConstants.STATUS_FAILURE));
    }


    private JsonArray getFailureHitsJsonArrayForEnableHttpsRule() {
        Gson gson = new Gson();
        JsonObject jsonObject = new JsonObject();
        jsonObject.add("_source", gson.fromJson(
                "{\"discoveryDate\":\"2023-01-23 11:00:00+0530\",\"_cloudType\":\"GCP\",\"region\":null,\"id\":\"8754306309307615551\",\"projectName\":null,\"projectId\":null,\"urlMap\":\"loadbalancer2\",\"targetHttpsProxy\":[\"loadbalancer2-target-proxy-2\",\"loadbalancer2-target-proxy-4\"],\"httpProxyDetailList\":[{\"name\":\"loadbalancer2-target-proxy-2\",\"hasCustomPolicy\":false},{\"name\":\"loadbalancer2-target-proxy-4\",\"hasCustomPolicy\":false}],\"logConfigEnabled\":true,\"sslPolicyList\":[{\"discoveryDate\":null,\"_cloudType\":\"GCP\",\"region\":null,\"id\":null,\"projectName\":null,\"projectId\":null,\"minTlsVersion\":\"TLS_1_2\",\"profile\":\"MODERN\",\"enabledFeatures\":[],\"discoverydate\":null}],\"discoverydate\":\"2023-01-23 11:00:00+0530\"}",
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
        assertThatThrownBy(() -> configureHttpWithCustomSSLPolicy.execute(getMapString("r_123 "), getMapString("r_123 "))).isInstanceOf(InvalidInputException.class);
    }


    public static Map<String, String> getMapString(String passRuleResourceId) {
        Map<String, String> commonMap = new HashMap<>();
        commonMap.put("executionId", "1234");
        commonMap.put("_resourceid", passRuleResourceId);
        commonMap.put("severity", "medium");
        commonMap.put("ruleCategory", "security");
        commonMap.put("accountid", "12345");
        commonMap.put("ruleId", "Configure_custom_ssl_for_https");
        commonMap.put("policyId", "Configure_custom_ssl_for_https");
        commonMap.put("policyVersion", "version-1");


        return commonMap;
    }

    @Test
    public void getHelpTextTest() {
        assertThat(configureHttpWithCustomSSLPolicy.getHelpText(), is(notNullValue()));
    }
}
