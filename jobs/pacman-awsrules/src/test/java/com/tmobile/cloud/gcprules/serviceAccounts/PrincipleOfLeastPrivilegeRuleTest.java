package com.tmobile.cloud.gcprules.serviceAccounts;

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

import static com.tmobile.cloud.gcprules.utils.TestUtils.getFailureJsonArrayForManagedServiceKeys;
import static com.tmobile.cloud.gcprules.utils.TestUtils.getHitJsonArrayForManagedServiceKeys;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.anyObject;
import static org.mockito.Matchers.anyString;
import static org.powermock.api.mockito.PowerMockito.mockStatic;
import static org.powermock.api.mockito.PowerMockito.when;

@PowerMockIgnore({ "javax.net.ssl.*", "javax.management.*","jdk.internal.reflect.*" })
@RunWith(PowerMockRunner.class)
@PrepareForTest({PacmanUtils.class, GCPUtils.class})
public class PrincipleOfLeastPrivilegeRuleTest {
    @InjectMocks
    PrincipleOfLeastPrivilegeRule principleOfLeastPrivilegeRule;
    @Before
    public void setUp() {
        mockStatic(PacmanUtils.class);
        mockStatic(GCPUtils.class);
    }

    @Test
    public void executeSuccessTest() throws Exception {


        when(PacmanUtils.getPacmanHost(anyString())).thenReturn("host");
        when(GCPUtils.getHitsArrayFromEs(anyObject(), anyObject())).thenReturn(getHitJsonArrayForServiceAccounts());

        when(PacmanUtils.createAnnotation(anyString(), anyObject(), anyString(), anyString(), anyString())).thenReturn(CommonTestUtils.getAnnotation("123"));
        when(PacmanUtils.doesAllHaveValue(anyString(), anyString(), anyString())).thenReturn(
                true);
        assertThat(principleOfLeastPrivilegeRule.execute(getMapString("r_123 "), getMapString("r_123 ")).getStatus(), is(PacmanSdkConstants.STATUS_SUCCESS));

    }

