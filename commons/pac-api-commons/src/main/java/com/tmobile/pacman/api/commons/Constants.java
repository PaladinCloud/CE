/**
 * Copyright 2018 T Mobile, Inc. or its affiliates. All Rights Reserved.
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License.  You may obtain a copy
 * of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  See the
 * License for the specific language governing permissions and limitations under
 * the License.
 * <p>
 * Copyright (C) 2017 T Mobile Inc - All Rights Reserve
 * Purpose:
 * Author :kkumar
 * Modified Date: Oct 18, 2017
 **/
/**
 Copyright (C) 2017 T Mobile Inc - All Rights Reserve
 Purpose:
 Author :kkumar
 Modified Date: Oct 18, 2017
 **/
package com.tmobile.pacman.api.commons;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;
import java.util.List;

public interface Constants {

    String EXCEPTION_IN_GETTING_COUNT_OF_ACCOUNT_IDS = "Exception in getting count of account ids";

    String EXEMPTION_CREATED_BY = "exemption-created-by";
    String EXEMPTION_RAISED_BY = "exemption-raised-by";
    String EXEMPTION_CREATED_ON = "exemption-created-on";

    String STATUS_SUCCESS_CAPS = "Success";
    String STATUS_PARTIAL_SUCCESS = "Partial Success";
    String LOG_UPLOADING = "Uploading ";
    String NEW_LINE = "\n";
    String TARGET_EXEMPTION = "_exemption";
    String TARGET_ISSUE = "issue_";
    String AUDIT = "_audit";

    String OPEN_ISSUE_FETCH_ERROR_MESSAGE = "Error while fetching open issue details ";

    String STATUS_FAILED = "Failed";
    String DOC = "doc";
    String EXCEPTION_SIZE = "exception size";
    String EXEMPTION_RAISED_ON = "exemption-raised-on";
    String EXEMPTION_EXPIRING_ON = "exemption-expiring-on";
    String EXEMPTION_RAISED_EXPIRING_ON = "exemption-raised-expiring-on";

    String REASON_TO_EXEMPT_KEY = "reason-to-exempt";

    String NODE_AGGREGATIONS = "aggregations";

    String NODE_BUCKETS = "buckets";
    String NODE_KEY = "key";

    String OPTION_TYPE = "optionType";
    String OPTION_RANGE = "optionRange";

    String POLICY_COMPLIANCE_FILTER = "policyComplianceFilter";
    String OPTION_LIST = "optionList";
    String OPTION_VALUE = "optionValue";
    String BOOLEAN_TYPE  = "boolean";

    String MIN = "min";

    String MAX = "max";

    String EXCEPTION_IN_GETTING_COUNT_OF_POLICY_IDS = "Exception in getting count of policy ids";
    String CREATED_BY = "createdBy";
    Integer ES_PAGE_SIZE = 10000;
    String ES_PAGE_SCROLL_TTL = "2m";
    String ES_DOC_ID_KEY = "_id";
    String ES_DOC_PARENT_KEY = "_parent";
    String ES_DOC_ROUTING_KEY = "_routing";
    String MESSAGE_KEY = "message";
    String FAIL_MESSAGE = "failed";
    String STATUS_KEY = "message";
    String STATUS_SUCCESS = "success";
    String DATA_KEY = "data";
    String TYPE = "type";
    String SUBTYPE = "subtype";
    String LATEST = "latest";
    String TRUE = "true";
    String ISSUE = "issue";
    String ISSUES= "issues";

    String RESOURCEID_KEYWORD = "_resourceid.keyword";
    String RECOMMENDATION = "RECOMMENDATION";
    String CLOSED = "closed";
    String OPEN = "open";
    String EXEMPTION_START_DATE = "requestedExemptionStartDate";
    String EXEMPTION_END_DATE = "requestedExceptionEndDate";
    String EXEMPTION_REQUEST_RAISED = "requested";
    String EXEMPTION_REQUEST_CANCELLED = "cancelled";
    String EXEMPTION_REQUEST_APPROVED = "approved";
    String EXEMPTION_REQUEST_REVOKED = "revoked";
    String EXEMPTION_REQUEST_REVOKED_BY = "exemption-request-revoked-by";
    String EXEMPTION_REQUEST_REVOKED_ON = "exemption-request-revoked-on";
    String EXEMPTION_REQUEST_CANCELLED_BY = "exemption-request-cancelled-by";
    String EXEMPTION_REQUEST_APPROVED_BY = "exemption-request-approved-by";
    String EXEMPTION_REQUEST_CANCELLED_ON = "exemption-request-cancelled-on";
    String EXEMPTION_REQUEST_APPROVED_ON = "exemption-request-approved-on";
    String POLICY_CATEGORY = "policyCategory";
    String ISSUE_STATUS = "issueStatus";
    String INCLUDE_EXEMPT = "include_exempt";
    String SEVERITY = "severity";
    String NAME = "name";
    String DOMAINS = "domains";

