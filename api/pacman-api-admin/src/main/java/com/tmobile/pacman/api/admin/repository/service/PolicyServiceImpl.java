package com.tmobile.pacman.api.admin.repository.service;

import static com.tmobile.pacman.api.admin.common.AdminConstants.CLOUDWATCH_RULE_DISABLE_FAILURE;
import static com.tmobile.pacman.api.admin.common.AdminConstants.CLOUDWATCH_RULE_ENABLE_FAILURE;
import static com.tmobile.pacman.api.admin.common.AdminConstants.UNEXPECTED_ERROR_OCCURRED;

import java.nio.ByteBuffer;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.amazonaws.services.cloudwatchevents.model.DisableRuleRequest;
import com.amazonaws.services.cloudwatchevents.model.DisableRuleResult;
import com.amazonaws.services.cloudwatchevents.model.EnableRuleRequest;
import com.amazonaws.services.cloudwatchevents.model.EnableRuleResult;
import com.amazonaws.services.cloudwatchevents.model.PutEventsRequest;
import com.amazonaws.services.cloudwatchevents.model.PutRuleRequest;
import com.amazonaws.services.cloudwatchevents.model.PutRuleResult;
import com.amazonaws.services.cloudwatchevents.model.PutTargetsRequest;
import com.amazonaws.services.cloudwatchevents.model.PutTargetsResult;
import com.amazonaws.services.cloudwatchevents.model.RuleState;
import com.amazonaws.services.cloudwatchevents.model.Target;
import com.amazonaws.services.eventbridge.model.DescribeRuleRequest;
import com.amazonaws.services.eventbridge.model.DescribeRuleResult;
import com.amazonaws.services.lambda.AWSLambda;
import com.amazonaws.services.lambda.model.AddPermissionRequest;
import com.amazonaws.services.lambda.model.GetPolicyRequest;
import com.amazonaws.services.lambda.model.InvokeRequest;
import com.amazonaws.services.lambda.model.InvokeResult;
import com.amazonaws.services.lambda.model.ResourceNotFoundException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.tmobile.pacman.api.admin.common.AdminConstants;
import com.tmobile.pacman.api.admin.config.PacmanConfiguration;
import com.tmobile.pacman.api.admin.domain.CreateUpdatePolicyDetails;
import com.tmobile.pacman.api.admin.domain.PolicyProjection;
import com.tmobile.pacman.api.admin.exceptions.PacManException;
import com.tmobile.pacman.api.admin.repository.PolicyCategoryRepository;
import com.tmobile.pacman.api.admin.repository.PolicyRepository;
import com.tmobile.pacman.api.admin.repository.model.Policy;
import com.tmobile.pacman.api.admin.repository.model.PolicyCategory;
import com.tmobile.pacman.api.admin.service.AmazonClientBuilderService;
import com.tmobile.pacman.api.admin.service.AwsS3BucketService;
import com.tmobile.pacman.api.admin.util.AdminUtils;

/**
 * Rule Service Implementations
 */
@Service
public class PolicyServiceImpl implements PolicyService {

	private static final Logger log = LoggerFactory.getLogger(PolicyServiceImpl.class);

	@Autowired
	private PacmanConfiguration config;

	@Autowired
	private AmazonClientBuilderService amazonClient;

	@Autowired
	private AwsS3BucketService awsS3BucketService;

	@Autowired
	private PolicyRepository policyRepository;

	@Autowired
	private ObjectMapper mapper;
	
	@Value("${application.prefix}")
	private String applicationPrefix;

	@Autowired
	private PolicyCategoryRepository policyCategoryRepository;

	@Override
	public List<Policy> getAllPoliciesByTargetType(String targetType) {
		return policyRepository.findByTargetTypeIgnoreCase(targetType);
	}

	@Override
	public List<PolicyProjection> getAllPoliciesByTargetTypeName(String targetType) {
		return policyRepository.findByTargetType(targetType);
	}

