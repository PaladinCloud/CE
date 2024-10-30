package com.tmobile.cso.pacman.datashipper.entity;

import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.amazonaws.services.s3.model.GetObjectRequest;
import com.amazonaws.services.s3.model.S3Object;
import com.amazonaws.util.StringUtils;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.tmobile.cso.pacman.datashipper.config.CredentialProvider;
import com.tmobile.cso.pacman.datashipper.es.ESManager;
import com.tmobile.cso.pacman.datashipper.util.Constants;
import com.tmobile.pacman.commons.utils.ESUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;

public class AllAssetsAssociationManager {

    private static final Logger LOGGER = LoggerFactory.getLogger(AllAssetsAssociationManager.class);

    private static final String S3_ACCOUNT = System.getProperty("base.account");
    private static final String S3_REGION = System.getProperty("base.region");
    private static final String S3_ROLE = System.getProperty("s3.role");
    private static final String BUCKET_NAME = System.getProperty("s3");
    private static final String DATA_PATH = System.getProperty("s3.data");
    private static final String DATE_FORMAT_SEC = "yyyy-MM-dd HH:mm:00Z";
    private static final Map<String, String> sourceFileToIndexMapping = new HashMap<>(2);

    static {
        sourceFileToIndexMapping.put("%s-all_asset", "%s_all_asset");
    }

    public List<Map<String, String>> uploadAllAssets(String dataSource) {
        LOGGER.info("Started AllAssets collection for - {}", dataSource);
        List<Map<String, String>> errorList = new ArrayList<>();
        AmazonS3 s3Client = AmazonS3ClientBuilder.standard().withCredentials(
                new AWSStaticCredentialsProvider(new CredentialProvider().getCredentials(S3_ACCOUNT, S3_ROLE))).withRegion(S3_REGION).build();
        ObjectMapper objectMapper = new ObjectMapper();

        for (Map.Entry<String, String> entry : sourceFileToIndexMapping.entrySet()) {
            try {
                String indexName = String.format(entry.getValue(), dataSource);
                String filePrefix = String.format(entry.getKey(), dataSource);
                List<Map<String, Object>> entities;
                S3Object entitiesData = s3Client.getObject(new GetObjectRequest(BUCKET_NAME, DATA_PATH + "/" + filePrefix + ".data"));
                try (BufferedReader reader = new BufferedReader(new InputStreamReader(entitiesData.getObjectContent()))) {
                    entities = objectMapper.readValue(reader.lines().collect(Collectors.joining("\n")), new TypeReference<List<Map<String, Object>>>() {
                    });
                } catch (Exception e) {
                    LOGGER.info("{} data is empty", filePrefix);
                    continue;
                }
                if (Objects.isNull(entities)) {
                    LOGGER.info("{} object is empty for dataSource - {}", filePrefix, dataSource);
                    continue;
                }
                String url = ESUtils.getEsUrl();
                if (!ESUtils.isValidIndex(url, indexName)) {
                    ESUtils.createIndex(url, indexName);
                }
                String loaddate = new SimpleDateFormat(DATE_FORMAT_SEC).format(new Date());
                entities.parallelStream()
                        .forEach((obj) -> {
                            obj.remove("closedDate");
                            obj.put("_loaddate", loaddate);
                        });
                LOGGER.info("Collected vulnerabilities: {}", entities.size());
                ESManager.uploadAllAssetsData(indexName, entities);
                ESManager.deleteOldDocuments(indexName, null, "_loaddate.keyword", loaddate);
            } catch (Exception e) {
                LOGGER.error("Error in shipping vulnerability data for dataSource - {}", dataSource);
                Map<String, String> errorMap = new HashMap<>();
                errorMap.put(Constants.ERROR, "Exception in collecting vulnerability data for " + dataSource + " from " + DATA_PATH);
                errorMap.put(Constants.ERROR_TYPE, Constants.WARN);
                errorMap.put(Constants.EXCEPTION, e.getMessage());
                errorList.add(errorMap);
            }
        }
        LOGGER.info("Completed Vulnerability collection for {}", dataSource);
        return errorList;
    }
}
