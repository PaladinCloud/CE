package com.tmobile.cloud.gcprules.cloudsql;

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
public class DenyPublicIpForDbInstanceRuleTest {
    @InjectMocks
    DenyPublicIpForDbInstanceRule denyPublicIpForDbInstanceRule;

    @Before
    public void setUp() {
        mockStatic(PacmanUtils.class);
        mockStatic(GCPUtils.class);
    }

    @Test
    public void executeSuccessTest() throws Exception {


        when(PacmanUtils.getPacmanHost(anyString())).thenReturn("host");
        when(GCPUtils.getHitsArrayFromEs(anyObject(), anyObject())).thenReturn(getHitsJsonForSuccess());

        when(PacmanUtils.createAnnotation(anyString(), anyObject(), anyString(), anyString(), anyString())).thenReturn(CommonTestUtils.getAnnotation("123"));
        when(PacmanUtils.doesAllHaveValue(anyString(), anyString(), anyString())).thenReturn(
                true);
        assertThat(denyPublicIpForDbInstanceRule.execute(getMapString("r_123 "), getMapString("r_123 ")).getStatus(), is(PacmanSdkConstants.STATUS_SUCCESS));

    }

    @Test
    public void executeFailureTest() throws Exception {


        when(PacmanUtils.getPacmanHost(anyString())).thenReturn("host");
        when(GCPUtils.getHitsArrayFromEs(anyObject(), anyObject())).thenReturn(getHitsJsonForFailure());

        when(PacmanUtils.createAnnotation(anyString(), anyObject(), anyString(), anyString(), anyString())).thenReturn(CommonTestUtils.getAnnotation("123"));
        when(PacmanUtils.doesAllHaveValue(anyString(), anyString(), anyString())).thenReturn(
                true);
        assertThat(denyPublicIpForDbInstanceRule.execute(getMapString("r_123 "), getMapString("r_123 ")).getStatus(), is(PacmanSdkConstants.STATUS_FAILURE));
    }

    @Test
    public void executeFailureTestWithInvalidInputException() throws Exception {

        when(PacmanUtils.getPacmanHost(anyString())).thenReturn("host");
        when(GCPUtils.getHitsArrayFromEs(anyObject(), anyObject())).thenReturn(getHitsJsonForFailure());

        when(PacmanUtils.createAnnotation(anyString(), anyObject(), anyString(), anyString(), anyString())).thenReturn(CommonTestUtils.getAnnotation("123"));
        when(PacmanUtils.doesAllHaveValue(anyString(), anyString(), anyString())).thenReturn(
                false);
        assertThatThrownBy(() -> denyPublicIpForDbInstanceRule.execute(getMapString("r_123 "), getMapString("r_123 "))).isInstanceOf(InvalidInputException.class);
    }

    public static Map<String, String> getMapString(String passRuleResourceId) {
        Map<String, String> commonMap = new HashMap<>();
        commonMap.put("executionId", "1234");
        commonMap.put("_resourceid", passRuleResourceId);
        commonMap.put("severity", "medium");
        commonMap.put("ruleCategory", "security");
        commonMap.put("accountid", "12345");
        commonMap.put("ruleId", "Deny_public_ip_for_sql");
        commonMap.put("policyId", "Deny_public_ip_for_sql");
        commonMap.put("policyVersion", "version-1");
        return commonMap;
    }

