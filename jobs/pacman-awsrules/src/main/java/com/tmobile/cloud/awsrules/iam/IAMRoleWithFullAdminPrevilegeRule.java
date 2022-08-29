package com.tmobile.cloud.awsrules.iam;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;

import com.amazonaws.services.identitymanagement.AmazonIdentityManagementClient;
import com.amazonaws.services.identitymanagement.model.AttachedPolicy;
import com.amazonaws.util.CollectionUtils;
import com.amazonaws.util.StringUtils;
import com.tmobile.cloud.awsrules.utils.IAMUtils;
import com.tmobile.cloud.awsrules.utils.PacmanUtils;
import com.tmobile.cloud.constants.PacmanRuleConstants;
import com.tmobile.pacman.commons.AWSService;
import com.tmobile.pacman.commons.PacmanSdkConstants;
import com.tmobile.pacman.commons.exception.InvalidInputException;
import com.tmobile.pacman.commons.exception.RuleExecutionFailedExeption;
import com.tmobile.pacman.commons.exception.UnableToCreateClientException;
import com.tmobile.pacman.commons.rule.Annotation;
import com.tmobile.pacman.commons.rule.BaseRule;
import com.tmobile.pacman.commons.rule.PacmanRule;
import com.tmobile.pacman.commons.rule.RuleResult;

@PacmanRule(key = "iam-role-with-admin-privilege-policy", desc = "Checks if any iam cutomer managed policy with full admin previlege is attached to an iam role", severity = PacmanSdkConstants.SEV_HIGH, category = PacmanSdkConstants.SECURITY)
public class IAMRoleWithFullAdminPrevilegeRule extends BaseRule {

	/** The Constant LOGGER. */
	private static final Logger logger = LoggerFactory.getLogger(IAMRoleWithFullAdminPrevilegeRule.class);
	private static final String ROLE_NAME = "rolename";

	/**
	 * The method will get triggered from Rule Engine with following parameters
	 * 
	 * @param ruleParam
	 * 
	 ************** Following are the Rule Parameters********* <br><br>
	 * 
	 * ruleKey : iam-role-with-admin-privilege-policy <br><br>
	 *
	 * threadsafe : if true , rule will be executed on multiple threads <br><br>
	 *
	 * severity : Enter the value of severity <br><br>
	 * 
	 * ruleCategory : Enter the value of category <br><br>
	 * 
	 * @param resourceAttributes this is a resource in context which needs to be scanned this is provided by execution engine
	 *
	 */

	@Override
	public RuleResult execute(final Map<String, String> ruleParam, Map<String, String> resourceAttributes) {

		logger.debug("========IAMRoleWithFullAdminPrevilegeRule started=========");

		MDC.put("executionId", ruleParam.get("executionId"));
		MDC.put("ruleId", ruleParam.get(PacmanSdkConstants.RULE_ID));
		
		Optional.ofNullable(ruleParam)
				.filter(param -> (!PacmanUtils.doesAllHaveValue(param.get(PacmanRuleConstants.SEVERITY),
						param.get(PacmanRuleConstants.CATEGORY), param.get(PacmanSdkConstants.Role_IDENTIFYING_STRING),
						param.get(PacmanRuleConstants.ES_CUSTOMER_MGD_POLICY_URL))))
				.map(param -> {
					logger.info(PacmanRuleConstants.MISSING_CONFIGURATION);
					throw new InvalidInputException(PacmanRuleConstants.MISSING_CONFIGURATION);
				});
		
		Optional<String> opt = Optional.ofNullable(resourceAttributes)
				.map(resource -> checkValidation(ruleParam, resource));
		
		RuleResult ruleResult = Optional.ofNullable(ruleParam)
				.filter(param -> opt.isPresent())
				.map(param -> buildFailureAnnotation(param, opt.get()))
				.orElse(new RuleResult(PacmanSdkConstants.STATUS_SUCCESS, PacmanRuleConstants.SUCCESS_MESSAGE));


		logger.debug("========IAMRoleWithFullAdminPrevilegeRule ended=========");
		return ruleResult;

	}
    
