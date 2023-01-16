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

package com.tmobile.pacman.executor.rules;

import java.util.Map;

import com.tmobile.pacman.common.PacmanSdkConstants;
import com.tmobile.pacman.commons.policy.Annotation;
import com.tmobile.pacman.commons.policy.BasePolicy;
import com.tmobile.pacman.commons.policy.PacmanPolicy;
import com.tmobile.pacman.commons.policy.PolicyResult;

// TODO: Auto-generated Javadoc
/**
 * The Class TestPacRuleFailure.
 *
 * @author kkumar
 */

@PacmanPolicy(key="test_key_fail",category="security",desc="test rule",severity="high")
public class TestPacRuleFailure extends BasePolicy{

    /* (non-Javadoc)
     * @see com.tmobile.pacman.commons.rule.Rule#execute(java.util.Map, java.util.Map)
     */
    @Override
    public PolicyResult execute(Map<String, String> ruleParam, Map<String, String> resourceAttributes) {
        // TODO Auto-generated method stub
        return new PolicyResult(PacmanSdkConstants.STATUS_FAILURE, "test result",getAnnotation(ruleParam));
    }

    /**
     * Gets the annotation.
     *
     * @param ruleParam the rule param
     * @return the annotation
     */
    private Annotation getAnnotation(Map<String, String> ruleParam) {

        return Annotation.buildAnnotation(ruleParam, Annotation.Type.ISSUE);
    }
    /* (non-Javadoc)
     * @see com.tmobile.pacman.commons.rule.Rule#getHelpText()
     */
    @Override
    public String getHelpText() {
        // TODO Auto-generated method stub
        return "test rule";
    }

}
