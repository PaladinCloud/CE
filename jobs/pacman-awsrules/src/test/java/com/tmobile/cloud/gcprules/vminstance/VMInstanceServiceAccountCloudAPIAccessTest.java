package com.tmobile.cloud.gcprules.vminstance;

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
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import java.util.HashMap;
import java.util.Map;

import static com.tmobile.cloud.gcprules.utils.TestUtils.*;
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
public class VMInstanceServiceAccountCloudAPIAccessTest {
    @InjectMocks
    VMInstanceServiceAccountCloudAPIAccess vmInstanceServiceAccountCloudAPIAccess;

    @Before
    public void setUp() {
        mockStatic(PacmanUtils.class);
        mockStatic(GCPUtils.class);
    }

    @Test
    public void executeSuccessTest() throws Exception {


        when(PacmanUtils.getPacmanHost(anyString())).thenReturn("host");
        when(GCPUtils.getHitsArrayFromEs(anyObject(), anyObject())).thenReturn(getHitsJsonForVMCloudAPISuccess());

        when(PacmanUtils.createAnnotation(anyString(), anyObject(), anyString(), anyString(), anyString())).thenReturn(CommonTestUtils.getAnnotation("123"));
        when(PacmanUtils.doesAllHaveValue(anyString(), anyString(), anyString())).thenReturn(
                true);
        assertThat(vmInstanceServiceAccountCloudAPIAccess.execute(getMapString("r_123 "), getMapString("r_123 ")).getStatus(), is(PacmanSdkConstants.STATUS_SUCCESS));

    }

