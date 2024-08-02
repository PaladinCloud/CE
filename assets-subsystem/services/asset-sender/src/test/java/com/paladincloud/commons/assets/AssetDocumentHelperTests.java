package com.paladincloud.commons.assets;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.paladincloud.common.AssetDocumentFields;
import com.paladincloud.common.assets.AssetDocumentHelper;
import com.paladincloud.common.util.JsonHelper;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Set;
import org.junit.jupiter.api.Test;

/**
 * These tests are to ensure the conversion from a mapped entity to an AssetDTO is correct
 */
public class AssetDocumentHelperTests {

    static private AssetDocumentHelper getHelper(String dataSource, String idField) {
        return AssetDocumentHelper.builder()
            .loadDate(ZonedDateTime.now())
            .idField(idField)
            .docIdFields(List.of("accountid", "region", "instanceid"))
            .dataSource(dataSource)
            .displayName("ec2")
            .tags(List.of())
            .type("ec2")
            .accountIdToNameFn( (_) -> null)
            .build();
    }

    @Test
    void primaryDtoIsFullyPopulated() throws JsonProcessingException {
        AssetDocumentHelper helper = getHelper("aws", "instanceid");
        var mappedAsMap = JsonHelper.mapFromString(getSampleMapperPrimaryDocument());

        var dto = helper.createFrom(mappedAsMap);
        assertNotNull(dto);

        var dtoAsMap = JsonHelper.mapFromString(JsonHelper.objectMapper.writeValueAsString(dto));
        var expectedAssetMap = JsonHelper.mapFromString(getSamplePrimaryAssetDocument());

        assertNotNull(dtoAsMap.get(AssetDocumentFields.LOAD_DATE));

        // assetRiskScore & arsLoadDate are set by a later job
        // LoadDate is different for each test; a check that it exists is good enough
        // tags aren't being set at this time, they don't appear te be required
        var skippedFields = Set.of("assetRiskScore", "arsLoadDate", AssetDocumentFields.LOAD_DATE, "tags.Name", "tags.Tenant");

        // Ensure each value in the sample asset exists in the serialized/deserialized instance
        expectedAssetMap.forEach((key, value) -> {
            if (!skippedFields.contains(key)) {
                assertTrue(dtoAsMap.containsKey(key), key);
                assertEquals(value, dtoAsMap.get(key),
                    STR."\{key} value differs. expected=\{value} actual=\{dtoAsMap.get(key)}");
            }
        });

        // Ensure a few extra properties are set as well
        assertEquals("64_us-west-1_i-76", dto.getDocId());
        assertEquals("ec2", dto.getDocType());
        assertEquals("aws", dto.getReportingSource());
        assertEquals("64", dto.getAccountId());
        assertEquals("fubar", dto.getAccountName());
        assertEquals("Paladin Cloud", dto.getCspmSource());
        assertEquals("aws", dto.getReportingSource());
        assertTrue(dto.isEntity());
        assertTrue(dto.isOwner());
        assertTrue(dto.isLatest());
    }

    @Test
    void dtoIsUpdated() throws JsonProcessingException {
        var mappedAsMap = JsonHelper.mapFromString(getSampleMapperPrimaryDocument());
        var dto = getHelper("aws", "instanceid").createFrom(mappedAsMap);

        mappedAsMap.put(AssetDocumentFields.ACCOUNT_NAME, "new account name");

        AssetDocumentHelper helper = getHelper("aws", "instanceid");
        helper.updateFrom(mappedAsMap, dto);

        assertEquals("new account name", dto.getAccountName());
        assertTrue(dto.isLatest());
    }

    /**
     * Ensure an updated DTO receives new fields - this is the upgrade strategy for existing
     * documents.
     */
    @Test
    void updatedDtoHasNewFields() throws JsonProcessingException {
        var mappedAsMap = JsonHelper.mapFromString(getSampleMapperPrimaryDocument());
        var dto = getHelper("aws", "instanceid").createFrom(mappedAsMap);

        AssetDocumentHelper helper = getHelper("aws", "instanceid");
        helper.updateFrom(mappedAsMap, dto);

        assertEquals("Paladin Cloud", dto.getCspmSource());
        assertEquals("aws", dto.getReportingSource());
    }

    @Test
void secondaryDtoIsFullyPopulated() throws JsonProcessingException {
        AssetDocumentHelper helper = getHelper("secondary", "_resourceid");
        var mappedAsMap = JsonHelper.mapFromString(getSampleSecondaryDocument());
        var dto = helper.createFrom(mappedAsMap);
        assertNotNull(dto);

        assertEquals("Paladin Cloud", dto.getCspmSource());
        assertEquals("secondary", dto.getReportingSource());
    }

