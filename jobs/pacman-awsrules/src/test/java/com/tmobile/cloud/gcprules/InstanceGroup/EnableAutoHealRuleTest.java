package com.tmobile.cloud.gcprules.InstanceGroup;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.tmobile.cloud.awsrules.utils.CommonTestUtils;
import com.tmobile.cloud.awsrules.utils.PacmanUtils;
import com.tmobile.cloud.constants.PacmanRuleConstants;
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
public class EnableAutoHealRuleTest {
    @InjectMocks
    EnableAutoHealRule enableAutoHealRule;

    @Before
    public void setUp() {
        mockStatic(PacmanUtils.class);
        mockStatic(GCPUtils.class);
    }

    @Test
    public void executeSuccessTest() throws Exception {
        when(PacmanUtils.getPacmanHost(anyString())).thenReturn("host");
        when(GCPUtils.validateRuleParam(anyObject())).thenReturn(true);
        when(GCPUtils.getJsonObjFromSourceData(anyObject(), anyObject())).thenReturn(getHitsJsonObjectRule());
        when(PacmanUtils.createAnnotation(anyString(), anyObject(), anyString(), anyString(), anyString()))
                .thenReturn(CommonTestUtils.getAnnotation("123"));
        Map<String, String> map = getMapString("r_123 ");
        assertThat(enableAutoHealRule.execute(getMapString("r_123 "), getMapString("r_123 ")).getStatus(),
                is(PacmanSdkConstants.STATUS_SUCCESS));

    }

    @Test
    public void executeFailureTest() throws Exception {
        when(PacmanUtils.getPacmanHost(anyString())).thenReturn("host");
        when(GCPUtils.validateRuleParam(anyObject())).thenReturn(true);
        when(GCPUtils.getJsonObjFromSourceData(anyObject(), anyObject())).thenReturn(getFailureHitsJsonObjRule());
        when(GCPUtils.fetchPolicyResult(anyObject(), anyObject(), anyObject())).thenReturn(new PolicyResult(PacmanSdkConstants.STATUS_FAILURE, PacmanRuleConstants.FAILURE_MESSAGE,
                anyObject()));
        assertThat(enableAutoHealRule.execute(getMapString("r_123 "), getMapString("r_123 ")).getStatus(), is(PacmanSdkConstants.STATUS_FAILURE));
    }

    @Test
    public void executeFailureTestWithInvalidInputException() throws Exception {

        when(PacmanUtils.getPacmanHost(anyString())).thenReturn("host");
        when(GCPUtils.getHitsArrayFromEs(anyObject(), anyObject())).thenReturn(getFailureJsonArrayRule());

        when(PacmanUtils.createAnnotation(anyString(), anyObject(), anyString(), anyString(), anyString())).thenReturn(CommonTestUtils.getAnnotation("123"));
        when(PacmanUtils.doesAllHaveValue(anyString(), anyString(), anyString())).thenReturn(
                false);
        Map<String, String> map = getMapString("r_123 ");
        assertThatThrownBy(() -> enableAutoHealRule.execute(map, map)).isInstanceOf(InvalidInputException.class);
    }


    public static Map<String, String> getMapString(String passRuleResourceId) {
        Map<String, String> commonMap = new HashMap<>();
        commonMap.put(PacmanRuleConstants.EXECUTION_ID, "1234");
        commonMap.put(PacmanRuleConstants.RESOURCE_ID, passRuleResourceId);
        commonMap.put(PacmanRuleConstants.SEVERITY, "medium");
        commonMap.put(PacmanRuleConstants.CATEGORY, "security");
        commonMap.put(PacmanRuleConstants.ACCOUNTID, "12345");
        commonMap.put(PacmanRuleConstants.POLICY_ID, "Enable_Auto_Restart_Vm_Instance");
        commonMap.put("policyVersion", "version-1");
        commonMap.put(PacmanRuleConstants.ES_URL_PARAM, "/gcp_vminstance/_search");
        return commonMap;
    }

