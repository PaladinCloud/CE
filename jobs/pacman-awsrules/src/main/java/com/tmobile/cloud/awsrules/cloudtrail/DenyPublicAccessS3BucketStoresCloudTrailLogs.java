/*******************************************************************************
 * Copyright 2023 Paladin Cloud, Inc. or its affiliates. All Rights Reserved.
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
package com.tmobile.cloud.awsrules.cloudtrail;

import com.amazonaws.util.StringUtils;
import com.google.common.collect.HashMultimap;
import com.google.gson.*;
import com.nimbusds.oauth2.sdk.util.MapUtils;
import com.tmobile.cloud.awsrules.utils.PacmanUtils;
import com.tmobile.cloud.awsrules.utils.RulesElasticSearchRepositoryUtil;
import com.tmobile.cloud.constants.PacmanRuleConstants;
import com.tmobile.pacman.commons.PacmanSdkConstants;
import com.tmobile.pacman.commons.exception.InvalidInputException;
import com.tmobile.pacman.commons.exception.RuleExecutionFailedExeption;
import com.tmobile.pacman.commons.policy.Annotation;
import com.tmobile.pacman.commons.policy.BasePolicy;
import com.tmobile.pacman.commons.policy.PacmanPolicy;
import com.tmobile.pacman.commons.policy.PolicyResult;
import com.tmobile.pacman.commons.utils.CommonUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;

import java.util.*;

@PacmanPolicy(key = "deny-public-access-for-s3-bucket-that-stores-cloudtrail-logs", desc = "This rule checks S3 bucket that stores cloudtrail Logs should not be publicly accessible", severity = "critical", category = PacmanSdkConstants.SECURITY)
public class DenyPublicAccessS3BucketStoresCloudTrailLogs extends BasePolicy {
    private static final Logger logger = LoggerFactory.getLogger(DenyPublicAccessS3BucketStoresCloudTrailLogs.class);

    @Override
    public PolicyResult execute(final Map<String, String> ruleParam, Map<String, String> resourceAttributes) {
        logger.debug("Deny Public Access S3 Bucket Stores Cloud Trail Logs started");
        MDC.put(PacmanRuleConstants.EXECUTION_ID, ruleParam.get(PacmanRuleConstants.EXECUTION_ID));
        MDC.put(PacmanRuleConstants.RULE_ID, ruleParam.get(PacmanSdkConstants.POLICY_ID));

        String severity = ruleParam.get(PacmanRuleConstants.SEVERITY);
        String category = ruleParam.get(PacmanRuleConstants.CATEGORY);

        if (MapUtils.isNotEmpty(ruleParam) && !PacmanUtils.doesAllHaveValue(severity, category)) {
            logger.info(PacmanRuleConstants.MISSING_CONFIGURATION);
            throw new InvalidInputException(PacmanRuleConstants.MISSING_CONFIGURATION);
        }

        String s3BucketName = resourceAttributes.get("s3bucketname");
        if (!StringUtils.isNullOrEmpty(s3BucketName)) {
            String esUrl = CommonUtils.getEnvVariableValue(PacmanSdkConstants.ES_URI_ENV_VAR_NAME);
            if (!StringUtils.isNullOrEmpty(esUrl)) {
                esUrl = esUrl + "/aws/s3/_search";
            }
            String accountId = resourceAttributes.get(PacmanRuleConstants.ACCOUNTID);
            Map<String, Object> mustFilter = new HashMap<>();
            mustFilter.put(PacmanUtils.convertAttributetoKeyword(PacmanRuleConstants.NAME), s3BucketName);
            mustFilter.put(PacmanUtils.convertAttributetoKeyword(PacmanRuleConstants.ACCOUNTID), accountId);
            mustFilter.put(PacmanRuleConstants.LATEST, true);

            boolean isPublicAccessEnabled;
            try {
                isPublicAccessEnabled = isS3BucketStoringCloudtrailLogsPubliclyAccessible(esUrl, mustFilter);
            } catch (Exception e) {
                logger.error("unable to determine", e);
                throw new RuleExecutionFailedExeption("unable to determine" + e);
            }

            if (isPublicAccessEnabled) {
                List<LinkedHashMap<String, Object>> issueList = new ArrayList<>();
                LinkedHashMap<String, Object> issue = new LinkedHashMap<>();
                Annotation annotation = null;
                annotation = Annotation.buildAnnotation(ruleParam, Annotation.Type.ISSUE);
                annotation.put(PacmanSdkConstants.DESCRIPTION, "Ensure S3 bucket that stores cloudtrail Logs is not publicly accessible");
                annotation.put(PacmanRuleConstants.SEVERITY, severity);
                annotation.put(PacmanRuleConstants.CATEGORY, category);
                annotation.put(PacmanRuleConstants.NAME, resourceAttributes.get(PacmanRuleConstants.NAME));
                issue.put(PacmanRuleConstants.VIOLATION_REASON,
                        ruleParam.get(PacmanRuleConstants.RULE_ID) + " Violation Found!");
                issueList.add(issue);
                annotation.put(PacmanRuleConstants.ISSUE_DETAILS, issueList.toString());
                logger.debug("S3 bucket that stores cloudtrail Logs is publicly accessible");
                return new PolicyResult(PacmanSdkConstants.STATUS_FAILURE, PacmanRuleConstants.FAILURE_MESSAGE,
                        annotation);
            }
        }
        logger.debug("Deny Public Access S3 Bucket Stores Cloud Trail Logs");
        return new PolicyResult(PacmanSdkConstants.STATUS_SUCCESS, PacmanRuleConstants.SUCCESS_MESSAGE);
    }

    private boolean isS3BucketStoringCloudtrailLogsPubliclyAccessible(String esUrl, Map<String, Object> mustFilter) throws Exception {
        logger.debug("Validating the resource data from elastic search. ES URL:{}, FilterMap : {}", esUrl, mustFilter);
        boolean validationResult = false;
        JsonObject resultJson = RulesElasticSearchRepositoryUtil.getQueryDetailsFromES(esUrl, mustFilter,
                new HashMap<>(),
                HashMultimap.create(), null, 0, new HashMap<>(), null, null);

        if (resultJson.has(PacmanRuleConstants.HITS)) {
            JsonArray hitsJsonArray= resultJson.get(PacmanRuleConstants.HITS).getAsJsonObject().getAsJsonArray(PacmanRuleConstants.HITS);
            if (!hitsJsonArray.isEmpty()) {
                JsonObject source = (JsonObject) ((JsonObject) hitsJsonArray.get(0))
                        .get(PacmanRuleConstants.SOURCE);
                if (source.has(PacmanRuleConstants.ES_BKT_POLICY_ATTRIBUTE)) {
                    String bucketPolicyString = source.get(PacmanRuleConstants.ES_BKT_POLICY_ATTRIBUTE).getAsString();
                    JsonObject bucketPolicyObject = null;
                    try {
                        bucketPolicyObject = new Gson().fromJson(bucketPolicyString, JsonObject.class);
                    } catch (JsonSyntaxException e) {
                        logger.error("Error parsing bucket policy JSON: {}", e.getMessage());
                    }

                    if (bucketPolicyObject != null && bucketPolicyObject.has(PacmanRuleConstants.STATEMENT)) {
                        JsonArray bucketStatementList = bucketPolicyObject.getAsJsonArray(PacmanRuleConstants.STATEMENT);

                        for (JsonElement bucketStatement : bucketStatementList) {
                            JsonObject statementObject = bucketStatement.getAsJsonObject();

                            String effect = statementObject.get(PacmanRuleConstants.EFFECT).getAsString();
                            JsonObject principal = statementObject.getAsJsonObject(PacmanRuleConstants.PRINCIPAL);
                            String service = principal.get(PacmanRuleConstants.SERVICE).getAsString();

                            if (effect.equalsIgnoreCase("Allow") && service.equalsIgnoreCase("*")) {
                                validationResult = true;
                                break;
                            }
                        }
                    }
                }
            }
        }
        return validationResult;
    }

    @Override
    public String getHelpText() {
        return "Checks the S3 bucket that stores cloudtrail Logs should not be publicly accessible.";
    }
}
