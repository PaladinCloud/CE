package com.tmobile.pacman.api.compliance.controller;

import com.tmobile.pacman.api.commons.utils.ResponseUtils;
import com.tmobile.pacman.api.compliance.service.PolicyParamService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Api(value = "/policyparams")
@RestController
@PreAuthorize("@securityService.hasPermission(authentication, 'readonly') or #oauth2.hasScope('API_OPERATION/READ')")
@RequestMapping("/policyparams")
public class PolicyParamsController {

    private static final Logger log = LoggerFactory.getLogger(PolicyParamsController.class);
    private static final String UNEXPECTED_ERROR_OCCURRED = "Unexpected error occurred!!";

    @Autowired
    private PolicyParamService policyParamService;

    @ApiOperation(httpMethod = "GET", value = "API to get all params of a policies",  produces = MediaType.APPLICATION_JSON_VALUE)
    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Object> getPolicyParams(
            @ApiParam(value = "provide valid policyId", required = true) @RequestParam("policyId") String policyId) {
        try {
            return ResponseUtils.buildSucessResponse(policyParamService.getPolicyParamsByPolicyId(policyId));
        } catch (Exception exception) {
            log.error(UNEXPECTED_ERROR_OCCURRED, exception);
            return ResponseUtils.buildFailureResponse(new Exception(UNEXPECTED_ERROR_OCCURRED), exception.getMessage());
        }
    }


    @ApiOperation(httpMethod = "GET", value = "API to get param of a policies", produces = MediaType.APPLICATION_JSON_VALUE)
    @GetMapping(path = "/param",produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Object> getPolicyParamsByKey(
            @ApiParam(value = "provide valid policyId", required = true) @RequestParam("policyId") String policyId,
            @ApiParam(value = "provide valid policy param key", required = true) @RequestParam("policyParamKey") String policyParamkey) {
        try {
            return ResponseUtils.buildSucessResponse(policyParamService.getPolicyParamsByPolicyIdAndKey(policyId, policyParamkey));
        } catch (Exception exception) {
            log.error(UNEXPECTED_ERROR_OCCURRED, exception);
            return ResponseUtils.buildFailureResponse(new Exception(UNEXPECTED_ERROR_OCCURRED), exception.getMessage());
        }
    }



}
