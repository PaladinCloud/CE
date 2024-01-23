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
package com.tmobile.cso.pacman.datashipper.dto;

public class JobSchedulerSQSMessageBody {
    private String jobName;
    private String paladinCloudTenantId;
    private String source;

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

    public JobSchedulerSQSMessageBody(String jobName, String paladinCloudTenantId, String source) {
        this.jobName = jobName;
        this.paladinCloudTenantId = paladinCloudTenantId;
        this.source = source;
    }
}
