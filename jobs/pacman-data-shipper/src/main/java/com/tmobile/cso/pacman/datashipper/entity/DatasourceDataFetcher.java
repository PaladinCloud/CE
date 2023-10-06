package com.tmobile.cso.pacman.datashipper.entity;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.tmobile.cso.pacman.datashipper.dto.DatasourceData;
import com.tmobile.cso.pacman.datashipper.util.AuthManager;
import com.tmobile.cso.pacman.datashipper.util.HttpUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DatasourceDataFetcher {

    private static final Logger LOGGER = LoggerFactory.getLogger(DatasourceDataFetcher.class);

    /**
     * The Constant asstApiUri.
     */
    private static final String ASSET_API_URL = System.getenv("ASSET_API_URL");

    private static DatasourceDataFetcher instance;

    private DatasourceDataFetcher() {
    }

    public static DatasourceDataFetcher getInstance() {
        if (instance == null) {
            instance = new DatasourceDataFetcher();
        }
        return instance;
    }

    public DatasourceData fetchDatasourceData(String datasource) throws IOException {
        DatasourceData datasourceData = new DatasourceData();

        try {
            String token = AuthManager.getToken();
            String datasourceStringData = HttpUtil.get(ASSET_API_URL
                    + "/list/assetGroupsByDatasource?datasource=" + datasource, token);
            ObjectMapper objectMapper = new ObjectMapper();
            JsonNode rootNode = objectMapper.readTree(datasourceStringData);

            if (rootNode.has("data")) {
                JsonNode dataNode = rootNode.get("data");

                if (dataNode.has("accountIds")) {
                    List<String> accountIds = new ArrayList<>();
                    JsonNode accountIdsNode = dataNode.get("accountIds");
                    for (JsonNode accountIdNode : accountIdsNode) {
                        accountIds.add(accountIdNode.asText());
                    }
                    datasourceData.setAccountIds(accountIds);
                }

                if (dataNode.has("assetGroupDomains")) {
                    JsonNode domainNode = dataNode.get("assetGroupDomains");
                    Map<String, List<String>> assetGroupDomains = new HashMap<>();
                    domainNode.fieldNames().forEachRemaining((nodeName) -> {
                        List<String> domains = new ArrayList<>();
                        JsonNode assetGroupsNode = domainNode.get(nodeName);
                        for (JsonNode assetGroupNode : assetGroupsNode) {
                            domains.add(assetGroupNode.asText());
                        }
                        assetGroupDomains.put(nodeName, domains);
                    });
                    datasourceData.setAssetGroupDomains(assetGroupDomains);
                }
            }
        } catch (Exception e) {
            LOGGER.error("Error while constructing datasource related data", e);
            return null;
        }

        return datasourceData;
    }
}
