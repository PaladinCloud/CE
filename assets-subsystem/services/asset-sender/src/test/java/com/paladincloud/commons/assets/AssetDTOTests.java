package com.paladincloud.commons.assets;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.paladincloud.common.AssetDocumentFields;
import com.paladincloud.common.assets.AssetDTO;
import com.paladincloud.common.util.JsonHelper;
import com.paladincloud.common.util.TimeHelper;
import java.time.ZonedDateTime;
import org.junit.jupiter.api.Test;

public class AssetDTOTests {

    @Test
    void serializedAdditionalPropertyOverrides() throws JsonProcessingException {
        var original = createAsset();

        var expectedCloudType = "test";
        // Add a property to ensure it overrides the '_cloudType' property
        // NOTE: It DOES NOT overwrite the value in 'original' - that happens at serialization time.
        original.addAdditionalProperty("_cloudType", expectedCloudType);
        var asJson = JsonHelper.objectMapper.writeValueAsString(original);
        var deserialized = JsonHelper.objectMapper.readValue(asJson, AssetDTO.class);

        assertNotNull(deserialized);
        assertEquals(expectedCloudType, deserialized.getCloudType());
        assertNull(original.getCloudType());
    }

    /**
     * Some AssetDTO fields are stored as strings though they're booleans and dates. This validates
     * they're stored in JSON as the correct type for backward compatibility.
     */
    @Test
    void specialTypesUsedForSomeFields() throws JsonProcessingException {
        var dateTime = ZonedDateTime.now();
        var original = createAsset(dateTime);
        var asJson = JsonHelper.objectMapper.writeValueAsString(original);

        // Deserialize to a map to avoid using the Json directives - the directives are being
        // verified here
        var asMap = JsonHelper.mapFromString(asJson);
        assertNotNull(asMap);

        // These are serialized as strings even though they're booleans.
        assertEquals("true", asMap.get(AssetDocumentFields.ENTITY));

        // These are serialized as strings and exposed as ZonedDateTime
        assertEquals(TimeHelper.formatZeroSeconds(dateTime),
            asMap.get(AssetDocumentFields.LOAD_DATE));
        assertEquals(TimeHelper.formatZeroSeconds(dateTime),
            asMap.get(AssetDocumentFields.FIRST_DISCOVERED));
        assertEquals(TimeHelper.formatZeroSeconds(dateTime),
            asMap.get(AssetDocumentFields.DISCOVERY_DATE));
    }

    @Test
    void existingFormatWorksEndToEnd() throws JsonProcessingException {
        var sampleJson = getSampleSerializedDocument();

        // Get a round-trip from the string to AssetDTO back to string for validation
        var deserialized = JsonHelper.objectMapper.readValue(sampleJson, AssetDTO.class);
        assertNotNull(deserialized);
        var deserializedJson = JsonHelper.objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(deserialized);
        assertNotNull(deserializedJson);

        // Deserialize to a map to avoid using the Json directives - it's the directives being
        // verified here
        var sampleAsMap = JsonHelper.mapFromString(sampleJson);
        assertNotNull(sampleAsMap);
        var deserializedAsMap = JsonHelper.mapFromString(deserializedJson);
        assertNotNull(deserializedAsMap);

        // Ensure each value in the sample exists in the serialized/deserialized instance
        sampleAsMap.forEach((key, value) -> {
            assertTrue(deserializedAsMap.containsKey(key), key);
            assertEquals(value, deserializedAsMap.get(key), STR."\{key} value differs. actual=\{value} expected=\{deserializedAsMap.get(key)}");
        });
    }

    private AssetDTO createAsset() {
        return createAsset(ZonedDateTime.now());
    }

    private AssetDTO createAsset(ZonedDateTime dateTime) {
        var dto = new AssetDTO();
        dto.setDocId("1");
        dto.setName("name");
        dto.setLatest(true);
        dto.setEntity(true);
        dto.setLoadDate(dateTime);
        dto.setDiscoveryDate(dateTime);
        dto.setFirstDiscoveryDate(dateTime);
        return dto;
    }

    private String getSampleSerializedDocument() {
        return """
            {
                "monitoringstate": "enabled",
                "discoverydate": "2024-01-22 09:00:00+0000",
                "_docid": "aws_ec2_555_us-west-1_i-75",
                "hostid": "",
                "statereasoncode": "",
                "_resourceidNew": "aws_ec2_555_us-west-1_i-75",
                "_entity": "true",
                "accountid": "555",
                "virtualizationtype": "hvm",
                "accountname": "saasdev",
                "tags.Environment": "Prod",
                "rootdevicename": "/dev/xvda",
                "iaminstanceprofilearn": "arn:aws:iam::555:instance-profile/profile",
                "targettypedisplayname": "EC2",
                "tags.Application": "PaladinCloud",
                "ec2_relations": "ec2",
                "spotinstancerequestid": "",
                "tags.aws:autoscaling:groupName": "asg-44-1c0c-55-66-77",
                "keyname": "key",
                "_loaddate": "2024-01-22 09:11:00+0000",
                "launchtime": "2024-01-22 09:07:57+0000",
                "amilaunchindex": "0",
                "kernelid": "",
                "tags.Tenant": "key",
                "firstdiscoveredon": "2024-01-22 09:00:00+0000",
                "rootdevicetype": "ebs",
                "statename": "running",
                "groupname": "",
                "sriovnetsupport": "",
                "privateipaddress": "10.0.9.273",
                "instanceid": "i-75",
                "iaminstanceprofileid": "XYZ",
                "_entitytype": "ec2",
                "_resourcename": "i-75",
                "region": "us-west-1",
                "instancelifecycle": "",
                "availabilityzone": "us-west-1z",
                "affinity": "",
                "imageid": "ami-75",
                "tags.Created By": "fubar",
                "clienttoken": "43-72",
                "_cloudType": "Aws",
                "statetransitionreason": "",
                "statecode": "16",
                "assetRiskScore": 566,
                "platform": "",
                "privatednsname": "ip-10-0-9-273.ec2.internal",
                "enasupport": "true",
                "sourcedestcheck": "true",
                "hypervisor": "xen",
                "arsLoadDate": "2024-01-23 08:11:00+0530",
                "instancetype": "m5.xlarge",
                "architecture": "x86_64",
                "latest": false,
                "subnetid": "subnet-73",
                "_resourceid": "i-75",
                "ramdiskid": "",
                "docType": "ec2",
                "tenancy": "default",
                "ebsoptimized": "false",
                "publicipaddress": "",
                "statereasonmessage": "",
                "publicdnsname": "",
                "vpcid": "vpc-14"
                }
            """.trim();
    }
}
