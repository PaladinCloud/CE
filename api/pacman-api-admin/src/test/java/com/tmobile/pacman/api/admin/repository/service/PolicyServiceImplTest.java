
package com.tmobile.pacman.api.admin.repository.service;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.when;
import static org.powermock.api.mockito.PowerMockito.mock;
import static org.powermock.api.mockito.PowerMockito.mockStatic;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.apache.commons.lang.StringUtils;
import org.apache.http.util.EntityUtils;
import org.elasticsearch.client.Response;
import org.elasticsearch.client.RestClient;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;

import com.amazonaws.services.cloudwatchevents.AmazonCloudWatchEvents;
import com.amazonaws.services.cloudwatchevents.model.PutRuleRequest;
import com.amazonaws.services.cloudwatchevents.model.PutRuleResult;
import com.amazonaws.services.cloudwatchevents.model.PutTargetsRequest;
import com.amazonaws.services.cloudwatchevents.model.PutTargetsResult;
import com.amazonaws.services.cloudwatchevents.model.RuleState;
import com.amazonaws.services.lambda.AWSLambdaClient;
import com.amazonaws.services.lambda.model.GetPolicyRequest;
import com.amazonaws.services.lambda.model.GetPolicyResult;
import com.amazonaws.services.lambda.model.InvokeRequest;
import com.amazonaws.services.lambda.model.InvokeResult;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.tmobile.pacman.api.admin.common.AdminConstants;
import com.tmobile.pacman.api.admin.config.PacmanConfiguration;
import com.tmobile.pacman.api.admin.domain.AWSProperty;
import com.tmobile.pacman.api.admin.domain.CreateUpdatePolicyDetails;
import com.tmobile.pacman.api.admin.domain.LambdaProperty;
import com.tmobile.pacman.api.admin.domain.PolicyProjection;
import com.tmobile.pacman.api.admin.domain.RuleProperty;
import com.tmobile.pacman.api.admin.domain.S3Property;
import com.tmobile.pacman.api.admin.exceptions.PacManException;
import com.tmobile.pacman.api.admin.repository.PolicyCategoryRepository;
import com.tmobile.pacman.api.admin.repository.PolicyRepository;
import com.tmobile.pacman.api.admin.repository.model.Policy;
import com.tmobile.pacman.api.admin.service.AmazonClientBuilderService;
import com.tmobile.pacman.api.admin.util.AdminUtils;
import com.tmobile.pacman.api.commons.utils.PacHttpUtils;

@RunWith(PowerMockRunner.class)
@PrepareForTest({ AdminUtils.class, ByteBuffer.class, RuleState.class, PacHttpUtils.class, EntityUtils.class, Response.class, RestClient.class })
public class PolicyServiceImplTest {

	@InjectMocks
	private PolicyServiceImpl policyService;

	@Mock
	private PacmanConfiguration config;
	
	@Mock
	private AmazonClientBuilderService amazonClient;
	
	@Mock
	private ObjectMapper mapper;

	@Mock
	private PolicyRepository policyRepository;
	
	@Mock
	private PutRuleResult putRuleResult;
	
	private AWSLambdaClient awsLambdaClient;
	
	private AmazonCloudWatchEvents amazonCloudWatchEvents;
	
	@Mock
	private InvokeRequest invokeRequest;
	
	@Mock
	private PutTargetsRequest putTargetsRequest;
	
	@Mock
	private PutRuleRequest putRuleRequest;
	
	@Mock
	private PutTargetsResult putTargetsResult;
	
	@Mock
	private GetPolicyResult getPolicyResult;
	
	@Mock
	private InvokeResult invokeResult;
	
	@Mock
	private PolicyCategoryRepository policyCategoryRepository;

	@Before
    public void setUp() throws Exception{
        awsLambdaClient = mock(AWSLambdaClient.class);
        amazonCloudWatchEvents = mock(AmazonCloudWatchEvents.class);
       // invokeRequest = mock(InvokeRequest.class);
        invokeResult = mock(InvokeResult.class);
        PowerMockito.whenNew(ObjectMapper.class).withNoArguments().thenReturn(mapper);
        invokeRequest = Mockito.spy(new InvokeRequest());
        getPolicyResult = Mockito.spy(new GetPolicyResult());
        putRuleRequest = Mockito.spy(new PutRuleRequest());
        putRuleResult = Mockito.spy(new PutRuleResult());
        putTargetsResult = Mockito.spy(new PutTargetsResult());
        putTargetsRequest = Mockito.spy(new PutTargetsRequest());
        AWSProperty awsCredentialsProperty = buildAWSCredentials();
		when(config.getAws()).thenReturn(awsCredentialsProperty);
        PowerMockito.whenNew(AWSLambdaClient.class).withAnyArguments().thenReturn(awsLambdaClient);  
        when(amazonClient.getAWSLambdaClient(anyString())).thenReturn(awsLambdaClient);
		when(amazonClient.getAmazonCloudWatchEvents(anyString())).thenReturn(amazonCloudWatchEvents);
    }
	 
