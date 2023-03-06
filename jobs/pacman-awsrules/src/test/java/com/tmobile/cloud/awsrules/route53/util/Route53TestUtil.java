package com.tmobile.cloud.awsrules.route53.util;

import com.tmobile.cloud.constants.PacmanRuleConstants;
import com.tmobile.pacman.commons.PacmanSdkConstants;

import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;

public class Route53TestUtil {

    public static final DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ssZ");

    public static final String HOSTED_ZONE_URL = "/aws/route53_hostedzone/_search";
    public static final String RESOURCE_RECORD_URL = "/aws/route53_resourcerecord/_search";

    public static Map<String, String> getInputParamMap() {
        Map<String, String> ruleParam = new HashMap<>();
        ruleParam.put(PacmanSdkConstants.EXECUTION_ID, "exectionid");
        ruleParam.put(PacmanSdkConstants.POLICY_ID,
                "test_version-1_Route53_test");
        ruleParam.put(PacmanRuleConstants.CATEGORY, PacmanSdkConstants.SECURITY);
        ruleParam.put(PacmanRuleConstants.SEVERITY, PacmanSdkConstants.SEV_HIGH);
        ruleParam.put(PacmanRuleConstants.ACCOUNTID, "123456789");
        ruleParam.put(PacmanRuleConstants.REGIONS, "test");
        return ruleParam;
    }

    public static Map<String, String> getValidResourceData() {
        Map<String, String> resObj = new HashMap<>();
        resObj.put("_resourceid", "test");
        resObj.put(PacmanRuleConstants.ACCOUNTID, "123456789");
        resObj.put(PacmanRuleConstants.DOMAIN_NAME, "paladin.io");
        return resObj;
    }
}