    private JsonArray getHitsJsonForSuccess() {
        Gson gson = new Gson();
        JsonObject jsonObject = new JsonObject();
        jsonObject.add("_source", gson.fromJson("{\n" +
                "          \"_cloudType\": \"gcp\",\n" +
                "          \"region\": \"us-central1\",\n" +
                "          \"id\": \"cool-bay-349411:us-central1:gcp-mysql-instance1\",\n" +
                "          \"projectName\": \"cool-bay-349411\",\n" +
                "          \"name\": \"gcp-mysql-instance1\",\n" +
                "          \"kind\": \"sql#instance\",\n" +
                "          \"createdTime\": \"2022-06-28T03:07:43.761Z\",\n" +
                "          \"masterInstanceName\": null,\n" +
                "          \"backendType\": \"SECOND_GEN\",\n" +
                "          \"state\": \"SUSPENDED\",\n" +
                "          \"databaseVersion\": \"MYSQL_8_0\",\n" +
                "          \"databaseInstalledVersion\": \"MYSQL_8_0_26\",\n" +
                "          \"instanceType\": \"CLOUD_SQL_INSTANCE\",\n" +
                "          \"eTag\": \"44c8f7e7d3b041fb1e4f8089c1558b6dcdf4de2e127d574a98b86452990c08c6\",\n" +
                "          \"selfLink\": \"https://sqladmin.googleapis.com/v1/projects/cool-bay-349411/instances/gcp-mysql-instance1\",\n" +
                "          \"serviceAccountEmail\": \"p47822473470-0s7wz4@gcp-sa-cloud-sql.iam.gserviceaccount.com\",\n" +
                "          \"kmsKeyVersion\": null,\n" +
                "          \"kmsKeyName\": null,\n" +
                "          \"maxDiskSize\": null,\n" +
                "          \"currentDiskSize\": null,\n" +
                "          \"ipAddress\": [\n" +
                "            {\n" +
                "              \"ip\": \"34.68.114.195\",\n" +
                "              \"type\": \"SEC\"\n" +
                "            }\n" +
                "          ],\n" +
                "          \"serverCaCert\": {\n" +
                "            \"certSerialNumber\": \"0\",\n" +
                "            \"commonName\": \"C=US,O=Google\\\\, Inc,CN=Google Cloud SQL Server CA,dnQualifier=39b1af3a-6bd5-4bcf-bd3c-b76ea723538b\",\n" +
                "            \"createTime\": \"2022-06-28T03:09:24.129Z\",\n" +
                "            \"expirationTime\": \"2032-06-25T03:10:24.129Z\",\n" +
                "            \"instance\": \"gcp-mysql-instance1\",\n" +
                "            \"kind\": \"sql#sslCert\"\n" +
                "          },\n" +
                "          \"settings\": {\n" +
                "            \"activationPolicy\": \"ALWAYS\",\n" +
                "            \"authorizedGaeApplications\": [],\n" +
                "            \"availabilityType\": \"ZONAL\",\n" +
                "            \"backupConfiguration\": {\n" +
                "              \"backupRetentionSettings\": {\n" +
                "                \"retainedBackups\": 7,\n" +
                "                \"retentionUnit\": \"COUNT\"\n" +
                "              },\n" +
                "              \"binaryLogEnabled\": true,\n" +
                "              \"enabled\": true,\n" +
                "              \"kind\": \"sql#backupConfiguration\",\n" +
                "              \"location\": \"us\",\n" +
                "              \"startTime\": \"03:00\",\n" +
                "              \"transactionLogRetentionDays\": 7\n" +
                "            },\n" +
                "            \"dataDiskSizeGb\": 100,\n" +
                "            \"dataDiskType\": \"PD_SSD\",\n" +
                "            \"ipConfiguration\": {\n" +
                "              \"authorizedNetworks\": [],\n" +
                "              \"ipv4Enabled\": true\n" +
                "            },\n" +
                "            \"kind\": \"sql#settings\",\n" +
                "            \"locationPreference\": {\n" +
                "              \"kind\": \"sql#locationPreference\",\n" +
                "              \"zone\": \"us-central1-b\"\n" +
                "            },\n" +
                "            \"maintenanceWindow\": {\n" +
                "              \"day\": 0,\n" +
                "              \"hour\": 0,\n" +
                "              \"kind\": \"sql#maintenanceWindow\",\n" +
                "              \"updateTrack\": \"stable\"\n" +
                "            },\n" +
                "            \"pricingPlan\": \"PER_USE\",\n" +
                "            \"replicationType\": \"SYNCHRONOUS\",\n" +
                "            \"settingsVersion\": 17,\n" +
                "            \"storageAutoResize\": true,\n" +
                "            \"storageAutoResizeLimit\": 0,\n" +
                "            \"tier\": \"db-custom-2-8192\",\n" +
                "            \"userLabels\": {\n" +
                "              \"environment\": \"demo\",\n" +
                "              \"application\": \"paladincloud\",\n" +
                "              \"created_by\": \"paladin\"\n" +
                "            }\n" +
                "          },\n" +
                "          \"discoverydate\": \"2022-08-16 11:00:00+0000\",\n" +
                "          \"_resourceid\": \"cool-bay-349411:us-central1:gcp-mysql-instance1\",\n" +
                "          \"_docid\": \"cool-bay-349411:us-central1:gcp-mysql-instance1\",\n" +
                "          \"_entity\": \"true\",\n" +
                "          \"_entitytype\": \"cloudsql\",\n" +
                "          \"firstdiscoveredon\": \"2022-07-20 15:00:00+0000\",\n" +
                "          \"latest\": true,\n" +
                "          \"_loaddate\": \"2022-08-16 12:22:00+0000\"\n" +
                "        }\n" , JsonElement.class));
        JsonArray array = new JsonArray();
        array.add(jsonObject);
        return array;
    }