	@Test
	public void getAllPoliciesByTargetTypeTest() {
		List<Policy> policyDetails = Lists.newArrayList();
		Optional<Policy> policy = getPolicyDetailsResponse();
		policyDetails.add(policy.get());
		when(policyRepository.findByTargetTypeIgnoreCase(anyString())).thenReturn(policyDetails);
		assertThat(policyService.getAllPoliciesByTargetType(anyString()).size(), is(1));
	}

	@Test
	public void getAllPoliciesByTargetTypeNameTest() {
		List<PolicyProjection> policyDetails = Lists.newArrayList();
		policyDetails.add(getPolicyProjection());
		when(policyRepository.findByTargetType(anyString())).thenReturn(policyDetails);
		assertThat(policyService.getAllPoliciesByTargetTypeName(anyString()).size(), is(1));
	}

	@Test
	public void getAllPoliciesByTargetTypeAndNotInPolicyIdListTest() {
		List<PolicyProjection> policyDetails = Lists.newArrayList();
		policyDetails.add(getPolicyProjection());
		when(policyRepository.findByTargetTypeAndPolicyIdNotIn(anyString(), any())).thenReturn(policyDetails);
		assertThat(policyService.getAllPoliciesByTargetTypeAndNotInPolicyIdList(anyString(), any()).size(), is(1));
	}

	@Test
	public void getAllPoliciesByTargetTypeAndPolicyIdListTest() {
		List<PolicyProjection> policyDetails = Lists.newArrayList();
		policyDetails.add(getPolicyProjection());
		when(policyRepository.findByTargetTypeAndPolicyIdIn(anyString(), any())).thenReturn(policyDetails);
		assertThat(policyService.getAllPoliciesByTargetTypeAndPolicyIdList(anyString(), any()).size(), is(1));
	}

	@Test
	public void getByIdTest() {
		Optional<Policy> policyDetails = getPolicyDetailsResponse();
		when(policyRepository.findByPolicyId(anyString())).thenReturn(policyDetails.get());
		assertThat(policyService.getByPolicyId(anyString()).getPolicyId(), is("policyId123"));
	}

	@Test
	public void getPoliciesTest() {
		List<Policy> policyDetails = Lists.newArrayList();
		Optional<Policy> policy = getPolicyDetailsResponse();
		policyDetails.add(policy.get());
		Page<Policy> allPoliciesDetails = new PageImpl<Policy>(policyDetails, new PageRequest(0, 1), policyDetails.size());
		when(policyService.getPolicies(StringUtils.EMPTY, 0, 1)).thenReturn(allPoliciesDetails);
		assertThat(policyRepository.findAll(StringUtils.EMPTY, new PageRequest(0, 1)).getContent().size(), is(1));
	}

	@Test
	public void getAllAlexaKeywordsTest() {
		Collection<String> alexaKeywords = Lists.newArrayList();
		alexaKeywords.add("Alexa1");
		alexaKeywords.add("Alexa2");
		when(policyService.getAllAlexaKeywords()).thenReturn(alexaKeywords);
		assertThat(policyRepository.getAllAlexaKeywords().size(), is(2));
	}
	
	@Test
	public void invokePolicyTest() throws Exception {
		Optional<Policy> policy = getPolicyDetailsResponse();
		when(policyRepository.findById(anyString())).thenReturn(policy);
		List<Map<String, Object>> additionalPolicyParams = Lists.newArrayList();
		String referenceId = "S4FA";
        mockStatic(AdminUtils.class);
        mockStatic(ByteBuffer.class);
        when(AdminUtils.getReferenceId()).thenReturn(referenceId);
        Map<String, Object> policyParamDetails = Maps.newHashMap();
        when(mapper.readValue(anyString(), any(TypeReference.class))).thenReturn(policyParamDetails);
        when(mapper.writeValueAsString(any())).thenReturn("[]");
        RuleProperty ruleProperty = buildRuleProperty();
        when(config.getRule()).thenReturn(ruleProperty);
        ByteBuffer params = ByteBuffer.wrap(policy.get().getPolicyParams().getBytes());
        when(ByteBuffer.wrap(any())).thenReturn(params);   
        when(awsLambdaClient.invoke(any())).thenReturn(invokeResult);
        when(invokeResult.getStatusCode()).thenReturn(200);
        when(invokeRequest.withFunctionName(anyString()).withPayload(any(ByteBuffer.class))).thenReturn(invokeRequest);
        assertThat(policyService.invokePolicy("ruleId123", additionalPolicyParams), is("S4FA"));
	}

