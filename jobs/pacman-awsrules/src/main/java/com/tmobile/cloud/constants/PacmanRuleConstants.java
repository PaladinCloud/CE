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
 Author :santoshi
 Modified Date: Jun 22, 2017

 **/
package com.tmobile.cloud.constants;

public class PacmanRuleConstants {

    public static final String UDP = "UDP";
    public static final String NAME = "name";
    public static final String ES_VM_URL = "esvmURL";
    public static final String GCP_NETWORK_INTERFACE = "networkInterfaces";
    public static final String GCP_ACCESS_CONFIGS = "accessConfigs";
    public static final String GCP_NAT_IP = "natIP";
    public static final String USERS = "users";
    public static final String ALL_USERS = "allUsers";
    public static final String ALL_AUTH_USERS = "allAuthenticatedUsers";
    public static final String SPECIAL_GROUP = "specialGroup";
    public static final String IAM_MEMBER = "iamMember";
    public static final String ITEMS = "items";
    public static final String KEY = "key";
    public static final String VALUE = "value";
    public static final String TWO_FACTOR_AUTH = "enable-oslogin-2fa";
    public static final String PUBSUB_KMS_NAME = "kmsKeyName";
    public static final String VIOLATION_REASON_POLICYNAME = "policyName";
    public static final String BLOB_PUBLIC_ACCESS_ALLOWED = "blobPublicAccessAllowed";
    public static final String GCP_CMK = "hasKmsKeyName";
    public static final String GCP_CSEK = "hasSha256";
    public static final String ADMIN_FOR_KEYS = "adminForKeys";
    public static final String ADMIN_FOR_SECRETS = "adminForSecrets";
    public static final String ADMIN_FOR_CERTIFICTAES = "adminForCertificates";
    public static final String STORAGE_KMS_KEY_NAME = "defaultKmsKeyName";
    public static final String FIREWALL_RULE_NAME = "firewallRuleName";
    public static final String GCP_ON_HOST_MAINTENANCE ="onHostMaintainence" ;
    public static final String MIGRATE ="MIGRATE" ;
    public static final String AVAILABILITY = "availabilityType";
    public static final String SETTINGS ="settings" ;
    public static final String AZURE_AUTHENABLED ="authEnabled" ;

    private PacmanRuleConstants() {
        throw new IllegalAccessError("Constant class");
    }