	/**
	 * @param ruleParam
	 * @param resourceAttributes
	 * @return
	 * 
	 * Validate the inline and attached policies to the role for the statement {Effect:*,Action:*,Resource:*}
	 * 
	 */
	private String checkValidation(Map<String, String> ruleParam, Map<String, String> resourceAttributes) {

		String description = null;
		Map<String, Object> map = null;
		boolean isAdminPrevilege = false;
		AmazonIdentityManagementClient iamClient = null;
		Map<String, String> ruleParamforIAM = new HashMap<>();

		ruleParamforIAM.putAll(ruleParam);
		ruleParamforIAM.put("region", "us-east-1");

		String roleName = resourceAttributes.get(ROLE_NAME);

		try {
			map = getClientFor(AWSService.IAM, ruleParamforIAM.get(PacmanSdkConstants.Role_IDENTIFYING_STRING), ruleParamforIAM);
			iamClient = (AmazonIdentityManagementClient) map.get(PacmanSdkConstants.CLIENT);

			List<AttachedPolicy> attachedPolicy = IAMUtils.getAttachedPolicyOfIAMRole(roleName, iamClient);

			Set<String> policyNames = new HashSet<>();
			if (!CollectionUtils.isNullOrEmpty(attachedPolicy)) {
				policyNames = attachedPolicy.stream().map(AttachedPolicy::getPolicyName).collect(Collectors.toSet());

				if (!CollectionUtils.isNullOrEmpty(policyNames)) {
					String formattedIamPolicyUrl = PacmanUtils.formatUrl(ruleParam, PacmanRuleConstants.ES_CUSTOMER_MGD_POLICY_URL);
					String esIamPoliciesUrl = !StringUtils.isNullOrEmpty(formattedIamPolicyUrl) ? formattedIamPolicyUrl : "";

					Set<String> policies = PacmanUtils.getIamCustManagedPolicyByName(policyNames, esIamPoliciesUrl);

					if (!CollectionUtils.isNullOrEmpty(policies)) {
						for (String policyArn : policies) {
							if (!StringUtils.isNullOrEmpty(policyArn))
								isAdminPrevilege = IAMUtils.isPolicyWithFullAdminAccess(policyArn, iamClient);

							if (isAdminPrevilege)
								return description = "Customer managed policy having full admin privilege is attached to the IAM role!!";
						}

					}
				}
			}
			if (IAMUtils.isInlineRolePolicyWithFullAdminAccess(roleName, iamClient))
				return description = "Customer managed policy having full admin privilege is attached to the IAM role!!";

		} catch (UnableToCreateClientException e) {
			logger.error("unable to get client for following input", e);
			throw new InvalidInputException(e.toString());
		} catch (Exception e) {
			logger.error("unable to determine", e);
			throw new RuleExecutionFailedExeption("unable to determine" + e);
		}
		return description;
	}

	private static RuleResult buildFailureAnnotation(final Map<String, String> ruleParam, String description) {
		
		Annotation annotation = null;
		LinkedHashMap<String, Object> issue = new LinkedHashMap<>();
		List<LinkedHashMap<String, Object>> issueList = new ArrayList<>();

		annotation = Annotation.buildAnnotation(ruleParam,Annotation.Type.ISSUE);
		annotation.put(PacmanSdkConstants.DESCRIPTION,description);
		annotation.put(PacmanRuleConstants.SEVERITY, ruleParam.get(PacmanRuleConstants.SEVERITY));
		annotation.put(PacmanRuleConstants.CATEGORY, ruleParam.get(PacmanRuleConstants.CATEGORY));
		annotation.put(PacmanRuleConstants.RESOURCE_ID, ruleParam.get(PacmanRuleConstants.RESOURCE_ID));
		issue.put(PacmanRuleConstants.VIOLATION_REASON, description);
		issueList.add(issue);
		annotation.put("issueDetails",issueList.toString());
		logger.debug("========IAMRoleWithFullAdminPrevilegeRule annotation {} :=========",annotation);
		return new RuleResult(PacmanSdkConstants.STATUS_FAILURE,PacmanRuleConstants.FAILURE_MESSAGE, annotation);
	
	
	}
	
	@Override
	public String getHelpText() {
		return "Checks if any iam cutomer managed policy with full admin previlege is attached to an iam role";
	}

}
