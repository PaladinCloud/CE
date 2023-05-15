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
package com.tmobile.cloud.awsrules.misc;

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
import com.tmobile.pacman.commons.policy.Annotation;
import com.tmobile.pacman.commons.policy.BasePolicy;
import com.tmobile.pacman.commons.policy.PacmanPolicy;
import com.tmobile.pacman.commons.policy.PolicyResult;

import static com.tmobile.cloud.awsrules.utils.PacmanUtils.AWS_SEARCH_INDEX;

@PacmanPolicy(key = "check-for-external-vpc-peering-connections", desc = "checks for external VPC peering connections", severity = PacmanSdkConstants.SEV_HIGH, category = PacmanSdkConstants.SECURITY)
public class ExternalVpcPeeringConnections extends BasePolicy {
    private static final Logger logger = LoggerFactory.getLogger(ExternalVpcPeeringConnections.class);

    /**
     * The method will get triggered from Rule Engine with following parameters
     * 
     * @param ruleParam
     * 
     * ************* Following are the Rule Parameters********* <br><br>
     * 
     * accountEsURL : The ES URL of the account <br><br>
     * 
     * ruleKey : check-for-ec2-public-access <br><br>
     * 
     * severity : Enter the value of severity <br><br>
     * 
     * ruleCategory : Enter the value of category <br><br>
     * 
     * threadsafe : if true , rule will be executed on multiple threads <br><br>
     * 
     * @param resourceAttributes this is a resource in context which needs to be scanned this is provided by execution engine
     *
     */

    @Override
	public PolicyResult execute(Map<String, String> ruleParam, Map<String, String> resourceAttributes) {

		logger.debug("========EC2WithExternalVpcPeeringConnections started=========");
		String accountEsURL = null;
		Annotation annotation = null;

		MDC.put("executionId", ruleParam.get("executionId"));
		MDC.put("ruleId", ruleParam.get(PacmanSdkConstants.POLICY_ID));

		Set<String> accountIdSet = new HashSet<>();
		LinkedHashMap<String, Object> issue = new LinkedHashMap<>();
		List<LinkedHashMap<String, Object>> issueList = new ArrayList<>();

		String severity = ruleParam.get(PacmanRuleConstants.SEVERITY);
		String category = ruleParam.get(PacmanRuleConstants.CATEGORY);
		String entityId = resourceAttributes.get("vpcpeeringconnectionid");
		String accepterOwnerId = resourceAttributes.get("acceptervpcownerid");
		String requesterOwnerId = resourceAttributes.get("requestervpcownerid");
		String pacmanHost = PacmanUtils.getPacmanHost(PacmanRuleConstants.ES_URI);
		logger.debug("========pacmanHost {}  =========", pacmanHost);

		if (!StringUtils.isNullOrEmpty(pacmanHost)) {
			accountEsURL = pacmanHost + AWS_SEARCH_INDEX;
		}
		logger.debug("========ec2SgEsURL URL after concatination param {}  =========", accountEsURL);

		if (!PacmanUtils.doesAllHaveValue(severity, category, accountEsURL)) {
			logger.info(PacmanRuleConstants.MISSING_CONFIGURATION);
			throw new InvalidInputException(PacmanRuleConstants.MISSING_CONFIGURATION);
		}

		try {
			int accountIdCount = PacmanUtils.getCountOfAccountIds(accountEsURL, requesterOwnerId, accepterOwnerId);
			if ((accepterOwnerId.equals(requesterOwnerId) && accountIdCount != 1) ||
					(!accepterOwnerId.equals(requesterOwnerId) && accountIdCount != 2)) {
				annotation = Annotation.buildAnnotation(ruleParam, Annotation.Type.ISSUE);
				annotation.put(PacmanSdkConstants.DESCRIPTION, "External vpc peering connection found");
				annotation.put(PacmanRuleConstants.SEVERITY, severity);
				annotation.put(PacmanRuleConstants.CATEGORY, category);
				annotation.put(PacmanRuleConstants.RESOURCE_ID, entityId);
				issue.put(PacmanRuleConstants.VIOLATION_REASON, "External vpc peering connection found");
				issueList.add(issue);
				annotation.put("issueDetails", issueList.toString());
				logger.debug("========EC2WithExternalVpcPeeringConnections ended with an annotation {} : =========", annotation);
				return new PolicyResult(PacmanSdkConstants.STATUS_FAILURE, PacmanRuleConstants.FAILURE_MESSAGE, annotation);
			}

		} catch (Exception exception) {
			logger.error("error: ", exception);
			throw new RuleExecutionFailedExeption(exception.getMessage());
		}
		logger.debug("========EC2WithExternalVpcPeeringConnections ended=========");
		return new PolicyResult(PacmanSdkConstants.STATUS_SUCCESS, PacmanRuleConstants.SUCCESS_MESSAGE);
	}

    @Override
    public String getHelpText() {
        return "Checks entirely for external VPC peering connections";
    }

}