	@Override
	public List<PolicyProjection> getAllPoliciesByTargetTypeAndNotInPolicyIdList(final String targetType,
			final List<String> policyIdList) {
		return policyRepository.findByTargetTypeAndPolicyIdNotIn(targetType, policyIdList);
	}

	@Override
	public List<PolicyProjection> getAllPoliciesByTargetTypeAndPolicyIdList(final String targetType,
			final List<String> policyIdList) {
		return policyRepository.findByTargetTypeAndPolicyIdIn(targetType, policyIdList);
	}

	@Override
	public Policy getByPolicyId(String policyId) {
		return policyRepository.findByPolicyId(policyId);
	}

	@Override
	public Page<Policy> getPolicies(final String searchTerm, final int page, final int size) {
		return policyRepository.findAll(searchTerm.toLowerCase(), PageRequest.of(page, size));
	}

	@Override
	public Collection<String> getAllAlexaKeywords() {
		return policyRepository.getAllAlexaKeywords();
	}

	@Override
	public Collection<String> getAllPolicyIds() {
		return policyRepository.getAllPolicyIds();
	}

	@Override
	public String createPolicy(final MultipartFile fileToUpload, final CreateUpdatePolicyDetails policyDetails,
			final String userId) throws PacManException {
		checkPolicyTypeNotServerlessOrManaged(policyDetails, fileToUpload);
		return addPolicyInstance(fileToUpload, policyDetails, userId);
	}

	@Override
	public String updatePolicy(MultipartFile fileToUpload, CreateUpdatePolicyDetails updatePolicyDetails, String userId)
			throws PacManException {
		checkPolicyTypeNotServerlessOrManaged(updatePolicyDetails, fileToUpload);
		return updatePolicyInstance(fileToUpload, updatePolicyDetails, userId);
	}

	@Override
	public String invokePolicy(String policyId, List<Map<String, Object>> policyOptionalParams) {
		Policy policyDetails = policyRepository.findById(policyId).get();
		AWSLambda awsLambdaClient = amazonClient.getAWSLambdaClient(config.getRule().getLambda().getRegion());
		String invocationId = AdminUtils.getReferenceId();
		boolean invokeStatus = invokePolicy(awsLambdaClient, policyDetails, invocationId, policyOptionalParams);
		if (invokeStatus) {
			return invocationId;
		} else {
			return null;
		}
	}

	@Override
	public String enableDisablePolicy(final String policyId, final String action, final String userId)
			throws PacManException {
		if (policyRepository.existsById(policyId)) {
			Policy existingPolicy = policyRepository.findById(policyId).get();
			if (action.equalsIgnoreCase("enable")) {
				return enableCloudWatchRule(existingPolicy, userId, RuleState.ENABLED);
			} else {
				return disableCloudWatchRule(existingPolicy, userId, RuleState.DISABLED);
			}
		} else {
			throw new PacManException(String.format(AdminConstants.POLICY_ID_NOT_EXITS, policyId));
		}
	}

	private String getEventBus(String assetGroup) {
		String eventBus = "default";
		switch (assetGroup.toLowerCase()) {
		case "azure":
			String azureBusDetails = config.getAzure().getEventbridge().getBus().getDetails();
			eventBus = azureBusDetails.split(":")[0];
			break;
		case "gcp":
			String gcpBusDetails = config.getGcp().getEventbridge().getBus().getDetails();
			eventBus = gcpBusDetails.split(":")[0];
			break;
		case "aws":
			String awsBusDetails = config.getAws().getEventbridge().getBus().getDetails();
			eventBus = awsBusDetails.split(":")[0];
			break;
		default:
			eventBus = "default";
		}
		log.info("Event bridge bus : {} ", eventBus);
		return eventBus;
	}

