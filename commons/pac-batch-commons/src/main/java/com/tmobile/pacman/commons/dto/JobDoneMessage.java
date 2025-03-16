/*******************************************************************************
 * Copyright 2024 Paladin Cloud, Inc. or its affiliates. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License.  You may obtain a copy
 * of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  See the
 * License for the specific language governing permissions and limitations under
 * the License.
 ******************************************************************************/
package com.tmobile.pacman.commons.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public class JobDoneMessage {
    private String jobName;
    // NOTE: 'paladinCloudTenantId' is currently sent by the legacy shipper while 'tenantId' is sent
    // by the delta engine; it's not clear if different recipients will use what's available so
    // both are sent here.
    private String paladinCloudTenantId;
    @JsonProperty("tenant_id")
    private String tenantId;
    private String source;
    private String enricherSource;
    @JsonProperty("tenant_name")
    private String tenantName;
    private String[] assetTypes;


    public String getJobName() {
        return jobName;
    }

    public void setJobName(String jobName) {
        this.jobName = jobName;
    }

    public String getPaladinCloudTenantId() {
        return paladinCloudTenantId;
    }

    public void setPaladinCloudTenantId(String paladinCloudTenantId) {
        this.paladinCloudTenantId = paladinCloudTenantId;
    }

    public String getSource() {
        return source;
    }

    public void setSource(String source) {
        this.source = source;
    }

    public String getEnricherSource() {
        return enricherSource;
    }

    public void setEnricherSource(String enricherSource) {
        this.enricherSource = enricherSource;
    }

    public String getTenantName() { return tenantName; }
    public String[] getAssetTypes() { return assetTypes; }

    public JobDoneMessage (String jobName, String paladinCloudTenantId, String source, String enricherSource) {
        this(jobName, paladinCloudTenantId, source, enricherSource, null, null);
    }

    public JobDoneMessage (String jobName, String paladinCloudTenantId, String source, String enricherSource, String tenantName, String[] assetTypes) {
        this.jobName = jobName;
        this.paladinCloudTenantId = paladinCloudTenantId;
        this.tenantId = paladinCloudTenantId;
        this.tenantName = tenantName;
        this.source = source;
        this.enricherSource = enricherSource;
        this.assetTypes = assetTypes;
    }
}