    public static final String CIDR_FILTERVALUE = "0.0.0.0/0";
    public static final String SUBNT_FILTERNAME = "association.subnet-id";
    public static final String TRUE_VAL = "true";
    public static final String SECURITYGROUP_ID = "securityGroupId";
    public static final String PORT_TO_CHECK = "portToCheck";
    public static final String PORT_22 = "22";
    public static final String PORT_ALL = "ALL";
    public static final String YES = "YES";
    public static final String NO = "NO";
    public static final String REGION = "region";
    public static final String INTSANCE_NAME = "Instance Name";
    public static final String RUNNING_STATE = "running";
    public static final String STOPPED_STATE = "stopped";
    public static final String STOPPED_INSTANCE = "Stopped";
    public static final String MANDATORY_TAGS_MISSING_FLG = "mandatoryTagsMissingFlg";
    public static final String FOUND = "Found";
    public static final String NOTFOUND = "Not Found";
    public static final String SEVERITY = "severity";
    public static final String SUBTYPE = "subtype";
    public static final String MEDIUM = "MEDIUM";
    public static final String MISSING_TAGS = "missingTags";
    public static final String SUCCESS_MESSAGE = "Rule evaluation sucessfull";
    public static final String S3_BUCKET_NAME = "s3BucketName";
    public static final String ANY_S3_AUTHENTICATED_USER_URI = "http://acs.amazonaws.com/groups/global/AuthenticatedUsers";
    public static final String ALL_S3_USER_URI = "http://acs.amazonaws.com/groups/global/AllUsers";
    // above URL have to make configurable
    public static final String PERMISSION_NAME = "permissionName";
    public static final String READ_ACCESS = "READ";
    public static final String DEPRECATED_INSTANCE_TYPE = "deprecatedInstanceType";
    public static final String WRITE_ACCESS = "WRITE";
    public static final String EXPIRY_DAY = "expiryDate";
    public static final String INTERNET_GATEWAY = "internetGateWay";
    public static final String INTSANCE_ID = "instanceId";
    public static final String SUBNET_ID = "subNetId";
    public static final String AUTH_TYPE = "authType";
    public static final String HTTP_METHOD_TYPE = "httpMethodType";
    public static final String REST_API_ID = "restApiId";
    public static final String HIGH = "high";
    public static final String FAILURE_MESSAGE = "Error in rule evaluation";
    public static final String IS_API_REQUIRED = "isApiRequired";
    public static final String FULL_CONTROL = "FULL_CONTROL";
    public static final String PORT = "port";
    public static final String SECURITY_GROUP_NAME = "securityGroupName";
    public static final String RESOURCE_NAME = "resource-id";
    public static final String OPEN_URL = "openURL";
    public static final String SERVICE_ACCOUNTS = "svc_";
    public static final String ACCOUNT_DETAILS = "accountDetails";
    public static final int ACCESSKEY_ROTATION_DURATION = 90;
    // 90 have to make configurable
    public static final String LAMBDA_FUNCTION_NAME = "functionName";
    public static final String URL = "url";
    public static final String TAGS = "tags";
    public static final String INSTANCE_TYPE_PARAM = "instanceTypeParam";
    public static final String ADMIN_ROLES_TO_COMPARE = "adminRolesToCompare";
    public static final String ADMINISTRATOR_ACCESS = "AdministratorAccess";
    public static final String IAM_FULL_ACCESS = "IAMFullAccess";
    public static final String STATEMENT = "Statement";
    public static final String ACTION = "Action";
    public static final String IAM_COLON_STAR = "iam:*";
    public static final String EFFECT = "Effect";
    public static final String ALLOW = "Allow";
    public static final String STATE = "state";
    public static final String REASON_LOGGED = "reasonLogged";
    public static final String STOPPED_DURATION = "targetstoppedDuration";
    public static final String API_KEY_NAME = "apiKeyName";
    public static final String APIGW_URL = "apiGWURL";
    public static final String API_KEY_VALUE = "apiKeyValue";
    public static final String S3_NOT_PUBLICALLY_ACCESSIBLE = "NO";
    public static final String S3_PUBLICALLY_ACCESSIBLE = "YES";
    public static final String INSTANCE_TYPE = "instancetype";
    public static final String KERNEL_CRITERIA_KEY = "pacman.kernel.compliance.map";
    public static final String STATE_TRANSITION_REASON = "statetransitionreason";
    public static final String STATE_NAME = "statename";
    public static final String PRIVATE_IP_ADDRESS = "privateipaddress";
    public static final String CATEGORY = "ruleCategory";
    public static final String ACTIVITY_RULE_CATEGORY = "category";
    public static final String ADMINISTRATIVE = "Administrative";
    public static final String KERNEL_RELEASE = "kernel_release";
    public static final String INSTANCEID = "instanceid";
    public static final String ACCOUNTID = "accountid";
    public static final String PUBLIC_IP_ADDR = "publicipaddress";
    public static final String VPC_ID = "vpcid";
    public static final String SUBNETID = "subnetid";
    public static final String HTTP_MESSAGE_FOR_PUBLIC_MONGODB = "It looks like you are trying to access MongoDB over HTTP on the native driver port.";
    public static final String PLATFORM_VAL = "windows";
    public static final String PLATFORM = "platform";
    public static final String ROLE_NAME = "rolename";
    public static final String MEMORY_SIZE = "memorysize";
    public static final String PUBLIC_ACCESS = "publiclyaccessible";
    public static final String LIMIT_SIZE = "memorySizeLimit";
    public static final String ENDPOINT_PORT = "endpointport";
    public static final String ENDPOINT_ADDR = "endpointaddress";
    public static final String FUNCTION_NAME = "FunctionName";
    public static final String LAMBDA_INVOCATION_COUNT = "InvocationCount";
    public static final String FUNCTION_NAME_RES_ATTR = "functionname";
    public static final String INVOCATIONS = "Invocations";
    public static final String AWS_LAMBDA = "AWS/Lambda";
    public static final String THRESHOLD = "threshold";
    public static final String TIME_PERIOD = "timePeriodInHours";
    public static final String THROTTLES = "Throttles";
    public static final String SUM = "Sum";
    public static final String ACCOUNT_NAMES = "accountNames";
    public static final String ACCOUNT_NAME = "accountname";
    public static final String GROUP_ID = "groupid";
    public static final String GROUP_NAME = "groupname";
    public static final String UNKNOWN_MESSAGE = "Found unknown rule evaluation";
    public static final String EC2_WITH_SECURITYGROUP_ID = "securitygroupid";
    public static final String SECURITYGROUP_ID_ATTRIBUTE = "vpcsecuritygroupid";
    public static final String LOAD_BALANCER_ID_ATTRIBUTE = "loadbalancername";
    public static final String DNS_NAME = "dnsname";
    public static final String COST_LIMIT = "costLimit";
    public static final String VOLUME_ID = "volumeid";
    public static final String COST = "cost";
    public static final String CHECK_ID = "checkId";
    public static final String APP_LOAD_BALANCER_ARN_ATTRIBUTE = "loadbalancerarn";
    public static final String REGION_ATTR = "region";
    public static final String SERVICE_LIMIT = "serviceLimit";
    public static final String STATUS_RED = "RED";
    public static final String STATUS_YELLOW = "YELLOW";
    public static final String CHECK_ID_KEYWORD = "checkid.keyword";
    public static final String EC2_SCAN_BY_QUALYS_ES_SEARCH_URL = "{ES_HOST}/aws_ec2/qualys-info/_search";
    public static final String VPC_CONFIG_ID = "vpcconfigid";
    public static final String USER_NAME = "userName";
    public static final String VALID_TO = "validto";
    public static final String COMMON_NAME = "commonname";
    public static final String EXPIRED_DURATION = "targetExpireDuration";
    public static final String TARGET_TYPE = "targetType";
    public static final String TARGET_TYPE_EC2 = "ec2";
    public static final String ES_URL_PARAM = "esUrl";
    public static final String ES_LDAP_URL = "esLdapUrl";
    public static final String ES_SATLLITE_AND_SPACEWALK_URL = "esSatAndSpacewalkUrl";
    public static final String ES_QUALYS_URL = "esQualysUrl";
    public static final String HEIMDALL_ES_URL = "heimdallESURL";
    public static final String TAGS_APP = "tags.Application";
    public static final String APP_TAG = "appTag";
    public static final String APP_TAG_KEYWORD = "appTag.keyword";
    public static final String APP_ID = "appID";
    public static final String TARGET = "target";
    public static final String REQUIRE_SYMBOLS = "requireSymbols";
    public static final String REQUIRE_NUMBERS = "requireNumbers";
    public static final String REQUIRE_UPPER_CASE_CHAR = "requireUppercaseCharacters";
    public static final String REQUIRE_LOWER_CASE_CHAR = "requireLowercaseCharacters";
    public static final String HARD_EXPIRY = "hardExpiry";
    public static final String DEFAULT_KERNEL_CRITERIA_URL = "defaultKernelCriteriaUrl";
    public static final String EC2_PORT_RULE_ID = "ec2PortRuleId";
    public static final String SEVERITY_VULN = "severityVulnValue";
    public static final String RULE_NAME = "ruleName";
    public static final String SOURCE_TYPE = "sourceType";
    public static final String FAILED_TYPES = "failedTypes";
    public static final String KERNEL_VERSION = "kernelVersion";
    public static final String LDAP = "ldap";
    public static final String SSH = "ssh";
    public static final String SPACEWALK_SAT = "spaceandsat";
    public static final String QUALYS = "qualys";
    public static final String RHN_SYSTEM_DETAILS = "rhnsystemdetails";
    public static final String DESCRIPTION = "description";
    public static final String CIDR_IP = "cidrIp";
    public static final String GUARD_DUTY_INSTANCE_ATTR = "Resource.InstanceDetails.InstanceId";
    public static final String PD_LAST_USED = "passwordlastused";
    public static final String PD_INACTIVE_DURATION = "pwdInactiveDuration";
    public static final String VALID_REGIONS = "validRegions";
    public static final String LAMBDA_ROLE = "role";
    public static final String NON_ADMIN_ACCNT_WITH_IAM_FULL_ACC_RULE_ID = "nonAdminAccntsWithIAMFullAccessRuleId";
    public static final String BLACKLISTED_REGIONS = "blackListedRegions";