    private JsonArray getHitsJsonForVMCloudAPISuccess() {
        Gson gson = new Gson();
        JsonObject jsonObject = new JsonObject();
        jsonObject.add("_source", gson.fromJson("{\n" +
                "          \"discoveryDate\": \"2022-09-29 17:00:00+0000\",\n" +
                "          \"_cloudType\": \"GCP\",\n" +
                "          \"region\": \"us-central1-a\",\n" +
                "          \"id\": \"1443986351879888987\",\n" +
                "          \"projectName\": \"Paladin Cloud\",\n" +
                "          \"projectId\": \"central-run-349616\",\n" +
                "          \"name\": \"dev-pc-test\",\n" +
                "          \"description\": \"\",\n" +
                "          \"disks\": [\n" +
                "            {\n" +
                "              \"discoveryDate\": null,\n" +
                "              \"_cloudType\": \"GCP\",\n" +
                "              \"region\": null,\n" +
                "              \"id\": \"0\",\n" +
                "              \"projectName\": \"Paladin Cloud\",\n" +
                "              \"projectId\": null,\n" +
                "              \"name\": \"dev-pc-test\",\n" +
                "              \"sizeInGB\": 10,\n" +
                "              \"type\": \"PERSISTENT\",\n" +
                "              \"hasSha256\": false,\n" +
                "              \"hasKmsKeyName\": false,\n" +
                "              \"discoverydate\": null\n" +
                "            }\n" +
                "          ],\n" +
                "          \"tags\": {\n" +
                "            \"application\": \"paladincloud-gcp\",\n" +
                "            \"enviornment\": \"demo\"\n" +
                "          },\n" +
                "          \"machineType\": \"https://www.googleapis.com/compute/v1/projects/central-run-349616/zones/us-central1-a/machineTypes/e2-medium\",\n" +
                "          \"status\": \"RUNNING\",\n" +
                "          \"networkInterfaces\": [\n" +
                "            {\n" +
                "              \"discoveryDate\": null,\n" +
                "              \"_cloudType\": \"GCP\",\n" +
                "              \"region\": null,\n" +
                "              \"id\": \"nic0\",\n" +
                "              \"projectName\": null,\n" +
                "              \"projectId\": null,\n" +
                "              \"name\": \"nic0\",\n" +
                "              \"accessConfigs\": [\n" +
                "                {\n" +
                "                  \"discoveryDate\": null,\n" +
                "                  \"_cloudType\": \"GCP\",\n" +
                "                  \"region\": null,\n" +
                "                  \"id\": \"External NAT\",\n" +
                "                  \"projectName\": \"Paladin Cloud\",\n" +
                "                  \"projectId\": null,\n" +
                "                  \"name\": \"External NAT\",\n" +
                "                  \"natIP\": \"35.239.207.67\",\n" +
                "                  \"discoverydate\": null\n" +
                "                }\n" +
                "              ],\n" +
                "              \"network\": \"https://www.googleapis.com/compute/v1/projects/central-run-349616/global/networks/default\",\n" +
                "              \"discoverydate\": null\n" +
                "            }\n" +
                "          ],\n" +
                "          \"items\": [\n" +
                "            {\n" +
                "              \"discoveryDate\": null,\n" +
                "              \"_cloudType\": \"GCP\",\n" +
                "              \"region\": null,\n" +
                "              \"id\": null,\n" +
                "              \"projectName\": null,\n" +
                "              \"projectId\": null,\n" +
                "              \"key\": \"block-project-ssh-keys\",\n" +
                "              \"value\": \"true\",\n" +
                "              \"discoverydate\": null\n" +
                "            }\n" +
                "          ],\n" +
                "          \"onHostMaintainence\": \"MIGRATE\",\n" +
                "          \"shieldedInstanceConfig\": {\n" +
                "            \"enableVtpm\": true,\n" +
                "            \"enableIntegrityMonitoring\": true\n" +
                "          },\n" +
                "          \"projectNumber\": \"344106022091\",\n" +
                "          \"scopesList\": [\n" +
                "            \"https://www.googleapis.com/auth/cloud-platform\"\n" +
                "          ],\n" +
                "          \"emailList\": [\n" +
                "            \"pacbot-inventory@central-run-349616.iam.gserviceaccount.com\"\n" +
                "          ],\n" +
                "          \"discoverydate\": \"2022-09-29 17:00:00+0000\",\n" +
                "          \"_resourceid\": \"1443986351879888987\",\n" +
                "          \"_docid\": \"1443986351879888987\",\n" +
                "          \"_entity\": \"true\",\n" +
                "          \"_entitytype\": \"vminstance\",\n" +
                "          \"firstdiscoveredon\": \"2022-08-18 15:00:00+0000\",\n" +
                "          \"latest\": true,\n" +
                "          \"_loaddate\": \"2022-09-29 17:46:00+0000\"\n" +
                "        }", JsonElement.class));

        JsonArray array = new JsonArray();
        array.add(jsonObject);
        return array;
    }
    private JsonArray getHitsJsonForVMCloudAPIFailure() {
        Gson gson = new Gson();
        JsonObject jsonObject = new JsonObject();
        jsonObject.add("_source", gson.fromJson("{\n" +
                "          \"discoveryDate\": \"2022-09-29 17:00:00+0000\",\n" +
                "          \"_cloudType\": \"GCP\",\n" +
                "          \"region\": \"us-central1-a\",\n" +
                "          \"id\": \"1443986351879888987\",\n" +
                "          \"projectName\": \"Paladin Cloud\",\n" +
                "          \"projectId\": \"central-run-349616\",\n" +
                "          \"name\": \"dev-pc-test\",\n" +
                "          \"description\": \"\",\n" +
                "          \"disks\": [\n" +
                "            {\n" +
                "              \"discoveryDate\": null,\n" +
                "              \"_cloudType\": \"GCP\",\n" +
                "              \"region\": null,\n" +
                "              \"id\": \"0\",\n" +
                "              \"projectName\": \"Paladin Cloud\",\n" +
                "              \"projectId\": null,\n" +
                "              \"name\": \"dev-pc-test\",\n" +
                "              \"sizeInGB\": 10,\n" +
                "              \"type\": \"PERSISTENT\",\n" +
                "              \"hasSha256\": false,\n" +
                "              \"hasKmsKeyName\": false,\n" +
                "              \"discoverydate\": null\n" +
                "            }\n" +
                "          ],\n" +
                "          \"tags\": {\n" +
                "            \"application\": \"paladincloud-gcp\",\n" +
                "            \"enviornment\": \"demo\"\n" +
                "          },\n" +
                "          \"machineType\": \"https://www.googleapis.com/compute/v1/projects/central-run-349616/zones/us-central1-a/machineTypes/e2-medium\",\n" +
                "          \"status\": \"RUNNING\",\n" +
                "          \"networkInterfaces\": [\n" +
                "            {\n" +
                "              \"discoveryDate\": null,\n" +
                "              \"_cloudType\": \"GCP\",\n" +
                "              \"region\": null,\n" +
                "              \"id\": \"nic0\",\n" +
                "              \"projectName\": null,\n" +
                "              \"projectId\": null,\n" +
                "              \"name\": \"nic0\",\n" +
                "              \"accessConfigs\": [\n" +
                "                {\n" +
                "                  \"discoveryDate\": null,\n" +
                "                  \"_cloudType\": \"GCP\",\n" +
                "                  \"region\": null,\n" +
                "                  \"id\": \"External NAT\",\n" +
                "                  \"projectName\": \"Paladin Cloud\",\n" +
                "                  \"projectId\": null,\n" +
                "                  \"name\": \"External NAT\",\n" +
                "                  \"natIP\": \"35.239.207.67\",\n" +
                "                  \"discoverydate\": null\n" +
                "                }\n" +
                "              ],\n" +
                "              \"network\": \"https://www.googleapis.com/compute/v1/projects/central-run-349616/global/networks/default\",\n" +
                "              \"discoverydate\": null\n" +
                "            }\n" +
                "          ],\n" +
                "          \"items\": [\n" +
                "            {\n" +
                "              \"discoveryDate\": null,\n" +
                "              \"_cloudType\": \"GCP\",\n" +
                "              \"region\": null,\n" +
                "              \"id\": null,\n" +
                "              \"projectName\": null,\n" +
                "              \"projectId\": null,\n" +
                "              \"key\": \"block-project-ssh-keys\",\n" +
                "              \"value\": \"true\",\n" +
                "              \"discoverydate\": null\n" +
                "            }\n" +
                "          ],\n" +
                "          \"onHostMaintainence\": \"MIGRATE\",\n" +
                "          \"shieldedInstanceConfig\": {\n" +
                "            \"enableVtpm\": true,\n" +
                "            \"enableIntegrityMonitoring\": true\n" +
                "          },\n" +
                "          \"projectNumber\": \"344106022091\",\n" +
                "          \"scopesList\": [\n" +
                "            \"https://www.googleapis.com/auth/cloud-platform\"\n" +
                "          ],\n" +
                "          \"emailList\": [\n" +
                "            \"344106022091-compute@developer.gserviceaccount.com\"\n" +
                "          ],\n" +
                "          \"discoverydate\": \"2022-09-29 17:00:00+0000\",\n" +
                "          \"_resourceid\": \"1443986351879888987\",\n" +
                "          \"_docid\": \"1443986351879888987\",\n" +
                "          \"_entity\": \"true\",\n" +
                "          \"_entitytype\": \"vminstance\",\n" +
                "          \"firstdiscoveredon\": \"2022-08-18 15:00:00+0000\",\n" +
                "          \"latest\": true,\n" +
                "          \"_loaddate\": \"2022-09-29 17:46:00+0000\"\n" +
                "        }", JsonElement.class));

        JsonArray array = new JsonArray();
        array.add(jsonObject);
        return array;
    }
    @Test
    public void executeFailureTest() throws Exception {


        when(PacmanUtils.getPacmanHost(anyString())).thenReturn("host");
        when(GCPUtils.getHitsArrayFromEs(anyObject(), anyObject())).thenReturn(getHitsJsonForVMCloudAPIFailure());

        when(PacmanUtils.createAnnotation(anyString(), anyObject(), anyString(), anyString(), anyString())).thenReturn(CommonTestUtils.getAnnotation("123"));
        when(PacmanUtils.doesAllHaveValue(anyString(), anyString(), anyString())).thenReturn(
                true);
        assertThat(vmInstanceServiceAccountCloudAPIAccess.execute(getMapString("r_123 "), getMapString("r_123 ")).getStatus(), is(PacmanSdkConstants.STATUS_FAILURE));
    }

