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
import static org.mockito.Matchers.anyObject;
import static org.mockito.Matchers.anyString;
import static org.powermock.api.mockito.PowerMockito.mockStatic;
import static org.powermock.api.mockito.PowerMockito.when;

import com.tmobile.pacman.commons.PacmanSdkConstants;
import com.tmobile.pacman.commons.policy.Annotation;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import com.tmobile.cloud.awsrules.utils.CommonTestUtils;
import com.tmobile.cloud.awsrules.utils.PacmanEc2Utils;
import com.tmobile.cloud.awsrules.utils.PacmanUtils;
import com.tmobile.pacman.commons.exception.InvalidInputException;
import com.tmobile.pacman.commons.exception.RuleExecutionFailedExeption;

@RunWith(PowerMockRunner.class)
@PrepareForTest({ PacmanUtils.class,PacmanEc2Utils.class, Annotation.class})
public class EC2WithPublicIPAccessTest {

    @InjectMocks
    EC2WithPublicIPAccess ec2WithPublicIPAccess;
 
    @Test
    public void executeTest() throws Exception {
        mockStatic(PacmanUtils.class);
        when(PacmanUtils.doesAllHaveValue(anyString(),anyString(),anyString(),anyString(),anyString(),anyString(),anyString(),anyString(),anyString(),anyString(),anyString())).thenReturn(
                true);
        mockStatic(Annotation.class);
        when(Annotation.buildAnnotation(anyObject(),anyObject())).thenReturn(getMockAnnotation());
        when(PacmanUtils.getPacmanHost(anyString())).thenReturn("host");
        when(PacmanUtils.getRouteTableId(anyString(),anyString(),anyString(),anyString())).thenReturn(CommonTestUtils.getSetString("123"));
        
        when(PacmanUtils.isIgwFound(anyString(),anyString(),anyString(),anyObject(),anyObject(),anyString(),anyString(),anyString())).thenReturn(false);
        
        when(PacmanUtils.getSecurityGroupsByInstanceId(anyString(),anyString())).thenReturn(CommonTestUtils.getListSecurityGroupId());
        
        when(PacmanUtils.checkAccessibleToAll(anyObject(),anyString(),anyString(),anyString(),anyString(),anyString())).thenReturn(CommonTestUtils.getLinkedHashMapBoolean("123"));
        assertThat(ec2WithPublicIPAccess.execute(CommonTestUtils.getMapString("r_123 "),CommonTestUtils.getMapString("r_123 ")), is(notNullValue()));
        
        when(PacmanUtils.isIgwFound(anyString(),anyString(),anyString(),anyObject(),anyObject(),anyString(),anyString(),anyString())).thenReturn(false);
        assertThat(ec2WithPublicIPAccess.execute(CommonTestUtils.getMapString("r_123 "),CommonTestUtils.getMapString("r_123 ")), is(notNullValue()));
        when(PacmanUtils.checkAccessibleToAll(anyObject(),anyString(),anyString(),anyString(),anyString(),anyString())).thenReturn(CommonTestUtils.getMapBoolean("123"));
        when(PacmanUtils.isIgwFound(anyString(),anyString(),anyString(),anyObject(),anyObject(),anyString(),anyString(),anyString())).thenReturn(true);
        assertThat(ec2WithPublicIPAccess.execute(CommonTestUtils.getMapString("r_123 "),CommonTestUtils.getMapString("r_123 ")), is(notNullValue()));
       
        
        when(PacmanUtils.getRouteTableId(anyString(),anyString(),anyString(),anyString())).thenThrow(new Exception());
        assertThatThrownBy( 
                () -> ec2WithPublicIPAccess.execute(CommonTestUtils.getMapString("r_123 "),CommonTestUtils.getMapString("r_123 "))).isInstanceOf(RuleExecutionFailedExeption.class);
        
        
        
        
        when(PacmanUtils.doesAllHaveValue(anyString(),anyString(),anyString(),anyString(),anyString(),anyString(),anyString(),anyString(),anyString(),anyString(),anyString())).thenReturn(
                false);
        assertThatThrownBy(
                () -> ec2WithPublicIPAccess.execute(CommonTestUtils.getMapString("r_123 "),CommonTestUtils.getMapString("r_123 "))).isInstanceOf(InvalidInputException.class);
        assertThat(ec2WithPublicIPAccess.execute(CommonTestUtils.getOneMoreMapString("r_123 "),CommonTestUtils.getOneMoreMapString("r_123 ")), is(notNullValue()));
        
        
        
        
    }
    
    @Test
    public void getHelpTextTest(){
        assertThat(ec2WithPublicIPAccess.getHelpText(), is(notNullValue()));
    }
    private Annotation getMockAnnotation() {
        Annotation annotation=new Annotation();
        annotation.put(PacmanSdkConstants.POLICY_NAME,"Mock policy name");
        annotation.put(PacmanSdkConstants.POLICY_ID, "Mock policy id");
        annotation.put(PacmanSdkConstants.POLICY_VERSION, "Mock policy version");
        annotation.put(PacmanSdkConstants.RESOURCE_ID, "Mock resource id");
        annotation.put(PacmanSdkConstants.TYPE, "Mock type");
        return annotation;
    }
}
