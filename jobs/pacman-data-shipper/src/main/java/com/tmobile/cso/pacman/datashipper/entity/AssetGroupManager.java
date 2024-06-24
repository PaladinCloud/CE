/*******************************************************************************
 * Copyright 2023 Paladin Cloud, Inc. or its affiliates. All Rights Reserved.
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License.  You may obtain a copy
 * of the License at
 * <p>
 *   http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  See the
 * License for the specific language governing permissions and limitations under
 * the License.
 ******************************************************************************/
package com.tmobile.cso.pacman.datashipper.entity;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.tmobile.cso.pacman.datashipper.dao.RDSDBManager;
import com.tmobile.cso.pacman.datashipper.es.ESManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

public class AssetGroupManager {
    private static final Logger LOGGER = LoggerFactory.getLogger(AssetGroupManager.class);
    private static final String DATE_FORMAT = "MM/dd/yyyy HH:mm";
    private static final String ASSET_GROUP_FOR_ALL_RESOURCES = "all-sources";
    private static final String FETCH_IMPACTED_ALIAS_QUERY_TEMPLATE = "SELECT DISTINCT agd.groupId, agd.groupName, " +
            "agd.groupType, agd.aliasQuery FROM cf_AssetGroupDetails AS agd " +
            //Checks if data source is selected as cloudType
            "WHERE (agd.groupId NOT IN ( " +
            "    SELECT DISTINCT agcd.groupId " +
            "    FROM cf_AssetGroupCriteriaDetails AS agcd " +
            "    WHERE agcd.attributeName = 'CloudType') AND agd.groupType <> 'user' AND agd.groupType <> 'system' " +
            "AND agd.groupName <> '" + ASSET_GROUP_FOR_ALL_RESOURCES + "' and aliasQuery like '%s') " +
            //for all user asset groups
            "OR (agd.groupType = 'user') " +
            //for current data source
            "OR (agd.groupName = '%s' AND agd.groupType = 'system') " +
            //for all-sources
            "OR (agd.groupName = '" + ASSET_GROUP_FOR_ALL_RESOURCES + "') " +
            "OR (agd.groupType <> 'user' " +
            "    AND agd.groupType <> 'system' AND agd.groupName <> '" + ASSET_GROUP_FOR_ALL_RESOURCES + "' and aliasQuery like '%s')";
    private static final String UPDATE_ES_ALIAS_TEMPLATE = "{\"actions\": [{\"add\": {%s \"index\": \"%s\"," +
            "\"alias\": \"%s\"}}]}";
    private static final String FILTER_TEMPLATE = "\"filter\": %s,";
    private static final String ES_ATTRIBUTE_TAG = "tags.";
    private static final String ES_ATTRIBUTE_KEYWORD = ".keyword";
    private static final String DISTINCT_DATA_SOURCES_QUERY_TEMPLATE = "SELECT DISTINCT dataSourceName FROM cf_Target";
    private static final String CHECK_PLUGIN_ENABLED_QUERY_TEMPLATE =
            "SELECT `value` FROM pac_config_properties WHERE cfkey = '%s.enabled'";
    private static final String CRITERIA_DETAILS_QUERY_TEMPLATE =
            "SELECT attributeName, attributeValue FROM cf_AssetGroupCriteriaDetails WHERE groupId = '%s'";
    private static final Map<String, List<String>> dbCache = new HashMap<>();
    private static final List<String> datasourceCache = new ArrayList<>();
    private static final Map<String, List<Map<String, String>>> assetGroupTagsCache = new HashMap<>();

    private final ObjectMapper objectMapper;

    private AssetGroupManager() {
        objectMapper = new ObjectMapper();
    }

    public static AssetGroupManager getInstance() {
        return InstanceHolder.instance;
    }

