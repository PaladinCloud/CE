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
package com.tmobile.pacman.api.compliance.repository;

import com.google.common.base.Strings;
import com.google.common.collect.HashMultimap;
import com.google.gson.Gson;
import com.tmobile.pacman.api.commons.Constants;
import com.tmobile.pacman.api.commons.exception.DataException;
import com.tmobile.pacman.api.commons.exception.ServiceException;
import com.tmobile.pacman.api.commons.repo.ElasticSearchRepository;
import com.tmobile.pacman.api.commons.repo.PacmanRdsRepository;
import com.tmobile.pacman.api.commons.utils.CommonUtils;
import com.tmobile.pacman.api.compliance.client.AssetServiceClient;
import com.tmobile.pacman.api.compliance.domain.AssetApi;
import com.tmobile.pacman.api.compliance.domain.AssetApiData;
import com.tmobile.pacman.api.compliance.domain.AssetCountDTO;
import com.tmobile.pacman.api.compliance.domain.ResponseData;
import lombok.var;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Repository;
import org.springframework.util.CollectionUtils;

import java.math.BigDecimal;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * The Class FilterRepositoryImpl.
 */
@Repository
public class FilterRepositoryImpl implements FilterRepository, Constants {
    @Value("${tagging.mandatoryTags}")
    private String mandatoryTags;

    /** The rdsepository. */
    @Autowired
    private PacmanRdsRepository rdsepository;

    /** The elastic search repository. */
    @Autowired
    private ElasticSearchRepository elasticSearchRepository;

    /** The asset service client. */
    @Autowired
    private AssetServiceClient assetServiceClient;

    protected final Log logger = LogFactory.getLog(getClass());

    private static final String UNDERSCORE_ENTITY="_entity";

    /*
     * (non-Javadoc)
     * @see com.tmobile.pacman.api.compliance.repository.FilterRepository# getFiltersFromDb(int)
     */
    @Override
    public List<Map<String, Object>> getFiltersFromDb(int filterId)
            throws DataException {
        String ruleIdWithTargetTypeQuery = "SELECT opt.optionName as optionName,opt.optionValue as optionValue,opt.optionURL as optionURL FROM pac_v2_ui_filters filters LEFT JOIN pac_v2_ui_options opt ON filters.filterId = opt.filterId WHERE opt.filterId = "
                + filterId + "";
        return rdsepository.getDataFromPacman(ruleIdWithTargetTypeQuery);
    }

    /*
     * (non-Javadoc)
     *
     * @see com.tmobile.pacman.api.compliance.repository.FilterRepository#
     * getPoliciesFromDB(java.lang.String)
     */
    public List<Map<String, Object>> getPoliciesFromDB(String targetTypes)
            throws DataException {
        String ruleIdQuery = "SELECT policyId,policyName FROM cf_PolicyTable  WHERE status = 'ENABLED' AND targetType IN ("
                + targetTypes + ") GROUP BY policyId";
        return rdsepository.getDataFromPacman(ruleIdQuery);
    }

    /*
     * (non-Javadoc)
     *
     * @see com.tmobile.pacman.api.compliance.repository.FilterRepository#
     * getPoliciesFromES(java.lang.String)
     */
    public Map<String, Long> getPoliciesFromES(String assetGroup)
            throws DataException {
        Map<String, Object> mustFilter = new HashMap<>();
        Map<String, Object> mustNotFilter = new HashMap<>();
        String aggsFilter = CommonUtils.convertAttributetoKeyword("policyId");
        try {
            return elasticSearchRepository.getTotalDistributionForIndexAndType(
                    assetGroup, null, mustFilter, mustNotFilter, null, aggsFilter,
                    1000, null);
        } catch (Exception e) {
            logger.error("error in getPoliciesFromES", e);
            throw new DataException(e);
        }
    }

    /*
     * (non-Javadoc)
     *
     * @see com.tmobile.pacman.api.compliance.repository.FilterRepository#
     * getAccountsFromES(java.lang.String)
     */
    public List<Map<String, Object>> getAccountsFromES(String assetGroup)
            throws DataException {
        Map<String, Object> mustFilter = new HashMap<>();
        mustFilter.put("latest", "true");
        ArrayList<String> fields = new ArrayList<>();
        fields.add("accountname");
        fields.add("accountid");
        try {
            return elasticSearchRepository.getSortedDataFromES(assetGroup,
                    "account", mustFilter, null, null, fields, null, null);
        } catch (Exception e) {
            throw new DataException(e);
        }
    }

