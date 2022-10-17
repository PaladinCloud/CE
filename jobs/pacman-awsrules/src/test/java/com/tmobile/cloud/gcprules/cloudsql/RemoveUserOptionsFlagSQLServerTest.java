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
import org.powermock.core.classloader.annotations.PowerMockIgnore;
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
@PowerMockIgnore({"javax.net.ssl.*", "javax.management.*","jdk.internal.reflect.*"})
public class RemoveUserOptionsFlagSQLServerTest {
    @InjectMocks
    DisableOrEnableDBFlagsRule disableOrEnableDBFlagsRule;
    @Before
    public void setUp() {
        mockStatic(PacmanUtils.class);
        mockStatic(GCPUtils.class);
    }

    @Test
    public void executeSuccessTest() throws Exception {


        when(PacmanUtils.getPacmanHost(anyString())).thenReturn("host");
        when(GCPUtils.getHitsArrayFromEs(anyObject(), anyObject())).thenReturn(getHitjsonArrayForUserOptionsFlag());

        when(PacmanUtils.createAnnotation(anyString(), anyObject(), anyString(), anyString(), anyString())).thenReturn(CommonTestUtils.getAnnotation("123"));
        when(PacmanUtils.doesAllHaveValue(anyString(), anyString(), anyString())).thenReturn(
                true);
        assertThat(disableOrEnableDBFlagsRule.execute(getMapString("r_123 "), getMapString("r_123 ")).getStatus(), is(PacmanSdkConstants.STATUS_SUCCESS));

    }

