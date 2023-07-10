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
package com.tmobile.pacman.api.admin.common;

/**
 * Admin Constants
 */
public final class AdminConstants {
	
	private AdminConstants() {
	}
	public static final String CLOUD_TYPE = "CloudType";
	public static final String ACTIONS = "actions";
	public static final String TARGET_TYPE = "TargetType";
	public static final String INDEX					= 	"index";
	public static final String COUNT = "count";
	public static final String SERVERLESS_RULE_TYPE					= 	"Serverless";
	public static final String MANAGED_POLICY_TYPE					= 	"ManagePolicy";
	public static final String JAR_EXTENSION						= 	".jar";
	public static final String JAR_FILE_MISSING						=	"Jar file is missing";
	public static final String RESOURCE_ACCESS_DENIED				=	"You don't have sufficient privileges to access this resource";
	public static final String FAILED								=	"failed";
	public static final String ENABLED_CAPS							=	"ENABLED";
	public static final String RULE									=	"rule";
	public static final String JOB									=	"job";
	public static final String ENABLE								=	"enable";
	
	public static final String DATE_FORMAT 							= 	"MM/dd/yyyy HH:mm";
	
	public static final String POLICY_CREATION_SUCCESS				=	"Policy has been successfully created";
	public static final String POLICY_DISABLE_ENABLE_SUCCESS		=	"Policy has been successfully %s !!";
	public static final String POLICY_ID_EXITS 						= 	"Policy id %s already exists!!";
	public static final String POLICY_ID_NOT_EXITS 					= 	"Policy id %s does not exists!!";
	public static final String UNABLE_TO_UPDATE_POLICY_PARAMS = " Unable to update policy params for Policy id %s - ";
	public static final String STATUS_CONFIGURED = "configured";
	public static final String JOB_DISABLE_ENABLE_SUCCESS           =   "Job has been successfully %s !!";

	public static final String JOB_CREATION_SUCCESS					=	"Job has been successfully created";
	public static final String JOB_UPDATION_SUCCESS					=	"Job has been successfully updated";
	public static final String JOB_ID_ALREADY_EXITS					=	"Job %s already exists!!";
	public static final String JOB_ID_NOT_EXITS						=	"Job %s does not exists!!";
	public static final String INVALID_JOB_FREQUENCY				=	"Invalid Job Frequency or Cron Expression!";
	public static final String UNEXPECTED_ERROR_OCCURRED 			= 	"Unexpected error occurred!!";
	public static final String LAMBDA_LINKING_EXCEPTION             =   "Failed in linking the lambda function to the rule";
	public static final String CLOUDWATCH_RULE_DELETION_FAILURE     =   "Failed in deleting the cloudwatch rule while disabling the rule";
	public static final String CLOUDWATCH_RULE_DISABLE_FAILURE     	=   "Failed in disabling the cloudwatch rule";
	public static final String CLOUDWATCH_RULE_ENABLE_FAILURE     	=   "Failed in enabling the cloudwatch rule";

	public static final String DOMAIN_CREATION_SUCCESS				=	"Domain has been successfully created";
	public static final String DOMAIN_NAME_EXITS					=	"Domain name already exits!!!";
	public static final String DOMAIN_UPDATION_SUCCESS 				=	"Domain has been successfully updated";
	
	public static final String TARGET_TYPE_NAME_NOT_EXITS			=	"Target Type name does not exits!!!";
	public static final String TARGET_TYPE_NAME_EXITS				=	"Target Type name already exits!!!";
	public static final String TARGET_TYPE_CREATION_SUCCESS			=	"Target Type has been successfully created";
	public static final String TARGET_TYPE_UPDATION_SUCCESS 		=	"Target Type has been successfully updated";
	public static final String TARGET_TYPE_CREATION_FAILURE 		=	"Failed in creating Target Type";
	public static final String TARGET_TYPE_UPDATION_FAILURE 		=	"Failed in updating Target Type";
	public static final String TARGET_TYPE_INDEX_EXITS				=	"Target Type index already exits!!!";
	
	public static final String ASSET_GROUP_CREATION_SUCCESS			=	"Asset Group has been successfully created";

	public static final String ASSET_GROUP_ALREADY_EXISTS		=	"Asset Group already exits with the provided asset group name";
	public static final String ASSET_GROUP_UPDATION_SUCCESS 		=	"Asset Group has been successfully updated";
	public static final String ASSET_GROUP_DELETE_SUCCESS 			=	"Asset Group has been successfully deleted";
	public static final String ASSET_GROUP_DELETE_FAILED 			=	"Failed in deleting the Asset Group";
	public static final String ASSET_GROUP_NOT_EXITS				=	"Asset Group does not exits!!!";
	public static final String ASSET_GROUP_ALIAS_DELETION_FAILED	= 	"Failed in deleting the Asset Group Alias";
	public static final String ASSET_GROUP_ENABLED					=	"Asset Group ENABLED!!!";
	public static final String ASSET_GROUP_DISABLED					=	"Asset Group DISABLED!!!";

