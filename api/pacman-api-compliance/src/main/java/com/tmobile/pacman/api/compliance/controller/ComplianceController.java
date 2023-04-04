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
package com.tmobile.pacman.api.compliance.controller;

import com.google.common.base.Strings;
import com.tmobile.pacman.api.commons.Constants;
import com.tmobile.pacman.api.commons.exception.ServiceException;
import com.tmobile.pacman.api.commons.utils.ResponseUtils;
import com.tmobile.pacman.api.compliance.domain.*;
import com.tmobile.pacman.api.compliance.service.ComplianceService;
import com.tmobile.pacman.api.compliance.service.PolicyTableService;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import org.apache.commons.collections.MapUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;
import java.util.TimeZone;

/**
 * The Class ComplianceController.
 */
@RestController
@PreAuthorize("@securityService.hasPermission(authentication, 'readonly') or #oauth2.hasScope('API_OPERATION/READ')")
public class ComplianceController implements Constants {
    /**
     * The Constant logger
     */
    private static final Logger log = LoggerFactory.getLogger(ComplianceController.class);

    /**
     * The compliance service
     */
    @Autowired
    private ComplianceService complianceService;

    /**
     * The policy table service
     */
    @Autowired
    private PolicyTableService policyTableService;

    /**
     * Gets the issues details.Request expects asssetGroup and domain as
     * mandatory, policyId as optional.If API receives assetGroup and domain as
     * request parameter, it gives details of all open issues for all the policies
     * associated to that domain. If API receives assetGroup, domain and policyId
     * as request parameter,it gives only open issues of that policy associated to
     * that domain. SearchText is used to match any text you are looking
     * for.From and size are for the pagination
     *
     * @param request request body
     * @return issues
     */
    @RequestMapping(path = "/v1/issues", method = RequestMethod.POST)
    @ResponseBody
    public ResponseEntity<Object> getIssues(@RequestBody(required = false) Request request) {
        String assetGroup = request.getAg();
        Map<String, String> filters = request.getFilter();

        if (Strings.isNullOrEmpty(assetGroup) || MapUtils.isEmpty(filters) || Strings.isNullOrEmpty(filters.get(DOMAIN))) {
            return ResponseUtils.buildFailureResponse(new Exception(ASSET_GROUP_DOMAIN));
        }

        ResponseWithOrder response = null;
        try {
            response = complianceService.getIssues(request);
        } catch (ServiceException e) {
            return complianceService.formatException(e);
        }

        return ResponseUtils.buildSucessResponse(response);
    }

    /**
     * Gets the issues count. asssetGroup and domain are mandatory & policyId is
     * optional parameter, it gives issues count of all open issues for all the policies
     * associated to that domain. If API receives assetGroup,domain and policyId
     * as request parameter,it gives issues count of all open issues for that
     * policy associated to that domain.
     *
     * @param assetGroup name of the asset group
     * @param domain     the domain
     * @param policyId   the policy id
     * @return the issues count
     */
    @RequestMapping(path = "/v1/issues/count", method = RequestMethod.GET)
    public ResponseEntity<Object> getIssuesCount(@RequestParam("ag") String assetGroup,
                                                 @RequestParam("domain") String domain,
                                                 @RequestParam(name = "policyId", required = false) String policyId) {
        if (Strings.isNullOrEmpty(assetGroup) || Strings.isNullOrEmpty(domain)) {
            return ResponseUtils.buildFailureResponse(new Exception(ASSET_GROUP_DOMAIN));
        }

        Map<String, Long> response = new HashMap<>();
        try {
            response.put(TOTAL_ISSUES, complianceService.getIssuesCount(assetGroup, policyId, domain, null));
        } catch (ServiceException e) {
            return ResponseUtils.buildFailureResponse(e);
        }

        return ResponseUtils.buildSucessResponse(response);
    }

