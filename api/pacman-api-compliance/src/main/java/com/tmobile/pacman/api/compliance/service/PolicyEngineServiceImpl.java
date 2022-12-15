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
package com.tmobile.pacman.api.compliance.service;

import java.nio.ByteBuffer;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.services.lambda.AWSLambdaAsyncClient;
import com.amazonaws.services.lambda.model.InvokeRequest;
import com.amazonaws.services.lambda.model.InvokeResult;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.tmobile.pacman.api.commons.Constants;
import com.tmobile.pacman.api.commons.exception.ServiceException;
import com.tmobile.pacman.api.compliance.repository.PacPolicyEngineAutofixActionsRepository;
import com.tmobile.pacman.api.compliance.repository.model.PacPolicyEngineAutofixActions;
import com.tmobile.pacman.api.compliance.repository.model.PolicyTable;
import com.tmobile.pacman.api.compliance.util.CommonUtil;

/**
 * The Class RuleEngineServiceImpl.
 */
@Service
public class PolicyEngineServiceImpl implements PolicyEngineService, Constants {
    
    /** The log. */
    private final Logger log = LoggerFactory.getLogger(getClass());

    /** The policy lambda function name. */
    @Value("${policy-engine.invoke.url}")
    private String policyLambdaFunctionName;
    
    /** The policy aws access key. */
    private String policyAwsAccessKey = "pacman.policy.access.keyA";
    
    /** The policy aws secret key. */
    private String policyAwsSecretKey = "pacman.policy.secret.keyA";
    
    /** The additional params. */
    private String additionalParams = "additionalParams";

    /** The system config service. */
    @Autowired
    private SystemConfigurationService systemConfigService;

    /** The policy table service. */
    @Autowired
    private PolicyTableService policyTableService;

    /** The rule engine autofix repository. */
    @Autowired
    private PacPolicyEngineAutofixActionsRepository policyEngineAutofixRepository;

    /* (non-Javadoc)
     * @see com.tmobile.pacman.api.compliance.service.PolicyEngineService#runPolicy(java.lang.String, java.util.Map)
     */
    @Override
    public void runPolicy(final String policyId, Map<String, String> runTimeParams)
            throws ServiceException {
        Boolean isPoicyInvocationSuccess = invokePolicy(policyId, runTimeParams);
        if (!isPoicyInvocationSuccess) {
            throw new ServiceException("Poilcy Invocation Failed");
        }
    }

    /* (non-Javadoc)
     * @see com.tmobile.pacman.api.compliance.service.RuleEngineService#getLastAction(java.lang.String)
     */
    @Override
    public Map<String, Object> getLastAction(final String resourceId) {
        Map<String, Object> response = Maps.newHashMap();
        SimpleDateFormat dateFormatUTC = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ");
    	dateFormatUTC.setTimeZone(TimeZone.getTimeZone("UTC"));
        try {
            List<String> lastActions = Lists.newArrayList();
            List<PacPolicyEngineAutofixActions> pacRuleEngineAutofixActions = policyEngineAutofixRepository
                    .findLastActionByResourceId(resourceId);
            pacRuleEngineAutofixActions.forEach(autofixLastAction -> {
			lastActions.add(dateFormatUTC.format(autofixLastAction.getLastActionTime()));
            });
            if (lastActions.isEmpty()) {
                response.put(RESPONSE_CODE, 0);
                response.put(LAST_ACTIONS, Lists.newArrayList());
                response.put(MESSAGE_KEY, "Last action not found!!!");
            } else {
                response.put(RESPONSE_CODE, 1);
                response.put(MESSAGE_KEY, "Last action found!!!");
                response.put(LAST_ACTIONS, lastActions);
            }
        } catch (Exception e) {
            response.put(RESPONSE_CODE, 0);
            response.put(LAST_ACTIONS, Lists.newArrayList());
            response.put(MESSAGE_KEY, "Unexpected error occurred!!!");
        }
        return response;
    }

    /* (non-Javadoc)
     * @see com.tmobile.pacman.api.compliance.service.RuleEngineService#postAction(java.lang.String, java.lang.String)
     */
    @Override
    public void postAction(final String resourceId, final String action)
            throws ServiceException {
    	SimpleDateFormat dateFormatUTC = new SimpleDateFormat("yyyy-MMM-dd HH:mm:ss");
    	dateFormatUTC.setTimeZone(TimeZone.getTimeZone("UTC"));
        PacPolicyEngineAutofixActions autofixActions = new PacPolicyEngineAutofixActions();
        autofixActions.setAction(action);
        autofixActions.setResourceId(resourceId);
        try {
			autofixActions.setLastActionTime(dateFormatUTC.parse(dateFormatUTC.format(new Date())));
		} catch (ParseException e) {
			throw new ServiceException("error parsing date");
		}
        policyEngineAutofixRepository.save(autofixActions);
    }

    /**
     * Invoke Policy.
     *
     * @param policyId the policy id
     * @param runTimeParams the run time params
     * @return true, if successful
     */
    @SuppressWarnings("unchecked")
    private boolean invokePolicy(final String policyId,
            Map<String, String> runTimeParams) {
        PolicyTable policyInstance = policyTableService
                .getPolicyTableByPolicyId(policyId);
        String policyParams = policyInstance.getPolicyParams();
        Map<String, Object> policyParamDetails = (Map<String, Object>) CommonUtil
                .deSerializeToObject(policyParams);
        if (runTimeParams != null) {
            policyParamDetails.put(additionalParams,
                    formatAdditionalParameters(runTimeParams));
        }
        policyParams = CommonUtil.serializeToString(policyParamDetails);
        AWSLambdaAsyncClient awsLambdaClient = getAWSLambdaAsyncClient();
        InvokeRequest invokeRequest = new InvokeRequest().withFunctionName(
                policyLambdaFunctionName).withPayload(
                ByteBuffer.wrap(policyParams.getBytes()));
        InvokeResult invokeResult = awsLambdaClient.invoke(invokeRequest);
        if (invokeResult.getStatusCode() == TWO_HUNDRED) {
            ByteBuffer responsePayload = invokeResult.getPayload();
            log.debug("Return Value :" + new String(responsePayload.array()));
            return true;
        } else {
            log.error("Received a non-OK response from AWS: "
                    + invokeResult.getStatusCode());
            return false;
        }
    }

    /**
     * Format additional parameters.
     *
     * @param runTimeParams the run time params
     * @return the list
     */
    private List<Map<String, Object>> formatAdditionalParameters(
            Map<String, String> runTimeParams) {
        List<Map<String, Object>> additionalParamsList = Lists.newArrayList();
        runTimeParams.forEach((key, value) -> {
            Map<String, Object> additionalParam = Maps.newHashMap();
            additionalParam.put("key", key);
            additionalParam.put("value", value);
            additionalParam.put("encrypt", false);
            additionalParamsList.add(additionalParam);
        });
        return additionalParamsList;
    }

    /**
     * Gets the AWS lambda async client.
     *
     * @return the AWS lambda async client
     */
    @SuppressWarnings("deprecation")
    public AWSLambdaAsyncClient getAWSLambdaAsyncClient() {
        BasicAWSCredentials creds = new BasicAWSCredentials(
                systemConfigService.getConfigValue(policyAwsAccessKey),
                systemConfigService.getConfigValue(policyAwsSecretKey));
        return new AWSLambdaAsyncClient(creds);
    }
}
