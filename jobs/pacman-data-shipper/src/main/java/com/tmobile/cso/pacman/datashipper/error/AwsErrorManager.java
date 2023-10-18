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
package com.tmobile.cso.pacman.datashipper.error;

import com.google.common.base.Strings;
import com.tmobile.cso.pacman.datashipper.es.ESManager;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AwsErrorManager extends ErrorManager {
    protected AwsErrorManager() { }

    /**
     * Handle error.
     *
     * @param index       the index
     * @param type        the type
     * @param loaddate    the loaddate
     * @param errorList   the error list
     * @param checkLatest the check latest
     */
    public Map<String, Long> handleError(String index, String type, String loaddate, List<Map<String, String>> errorList, boolean checkLatest) {
        Map<String, List<Map<String, String>>> errorInfo = getErrorInfo(errorList);
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
                        if (!Strings.isNullOrEmpty(accountId) || !Strings.isNullOrEmpty(region)) {
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
            long updateCount = ESManager.updateLoadDate(index, updateJson.toString());
            errorUpdateInfo.put("totalDocUpdated", updateCount);
        }

        return errorUpdateInfo;
    }
}
