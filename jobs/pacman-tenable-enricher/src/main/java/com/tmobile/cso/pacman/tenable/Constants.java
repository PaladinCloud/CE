package com.tmobile.cso.pacman.tenable;

/**
 * The Interface Constants.
 */
public class Constants {

    // TODO: Set to true to get detailed debug logs
    public static final boolean IS_DEBUG_MODE = true;

    public static final String FAILED = "failed";
    public static final String ERROR = "error";
    public static final String EXCEPTION = "exception";
    public static final String ERROR_TYPE = "type";
    public static final String WARN = "warn";
    public static final String FATAL = "fatal";
    public static final String SOURCE = "source";
    public static final String NAME = "name";

    public static final String CONFIG_CREDS = "config_creds";
    public static final String X_API_KEYS_HEADER_NAME = "X-ApiKeys";
    public static final String USER_AGENT_HEADER_NAME = "User-Agent";

    private Constants() {
        throw new IllegalStateException("Constants is a utility class");
    }
}
