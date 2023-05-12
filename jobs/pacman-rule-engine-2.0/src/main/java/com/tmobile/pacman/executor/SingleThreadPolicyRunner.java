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

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import com.google.common.base.Strings;
import com.tmobile.cloud.constants.PacmanRuleConstants;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.MultiThreadedHttpConnectionManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Joiner;
import com.tmobile.pacman.common.PacmanSdkConstants;
import com.tmobile.pacman.commons.policy.Annotation;
import com.tmobile.pacman.commons.policy.PacmanPolicy;
import com.tmobile.pacman.commons.policy.PolicyResult;
import com.tmobile.pacman.util.ReflectionUtils;
import com.tmobile.pacman.util.PolicyExecutionUtils;

// TODO: Auto-generated Javadoc
/**
 * The Class SingleThreadRuleRunner.
 */
public class SingleThreadPolicyRunner implements PolicyRunner {

    /** The Constant logger. */
    private static final Logger logger = LoggerFactory.getLogger(SingleThreadPolicyRunner.class);

    /* (non-Javadoc)
     * @see com.tmobile.pacman.executor.RuleRunner#runRules(java.util.List, java.util.Map, java.lang.String)
     */
    @SuppressWarnings("nls")
    @Override
    public List<PolicyResult> runPolicies(List<Map<String, String>> resources, Map<String, String> policyParam,
            String executionId) throws Exception {
        String policyKey = null;
        Class<?> policyClass = null;
        Object policyObject = null;
        Method executeMethod = null;
        List<PolicyResult> evaluations = new ArrayList<PolicyResult>();
        HttpClient httpClient = new HttpClient(new MultiThreadedHttpConnectionManager());
        if (!PacmanSdkConstants.POLICY_TYPE_SERVERLESS.equals(policyParam.get(PacmanSdkConstants.POLICY_TYPE))) {
            try {
                policyKey = policyParam.get(PacmanSdkConstants.POLICY_KEY);
                policyClass = ReflectionUtils.findAssociateClass(policyKey);
                policyObject = policyClass.newInstance();
                // executeMethod =
                // ReflectionUtils.findEntryMethod(ruleObject,PacmanExecute.class);
                executeMethod = ReflectionUtils.findAssociatedMethod(policyObject, "execute");
            } catch (Exception e) {
                logger.error("Please check the rule class complies to implemetation contract, rule key=" + policyKey, e);
                throw e;
            }
        }
        logger.info(
                "----------------------------------------------------scan start------------------------------------------------------------------");

        httpClient = new HttpClient(new MultiThreadedHttpConnectionManager()); // create
                                                                               // this
                                                                               // outside
                                                                               // the
                                                                               // loop
                                                                               // below

        for (Map<String, String> resource : resources) {
            try {
                Map<String, String> localPolicyParam = PolicyExecutionUtils.getLocalPolicyParam(policyParam, resource);
                logger.debug("Resource-->: " + Joiner.on("#").withKeyValueSeparator("=").join(resource));
                PolicyResult result = null;
                // RuleResult result =
                // (RuleResult)executeMethod.invoke(ruleObject,
                // Collections.unmodifiableMap(ruleParam),null); // let rule not
                // allow modify input
                PacmanPolicy policyAnnotation = null;
                if (PacmanSdkConstants.POLICY_TYPE_SERVERLESS.equals(localPolicyParam.get(PacmanSdkConstants.POLICY_TYPE))) {
                    result = new ServerlessPolicyHandler(httpClient).handlePolicy(policyParam, resource);
                } else {
                    try {
                        result = (PolicyResult) executeMethod.invoke(policyObject,
                                Collections.unmodifiableMap(localPolicyParam), Collections.unmodifiableMap(resource)); // let
                                                                                                                     // rule
                                                                                                                     // not
                                                                                                                     // allow
                                                                                                                     // modify
                                                                                                                     // input
                    } catch (Exception e) {
                        // in case not able to evaluate the result :
                        // RuleExecutor class will detect this by taking the
                        // delta between resource in and result out
                        logger.error(String.format("unable to evaluvate for this resource %s" , resource), e); // this will be handled as missing evaluation at RuleEcecutor
                    }
                    policyAnnotation = policyClass.getAnnotation(PacmanPolicy.class);
                }
                // if fail issue will get logged to database, hence update the
                // category and severity
                String tagsMandatory = policyParam.get(PacmanSdkConstants.TAGGING_MANDATORY_TAGS);
                Map<String, String> mandatoryTag = getMandatoryTagsForAnnotation(tagsMandatory,resource);
                if (result!= null && (PacmanSdkConstants.STATUS_FAILURE.equalsIgnoreCase(result.getStatus())
                        || PacmanSdkConstants.STATUS_UNKNOWN.equalsIgnoreCase(result.getStatus()))) {
                    if (policyParam.containsKey(PacmanSdkConstants.INVOCATION_ID)) {
                        result.getAnnotation().put(PacmanSdkConstants.INVOCATION_ID,
                                policyParam.get(PacmanSdkConstants.INVOCATION_ID));
                    }
                    result.getAnnotation().put(PacmanSdkConstants.RESOURCE_ID,
                            resource.get(PacmanSdkConstants.RESOURCE_ID_COL_NAME_FROM_ES));
                    populateAnnotationParams(result,resource,policyParam);
                    result.getAnnotation().put(PacmanSdkConstants.REGION, resource.get("region"));
                    result.getAnnotation().put(PacmanSdkConstants.POLICY_CATEGORY, PolicyExecutionUtils
                            .getPolicyAttribute(result, policyParam, policyAnnotation, PacmanSdkConstants.POLICY_CATEGORY));
                    result.getAnnotation().put(PacmanSdkConstants.POLICY_SEVERITY, PolicyExecutionUtils
                            .getPolicyAttribute(result, policyParam, policyAnnotation, PacmanSdkConstants.POLICY_SEVERITY));
                    result.getAnnotation().put(PacmanSdkConstants.TARGET_TYPE,
                            policyParam.get(PacmanSdkConstants.TARGET_TYPE));
                    result.getAnnotation().put(PacmanSdkConstants.DOC_ID, resource.get(PacmanSdkConstants.DOC_ID));
                    result.getAnnotation().put(PacmanSdkConstants.EXECUTION_ID, executionId);
                    result.getAnnotation().put(PacmanSdkConstants.ACCOUNT_NAME, resource.get(PacmanRuleConstants.ACCOUNT_NAME));
                    result.getAnnotation().put(PacmanRuleConstants.ACCOUNTID,resource.get(PacmanRuleConstants.ACCOUNTID));
                    mandatoryTag.forEach(result.getAnnotation()::putIfAbsent);
                }
                else {
                            Annotation annotation = Annotation.buildAnnotation(policyParam, Annotation.Type.ISSUE);
                            annotation.put(PacmanSdkConstants.DATA_SOURCE_KEY,
                                    policyParam.get(PacmanSdkConstants.DATA_SOURCE_KEY));
                            annotation.put(PacmanSdkConstants.TARGET_TYPE, policyParam.get(PacmanSdkConstants.TARGET_TYPE));
                            if(null!=result){
                                annotation.put(PacmanSdkConstants.REASON_TO_CLOSE_KEY, result.getDesc());
                            }
                            annotation.put(PacmanSdkConstants.POLICY_ID, policyParam.get(PacmanSdkConstants.POLICY_ID));
                            if (policyParam.containsKey(PacmanSdkConstants.INVOCATION_ID)) {
                                annotation.put(PacmanSdkConstants.INVOCATION_ID,
                                        policyParam.get(PacmanSdkConstants.INVOCATION_ID));
                            }
                            annotation.put(PacmanSdkConstants.RESOURCE_ID,
                                    resource.get(PacmanSdkConstants.RESOURCE_ID_COL_NAME_FROM_ES));
                            annotation.put(PacmanSdkConstants.ACCOUNT_ID, resource.get(PacmanRuleConstants.ACCOUNTID));
                            annotation.put(PacmanSdkConstants.ACCOUNT_NAME, resource.get(PacmanRuleConstants.ACCOUNT_NAME));
                            annotation.put(PacmanSdkConstants.DOC_ID, resource.get(PacmanSdkConstants.DOC_ID)); // this is important to close the issue
                            mandatoryTag.forEach(annotation::putIfAbsent);
                            if(null!=result){
                                result.setAnnotation(annotation);
                            }else{
                                continue;
                            }
                }
                evaluations.add(result);
            } catch (Exception e) {
                logger.debug("rule execution for resource " + resource.get("id") + " failed due to " + e.getMessage(),
                        e);
            }
        }
        logger.info(
                "----------------------------------------------------scan complete------------------------------------------------------------------");
        return evaluations;
    }

}
