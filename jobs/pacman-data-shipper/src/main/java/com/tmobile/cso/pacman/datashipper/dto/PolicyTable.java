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
package com.tmobile.cso.pacman.datashipper.dto;

public class PolicyTable {
    private String policyId;
    private String policyUUID;
    private String policyName;
    private String policyDisplayName;
    private String policyDesc;
    private String severity;
    private String target;
    private String assetgroup;
    private String category;
    private String status;
    private String policyFrequency;
    private String resolutionUrl;

    public String getPolicyId() {
        return policyId;
    }

    public void setPolicyId(String policyId) {
        this.policyId = policyId;
    }

    public String getPolicyUUID() {
        return policyUUID;
    }

    public void setPolicyUUID(String policyUUID) {
        this.policyUUID = policyUUID;
    }

    public String getPolicyName() {
        return policyName;
    }

    public void setPolicyName(String policyName) {
        this.policyName = policyName;
    }

    public String getPolicyDisplayName() {
        return policyDisplayName;
    }

    public void setPolicyDisplayName(String policyDisplayName) {
        this.policyDisplayName = policyDisplayName;
    }

    public String getPolicyDesc() {
        return policyDesc;
    }

    public void setPolicyDesc(String policyDesc) {
        this.policyDesc = policyDesc;
    }

    public String getSeverity() {
        return severity;
    }

    public void setSeverity(String severity) {
        this.severity = severity;
    }

    public String getTarget() {
        return target;
    }

    public void setTarget(String target) {
        this.target = target;
    }

    public String getAssetgroup() {
        return assetgroup;
    }

    public void setAssetgroup(String assetgroup) {
        this.assetgroup = assetgroup;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getPolicyFrequency() {
        return policyFrequency;
    }

    public void setPolicyFrequency(String policyFrequency) {
        this.policyFrequency = policyFrequency;
    }

    public String getResolutionUrl() {
        return resolutionUrl;
    }

    public void setResolutionUrl(String resolutionUrl) {
        this.resolutionUrl = resolutionUrl;
    }
}