	private String disableCloudWatchRule(Policy existingPolicy, String userId, RuleState ruleState)
			throws PacManException {
		String eventBusName = getEventBus(existingPolicy.getAssetGroup());
		DisableRuleRequest disableRuleRequest = new DisableRuleRequest().withName(existingPolicy.getPolicyUUID());
		disableRuleRequest.setEventBusName(eventBusName);
		DisableRuleResult disableRuleResult = amazonClient
				.getAmazonCloudWatchEvents(config.getRule().getLambda().getRegion()).disableRule(disableRuleRequest);
		if (disableRuleResult.getSdkHttpMetadata() != null) {
			if (disableRuleResult.getSdkHttpMetadata().getHttpStatusCode() == 200) {
				existingPolicy.setUserId(userId);
				existingPolicy.setModifiedDate(new Date());
				existingPolicy.setStatus(ruleState.name());
				policyRepository.save(existingPolicy);
				return String.format(AdminConstants.POLICY_DISABLE_ENABLE_SUCCESS, ruleState.name().toLowerCase());
			} else {
				throw new PacManException(CLOUDWATCH_RULE_DISABLE_FAILURE);
			}
		} else {
			throw new PacManException(CLOUDWATCH_RULE_DISABLE_FAILURE);
		}
	}

	private String enableCloudWatchRule(Policy existingPolicy, String userId, RuleState ruleState)
			throws PacManException {
		AWSLambda awsLambdaClient = amazonClient.getAWSLambdaClient(config.getRule().getLambda().getRegion());
		if (!checkIfPolicyAvailableForLambda(config.getRule().getLambda().getFunctionName(), awsLambdaClient)) {
			createPolicyForLambda(config.getRule().getLambda().getFunctionName(), awsLambdaClient);
		}
		String eventBusName = getEventBus(existingPolicy.getAssetGroup());

		EnableRuleRequest enableRuleRequest = new EnableRuleRequest().withName(existingPolicy.getPolicyUUID());
		enableRuleRequest.setEventBusName(eventBusName);
		EnableRuleResult enableRuleResult = amazonClient
				.getAmazonCloudWatchEvents(config.getRule().getLambda().getRegion()).enableRule(enableRuleRequest);
		if (enableRuleResult.getSdkHttpMetadata() != null) {
			if (enableRuleResult.getSdkHttpMetadata().getHttpStatusCode() == 200) {
				existingPolicy.setUserId(userId);
				existingPolicy.setModifiedDate(new Date());
				existingPolicy.setStatus(ruleState.name());
				policyRepository.save(existingPolicy);
				invokePolicy(awsLambdaClient, existingPolicy, null, null);
				return String.format(AdminConstants.POLICY_DISABLE_ENABLE_SUCCESS, ruleState.name().toLowerCase());
			} else {
				throw new PacManException(CLOUDWATCH_RULE_ENABLE_FAILURE);
			}
		} else {
			throw new PacManException(CLOUDWATCH_RULE_ENABLE_FAILURE);
		}
	}

	private void checkPolicyTypeNotServerlessOrManaged(CreateUpdatePolicyDetails policyDetails,
			MultipartFile fileToUpload) throws PacManException {
		if (isPolicyTypeNotServerlessOrManaged(policyDetails.getPolicyType()) && policyDetails.getIsFileChanged()) {
			if (fileToUpload.isEmpty()) {
				throw new PacManException(AdminConstants.JAR_FILE_MISSING);
			}
		}
	}