    String GROUP_NAME = "groupName";
    String COMPLIANCEPERCENT="compliance-percent";
    String COMPLIANT="compliant";
    String POLICYID = "policyId";
    String OPEN_ISSUES = "openIssues";
    String EC2_MANDATORY_TAG_RULE = "ec2mandatorytags_version-1_ec2mandatorytags_ec2";
    String EC2_KERNEL_COMPLIANCE_RULE = "cloud-kernel-compliance_version-1_Ec2-Kernel-Compliance-Rule_ec2";
    String VULNAR_SEVERITY_LEVEL = "vuln.list.hostAssetVuln.severitylevel";
    String SSL_EXPIRY_RULE = "certificate-expiry-policy_version-1_SSLcertificatesexpirywithin45days_cert";
    String KEY = "key";
    String VALUE = "value";
    String PARAMS = "params";
    String POLICY_NAME = "policyName";
    String DISPLAY_NAME = "policyDisplayName";
    String ASSETS_SCANNED = "assetsScanned";
    String ASSETS = "assets";
    String PASSED = "passed";
    String FAILED = "failed";
    String POLICY_DISPAY_NAME = "policyDisplayName";
    String POLICY_PARAMS = "policyParams";
    String TARGET_TYPE = "targetType";
    String ISSUE_RESOURCE_ID = "resourceId";
    String ISSUE_EXCEPTION = "issueException";
    String ISSUE_EXEMPTION_REASON = "exemptionReason";
    String DELIMITER_UNDERSCORE = "_";
    String DELIMITER_COMMA = ",";
    String RESOURCE_TYPE = "resourcetType";
    String ANNOTATIONID = "annotationid";
    String POLICY_DESC = "policyDesc";
    String TITLE = "title";
    String DESCRIPTION = "description";
    String AGE = "Age";
    String METRIC_NAME = "metricName";
    String METRIC_VALUE = "metricValue";
    String COUNT = "count";
    String SEVEITY_LEVEL = "severitylevel";
    String DATE = "date";
    String NETWORK_IN = "networkIn";
    String NETWORK_OUT = "networkOut";
    String CPU_UTILIZATION = "cpu-utilization";
    String DISK_READ_IN_BYTES = "diskReadinBytes";
    String DISK_WRITE_IN_BYTES = "diskWriteinBytes";
    String AWS = "aws";
    String REDHAT = "redhat";
    String RESOURCEID = "_resourceid";
    String AUDIT_DATE = "auditdate";
    String STATUS = "status";
    String ENABLED = "ENABLED";
    String DISABLED = "DISABLED";
    String DATA_SOURCE = "datasource";
    String DELIMITER = "*";
    String _ID = "_id";
    String ID = "id";
    String EXEMPTED = "exempted";
    String EXEMPT = "exempt";
    String REQUEST_EXEMPT = "request exemption";
    String REVOKE_EXEMPT = "revoke request";
    String DENY_EXEMPT = "deny request";
    String GRANT_EXEMPT = "grant request";
    String MODIFIED_DATE = "modifiedDate";
    String PAC_DS = "pac_ds";
    String POLICY_VIOLATION = "#violation";
    String CONTRIBUTION = "contribution";
    String COMPLIANCE_PERCENTAGE = "compliance";
    String COMPLIANCE_PERCENT = "compliance_percent";
    String LAST_SCAN = "lastScan";
    String CATEGORY_TAGGING = "tagging";
    String TAGGED = "tagged";
    String POLICY_DETAILS_BY_ENVIRONMENT = "policydetailsbyenvironment";
    String POLICY_DETAILS_BY_APPLICATION = "policydetailsbyapplication";
    String NON_COMPLIANCE_POLICY = "noncompliancepolicy";
    String OVERALL = "overall";
    String DISCOVERED_DAYS_RANGE = "discoveredDaysRange";
    String TOTAL_ISSUES = "total_issues";
    String TAGS_APPLICATION = "tags.Application";
    String MISSING_TAGS = "missingTags";
    String ACCOUNT_ID = "accountid";
    String ACCOUNT_NAME = "accountname";
    String REGION = "region";
    String CREATED_DATE = "createdDate";
    String TAGS_ENVIRONMENT = "tags.Environment";
    String DESC = "desc";
    String ASC = "asc";
    String ORDER = "order";

