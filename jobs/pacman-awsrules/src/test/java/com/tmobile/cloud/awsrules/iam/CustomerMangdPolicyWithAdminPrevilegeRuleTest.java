package com.tmobile.cloud.awsrules.iam;

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

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PowerMockIgnore;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import com.amazonaws.services.identitymanagement.AmazonIdentityManagementClient;
import com.tmobile.cloud.awsrules.utils.IAMUtils;
import com.tmobile.cloud.awsrules.utils.PacmanUtils;
import com.tmobile.cloud.constants.PacmanRuleConstants;
import com.tmobile.pacman.commons.PacmanSdkConstants;
import com.tmobile.pacman.commons.exception.InvalidInputException;
import com.tmobile.pacman.commons.exception.RuleExecutionFailedExeption;
import com.tmobile.pacman.commons.rule.BaseRule;
import com.tmobile.pacman.commons.rule.RuleResult;

@PowerMockIgnore({ "javax.net.ssl.*", "javax.management.*" })
@RunWith(PowerMockRunner.class)
@PrepareForTest({ PacmanUtils.class, BaseRule.class, IAMUtils.class })
public class CustomerMangdPolicyWithAdminPrevilegeRuleTest {

	@InjectMocks
	CustomerMangdPolicyWithAdminPrevilegeRule customerMangdPolicyWithAdminPrevilegeRule;

	@Mock
	AmazonIdentityManagementClient amazonIdentityManagementClient;

	@Before
	public void setUp() throws Exception {
		amazonIdentityManagementClient = PowerMockito.mock(AmazonIdentityManagementClient.class);
	}

	@Test
	public void policyWithFullAdminAccess() throws Exception {

		mockStatic(PacmanUtils.class);
		mockStatic(IAMUtils.class);
		when(PacmanUtils.doesAllHaveValue(anyString(), anyString(), anyString())).thenReturn(true);

		Map<String, String> ruleParam = getInputParamMap();
		ruleParam.put(PacmanRuleConstants.RESOURCE_ID, "arn:aws:iam::123456789:policy/test_ful_admin_policy");

		Map<String, Object> map = new HashMap<String, Object>();
		map.put("client", amazonIdentityManagementClient);
		CustomerMangdPolicyWithAdminPrevilegeRule spy = Mockito.spy(new CustomerMangdPolicyWithAdminPrevilegeRule());

		Mockito.doReturn(map).when((BaseRule) spy).getClientFor(anyObject(), anyString(), anyObject());

		when(IAMUtils.isPolicyWithFullAdminAccess(anyString(), anyObject())).thenReturn(true);
		Map<String, String> resourceAttribute = getResourceData("arn:aws:iam::123456789:policy/test_ful_admin_policy");
		resourceAttribute.put("policyname", "test_ful_admin_policy");

		RuleResult ruleResult = spy.execute(ruleParam, resourceAttribute);

		assertEquals(PacmanSdkConstants.STATUS_FAILURE, ruleResult.getStatus());
	}

	@Test
	public void policyWithoutFullAdminAccess() throws Exception {

		mockStatic(PacmanUtils.class);
		mockStatic(IAMUtils.class);
		when(PacmanUtils.doesAllHaveValue(anyString(), anyString(), anyString())).thenReturn(true);

		Map<String, String> ruleParam = getInputParamMap();
		ruleParam.put(PacmanRuleConstants.RESOURCE_ID, "arn:aws:iam::123456789:policy/test_admin_policy");

		Map<String, Object> map = new HashMap<String, Object>();
		map.put("client", amazonIdentityManagementClient);
		CustomerMangdPolicyWithAdminPrevilegeRule spy = Mockito.spy(new CustomerMangdPolicyWithAdminPrevilegeRule());

		Mockito.doReturn(map).when((BaseRule) spy).getClientFor(anyObject(), anyString(), anyObject());

		when(IAMUtils.isPolicyWithFullAdminAccess(anyString(), anyObject())).thenReturn(false);
		Map<String, String> resourceAttribute = getResourceData("arn:aws:iam::123456789:policy/test_admin_policy");
		resourceAttribute.put("policyname", "test_admin_policy");

		RuleResult ruleResult = spy.execute(ruleParam, resourceAttribute);

		assertEquals(PacmanSdkConstants.STATUS_SUCCESS, ruleResult.getStatus());
	}

	@Test
	public void mandatoryDataTest() throws Exception {

		mockStatic(PacmanUtils.class);
		mockStatic(IAMUtils.class);

		Map<String, String> ruleParam = getInputParamMap();
		ruleParam.put(PacmanRuleConstants.RESOURCE_ID, "arn:aws:iam::123456789:policy/test_admin_policy");

		Map<String, String> resourceAttribute = getResourceData("arn:aws:iam::123456789:policy/test_admin_policy");
		resourceAttribute.put("policyname", "test_admin_policy");

		assertThatThrownBy(() -> customerMangdPolicyWithAdminPrevilegeRule.execute(ruleParam, resourceAttribute))
				.isInstanceOf(InvalidInputException.class);

	}

	@Test
	public void exceptionTest() throws Exception {

		mockStatic(PacmanUtils.class);
		mockStatic(IAMUtils.class);

		when(PacmanUtils.doesAllHaveValue(anyString(), anyString(), anyString())).thenReturn(true);

		Map<String, String> ruleParam = getInputParamMap();
		ruleParam.put(PacmanRuleConstants.RESOURCE_ID, "arn:aws:iam::123456789:policy/test_admin_policy");

		Map<String, String> resourceAttribute = getResourceData("arn:aws:iam::123456789:policy/test_admin_policy");
		resourceAttribute.put("policyname", "test_admin_policy");

		Map<String, Object> map = new HashMap<String, Object>();
		map.put("client", amazonIdentityManagementClient);
		CustomerMangdPolicyWithAdminPrevilegeRule spy = Mockito.spy(new CustomerMangdPolicyWithAdminPrevilegeRule());

		Mockito.doReturn(map).when((BaseRule) spy).getClientFor(anyObject(), anyString(), anyObject());

		when(IAMUtils.isPolicyWithFullAdminAccess(anyString(), anyObject()))
				.thenThrow(new RuleExecutionFailedExeption());
		assertThatThrownBy(() -> spy.execute(ruleParam, resourceAttribute))
				.isInstanceOf(RuleExecutionFailedExeption.class);

	}

	private Map<String, String> getInputParamMap() {
		Map<String, String> ruleParam = new HashMap<>();
		ruleParam.put(PacmanSdkConstants.EXECUTION_ID, "exectionid");
		ruleParam.put(PacmanSdkConstants.RULE_ID,
				"AWS_IAM_Custom_Policy_With_Admin_Previlege_version-1_Admin_Previlege_iampolicies");
		ruleParam.put(PacmanRuleConstants.CATEGORY, PacmanSdkConstants.SECURITY);
		ruleParam.put(PacmanRuleConstants.SEVERITY, PacmanSdkConstants.SEV_HIGH);
		ruleParam.put(PacmanRuleConstants.ACCOUNTID, "123456789");
		ruleParam.put(PacmanSdkConstants.Role_IDENTIFYING_STRING, "test/ro");
		return ruleParam;
	}

	private Map<String, String> getResourceData(String id) {
		Map<String, String> resObj = new HashMap<>();
		resObj.put("_resourceid", id);
		resObj.put("policyarn", id);
		return resObj;
	}

	@Test
	public void getHelpTextTest() {
		assertThat(customerMangdPolicyWithAdminPrevilegeRule.getHelpText(), is(notNullValue()));
	}

}
