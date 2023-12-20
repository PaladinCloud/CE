/*******************************************************************************
 *Copyright <2023> Paladin Cloud, Inc or its affiliates. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); You may not use
 * this file except in compliance with the License. A copy of the License is located at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * or in the "license" file accompanying this file. This file is distributed on
 * an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, express or
 * implied. See the License for the specific language governing permissions and
 * limitations under the License.
 ******************************************************************************/
package com.tmobile.cloud.awsrules.cloudtrail;

import com.nimbusds.oauth2.sdk.util.MapUtils;
import com.tmobile.cloud.awsrules.utils.PacmanUtils;
import com.tmobile.cloud.constants.PacmanRuleConstants;
import com.tmobile.pacman.commons.PacmanSdkConstants;
import com.tmobile.pacman.commons.exception.InvalidInputException;
import com.tmobile.pacman.commons.policy.Annotation;
import com.tmobile.pacman.commons.policy.BasePolicy;
import com.tmobile.pacman.commons.policy.PacmanPolicy;
import com.tmobile.pacman.commons.policy.PolicyResult;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import java.util.*;

@PacmanPolicy(key = "enable-cloudtrail-cloudwatch-logs-integration", desc = "This rule checks that CloudTrail trails are integrated with CloudWatch Logs", severity = "critical", category = PacmanSdkConstants.SECURITY)
public class EnableCloudTrailCloudwatchLogsIntegration extends BasePolicy {

    private static final Logger logger = LoggerFactory.getLogger(EnableCloudTrailCloudwatchLogsIntegration.class);

    @Override
    public PolicyResult execute(final Map<String, String> ruleParam, Map<String, String> resourceAttributes) {
        Annotation annotation = null;
        MDC.put(PacmanRuleConstants.EXECUTION_ID, ruleParam.get(PacmanRuleConstants.EXECUTION_ID));
        MDC.put(PacmanRuleConstants.RULE_ID, ruleParam.get(PacmanSdkConstants.POLICY_ID));

        String severity = ruleParam.get(PacmanRuleConstants.SEVERITY);
        String category = ruleParam.get(PacmanRuleConstants.CATEGORY);

        String cloudWatchLogsLogGroupArn = resourceAttributes.get(PacmanRuleConstants.CLOUD_WATCH_LOGS_ARN);

        List<LinkedHashMap<String, Object>> issueList = new ArrayList<>();
        LinkedHashMap<String, Object> issue = new LinkedHashMap<>();

        if (MapUtils.isNotEmpty(ruleParam) && !PacmanUtils.doesAllHaveValue(severity, category)) {
            logger.info(PacmanRuleConstants.MISSING_CONFIGURATION);
            throw new InvalidInputException(PacmanRuleConstants.MISSING_CONFIGURATION);
        }

        if (StringUtils.isEmpty(cloudWatchLogsLogGroupArn)) {
            annotation = Annotation.buildAnnotation(ruleParam, Annotation.Type.ISSUE);
            annotation.put(PacmanSdkConstants.DESCRIPTION, "This rule checks that CloudTrail trails are integrated with CloudWatch Logs.");
            annotation.put(PacmanRuleConstants.SEVERITY, severity);
            annotation.put(PacmanRuleConstants.CATEGORY, category);
            issue.put(PacmanRuleConstants.VIOLATION_REASON, "CloudTrail trails are not integrated with cloudwatch Logs.");
            issueList.add(issue);
            annotation.put(PacmanRuleConstants.ISSUE_DETAILS, issueList.toString());
            return new PolicyResult(PacmanSdkConstants.STATUS_FAILURE, PacmanRuleConstants.FAILURE_MESSAGE, annotation);
        }
        logger.debug("CloudTrail trails are integrated with cloudwatch Logs.");
        return new PolicyResult(PacmanSdkConstants.STATUS_SUCCESS, PacmanRuleConstants.SUCCESS_MESSAGE);
    }

    @Override
    public String getHelpText() {
        return "This rule checks if CloudTrail trails are integrated with cloudwatch Logs.";
    }
}


