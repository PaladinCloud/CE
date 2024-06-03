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
package com.tmobile.cloud.awsrules.ec2;

import com.amazonaws.util.CollectionUtils;
import com.amazonaws.util.StringUtils;
import com.google.gson.JsonObject;
import com.tmobile.cloud.awsrules.utils.PacmanUtils;
import com.tmobile.cloud.constants.PacmanRuleConstants;
import com.tmobile.pacman.commons.PacmanSdkConstants;
import com.tmobile.pacman.commons.constants.PluginDisplayName;
import com.tmobile.pacman.commons.exception.InvalidInputException;
import com.tmobile.pacman.commons.exception.RuleExecutionFailedExeption;
import com.tmobile.pacman.commons.policy.Annotation;
import com.tmobile.pacman.commons.policy.BasePolicy;
import com.tmobile.pacman.commons.policy.PacmanPolicy;
import com.tmobile.pacman.commons.policy.PolicyResult;
import org.apache.commons.lang3.ObjectUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;

import java.util.List;
import java.util.Map;

@PacmanPolicy(key = "check-asset-scanned-by-source", desc = "checks for Assets scanned by source", severity = PacmanSdkConstants.SEV_HIGH, category = PacmanSdkConstants.GOVERNANCE)
public class AssetTypeScannedBySourceRule extends BasePolicy {

    private static final Logger logger = LoggerFactory.getLogger(AssetTypeScannedBySourceRule.class);
    private static final String VULN_ASSET_LOOKUP_KEY = "asset_lookup_key";
    private static final String SRC_ASSET_KEY = "src_asset_key";
    private static final String ASSET_LOOKUP_INDEX = "asset_lookup_index";

    /**
     * The method will get triggered from Rule Engine with following parameters.
     *
     * @param policyParam        ************* Following are the Rule Parameters********* <br><br>
     *                           <p>
     *                           ruleKey : check-asset-scanned-by-source <br><br>
     *                           <p>
     *                           target : Enter the target days <br><br>
     *                           <p>
     *                           discoveredDaysRange : Enter the discovered days Range <br><br>
     *                           <p>
     *                           vulnIndex : Enter the Plugin URL <br><br>
     * @param resourceAttributes this is a resource in context which needs to be scanned this is provided by execution engine
     * @return the rule result
     */
    @Override
    public PolicyResult execute(Map<String, String> policyParam, Map<String, String> resourceAttributes) {
        logger.debug("AssetTypeScannedBySourceRule execution started ");
        MDC.put("executionId", policyParam.get("executionId"));
        MDC.put("policyId", policyParam.get(PacmanSdkConstants.POLICY_ID));
        String category = policyParam.get(PacmanRuleConstants.CATEGORY);
        String severity = policyParam.get(PacmanRuleConstants.SEVERITY);
        String assetLookupIndex = policyParam.get(ASSET_LOOKUP_INDEX);
        /* this param determines the asset key in source index. for eg. for azure_virtualmachine asset key is vmId  */
        String srcAssetKey = policyParam.get(SRC_ASSET_KEY);
        String vulnAssetLookupKey = policyParam.get(VULN_ASSET_LOOKUP_KEY);
        String vulnerabilitiesEndpoint = PacmanUtils.getPacmanHost(PacmanRuleConstants.ES_URI) + "/" + assetLookupIndex + "/_search";
        if (!PacmanUtils.doesAllHaveValue(category, severity)) {
            logger.info(PacmanRuleConstants.MISSING_CONFIGURATION);
            throw new InvalidInputException(PacmanRuleConstants.MISSING_CONFIGURATION);
        }
        PolicyResult policyResult = null;
        if (resourceAttributes != null) {
            String instanceId = StringUtils.trim(ObjectUtils.firstNonNull(resourceAttributes.get(srcAssetKey), resourceAttributes.get(PacmanRuleConstants.RESOURCE_ID)));
            List<JsonObject> vulAssetList;
            try {
                vulAssetList = PacmanUtils.matchAssetAgainstSourceVulnIndex(instanceId, vulnerabilitiesEndpoint, vulnAssetLookupKey, null);
            } catch (Exception e) {
                logger.error("unable to determine", e);
                throw new RuleExecutionFailedExeption("unable to determine" + e);
            }

            if (CollectionUtils.isNullOrEmpty(vulAssetList)) {
                Annotation annotation = Annotation.buildAnnotation(policyParam, Annotation.Type.ISSUE);
                annotation.put(PacmanSdkConstants.DESCRIPTION, policyParam.get("targetTypeDisplayName") + " is not been scanned by " + PluginDisplayName.getDisplayNameByString(policyParam.get("pluginName")));
                annotation.put(PacmanRuleConstants.SEVERITY, severity);
                annotation.put(PacmanRuleConstants.CATEGORY, category);
                policyResult = new PolicyResult(PacmanSdkConstants.STATUS_FAILURE, PacmanRuleConstants.FAILURE_MESSAGE, annotation);

            }

            if (policyResult == null) {
                policyResult = new PolicyResult(PacmanSdkConstants.STATUS_SUCCESS, PacmanRuleConstants.SUCCESS_MESSAGE);
            }
        }
        return policyResult;
    }

    @Override
    public String getHelpText() {
        return null;
    }
}
