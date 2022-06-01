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
package com.tmobile.cloud.awsrules.elb;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.anyString;
import static org.powermock.api.mockito.PowerMockito.mockStatic;
import static org.powermock.api.mockito.PowerMockito.when;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import com.tmobile.cloud.awsrules.utils.PacmanEc2Utils;
import com.tmobile.cloud.awsrules.utils.PacmanUtils;
import com.tmobile.pacman.commons.exception.InvalidInputException;
import com.tmobile.pacman.commons.exception.RuleExecutionFailedExeption;

@RunWith(PowerMockRunner.class)
@PrepareForTest({ PacmanUtils.class,PacmanEc2Utils.class})
public class ElbV2ListenerSecurityRuleTest {

    @InjectMocks
    ElbV2ListenerSecurityRule elbV2ListenerSecurityRule;
 
    @Test
    public void executeTest() throws Exception {
        mockStatic(PacmanUtils.class);
        when(PacmanUtils.doesAllHaveValue(anyString(),anyString())).thenReturn(true);
        
        when(PacmanUtils.getPacmanHost(anyString())).thenReturn("host");
        when(PacmanUtils.checkElbUsingSecuredProtocol(anyString(),anyString())).thenReturn(geValidSetString("r_123 "));
        assertThat(elbV2ListenerSecurityRule.execute(getMapString("r_123 "),getMapString("r_123 ")), is(notNullValue()));
        
        when(PacmanUtils.checkElbUsingSecuredProtocol(anyString(),anyString())).thenReturn(new HashSet<>());
        assertThat(elbV2ListenerSecurityRule.execute(getMapString("r_123 "),getMapString("r_123 ")), is(notNullValue()));
        
        
        when(PacmanUtils.checkElbUsingSecuredProtocol(anyString(),anyString())).thenThrow(new Exception());
        assertThatThrownBy( 
                () -> elbV2ListenerSecurityRule.execute(getMapString("r_123 "),getMapString("r_123 "))).isInstanceOf(RuleExecutionFailedExeption.class);
        
        when(PacmanUtils.doesAllHaveValue(anyString(),anyString())).thenReturn(false);
        assertThatThrownBy(
                () -> elbV2ListenerSecurityRule.execute(getMapString("r_123 "),getMapString("r_123 "))).isInstanceOf(InvalidInputException.class);
    }
    
    @Test
    public void getHelpTextTest(){
        assertThat(elbV2ListenerSecurityRule.getHelpText(), is(notNullValue()));
    }
    
    public static Map<String, String> getMapString(String passRuleResourceId) {
        Map<String, String> commonMap = new HashMap<>();
        commonMap.put("executionId", "1234");
        commonMap.put("_resourceid", passRuleResourceId);
        commonMap.put("severity", "critical");
        commonMap.put("ruleCategory", "security");
        commonMap.put("type", "Task");
        commonMap.put("accountid", "12345");
        commonMap.put("ruleId", "AwsElbV2ListenerSecurity_version-1_AwsListenerSecurity_appelb");
        commonMap.put("policyId", "AwsElbV2ListenerSecurity_version-1");
        commonMap.put("policyVersion", "version-1");
        commonMap.put("securityGroupName", "default");
        commonMap.put("statename", "running");
        commonMap.put("esElbV2ListenerURL", "esElbV2ListenerURL");
        return commonMap;
    }
    
    public static Set<String> geValidSetString(String passRuleResourceId) {
        Set<String> commonSet = new HashSet<>();
        commonSet.add("HTTPS");
        return commonSet;
    }
}
