package com.tmobile.cloud.azurerules.Defender;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.tmobile.cloud.awsrules.utils.CommonTestUtils;
import com.tmobile.cloud.awsrules.utils.PacmanUtils;
import com.tmobile.cloud.awsrules.utils.RulesElasticSearchRepositoryUtil;
import com.tmobile.pacman.commons.PacmanSdkConstants;
import com.tmobile.pacman.commons.policy.Annotation;
import com.tmobile.pacman.commons.policy.BasePolicy;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.powermock.core.classloader.annotations.PowerMockIgnore;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.*;
import static org.mockito.Matchers.anyObject;
import static org.powermock.api.mockito.PowerMockito.mockStatic;
import static org.powermock.api.mockito.PowerMockito.when;

@PowerMockIgnore({"javax.net.ssl.*", "javax.management.*"})
@RunWith(PowerMockRunner.class)
@PrepareForTest({PacmanUtils.class, BasePolicy.class, RulesElasticSearchRepositoryUtil.class, Annotation.class})
public class AzureMsDefenderTest {
    @InjectMocks
    NotifyAlertsSeverityRule notifyAlertsSeverityRule;
    @InjectMocks
    EmailAlertSettingsRule emailAlertSettingsRule;
    @InjectMocks
    AdditionalEmailAddressConfigureRule additionalEmailAddressConfigureRule;

    public JsonObject getFailureJsonArrayForMsDefEmailSettings(){
        Gson gson=new Gson();
        JsonObject jsonObject = new JsonObject();
        jsonObject.add("hits", gson.fromJson("{\"hits\": [\n" +
                "      {\n" +
                "        \"_index\": \"azure_defender\",\n" +
                "        \"_type\": \"defender\",\n" +
                "        \"_id\": \"subscriptions/f4d319d8-7eac-4e15-a561-400f7744aa81/providers/Microsoft.Security/securityContacts/default\",\n" +
                "        \"_score\": 0.2876821,\n" +
                "        \"_source\": {\n" +
                "          \"discoverydate\": \"2022-10-20 16:00:00+0000\",\n" +
                "          \"_cloudType\": \"Azure\",\n" +
                "          \"subscription\": null,\n" +
                "          \"region\": \"West Europe\",\n" +
                "          \"subscriptionName\": null,\n" +
                "          \"resourceGroupName\": null,\n" +
                "          \"id\": \"subscriptions/f4d319d8-7eac-4e15-a561-400f7744aa81/providers/Microsoft.Security/securityContacts/default\",\n" +
                "          \"properties\": {\n" +
                "            \"emails\": \"\",\n" +
                "            \"phone\": \"\",\n" +
                "            \"notificationsByRole\": {\n" +
                "              \"state\": \"On\",\n" +
                "              \"roles\": [\n" +
                "                \"User\"\n" +
                "              ]\n" +
                "            },\n" +
                "            \"alertNotifications\": {\n" +
                "              \"state\": \"Off\",\n" +
                "              \"minimalSeverity\": \"High\"\n" +
                "            }\n" +
                "          },\n" +
                "          \"name\": \"default\",\n" +
                "          \"type\": \"Microsoft.Security/securityContacts\",\n" +
                "          \"etag\": \"b8004fbd\",\n" +
                "          \"_resourceid\": \"subscriptions/f4d319d8-7eac-4e15-a561-400f7744aa81/providers/Microsoft.Security/securityContacts/default\",\n" +
                "          \"_docid\": \"subscriptions/f4d319d8-7eac-4e15-a561-400f7744aa81/providers/Microsoft.Security/securityContacts/default\",\n" +
                "          \"_entity\": \"true\",\n" +
                "          \"_entitytype\": \"defender\",\n" +
                "          \"firstdiscoveredon\": \"2022-10-14 15:00:00+0530\",\n" +
                "          \"latest\": true,\n" +
                "          \"_loaddate\": \"2022-10-20 16:59:00+0000\"\n" +
                "        }\n" +
                "      }\n" +
                "    ]}", JsonElement.class));
        return jsonObject;
    }

