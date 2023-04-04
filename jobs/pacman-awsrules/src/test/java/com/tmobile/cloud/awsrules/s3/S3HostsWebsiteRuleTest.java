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
package com.tmobile.cloud.awsrules.s3;

import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.anyObject;
import static org.powermock.api.mockito.PowerMockito.mockStatic;
import static org.powermock.api.mockito.PowerMockito.when;

import java.util.HashMap;
import java.util.Map;

import com.tmobile.cloud.awsrules.utils.PacmanEc2Utils;
import com.tmobile.cloud.awsrules.utils.PacmanUtils;
import com.tmobile.pacman.commons.policy.Annotation;
import org.junit.Test;

import com.tmobile.cloud.constants.PacmanRuleConstants;
import com.tmobile.pacman.commons.PacmanSdkConstants;
import com.tmobile.pacman.commons.policy.PolicyResult;
import org.junit.runner.RunWith;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

/**
 * Purpose: This test checks for s3 bucket containing web-site configuration.
 * 
 * Author: pavankumarchaitanya
 * 
 * Reviewers: Kamal, Kanchana
 * 
 * Modified Date: April 11th, 2019
 * 
 */
@RunWith(PowerMockRunner.class)
@PrepareForTest({Annotation.class})
public class S3HostsWebsiteRuleTest {

	@Test
	public void testExecute() {
		Map<String, String> ruleParam = new HashMap<>();
		Map<String, String> resourceAttributes = new HashMap<>();
		S3HostsWebsiteRule s3HostsWebsiteRule = new S3HostsWebsiteRule();
		resourceAttributes.put(PacmanSdkConstants.RESOURCE_ID, "test-resource-id");
		resourceAttributes.put(PacmanRuleConstants.WEB_SITE_CONFIGURATION, "true");
		ruleParam.put("executionId", "test");
		ruleParam.put(PacmanSdkConstants.POLICY_ID, "rule-id");
		mockStatic(Annotation.class);
		when(Annotation.buildAnnotation(anyObject(),anyObject())).thenReturn(getMockAnnotation());
		PolicyResult ruleResult = s3HostsWebsiteRule.execute(ruleParam, resourceAttributes);
		assertTrue(ruleResult.getStatus().equals(PacmanSdkConstants.STATUS_FAILURE));

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
	@Test
	public void testExecuteNoWebsiteConfiguration() {
		Map<String, String> ruleParam = new HashMap<>();
		Map<String, String> resourceAttributes = new HashMap<>();
		S3HostsWebsiteRule s3HostsWebsiteRule = new S3HostsWebsiteRule();
		resourceAttributes.put(PacmanSdkConstants.RESOURCE_ID, "test-resource-id");
		resourceAttributes.put(PacmanRuleConstants.WEB_SITE_CONFIGURATION, "false");
		ruleParam.put("executionId", "test");
		ruleParam.put(PacmanSdkConstants.POLICY_ID, "rule-id");
		PolicyResult ruleResult = s3HostsWebsiteRule.execute(ruleParam, resourceAttributes);
		assertTrue(ruleResult.getStatus().equals(PacmanSdkConstants.STATUS_SUCCESS));

	}

}