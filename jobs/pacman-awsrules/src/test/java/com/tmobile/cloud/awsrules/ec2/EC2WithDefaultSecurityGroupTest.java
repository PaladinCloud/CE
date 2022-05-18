/*******************************************************************************
 * Copyright 2018 T Mobile, Inc. or its affiliates. All Rights Reserved.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License.  You may obtain a copy
 * of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  See the
 * License for the specific language governing permissions and limitations under
 * the License.
 ******************************************************************************/
package com.tmobile.cloud.awsrules.ec2;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.anyString;
import static org.powermock.api.mockito.PowerMockito.mockStatic;
import static org.powermock.api.mockito.PowerMockito.when;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import com.tmobile.cloud.awsrules.utils.CommonTestUtils;
import com.tmobile.cloud.awsrules.utils.PacmanUtils;
import com.tmobile.pacman.commons.exception.InvalidInputException;
import com.tmobile.pacman.commons.exception.RuleExecutionFailedExeption;

@RunWith(PowerMockRunner.class)
@PrepareForTest({ PacmanUtils.class})
public class EC2WithDefaultSecurityGroupTest {

    @InjectMocks
    EC2WithDefaultSecurityGroup ec2WithDefaultSecurityGroup;
 
    @Test
    public void executeTest() throws Exception {
        mockStatic(PacmanUtils.class);
        when(PacmanUtils.doesAllHaveValue(anyString(),anyString(),anyString(),anyString())).thenReturn(true);
        
        when(PacmanUtils.getPacmanHost(anyString())).thenReturn("host");
        
        when(PacmanUtils.getDefaultSecurityGroupsByInstanceId(anyString(),anyString(),anyString())).thenReturn(CommonTestUtils.getListSecurityGroupId());
        assertThat(ec2WithDefaultSecurityGroup.execute(getMapString("r_123 "),getMapString("r_123 ")), is(notNullValue()));
        
        when(PacmanUtils.getDefaultSecurityGroupsByInstanceId(anyString(),anyString(),anyString())).thenReturn(new ArrayList<>());
        assertThat(ec2WithDefaultSecurityGroup.execute(getMapString("r_123 "),getMapString("r_123 ")), is(notNullValue()));
        
        when(PacmanUtils.getDefaultSecurityGroupsByInstanceId(anyString(),anyString(),anyString())).thenThrow(new Exception());
        assertThatThrownBy( 
                () -> ec2WithDefaultSecurityGroup.execute(getMapString("r_123 "),getMapString("r_123 "))).isInstanceOf(RuleExecutionFailedExeption.class);
        
        when(PacmanUtils.doesAllHaveValue(anyString(),anyString(),anyString(),anyString())).thenReturn(false);
        assertThatThrownBy(
                () -> ec2WithDefaultSecurityGroup.execute(getMapString("r_123 "),getMapString("r_123 "))).isInstanceOf(InvalidInputException.class);
        
    }
    
    public static Map<String, String> getMapString(String passRuleResourceId) {
        Map<String, String> commonMap = new HashMap<>();
        commonMap.put("executionId", "1234");
        commonMap.put("_resourceid", passRuleResourceId);
        commonMap.put("severity", "critical");
        commonMap.put("ruleCategory", "security");
        commonMap.put("type", "Task");
        commonMap.put("accountid", "12345");
        commonMap.put("ruleId", "PacMan_AwsEc2WithDefaultSecurityGroup_version-1_AwsEc2WithDefaultSecurityGroup_ec2");
        commonMap.put("policyId", "PacMan_AwsEc2WithDefaultSecurityGroup_version-1");
        commonMap.put("policyVersion", "version-1");
        commonMap.put("securityGroupName", "default");
        commonMap.put("statename", "running");
        commonMap.put("esEc2SgURL", "esEc2SgURL");
        return commonMap;
    }
    
    @Test
    public void getHelpTextTest(){
        assertThat(ec2WithDefaultSecurityGroup.getHelpText(), is(notNullValue()));
    }
}
