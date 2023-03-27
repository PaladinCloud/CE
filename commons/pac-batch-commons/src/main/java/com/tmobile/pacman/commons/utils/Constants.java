package com.tmobile.pacman.commons.utils;

public interface Constants {

    String RDS_DB_URL = "spring.datasource.url";

    /** The rds user. */
    String RDS_USER = "spring.datasource.username";

    /** The rds pwd. */
    String RDS_PWD = "spring.datasource.password";


    /** The target type info. */
    String TARGET_TYPE_INFO = "targetTypes";

    String CONFIG_CREDS = "config_creds";


    /** The target type info. */
    String TARGET_TYPE_OUTSCOPE = "typesNotInScope";

    /** The API  User:Password  */
    String API_AUTH_INFO = "apiauthinfo";

    String CONFIG_QUERY = "configquery";

    String CONFIG_URL = "CONFIG_URL";


    /** The failed. */
    String FAILED = "failed";

    /** The error. */
    String ERROR = "error";

    /** The exception. */
    String EXCEPTION = "exception";

    /** The error type. */
    String ERROR_TYPE = "type";

    /** The warn. */
    String WARN = "warn";

    /** The fatal. */
    String FATAL = "fatal";

    String SOURCE = "source";

    String NAME = "name";

    enum NotificationTypes {
        EXEMPTIONS,
        VIOLATIONS,
        AUTOFIX
    }

    enum Actions {
        CREATE,
        REVOKE,
        DELETE,
        UPDATE,
        CLOSE
    }
    String EVENT_SOURCE = "paladinCloud";
    String EVENT_SOURCE_NAME = "PaladinCloud";


}
