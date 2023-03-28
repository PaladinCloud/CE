package com.tmobile.cloud.awsrules.elasticsearch;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.anyObject;
import static org.powermock.api.mockito.PowerMockito.mockStatic;
import static org.powermock.api.mockito.PowerMockito.when;

import java.util.HashMap;
import java.util.Map;

import com.tmobile.pacman.commons.policy.Annotation;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.powermock.core.classloader.annotations.PowerMockIgnore;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import com.tmobile.cloud.awsrules.utils.PacmanUtils;
import com.tmobile.cloud.constants.PacmanRuleConstants;
import com.tmobile.pacman.commons.PacmanSdkConstants;
import com.tmobile.pacman.commons.policy.PolicyResult;

@PowerMockIgnore("jdk.internal.reflect.*")
@RunWith(PowerMockRunner.class)
@PrepareForTest({ PacmanUtils.class , Annotation.class})
public class ESNodetoNodeEncryptionRuleTest {

	@InjectMocks
	ESNodetoNodeEncryptionRule esNodetoNodeEncryptionRule;

	@Test
	public void esNodeWithEncryption() {
		Map<String, String> ruleParam = new HashMap<>();
		ruleParam.put(PacmanSdkConstants.EXECUTION_ID, "exectionid");
		ruleParam.put(PacmanSdkConstants.POLICY_ID, "esEncryptionAtRestRule");
		ruleParam.put(PacmanRuleConstants.CATEGORY, PacmanSdkConstants.SECURITY);
		ruleParam.put(PacmanRuleConstants.SEVERITY, PacmanSdkConstants.SEV_HIGH);
		Map<String, String> resourceAttribute = getResourceForEncypted("ES1234");
		PolicyResult ruleResult = esNodetoNodeEncryptionRule.execute(ruleParam, resourceAttribute);
		assertEquals(PacmanSdkConstants.STATUS_SUCCESS, ruleResult.getStatus());
	}

	@Test
	public void esNodeWithOutEncryption() {
		Map<String, String> ruleParam = new HashMap<>();
		ruleParam.put(PacmanSdkConstants.EXECUTION_ID, "exectionid");
		ruleParam.put(PacmanSdkConstants.POLICY_ID, "esEncryptionAtRestRule");
		ruleParam.put(PacmanRuleConstants.CATEGORY, PacmanSdkConstants.SECURITY);
		ruleParam.put(PacmanRuleConstants.SEVERITY, PacmanSdkConstants.SEV_HIGH);
		mockStatic(Annotation.class);
		when(Annotation.buildAnnotation(anyObject(),anyObject())).thenReturn(getMockAnnotation());
		Map<String, String> resourceAttribute = getResourceForWithOutEncryption("ES1234");
		PolicyResult ruleResult = esNodetoNodeEncryptionRule.execute(ruleParam, resourceAttribute);
		assertEquals(PacmanSdkConstants.STATUS_FAILURE, ruleResult.getStatus());
	}
	
	@Test
	public void esNodeWithLowerVersion() {
		Map<String, String> ruleParam = new HashMap<>();
		ruleParam.put(PacmanSdkConstants.EXECUTION_ID, "exectionid");
		ruleParam.put(PacmanSdkConstants.POLICY_ID, "esEncryptionAtRestRule");
		ruleParam.put(PacmanRuleConstants.CATEGORY, PacmanSdkConstants.SECURITY);
		ruleParam.put(PacmanRuleConstants.SEVERITY, PacmanSdkConstants.SEV_HIGH);

		Map<String, String> resourceAttribute = getResourceWithLowerVersion("ES1234");
		PolicyResult ruleResult = esNodetoNodeEncryptionRule.execute(ruleParam, resourceAttribute);
		assertEquals(PacmanSdkConstants.STATUS_SUCCESS, ruleResult.getStatus());
	}

	@Test
	public void getHelpTextTest() {
		assertThat(esNodetoNodeEncryptionRule.getHelpText(), is(notNullValue()));
	}

	private Map<String, String> getResourceForEncypted(String clusterID) {
		Map<String, String> clusterObj = new HashMap<>();
		clusterObj.put("_resourceid", clusterID);
		clusterObj.put("name", "AWS-ES");
		clusterObj.put("creationTimestamp", "2022-01-10T13:00:38.628-08:00");
		clusterObj.put("nodetonodeencryption", "true");
		clusterObj.put("elasticsearchversion", "6.0");

		return clusterObj;
	}

	private Map<String, String> getResourceForWithOutEncryption(String clusterID) {
		Map<String, String> clusterObj = new HashMap<>();
		clusterObj.put("_resourceid", clusterID);
		clusterObj.put("name", "AWS-ES");
		clusterObj.put("creationTimestamp", "2022-01-10T13:00:38.628-08:00");
		clusterObj.put("nodetonodeencryption", "false");
		clusterObj.put("elasticsearchversion", "6.0");

		return clusterObj;
	}
	
	private Map<String, String> getResourceWithLowerVersion(String clusterID) {
		Map<String, String> clusterObj = new HashMap<>();
		clusterObj.put("_resourceid", clusterID);
		clusterObj.put("name", "AWS-ES");
		clusterObj.put("creationTimestamp", "2022-01-10T13:00:38.628-08:00");
		clusterObj.put("nodetonodeencryption", "false");
		clusterObj.put("elasticsearchversion", "5.5");

		return clusterObj;
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