    /*
     * (non-Javadoc)
     *
     * @see com.tmobile.pacman.api.compliance.repository.FilterRepository#
     * getRegionsFromES(java.lang.String)
     */
    public Map<String, Long> getRegionsFromES(String assetGroup,Map<String,Object> filter)
            throws DataException {
        Map<String, Object> mustFilter = new HashMap<>();
        Map<String, Object> mustNotFilter = new HashMap<>();
        Map<String, Object> mustTermsFilter=new HashMap<>();
        HashMultimap<String, Object> shouldFilter = HashMultimap.create();
        mustFilter.put(Constants.TYPE, "issue");
        String aggsFilter = CommonUtils.convertAttributetoKeyword("region");
        if(filter.keySet().size()!=0)
        {
            for(String key: filter.keySet())
            {
                mustTermsFilter.put(CommonUtils.convertAttributetoKeyword(key),filter.get(key));
            }
        }
        try {
            return elasticSearchRepository.getTotalDistributionForIndexAndType(
                    assetGroup, null, mustFilter, mustNotFilter, shouldFilter, aggsFilter,
                    THOUSAND, mustTermsFilter);
        } catch (Exception e) {
            throw new DataException(e);
        }
    }

    /**
     * Gets the map of all the severities and its count from the ES.
     *
     * @param assetGroup the asset group
     * @return Map<String, Long>.
     * @throws DataException the data exception
     */
    public Map<String, Long> getSeveritiesFromES(String assetGroup,Map<String,Object> filter)
            throws DataException {
        Map<String, Object> mustFilter = new HashMap<>();
        Map<String, Object> mustNotFilter = new HashMap<>();
        Map<String, Object> mustTermsFilter=new HashMap<>();
        HashMultimap<String, Object> shouldFilter = HashMultimap.create();
        mustFilter.put(Constants.TYPE, "issue");
        String aggsFilter = CommonUtils.convertAttributetoKeyword("severity");
        if(filter.keySet().size()!=0)
        {
            for(String key: filter.keySet())
            {
                mustTermsFilter.put(CommonUtils.convertAttributetoKeyword(key),filter.get(key));
            }
        }
        try {
            return elasticSearchRepository.getTotalDistributionForIndexAndType(
                    assetGroup, null, mustFilter, mustNotFilter, shouldFilter, aggsFilter,
                    THOUSAND, mustTermsFilter);
        } catch (Exception e) {
            throw new DataException(e);
        }
    }

    /**
     * Gets the map of all the ruleCategories and its count from the ES.
     *
     * @param assetGroup the asset group
     * @return Map<String, Long>.
     * @throws DataException the data exception
     */
    public Map<String, Long> getCategoriesFromES(String assetGroup)
            throws DataException {
        Map<String, Object> mustFilter = new HashMap<>();
        Map<String, Object> mustNotFilter = new HashMap<>();
        String aggsFilter = CommonUtils.convertAttributetoKeyword("policyCategory");
        try {
            return elasticSearchRepository.getTotalDistributionForIndexAndType(
                    assetGroup, null, mustFilter, mustNotFilter, null, aggsFilter,
                    THOUSAND, null);
        } catch (Exception e) {
            throw new DataException(e);
        }
    }

    /*
     * (non-Javadoc)
     *
     * @see
     * com.tmobile.pacman.api.compliance.repository.FilterRepository#getRulesFromES(
     * java.lang.String)
     */
    public Map<String, Long> getRulesFromES(String assetGroup) throws DataException {
        Map<String, Object> mustFilter = new HashMap<>();
        Map<String, Object> mustNotFilter = new HashMap<>();
        String aggsFilter = CommonUtils.convertAttributetoKeyword(POLICYID);
        try {
            return elasticSearchRepository.getTotalDistributionForIndexAndType(
                    assetGroup, null, mustFilter, mustNotFilter, null, aggsFilter,
                    THOUSAND, null);
        } catch (Exception e) {
            throw new DataException(e);
        }
    }

    /*
     * (non-Javadoc)
     *
     * @see com.tmobile.pacman.api.compliance.repository.FilterRepository#
     * getListOfApplications(java.lang.String, java.lang.String)
     */
    public AssetCountDTO[] getListOfApplications(String assetGroup,
                                                 String domain) throws DataException {
        AssetApi asstApi = assetServiceClient.getApplicationsList(assetGroup,
                domain);
        AssetApiData data = asstApi.getData();
        return data.getApplications();
    }

