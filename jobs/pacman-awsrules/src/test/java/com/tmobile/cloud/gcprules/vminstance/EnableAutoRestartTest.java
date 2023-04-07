package com.tmobile.cloud.gcprules.vminstance;

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
import com.tmobile.pacman.commons.policy.Annotation;
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
@PrepareForTest({PacmanUtils.class, GCPUtils.class, Annotation.class})
@PowerMockIgnore({"javax.net.ssl.*", "javax.management.*","jdk.internal.reflect.*"})
public class EnableAutoRestartTest {
    @InjectMocks
    EnableAutoRestart enableAutoRestart;

    @Before
    public void setUp() {
        mockStatic(PacmanUtils.class);
        mockStatic(GCPUtils.class);
        mockStatic(Annotation.class);
    }

    @Test
    public void executeSuccessTest() throws Exception {
        when(PacmanUtils.getPacmanHost(anyString())).thenReturn("host");
        when(GCPUtils.validateRuleParam(anyObject())).thenReturn(true);
        when(GCPUtils.getJsonObjFromSourceData(anyObject(), anyObject())).thenReturn(getHitsJsonObjectRule());
        when(Annotation.buildAnnotation(anyObject(), anyObject())).thenReturn(CommonTestUtils.getMockAnnotation());
        Map<String, String> map = getMapString("r_123 ");
        assertThat(enableAutoRestart.execute(getMapString("r_123 "), getMapString("r_123 ")).getStatus(), is(PacmanSdkConstants.STATUS_SUCCESS));

    }

    @Test
    public void executeFailureTest() throws Exception {
        when(PacmanUtils.getPacmanHost(anyString())).thenReturn("host");
        when(GCPUtils.validateRuleParam(anyObject())).thenReturn(true);
        when(GCPUtils.getJsonObjFromSourceData(anyObject(), anyObject())).thenReturn(getFailureHitsJsonObjRule());
        when(GCPUtils.fetchPolicyResult(anyObject(), anyObject(), anyObject())).thenReturn(new PolicyResult(PacmanSdkConstants.STATUS_FAILURE, PacmanRuleConstants.FAILURE_MESSAGE,
                anyObject()));
        assertThat(enableAutoRestart.execute(getMapString("r_123 "), getMapString("r_123 ")).getStatus(), is(PacmanSdkConstants.STATUS_FAILURE));
    }

