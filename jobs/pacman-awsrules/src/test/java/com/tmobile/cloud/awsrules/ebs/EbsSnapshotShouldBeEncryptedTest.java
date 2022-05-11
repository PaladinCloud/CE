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
package com.tmobile.cloud.awsrules.ebs;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.anyObject;
import static org.mockito.Matchers.anyString;
import static org.powermock.api.mockito.PowerMockito.mockStatic;
import static org.powermock.api.mockito.PowerMockito.when;

import java.util.HashMap;
import java.util.Map;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.powermock.core.classloader.annotations.PowerMockIgnore;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import com.tmobile.cloud.awsrules.utils.PacmanUtils;
import com.tmobile.pacman.commons.PacmanSdkConstants;

@PowerMockIgnore("jdk.internal.reflect.*")
@RunWith(PowerMockRunner.class)
@PrepareForTest({ PacmanUtils.class})
public class EbsSnapshotShouldBeEncryptedTest {

    @InjectMocks
    EbsSnapshotShouldBeEncrypted ebsSnapshotShouldBeEncrypted;
 
    @Test
    public void executeTest() throws Exception {
        mockStatic(PacmanUtils.class);
        Map<String, String> ruleParam = new HashMap<>();
		ruleParam.put(PacmanSdkConstants.EXECUTION_ID, "exectionid");
		ruleParam.put(PacmanSdkConstants.RULE_ID, "ruleid");
        when(PacmanUtils.doesAllHaveValue(anyString(),anyString(),anyString())).thenReturn(
                true);
        when(PacmanUtils.formatUrl(anyObject(),anyString())).thenReturn("host");
        when(PacmanUtils.checkISResourceIdExistsFromElasticSearch(anyString(),anyString(),anyString(),anyString())).thenReturn(true);
        assertThat(ebsSnapshotShouldBeEncrypted.execute(ruleParam,getEBSSnaphostWithoutEncryption("ebs123 ")), is(notNullValue()));
        
        when(PacmanUtils.checkISResourceIdExistsFromElasticSearch(anyString(),anyString(),anyString(),anyString())).thenReturn(true);
        assertThat(ebsSnapshotShouldBeEncrypted.execute(ruleParam, getEncryptedEBSSnaphost("ebs123")), is(notNullValue()));
             
          }
    
    private Map<String, String> getEncryptedEBSSnaphost(String ebsID) {
		Map<String, String> ebsResourceObj = new HashMap<>();
		ebsResourceObj.put("_resourceid", ebsID);
		ebsResourceObj.put("encrypted", "true");
		return ebsResourceObj;
	}
    
    private Map<String, String> getEBSSnaphostWithoutEncryption(String ebsID) {
		Map<String, String> ebsResourceObj = new HashMap<>();
		ebsResourceObj.put("_resourceid", ebsID);
		ebsResourceObj.put("encrypted", "false");
		return ebsResourceObj;
	}
    
    @Test
    public void getHelpTextTest(){
        assertThat(ebsSnapshotShouldBeEncrypted.getHelpText(), is(notNullValue()));
    }
}