    /*
     * (non-Javadoc)
     *
     * @see com.tmobile.pacman.api.compliance.repository.FilterRepository#
     * getListOfEnvironments(java.lang.String, java.lang.String, java.lang.String)
     */
    public AssetCountDTO[] getListOfEnvironments(String assetGroup,
                                                 String application, String domain) throws DataException {
        AssetApi asstApi = assetServiceClient.getEnvironmentList(assetGroup,
                application, domain);
        AssetApiData data = asstApi.getData();
        return data.getEnvironments();
    }

    /*
     * (non-Javadoc)
     *
     * @see com.tmobile.pacman.api.compliance.repository.FilterRepository#
     * getListOfTargetTypes(java.lang.String, java.lang.String)
     */
    public AssetCountDTO[] getListOfTargetTypes(String assetGroup, String domain)
            throws DataException {
        AssetApi asstApi = assetServiceClient.getTargetTypeList(assetGroup,
                domain);
        AssetApiData data = asstApi.getData();
        return data.getTargettypes();
    }

    @Override
    public AssetCountDTO[] getValueListforTag(String assetGroup, String tag, String type) throws DataException {
        AssetApi asstApi = assetServiceClient.getValuesByTag(assetGroup,tag, type);
        AssetApiData data = asstApi.getData();
        return data.getAssets();

    }

    /*
     * (non-Javadoc)
     *
     * @see com.tmobile.pacman.api.compliance.repository.FilterRepository#
     * getRegionsFromES(java.lang.String)
     */
    public Map<String, Long> getNotificationTypesFromES(Map<String, List<String>> filter)
            throws DataException {
        Map<String, Object> mustFilter = new HashMap<>();
        Map<String, Object> mustNotFilter = new HashMap<>();
        Map<String, Object> mustTermsFilter = new HashMap<>();

        List<String> eventSource = filter.get(NOTIFICATION_SOURCE_NAME);
        List<String> eventName = filter.get(NOTIFICATION_EVENT_NAME);

        if(eventSource!=null && !eventSource.isEmpty()) {
            mustFilter.put("eventSourceName.keyword", eventSource);
        }

        if(eventName!=null && !eventName.isEmpty()) {
            mustFilter.put("eventName.keyword", eventName);
        }

        if(!mustFilter.isEmpty()) {
            mustTermsFilter.putAll(mustFilter);
        }

        Map<String ,Object> dateFilterMap=getDateFilter(filter);

        if(dateFilterMap!=null  && !dateFilterMap.isEmpty()){
            mustFilter.put("range",dateFilterMap);
        }

        String aggsFilter = CommonUtils.convertAttributetoKeyword(NOTIFICATION_CATEGEORY_NAME);
        try {
            return elasticSearchRepository.getTotalDistributionForIndexAndType(
                    NOTIFICATION_INDEX, NOTIFICATION_INDEX_TYPE, mustFilter, mustNotFilter, null, aggsFilter,
                    THOUSAND, mustTermsFilter);
        } catch (Exception e) {
            throw new DataException(e);
        }
    }

