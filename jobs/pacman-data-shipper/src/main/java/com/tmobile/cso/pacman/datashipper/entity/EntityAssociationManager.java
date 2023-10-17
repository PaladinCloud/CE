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
package com.tmobile.cso.pacman.datashipper.entity;

import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.amazonaws.services.s3.model.GetObjectRequest;
import com.amazonaws.services.s3.model.ListObjectsV2Request;
import com.amazonaws.services.s3.model.S3Object;
import com.amazonaws.services.s3.model.S3ObjectSummary;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.tmobile.cso.pacman.datashipper.config.ConfigManager;
import com.tmobile.cso.pacman.datashipper.config.CredentialProvider;
import com.tmobile.cso.pacman.datashipper.error.ErrorManager;
import com.tmobile.cso.pacman.datashipper.es.ESManager;
import com.tmobile.cso.pacman.datashipper.util.Util;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;

import static com.tmobile.cso.pacman.datashipper.util.AppConstants.*;
import static com.tmobile.pacman.commons.utils.Constants.*;

public class EntityAssociationManager {

    private static final Logger LOGGER = LoggerFactory.getLogger(EntityAssociationManager.class);

    /**
     * Execute.
     *
     * @param dataSource the data source
     * @param type       the type
     * @return the list
     */
    public List<Map<String, String>> uploadAssociationInfo(String dataSource, String type) {
        LOGGER.info("Started EntityAssociationDataCollector for {}", type);
        List<Map<String, String>> errorList = new ArrayList<>();
        AmazonS3 s3Client = AmazonS3ClientBuilder.standard().withCredentials(
                new AWSStaticCredentialsProvider(new CredentialProvider().getCredentials(S3_ACCOUNT, S3_ROLE))).withRegion(S3_REGION).build();
        ObjectMapper objectMapper = new ObjectMapper();

        String indexName = null;
        try {
            indexName = dataSource + "_" + type;
            String filePrefix = dataSource + "-" + type + "-";
            List<String> childTypes = new ArrayList<>();
            for (S3ObjectSummary objectSummary : s3Client.listObjectsV2(new ListObjectsV2Request().withBucketName(BUCKET_NAME).withPrefix(DATA_PATH + "/" + filePrefix)).getObjectSummaries()) {
                String fileName = objectSummary.getKey().replace(DATA_PATH + "/", "").replace(".data", "");
                if (fileName.chars().filter(ch -> ch == '-').count() == 2) {
                    childTypes.add(fileName.replace(filePrefix, ""));
                }
            }
            String key = ConfigManager.getKeyForType(dataSource, type);
            if (!childTypes.isEmpty()) {
                for (String childType : childTypes) {
                    String childTypeES = type + "_" + childType;
                    if (!childType.equalsIgnoreCase("tags")) {
                        ESManager.createType(indexName, childTypeES, type);
                        LOGGER.info("Fetching data for {}", childTypeES);
                        List<Map<String, Object>> entities = new ArrayList<>();
                        S3Object entitiesData = s3Client.getObject(new GetObjectRequest(BUCKET_NAME, DATA_PATH + "/" + filePrefix + childType + ".data"));
                        try (BufferedReader reader = new BufferedReader(new InputStreamReader(entitiesData.getObjectContent()))) {
                            entities = objectMapper.readValue(reader.lines().collect(Collectors.joining("\n")), new TypeReference<List<Map<String, Object>>>() {
                            });
                        }
                        String loaddate = new SimpleDateFormat("yyyy-MM-dd HH:mm:00Z").format(new java.util.Date());
                        entities.parallelStream().forEach((obj) -> {

                            obj.put("_loaddate", loaddate);
                            obj.put("docType", childTypeES);
                            String parentID = Util.concatenate(obj, key.split(","), "_");
                            if ("aws".equalsIgnoreCase(dataSource)) {
                                if (Arrays.asList(key.split(",")).contains("accountid")) {
                                    parentID = dataSource + "_" + type + "_" + parentID;
                                }
                            }
                            Map<String, Object> relMap = new HashMap<>();
                            relMap.put("name", childTypeES);
                            relMap.put("parent", parentID);
                            obj.put(type + "_relations", relMap);
                        });

                        LOGGER.info("Collected :  {}", entities.size());
                        if (!entities.isEmpty()) {
                            ErrorManager.getInstance(dataSource).handleError(indexName, childTypeES, loaddate, errorList, false);
                            ESManager.uploadData(indexName, type, childTypeES, entities, key.split(","), dataSource);
                            ESManager.deleteOldDocuments(indexName, childTypeES, "_loaddate.keyword",
                                    loaddate);
                        } else {
                            ErrorManager.getInstance(dataSource).handleError(indexName, childTypeES, loaddate, errorList, false);
                            ESManager.deleteOldDocuments(indexName, childTypeES, "_loaddate.keyword",
                                    loaddate);
                        }
                    }
                }
            }
        } catch (Exception e) {
            LOGGER.error("Error in populating child tables", e);
            LOGGER.error("Child tables for: ", indexName);
            Map<String, String> errorMap = new HashMap<>();
            errorMap.put(ERROR, "Error in populating child tables");
            errorMap.put(ERROR_TYPE, WARN);
            errorMap.put(EXCEPTION, e.getMessage());
            errorList.add(errorMap);
        }

        LOGGER.info("Completed EntityAssociationDataCollector for {}", type);
        return errorList;
    }
}
