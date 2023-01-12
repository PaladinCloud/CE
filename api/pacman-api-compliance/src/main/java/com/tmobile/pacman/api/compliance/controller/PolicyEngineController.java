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
/**
  Copyright (C) 2018 T Mobile Inc - All Rights Reserve
  Purpose:
  Author :Nidhish
  Modified Date: April 2, 2018

 **/
package com.tmobile.pacman.api.compliance.controller;

import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.tmobile.pacman.api.commons.utils.ResponseUtils;
import com.tmobile.pacman.api.compliance.service.PolicyEngineService;

import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;

/**
 * The Class PolicyEngineController.
 */
@RestController
@PreAuthorize("@securityService.hasPermission(authentication, 'readonly') or #oauth2.hasScope('API_OPERATION/READ')")
public class PolicyEngineController {

    /** The Constant LOGGER. */
    private static final Logger LOGGER = LoggerFactory.getLogger(PolicyEngineController.class);

    /** The Policy engine service. */
    @Autowired
    private PolicyEngineService policyEngineService;

    /**
     * Run Policy.
     *
     * @param policyId the policy id
     * @param runTimeParams the run time params
     * @return the response entity
     */
    @ApiOperation(httpMethod = "POST", value = "Invoke PacMan Policy")
    @RequestMapping(path = "/v1/invoke-policy", method = RequestMethod.POST)
    @ResponseBody
    
    public ResponseEntity<Object> runPolicy(
            @ApiParam(value = "Provide valid Policy Id", required = true) @RequestParam("policyId") String policyId,
            @ApiParam(value = "Provide valid Policy Runtime Parameters") @RequestBody Map<String, String> runTimeParams) {
        try {
            policyEngineService.runPolicy(policyId, runTimeParams);
        } catch (Exception exception) {
            LOGGER.error(exception.toString());
            return ResponseUtils.buildFailureResponse(exception);
        }
        return ResponseUtils
                .buildSucessResponse("Invoked Policy Successfully!!!");
    }

    /**
     * Gets the last action.
     *
     * @param resourceId the resource id
     * @return the last action
     */
    @ApiOperation(httpMethod = "GET", value = "Get Last Action")
    @RequestMapping(path = "/v1/get-last-action", method = RequestMethod.GET)
    @ResponseBody
    
    public ResponseEntity<Object> getLastAction(
            @ApiParam(value = "Provide valid Resource Id", required = true) @RequestParam("resourceId") String resourceId) {
        try {
            Map<String, Object> lastAction = policyEngineService
                    .getLastAction(resourceId);
            return new ResponseEntity<>(lastAction, HttpStatus.OK);
        } catch (Exception exception) {
            LOGGER.error(exception.toString());
            return new ResponseEntity<>(buildErrorResponse(),
                    HttpStatus.FORBIDDEN);
        }
    }

    /**
     * Builds the error response.
     *
     * @return the map
     */
    private Map<String, Object> buildErrorResponse() {
        Map<String, Object> errorResponse = Maps.newHashMap();
        errorResponse.put("responseCode", 0);
        errorResponse.put("lastActions", Lists.newArrayList());
        errorResponse.put("message", "Unexpected error occurred!!!");
        return errorResponse;
    }

    /**
     * Post action.
     *
     * @param resourceId the resource id
     * @param action the action
     * @return the response entity
     */
    @ApiOperation(httpMethod = "POST", value = "Post new Resource Action")
    @RequestMapping(path = "/v1/post-action", method = RequestMethod.POST)
    @ResponseBody
    
    public ResponseEntity<Object> postAction(
            @ApiParam(value = "Provide valid Resource Id", required = true) @RequestParam("resourceId") String resourceId,
            @ApiParam(value = "Provide a valid Action", required = true) @RequestParam("action") String action) {
        try {
            policyEngineService.postAction(resourceId, action);
            return ResponseUtils
                    .buildSucessResponse("Successfully Created new Resource Action");
        } catch (Exception exception) {
            LOGGER.error(exception.toString());
            return ResponseUtils.buildFailureResponse(new Exception("failure"),
                    "Failure in Creating new Resource Action");
        }
    }
}