    private JsonArray getHitJsonArrayForServiceAccountsFailure() {
        Gson gson = new Gson();
        JsonObject jsonObject = new JsonObject();
        jsonObject.add("_source", gson.fromJson(
                "{\n" +
                        "          \"discoveryDate\": \"2022-09-28 06:00:00+0000\",\n" +
                        "          \"_cloudType\": \"GCP\",\n" +
                        "          \"region\": null,\n" +
                        "          \"id\": \"115653853863124476885\",\n" +
                        "          \"projectName\": \"Paladin Cloud\",\n" +
                        "          \"projectId\": \"central-run-349616\",\n" +
                        "          \"name\": \"projects/central-run-349616/serviceAccounts/344106022091-compute@developer.gserviceaccount.com\",\n" +
                        "          \"displayName\": \"Compute Engine default service account\",\n" +
                        "          \"email\": \"344106022091-compute@developer.gserviceaccount.com\",\n" +
                        "          \"description\": null,\n" +
                        "          \"serviceAccountKey\": [\n" +
                        "            {\n" +
                        "              \"name\": \"projects/central-run-349616/serviceAccounts/344106022091-compute@developer.gserviceaccount.com/keys/7432b40154c34c0b28b6a78cce6898b0ca31a03c\",\n" +
                        "              \"keyType\": \"SYSTEM_MANAGED\"\n" +
                        "            },\n" +
                        "            {\n" +
                        "              \"name\": \"projects/central-run-349616/serviceAccounts/344106022091-compute@developer.gserviceaccount.com/keys/e9def5b44d0bb9eacef39a0290c3b6b06633a147\",\n" +
                        "              \"keyType\": \"SYSTEM_MANAGED\"\n" +
                        "            }\n" +
                        "          ],\n" +
                        "          \"rolesMembers\": {\n" +
                        "            \"roles/containeranalysis.ServiceAgent\": [\n" +
                        "              \"serviceAccount:service-344106022091@container-analysis.iam.gserviceaccount.com\"\n" +
                        "            ],\n" +
                        "            \"roles/containerregistry.ServiceAgent\": [\n" +
                        "              \"serviceAccount:service-344106022091@containerregistry.iam.gserviceaccount.com\"\n" +
                        "            ],\n" +
                        "            \"roles/compute.serviceAgent\": [\n" +
                        "              \"serviceAccount:service-344106022091@compute-system.iam.gserviceaccount.com\"\n" +
                        "            ],\n" +
                        "            \"roles/cloudkms.serviceAgent\": [\n" +
                        "              \"serviceAccount:service-344106022091@gcp-sa-cloudkms.iam.gserviceaccount.com\"\n" +
                        "            ],\n" +
                        "            \"roles/container.serviceAgent\": [\n" +
                        "              \"serviceAccount:service-344106022091@container-engine-robot.iam.gserviceaccount.com\"\n" +
                        "            ],\n" +
                        "            \"roles/editor\": [\n" +
                        "              \"serviceAccount:344106022091-compute@developer.gserviceaccount.com\",\n" +
                        "              \"serviceAccount:344106022091@cloudservices.gserviceaccount.com\",\n" +
                        "              \"serviceAccount:pacbot-inventory@central-run-349616.iam.gserviceaccount.com\",\n" +
                        "              \"user:aishwarya.kulkarni@paladincloud.io\",\n" +
                        "              \"deleted:user:amisha.vijayakumar@paladincloud.io?uid=500546376536335355765\",\n" +
                        "              \"user:anjali-madhavi.nakirikanti@paladincloud.io\",\n" +
                        "              \"user:arun.kotratil@paladincloud.io\",\n" +
                        "              \"user:dheeraj.kholia@paladincloud.io\",\n" +
                        "              \"user:kushagra.jain@paladincloud.io\",\n" +
                        "              \"user:nithin.r@paladincloud.io\",\n" +
                        "              \"user:preethi.rajasekaran@paladincloud.io\",\n" +
                        "              \"user:ranadheer.bolli@paladincloud.io\",\n" +
                        "              \"user:santhosh.challa@paladincloud.io\",\n" +
                        "              \"user:sidharth.jain@paladincloud.io\"\n" +
                        "            ],\n" +
                        "            \"roles/owner\": [\n" +
                        "              \"user:cloudadmin@paladincloud.io\"\n" +
                        "            ],\n" +
                        "            \"roles/pubsub.serviceAgent\": [\n" +
                        "              \"serviceAccount:service-344106022091@gcp-sa-pubsub.iam.gserviceaccount.com\"\n" +
                        "            ],\n" +
                        "            \"roles/dataproc.serviceAgent\": [\n" +
                        "              \"serviceAccount:service-344106022091@dataproc-accounts.iam.gserviceaccount.com\"\n" +
                        "            ],\n" +
                        "            \"roles/osconfig.serviceAgent\": [\n" +
                        "              \"serviceAccount:service-344106022091@gcp-sa-osconfig.iam.gserviceaccount.com\"\n" +
                        "            ],\n" +
                        "            \"roles/cloudtasks.serviceAgent\": [\n" +
                        "              \"serviceAccount:service-344106022091@gcp-sa-cloudtasks.iam.gserviceaccount.com\"\n" +
                        "            ]\n" +
                        "          },\n" +
                        "          \"disabled\": false,\n" +
                        "          \"discoverydate\": \"2022-09-28 06:00:00+0000\",\n" +
                        "          \"_resourceid\": \"115653853863124476885\",\n" +
                        "          \"_docid\": \"115653853863124476885\",\n" +
                        "          \"_entity\": \"true\",\n" +
                        "          \"_entitytype\": \"serviceaccounts\",\n" +
                        "          \"firstdiscoveredon\": \"2022-09-19 15:00:00+0530\",\n" +
                        "          \"latest\": true,\n" +
                        "          \"_loaddate\": \"2022-09-28 06:30:00+0000\"\n" +
                        "        }",
                JsonElement.class));
        JsonArray array = new JsonArray();
        array.add(jsonObject);
        return array;
    }