    public static final String IP_ADDRESS = "ip_address";
    public static final String ASSET = "asset";
    public static final String SVC_ADDS_UNOWNED = "svc_adds_unowned";
    public static final String MANAGED_BY = "managedBy";
    public static final String SOURCE = "_source";
    public static final String LATEST = "latest";
    public static final String MEMBER_OF = "memberOf";
    public static final String ROLE = "r_";
    public static final String SVC_ATTR = "svc_";
    public static final String CAP_SVC_ATTR = "Svc_";
    public static final String CAPITAL_SVC_ATTR = "SVC_";
    public static final String SERVICE_ACCOUNT_ADMINS = "serviceAccountAdmins";
    public static final String ADMIN = "_admin";
    public static final String R_WIN = "r_win_";
    public static final String R_RHEL = "r_rhel_";
    public static final String ROLE_ATTR = "r_";
    public static final String CAP_ROLE_ATTR = "R_";
    public static final String LAST_VULN_SCAN = "lastVulnScan";
    public static final String ACCOUNT_ID_KEYWORD = "accountid.keyword";
    public static final String EST_MONTHLY_SAVINGS = "estimatedMonthlySavings";
    public static final String NO_OF_LOW_UTILIZATION = "noOfDaysLowUtilization";
    public static final String REASON = "reason";
    public static final String DB_INSTANCE_IDENTIFIER = "dbinstanceidentifier";
    public static final String INSTANCETYPE = "instanceType";
    public static final String HITS = "hits";
    public static final String TOTAL = "total";
    public static final String RESOURCE_INFO = "resourceinfo";
    public static final String STATUS = "status";
    public static final String ISSUE = "issue";
    public static final String ROUTE_TABLE_ID = "routetableid";
    public static final String SUBNET = "subnet";
    public static final String DEST_CIDR_BLOCK = "destinationcidrblock";
    public static final String GATE_WAY_ID = "gatewayid";
    public static final String CIDRIP = "cidrip";
    public static final String INBOUND = "inbound";
    public static final String U_PATCHING_DIRECTOR = "u_patching_director";
    public static final String PATCHING_DIRECTOR = "patchingDirector";
    public static final String IN_SCOPE = "inScope";
    public static final String FINAL_U_LAST_PATCHED = "final_u_last_patched";
    public static final String FINAL_KERNEL_RELEASE = "final_kernel_release";
    public static final String WEB_SERVICE = "webservice";
    public static final String ISSUE_DETAILS = "issueDetails";
    public static final String VIOLATION_REASON = "violationReason";
    public static final String RESOURCEID = "resourceId";
    public static final String COMPLIANT_NOT_FOUND = "Resource is not compliant";
    public static final String NO_DEFAULT_TARGET = "Default target kernel criteria not maintained";
    public static final String TASK = "t_";
    public static final String PING_STATUS = "pingstatus";
    public static final String ONLINE = "Online";
    public static final String SOURCE_VERIFIED = "sources_verified";
    public static final String CHECKID = "check_id";
    public static final String ESTMIATED_COST = "estimated_cost_saving";
    public static final String DEPRECATED_INSTANCE_TYPES = "deprecated_instance_type";
    public static final String PORTS_VIOLATED = "ports_violated";
    public static final String VPCID = "vpc_id";
    public static final String ROUTE_TABLEID = "routeTable_id";
    public static final String SUBID = "subnet_id";
    public static final String IGW_OPENED = "IGW_opened";
    public static final String PUBLICIP = "public_iP";
    public static final String SEC_GRP = "security_groups";
    public static final String TIME_PERIOD_HRS = "time_period_in_hours";
    public static final String FAILED_REASON_QUALYS = "failed_reason_for_qualys_scan";
    public static final String FAILED_REASON = "failed_reason";
    public static final String MISSING_CONFIGURATION = "Missing value in rule configuration, cannot execute the rule";
    public static final String CREATED_EVENT_TYPE = "created";
    public static final String SERVICE_LIMIT_STATUS_RED = "status_RED";
    public static final String MATCH_ALL = "match_all";
    public static final String QUERY = "query";
    public static final String RANGE = "range";
    public static final String ERROR_MESSAGE = "error retrieving inventory from ES";
    public static final String TERMS = "terms";
    public static final String HAS_CHILD = "has_child";
    public static final String HAS_PARENT = "has_parent";
    public static final String RESOURCE_ID = "_resourceid";
    public static final String ISSUE_REASON = "Reason";
    public static final String ESTIMATED_MONTHLY_SAVINGS = "Estimated Monthly Savings";
    public static final String PUBLIC = "public";
    public static final String THROUGH_BUCKET_POLICY = " through Bucket policy";
    public static final String INSTANCE_TYPE_CAP = "Instance Type";
    public static final String CONDITION = "Condition";
    public static final String IP_ADDRESS_CAP = "IpAddress";
    public static final String SOURCE_IP = "aws:SourceIp";
    public static final String PRINCIPAL = "Principal";
    public static final String WRITE = "Write";
    public static final String S3_PUT = "s3:Put";
    public static final String S3_GET = "s3:Get";
    public static final String STATUS_CAP = "Status";
    public static final String DOC_COUNT = "doc_count";
    public static final String APPLICATION_JSON = "application/json";
    public static final String SERVICES = "services";
    public static final String CONTENT_TYPE = "Content-Type";
    public static final String MATCH_PHRASE = "match_phrase";
    public static final String REGION_AND_COUNT = "regionAndCount";
    public static final String FIRST_DISCOVERED_ON = "firstdiscoveredon";
    public static final String FAILED_MESSAGE = "failed";
    public static final String DISCOVERED_DAYS_RANGE = "discoveredDaysRange";
    public static final String ES_URI = "ES_URI";
    public static final String ES_ADGROUP_URL = "esAdGroupURL";
    public static final String ES_CHECK_SERVICE_SEARCH_URL_PARAM = "esServiceURL";
    public static final String ES_EBS_WITH_INSTANCE_URL = "esEbsWithInstanceUrl";
    public static final String ES_APPLICATION_TAG_URL = "esAppTagURL";
    public static final String ES_EC2_SG_URL = "esEc2SgURL";
    public static final String ES_EC2_WITH_VULN_INFO_S5_URL = "esEc2WithVulnInfoForS5Url";
    public static final String ES_EC2_PUB_ACC_PORT_URL = "esEc2PubAccessPortUrl";
    public static final String ES_SSM_WITH_INSTANCE_URL = "esSsmWithInstanceUrl";
    public static final String ES_ELASTIC_IP_URL = "esElasticIpUrl";
    public static final String ES_APPLELB_WITH_INSTANCE_URL = "esAppElbWithInstanceUrl";
    public static final String ES_CLASSIC_ELB_WITH_INSTANCE_URL = "esClassicElbWithInstanceUrl";
    public static final String ES_GUARD_DUTY_URL = "esGuardDutyUrl";
    public static final String ES_NON_ADMIN_ACCNT_WITH_IAM_FULL_ACC_URL = "esNonAdminAccntsWithIAMFullAccessUrl";
    public static final String ES_SG_RULES_URL = "esSgRulesUrl";
    public static final String ES_SERVICES_WITH_SG_URL = "esServiceWithSgUrl";
    public static final String ES_SERVICE_ACCOUNT_ES_URL = "esServiceAccountURL";
    public static final String ES_ROUTE_TABLE_ASSOCIATIONS_URL = "esRoutetableAssociationsURL";
    public static final String ES_ROUTE_TABLE_ROUTES_URL = "esRoutetableRoutesURL";
    public static final String ES_ROUTE_TABLE_URL = "esRoutetableURL";
    public static final String UNKNOWN = "Unknown";
    public static final String ARN = "arn";
    public static final String STANDARD_REGIONS = "standardRegions";
    public static final String KERNEL_INFO_API = "kernelInfoApi";
    public static final String SOURCE_FIELD = "source";
    public static final String KERNEL_FIELD = "kernel";
    public static final String AWS_SEARCH = "awsSearch";
    public static final String ES_HEIMDALL_URL = "esHeimdallURL";
    public static final String HEIMDALL_URI = "HEIMDALL_URI";
    public static final String KERNEL_VERSION_BY_INSTANCEID_API = "kernelVersionByInstanceIdAPI";
    public static final String PACMAN_API_URI = "PACMAN_API_URI";
    public static final String IAM_USER_NAME = "username";
    public static final String UNAPPROVED_IAM_ACTIONS = "unApprovedIamActions";
    public static final String UNABLE_TO_GET_CLIENT = "Unable to get client";
    public static final String GLOBAL_ACCESS = "global";
    public static final String CIDRIPV6 = "cidripv6";
    public static final String DEST_CIDR_IPV6_BLOCK = "destinationipv6cidrblock";
    public static final String ANY_PORT = "ANY";
    public static final String SSH_PORT = "22";
    public static final String RDP_PORT = "3389";
    public static final String DEFAULT_CIDR_IP = "defaultCidrIp";
    public static final String END_POINT = "endpoint";
    public static final String ENTITY_TYPE = "_entitytype";
    public static final String ACCESS_POLICIES = "accesspolicies";
    public static final String RESOURCE_DISPLAY_ID = "resourceDisplayId";
    public static final String SCHEME = "scheme";
    public static final String ES_ELB_WITH_SECURITYGROUP_URL = "esElbWithSGUrl";
    public static final String ES_CLASSIC_ELB_WITH_SECURITYGROUP_URL = "esClassicElbWithSGUrl";
    public static final String SUBNETS_LIST = "subnets";
    public static final String INTERNET_FACING = "internet-facing";
    public static final String WEB_SITE_CONFIGURATION = "websiteConfiguration";
    public static final String ELB_TYPE = "type";
    public static final String TYPE_OF_ELB = "elbType";
    public static final String ES_REDSHIFT_SG_URL = "esRedshiftSgURL";
    public static final String ALLOCATION_ID = "allocationid";
    public static final String ASSOCIATION_ID = "associationid";
    public static final String SECURITY_GROUPS = "securitygroups";
    public static final String ES_RDSDB_SG_URL = "esRdsDbSgUrl";
    public static final String S3_PUBLIC_ACCESS_RULE_ID = "s3PublicAccessRuleId";
    public static final String ES_S3_PUBLIC_ACCESS_ISSUE_URL = "esS3PubAccessIssueUrl";
    public static final String IS_S3_ACCESS_LOGS_ENABLED = "isLoggingEnabled";
    public static final String DESTINATION_BUCKET_NAME = "destinationBucketName";
    public static final String LOG_FILE_PREFIX = "logFilePrefix";
    public static final String DESTINATION_BUCKET_AUTOFIX = "destinationBucketForAutofix";
    public static final String ACCESSLOGS_ENABLED_REGIONS = "accessLogsEnabledRegions";
    public static final String RULE_ID = "ruleId";
    public static final String STATUS_EXEMPTED = "exempted";
    public static final String ES_RESOURCE_WITH_VULN_INFO_SEVERITY_URL = "esResourceWithVulnInfoForSeverityUrl";
    public static final int FIRST_DISCOVERED_DATE_FORMAT_LENGTH = 10;
    public static final String POLICYNAME = "recommendation.policyName";
    public static final String AZURERESOURCEID = "recommendation._resourceIdLower";
    public static final String RECOMMENDATION = "recommendation";
    public static final String DETAILS = "details";
    public static final String AZURE_SUBSCRIPTION = "subscription";
    public static final String AZURE_SUBSCRIPTION_NAME = "subscriptionName";
    public static final String IS_PASSWORD_BASED_AUTHENTICATION_DISABLED = "passwordBasedAuthenticationDisabled";
    public static final String IS_ENCRYPTION_ENABLED = "isEncryptionEnabled";
    public static final String DISKS = "disks";

