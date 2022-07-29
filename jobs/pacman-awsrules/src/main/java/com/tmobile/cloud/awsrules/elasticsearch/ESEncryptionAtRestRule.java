package com.tmobile.cloud.awsrules.elasticsearch;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;

import com.amazonaws.util.StringUtils;
import com.tmobile.cloud.awsrules.utils.PacmanUtils;
import com.tmobile.cloud.constants.PacmanRuleConstants;
import com.tmobile.pacman.commons.PacmanSdkConstants;
import com.tmobile.pacman.commons.exception.InvalidInputException;
import com.tmobile.pacman.commons.rule.Annotation;
import com.tmobile.pacman.commons.rule.BaseRule;
import com.tmobile.pacman.commons.rule.PacmanRule;
import com.tmobile.pacman.commons.rule.RuleResult;

@PacmanRule(key = "check-for-es-encrypted-at-rest", desc = "checks for Amazon Elasticsearch domain encrypted at Rest", severity = PacmanSdkConstants.SEV_HIGH, category = PacmanSdkConstants.SECURITY)
public class ESEncryptionAtRestRule extends BaseRule {
	private static final Logger logger = LoggerFactory.getLogger(ESEncryptionAtRestRule.class);


	public static final String ES_PROP_ENCRYPTION_ENABLED = "encryptionenabled";
	public static final String ES_PROP_VERSION = "elasticsearchversion";


	/**
	 * The method will get triggered from Rule Engine with following parameters
	 * 
	 * @param ruleParam
	 * 
	 **************Following are the Rule Parameters********* <br>
	 *                           <br>
	 * 
	 *                           ruleKey : check-for-es-encrypted-at-rest
	 *                           <br>
	 *                           <br>
	 * 
	 *                           threadsafe : if true , rule will be executed on
	 *                           multiple threads <br>
	 *                           <br>
	 * 
	 *                           severity : Enter the value of severity <br>
	 *                           <br>
	 * 
	 *                           ruleCategory : Enter the value of category <br>
	 *                           <br>
	 *
	 *                           engineType : Enter the Cluster Engine type to
	 *                           validate <br>
	 *                           <br>
	 *
	 *                           engineVersion : Enter the Cluster Engine version to
	 *                           validate <br>
	 *                           <br>
	 *
	 * @param resourceAttributes this is a resource in context which needs to be
	 *                           scanned this is provided by execution engine
	 *
	 */
	@Override
	public RuleResult execute(Map<String, String> ruleParam, Map<String, String> resourceAttributes) {
		logger.debug("========ESEncryptionAtRestRule started=========");
		Annotation annotation = null;

		String severity = ruleParam.get(PacmanRuleConstants.SEVERITY);
		String category = ruleParam.get(PacmanRuleConstants.CATEGORY);


		MDC.put("executionId", ruleParam.get("executionId")); // this is the logback Mapped Diagnostic Contex
		MDC.put("ruleId", ruleParam.get(PacmanSdkConstants.RULE_ID)); // this is the logback Mapped Diagnostic Contex

		if (!PacmanUtils.doesAllHaveValue(severity, category)) {
			logger.info(PacmanRuleConstants.MISSING_CONFIGURATION);
			throw new InvalidInputException(PacmanRuleConstants.MISSING_CONFIGURATION);
		}
		if (resourceAttributes != null) {
			String esEncryEnable = StringUtils.trim(resourceAttributes.get(ES_PROP_ENCRYPTION_ENABLED));

				if (Double.parseDouble(resourceAttributes.get(ES_PROP_VERSION)) >= 5.1 &&
						(esEncryEnable == null || "".equals(esEncryEnable) 
						|| !PacmanRuleConstants.TRUE_VAL.equalsIgnoreCase(esEncryEnable) )) {
					List<LinkedHashMap<String, Object>> issueList = new ArrayList<>();
					LinkedHashMap<String, Object> issue = new LinkedHashMap<>();
					annotation = Annotation.buildAnnotation(ruleParam, Annotation.Type.ISSUE);
					annotation.put(PacmanSdkConstants.DESCRIPTION, "ES domain are not encrypted."
							+ "At rest encryption should be enabled for OpenSearch with version 5.1 or greater!!");
					annotation.put(PacmanRuleConstants.SEVERITY, severity);;
					annotation.put(PacmanRuleConstants.CATEGORY, category);
					issue.put(PacmanRuleConstants.VIOLATION_REASON,
							"ES domain are not encrypted!!");
					issueList.add(issue);
					annotation.put("issueDetails", issueList.toString());
					logger.debug("========ESEncryptionAtRestRule ended with annotation {} :=========", annotation);
					return new RuleResult(PacmanSdkConstants.STATUS_FAILURE, PacmanRuleConstants.FAILURE_MESSAGE,
							annotation);
				}

		}
		logger.debug("========ESEncryptionAtRestRule ended=========");
		return new RuleResult(PacmanSdkConstants.STATUS_SUCCESS, PacmanRuleConstants.SUCCESS_MESSAGE);

	}

	@Override
	public String getHelpText() {
		return "This rule checks for Amazon Elasticsearch domain encrypted at Rest";
	}

}
