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

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.tmobile.pacman.common.PacmanSdkConstants;
import com.tmobile.pacman.commons.policy.Annotation;

import static com.tmobile.pacman.common.PacmanSdkConstants.*;
import static com.tmobile.pacman.commons.PacmanSdkConstants.DATA_ALERT_ERROR_STRING;
import static com.tmobile.pacman.commons.PacmanSdkConstants.ERROR_MESSAGE;
import static com.tmobile.pacman.commons.PacmanSdkConstants.ENDING_QUOTES;

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
    public static void postAuditTrail(List<? extends Map<String, String>> annotations, Map<String, Map<String, String>> existingIssues) {
        String esUrl = ESUtils.getEsUrl();
        String actionTemplate = "{ \"index\" : { \"_index\" : \"%s\", \"routing\" : \"%s\" } }%n";
        StringBuilder requestBody = new StringBuilder();
        String requestUrl = esUrl + "/_bulk";

        for (Map<String, String> annotation : annotations) {
            String datasource = annotation.get(PacmanSdkConstants.DATA_SOURCE_KEY);
            String _id = CommonUtils.getUniqueAnnotationId(annotation);
            String type = annotation.get(TARGET_TYPE);

            String _index = datasource + "_" + type;
            String _type = "issue_" + type + "_audit";

            /** preAuditStatus will audit history/reason for the actual status, and can be either 'Request Expired' or 'Exemption Expired' **/
            String preAuditStatus = existingIssues != null ? getPreAuditStatus(annotation, existingIssues.get(_id)) : null;
            if (preAuditStatus != null) {
                requestBody.append(String.format(actionTemplate, _index, _id))
                        .append(createAuditTrail(annotation, preAuditStatus, _id) + "\n");
            }

            requestBody.append(String.format(actionTemplate, _index, _id))
                    .append(createAuditTrail(annotation, annotation.get(PacmanSdkConstants.ISSUE_STATUS_KEY), _id) + "\n");
            try {
                if (requestBody.toString().getBytes("UTF-8").length >= 5 * 1024 * 1024) { // 5
                    // MB
                    try {
                        CommonUtils.doHttpPost(requestUrl, requestBody.toString());
                    } catch (Exception e) {
                        logger.error(DATA_ALERT_ERROR_STRING + annotation.get(POLICY_ID) + ERROR_MESSAGE + "Audit creation failed" + ENDING_QUOTES, e);
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
                logger.error(DATA_ALERT_ERROR_STRING + JOB_NAME + ERROR_MESSAGE + "Audit creation failed." + ENDING_QUOTES, e);
            }
        }

    }

    /** get the status which led to the actual audit status **/
    private static String getPreAuditStatus(Map<String, String> annotation, Map<String, String> originalIssue) {
        if (originalIssue == null) {
            return null;
        }
        if (PacmanSdkConstants.EXEMPTION_REQUEST_RAISED.equalsIgnoreCase(originalIssue.get(PacmanSdkConstants.STATUS_KEY))
                && PacmanSdkConstants.EXEMPTION_REQUEST_CANCELLED.equalsIgnoreCase(annotation.get(PacmanSdkConstants.STATUS_KEY))) {
            return PacmanSdkConstants.REQUEST_EXPIRED;
        }
        if (PacmanSdkConstants.STATUS_EXEMPTED.equalsIgnoreCase(originalIssue.get(ISSUE_STATUS_KEY))
                && PacmanSdkConstants.STATUS_OPEN.equalsIgnoreCase(annotation.get(PacmanSdkConstants.ISSUE_STATUS_KEY))) {
            return PacmanSdkConstants.EXEMPTION_EXPIRED;
        }
        return null;
    }

    /**
     * @param annotation
     * @param status
     * @param id
     * @return
     */

    private static String createAuditTrail(Map<String, String> annotation, String status, String id) {
        String type = annotation.get(TARGET_TYPE);
        String _type = "issue_" + type + "_audit";
        String date = CommonUtils.getCurrentDateStringWithFormat(PacmanSdkConstants.PAC_TIME_ZONE, PacmanSdkConstants.DATE_FORMAT);
        Map<String, Object> auditTrail = new LinkedHashMap<>();
        auditTrail.put(PacmanSdkConstants.DOC_TYPE, _type);
        // add relations to annotation
        Map<String, Object> relMap = new HashMap<>();
        relMap.put("name", _type);
        relMap.put("parent", id);
        auditTrail.put(type + "_relations", relMap);
        auditTrail.put(PacmanSdkConstants.DATA_SOURCE_ATTR, annotation.get(PacmanSdkConstants.DATA_SOURCE_KEY));
        auditTrail.put(TARGET_TYPE, type);
        auditTrail.put("_docid", id);
        auditTrail.put(PacmanSdkConstants.ANNOTATION_PK, id);
        auditTrail.put(PacmanSdkConstants.STATUS_KEY, status);
        auditTrail.put(PacmanSdkConstants.AUDIT_DATE, date);
        auditTrail.put(PacmanSdkConstants._AUDIT_DATE, date.substring(0, date.indexOf('T')));
        auditTrail.put(CREATED_BY, SYSTEM);
        if (STATUS_CLOSE.equalsIgnoreCase(status) && StringUtils.isNotBlank(annotation.get(PacmanSdkConstants.REASON_TO_CLOSE_KEY))) {
            auditTrail.put(PacmanSdkConstants.REASON_TO_CLOSE_KEY, annotation.get(PacmanSdkConstants.REASON_TO_CLOSE_KEY));
        }
        String _auditTrail = null;
        try {
            _auditTrail = new ObjectMapper().writeValueAsString(auditTrail);
        } catch (JsonProcessingException e) {
            logger.error(e.getMessage());
        }
        return _auditTrail;
    }

}
