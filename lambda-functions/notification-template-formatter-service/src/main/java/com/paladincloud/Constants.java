package com.paladincloud;

public class Constants {
    public static final String mailTopicArn = "notification.email.topic.arn";
    public static final String apiauthinfo = "apiauthinfo";
    public static final String fromEmail = "no-reply@paladincloud.io";
    public enum AutoFixAction {
        AUTOFIX_ACTION_EMAIL,
        /** The autofix action fix. */
        AUTOFIX_ACTION_FIX,
        /** The autofix action exempted. */
        AUTOFIX_ACTION_EXEMPTED,

    }

}