	private String updatePolicyInstance(final MultipartFile fileToUpload, CreateUpdatePolicyDetails policyDetails,
			String userId) throws PacManException {
		if (policyDetails != null) {
			if (isPolicyIdExits(policyDetails.getPolicyId())) {
				Date currentDate = new Date();
				Policy updatePolicyDetails = policyRepository.findById(policyDetails.getPolicyId()).get();

				String policyParams = buildAndGetPolicyParams(policyDetails, updatePolicyDetails.getPolicyUUID(),
						false);
				if (AdminConstants.MANAGED_POLICY_TYPE.equalsIgnoreCase(policyDetails.getPolicyType())) {
					updatePolicyDetails.setPolicyParams(policyParams);
					updatePolicyDetails.setSeverity(policyDetails.getSeverity());
					updatePolicyDetails.setCategory(policyDetails.getCategory());
					updatePolicyDetails.setAutoFixEnabled(policyDetails.getIsAutofixEnabled());
					updateCustomEventBridgeRule(updatePolicyDetails);
				} else {
					policyDetails.setTargetType(updatePolicyDetails.getTargetType());
					policyDetails.setDataSource(retrieveDataSource(updatePolicyDetails));
					updatePolicyDetails.setPolicyParams(policyParams);
					updatePolicyDetails.setPolicyFrequency(policyDetails.getPolicyFrequency());
					updatePolicyDetails.setPolicyExecutable(policyDetails.getPolicyExecutable());
					updatePolicyDetails.setUserId(userId);
					updatePolicyDetails.setPolicyDisplayName(policyDetails.getPolicyDisplayName());
					updatePolicyDetails.setAssetGroup(policyDetails.getAssetGroup());
					updatePolicyDetails.setAlexaKeyword(policyDetails.getAlexaKeyword());
					updatePolicyDetails.setModifiedDate(currentDate);
					updatePolicyDetails.setPolicyType(policyDetails.getPolicyType());
					updatePolicyDetails.setPolicyRestUrl(policyDetails.getPolicyRestUrl());
					updatePolicyDetails.setSeverity(policyDetails.getSeverity());
					updatePolicyDetails.setCategory(policyDetails.getCategory());
					updatePolicyDetails.setPolicyDesc(policyDetails.getPolicyDesc());
					updatePolicyDetails.setResolution(policyDetails.getResolution());
					updatePolicyDetails.setResolutionUrl(policyDetails.getResolutionUrl());
					updatePolicyDetails.setAutoFixEnabled(policyDetails.getIsAutofixEnabled());
					createUpdateCloudWatchEventRule(updatePolicyDetails);
					if (policyDetails.getIsFileChanged() && policyDetails.getPolicyType().equalsIgnoreCase("Classic")) {
						createUpdatePolicyJartoS3Bucket(fileToUpload, updatePolicyDetails.getPolicyUUID());
					}
				}
			} else {
				throw new PacManException(String.format(AdminConstants.POLICY_ID_NOT_EXITS,
						(policyDetails.getPolicyId() == null ? "given" : policyDetails.getPolicyId())));
			}
		} else {
			throw new PacManException("Invalid Policy Instance, please provide valid details.");
		}
		return AdminConstants.POLICY_CREATION_SUCCESS;
	}

	private String retrieveDataSource(final Policy updatePolicyDetails) {
		Map<String, Object> policyParams;
		try {
			policyParams = mapper.readValue(updatePolicyDetails.getPolicyParams(),
					new TypeReference<Map<String, Object>>() {
					});
			return String.valueOf(policyParams.get("pac_ds"));
		} catch (Exception exception) {
			log.error(UNEXPECTED_ERROR_OCCURRED, exception);
			return StringUtils.EMPTY;
		}
	}

