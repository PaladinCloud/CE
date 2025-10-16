/*******************************************************************************
 * Copyright 2023 Paladin Cloud, Inc. or its affiliates. All Rights Reserved.
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
package com.tmobile.pacman.commons.utils;

import com.google.gson.annotations.SerializedName;

public final class Constants {

    private Constants() {
        throw new IllegalStateException("Constants is a utility class");
    }

    public static final String RDS_DB_URL = "spring.datasource.url";
    public static final String RDS_USER = "spring.datasource.username";
    public static final String RDS_PWD = "spring.datasource.password";
    public static final String BASE_ACCOUNT = "base.account";
    public static final String BASE_REGION = "base.region";
    public static final String BASE_ROLE = "s3.role";
    public static final String SECRET_MANAGER_PATH = "secret.manager.path";
    public static final String API_AUTH_INFO = "apiauthinfo";
    public static final String AUTH_API_URL = "AUTH_API_URL";
    public static final String EVENT_SOURCE = "paladinCloud";
    public static final String EVENT_SOURCE_NAME = "Paladin Cloud";
    public static final String APPLICATION_JSON = "application/json";
    public static final String AUTHORIZATION = "Authorization";
    public static final String TENANT_ID = "tenant_id";

    public enum NotificationTypes {
        @SerializedName("exemption")
        EXEMPTION("exemption"),
        @SerializedName("violation")
        VIOLATION("violation"),
        @SerializedName("autofix")
        AUTOFIX("autofix"),
        @SerializedName("plugin")
        PLUGIN("plugin");

        String value;

        @Override
        public String toString() {
            return super.toString().toLowerCase();
        }

        public String getValue() {
            return this.value;
        }

        NotificationTypes(String value) {
            this.value = value;
        }
    }

    public enum Actions {
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

        public static String getPolicyMessage(Actions action) {
            switch (action) {
                case CREATE:
                    return "Policy Violation Created";
                case CLOSE:
                    return "Policy Violation Closed";
                case DELETE:
                    return "Policy Violation Deleted";
                case UPDATE:
                    return "Policy Violation Updated";
                case REVOKE:
                    return "Policy Violation Revoked";
                default:
                    return "Unknown Policy Action";
            }
        }

        public static String getViolationEvent(Actions action) {
            switch (action) {
                case CREATE:
                    return "Violation for policy - %s";
                case CLOSE:
                    return "Violation Closed for policy - %s";
                case DELETE:
                    return "Violation Deleted for policy - %s";
                case UPDATE:
                    return "Violation Updated for policy - %s";
                case REVOKE:
                    return "Violation Revoked for policy - %s";
                default:
                    return "Unknown Violation Event";
            }
        }
    }

}