    String FIELD = "field";
    String POLICY_DISPLAY_NAME = "PolicyName";
    String POLICY_DISPLAY_ID = "PolicyId";
    String ISSUE_ID = "IssueId";
    String RESOURCE_DISPLAY_ID = "ResourceId";
    String APPLICATION = "Application";
    String ENVIRONMENT = "Environment";
    String REGION_DISPALY_NAME = "Region";
    String SEVERITY_DISPALY_NAME = "Severity";
    String POLICYCATEGORY_DISPALY_NAME = "PolicyCategory";
    String ACCOUNT_DISPLAYI_D = "AccountId";
    String ACCOUNT_DISPALY_NAME = "AccountName";
    String CREATED_DISPLAY_DATE = "CreatedOn";
    String MODIFIED_DISPLAY_DATE = "ModifiedOn";
    String STATUS_DISPLAY_NAME = "Status";
    String RUNNING = "running";
    String PLATFORM = "platform";
    String STATE_NAME = "statename";
    String WINDOWS = "windows";
    String ONPREM_KERNEL_COMPLIANCE_RULE = "onpremisekernelversion_version-1_onpremKernelVersionRule_onpremserver";
    String CLOUD_QUALYS_RULE = "Ec2InstanceScannedByQualys_version-1_Ec2-instance-scanned-by-qualys-API_ec2";
    String PROJECT_ID = "_projectid";
    String REPOSITORY_ID = "_repositoryid";
    String BRANCH = "branches";
    String INSCOPE = "inScope";
    String DOMAIN = "domain";
    String DEFAULT_DOMAIN = "Infra & Platforms";
    String DOMAIN_NAME = "domainName";
    String ENV = "environment";
    String ASSET_MANDATORY = "Asset group is mandatory";
    String ASSET_CATEGORY_MANDATORY = "Asset group and Category is mandatory";
    String ASSET_GROUP_DOMAIN = "Asset group/Domain is mandatory";
    int TWO_HUNDRED = 200;
    Double HUNDRED = 100.00;
    String ONPREMSERVER = "onpremserver";
    String EXE_CERT_EXPIRY = "Exception in getCerticatesExpiryByApplication";
    int FOUR = 4;
    int START_YEAR = 1900;
    int LAST_YEAR = 2100;
    int FIVE_THOUSAND = 5000;
    int NEG_FIFTEEN = -15;
    int NEG_THIRTY = -30;
    String RESPONSE = "response";
    String EXE_VULN = "Exception in getVulnerabilitiesDetails";
    int TEN_THOUSAND = 10000;
    String CERT_SEARCH = "/cert/_search";
    String BUCKETS = "buckets";
    String EXP_IN_45_DAYS = "expiry45Days";
    String EXP_IN_30_DAYS = "expiry30Days";
    String DOC_COUNT = "doc_count";
    String DOC_TYPE = "docType";
    String DOC_TYPE_KEYWORD = "docType.keyword";

    String COMPLIANCE = "compliance";
    String EXP_IN = "expiringIn";
    String THIRTY = "30";
    String FOURTYFIVE = "45";
    String SSL_CERT_45_DAYS_EXP_RULE = "certificate-expiry-policy_version-1_SSLcertificatesexpirywithin45days_cert";
    String SSL_CERT_30_DAYS_EXP_RULE = "certificate-expiry-policy_version-1_certificates-expiry-next-30days_cert";
    String VALID_TO = "validto";
    String UNDERSCORE_COUNT = "_count";
    String ISSUE_DETAILS = "issueDetails";
    String VULNERABILITY_DETAILS = "vulnerabilityDetails";
    String ERROR_READING_VULNERABILITY = "Error in parsing vulnerability details";
    String QUALYS_ISSUE_DETAILS = "qualysIssueDetails";
    String QUALYS_VIOLATION_DETAILS = "qualysViolationDetails";
    String VIOLATION_REASON = "violationReason";
    String SEARCH = "_search";
    String INFRA_AND_PLATFORMS = "Infra & Platforms";
    String DATE_FORMAT = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'";
    String DATE_FORMAT_FOR_BANNER = "MMM dd, YYYY";
    String ERROR_IN_US = "error retrieving inventory from ES";
    String SOX = "sox";
    String AGGREGATIONS = "aggregations";
    String MAXDATE = "maxdate";
    String KERNEL_VERSION = "kernelVersion";
    String DENOMINATOR = "denominator";
    String NUMERATOR = "numerator";
    String RULE_DESC = "ruleDescription";
    String RESOLUTION = "resolution";
    String CERTIFICATES = "certificates";
    String OUTPUT = "output";
    String MATCH = "match";
    String MUST = "must";
    String DIRECTOR = "director";
    String EXCUTIVE_SPONSOR = "executiveSponsor";
    String UNPATCHED_INSTANCE = "unpatched_instances";
    String NO_DATA_FOUND = "No Data Found";
    String UNPATCHED_INSTANCES = "unpatched_instances";
    String TOTAL_INSTANCES = "total_instances";
    String PATCHING_PERCENTAGE = "patching_percentage";
    String EC2 = "ec2";
    String POLICYID_KEYWORD = "policyId.keyword";
    String POLICY_VERSION = "policyVersion";
    String COMPLIANTPERCENTAGE = "compliantPercentage";
    String TOTAL = "total";
    String ISSUE_REASON = "issueReason";
    String HITS = "hits";
    String DOCID = "_docid";