    private static Map<String, Object> buildQueryForUserAssetGroup(List<Map<String, String>> assetGroupTags) {
        List<Object> mustList = new ArrayList<>();
        Map<String, Object> mustObj = Maps.newHashMap();
        Map<String, List<String>> groupedTags = assetGroupTags.stream()
                .collect(Collectors.groupingBy(
                        map -> map.get("attributeName"),
                        Collectors.mapping(map -> map.get("attributeValue"), Collectors.toList())
                ));
        groupedTags.forEach((tagName, tags) -> mustList.add(getShouldMapForStakeholderAssetGroup(tagName, tags)));
        mustObj.put("must", mustList);
        return mustObj;
    }

    private static Map<String, Object> getShouldMapForStakeholderAssetGroup(String tagName, List<String> tags) {
        List<Object> matchList = new ArrayList<>();
        Map<String, Object> shouldObj = Maps.newHashMap();
        if (tags.size() == 1) {
            Map<String, Object> attributeObj = Maps.newHashMap();
            Map<String, Object> match = Maps.newHashMap();
            attributeObj.put(ES_ATTRIBUTE_TAG + tagName + ES_ATTRIBUTE_KEYWORD, tags.get(0));
            match.put("match", attributeObj);
            return match;
        } else {
            Map<String, Object> boolObj = Maps.newHashMap();
            tags.forEach(value -> {
                Map<String, Object> attributeObj = Maps.newHashMap();
                Map<String, Object> match = Maps.newHashMap();
                attributeObj.put(ES_ATTRIBUTE_TAG + tagName + ES_ATTRIBUTE_KEYWORD, value);
                match.put("match", attributeObj);
                matchList.add(match);
            });
            shouldObj.put("should", matchList);
            shouldObj.put("minimum_should_match", 1);
            boolObj.put("bool", shouldObj);
            return boolObj;
        }
    }

    private List<String> getCachedResultOrFetch(String query) {
        if (dbCache.containsKey(query)) {
            return dbCache.get(query);
        } else {
            List<String> result = RDSDBManager.executeStringQuery(query);
            dbCache.put(query, result);
            return result;
        }
    }

    private List<String> getCachedDatasourceOrFetch(String currentDatasource) {
        if (datasourceCache.isEmpty()) {
            List<String> enabledDatasource = getEnabledDatasource(currentDatasource);
            datasourceCache.addAll(enabledDatasource);
        }
        return datasourceCache;
    }

    private List<Map<String, String>> getCachedAssetGroupTagsOrFetch(String groupId) {
        if (assetGroupTagsCache.containsKey(groupId)) {
            return assetGroupTagsCache.get(groupId);
        } else {
            List<Map<String, String>> result = RDSDBManager.executeQuery(String.format(
                    CRITERIA_DETAILS_QUERY_TEMPLATE, groupId));
            assetGroupTagsCache.put(groupId, result);
            return result;
        }
    }

