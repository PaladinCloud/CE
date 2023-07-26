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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.google.common.collect.HashMultimap;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Repository;

import com.tmobile.pacman.api.commons.Constants;
import com.tmobile.pacman.api.commons.exception.DataException;
import com.tmobile.pacman.api.commons.repo.ElasticSearchRepository;
import com.tmobile.pacman.api.commons.repo.PacmanRdsRepository;
import com.tmobile.pacman.api.commons.utils.CommonUtils;
import com.tmobile.pacman.api.compliance.client.AssetServiceClient;
import com.tmobile.pacman.api.compliance.domain.AssetApi;
import com.tmobile.pacman.api.compliance.domain.AssetApiData;
import com.tmobile.pacman.api.compliance.domain.AssetCountDTO;

import java.util.stream.Collectors;

import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.TimeUnit;

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
    public Map<String, Long> getNotificationTypesFromES()
            throws DataException {
        Map<String, Object> mustFilter = new HashMap<>();
        Map<String, Object> mustNotFilter = new HashMap<>();
        String aggsFilter = CommonUtils.convertAttributetoKeyword(NOTIFICATION_CATEGEORY_NAME);
        try {
            return elasticSearchRepository.getTotalDistributionForIndexAndType(
                    NOTIFICATION_INDEX, NOTIFICATION_INDEX_TYPE, mustFilter, mustNotFilter, null, aggsFilter,
                    THOUSAND, null);
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
    public Map<String, Long> getNotificationSourceFromES()
            throws DataException {
        Map<String, Object> mustFilter = new HashMap<>();
        Map<String, Object> mustNotFilter = new HashMap<>();
        String aggsFilter = CommonUtils.convertAttributetoKeyword(NOTIFICATION_SOURCE_NAME);
        try {
            return elasticSearchRepository.getTotalDistributionForIndexAndType(
                    NOTIFICATION_INDEX, NOTIFICATION_INDEX_TYPE, mustFilter, mustNotFilter, null, aggsFilter,
                    THOUSAND, null);
        } catch (Exception e) {
            throw new DataException(e);
        }
    }
    public Map<String, Long> getAttributeValuesFromES(String assetGroup, Map<String,Object> filter, String entityType,String attributeName,String targetTypes)
            throws DataException {
        Map<String, Object> mustFilter = new HashMap<>();
        Map<String, Object> mustNotFilter = new HashMap<>();
        Map<String, Object> mustTermsFilter=new HashMap<>();
        HashMultimap<String, Object> shouldFilter = HashMultimap.create();
        checkEntityType(mustFilter,mustTermsFilter,entityType,targetTypes);
        setFilters(filter,mustFilter,shouldFilter,mustTermsFilter,mustNotFilter, assetGroup);
        String aggsFilter=attributeName;
        try {
            if(attributeName.equalsIgnoreCase(CREATED_DATE)){
                //for createdDate .keyword doesn't work, pass it as is
                if(entityType.equalsIgnoreCase(ISSUE)){
                    mustTermsFilter.put("issueStatus.keyword", Arrays.asList("open","exempted"));
                }
                Map<String, Long> totalDistributionForIndexAndType = elasticSearchRepository.getTotalDistributionForIndexAndType(
                        assetGroup, null, mustFilter, mustNotFilter, shouldFilter, aggsFilter,
                        THOUSAND, mustTermsFilter);
                Map<String, Long> resultMap=new HashMap<>();
                for (String key : totalDistributionForIndexAndType.keySet()){
                    Long val=totalDistributionForIndexAndType.get(key);
                    SimpleDateFormat dateFormatter = new SimpleDateFormat("yyyy-MM-dd");
                    BigDecimal bigDecimal = new BigDecimal(key);
                    Date createdDate = new Date(Long.parseLong(bigDecimal.toPlainString()));
                    //Format and parse to get only the date part
                    createdDate=dateFormatter.parse(dateFormatter.format(createdDate));
                    Date now = dateFormatter.parse(dateFormatter.format(new Date()));
                    long diffInMillies = Math.abs(now.getTime() - createdDate.getTime());
                    long diff = TimeUnit.DAYS.convert(diffInMillies, TimeUnit.MILLISECONDS);
                    if (resultMap.containsKey(Long.toString(diff))) {
                        val = resultMap.get(Long.toString(diff)) + val;
                    }
                    resultMap.put(Long.toString(diff), val);
                    if(resultMap.containsKey("0")){
                        Long value = resultMap.get("0");
                        resultMap.remove("0");
                        resultMap.put("< 1",value);
                    }

                }
                return resultMap;
            }else{
                if(StringUtils.endsWith(attributeName,".keyword") || attributeName.contains(AUTOFIX_PLANNED)){
                    aggsFilter=attributeName;
                }else{
                    aggsFilter=CommonUtils.convertAttributetoKeyword(attributeName);
                }
                Map<String, Long> totalDistributionForIndexAndType = elasticSearchRepository.getTotalDistributionForIndexAndType(
                        assetGroup, null, mustFilter, mustNotFilter, shouldFilter, aggsFilter,
                        THOUSAND, mustTermsFilter);
                if(attributeName.contains(AUTOFIX_PLANNED)){
                    updateFinalMap(totalDistributionForIndexAndType);
                }
                return totalDistributionForIndexAndType;
            }
        } catch (Exception e) {
            throw new DataException(e);
        }
    }

    private void setFilters(Map<String, Object> filter, Map<String, Object> mustFilter, HashMultimap<String, Object> shouldFilter,
                            Map<String, Object> mustTermsFilter, Map<String, Object> mustNotFilter, String assetGroup) {
        Iterator it = filter.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry entry = (Map.Entry) it.next();
            String key = (String) entry.getKey();
            if(key.equalsIgnoreCase(FILTER_TAGGED)){
                Set<String> tagsSet = new HashSet<>(Arrays.asList(mandatoryTags.split(",")));
                ArrayList<String> tagList=tagsSet.stream().map(tag->"tags."+tag).collect(Collectors.toCollection(ArrayList::new));
                String filterTagged = "";
                if(filter.get(key) instanceof String){
                    filterTagged = filter.get(key).toString();
                }
                else{
                    filterTagged = ((List<String>) filter.get(key)).get(0);
                }
                if(filterTagged.equalsIgnoreCase(FALSE)){
                    mustFilter.put(FILTER_UNTAGGED, tagList);
                }
                else if (filterTagged.equalsIgnoreCase(TRUE)){
                    mustFilter.put(FILTER_TAGGED, tagList);
                }
            }
            else if(key.equalsIgnoreCase(EXEMPTED)){
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
                    if(filter.get(key) instanceof String){
                        if(((String) filter.get(key)).equalsIgnoreCase(TRUE)){
                            mustFilter.put((String) key,exemptedResourceIds);
                        } else{
                            mustNotFilter.put((String) key,exemptedResourceIds);
                        }
                    }
                    else{
                        List<String> exemptInp = (List<String>) filter.get(key);
                        if(exemptInp != null && exemptInp.size() == 1){
                            if(exemptInp.get(0).equalsIgnoreCase(TRUE)){
                                mustFilter.put((String) key,exemptedResourceIds);
                            } else{
                                mustNotFilter.put((String) key,exemptedResourceIds);
                            }
                        }
                    }

                } catch (Exception e){
                    logger.error("Error in fetching attributes ",e);
                }
            }
            else if(key.equalsIgnoreCase(AUTOFIX_PLANNED)){
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
            }
            else
                mustTermsFilter.put(CommonUtils.convertAttributetoKeyword(key),filter.get(key));
        }


    }

    private void checkEntityType(Map<String, Object> mustFilter, Map<String, Object> mustTermsFilter, String entityType,String targetTypes) {
        if(entityType.equalsIgnoreCase("asset")){
            mustFilter.put(UNDERSCORE_ENTITY, Constants.TRUE);
            mustFilter.put(LATEST, Constants.TRUE);
            mustTermsFilter.put("_entitytype.keyword",Arrays.asList(targetTypes.replace("'","").split(",")));
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

    public Map<String, Long> getNotificationEventNamesFromES()
            throws DataException {
        Map<String, Object> mustFilter = new HashMap<>();
        Map<String, Object> mustNotFilter = new HashMap<>();
        String aggsFilter = CommonUtils.convertAttributetoKeyword(NOTIFICATION_EVENT_NAME);
        try {
            return elasticSearchRepository.getTotalDistributionForIndexAndType(
                    NOTIFICATION_INDEX, NOTIFICATION_INDEX_TYPE, mustFilter, mustNotFilter, null, aggsFilter,
                    THOUSAND, null);
        } catch (Exception e) {
            throw new DataException(e);
        }
    }

    private List<Map<String, Object>> getAssetsExempted(String assetGroup)
    {
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
}