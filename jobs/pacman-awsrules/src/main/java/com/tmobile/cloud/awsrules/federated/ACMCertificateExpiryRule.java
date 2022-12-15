/*******************************************************************************
 * Copyright 2019 T Mobile, Inc. or its affiliates. All Rights Reserved.
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
  Copyright (C) 2019 T Mobile Inc - All Rights Reserve
  Purpose:
  Author :Avinash
  Date: Feb 27, 2019

 **/
package com.tmobile.cloud.awsrules.federated;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.joda.time.DateTime;
import org.joda.time.Days;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;

import com.tmobile.cloud.awsrules.utils.PacmanUtils;
import com.tmobile.cloud.constants.PacmanRuleConstants;
import com.tmobile.pacman.commons.PacmanSdkConstants;
import com.tmobile.pacman.commons.exception.InvalidInputException;
import com.tmobile.pacman.commons.policy.Annotation;
import com.tmobile.pacman.commons.policy.BasePolicy;
import com.tmobile.pacman.commons.policy.PacmanPolicy;
import com.tmobile.pacman.commons.policy.PolicyResult;

@PacmanPolicy(key = "check-for-acm-certificate-expiry", desc = "This Rule should look for the SSL(ACM) expiry with given Date Range", severity = PacmanSdkConstants.SEV_HIGH, category = PacmanSdkConstants.GOVERNANCE)
public class ACMCertificateExpiryRule extends BasePolicy{


	private static final Logger logger = LoggerFactory.getLogger(ACMCertificateExpiryRule.class);

	/**
	 * The method will get triggered from Rule Engine with following parameters
	 *
	 * @param ruleParam
	 *
	 **************Following are the Rule Parameters********* <br><br>
	 *
	 * ruleKey : check-for-acm-certificate-expiry <br><br>
	 *
	 * targetExpireDuration : specify the expiry duration in numbers <br><br>
	 *
	 * severity : Enter the value of severity <br><br>
	 *
	 * ruleCategory : Enter the value of category <br><br>
	 *
	 * @param resourceAttributes this is a resource in context which needs to be scanned this is provided by execution engine
	 *
	 */

	public PolicyResult execute(final Map<String, String> ruleParam,Map<String, String> resourceAttributes) {
		logger.debug("========ACMCertificateExpiryRule started=========");
		Annotation annotation = null;
		Date validTo = null;
		String expiredDate = resourceAttributes.get("expirydate");
		SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		String targetExpiryDurationInString = ruleParam.get(PacmanRuleConstants.EXPIRED_DURATION);
		String severity = ruleParam.get(PacmanRuleConstants.SEVERITY);
		String category = ruleParam.get(PacmanRuleConstants.CATEGORY);

		MDC.put("executionId", ruleParam.get("executionId"));
		MDC.put("ruleId", ruleParam.get(PacmanSdkConstants.POLICY_ID));

		List<LinkedHashMap<String,Object>>issueList = new ArrayList<>();
		LinkedHashMap<String,Object>issue = new LinkedHashMap<>();

		if (!PacmanUtils.doesAllHaveValue(targetExpiryDurationInString,severity,category)) {
			logger.info(PacmanRuleConstants.MISSING_CONFIGURATION);
			throw new InvalidInputException(PacmanRuleConstants.MISSING_CONFIGURATION);
		}
		if (expiredDate != null) {
			try {
				validTo = dateFormat.parse(expiredDate);
			} catch (ParseException e) {
				logger.info("Exception in ACM accesskey" + e.getMessage());
			}
			int targetExpiryDurationInt = Integer.parseInt(targetExpiryDurationInString);
			if (calculateSslExpiredDuration(validTo, targetExpiryDurationInt)) {
					annotation = Annotation.buildAnnotation(ruleParam,Annotation.Type.ISSUE);
					annotation.put(PacmanSdkConstants.DESCRIPTION,"SSL(ACM) Expiry within "+ targetExpiryDurationInString+ " days found!!");
					annotation.put(PacmanRuleConstants.SEVERITY, severity);
					annotation.put(PacmanRuleConstants.CATEGORY, category);

					issue.put(PacmanRuleConstants.VIOLATION_REASON, "SSL(ACM) Expiry within "+ targetExpiryDurationInString+ " days found!!");
					issueList.add(issue);
					annotation.put("issueDetails",issueList.toString());
					logger.debug("========ACMCertificateExpiryRule ended with annotation {} : =========",annotation);
					return new PolicyResult(PacmanSdkConstants.STATUS_FAILURE,PacmanRuleConstants.FAILURE_MESSAGE, annotation);
			} else {
				logger.info("SSL(ACM) validity not expired");
			}
		}
		logger.debug("========ACMCertificateExpiryRule ended=========");
		return new PolicyResult(PacmanSdkConstants.STATUS_SUCCESS,PacmanRuleConstants.SUCCESS_MESSAGE);
	}

	public String getHelpText() {
		return "This Rule should look for the SSL(ACM) expiry with given Date Range";
	}

	/**
	 * This method calculates the difference between the current date and the
	 * validto date It uses the TimeUnit utility for conversion purpose.
	 *
	 * @param formattedDateString - String
	 * @return expiredDuration - Long
	 * @throws ParseException
	 */

	private boolean calculateSslExpiredDuration(Date expiryDateFormat, int targetExpiryDurationInt) {
		boolean isFlag = false;
		logger.debug("targetExpiryDurationInt {}", targetExpiryDurationInt);
		if(expiryDateFormat!=null){
			DateTime expiryDate = new DateTime(expiryDateFormat);
			logger.debug("expiryDate {}", expiryDate);
			DateTime currentDate = new DateTime();
			logger.debug("currentDate {}", currentDate);
			int day = Days.daysBetween(currentDate, expiryDate).getDays();
			logger.debug("day {}", day);
			if (Days.daysBetween(currentDate, expiryDate).getDays() <= targetExpiryDurationInt) {
				isFlag = true;
			}
		}
		logger.debug("isFlag {}" , isFlag);
		 return isFlag;
	}



}