    public static JsonArray getFailureJsonArrayRule() {
        Gson gson = new Gson();
        JsonObject jsonObject = new JsonObject();
        jsonObject.add("_source", gson.fromJson(
                "{\"_cloudType\":\"GCP\",\"region\":\"us-central1-a\",\"id\":\"1989258739626373651\",\"projectName\":\"Paladin Cloud\",\"projectId\":\"gke-cluster-1-pool-2-6dc35fd2-grp\",\"tags\":null,\"name\":\"gke-cluster-1-pool-2-6dc35fd2-grp\",\"managedInstance\":[{\"name\":\"https://www.googleapis.com/compute/v1/projects/central-run-349616/zones/us-central1-a/instances/gke-cluster-1-pool-2-6dc35fd2-jtqv\",\"instanceHealth\":[\"https://www.googleapis.com/compute/v1/projects/central-run-349616/global/healthChecks/healthcheck\"],\"instanceHealthCount\":1},{\"name\":\"https://www.googleapis.com/compute/v1/projects/central-run-349616/zones/us-central1-a/instances/gke-cluster-1-pool-2-6dc35fd2-p8to\",\"instanceHealth\":[\"https://www.googleapis.com/compute/v1/projects/central-run-349616/global/healthChecks/healthcheck\"],\"instanceHealthCount\":1},{\"name\":\"https://www.googleapis.com/compute/v1/projects/central-run-349616/zones/us-central1-a/instances/gke-cluster-1-pool-2-6dc35fd2-q2if\",\"instanceHealth\":[\"https://www.googleapis.com/compute/v1/projects/central-run-349616/global/healthChecks/healthcheck\"],\"instanceHealthCount\":0}],\"discoverydate\":\"2023-02-27 15:00:00+0530\"}",
                JsonElement.class));

        JsonArray array = new JsonArray();
        array.add(jsonObject);
        return array;
    }

    private JsonObject getFailureHitsJsonObjRule() {
        JsonArray hitsJsonArray = getFailureJsonArrayRule();
        JsonObject sourceData= null;
        if (hitsJsonArray != null && hitsJsonArray.size() > 0){
            sourceData = (JsonObject) ((JsonObject) hitsJsonArray.get(0))
                    .get(PacmanRuleConstants.SOURCE);
        }
        return sourceData;
    }

    public static JsonArray getHitsJsonArrayRule() {
        Gson gson = new Gson();
        JsonObject jsonObject = new JsonObject();
        jsonObject.add("_source", gson.fromJson(
                "{\"_cloudType\":\"GCP\",\"region\":\"us-central1-a\",\"id\":\"1989258739626373651\",\"projectName\":\"Paladin Cloud\",\"projectId\":\"gke-cluster-1-pool-2-6dc35fd2-grp\",\"tags\":null,\"name\":\"gke-cluster-1-pool-2-6dc35fd2-grp\",\"managedInstance\":[{\"name\":\"https://www.googleapis.com/compute/v1/projects/central-run-349616/zones/us-central1-a/instances/gke-cluster-1-pool-2-6dc35fd2-jtqv\",\"instanceHealth\":[\"https://www.googleapis.com/compute/v1/projects/central-run-349616/global/healthChecks/healthcheck\"],\"instanceHealthCount\":1},{\"name\":\"https://www.googleapis.com/compute/v1/projects/central-run-349616/zones/us-central1-a/instances/gke-cluster-1-pool-2-6dc35fd2-p8to\",\"instanceHealth\":[\"https://www.googleapis.com/compute/v1/projects/central-run-349616/global/healthChecks/healthcheck\"],\"instanceHealthCount\":1},{\"name\":\"https://www.googleapis.com/compute/v1/projects/central-run-349616/zones/us-central1-a/instances/gke-cluster-1-pool-2-6dc35fd2-q2if\",\"instanceHealth\":[\"https://www.googleapis.com/compute/v1/projects/central-run-349616/global/healthChecks/healthcheck\"],\"instanceHealthCount\":1}],\"discoverydate\":\"2023-02-27 15:00:00+0530\"}",
                JsonElement.class));

        JsonArray array = new JsonArray();
        array.add(jsonObject);
        return array;
    }
    private JsonObject getHitsJsonObjectRule() {
        JsonArray hitsJsonArray = getHitsJsonArrayRule();
        JsonObject sourceData= null;
        if (hitsJsonArray != null && hitsJsonArray.size() > 0){
            sourceData = (JsonObject) ((JsonObject) hitsJsonArray.get(0))
                    .get(PacmanRuleConstants.SOURCE);
        }
        return sourceData;
    }


    @Test
    public void getHelpTextTest() {
        assertThat(enableAutoHealRule.getHelpText(), is(notNullValue()));
    }
}