    /**
     * Gets the issue distribution by policyCategory and severity.asssetGroup
     * is mandatory, domain is optional. API return issue distribution policy
     * severity & policy Category for given asset group
     *
     * @param assetGroup name of the asset group
     * @param domain     the domain
     * @return ResponseEntity
     */
    @RequestMapping(path = "/v1/issues/distribution", method = RequestMethod.GET)
    public ResponseEntity<Object> getDistribution(@RequestParam("ag") String assetGroup,
                                                  @RequestParam(name = "domain", required = false) String domain,
                                                  @RequestParam(name = "accountId", required = false) String accountId) {
        if (Strings.isNullOrEmpty(assetGroup)) {
            return ResponseUtils.buildFailureResponse(new Exception(ASSET_MANDATORY));
        }

        DitributionDTO distribution = null;
        try {
            distribution = new DitributionDTO(complianceService.getDistribution(assetGroup, domain, accountId));
        } catch (ServiceException e) {
            return complianceService.formatException(e);
        }

        return ResponseUtils.buildSucessResponse(distribution);
    }

    /**
     * Gets the issue distribution by policyCategory and severity.asssetGroup
     * is mandatory, domain is optional. API return issue distribution policy
     * severity & policy Category for given asset group
     *
     * @param assetGroup name of the asset group
     * @return ResponseEntity
     */
    @RequestMapping(path = "/v1/issues/distribution/aggregate", method = RequestMethod.GET)
    public ResponseEntity<Object> getDistributionBySeverity(@RequestParam(name = "ag", required = true) String assetGroup,
                                                            @RequestParam(name = "domain", required = false) String domainName) {
        if (Strings.isNullOrEmpty(assetGroup)) {
            return ResponseUtils.buildFailureResponse(new Exception(ASSET_MANDATORY));
        }

        DitributionDTO distribution = null;
        try {
            distribution = new DitributionDTO(complianceService.getDistributionBySeverity(assetGroup, domainName));
        } catch (ServiceException e) {
            return complianceService.formatException(e);
        }

        return ResponseUtils.buildSucessResponse(distribution);
    }

    /**
     * Gets the tagging compliance summary.asssetGroup is mandatory and
     * targetType is optional If API receives assetGroup as request parameter,
     * api returns tagged/un-tagged/asset count of all the target types for that
     * asset group. If API receives both assetGroup and targetType as request
     * parameter,api returns tagged/un-tagged/asset count of specified target
     * type.
     *
     * @param assetGroup name of the asset group
     * @param targetType the target type
     * @return ResponseEntity
     */
    @RequestMapping(path = "/v1/tagging", method = RequestMethod.GET)
    public ResponseEntity<Object> getTagging(@RequestParam("ag") String assetGroup,
                                             @RequestParam(name = "targettype", required = false) String targetType) {
        if (Strings.isNullOrEmpty(assetGroup)) {
            return ResponseUtils.buildFailureResponse(new Exception(ASSET_MANDATORY));
        }

        OutputDTO output = null;
        try {
            output = new OutputDTO(complianceService.getTagging(assetGroup, targetType));
        } catch (ServiceException e) {
            return complianceService.formatException(e);
        }

        return ResponseUtils.buildSucessResponse(output);
    }

    /**
     * Gets the certificates compliance details.asssetGroup is mandatory. API
     * returns count of expiredCertificates with in 60days and totalCertificates
     * for given assetGroup
     *
     * @param assetGroup name of the asset group
     * @return ResponseEntity
     */
    @RequestMapping(path = "/v1/certificates", method = RequestMethod.GET)
    public ResponseEntity<Object> getCertificates(@RequestParam("ag") String assetGroup) {
        if (Strings.isNullOrEmpty(assetGroup)) {
            return ResponseUtils.buildFailureResponse(new Exception(ASSET_MANDATORY));
        }

        OutputDTO output = null;
        try {
            output = new OutputDTO(complianceService.getCertificates(assetGroup));
        } catch (ServiceException e) {
            return complianceService.formatException(e);
        }

        return ResponseUtils.buildSucessResponse(output);
    }

    /**
     * Gets the patching compliance details.AssetGroup is mandatory. API returns
     * count of totalPached/toalUnpatched/TotalInstances for given assetGroup
     *
     * @param assetGroup name of the asset group
     * @return ResponseEntity
     */
    @RequestMapping(path = "/v1/patching", method = RequestMethod.GET)
    public ResponseEntity<Object> getPatching(@RequestParam("ag") String assetGroup) {
        if (Strings.isNullOrEmpty(assetGroup)) {
            return ResponseUtils.buildFailureResponse(new Exception("Asset group is mandatory"));
        }

        OutputDTO output = null;
        try {
            output = new OutputDTO(complianceService.getPatching(assetGroup, null, null));
        } catch (ServiceException e) {
            return complianceService.formatException(e);
        }

        return ResponseUtils.buildSucessResponse(output);
    }

