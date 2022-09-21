package com.tmobile.cloud.awsrules.s3;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;
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
import com.tmobile.cloud.constants.PacmanRuleConstants;
import com.tmobile.pacman.commons.PacmanSdkConstants;
import com.tmobile.pacman.commons.exception.InvalidInputException;
import com.tmobile.pacman.commons.rule.BaseRule;
import com.tmobile.pacman.commons.rule.RuleResult;

@PowerMockIgnore({ "javax.net.ssl.*", "javax.management.*" })
@RunWith(PowerMockRunner.class)
@PrepareForTest({ PacmanUtils.class, BaseRule.class })
public class CheckHTTPDeniedRuleTest {

	@InjectMocks
	CheckHTTPDeniedRule checkHTTPDeniedRule;

	@Test
	public void s3BucketWithoutValidPolicy() throws Exception {

		mockStatic(PacmanUtils.class);
		when(PacmanUtils.doesAllHaveValue(anyString(), anyString())).thenReturn(true);

		Map<String, String> ruleParam = getInputParamMap();
		ruleParam.put(PacmanRuleConstants.RESOURCE_ID, "test1");

		Map<String, String> resourceAttribute = getInValidResourceData("test1");

		when(PacmanUtils.checkNaclWithInvalidRules(anyString(), anyString(), anyString())).thenReturn(true);

		RuleResult ruleResult = checkHTTPDeniedRule.execute(ruleParam, resourceAttribute);

		assertEquals(PacmanSdkConstants.STATUS_FAILURE, ruleResult.getStatus());
	}

	@Test
	public void s3BucketWithValidPolicy() throws Exception {

		mockStatic(PacmanUtils.class);
		when(PacmanUtils.doesAllHaveValue(anyString(), anyString())).thenReturn(true);

		Map<String, String> ruleParam = getInputParamMap();
		ruleParam.put(PacmanRuleConstants.RESOURCE_ID, "test1");

		Map<String, String> resourceAttribute = getValidResourceData("test1");

		when(PacmanUtils.checkNaclWithInvalidRules(anyString(), anyString(), anyString())).thenReturn(false);

		RuleResult ruleResult = checkHTTPDeniedRule.execute(ruleParam, resourceAttribute);

		assertEquals(PacmanSdkConstants.STATUS_SUCCESS, ruleResult.getStatus());
	}

	@Test
	public void mandatoryDataTest() throws Exception {

		mockStatic(PacmanUtils.class);

		Map<String, String> ruleParam = getInputParamMap();
		ruleParam.put(PacmanRuleConstants.RESOURCE_ID, "test1");

		Map<String, String> resourceAttribute = getValidResourceData("test1");

		when(PacmanUtils.doesAllHaveValue(anyString(), anyString())).thenReturn(false);
		assertThatThrownBy(() -> checkHTTPDeniedRule.execute(ruleParam, resourceAttribute))
				.isInstanceOf(InvalidInputException.class);

	}

	@Test
	public void getHelpTextTest() {
		assertThat(checkHTTPDeniedRule.getHelpText(), is(notNullValue()));
	}

	private Map<String, String> getInputParamMap() {
		Map<String, String> ruleParam = new HashMap<>();
		ruleParam.put(PacmanSdkConstants.EXECUTION_ID, "exectionid");
		ruleParam.put(PacmanSdkConstants.RULE_ID, "S3BucketAllowHTTPRequest_version-1_S3BucketAllowHTTPRequest_s3");
		ruleParam.put(PacmanRuleConstants.CATEGORY, PacmanSdkConstants.SECURITY);
		ruleParam.put(PacmanRuleConstants.SEVERITY, PacmanSdkConstants.SEV_HIGH);
		ruleParam.put(PacmanRuleConstants.ACCOUNTID, "123456789");
		return ruleParam;
	}

	private Map<String, String> getValidResourceData(String id) {
		Map<String, String> resObj = new HashMap<>();
		resObj.put("_resourceid", id);
		resObj.put("bucketpolicy", "{\"Version\":\"2012-10-17\",\"Statement\":[{\"Sid\":\"1\",\"Effect\":\"Deny\",\"Principal\":\"*\",\"Action\":\"s3:*\",\"Resource\":\"arn:aws:s3:::test1/*\",\"Condition\":{\"Bool\":{\"aws:SecureTransport\":\"false\"}}}]}");
		return resObj;
	}

	private Map<String, String> getInValidResourceData(String id) {
		Map<String, String> resObj = new HashMap<>();
		resObj.put("_resourceid", id);
		resObj.put("bucketpolicy", "{\"Version\":\"2012-10-17\",\"Statement\":[{\"Sid\":\"1\",\"Effect\":\"Allow\",\"Principal\":\"*\",\"Action\":\"s3:*\",\"Resource\":\"arn:aws:s3:::test1/*\",\"Condition\":{\"Bool\":{\"aws:SecureTransport\":\"true\"}}}]}");
		return resObj;
	}
}
