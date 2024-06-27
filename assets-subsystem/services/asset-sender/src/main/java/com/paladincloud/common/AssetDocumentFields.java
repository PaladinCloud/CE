package com.paladincloud.common;

/**
 * These are the fields in the ElasticSearch Asset documents
 */
public interface AssetDocumentFields {

    String ACCOUNT_ID = "accountid";
    String ACCOUNT_NAME = "accountname";
    String ASSET_ID_DISPLAY_NAME = "assetIdDisplayName";

    String CLOUD_TYPE = "_cloudType";

    String DATA_SOURCE = "datasource";
    String DISCOVERY_DATE = "discoverydate";
    String DOC_ID = "_docid";
    String DOC_TYPE = "docType";

    String END_TIME = "end_time";
    String ENTITY = "_entity";
    String ENTITY_TYPE = "_entitytype";

    String FIRST_DISCOVERED = "firstdiscoveredon";

    String IN_SCOPE = "inScope";

    String LATEST = "latest";
    String LOAD_DATE = "_loaddate";

    String NAME = "name";
    String NEWLY_DISCOVERED = "newly_discovered";

    String PROJECT_ID = "projectId";
    String PROJECT_NAME = "projectName";

    String RELATIONS = "_relations";
    String RESOURCE_GROUP_NAME = "resourceGroupName";
    String RESOURCE_ID = "_resourceid";
    String RESOURCE_NAME = "_resourcename";

    String START_TIME = "start_time";

    String SUBSCRIPTION = "subscription";
    String SUBSCRIPTION_NAME = "subscriptionName";

    String TAGS = "tags";
    String TAGS_APPLICATION = "tags.Application";
    String TAGS_ENVIRONMENT = "tags.Environment";
    String TAGS_PREFIX = "tags.";
    String TARGET_TYPE_DISPLAY_NAME = "targettypedisplayname";
    String TOTAL_DOCS = "total_docs";

    String U_BUSINESS_SERVICE = "u_business_service";
    String USED_FOR = "used_for";
}
