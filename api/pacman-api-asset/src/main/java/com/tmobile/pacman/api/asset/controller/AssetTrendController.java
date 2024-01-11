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
package com.tmobile.pacman.api.asset.controller;

import java.util.Calendar;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;

import org.elasticsearch.common.Strings;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import com.tmobile.pacman.api.asset.service.AssetService;
import com.tmobile.pacman.api.commons.Constants;
import com.tmobile.pacman.api.commons.utils.ResponseUtils;

import io.swagger.annotations.ApiOperation;

/**
 * The controller layer which has methods to return trend of assets.
 */
@RestController
@PreAuthorize("@securityService.hasPermission(authentication, 'readonly') or #oauth2.hasScope('API_OPERATION/READ')")
@CrossOrigin
public class AssetTrendController {
    @Autowired
    AssetService assetService;

    private static String FROM_DATE = "fromDate";
    private static String TO_DATE = "toDate";
    private static String INVALID = "invalid";
    /**
     * Fetches the asset trends(daily min/max) over the period of last 1 month
     * for the given asset group. From and to can be passed to fetch the asset
     * trends for particular days.
     *
     * @param assetGroup name of the asset group
     * @param type target type of the asset group
     * @param fromDate starting date of the asset trend
     * @param toDate end date of the asset trend needed
     * @param domain domain of the group
     * 
     * @return list of days with its min/max asset count.
     */
    
    @ApiOperation(value = "View the asset trends(daily min/max) over the period of last 1 month", response = Iterable.class)
    @GetMapping(path = "/v1/trend/minmax")
    public ResponseEntity<Object> getMinMaxAssetCount(@RequestParam(name = "ag", required = true) String assetGroup,
            @RequestParam(name = "type", required = true) String type,
            @RequestParam(name = "from", required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") Date fromDate,
            @RequestParam(name = "to", required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") Date toDate,
            @RequestParam(name = "domain", required = false) String domain) {
        try {
            Date from = fromDate; 
            Date to = toDate;
            if (from == null && to == null) {
                Calendar cal = Calendar.getInstance();
                cal.setTimeZone(TimeZone.getTimeZone("UTC"));
                to = cal.getTime();
                cal.add(Calendar.DATE, Constants.NEG_THIRTY);
                from = cal.getTime();
            }
            Map<String, Object> response = new LinkedHashMap<>();
            response.put("ag", assetGroup);
            response.put("type", type);
            List<Map<String, Object>> trendList = assetService.getAssetMinMax(assetGroup, type, from, to);
            response.put("trend", trendList);
            return ResponseUtils.buildSucessResponse(response);
        } catch (Exception e) {
            return ResponseUtils.buildFailureResponse(e);
        }
    }

    @ApiOperation(value = "Trends of daily total assets count over the period between from and to date", response = Iterable.class)
    @PostMapping(path = "/v1/trend/assetcount")
    public ResponseEntity<Object> getAssetCount(@RequestBody Map<String, Object> requestPayload) {
        try {
            String strDateRegEx = "\\d{4}-(0[1-9]|1[012])-(0[1-9]|[12][0-9]|[3][01])";
            String from = requestPayload.get(FROM_DATE) != null ? (requestPayload.get(FROM_DATE).toString().matches(strDateRegEx) ? (requestPayload.get(FROM_DATE).toString()) : INVALID) : null;
            String to = requestPayload.get(TO_DATE) != null ? (requestPayload.get(TO_DATE).toString().matches(strDateRegEx) ? (requestPayload.get(TO_DATE).toString()) : INVALID) : null;
            if (INVALID.equalsIgnoreCase(from) || INVALID.equalsIgnoreCase(to)) {
                return ResponseUtils.buildFailureResponse(new Exception("fromDate or toDate is invalid or format is invalid. Expected format - yyyy-MM-dd"));
            }
            String assetGroup = requestPayload.get("ag") != null ? requestPayload.get("ag").toString() : null;
            if (Strings.isNullOrEmpty(assetGroup)) {
                return ResponseUtils.buildFailureResponse(new Exception("Attribute 'assetGroup' not provided."));
            }
            List<String> type = null;
            if (requestPayload.get("type") != null) {
                if (!(requestPayload.get("type") instanceof List)) {
                    return ResponseUtils.buildFailureResponse(new Exception("Attribute 'type' should be a List."));
                }
                type = (List<String>) requestPayload.get("type");
                if (type.isEmpty()) {
                    return ResponseUtils.buildFailureResponse(new Exception("Attribute 'type' cannot be empty."));
                }
            } else {
                return ResponseUtils.buildFailureResponse(new Exception("Attribute 'type' is not provided."));
            }
            Map<String, Object> response = new LinkedHashMap<>();
            response.put("ag", assetGroup);
            List<Map<String, Object>> trendList = assetService.getAssetCountTrend(assetGroup, type, from, to);
            response.put("trend", trendList);
            return ResponseUtils.buildSucessResponse(response);
        } catch (Exception e) {
            return ResponseUtils.buildFailureResponse(e);
        }
    }
}