    private JsonArray getHitsJsonForFailure() {
        Gson gson = new Gson();
        JsonObject jsonObject = new JsonObject();
        jsonObject.add("_source", gson.fromJson("{\n" +
                "          \"_cloudType\": \"gcp\",\n" +
                "          \"region\": \"us-central1\",\n" +
                "          \"id\": \"cool-bay-349411:us-central1:gcp-mysql-instance1\",\n" +
                "          \"projectName\": \"cool-bay-349411\",\n" +
                "          \"name\": \"gcp-mysql-instance1\",\n" +
                "          \"kind\": \"sql#instance\",\n" +
                "          \"createdTime\": \"2022-06-28T03:07:43.761Z\",\n" +
                "          \"masterInstanceName\": null,\n" +
                "          \"backendType\": \"SECOND_GEN\",\n" +
                "          \"state\": \"SUSPENDED\",\n" +
                "          \"databaseVersion\": \"MYSQL_8_0\",\n" +
                "          \"databaseInstalledVersion\": \"MYSQL_8_0_26\",\n" +
                "          \"instanceType\": \"CLOUD_SQL_INSTANCE\",\n" +
                "          \"eTag\": \"44c8f7e7d3b041fb1e4f8089c1558b6dcdf4de2e127d574a98b86452990c08c6\",\n" +
                "          \"selfLink\": \"https://sqladmin.googleapis.com/v1/projects/cool-bay-349411/instances/gcp-mysql-instance1\",\n" +
                "          \"serviceAccountEmail\": \"p47822473470-0s7wz4@gcp-sa-cloud-sql.iam.gserviceaccount.com\",\n" +
                "          \"kmsKeyVersion\": null,\n" +
                "          \"kmsKeyName\": null,\n" +
                "          \"maxDiskSize\": null,\n" +
                "          \"currentDiskSize\": null,\n" +
                "          \"ipAddress\": [\n" +
                "            {\n" +
                "              \"ip\": \"34.68.114.195\",\n" +
                "              \"type\": \"PRIMARY\"\n" +
                "            }\n" +
                "          ],\n" +
                "          \"serverCaCert\": {\n" +
                "            \"certSerialNumber\": \"0\",\n" +
                "            \"commonName\": \"C=US,O=Google\\\\, Inc,CN=Google Cloud SQL Server CA,dnQualifier=39b1af3a-6bd5-4bcf-bd3c-b76ea723538b\",\n" +
                "            \"createTime\": \"2022-06-28T03:09:24.129Z\",\n" +
                "            \"expirationTime\": \"2032-06-25T03:10:24.129Z\",\n" +
                "            \"instance\": \"gcp-mysql-instance1\",\n" +
                "            \"kind\": \"sql#sslCert\"\n" +
                "          },\n" +
                "          \"settings\": {\n" +
                "            \"activationPolicy\": \"ALWAYS\",\n" +
                "            \"authorizedGaeApplications\": [],\n" +
                "            \"availabilityType\": \"ZONAL\",\n" +
                "            \"backupConfiguration\": {\n" +
                "              \"backupRetentionSettings\": {\n" +
                "                \"retainedBackups\": 7,\n" +
                "                \"retentionUnit\": \"COUNT\"\n" +
                "              },\n" +
                "              \"binaryLogEnabled\": true,\n" +
                "              \"enabled\": true,\n" +
                "              \"kind\": \"sql#backupConfiguration\",\n" +
                "              \"location\": \"us\",\n" +
                "              \"startTime\": \"03:00\",\n" +
                "              \"transactionLogRetentionDays\": 7\n" +
                "            },\n" +
                "            \"dataDiskSizeGb\": 100,\n" +
                "            \"dataDiskType\": \"PD_SSD\",\n" +
                "            \"ipConfiguration\": {\n" +
                "              \"authorizedNetworks\": [],\n" +
                "              \"ipv4Enabled\": true\n" +
                "            },\n" +
                "            \"kind\": \"sql#settings\",\n" +
                "            \"locationPreference\": {\n" +
                "              \"kind\": \"sql#locationPreference\",\n" +
                "              \"zone\": \"us-central1-b\"\n" +
                "            },\n" +
                "            \"maintenanceWindow\": {\n" +
                "              \"day\": 0,\n" +
                "              \"hour\": 0,\n" +
                "              \"kind\": \"sql#maintenanceWindow\",\n" +
                "              \"updateTrack\": \"stable\"\n" +
                "            },\n" +
                "            \"pricingPlan\": \"PER_USE\",\n" +
                "            \"replicationType\": \"SYNCHRONOUS\",\n" +
                "            \"settingsVersion\": 17,\n" +
                "            \"storageAutoResize\": true,\n" +
                "            \"storageAutoResizeLimit\": 0,\n" +
                "            \"tier\": \"db-custom-2-8192\",\n" +
                "            \"userLabels\": {\n" +
                "              \"environment\": \"demo\",\n" +
                "              \"application\": \"paladincloud\",\n" +
                "              \"created_by\": \"paladin\"\n" +
                "            }\n" +
                "          },\n" +
                "          \"discoverydate\": \"2022-08-16 11:00:00+0000\",\n" +
                "          \"_resourceid\": \"cool-bay-349411:us-central1:gcp-mysql-instance1\",\n" +
                "          \"_docid\": \"cool-bay-349411:us-central1:gcp-mysql-instance1\",\n" +
                "          \"_entity\": \"true\",\n" +
                "          \"_entitytype\": \"cloudsql\",\n" +
                "          \"firstdiscoveredon\": \"2022-07-20 15:00:00+0000\",\n" +
                "          \"latest\": true,\n" +
                "          \"_loaddate\": \"2022-08-16 12:22:00+0000\"\n" +
                "        }\n", JsonElement.class));
        JsonArray array = new JsonArray();
        array.add(jsonObject);
        return array;
    }

    @Test
    public void getHelpTextTest() {
        assertThat(denyPublicIpForDbInstanceRule.getHelpText(), is(notNullValue()));
    }
}
