package com.tmobile.cloud.gcprules.iam;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.tmobile.cloud.awsrules.utils.CommonTestUtils;
import com.tmobile.cloud.awsrules.utils.PacmanUtils;
import com.tmobile.cloud.gcprules.utils.GCPUtils;
import com.tmobile.pacman.commons.PacmanSdkConstants;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import java.util.HashMap;
import java.util.Map;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.anyObject;
import static org.mockito.Matchers.anyString;
import static org.powermock.api.mockito.PowerMockito.mockStatic;
import static org.powermock.api.mockito.PowerMockito.when;

@RunWith(PowerMockRunner.class)
@PrepareForTest({PacmanUtils.class, GCPUtils.class})
public class SeparationOfDutiesForKMSRuleTest {
    @InjectMocks
    SeparationOfDutiesForKMSRule separationOfDutiesForKMSRule;
    @Before
    public void setUp() {
        mockStatic(PacmanUtils.class);
        mockStatic(GCPUtils.class);
    }

    @Test
    public void executeSuccessTest() throws Exception {

        when(PacmanUtils.getPacmanHost(anyString())).thenReturn("host");
        when(GCPUtils.getHitsArrayFromEs(anyObject(), anyObject())).thenReturn(getHitsJsonForSeparationOfDutiesForKNSRoleSuccess());

        when(PacmanUtils.createAnnotation(anyString(), anyObject(), anyString(), anyString(), anyString()))
                .thenReturn(CommonTestUtils.getAnnotation("123"));
        when(PacmanUtils.doesAllHaveValue(anyString(), anyString(), anyString())).thenReturn(
                true);
        assertThat(separationOfDutiesForKMSRule.execute(getMapString("r_123 "), getMapString("r_123 ")).getStatus(),
                is(PacmanSdkConstants.STATUS_SUCCESS));

    }

    private JsonArray getHitsJsonForSeparationOfDutiesForKNSRoleSuccess() {
        Gson gson = new Gson();
        JsonObject jsonObject = new JsonObject();
        jsonObject.add("_source", gson.fromJson(
                "{\n" +
                        "          \"discoveryDate\": \"2022-11-29 18:00:00+0530\",\n" +
                        "          \"_cloudType\": \"GCP\",\n" +
                        "          \"region\": null,\n" +
                        "          \"id\": \"central-run-349616_anjali-madhavi.nakirikanti@paladincloud.io\",\n" +
                        "          \"projectName\": \"Paladin Cloud\",\n" +
                        "          \"projectId\": \"central-run-349616\",\n" +
                        "          \"userId\": \"user:anjali-madhavi.nakirikanti@paladincloud.io\",\n" +
                        "          \"email\": \"anjali-madhavi.nakirikanti@paladincloud.io\",\n" +
                        "          \"roles\": [\n" +
                        "            \"roles/cloudkms.cryptoKeyEncrypterDecrypter\",\n" +
                        "            \"roles/resourcemanager.projectIamAdmin\",\n" +
                        "            \"roles/cloudkms.cryptoKeyDecrypter\",\n" +
                        "            \"roles/resourcemanager.organizationAdmin\",\n" +
                        "            \"roles/owner\"\n" +
                        "          ],\n" +
                        "          \"discoverydate\": \"2022-11-29 18:00:00+0530\",\n" +
                        "          \"_resourceid\": \"central-run-349616_anjali-madhavi.nakirikanti@paladincloud.io\",\n" +
                        "          \"_docid\": \"central-run-349616_anjali-madhavi.nakirikanti@paladincloud.io\",\n" +
                        "          \"_entity\": \"true\",\n" +
                        "          \"_entitytype\": \"iamusers\",\n" +
                        "          \"firstdiscoveredon\": \"2022-11-29 18:00:00+0530\",\n" +
                        "          \"latest\": true,\n" +
                        "          \"_loaddate\": \"2022-11-29 12:49:00+0000\"\n" +
                        "        }",
                JsonElement.class));
        JsonArray array = new JsonArray();
        array.add(jsonObject);
        return array;
    }

    @Test
    public void executeFailureTest() throws Exception {

        when(PacmanUtils.getPacmanHost(anyString())).thenReturn("host");
        when(GCPUtils.getHitsArrayFromEs(anyObject(), anyObject()))
                .thenReturn(getHitsJsonForGKEIntegrityMonitoringFailure());

        when(PacmanUtils.createAnnotation(anyString(), anyObject(), anyString(), anyString(), anyString()))
                .thenReturn(CommonTestUtils.getAnnotation("123"));
        when(PacmanUtils.doesAllHaveValue(anyString(), anyString(), anyString())).thenReturn(
                true);
        assertThat(separationOfDutiesForKMSRule.execute(getMapString("r_123 "), getMapString("r_123 ")).getStatus(),
                is(PacmanSdkConstants.STATUS_FAILURE));
    }

    private JsonArray getHitsJsonForGKEIntegrityMonitoringFailure() {
        Gson gson = new Gson();
        JsonObject jsonObject = new JsonObject();
        jsonObject.add("_source", gson.fromJson(
                "{\n" +
                        "          \"discoveryDate\": \"2022-11-29 18:00:00+0530\",\n" +
                        "          \"_cloudType\": \"GCP\",\n" +
                        "          \"region\": null,\n" +
                        "          \"id\": \"central-run-349616_aishwarya.kulkarni@paladincloud.io\",\n" +
                        "          \"projectName\": \"Paladin Cloud\",\n" +
                        "          \"projectId\": \"central-run-349616\",\n" +
                        "          \"userId\": \"user:aishwarya.kulkarni@paladincloud.io\",\n" +
                        "          \"email\": \"aishwarya.kulkarni@paladincloud.io\",\n" +
                        "          \"roles\": [\n" +
                        "            \"roles/owner\",\n" +
                        "            \"roles/cloudkms.admin\",\n" +
                        "            \"roles/cloudkms.cryptoKeyEncrypter\"\n" +
                        "          ],\n" +
                        "          \"discoverydate\": \"2022-11-29 18:00:00+0530\",\n" +
                        "          \"_resourceid\": \"central-run-349616_aishwarya.kulkarni@paladincloud.io\",\n" +
                        "          \"_docid\": \"central-run-349616_aishwarya.kulkarni@paladincloud.io\",\n" +
                        "          \"_entity\": \"true\",\n" +
                        "          \"_entitytype\": \"iamusers\",\n" +
                        "          \"firstdiscoveredon\": \"2022-11-29 18:00:00+0530\",\n" +
                        "          \"latest\": true,\n" +
                        "          \"_loaddate\": \"2022-11-29 12:49:00+0000\"\n" +
                        "        }",
                JsonElement.class));
        JsonArray array = new JsonArray();
        array.add(jsonObject);
        return array;
    }

    public static Map<String, String> getMapString(String passRuleResourceId) {
        Map<String, String> commonMap = new HashMap<>();
        commonMap.put("executionId", "1234");
        commonMap.put("_resourceid", passRuleResourceId);
        commonMap.put("severity", "medium");
        commonMap.put("ruleCategory", "security");
        commonMap.put("accountid", "12345");
        commonMap.put("violationReason", "Separation of Duties While Assigning KMS Related Roles to Users is NOT enforced");
        return commonMap;
    }

    @Test
    public void getHelpTextTest() {
        assertThat(separationOfDutiesForKMSRule.getHelpText(), is(notNullValue()));
    }
}