	public static final String EXCEPTION_DELETEION_SUCCESS 			= 	"Asset Group Exception has been successfully deleted";
	public static final String EXCEPTION_DELETEION_FAILURE 			= 	"Failed in deleting Asset Group Exception";
	public static final String CONFIG_STICKY_EXCEPTION_SUCCESS 		= 	"Successfully Configured Sticky Exceptions";
	public static final String CONFIG_STICKY_EXCEPTION_FAILED 		= 	"Failed in ConfigurING Sticky Exceptions";
	
	public static final String USER_ROLE_CREATION_SUCCESS			=	"User Role has been successfully created";
	public static final String USER_ROLE_UPDATION_SUCCESS 			=	"User Role has been successfully updated";
	public static final String USER_ROLE_NOT_EXITS					=	"User Role does not exits!!!";
	public static final String USER_ROLE_ALREADY_EXITS				=	"User Role already exits!!!";
	public static final String USER_ROLE_ALLOCATION_FAILURE 		=	"Failed in user role allocation";
	public static final String USER_ROLE_ALLOCATION_SUCCESS 		=	"User Roles has been successfully allocated";
	
	public static final String QUERY								= 	"query";
	
	public static final String PLUGIN_DETAILS                       =   "pluginDetails";
	
	public static final String ACCOUNT_CREATION_SUCCESS             =   "Account has been successfully created";
	public static final String ACCOUNT_ID_EXITS                     =   "Account Id already exits!!!";
	public static final String ACCOUNT_ID_NOT_EXITS                 =   "Account Id does not exits!!!";
	public static final String ACCOUNT_UPDATION_SUCCESS             =   "Account has been successfully updated";
	public static final String ACCOUNT_DELETION_SUCCESS             =   "Account has been successfully deleted";
	public static final String ACCOUNT_DELETE_FAILED                =   "Failed in deleting the Account";
	public static final Integer TEMPORARY_CREDS_VALID_SECONDS 		=   3600;
	public static final String DEFAULT_SESSION_NAME 				=   "PAC_GET_ADMIN_DATA_SESSION";
	
	public static final String JOBID_OR_POLICYID_NOT_EMPTY            =   "Both Job Id or Policy Id cannot be blank";
    public static final String DELETE_RULE_TARGET_FAILED            =   "Failed in deleting the lambda target from rule";
    
	public static final String ES_EXCEPTION_INDEX					= 	"/exceptions";
	public static final String INIT_ES_CREATE_INDEX					= 	"INIT_ES_CREATE_INDEX";
	
	public static final String ERROR_CONFIG_MANDATORY				=	"Config key, Config value and application are mandatory";
    public static final String LATEST								=	"latest";
    public static final String CONFIG_ROLLBACK_MSG					=	"Rollback to an older timestamp through API invocation";
    public static final String POLICY_UUID							= 	"policyUUID";
    public static final String INVOCATION_ID						= 	"invocationId";
	public static final String AUTOFIX_ENABLE_SUCCESS				=	"AutoFix has been successfully Enabled !!";
	public static final String AUTOFIX_DISABLE_SUCCESS				=	"AutoFix has been successfully Disabled !!";
    public static final String AUTO_FIX_KEY							=   "fixKey";
    public static final String AUTO_FIX_KEYWORD						= 	"autofix";
	public static final String CREATE_EXCEPTION_EVENT_NAME 			=	"Sticky exception %s is created";
	public static final String UPDATE_EXCEPTION_EVENT_NAME 			=	"Sticky exception %s is updated";

	public static final String DELETE_EXCEPTION_EVENT_NAME  			=	"Sticky exception %s is deleted";

	public static final String CREATE_STICKY_EXCEPTION_SUBJECT  		= "Sticky Exception Created";
	public static final String DELETE_STICKY_EXCEPTION_SUBJECT  		= "Sticky Exception Deleted";

