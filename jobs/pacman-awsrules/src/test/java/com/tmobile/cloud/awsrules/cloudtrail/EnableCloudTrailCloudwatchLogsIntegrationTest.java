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
public class EnableCloudTrailCloudwatchLogsIntegrationTest {

    @InjectMocks
    EnableCloudTrailCloudwatchLogsIntegration enableCloudTrailCloudwatchLogsIntegration;

    @Test
    public void executeTest() throws Exception{
        mockStatic(Annotation.class);
        when(Annotation.buildAnnotation(anyObject(),anyObject())).thenReturn(CommonTestUtils.getMockAnnotation());
        mockStatic(PacmanUtils.class);
        when(PacmanUtils.doesAllHaveValue(anyString(),anyString())).thenReturn(
                true);
        assertThat(enableCloudTrailCloudwatchLogsIntegration.execute(getMapString("r_123 "),getMapString("r_123 ")), is(notNullValue()));
        when(PacmanUtils.doesAllHaveValue(anyString(),anyString())).thenReturn(
                false);
        assertThatThrownBy(
                () -> enableCloudTrailCloudwatchLogsIntegration.execute(getMapString("r_123 "),getMapString("r_123 "))).isInstanceOf(InvalidInputException.class);
    }

    @Test
    public void getHelpTextTest(){
        assertThat(enableCloudTrailCloudwatchLogsIntegration.getHelpText(), is(notNullValue()));
    }

    public static Map<String, String> getMapString(String passRuleResourceId) {
        Map<String, String> commonMap = new HashMap<>();
        commonMap.put("executionId", "1234");
        commonMap.put("_resourceid", passRuleResourceId);
        commonMap.put("severity", "high");
        commonMap.put("ruleCategory", "security");
        commonMap.put("type", "Task");
        commonMap.put("accountid", "12345");
        commonMap.put("ruleId", "PacMan_AWS_CloudTrail_Global_Services_version-1_Enable_CloudTrail_Global_Services_cloudtrail");
        commonMap.put("policyId", "PacMan_AWS_CloudTrail_Global_Services_version-1");
        commonMap.put("policyVersion", "version-1");
        commonMap.put("cloudWatchLogsLogGroupArn", "arn:aws:logs:54623:log-group:aws-cloudtrail-logs-50322-1232:*");
        return commonMap;
    }
}
