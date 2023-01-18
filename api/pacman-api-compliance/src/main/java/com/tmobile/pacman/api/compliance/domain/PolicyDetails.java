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
/**
  Copyright (C) 2017 T Mobile Inc - All Rights Reserve
  Purpose:
  Author :Nidhish
  Modified Date: Nov 22, 2017

 **/
package com.tmobile.pacman.api.compliance.domain;

/**
 * The Class RuleDetails.
 */
public class PolicyDetails {

    /** The rule id. */
    private String policyId;

    /** The reason. */
    private String reason;

    /** The user id. */
    private String userId;

    /**
     * Gets the policy id.
     *
     * @return the policy id
     */
    public String getPolicyId() {
        return policyId;
    }

    /**
     * Sets the policy id.
     *
     * @param policyId the new rule id
     */
    public void setPolicyId(String policyId) {
        this.policyId = policyId;
    }

    /**
     * Gets the reason.
     *
     * @return the reason
     */
    public String getReason() {
        return reason;
    }

    /**
     * Sets the reason.
     *
     * @param reason the new reason
     */
    public void setReason(String reason) {
        this.reason = reason;
    }

    /**
     * Gets the user id.
     *
     * @return the user id
     */
    public String getUserId() {
        return userId;
    }

    /**
     * Sets the user id.
     *
     * @param userId the new user id
     */
    public void setUserId(String userId) {
        this.userId = userId;
    }
}
