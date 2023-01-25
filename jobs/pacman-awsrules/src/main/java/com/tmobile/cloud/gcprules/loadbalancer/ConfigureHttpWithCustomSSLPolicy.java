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
import com.tmobile.pacman.commons.policy.Annotation;
import com.tmobile.pacman.commons.policy.BasePolicy;
import com.tmobile.pacman.commons.policy.PacmanPolicy;
import com.tmobile.pacman.commons.policy.PolicyResult;
import com.tmobile.pacman.commons.utils.CommonUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;

import java.util.*;

@PacmanPolicy(key = "configure-custom-ssl-for-https", desc = "Configure custom ssl policy instead of default for https", severity = PacmanSdkConstants.SEV_HIGH, category = PacmanSdkConstants.SECURITY)
public class ConfigureHttpWithCustomSSLPolicy extends BasePolicy {
    private static final Logger logger = LoggerFactory.getLogger(ConfigureHttpWithCustomSSLPolicy.class);

    @Override
    public PolicyResult execute(Map<String, String> ruleParam, Map<String, String> resourceAttributes) {

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

        MDC.put(PacmanSdkConstants.EXECUTION_ID, ruleParam.get("executionId"));
        MDC.put(PacmanSdkConstants.POLICY_ID, ruleParam.get(PacmanSdkConstants.POLICY_ID));

        if (!StringUtils.isNullOrEmpty(resourceId)) {
            logger.debug("========after url");

            Map<String, Object> mustFilter = new HashMap<>();
            mustFilter.put(PacmanUtils.convertAttributetoKeyword(PacmanRuleConstants.RESOURCE_ID), resourceId);
            mustFilter.put(PacmanRuleConstants.LATEST, true);

            try {
                boolean hasCustomSSLPolicy = checkConfigureHttpWithCustomSSLPolicy(vmEsURL, mustFilter);
                if (!hasCustomSSLPolicy) {
                    List<LinkedHashMap<String, Object>> issueList = new ArrayList<>();
                    LinkedHashMap<String, Object> issue = new LinkedHashMap<>();

                    Annotation annotation = Annotation.buildAnnotation(ruleParam, Annotation.Type.ISSUE);
                    annotation.put(PacmanSdkConstants.DESCRIPTION, "Google Cloud Platform (GCP) load balancers https target proxy should be configured with custom ssl policy instead of default");
                    annotation.put(PacmanRuleConstants.SEVERITY, severity);
                    annotation.put(PacmanRuleConstants.CATEGORY, category);
                    issue.put(PacmanRuleConstants.VIOLATION_REASON,"HTTPS for Google Cloud Load Balancers wasnot configured with custom SSL Policy");
                    issueList.add(issue);
                    annotation.put(PacmanRuleConstants.ISSUE_DETAILS, issueList.toString());
                    logger.debug("========rule ended with status failure {}", annotation);
                    return new PolicyResult(PacmanSdkConstants.STATUS_FAILURE, PacmanRuleConstants.FAILURE_MESSAGE,
                            annotation);
                }

            } catch (Exception exception) {
                throw new RuleExecutionFailedExeption(exception.getMessage());
            }
        }

        logger.debug("======== ended with status true=========");
        return new PolicyResult(PacmanSdkConstants.STATUS_SUCCESS, PacmanRuleConstants.SUCCESS_MESSAGE);

    }

    private boolean checkConfigureHttpWithCustomSSLPolicy(String vmEsURL, Map<String, Object> mustFilter) throws Exception {
        logger.debug("========checkConfigureHttpWithCustomSSLPolicy  started=========");
        JsonArray hitsJsonArray = GCPUtils.getHitsArrayFromEs(vmEsURL, mustFilter);
        boolean hasCustomSSLPolicy = true;
        try{
            if (hitsJsonArray.size() > 0) {
                logger.debug("========checkConfigureHttpWithCustomSSLPolicy hit array=========");
                JsonObject loadBalancer = (JsonObject) ((JsonObject) hitsJsonArray.get(0))
                        .get(PacmanRuleConstants.SOURCE);

                logger.debug("Validating the data item: {}", loadBalancer);
                JsonArray httpProxyDetailList=loadBalancer.get(PacmanRuleConstants.HTTP_PROXY_DETAIL_LIST).getAsJsonArray();
                for(JsonElement js : httpProxyDetailList){
                    JsonObject ob = js.getAsJsonObject();
                    if(!ob.get(PacmanRuleConstants.HAS_CUSTOM_POLICY).getAsBoolean()){
                        hasCustomSSLPolicy = false;
                        break;
                    }
                }
            }
        }catch (Exception e){
            logger.error("rror occurred in checkConfigureHttpWithCustomSSLPolicy: {}", hitsJsonArray, e);
            hasCustomSSLPolicy = false;
        }
        return hasCustomSSLPolicy;
    }

    @Override
    public String getHelpText() {
        return "Google Cloud Platform (GCP) load balancers https target proxy should be configured with custom ssl policy instead of default";
    }
}
