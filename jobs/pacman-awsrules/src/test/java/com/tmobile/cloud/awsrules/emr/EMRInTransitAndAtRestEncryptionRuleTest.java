package com.tmobile.cloud.awsrules.emr;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;

import java.util.HashMap;
import java.util.Map;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.powermock.core.classloader.annotations.PowerMockIgnore;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import com.tmobile.cloud.awsrules.utils.PacmanUtils;
import com.tmobile.cloud.constants.PacmanRuleConstants;
import com.tmobile.pacman.commons.PacmanSdkConstants;
import com.tmobile.pacman.commons.rule.RuleResult;

@PowerMockIgnore("jdk.internal.reflect.*")
@RunWith(PowerMockRunner.class)
@PrepareForTest({ PacmanUtils.class })
public class EMRInTransitAndAtRestEncryptionRuleTest {

	@InjectMocks
	EMRInTransitAndAtRestEncryptionRule emrInTransitAndAtRestEncryptionRule;

	@Test
	public void elasticCacheWithEncryption() {
		Map<String, String> ruleParam = new HashMap<>();
		ruleParam.put(PacmanSdkConstants.EXECUTION_ID, "exectionid");
		ruleParam.put(PacmanSdkConstants.RULE_ID, "emrInTransitAndAtRestEncryptionRule");
		ruleParam.put(PacmanRuleConstants.CATEGORY, PacmanSdkConstants.SECURITY);
		ruleParam.put(PacmanRuleConstants.SEVERITY, PacmanSdkConstants.SEV_HIGH);
		Map<String, String> resourceAttribute = getResourceForEncyptedCache("EMR1234");
		RuleResult ruleResult = emrInTransitAndAtRestEncryptionRule.execute(ruleParam, resourceAttribute);
		assertEquals(PacmanSdkConstants.STATUS_SUCCESS, ruleResult.getStatus());
	}

	@Test
	public void elasticCacheWithOutEncryption() {
		Map<String, String> ruleParam = new HashMap<>();
		ruleParam.put(PacmanSdkConstants.EXECUTION_ID, "exectionid");
		ruleParam.put(PacmanSdkConstants.RULE_ID, "emrInTransitAndAtRestEncryptionRule");
		ruleParam.put(PacmanRuleConstants.CATEGORY, PacmanSdkConstants.SECURITY);
		ruleParam.put(PacmanRuleConstants.SEVERITY, PacmanSdkConstants.SEV_HIGH);

		Map<String, String> resourceAttribute = getResourceForWithOutEncryption("EMR1234");
		RuleResult ruleResult = emrInTransitAndAtRestEncryptionRule.execute(ruleParam, resourceAttribute);
		assertEquals(PacmanSdkConstants.STATUS_FAILURE, ruleResult.getStatus());
	}

	@Test
	public void getHelpTextTest() {
		assertThat(emrInTransitAndAtRestEncryptionRule.getHelpText(), is(notNullValue()));
	}

	private Map<String, String> getResourceForEncyptedCache(String clusterID) {
		Map<String, String> clusterObj = new HashMap<>();
		clusterObj.put("_resourceid", clusterID);
		clusterObj.put("name", "AWS-EMR");
		clusterObj.put("creationTimestamp", "2022-01-10T13:00:38.628-08:00");
		clusterObj.put("securityconfig", "securityconfig");

		return clusterObj;
	}

	private Map<String, String> getResourceForWithOutEncryption(String clusterID) {
		Map<String, String> clusterObj = new HashMap<>();
		clusterObj.put("_resourceid", clusterID);
		clusterObj.put("name", "AWS-EMR");
		clusterObj.put("creationTimestamp", "2022-01-10T13:00:38.628-08:00");
		clusterObj.put("securityconfig", null);

		return clusterObj;
	}

}