    String UNMAPPED_TYPE = "unmapped_type";
    String LONG = "long";
    int INT_HUNDRED = 100;
    String RANGE = "range";
    String AG_STATS = "assetgroup_stats";
    String VULN_INFO = "vulninfo";
    String APPS = "application";
    String SCROLL = "?scroll=";
    String SLASH_SCROLL = "/scroll";
    String TAGS_APPS = "tags.Application.keyword";
    String SEVERITY_INFO = "severityInfo";
    String QUALYS_INFO = "qualysinfo";
    String MATCH_ALL = "match_all";
    String VULN = "vulns";
    String KEYWORD = ".keyword";
    String VULNEREBILITIES = "vulnerabilities";
    String AGING = "aging";
    String ZERO = "0";
    String VULN_COUNT = "vulnInstanceCount";
    int THREE = 3;
    int FOUR_NOT_THREE = 403;
    int TEN = 10;
    int FIVE = 5;
    int ONE = 1;
    int ELEVEN = 11;
    String COMPLAINT = "compliant";
    String NON_COMPLIANT = "noncompliant";
    String NON_COMPLIANT_KEY = "non-compliant";
    int NINE_THOUSAND_THREE_HUNDRED = 9300;
    String NON_COMPLIANT_NUMBER = "nonCompliantNumber";
    String APP_TAG = "appTag";
    String KERNEL_RELEASE = "kernel_release";
    String PATCHED_INSTANCES = "patched_instances";
    String PATCHED_INSTANCE = "patched_instances";
    String START_DATE = "start_date";
    String END_DATE = "end_date";
    String INSTANCE_ID = "instanceid";
    String PATCHED = "patched";
    String UN_PATCHED = "unpatched";
    String CRITICAL = "critical";
    String MEDIUM = "medium";
    String LOW = "low";
    String HIGH = "high";
    int THOUSAND = 1000;
    int SIXTY = 60;
    String RESPONSE_CODE = "responseCode";
    String LAST_ACTIONS = "lastActions";
    String SEV_INFO = "severityinfo";
    String ASSETS_AFFECTED = "assetsAffected";
    String CATEGORY = "category";

    String ASSET_TYPE = "assetType";
    String VULN_TYPE = "vulntype";
    String PATCHABLE = "patchable";
    String ATTRIBUTES = "attributes";
    String SRC_NAME = "srcname";
    String DOUBLE_ZERO = "0.0";
    String TAGS_ENV = "tags.Environment.keyword";
    int TWO = 2;
    String ISSUE_UNDERSCORE = "issue_";
    String HAS_PARENT = "has_parent";
    int THOUSAND_TWENTY_FOUR = 1024;
    int TWELVE = 12;
    int SIX = 6;
    int FOURTEEN = 14;
    int SEVEN = 7;
    String NOT_ELIGIBLE_PROJECTIONS = "asset group not eligible for projection";
    String CLOUD = "Cloud";
    String ONPREM = "OnPrem";
    String IP_ADDRESS = "ip_address";
    String VPC_ID = "vpcid";
    String SOURCE = "source";
    String ERROR_UNIQUEHOST = "Error in getUniqueHost from ES";
    String ISSUE_COUNT_PER_RULE_AG = "issuecountPerRuleAG";
    String ASSET_COUNT = "assetCount";
    String CONTR_PERCENTAGE = "contributionPercentage";

