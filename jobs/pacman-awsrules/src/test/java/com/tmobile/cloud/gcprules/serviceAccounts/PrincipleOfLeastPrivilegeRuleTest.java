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
                        "          \"discoveryDate\": \"2022-09-29 05:00:00+0000\",\n" +
                        "          \"_cloudType\": \"GCP\",\n" +
                        "          \"region\": null,\n" +
                        "          \"id\": \"114500682845143177107\",\n" +
                        "          \"projectName\": \"Paladin Cloud\",\n" +
                        "          \"projectId\": \"central-run-349616\",\n" +
                        "          \"name\": \"projects/central-run-349616/serviceAccounts/pacbot-inventory@central-run-349616.iam.gserviceaccount.com\",\n" +
                        "          \"displayName\": \"pacbot-inventory\",\n" +
                        "          \"email\": \"pacbot-inventory@central-run-349616.iam.gserviceaccount.com\",\n" +
                        "          \"description\": \"collect pacbot inventory\",\n" +
                        "          \"serviceAccountKey\": [\n" +
                        "            {\n" +
                        "              \"name\": \"projects/central-run-349616/serviceAccounts/pacbot-inventory@central-run-349616.iam.gserviceaccount.com/keys/b91c8c844a63fc5e5fadc6cd4349a331d3239819\",\n" +
                        "              \"keyType\": \"SYSTEM_MANAGED\"\n" +
                        "            },\n" +
                        "            {\n" +
                        "              \"name\": \"projects/central-run-349616/serviceAccounts/pacbot-inventory@central-run-349616.iam.gserviceaccount.com/keys/13f285344fa1cca83e1e452a0ff39260703f27c6\",\n" +
                        "              \"keyType\": \"SYSTEM_MANAGED\"\n" +
                        "            },\n" +
                        "            {\n" +
                        "              \"name\": \"projects/central-run-349616/serviceAccounts/pacbot-inventory@central-run-349616.iam.gserviceaccount.com/keys/8e756a62645b5d2680296797201f1fde7bf0ba37\",\n" +
                        "              \"keyType\": \"USER_MANAGED\"\n" +
                        "            }\n" +
                        "          ],\n" +
                        "          \"roles\": [\n" +
                        "            \"roles/editor\"\n" +
                        "          ],\n" +
                        "          \"disabled\": false,\n" +
                        "          \"discoverydate\": \"2022-09-29 05:00:00+0000\",\n" +
                        "          \"_resourceid\": \"114500682845143177107\",\n" +
                        "          \"_docid\": \"114500682845143177107\",\n" +
                        "          \"_entity\": \"true\",\n" +
                        "          \"_entitytype\": \"serviceaccounts\",\n" +
                        "          \"firstdiscoveredon\": \"2022-09-19 15:00:00+0530\",\n" +
                        "          \"latest\": true,\n" +
                        "          \"_loaddate\": \"2022-09-29 06:08:00+0000\"\n" +
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
                        "          \"discoveryDate\": \"2022-09-29 05:00:00+0000\",\n" +
                        "          \"_cloudType\": \"GCP\",\n" +
                        "          \"region\": null,\n" +
                        "          \"id\": \"114500682845143177107\",\n" +
                        "          \"projectName\": \"Paladin Cloud\",\n" +
                        "          \"projectId\": \"central-run-349616\",\n" +
                        "          \"name\": \"projects/central-run-349616/serviceAccounts/pacbot-inventory@central-run-349616.iam.gserviceaccount.com\",\n" +
                        "          \"displayName\": \"pacbot-inventory\",\n" +
                        "          \"email\": \"pacbot-inventory@central-run-349616.iam.gserviceaccount.com\",\n" +
                        "          \"description\": \"collect pacbot inventory\",\n" +
                        "          \"serviceAccountKey\": [\n" +
                        "            {\n" +
                        "              \"name\": \"projects/central-run-349616/serviceAccounts/pacbot-inventory@central-run-349616.iam.gserviceaccount.com/keys/b91c8c844a63fc5e5fadc6cd4349a331d3239819\",\n" +
                        "              \"keyType\": \"SYSTEM_MANAGED\"\n" +
                        "            },\n" +
                        "            {\n" +
                        "              \"name\": \"projects/central-run-349616/serviceAccounts/pacbot-inventory@central-run-349616.iam.gserviceaccount.com/keys/13f285344fa1cca83e1e452a0ff39260703f27c6\",\n" +
                        "              \"keyType\": \"SYSTEM_MANAGED\"\n" +
                        "            },\n" +
                        "            {\n" +
                        "              \"name\": \"projects/central-run-349616/serviceAccounts/pacbot-inventory@central-run-349616.iam.gserviceaccount.com/keys/8e756a62645b5d2680296797201f1fde7bf0ba37\",\n" +
                        "              \"keyType\": \"USER_MANAGED\"\n" +
                        "            }\n" +
                        "          ],\n" +
                        "          \"roles\": [\n" +
                        "            \"roles/user\"\n" +
                        "          ],\n" +
                        "          \"disabled\": false,\n" +
                        "          \"discoverydate\": \"2022-09-29 05:00:00+0000\",\n" +
                        "          \"_resourceid\": \"114500682845143177107\",\n" +
                        "          \"_docid\": \"114500682845143177107\",\n" +
                        "          \"_entity\": \"true\",\n" +
                        "          \"_entitytype\": \"serviceaccounts\",\n" +
                        "          \"firstdiscoveredon\": \"2022-09-19 15:00:00+0530\",\n" +
                        "          \"latest\": true,\n" +
                        "          \"_loaddate\": \"2022-09-29 06:08:00+0000\"\n" +
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
