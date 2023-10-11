package com.tmobile.cso.pacman.datashipper.entity;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.tmobile.cso.pacman.datashipper.dao.RDSDBManager;
import com.tmobile.cso.pacman.datashipper.dto.DatasourceData;
import com.tmobile.cso.pacman.datashipper.util.AuthManager;
import com.tmobile.cso.pacman.datashipper.util.HttpUtil;
import com.tmobile.pacman.commons.utils.CommonUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class DatasourceDataFetcher {

    private static final Logger LOGGER = LoggerFactory.getLogger(DatasourceDataFetcher.class);
    private static final String ES_URI = System.getenv("ES_URI");

    private static DatasourceDataFetcher instance;

    private DatasourceDataFetcher() {
    }

    public static DatasourceDataFetcher getInstance() {
        if (instance == null) {
            instance = new DatasourceDataFetcher();
        }
        return instance;
    }

    /**
     * Retrieves asset group data for a shipper based on a given datasource. This method
     * fetches information about asset group domains, target types, and account IDs associated
     * with the provided datasource.
     *
     * @param datasource The name of the datasource for which asset group data is retrieved.
     * @return A DatasourceData object containing asset group domains and account IDs.
     */
    public DatasourceData fetchDatasourceData(String datasource) {
        DatasourceData datasourceData = new DatasourceData();
        try {
            List<String> aliases = getAliasByDatasource(datasource);
            List<String> assetGroups = getVisibleAssetGroupsFiltered(aliases);
            datasourceData.setAssetGroups(assetGroups);
            datasourceData.setAccountIds(getAccountsByDatasource(datasource));
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
        try {
            return fetchAliases().entrySet().stream()
                    .filter(entry -> entry.getValue().stream().anyMatch(value -> value.startsWith(datasource + "_")))
                    .map(Map.Entry::getKey)
                    .collect(Collectors.toList());
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
        return  RDSDBManager.executeStringQuery(query);
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
     * Fetches aliases from Elasticsearch and returns a mapping of aliases to their associated indices.
     *
     * @return A mapping of aliases to their associated indices.
     * @throws IOException If an error occurs during the retrieval of aliases.
     */
    private Map<String, List<String>> fetchAliases() throws Exception {
        String urlToQuery = ES_URI + "/_cat/aliases";
        String responseDetails = HttpUtil.httpGetMethodWithHeaders(urlToQuery, new HashMap<>());

        String[] lines = responseDetails.split("\n");
        Map<String, List<String>> aliasIndexMap = new HashMap<>();
        for (String line : lines) {
            String[] parts = line.split("\\s+");
            if (parts.length >= 2) {
                String alias = parts[0];             // First part is the alias
                String index = parts[1];             // Second part is the index
                // If alias already exists in the map, add the index to its list
                // Otherwise, create a new list for the alias and add the index
                aliasIndexMap.computeIfAbsent(alias, k -> new ArrayList<>()).add(index);
            }
        }
        return aliasIndexMap;
    }
}