    String IS_ASSETS_EXEMPTED = "isAssetsExempted";
    String CONTRIBUTION_PERCENT = "contribution_percent";
    String COMP_PERCENTAGE = "compliancePercentage";
    String WORKING = "working";
    String EXE_EMAIL_SEND = "Exception in sendTextMail";
    String APP_TAB_DETAILS = "appTableDetails";
    String CRITICAL_ISSUE_COUNT = "criticalIssuesCount";
    String TOP_APP_COUNT = "topAppCount";
    String EMAIL_SERVICE_STARTED = "Execution of Email Service for Asset Group Started";
    String EMAIL_SERVICE_COMPLETED = "Email Service for Asset Group Succeessfully Completed";
    String PATCHING_EXCEPTION = "ServiceException in getPatchingDetails";
    String DISTRIBUTION = "distribution";
    String HIGHEST = "Highest";
    String DIRECTORS = "directors";
    String UNIQUE_QID = "unique-qid";
    String TOTAL_VULN_ASSETS = "totalVulnerableAssets";
    String UNIQUE_VULN_COUNT = "uniqueVulnCount";
    String NOTE_ID = "noteId";
    String SEVERITY_LEVELS = "3,4,5";
    String ACTION = "action";
    String SUB_ACTIONS = "subActions";
    String MATCHING_STRING = "matchingString";
    String FIRST_DISCOVERED_ON = "firstdiscoveredon";
    String SSM_AGENT_RULE = "SSMAgentCheckRule_version-1_SSMAgentCheckRule_ec2";
    String SERVICE_DNS_NAME = "service.dns.name";
    String AUTHORIZATION = "Authorization";
    String BEARER = "bearer";
    String BASIC_AUTH = "basicAuth";
    long MILLIS_ONE_DAY = 86400000;
    String STATUS_FAILURE = "fail";
    String ERROR_MESSAGE = "errorMessage";
    String ERROR_DETAILS = "errorDetails";
    String PROVIDER = "provider";
    String FILTER_MANDATORY = "Filter is mandatory, pass the resourceid/docid/issueid/planid";
    String EC2_QUALYS_RULEID = "Ec2InstanceScannedByQualys_version-1_Ec2-instance-scanned-by-qualys-API_ec2";
    String VIRTUALMACHINE = "virtualmachine";
    String VIRTUALMACHINE_QUALYS_RULEID = "Ec2InstanceScannedByQualys_version-1_VmInstanceScannedByQualys_virtualmachine";
    String ONPREM_QUALYS_RULEID = "Onprem-asset-scanned-by-qualys-API_version-1_OnpremassetscannedbyqualysAPI_onpremserver";
    String AZURE = "azure";
    String MASTER_ALIAS = "ds-all";
    String ROOT_ALIAS = "*";
    String CLOUD_TYPE_KEYWORD = "_cloudType.keyword";
    String AGGS_NAME_PROVIDERS = "providers";
    String APPLICATION_COUNT = "applicationCount";
    String APPLICATION_PROVIDERS = "applicationproviders";
    String TYPE_COUNT = "typecount";
    String PROVIDERS = "providers";
    String ENVIRONMENTS = "environments";
    String ENV_COUNT = "envCount";
    String PRODUCTION_ENV = "Prod";
    String STAGE_ENV = "Stage";
    String DEV_ENV = "Dev";
    String NPE_ENV = "NPE";
    String OTHER_ENV = "Others";
    String UNTAGGED_ENV = "Untagged";
    String QUALYS = "qualys";
    String PERCENTAGE = "percentage";
    String PROD_PATTERN = "^(((prod)(uction)?)|((prd).*))(:+(((prod)(uction)?)|((prd).*)))?";
    String STG_PATTERN = "(^(stag|stg).*)|(.*:+(stag|stg).*)";
    String DEV_PATTERN = "(^(dev|development).*)|(.*:+(dev).*)";
    String NPE_PATTERN = "(^(npe|non.?prod(uction)?))|(.*:+(npe).*)";
    String AZURE_WINDOWS = "Windows";
    String CLOUD_KERNEL_COMPLIANCE_POLICY = "cloud-kernel-compliance_version-1";
    String CLOUD_QUALYS_POLICY = "Ec2InstanceScannedByQualys_version-1";
    String VIRTUALMACHINE_KERNEL_COMPLIANCE_RULE = "cloud-kernel-compliance_version-1_Virtualmachine-Kernel-Compliance-Rule_virtualmachine";
    String GCP = "gcp";
    String RESOLUTION_URL = "resolutionUrl";
    String UNABLE_TO_DETERMINE = "unable to determine";
    String STATUS_REASON = "status-reason";
    String AUTOFIX_ENABLED = "autoFixEnabled";
    String AUTOFIX_AVAILABLE = "autoFixAvailable";
    String AUTOFIX = "autofix";
    String AQUA = "aqua";
    String ISSUE_ID_UI_PATH  = "#pl#compliance#issue-listing#issue-details#".replace("#","/");
    String FIELD_NAME = "fieldName";