    /*
     * (non-Javadoc)
     *
     * @see com.tmobile.pacman.api.compliance.repository.FilterRepository#
     * getRegionsFromES(java.lang.String)
     */
    public Map<String, Long> getNotificationSourceFromES(Map<String, List<String>> filter)
            throws DataException {
        Map<String, Object> mustFilter = new HashMap<>();
        Map<String, Object> mustNotFilter = new HashMap<>();
        Map<String, Object> mustTermsFilter = new HashMap<>();

        List<String> eventCategory = filter.get(NOTIFICATION_CATEGEORY_NAME);
        List<String> eventName = filter.get(NOTIFICATION_EVENT_NAME);

        if(eventCategory!=null && !eventCategory.isEmpty()) {
            mustFilter.put("eventCategoryName.keyword", eventCategory);
        }

        if(eventName!=null && !eventName.isEmpty()) {
            mustFilter.put("eventName.keyword", eventName);
        }

        if(!mustFilter.isEmpty()) {
            mustTermsFilter.putAll(mustFilter);
        }

        Map<String ,Object> dateFilterMap=getDateFilter(filter);

        if(dateFilterMap!=null  && !dateFilterMap.isEmpty()){
            mustFilter.put("range",dateFilterMap);
        }

        String aggsFilter = CommonUtils.convertAttributetoKeyword(NOTIFICATION_SOURCE_NAME);
        try {
            return elasticSearchRepository.getTotalDistributionForIndexAndType(
                    NOTIFICATION_INDEX, NOTIFICATION_INDEX_TYPE, mustFilter, mustNotFilter, null, aggsFilter,
                    THOUSAND, mustTermsFilter);
        } catch (Exception e) {
            throw new DataException(e);
        }
    }
    public Map<String, ?> getAttributeValuesFromES(String assetGroup, Map<String,Object> filter, String entityType,String attributeName,String targetTypes,String searchText)
            throws DataException {
        Map<String, Object> mustFilter = new HashMap<>();
        Map<String, Object> mustNotFilter = new HashMap<>();
        Map<String, Object> mustTermsFilter=new HashMap<>();
        HashMultimap<String, Object> shouldFilter = HashMultimap.create();
        List<String> validTypes = new ArrayList<String>(Arrays.asList(targetTypes.replace("'","").split(",")));
        checkEntityType(mustFilter,mustTermsFilter,entityType,validTypes);
        setFilters(filter,mustFilter,shouldFilter,mustTermsFilter,mustNotFilter, assetGroup, validTypes,entityType);
        if (!Strings.isNullOrEmpty(searchText)) {
            setWildCardFilter(searchText, mustFilter, attributeName);
        }
        String aggsFilter=attributeName;
        try {
            if(StringUtils.endsWith(attributeName,".keyword") || attributeName.contains(AUTOFIX_PLANNED)){
                aggsFilter=attributeName;
            }else{
                aggsFilter=CommonUtils.convertAttributetoKeyword(attributeName);
            }
            //ignore "Unknown Status" while fetching for violation
            if(attributeName.equalsIgnoreCase("issueStatus") && aggsFilter.equalsIgnoreCase("issueStatus.keyword")){
                mustNotFilter.put("issueStatus.keyword","unknown");
            }

            Map<String, Long> totalDistributionForIndexAndType = elasticSearchRepository.getTotalDistributionForIndexAndType(
                    assetGroup, null, mustFilter, mustNotFilter, shouldFilter, aggsFilter,
                    THOUSAND, mustTermsFilter);
            if(attributeName.contains(AUTOFIX_PLANNED)){
                updateFinalMap(totalDistributionForIndexAndType);
            }
            return totalDistributionForIndexAndType;
        } catch (Exception e) {
            throw new DataException(e);
        }
    }

    private void setWildCardFilter(String searchText, Map<String, Object> mustFilter, String attributeName) {
        Map<String, Object> wildCardMap = new HashMap<String, Object>();
        Map<String, Object> valueMap = new HashMap<String, Object>();
        valueMap.put("value","*"+searchText+"*");
        wildCardMap.put(attributeName,valueMap);
        mustFilter.put("wildcard",wildCardMap);

    }