    private JsonArray getHitjsonArrayForUserOptionsFlag() {
        Gson gson = new Gson();
        JsonObject jsonObject = new JsonObject();
        jsonObject.add("_source", gson.fromJson(
                "{\n" +
                        "          \"discoveryDate\": \"2022-10-17 10:00:00+0000\",\n" +
                        "          \"_cloudType\": \"gcp\",\n" +
                        "          \"region\": \"us-central1\",\n" +
                        "          \"id\": \"central-run-349616:us-central1:sql-server-instance1\",\n" +
                        "          \"projectName\": \"Paladin Cloud\",\n" +
                        "          \"projectId\": \"central-run-349616\",\n" +
                        "          \"name\": \"sql-server-instance1\",\n" +
                        "          \"kind\": \"sql#instance\",\n" +
                        "          \"createdTime\": \"2022-08-18T10:03:14.182Z\",\n" +
                        "          \"masterInstanceName\": null,\n" +
                        "          \"backendType\": \"SECOND_GEN\",\n" +
                        "          \"state\": \"RUNNABLE\",\n" +
                        "          \"databaseVersion\": \"SQLSERVER_2019_STANDARD\",\n" +
                        "          \"databaseInstalledVersion\": \"SQLSERVER_2019_STANDARD_CU16_GDR\",\n" +
                        "          \"instanceType\": \"CLOUD_SQL_INSTANCE\",\n" +
                        "          \"eTag\": \"a2084b791a9b2c4700c10dd10d019832dbfaf4872907ecad828a38c16a5a792f\",\n" +
                        "          \"selfLink\": \"https://sqladmin.googleapis.com/v1/projects/central-run-349616/instances/sql-server-instance1\",\n" +
                        "          \"serviceAccountEmail\": \"p344106022091-kdzpvj@gcp-sa-cloud-sql.iam.gserviceaccount.com\",\n" +
                        "          \"kmsKeyVersion\": null,\n" +
                        "          \"kmsKeyName\": null,\n" +
                        "          \"maxDiskSize\": null,\n" +
                        "          \"currentDiskSize\": null,\n" +
                        "          \"ipAddress\": [\n" +
                        "            {\n" +
                        "              \"ip\": \"34.135.103.201\",\n" +
                        "              \"type\": \"PRIMARY\"\n" +
                        "            }\n" +
                        "          ],\n" +
                        "          \"serverCaCert\": {\n" +
                        "            \"certSerialNumber\": \"0\",\n" +
                        "            \"commonName\": \"C=US,O=Google\\\\, Inc,CN=Google Cloud SQL Server CA,dnQualifier=92d054a8-75ed-4929-aa79-fc4aa7e03d41\",\n" +
                        "            \"createTime\": \"2022-08-18T10:05:17.124Z\",\n" +
                        "            \"expirationTime\": \"2032-08-15T10:06:17.124Z\",\n" +
                        "            \"instance\": \"sql-server-instance1\",\n" +
                        "            \"kind\": \"sql#sslCert\"\n" +
                        "          },\n" +
                        "          \"settings\": {\n" +
                        "            \"activationPolicy\": \"ALWAYS\",\n" +
                        "            \"activeDirectoryConfig\": {\n" +
                        "              \"kind\": \"sql#activeDirectoryConfig\"\n" +
                        "            },\n" +
                        "            \"authorizedGaeApplications\": [],\n" +
                        "            \"availabilityType\": \"ZONAL\",\n" +
                        "            \"backupConfiguration\": {\n" +
                        "              \"backupRetentionSettings\": {\n" +
                        "                \"retainedBackups\": 7,\n" +
                        "                \"retentionUnit\": \"COUNT\"\n" +
                        "              },\n" +
                        "              \"enabled\": true,\n" +
                        "              \"kind\": \"sql#backupConfiguration\",\n" +
                        "              \"location\": \"us\",\n" +
                        "              \"startTime\": \"12:00\",\n" +
                        "              \"transactionLogRetentionDays\": 7\n" +
                        "            },\n" +
                        "            \"collation\": \"SQL_Latin1_General_CP1_CI_AS\",\n" +
                        "            \"dataDiskSizeGb\": 100,\n" +
                        "            \"dataDiskType\": \"PD_SSD\",\n" +
                        "            \"databaseFlags\": [\n" +
                        "              {\n" +
                        "                \"name\": \"user connections\",\n" +
                        "                \"value\": \"0\"\n" +
                        "              }\n" +
                        "            ],\n" +
                        "            \"ipConfiguration\": {\n" +
                        "              \"authorizedNetworks\": [\n" +
                        "                {\n" +
                        "                  \"kind\": \"sql#aclEntry\",\n" +
                        "                  \"name\": \"public-ip\",\n" +
                        "                  \"value\": \"0.0.0.0/0\"\n" +
                        "                }\n" +
                        "              ],\n" +
                        "              \"ipv4Enabled\": true,\n" +
                        "              \"requireSsl\": true\n" +
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
                        "            \"settingsVersion\": 39,\n" +
                        "            \"sqlServerAuditConfig\": {\n" +
                        "              \"kind\": \"sql#sqlServerAuditConfig\",\n" +
                        "              \"retentionInterval\": \"0s\",\n" +
                        "              \"uploadInterval\": \"0s\"\n" +
                        "            },\n" +
                        "            \"storageAutoResize\": true,\n" +
                        "            \"storageAutoResizeLimit\": 0,\n" +
                        "            \"tier\": \"db-custom-2-8192\",\n" +
                        "            \"connectorEnforcement\": \"NOT_REQUIRED\",\n" +
                        "            \"deletionProtectionEnabled\": true\n" +
                        "          },\n" +
                        "          \"backupEnabled\": true,\n" +
                        "          \"authorizedNetwork\": {\n" +
                        "            \"kind\": \"sql#aclEntry\",\n" +
                        "            \"name\": \"public-ip\",\n" +
                        "            \"value\": \"0.0.0.0/0\"\n" +
                        "          },\n" +
                        "          \"discoverydate\": \"2022-10-17 10:00:00+0000\",\n" +
                        "          \"_resourceid\": \"central-run-349616:us-central1:sql-server-instance1\",\n" +
                        "          \"_docid\": \"central-run-349616:us-central1:sql-server-instance1\",\n" +
                        "          \"_entity\": \"true\",\n" +
                        "          \"_entitytype\": \"cloudsql_sqlserver\",\n" +
                        "          \"firstdiscoveredon\": \"2022-08-25 07:00:00+0000\",\n" +
                        "          \"latest\": true,\n" +
                        "          \"_loaddate\": \"2022-10-17 10:35:00+0000\"\n" +
                        "        }",
                JsonElement.class));
        JsonArray array = new JsonArray();
        array.add(jsonObject);
        return array;
    }

    @Test
    public void executeFailureTest() throws Exception {


        when(PacmanUtils.getPacmanHost(anyString())).thenReturn("host");
        when(GCPUtils.getHitsArrayFromEs(anyObject(), anyObject())).thenReturn(getFailurejsonArrayForDForUserOptionsFlag());

        when(PacmanUtils.createAnnotation(anyString(), anyObject(), anyString(), anyString(), anyString())).thenReturn(CommonTestUtils.getAnnotation("123"));
        when(PacmanUtils.doesAllHaveValue(anyString(), anyString(), anyString())).thenReturn(
                true);
        assertThat(disableOrEnableDBFlagsRule.execute(getMapString("r_123 "), getMapString("r_123 ")).getStatus(), is(PacmanSdkConstants.STATUS_FAILURE));
    }

