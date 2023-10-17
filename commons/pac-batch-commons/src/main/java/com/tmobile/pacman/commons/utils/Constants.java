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

    String CONFIG_CREDS = "config_creds";

    /** The API  User:Password  */
    String API_AUTH_INFO = "apiauthinfo";
    String AUTH_API_URL = "AUTH_API_URL";
    String CONFIG_URL = "CONFIG_URL";

     String API_READ_SCOPE = "API_OPERATION/READ";

    String FAILED = "failed";
    String ERROR = "error";
    String EXCEPTION = "exception";
    String ERROR_TYPE = "type";
    String WARN = "warn";
    String FATAL = "fatal";

    String SOURCE = "source";
    String NAME = "name";

    String CONFIG_QUERY = "configquery";

    enum NotificationTypes {
        @SerializedName("exemption")
        EXEMPTION("exemption"),
        @SerializedName("violation")
        VIOLATION("violation"),
        @SerializedName("autofix")
        AUTOFIX("autofix"),
        @SerializedName("permission")
        PERMISSION("permission");

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

    String APPLICATION_JSON = "application/json";
    String AUTHORIZATION = "Authorization";
}