    public  JsonObject getHitJsonArrayForMsDefEmailSettings() {
        Gson gson = new Gson();
        JsonObject jsonObject = new JsonObject();
        jsonObject.add("hits", gson.fromJson("{\"hits\": [\n" +
                "      {\n" +
                "        \"_index\": \"azure_defender\",\n" +
                "        \"_type\": \"defender\",\n" +
                "        \"_id\": \"subscriptions/f4d319d8-7eac-4e15-a561-400f7744aa81/providers/Microsoft.Security/securityContacts/default\",\n" +
                "        \"_score\": 0.2876821,\n" +
                "        \"_source\": {\n" +
                "          \"discoverydate\": \"2022-10-20 16:00:00+0000\",\n" +
                "          \"_cloudType\": \"Azure\",\n" +
                "          \"subscription\": null,\n" +
                "          \"region\": \"West Europe\",\n" +
                "          \"subscriptionName\": null,\n" +
                "          \"resourceGroupName\": null,\n" +
                "          \"id\": \"subscriptions/f4d319d8-7eac-4e15-a561-400f7744aa81/providers/Microsoft.Security/securityContacts/default\",\n" +
                "          \"properties\": {\n" +
                "            \"emails\": \"anjali-madhavi.nakirikanti@paladincloud.io\",\n" +
                "            \"phone\": \"\",\n" +
                "            \"notificationsByRole\": {\n" +
                "              \"state\": \"On\",\n" +
                "              \"roles\": [\n" +
                "                \"Owner\"\n" +
                "              ]\n" +
                "            },\n" +
                "            \"alertNotifications\": {\n" +
                "              \"state\": \"On\",\n" +
                "              \"minimalSeverity\": \"High\"\n" +
                "            }\n" +
                "          },\n" +
                "          \"name\": \"default\",\n" +
                "          \"type\": \"Microsoft.Security/securityContacts\",\n" +
                "          \"etag\": \"b8004fbd\",\n" +
                "          \"_resourceid\": \"subscriptions/f4d319d8-7eac-4e15-a561-400f7744aa81/providers/Microsoft.Security/securityContacts/default\",\n" +
                "          \"_docid\": \"subscriptions/f4d319d8-7eac-4e15-a561-400f7744aa81/providers/Microsoft.Security/securityContacts/default\",\n" +
                "          \"_entity\": \"true\",\n" +
                "          \"_entitytype\": \"defender\",\n" +
                "          \"firstdiscoveredon\": \"2022-10-14 15:00:00+0530\",\n" +
                "          \"latest\": true,\n" +
                "          \"_loaddate\": \"2022-10-20 16:59:00+0000\"\n" +
                "        }\n" +
                "      }\n" +
                "    ]}", JsonElement.class));
        return jsonObject;
    }

    @Test
    public void executeSucessForNotifyAlertsTest() throws Exception {
        mockStatic(PacmanUtils.class);
        mockStatic(RulesElasticSearchRepositoryUtil.class);
        when(PacmanUtils.doesAllHaveValue(anyString(), anyString()))
                .thenReturn(
                        true);
        when(RulesElasticSearchRepositoryUtil.getQueryDetailsFromES(anyString(),anyObject(),
                anyObject(),
                anyObject(), anyObject(), anyInt(), anyObject(), anyObject(), anyObject())).thenReturn(getHitJsonArrayForMsDefEmailSettings());
        assertThat(notifyAlertsSeverityRule.execute(CommonTestUtils.getMapString("r_123 "),
                CommonTestUtils.getMapString("r_123 ")).getStatus(), is(PacmanSdkConstants.STATUS_SUCCESS));
    }

    @Test
    public void executeSucessForAdditionalEmailTest() throws Exception {
        mockStatic(PacmanUtils.class);
        mockStatic(RulesElasticSearchRepositoryUtil.class);
        when(PacmanUtils.doesAllHaveValue(anyString(), anyString()))
                .thenReturn(
                        true);
        when(RulesElasticSearchRepositoryUtil.getQueryDetailsFromES(anyString(),anyObject(),
                anyObject(),
                anyObject(), anyObject(), anyInt(), anyObject(), anyObject(), anyObject())).thenReturn(getHitJsonArrayForMsDefEmailSettings());
        assertThat(additionalEmailAddressConfigureRule.execute(CommonTestUtils.getMapString("r_123 "),
                CommonTestUtils.getMapString("r_123 ")).getStatus(), is(PacmanSdkConstants.STATUS_SUCCESS));
    }