    private void setFilters(Map<String, Object> filter, Map<String, Object> mustFilter, HashMultimap<String, Object> shouldFilter,
                            Map<String, Object> mustTermsFilter, Map<String, Object> mustNotFilter, String assetGroup,  List<String> validTypes, String entityType) {
        Iterator it = filter.entrySet().iterator();
        List<String> taggingTargetTypesList = new ArrayList<String>();
        List<String> policies = null;

        List<String> mustResourceIds = new ArrayList<>();
        List<String> mustNotResourceIds = new ArrayList<>();
        if(entityType.equalsIgnoreCase("issue")){
            mustNotFilter.put("issueStatus.keyword", "unknown");
        }
        while (it.hasNext()) {
            Map.Entry entry = (Map.Entry) it.next();
            String key = (String) entry.getKey();
            if(CommonUtils.isEqualToAttribue("policyId", key)){
                policies = (List<String>) entry.getValue();
                List<Object> targetTypesForPolicy = getTargetTypeByRuleIds(assetGroup, policies, validTypes);
                validTypes.retainAll(targetTypesForPolicy);
            }else if(entityType.equals("asset") && CommonUtils.isEqualToAttribue("_resourceid", key)){
                mustResourceIds.addAll((List<String>) entry.getValue());
            }
            else if(CommonUtils.isEqualToAttribue("compliant", key)){
                List<String> nonCompliantId = getNonCompliantAssetsForPolicy(policies, assetGroup);
                String filterCompliant = fetchInputValFromFilter(entry.getValue());
                if(filterCompliant.equals(FALSE)) {
                    mustResourceIds.addAll(nonCompliantId);;
                }
                else{
                    mustNotResourceIds.addAll(nonCompliantId);
                    mustNotFilter.put("_resourceid.keyword",nonCompliantId);
                }
            }
            else if (CommonUtils.isEqualToAttribue(FILTER_TAGGED, key)) {
                String filterTagged = fetchInputValFromFilter(entry.getValue());
                List<String> untaggedAssets = new ArrayList<>();
                untaggedAssets = getUntaggedAssets(assetGroup);
                if (filterTagged.equalsIgnoreCase(FALSE)) {
                    for (String untaggedAsset : untaggedAssets) {
                        if (!mustNotResourceIds.contains(untaggedAsset)) {
                            mustResourceIds.add(untaggedAsset);
                        }
                    }
                } else {
                    List<Map<String, Object>> targetTypesForTaggingList = rdsepository.getDataFromPacman("SELECT p.targetType FROM cf_PolicyTable p WHERE p.status = 'ENABLED' AND p.category = '" + Constants.CATEGORY_TAGGING + "'");
                    taggingTargetTypesList = targetTypesForTaggingList.stream().map(obj -> (String) obj.get("targetType")).collect(Collectors.toList());
                    validTypes.retainAll(taggingTargetTypesList);
                    if (filterTagged.equalsIgnoreCase(TRUE)) {
                        for (String untaggedAsset : untaggedAssets) {
                            mustNotResourceIds.add(untaggedAsset);
                            if (mustResourceIds.contains(untaggedAsset)) {
                                mustResourceIds.remove(untaggedAsset);
                            }
                        }
                    }
                }
            } else if(CommonUtils.isEqualToAttribue(EXEMPTED, key)) {
                List<Map<String, Object>> masterList;
                List<String> exemptedResourceIds=new ArrayList<>();
                try {
                    masterList = getAssetsExempted(assetGroup);
                    for(Map<String,Object>asset:masterList){
                        if(asset.containsKey(RESOURCEID)){
                            Object resourceId=asset.get(RESOURCEID);
                            exemptedResourceIds.add((String) resourceId);
                        }
                    }
                    List<String> exemptInp = (List<String>) filter.get(key);
                    if (exemptInp != null && exemptInp.size() == 1) {
                        if (exemptInp.get(0).equalsIgnoreCase(TRUE)) {
                            exemptedResourceIds = exemptedResourceIds.stream().filter(x -> !mustNotResourceIds.contains(x)).collect(Collectors.toList());
                            mustResourceIds.addAll(exemptedResourceIds);
                        } else {
                            mustNotResourceIds.addAll(exemptedResourceIds);
                            mustResourceIds.removeAll(exemptedResourceIds);
                        }
                    }

                } catch (Exception e) {
                    logger.error("Error in fetching attributes ",e);
                }
            }
            else if(CommonUtils.isEqualToAttribue(AUTOFIX_PLANNED, key)){
                Boolean isAutofixPlanned = false;
                if(filter.get(key) == null){
                    isAutofixPlanned = null;
                }
                else if(filter.get(key) instanceof String || filter.get(key) instanceof Boolean){
                    isAutofixPlanned = Boolean.parseBoolean((String)filter.get(key));
                }
                else{
                    List<String> ls = (List<String>) filter.get(key);
                    isAutofixPlanned = ls.size() == 1 ? Boolean.parseBoolean(ls.get(0)) : null;
                }

                if(isAutofixPlanned!=null){
                    if(isAutofixPlanned){
                        mustFilter.put(AUTOFIX_PLANNED, true);
                    }else{
                        List<Map<String,Object>> mustNotList=new ArrayList<>();
                        Map<String,Object> existMap=new HashMap<>();
                        existMap.put("field", AUTOFIX_PLANNED);
                        Map<String,Object> mustNotCondition=new HashMap<>();
                        mustNotCondition.put("exists", existMap);
                        mustNotList.add(mustNotCondition);
                        Map<String,Object> mustNotMap=new HashMap<>();
                        mustNotMap.put("must_not",mustNotList);
                        shouldFilter.put("bool", mustNotMap);
                        shouldFilter.put(AUTOFIX_PLANNED,"false");
                    }
                }
            } else if (CommonUtils.isEqualToAttribue("_entitytype", key)) {
                List<String> entityTypeFromReq = (List<String>) filter.get(key);
                validTypes.retainAll(entityTypeFromReq);
            } else
                mustTermsFilter.put(CommonUtils.convertAttributetoKeyword(key),filter.get(key));
        }
        if(entityType.equalsIgnoreCase("asset")){
            mustTermsFilter.put("_entitytype.keyword", validTypes);
        }
        if(!CollectionUtils.isEmpty(mustResourceIds)) {
            mustTermsFilter.put("_resourceid.keyword", mustResourceIds);
        }
        if(!CollectionUtils.isEmpty(mustNotResourceIds)) {
            mustNotFilter.put("_resourceid.keyword", mustNotResourceIds);
        }

    }