	@Test
	public void invokePolicyCheckInvokeStatusFalseTest() throws Exception {
		Optional<Policy> policy = getPolicyDetailsResponse();
		when(policyRepository.findById(anyString())).thenReturn(policy);
		List<Map<String, Object>> additionalPolicyParams = Lists.newArrayList();
		String referenceId = null;
        mockStatic(AdminUtils.class);
        mockStatic(ByteBuffer.class);
        when(AdminUtils.getReferenceId()).thenReturn(referenceId);
      
        RuleProperty policyProperty = buildRuleProperty();
        when(config.getRule()).thenReturn(policyProperty);
        ByteBuffer params = ByteBuffer.wrap(policy.get().getPolicyParams().getBytes());
        when(ByteBuffer.wrap(any())).thenReturn(params);   
        when(awsLambdaClient.invoke(any())).thenReturn(invokeResult);
        when(invokeResult.getStatusCode()).thenReturn(500);
        when(invokeRequest.withFunctionName(anyString()).withPayload(any(ByteBuffer.class))).thenReturn(invokeRequest);
        assertNull(policyService.invokePolicy("policyId123", additionalPolicyParams));
	}
	
	@SuppressWarnings("unchecked")
	@Test
	public void enableDisablePolicyNotFoundExceptionTest() throws Exception {
		String action = "disable";
		mockStatic(RuleState.class);
		when(policyRepository.findById(anyString())).thenThrow(PacManException.class);
	    assertThatThrownBy(() -> policyService.enableDisablePolicy("policyId123", action, "userId123")).isInstanceOf(PacManException.class);
	}
	
	@SuppressWarnings("unchecked")
	@Test
	public void enableDisablePolicyCreatePolicyForLambdaExceptionTest() throws Exception {
		String action = "enable";
		Optional<Policy> policy = getPolicyDetailsResponse();
		mockStatic(RuleState.class);
		when(policyRepository.findById(anyString())).thenReturn(policy);
		when(putRuleRequest.withName(anyString()).withDescription(anyString()).withState(anyString())).thenReturn(putRuleRequest);
		when(amazonClient.getAWSLambdaClient(anyString())).thenThrow(Exception.class);
	    RuleProperty policyProperty = buildRuleProperty();
        when(config.getRule()).thenReturn(policyProperty);
	    when(policyRepository.save(policy.get())).thenReturn(policy.get()); 
	    assertThatThrownBy(() -> policyService.enableDisablePolicy("policyId123", action, "userId123")).isInstanceOf(PacManException.class);
	}
	
	@SuppressWarnings("unchecked")
	@Test
	public void enableDisablePolicyResourceNotFoundExceptionTest() throws Exception {
		String action = "enable";
		String lambdaFunctionName = "lambdaFunctionName123";
		GetPolicyRequest getPolicyRequest = new GetPolicyRequest();
		getPolicyRequest.setFunctionName(lambdaFunctionName);
		PowerMockito.whenNew(GetPolicyRequest.class).withNoArguments().thenThrow(Exception.class);
		Optional<Policy> policy = getPolicyDetailsResponse();
		mockStatic(RuleState.class);
		when(policyRepository.findById(anyString())).thenReturn(policy);
		when(putRuleRequest.withName(anyString()).withDescription(anyString()).withState(anyString())).thenReturn(putRuleRequest);
		when(amazonClient.getAWSLambdaClient(anyString())).thenThrow(Exception.class);
	    RuleProperty policyProperty = buildRuleProperty();
        when(config.getRule()).thenReturn(policyProperty);
	    when(policyRepository.save(policy.get())).thenReturn(policy.get());
	    assertThatThrownBy(() -> policyService.enableDisablePolicy("policyId123", action, "userId123")).isInstanceOf(PacManException.class);
	}

	
	