    private JsonArray getHitJsonArrayForServiceAccounts() {
        Gson gson = new Gson();
        JsonObject jsonObject = new JsonObject();
        jsonObject.add("_source", gson.fromJson(
                "{\n" +
                        "          \"discoveryDate\": \"2022-09-28 06:00:00+0000\",\n" +
                        "          \"_cloudType\": \"GCP\",\n" +
                        "          \"region\": null,\n" +
                        "          \"id\": \"115653853863124476885\",\n" +
                        "          \"projectName\": \"Paladin Cloud\",\n" +
                        "          \"projectId\": \"central-run-349616\",\n" +
                        "          \"name\": \"projects/central-run-349616/serviceAccounts/344106022091-compute@developer.gserviceaccount.com\",\n" +
                        "          \"displayName\": \"Compute Engine default service account\",\n" +
                        "          \"email\": \"344106022091-compute@developer.gserviceaccount.com\",\n" +
                        "          \"description\": null,\n" +
                        "          \"serviceAccountKey\": [\n" +
                        "            {\n" +
                        "              \"name\": \"projects/central-run-349616/serviceAccounts/344106022091-compute@developer.gserviceaccount.com/keys/7432b40154c34c0b28b6a78cce6898b0ca31a03c\",\n" +
                        "              \"keyType\": \"SYSTEM_MANAGED\"\n" +
                        "            },\n" +
                        "            {\n" +
                        "              \"name\": \"projects/central-run-349616/serviceAccounts/344106022091-compute@developer.gserviceaccount.com/keys/e9def5b44d0bb9eacef39a0290c3b6b06633a147\",\n" +
                        "              \"keyType\": \"SYSTEM_MANAGED\"\n" +
                        "            }\n" +
                        "          ],\n" +
                        "          \"rolesMembers\": {\n" +
                        "            \"roles/containeranalysis.ServiceAgent\": [\n" +
                        "              \"serviceAccount:service-344106022091@container-analysis.iam.gserviceaccount.com\"\n" +
                        "            ],\n" +
                        "            \"roles/containerregistry.ServiceAgent\": [\n" +
                        "              \"serviceAccount:service-344106022091@containerregistry.iam.gserviceaccount.com\"\n" +
                        "            ],\n" +
                        "            \"roles/compute.serviceAgent\": [\n" +
                        "              \"serviceAccount:service-344106022091@compute-system.iam.gserviceaccount.com\"\n" +
                        "            ],\n" +
                        "            \"roles/cloudkms.serviceAgent\": [\n" +
                        "              \"serviceAccount:service-344106022091@gcp-sa-cloudkms.iam.gserviceaccount.com\"\n" +
                        "            ],\n" +
                        "            \"roles/container.serviceAgent\": [\n" +
                        "              \"serviceAccount:service-344106022091@container-engine-robot.iam.gserviceaccount.com\"\n" +
                        "            ],\n" +
                        "            \"roles/editor\": [\n" +
                        "              \"user:aishwarya.kulkarni@paladincloud.io\",\n" +
                        "              \"deleted:user:amisha.vijayakumar@paladincloud.io?uid=500546376536335355765\",\n" +
                        "              \"user:anjali-madhavi.nakirikanti@paladincloud.io\",\n" +
                        "              \"user:arun.kotratil@paladincloud.io\",\n" +
                        "              \"user:dheeraj.kholia@paladincloud.io\",\n" +
                        "              \"user:kushagra.jain@paladincloud.io\",\n" +
                        "              \"user:nithin.r@paladincloud.io\",\n" +
                        "              \"user:preethi.rajasekaran@paladincloud.io\",\n" +
                        "              \"user:ranadheer.bolli@paladincloud.io\",\n" +
                        "              \"user:santhosh.challa@paladincloud.io\",\n" +
                        "              \"user:sidharth.jain@paladincloud.io\"\n" +
                        "            ],\n" +
                        "            \"roles/owner\": [\n" +
                        "              \"user:cloudadmin@paladincloud.io\"\n" +
                        "            ],\n" +
                        "            \"roles/admin\": [\n" +
                        "              \"user:cloudadmin@paladincloud.io\"\n" +
                        "            ],\n" +
                        "            \"roles/pubsub.serviceAgent\": [\n" +
                        "              \"serviceAccount:service-344106022091@gcp-sa-pubsub.iam.gserviceaccount.com\"\n" +
                        "            ],\n" +
                        "            \"roles/dataproc.serviceAgent\": [\n" +
                        "              \"serviceAccount:service-344106022091@dataproc-accounts.iam.gserviceaccount.com\"\n" +
                        "            ],\n" +
                        "            \"roles/osconfig.serviceAgent\": [\n" +
                        "              \"serviceAccount:service-344106022091@gcp-sa-osconfig.iam.gserviceaccount.com\"\n" +
                        "            ],\n" +
                        "            \"roles/cloudtasks.serviceAgent\": [\n" +
                        "              \"serviceAccount:service-344106022091@gcp-sa-cloudtasks.iam.gserviceaccount.com\"\n" +
                        "            ]\n" +
                        "          },\n" +
                        "          \"disabled\": false,\n" +
                        "          \"discoverydate\": \"2022-09-28 06:00:00+0000\",\n" +
                        "          \"_resourceid\": \"115653853863124476885\",\n" +
                        "          \"_docid\": \"115653853863124476885\",\n" +
                        "          \"_entity\": \"true\",\n" +
                        "          \"_entitytype\": \"serviceaccounts\",\n" +
                        "          \"firstdiscoveredon\": \"2022-09-19 15:00:00+0530\",\n" +
                        "          \"latest\": true,\n" +
                        "          \"_loaddate\": \"2022-09-28 06:30:00+0000\"\n" +
                        "        }",
                JsonElement.class));
        JsonArray array = new JsonArray();
        array.add(jsonObject);
        return array;
    }