    private void checkEntityType(Map<String, Object> mustFilter, Map<String, Object> mustTermsFilter, String entityType,List<String> targetTypes) {
        if(entityType.equalsIgnoreCase("asset")){
            mustFilter.put(UNDERSCORE_ENTITY, Constants.TRUE);
            mustFilter.put(LATEST, Constants.TRUE);
            mustTermsFilter.put("_entitytype.keyword",targetTypes);
        }else if(entityType.equalsIgnoreCase("issue")){
            mustFilter.put(Constants.TYPE, "issue");
        }
    }

    private void updateFinalMap(Map<String, Long> totalDistributionForIndexAndType) {
        Map<String, Long> tempMap = new HashMap<>();
        totalDistributionForIndexAndType.entrySet().forEach(entry -> {
            String key = entry.getKey();
            String newKey = key.equals("1.0") ? "true" : "false";
            tempMap.put(newKey, entry.getValue());
        });
        if(!tempMap.containsKey("false")){
            tempMap.put("false",0L);
        }
        totalDistributionForIndexAndType.clear();
        totalDistributionForIndexAndType.putAll(tempMap);
    }

    public Map<String, Long> getNotificationEventNamesFromES(Map<String, List<String>> filter)
            throws DataException {
        Map<String, Object> mustFilter = new HashMap<>();
        Map<String, Object> mustNotFilter = new HashMap<>();
        Map<String, Object> mustTermsFilter = new HashMap<>();

        List<String> eventCategory = filter.get(NOTIFICATION_CATEGEORY_NAME);
        List<String>  eventSource = filter.get( NOTIFICATION_SOURCE_NAME);

        if(eventCategory!=null && !eventCategory.isEmpty()){
        mustFilter.put("eventCategoryName.keyword",eventCategory);
        }

        if(eventSource!=null && !eventSource.isEmpty()) {
            mustFilter.put("eventSourceName.keyword", eventSource);
        }

        if(!mustFilter.isEmpty()) {
            mustTermsFilter.putAll(mustFilter);
        }

        Map<String ,Object> dateFilterMap=getDateFilter(filter);

        if(dateFilterMap!=null  && !dateFilterMap.isEmpty()){
            mustFilter.put("range",dateFilterMap);
        }
        String aggsFilter = CommonUtils.convertAttributetoKeyword(NOTIFICATION_EVENT_NAME);
        try {
            return elasticSearchRepository.getTotalDistributionForIndexAndType(
                    NOTIFICATION_INDEX, NOTIFICATION_INDEX_TYPE, mustFilter, mustNotFilter, null, aggsFilter,
                    THOUSAND,mustTermsFilter);
        } catch (Exception e) {
            throw new DataException(e);
        }
    }

    private List<String> getNonCompliantAssetsForPolicy(List<String> policyId, String assetGroup) {
        Map<String, Object> mustFilter = new HashMap<>();
        Map<String, Object> mustTermsFilter = new HashMap<>();
        mustFilter.put(CommonUtils.convertAttributetoKeyword(Constants.TYPE), Constants.ISSUE);
        mustFilter.put(CommonUtils.convertAttributetoKeyword(Constants.ISSUE_STATUS), Constants.OPEN);
        if (!CollectionUtils.isEmpty(policyId)) {
            mustTermsFilter.put(CommonUtils.convertAttributetoKeyword(Constants.POLICYID), policyId);
        }
        List<Map<String, Object>> nonCompliantAssets = new ArrayList<>();
        Map<String, Long> totalDistributionForIndexAndType = null;
        try {
            int totalDocs = (int) elasticSearchRepository.getTotalDocumentCountForIndexAndType(assetGroup, null, mustFilter, null,
                    null, null, mustTermsFilter);
            totalDistributionForIndexAndType = elasticSearchRepository.getTotalDistributionForIndexAndType(
                    assetGroup, null, mustFilter, null, null, "_resourceid.keyword", totalDocs, mustTermsFilter);
        } catch (Exception e) {
            logger.error("Exception occurred in fetching non compliant assets for policy ", e);
        }
        List<String> nonCompliantresourceIds = totalDistributionForIndexAndType.entrySet().parallelStream()
                .map(obj -> obj.getKey().toString()).collect(Collectors.toList());

        return nonCompliantresourceIds;
    }