    public static final String VM_EXTENSIONS = "extensionList";
    public static final String DISK_TYPE = "type";
    public static final String OSDISK = "OSDisk";

    public static final String NOTIFICATION_RECEPIENTS_EMAILS = "notificationRecipientsEmails";
    public static final String START_IP_ADDRESS = "startIPAddress";
    public static final String FIREWALL_RULE_DETAILS = "firewallRuleDetails";
    public static final String AZURE_INBOUNDARYSECURITYRULES = "inBoundSecurityRules";

    public static final String AZURE_REMOTEDEBUGGING = "remoteDebuggingEnabled";
    public static final String CUSTOMER_MANAGED_KEY = "customerManagedKey";

    public static final String AZURE_NONSSLPORT = "nonSslPort";
    public static final String SECURITY_RULE_SOURCEADDRESSPREFIXES = "sourceAddressPrefixes";
    public static final String PROTOCOL = "protocol";
    public static final String DESTINATIONPORTRANGES = "destinationPortRanges";
    public static final String PORT_1433 = "1433";

    public static final String PORT_3306 = "3306";
    public static final String PORT_ANY = "*";
    public static final String INTERNET = "internet";
    public static final String ANY = "any";
    public static final String PROTOCOL_TCP = "TCP";
    public static final String RESOURCE_DATA_NOT_FOUND = "Resource data not found!! Skipping this validation";