	private String addPolicyInstance(final MultipartFile fileToUpload, CreateUpdatePolicyDetails policyDetails,
			String userId) throws PacManException {
		if (policyDetails != null) {
			Date currentDate = new Date();
			if (!isPolicyIdExits(policyDetails.getPolicyId())) {
				Policy newPolicyDetails = new Policy();
				String policyUUID = UUID.randomUUID().toString();
				newPolicyDetails.setPolicyId(policyDetails.getPolicyId());
				newPolicyDetails.setPolicyName(policyDetails.getPolicyName());
				newPolicyDetails.setTargetType(policyDetails.getTargetType());
				String policyParams = buildAndGetPolicyParams(policyDetails, policyUUID, true);
				newPolicyDetails.setPolicyParams(policyParams);
				newPolicyDetails.setPolicyFrequency(policyDetails.getPolicyFrequency());
				newPolicyDetails.setPolicyExecutable(policyDetails.getPolicyExecutable());
				newPolicyDetails.setPolicyDisplayName(policyDetails.getPolicyDisplayName());
				newPolicyDetails.setPolicyDesc(policyDetails.getPolicyDesc());
				newPolicyDetails.setResolution(policyDetails.getResolution());
				newPolicyDetails.setResolutionUrl(policyDetails.getResolutionUrl());
				newPolicyDetails.setUserId(userId);
				newPolicyDetails.setStatus(RuleState.ENABLED.name().toUpperCase());
				newPolicyDetails.setAssetGroup(policyDetails.getAssetGroup());
				newPolicyDetails.setAlexaKeyword(policyDetails.getAlexaKeyword());
				newPolicyDetails.setCreatedDate(currentDate);
				newPolicyDetails.setModifiedDate(currentDate);
				newPolicyDetails.setPolicyUUID(policyUUID);
				newPolicyDetails.setPolicyType(policyDetails.getPolicyType());
				newPolicyDetails.setPolicyRestUrl(policyDetails.getPolicyRestUrl());
				newPolicyDetails.setSeverity(policyDetails.getSeverity());
				newPolicyDetails.setCategory(policyDetails.getCategory());
				createUpdateCloudWatchEventRule(newPolicyDetails);
				if (policyDetails.getIsFileChanged() && policyDetails.getPolicyType().equalsIgnoreCase("Classic")) {
					createUpdatePolicyJartoS3Bucket(fileToUpload, policyUUID);
				}
			} else {
				throw new PacManException(String.format(AdminConstants.POLICY_ID_EXITS,
						(policyDetails.getPolicyId() == null ? "given" : policyDetails.getPolicyId())));
			}
		} else {
			throw new PacManException("Invalid Policy Instance, please provide valid details.");
		}
		return AdminConstants.POLICY_CREATION_SUCCESS;
	}

	private void createUpdateCloudWatchEventRule(final Policy policyDetails) {
		try {

			AWSLambda awsLambdaClient = amazonClient.getAWSLambdaClient(config.getRule().getLambda().getRegion());

			if (!checkIfPolicyAvailableForLambda(config.getRule().getLambda().getFunctionName(), awsLambdaClient)) {
				createPolicyForLambda(config.getRule().getLambda().getFunctionName(), awsLambdaClient);
			}
			PutRuleRequest ruleRequest = new PutRuleRequest().withName(policyDetails.getPolicyUUID())
					.withDescription(policyDetails.getPolicyId());
			ruleRequest.withScheduleExpression("cron(".concat(policyDetails.getPolicyFrequency()).concat(")"));
			if (policyDetails.getStatus().equalsIgnoreCase(RuleState.ENABLED.name())) {
				ruleRequest.setState(RuleState.ENABLED);
			} else {
				ruleRequest.setState(RuleState.DISABLED);
			}
			PutRuleResult ruleResult = amazonClient.getAmazonCloudWatchEvents(config.getRule().getLambda().getRegion())
					.putRule(ruleRequest);
			String ruleArn = ruleResult.getRuleArn();
			if (ruleArn != null) {
				policyDetails.setPolicyArn(ruleArn);
				boolean isLambdaFunctionLinked = linkTargetWithRule(policyDetails);
				if (!isLambdaFunctionLinked) {
					// message.put(RuleConst.SUCCESS.getName(), false);
					// message.put(RuleConst.MESSAGE.getName(), "Unexpected Error Occured!");
				} else {
					policyRepository.save(policyDetails);
					invokePolicy(awsLambdaClient, policyDetails, null, null);
				}
			}

		} catch (Exception exception) {
			log.error(UNEXPECTED_ERROR_OCCURRED, exception);
		}
	}

