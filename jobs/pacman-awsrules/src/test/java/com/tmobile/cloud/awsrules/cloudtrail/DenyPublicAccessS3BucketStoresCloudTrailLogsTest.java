/*******************************************************************************
        *Copyright <2023> Paladin Cloud, Inc or its affiliates. All Rights Reserved.
        *
        * Licensed under the Apache License, Version 2.0 (the "License"); You may not use
        * this file except in compliance with the License. A copy of the License is located at
        *
        * http://www.apache.org/licenses/LICENSE-2.0
        *
        * or in the "license" file accompanying this file. This file is distributed on
        * an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, express or
        * implied. See the License for the specific language governing permissions and
        * limitations under the License.
 ******************************************************************************/
package com.tmobile.cloud.awsrules.cloudtrail;
import com.tmobile.cloud.awsrules.utils.CommonTestUtils;
import com.tmobile.cloud.awsrules.utils.PacmanUtils;
import com.tmobile.pacman.commons.exception.InvalidInputException;
import com.tmobile.pacman.commons.policy.Annotation;
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
@PrepareForTest({PacmanUtils.class, Annotation.class})
public class DenyPublicAccessS3BucketStoresCloudTrailLogsTest {

    @InjectMocks
    DenyPublicAccessS3BucketStoresCloudTrailLogs denyPublicAccessS3BucketStoresCloudTrailLogs;

    @Test
    public void executeTest() throws Exception{
        mockStatic(Annotation.class);
        when(Annotation.buildAnnotation(anyObject(),anyObject())).thenReturn(CommonTestUtils.getMockAnnotation());
        mockStatic(PacmanUtils.class);
        when(PacmanUtils.doesAllHaveValue(anyString(),anyString())).thenReturn(
                true);
        assertThat(denyPublicAccessS3BucketStoresCloudTrailLogs.execute(getMapString("r_123 "),getMapString("r_123 ")), is(notNullValue()));
        when(PacmanUtils.doesAllHaveValue(anyString(),anyString())).thenReturn(
                false);
        assertThatThrownBy(
                () -> denyPublicAccessS3BucketStoresCloudTrailLogs.execute(getMapString("r_123 "),getMapString("r_123 "))).isInstanceOf(InvalidInputException.class);
    }

    @Test
    public void getHelpTextTest(){
        assertThat(denyPublicAccessS3BucketStoresCloudTrailLogs.getHelpText(), is(notNullValue()));
    }

    public static Map<String, String> getMapString(String passRuleResourceId) {
        Map<String, String> commonMap = new HashMap<>();
        commonMap.put("executionId", "1234");
        commonMap.put("_resourceid", passRuleResourceId);
        commonMap.put("severity", "critical");
        commonMap.put("ruleCategory", "security");
        commonMap.put("type", "Task");
        commonMap.put("accountid", "12345");
        commonMap.put("ruleId", "aws_s3_bucket_used_to_store_cloudtrail_logs_publicly_accessible");
        commonMap.put("policyId","aws_s3_bucket_used_to_store_cloudtrail_logs_publicly_accessible");
        commonMap.put("policyVersion", "version-1");
        String bucketPolicyString = "{\"Version\":\"2012-10-17\",\"Statement\":[{\"Sid\":\"AWSCloudTrailAclCheck20150319\",\"Effect\":\"Allow\",\"Principal\":{\"Service\":\"cloudtrail.amazonaws.com\"},\"Action\":\"s3:GetBucketAcl\",\"Resource\":\"arn:aws:s3:::aws-cloudtrail-logs-50283-122123\",\"Condition\":{\"StringEquals\":{\"AWS:SourceArn\":\"arn:aws:cloudtrail:us-east-1:500222:trail/management-events\"}}},{\"Sid\":\"AWSCloudTrailWrite20150319\",\"Effect\":\"Allow\",\"Principal\":{\"Service\":\"cloudtrail.amazonaws.com\"},\"Action\":\"s3:PutObject\",\"Resource\":\"arn:aws:s3:::aws-cloudtrail-logs-51211-12111/AWSLogs/5012111/*\",\"Condition\":{\"StringEquals\":{\"AWS:SourceArn\":\"arn:aws:cloudtrail:us-east-1:50012111:trail/management-events\",\"s3:x-amz-acl\":\"bucket-owner-full-control\"}}}]}";
        commonMap.put("bucketpolicy",bucketPolicyString);
        return commonMap;
    }
}
