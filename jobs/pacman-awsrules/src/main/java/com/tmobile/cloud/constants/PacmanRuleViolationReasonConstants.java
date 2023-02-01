package com.tmobile.cloud.constants;

public class PacmanRuleViolationReasonConstants {
    private PacmanRuleViolationReasonConstants(){}

    public static final String ENABLE_AUTO_RESTART = "VM instance does not have automatic restart";
    public static final String AUTO_DELETE_PERSISTENT_DISK = "VM instance does not have automatic restart";
    public static final String  INGRESS_SETTING = "GCP Cloud Functions are configured with overly permissive Ingress setting";
    public static final String  VPC_CONNECTOR = "GCP Cloud Functions are not configured with a VPC connector";
    public static final String  HTTP_TRIGGERS = "GCP Cloud Functions HTTP trigger is not secured";
}