    private JsonArray getFailurejsonArrayForDForUserOptionsFlag() {
        Gson gson = new Gson();
        JsonObject jsonObject = new JsonObject();
        jsonObject.add("_source", gson.fromJson(
                "{\n" +
                        "          \"discoveryDate\": \"2022-10-17 10:00:00+0000\",\n" +
                        "          \"_cloudType\": \"gcp\",\n" +
                        "          \"region\": \"us-central1\",\n" +
                        "          \"id\": \"central-run-349616:us-central1:sql-server-instance1\",\n" +
                        "          \"projectName\": \"Paladin Cloud\",\n" +
                        "          \"projectId\": \"central-run-349616\",\n" +
                        "          \"name\": \"sql-server-instance1\",\n" +
                        "          \"kind\": \"sql#instance\",\n" +
                        "          \"createdTime\": \"2022-08-18T10:03:14.182Z\",\n" +
                        "          \"masterInstanceName\": null,\n" +
                        "          \"backendType\": \"SECOND_GEN\",\n" +
                        "          \"state\": \"RUNNABLE\",\n" +
                        "          \"databaseVersion\": \"SQLSERVER_2019_STANDARD\",\n" +
                        "          \"databaseInstalledVersion\": \"SQLSERVER_2019_STANDARD_CU16_GDR\",\n" +
                        "          \"instanceType\": \"CLOUD_SQL_INSTANCE\",\n" +
                        "          \"eTag\": \"a2084b791a9b2c4700c10dd10d019832dbfaf4872907ecad828a38c16a5a792f\",\n" +
                        "          \"selfLink\": \"https://sqladmin.googleapis.com/v1/projects/central-run-349616/instances/sql-server-instance1\",\n" +
                        "          \"serviceAccountEmail\": \"p344106022091-kdzpvj@gcp-sa-cloud-sql.iam.gserviceaccount.com\",\n" +
                        "          \"kmsKeyVersion\": null,\n" +
                        "          \"kmsKeyName\": null,\n" +
                        "          \"maxDiskSize\": null,\n" +
                        "          \"currentDiskSize\": null,\n" +
                        "          \"ipAddress\": [\n" +
                        "            {\n" +
                        "              \"ip\": \"34.135.103.201\",\n" +
                        "              \"type\": \"PRIMARY\"\n" +
                        "            }\n" +
                        "          ],\n" +
                        "          \"serverCaCert\": {\n" +
                        "            \"certSerialNumber\": \"0\",\n" +
                        "            \"commonName\": \"C=US,O=Google\\\\, Inc,CN=Google Cloud SQL Server CA,dnQualifier=92d054a8-75ed-4929-aa79-fc4aa7e03d41\",\n" +
                        "            \"createTime\": \"2022-08-18T10:05:17.124Z\",\n" +
                        "            \"expirationTime\": \"2032-08-15T10:06:17.124Z\",\n" +
                        "            \"instance\": \"sql-server-instance1\",\n" +
                        "            \"kind\": \"sql#sslCert\"\n" +
                        "          },\n" +
                        "          \"settings\": {\n" +
                        "            \"activationPolicy\": \"ALWAYS\",\n" +
                        "            \"activeDirectoryConfig\": {\n" +
                        "              \"kind\": \"sql#activeDirectoryConfig\"\n" +
                        "            },\n" +
                        "            \"authorizedGaeApplications\": [],\n" +
                        "            \"availabilityType\": \"ZONAL\",\n" +
                        "            \"backupConfiguration\": {\n" +
                        "              \"backupRetentionSettings\": {\n" +
                        "                \"retainedBackups\": 7,\n" +
                        "                \"retentionUnit\": \"COUNT\"\n" +
                        "              },\n" +
                        "              \"enabled\": true,\n" +
                        "              \"kind\": \"sql#backupConfiguration\",\n" +
                        "              \"location\": \"us\",\n" +
                        "              \"startTime\": \"12:00\",\n" +
                        "              \"transactionLogRetentionDays\": 7\n" +
                        "            },\n" +
                        "            \"collation\": \"SQL_Latin1_General_CP1_CI_AS\",\n" +
                        "            \"dataDiskSizeGb\": 100,\n" +
                        "            \"dataDiskType\": \"PD_SSD\",\n" +
                        "            \"databaseFlags\": [\n" +
                        "              {\n" +
                        "                \"name\": \"user connections\",\n" +
                        "                \"value\": \"0\"\n" +
                        "              },\n" +
                        "              {\n" +
                        "                \"name\": \"user options\",\n" +
                        "                \"value\": \"1\"\n" +
                        "              }\n" +
                        "            ],\n" +
                        "            \"ipConfiguration\": {\n" +
                        "              \"authorizedNetworks\": [\n" +
                        "                {\n" +
                        "                  \"kind\": \"sql#aclEntry\",\n" +
                        "                  \"name\": \"public-ip\",\n" +
                        "                  \"value\": \"0.0.0.0/0\"\n" +
                        "                }\n" +
                        "              ],\n" +
                        "              \"ipv4Enabled\": true,\n" +
                        "              \"requireSsl\": true\n" +
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
                        "            \"settingsVersion\": 39,\n" +
                        "            \"sqlServerAuditConfig\": {\n" +
                        "              \"kind\": \"sql#sqlServerAuditConfig\",\n" +
                        "              \"retentionInterval\": \"0s\",\n" +
                        "              \"uploadInterval\": \"0s\"\n" +
                        "            },\n" +
                        "            \"storageAutoResize\": true,\n" +
                        "            \"storageAutoResizeLimit\": 0,\n" +
                        "            \"tier\": \"db-custom-2-8192\",\n" +
                        "            \"connectorEnforcement\": \"NOT_REQUIRED\",\n" +
                        "            \"deletionProtectionEnabled\": true\n" +
                        "          },\n" +
                        "          \"backupEnabled\": true,\n" +
                        "          \"authorizedNetwork\": {\n" +
                        "            \"kind\": \"sql#aclEntry\",\n" +
                        "            \"name\": \"public-ip\",\n" +
                        "            \"value\": \"0.0.0.0/0\"\n" +
                        "          },\n" +
                        "          \"discoverydate\": \"2022-10-17 10:00:00+0000\",\n" +
                        "          \"_resourceid\": \"central-run-349616:us-central1:sql-server-instance1\",\n" +
                        "          \"_docid\": \"central-run-349616:us-central1:sql-server-instance1\",\n" +
                        "          \"_entity\": \"true\",\n" +
                        "          \"_entitytype\": \"cloudsql_sqlserver\",\n" +
                        "          \"firstdiscoveredon\": \"2022-08-25 07:00:00+0000\",\n" +
                        "          \"latest\": true,\n" +
                        "          \"_loaddate\": \"2022-10-17 10:35:00+0000\"\n" +
                        "        }",
                JsonElement.class));
        JsonArray array = new JsonArray();
        array.add(jsonObject);
        return array;
    }

