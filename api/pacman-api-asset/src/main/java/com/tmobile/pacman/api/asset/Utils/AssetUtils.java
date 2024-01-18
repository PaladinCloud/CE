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
package com.tmobile.pacman.api.asset.Utils;

import com.tmobile.pacman.api.asset.AssetConstants;
import com.tmobile.pacman.api.commons.Constants;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import java.util.*;
import java.util.stream.Collectors;

import static com.tmobile.pacman.api.commons.Constants.EC2;

@Component
public class AssetUtils {

    static String CLOUD_QUALYS_POLICY = "Ec2InstanceScannedByQualys_version-1";
    static String CLOUD_QUALYS_POLICY_RULES = "Ec2WithSeverityVulnerability_version-1";

    private static List<String> statesForEc2 = Arrays.asList(Constants.RUNNING, AssetConstants.STOPPED, AssetConstants.STOPPING);

    public static String fetchInputValFromFilter(Object obj) {
        String filterExempted;
        if (obj instanceof String) {
           return (String) obj;
        } else if (obj instanceof List) {
            List<String> filterExemptedList = (List<String>) obj;
            return (!CollectionUtils.isEmpty(filterExemptedList) && filterExemptedList.size() == 1) ? filterExemptedList.get(0) : "";
        } else {
            return  "";
        }
    }

    /**
     * Fetches the intersection of the two input resource ids provided
     * if there is already an existing set of resource ids to be filtered we
     * need tp find the intersection of the two, if not we can return the new set of resource ids
     * if no common resource id exist, then return empty list signifying no data present for both the filter applied     *
     *
     * @param existingMust the existing set of resource ids that need to be filteres
     * @param newAdd       the next set of resource ids that needs to be applied as part of filter     *
     * @return list common resource ids.
     */
    public static List<String> getMustResourceIdsForAssets(List<String> existingMust, List<String> newAdd) {
        if (CollectionUtils.isEmpty(newAdd)) {
            return new ArrayList<>();
        } else if (CollectionUtils.isEmpty(existingMust)) {
            existingMust.addAll(newAdd);
        } else {
            existingMust.retainAll(newAdd);
            if (existingMust.isEmpty()) {
                return new ArrayList<>();
            }
        }
        return existingMust;
    }

    /**
     * Creates an OR relation in between two parts of es query, one for the asset type ec2 if present in the filter or input
     * and the other part for rest of the asset type
     * this is done to apply the state filter for ec2 asset types only     *
     *
     * @param targetTypes   target type
     * @param policyIds     input filter policyId
     * @param resourceIds   target type
     * @param qualysEnabled boolean
     * @return OR object for asset type ec2 and rest.
     */
    public static Map<String, Object> createShouldObjectForAssetTypeESQuery(List<String> targetTypes, List<String> policyIds, List<String> resourceIds, boolean qualysEnabled) {
        List<String> entityTypes = targetTypes.stream().filter(x -> !x.equalsIgnoreCase(EC2)).collect(Collectors.toList());
        boolean isEc2 = targetTypes.stream().anyMatch(x -> x.equalsIgnoreCase(EC2));
        Map<String, Object> entityType = new HashMap<>();
        entityType.put(AssetConstants.UNDERSCORE_ENTITY_TYPE_KEYWORD, entityTypes);
        Map<String, Object> terms = new HashMap<>();
        terms.put("terms", entityType);
        List<Map<String, Object>> mustList = new ArrayList<>();
        mustList.add(terms);
        Map<String, Object> mustObj = new HashMap<>();
        mustObj.put("must", mustList);
        Map<String, Object> boolObj = new HashMap<>();
        boolObj.put("bool", mustObj);
        List<Map<String, Object>> shouldList = new ArrayList<>();
        if (!CollectionUtils.isEmpty(entityTypes)) {
            shouldList.add(boolObj);
        }
        if (isEc2) {
            boolObj = new HashMap<>();
            List<Map<String, Object>> stateShouldList = new ArrayList<>();
            for (String stateEc2 : statesForEc2) {
                Map<String, Object> term = new HashMap<>();
                Map<String, Object> state = new HashMap<>();
                state.put(Constants.STATE_NAME, stateEc2);
                term.put("term", state);
                stateShouldList.add(term);
            }
            Map<String, Object> term = new HashMap<>();
            entityType = new HashMap<>();
            entityType.put(AssetConstants.UNDERSCORE_ENTITY_TYPE_KEYWORD, EC2);
            term.put("term", entityType);
            mustList = new ArrayList<>();
            mustList.add(term);
            if (!policyIds.isEmpty() && (isQualysPolicy(policyIds, qualysEnabled) || policyIds.contains(Constants.SSM_AGENT_RULE))) {
                term = new HashMap<>();
                Map<String, Object> resourceIdsMap = new HashMap<>();
                resourceIdsMap.put("_docid.keyword", resourceIds);
                term.put("terms", resourceIdsMap);
                mustList.add(term);
            }
            boolObj.put("should", stateShouldList);
            boolObj.put("minimum_should_match", "1");
            boolObj.put("must", mustList);
            Map<String, Object> bool = new HashMap<>();
            bool.put("bool", boolObj);
            shouldList.add(bool);
        }
        Map<String, Object> shouldObject = new HashMap<>();
        shouldObject.put("should", shouldList);
        return shouldObject;
    }

    public static boolean isQualysPolicy(List<String> policyId, boolean qualysEnabled) {
        return qualysEnabled && (policyId.stream().anyMatch( policy -> policy.contains(CLOUD_QUALYS_POLICY) || policy.contains(CLOUD_QUALYS_POLICY_RULES)));
    }

}