    /**
     * Gets the recommendations details by policy.asssetGroup is mandatory and
     * targetType is optional. If API receives assetGroup as request parameter,
     * API returns list of all the issue counts which are related to
     * recommendations policies from the ES for the given assetGroup with all the
     * targetTypes.If API receives both assetGroup and targetType as request
     * parameter,API returns list of all the issue counts which are related to
     * recommendations policies from the ES for the given targetType & assetGroup.
     *
     * @param assetGroup name of the asset group
     * @param targetType the target type
     * @return ResponseEntity
     */
    @RequestMapping(path = "/v1/recommendations", method = RequestMethod.GET)
    public ResponseEntity<Object> getRecommendations(@RequestParam("ag") String assetGroup,
                                                     @RequestParam(name = "targettype", required = false) String targetType) {
        if (Strings.isNullOrEmpty(assetGroup)) {
            return ResponseUtils.buildFailureResponse(new Exception(ASSET_MANDATORY));
        }

        ResponseData response = null;
        try {
            response = new ResponseData(complianceService.getRecommendations(assetGroup, targetType));
        } catch (ServiceException e) {
            return complianceService.formatException(e);
        }

        return ResponseUtils.buildSucessResponse(response);

    }

    /**
     * Gets the issue audit details.This request accepts
     * annotationId,targetType,size as mandatory. If API receives
     * annotationId,targetType,size as request parameter, API returns list of
     * data source, audit date and status of that annotationId. searchText is used
     * to match any text you are looking for. from and size are for pagination.
     *
     * @param request the request
     * @return the issue audit
     */
    @RequestMapping(path = "/v1/issueauditlog", method = RequestMethod.POST)
    public ResponseEntity<Object> getIssueAudit(@RequestBody IssueAuditLogRequest request) {
        String issueId = request.getIssueId();
        String targetType = request.getTargetType();
        String dataSource = request.getDataSource();
        String searchText = request.getSearchText();

        int from = request.getFrom();
        int size = request.getSize();

        if (Strings.isNullOrEmpty(issueId) || Strings.isNullOrEmpty(targetType) || from < 0 || size <= 0) {
            return ResponseUtils.buildFailureResponse(new Exception("IssueId/Targettype/from/size is Mandatory"));
        }

        ResponseWithOrder response = null;
        try {
            response = complianceService.getIssueAuditLog(dataSource, issueId, targetType, from, size, searchText);
        } catch (ServiceException e) {
            return complianceService.formatException(e);
        }

        return ResponseUtils.buildSucessResponse(response);

    }

    /**
     * Gets the resource details.assetGroup and resourceId are mandatory. API
     * returns map details for given resourceId
     *
     * @param assetGroup name of the asset group
     * @param resourceId the resource id
     * @return ResponseEntity
     */

    @RequestMapping(path = "/v1/resourcedetails", method = RequestMethod.GET)
    public ResponseEntity<Object> getResourceDetails(@RequestParam("ag") String assetGroup,
                                                     @RequestParam("resourceId") String resourceId) {
        if (Strings.isNullOrEmpty(resourceId)) {
            return ResponseUtils.buildFailureResponse(new Exception("assetGroup/resourceId is mandatory"));
        }

        ResponseData response = null;
        try {
            response = new ResponseData(complianceService.getResourceDetails(assetGroup, resourceId));
        } catch (ServiceException e) {
            return complianceService.formatException(e);
        }

        return ResponseUtils.buildSucessResponse(response);
    }

    /**
     * Close issues.policyDetails expects policyId,reason and userId, Api returns
     * true if its successfully closes all issues in ES for that policyId else
     * false
     *
     * @param policyDetails the policy details
     * @return ResponseEntity
     */
    @ApiOperation(httpMethod = "PUT", value = "Close Issues by Policy Details")
    @RequestMapping(path = "/v1/issues/close-by-policy-id", method = RequestMethod.PUT)
    @ResponseBody
    public ResponseEntity<Object> closeIssues(
            @ApiParam(value = "Provide valid Policy Details ", required = true) @RequestBody(required = true) PolicyDetails policyDetails) {
        Map<String, Object> response = complianceService.closeIssuesByPolicy(policyDetails);
        if (Integer.parseInt(response.get(STATUS).toString()) == TWO_HUNDRED) {
            return new ResponseEntity<>(response, HttpStatus.OK);
        } else {
            return new ResponseEntity<>(response, HttpStatus.FORBIDDEN);
        }
    }

