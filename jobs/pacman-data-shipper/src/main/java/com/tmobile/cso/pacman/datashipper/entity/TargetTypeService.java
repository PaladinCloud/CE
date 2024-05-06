package com.tmobile.cso.pacman.datashipper.entity;

import com.amazonaws.services.s3.model.S3Object;
import com.amazonaws.services.s3.model.S3ObjectSummary;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.tmobile.cso.pacman.datashipper.config.ConfigManager;
import com.tmobile.cso.pacman.datashipper.config.S3ClientConfig;
import com.tmobile.cso.pacman.datashipper.dao.RDSDBManager;
import com.tmobile.cso.pacman.datashipper.util.Constants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.sql.SQLIntegrityConstraintViolationException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class TargetTypeService {

    private final String bucketName;
    private final String dataPath;
    private final String esHost;
    private final String esPort;
    private final DateTimeFormatter formatter;
    private final ObjectMapper objectMapper;

    private static final Logger LOGGER = LoggerFactory.getLogger(TargetTypeService.class);

    private TargetTypeService() {
        bucketName = System.getProperty("s3");
        dataPath = System.getProperty("s3.data");
        esHost = System.getProperty("elastic-search.host");
        esPort = System.getProperty("elastic-search.port");
        objectMapper = new ObjectMapper();
        formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    }

    public static TargetTypeService getInstance() {
        return InstanceHolder.instance;
    }

    public void configureNewTargetType(String datasource) {
        try {
            List<S3ObjectSummary> summaryList = S3ClientConfig.getInstance().getS3Client().listObjects(bucketName, dataPath)
                    .getObjectSummaries();
            summaryList = summaryList.stream().filter(sl -> !sl.getKey().contains("-issues") &&
                    !sl.getKey().contains("-policy")).collect(Collectors.toList());
            Map<String, S3ObjectSummary> typesFromS3 = getTypesFromS3(summaryList, datasource);
            Map<String, String> types = ConfigManager.getTypesWithDisplayName(datasource);
            List<String> typesInDB = new ArrayList<>(types.keySet());
            List<String> newTypes = new ArrayList<>(typesFromS3.keySet());
            newTypes.removeAll(typesInDB);
            if (newTypes.isEmpty()) {
                return;
            }
            List<String> keysToRemove = new ArrayList<>();
            typesFromS3.forEach((key, value) -> {
                if (!newTypes.contains(key)) {
                    keysToRemove.add(key);
                }
            });
            keysToRemove.forEach(typesFromS3::remove);
            Map<String, String> typesFromS3Folder = processS3Folder(new ArrayList<>(typesFromS3.values()));
            typesFromS3Folder.forEach((targetType, displayName) -> {
                createNewTargetType(targetType, displayName, datasource);
            });
        } catch (Exception e) {
            LOGGER.error("Error in configuring new target types for {}", datasource, e);
        }
    }

    private Map<String, S3ObjectSummary> getTypesFromS3(List<S3ObjectSummary> summaryList, String datasource) {
        Map<String, S3ObjectSummary> typesObjectSummary = new HashMap<>();
        for (S3ObjectSummary summary : summaryList) {
            String key = summary.getKey();
            if (!key.endsWith(".data")) {
                continue;
            }
            String[] parts = key.split("/");
            String fileName = parts[parts.length - 1];
            String[] fileNameParts = fileName.split("-");
            if (!fileNameParts[0].equalsIgnoreCase(datasource)) {
                continue;
            }
            String type = fileNameParts[1].split("\\.")[0];
            typesObjectSummary.put(type, summary);
        }
        return typesObjectSummary;
    }

    private Map<String, String> processS3Folder(List<S3ObjectSummary> summaryList) {
        Map<String, String> result = new HashMap<>();
        for (S3ObjectSummary objectSummary : summaryList) {
            String fileName = objectSummary.getKey();
            S3Object s3Object = S3ClientConfig.getInstance().getS3Client().getObject(bucketName, fileName);
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(s3Object.getObjectContent()))) {
                String firstLine = reader.readLine();
                JsonNode rootNode = objectMapper.readTree(firstLine);
                if (rootNode.isArray() && rootNode.size() > 0) {
                    JsonNode firstRecord = rootNode.get(0);
                    String targetType = firstRecord.has("targetType") ?
                            firstRecord.get("targetType").asText() : null;
                    String compositePluginAssetType = firstRecord.has("targetTypeDisplayName") ?
                            firstRecord.get("targetTypeDisplayName").asText() : null;
                    if (targetType == null) {
                        continue;
                    }
                    if (compositePluginAssetType == null) {
                        targetType = targetType.toUpperCase();
                    }
                    result.put(targetType, compositePluginAssetType);
                } else {
                    LOGGER.info("JSON array is empty for {}", fileName);
                }
            } catch (IOException e) {
                LOGGER.info("Unable to find targetType from {}", fileName);
            }
        }
        return result;
    }

    private void createNewTargetType(String targetType, String displayName, String datasource) {
        Map<String, String> data = new HashMap<>();
        String description = (datasource + " " + targetType).toUpperCase();
        data.put("targetName", targetType);
        data.put("targetDesc", description);
        data.put("category", description);
        data.put("dataSourceName", datasource);
        data.put("targetConfig", "{\"key\":\"id\",\"id\":\"id\",\"name\":\"name\"}");
        data.put("status", "enabled");
        data.put("userId", "admin@paladincloud.io");
        data.put("endpoint", "http://" + esHost + ":" + esPort + "/" + datasource + "_" + targetType + "/" + targetType);
        data.put("createdDate", LocalDate.now().format(formatter));
        data.put("domain", "Infra & Platforms");
        data.put("displayName", displayName);
        RDSDBManager.insertRecord("cf_Target", data);
    }

    private static class InstanceHolder {
        private static final TargetTypeService instance = new TargetTypeService();
    }
}
