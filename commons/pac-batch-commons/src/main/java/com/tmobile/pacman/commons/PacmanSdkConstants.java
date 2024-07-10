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

public final class PacmanSdkConstants {

    private PacmanSdkConstants() {
        throw new IllegalStateException("PacmanSdkConstants is a utility class");
    }

    public static final String CLIENT = "client";
    public static final Integer TEMPORARY_CREDS_VALID_SECONDS = 3600;
    public static final String DEFAULT_SESSION_NAME = "PAC_GET_DATA_SESSION";
    public static final String PACMAN_DEV_PROFILE_NAME = "pacman-dev";
    public static final String ACCOUNT_ID = "accountid";
    public static final String REGION = "region";
    public static final String ROLE_ARN_PREFIX = "arn:aws:iam::";
    public static final String RESOURCE_ID = "_resourceid";
    public static final String POLICY_ID = "policyId";
    public static final String POLICY_NAME = "policyName";
    public static final String POLICY_DISPLAY_NAME = "policyDisplayName";
    public static final String POLICY_VERSION = "policyVersion";
    public static final String Role_IDENTIFYING_STRING = "roleIdentifyingString";
    public static final String SPLITTER_CHAR = "splitterChar";
    public static final String DESCRIPTION = "desc";
    public static final String EXCEPTION = "Exception";
    public static final String TARGET_TYPE = "targetType";
    public static final String TYPE = "type";
    public static final String SEV_HIGH = "high";
    public static final String SEV_MEDIUM = "medium";
    public static final String SEV_LOW = "low";
    public static final String SECURITY = "security";
    public static final String GOVERNANCE = "governance";
    public static final String PAC_TIME_ZONE = "UTC";
    public static final String ISSUE_STATUS_KEY = "issueStatus";
    public static final String DATA_SOURCE_KEY = "pac_ds";
    public static final String BASE_AWS_ACCOUNT_ENV_VAR_NAME = "BASE_AWS_ACCOUNT";
    public static final String ES_DOC_ID_KEY = "_id";
    public static final String DATE_FORMAT = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'";
    public static final String ES_URI_ENV_VAR_NAME = "ES_URI";
    public static final String PARENT_ID = "_docid";
    public static final String EXECUTION_ID = "executionId";
    public static final Integer ES_PAGE_SIZE = 1000;
    public static final String ES_PAGE_SCROLL_TTL = "5m";
    public static final String ACCOUNT_NAME = "accountname";
    public static final String ES_DOC_PARENT_KEY = "_parent";
    public static final String ES_DOC_ROUTING_KEY = "_routing";
    public static final String STATUS_OPEN = "open";
    public static final String STATUS_SUCCESS = "success";
    public static final String STATUS_FAILURE = "fail";
    public static final String STATUS_UNKNOWN = "unknown";
    public static final String STATUS_UNKNOWN_MESSAGE = "unable to determine for this resource";
    public static final String CURRENT_APP_TAG_KEY = "current_application_tag";
    public static final String CORRECT_APP_TAG_KEY = "correct_application_tag";
    public static final String CONFIG_CREDENTIALS = "CONFIG_CREDENTIALS";
    public static final String CONFIG_SERVICE_URL = "CONFIG_SERVICE_URL";
    public static final String MISSING_CONFIGURATION = "Missing value in the env configuration";
    public static final String MISSING_DB_CONFIGURATION = "Missing db configurations";
    public static final String NAME = "name";
    public static final String SOURCE = "source";
    public static final String TAGGING_MANDATORY_TAGS = "tagging.mandatoryTags";
    public static final String CLOUD_INSIGHT_SQL_SERVER = "CLOUD_INSIGHT_SQL_SERVER";
    public static final String CLOUD_INSIGHT_USER = "CLOUD_INSIGHT_USER";
    public static final String CLOUD_INSIGHT_PASSWORD = "CLOUD_INSIGHT_PASSWORD";
    public static final String OPERATIONS = "operations";
    public static final Integer MAX_RETRY_COUNT = 3;
    public static final String ANNOTATION_ID	= "annotationid";
    public static final String VULNERABILITY_DETAILS = "vulnerabilityDetails";
    public static final String FILED_TITLE = "title";
    public static final String VULNERABILITY_URL = "vulnerabilityUrl";
    public static final String CVE_LIST = "cveList";
    public static final String UPDATE_DESCRIPTION = "updateDescription";
    public static final String VULNERABILITY_DESC = "desc";
    public static final String RESOURCE_NAME = "resourceName";
    public static final String FIELD_URL = "url";
    public static final String MULTIPLE_VIOLATION_MAPPING = "manyToOneViolationMapping";

    //Data dog will create data alert if it finds any logger message starting with below string.
    public static final String DATA_ALERT_ERROR_STRING = "error occurred in job_name:";
    public static final String ERROR_MESSAGE = " error_message:\"";
    public static final String ENDING_QUOTES = "\" ";
    public static final String ENRICHER_SQS_QUEUE_URL = "ENRICHER_SQS_QUEUE_URL";
}
