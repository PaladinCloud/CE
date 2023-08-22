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

import com.google.common.base.Strings;
import com.google.common.collect.Maps;
import com.google.gson.*;
import com.tmobile.pacman.api.commons.Constants;
import com.tmobile.pacman.api.commons.exception.DataException;
import com.tmobile.pacman.api.commons.exception.ServiceException;
import com.tmobile.pacman.api.commons.repo.ElasticSearchRepository;
import com.tmobile.pacman.api.commons.utils.CommonUtils;
import com.tmobile.pacman.api.commons.utils.PacHttpUtils;
import com.tmobile.pacman.api.commons.utils.ResponseUtils;
import com.tmobile.pacman.api.compliance.client.AuthServiceClient;
import com.tmobile.pacman.api.compliance.domain.*;
import com.tmobile.pacman.api.compliance.enums.PolicyComplianceFilter;
import com.tmobile.pacman.api.compliance.repository.ComplianceRepository;
import com.tmobile.pacman.api.compliance.repository.model.PolicyParams;
import com.tmobile.pacman.api.compliance.repository.FilterRepository;
import com.tmobile.pacman.api.compliance.util.CommonUtil;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

import javax.annotation.PostConstruct;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.Map.Entry;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * The Class ComplianceServiceImpl.
 */
@Service
public class ComplianceServiceImpl implements ComplianceService, Constants {

    private static final String ACTION_REQUIRED_MSG = "Action is required";
    private static final String ASSET_GROUP_REQUIRED_MSG = "Asset group is required";
    private static final String CREATED_BY_REQUIRED_MSG = "Created by is required";
    private static final String APPROVED_BY_REQUIRED_MSG = "Approved by is required";
    private static final String ISSUE_ID_REQUIRED_MSG = "At least one issue Id is required";
    private static final String GRANTED_DATE_REQUIRED_MSG = "Exemption Granted Date is mandatory";
    private static final String GRANTED_DATE_VALIDATION_MSG = "Exception Granted Date cannot be earlier date than today";
    private static final String END_DATE_VALIDATION_MSG = "Exception End Date cannot be earlier date than today";
    private static final String END_DATE_REQUIRED_MSG = "Exception End Date is mandatory";
    private static final String DATE_FORMAT = "yyyy-MM-dd";
    private static final String UTC = "UTC";

    /**
     * The Constant PROTOCOL
     */
    static final String PROTOCOL = "http";
    /**
     * The logger
     */
    private final Logger logger = LoggerFactory.getLogger(getClass());

    /** The statistics client */
    // @Autowired
    /**
     * The mandatory tags
     */
    @Value("${tagging.mandatoryTags}")
    private String mandatoryTags;
    /**
     * The auth client
     */
    @Autowired
    private AuthServiceClient authClient;
    /**
     * The repository
     */
    @Autowired
    private ComplianceRepository repository;
    /**
     * The proj eligibletypes
     */
    @Autowired
    @Value("${projections.targetTypes}")
    private String projEligibletypes;
    /**
     * The elastic search repository
     */
    @Autowired
    private ElasticSearchRepository elasticSearchRepository;
    /**
     * The filter repository
     */
    @Autowired
    private FilterRepository filterRepository;
    /**
     * The system configuration service
     */
    @Autowired
    private SystemConfigurationService systemConfigurationService;
    @Value("${features.vulnerability.enabled:false}")
    private boolean qualysEnabled;
    /**
     * The es host
     */
    @Value("${elastic-search.host}")
    private String esHost;
    /**
     * The es port
     */
    @Value("${elastic-search.port}")
    private int esPort;
    /**
     * The critical issue default time interval for calculating delta
     */
    // @Value("${critical.issues.defaulttime}")
    private final String defaultTime = "24hrs";
    /**
     * The es url
     */
    private String esUrl;

    @Autowired
    private PolicyParamService policyParamService;

