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

@PacmanPolicy(key = "enable-quic-protocol-for-loadbalancer", desc = "Enable quic protocol for load balancer",
        severity = PacmanSdkConstants.SEV_HIGH, category = PacmanSdkConstants.SECURITY)
public class EnableQuicProtocolRule extends BasePolicy {

    private static final Logger logger = LoggerFactory.getLogger(EnableQuicProtocolRule.class);

    @Override
    public PolicyResult execute(Map<String, String> ruleParam, Map<String, String> resourceAttributes) {
        Annotation annotation;

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
        boolean isQuicEnabled;

        MDC.put(PacmanSdkConstants.EXECUTION_ID, ruleParam.get("executionId"));
        MDC.put(PacmanSdkConstants.POLICY_ID, ruleParam.get(PacmanSdkConstants.POLICY_ID));

        if (!StringUtils.isNullOrEmpty(resourceId)) {
            logger.debug("========after url");

            Map<String, Object> mustFilter = new HashMap<>();
            mustFilter.put(PacmanUtils.convertAttributetoKeyword(PacmanRuleConstants.RESOURCE_ID), resourceId);
            mustFilter.put(PacmanRuleConstants.LATEST, true);

            try {
                isQuicEnabled = checkQuicEnabled(vmEsURL, mustFilter);
                if (!isQuicEnabled) {
                    List<LinkedHashMap<String, Object>> issueList = new ArrayList<>();
                    LinkedHashMap<String, Object> issue = new LinkedHashMap<>();

                    annotation = Annotation.buildAnnotation(ruleParam, Annotation.Type.ISSUE);
                    annotation.put(PacmanSdkConstants.DESCRIPTION, "This policy identifies Load Balancer HTTPS target proxies which are not configured with QUIC protocol. Enabling QUIC protocol in load balancer target https proxies adds advantage by establishing connections faster, stream-based multiplexing, improved loss recovery, and eliminates head-of-line blocking.");
                    annotation.put(PacmanRuleConstants.SEVERITY, severity);
                    annotation.put(PacmanRuleConstants.CATEGORY, category);
                    issue.put(PacmanRuleConstants.VIOLATION_REASON," Google cloud service load Balancer HTTPS target proxies are not configured with QUIC protocol" );
                    issueList.add(issue);
                    annotation.put(PacmanRuleConstants.ISSUE_DETAILS, issueList.toString());
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

    private boolean checkQuicEnabled(String vmEsURL, Map<String, Object> mustFilter) throws Exception {
        logger.debug("========checkQuicNegotiationEnabledForHttpsTargetProxies  started=========");
        boolean validationResult = true;
        JsonArray hitsJsonArray = GCPUtils.getHitsArrayFromEs(vmEsURL, mustFilter);
        try{
            if (hitsJsonArray.size() > 0) {
                logger.debug("========checkQuicNegotiationEnabledForHttpsTargetProxies hit array=========");
                JsonObject loadBalancer = (JsonObject) ((JsonObject) hitsJsonArray.get(0))
                        .get(PacmanRuleConstants.SOURCE);

                logger.debug("Validating the data item: {}", loadBalancer);
                JsonArray quicNegotiation = loadBalancer.get(PacmanRuleConstants.QUIC_NEGOTIATION).getAsJsonArray();
                for(JsonElement quic : quicNegotiation){
                    if(!quic.getAsBoolean()){
                        validationResult = false;
                        break;
                    }
                }
            }
        }catch (Exception e){
            logger.error("rror occurred in checkQuicNegotiationEnabledForHttpsTargetProxies: {}", hitsJsonArray, e);
            validationResult = false;
        }
        return validationResult;
    }

    @Override
    public String getHelpText() {
        return "Enable Quic negotiation protocol for Google Cloud Load Balancers";
    }
}
