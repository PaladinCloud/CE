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

import java.util.List;

public class DatasourceData {
    private List<String> accountIds;
    private List<String> assetGroups;
    public List<String> getAccountIds() {
        return accountIds;
    }
    public void setAccountIds(List<String> accountIds) {
        this.accountIds = accountIds;
    }
    public List<String> getAssetGroups() {
        return assetGroups;
    }
    public void setAssetGroups(List<String> assetGroups) {
        this.assetGroups = assetGroups;
    }
}
