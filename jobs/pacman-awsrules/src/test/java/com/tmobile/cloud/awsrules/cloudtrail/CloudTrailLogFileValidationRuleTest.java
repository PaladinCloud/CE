package com.tmobile.cloud.awsrules.cloudtrail;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Matchers.anyObject;
import static org.mockito.Matchers.anyString;
import static org.powermock.api.mockito.PowerMockito.mockStatic;
import static org.powermock.api.mockito.PowerMockito.when;

import java.util.HashMap;
import java.util.Map;

import com.tmobile.cloud.awsrules.utils.CommonTestUtils;
import com.tmobile.pacman.commons.policy.Annotation;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.powermock.core.classloader.annotations.PowerMockIgnore;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import com.tmobile.cloud.awsrules.utils.PacmanUtils;
import com.tmobile.cloud.constants.PacmanRuleConstants;
import com.tmobile.pacman.commons.PacmanSdkConstants;
import com.tmobile.pacman.commons.exception.InvalidInputException;
import com.tmobile.pacman.commons.policy.BasePolicy;
import com.tmobile.pacman.commons.policy.PolicyResult;

@PowerMockIgnore({ "javax.net.ssl.*", "javax.management.*" })
@RunWith(PowerMockRunner.class)
@PrepareForTest({ PacmanUtils.class, BasePolicy.class, Annotation.class })
public class CloudTrailLogFileValidationRuleTest {

	@InjectMocks
	CloudTrailLogFileValidationRule cloudTrailLogFileValidationRule;

	@Before
	public void setup() throws Exception {
		mockStatic(Annotation.class);
		when(Annotation.buildAnnotation(anyObject(), anyObject())).thenReturn(CommonTestUtils.getMockAnnotation());
	}

	@Test
	public void isEnabled() throws Exception {

		mockStatic(PacmanUtils.class);

		when(PacmanUtils.doesAllHaveValue(anyString(), anyString())).thenReturn(true);

		Map<String, String> ruleParam = getInputParamMap();
		ruleParam.put(PacmanRuleConstants.RESOURCE_ID, "test1");

		Map<String, String> resourceAttribute = getValidResourceData("test1");

		when(PacmanUtils.checkNaclWithInvalidRules(anyString(), anyString(), anyString())).thenReturn(true);

		PolicyResult ruleResult = cloudTrailLogFileValidationRule.execute(ruleParam, resourceAttribute);
		assertEquals(PacmanSdkConstants.STATUS_SUCCESS, ruleResult.getStatus());
	}

	@Test
	public void isNotEnabled() throws Exception {

		mockStatic(PacmanUtils.class);

		when(PacmanUtils.doesAllHaveValue(anyString(), anyString())).thenReturn(true);

		Map<String, String> ruleParam = getInputParamMap();
		ruleParam.put(PacmanRuleConstants.RESOURCE_ID, "test1");

		Map<String, String> resourceAttribute = getInValidResourceData("test1");

		when(PacmanUtils.checkNaclWithInvalidRules(anyString(), anyString(), anyString())).thenReturn(true);

		PolicyResult ruleResult = cloudTrailLogFileValidationRule.execute(ruleParam, resourceAttribute);
		assertEquals(PacmanSdkConstants.STATUS_FAILURE, ruleResult.getStatus());
	}

	@Test
	public void mandatoryDataTest() throws Exception {

		mockStatic(PacmanUtils.class);

		Map<String, String> ruleParam = getInputParamMap();
		ruleParam.put(PacmanRuleConstants.RESOURCE_ID, "test1");

		Map<String, String> resourceAttribute = getValidResourceData("test1");

		when(PacmanUtils.doesAllHaveValue(anyString(), anyString())).thenReturn(false);
		assertThatThrownBy(() -> cloudTrailLogFileValidationRule.execute(ruleParam, resourceAttribute))
				.isInstanceOf(InvalidInputException.class);

	}

	@Test
	public void getHelpTest() {

		assertNotNull(cloudTrailLogFileValidationRule.getHelpText());
	}

	private Map<String, String> getInputParamMap() {
		Map<String, String> ruleParam = new HashMap<>();
		ruleParam.put(PacmanSdkConstants.EXECUTION_ID, "exectionid");
		ruleParam.put(PacmanSdkConstants.POLICY_ID, "S3BucketEncryption_version-1_S3BucketWithoutEncryption_s3");
		ruleParam.put(PacmanRuleConstants.CATEGORY, PacmanSdkConstants.SECURITY);
		ruleParam.put(PacmanRuleConstants.SEVERITY, PacmanSdkConstants.SEV_HIGH);
		ruleParam.put(PacmanRuleConstants.ACCOUNTID, "123456789");
		return ruleParam;
	}

	private Map<String, String> getValidResourceData(String id) {
		Map<String, String> resObj = new HashMap<>();
		resObj.put("_resourceid", id);
		resObj.put(PacmanRuleConstants.CLOUD_TRAIL_LOG_FILE_VALIDATION, "true");
		return resObj;
	}

	private Map<String, String> getInValidResourceData(String id) {
		Map<String, String> resObj = new HashMap<>();
		resObj.put("_resourceid", id);
		resObj.put(PacmanRuleConstants.CLOUD_TRAIL_LOG_FILE_VALIDATION, "false");
		return resObj;
	}

}