    private String getSampleMapperPrimaryDocument() {
        return """
            {
                "_cspm_source": "Paladin Cloud",
                "_reporting_source": "aws",
                "monitoringstate": "disabled",
                "imageid": "ami-43",
                "discoverydate": "2024-07-23 18:00:00+0000",
                "clienttoken": "55-33",
                "hostid": "",
                "statereasoncode": "Client.UserInitiatedShutdown",
                "_cloudType": "Aws",
                "statetransitionreason": "User initiated (2024-02-09 06:43:46 GMT)",
                "statecode": "80",
                "platform": "",
                "privatednsname": "ip-10-0-400-500.ec2.internal",
                "accountid": "64",
                "enasupport": "true",
                "sourcedestcheck": "true",
                "virtualizationtype": "hvm",
                "accountname": "fubar",
                "rootdevicename": "/dev/xvda",
                "iaminstanceprofilearn": "",
                "hypervisor": "xen",
                "instancetype": "t2.xlarge",
                "spotinstancerequestid": "",
                "keyname": "fdnew",
                "architecture": "x86_64",
                "launchtime": "2024-02-09 06:39:55+0000",
                "subnetid": "subnet-76",
                "amilaunchindex": "0",
                "kernelid": "",
                "ramdiskid": "",
                "tenancy": "default",
                "rootdevicetype": "ebs",
                "statename": "stopped",
                "groupname": "",
                "sriovnetsupport": "",
                "ebsoptimized": "false",
                "privateipaddress": "10.0.400.500",
                "publicipaddress": "",
                "statereasonmessage": "Client.UserInitiatedShutdown: User initiated shutdown",
                "instanceid": "i-76",
                "iaminstanceprofileid": "",
                "publicdnsname": "",
                "vpcid": "vpc-88",
                "region": "us-west-1",
                "instancelifecycle": "",
                "availabilityzone": "us-west-1z",
                "affinity": ""
            }""".trim();
    }

    private String getSampleSecondaryDocument() {
        return """
            {
                "_cspm_source": "Paladin Cloud",
                "_reporting_source": "secondary",
                "_resourceid": "i-76",
                "_resourcename": "ABD-DEF",
                "_cloudType": "aws",
                "_entitytype": "server",
                "lastSeenTime": "2024-07-19T05:23:44Z",
                "osBuild": "20348",
                "serialNumber": "007",
                "publicIpAddress": "74.313.911.257",
                "discoverydate": "2024-07-25 16:32:26+0000",
                "systemProductName": "HVM domU",
                "externalId": "ext-7e86",
                "accountid": "64",
                "externalAccountId": "ext-7e86-3924",
                "osVersion": "Windows Server 2022",
                "provisionStatus": "Provisioned",
                "firstSeenTime": "2024-05-07T10:15:19Z",
                "osType": "Windows",
                "systemManufacturer": "Xen",
                "reducedFunctionalityMode": "no",
                "region": "us-2",
                "status": "normal"
            }""".trim();
    }

    private String getSamplePrimaryAssetDocument() {
        return """
            {
                "monitoringstate": "disabled",
                "imageid": "ami-43",
                "discoverydate": "2024-07-23 18:00:00+0000",
                "clienttoken": "55-33",
                "hostid": "",
                "statereasoncode": "Client.UserInitiatedShutdown",
                "_cloudType": "aws",
                "statetransitionreason": "User initiated (2024-02-09 06:43:46 GMT)",
                "statecode": "80",
                "platform": "",
                "privatednsname": "ip-10-0-400-500.ec2.internal",
                "accountid": "64",
                "enasupport": "true",
                "sourcedestcheck": "true",
                "virtualizationtype": "hvm",
                "accountname": "fubar",
                "rootdevicename": "/dev/xvda",
                "iaminstanceprofilearn": "",
                "hypervisor": "xen",
                "instancetype": "t2.xlarge",
                "spotinstancerequestid": "",
                "keyname": "fdnew",
                "architecture": "x86_64",
                "launchtime": "2024-02-09 06:39:55+0000",
                "subnetid": "subnet-76",
                "amilaunchindex": "0",
                "kernelid": "",
                "ramdiskid": "",
                "tenancy": "default",
                "rootdevicetype": "ebs",
                "statename": "stopped",
                "groupname": "",
                "sriovnetsupport": "",
                "ebsoptimized": "false",
                "privateipaddress": "10.0.400.500",
                "publicipaddress": "",
                "statereasonmessage": "Client.UserInitiatedShutdown: User initiated shutdown",
                "instanceid": "i-76",
                "iaminstanceprofileid": "",
                "publicdnsname": "",
                "vpcid": "vpc-88",
                "region": "us-west-1",
                "instancelifecycle": "",
                "availabilityzone": "us-west-1z",
                "affinity": "",
                "_resourcename": "i-76",
                "_resourceid": "i-76",
                "_docid": "64_us-west-1_i-76",
                "_entity": "true",
                "_entitytype": "ec2",
                "targettypedisplayname": "ec2",
                "docType": "ec2",
                "ec2_relations": "ec2",
                "firstdiscoveredon": "2024-07-23 18:00:00+0000",
                "assetRiskScore": 570,
                "arsLoadDate": "2024-07-23 18:45:00+0000",
                "tags.Name": "fd-mysql-upgrade",
                "tags.Tenant": "FD",
                "latest": true,
                "_loaddate": "2024-07-23 18:36:00+0000"
            }
        """.trim();
    }
}
