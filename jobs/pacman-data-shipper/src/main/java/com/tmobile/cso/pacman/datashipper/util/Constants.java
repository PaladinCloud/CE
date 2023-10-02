package com.tmobile.cso.pacman.datashipper.util;


/**
 * The Interface Constants.
 */
public interface Constants {

    /**
     * The rds url.
     */
    String RDS_DB_URL = "spring.datasource.url";
    /**
     * The rds user.
     */
    String RDS_USER = "spring.datasource.username";
    /**
     * The rds pwd.
     */
    String RDS_PWD = "spring.datasource.password";

    /**
     * The target type info.
     */
    String TARGET_TYPE_INFO = "targetTypes";
    String TARGET_TYPE_OUTSCOPE = "typesNotInScope";

    String CONFIG_CREDS = "config_creds";

    /**
     * The API  User:Password
     */
    String API_AUTH_INFO = "apiauthinfo";
    String CONFIG_QUERY = "configquery";
    String CONFIG_URL = "CONFIG_URL";

    String FAILED = "failed";
    String ERROR = "error";
    String EXCEPTION = "exception";
    String ERROR_TYPE = "type";
    String WARN = "warn";
    String FATAL = "fatal";

    String SOURCE = "source";
    String NAME = "name";
    String DOC_TYPE = "docType";

    String ADMIN_MAIL_ID = "admin@paladincloud.io";

    String DEFAULT_POLICY_FREQUENCY = "0 0 1/1 * ? *";
    String EXTERNAL_POLICY = "External";
}
