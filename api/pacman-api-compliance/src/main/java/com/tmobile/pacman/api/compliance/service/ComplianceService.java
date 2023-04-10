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

import com.tmobile.pacman.api.commons.exception.ServiceException;
import com.tmobile.pacman.api.compliance.domain.*;
import org.springframework.http.ResponseEntity;

import java.util.List;
import java.util.Map;

// TODO: Auto-generated Javadoc

/**
 * The Interface ComplianceService.
 */
public interface ComplianceService {

    /**
     * Gets the issues details based on name of the domain passed.
     *
     * @param request the request
     * @return ResponseWithOrder
     * @throws ServiceException the service exception
     */
    ResponseWithOrder getIssues(Request request) throws ServiceException;

    /**
     * Gets Issue count based on name of the asset group/policyId/domain passed.
     *
     * @param assetGroup the asset group
     * @param policyId   the policy id
     * @param domain     the domain
     * @param accountId  the account id
     * @return long
     * @throws ServiceException the service exception
     */
    long getIssuesCount(String assetGroup, String policyId, String domain, String accountId) throws ServiceException;

    /**
     * Gets Compliance distribution by policy category and severity.
     *
     * @param assetGroup the asset group
     * @param domain     the domain
     * @param accountId  the account id
     * @return Map<String, Object>
     * @throws ServiceException the service exception
     */
    Map<String, Object> getDistribution(String assetGroup, String domain, String accountId) throws ServiceException;

    /**
     * Gets Compliance Average age distribution by  severity.
     *
     * @param assetGroup the asset group
     * @return Map<String, Object>
     * @throws ServiceException the service exception
     */
    Map<String, Object> getDistributionBySeverity(String assetGroup, String domain) throws ServiceException;

    /**
     * Gets Tagging compliance details based on name of name of the asset group/tagettype passed.
     *
     * @param assetGroup the asset group
     * @param targetType the target type
     * @return Map<String, Long>
     * @throws ServiceException the service exception
     */
    Map<String, Long> getTagging(String assetGroup, String targetType) throws ServiceException;

    /**
     * Gets the count of expiredCertificates with in 60days and
     * totalCertificates for given assetGroup.
     *
     * @param assetGroup the asset group
     * @return Map<String, Long>
     * @throws ServiceException the service exception
     */
    Map<String, Long> getCertificates(String assetGroup) throws ServiceException;

    /**
     * Gets the patching.
     *
     * @param assetGroup  name of the asset group
     * @param targetType  the target type
     * @param application the application
     * @return Method description: asssetGroup is mandatory. Method returns
     * count of totalPached/toalUnpatched/TotalInstances for given
     * assetGroup.
     * @throws ServiceException the service exception
     */
    Map<String, Long> getPatching(String assetGroup, String targetType, String application) throws ServiceException;

    /**
     * If method receives
     * assetGroup as request parameter, method returns list of all the issue
     * counts which are related to recommendations policies from the ES for the
     * given assetGroup with all the targetTypes.If method receives both
     * assetGroup and targetType as request parameter,method returns list of all
     * the issue counts which are related to recommendations policies from the ES
     * for the given targetType & assetGroup.
     *
     * @param assetGroup the asset group
     * @param targetType the target type
     * @return List<Map < String, Object>>
     * @throws ServiceException the service exception
     */
    List<Map<String, Object>> getRecommendations(String assetGroup, String targetType) throws ServiceException;

    /**
     * Gets list of issue audit log details for the size you have given.
     *
     * @param annotationId the annotation id
     * @param targetType   the target type
     * @param from         the from
     * @param size         the size
     * @param searchText   the search text
     * @return ResponseWithOrder
     * @throws ServiceException the service exception
     */
    ResponseWithOrder getIssueAuditLog(String datasource, String annotationId, String targetType, int from, int size,
                                              String searchText) throws ServiceException;

    /**
     * Gets the resource details.
     *
     * @param assetGroup the asset group
     * @param resourceId the resource id
     * @return List<Map < String, Object>>
     * @throws ServiceException the service exception
     */
    List<Map<String, Object>> getResourceDetails(String assetGroup, String resourceId) throws ServiceException;

    /**
     * Returns true if its successfully closes all the issues in ES
     * for that policyId else false.
     *
     * @param policyDetails the policy details
     * @return Map<String, Object>
     */

    Map<String, Object> closeIssuesByPolicy(PolicyDetails policyDetails);

    /**
     * Gets the list of all the policies compliance mapped to that domain.
     *
     * @param request the request
     * @return ResponseWithOrder
     * @throws ServiceException the service exception
     */
    ResponseWithOrder getPolicyCompliance(Request request) throws ServiceException;