	@Test
	public void createPolicyTest() throws Exception {
		CreateUpdatePolicyDetails createRuleDetails = getCreateUpdatePolicyDetailsRequest();
		MultipartFile firstFile = getMockMultipartFile();
		Map<String, Object> policyParamDetails = Maps.newHashMap();
		List<Map<String, Object>> params = Lists.newArrayList();
		Map<String, Object> param = Maps.newHashMap();
		param.put("name", "name123");
		param.put("value", "name123");
		param.put("encrypt", true);
		params.add(param);
		param = Maps.newHashMap();
		param.put("name", "name123");
		param.put("value", "name123");
		param.put("encrypt", false);
		param.put("isValueNew", true);
		params.add(param);
		policyParamDetails.put("environmentVariables", params);
		policyParamDetails.put("params", params);
		
		when(mapper.readValue(anyString(), any(TypeReference.class))).thenReturn(policyParamDetails);
	    when(mapper.writeValueAsString(any())).thenReturn("[]");
		Optional<Policy> policy = getPolicyDetailsResponse();
		RuleProperty policyProperty = buildRuleProperty();
        when(config.getRule()).thenReturn(policyProperty);
	    when(policyRepository.save(policy.get())).thenReturn(policy.get());
	    mockStatic(ByteBuffer.class);
	    mockStatic(RuleState.class);
	    when(amazonCloudWatchEvents.putRule(any())).thenReturn(putRuleResult);
	    String policyArn = "policyArn123";
		when(putRuleResult.getRuleArn()).thenReturn(policyArn);
		when(amazonCloudWatchEvents.putTargets(any())).thenReturn(putTargetsResult);
        ByteBuffer byteBuffer = ByteBuffer.wrap(createRuleDetails.getPolicyParams().getBytes());
        when(ByteBuffer.wrap(any())).thenReturn(byteBuffer);   
        when(awsLambdaClient.invoke(any())).thenReturn(invokeResult);
        when(awsLambdaClient.getPolicy(any())).thenReturn(getPolicyResult);
        when(invokeResult.getStatusCode()).thenReturn(200);
        when(invokeRequest.withFunctionName(anyString()).withPayload(any(ByteBuffer.class))).thenReturn(invokeRequest);
        int count = 0;
        when(putTargetsResult.getFailedEntryCount()).thenReturn(count);
		assertThat(policyService.createPolicy(firstFile, createRuleDetails, "userId123"), is(String.format(AdminConstants.POLICY_CREATION_SUCCESS)));
	}
	
	@Test
	public void updatePolicyTest() throws Exception {
		
		Optional<Policy> policy = getPolicyDetailsResponse();
		when(policyRepository.findById(anyString())).thenReturn(policy);
		
		MultipartFile firstFile = getMockMultipartFile();
		Map<String, Object> policyParamDetails = Maps.newHashMap();
		List<Map<String, Object>> params = Lists.newArrayList();
		Map<String, Object> param = Maps.newHashMap();
		param.put("name", "name123");
		param.put("value", "name123");
		param.put("encrypt", true);
		params.add(param);
		param = Maps.newHashMap();
		param.put("name", "name123");
		param.put("value", "name123");
		param.put("encrypt", false);
		param.put("isValueNew", true);
		params.add(param);
		policyParamDetails.put("environmentVariables", params);
		policyParamDetails.put("params", params);
		RuleProperty ruleProperty = buildRuleProperty();
        when(config.getRule()).thenReturn(ruleProperty);
        when(policyRepository.findByPolicyId(anyString())).thenReturn(policy.get());
	    when(policyRepository.save(policy.get())).thenReturn(policy.get());
        when(mapper.readValue(anyString(), any(TypeReference.class))).thenReturn(policyParamDetails);
        when(mapper.writeValueAsString(any())).thenReturn("[]");
		assertThat(policyService.updatePolicy(firstFile, getCreateUpdatePolicyDetailsRequest(), "userId123"), is(String.format(AdminConstants.POLICY_CREATION_SUCCESS)));
	}
	
	@Test
	public void getAllPolicyCategoriesTest() throws PacManException {
		
		when(policyCategoryRepository.findAll()).thenReturn(new ArrayList<>());
		assertThat(policyService.getAllPolicyCategories(), is(notNullValue()));
	}
	
	
	private RuleProperty buildRuleProperty() {
		RuleProperty ruleProperty = new RuleProperty();
		S3Property s3Property = new S3Property();
		s3Property.setBucketName("job-execution-manager-executables");
		LambdaProperty lambdaProperty = new LambdaProperty();
		lambdaProperty.setActionDisabled("actionDisabled123");
		lambdaProperty.setActionEnabled("actionEnabled123");
		lambdaProperty.setFunctionArn("functionArn123");
		lambdaProperty.setFunctionName("functionName123");
		lambdaProperty.setPrincipal("principal123");
		lambdaProperty.setTargetId("targetId123");
		ruleProperty.setLambda(lambdaProperty);
		ruleProperty.setS3(s3Property);
		return ruleProperty;
	}
	