    @Test
    public void executeSucessForSetAllUsersToOwnerTest() throws Exception {
        mockStatic(PacmanUtils.class);
        mockStatic(RulesElasticSearchRepositoryUtil.class);
        when(PacmanUtils.doesAllHaveValue(anyString(), anyString()))
                .thenReturn(
                        true);
        when(RulesElasticSearchRepositoryUtil.getQueryDetailsFromES(anyString(),anyObject(),
                anyObject(),
                anyObject(), anyObject(), anyInt(), anyObject(), anyObject(), anyObject())).thenReturn(getHitJsonArrayForMsDefEmailSettings());
        assertThat(emailAlertSettingsRule.execute(CommonTestUtils.getMapString("r_123 "),
                CommonTestUtils.getMapString("r_123 ")).getStatus(), is(PacmanSdkConstants.STATUS_SUCCESS));
    }

    @Test
    public void executeFailureForNotifyAlertsTest() throws Exception {
        mockStatic(PacmanUtils.class);
        mockStatic(RulesElasticSearchRepositoryUtil.class);
        when(PacmanUtils.doesAllHaveValue(anyString(), anyString()))
                .thenReturn(
                        true);
        when(RulesElasticSearchRepositoryUtil.getQueryDetailsFromES(anyString(),anyObject(),
                anyObject(),
                anyObject(), anyObject(), anyInt(), anyObject(), anyObject(), anyObject())).thenReturn(getFailureJsonArrayForMsDefEmailSettings());
        mockStatic(Annotation.class);
        when(Annotation.buildAnnotation(anyObject(),anyObject())).thenReturn(getMockAnnotation());
        assertThat(notifyAlertsSeverityRule.execute(CommonTestUtils.getMapString("r_123 "),
                CommonTestUtils.getMapString("r_123 ")).getStatus(), is(PacmanSdkConstants.STATUS_FAILURE));
    }

    @Test
    public void executeFailureForAdditionalEmailTest() throws Exception {
        mockStatic(PacmanUtils.class);
        mockStatic(RulesElasticSearchRepositoryUtil.class);
        when(PacmanUtils.doesAllHaveValue(anyString(), anyString()))
                .thenReturn(
                        true);
        when(RulesElasticSearchRepositoryUtil.getQueryDetailsFromES(anyString(),anyObject(),
                anyObject(),
                anyObject(), anyObject(), anyInt(), anyObject(), anyObject(), anyObject())).thenReturn(getFailureJsonArrayForMsDefEmailSettings());
        mockStatic(Annotation.class);
        when(Annotation.buildAnnotation(anyObject(),anyObject())).thenReturn(getMockAnnotation());
        assertThat(additionalEmailAddressConfigureRule.execute(CommonTestUtils.getMapString("r_123 "),
                CommonTestUtils.getMapString("r_123 ")).getStatus(), is(PacmanSdkConstants.STATUS_FAILURE));
    }

    @Test
    public void executeFailureForSetAllUsersToOwnerTest() throws Exception {
        mockStatic(PacmanUtils.class);
        mockStatic(RulesElasticSearchRepositoryUtil.class);
        when(PacmanUtils.doesAllHaveValue(anyString(), anyString()))
                .thenReturn(
                        true);
        when(RulesElasticSearchRepositoryUtil.getQueryDetailsFromES(anyString(),anyObject(),
                anyObject(),
                anyObject(), anyObject(), anyInt(), anyObject(), anyObject(), anyObject())).thenReturn(getFailureJsonArrayForMsDefEmailSettings());
        mockStatic(Annotation.class);
        when(Annotation.buildAnnotation(anyObject(),anyObject())).thenReturn(getMockAnnotation());
        assertThat(emailAlertSettingsRule.execute(CommonTestUtils.getMapString("r_123 "),
                CommonTestUtils.getMapString("r_123 ")).getStatus(), is(PacmanSdkConstants.STATUS_FAILURE));
    }

    private Annotation getMockAnnotation() {
        Annotation annotation=new Annotation();
        annotation.put(PacmanSdkConstants.POLICY_NAME,"Mock policy name");
        annotation.put(PacmanSdkConstants.POLICY_ID, "Mock policy id");
        annotation.put(PacmanSdkConstants.POLICY_VERSION, "Mock policy version");
        annotation.put(PacmanSdkConstants.RESOURCE_ID, "Mock resource id");
        annotation.put(PacmanSdkConstants.TYPE, "Mock type");
        return annotation;
    }
}

