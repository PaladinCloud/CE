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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
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
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.TimeUnit;

/**
 * The Class FilterRepositoryImpl.
 */
@Repository
public class FilterRepositoryImpl implements FilterRepository, Constants {

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
    public Map<String, Long> getRegionsFromES(String assetGroup)
            throws DataException {
        Map<String, Object> mustFilter = new HashMap<>();
        Map<String, Object> mustNotFilter = new HashMap<>();
        String aggsFilter = CommonUtils.convertAttributetoKeyword("region");
        try {
            return elasticSearchRepository.getTotalDistributionForIndexAndType(
                    assetGroup, null, mustFilter, mustNotFilter, null, aggsFilter,
                    THOUSAND, null);
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
    public Map<String, Long> getSeveritiesFromES(String assetGroup)
            throws DataException {
        Map<String, Object> mustFilter = new HashMap<>();
        Map<String, Object> mustNotFilter = new HashMap<>();
        String aggsFilter = CommonUtils.convertAttributetoKeyword("severity");
        try {
            return elasticSearchRepository.getTotalDistributionForIndexAndType(
                    assetGroup, null, mustFilter, mustNotFilter, null, aggsFilter,
                    THOUSAND, null);
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
    public Map<String, Long> getAttributeValuesFromES(String assetGroup, String attributeName, String entityType)
            throws DataException {
        Map<String, Object> mustFilter = new HashMap<>();
        Map<String, Object> mustNotFilter = new HashMap<>();
        if(entityType.equalsIgnoreCase("asset")){
            mustFilter.put(UNDERSCORE_ENTITY, Constants.TRUE);
        }else if(entityType.equalsIgnoreCase("issue")){
            mustFilter.put(Constants.TYPE, "issue");
        }

        String aggsFilter=attributeName;
        try {
            if(attributeName.equalsIgnoreCase(CREATED_DATE)){
                //for createdDate .keyword doesn't work, pass it as is
                Map<String, Object> mustTermsFilter=new HashMap<>();
                if(entityType.equalsIgnoreCase(ISSUE)){
                        mustTermsFilter.put("issueStatus.keyword", Arrays.asList("open","exempted"));
                }
                Map<String, Long> totalDistributionForIndexAndType = elasticSearchRepository.getTotalDistributionForIndexAndType(
                        assetGroup, null, mustFilter, mustNotFilter, null, aggsFilter,
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
                aggsFilter=CommonUtils.convertAttributetoKeyword(attributeName);
                return elasticSearchRepository.getTotalDistributionForIndexAndType(
                        assetGroup, null, mustFilter, mustNotFilter, null, aggsFilter,
                        THOUSAND, null);
            }
        } catch (Exception e) {
            throw new DataException(e);
        }
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

}