    @Test
    public void executeFailureTestWithInvalidInputException() throws Exception {
        when(PacmanUtils.getPacmanHost(anyString())).thenReturn("host");
        when(GCPUtils.getHitsArrayFromEs(anyObject(), anyObject())).thenReturn(getFailureJsonArrayRule());
        when(Annotation.buildAnnotation(anyObject(), anyObject())).thenReturn(CommonTestUtils.getMockAnnotation());
        when(PacmanUtils.doesAllHaveValue(anyString(), anyString(), anyString())).thenReturn(
                false);
        Map<String, String> map = getMapString("r_123 ");
        assertThatThrownBy(() -> enableAutoRestart.execute(map, map)).isInstanceOf(InvalidInputException.class);
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
                "{\"discoveryDate\":\"2022-09-3014:00:00+0530\",\"_cloudType\":\"GCP\",\"region\":\"us-central1-a\",\"id\":\"1443986351879888987\",\"projectName\":\"PaladinCloud\",\"projectId\":\"central-run-349616\",\"name\":\"dev-pc-test\",\"description\":\"\",\"disks\":[{\"discoveryDate\":null,\"_cloudType\":\"GCP\",\"region\":null,\"id\":\"0\",\"projectName\":\"PaladinCloud\",\"projectId\":null,\"name\":\"dev-pc-test\",\"sizeInGB\":10,\"type\":\"PERSISTENT\",\"hasSha256\":false,\"hasKmsKeyName\":false,\"discoverydate\":null}],\"tags\":{\"application\":\"paladincloud-gcp\",\"enviornment\":\"demo\"},\"machineType\":\"https://www.googleapis.com/compute/v1/projects/central-run-349616/zones/us-central1-a/machineTypes/e2-medium\",\"status\":\"RUNNING\",\"networkInterfaces\":[{\"discoveryDate\":null,\"_cloudType\":\"GCP\",\"region\":null,\"id\":\"nic0\",\"projectName\":null,\"projectId\":null,\"name\":\"nic0\",\"accessConfigs\":[{\"discoveryDate\":null,\"_cloudType\":\"GCP\",\"region\":null,\"id\":\"ExternalNAT\",\"projectName\":\"PaladinCloud\",\"projectId\":null,\"name\":\"ExternalNAT\",\"natIP\":\"35.239.207.67\",\"discoverydate\":null}],\"network\":\"https://www.googleapis.com/compute/v1/projects/central-run-349616/global/networks/default\",\"discoverydate\":null}],\"items\":[{\"discoveryDate\":null,\"_cloudType\":\"GCP\",\"region\":null,\"id\":null,\"projectName\":null,\"projectId\":null,\"key\":\"block-project-ssh-keys\",\"value\":\"true\",\"discoverydate\":null}],\"serviceAccounts\":[{\"scopeList\":[\"https://www.googleapis.com/auth/cloud-platform\"],\"autoRestart\":false,\"emailBytes\":{\"validUtf8\":true,\"empty\":false},\"email\":\"344106022091-compute@developer.gserviceaccount.com\"}],\"onHostMaintainence\":\"MIGRATE\",\"shieldedInstanceConfig\":{\"enableVtpm\":true,\"enableIntegrityMonitoring\":true},\"projectNumber\":\"344106022091\",\"scopesList\":[\"https://www.googleapis.com/auth/cloud-platform\"],\"autoRestart\":false,\"emailList\":[\"pacbot-inventory@central-run-349616.iam.gserviceaccount.com\"],\"discoverydate\":\"2022-09-3014:00:00+0530\"}",
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
                "{\"discoveryDate\":\"2022-09-3014:00:00+0530\",\"_cloudType\":\"GCP\",\"region\":\"us-central1-a\",\"id\":\"1443986351879888987\",\"projectName\":\"PaladinCloud\",\"projectId\":\"central-run-349616\",\"name\":\"dev-pc-test\",\"description\":\"\",\"disks\":[{\"discoveryDate\":null,\"_cloudType\":\"GCP\",\"region\":null,\"id\":\"0\",\"projectName\":\"PaladinCloud\",\"projectId\":null,\"name\":\"dev-pc-test\",\"sizeInGB\":10,\"type\":\"PERSISTENT\",\"hasSha256\":false,\"hasKmsKeyName\":false,\"discoverydate\":null}],\"tags\":{\"application\":\"paladincloud-gcp\",\"enviornment\":\"demo\"},\"machineType\":\"https://www.googleapis.com/compute/v1/projects/central-run-349616/zones/us-central1-a/machineTypes/e2-medium\",\"status\":\"RUNNING\",\"networkInterfaces\":[{\"discoveryDate\":null,\"_cloudType\":\"GCP\",\"region\":null,\"id\":\"nic0\",\"projectName\":null,\"projectId\":null,\"name\":\"nic0\",\"accessConfigs\":[{\"discoveryDate\":null,\"_cloudType\":\"GCP\",\"region\":null,\"id\":\"ExternalNAT\",\"projectName\":\"PaladinCloud\",\"projectId\":null,\"name\":\"ExternalNAT\",\"natIP\":\"35.239.207.67\",\"discoverydate\":null}],\"network\":\"https://www.googleapis.com/compute/v1/projects/central-run-349616/global/networks/default\",\"discoverydate\":null}],\"items\":[{\"discoveryDate\":null,\"_cloudType\":\"GCP\",\"region\":null,\"id\":null,\"projectName\":null,\"projectId\":null,\"key\":\"block-project-ssh-keys\",\"value\":\"true\",\"discoverydate\":null}],\"serviceAccounts\":[{\"scopeList\":[\"https://www.googleapis.com/auth/cloud-platform\"],\"autoRestart\":true,\"emailBytes\":{\"validUtf8\":true,\"empty\":false},\"email\":\"344106022091-compute@developer.gserviceaccount.com\"}],\"onHostMaintainence\":\"MIGRATE\",\"shieldedInstanceConfig\":{\"enableVtpm\":true,\"enableIntegrityMonitoring\":true},\"projectNumber\":\"344106022091\",\"scopesList\":[\"https://www.googleapis.com/auth/cloud-platform\"],\"autoRestart\":true,\"emailList\":[\"pacbot-inventory@central-run-349616.iam.gserviceaccount.com\"],\"discoverydate\":\"2022-09-3014:00:00+0530\"}",
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
        assertThat(enableAutoRestart.getHelpText(), is(notNullValue()));
    }
}