    public void updateImpactedAliases(List<String> newIndices, String datasource) {
        // IS_ALL_RESOURCE_EXISTS_QUERY returns 1 if ASSET_GROUP_FOR_ALL_RESOURCES present in DB
        try {
            String queryForAllResources = "SELECT EXISTS (SELECT 1 FROM " +
                    "cf_AssetGroupDetails WHERE groupName = '" + ASSET_GROUP_FOR_ALL_RESOURCES + "') AS row_exists";
            Optional<String> isAllResourceExists = RDSDBManager.executeStringQuery(queryForAllResources)
                    .stream().findFirst();
            if (isAllResourceExists.isPresent() && isAllResourceExists.get().equalsIgnoreCase("0")) {
                // Creates ASSET_GROUP_FOR_ALL_RESOURCES if not present
                String aliasQuery = getAliasQueryForDefaultAssetGroup(datasource);
                createDefaultAssetGroup(aliasQuery);
                ESManager.invokeAPI("POST", "_aliases/", aliasQuery);
                LOGGER.info("Created default asset group with group name as {}", ASSET_GROUP_FOR_ALL_RESOURCES);
            }
        } catch (Exception e) {
            LOGGER.error("Unexpected error occurred while creating default asset group", e);
        }
        if (newIndices.isEmpty()) {
            return;
        }

        LOGGER.info("Updating impacted Aliases for following indices {} and datasource {}", newIndices, datasource);
        List<Map<String, String>> assetGroupsList = RDSDBManager.executeQuery(String
                .format(FETCH_IMPACTED_ALIAS_QUERY_TEMPLATE, "%_*%", datasource, "%" + datasource + "_*%"));
        LOGGER.info("Found {} asset groups", assetGroupsList.size());
        // If the assetGroupsList is empty then the asset group for current datasource is not available
        if (assetGroupsList.isEmpty()) {
            LOGGER.error("Cannot update because assetGroupsList is empty for datasource : {}", datasource);
            return;
        }
        String filter;
        List<String> updatedAssetGroupsList = new ArrayList<>();
        List<String> jsonStringsForActions = new ArrayList<>();
        int updatedAssetGroups = 0;
        for (Map<String, String> assetGroup : assetGroupsList) {
            String alias = assetGroup.get("groupName");
            String groupType = assetGroup.get("groupType");
            String existingAliasQuery = assetGroup.get("aliasQuery");
            String groupId = assetGroup.get("groupId");
            if (alias == null || groupType == null || groupId == null) {
                LOGGER.info("Cannot update because one of the fields is null. Alias: " + alias + ", GroupType: " +
                        groupType + ", GroupId: " + groupId);
                continue;
            }
            boolean isUserAssetGroupWithNullAliasQuery = (existingAliasQuery == null ||
                    existingAliasQuery.equalsIgnoreCase("null")) && groupType.equalsIgnoreCase("user");
            if (isUserAssetGroupWithNullAliasQuery) {
                List<Map<String, String>> assetGroupTags = getCachedAssetGroupTagsOrFetch(groupId);
                if (assetGroupTags.isEmpty()) {
                    LOGGER.error("assetGroupTags is empty for groupId : {}", groupId);
                    continue;
                }
                jsonStringsForActions.add(createAliasForStakeholderAssetGroup(alias, assetGroupTags, datasource));
                updatedAssetGroups++;
                updatedAssetGroupsList.add(alias);
            } else if ((alias.equalsIgnoreCase(ASSET_GROUP_FOR_ALL_RESOURCES)
                    || alias.equalsIgnoreCase(datasource))) {
                filter = "";
                jsonStringsForActions.add(String.format(UPDATE_ES_ALIAS_TEMPLATE, filter, datasource + "_*", alias));
                updatedAssetGroups++;
                updatedAssetGroupsList.add(alias);
            } else if ((groupType.equalsIgnoreCase("user") && existingAliasQuery != null &&
                    !existingAliasQuery.equalsIgnoreCase("null") &&
                    !alias.equalsIgnoreCase(ASSET_GROUP_FOR_ALL_RESOURCES) &&
                    !alias.equalsIgnoreCase(datasource)) ||
                    (existingAliasQuery != null && existingAliasQuery.contains("_*"))) {
                String filterContents = getFilterFromExistingAliasQuery(existingAliasQuery);
                filter = filterContents.isEmpty() ? "" :
                        String.format(FILTER_TEMPLATE, filterContents);
                jsonStringsForActions.add(String.format(UPDATE_ES_ALIAS_TEMPLATE, filter, datasource + "_*", alias));
                updatedAssetGroups++;
                updatedAssetGroupsList.add(alias);
            } else {
                LOGGER.error("Cannot update alias {} for existingAliasQuery {}", alias, existingAliasQuery);
            }
        }
        try {
            JsonNode combinedActions = mergeActions(jsonStringsForActions);
            if (combinedActions == null || combinedActions.toString().length() == 0) {
                LOGGER.error("Update failed, aliasUpdateQuery is empty for datasource {}", datasource);
                return;
            }
            String combinedJson = "{\"actions\":" + combinedActions + "}";
            ESManager.invokeAPI("POST", "_aliases/", combinedJson);
        } catch (Exception e) {
            LOGGER.error("Error while updating alias for datasource {}", datasource, e);
        }
        // We need to update asset groups only once, because we added datasource_* in its ES query
        LOGGER.info("Finished Updating impacted Aliases for following indices {} and datasource {}. " +
                        "Updated {} asset groups having names {}", newIndices, datasource, updatedAssetGroups,
                updatedAssetGroupsList);
    }

