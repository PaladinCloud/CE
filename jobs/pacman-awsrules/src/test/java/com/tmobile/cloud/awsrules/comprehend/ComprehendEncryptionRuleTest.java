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
package com.tmobile.cloud.awsrules.comprehend;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.anyObject;
import static org.mockito.Matchers.anyString;
import static org.powermock.api.mockito.PowerMockito.mockStatic;
import static org.powermock.api.mockito.PowerMockito.when;

import java.util.HashMap;
import java.util.Map;

import com.tmobile.cloud.awsrules.utils.CommonTestUtils;
import com.tmobile.pacman.commons.policy.Annotation;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import com.tmobile.cloud.awsrules.utils.PacmanUtils;
import com.tmobile.pacman.commons.exception.InvalidInputException;

@RunWith(PowerMockRunner.class)
@PrepareForTest({ PacmanUtils.class, Annotation.class})
public class ComprehendEncryptionRuleTest {

    @InjectMocks
    ComprehendEncryptionRule comprehendEncryptionRule;
 
    @Test
    public void executeTest() throws Exception {
        mockStatic(Annotation.class);
        when(Annotation.buildAnnotation(anyObject(), anyObject())).thenReturn(CommonTestUtils.getMockAnnotation());
        mockStatic(PacmanUtils.class);
        when(PacmanUtils.doesAllHaveValue(anyString(),anyString())).thenReturn(true);
        
        assertThat(comprehendEncryptionRule.execute(getMapString("r_123 "),getMapString("r_123 ")), is(notNullValue()));
        
        assertThat(comprehendEncryptionRule.execute(getMapNotExistString("r_123 "),getMapNotExistString("r_123 ")), is(notNullValue()));
        
        when(PacmanUtils.doesAllHaveValue(anyString(),anyString())).thenReturn(false);
        assertThatThrownBy(
                () -> comprehendEncryptionRule.execute(getMapString("r_123 "),getMapString("r_123 "))).isInstanceOf(InvalidInputException.class);
        
    }
    
    @Test
    public void getHelpTextTest(){
        assertThat(comprehendEncryptionRule.getHelpText(), is(notNullValue()));
    }
    
    public static Map<String, String> getMapString(String passRuleResourceId) {
        Map<String, String> commonMap = new HashMap<>();
        commonMap.put("executionId", "1234");
        commonMap.put("_resourceid", passRuleResourceId);
        commonMap.put("severity", "high");
        commonMap.put("ruleCategory", "security");
        commonMap.put("accountid", "12345");
        commonMap.put("ruleId", "Aws_comprehend_job_results_should_be_encrypted_version-1_aws_enable_job_results_encryption_awscomprehend");
        commonMap.put("policyId", "Aws_comprehend_job_results_should_be_encrypted_version-1");
        commonMap.put("policyVersion", "version-1");
        commonMap.put("kmskeyid", "keyid");
        return commonMap;
    }
    
    public static Map<String, String> getMapNotExistString(String passRuleResourceId) {
        Map<String, String> commonMap = new HashMap<>();
        commonMap.put("executionId", "1234");
        commonMap.put("_resourceid", passRuleResourceId);
        commonMap.put("severity", "high");
        commonMap.put("ruleCategory", "security");
        commonMap.put("accountid", "12345");
        commonMap.put("ruleId", "Aws_comprehend_job_results_should_be_encrypted_version-1_aws_enable_job_results_encryption_awscomprehend");
        commonMap.put("policyId", "Aws_comprehend_job_results_should_be_encrypted_version-1");
        commonMap.put("policyVersion", "version-1");
        commonMap.put("kmskeyid", "");
        return commonMap;
    }
    
}
