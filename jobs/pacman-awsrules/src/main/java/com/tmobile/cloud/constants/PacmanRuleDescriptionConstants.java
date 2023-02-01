package com.tmobile.cloud.constants;

public class PacmanRuleDescriptionConstants {

    private PacmanRuleDescriptionConstants(){}

    public static final String ENABLE_AUTO_RESTART = "VM instance does not have automatic restart";
    public static final String AUTO_DELETE_PERSISTENT_DISK = "VM instance does not have automatic restart";
    public static final String INGRESS_SETTING = "Google Cloud Platform (GCP) cloud functions should not be configured with overly permissive ingress setting";
    public static final String VPC_CONNECTOR = "It is recommended to configure the GCP Cloud Function with a VPC connector. VPC connector helps function to connect to a resource inside a VPC in the same project. Setting up the VPC connector allows you to set up a secure perimeter to guard against data exfiltration and prevent functions from accidentally sending any data to unwanted destinations";
    public static final String  HTTP_TRIGGERS = "Google Cloud Platform (GCP) cloud HTTP functions to be triggered only with HTTPS, user requests will be redirected to use the HTTPS protocol, which is more secure. It is recommended to set the 'Require HTTPS' for configuring HTTP triggers while deploying your function.";
}