	private void updateCustomEventBridgeRule(final Policy policyDetails) {
		try {

			AWSLambda awsLambdaClient = amazonClient.getAWSLambdaClient(config.getRule().getLambda().getRegion());
			String eventBridgeName = applicationPrefix+"-"+policyDetails.getAssetGroup();
			if (!checkIfPolicyAvailableForLambda(config.getRule().getLambda().getFunctionName(), awsLambdaClient)) {
				createPolicyForLambda(config.getRule().getLambda().getFunctionName(), awsLambdaClient);
			}
			if (policyDetails.getStatus().equalsIgnoreCase(RuleState.ENABLED.name())) {
				com.amazonaws.services.eventbridge.model.EnableRuleRequest enableRuleRequest = new com.amazonaws.services.eventbridge.model.EnableRuleRequest();
				enableRuleRequest.withEventBusName(eventBridgeName)
				.withName(policyDetails.getPolicyUUID());
				amazonClient.getAmazonEventBridgeClient(config.getRule().getLambda().getRegion()).enableRule(enableRuleRequest);
			}else {
				com.amazonaws.services.eventbridge.model.DisableRuleRequest disableRuleRequest = new com.amazonaws.services.eventbridge.model.DisableRuleRequest();
				disableRuleRequest.withEventBusName(eventBridgeName)
				.withName(policyDetails.getPolicyUUID());
				amazonClient.getAmazonEventBridgeClient(config.getRule().getLambda().getRegion()).disableRule(disableRuleRequest);
				
			}
			DescribeRuleRequest describeRuleRequest = new DescribeRuleRequest();
			describeRuleRequest.withEventBusName(eventBridgeName)
			.withName(policyDetails.getPolicyUUID());
			DescribeRuleResult describeRule = amazonClient.getAmazonEventBridgeClient(config.getRule().getLambda().getRegion()).describeRule(describeRuleRequest);
			String arn = describeRule.getArn();
			if (arn != null) {
				policyDetails.setPolicyArn(arn);
				boolean isLambdaFunctionLinked = linkTargetWithRuleForManagedPolicy(policyDetails,eventBridgeName);
				if (!isLambdaFunctionLinked) {
					// message.put(RuleConst.SUCCESS.getName(), false);
					// message.put(RuleConst.MESSAGE.getName(), "Unexpected Error Occured!");
				} else {
					policyRepository.save(policyDetails);
					invokePolicy(awsLambdaClient, policyDetails, null, null);
				}
			}

		} catch (Exception exception) {
			log.error(UNEXPECTED_ERROR_OCCURRED, exception);
		}
	}

	@Override
	public Map<String, Object> invokeAllPolicies(List<String> polocyIds) {
		AWSLambda awsLambdaClient = amazonClient.getAWSLambdaClient(config.getRule().getLambda().getRegion());
		Map<String, Object> responseLists = Maps.newHashMap();
		List<String> successList = Lists.newArrayList();
		List<String> failedList = Lists.newArrayList();
		for (String policyId : polocyIds) {
			Policy ruleInstance = policyRepository.findById(policyId).get();
			boolean isInvoked = invokePolicy(awsLambdaClient, ruleInstance, null, Lists.newArrayList());
			if (isInvoked) {
				successList.add(policyId);
			} else {
				failedList.add(policyId);
			}
		}
		responseLists.put("successList", successList);
		responseLists.put("failedList", failedList);
		return responseLists;
	}

	private boolean invokePolicy(AWSLambda awsLambdaClient, Policy policyDetails, String invocationId,
			List<Map<String, Object>> additionalRuleParams) {
		String ruleParams = policyDetails.getPolicyParams();
		if (invocationId != null) {
			Map<String, Object> ruleParamDetails;
			try {
				ruleParamDetails = mapper.readValue(policyDetails.getPolicyParams(),
						new TypeReference<Map<String, Object>>() {
						});
				ruleParamDetails.put("invocationId", invocationId);
				ruleParamDetails.put("additionalParams", mapper.writeValueAsString(additionalRuleParams));
				ruleParams = mapper.writeValueAsString(ruleParamDetails);
			} catch (Exception exception) {
				log.error(UNEXPECTED_ERROR_OCCURRED, exception);
			}
		}
		String functionName = config.getRule().getLambda().getFunctionName();
		ByteBuffer payload = ByteBuffer.wrap(ruleParams.getBytes());
		InvokeRequest invokeRequest = new InvokeRequest().withFunctionName(functionName).withPayload(payload);
		InvokeResult invokeResult = awsLambdaClient.invoke(invokeRequest);
		if (invokeResult.getStatusCode() == 200) {
			return true;
		} else {
			return false;
		}
	}

