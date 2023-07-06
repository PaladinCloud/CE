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
package com.tmobile.pacman.api.asset.client;

import com.tmobile.pacman.api.asset.domain.PolicyParamResponse;
import com.tmobile.pacman.api.asset.domain.Request;
import com.tmobile.pacman.api.asset.domain.ResponseWithTotal;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

import com.tmobile.pacman.api.asset.domain.PolicyViolationApi;

import java.util.Map;

/**
 * The Interface ComplianceServiceClient.
 */
@FeignClient(name = "compliance", url = "${service.url.compliance}")
public interface ComplianceServiceClient {

    /**
     * Gets the total issues.
     *
     * @param assetGroup the asset group
     * @return the total issues
     */
    @RequestMapping(method = RequestMethod.GET, value = "/v1/issues/count")
    String getTotalIssues(@RequestParam("ag") String assetGroup);

    /**
     * Gets the policy violation summary.
     *
     * @param resourceId   the resource id
     * @param dataSource   the data source
     * @param resourceType the resource type
     * @return the policy violation summary
     */
    @RequestMapping(method = RequestMethod.GET, value = "/v1/policyviolations/summary/{dataSource}/{resourceType}/{resourceId}")
    PolicyViolationApi getPolicyViolationSummary(@PathVariable("resourceId") String resourceId,
                                                 @PathVariable("dataSource") String dataSource, @PathVariable("resourceType") String resourceType);

    @RequestMapping(method = RequestMethod.GET, value = "/v1/getPolicyCountByAssetGroup")
    Integer getPolicyCountByAssetGroup(@RequestParam("ag") String assetGroup);

    /**
     * Gets the non-compliance policy.
     *
     * @param request the pay load
     * @return the open issues
     */
    @RequestMapping(method = RequestMethod.POST, value = "/v1/noncompliancepolicy", consumes = "application/json")
    ResponseWithTotal getNonCompliancePolicies(@RequestBody Request request);

    @GetMapping(path = "/policyparams/param")
    PolicyParamResponse getPolicyParam(@RequestParam("policyId") String policyId,
                                       @RequestParam("policyParamKey") String policyParamkey);

}
