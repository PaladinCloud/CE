/*******************************************************************************
 *  Copyright 2023 Paladin Cloud, Inc. or its affiliates. All Rights Reserved.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License"); you may not
 *  use this file except in compliance with the License.  You may obtain a copy
 *  of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 *  WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  See the
 *  License for the specific language governing permissions and limitations under
 *  the License.
 ******************************************************************************/
package com.tmobile.cloud.redhatrules;

import com.tmobile.cloud.awsrules.utils.PacmanUtils;
import com.tmobile.cloud.redhatpolicies.misc.RedhatNotScanningClusterPolicy;
import com.tmobile.pacman.commons.PacmanSdkConstants;
import com.tmobile.pacman.commons.exception.InvalidInputException;
import com.tmobile.pacman.commons.policy.PolicyResult;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.powermock.api.mockito.PowerMockito.mockStatic;
import static org.powermock.api.mockito.PowerMockito.when;

@RunWith(PowerMockRunner.class)
@PrepareForTest(PacmanUtils.class)
public class RedhatNotScanningClusterPolicyTest {

    @InjectMocks
    RedhatNotScanningClusterPolicy policy;

    Map<String, String> commonMap;

    @Before
    public void setUp() {
        mockStatic(PacmanUtils.class);
        when(PacmanUtils.doesAllHaveValue(anyString(), anyString())).thenReturn(true);
        commonMap = getMapString();
    }

    @Test
    public void executeSuccessTest() throws Exception {
        when(PacmanUtils.getValueFromElasticSearchAsSet(anyString(), any(), any(),
                any(), any(), any())).thenReturn(new HashSet<>(Collections.singletonList("test")));
        PolicyResult result = policy.execute(commonMap, commonMap);
        Assert.assertEquals(result.getStatus(), PacmanSdkConstants.STATUS_SUCCESS);
    }

    @Test(expected = InvalidInputException.class)
    public void executeFailTest() {
        when(PacmanUtils.doesAllHaveValue(anyString(), anyString())).thenReturn(false);
        policy.execute(commonMap, commonMap);
    }

    @Test
    public void executeFailAnnotationTest() throws Exception {
        when(PacmanUtils.getValueFromElasticSearchAsSet(anyString(), any(), any(),
                any(), any(), any())).thenReturn(null);
        PolicyResult result = policy.execute(commonMap, commonMap);
        Assert.assertEquals(result.getStatus(), PacmanSdkConstants.STATUS_FAILURE);
    }

    public Map<String, String> getMapString() {
        Map<String, String> commonMap = new HashMap<>();
        commonMap.put("executionId", "1234");
        commonMap.put("_resourceid", "12345");
        commonMap.put("severity", "critical");
        commonMap.put("policyCategory", "security");
        commonMap.put("accountid", "12345");
        commonMap.put("region", "us-central-1c");
        commonMap.put("name", "cluster1");
        return commonMap;
    }
}
