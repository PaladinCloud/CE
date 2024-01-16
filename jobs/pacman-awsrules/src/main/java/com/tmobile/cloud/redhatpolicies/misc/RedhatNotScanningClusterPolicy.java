/*******************************************************************************
 *  Copyright 2023 Paladin Cloud, Inc. or its affiliates. All Rights Reserved.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License"); you may not
 *  use this file except in compliance with the License.  You may obtain a copy
 *  of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 *  WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  See the
 *  License for the specific language governing permissions and limitations under
 *  the License.
 ******************************************************************************/
package com.tmobile.cloud.redhatpolicies.misc;

import com.google.common.collect.HashMultimap;
import com.nimbusds.oauth2.sdk.util.MapUtils;
import com.tmobile.cloud.awsrules.utils.PacmanUtils;
import com.tmobile.cloud.constants.PacmanRuleConstants;
import com.tmobile.pacman.commons.PacmanSdkConstants;
import com.tmobile.pacman.commons.exception.InvalidInputException;
import com.tmobile.pacman.commons.exception.RuleExecutionFailedExeption;
import com.tmobile.pacman.commons.policy.Annotation;
import com.tmobile.pacman.commons.policy.BasePolicy;
import com.tmobile.pacman.commons.policy.PacmanPolicy;
import com.tmobile.pacman.commons.policy.PolicyResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

@PacmanPolicy(key = "redhat-not-scanning-clusters", desc = "Checks for the presence of vulnerabilities.", severity = PacmanSdkConstants.SEV_HIGH, category = PacmanSdkConstants.SECURITY)
public class RedhatNotScanningClusterPolicy extends BasePolicy {

    private static final Logger logger = LoggerFactory.getLogger(RedhatNotScanningClusterPolicy.class);

    private static final String REDHAT_CLUSTER_URL = "/redhat_cluster/_search";

    @Override
    public PolicyResult execute(Map<String, String> policyParam, Map<String, String> resourceAttributes) {
        logger.debug("RedhatNotScanningClusterPolicy started");
        if (MapUtils.isEmpty(policyParam) || MapUtils.isEmpty(resourceAttributes) || (MapUtils.isNotEmpty(policyParam)
                && !PacmanUtils.doesAllHaveValue(policyParam.get(PacmanRuleConstants.SEVERITY),
                policyParam.get(PacmanRuleConstants.CATEGORY)))) {
            logger.info(PacmanRuleConstants.MISSING_CONFIGURATION);
            throw new InvalidInputException(PacmanRuleConstants.MISSING_CONFIGURATION);
        }
        String policyId = policyParam.get(PacmanSdkConstants.POLICY_ID);
        String severity = policyParam.get(PacmanRuleConstants.SEVERITY);
        String category = policyParam.get(PacmanRuleConstants.CATEGORY);
        MDC.put("executionId", policyParam.get("executionId"));
        MDC.put("policyId", policyId);
        try {
            String pacmanHost = PacmanUtils.getPacmanHost(PacmanRuleConstants.ES_URI);
            String clusterName = resourceAttributes.get("name");
            String region = resourceAttributes.get("region");
            String accountId = resourceAttributes.get(PacmanRuleConstants.ACCOUNTID);
            Map<String, Object> mustFilter = new HashMap<>();
            mustFilter.put("status.providerMetadata.google.clusterName", clusterName);
            mustFilter.put("status.providerMetadata.zone", region);
            mustFilter.put("status.providerMetadata.google.project", accountId);
            HashMultimap<String, Object> shouldFilter = HashMultimap.create();
            Map<String, Object> mustTermsFilter = new HashMap<>();
            String endPoint = pacmanHost + REDHAT_CLUSTER_URL;
            Set<String> resourceName = PacmanUtils.getValueFromElasticSearchAsSet(endPoint, mustFilter, shouldFilter,
                    mustTermsFilter, "_resourcename", null);
            if (resourceName == null || resourceName.isEmpty()) {
                List<LinkedHashMap<String, Object>> issueList = new ArrayList<>();
                LinkedHashMap<String, Object> issue = new LinkedHashMap<>();
                Annotation annotation = Annotation.buildAnnotation(policyParam, Annotation.Type.ISSUE);
                annotation.put(PacmanSdkConstants.DESCRIPTION, "Cluster - " + clusterName + " not scanned by redhat acs found");
                annotation.put(PacmanRuleConstants.SEVERITY, severity);
                annotation.put(PacmanRuleConstants.CATEGORY, category);
                issue.put(PacmanRuleConstants.VIOLATION_REASON, "Cluster - " + clusterName + " not scanned by redhat acs found");
                issueList.add(issue);
                annotation.put("issueDetails", issueList.toString());
                logger.debug("========RedhatNotScanningClusterPolicy ended with annotation {} : =========", annotation);
                return new PolicyResult(PacmanSdkConstants.STATUS_FAILURE, PacmanRuleConstants.FAILURE_MESSAGE, annotation);
            }
        } catch (Exception e) {
            logger.error("Unable to scan cluster", e);
            throw new RuleExecutionFailedExeption(e.getMessage());
        }
        logger.debug("========RedhatNotScanningClusterPolicy ended=========");
        return new PolicyResult(PacmanSdkConstants.STATUS_SUCCESS, PacmanRuleConstants.SUCCESS_MESSAGE);
    }

    @Override
    public String getHelpText() {
        return "Checks for the presence of vulnerabilities.";
    }
}
