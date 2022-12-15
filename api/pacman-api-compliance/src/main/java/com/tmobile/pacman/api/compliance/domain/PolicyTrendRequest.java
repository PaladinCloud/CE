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
package com.tmobile.pacman.api.compliance.domain;
/**
 * The Class PolicyTrendRequest.
 */
public class PolicyTrendRequest extends CompliantTrendRequest {

    /** The policyid. */
    private String policyid;

    /**
     * Gets the policyid.
     *
     * @return the policyid
     */
    public String getPolicyid() {
        return policyid;
    }

    /**
     * Sets the policyid.
     *
     * @param policyid the new policyid
     */
    public void setPolicyid(String policyid) {
        this.policyid = policyid;
    }

}
