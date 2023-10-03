/*******************************************************************************
 * Copyright 2018 T Mobile, Inc. or its affiliates. All Rights Reserved.
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

import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.amazonaws.services.s3.model.GetObjectRequest;
import com.amazonaws.services.s3.model.S3Object;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.tmobile.cso.pacman.datashipper.config.CredentialProvider;
import com.tmobile.cso.pacman.datashipper.es.ESManager;
import com.tmobile.cso.pacman.datashipper.util.Constants;
import com.tmobile.cso.pacman.datashipper.util.Util;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;

/**
 * The Class ViolationAssociationManager.
 */
public class ViolationAssociationManager {
    private static final Logger LOGGER = LoggerFactory.getLogger(ViolationAssociationManager.class);

    private static final String S3_ACCOUNT = System.getProperty("base.account");
    private static final String S3_REGION = System.getProperty("base.region");
    private static final String S3_ROLE = System.getProperty("s3.role");
    private static final String BUCKET_NAME = System.getProperty("s3");
    private static final String DATA_PATH = System.getProperty("s3.data");

    private static final String DATE_FORMAT_NANO_SEC = "yyyy-MM-dd'T'HH:mm:ss.SSSSSSSSS'Z'";
    private static final String DATE_FORMAT_MILL_SEC = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'";
    private static final String DATE_FORMAT_SEC = "yyyy-MM-dd HH:mm:00Z";

    /**
     * Execute.
     *
     * @param dataSource the data source
     * @param type       the type
     * @return the list
     */
    public List<Map<String, String>> uploadViolationInfo(String dataSource, String type) {
        LOGGER.info("Started EntityAssociationDataCollector for - {}", type);
        List<Map<String, String>> errorList = new ArrayList<>();
        AmazonS3 s3Client = AmazonS3ClientBuilder.standard().withCredentials(
                new AWSStaticCredentialsProvider(new CredentialProvider().getCredentials(S3_ACCOUNT, S3_ROLE))).withRegion(S3_REGION).build();
        ObjectMapper objectMapper = new ObjectMapper();

        String indexName;
        try {
            indexName = dataSource + "_" + type;
            String filePrefix = dataSource + "-issues-" + type;
            String docType = String.format("issue_%s", type);


            List<Map<String, Object>> entities;
            S3Object entitiesData = s3Client.getObject(new GetObjectRequest(BUCKET_NAME, DATA_PATH + "/" + filePrefix + ".data"));
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(entitiesData.getObjectContent()))) {
                entities = objectMapper.readValue(reader.lines().collect(Collectors.joining("\n")), new TypeReference<List<Map<String, Object>>>() {
                });
            } catch (Exception e) {
                LOGGER.error("violation object is empty");
                return errorList;
            }

            String loaddate = new SimpleDateFormat(DATE_FORMAT_SEC).format(new java.util.Date());
            entities.parallelStream().forEach((obj) -> {
                obj.put("_loaddate", loaddate);
                obj.put("docType", docType);
                obj.put("_docid", obj.get("_resourceid"));
                obj.put("targetType", type);

                Map<String, Object> relMap = new HashMap<>();
                relMap.put("name", docType);
                relMap.put("parent", obj.get("_resourceid"));
                obj.put(type + "_relations", relMap);
            });

            LOGGER.info("Collected : {}", entities.size());
            if (!entities.isEmpty()) {
                ESManager.uploadData(indexName, entities, dataSource);
                ESManager.deleteOldDocuments(indexName, docType, "_loaddate.keyword", loaddate);
                String auditDocType = "issue_" + type + "_audit";
                List<Map<String, Object>> auditLogEntites = createAuditLog(dataSource, type, entities);
                ESManager.deleteOldDocuments(indexName, auditDocType, "auditdate.keyword", "*");
                ESManager.uploadAuditLogData(indexName, auditLogEntites, dataSource);
            }
        } catch (Exception e) {
            LOGGER.debug("violation data not exists for Asset type - {}", type);
        }

        LOGGER.info("Completed EntityAssociationDataCollector for {}", type);
        return errorList;
    }

    private List<Map<String, Object>> createAuditLog(String dataSource, String type, List<Map<String, Object>> violationList) {
        List<Map<String, Object>> auditLogList = new ArrayList<>();
        String docType = "issue_" + type + "_audit";
        violationList.forEach(violationObj -> {
            Map<String, Object> auditLog = new HashMap<>();
            String issueId = (String) violationObj.get("annotationid");
            try {
                Date viloationCreatedDate = Util.getDateFromString((String) violationObj.get("createdDate"),
                        DATE_FORMAT_NANO_SEC);
                String auditDateTime = Util.getDateToStringWithFormat(viloationCreatedDate,
                        DATE_FORMAT_MILL_SEC);
                String auditDate = auditDateTime.contains("T")
                        ? auditDateTime.substring(0, auditDateTime.indexOf("T"))
                        : auditDateTime;
                auditLog.put(Constants.DOC_TYPE, docType);
                auditLog.put("datasource", dataSource);
                auditLog.put("targetType", type);
                auditLog.put("annotationid", issueId);
                auditLog.put("_docid", issueId);
                auditLog.put("auditdate", auditDateTime);
                auditLog.put("_auditdate", auditDate);
                auditLog.put("status", violationObj.get("issueStatus"));
                Map<String, Object> relationMap = new HashMap<>();
                relationMap.put("parent", issueId);
                relationMap.put("name", docType);
                auditLog.put(type + "_relations", relationMap);
                auditLogList.add(auditLog);
            } catch (ParseException e) {
                LOGGER.error("date format error", e);
            } catch (Exception e) {
                LOGGER.error(" data conversion error", e);
            }
        });

        return auditLogList;
    }
}