    private JsonNode mergeActions(List<String> jsonStrings) {
        List<JsonNode> actionsList = new ArrayList<>();
        for (String jsonString : jsonStrings) {
            try {
                JsonNode jsonObj = objectMapper.readTree(jsonString);
                if (jsonObj.has("actions")) {
                    JsonNode actions = jsonObj.get("actions");
                    actionsList.addAll(convertToList(actions));
                }
            } catch (Exception e) {
                LOGGER.error("Error in combining actions", e);
            }
        }
        return objectMapper.valueToTree(actionsList);
    }

    private List<JsonNode> convertToList(JsonNode node) {
        List<JsonNode> list = new ArrayList<>();
        if (node.isArray()) {
            node.forEach(list::add);
        } else {
            list.add(node);
        }
        return list;
    }

    private void createDefaultAssetGroup(String aliasQuery) {
        Map<String, String> data = new HashMap<>();
        data.put("groupId", UUID.randomUUID().toString());
        data.put("groupName", ASSET_GROUP_FOR_ALL_RESOURCES);
        data.put("dataSource", "");
        data.put("displayName", "All Sources");
        data.put("groupType", "system");
        data.put("createdBy", "admin@paladincloud.io");
        data.put("createdUser", "admin@paladincloud.io");
        data.put("modifiedUser", "admin@paladincloud.io");
        data.put("description", "All assets from all Sources");
        data.put("aliasQuery", aliasQuery);
        data.put("isVisible", "1");
        RDSDBManager.insertRecord("cf_AssetGroupDetails", data, null);
    }

    private String getAliasQueryForDefaultAssetGroup(String currentDatasource) {
        Map<String, Object> alias = Maps.newHashMap();
        List<Object> action = Lists.newArrayList();
        try {
            List<String> dataSourceList = getCachedDatasourceOrFetch(currentDatasource);
            for (String ds : dataSourceList) {
                Map<String, Object> addObj = Maps.newHashMap();
                addObj.put("index", ds.toLowerCase().trim() + "_*");
                addObj.put("alias", ASSET_GROUP_FOR_ALL_RESOURCES);
                Map<String, Object> add = Maps.newHashMap();
                add.put("add", addObj);
                action.add(add);
            }
            alias.put("actions", action);
            return objectMapper.writeValueAsString(alias);
        } catch (JsonProcessingException e) {
            LOGGER.error("Exception in creating alias query", e);
        }
        return "";
    }

    /**
     * Retrieves a list of enabled data sources, ensuring each data source is enabled.
     * If the provided 'currentDatasource' is not included in the retrieved list, it is added.
     *
     * @param currentDatasource The current data source to be included in the list if not present.
     * @return A List of enabled data sources that have indices, including the 'currentDatasource' if necessary.
     */
    private List<String> getEnabledDatasource(String currentDatasource) {
        List<String> dataSourceList = getCachedResultOrFetch(DISTINCT_DATA_SOURCES_QUERY_TEMPLATE);
        if (dataSourceList.isEmpty()) {
            LOGGER.error("Data source count is zero");
        }
        // Each datasource should be enabled.
        dataSourceList = dataSourceList.stream().filter(ds -> {
            Optional<String> isEnabled = getCachedResultOrFetch(
                    String.format(CHECK_PLUGIN_ENABLED_QUERY_TEMPLATE, ds)).stream().findFirst();
            return isEnabled.isPresent() && isEnabled.get().equalsIgnoreCase(Boolean.TRUE.toString());
        }).collect(Collectors.toList());
        if (dataSourceList.isEmpty()) {
            LOGGER.error("Count of enabled Data source is zero");
        }
        // If none of the existing data sources is enabled or does not contain the current data source,
        // we will add the current data source. We assume that Shipper ran because the current data source is enabled.
        if (!dataSourceList.contains(currentDatasource)) {
            dataSourceList.add(currentDatasource);
        }
        Set<String> uniqueDataSource = new HashSet<>(dataSourceList);
        uniqueDataSource.addAll(getDatasourceFromEnabledCompositePlugins());
        // return at least one datasource
        return new ArrayList<>(uniqueDataSource);
    }