    public static final String PORT_5432 = "5432";
    public static final String ES_VAULT_POLICY_ATTRIBUTE = "accessPolicy";
    public static final String DENY = "Deny";
    public static final String FALSE = "false";
    public static final String ES_ASG_LC_URL = "esAsgLcURL";
    public static final String ES_AMI_URL = "esAmiUrl";
    public static final String ES_ASG_ARN_ATTRIBUTE = "autoscalinggrouparn";
    public static final String ES_CONFIG_ARN_ATTRIBUTE = "launchconfigurationarn";
    public static final String ES_SNAPSHOT_ID_ATTRIBUTE = "snapshotid";
    public static final String ES_IMAGE_ID_ATTRIBUTE = "imageid";
    public static final String ES_AMI_BLOCK_DEVICE_MAPPING_URL = "esAmiBlockDeviceMappingUrl";
    public static final String ES_RESOURCE_ATTRIBUTE = "resource";
    public static final String STATUS_ACTIVE = "Active";
    public static final String ES_ACCESS_ANALYZER_FINDINGS_URL = "esFindingsUrl";
    public static final String ES_ANALYZER_ARN_ATTRIBUTE = "analyzerarn";
    public static final String ES_LOG_DRIVER_ATTRIBUTE = "logdriver";
    public static final String ES_KMS_ARN_ATTRIBUTE = "kmsarn";
    public static final String ES_ENCRYPTION_OPTION_ATTRIBUTE = "encryptionoption";
    public static final String ES_SSE_STATUS_ATTRIBUTE = "ssestatus";
    public static final String ES_KEY_ARN_ATTRIBUTE = "keyarn";
    public static final String ES_ENCRYPTED_ATTRIBUTE = "encrypted";
    public static final String ES_IAM_USER_KEY_URL = "esIamUserKeyUrl";
    public static final String ES_IAM_USER_ACCESS_KEY_ATTRIBUTE = "accesskey";
    public static final String ES_DMS_PUBLIC_ACCESS_ATTRIBUTE = "publiclyAccessible";
    public static final String ES_DYNAMO_DB_SSE_TYPE_ATTRIBUTE = "ssetype";
    public static final String ES_KMS_KEY_MANAGER_ATTRIBUTE = "keymanager";
    public static final String ES_KMS_ALIAS_ATTRIBUTE = "aliasname";
    public static final String ES_KMS_URL = "esKmsUrl";
    public static final String ES_KMS_KEY_ID_ATTRIBUTE = "kmskeyid";
    public static final String ES_SG_FROM_PORT_ATTRIBUTE = "fromport";
    public static final String ES_SG_TO_PORT_ATTRIBUTE = "toport";
    public static final String PROTOCOL_ALL = "All";
    public static final String PROTOCOL_ICMP = "ICMP";
    public static final String ES_SG_IP_PROTOCOL_ATTRIBUTE = "ipprotocol";
    public static final String ELB_V2_ARN_ATTRIBUTE = "loadBalancerarn";
    public static final String ELB_PROTOCOL = "protocol";
    public static final String PROTOCOL_SSL = "SSL";
    public static final String PROTOCOL_HTTPS = "HTTPS";
    public static final String PROTOCOL_TLS = "TLS";
    public static final String ES_ELB_V2_LISTENER_URL = "esElbV2ListenerURL";
    public static final String ES_CLASSIC_ELB_LISTENER_URL = "esClassicELBListenerURL";
    public static final String ES_SG_URL = "esSgURL";
    public static final String ES_ACCOUNT_URL = "accountEsURL";
    public static final String STORAGE_ENCRYPTED = "storageencrypted";
    public static final String INCLUDE_GLOBAL_SERVICE_EVENTS = "includeglobalserviceevents";
    public static final String EXCLUDEDDETECTIONTYPES = "excludedDetectionTypes";
    public static final String FIELD = "field";
    public static final String EQUALS_STRING = "equals";
    public static final String SUCESS = "SUCESS";
    public static final String FAILURE = "failure";
    public static final String ALLOF = "allOf";
    public static final String PRICING_TIER = "pricingTier";
    public static final String PROPERTIES = "properties";
    public static final String KEYVAULTNAMES = "keyValutName";
    public static final String KEYVAULTKEY = "keyVaultKey";
    public static final String KEYVAULTVALUE = "keyVaultValue";
    public static final String SUCCESS = "SUCCESS";
    public static final String PROPERTIESMAP = "propertiesMap";
    public static final String DISABLED = "disabled";
    public static final String DIRECTION = "direction";
    public static final String SOURCERANGES = "sourceRanges";
    public static final String INGRESS = "INGRESS";
    public static final String PORTS = "ports";
    public static final String ICMP = "icmp";
    public static final String SOURCERANGE = "0.0.0.0/0";
    public static final String ACL = "acl";
    public static final String KMS_KEY_NAME = "kmsKeyName";
    public static final String DESTINATIONRANGE = "destinationRanges";
    public static final String EGRESS = "egress";
    public static final String HTTP20ENABLED = "http20Enabled";
    public static final String MASTRERAUTHORRIZEDNETWORKCONFIG = "masterAuthorizedNetworksConfig";
    public static final String DISKENCRYPTION = "diskEncryption";
    public static final String KEYNAME = "keyName";
    public static final String BOOTDISKKMSKEY = "bootDiskKmsKey";
    public  static final String BACKUP_CONFIG="backupConfiguration";
    public  static  final String DBFLAGS="databaseFlags";
    public static final String DB_PROPERTY_OWNER_CHANGING_FLAG="crossdbownershipchaining";
    public static final String OFF="off";
    public static final String NETWORKRULEBYPASS="networkruleBypass";
    public  static  final  String HTTPS_ONLY="httpsOnly";
    public  static  final String FTP_STATE="ftpState";
    public  static  final String TLS_VERSION="minTlsVersion";

}