    @Test
    public void executeFailureTestWithInvalidInputException() throws Exception {

        when(PacmanUtils.getPacmanHost(anyString())).thenReturn("host");
        when(GCPUtils.getHitsArrayFromEs(anyObject(), anyObject())).thenReturn(getHitsJsonArrayForVM2FAFailure());

        when(PacmanUtils.createAnnotation(anyString(), anyObject(), anyString(), anyString(), anyString())).thenReturn(CommonTestUtils.getAnnotation("123"));
        when(PacmanUtils.doesAllHaveValue(anyString(), anyString(), anyString())).thenReturn(
                false);
        assertThatThrownBy(() -> vmInstanceServiceAccountCloudAPIAccess.execute(getMapString("r_123 "), getMapString("r_123 "))).isInstanceOf(InvalidInputException.class);
    }


    public static Map<String, String> getMapString(String passRuleResourceId) {
        Map<String, String> commonMap = new HashMap<>();
        commonMap.put("executionId", "1234");
        commonMap.put("_resourceid", passRuleResourceId);
        commonMap.put("severity", "medium");
        commonMap.put("ruleCategory", "security");
        commonMap.put("accountid", "12345");
        commonMap.put("ruleId", "GCP_VM_Instance_ServiceAccount_Full_CloudAPI_Access");
        commonMap.put("policyId", "GCP_VM_Instance_ServiceAccount_Full_CloudAPI_Access");
        commonMap.put("policyVersion", "version-1");
        return commonMap;
    }

    @Test
    public void getHelpTextTest() {
        assertThat(vmInstanceServiceAccountCloudAPIAccess.getHelpText(), is(notNullValue()));
    }
}
