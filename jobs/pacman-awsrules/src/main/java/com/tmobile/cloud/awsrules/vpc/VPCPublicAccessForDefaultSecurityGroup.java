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
package com.tmobile.cloud.awsrules.vpc;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

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

@PacmanRule(key = "check-for-vpc-default-security-group-public-access", desc = "checks for vpc has public access for default security group", severity = PacmanSdkConstants.SEV_HIGH, category = PacmanSdkConstants.SECURITY)
public class VPCPublicAccessForDefaultSecurityGroup extends BaseRule {
    private static final Logger logger = LoggerFactory.getLogger(VPCPublicAccessForDefaultSecurityGroup.class);

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
     * esSgRulesUrl : Enter the SG Rule URL <br><br>
     * 
     * securityGroupName : Enter the security group name <br><br>
     * 
     * cidrIp : Enter the ip as 0.0.0.0/0 <br><br>
     * 
     * cidripv6 : Enter the ip as ::/0 <br><br>
     * 
     * threadsafe : if true , rule will be executed on multiple threads <br><br>
     * 
     * @param resourceAttributes this is a resource in context which needs to be scanned this is provided by execution engine
     *
     */

	@Override
	public RuleResult execute(Map<String, String> ruleParam, Map<String, String> resourceAttributes) {

		logger.debug("========VPCPublicAccessForDefaultSecurityGroup started=========");
		String esSgURL = null;
		String esSgRulesURL = null;
		Annotation annotation = null;

		MDC.put("executionId", ruleParam.get("executionId"));
		MDC.put("ruleId", ruleParam.get(PacmanSdkConstants.RULE_ID));

		Set<String> securityGroupsSet = new HashSet<>();
		Set<String> unrestrictedSecurityGroupsSet = new HashSet<>();
		LinkedHashMap<String, Object> issue = new LinkedHashMap<>();
		List<LinkedHashMap<String, Object>> issueList = new ArrayList<>();

		String cidrIp = ruleParam.get(PacmanRuleConstants.CIDR_IP);
		String cidrIpv6 = ruleParam.get(PacmanRuleConstants.CIDRIPV6);
		String severity = ruleParam.get(PacmanRuleConstants.SEVERITY);
		String category = ruleParam.get(PacmanRuleConstants.CATEGORY);
		String vpcId = ruleParam.get(PacmanRuleConstants.RESOURCE_ID);
		String pacmanHost = PacmanUtils.getPacmanHost(PacmanRuleConstants.ES_URI);
		String securityGroupName = ruleParam.get(PacmanRuleConstants.SECURITY_GROUP_NAME);

		logger.debug("========pacmanHost {}  =========", pacmanHost);

		if (!StringUtils.isNullOrEmpty(pacmanHost)) {
			esSgURL = ruleParam.get(PacmanRuleConstants.ES_SG_URL);
			esSgRulesURL = ruleParam.get(PacmanRuleConstants.ES_SG_RULES_URL);
			esSgURL = pacmanHost + esSgURL;
			esSgRulesURL = pacmanHost + esSgRulesURL;
		}
		logger.debug("========ec2SgEsURL URL after concatination param {}  =========", esSgURL);
		logger.debug("========esSgRulesURL URL after concatination param {}  =========", esSgRulesURL);

		if (!PacmanUtils.doesAllHaveValue(severity, category, esSgURL, esSgRulesURL, securityGroupName, cidrIp, cidrIpv6)) {
			logger.info(PacmanRuleConstants.MISSING_CONFIGURATION);
			throw new InvalidInputException(PacmanRuleConstants.MISSING_CONFIGURATION);
		}

		try {
			List<String> listSecurityGroupID = PacmanUtils.getDefaultSecurityGroupsByVpcId(vpcId, esSgURL, securityGroupName);
			securityGroupsSet.addAll(listSecurityGroupID);
			List<String> listUnrestrictedSecGroup = PacmanUtils.getUnrestrictedSecurityGroupsById(securityGroupsSet, esSgRulesURL, cidrIpv6, cidrIp);
			unrestrictedSecurityGroupsSet.addAll(listUnrestrictedSecGroup);

			if (!unrestrictedSecurityGroupsSet.isEmpty()) {
				annotation = Annotation.buildAnnotation(ruleParam, Annotation.Type.ISSUE);
				annotation.put(PacmanSdkConstants.DESCRIPTION, "VPC with default security group public access found!!");
				annotation.put(PacmanRuleConstants.SEVERITY, severity);
				annotation.put(PacmanRuleConstants.CATEGORY, category);
				annotation.put(PacmanRuleConstants.RESOURCE_ID, vpcId);
				issue.put(PacmanRuleConstants.VIOLATION_REASON, "VPC with default security group public access found!!");
				issueList.add(issue);
				annotation.put("issueDetails", issueList.toString());
				logger.debug("========VPCPublicAccessForDefaultSecurityGroup ended with an annotation {} : =========", annotation);
				return new RuleResult(PacmanSdkConstants.STATUS_FAILURE, PacmanRuleConstants.FAILURE_MESSAGE, annotation);
			}

		} catch (Exception exception) {
			logger.error("error: ", exception);
			throw new RuleExecutionFailedExeption(exception.getMessage());
		}
		logger.debug("========VPCPublicAccessForDefaultSecurityGroup ended=========");
		return new RuleResult(PacmanSdkConstants.STATUS_SUCCESS, PacmanRuleConstants.SUCCESS_MESSAGE);
	}

    @Override
    public String getHelpText() {
        return "checks entirely for vpc has public access for default security group";
    }

}