    private Set<String> getDatasourceFromEnabledCompositePlugins() {
        Set<String> enabledPlugins = new HashSet<>();
        String compositePlugins = System.getProperty("composite.plugins");
        if (compositePlugins == null || compositePlugins.isEmpty()) {
            return enabledPlugins;
        }
        List<String> compositePluginsList = Arrays.stream(compositePlugins.split(","))
                .collect(Collectors.toList());
        for (String cp : compositePluginsList) {
            String isPluginEnabled = System.getProperty(cp + ".enabled");
            if (isPluginEnabled == null || isPluginEnabled.isEmpty() || isPluginEnabled.equalsIgnoreCase("false")
                    || isPluginEnabled.equalsIgnoreCase("0")) {
                continue;
            }
            String availablePlugins = System.getProperty(cp + ".available.clouds");
            List<String> availablePluginList = Arrays.stream(availablePlugins.split(","))
                    .collect(Collectors.toList());
            enabledPlugins.addAll(availablePluginList);
        }
        return enabledPlugins;
    }

    private String getFilterFromExistingAliasQuery(String existingAliasQuery) {
        try {
            JsonNode rootNode = objectMapper.readTree(existingAliasQuery);
            if (rootNode != null && rootNode.has("actions")) {
                rootNode = rootNode.get("actions");
                for (JsonNode actionNode : rootNode) {
                    JsonNode addNode = actionNode.path("add");
                    if (addNode.get("index").isMissingNode() || !addNode.get("index").toString().contains("_*")) {
                        continue;
                    }
                    JsonNode filterNode = addNode.get("filter");
                    if (filterNode != null && !filterNode.isMissingNode()) {
                        return filterNode.toString();
                    }
                }
            }
        } catch (Exception e) {
            LOGGER.error("Error while extracting filter from existingAliasQuery : {}", existingAliasQuery, e);
        }
        return "";
    }

    private String createAliasForStakeholderAssetGroup(String assetGroup, List<Map<String, String>> assetGroupTags,
                                                       String currentDatasource) {
        Map<String, Object> alias = Maps.newHashMap();
        List<Object> action = Lists.newArrayList();
        try {
            List<String> dataSourceList = getCachedDatasourceOrFetch(currentDatasource);
            Map<String, Object> tagMap = buildQueryForUserAssetGroup(assetGroupTags);
            for (String datasource : dataSourceList) {
                Map<String, Object> addObj = Maps.newHashMap();
                addObj.put("index", datasource.toLowerCase().trim() + "_*");
                addObj.put("alias", assetGroup);
                Map<String, Object> filterDetails = Maps.newHashMap();
                filterDetails.put("bool", tagMap);
                addObj.put("filter", filterDetails);
                Map<String, Object> add = Maps.newHashMap();
                add.put("add", addObj);
                action.add(add);
            }
            alias.put("actions", action);
            return objectMapper.writeValueAsString(alias);
        } catch (JsonProcessingException e) {
            LOGGER.error("Error while updating alias for asset group {}", alias, e);
        } catch (Exception exception) {
            LOGGER.error("Exception in creating alias query", exception);
        }
        return "";
    }

    private static final class InstanceHolder {
        static final AssetGroupManager instance = new AssetGroupManager();
    }
}
