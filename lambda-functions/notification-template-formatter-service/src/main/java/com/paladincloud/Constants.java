package com.paladincloud;

public class Constants {

    public static final String mailTopicArn = "notification.email.topic.arn";
    public static final String slackTopicArn = "notification.slack.topic.arn";
    public static final String jiraTopicArn = "notification.jira.topic.arn";
    public static final String toMailId = "notification.to.emailid";
    public static final String apiauthinfo = "apiauthinfo";

    public static final String violationNotificationSubject = "Policy Violation Notification";
    public static final String fromEmail = "no-reply@paladincloud.io";
    public static final String assetGroupNotificationSubject = "Asset Group Notification";

    public static final String exemptionNotificationSubject = "Exemption Notification";

    public enum AutoFixAction {
        AUTOFIX_ACTION_EMAIL,
        /** The autofix action fix. */
        AUTOFIX_ACTION_FIX,
        /** The autofix action exempted. */
        AUTOFIX_ACTION_EXEMPTED,

    }

}