	private boolean linkTargetWithRule(final Policy policy) {
		Target target = new Target().withId(config.getRule().getLambda().getTargetId())
				.withArn(config.getRule().getLambda().getFunctionArn()).withInput(policy.getPolicyParams());

		PutTargetsRequest targetsRequest = new PutTargetsRequest().withTargets(target).withRule(policy.getPolicyUUID());

		try {
			PutTargetsResult targetsResult = amazonClient
					.getAmazonCloudWatchEvents(config.getRule().getLambda().getRegion()).putTargets(targetsRequest);
			return (targetsResult.getFailedEntryCount() == 0);
		} catch (Exception exception) {
			return false;
		}
	}

	private boolean linkTargetWithRuleForManagedPolicy(final Policy policy, String eventBridge) {
		com.amazonaws.services.eventbridge.model.Target eventTarget = new com.amazonaws.services.eventbridge.model.Target();
		eventTarget.withArn(config.getRule().getLambda().getFunctionArn())
				.withId(config.getRule().getLambda().getTargetId()).withInput(policy.getPolicyParams());
		com.amazonaws.services.eventbridge.model.PutTargetsRequest targetReq = new com.amazonaws.services.eventbridge.model.PutTargetsRequest();
		targetReq.withTargets(eventTarget).withEventBusName(eventBridge).withRule(policy.getPolicyUUID());

		try {
			com.amazonaws.services.eventbridge.model.PutTargetsResult putTargetResult = amazonClient
					.getAmazonEventBridgeClient(config.getRule().getLambda().getRegion()).putTargets(targetReq);
			return (putTargetResult.getFailedEntryCount() == 0);
		} catch (Exception exception) {
			return false;
		}
	}

	private void createPolicyForLambda(final String lambdaFunctionName, final AWSLambda lambdaClient) {
		AddPermissionRequest addPermissionRequest = new AddPermissionRequest().withFunctionName(lambdaFunctionName)
				.withPrincipal(config.getRule().getLambda().getPrincipal())
				.withStatementId("sid-".concat(config.getRule().getLambda().getTargetId()))
				.withAction(config.getRule().getLambda().getActionEnabled());
		lambdaClient.addPermission(addPermissionRequest);
	}

	private static boolean checkIfPolicyAvailableForLambda(final String lambdaFunctionName,
			final AWSLambda lambdaClient) {
		try {
			GetPolicyRequest getPolicyRequest = new GetPolicyRequest();
			getPolicyRequest.setFunctionName(lambdaFunctionName);
			lambdaClient.getPolicy(getPolicyRequest);
			return true;
		} catch (ResourceNotFoundException resourceNotFoundException) {
			if (resourceNotFoundException.getStatusCode() == 404) {
				return false;
			}
		}
		return false;
	}

	private boolean isPolicyTypeNotServerlessOrManaged(final String policyType) {
		String policyTypeToCheck = policyType.replace(" ", StringUtils.EMPTY);
		return (!policyTypeToCheck.equalsIgnoreCase(AdminConstants.SERVERLESS_RULE_TYPE)
				&& !policyTypeToCheck.equalsIgnoreCase(AdminConstants.MANAGED_POLICY_TYPE));
	}

	private boolean createUpdatePolicyJartoS3Bucket(MultipartFile fileToUpload, String ruleUUID) {
		// @Todo folder name hard coded.
		return awsS3BucketService.uploadFile(amazonClient.getAmazonS3(config.getRule().getS3().getBucketRegion()),
				fileToUpload, config.getJob().getS3().getBucketName() + "/pacbot", ruleUUID.concat(".jar"));
	}