    private List<Map<String, Object>> getAssetsExempted(String assetGroup)  {
        logger.info("Inside getListAssetsExempted");
        List<Map<String, Object>> assetList = new ArrayList<>();
        Map<String, Object> mustFilter = new HashMap<>();
        Map<String, Object> mustTermsFilter = new HashMap<>();
        Map<String, Object> mustNotFilter = new HashMap<>();
        HashMultimap<String, Object> shouldFilter = HashMultimap.create();
        mustFilter.put(CommonUtils.convertAttributetoKeyword(Constants.TYPE), Constants.ISSUE);
        mustFilter.put(CommonUtils.convertAttributetoKeyword(Constants.ISSUE_STATUS), Constants.EXEMPTED);
        try {
            assetList = elasticSearchRepository.getDataFromES(assetGroup, null,
                    mustFilter, mustNotFilter, shouldFilter, null, mustTermsFilter);
            return assetList;
        } catch (Exception e) {
            logger.error("Error retrieving inventory from ES in getExemptedAssetCount ", e);
        }
        return assetList;
    }

    private List<String> getUntaggedAssets(String assetGroup) {
        try {
            Map<String, Object> mustFilter = new HashMap<>();
            Map<String, Object> missingTagsMap = new HashMap<>();
            mustFilter.put("type.keyword", "issue");
            mustFilter.put("policyCategory.keyword", "tagging");
            mustFilter.put("issueStatus.keyword", "open");
            mustFilter.put("match", missingTagsMap);
            Set<String> tagsSet = new HashSet<>(Arrays.asList(mandatoryTags.split(",")));
            String mandatoryTagMatchClause = tagsSet.stream().collect(Collectors.joining(" "));
            missingTagsMap.put("missingTags", mandatoryTagMatchClause);
            List<Map<String, Object>> untaggedAssetList = elasticSearchRepository.getDataFromES(assetGroup, null, mustFilter, null, null, Arrays.asList(RESOURCEID), null);
            return untaggedAssetList.stream().map(obj -> (String) obj.get(RESOURCEID)).collect(Collectors.toList());
        } catch (Exception e) {
            logger.error("Error occured while fetching untagged assets");
            return new ArrayList<>();
        }
    }

