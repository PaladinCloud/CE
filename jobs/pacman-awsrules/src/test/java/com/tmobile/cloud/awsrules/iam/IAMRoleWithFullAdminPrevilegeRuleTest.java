package com.tmobile.cloud.awsrules.iam;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;
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

import com.tmobile.pacman.commons.policy.Annotation;
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
import com.tmobile.pacman.commons.policy.BasePolicy;
import com.tmobile.pacman.commons.policy.PolicyResult;

@PowerMockIgnore({ "javax.net.ssl.*", "javax.management.*" })
@RunWith(PowerMockRunner.class)
@PrepareForTest({ PacmanUtils.class, BasePolicy.class, IAMUtils.class, Annotation.class })
public class IAMRoleWithFullAdminPrevilegeRuleTest {

	@InjectMocks
	IAMRoleWithFullAdminPrevilegeRule iamRoleWithFullAdminPrevilegeRule;

	@Mock
	AmazonIdentityManagementClient amazonIdentityManagementClient;

	@Before
	public void setUp() throws Exception {
		amazonIdentityManagementClient = PowerMockito.mock(AmazonIdentityManagementClient.class);
	}

	@Test
	public void iamRoleWithFullAdminAccess() throws Exception {

		mockStatic(PacmanUtils.class);
		mockStatic(IAMUtils.class);
		when(PacmanUtils.doesAllHaveValue(anyString(), anyString(), anyString(), anyString())).thenReturn(true);

		Map<String, String> ruleParam = getInputParamMap();
		ruleParam.put(PacmanRuleConstants.RESOURCE_ID, "arn:aws:iam::123456789:role/test_ful_admin_access");
		
		Map<String, String> resourceAttribute = getResourceData("arn:aws:iam::123456789:role/test_ful_admin_access");
		resourceAttribute.put("rolename", "test_ful_admin_access");

		Map<String, Object> map = new HashMap<String, Object>();
		map.put("client", amazonIdentityManagementClient);
		IAMRoleWithFullAdminPrevilegeRule spy = Mockito.spy(new IAMRoleWithFullAdminPrevilegeRule());
		Mockito.doReturn(map).when((BasePolicy) spy).getClientFor(anyObject(), anyString(), anyObject());
		
		when(IAMUtils.getAttachedPolicyOfIAMRole(anyString(),anyObject())).thenReturn(mockAttachedRolePolicies());
		when(PacmanUtils.getIamCustManagedPolicyByName(anySetOf(String.class),anyString())).thenReturn(mockCustomerMgedPolicyArns());
		when(IAMUtils.isPolicyWithFullAdminAccess(anyString(), anyObject())).thenReturn(true);
		mockStatic(Annotation.class);
		when(Annotation.buildAnnotation(anyObject(),anyObject())).thenReturn(getMockAnnotation());
		PolicyResult ruleResult = spy.execute(ruleParam, resourceAttribute);

		assertEquals(PacmanSdkConstants.STATUS_FAILURE, ruleResult.getStatus());
	}

	@Test
	public void iamRoleWithoutFullAdminAccess() throws Exception {

		mockStatic(PacmanUtils.class);
		mockStatic(IAMUtils.class);
		when(PacmanUtils.doesAllHaveValue(anyString(), anyString(), anyString(), anyString())).thenReturn(true);

		Map<String, String> ruleParam = getInputParamMap();
		ruleParam.put(PacmanRuleConstants.RESOURCE_ID, "arn:aws:iam::123456789:role/test_ful_admin_access");
		
		Map<String, String> resourceAttribute = getResourceData("arn:aws:iam::123456789:role/test_ful_admin_access");
		resourceAttribute.put("rolename", "test_ful_admin_access");

		Map<String, Object> map = new HashMap<String, Object>();
		map.put("client", amazonIdentityManagementClient);
		IAMRoleWithFullAdminPrevilegeRule spy = Mockito.spy(new IAMRoleWithFullAdminPrevilegeRule());
		Mockito.doReturn(map).when((BasePolicy) spy).getClientFor(anyObject(), anyString(), anyObject());
		
		when(IAMUtils.getAttachedPolicyOfIAMRole(anyString(),anyObject())).thenReturn(mockAttachedRolePolicies());
		when(PacmanUtils.getIamCustManagedPolicyByName(anySetOf(String.class),anyString())).thenReturn(mockCustomerMgedPolicyArns());
		when(IAMUtils.isPolicyWithFullAdminAccess(anyString(), anyObject())).thenReturn(false);
		
		PolicyResult ruleResult = spy.execute(ruleParam, resourceAttribute);

		assertEquals(PacmanSdkConstants.STATUS_SUCCESS, ruleResult.getStatus());
	}

	@Test
	public void mandatoryDataTest() throws Exception {

		mockStatic(PacmanUtils.class);
		mockStatic(IAMUtils.class);

		Map<String, String> ruleParam = getInputParamMap();
		ruleParam.put(PacmanRuleConstants.RESOURCE_ID, "arn:aws:iam::123456789:role/test_ful_admin_access");

		Map<String, String> resourceAttribute = getResourceData("arn:aws:iam::123456789:role/test_ful_admin_access");
		resourceAttribute.put("rolename", "test_admin_policy");

		when(PacmanUtils.doesAllHaveValue(anyString(), anyString(), anyString(), anyString())).thenReturn(false);
		assertThatThrownBy(() -> iamRoleWithFullAdminPrevilegeRule.execute(ruleParam, resourceAttribute)).isInstanceOf(InvalidInputException.class);

	}

