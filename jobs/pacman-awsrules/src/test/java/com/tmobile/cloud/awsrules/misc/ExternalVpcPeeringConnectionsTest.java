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
package com.tmobile.cloud.awsrules.misc;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.anyString;
import static org.powermock.api.mockito.PowerMockito.mockStatic;
import static org.powermock.api.mockito.PowerMockito.when;

import java.util.HashMap;
import java.util.Map;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import com.tmobile.cloud.awsrules.utils.PacmanUtils;
import com.tmobile.pacman.commons.exception.InvalidInputException;
import com.tmobile.pacman.commons.exception.RuleExecutionFailedExeption;

@RunWith(PowerMockRunner.class)
@PrepareForTest({ PacmanUtils.class})
public class ExternalVpcPeeringConnectionsTest {

    @InjectMocks
    ExternalVpcPeeringConnections externalVpcPeeringConnections;
 
    @Test
    public void executeTest() throws Exception {
        mockStatic(PacmanUtils.class);
        when(PacmanUtils.doesAllHaveValue(anyString(),anyString(),anyString())).thenReturn(true);
        
        when(PacmanUtils.getPacmanHost(anyString())).thenReturn("host");
        
        when(PacmanUtils.getCountOfAccountIds(anyString(), anyString(), anyString())).thenReturn(1);
        assertThat(externalVpcPeeringConnections.execute(getSameAccountIdMap("r_123 "), getSameAccountIdMap("r_123 ")), is(notNullValue()));
        
        when(PacmanUtils.getCountOfAccountIds(anyString(), anyString(), anyString())).thenReturn(0);
        assertThat(externalVpcPeeringConnections.execute(getDifferentAccountIdMap("r_123 "), getDifferentAccountIdMap("r_123 ")), is(notNullValue()));
        
        when(PacmanUtils.getCountOfAccountIds(anyString(), anyString(), anyString())).thenThrow(new Exception());
        assertThatThrownBy( 
                () -> externalVpcPeeringConnections.execute(getSameAccountIdMap("r_123 "), getSameAccountIdMap("r_123 "))).isInstanceOf(RuleExecutionFailedExeption.class);
        
        when(PacmanUtils.doesAllHaveValue(anyString(),anyString(),anyString())).thenReturn(false);
        assertThatThrownBy(
                () -> externalVpcPeeringConnections.execute(getSameAccountIdMap("r_123 "), getSameAccountIdMap("r_123 "))).isInstanceOf(InvalidInputException.class);
        
    }
    
    public static Map<String, String> getSameAccountIdMap(String passRuleResourceId) {
        Map<String, String> commonMap = new HashMap<>();
        commonMap.put("executionId", "1234");
        commonMap.put("_resourceid", passRuleResourceId);
        commonMap.put("severity", "critical");
        commonMap.put("ruleCategory", "security");
        commonMap.put("type", "Task");
        commonMap.put("accountid", "6453991268547126");
        commonMap.put("ruleId", "PacMan_AwsVpcExternalPeeringConnection_version-1_AwsVpcExternalPeeringConnection_peeringconnection");
        commonMap.put("policyId", "PacMan_AwsVpcExternalPeeringConnection_version-1");
        commonMap.put("policyVersion", "version-1");
        commonMap.put("accountEsURL", "accountEsURL");
        commonMap.put("acceptervpcownerid", "6453991268547126");
        commonMap.put("requestervpcownerid", "6453991268547126");
        return commonMap;
    }
    
    public static Map<String, String> getDifferentAccountIdMap(String passRuleResourceId) {
        Map<String, String> commonMap = new HashMap<>();
        commonMap.put("executionId", "1234");
        commonMap.put("_resourceid", passRuleResourceId);
        commonMap.put("severity", "critical");
        commonMap.put("ruleCategory", "security");
        commonMap.put("type", "Task");
        commonMap.put("accountid", "4564534234112323");
        commonMap.put("ruleId", "PacMan_AwsVpcExternalPeeringConnection_version-1_AwsVpcExternalPeeringConnection_peeringconnection");
        commonMap.put("policyId", "PacMan_AwsVpcExternalPeeringConnection_version-1");
        commonMap.put("policyVersion", "version-1");
        commonMap.put("accountEsURL", "accountEsURL");
        commonMap.put("acceptervpcownerid", "6453991268547126");
        commonMap.put("requestervpcownerid", "4564534234112323");
        return commonMap;
    }
    
    @Test
    public void getHelpTextTest(){
        assertThat(externalVpcPeeringConnections.getHelpText(), is(notNullValue()));
    }
}