    /**
     * Gets the policy details by application.SearchText is used to match any text
     * you are looking for.
     *
     * @param assetGroup the asset group
     * @param policyId   the policy id
     * @param searchText the search text
     * @return List<Map < String, Object>>
     * @throws ServiceException the service exception
     */
    List<Map<String, Object>> getPolicyDetailsByApplication(String assetGroup, String policyId, String searchText)
            throws ServiceException;

    /**
     * Gets the policy details by environment.SearchText is used to match any
     * text you are looking for.
     *
     * @param assetGroup  the asset group
     * @param policyId    the policy id
     * @param application the application
     * @param searchText  the search text
     * @return List<Map < String, Object>>
     * @throws ServiceException the service exception
     */
    List<Map<String, Object>> getPolicyDetailsByEnvironment(String assetGroup, String policyId, String application,
                                                                   String searchText) throws ServiceException;

    /**
     * Gets the policy description and other details.
     *
     * @param policyId the policy id
     * @return Map<String, Object>
     * @throws ServiceException the service exception
     */
    Map<String, Object> getPolicyDescription(String policyId) throws ServiceException;

    /**
     * Gets the kernel version of an instance id from DB where the kernel version updated by web service.
     *
     * @param instanceId the instance id
     * @return Map<String, Object>
     * @throws ServiceException the service exception
     */
    Map<String, Object> getKernelComplianceByInstanceIdFromDb(String instanceId) throws ServiceException;

    /**
     * Returns true if it updates the
     * kernel version for the given instanceId successfully.
     *
     * @param kernelVersion the kernel version
     * @return Map<String, Object>
     */
    Map<String, Object> updateKernelVersion(final KernelVersion kernelVersion);

    /**
     * Gets the overall compliance by domain.Over all compliance is calculated by its severity and policy category weightages.
     *
     * @param assetGroup the asset group
     * @param domain     the domain
     * @return Map<String, Object>
     * @throws ServiceException the service exception
     */
    Map<String, Object> getOverallComplianceByDomain(String assetGroup, String domain) throws ServiceException;

    /**
     * Gets the list of targetTypes for given asset group and domain
     * based on project target types configurations.
     *
     * @param assetGroup the assetGroup
     * @param domain     the domain
     * @return List<String>
     * @throws ServiceException the service exception
     */
    List<String> getResourceType(String assetGroup, String domain) throws ServiceException;

    /**
     * Gets the policy severity and category details.
     *
     * @param policyDetails the policy details
     * @return List<Map < String, Object>>
     * @throws ServiceException the service exception
     */
    List<Map<String, Object>> getPoliciesCatDetails(List<Map<String, Object>> policyDetails) throws ServiceException;

    /**
     * Gets the policy violation details by issue id.
     *
     * @param assetGroup the assetGroup
     * @param issueId    the issue id
     * @return PolicyViolationDetails
     * @throws ServiceException the service exception
     */
    PolicyViolationDetails getPolicyViolationDetailsByIssueId(String assetGroup, String issueId)
            throws ServiceException;

    /**
     * Adds the issue exception.
     *
     * @param issueException the issue exception
     * @return Boolean
     * @throws ServiceException the service exception
     */
    Boolean addIssueException(IssueResponse issueException) throws ServiceException;

    /**
     * Revoke issue exception.
     *
     * @param issueId the issue id
     * @return boolean
     * @throws ServiceException the service exception
     */
    Boolean revokeIssueException(String issueId) throws ServiceException;

    /**
     * Generic method to throw the service exception.
     *
     * @param e the e
     * @return ResponseEntity<Object>
     */
    ResponseEntity<Object> formatException(ServiceException e);

    /**
     * method to get current kernel versions.
     *
     * @return Map<String, String>
     */
    Map<String, String> getCurrentKernelVersions();

    /**
     * Adds the multiple issue exception.
     *
     * @param assetGroup      the asset group
     * @param issuesException the issues exception
     * @return the issue exception response
     * @throws ServiceException the service exception
     */
    IssueExceptionResponse addMultipleIssueException(String assetGroup, IssuesException issuesException) throws ServiceException;

    /**
     * Revoke multiple issue exception.
     *
     * @param assetGroup the asset group
     * @param issueIds   the issue ids
     * @param revokedBy
     * @return the issue exception response
     * @throws ServiceException the service exception
     */
    IssueExceptionResponse revokeMultipleIssueException(String assetGroup, List<String> issueIds, String revokedBy) throws ServiceException;
}
