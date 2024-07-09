package com.paladincloud.common;

/**
 * These are the fields in the ElasticSearch Asset documents
 */
public interface AssetDocumentFields {

    String NAME = "name";
    String ASSET_ID_DISPLAY_NAME = "assetIdDisplayName";
    String TARGET_TYPE_DISPLAY_NAME = "targettypedisplayname";

    String DATA_SOURCE = "datasource";
    String DOC_TYPE = "docType";

    String DISCOVERY_DATE = "discoverydate";
    String FIRST_DISCOVERED = "firstdiscoveredon";
    String LOAD_DATE = "_loaddate";
    String START_TIME = "start_time";
    String END_TIME = "end_time";

    String ACCOUNT_ID = "accountid";
    String ACCOUNT_NAME = "accountname";
    String PROJECT_ID = "projectId";
    String PROJECT_NAME = "projectName";
    String SUBSCRIPTION = "subscription";
    String SUBSCRIPTION_NAME = "subscriptionName";

    String CLOUD_TYPE = "_cloudType";
    String DOC_ID = "_docid";
    String ENTITY = "_entity";
    String ENTITY_TYPE = "_entitytype";
    String IN_SCOPE = "inScope";
    String RELATIONS = "_relations";

    String LATEST = "latest";
    String NEWLY_DISCOVERED = "newly_discovered";
    String TOTAL_DOCS = "total_docs";

    String RESOURCE_GROUP_NAME = "resourceGroupName";
    String RESOURCE_ID = "_resourceid";
    String RESOURCE_NAME = "_resourcename";

    String U_BUSINESS_SERVICE = "u_business_service";
    String UPLOADED_DOC_COUNT = "uploaded_docs";
    String USED_FOR = "used_for";

    String TAGS = "tags";
    interface Tags {
        String APPLICATION = "tags.Application";
        String ENVIRONMENT = "tags.Environment";
    }

    static String asKeyword(String field) {
        return STR."\{field}.keyword";
    }

    static String asTag(String field) {
        return STR."\{TAGS}.\{field}";
    }

}
