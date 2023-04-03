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
import com.tmobile.pacman.commons.policy.Annotation;
import com.tmobile.pacman.commons.policy.BasePolicy;
import com.tmobile.pacman.commons.policy.PacmanPolicy;
import com.tmobile.pacman.commons.policy.PolicyResult;

@PacmanPolicy(key = "check-for-es-node-to-node-encrypted", desc = "checks for Amazon Elasticsearch node to node encrypted", severity = PacmanSdkConstants.SEV_HIGH, category = PacmanSdkConstants.SECURITY)
public class ESNodetoNodeEncryptionRule extends BasePolicy {
	private static final Logger logger = LoggerFactory.getLogger(ESNodetoNodeEncryptionRule.class);


	public static final String ES_PROP_NODE_ENCRYPTION = "nodetonodeencryption";
	public static final String ES_PROP_VERSION = "elasticsearchversion";

	/**
	 * The method will get triggered from Rule Engine with following parameters
	 * 
	 * @param ruleParam
	 * 
	 **************Following are the Rule Parameters********* <br>
	 *                           <br>
	 * 
	 *                           ruleKey : check-for-es-node-to-node-encrypted
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
	public PolicyResult execute(Map<String, String> ruleParam, Map<String, String> resourceAttributes) {
		logger.debug("========ESNodetoNodeEncryptionRule started=========");
		Annotation annotation = null;

		String severity = ruleParam.get(PacmanRuleConstants.SEVERITY);
		String category = ruleParam.get(PacmanRuleConstants.CATEGORY);


		MDC.put("executionId", ruleParam.get("executionId")); // this is the logback Mapped Diagnostic Contex
		MDC.put("ruleId", ruleParam.get(PacmanSdkConstants.POLICY_ID)); // this is the logback Mapped Diagnostic Contex

		if (!PacmanUtils.doesAllHaveValue(severity, category)) {
			logger.info(PacmanRuleConstants.MISSING_CONFIGURATION);
			throw new InvalidInputException(PacmanRuleConstants.MISSING_CONFIGURATION);
		}
		if (resourceAttributes != null) {
			String nodeEncy = StringUtils.trim(resourceAttributes.get(ES_PROP_NODE_ENCRYPTION));

				if (checkIfVersionIsCorrect(resourceAttributes.get(ES_PROP_VERSION)) &&
						(nodeEncy == null || "".equals(nodeEncy) 
						|| !PacmanRuleConstants.TRUE_VAL.equalsIgnoreCase(nodeEncy)) ) {
					List<LinkedHashMap<String, Object>> issueList = new ArrayList<>();
					LinkedHashMap<String, Object> issue = new LinkedHashMap<>();
					annotation = Annotation.buildAnnotation(ruleParam, Annotation.Type.ISSUE);
					annotation.put(PacmanSdkConstants.DESCRIPTION, "ES node to node encryption not enabled."
							+ "Node to node encryption should be enabled for OpenSearch with version 6.0 or greater!! ");
					annotation.put(PacmanRuleConstants.SEVERITY, severity);;
					annotation.put(PacmanRuleConstants.CATEGORY, category);
					issue.put(PacmanRuleConstants.VIOLATION_REASON,
							"ES node to node encryption not enabled!!");
					issueList.add(issue);
					annotation.put("issueDetails", issueList.toString());
					logger.debug("========ESNodetoNodeEncryptionRule ended with annotation {} :=========", annotation);
					return new PolicyResult(PacmanSdkConstants.STATUS_FAILURE, PacmanRuleConstants.FAILURE_MESSAGE,
							annotation);
				}

		}
		logger.debug("========ESNodetoNodeEncryptionRule ended=========");
		return new PolicyResult(PacmanSdkConstants.STATUS_SUCCESS, PacmanRuleConstants.SUCCESS_MESSAGE);

	}
	private boolean checkIfVersionIsCorrect(String version) {
		if(version.startsWith("OpenSearch"))
			return true;
		if(Double.parseDouble(version) >= 6.0)
			return true;
		return false;
	}

	@Override
	public String getHelpText() {
		return "checks for Amazon Elasticsearch node to node encrypted";
	}

}
