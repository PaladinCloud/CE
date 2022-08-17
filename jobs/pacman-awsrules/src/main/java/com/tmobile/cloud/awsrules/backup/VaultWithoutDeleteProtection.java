package com.tmobile.cloud.awsrules.backup;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;

import com.amazonaws.util.StringUtils;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.tmobile.cloud.awsrules.utils.PacmanUtils;
import com.tmobile.cloud.constants.PacmanRuleConstants;
import com.tmobile.pacman.commons.PacmanSdkConstants;
import com.tmobile.pacman.commons.exception.InvalidInputException;
import com.tmobile.pacman.commons.exception.RuleExecutionFailedExeption;
import com.tmobile.pacman.commons.rule.Annotation;
import com.tmobile.pacman.commons.rule.BaseRule;
import com.tmobile.pacman.commons.rule.PacmanRule;
import com.tmobile.pacman.commons.rule.RuleResult;

@PacmanRule(key = "check-for-bakup-vault-delete-policy", desc = "checks for AWS backup vault is configured with delete access policy", severity = PacmanSdkConstants.SEV_HIGH, category = PacmanSdkConstants.SECURITY)
public class VaultWithoutDeleteProtection extends BaseRule {

	private static final Logger logger = LoggerFactory.getLogger(VaultWithoutDeleteProtection.class);
	
	/**
	 * The method will get triggered from Rule Engine with following parameters
	 * 
	 * @param ruleParam
	 * 
	 ************** Following are the Rule Parameters********* <br><br>
	 * 
	 * ruleKey : check-for-bakup-vault-delete-policy <br><br>
	 * 
	 * esVaultUrl : Enter the es ami URL<br><br>
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

	public RuleResult execute(final Map<String, String> ruleParam, Map<String, String> resourceAttributes) {

		logger.debug("========VaultWithoutDeleteProtection started=========");

		MDC.put("executionId", ruleParam.get("executionId"));
		MDC.put("ruleId", ruleParam.get(PacmanSdkConstants.RULE_ID));

		Optional.ofNullable(ruleParam)
				.filter(param -> (!PacmanUtils.doesAllHaveValue(param.get(PacmanRuleConstants.SEVERITY),
						param.get(PacmanRuleConstants.CATEGORY))))
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


		logger.debug("========VaultWithoutDeleteProtection ended=========");
		return ruleResult;

	}
	
	private String checkValidation(Map<String, String> ruleParam, Map<String, String> resourceAttributes) {

		String description = null;
		String accessPolicies = resourceAttributes.get(PacmanRuleConstants.ES_VAULT_POLICY_ATTRIBUTE);

		try {
			if (org.apache.commons.lang3.StringUtils.isEmpty(accessPolicies) || !checkVaultDenyDeletePolicyExist(accessPolicies)) 
				description = "Delete protection access policy is not configured for the Bakup vault !!";
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
		issue.put(PacmanRuleConstants.VIOLATION_REASON, "Backup vault should be configured with access policy to deny deletion.");
		issueList.add(issue);
		annotation.put("issueDetails",issueList.toString());
		logger.debug("========VaultWithoutDeleteProtection annotation {} :=========",annotation);
		return new RuleResult(PacmanSdkConstants.STATUS_FAILURE,PacmanRuleConstants.FAILURE_MESSAGE, annotation);
	
	
	}

	public String getHelpText() {
		return "This rule checks for AWS backup vault is configured with delete access policy";
	}
	
	/**
	 * @param isDeleteProtected
	 * @param accessPolicies
	 * @return
	 */
	public static Boolean checkVaultDenyDeletePolicyExist(String accessPolicies) throws Exception{

		JsonArray statments = new JsonArray();
		Gson serializer = new GsonBuilder().create();
		JsonObject accessPoliciesJson = serializer.fromJson(accessPolicies, JsonObject.class);
		if (accessPoliciesJson.has(PacmanRuleConstants.STATEMENT)) 
			statments = accessPoliciesJson.get(PacmanRuleConstants.STATEMENT).getAsJsonArray();
		try {
			
			List<String> actionList = new ArrayList<>();
			if (null != statments && !statments.isEmpty()) {
				for (int i = 0; i < statments.size(); i++) {

					String principalStr = null;
					Boolean isPrincipalStar = false;
					JsonObject principal = new JsonObject();

					JsonObject eachStatement = statments.get(i).getAsJsonObject();
					String effect = eachStatement.get(PacmanRuleConstants.EFFECT).getAsString();

					if (eachStatement.get(PacmanRuleConstants.PRINCIPAL).isJsonObject())
						principal = eachStatement.get(PacmanRuleConstants.PRINCIPAL).getAsJsonObject();
					else
						principalStr = eachStatement.get(PacmanRuleConstants.PRINCIPAL).getAsString();

					if (StringUtils.isNullOrEmpty(principalStr) || null != principal) {
						if ("*".equalsIgnoreCase(principalStr))
							isPrincipalStar = true;
						else if (principal.has("AWS")) {
							if (principal.get("AWS").isJsonArray()) {
								JsonArray principals = new JsonArray();
								principals = principal.get("AWS").getAsJsonArray();
								for (int k = 0; k < principals.size(); k++) {
									if ("*".equalsIgnoreCase(principals.get(k).getAsString())) {
										isPrincipalStar = true;
										break;
									}
								}
							} else if ("*".equalsIgnoreCase(principal.get("AWS").getAsString()))
									isPrincipalStar = true;
						}

						JsonArray actions = new JsonArray();
						if (eachStatement.get(PacmanRuleConstants.ACTION).isJsonArray()) {
							actions = eachStatement.get(PacmanRuleConstants.ACTION).getAsJsonArray();
							if (null != actions && !actions.isEmpty()) {
								for (int j = 0; j < actions.size(); j++) {
									actionList.add(actions.get(j).getAsString());
								}

							}
						}else 
							actionList.add(eachStatement.get(PacmanRuleConstants.ACTION).getAsString());

						if (effect.equalsIgnoreCase(PacmanRuleConstants.DENY) && isPrincipalStar
								&& actionList.contains("backup:DeleteRecoveryPoint")) {
							return true;
						}
					}
				}
			}

		} catch (Exception e1) {
			logger.error("error", e1);
			throw new RuleExecutionFailedExeption(e1.getMessage());
		}

		return false;
	}
	
}
