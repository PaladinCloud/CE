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
import com.tmobile.cso.pacman.datashipper.util.Constants;
import com.tmobile.cso.pacman.datashipper.util.Util;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * The Class ChildTableDataCollector.
 */
public class ViolationAssociationManager implements Constants {

    /** The Constant LOGGER. */
    private static final Logger LOGGER = LoggerFactory.getLogger(ViolationAssociationManager.class);
    
	/** The s 3 account. */
	private String s3Account = System.getProperty("base.account");
	
	/** The s 3 region. */
	private String s3Region = System.getProperty("base.region");
	
	/** The s 3 role. */
	private String s3Role =  System.getProperty("s3.role");
	
	/** The bucket name. */
	private String bucketName =  System.getProperty("s3");
	
	/** The data path. */
	private String dataPath =  System.getProperty("s3.data");

    /**
     * Execute.
     *
     * @param dataSource the data source
     * @param type the type
     * @return the list
     */
    public List<Map<String, String>> uploadViolationInfo(String dataSource,String type) {
        LOGGER.info("Started EntityAssociationDataCollector for {}", type);
        List<Map<String, String>> errorList = new ArrayList<>();
        AmazonS3 s3Client = AmazonS3ClientBuilder.standard().withCredentials(
                new AWSStaticCredentialsProvider(new CredentialProvider().getCredentials(s3Account, s3Role))).withRegion(s3Region).build();
        ObjectMapper objectMapper = new ObjectMapper();

        String indexName = null;
        try {
            indexName = dataSource + "_" + type;
            String filePrefix = dataSource + "-issues-" + type;
            String docType = "issue_"+type;
           
           
                       	List<Map<String, Object>> entities = new ArrayList<>();
                        S3Object entitiesData = s3Client.getObject(new GetObjectRequest(bucketName, dataPath + "/" + filePrefix  + ".data"));
                        try (BufferedReader reader = new BufferedReader(new InputStreamReader(entitiesData.getObjectContent()))) {
                            entities = objectMapper.readValue(reader.lines().collect(Collectors.joining("\n")), new TypeReference<List<Map<String, Object>>>() {
                            });
                        } catch (Exception e) {
                        	LOGGER.error("violation object is empty");
                        	return errorList;
                        }
                        String loaddate = new SimpleDateFormat(Constants.DATE_FORMAT_SEC).format(new java.util.Date());
                        entities.parallelStream().forEach((obj) -> {

                            obj.put("_loaddate", loaddate);
                            obj.put(Constants.DOC_TYPE, docType);
                            obj.put("_docid", obj.get("_resourceid"));
                            obj.put("targetType", type);

                           // String parentID = Util.concatenate(obj, key.split(","), "_");
                           
                            Map<String, Object> relMap = new HashMap<>();
                            relMap.put("name", docType);
                            relMap.put("parent", obj.get("_resourceid"));
                            obj.put(type + "_relations", relMap);
                        });

                        LOGGER.info("Collected :  {}", entities.size());
						if (!entities.isEmpty()) {
							ESManager.uploadData(indexName, entities, dataSource);
							ESManager.deleteOldDocuments(indexName, docType, "_loaddate.keyword", loaddate);
							String auditDocType = "issue_" + type + "_audit";
							List<Map<String, Object>> auditLogEntites = createAuditLog(dataSource, type, entities);
							ESManager.deleteOldDocuments(indexName, auditDocType, "auditdate.keyword", "*");
							ESManager.uploadAuditLogData(indexName, auditLogEntites, dataSource);
						}
                    
                
        } catch (Exception e) {
        	LOGGER.debug("violation data not exists for Asset type  :  {}", type);
        }
        LOGGER.info("Completed EntityAssociationDataCollector for {}", type);
        return errorList;
    }
    
    private List<Map<String, Object>> createAuditLog(String dataSource, String type, List<Map<String, Object>> violationList){
    	List<Map<String, Object>> auditLogList = new ArrayList<>();
    	String docType = "issue_"+type+"_audit";
    	violationList.forEach(violationObj -> {
    		Map<String, Object> auditLog =  new HashMap<String, Object>();
    		String issueId = (String)violationObj.get(Constants.ANNOTATION_ID);
    		try {
				Date viloationCreatedDate = Util.getDateFromString((String) violationObj.get(Constants.CREATED_DATE),
						Constants.DATE_FORMAT_NANO_SEC);
				String auditDateTime = Util.getDateToStringWithFormat(viloationCreatedDate,
						Constants.DATE_FORMAT_MILL_SEC);
				String auditDate = auditDateTime.indexOf("T") != -1
						? auditDateTime.substring(0, auditDateTime.indexOf("T"))
						: auditDateTime;
				auditLog.put(Constants.DOC_TYPE, docType);
				auditLog.put(Constants.DATA_SOURCE, dataSource);
				auditLog.put(Constants.TARGET_TYPE, type);
				auditLog.put(Constants.ANNOTATION_ID, issueId);
				auditLog.put(Constants.DOC_ID, issueId);
				auditLog.put(Constants.AUDIT_DATE, auditDateTime);
				auditLog.put(Constants._AUDIT_DATE, auditDate);
				auditLog.put(Constants.STATUS, violationObj.get(Constants.ISSUE_STATUS));
				Map<String, Object> relationMap = new HashMap<String, Object>();
				relationMap.put("parent", issueId);
				relationMap.put("name", docType);
				auditLog.put(type + "_relations", relationMap);
				auditLogList.add(auditLog);
    		} catch (ParseException e) {
				LOGGER.error("date format error {}", e);
			} catch (Exception e ) {
				LOGGER.error(" data conversion error {}", e);
			}
    		
    		
    	});
    	return auditLogList;
    }
}