	public boolean isPolicyIdExits(String policyId) {
		return policyRepository.findByPolicyId(policyId) != null;
	}

	@SuppressWarnings("unchecked")
	private String buildAndGetPolicyParams(final CreateUpdatePolicyDetails policyDetails, final String policyUUID,
			final boolean isCreatedNew) {
		Map<String, Object> newJobParams;
		try {
			newJobParams = mapper.readValue(policyDetails.getPolicyParams(), new TypeReference<Map<String, Object>>() {
			});
			newJobParams.put("autofix", policyDetails.getIsAutofixEnabled());
			newJobParams.put("alexaKeyword", policyDetails.getAlexaKeyword());
			newJobParams.put("policyRestUrl", policyDetails.getPolicyRestUrl());
			newJobParams.put("targetType", policyDetails.getTargetType());
			newJobParams.put("pac_ds", policyDetails.getDataSource());
			newJobParams.put("policyId", policyDetails.getPolicyId());
			newJobParams.put("assetGroup", policyDetails.getAssetGroup());
			newJobParams.put("policyUUID", policyUUID);
			newJobParams.put("policyType", policyDetails.getPolicyType());
			Map<String, Object> severity = new HashMap<>();
			severity.put("key", "severity");
			severity.put("value", policyDetails.getSeverity());
			severity.put("encrypt", false);
			Map<String, Object> category = new HashMap<>();
			category.put("key", "policyCategory");
			category.put("value", policyDetails.getCategory());
			category.put("encrypt", false);
			List<Map<String, Object>> environmentVariables = (List<Map<String, Object>>) newJobParams
					.get("environmentVariables");
			List<Map<String, Object>> params = (List<Map<String, Object>>) newJobParams.get("params");
			params.add(severity);
			params.add(category);
			newJobParams.put("environmentVariables",
					encryptDecryptValues(environmentVariables, policyUUID, isCreatedNew));
			newJobParams.put("params", encryptDecryptValues(params, policyUUID, isCreatedNew));
			return mapper.writeValueAsString(newJobParams);
		} catch (Exception exception) {
			log.error(UNEXPECTED_ERROR_OCCURRED, exception);
		}
		return policyDetails.getPolicyParams();
	}

	private List<Map<String, Object>> encryptDecryptValues(List<Map<String, Object>> ruleParams, String ruleUUID,
			boolean isCreatedNew) {
		for (int index = 0; index < ruleParams.size(); index++) {
			Map<String, Object> keyValue = ruleParams.get(index);
			if (isCreatedNew) {
				String isToBeEncrypted = keyValue.get("encrypt").toString();
				if (StringUtils.isNotBlank(isToBeEncrypted) && Boolean.parseBoolean(isToBeEncrypted)) {
					try {
						keyValue.put("value", AdminUtils.encrypt(keyValue.get("value").toString(), ruleUUID));
					} catch (Exception exception) {
						keyValue.put("value", keyValue.get("value").toString());
					}
				}
			} else {
				if (keyValue.get("isValueNew") != null) {
					String isValueNew = keyValue.get("isValueNew").toString();
					String isToBeEncrypted = keyValue.get("encrypt").toString();
					if (StringUtils.isNotBlank(isValueNew) && Boolean.parseBoolean(isValueNew)) {
						if (StringUtils.isNotBlank(isToBeEncrypted) && Boolean.parseBoolean(isToBeEncrypted)) {
							try {
								keyValue.put("value", AdminUtils.encrypt(keyValue.get("value").toString(), ruleUUID));
							} catch (Exception exception) {
								keyValue.put("value", keyValue.get("value").toString());
							}
						}
					}
				}
			}
		}
		return ruleParams;
	}

	@Override
	public List<PolicyCategory> getAllPolicyCategories() throws PacManException {
		return policyCategoryRepository.findAll();
	}
}