	@Test
	public void exceptionTest() throws Exception {

		mockStatic(PacmanUtils.class);
		mockStatic(IAMUtils.class);

		when(PacmanUtils.doesAllHaveValue(anyString(), anyString(), anyString(), anyString())).thenReturn(true);

		Map<String, String> ruleParam = getInputParamMap();
		ruleParam.put(PacmanRuleConstants.RESOURCE_ID, "arn:aws:iam::123456789:role/test_ful_admin_access");

		Map<String, String> resourceAttribute = getResourceData("arn:aws:iam::123456789:role/test_ful_admin_access");
		resourceAttribute.put("rolename", "test_ful_admin_access");

		Map<String, Object> map = new HashMap<String, Object>();
		map.put("client", amazonIdentityManagementClient);
		IAMRoleWithFullAdminPrevilegeRule spy = Mockito.spy(new IAMRoleWithFullAdminPrevilegeRule());

		Mockito.doReturn(map).when((BasePolicy) spy).getClientFor(anyObject(), anyString(), anyObject());

		when(IAMUtils.getAttachedPolicyOfIAMRole(anyString(),anyObject())).thenReturn(mockAttachedRolePolicies());
		when(PacmanUtils.getIamCustManagedPolicyByName(anySetOf(String.class),anyString())).thenThrow(new RuleExecutionFailedExeption());
		when(IAMUtils.isPolicyWithFullAdminAccess(anyString(), anyObject())).thenReturn(false);
		assertThatThrownBy(() -> spy.execute(ruleParam, resourceAttribute)).isInstanceOf(RuleExecutionFailedExeption.class);

	}
	
	@Test
	public void iamFullAdminAccessInlinePolicyTest() throws Exception {

		mockStatic(PacmanUtils.class);
		mockStatic(IAMUtils.class);
		when(PacmanUtils.doesAllHaveValue(anyString(), anyString(), anyString(), anyString())).thenReturn(true);

		Map<String, String> ruleParam = getInputParamMap();
		ruleParam.put(PacmanRuleConstants.RESOURCE_ID, "arn:aws:iam::123456789:role/test_ful_admin_access");
		
		Map<String, String> resourceAttribute = getResourceData("arn:aws:iam::123456789:role/test_ful_admin_access");
		resourceAttribute.put("rolename", "test_ful_admin_access");

		Map<String, Object> map = new HashMap<String, Object>();
		map.put("client", amazonIdentityManagementClient);
		IAMRoleWithFullAdminPrevilegeRule spy = Mockito.spy(new IAMRoleWithFullAdminPrevilegeRule());
		Mockito.doReturn(map).when((BasePolicy) spy).getClientFor(anyObject(), anyString(), anyObject());
		
		when(IAMUtils.getAttachedPolicyOfIAMRole(anyString(),anyObject())).thenReturn(new ArrayList<>());
		when(IAMUtils.isInlineRolePolicyWithFullAdminAccess(anyString(), anyObject())).thenReturn(true);
		mockStatic(Annotation.class);
		when(Annotation.buildAnnotation(anyObject(),anyObject())).thenReturn(getMockAnnotation());
		PolicyResult ruleResult = spy.execute(ruleParam, resourceAttribute);

		assertEquals(PacmanSdkConstants.STATUS_FAILURE, ruleResult.getStatus());
	}
	
	@Test
	public void getHelpTextTest() {
		assertThat(iamRoleWithFullAdminPrevilegeRule.getHelpText(), is(notNullValue()));
	}

	private Map<String, String> getInputParamMap() {
		Map<String, String> ruleParam = new HashMap<>();
		ruleParam.put(PacmanSdkConstants.EXECUTION_ID, "exectionid");
		ruleParam.put(PacmanSdkConstants.POLICY_ID, "AWS_IAM_Role_Full_Admin_Privilege_version-1_Admin_Previlege_iamrole");
		ruleParam.put(PacmanRuleConstants.CATEGORY, PacmanSdkConstants.SECURITY);
		ruleParam.put(PacmanRuleConstants.SEVERITY, PacmanSdkConstants.SEV_MEDIUM);
		ruleParam.put(PacmanRuleConstants.ACCOUNTID, "123456789");
		ruleParam.put(PacmanSdkConstants.Role_IDENTIFYING_STRING, "test/ro");
		ruleParam.put("esIamPoliciesUrl", "esIamPoliciesUrl");
		return ruleParam;
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
	private Map<String, String> getResourceData(String id) {
		Map<String, String> resObj = new HashMap<>();
		resObj.put("_resourceid", id);
		resObj.put("policyarn", id);
		return resObj;
	}
	
	private Set<String> mockCustomerMgedPolicyArns() {
		Set<String> arns = new HashSet<>();
		arns.add("arn:aws:iam::123456789:policy/test_ful_admin_policy");
		return arns;
	}

	private List<AttachedPolicy> mockAttachedRolePolicies() {
		AttachedPolicy policy = new AttachedPolicy();
		policy.setPolicyArn("arn:aws:iam::123456789:policy/test_ful_admin_policy");
		policy.setPolicyName("test_ful_admin_policy");
		
		AttachedPolicy policy1 = new AttachedPolicy();
		policy1.setPolicyArn("arn:aws:iam::123456789:policy/test_policy");
		policy1.setPolicyName("test_policy");
		return Arrays.asList(policy,policy1);
	}

}
