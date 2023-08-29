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
import com.tmobile.cso.pacman.datashipper.dao.RDSDBManager;
import com.tmobile.cso.pacman.datashipper.dto.PolicyTable;
import com.tmobile.cso.pacman.datashipper.error.ErrorManager;
import com.tmobile.cso.pacman.datashipper.es.ESManager;
import com.tmobile.cso.pacman.datashipper.util.Constants;
import com.tmobile.cso.pacman.datashipper.util.Util;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * The Class ChildTableDataCollector.
 */
public class ExternalPolicies implements Constants {

    /** The Constant LOGGER. */
    private static final Logger LOGGER = LoggerFactory.getLogger(ExternalPolicies.class);
    
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
    public boolean uploadPolicyDefinition(String dataSource) {
        LOGGER.info("Started upload policy definition  for {}", dataSource);
        AmazonS3 s3Client = AmazonS3ClientBuilder.standard().withCredentials(
                new AWSStaticCredentialsProvider(new CredentialProvider().getCredentials(s3Account, s3Role))).withRegion(s3Region).build();
        ObjectMapper objectMapper = new ObjectMapper();

        try {
            String filePrefix = dataSource + "-policy" ;
           
           
                       	List<Map<String, Object>> entities = new ArrayList<>();
                        S3Object entitiesData = s3Client.getObject(new GetObjectRequest(bucketName, dataPath + "/" + filePrefix  + ".data"));
                        try (BufferedReader reader = new BufferedReader(new InputStreamReader(entitiesData.getObjectContent()))) {
                            entities = objectMapper.readValue(reader.lines().collect(Collectors.joining("\n")), new TypeReference<List<Map<String, Object>>>() {
                            });
                        } catch (Exception e) {
                        	LOGGER.error("policy object is empty");
                        	return false;
                        }
                        
                        List<PolicyTable> policyList = new ArrayList<>();
                        for (int i = 0; i < entities.size(); i++) {
                            Map<String, Object> map = entities.get(i);
                            PolicyTable policy = objectMapper.convertValue(map, PolicyTable.class);
                            policyList.add(policy);
                        }
                        
                        RDSDBManager.insertNewPolicy(policyList);
                       
    } catch (Exception e) {
    	LOGGER.error("policy upload exception {}",e);
    }
        return true;
    }
}