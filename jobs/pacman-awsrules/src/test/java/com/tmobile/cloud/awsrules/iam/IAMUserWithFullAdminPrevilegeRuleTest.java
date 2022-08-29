package com.tmobile.cloud.awsrules.iam;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.anyList;
import static org.mockito.Matchers.anyListOf;
import static org.mockito.Matchers.anyObject;
import static org.mockito.Matchers.anySetOf;
import static org.mockito.Matchers.anyString;
import static org.powermock.api.mockito.PowerMockito.mockStatic;
import static org.powermock.api.mockito.PowerMockito.when;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

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
import com.amazonaws.services.identitymanagement.model.AttachedPolicy;
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
public class IAMUserWithFullAdminPrevilegeRuleTest {

	@InjectMocks
	IAMUserWithFullAdminPrevilegeRule iamUserWithFullAdminPrevilegeRule;

	@Mock
	AmazonIdentityManagementClient amazonIdentityManagementClient;

	@Before
	public void setUp() throws Exception {
		amazonIdentityManagementClient = PowerMockito.mock(AmazonIdentityManagementClient.class);
	}

	@Test
	public void iamUserWithFullAdminAccess() throws Exception {

		mockStatic(PacmanUtils.class);
		mockStatic(IAMUtils.class);
		when(PacmanUtils.doesAllHaveValue(anyString(), anyString(), anyString(), anyString(), anyString())).thenReturn(true);

		Map<String, String> ruleParam = getInputParamMap();
		ruleParam.put(PacmanRuleConstants.RESOURCE_ID, "arn:aws:iam::123456789:user/test");
		
		Map<String, String> resourceAttribute = getResourceData("arn:aws:iam::123456789:user/test");
		resourceAttribute.put("username", "test");

		Map<String, Object> map = new HashMap<String, Object>();
		map.put("client", amazonIdentityManagementClient);
		IAMUserWithFullAdminPrevilegeRule spy = Mockito.spy(new IAMUserWithFullAdminPrevilegeRule());
		Mockito.doReturn(map).when((BaseRule) spy).getClientFor(anyObject(), anyString(), anyObject());
		
		when(PacmanUtils.getPolicyByGroup(anyListOf(String.class), anyString())).thenReturn(mockGroupPolicies());
		when(IAMUtils.getAttachedPolicyOfIAMUser(anyString(),anyObject())).thenReturn(mockAttachedUserPolicies());
		when(PacmanUtils.getIamCustManagedPolicyByName(anySetOf(String.class),anyString())).thenReturn(mockCustomerMgedPolicyArns());
		when(IAMUtils.isPolicyWithFullAdminAccess(anyString(), anyObject())).thenReturn(true);
		when(IAMUtils.isInlineUserPolicyWithFullAdminAccess(anyString(), anyObject())).thenReturn(true);
		when(IAMUtils.isInlineGroupPolicyWithFullAdminAccess(anyString(), anyObject())).thenReturn(true);
		
		RuleResult ruleResult = spy.execute(ruleParam, resourceAttribute);

		assertEquals(PacmanSdkConstants.STATUS_FAILURE, ruleResult.getStatus());
	}

	@Test
	public void iamUserWithoutFullAdminAccess() throws Exception {

		mockStatic(PacmanUtils.class);
		mockStatic(IAMUtils.class);
		when(PacmanUtils.doesAllHaveValue(anyString(), anyString(), anyString(), anyString(), anyString())).thenReturn(true);

		Map<String, String> ruleParam = getInputParamMap();
		ruleParam.put(PacmanRuleConstants.RESOURCE_ID, "arn:aws:iam::123456789:user/test");
		
		Map<String, String> resourceAttribute = getResourceData("arn:aws:iam::123456789:user/test");
		resourceAttribute.put("username", "test");

		Map<String, Object> map = new HashMap<String, Object>();
		map.put("client", amazonIdentityManagementClient);
		IAMUserWithFullAdminPrevilegeRule spy = Mockito.spy(new IAMUserWithFullAdminPrevilegeRule());
		Mockito.doReturn(map).when((BaseRule) spy).getClientFor(anyObject(), anyString(), anyObject());
		
		when(PacmanUtils.getPolicyByGroup(anyListOf(String.class), anyString())).thenReturn(mockGroupPolicies());
		when(IAMUtils.getAttachedPolicyOfIAMUser(anyString(),anyObject())).thenReturn(mockAttachedUserPolicies());
		when(PacmanUtils.getIamCustManagedPolicyByName(anySetOf(String.class),anyString())).thenReturn(mockCustomerMgedPolicyArns());
		when(IAMUtils.isPolicyWithFullAdminAccess(anyString(), anyObject())).thenReturn(false);
		when(IAMUtils.isInlineUserPolicyWithFullAdminAccess(anyString(), anyObject())).thenReturn(false);
		when(IAMUtils.isInlineGroupPolicyWithFullAdminAccess(anyString(), anyObject())).thenReturn(false);
		
		RuleResult ruleResult = spy.execute(ruleParam, resourceAttribute);

		assertEquals(PacmanSdkConstants.STATUS_SUCCESS, ruleResult.getStatus());
	}

	@Test
	public void mandatoryDataTest() throws Exception {

		mockStatic(PacmanUtils.class);
		mockStatic(IAMUtils.class);

		Map<String, String> ruleParam = getInputParamMap();
		ruleParam.put(PacmanRuleConstants.RESOURCE_ID, "arn:aws:iam::123456789:user/test");

		Map<String, String> resourceAttribute = getResourceData("arn:aws:iam::123456789:user/test");
		resourceAttribute.put("username", "test_admin_policy");

		when(PacmanUtils.doesAllHaveValue(anyString(), anyString(), anyString(), anyString(), anyString())).thenReturn(false);
		assertThatThrownBy(() -> iamUserWithFullAdminPrevilegeRule.execute(ruleParam, resourceAttribute)).isInstanceOf(InvalidInputException.class);

	}

