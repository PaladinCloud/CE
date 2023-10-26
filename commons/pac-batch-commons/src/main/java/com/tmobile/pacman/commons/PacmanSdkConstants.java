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

package com.tmobile.pacman.commons;

/**
 * The Interface PacmanSdkConstants.
 */
public interface PacmanSdkConstants {

    String CLIENT = "client";
    Integer TEMPORARY_CREDS_VALID_SECONDS = 3600;
    String DEFAULT_SESSION_NAME = "PAC_GET_DATA_SESSION";
    String PACMAN_DEV_PROFILE_NAME = "pacman-dev";
    String ACCOUNT_ID = "accountid";
    String REGION = "region";
    String ROLE_ARN_PREFIX = "arn:aws:iam::";
    String RESOURCE_ID = "_resourceid";
    String POLICY_ID = "policyId";
    String POLICY_NAME = "policyName";
    String POLICY_DISPLAY_NAME = "policyDisplayName";
    String POLICY_VERSION = "policyVersion";
    String Role_IDENTIFYING_STRING = "roleIdentifyingString";
    String SPLITTER_CHAR = "splitterChar";
    String DESCRIPTION = "desc";
    String EXCEPTION = "Exception";
    String TARGET_TYPE = "targetType";
    String TYPE = "type";
    String SEV_HIGH = "high";
    String SEV_MEDIUM = "medium";
    String SEV_LOW = "low";
    String SECURITY = "security";
    String GOVERNANCE = "governance";
    String PAC_TIME_ZONE = "UTC";
    String ISSUE_STATUS_KEY = "issueStatus";
    String DATA_SOURCE_KEY = "pac_ds";
    String BASE_AWS_ACCOUNT_ENV_VAR_NAME = "BASE_AWS_ACCOUNT";
    String ES_DOC_ID_KEY = "_id";
    String DATE_FORMAT = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'";
    String ES_URI_ENV_VAR_NAME = "ES_URI";
    String PARENT_ID = "_docid";
    Object RESOURCE_ID_COL_NAME_FROM_ES = "_resourceid";
    String EXECUTION_ID = "executionId";
    Integer ES_PAGE_SIZE = 1000;
    String ES_PAGE_SCROLL_TTL = "5m";
    String ACCOUNT_NAME = "accountname";
    String ES_DOC_PARENT_KEY = "_parent";
    String ES_DOC_ROUTING_KEY = "_routing";
    String STATUS_OPEN = "open";
    String STATUS_SUCCESS = "success";
    String STATUS_FAILURE = "fail";
    String STATUS_UNKNOWN = "unknown";
    String STATUS_UNKNOWN_MESSAGE = "unable to determine for this resource";
    String CURRENT_APP_TAG_KEY = "current_application_tag";
    String CORRECT_APP_TAG_KEY = "correct_application_tag";
    String CONFIG_CREDENTIALS = "CONFIG_CREDENTIALS";
    String CONFIG_SERVICE_URL = "CONFIG_SERVICE_URL";
    String MISSING_CONFIGURATION = "Missing value in the env configuration";
    String MISSING_DB_CONFIGURATION = "Missing db configurations";
    String NAME = "name";
    String SOURCE = "source";
    String TAGGING_MANDATORY_TAGS = "tagging.mandatoryTags";
    String CLOUD_INSIGHT_SQL_SERVER = "CLOUD_INSIGHT_SQL_SERVER";
    String CLOUD_INSIGHT_USER = "CLOUD_INSIGHT_USER";
    String CLOUD_INSIGHT_PASSWORD = "CLOUD_INSIGHT_PASSWORD";
    String OPERATIONS = "operations";
    String PROJECT_ID = "projectId";
    Integer MAX_RETRY_COUNT = 3;
}
