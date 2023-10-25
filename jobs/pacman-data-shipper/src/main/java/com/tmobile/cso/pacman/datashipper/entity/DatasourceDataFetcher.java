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

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.tmobile.cso.pacman.datashipper.dao.RDSDBManager;
import com.tmobile.cso.pacman.datashipper.dto.DatasourceData;
import com.tmobile.cso.pacman.datashipper.util.HttpUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class DatasourceDataFetcher {
    private static final Logger LOGGER = LoggerFactory.getLogger(DatasourceDataFetcher.class);
    private static final String ES_URI = System.getenv("ES_URI");

    private final ObjectMapper objectMapper;

    private DatasourceDataFetcher() {
        objectMapper = new ObjectMapper();
    }

    private static final class InstanceHolder {
        static final DatasourceDataFetcher instance = new DatasourceDataFetcher();
    }

    public static DatasourceDataFetcher getInstance() {
        return InstanceHolder.instance;
    }

    /**
     * Retrieves asset group data for a shipper based on a given datasource. This method
     * fetches information about asset groups, and account IDs associated
     * with the provided datasource.
     *
     * @param datasource The name of the datasource for which asset group data is retrieved.
     * @return A DatasourceData object containing asset groups and account IDs.
     */
    public DatasourceData fetchDatasourceData(String datasource) {
        DatasourceData datasourceData = new DatasourceData();
        try {
            List<String> aliases = getAliasByDatasource(datasource);
            List<String> assetGroups = getVisibleAssetGroupsFiltered(aliases);
            datasourceData.setAssetGroups(assetGroups);
            datasourceData.setAccountIds(getAccountsByDatasource(datasource));
            LOGGER.info("SUCCESS: Count of asset groups : {} and count of account IDs : {}", assetGroups.size(),
                    datasourceData.getAccountIds().size());
            return datasourceData;
        } catch (Exception e) {
            LOGGER.error("Error while constructing datasource related data", e);
        }

        return null;
    }

    /**
     * Retrieves a list of unique alias names associated with a list of Elasticsearch indices.
     * Sends HTTP requests to Elasticsearch to fetch alias information and stores the unique alias names.
     *
     * @return A list of unique alias names associated with the specified indices.
     */
    private List<String> getAliasByDatasource(String datasource) {
        Set<String> uniqueAliases = new HashSet<>();
        try {
            JsonNode rootNode = objectMapper.readTree(fetchAliases(datasource));
            // Loop through each root node to get a list of unique aliases
            rootNode.fields().forEachRemaining(rootNodeEntry -> {
                JsonNode aliases = rootNodeEntry.getValue().path("aliases");
                for (Iterator<Map.Entry<String, JsonNode>> it = aliases.fields(); it.hasNext(); ) {
                    Map.Entry<String, JsonNode> alias = it.next();
                    uniqueAliases.add(alias.getKey());
                }
            });
            return new ArrayList<>(uniqueAliases);
        } catch (Exception e) {
            LOGGER.error("An error occurred while retrieving alias names: " + e.getMessage(), e);
        }

        return new ArrayList<>();
    }

    /**
     * Retrieves a list of visible asset group names that match a given list of asset group names.
     *
     * @param assetListToFilter A list of asset group names to filter by visibility.
     * @return A list of visible asset group names that match the provided asset list.
     */
    private List<String> getVisibleAssetGroupsFiltered(List<String> assetListToFilter) {
        String query = "select distinct groupName from cf_AssetGroupDetails where isVisible = true and groupName in " +
                "('" + String.join("','", assetListToFilter) + "')";

        return RDSDBManager.executeStringQuery(query);
    }

    /**
     * Retrieves a list of account IDs associated with a specific datasource.
     *
     * @param datasource The name of the datasource for which account IDs are retrieved.
     * @return A list of account IDs that meet the criteria.
     */
    private List<String> getAccountsByDatasource(String datasource) {
        String query = "select accountId from cf_Accounts  where platform = '" +
                datasource + "' and accountStatus= 'configured'";

        return RDSDBManager.executeStringQuery(query);
    }

    /**
     * Fetches aliases from Elasticsearch and returns a mapping of aliases to their associated indices as String.
     *
     * @return A mapping of aliases to their associated indices as String.
     * @throws IOException If an error occurs during the retrieval of aliases.
     */
    private String fetchAliases(String datasource) throws Exception {
        String urlToQuery = ES_URI + "/_alias?filter_path=" + datasource + "_*.aliases";

        return HttpUtil.httpGetMethodWithHeaders(urlToQuery, new HashMap<>());
    }
}