    /**
     * Inits the
     */
    @PostConstruct
    void init() {
        esUrl = PROTOCOL + "://" + esHost + ":" + esPort;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public long getIssuesCount(String assetGroup, String policyId, String domain, String accountId) throws ServiceException {
        Assert.notNull(assetGroup, "asset group cannot be empty or blank");
        // transform the data here
        try {
            return repository.getIssuesCount(assetGroup, policyId, domain, accountId);
        } catch (DataException e) {
            throw new ServiceException(e);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ResponseWithOrder getIssues(APIRequest request) throws ServiceException {
        try {
            return repository.getIssuesFromES(request);
        } catch (DataException e) {
            throw new ServiceException(e);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Map<String, Object> getDistribution(String assetGroup, String domain, String accountId) throws ServiceException {
        try {
            Map<String, Object> distribution = new HashMap<>();

            // get Policies mapped to targetType
            String targetTypes = repository.getTargetTypeForAG(assetGroup, domain);
            logger.info("Compliance API >> Fetched target types from repository: {}", targetTypes);

            List<Object> policies = repository.getPolicyIds(targetTypes);
            logger.info("Compliance API >> Fetched policies from repository: {}", policies);

            // get issue count
            Long totalIssues = getIssuesCount(assetGroup, null, domain, accountId);
            logger.info("Compliance API >> Fetched total issues count from repository: {}", totalIssues);

            // get severity distribution
            Map<String, Long> policiesSeverityDistribution = repository.getPoliciesDistribution(assetGroup, domain, policies, SEVERITY);
            logger.info("Compliance API >> Fetched policiesSeverityDistribution from repository: {}", policiesSeverityDistribution);

            // get category distribution
            Map<String, Long> policyCategoryDistribution = repository.getPoliciesDistribution(assetGroup, domain, policies,
                    POLICY_CATEGORY);
            logger.info("Compliance API >> Fetched policyCategoryDistribution from repository: {}", policyCategoryDistribution);

            // get policy category distribution
            Map<String, Object> policyCategoryPercentage = repository.getPolicyCategoryPercentage(policyCategoryDistribution, totalIssues);
            logger.info("Compliance API >> Fetched policyCategoryPercentage from repository: {}", policyCategoryPercentage);

            distribution.put("distribution_by_severity", policiesSeverityDistribution);
            distribution.put("distribution_policyCategory", policyCategoryDistribution);
            distribution.put("policyCategory_percentage", policyCategoryPercentage);
            distribution.put("total_issues", totalIssues);

            return distribution;
        } catch (DataException e) {
            logger.error("Compliance API >> getDistribution >> DataException in getting distribution:{}", e.getStackTrace());
            logger.error("Compliance API >> getDistribution >> DataException in getting distribution", e);
            throw new ServiceException(e);
        }
    }

    private Map<String, Object> mergeMaps(List<Map<String, Object>> listOfMaps) {
        Optional<Map<String, Object>> mergedMap = listOfMaps.stream().reduce((map1, map2) -> {
            return Stream.concat(map1.entrySet().stream(), map2.entrySet().stream())
                    .collect(Collectors.toMap(
                            Entry::getKey,
                            Entry::getValue,
                            (value1, value2) -> {
                                Map<String, Object> resultMap = new HashMap<>((Map<String, Object>) value1);
                                resultMap.putAll((Map<String, Object>) value2);
                                return resultMap;
                            }));
        });

        if (mergedMap.isPresent()) {
            return mergedMap.get();
        }

        return new HashMap<>();
    }

    @Override
    public Map<String, Object> getDistributionBySeverity(String assetGroup, String domain) throws ServiceException {
        try {
            Map<String, Object> distribution = new HashMap<>();
            // get Policies mapped to targetType
            String targetTypes = repository.getTargetTypeForAG(assetGroup, domain);
            logger.info("Compliance API >> Fetched target types from repository: {}", targetTypes);
            List<Object> policies = repository.getPolicyIds(targetTypes);
            logger.info("Compliance API >> Fetched policies from repository: {}", policies);

            // get Policies mapped to targetType
            Map<String, Object> assetDistributionBySeverity = repository.getAssetCountBySeverity(assetGroup, policies);
            logger.info("Compliance API >> Fetched assetDistributionBySeverity from repository: {}", assetDistributionBySeverity);

            Map<String, Object> policyDistributionBySeverity = repository.getPolicyCountBySeverity(assetGroup, policies);
            logger.info("Compliance API >> Fetched policyDistributionBySeverity from repository: {}", policyDistributionBySeverity);

            Map<String, Object> distributionBySeverity = this.mergeMaps(Arrays.asList(new Map[]{policyDistributionBySeverity, assetDistributionBySeverity}));
            distribution.put("distributionBySeverity", distributionBySeverity);
            return distribution;
        } catch (DataException e) {
            logger.error("Compliance API >> DataException in getting distribution:{}",e);
            throw new ServiceException(e);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Map<String, Long> getTagging(String assetGroup, String targetType) throws ServiceException {
        try {
            return repository.getTagging(assetGroup, targetType);
        } catch (DataException e) {
            throw new ServiceException(e);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Map<String, Long> getCertificates(String assetGroup) throws ServiceException {
        try {
            return repository.getCertificates(assetGroup);
        } catch (DataException e) {
            throw new ServiceException(e);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Map<String, Long> getPatching(String assetGroup, String targetType, String application) throws ServiceException {
        logger.info("input value for getPatching are {} {} {}", assetGroup, targetType, application);

        double patchingPercentage;
        Long totalPatched;
        Long totalUnpatched = 0L;
        Long totalAssets = 0L;

        Map<String, Long> patching = new HashMap<>();
        AssetCountDTO[] targetTypes;

        try {
            if (StringUtils.isEmpty(targetType)) {
                targetTypes = filterRepository.getListOfTargetTypes(assetGroup, null);
            } else {
                AssetCountDTO apiName = new AssetCountDTO();
                apiName.setType(targetType);
                targetTypes = new AssetCountDTO[]{apiName};
            }

            for (AssetCountDTO targettype : targetTypes) {
                String type = targettype.getType();
                if (EC2.equalsIgnoreCase(type) || VIRTUALMACHINE.equalsIgnoreCase(type)) {
                    totalAssets += repository.getPatchabeAssetsCount(assetGroup, targettype.getType(), application,
                            null, null);
                    totalUnpatched += repository.getUnpatchedAssetsCount(assetGroup, targettype.getType(), application);
                }
            }
        } catch (DataException e) {
            logger.error("Error @ getPatching ", e);
            throw new ServiceException(e);
        }

        if (totalUnpatched > totalAssets) {
            totalUnpatched = totalAssets;
        }
        totalPatched = totalAssets - totalUnpatched;

        if (totalAssets > 0) {
            patchingPercentage = (totalPatched * HUNDRED) / totalAssets;
            patchingPercentage = Math.floor(patchingPercentage);
        } else {
            patchingPercentage = HUNDRED;
        }

        patching.put(UNPATCHED_INSTANCES, totalUnpatched);
        patching.put(TOTAL_INSTANCES, totalAssets);
        patching.put(PATCHED_INSTANCES, totalPatched);
        patching.put(PATCHING_PERCENTAGE, (long) patchingPercentage);

        if (patching.isEmpty()) {
            throw new ServiceException(NO_DATA_FOUND);
        }

        return patching;
    }

    /**
     * {@inheritDoc}
     */
    public List<Map<String, Object>> getRecommendations(String assetGroup, String targetType) throws ServiceException {
        try {
            return repository.getRecommendations(assetGroup, targetType);
        } catch (DataException e) {
            throw new ServiceException(e);
        }
    }

    /**
     * {@inheritDoc}
     */
    public ResponseWithOrder getIssueAuditLog(String dataSource, String annotationId, String targetType, int from, int size, String searchText) throws ServiceException {
        List<LinkedHashMap<String, Object>> issueAuditLogList;

        try {
            long issueAuditCount = repository.getIssueAuditLogCount(annotationId, targetType);
            issueAuditLogList = repository.getIssueAuditLog(dataSource, annotationId, targetType, from, size, searchText);

            for(Map<String,Object> auditMap:issueAuditLogList){
                if(auditMap.get(STATUS).equals("open") || auditMap.get(STATUS).equals("closed") ){
                    auditMap.put(CREATED_BY,"system");
                }
            }

            if (issueAuditLogList.isEmpty()) {
                throw new ServiceException(NO_DATA_FOUND);
            }

            return new ResponseWithOrder(issueAuditLogList, issueAuditCount);
        } catch (DataException e) {
            throw new ServiceException(e);
        }
    }

    /**
     * {@inheritDoc}
     */
    public List<Map<String, Object>> getResourceDetails(String assetGroup, String resourceId) throws ServiceException {
        try {
            return repository.getResourceDetailsFromES(assetGroup, resourceId);
        } catch (DataException e) {
            throw new ServiceException(e);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Boolean addIssueException(final IssueResponse issueException) throws ServiceException {
        try {
            return repository.exemptAndUpdateIssueDetails(issueException);
        } catch (DataException e) {
            throw new ServiceException(e);
        }
    }

    /*
     * (non-Javadoc)
     *
     * @see
     * com.tmobile.pacman.api.compliance.service.ComplianceService#getPolicycompliance
     * (com.tmobile.pacman.api.compliance.domain.Request)
     */
    @SuppressWarnings("rawtypes")
    public ResponseWithOrder getPolicyCompliance(Request request) throws ServiceException {
        // Ignoring input as we need to return all.
        // logger.debug("getPolicyCompliance invoked with {}", request);
        int size = 0;
        int from = 0;
        String assetGroup = request.getAg();
        String searchText = request.getSearchtext();
        Map<String, String> filters = request.getFilter();
        String policyCategory = "";
        if (null != filters.get(CommonUtils.convertAttributetoKeyword(POLICY_CATEGORY))) {
            policyCategory = filters.get(CommonUtils.convertAttributetoKeyword(POLICY_CATEGORY));
        }
        List<LinkedHashMap<String, Object>> openIssuesByPolicyList = new ArrayList<>();
        List<LinkedHashMap<String, Object>> openIssuesByPolicyListFinal;
        ResponseWithOrder response = null;
        String policy = null;
        String ttypes = "";
        String resourceTypeFilter = null;
        if (filters.containsKey(Constants.RESOURCE_TYPE)
                && StringUtils.isNotBlank(filters.get(Constants.RESOURCE_TYPE))) {
            ttypes = "'" + filters.get(Constants.RESOURCE_TYPE).trim() + "'";
            resourceTypeFilter = filters.get(Constants.RESOURCE_TYPE).trim();
        } else if (!Strings.isNullOrEmpty(filters.get(CommonUtils.convertAttributetoKeyword(TARGET_TYPE)))) {
            ttypes = "'" + filters.get(CommonUtils.convertAttributetoKeyword(TARGET_TYPE)).trim() + "'";
            resourceTypeFilter = filters.get(CommonUtils.convertAttributetoKeyword(TARGET_TYPE)).trim();
        } else {
            ttypes = repository.getTargetTypeForAG(assetGroup, filters.get(DOMAIN));
        }
        final List<Map<String, String>> dataSourceTargetType = repository.getDataSourceForTargetTypeForAG(assetGroup,
                filters.get(DOMAIN), resourceTypeFilter);
        String application;
        if (filters.containsKey(Constants.APPS)) {
            application = filters.get(Constants.APPS);
        } else {
            application = null;
        }
        if (!Strings.isNullOrEmpty(ttypes)) {
            try {
                List<Map<String, Object>> policies = new ArrayList<>();

                /*--For filters we need to take policy Id's which match the filter condition--*/
                if (!Strings.isNullOrEmpty(filters.get(POLICYID_KEYWORD))) {

                    policy = policy + "," + "'" + filters.get(POLICYID_KEYWORD) + "'";
                    policies = repository.getPolicyIdDetails(policy, request.isIncludeDisabled());
                    if (!policies.isEmpty())
                        resourceTypeFilter = policies.get(0).get(TARGET_TYPE).toString();
                } else {
                    policies = repository.getPolicyIdWithDisplayNameWithPolicyCategoryQuery(
                            ttypes, policyCategory, request.isIncludeDisabled());
                }

                logger.debug("Policies in scope {}", policies);

                if (!policies.isEmpty()) {
                    // Make map of policy severity,category

                    List<Map<String, Object>> policiesevCatDetails = getPoliciesevCatDetails(policies);
                    Map<String, Object> policyCatDetails = policiesevCatDetails.parallelStream().
                            filter(c -> c.get(POLICYID) != null && c.get(POLICY_CATEGORY) != null).
                            collect(Collectors.toMap(c -> c.get(POLICYID).toString(), c -> c.get(POLICY_CATEGORY), (oldvalue,
                                                                                                                    newValue) -> newValue));


                    Map<String, Object> policiesevDetails = policiesevCatDetails.parallelStream().
                            filter(c -> c.get(POLICYID) != null && c.get(SEVERITY) != null).
                            collect(Collectors.toMap(c -> c.get(POLICYID).toString(), c -> c.get(SEVERITY),
                                    (oldvalue, newValue) -> newValue));



                    Map<String, Object> policyAutoFixDetails = policiesevCatDetails.parallelStream().collect(
                            Collectors.toMap(c -> c.get(POLICYID).toString(), c -> Boolean.parseBoolean(c.get(AUTOFIX).toString()), (oldValue, newValue) -> newValue));

                    Map<String, Object> policyAutoFixAvailableDetails = policiesevCatDetails.parallelStream().collect(
                            Collectors.toMap(c -> c.get(POLICYID).toString(), c -> Boolean.parseBoolean(c.get(AUTOFIX_AVAILABLE).toString()), (oldValue, newValue) -> newValue));

                    ExecutorService executor = Executors.newCachedThreadPool();

                    Map<String, Long> totalassetCount = new HashMap<>();

                    totalassetCount.putAll(repository.getTotalAssetCount(assetGroup, filters.get(DOMAIN), application,
                            resourceTypeFilter)); // Can't execute in thread as security context is not passed in feign.

                    List<Map<String, Object>> policyIdwithsScanDate = new ArrayList<>();
                    executor.execute(() -> {
                        try {
                            policyIdwithsScanDate.addAll(repository.getPoliciesLastScanDate());
                        } catch (DataException e) {
                            logger.error("Error fetching policy Last scan date", e);
                        }

                    });

                    Map<String, Integer> exemptedAssetsCount = new HashMap<>();
                    try {
                        // Currently exempted info is only used when
                        if (filters.containsKey(Constants.RESOURCE_TYPE)) {
                            // resourceType is passed. Temporary perf fix
                            exemptedAssetsCount.putAll(repository.getExemptedAssetsCountByPolicy(assetGroup, application, filters.get(Constants.RESOURCE_TYPE)));
                        }
                    } catch (DataException e) {
                        logger.error("getPolicyCompliance >> Error fetching exempted asset count", e);
                    }

                    Map<String, Object> untagMap = new HashMap<>();
                    List<Map<String, Object>> policiesTemp = policies;
                    String ttypesTemp = ttypes;

                    executor.execute(() -> {
                        boolean taggingPolicyExists = policiesTemp.stream()
                                .filter(policyObj -> policyObj.get(POLICYID).toString().contains(CATEGORY_TAGGING)).findAny().isPresent();

                        if (taggingPolicyExists)
                            try {
                                untagMap.putAll(repository.getTaggingByAG(assetGroup, ttypesTemp, application));
                            } catch (DataException e) {
                                logger.error("getPolicyCompliance >> Error fetching tagging information ", e);
                            }
                    });

                    final Map<String, Long> openIssuesByPolicyByAG = new HashMap<>();
                    executor.execute(() -> {
                        try {
                            openIssuesByPolicyByAG.putAll(repository.getNonCompliancePolicyByEsWithAssetGroup(
                                    assetGroup, null, filters, from, size, ttypesTemp));
                        } catch (DataException e) {
                            logger.error("getPolicyCompliance >> Error fetching policy issue aggregations ", e);
                        }
                    });

                    executor.shutdown();

                    while (!executor.isTerminated()) {
                        try {
                            Thread.sleep(50);
                        } catch (InterruptedException e) {
                            logger.info("exception in Thread.sleep for policyCompliance");
                        }

                    }

                    policies.forEach(policyIdDetails -> {
                        Map<String, String> policyIdwithsScanDateMap = new HashMap<>();
                        LinkedHashMap<String, Object> openIssuesByPolicy = new LinkedHashMap<>();
                        Long assetCount = 0l;
                        Long issuecountPerPolicyAG = 0l;
                        double compliancePercentage;
                        double contributionPercentage = 0;
                        String resourceType = null;
                        String policyId = null;

                        if (!policyIdwithsScanDate.isEmpty()) {
                            policyIdwithsScanDateMap = policyIdwithsScanDate.stream().collect(
                                    Collectors.toMap(s -> (String) s.get(POLICYID),
                                            s -> (String) s.get(MODIFIED_DATE)));
                        }
                        policyId = policyIdDetails.get(POLICYID).toString();
                        resourceType = policyIdDetails.get(TARGET_TYPE).toString();
                        assetCount = (null != totalassetCount.get(resourceType)) ? totalassetCount
                                .get(resourceType) : 0l;
                        if (null != openIssuesByPolicyByAG.get(policyId)) {
                            issuecountPerPolicyAG = (null != openIssuesByPolicyByAG.get(policyId)) ? openIssuesByPolicyByAG
                                    .get(policyId) : 0l;

                        }
                        if (policyId.contains(CLOUD_KERNEL_COMPLIANCE_POLICY)
                                || policyId.equalsIgnoreCase(ONPREM_KERNEL_COMPLIANCE_RULE)) {

                            try {
                                assetCount = repository.getPatchabeAssetsCount(assetGroup, resourceType, application,
                                        null, null);
                                issuecountPerPolicyAG = repository.getUnpatchedAssetsCount(assetGroup, resourceType,
                                        application);
                            } catch (DataException e) {
                                logger.error("Error fetching patching info", e);
                            }

                        } else if (policyId.contains(CATEGORY_TAGGING)) {
                            issuecountPerPolicyAG = 0l;
                            if (untagMap.get(resourceType) != null) {
                                String totaluntaggedStr = untagMap.get(resourceType).toString()
                                        .substring(0, untagMap.get(resourceType).toString().length() - TWO);
                                issuecountPerPolicyAG = Long.parseLong(totaluntaggedStr);
                            }
                        } else {
                            if ((policyId.contains(CLOUD_QUALYS_POLICY) && qualysEnabled)
                                    || policyId.equalsIgnoreCase(SSM_AGENT_RULE)) {
                                // qualys coverage require only running instances
                                PolicyParams discoveredDayRangeParam = policyParamService.getPolicyParamsByPolicyIdAndKey(policyId,DISCOVERED_DAYS_RANGE);
                                String discoverDayRange=discoveredDayRangeParam.getValue();
                                logger.info("qualys coverage require only running instances {}", policyId);
                                try {
                                    if (StringUtils.isNotBlank(filters.get(Constants.APPS))) {
                                        assetCount = repository.getInstanceCountForQualys(assetGroup, NON_COMPLIANCE_POLICY, filters.get(Constants.APPS), "", resourceType,discoverDayRange);
                                    } else {
                                        assetCount = repository.getInstanceCountForQualys(assetGroup, NON_COMPLIANCE_POLICY, "", "", resourceType,discoverDayRange);
                                    }
                                } catch (DataException e) {
                                    logger.error("getPolicyCompliance >> Error fetching qualys data", e);
                                }
                            }
                        }

                        if (issuecountPerPolicyAG > assetCount) {
                            issuecountPerPolicyAG = assetCount;
                        }
                        Long passed = assetCount - issuecountPerPolicyAG;
                        compliancePercentage = Math.floor(((assetCount - issuecountPerPolicyAG) * HUNDRED) / assetCount);
                        if (assetCount == 0) {
                            compliancePercentage = 100;
                            issuecountPerPolicyAG = 0L;
                            passed = 0L;
                            contributionPercentage = 0.0;
                        }

                        openIssuesByPolicy.put(SEVERITY, policiesevDetails.get(policyId));
                        openIssuesByPolicy.put(NAME, policyIdDetails.get(DISPLAY_NAME).toString());
                        if (request.isIncludeDisabled() && !Objects.isNull(policyIdDetails.get(STATUS)) &&
                                !Objects.isNull(policyIdDetails.get(STATUS).toString())) {
                            openIssuesByPolicy.put(STATUS, policyIdDetails.get(STATUS).toString());
                        }
                        openIssuesByPolicy.put(COMPLIANCE_PERCENT, compliancePercentage);
                        String lastScanDate = repository.getScanDate(policyId, policyIdwithsScanDateMap);
                        if (lastScanDate != null) {
                            openIssuesByPolicy.put(LAST_SCAN, lastScanDate);
                        } else {
                            openIssuesByPolicy.put(LAST_SCAN, "");
                        }
                        final String resourceTypeFinal = resourceType;
                        openIssuesByPolicy.put(POLICY_CATEGORY, policyCatDetails.get(policyId));
                        openIssuesByPolicy.put(RESOURCE_TYPE, resourceType);
                        openIssuesByPolicy.put(PROVIDER, dataSourceTargetType.stream()
                                .filter(datasourceObj -> datasourceObj.get(TYPE).equals(resourceTypeFinal))
                                .findFirst().get().get(PROVIDER));
                        openIssuesByPolicy.put(POLICYID, policyId);
                        openIssuesByPolicy.put(ASSETS_SCANNED, assetCount);
                        openIssuesByPolicy.put(PASSED, passed);
                        openIssuesByPolicy.put(FAILED, issuecountPerPolicyAG);
                        openIssuesByPolicy.put(CONTRIBUTION_PERCENT, contributionPercentage);
                        openIssuesByPolicy.put(AUTOFIX_ENABLED, policyAutoFixDetails.get(policyId));
                        openIssuesByPolicy.put(AUTOFIX_AVAILABLE, policyAutoFixAvailableDetails.get(policyId));

                        if (exemptedAssetsCount.containsKey(policyId)) {
                            openIssuesByPolicy.put(EXEMPTED, exemptedAssetsCount.get(policyId));
                            openIssuesByPolicy.put(IS_ASSETS_EXEMPTED, exemptedAssetsCount.get(policyId).intValue() > 0);
                        } else {
                            openIssuesByPolicy.put(EXEMPTED, 0);
                            openIssuesByPolicy.put(IS_ASSETS_EXEMPTED, false);
                        }

                        if (!Strings.isNullOrEmpty(searchText)) {
                            for (Map.Entry<String, Object> issueByPolicy : openIssuesByPolicy.entrySet()) {
                                if (null != issueByPolicy.getValue() && issueByPolicy.getValue().toString().toLowerCase().contains(searchText.toLowerCase())) {
                                    openIssuesByPolicyList.add(openIssuesByPolicy);
                                    break;
                                }
                            }
                        } else {
                            openIssuesByPolicyList.add(openIssuesByPolicy);
                        }
                    });
                }
                openIssuesByPolicyListFinal = openIssuesByPolicyList;

                // Sorting by #Violation in descending order
                Collections.sort(openIssuesByPolicyListFinal, Collections.reverseOrder(new Compare()));

                if (openIssuesByPolicyList.isEmpty()) {
                    throw new DataException(NO_DATA_FOUND);
                } else {
                    //adding filter and sorting
                    openIssuesByPolicyListFinal = filterPolicyComplianceData(openIssuesByPolicyListFinal, request);
                    int to = (request.getFrom() + request.getSize() == 0 ? openIssuesByPolicyListFinal.size() : request.getSize());
                    if (to > openIssuesByPolicyListFinal.size()) {
                        to = openIssuesByPolicyListFinal.size();
                    }
                    int totalCount = openIssuesByPolicyListFinal.size();
                    openIssuesByPolicyListFinal = openIssuesByPolicyListFinal.subList(request.getFrom(), to);
                    response = new ResponseWithOrder(openIssuesByPolicyListFinal, totalCount);
                }
            } catch (DataException e) {
                logger.error("Error @ getPolicyCompliance while getting the data from ES", e);
                throw new ServiceException(e);
            }
        }

        return response;
    }

    private List<LinkedHashMap<String, Object>> filterPolicyComplianceData(List<LinkedHashMap<String, Object>> openIssuesByPolicyListFinal, Request request){
        Map<String, Object> filter = request.getReqFilter();
        Map<String, Object> sortFilter = request.getSortFilter() == null ? new HashMap<>() : request.getSortFilter();
        if(MapUtils.isNotEmpty(filter)){
            List<String> provider = filter.containsKey(PolicyComplianceFilter.PROVIDER.filter) ?
                    (List<String>) filter.get(PolicyComplianceFilter.PROVIDER.filter) : new ArrayList<>();
            List<Boolean> autoFixAvailable = filter.containsKey(PolicyComplianceFilter.AUTOFIX.filter) ?
                    (List<Boolean>) filter.get(PolicyComplianceFilter.AUTOFIX.filter) : new ArrayList<>();
            List<String> policyName = filter.containsKey(PolicyComplianceFilter.POLICY_NAME.filter) ?
                    (List<String>) filter.get(PolicyComplianceFilter.POLICY_NAME.filter) : new ArrayList<>();
            List<String> severity = filter.containsKey(PolicyComplianceFilter.SEVERITY.filter) ?
                    (List<String>) filter.get(PolicyComplianceFilter.SEVERITY.filter) : new ArrayList<>();
            List<String> category = filter.containsKey(PolicyComplianceFilter.CATEGORY.filter) ?
                    (List<String>) filter.get(PolicyComplianceFilter.CATEGORY.filter) : new ArrayList<>();
            List<String> assetType = filter.containsKey(PolicyComplianceFilter.ASSET_TYPE.filter) ?
                    (List<String>) filter.get(PolicyComplianceFilter.ASSET_TYPE.filter) : new ArrayList<>();
            List<Map<String, String>> violations = filter.containsKey(PolicyComplianceFilter.VIOLATIONS.filter) ?
                    (List<Map<String, String>>) filter.get(PolicyComplianceFilter.VIOLATIONS.filter) : new ArrayList<>();
            List<Map<String, Object>> compliance = filter.containsKey(PolicyComplianceFilter.COMPLIANCE.filter) ?
                    (List<Map<String, Object>>) filter.get(PolicyComplianceFilter.COMPLIANCE.filter) : new ArrayList<>();

            openIssuesByPolicyListFinal = openIssuesByPolicyListFinal.stream()
                    .filter(x -> provider.size() == 0 || provider.contains(x.get(PolicyComplianceFilter.PROVIDER.filter)))
                    .filter(x -> policyName.isEmpty() || policyName.contains(x.get(PolicyComplianceFilter.POLICY_NAME.filter)))
                    .filter(x -> severity.isEmpty() || severity.contains(x.get(PolicyComplianceFilter.SEVERITY.filter)))
                    .filter(x -> category.isEmpty() || category.contains(x.get(PolicyComplianceFilter.CATEGORY.filter)))
                    .filter(x -> compliance.isEmpty() || isComplianceInRange(Double.parseDouble(x.get(PolicyComplianceFilter.COMPLIANCE.filter).toString()), compliance))
                    .filter(x -> violations.isEmpty() || isViolationsInRange(Long.valueOf(x.get(PolicyComplianceFilter.VIOLATIONS.filter).toString()), violations))
                    .filter(x -> assetType.isEmpty() || assetType.contains(x.get(PolicyComplianceFilter.ASSET_TYPE.filter)))
                    .filter(x -> autoFixAvailable.size() ==0 || autoFixAvailable.size() ==2 || autoFixAvailable.contains(x.get(PolicyComplianceFilter.AUTOFIX.filter).toString()))
                    .collect(Collectors.toList());
        }
        if(!CollectionUtils.isEmpty(openIssuesByPolicyListFinal)){
            openIssuesByPolicyListFinal = sortPolicyComplianceData(openIssuesByPolicyListFinal, sortFilter);
        }
        return openIssuesByPolicyListFinal;
    }

    private boolean isViolationsInRange(long percent, List<Map<String, String>> rangeList){
        boolean res = false;
        for(Map<String, String> map: rangeList){
            long min = Long.parseLong(map.get(Constants.MIN));
            long max = Long.parseLong(map.get(Constants.MAX));
            if(percent >= min && percent <= max){
                res = true;
            }
        }
        return res;
    }

    private boolean isComplianceInRange(double percent, List<Map<String, Object>> rangeList){
        for(Map<String, Object> map: rangeList){
            double min = Double.parseDouble(map.get(Constants.MIN).toString());
            double max = Double.parseDouble(map.get(Constants.MAX).toString());
            if(percent >= min && percent <= max){
                return true;
            }
        }
        return false;
    }

    private List<LinkedHashMap<String, Object>> sortPolicyComplianceData(List<LinkedHashMap<String, Object>> openIssuesByPolicyListFinal, Map<String, Object> sortFilter){
        sortFilter = sortFilter == null ? new HashMap<>() : sortFilter;
        String sortAttribute = sortFilter.get(FIELD) == null ? PolicyComplianceFilter.POLICY_NAME.filter :(String) sortFilter.get(FIELD);
        String sortOrder = sortFilter.get(ORDER) == null ? ASC :(String) sortFilter.get(ORDER);
        boolean isSortDouble = false;
        boolean isSortLong = false;
        if(openIssuesByPolicyListFinal.get(0).get(sortAttribute) instanceof Double){
            isSortDouble = true;
        }
        else if(openIssuesByPolicyListFinal.get(0).get(sortAttribute) instanceof Long){
            isSortLong = true;
        }
        if(isSortDouble){
            openIssuesByPolicyListFinal = openIssuesByPolicyListFinal.stream()
                    .sorted(Comparator.comparing(a -> ((Double) a.get(sortAttribute))))
                    .collect(Collectors.toList());
        } else if (isSortLong) {
            openIssuesByPolicyListFinal = openIssuesByPolicyListFinal.stream()
                    .sorted(Comparator.comparing(a -> ((Long) a.get(sortAttribute))))
                    .collect(Collectors.toList());
        } else{
            openIssuesByPolicyListFinal = openIssuesByPolicyListFinal.stream()
                    .sorted(Comparator.comparing(a -> ((String) a.get(sortAttribute))))
                    .collect(Collectors.toList());
        }
        if(sortOrder.equalsIgnoreCase(DESC)){
            Collections.reverse(openIssuesByPolicyListFinal);
        }
        return openIssuesByPolicyListFinal;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Map<String, Object> closeIssuesByPolicy(final PolicyDetails policyDetails) {
        Map<String, Object> response = Maps.newHashMap();
        Boolean isAllClosed = repository.closeIssuesByPolicy(policyDetails);
        if (isAllClosed) {
            response.put(STATUS, TWO_HUNDRED);
            response.put("message", "Successfully Closed all Issues!!!");

            return response;
        } else {
            response.put(STATUS, FOUR_NOT_THREE);
            response.put("message", "Failed in Issues Closure!!!");
        }

        return response;
    }

    /*
     * (non-Javadoc)
     *
     * @see com.tmobile.pacman.api.compliance.repository.ComplianceRepository#
     * getPolicyDetailsByApplicationFromES(java.lang.String, java.lang.String,
     * java.lang.String)
     */
    public JsonArray getPolicyDetailsByApplicationFromES(String assetGroup, String policyId, String searchText)
            throws DataException {
        String responseJson = null;
        JsonParser jsonParser;
        JsonObject resultJson;
        StringBuilder requestBody = null;
        StringBuilder urlToQueryBuffer = new StringBuilder(esUrl).append("/").append(assetGroup).append("/")
                .append(SEARCH);
        requestBody = new StringBuilder(
                "{\"size\":0,\"query\":{\"bool\":{\"must\":[{\"term\":{\"type.keyword\":{\"value\":\"issue\"}}},{\"term\":{\"policyId.keyword\":{\"value\":\""
                        + policyId + "\"}}},{\"term\":{\"issueStatus.keyword\":{\"value\":\"open\"}}}");
        if (!StringUtils.isEmpty(searchText)) {
            requestBody.append(",{\"match_phrase_prefix\":{\"_all\":\"" + searchText + "\"}}");
        }
        // additional filters for kernel compliance policy
        if (EC2_KERNEL_COMPLIANCE_RULE.equalsIgnoreCase(policyId)) {
            requestBody.append(
                    ",{\"has_parent\":{\"parent_type\":\"ec2\",\"query\":{\"bool\":{\"must\":[{\"match\":{\"latest\":\"true\"}},{\"match\":{\"statename\":\"running\"}}],\"must_not\":[{\"match\":{\"platform\":\"windows\"}}]}}}}");
        } else if (VIRTUALMACHINE_KERNEL_COMPLIANCE_RULE.equalsIgnoreCase(policyId)) {
            requestBody.append(
                    ",{\"has_parent\":{\"parent_type\":\"virtualmachine\",\"query\":{\"bool\":{\"must\":[{\"match\":{\"latest\":\"true\"}},{\"match\":{\"status\":\""
                            + RUNNING + "\"}}],\"must_not\":[{\"match\":{\"osType\":\"" + AZURE_WINDOWS + "\"}}]}}}}");
        }
        requestBody.append("]");
        // additional filters for Tagging compliance policy
        if (policyId.contains(CATEGORY_TAGGING)) {
            List<String> tagsList = new ArrayList<>(Arrays.asList(mandatoryTags.split(",")));
            if (!tagsList.isEmpty()) {
                requestBody = requestBody.append(",\"should\":[");
                for (String tag : tagsList) {
                    requestBody = requestBody.append("{\"match_phrase_prefix\":{\"missingTags\":\"" + tag + "\"}},");
                }
                requestBody.setLength(requestBody.length() - 1);
                requestBody.append("]");
                requestBody.append(",\"minimum_should_match\":1");
            }
        }
        requestBody
                .append("}},\"aggs\":{\"NAME\":{\"terms\":{\"field\":\"tags.Application.keyword\",\"size\":1000}}}}");
        try {
            responseJson = PacHttpUtils.doHttpPost(urlToQueryBuffer.toString(), requestBody.toString());
        } catch (Exception e) {
            logger.error(ERROR_IN_US, e);
            throw new DataException(e);
        }
        jsonParser = new JsonParser();
        resultJson = (JsonObject) jsonParser.parse(responseJson);
        JsonObject aggsJson = (JsonObject) jsonParser.parse(resultJson.get(AGGREGATIONS).toString());
        return aggsJson.getAsJsonObject("NAME").getAsJsonArray(BUCKETS);
    }

    /*
     * (non-Javadoc)
     *
     * @see com.tmobile.pacman.api.compliance.service.ComplianceService#
     * getPolicyDetailsByEnvironment(java.lang.String, java.lang.String,
     * java.lang.String, java.lang.String)
     */
    public List<Map<String, Object>> getPolicyDetailsByEnvironment(String assetGroup, String policyId, String application, String searchText) throws ServiceException {
        List<Map<String, Object>> environmentList = new ArrayList<>();
        String targetType = getTargetTypeByPolicyId(policyId);
        JsonArray buckets;
        try {
            buckets = repository.getPolicyDetailsByEnvironmentFromES(assetGroup, policyId, application, searchText, targetType);
        } catch (DataException e) {
            logger.error("Error @ getPolicyDetailsByEnvironment while getting the env by policy and application from ES", e);
            throw new ServiceException(e);
        }

        Gson googleJson = new Gson();
        List<Map<String, Object>> issuesForApplcationByEnvList = googleJson.fromJson(buckets, ArrayList.class);
        Map<String, Long> issuesByApplcationListMap = issuesForApplcationByEnvList.parallelStream().collect(
                Collectors.toMap(issue -> issue.get(KEY).toString(), issue -> (long) Double.parseDouble(issue.get(DOC_COUNT).toString())));

        Map<String, Long> assetCountByEnv = repository.getTotalAssetCountByEnvironment(assetGroup, application, targetType);

        formComplianceDetailsForApplicationByEnvironment(
                policyId, assetCountByEnv, issuesByApplcationListMap, assetGroup, application, environmentList, targetType, searchText);

        return environmentList;
    }

    /**
     * {@inheritDoc}
     */
    public Map<String, Object> getPolicyDescription(String policyId) throws ServiceException {
        Map<String, Object> policydetails = new HashMap<>();
        policydetails.put(POLICYID, policyId);
        try {
            List<Map<String, Object>> description = repository.getPolicyDescriptionFromDb(policyId);
            if (!description.isEmpty()) {
                policydetails = getPolicyDescriptionDetails(description, policydetails);
            } else {
                throw new DataException(NO_DATA_FOUND);
            }
        } catch (DataException e) {
            throw new ServiceException(e);
        }

        return policydetails;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Boolean revokeIssueException(final String issueId) throws ServiceException {
        try {
            return repository.revokeAndUpdateIssueDetails(issueId);
        } catch (DataException e) {
            throw new ServiceException(e);
        }
    }

    /*
     * (non-Javadoc)
     *
     * @see com.tmobile.pacman.api.compliance.service.ComplianceService#
     * getKernelComplianceByInstanceIdFromDb(java.lang.String)
     */
    @Override
    public Map<String, Object> getKernelComplianceByInstanceIdFromDb(String instanceId) throws ServiceException {
        Map<String, Object> map = new HashMap<>();
        List<Map<String, Object>> kernelMap;
        try {
            kernelMap = repository.getKernelComplianceByInstanceIdFromDb(instanceId);
        } catch (DataException e) {
            throw new ServiceException(e);
        }

        for (Map<String, Object> kv : kernelMap) {
            if (null != kv.get(KERNEL_VERSION)) {
                map.put(KERNEL_VERSION, kv.get(KERNEL_VERSION));
            }
        }

        if (map.isEmpty()) {
            throw new ServiceException(NO_DATA_FOUND);
        }

        return map;
    }

    /*
     * (non-Javadoc)
     *
     * @see com.tmobile.pacman.api.compliance.service.ComplianceService#
     * updateKernelVersion
     * (com.tmobile.pacman.api.compliance.domain.KernelVersion)
     */
    @Override
    public Map<String, Object> updateKernelVersion(final KernelVersion kernelVersion) {
        return repository.updateKernelVersion(kernelVersion);
    }

    /*
     * (non-Javadoc)
     *
     * @see com.tmobile.pacman.api.compliance.service.ComplianceService#
     * getOverallComplianceByDomain(java.lang.String, java.lang.String)
     */
    @Override
    public Map<String, Object> getOverallComplianceByDomain(String assetGroup, String domain) throws ServiceException {
        double numerator = 0;
        double denominator = 0;
        double overallcompliance = 0;

        // get all the targetTypes mapped to domain
        // get all policies mapped to these targetTypes
        List<Object> policies = getPolicies(repository.getTargetTypeForAG(assetGroup, domain));
        List<LinkedHashMap<String, Object>> complainceByPolicies = getComplianceByPolicies(domain, assetGroup, policies);

        Map<String, Map<String, Double>> policiesComplianceByCategory = getPoliciesComplianceByCategory(complainceByPolicies,
                assetGroup);
        int totalCategories = policiesComplianceByCategory.entrySet().size();
        LinkedHashMap<String, Object> policyCatWeightage = getPolicyCategoryBWeightage(domain, totalCategories,
                policiesComplianceByCategory);

        int policyCategoryWeightage = 1;
        int totalWeightage = 0;
        LinkedHashMap<String, Object> policyCatDistributionWithOverall = new LinkedHashMap<>();
        for (Map.Entry<String, Object> entry : policyCatWeightage.entrySet()) {
            for (Map.Entry<String, Map<String, Double>> categoryDistribution : policiesComplianceByCategory.entrySet()) {
                // calculate compliance By Category
                if (entry.getKey().equals(categoryDistribution.getKey())) {

                    policyCategoryWeightage = (null != policyCatWeightage.get(categoryDistribution.getKey())) ? Integer
                            .valueOf(policyCatWeightage.get(categoryDistribution.getKey()).toString()) : 1;
                    totalWeightage += policyCategoryWeightage;

                    denominator = (categoryDistribution.getValue().get(DENOMINATOR));
                    numerator = (categoryDistribution.getValue().get(NUMERATOR));
                    double issueCompliance = calculateIssueCompliance(numerator, denominator);

                    policyCatDistributionWithOverall.put(categoryDistribution.getKey(), issueCompliance);
                    overallcompliance += (policyCategoryWeightage * issueCompliance);
                    if (totalCategories == 1) {
                        overallcompliance = overallcompliance / totalWeightage;
                        overallcompliance = Math.floor(overallcompliance);
                        policyCatDistributionWithOverall.put(OVERALL, overallcompliance);
                    }
                    // Calculate Overall Compliance
                    totalCategories -= 1;

                }
            }
        }
        if (policyCatDistributionWithOverall.isEmpty()) {
            throw new ServiceException(NO_DATA_FOUND);
        }
        return policyCatDistributionWithOverall;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<String> getResourceType(String assetGroup, String domain) throws ServiceException {
        List<String> targetTypes = new ArrayList<>();
        if (!StringUtils.isEmpty(projEligibletypes)) {
            String[] projectionTargetTypes = projEligibletypes.split(",");
            String ttypes = repository.getTargetTypeForAG(assetGroup, domain);
            for (String projTargetType : projectionTargetTypes) {
                if (ttypes.contains(projTargetType)) {
                    targetTypes.add(projTargetType);
                }
            }
        } else {
            throw new ServiceException("getResourceType >> Please configure the projection targetTypes");
        }

        return targetTypes;
    }

    /**
     * {@inheritDoc}
     */
    @SuppressWarnings("serial")
    @Override
    public List<Map<String, Object>> getPoliciesCatDetails(List<Map<String, Object>> policyDetails) {
        List<Map<String, Object>> policiesCatDetails = new ArrayList<>();
        for (Map<String, Object> policyDetail : policyDetails) {
            logger.debug("getPoliciesCatDetails >> Fetching details for policy: {}", policyDetail);
            Map<String, Object> policyCatDetail = new HashMap<>();
            policyCatDetail.put(POLICYID, policyDetail.get(POLICYID));
            policyCatDetail.put(TARGET_TYPE, policyDetail.get(TARGET_TYPE));
            policyCatDetail.put(DISPLAY_NAME, policyDetail.get(DISPLAY_NAME));
            policyCatDetail.put(POLICY_CATEGORY, policyDetail.get(CATEGORY));
            policyCatDetail.put(SEVERITY, policyDetail.get(SEVERITY));
            policyCatDetail.put(AUTOFIX, policyDetail.get(AUTOFIX_ENABLED));
            policyCatDetail.put(AUTOFIX_AVAILABLE, policyDetail.get(AUTOFIX_AVAILABLE));
            policiesCatDetails.add(policyCatDetail);
        }

        return policiesCatDetails;
    }

    /**
     * {@inheritDoc}
     */
    public PolicyViolationDetails getPolicyViolationDetailsByIssueId(String assetGroup, String issueId) throws ServiceException {
        String policyViolated = null;
        String policyDescription = null;
        String resourceId = null;
        String policyId = null;
        String issueDetails = null;
        String vulnerabilityDetails=null;
        String pac_ds = "";
        List<Map<String, Object>> violationList = new ArrayList<>();
        List<Map<String, Object>> vulnerabilityList = new ArrayList<>();
        Map<String, Object> violation = null;
        Map<String, Object> policyViolationByIssueId;
        try {
            policyViolationByIssueId = repository.getPolicyViolationDetailsByIssueId(assetGroup, issueId);
        } catch (DataException e) {
            throw new ServiceException(e);
        }

        if (!policyViolationByIssueId.isEmpty()) {
            policyId = policyViolationByIssueId.get(POLICYID).toString();
            resourceId = policyViolationByIssueId.get(RESOURCEID).toString();
            pac_ds = policyViolationByIssueId.get(PAC_DS).toString();

            // get policy description from DB
            policyDescription = (null != getPolicyDescription(policyId).get(RULE_DESC)) ? getPolicyDescription(policyId).get(RULE_DESC).toString() : "";
            // get policy title from DB
            policyViolated = (null != getPolicyDescription(policyId).get(DISPLAY_NAME)) ? getPolicyDescription(policyId).get(DISPLAY_NAME).toString() : "";
            vulnerabilityDetails = (null != policyViolationByIssueId.get(VULNERABILITY_DETAILS)) ? policyViolationByIssueId.get(
                    VULNERABILITY_DETAILS).toString() : null;
            // get issues details from DB
            issueDetails = (null != policyViolationByIssueId.get(ISSUE_DETAILS)) ? policyViolationByIssueId.get(ISSUE_DETAILS).toString() : null;
            if(vulnerabilityDetails!=null){
                JsonParser jsonParser = new JsonParser();
                try {
                    JsonPrimitive resultJson = (JsonPrimitive) jsonParser.parse(vulnerabilityDetails);
                    Map<String, Object> vulnerabitity=new HashMap<>();
                    vulnerabitity.put("vulnerabitityDetails", resultJson.toString());
                    vulnerabilityList.add(vulnerabitity);

                } catch (Exception e) {
                    logger.error("Error in reading qualys details", e);
                    throw new ServiceException(ERROR_READING_VULNERABILITY);
                }
            }
            if (null != policyViolationByIssueId.get(QUALYS_ISSUE_DETAILS)) {
                String violationTitle = policyViolationByIssueId.get(QUALYS_ISSUE_DETAILS).toString();
                violation = new HashMap<>();
                violation.put(QUALYS_VIOLATION_DETAILS, violationTitle);
                violationList.add(violation);
            } else if (!StringUtils.isEmpty(issueDetails) && issueDetails.length() >= 4) {
                issueDetails = issueDetails.substring(TWO, issueDetails.length() - TWO);
                List<String> issueList = new ArrayList<String>();
                int startPosition = 0;
                boolean isOpenParanthesis = false;
                for (int currentPosition = 0; currentPosition < issueDetails.length(); currentPosition++) {
                    if (issueDetails.charAt(currentPosition) == '{') {
                        isOpenParanthesis = !isOpenParanthesis;
                    }
                    else if (issueDetails.charAt(currentPosition) == '}') {
                        isOpenParanthesis = !isOpenParanthesis;
                    }
                    else if(issueDetails.charAt(currentPosition) == ',' && !isOpenParanthesis){
                        issueList.add(issueDetails.substring(startPosition, currentPosition).trim());
                        startPosition = currentPosition + 1;
                    }
                }
                String lastToken = issueDetails.substring(startPosition);
                if (lastToken.equals(",")) {
                    issueList.add("");
                } else {
                    issueList.add(lastToken);
                }

                violation = issueList.stream().map(s -> s.split("="))
                        .filter(obj -> obj.length == 2)
                        .collect(Collectors.toMap(a -> a[0], // key
                                a -> a[1] // value
                        ));

                violation.remove(VIOLATION_REASON);
                violationList.add(violation);
            }

            ExemptionDTO exemption = ExemptionDTO.builder()
                    .reasonToExempt(Objects.isNull(policyViolationByIssueId.get(REASON_TO_EXEMPT_KEY)) ?
                            StringUtils.EMPTY : String.valueOf(policyViolationByIssueId.get(REASON_TO_EXEMPT_KEY)))
                    .status(Objects.isNull(policyViolationByIssueId.get(STATUS)) ? StringUtils.EMPTY :
                            String.valueOf(policyViolationByIssueId.get(STATUS)))
                    .exemptionExpiringOn(Objects.isNull(policyViolationByIssueId.get(EXEMPTION_EXPIRING_ON)) ?
                            StringUtils.EMPTY : String.valueOf(policyViolationByIssueId.get(EXEMPTION_EXPIRING_ON)))
                    .exemptionRaisedExpiringOn(Objects.isNull(policyViolationByIssueId
                            .get(EXEMPTION_RAISED_EXPIRING_ON)) ?
                            StringUtils.EMPTY : String.valueOf(policyViolationByIssueId
                            .get(EXEMPTION_RAISED_EXPIRING_ON)))
                    .exemptionRaisedBy(Objects.isNull(policyViolationByIssueId.get(EXEMPTION_RAISED_BY)) ?
                            StringUtils.EMPTY : String.valueOf(policyViolationByIssueId.get(EXEMPTION_RAISED_BY)))
                    .exemptionRaisedOn(Objects.isNull(policyViolationByIssueId.get(EXEMPTION_RAISED_ON)) ?
                            StringUtils.EMPTY : String.valueOf(policyViolationByIssueId.get(EXEMPTION_RAISED_ON)))
                    .exemptionRevokedOn(Objects.isNull(policyViolationByIssueId.get(EXEMPTION_REQUEST_REVOKED_ON)) ?
                            StringUtils.EMPTY : String.valueOf(policyViolationByIssueId
                            .get(EXEMPTION_REQUEST_REVOKED_ON)))
                    .exemptionRevokedBy(Objects.isNull(policyViolationByIssueId.get(EXEMPTION_REQUEST_REVOKED_BY)) ?
                            StringUtils.EMPTY : String.valueOf(policyViolationByIssueId
                            .get(EXEMPTION_REQUEST_REVOKED_BY)))
                    .exemptionCancelledOn(Objects.isNull(policyViolationByIssueId.get(EXEMPTION_REQUEST_CANCELLED_ON)) ?
                            StringUtils.EMPTY : String.valueOf(policyViolationByIssueId
                            .get(EXEMPTION_REQUEST_CANCELLED_ON)))
                    .exemptionCancelledBy(Objects.isNull(policyViolationByIssueId.get(EXEMPTION_REQUEST_CANCELLED_BY)) ?
                            StringUtils.EMPTY : String.valueOf(policyViolationByIssueId
                            .get(EXEMPTION_REQUEST_CANCELLED_BY)))
                    .exemptionApprovedOn(Objects.isNull(policyViolationByIssueId.get(EXEMPTION_REQUEST_APPROVED_ON)) ?
                            StringUtils.EMPTY : String.valueOf(policyViolationByIssueId
                            .get(EXEMPTION_REQUEST_APPROVED_ON)))
                    .exemptionApprovedBy(Objects.isNull(policyViolationByIssueId.get(EXEMPTION_REQUEST_APPROVED_BY)) ?
                            StringUtils.EMPTY : String.valueOf(policyViolationByIssueId
                            .get(EXEMPTION_REQUEST_APPROVED_BY)))
                    .build();
            return new PolicyViolationDetails(policyViolationByIssueId.get(TARGET_TYPE).toString(),
                    policyViolationByIssueId.get(ISSUE_STATUS).toString(), policyViolationByIssueId.get(SEVERITY)
                    .toString(),
                    policyViolationByIssueId.get(POLICY_CATEGORY).toString(), resourceId,
                    policyViolated, policyDescription, policyViolationByIssueId.get(ISSUE_REASON).toString(),
                    policyViolationByIssueId.get(CREATED_DATE).toString(), policyViolationByIssueId.get(MODIFIED_DATE)
                    .toString(), policyId, pac_ds, violationList, vulnerabilityList, exemption);
        } else {
            throw new ServiceException(NO_DATA_FOUND);
        }
    }

    /**
     * {@inheritDoc}
     */
    public ResponseEntity<Object> formatException(ServiceException e) {
        if (e.getMessage().contains(NO_DATA_FOUND)) {
            List<Map<String, Object>> emptylist = new ArrayList<>();
            ResponseData res = new ResponseData(emptylist);

            return ResponseUtils.buildSucessResponse(res);
        } else {
            return ResponseUtils.buildFailureResponse(e);
        }
    }

    private Map<String, Object> getPolicyDescriptionDetails(List<Map<String, Object>> description, Map<String, Object> policyDetails) {
        List<String> resolution = new ArrayList<>();
        for (Map<String, Object> policy : description) {
            policyDetails.put(POLICY_DESC, policy.get(POLICY_DESC));
            policyDetails.put(DISPLAY_NAME, policy.get(DISPLAY_NAME));
            policyDetails.put(RESOLUTION_URL, policy.get(RESOLUTION_URL));
            policyDetails.put(POLICY_CATEGORY, policy.get(CATEGORY));
            policyDetails.put(SEVERITY, policy.get(SEVERITY));

            if (null != policy.get(RESOLUTION)) {
                resolution = Arrays.asList(policy.get(RESOLUTION).toString().split(","));
                policyDetails.put(RESOLUTION, resolution);
            } else {
                policyDetails.put(RESOLUTION, resolution);
            }
        }

        return policyDetails;
    }

    private List<Map<String, Object>> formComplianceDetailsByApplication(List<Map<String, Object>> applicationList,
                                                                         Map<String, Long> assetCountByApplications,
                                                                         Map<String, Long> issuesByApplcationListMap) {
        Map<String, Object> application;
        String applicationFromAsset;
        double compliancePercentage;

        Long assetCount;
        long issueCount = 0;
        long complaintAssets;


        // Form Compliance Details by Application
        for (Map.Entry<String, Long> assetCountByApplication : assetCountByApplications.entrySet()) {
            application = new HashMap<>();
            assetCount = assetCountByApplication.getValue();
            applicationFromAsset = assetCountByApplication.getKey();

            issueCount = (null == issuesByApplcationListMap.get(applicationFromAsset)) ? 0L : issuesByApplcationListMap.get(applicationFromAsset);
            if (issueCount <= 0) {
                complaintAssets = assetCount;
                compliancePercentage = HUNDRED;
            } else {
                if (issueCount > assetCount) {
                    issueCount = assetCount;
                }

                complaintAssets = assetCount - issueCount;
                compliancePercentage = (complaintAssets * HUNDRED / assetCount);
                compliancePercentage = Math.floor(compliancePercentage);
            }

            application.put(TOTAL, assetCount);
            application.put(APPS, assetCountByApplication.getKey());
            application.put(COMPLAINT, complaintAssets);
            application.put(NON_COMPLIANT_KEY, issueCount);
            application.put(COMPLIANTPERCENTAGE, compliancePercentage);
            applicationList.add(application);
        }

        return applicationList;
    }

    private String getTargetTypeByPolicyId(String policyId) throws ServiceException {
        List<Map<String, Object>> targetTypeByPolicyId;
        try {
            targetTypeByPolicyId = repository.getTargetTypeByPolicyId(policyId);
        } catch (DataException e) {
            throw new ServiceException(e);
        }

        // Get targetType By Application
        for (Map<String, Object> policy : targetTypeByPolicyId) {
            if (policy.get(TARGET_TYPE) != null) {
                return policy.get(TARGET_TYPE).toString();
            }
        }

        return null;
    }

    private int getSeverityWeightage(String severity) {
        switch (severity) {
            case CRITICAL:
                return TEN;
            case HIGH:
                return FIVE;
            case MEDIUM:
                return THREE;
            case LOW:
            default:
                return ONE;
        }
    }

    private LinkedHashMap<String, Object> getPolicyCategoryBWeightage(String domain, int totalCategories,
                                                                      Map<String, Map<String, Double>> policiesComplianceByCategory) throws ServiceException {
        int defaultWeightage = 0;
        Map<String, Object> policyCatWeightageUnsortedMap;

        // get asset count by Target Type
        try {
            policyCatWeightageUnsortedMap = repository.getPolicyCategoryWeightagefromDB(domain);
        } catch (DataException e) {
            throw new ServiceException(e);
        }

        LinkedHashMap<String, Object> policyCatWeightage = new LinkedHashMap<>();
        List<Entry<String, Object>> list = null;
        if (null != policyCatWeightageUnsortedMap && !policyCatWeightageUnsortedMap.isEmpty()) {
            Set<Entry<String, Object>> set = policyCatWeightageUnsortedMap.entrySet();
            list = new ArrayList<>(set);

            Collections.sort(list, new Comparator<Map.Entry<String, Object>>() {
                public int compare(Map.Entry<String, Object> o1, Map.Entry<String, Object> o2) {
                    return (o2.getValue().toString()).compareTo(o1.getValue().toString());
                }
            });

            for (Map.Entry<String, Object> entry : list) {
                policyCatWeightage.put(entry.getKey(), entry.getValue());
            }
        }

        if (policyCatWeightage.isEmpty()) {
            defaultWeightage = INT_HUNDRED / totalCategories;
            for (Map.Entry<String, Map<String, Double>> categoryDistribution : policiesComplianceByCategory.entrySet()) {
                policyCatWeightage.put(categoryDistribution.getKey(), defaultWeightage);
            }
        }

        return policyCatWeightage;
    }

    private Map<String, Map<String, Double>> getPoliciesComplianceByCategory(List<LinkedHashMap<String,
            Object>> complianceByPolicies, String assetGroup) throws ServiceException {
        String severity;
        int severityWeightage = 1;
        boolean isTaggingPresent = false;
        double numerator = 0;
        double denominator = 0;
        double compliance = 0;

        Map<String, Map<String, Double>> policiesComplianceByCategory = new HashMap<>();
        for (Map<String, Object> complianceByPolicy : complianceByPolicies) {
            if (CATEGORY_TAGGING.equals(complianceByPolicy.get(POLICY_CATEGORY).toString())) {
                isTaggingPresent = true;
                continue;
            }

            Map<String, Double> compliancePercentageByPolicy = new HashMap<>();
            compliance = Double.valueOf(complianceByPolicy.get(COMPLIANCE_PERCENT).toString());
            severity = complianceByPolicy.get(SEVERITY).toString();
            severityWeightage = getSeverityWeightage(severity);
            denominator = severityWeightage;
            numerator = (compliance * severityWeightage);

            if (!policiesComplianceByCategory.isEmpty() && (null != policiesComplianceByCategory.get(complianceByPolicy.get(POLICY_CATEGORY).toString()))) {
                Map<String, Double> existingCompliancePercentageByPolicy = policiesComplianceByCategory.get(complianceByPolicy.get(POLICY_CATEGORY));
                denominator += (existingCompliancePercentageByPolicy.get(DENOMINATOR));
                numerator += (existingCompliancePercentageByPolicy.get(NUMERATOR));
            }

            compliancePercentageByPolicy.put(DENOMINATOR, denominator);
            compliancePercentageByPolicy.put(NUMERATOR, numerator);
            policiesComplianceByCategory.put(complianceByPolicy.get(POLICY_CATEGORY).toString(), compliancePercentageByPolicy);
        }

        if (isTaggingPresent) {
            Map<String, Long> taggingInfo = getTagging(assetGroup, null);
            Map<String, Double> compliancePercentageByPolicy = new HashMap<>();
            compliancePercentageByPolicy.put(DENOMINATOR, taggingInfo.get(ASSETS).doubleValue());
            compliancePercentageByPolicy.put(NUMERATOR, taggingInfo.get(TAGGED).doubleValue() * HUNDRED);
            policiesComplianceByCategory.put(CATEGORY_TAGGING, compliancePercentageByPolicy);
        }

        return policiesComplianceByCategory;
    }

    private double calculateIssueCompliance(double numerator, double denominator) {
        if (denominator > 0) {
            return Math.floor(numerator / denominator);
        } else {
            return HUNDRED;
        }
    }

    private List<Object> getPolicies(String ttypes) throws ServiceException {
        List<Object> policies;
        try {
            policies = repository.getPolicyIds(ttypes);
            // get asset count by Target Type
        } catch (DataException e) {
            throw new ServiceException(e);
        }

        return policies;
    }

    private List<LinkedHashMap<String, Object>> getComplianceByPolicies(String domain, String assetGroup, List<Object> policies) throws ServiceException {
        List<LinkedHashMap<String, Object>> complainceByPolicies = null;
        Map<String, String> filter = new HashMap<>();

        filter.put(DOMAIN, domain);
        Request request = new Request("", 0, policies.size(), filter, assetGroup);
        ResponseWithOrder response = getPolicyCompliance(request);

        if (null != response) {
            complainceByPolicies = response.getResponse();
        }

        return complainceByPolicies;
    }

    @Override
    public Map<String, String> getCurrentKernelVersions() {
        return buildCriteriaMap(CommonUtil.getCurrentQuarterCriteriaKey());
    }

    private Map<String, String> buildCriteriaMap(String compCriteriaMap) {
        Map<String, String> kernelCriteriaMap = new TreeMap<>();
        try {
            String kernelSriteriaString = systemConfigurationService.getConfigValue(compCriteriaMap);
            StringTokenizer st = new StringTokenizer(kernelSriteriaString, "|");
            StringTokenizer keyValue;

            logger.debug("criteria string {}", kernelSriteriaString);

            while (st.hasMoreTokens()) {
                keyValue = new StringTokenizer(st.nextToken(), "#");
                kernelCriteriaMap.put(keyValue.nextToken(), keyValue.nextToken());
            }
            logger.debug("criteria map {} ", kernelCriteriaMap);

            return kernelCriteriaMap;
        } catch (Exception e) {
            // create a empty map
            logger.error("buildCriteriaMap >> error parsing pacman.kernel.compliance.map from system configuration", e.getMessage());

            return new TreeMap<>();
        }
    }

    @Override
    public IssueExceptionResponse addMultipleIssueException(String assetGroup, IssuesException issuesException) throws ServiceException {
        try {
            return repository.exemptAndUpdateMultipleIssueDetails(assetGroup, issuesException);
        } catch (DataException e) {
            throw new ServiceException(e);
        }
    }

    @Override
    public IssueExceptionResponse revokeMultipleIssueException(String assetGroup, List<String> issueIds, String revokedBy) throws ServiceException {
        try {
            return repository.revokeAndUpdateMultipleIssueDetails(assetGroup, issueIds, revokedBy);
        } catch (DataException e) {
            throw new ServiceException(e);
        }
    }

    private List<Map<String, Object>> formComplianceDetailsForApplicationByEnvironment(String policyId,
                                                                                       Map<String, Long> assetCountByEnvs, Map<String, Long> issuesForApplcationByEnvMap, String assetGroup,
                                                                                       String application, List<Map<String, Object>> environmentList, String targetType, String searchText) throws ServiceException {
        Map<String, Object> environment;
        String envFromAsset;
        double compliancePercentage;

        Long assetCount;
        long issueCount = 0;
        long complaintAssets;

        // Form Compliance Details for Application by Envi
        for (Map.Entry<String, Long> assetCountByEnv : assetCountByEnvs.entrySet()) {
            environment = new HashMap<>();
            assetCount = assetCountByEnv.getValue();
            envFromAsset = assetCountByEnv.getKey();
            if ((policyId.contains(CLOUD_QUALYS_POLICY) && qualysEnabled) || policyId.equalsIgnoreCase(SSM_AGENT_RULE)) {
                try {
                    PolicyParams discoveredDayRangeParam = policyParamService.getPolicyParamsByPolicyIdAndKey(policyId,DISCOVERED_DAYS_RANGE);
                    String discoverDayRange=discoveredDayRangeParam.getValue();
                    assetCount = repository.getInstanceCountForQualys(assetGroup, POLICY_DETAILS_BY_ENVIRONMENT, application, envFromAsset, targetType, discoverDayRange);
                } catch (DataException e) {
                    logger.error("Error @ formComplianceDetailsForApplicationByEnvironment while getting the asset count from the qualys or ssm from ES", e);
                    throw new ServiceException(e);
                }
            }

            if (policyId.contains(CLOUD_KERNEL_COMPLIANCE_POLICY)) {
                try {
                    assetCount = repository.getPatchabeAssetsCount(assetGroup, targetType, application, envFromAsset, searchText);
                } catch (DataException e) {
                    logger.error("Error @ formComplianceDetailsForApplicationByEnvironment while getting the asset count from the cloud kernel policy from ES", e);
                    throw new ServiceException(e);
                }
            }

            issueCount = (null != issuesForApplcationByEnvMap.get(envFromAsset)) ? issuesForApplcationByEnvMap.get(envFromAsset) : 0L;
            if (issueCount > 0) {
                if (issueCount > assetCount) {
                    issueCount = assetCount;
                }

                complaintAssets = assetCount - issueCount;
                compliancePercentage = (complaintAssets * HUNDRED / assetCount);
                compliancePercentage = Math.floor(compliancePercentage);
            } else {
                complaintAssets = assetCount;
                compliancePercentage = HUNDRED;
            }

            environment.put(TOTAL, assetCount);
            environment.put(ENV, envFromAsset);
            environment.put(COMPLAINT, complaintAssets);
            environment.put(NON_COMPLIANT, issueCount);
            environment.put(COMPLIANTPERCENTAGE, compliancePercentage);
            environmentList.add(environment);
        }

        return environmentList;
    }

    @SuppressWarnings("unchecked")
    public List<Map<String, Object>> getPolicyDetailsByApplication(String assetGroup, String policyId, String searchText) throws ServiceException {
        Map<String, Long> assetCountByApplications;
        List<Map<String, Object>> applicationList = new ArrayList<>();
        String targetType = null;
        JsonArray buckets;

        try {
            buckets = repository.getPolicyDetailsByApplicationFromES(assetGroup, policyId, searchText);
        } catch (DataException e) {
            logger.error("Error @ getPolicyDetailsByApplication while getting the application by policy from ES", e);
            throw new ServiceException(e);
        }

        Gson googleJson = new Gson();
        List<Map<String, Object>> issuesByApplcationList = googleJson.fromJson(buckets, ArrayList.class);
        Map<String, Long> issuesByApplcationListMap = issuesByApplcationList.parallelStream().collect(
                Collectors.toMap(issue -> issue.get(KEY).toString(), issue -> (long) Double.parseDouble(issue.get(DOC_COUNT).toString())));
        targetType = getTargetTypeByPolicyId(policyId);

        if (!Strings.isNullOrEmpty(targetType)) {
            // Get AssetCount By application for Policy TargetType
            if (policyId.contains(CLOUD_KERNEL_COMPLIANCE_POLICY)) {
                try {
                    assetCountByApplications = repository.getPatchableAssetsByApplication(assetGroup, searchText, targetType);
                } catch (DataException e) {
                    logger.error("Error @ getPolicyDetailsByApplication while getting the instance count for cloud kernel policy from ES", e);
                    throw new ServiceException(e);
                }
            } else if ((policyId.equalsIgnoreCase(ONPREM_KERNEL_COMPLIANCE_RULE))) {
                try {
                    assetCountByApplications = repository.getPatchableAssetsByApplication(assetGroup, searchText, ONPREMSERVER);
                } catch (DataException e) {
                    logger.error("Error @ getPolicyDetailsByApplication while getting the instance count for onprem kernel policy from ES", e);
                    throw new ServiceException(e);
                }
            } else if ((policyId.contains(CLOUD_QUALYS_POLICY) && qualysEnabled) || policyId.equalsIgnoreCase(SSM_AGENT_RULE)) {
                try {
                    PolicyParams discoveredDayRangeParam = policyParamService.getPolicyParamsByPolicyIdAndKey(policyId,DISCOVERED_DAYS_RANGE);
                    String discoverDayRange=discoveredDayRangeParam.getValue();
                    assetCountByApplications = repository.getInstanceCountForQualysByAppsOrEnv(assetGroup,
                            POLICY_DETAILS_BY_APPLICATION, "", "", targetType, discoverDayRange);
                } catch (DataException e) {
                    logger.error("Error @ getPolicyDetailsByApplication while getting the instance count for qualys from ES", e);
                    throw new ServiceException(e);
                }
            } else {
                assetCountByApplications = repository.getAllApplicationsAssetCountForTargetType(assetGroup, targetType);
            }

            // Form Compliance Details by Application
            formComplianceDetailsByApplication(applicationList, assetCountByApplications, issuesByApplcationListMap);
        } else {
            throw new ServiceException("No Target Type associated");
        }

        return applicationList;
    }

    @Override
    public List<Map<String, Object>> getPoliciesevCatDetails(List<Map<String, Object>> policyDetails)
            throws ServiceException {
        List<Map<String, Object>> policiesevCatDetails = new ArrayList<>();
        for (Map<String, Object> policyDetail : policyDetails) {
            logger.debug("Fetching details for policy: {}", policyDetail);
            Map<String, Object> policiesevCatDetail = new HashMap<>();
            policiesevCatDetail.put(POLICYID, policyDetail.get(POLICYID));
            policiesevCatDetail.put(AUTOFIX, policyDetail.get(AUTOFIX_ENABLED));
            policiesevCatDetail.put(TARGET_TYPE, policyDetail.get(TARGET_TYPE));
            policiesevCatDetail.put(DISPLAY_NAME, policyDetail.get(DISPLAY_NAME));
            policiesevCatDetail.put(POLICY_CATEGORY, policyDetail.get(CATEGORY));
            policiesevCatDetail.put(SEVERITY, policyDetail.get(SEVERITY));
            policiesevCatDetail.put(AUTOFIX_AVAILABLE, policyDetail.get(AUTOFIX_AVAILABLE));
            policiesevCatDetails.add(policiesevCatDetail);

        }
        return policiesevCatDetails;
    }

    public ResponseEntity<Object> validateIssuesExemptionRequest(ExemptionRequest exemptionRequest) throws ParseException
    {
        if (Objects.isNull(exemptionRequest.getAction()) ||
                Strings.isNullOrEmpty(exemptionRequest.getAction().toString())) {
            return ResponseUtils.buildFailureResponse(new Exception(ACTION_REQUIRED_MSG));
        }
        if (Objects.isNull(exemptionRequest.getAssetGroup()) ||
                Strings.isNullOrEmpty(exemptionRequest.getAssetGroup())) {
            return ResponseUtils.buildFailureResponse(new Exception(ASSET_GROUP_REQUIRED_MSG));
        }
        if (Objects.isNull(exemptionRequest.getCreatedBy()) ||
                Strings.isNullOrEmpty(exemptionRequest.getCreatedBy())) {
            return ResponseUtils.buildFailureResponse(new Exception(CREATED_BY_REQUIRED_MSG));
        }
        if (Objects.isNull(exemptionRequest.getIssueIds()) || exemptionRequest.getIssueIds().isEmpty()) {
            return ResponseUtils.buildFailureResponse(new Exception(ISSUE_ID_REQUIRED_MSG));
        }
        if (exemptionRequest.getAction().equals(ExemptionActions.CREATE_EXEMPTION_REQUEST) ||
                exemptionRequest.getAction().equals(ExemptionActions.APPROVE_EXEMPTION_REQUEST)) {
            if (exemptionRequest.getAction().equals(ExemptionActions.APPROVE_EXEMPTION_REQUEST) &&
                    StringUtils.isEmpty(exemptionRequest.getApprovedBy())) {
                return ResponseUtils.buildFailureResponse(new Exception(APPROVED_BY_REQUIRED_MSG));
            }
            if (exemptionRequest.getExceptionEndDate() == null) {
                return ResponseUtils.buildFailureResponse(new Exception(END_DATE_REQUIRED_MSG));
            }

            SimpleDateFormat sdf = new SimpleDateFormat(DATE_FORMAT);
            Calendar cal = Calendar.getInstance();
            cal.setTimeZone(TimeZone.getTimeZone(UTC));
            if (sdf.parse(sdf.format(exemptionRequest.getExceptionEndDate())).before(sdf.parse(sdf.format(cal.getTime())))) {
                return ResponseUtils.buildFailureResponse(
                        new Exception(END_DATE_VALIDATION_MSG));
            }
        }
        return null;
    }

    public ExemptionResponse createOrRevokeUserExemptionRequest(ExemptionRequest exemptionRequest)
            throws ServiceException {
        try {
            return repository.createOrRevokeUserExemptionRequest(exemptionRequest);
        } catch (DataException e) {
            throw new ServiceException(e);
        }
    }

}