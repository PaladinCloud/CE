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
package com.tmobile.cloud.awsrules.ec2;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;

import com.amazonaws.services.ec2.model.GroupIdentifier;
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

@PacmanRule(key = "check-for-ec2-with-default-security-group", desc = "checks for EC2 instance with default security group", severity = PacmanSdkConstants.SEV_HIGH, category = PacmanSdkConstants.SECURITY)
public class EC2WithDefaultSecurityGroup extends BaseRule {
    private static final Logger logger = LoggerFactory.getLogger(EC2WithDefaultSecurityGroup.class);

    /**
     * The method will get triggered from Rule Engine with following parameters
     * 
     * @param ruleParam
     * 
     * ************* Following are the Rule Parameters********* <br><br>
     * 
     * ruleKey : check-for-ec2-public-access <br><br>
     * 
     * severity : Enter the value of severity <br><br>
     * 
     * ruleCategory : Enter the value of category <br><br>
     * 
     * esEc2SgURL : Enter the EC2 with SG URL <br><br>
     * 
     * securityGroupName : Enter the security group name <br><br>
     * 
     * threadsafe : if true , rule will be executed on multiple threads <br><br>
     * 
     * @param resourceAttributes this is a resource in context which needs to be scanned this is provided by execution engine
     *
     */

	@Override
	public RuleResult execute(Map<String, String> ruleParam, Map<String, String> resourceAttributes) {

		logger.debug("========EC2WithDefaultSecurityGroup started=========");
		String ec2SgEsURL = null;
		Annotation annotation = null;

		MDC.put("executionId", ruleParam.get("executionId"));
		MDC.put("ruleId", ruleParam.get(PacmanSdkConstants.RULE_ID));

		Set<GroupIdentifier> securityGroupsSet = new HashSet<>();
		List<LinkedHashMap<String, Object>> issueList = new ArrayList<>();
		LinkedHashMap<String, Object> issue = new LinkedHashMap<>();

		if (resourceAttributes != null && StringUtils.trim(resourceAttributes.get("statename")).equals(PacmanRuleConstants.RUNNING_STATE)) {
			
			String entityId = resourceAttributes.get("instanceid");
			String severity = ruleParam.get(PacmanRuleConstants.SEVERITY);
			String category = ruleParam.get(PacmanRuleConstants.CATEGORY);
			String securityGroupName = ruleParam.get(PacmanRuleConstants.SECURITY_GROUP_NAME);
			String pacmanHost = PacmanUtils.getPacmanHost(PacmanRuleConstants.ES_URI);
			logger.debug("========pacmanHost {}  =========", pacmanHost);

			if (!StringUtils.isNullOrEmpty(pacmanHost)) {
				ec2SgEsURL = ruleParam.get(PacmanRuleConstants.ES_EC2_SG_URL);
				ec2SgEsURL = pacmanHost + ec2SgEsURL;
			}
			logger.debug("========ec2SgEsURL URL after concatination param {}  =========", ec2SgEsURL);

			if (!PacmanUtils.doesAllHaveValue(severity, category, ec2SgEsURL, securityGroupName)) {
				logger.info(PacmanRuleConstants.MISSING_CONFIGURATION);
				throw new InvalidInputException(PacmanRuleConstants.MISSING_CONFIGURATION);
			}

			try {
				List<GroupIdentifier> listSecurityGroupID = PacmanUtils.getDefaultSecurityGroupsByInstanceId(entityId, ec2SgEsURL, securityGroupName);
				securityGroupsSet.addAll(listSecurityGroupID);

				if (!securityGroupsSet.isEmpty()) {
					annotation = Annotation.buildAnnotation(ruleParam, Annotation.Type.ISSUE);
					annotation.put(PacmanSdkConstants.DESCRIPTION, "EC2 with default security group found");
					annotation.put(PacmanRuleConstants.SEVERITY, severity);
					annotation.put(PacmanRuleConstants.CATEGORY, category);
					annotation.put(PacmanRuleConstants.RESOURCE_ID, entityId);
					issue.put(PacmanRuleConstants.VIOLATION_REASON, "EC2 with default security group found");
					issueList.add(issue);
					annotation.put("issueDetails", issueList.toString());
					logger.debug("========EC2WithDefaultSecurityGroup ended with an annotation {} : =========", annotation);
					return new RuleResult(PacmanSdkConstants.STATUS_FAILURE, PacmanRuleConstants.FAILURE_MESSAGE, annotation);
				}

			} catch (Exception exception) {
				logger.error("error: ", exception);
				throw new RuleExecutionFailedExeption(exception.getMessage());
			}
		}
		logger.debug("========EC2WithDefaultSecurityGroup ended=========");
		return new RuleResult(PacmanSdkConstants.STATUS_SUCCESS, PacmanRuleConstants.SUCCESS_MESSAGE);
	}

    @Override
    public String getHelpText() {
        return "checks entirely for ec2 instance with default security group";
    }

}
