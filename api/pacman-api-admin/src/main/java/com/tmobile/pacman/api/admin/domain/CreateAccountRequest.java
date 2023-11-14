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
package com.tmobile.pacman.api.admin.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class CreateAccountRequest {
    private String platform;
    private String accountId;
    private String accountName;
    private String roleName;
    private String projectId;
    private String projectName;
    private String secretData;

    private String tenantId;
    private String tenantName;
    private String tenantSecretData;

    private String qualysApiUrl;
    private String qualysApiUser;
    private String qualysApiPassword;

    private String aquaApiUrl;
    private String aquaClientDomainUrl;
    private String aquaApiUser;
    private String aquaApiPassword;

    private String tenableAPIUrl;
    private String tenableAccessKey;
    private String tenableSecretKey;

    //contrast plugin
    private String organizationId;
    private String organizationName;
    private String apiKey;
    private String userId;
    private String serviceKey;
    @Builder.Default
    private String environmentName = "eval";

    private String createdBy;
}
