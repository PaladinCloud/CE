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

import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.amazonaws.services.s3.model.GetObjectRequest;
import com.amazonaws.services.s3.model.S3Object;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.base.Strings;
import com.tmobile.cso.pacman.datashipper.config.CredentialProvider;
import com.tmobile.cso.pacman.datashipper.config.S3ClientConfig;
import com.tmobile.cso.pacman.datashipper.util.Constants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class AWSErrorManager implements Constants {
    private static final Logger LOGGER = LoggerFactory.getLogger(AWSErrorManager.class);

    private static AWSErrorManager errorManager;
    private final String s3Account = System.getProperty("base.account");
    private final String s3Region = System.getProperty("base.region");
    private final String s3Role = System.getProperty("s3.role");
    private final String bucketName = System.getProperty("s3");
    private final String dataPath = System.getProperty("s3.data");
    private Map<String, List<Map<String, String>>> errorInfo;

    private AWSErrorManager() {
    }

    /**
     * Gets the single instance of AWSErrorManager.
     *
     * @return single instance of AWSErrorManager
     */
    public static AWSErrorManager getInstance() {
        if (errorManager == null) {
            errorManager = new AWSErrorManager();
        }
        return errorManager;
    }

    /**
     * Fetch error info.
     *
     * @param datasource the datasource
     * @param errorList  the error list
     */
    private void fetchErrorInfo(String datasource, List<Map<String, String>> errorList) {
        if (errorInfo == null) {
            ObjectMapper objectMapper = new ObjectMapper();
            List<Map<String, String>> inventoryErrors = new ArrayList<>();
            AmazonS3 s3Client = S3ClientConfig.getInstance().getS3Client();
            try {
                S3Object inventoryErrorData = s3Client.getObject(new GetObjectRequest(bucketName, dataPath + "/" + datasource + "-loaderror.data"));
                try (BufferedReader reader = new BufferedReader(new InputStreamReader(inventoryErrorData.getObjectContent()))) {
                    inventoryErrors = objectMapper.readValue(reader.lines().collect(Collectors.joining("\n")), new TypeReference<List<Map<String, String>>>() {
                    });
                }
            } catch (IOException e) {
                LOGGER.error("Exception in collecting inventory error data", e);
                Map<String, String> errorMap = new HashMap<>();
                errorMap.put(ERROR, "Exception in collecting inventory error data");
                errorMap.put(ERROR_TYPE, WARN);
                errorMap.put(EXCEPTION, e.getMessage());
                errorList.add(errorMap);
            }

            errorInfo = inventoryErrors.parallelStream().collect(Collectors.groupingBy(obj -> obj.get("type")));
        }
    }

    /**
     * Gets the error info.
     *
     * @param datasource the datasource
     * @param errorList  the error list
     * @return the error info
     */
    public Map<String, List<Map<String, String>>> getErrorInfo(String datasource, List<Map<String, String>> errorList) {
        if (errorInfo == null) {
            fetchErrorInfo(datasource, errorList);
        }

        return errorInfo;
    }

    /**
     * Handle error.
     *
     * @param dataSource  the data source
     * @param index       the index
     * @param type        the type
     * @param loaddate    the loaddate
     * @param errorList   the error list
     * @param checkLatest the check latest
     * @return
     */
    public Map<String, Long> handleError(String dataSource, String index, String type, String loaddate, List<Map<String, String>> errorList, boolean checkLatest) {
        fetchErrorInfo(dataSource, errorList);
        String parentType = index.replace(dataSource + "_", "");
        Map<String, Long> errorUpdateInfo = new HashMap<>();
        if (errorInfo.containsKey(parentType) || errorInfo.containsKey("all")) {
            List<Map<String, String>> errorByType = errorInfo.get(parentType);
            if (errorByType == null) {
                errorByType = errorInfo.get("all");
            }
            StringBuilder updateJson = new StringBuilder("{\"script\":{\"inline\":\"ctx._source._loaddate= '");
            updateJson.append(loaddate).append("'\"},\"query\":{\"bool\":{\"should\":[");
            errorByType.forEach(errorData -> {
                        String accountId = errorData.get("accountid");
                        String region = errorData.get("region");


                        updateJson.append("{\r\n"
                                + "          \"bool\": {\r\n"
                                + "            \"must\": [\r\n"
                                + "              {\r\n"
                                + "                \"term\": {\r\n"
                                + "                  \"accountid.keyword\": \"" + accountId + "\"\r\n"
                                + "                }\r\n"
                                + "              },\r\n"
                                + "              {\r\n"
                                + "                \"term\": {\r\n"
                                + "                  \"region.keyword\": \"" + region + "\"\r\n"
                                + "                }\r\n"
                                + "              }\r\n"
                                + "            ]\r\n"
                                + "          }\r\n"
                                + "        },");

                    }
            );

            if (!Strings.isNullOrEmpty(type)) {
                updateJson.deleteCharAt(updateJson.length() - 1);
                updateJson.append("], \"minimum_should_match\": 1,\"must\":[{\"match\":{\"docType.keyword\":\"");
                updateJson.append(type);
                updateJson.append("\"}}");
            }
            if (checkLatest) {
                updateJson.append(",{\"match\":{\"latest\":true }}");

            }
            updateJson.append("]}}}");
        }
        return errorUpdateInfo;
    }
}
