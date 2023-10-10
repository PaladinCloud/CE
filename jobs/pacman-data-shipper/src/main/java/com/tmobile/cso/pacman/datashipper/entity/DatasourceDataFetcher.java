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

    public DatasourceData fetchDatasourceData(String datasource) {
        try {
            String token = AuthManager.getToken();
            String datasourceStringData = HttpUtil.get(ASSET_API_URL
                    + "/list/assetGroupsByDatasource?datasource=" + datasource, token);
            ObjectMapper objectMapper = new ObjectMapper();
            JsonNode rootNode = objectMapper.readTree(datasourceStringData);

            if (rootNode.has("data")) {
                return objectMapper.treeToValue(rootNode.get("data"), DatasourceData.class);
            }
        } catch (Exception e) {
            LOGGER.error("Error while constructing datasource related data", e);
        }
        return null;
    }
}
