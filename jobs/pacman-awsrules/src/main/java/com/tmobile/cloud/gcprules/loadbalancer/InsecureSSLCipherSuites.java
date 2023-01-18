package com.tmobile.cloud.gcprules.loadbalancer;

import com.amazonaws.util.StringUtils;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.tmobile.cloud.awsrules.utils.PacmanUtils;
import com.tmobile.cloud.constants.PacmanRuleConstants;
import com.tmobile.cloud.gcprules.utils.GCPUtils;
import com.tmobile.pacman.commons.PacmanSdkConstants;
import com.tmobile.pacman.commons.exception.InvalidInputException;
import com.tmobile.pacman.commons.exception.RuleExecutionFailedExeption;
import com.tmobile.pacman.commons.policy.BasePolicy;
import com.tmobile.pacman.commons.policy.PacmanPolicy;
import com.tmobile.pacman.commons.policy.PolicyResult;
import com.tmobile.pacman.commons.policy.Annotation;
import com.tmobile.pacman.commons.utils.CommonUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;

import java.util.*;
@PacmanPolicy(key = "check-for-Insecure-SSL-Cipher-Suites", desc = "Google Cloud load balancer SSL policies use secure ciphers", severity = PacmanSdkConstants.SEV_MEDIUM, category = PacmanSdkConstants.SECURITY)
public class InsecureSSLCipherSuites extends BasePolicy {
    private static final Logger logger = LoggerFactory.getLogger(InsecureSSLCipherSuites.class);

    @Override
    public PolicyResult execute(Map<String, String> ruleParam, Map<String, String> resourceAttributes) {
        Annotation annotation = null;

        String resourceId = ruleParam.get(PacmanRuleConstants.RESOURCE_ID);

        String severity = ruleParam.get(PacmanRuleConstants.SEVERITY);

        String category = ruleParam.get(PacmanRuleConstants.CATEGORY);
        String vmEsURL = CommonUtils.getEnvVariableValue(PacmanSdkConstants.ES_URI_ENV_VAR_NAME);

        if (Boolean.FALSE.equals(PacmanUtils.doesAllHaveValue(severity, category, vmEsURL))) {
            logger.info(PacmanRuleConstants.MISSING_CONFIGURATION);
            throw new InvalidInputException(PacmanRuleConstants.MISSING_CONFIGURATION);
        }

        if (!StringUtils.isNullOrEmpty(vmEsURL)) {
            vmEsURL = vmEsURL + "/gcp_gcploadbalancer/_search";
        }
        logger.debug("========gcp_gcploadbalancer URL after concatenation param {}  =========", vmEsURL);
        boolean isSSLCipherSecured = false;

        MDC.put("executionId", ruleParam.get("executionId"));
        MDC.put("ruleId", ruleParam.get(PacmanSdkConstants.POLICY_ID));

        if (!StringUtils.isNullOrEmpty(resourceId)) {
            logger.debug("========after url");

            Map<String, Object> mustFilter = new HashMap<>();
            mustFilter.put(PacmanUtils.convertAttributetoKeyword(PacmanRuleConstants.RESOURCE_ID), resourceId);
            mustFilter.put(PacmanRuleConstants.LATEST, true);

            try {
                isSSLCipherSecured = checkSSLCipherSecured(vmEsURL, mustFilter);
                if (!isSSLCipherSecured) {
                    List<LinkedHashMap<String, Object>> issueList = new ArrayList<>();
                    LinkedHashMap<String, Object> issue = new LinkedHashMap<>();

                    annotation = Annotation.buildAnnotation(ruleParam, Annotation.Type.ISSUE);
                    annotation.put(PacmanSdkConstants.DESCRIPTION, "Check the Secure Socket Layer (SSL) policies associated with your HTTPS and SSL Proxy load balancers for any cipher suites that demonstrate vulnerabilities or have been considered insecure by recent exploits. ");
                    annotation.put(PacmanRuleConstants.SEVERITY, severity);
                    annotation.put(PacmanRuleConstants.CATEGORY, category);
                    issue.put(PacmanRuleConstants.VIOLATION_REASON," If Google Cloud load balancer SSL policies use insecure ciphers" );
                    issueList.add(issue);
                    annotation.put("issueDetails", issueList.toString());
                    logger.debug("========rule ended with status failure {}", annotation);
                    return new PolicyResult(PacmanSdkConstants.STATUS_FAILURE, PacmanRuleConstants.FAILURE_MESSAGE,
                            annotation);
                }

            } catch (Exception exception) {
                exception.printStackTrace();
                throw new RuleExecutionFailedExeption(exception.getMessage());
            }
        }

        logger.debug("======== ended with status true=========");
        return new PolicyResult(PacmanSdkConstants.STATUS_SUCCESS, PacmanRuleConstants.SUCCESS_MESSAGE);

    }

    private boolean checkSSLCipherSecured(String vmEsURL, Map<String, Object> mustFilter) throws Exception {
        logger.debug("========checkSSLCipherSecured  started=========");
        JsonArray hitsJsonArray = GCPUtils.getHitsArrayFromEs(vmEsURL, mustFilter);
        boolean validationResult = false;
        if (hitsJsonArray.size() > 0) {
            logger.debug("========checkSSLCipherSecured hit array=========");

            JsonObject loadBalancer = (JsonObject) ((JsonObject) hitsJsonArray.get(0))
                    .get(PacmanRuleConstants.SOURCE);

            logger.debug("Validating the data item: {}", loadBalancer);

            JsonArray sslPolicyList=loadBalancer.get("sslPolicyList").getAsJsonArray();
            for(JsonElement sslPolicy :sslPolicyList){
                String profile=sslPolicy.getAsJsonObject().get("profile").getAsString();
                String minTlsVersion=sslPolicy.getAsJsonObject().get("minTlsVersion").getAsString();
                if(profile.equalsIgnoreCase("MODERN") && minTlsVersion.equalsIgnoreCase("TLS_1_2")){
                    validationResult=true;
                }
                else if(profile.equalsIgnoreCase("RESTRICTED")){
                    validationResult=true;
                }
                else if(profile.equalsIgnoreCase("CUSTOM")){

                    validationResult=true;

                    JsonArray enabledFeatures=sslPolicy.getAsJsonObject().get("enabledFeatures").getAsJsonArray();

                    for(JsonElement enabledFeature:enabledFeatures){
                        String cipher=enabledFeature.getAsString();

                        if(cipher.equalsIgnoreCase("TLS_RSA_WITH_AES_128_GCM_SHA256") || cipher.equalsIgnoreCase("TLS_RSA_WITH_AES_256_GCM_SHA384")  || cipher.equalsIgnoreCase("TLS_RSA_WITH_AES_128_CBC_SHA") || cipher.equalsIgnoreCase("TLS_RSA_WITH_AES_256_CBC_SHA") || cipher.equalsIgnoreCase("TLS_RSA_WITH_3DES_EDE_CBC_SHA") ){
                            validationResult=false;
                            break;
                        }
                    }
                }
            }
        }

        return validationResult;
    }

    @Override
    public String getHelpText() {
        return "Google Cloud load balancer SSL policies use secure ciphers";
    }
}
