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
import static org.mockito.Matchers.anyListOf;
import static org.mockito.Matchers.anySetOf;
import static org.mockito.Matchers.anyString;
import static org.powermock.api.mockito.PowerMockito.mockStatic;
import static org.powermock.api.mockito.PowerMockito.when;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import com.amazonaws.services.ec2.model.GroupIdentifier;
import com.amazonaws.services.elasticloadbalancingv2.model.Listener;
import com.tmobile.cloud.awsrules.utils.CommonTestUtils;
import com.tmobile.cloud.awsrules.utils.PacmanEc2Utils;
import com.tmobile.cloud.awsrules.utils.PacmanUtils;
import com.tmobile.pacman.commons.exception.InvalidInputException;
import com.tmobile.pacman.commons.exception.RuleExecutionFailedExeption;

@RunWith(PowerMockRunner.class)
@PrepareForTest({ PacmanUtils.class,PacmanEc2Utils.class})
public class UnrestrctedElbSecurityGroupTest {

    @InjectMocks
    UnrestrctedElbSecurityGroup unrestrctedElbSecurityGroup;
 
    @Test
    public void executeTest() throws Exception {
        mockStatic(PacmanUtils.class);
        when(PacmanUtils.doesAllHaveValue(anyString(),anyString(),anyString(),anyString(),anyString())).thenReturn(true);
        
        when(PacmanUtils.getPacmanHost(anyString())).thenReturn("host");
        
        when(PacmanUtils.getSecurityBroupIdByElb(anyString(),anyString(),anyString(),anyString())).thenReturn(CommonTestUtils.getListSecurityGroupId());
        when(PacmanUtils.getListenerPortsByElbArn(anyString(),anyString())).thenReturn(getListenerPorts("123"));
        when(PacmanUtils.checkUnrestrictedSgAccess(anySetOf(GroupIdentifier.class),anyListOf(Listener.class),anyString())).thenReturn(getSecurityGroups("r_123"));
        assertThat(unrestrctedElbSecurityGroup.execute(getMapString("r_123 "),getMapString("r_123 ")), is(notNullValue()));
        
        when(PacmanUtils.getSecurityBroupIdByElb(anyString(),anyString(),anyString(),anyString())).thenReturn(CommonTestUtils.getListSecurityGroupId());
        when(PacmanUtils.getListenerPortsByElbArn(anyString(),anyString())).thenReturn(getListenerPorts("123"));
        when(PacmanUtils.checkUnrestrictedSgAccess(anySetOf(GroupIdentifier.class),anyListOf(Listener.class),anyString())).thenReturn(new ArrayList<>());
        assertThat(unrestrctedElbSecurityGroup.execute(getMapString("r_123 "),getMapString("r_123 ")), is(notNullValue()));
        
        when(PacmanUtils.getSecurityBroupIdByElb(anyString(),anyString(),anyString(),anyString())).thenReturn(CommonTestUtils.getListSecurityGroupId());
        when(PacmanUtils.getListenerPortsByElbArn(anyString(),anyString())).thenReturn(getListenerPorts("123"));
        when(PacmanUtils.checkUnrestrictedSgAccess(anySetOf(GroupIdentifier.class),anyListOf(Listener.class),anyString())).thenThrow(new Exception());
        assertThatThrownBy( 
                () -> unrestrctedElbSecurityGroup.execute(getMapString("r_123 "),getMapString("r_123 "))).isInstanceOf(RuleExecutionFailedExeption.class);
        
        when(PacmanUtils.doesAllHaveValue(anyString(),anyString(),anyString(),anyString(),anyString())).thenReturn(false);
        assertThatThrownBy(
                () -> unrestrctedElbSecurityGroup.execute(getMapString("r_123 "),getMapString("r_123 "))).isInstanceOf(InvalidInputException.class);
    }
  

	@Test
    public void getHelpTextTest(){
        assertThat(unrestrctedElbSecurityGroup.getHelpText(), is(notNullValue()));
    }
	
	 public static Map<String, String> getMapString(String passRuleResourceId) {
	        Map<String, String> commonMap = new HashMap<>();
	        commonMap.put("executionId", "1234");
	        commonMap.put("_resourceid", passRuleResourceId);
	        commonMap.put("severity", "high");
	        commonMap.put("ruleCategory", "security");
	        commonMap.put("type", "Task");
	        commonMap.put("accountid", "12345");
	        commonMap.put("ruleId", "AwsElbWithUnrestrictedSecurityGroup_version-1_AwsElbWithUnrestrictedSecurityGroup_appelb");
	        commonMap.put("policyId", "AwsElbWithUnrestrictedSecurityGroup_version-1");
	        commonMap.put("policyVersion", "version-1");
	        commonMap.put("esSgURL", "esSgURL");
	        commonMap.put("esSgRulesUrl", "esSgRulesUrl");
	        commonMap.put("esElbV2ListenerURL", "esElbV2ListenerURL");
	        return commonMap;
	    }
	
	  
    private List<Map<String, String>> getSecurityGroups(String rId) {
    	Map<String, String> map = new HashMap<>();
    	map.put("ipprotocol", "HTTP");
    	map.put("toport", "443");
    	map.put("fromport", "443");
    	map.put("groupid", "sg-12343232423");
		return Arrays.asList(map);
	}

	private List<Listener> getListenerPorts(String arn) {
		Listener listener = new Listener();
		listener.setPort(80);
		listener.setProtocol("HTTP");
		listener.setListenerArn(arn);
		return Arrays.asList(listener);
	}
}