    @Test
    public void executeFailureTest() throws Exception {


        when(PacmanUtils.getPacmanHost(anyString())).thenReturn("host");
        when(GCPUtils.getHitsArrayFromEs(anyObject(), anyObject())).thenReturn(getHitJsonArrayForServiceAccountsFailure());

        when(PacmanUtils.createAnnotation(anyString(), anyObject(), anyString(), anyString(), anyString())).thenReturn(CommonTestUtils.getAnnotation("123"));
        when(PacmanUtils.doesAllHaveValue(anyString(), anyString(), anyString())).thenReturn(
                true);
        assertThat(principleOfLeastPrivilegeRule.execute(getMapString("r_123 "), getMapString("r_123 ")).getStatus(), is(PacmanSdkConstants.STATUS_FAILURE));
    }

    @Test
    public void executeFailureTestWithInvalidInputException() throws Exception {

        when(PacmanUtils.getPacmanHost(anyString())).thenReturn("host");
        when(GCPUtils.getHitsArrayFromEs(anyObject(), anyObject())).thenReturn(getFailureJsonArrayForManagedServiceKeys());

        when(PacmanUtils.createAnnotation(anyString(), anyObject(), anyString(), anyString(), anyString())).thenReturn(CommonTestUtils.getAnnotation("123"));
        when(PacmanUtils.doesAllHaveValue(anyString(), anyString(), anyString())).thenReturn(
                false);
        assertThatThrownBy(() -> principleOfLeastPrivilegeRule.execute(getMapString("r_123 "), getMapString("r_123 "))).isInstanceOf(InvalidInputException.class);
    }

    public static Map<String, String> getMapString(String passRuleResourceId) {
        Map<String, String> commonMap = new HashMap<>();
        commonMap.put("executionId", "1234");
        commonMap.put("_resourceid", passRuleResourceId);
        commonMap.put("severity", "medium");
        commonMap.put("ruleCategory", "security");
        commonMap.put("accountid", "12345");
        commonMap.put("ruleId", "Deny_Admin_Privilege_Service_Account");
        commonMap.put("policyId", "Deny_Admin_Privilege_Service_Account");
        commonMap.put("policyVersion", "version-1");
        return commonMap;
    }

    @Test
    public void getHelpTextTest() {
        assertThat(principleOfLeastPrivilegeRule.getHelpText(), is(notNullValue()));
    }

}