    /**
     * Adds the issue exception.issueException expects
     * issueId,exceptionGrantedDate,exceptionEndDate and exceptionReason, API is
     * for adding issue exception to the corresponding target type.
     *
     * @param issueException the issue exception
     * @return ResponseEntity
     */
    @ApiOperation(httpMethod = "POST", value = "Adding issue exception to the corresponding target type")
    @RequestMapping(path = "/v1/issues/add-exception", method = RequestMethod.POST)
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Successfully Added Issue Exception"),
            @ApiResponse(code = 401, message = "You are not authorized to Add Issue Exception"),
            @ApiResponse(code = 403, message = "Add Issue Exception is forbidden")
    })
    @ResponseBody
    public ResponseEntity<Object> addIssueException(
            @ApiParam(value = "Provide Issue Exception Details", required = true) @RequestBody(required = true) IssueResponse issueException) {
        try {
            Boolean isExempted = complianceService.addIssueException(issueException);
            if (isExempted) {
                return ResponseUtils.buildSucessResponse("Successfully Added Issue Exception");
            } else {
                return ResponseUtils.buildFailureResponse(new Exception("Failed in Adding Issue Exception"));
            }
        } catch (ServiceException exception) {
            return ResponseUtils.buildFailureResponse(exception);
        }
    }

    /**
     * Revoke issue exception.
     *
     * @param issueId the issue id
     * @return ResponseEntity
     */
    @ApiOperation(httpMethod = "POST", value = "Revoking issue exception to the corresponding target type")
    @RequestMapping(path = "/v1/issues/revoke-exception", method = RequestMethod.POST)
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Successfully Revoked Issue Exception"),
            @ApiResponse(code = 401, message = "You are not authorized to Revoke Issue Exception"),
            @ApiResponse(code = 403, message = "Revoke IssueException is forbidden")
    })
    @ResponseBody
    public ResponseEntity<Object> revokeIssueException(
            @ApiParam(value = "Provide Issue Id", required = true) @RequestParam(required = true) String issueId) {
        try {
            Boolean isIssueExceptionRevoked = complianceService.revokeIssueException(issueId);
            if (isIssueExceptionRevoked) {
                return ResponseUtils.buildSucessResponse("Successfully Revoked Issue Exception");
            } else {
                return ResponseUtils.buildFailureResponse(new Exception("Failed in Revoking Issue Exception"));
            }
        } catch (ServiceException exception) {
            return ResponseUtils.buildFailureResponse(exception);
        }
    }

    /**
     * Gets the non-compliance policy by policy.request expects asset group and
     * domain as mandatory.Api returns list of all the policies associated to that
     * domain with compliance percentage/severity/policyCategory etc fields.
     *
     * @param request the request
     * @return ResponseEntity
     */
    @RequestMapping(path = "/v1/noncompliancepolicy", method = RequestMethod.POST)
    // TODO: performance after refactoring
    // @Cacheable(cacheNames="compliance",unless="#result.status==200")
    // @Cacheable(cacheNames="compliance",key="#request.key")
    public ResponseEntity<Object> getNonCompliancePolicyByPolicy(@RequestBody(required = false) Request request) {
        String assetGroup = request.getAg();
        Map<String, String> filters = request.getFilter();

        if (Strings.isNullOrEmpty(assetGroup) || MapUtils.isEmpty(filters) || Strings.isNullOrEmpty(filters.get(DOMAIN))) {
            return ResponseUtils.buildFailureResponse(new Exception(ASSET_GROUP_DOMAIN));
        }

        ResponseWithOrder response = null;
        try {
            response = (complianceService.getPolicyCompliance(request));
        } catch (ServiceException e) {
            return complianceService.formatException(e);
        }

        return ResponseUtils.buildSucessResponse(response);
    }

    /**
     * Gets the policy details by application.asssetGroup and policyId are
     * mandatory. API returns total/application/compliant/compliantPercentage of
     * the policyId for given assetGroup. SearchText is used to match any text you
     * are looking for
     *
     * @param assetGroup name of the asset group
     * @param policyId   the policy id
     * @param searchText the search text
     * @return ResponseEntity
     */
    @RequestMapping(path = "/v1/policydetailsbyapplication", method = RequestMethod.GET)
    // @Cacheable(cacheNames="compliance",unless="#result.status==200")
    public ResponseEntity<Object> getPolicydetailsbyApplication(@RequestParam("ag") String assetGroup,
                                                                @RequestParam("policyId") String policyId,
                                                                @RequestParam(name = "searchText", required = false) String searchText) {
        if (Strings.isNullOrEmpty(assetGroup) || Strings.isNullOrEmpty(policyId)) {
            return ResponseUtils.buildFailureResponse(new Exception("Assetgroup/policyId is mandatory"));
        }

        ResponseData response = null;
        try {

            response = new ResponseData(complianceService.getPolicyDetailsByApplication(assetGroup, policyId, searchText));
        } catch (ServiceException e) {
            return complianceService.formatException(e);
        }

        return ResponseUtils.buildSucessResponse(response);
    }

    /**
     * Gets the policy details by environment.asssetGroup,application and policyId
     * are mandatory. API returns
     * total/environment/compliant/compliantPercentage of the policyId for given
     * assetGroup and application. SearchText is used to match any text you are
     * looking for
     *
     * @param assetGroup  name of the asset group
     * @param application name of the application
     * @param policyId    the policy id
     * @param searchText  the search text
     * @return ResponseEntity
     */
    @RequestMapping(path = "/v1/policydetailsbyenvironment", method = RequestMethod.GET)

    public ResponseEntity<Object> getpolicydetailsbyEnvironment(@RequestParam("ag") String assetGroup,
                                                                @RequestParam("application") String application, @RequestParam("policyId") String policyId,
                                                                @RequestParam(name = "searchText", required = false) String searchText) {

        if (Strings.isNullOrEmpty(assetGroup) || Strings.isNullOrEmpty(application) || Strings.isNullOrEmpty(policyId)) {
            return ResponseUtils.buildFailureResponse(new Exception("assetGroup/application/policyId is mandatory"));
        }

        ResponseData response = null;
        try {
            response = new ResponseData(complianceService.getPolicyDetailsByEnvironment(assetGroup, policyId, application,
                    searchText));

        } catch (ServiceException e) {
            return complianceService.formatException(e);
        }

        return ResponseUtils.buildSucessResponse(response);
    }

    /**
     * API returns details of the given policyId.
     *
     * @param policyId the policy id
     * @return ResponseEntity<Object>
     */
    @RequestMapping(path = "/v1/policydescription", method = RequestMethod.GET)
    public ResponseEntity<Object> getPolicyDescription(@RequestParam("policyId") String policyId) {
        if (Strings.isNullOrEmpty(policyId)) {
            return ResponseUtils.buildFailureResponse(new Exception("policyId Mandatory"));
        }

        PolicyDescription response = null;
        try {
            response = new PolicyDescription(complianceService.getPolicyDescription(policyId));

        } catch (ServiceException e) {
            return complianceService.formatException(e);
        }

        return ResponseUtils.buildSucessResponse(response);
    }


    /**
     * API returns the kernel version of the given instanceId if it is
     * from web service.
     *
     * @param instanceId the instance id
     * @return ResponseEntity<Object>
     */
    @RequestMapping(path = "/v1/kernelcompliancebyinstanceid", method = RequestMethod.GET)
    public ResponseEntity<Object> getKernelComplianceByInstanceId(@RequestParam("instanceId") String instanceId) {
        if (Strings.isNullOrEmpty(instanceId)) {
            return ResponseUtils.buildFailureResponse(new Exception("instanceId is mandatory"));
        }

        PolicyDescription output = null;
        try {
            output = new PolicyDescription(complianceService.getKernelComplianceByInstanceIdFromDb(instanceId));
        } catch (ServiceException e) {
            return complianceService.formatException(e);
        }

        return ResponseUtils.buildSucessResponse(output);
    }

    /**
     * API returns true if it updates the kernel version for the given
     * instanceId successfully.
     *
     * @param kernelVersion the kernel version
     * @return ResponseEntity<Object>
     */
    @ApiOperation(httpMethod = "PUT", value = "Update Kernel Version by InstanceId")
    @RequestMapping(path = "/v1/update-kernel-version", method = RequestMethod.PUT)
    @ResponseBody
    public ResponseEntity<Object> updateKernelVersion(
            @ApiParam(value = "Provide valid Policy Details ", required = true) @RequestBody(required = true) KernelVersion kernelVersion) {
        Map<String, Object> response = complianceService.updateKernelVersion(kernelVersion);

        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    /**
     * API returns overall compliance based on policy category and severity weightages
     * for given asset group and domain.
     *
     * @param assetGroup - String
     * @param domain     - String
     * @return ResponseEntity<Object> .
     */
    @RequestMapping(path = "/v1/overallcompliance", method = RequestMethod.GET)
    public ResponseEntity<Object> getOverallCompliance(@RequestParam("ag") String assetGroup,
                                                       @RequestParam(name = "domain") String domain) {
        if (Strings.isNullOrEmpty(assetGroup) || Strings.isNullOrEmpty(domain)) {
            return ResponseUtils.buildFailureResponse(new Exception(ASSET_GROUP_DOMAIN));
        }

        DitributionDTO distribution = null;
        try {
            distribution = new DitributionDTO(complianceService.getOverallComplianceByDomain(assetGroup, domain));
        } catch (ServiceException e) {
            return complianceService.formatException(e);
        }

        return ResponseUtils.buildSucessResponse(distribution);
    }

    /**
     * API returns targetTypes for given asset group and domain based on
     * project target types configurations.
     *
     * @param assetGroup the assetGroup
     * @param domain     the domain
     * @return ResponseEntity<Object>
     */
    @RequestMapping(path = "/v1/targetType", method = RequestMethod.GET)
    public ResponseEntity<Object> getTargetType(@RequestParam("ag") String assetGroup,
                                                @RequestParam(name = "domain", required = false) String domain) {
        if (Strings.isNullOrEmpty(assetGroup)) {
            return ResponseUtils.buildFailureResponse(new Exception(ASSET_MANDATORY));
        }

        ResourceTypeResponse response;
        try {

            response = new ResourceTypeResponse(complianceService.getResourceType(assetGroup, domain));
        } catch (Exception e) {
            return ResponseUtils.buildFailureResponse(e);
        }

        return ResponseUtils.buildSucessResponse(response);
    }

    /**
     * API returns reason for violation along with other details for the
     * given asset group and issueId.
     *
     * @param assetGroup the assetGroup
     * @param issueId    the issue id
     * @return ResponseEntity<Object>
     */
    @RequestMapping(path = "/v1/policyViolationReason", method = RequestMethod.GET)
    public ResponseEntity<Object> policyViolationReason(@RequestParam("ag") String assetGroup,
                                                        @RequestParam(name = "issueId") String issueId) {
        if (Strings.isNullOrEmpty(assetGroup) && Strings.isNullOrEmpty(issueId)) {
            return ResponseUtils.buildFailureResponse(new Exception("AssetGroup/IssueId is Mandatory"));
        }

        PolicyViolationDetails response = null;
        try {
            response = complianceService.getPolicyViolationDetailsByIssueId(assetGroup, issueId);
        } catch (ServiceException e) {
            return complianceService.formatException(e);
        }

        return ResponseUtils.buildSucessResponse(response);
    }

    /**
     * API returns current kernel versions.
     *
     * @return ResponseEntity<Object>
     */
    @RequestMapping(path = "/v1/get-current-kernel-versions", method = RequestMethod.GET)
    public ResponseEntity<Object> getCurrentKernelVersions() {
        return ResponseUtils.buildSucessResponse(complianceService.getCurrentKernelVersions());
    }

    /**
     * Adds the issues exception.
     *
     * @param issuesException the issues exception
     * @return the response entity
     */
    @ApiOperation(httpMethod = "POST", value = "Adding issue exception to the corresponding target type")
    @RequestMapping(path = "/v2/issue/add-exception", method = RequestMethod.POST)
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Successfully Added Issue Exception"),
            @ApiResponse(code = 401, message = "You are not authorized to Add Issue Exception"),
            @ApiResponse(code = 403, message = "Add Issue Exception is forbidden")
    })
    @ResponseBody
    public ResponseEntity<Object> addIssuesException(@RequestParam("ag") String assetGroup,
                                                     @ApiParam(value = "Provide Issue Exception Details", required = true) @RequestBody(required = true) IssuesException issuesException) {
        try {
            if (issuesException.getExceptionGrantedDate() == null) {
                return ResponseUtils.buildFailureResponse(new Exception("Exception Granted Date is mandatory"));
            }

            if (issuesException.getExceptionEndDate() == null) {
                return ResponseUtils.buildFailureResponse(new Exception("Exception End Date is mandatory"));
            }

            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
            Calendar cal = Calendar.getInstance();
            cal.setTimeZone(TimeZone.getTimeZone("UTC"));
            if (sdf.parse(sdf.format(issuesException.getExceptionGrantedDate())).before(sdf.parse(sdf.format(cal.getTime())))) {
                return ResponseUtils.buildFailureResponse(new Exception("Exception Granted Date cannot be earlier date than today"));
            }

            if (sdf.parse(sdf.format(issuesException.getExceptionEndDate())).before(sdf.parse(sdf.format(cal.getTime())))) {
                return ResponseUtils.buildFailureResponse(new Exception("Exception End Date cannot be earlier date than today"));
            }

            if (issuesException.getIssueIds().isEmpty()) {
                return ResponseUtils.buildFailureResponse(new Exception("Atleast one issue id is required"));
            }

            return ResponseUtils.buildSucessResponse(complianceService.addMultipleIssueException(assetGroup, issuesException));
        } catch (ServiceException | ParseException exception) {
            return ResponseUtils.buildFailureResponse(exception);
        }
    }

    /**
     * Revoke issue exception.
     *
     * @param assetGroup the issue ids
     * @return ResponseEntity
     */
    @ApiOperation(httpMethod = "POST", value = "Revoking issue exception to the corresponding target type")
    @RequestMapping(path = "/v2/issue/revoke-exception", method = RequestMethod.POST)
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Successfully Revoked Issue Exception"),
            @ApiResponse(code = 401, message = "You are not authorized to Revoke Issue Exception"),
            @ApiResponse(code = 403, message = "Revoke IssueException is forbidden")
    })
    @ResponseBody
    public ResponseEntity<Object> revokeIssuesException(@RequestParam("ag") String assetGroup,
                                                        @ApiParam(value = "Provide Issue Id", required = true) @RequestBody(required = true) RevokeIssuesException revokeIssuesException) {
        try {
            if (revokeIssuesException.getIssueIds().isEmpty()) {
                return ResponseUtils.buildFailureResponse(new Exception("At least one issue id is required"));
            }

            return ResponseUtils.buildSucessResponse(complianceService.revokeMultipleIssueException(assetGroup, revokeIssuesException.getIssueIds(), revokeIssuesException.getRevokedBy()));
        } catch (ServiceException exception) {
            return ResponseUtils.buildFailureResponse(exception);
        }
    }

    /**
     * API to get policy by id
     *
     * @param policyId - valid policy Id
     * @return Policies details
     * @author
     */
    @ApiOperation(httpMethod = "GET", value = "API to get policy by id", produces = MediaType.APPLICATION_JSON_VALUE)
    @RequestMapping(path = "/v1/policy-details-by-id", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Object> getPoliciesById(
            @ApiParam(value = "provide valid policy id", required = true) @RequestParam(defaultValue = "", name = "policyId", required = true) String policyId) {
        try {
            return ResponseUtils.buildSucessResponse(policyTableService.getPolicyTableByPolicyId(policyId));
        } catch (Exception exception) {
            log.error("Unexpected error occurred!!", exception);
            return ResponseUtils.buildFailureResponse(new Exception("Unexpected error occurred!!"), exception.getMessage());
        }
    }

    /**
     * API to get policy by UUID
     *
     * @param policyUUID - valid policy UUID
     * @return Policies details
     * @author
     */
    @ApiOperation(httpMethod = "GET", value = "API to get policy by UUID", produces = MediaType.APPLICATION_JSON_VALUE)
    @RequestMapping(path = "/v1/policy-details-by-uuid", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Object> getPoliciesByUUID(
            @ApiParam(value = "provide valid policy UUID", required = true) @RequestParam(defaultValue = "", name = "policyUUID", required = true) String policyUUID) {
        try {
            return ResponseUtils.buildSucessResponse(policyTableService.getPolicyTableByPolicyUUID(policyUUID));
        } catch (Exception exception) {
            log.error("Unexpected error occurred!!", exception);
            return ResponseUtils.buildFailureResponse(new Exception("Unexpected error occurred!!"), exception.getMessage());
        }
    }
}
