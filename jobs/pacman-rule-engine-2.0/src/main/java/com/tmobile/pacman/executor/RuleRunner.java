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

package com.tmobile.pacman.executor;

import java.util.List;
import java.util.Map;

import com.tmobile.pacman.common.PacmanSdkConstants;
import com.tmobile.pacman.commons.rule.RuleResult;

// TODO: Auto-generated Javadoc
/**
 * The Interface RuleRunner.
 */
public interface RuleRunner {

    /**
     * Run rules.
     *
     * @param resources the resources
     * @param ruleParam the rule param
     * @param executionId the execution id
     * @return the list
     * @throws Exception the exception
     */
    public List<RuleResult> runRules(List<Map<String, String>> resources, Map<String, String> ruleParam,
            String executionId) throws Exception;
    default void populateAnnotationParams(RuleResult result,Map<String, String> resource, Map<String, String> ruleParam ){
        String assetGroup=ruleParam.get(PacmanSdkConstants.ASSET_GROUP_KEY);
        switch (assetGroup.toUpperCase()){
            case "AWS":
                result.getAnnotation().put(PacmanSdkConstants.ACCOUNT_ID, resource.get(PacmanSdkConstants.ACCOUNT_ID));
                break;
            case "AZURE":
                result.getAnnotation().put(PacmanSdkConstants.SUBSCRIPTION, resource.get(PacmanSdkConstants.SUBSCRIPTION));
                break;
            case "GCP":
                result.getAnnotation().put(PacmanSdkConstants.PROJECT_NAME, resource.get(PacmanSdkConstants.PROJECT_NAME));
                break;
        }
    }
}