    @Test
    public void executeFailureTestWithInvalidInputException() throws Exception {

        when(PacmanUtils.getPacmanHost(anyString())).thenReturn("host");
        when(GCPUtils.getHitsArrayFromEs(anyObject(), anyObject())).thenReturn(getHitjsonArrayForDBOwnerFlagChanging());

        when(PacmanUtils.createAnnotation(anyString(), anyObject(), anyString(), anyString(), anyString())).thenReturn(CommonTestUtils.getAnnotation("123"));
        when(PacmanUtils.doesAllHaveValue(anyString(), anyString(), anyString())).thenReturn(
                false);
        assertThatThrownBy(() -> disableOrEnableDBFlagsRule.execute(getMapString("r_123 "), getMapString("r_123 "))).isInstanceOf(InvalidInputException.class);
    }

    public static Map<String, String> getMapString(String passRuleResourceId) {
        Map<String, String> commonMap = new HashMap<>();
        commonMap.put("executionId", "1234");
        commonMap.put("_resourceid", passRuleResourceId);
        commonMap.put("severity", "low");
        commonMap.put("ruleCategory", "security");
        commonMap.put("accountid", "12345");
        commonMap.put("ruleId", "Remove_user_options_for_SQLServer");
        commonMap.put("policyId", "Remove_user_options_for_SQLServer");
        commonMap.put("policyVersion", "version-1");
        commonMap.put("dataBaseType", "gcp_cloudsql_sqlserver");
        commonMap.put("dbFlagName", "user options");
        commonMap.put("description", "In_order_to_avoid_defining_global_defaults_for_all_database_users,_delete_user_options_database_flag");
        commonMap.put("violationReason", "\"user options\" flag is enabled for your Google Cloud SQL Server database instances");

        return commonMap;
    }

    @Test
    public void getHelpTextTest() {
        assertThat(disableOrEnableDBFlagsRule.getHelpText(), is(notNullValue()));
    }



}