    String POLICY_DETAILS_UI_PATH  = "#pl#compliance#policy-knowledgebase-details#".replace("#","/");

    String ASSET_DETAILS_UI_PATH  =  "#pl#assets#asset-list#".replace("#","/");

    String DATA_SOURCE_KEY = "pac_ds";
    String TENABLE = "tenable";

    String RELATIONS = "_relations";

    String AUTOFIX_PLANNED = "isAutofixPlanned";

    enum NotificationTypes {
        @SerializedName("exemption")
        EXEMPTION("exemption"),
        @SerializedName("violation")
        VIOLATION("violation"),
        @SerializedName("autofix")
        AUTOFIX("autofix"),
        @SerializedName("policy")
        POLICY("policy");

        String value;

        @Override
        public String toString() {
            return super.toString().toLowerCase();
        }

        public String getValue() {
            return this.value;
        }

        NotificationTypes(String value){
            this.value=value;
        }

    }

    enum Actions {
        @SerializedName("create")
        CREATE,
        @SerializedName("create_exemption_request")
        CREATE_EXEMPTION_REQUEST,
        @SerializedName("revoke_exemption_request")
        REVOKE_EXEMPTION_REQUEST,
        @SerializedName("cancel_exemption_request")
        CANCEL_EXEMPTION_REQUEST,
        @SerializedName("approve_exemption_request")
        APPROVE_EXEMPTION_REQUEST,
        @SerializedName("revoke")
        REVOKE,
        @SerializedName("delete")
        DELETE,
        @SerializedName("update")
        UPDATE;
        @Override
        public String toString() {
            return super.toString().toLowerCase();
        }
    }
    String EVENT_SOURCE = "paladinCloud";
    String EVENT_SOURCE_NAME = "Paladin Cloud";
    String CLOUD_TYPE = "cloudType";
    String TAG_DETAILS = "tagDetails";
    String NOTIFICATION_INDEX = "notification";
    String NOTIFICATION_INDEX_TYPE = "notification";
    String NOTIFICATION_CATEGEORY_NAME = "eventCategoryName";
    String NOTIFICATION_SOURCE_NAME = "eventSourceName";
    String NOTIFICATION_EVENT_NAME = "eventName";

    String AUTOFIX_PLANNED_KEYWORD = "isAutofixPlanned.keyword";
    String TYPE_KEYWORD = "type.keyword";
    String ACTION_ILLEGAL_ARG_ERR_MSG  =  "Action value should be one of the following create_exemption_request," +
            " cancel_exemption_request, approve_exemption_request, revoke_exemption_request";
    String AVERAGE_AGE = "averageAge";

    enum ExemptionActions {
        CREATE_EXEMPTION_REQUEST("create_exemption_request"),
        CANCEL_EXEMPTION_REQUEST("cancel_exemption_request"),
        APPROVE_EXEMPTION_REQUEST("approve_exemption_request"),
        REVOKE_EXEMPTION_REQUEST("revoke_exemption_request");

        private final String text;

        ExemptionActions(String text) {
            this.text = text;
        }

        @Override
        public String toString() {
            return text;
        }

        @JsonCreator
        public static ExemptionActions fromText(String text){
            for(ExemptionActions r : ExemptionActions.values()){
                if(r.toString().equalsIgnoreCase(text)){
                    return r;
                }
            }
            throw new IllegalArgumentException(ACTION_ILLEGAL_ARG_ERR_MSG);
        }
    }

    String FILTER_TAGGED = "tagged";
    String FILTER_UNTAGGED = "untagged";
    String FALSE = "false";
    String TARGET_TYPE_DISPLAY_NAME="targetTypeDisplayName";

    String USER_POLICY_FILTER = "policyknowledgebase";

    List<String> RANGE_ATTRIBUTE =  new ArrayList<String>() {{
        add("LONG");
        add("INTEGER");
        add("DOUBLE");
        add("FLOAT");
    } };
}
