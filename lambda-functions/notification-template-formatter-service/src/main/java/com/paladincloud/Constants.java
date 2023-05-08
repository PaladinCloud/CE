package com.paladincloud;

public class Constants {
    public static final String mailTopicArn = "notification.email.topic.arn";
    public static final String apiauthinfo = "apiauthinfo";
    public static final String fromEmail = "notify@paladincloud.io";
    public static final String CREATE = "create";
    public static final String REVOKE = "revoke";
    public static final String CREATE_EXEMPTION_REQUEST = "create_exemption_request";
    public static final String REVOKE_EXEMPTION_REQUEST = "revoke_exemption_request";
    public static final String CANCEL_EXEMPTION_REQUEST = "cancel_exemption_request";
    public static final String APPROVE_EXEMPTION_REQUEST = "approve_exemption_request";
    public enum AutoFixAction {
        AUTOFIX_ACTION_EMAIL,
        /** The autofix action fix. */
        AUTOFIX_ACTION_FIX,
        /** The autofix action exempted. */
        AUTOFIX_ACTION_EXEMPTED,

    }

}