	private MultipartFile getMockMultipartFile() {
		return new MockMultipartFile("data", "policy.jar", "multipart/form-data", "policy content".getBytes());
	}

	private CreateUpdatePolicyDetails getCreateUpdatePolicyDetailsRequest() {
		CreateUpdatePolicyDetails policyDetails = new CreateUpdatePolicyDetails();
		policyDetails.setPolicyId("policyId123");
		policyDetails.setPolicyName("policyName123");
		policyDetails.setTargetType("targetType123");
		policyDetails.setAssetGroup("assetGroup123");
		policyDetails.setAlexaKeyword("alexaKeyword123");
		policyDetails.setPolicyParams("policyParams123");
		policyDetails.setPolicyFrequency("policyFrequency123");
		policyDetails.setPolicyExecutable("policyExecutable123");
		policyDetails.setPolicyRestUrl("policyRestUrl123");
		policyDetails.setPolicyType("policyType123");
		policyDetails.setStatus("status123");
		policyDetails.setPolicyDisplayName("displayName123");
		policyDetails.setDataSource("dataSource123");
		policyDetails.setAutofixEnabled("false");
		policyDetails.setIsFileChanged(false);
		return policyDetails;
	}

	private PolicyProjection getPolicyProjection() {
		return new PolicyProjection() {
			@Override
			public String getType() {
				return "Type123";
			}

			@Override
			public String getText() {
				return "Text123";
			}

			@Override
			public String getStatus() {
				return "Status123";
			}

			@Override
			public String getId() {
				return "PolicyId123";
			}

		};
	}

	private Optional<Policy> getPolicyDetailsResponse() {
		Policy policy = new Policy();
		policy.setPolicyUUID("UUID123");
		policy.setPolicyId("policyId123");
		policy.setPolicyName("policyName123");
		policy.setTargetType("targetType123");
		policy.setAssetGroup("assetGroup123");
		policy.setAlexaKeyword("alexaKeyword123");
		policy.setPolicyParams("{\"assetGroup\":\"aws\",\"environmentVariables\":[{\"encrypt\":false,\"value\":\"123\",\"key\":\"abc\"}],\"policyUUID\":\"22ce851c-7b6c-4986-9ba9-db97803b363a\",\"policyType\":\"Managepolicy\",\"pac_ds\":\"aws\",\"targetType\":\"cloudtrl\",\"params\":[{\"encrypt\":\"false\",\"value\":\"role/pac_ro\",\"key\":\"roleIdentifyingString\"},{\"encrypt\":\"false\",\"value\":\"check-for-aws-cloudtrail-config\",\"key\":\"policyKey\"},{\"encrypt\":false,\"value\":\"critical\",\"key\":\"severity\"},{\"encrypt\":false,\"value\":\"security\",\"key\":\"policyCategory\"}],\"policyId\":\"PacMan_AWSCloudTrailConfig_version-1_AWSCloudTrailConfig_cloudtrl\",\"autofix\":false,\"alexaKeyword\":\"AWSCloudTrailConfig\",\"policyRestUrl\":\"\"}");
		policy.setPolicyFrequency("policyFrequency123");
		policy.setPolicyExecutable("policyExecutable123");
		policy.setPolicyRestUrl("policyRestUrl123");
		policy.setPolicyType("policyType123");
		policy.setPolicyArn("policyArn123");
		policy.setStatus("status123");
		policy.setUserId("userId123");
		policy.setPolicyDesc("policy descritpion");
		policy.setResolution("resolution");
		policy.setResolutionUrl("resolution url");
		policy.setPolicyDisplayName("displayName123");
		policy.setCreatedDate(new Date());
		policy.setModifiedDate(new Date());
		return Optional.of(policy);
	}
	
	private AWSProperty buildAWSCredentials() {
		AWSProperty awsCredentials = new AWSProperty();
		awsCredentials.setAccessKey("accessKey");
		awsCredentials.setSecretKey("secretKey");
		return awsCredentials;
	}
}
