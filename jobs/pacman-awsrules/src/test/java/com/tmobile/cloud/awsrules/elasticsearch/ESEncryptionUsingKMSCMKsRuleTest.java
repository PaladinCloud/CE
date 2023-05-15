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
@PrepareForTest({ PacmanUtils.class, Annotation.class  })
public class ESEncryptionUsingKMSCMKsRuleTest {

	@InjectMocks
	ESEncryptionUsingKMSCMKsRule esEncryptionUsingKMSCMKsRule;

	@Test
	public void esWithEncryptionWithDefaultKMSKey() {
		Map<String, String> ruleParam = new HashMap<>();
		ruleParam.put(PacmanSdkConstants.EXECUTION_ID, "exectionid");
		ruleParam.put(PacmanSdkConstants.POLICY_ID, "esEncryptionAtRestRule");
		ruleParam.put(PacmanRuleConstants.CATEGORY, PacmanSdkConstants.SECURITY);
		ruleParam.put(PacmanRuleConstants.SEVERITY, PacmanSdkConstants.SEV_HIGH);
		Map<String, String> resourceAttribute = getResourceFordefaultEncypted("ES1234");
		mockStatic(Annotation.class);
		when(Annotation.buildAnnotation(anyObject(),anyObject())).thenReturn(getMockAnnotation());
		PolicyResult ruleResult = esEncryptionUsingKMSCMKsRule.execute(ruleParam, resourceAttribute);
		assertEquals(PacmanSdkConstants.STATUS_FAILURE, ruleResult.getStatus());
	}
	
	@Test
	public void esWithEncryptionWithCusmotKMSKey() {
		Map<String, String> ruleParam = new HashMap<>();
		ruleParam.put(PacmanSdkConstants.EXECUTION_ID, "exectionid");
		ruleParam.put(PacmanSdkConstants.POLICY_ID, "esEncryptionAtRestRule");
		ruleParam.put(PacmanRuleConstants.CATEGORY, PacmanSdkConstants.SECURITY);
		ruleParam.put(PacmanRuleConstants.SEVERITY, PacmanSdkConstants.SEV_HIGH);
		Map<String, String> resourceAttribute = getResourceForEncypted("ES1234");
		PolicyResult ruleResult = esEncryptionUsingKMSCMKsRule.execute(ruleParam, resourceAttribute);
		assertEquals(PacmanSdkConstants.STATUS_SUCCESS, ruleResult.getStatus());
	}

	@Test
	public void esWithOutEncryption() {
		Map<String, String> ruleParam = new HashMap<>();
		ruleParam.put(PacmanSdkConstants.EXECUTION_ID, "exectionid");
		ruleParam.put(PacmanSdkConstants.POLICY_ID, "esEncryptionAtRestRule");
		ruleParam.put(PacmanRuleConstants.CATEGORY, PacmanSdkConstants.SECURITY);
		ruleParam.put(PacmanRuleConstants.SEVERITY, PacmanSdkConstants.SEV_HIGH);
		mockStatic(Annotation.class);
		when(Annotation.buildAnnotation(anyObject(),anyObject())).thenReturn(getMockAnnotation());
		Map<String, String> resourceAttribute = getResourceForWithOutEncryption("ES1234");
		PolicyResult ruleResult = esEncryptionUsingKMSCMKsRule.execute(ruleParam, resourceAttribute);
		assertEquals(PacmanSdkConstants.STATUS_FAILURE, ruleResult.getStatus());
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
	public void esWithLowerVersion() {
		Map<String, String> ruleParam = new HashMap<>();
		ruleParam.put(PacmanSdkConstants.EXECUTION_ID, "exectionid");
		ruleParam.put(PacmanSdkConstants.POLICY_ID, "esEncryptionAtRestRule");
		ruleParam.put(PacmanRuleConstants.CATEGORY, PacmanSdkConstants.SECURITY);
		ruleParam.put(PacmanRuleConstants.SEVERITY, PacmanSdkConstants.SEV_HIGH);

		Map<String, String> resourceAttribute = getResourceWithLowerVersion("ES1234");
		PolicyResult ruleResult = esEncryptionUsingKMSCMKsRule.execute(ruleParam, resourceAttribute);
		assertEquals(PacmanSdkConstants.STATUS_SUCCESS, ruleResult.getStatus());
	}

	@Test
	public void getHelpTextTest() {
		assertThat(esEncryptionUsingKMSCMKsRule.getHelpText(), is(notNullValue()));
	}

	private Map<String, String> getResourceFordefaultEncypted(String clusterID) {
		Map<String, String> clusterObj = new HashMap<>();
		clusterObj.put("_resourceid", clusterID);
		clusterObj.put("name", "AWS-ES");
		clusterObj.put("creationTimestamp", "2022-01-10T13:00:38.628-08:00");
		clusterObj.put("encryptionenabled", "true");
		clusterObj.put("encryptionkmskey", "(Default) aws/es");
		clusterObj.put("elasticsearchversion", "5.5");

		return clusterObj;
	}

	private Map<String, String> getResourceForEncypted(String clusterID) {
		Map<String, String> clusterObj = new HashMap<>();
		clusterObj.put("_resourceid", clusterID);
		clusterObj.put("name", "AWS-ES");
		clusterObj.put("creationTimestamp", "2022-01-10T13:00:38.628-08:00");
		clusterObj.put("encryptionenabled", "true");
		clusterObj.put("encryptionkmskey", "true");
		clusterObj.put("elasticsearchversion", "5.5");

		return clusterObj;
	}

	private Map<String, String> getResourceForWithOutEncryption(String clusterID) {
		Map<String, String> clusterObj = new HashMap<>();
		clusterObj.put("_resourceid", clusterID);
		clusterObj.put("name", "AWS-ES");
		clusterObj.put("creationTimestamp", "2022-01-10T13:00:38.628-08:00");
		clusterObj.put("encryptionenabled", "false");
		clusterObj.put("elasticsearchversion", "5.5");

		return clusterObj;
	}
	
	private Map<String, String> getResourceWithLowerVersion(String clusterID) {
		Map<String, String> clusterObj = new HashMap<>();
		clusterObj.put("_resourceid", clusterID);
		clusterObj.put("name", "AWS-ES");
		clusterObj.put("creationTimestamp", "2022-01-10T13:00:38.628-08:00");
		clusterObj.put("encryptionenabled", "true");
		clusterObj.put("encryptionkmskey", "(Default) aws/es");
		clusterObj.put("elasticsearchversion", "5.0");

		return clusterObj;
	}

}
