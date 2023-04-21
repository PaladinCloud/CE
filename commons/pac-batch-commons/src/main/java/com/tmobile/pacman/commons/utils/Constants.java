package com.tmobile.pacman.commons.utils;

import com.google.gson.annotations.SerializedName;

public interface Constants {

    String RDS_DB_URL = "spring.datasource.url";

    /** The rds user. */
    String RDS_USER = "spring.datasource.username";

    /** The rds pwd. */
    String RDS_PWD = "spring.datasource.password";
    String BASE_ACCOUNT = "base.account";
    String BASE_REGION = "base.region";
    String BASE_ROLE = "s3.role";
    String SECRET_MANAGER_PATH = "secret.manager.path";


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
        @SerializedName("exemptions")
        EXEMPTIONS("Exemptions"),
        @SerializedName("violations")
        VIOLATIONS("Violations"),
        @SerializedName("autofix")
        AUTOFIX("Autofix");

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
        @SerializedName("revoke")
        REVOKE,
        @SerializedName("delete")
        DELETE,
        @SerializedName("update")
        UPDATE,
        @SerializedName("close")
        CLOSE;
        @Override
        public String toString() {
            return super.toString().toLowerCase();
        }
    }
    String EVENT_SOURCE = "paladinCloud";
    String EVENT_SOURCE_NAME = "Paladin Cloud";

}