    private static String fetchInputValFromFilter(Object obj) {
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

    public List<Map<String, Object>> getValueForFilterForAdminPolicy(Map<String, Object> filter, String attributeName, String searchText) throws ServiceException {
        if (attributeName.equalsIgnoreCase("policyId")) {
            attributeName = "policyId, policyDisplayName";
        }  else {
            attributeName = "distinct " + attributeName;
        }

        List<String> filterConditions = new ArrayList<>();
        if (!filter.isEmpty()) {
            for (Map.Entry<String, Object> entry : filter.entrySet()) {
                if (entry.getKey() != null && entry.getValue() != null) {
                    filterConditions.add(createFilterCondition(entry.getKey(), entry.getValue()));
                }
            }
        }
        StringBuilder policyQuery = new StringBuilder("select " + attributeName + " FROM \n" +
                " (SELECT  pt.policyId, pt.severity, pt.category, pt.targetType, pt.autoFixAvailable, pt.autoFixEnabled, pt.policyDisplayName,\n" +
                " pt.assetGroup, @c:=@c+1, target.displayName as targetDisplayName, " +
                " pt.status FROM cf_PolicyTable pt INNER JOIN cf_Target target ON  pt.targetType=target.targetName\n" +
                " LEFT JOIN cf_PolicyParams pp ON pt.policyId = pp.policyID AND pp.paramKey = 'pluginType' INNER JOIN \n" +
                " (select distinct(platform) from cf_Accounts where accountStatus='configured') acct \n" +
                "ON  acct.platform=pp.paramValue OR (pp.paramValue IS NULL AND target.dataSourceName=acct.platform) " +
                "JOIN (SELECT 'critical' as severity, 500 as weight UNION SELECT 'high' as severity, 300 as weight \n" +
                "UNION SELECT 'medium' as severity, 200 as weight UNION SELECT 'low' as severity, 100 as weight) sweights ON sweights.severity=pt.severity\n" +
                "JOIN (SELECT @c:=0) temp  where\n" +
                " (target.status='enabled' or target.status='active') %s \n" +
                " ) policyDet ");

        String combinedFilterCondition = !filterConditions.isEmpty() ? " AND " + filterConditions.stream().collect(Collectors.joining(" AND ")) : "";
        policyQuery = new StringBuilder(String.format(policyQuery.toString(), combinedFilterCondition));
        return rdsepository.getDataFromPacman(policyQuery.toString());
    }

    private String createFilterCondition(String column, Object filterValue) {
        String template = " pt.%s in ( %s ) ";
        List<String> filterValueList = (List<String>) filterValue;
        if (!filterValueList.isEmpty()) {
            String combinedFilterConditionStr = filterValueList.stream().map(str -> "'" + str + "'").collect(Collectors.joining(","));
            return String.format(template, column, combinedFilterConditionStr);
        }
        return "";
    }

    private Map<String ,Object> getDateFilter(Map<String, List<String>> filter){
        Map<String ,Object> dateFilterMap=new HashMap<>();

        Date startDate=null;
        Date endDate=null;

        List<String> dateFilter=filter.get("_loaddate");
        if(dateFilter!=null && !dateFilter.isEmpty()){
            List<String> dateRangeList = filter.get("_loaddate");
            StringBuilder dateRange= new StringBuilder();
            for(String range:dateRangeList){
                dateRange.append(range);
            }
            String[] dates = dateRange.toString().split(" - ");
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            try {
                startDate = dateFormat.parse(dates[0]);
                endDate = dateFormat.parse(dates[1]);

            } catch (ParseException e) {
                logger.error("Error in Date Format");
            }
        }

        String gte = null;
        String lte = null;

        if ( startDate!= null) {
            gte = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(startDate);
        }
        if ( endDate != null) {
            lte = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(endDate);
        }
        Map<String ,Object> loaddateMap=new HashMap<>();

        if(gte!=null){
            loaddateMap.put("gte",gte);
        }

        if(lte!=null){
            loaddateMap.put("lte",lte);
        }
        if(loaddateMap!=null && !loaddateMap.isEmpty()){
            dateFilterMap.put("_loaddate.keyword",loaddateMap);
        }

        return dateFilterMap;
    }


    public Map<String, Long> getNotificationDateFromES(Map<String, List<String>> filter)
            throws DataException {
        Map<String, Object> mustFilter = new HashMap<>();
        Map<String, Object> mustNotFilter = new HashMap<>();
        Map<String, Object> mustTermsFilter = new HashMap<>();

        List<String> eventCategory = filter.get(NOTIFICATION_CATEGEORY_NAME);
        List<String>  eventSource = filter.get( NOTIFICATION_SOURCE_NAME);
        List<String> eventName = filter.get(NOTIFICATION_EVENT_NAME);

        if(eventCategory!=null && !eventCategory.isEmpty()){
            mustFilter.put("eventCategoryName.keyword",eventCategory);
        }

        if(eventSource!=null && !eventSource.isEmpty()) {
            mustFilter.put("eventSourceName.keyword", eventSource);
        }

        if(eventName!=null && !eventName.isEmpty()) {
            mustFilter.put("eventName.keyword", eventName);
        }

        if(!mustFilter.isEmpty()) {
            mustTermsFilter.putAll(mustFilter);
        }

        String aggsFilter = CommonUtils.convertAttributetoKeyword("_loaddate");
        try {
            return elasticSearchRepository.getTotalDistributionForIndexAndType(
                    NOTIFICATION_INDEX, NOTIFICATION_INDEX_TYPE, mustFilter, mustNotFilter, null, aggsFilter,
                    THOUSAND,mustTermsFilter);
        } catch (Exception e) {
            throw new DataException(e);
        }
    }

    private List<Object> getTargetTypeByRuleIds(String assetGroup, List<String> ruleIds, List<String> targetTypes) {
        String ttypesTemp;
        String ruleIdsTemp;
        String rruleIds = null;
        String ttypes = null;
        for (String name : targetTypes) {
            ttypesTemp = new StringBuilder().append('\'').append(name).append('\'').toString();
            if (Strings.isNullOrEmpty(ttypes)) {
                ttypes = ttypesTemp;
            } else {
                ttypes = new StringBuilder().append(ttypes).append(",").append(ttypesTemp).toString();
            }
        }
        for (String rule : ruleIds) {
            ruleIdsTemp = new StringBuilder().append('\'').append(rule).append('\'').toString();
            if (Strings.isNullOrEmpty(rruleIds)) {
                rruleIds = ruleIdsTemp;
            } else {
                rruleIds = new StringBuilder().append(rruleIds).append(",").append(ruleIdsTemp).toString();
            }
        }
        String ruleIdWithTargetTypeQuery = "SELECT policyId, targetType FROM cf_PolicyTable WHERE STATUS = 'ENABLED'AND targetType IN ("
                + ttypes + ") and policyId IN (" + rruleIds + ")";
        List<Map<String, Object>> ruleIdwithTargetTypes = rdsepository.getDataFromPacman(ruleIdWithTargetTypeQuery);

        List<Object> targetRets = ruleIdwithTargetTypes.stream().map(s -> s.get(Constants.TARGET_TYPE)).collect(Collectors.toList());

        return targetRets;
    }

}
