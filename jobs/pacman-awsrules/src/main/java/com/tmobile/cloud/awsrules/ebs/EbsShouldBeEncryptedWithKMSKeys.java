/*******************************************************************************
 * Copyright 2018 T Mobile, Inc. or its affiliates. All Rights Reserved.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License.  You may obtain a copy
 * of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  See the
 * License for the specific language governing permissions and limitations under
 * the License.
 ******************************************************************************/
/**
  Copyright (C) 2017 T Mobile Inc - All Rights Reserve
  Purpose:
  Author :u55262
  Modified Date: Sep 19, 2017
  
 **/
package com.tmobile.cloud.awsrules.ebs;

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
import com.tmobile.pacman.commons.exception.RuleExecutionFailedExeption;
import com.tmobile.pacman.commons.rule.Annotation;
import com.tmobile.pacman.commons.rule.BaseRule;
import com.tmobile.pacman.commons.rule.PacmanRule;
import com.tmobile.pacman.commons.rule.RuleResult;

@PacmanRule(key = "check-for-ebs-volume-should-be-encrypted-with-kms-keys", desc = "checks for the ebs volumes are necrypted using KMS customer managed keys", severity = PacmanSdkConstants.SEV_HIGH, category = PacmanSdkConstants.SECURITY)
public class EbsShouldBeEncryptedWithKMSKeys extends BaseRule {

	private static final Logger logger = LoggerFactory.getLogger(EbsShouldBeEncryptedWithKMSKeys.class);
	
	public static final String VAL_TRUE = "true";
	public static final String VAL_FALSE = "false";
	public static final String VOLUME_ATTR_ENCRYPTED = "encrypted";
	public static final String DEFAULT_KMS_KEY_ALIAS = "alias/aws/ebs";
    
	/**
	 * The method will get triggered from Rule Engine with following parameters
	 * 
	 * @param ruleParam
	 * 
	 ************** Following are the Rule Parameters********* <br><br>
	 * 
	 * ruleKey : check-for-unused-ebs-rule <br><br>
	 *
	 * threadsafe : if true , rule will be executed on multiple threads <br><br>
	 *
	 * esEbsWithInstanceUrl : Enter the ebs es api <br><br>
	 * 
	 * esKmsUrl : Enter the kms es api url <br><br>
	 *
	 * severity : Enter the value of severity <br><br>
	 * 
	 * ruleCategory : Enter the value of category <br><br>
	 * 
	 * @param resourceAttributes this is a resource in context which needs to be scanned this is provided by execution engine
	 *
	 */

	public RuleResult execute(final Map<String, String> ruleParam, Map<String, String> resourceAttributes) {

		logger.debug("========EbsShouldBeEncryptedWithKMSKeys started=========");
		String region = null;
		String ebsUrl = null;
		String esKmsUrl = null;
		String volumeId = null;
		Annotation annotation = null;
		
		String severity = ruleParam.get(PacmanRuleConstants.SEVERITY);
		String category = ruleParam.get(PacmanRuleConstants.CATEGORY);
		

		String formattedUrl = PacmanUtils.formatUrl(ruleParam, PacmanRuleConstants.ES_EBS_WITH_INSTANCE_URL);
		String formattedKmsUrl = PacmanUtils.formatUrl(ruleParam, PacmanRuleConstants.ES_KMS_URL);

		if (!StringUtils.isNullOrEmpty(formattedUrl))
			ebsUrl = formattedUrl;
		if (!StringUtils.isNullOrEmpty(formattedKmsUrl))
			esKmsUrl = formattedKmsUrl;

		MDC.put("executionId", ruleParam.get("executionId"));
		MDC.put("ruleId", ruleParam.get(PacmanSdkConstants.RULE_ID));

		LinkedHashMap<String, Object> issue = new LinkedHashMap<>();
		List<LinkedHashMap<String, Object>> issueList = new ArrayList<>();

		if (!PacmanUtils.doesAllHaveValue(severity, category, ebsUrl, esKmsUrl)) {
			logger.info(PacmanRuleConstants.MISSING_CONFIGURATION);
			throw new InvalidInputException(PacmanRuleConstants.MISSING_CONFIGURATION);
		}

		if (resourceAttributes != null) {
			boolean isEbsWithEc2Exists = false;
			boolean isEbsEncryptedWithKms = false;
			
			volumeId = StringUtils.trim(resourceAttributes.get(PacmanRuleConstants.VOLUME_ID));
			region = StringUtils.trim(resourceAttributes.get(PacmanRuleConstants.REGION_ATTR));
			String encrypted = resourceAttributes.getOrDefault(VOLUME_ATTR_ENCRYPTED, VAL_FALSE);

			try {
				isEbsWithEc2Exists = PacmanUtils.checkISResourceIdExistsFromElasticSearch(volumeId, ebsUrl, PacmanRuleConstants.VOLUME_ID, region);
				String kmsKeyId = resourceAttributes.get(PacmanRuleConstants.ES_KMS_KEY_ID_ATTRIBUTE);
				if (isEbsWithEc2Exists && VAL_TRUE.equalsIgnoreCase(encrypted))
					isEbsEncryptedWithKms = PacmanUtils.checkIfEbsVolumeEncryptedWithCustomKMSKeys(kmsKeyId, esKmsUrl, DEFAULT_KMS_KEY_ALIAS);
			} catch (Exception e) {
				logger.error("unable to determine", e);
				throw new RuleExecutionFailedExeption("unable to determine" + e);
			}

			if (!isEbsEncryptedWithKms) {
				annotation = Annotation.buildAnnotation(ruleParam, Annotation.Type.ISSUE);
				annotation.put(PacmanSdkConstants.DESCRIPTION, "Attached EBS volume without KMS customer managed encryption found");
				annotation.put(PacmanRuleConstants.SEVERITY, severity);
				annotation.put(PacmanRuleConstants.SUBTYPE, Annotation.Type.RECOMMENDATION.toString());
				annotation.put(PacmanRuleConstants.CATEGORY, category);
				annotation.put(PacmanRuleConstants.RESOURCE_ID, volumeId);
				issue.put(PacmanRuleConstants.VIOLATION_REASON, "Attached EBS volume without KMS customer managed encryption found");
				issueList.add(issue);
				annotation.put("issueDetails", issueList.toString());
				logger.debug("========EbsShouldBeEncryptedWithKMSKeys ended with annotation {} :=========", annotation);
				return new RuleResult(PacmanSdkConstants.STATUS_FAILURE, PacmanRuleConstants.FAILURE_MESSAGE, annotation);
			}

		}
		logger.debug("========EbsShouldBeEncryptedWithKMSKeys ended=========");
		return new RuleResult(PacmanSdkConstants.STATUS_SUCCESS, PacmanRuleConstants.SUCCESS_MESSAGE);
	}

	public String getHelpText() {
		return "checks for the ebs volumes are necrypted using KMS customer managed keys";
	}
	
}