	@Test
	public void exceptionTest() throws Exception {

		mockStatic(PacmanUtils.class);
		mockStatic(IAMUtils.class);

		when(PacmanUtils.doesAllHaveValue(anyString(), anyString(), anyString(), anyString(), anyString())).thenReturn(true);

		Map<String, String> ruleParam = getInputParamMap();
		ruleParam.put(PacmanRuleConstants.RESOURCE_ID, "arn:aws:iam::123456789:user/test");

		Map<String, String> resourceAttribute = getResourceData("arn:aws:iam::123456789:user/test");
		resourceAttribute.put("username", "test");

		Map<String, Object> map = new HashMap<String, Object>();
		map.put("client", amazonIdentityManagementClient);
		IAMUserWithFullAdminPrevilegeRule spy = Mockito.spy(new IAMUserWithFullAdminPrevilegeRule());

		Mockito.doReturn(map).when((BaseRule) spy).getClientFor(anyObject(), anyString(), anyObject());

		when(IAMUtils.getAttachedPolicyOfIAMUser(anyString(),anyObject())).thenReturn(mockAttachedUserPolicies());
		when(PacmanUtils.getIamCustManagedPolicyByName(anySetOf(String.class),anyString())).thenThrow(new RuleExecutionFailedExeption());
		when(IAMUtils.isPolicyWithFullAdminAccess(anyString(), anyObject())).thenReturn(false);
		assertThatThrownBy(() -> spy.execute(ruleParam, resourceAttribute)).isInstanceOf(RuleExecutionFailedExeption.class);

	}
	
	@Test
	public void iamFullAdminAccessInlinePolicyTest() throws Exception {

		mockStatic(PacmanUtils.class);
		mockStatic(IAMUtils.class);
		when(PacmanUtils.doesAllHaveValue(anyString(), anyString(), anyString(), anyString(), anyString())).thenReturn(true);

		Map<String, String> ruleParam = getInputParamMap();
		ruleParam.put(PacmanRuleConstants.RESOURCE_ID, "arn:aws:iam::123456789:user/test");
		
		Map<String, String> resourceAttribute = getResourceData("arn:aws:iam::123456789:user/test");
		resourceAttribute.put("username", "test");

		Map<String, Object> map = new HashMap<String, Object>();
		map.put("client", amazonIdentityManagementClient);
		IAMUserWithFullAdminPrevilegeRule spy = Mockito.spy(new IAMUserWithFullAdminPrevilegeRule());
		Mockito.doReturn(map).when((BaseRule) spy).getClientFor(anyObject(), anyString(), anyObject());
		
		when(IAMUtils.getAttachedPolicyOfIAMUser(anyString(),anyObject())).thenReturn(new ArrayList<>());
		when(IAMUtils.isInlineUserPolicyWithFullAdminAccess(anyString(), anyObject())).thenReturn(true);

		RuleResult ruleResult = spy.execute(ruleParam, resourceAttribute);

		assertEquals(PacmanSdkConstants.STATUS_FAILURE, ruleResult.getStatus());
	}
	
	@Test
	public void getHelpTextTest() {
		assertThat(iamUserWithFullAdminPrevilegeRule.getHelpText(), is(notNullValue()));
	}

	private Map<String, String> getInputParamMap() {
		Map<String, String> ruleParam = new HashMap<>();
		ruleParam.put(PacmanSdkConstants.EXECUTION_ID, "exectionid");
		ruleParam.put(PacmanSdkConstants.RULE_ID, "AWS_IAM_User_Full_Admin_Privilege_version-1_Admin_Previlege_iamuser");
		ruleParam.put(PacmanRuleConstants.CATEGORY, PacmanSdkConstants.SECURITY);
		ruleParam.put(PacmanRuleConstants.SEVERITY, PacmanSdkConstants.SEV_MEDIUM);
		ruleParam.put(PacmanRuleConstants.ACCOUNTID, "123456789");
		ruleParam.put(PacmanSdkConstants.Role_IDENTIFYING_STRING, "test/ro");
		ruleParam.put("esIamPoliciesUrl", "esIamPoliciesUrl");
		ruleParam.put("esIamGroupUrl", "esIamGroupUrl");
		return ruleParam;
	}

	private Map<String, String> getResourceData(String id) {
		Map<String, String> resObj = new HashMap<>();
		resObj.put("_resourceid", id);
		resObj.put("policyarn", id);
		resObj.put("groups", "dev:;admin");
		return resObj;
	}
	
	private Set<String> mockCustomerMgedPolicyArns() {
		Set<String> arns = new HashSet<>();
		arns.add("arn:aws:iam::123456789:policy/test_ful_admin_policy");
		return arns;
	}

	private List<AttachedPolicy> mockAttachedUserPolicies() {
		AttachedPolicy policy = new AttachedPolicy();
		policy.setPolicyArn("arn:aws:iam::123456789:policy/test_ful_admin_policy");
		policy.setPolicyName("test_ful_admin_policy");
		
		AttachedPolicy policy1 = new AttachedPolicy();
		policy1.setPolicyArn("arn:aws:iam::123456789:policy/test_policy");
		policy1.setPolicyName("test_policy");
		return Arrays.asList(policy,policy1);
	}
	
	private Set<String> mockGroupPolicies() {
		Set<String> policy = new HashSet<>();
		policy.addAll(Arrays.asList("TestPolicy1","TesPolicy2"));
		return policy;
	}

}