	public static final String UPDATE_STICKY_EXCEPTION_SUBJECT  		= "Sticky Exception Updated";
	public static final String DS_ALL_SEARCH = "/ds-all/_search";
	public static final String FILTER_PATH = "?filter_path=";
	public static final String ES_PARAM_VALUE = "aggregations.%s.buckets.key";
	public static final String TAGS_QUERY = "{\"size\":0,\"query\":{\"bool\":{\"must\":{\"term\":" +
			"{\"latest\":true}}}},\"aggs\":{%s}}";
	public static final String AGG_QUERY = "\"%s\":{\"terms\":{\"field\":\"tags.%s.keyword\",\"size\": 10000}}";
	public static final String DELIMITER = ",";
	public static final String DELIMITER_MINUS = "-";
	public static final String DELIMITER_AT = "@";
	public static final String EMAIL_REGEX_PATTERN = "^[a-zA-Z0-9_!#$%&'*+/=?`{|}~^.-]+@[a-zA-Z0-9.-]+$";
	public static final String DELIMITER_DOT_REGEX = "\\.";
	public static final String DELIMITER_COLON = ": ";
	public static final String DELIMITER_SPACE = " ";
	public static final String DELIMITER_FORWARD_SLASH = "/";
	public static final String DELIMITER_UNDERSCORE = "_";
	public static final String DELIMITER_QUESTION_MARK = "?";
	public static final String ES_INDEX_WILD_CARD = "_*";
	public static final String DELIMITER_URL_SPACE = "%20";
	public static final String STAKEHOLDER_START_TAG = "sh";
	public static final String STAKEHOLDER_GROUP_TYPE = "user";
	public static final String AWS_DATA_SOURCE = "aws";
	public static final String DESCRIPTION = "User Asset Group for - %s";
	public static final String INVALID_USER_ATTRIBUTES = "Invalid user attributes ";
	public static final String INVALID_REQUEST = "Invalid request - ";
	public static final String DELETE_ASSET_GROUP_ERR_MSG = "Unable to delete/update stakeholder asset groups";
	public static final String DELETE_ALIAS_ERR_MSG = " - Unable to delete alias - response {}";
	public static final String DELETE_ALIAS_FOR_STAKEHOLDER_ERR_MSG = " - Unable to delete alias for stakeholder";
	public static final String DELETE_AG_OWNER_ERR_MSG = "Unable to delete stakeholder asset group owner details";
	public static final String ASSET_GROUP_NOT_FOUND = "Asset group doesn't exists";
	public static final String STAKEHOLDER_ASSET_GROUP_DELETED = "Stakeholder Asset group Successfully deleted";
	public static final String CREATE_ALIAS_ERR_MSG = "Unable to POST stakeholder alias query";
	public static final String CONFIG_VALUE_NULL_ERR_MSG = "Config value is null";
	public static final String CONFIG_VALUE_EMPTY_ERR_MSG = "Config value is empty -configValue: ";
	public static final String DATASOURCE_EMPTY_ERR_MSG = " DatasourceList is empty";
	public static final String ALIAS_NOT_FOUND_ERR_MSG = " Alias not found in elasticsearch - alias name - %s";
	public static final String INDEX_NOT_AVAILABLE_ERR_MSG = " Please add necessary index before creating asset group";
	public static final String TAGS_EMPTY_ERR_MSG = " tags are empty";
	public static final String NODE_AGGREGATIONS = "aggregations";
	public static final String NODE_BUCKETS = "buckets";
	public static final String NODE_KEY = "key";
	public static final String ES_BOOL = "bool";
	public static final String ES_MUST = "must";
	public static final String ES_MATCH = "match";
	public static final String ES_SHOULD = "should";
	public static final String ES_MINIMUM_SHOULD = "minimum_should_match";
	public static final String ES_ATTRIBUTE_TAG = "tags.";
	public static final String ES_ATTRIBUTE_KEYWORD = ".keyword";
	public static final String ES_ACTIONS = "actions";
	public static final String ES_ADD = "add";
	public static final String ES_FILTER = "filter";
	public static final String ES_ALIAS = "alias";
	public static final String ES_INDEX = "index";
	public static final String ES_ALIASES_PATH = "/_aliases";
	public static final String ES_ALIAS_PATH = "/_alias/";
	public static final String ALLOW_NO_INDICES_PARAM = "allow_no_indices=false";
	public static final String DISABLED_CAPS						=	"DISABLED";
	public static final String STATUS_OPEN							=	"open";
	public static final String STATUS_CLOSE							=	"close";
	public static final String POLICY_EXEMPTION_ID_NOT_EXITS 		= 	"EXCEMPTION does not exists for Policy uuid %s !!";
	public static final String POLICY_ENABLE_SUCCESS				=	"Policy id %s has been successfully enabled !!";
	public static final String EXPIRE_DATE_CAN_NOT_BE_NULL 			= 	"Expire date can't be null !!";
	public static final String DEFAULT_DATE_FORMAT					= 	"dd/MM/yyyy";
	public static final String EXPIRE_DATE_FORMAT_EXCEPTION			= 	"ExpireDate format expection";
	public static final String MISSING_PARAMETERS 					= 	"Missing parameters  !!";
	public static final String POLICY_DISABLE_DESCRIPTION			=	"The Policy has been disabled by %s and it will be enabled on %s";



	public static final String ACCOUNT_ID			=	"accountId";
	public static final String ACCOUNT_NAME			=	"accountName";
	public static final String CREATED_BY			=	"createdBy";
	public static final String PLATFORM			=	"platform";
	public static final String ASSET			=	"asset";
	public static final String VIOLATIONS			=	"violations";
	public static final String STATUS			=	"status";
	public static final String ASC			=	"ASC";
	public static final String SORT_ELEMENT = "sortElement";
	public static final String SORT_ORDER = "sortOrder";

	public static final String POLICY_ENABLE_DESCRIPTION			=	"The Policy has been enabled by %s on %s";
	public static final String POLICY_ACTION_EVENT_NAME  			=	"Policy id %s is %s";
	public static final String POLICY_ACTION_SUBJECT 				= 	"Policy status";
}

