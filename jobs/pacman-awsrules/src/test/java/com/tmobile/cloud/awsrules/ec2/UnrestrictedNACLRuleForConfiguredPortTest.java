package com.tmobile.cloud.awsrules.ec2;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.anyObject;
import static org.mockito.Matchers.anyString;
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
import com.tmobile.pacman.commons.exception.InvalidInputException;
import com.tmobile.pacman.commons.exception.RuleExecutionFailedExeption;
import com.tmobile.pacman.commons.policy.BasePolicy;
import com.tmobile.pacman.commons.policy.PolicyResult;

@PowerMockIgnore({ "javax.net.ssl.*", "javax.management.*" })
@RunWith(PowerMockRunner.class)
@PrepareForTest({ PacmanUtils.class, BasePolicy.class , Annotation.class})
public class UnrestrictedNACLRuleForConfiguredPortTest {

	@InjectMocks
	UnrestrictedNACLRuleForConfiguredPort unrestrictedNACLRuleForConfiguredPort;

	@Test
	public void invalidNaclRule() throws Exception {

		mockStatic(PacmanUtils.class);
		when(PacmanUtils.doesAllHaveValue(anyString(), anyString(), anyString(), anyString())).thenReturn(true);

		Map<String, String> ruleParam = getInputParamMap();
		ruleParam.put(PacmanRuleConstants.RESOURCE_ID, "acl-123456789");
		
		Map<String, String> resourceAttribute = getResourceData("acl-123456789");

		when(PacmanUtils.checkNaclWithInvalidRules(anyString(), anyString(), anyString())).thenReturn(true);
		mockStatic(Annotation.class);
		when(Annotation.buildAnnotation(anyObject(),anyObject())).thenReturn(getMockAnnotation());
		PolicyResult ruleResult = unrestrictedNACLRuleForConfiguredPort.execute(ruleParam,resourceAttribute);
		
		assertEquals(PacmanSdkConstants.STATUS_FAILURE, ruleResult.getStatus());
	}

	

	@Test
	public void validNaclRule() throws Exception {

		mockStatic(PacmanUtils.class);
		when(PacmanUtils.doesAllHaveValue(anyString(), anyString(), anyString(), anyString())).thenReturn(true);

		Map<String, String> ruleParam = getInputParamMap();
		ruleParam.put(PacmanRuleConstants.RESOURCE_ID, "acl-123456789");
		
		Map<String, String> resourceAttribute = getResourceData("acl-123456789");

		when(PacmanUtils.checkNaclWithInvalidRules(anyString(), anyString(), anyString())).thenReturn(false);
		
		PolicyResult ruleResult = unrestrictedNACLRuleForConfiguredPort.execute(ruleParam,resourceAttribute);
		
		assertEquals(PacmanSdkConstants.STATUS_SUCCESS, ruleResult.getStatus());
	}

	@Test
	public void mandatoryDataTest() throws Exception {

		mockStatic(PacmanUtils.class);

		Map<String, String> ruleParam = getInputParamMap();
		ruleParam.put(PacmanRuleConstants.RESOURCE_ID, "123456789");

		Map<String, String> resourceAttribute = getResourceData("123456789");

		when(PacmanUtils.doesAllHaveValue(anyString(), anyString(), anyString(), anyString())).thenReturn(false);
		assertThatThrownBy(() -> unrestrictedNACLRuleForConfiguredPort.execute(ruleParam, resourceAttribute)).isInstanceOf(InvalidInputException.class);

	}

	@Test
	public void exceptionTest() throws Exception {

		mockStatic(PacmanUtils.class);

		when(PacmanUtils.doesAllHaveValue(anyString(), anyString(), anyString(), anyString())).thenReturn(true);

		Map<String, String> ruleParam = getInputParamMap();
		ruleParam.put(PacmanRuleConstants.RESOURCE_ID, "123456789");
		
		Map<String, String> resourceAttribute = getResourceData("123456789");

		when(PacmanUtils.checkNaclWithInvalidRules(anyString(), anyString(), anyString())).thenThrow(new RuleExecutionFailedExeption());
		assertThatThrownBy(() -> unrestrictedNACLRuleForConfiguredPort.execute(ruleParam, resourceAttribute)).isInstanceOf(RuleExecutionFailedExeption.class);

	}
	
	@Test
	public void getHelpTextTest() {
		assertThat(unrestrictedNACLRuleForConfiguredPort.getHelpText(), is(notNullValue()));
	}

	private Map<String, String> getInputParamMap() {
		Map<String, String> ruleParam = new HashMap<>();
		ruleParam.put(PacmanSdkConstants.EXECUTION_ID, "exectionid");
		ruleParam.put(PacmanSdkConstants.POLICY_ID, "NaclPublicAccessPort_version-1_EC2WithPublicAccessForConfiguredPort3389_networkacl");
		ruleParam.put(PacmanRuleConstants.CATEGORY, PacmanSdkConstants.SECURITY);
		ruleParam.put(PacmanRuleConstants.SEVERITY, PacmanSdkConstants.SEV_HIGH);
		ruleParam.put(PacmanRuleConstants.ACCOUNTID, "123456789");
		ruleParam.put(PacmanRuleConstants.PORT_TO_CHECK, "22");
		ruleParam.put("esNaclEntryUrl", "esNaclEntryUrl");
		return ruleParam;
	}

	private Map<String, String> getResourceData(String id) {
		Map<String, String> resObj = new HashMap<>();
		resObj.put("_resourceid", id);
		resObj.put("networkaclid", id);
		return resObj;
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
