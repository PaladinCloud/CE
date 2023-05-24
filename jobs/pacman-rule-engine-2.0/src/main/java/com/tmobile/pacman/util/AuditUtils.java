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
package com.tmobile.pacman.util;

import java.io.UnsupportedEncodingException;
import java.util.*;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.tmobile.pacman.common.PacmanSdkConstants;
import com.tmobile.pacman.commons.policy.Annotation;

import static com.tmobile.pacman.common.PacmanSdkConstants.TARGET_TYPE;

// TODO: Auto-generated Javadoc
/**
 * The Class AuditUtils.
 */
public class AuditUtils {
    
    /** The Constant logger. */
    private static final Logger logger = LoggerFactory.getLogger(AuditUtils.class);

    /**
     * Post audit trail.
     *
     * @param annotations the annotations
     * @param status the status
     */
    public static void postAuditTrail(List<Annotation> annotations, String status) {
        String esUrl = ESUtils.getEsUrl();
        String actionTemplate = "{ \"index\" : { \"_index\" : \"%s\", \"routing\" : \"%s\" } }%n";
        StringBuilder requestBody = new StringBuilder();
        String requestUrl = esUrl + "/_bulk";

        for (Annotation annotation : annotations) {
            String datasource = annotation.get(PacmanSdkConstants.DATA_SOURCE_KEY);
            String _id = CommonUtils.getUniqueAnnotationId(annotation);
            String type = annotation.get(TARGET_TYPE);

            String _index = datasource + "_" + type;
            String _type = "issue_" + type + "_audit";

            requestBody.append(String.format(actionTemplate, _index, _id))
                    .append(createAuditTrail(datasource, type, status, _id) + "\n");
            try {
                if (requestBody.toString().getBytes("UTF-8").length >= 5 * 1024 * 1024) { // 5
                                                                                          // MB
                    try {
                        CommonUtils.doHttpPost(requestUrl, requestBody.toString());
                    } catch (Exception e) {
                        logger.error("Audit creation failed", e);
                    }
                    requestBody = new StringBuilder();
                }
            } catch (UnsupportedEncodingException e) {
                logger.error(e.getMessage());
            }
        }

        if (requestBody.length() > 0) {
            try {
                CommonUtils.doHttpPost(requestUrl, requestBody.toString());
            } catch (Exception e) {
                logger.error("Audit creation failed", e);
            }
        }

    }

    /**
     * Creating the JSON for audit.
     *
     * @param ds the ds
     * @param type the type
     * @param status the status
     * @param id the id
     * @return the string
     */

    private static String createAuditTrail(String ds, String type, String status, String id) {
        String _type = "issue_" + type + "_audit";
        String date = CommonUtils.getCurrentDateStringWithFormat(PacmanSdkConstants.PAC_TIME_ZONE,
                PacmanSdkConstants.DATE_FORMAT);
        Map<String, Object> auditTrail = new LinkedHashMap<>();
        auditTrail.put(PacmanSdkConstants.DOC_TYPE,_type);
        // add relations to annotation
        Map<String, Object> relMap = new HashMap<>();
        relMap.put("name",_type);
        relMap.put("parent", id);
        auditTrail.put( type + "_relations", relMap);
        auditTrail.put(PacmanSdkConstants.DATA_SOURCE_ATTR, ds);
        auditTrail.put(TARGET_TYPE, type);
        auditTrail.put("_docid",id);
        auditTrail.put(PacmanSdkConstants.ANNOTATION_PK, id);
        auditTrail.put(PacmanSdkConstants.STATUS_KEY, status);
        auditTrail.put(PacmanSdkConstants.AUDIT_DATE, date);
        auditTrail.put(PacmanSdkConstants._AUDIT_DATE, date.substring(0, date.indexOf('T')));
        String _auditTrail = null;
        try {
            _auditTrail = new ObjectMapper().writeValueAsString(auditTrail);
        } catch (JsonProcessingException e) {
            logger.error(e.getMessage());
        }
        return _auditTrail;
    }

}
