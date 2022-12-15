package com.tmobile.cloud.gcprules.vminstance;

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

@PacmanPolicy(key = "ip-forward-rule", desc = "disable ip forwarding for GCE instance and enable for GKE instance", severity = PacmanSdkConstants.SEV_MEDIUM, category = PacmanSdkConstants.SECURITY)
public class IPFowardingRule  extends BasePolicy {
    private static final Logger logger = LoggerFactory.getLogger(IPFowardingRule.class);
    public String violationReason="";
    @Override
    public PolicyResult execute(Map<String, String> ruleParam, Map<String, String> resourceAttributes) {
        logger.debug("======== IPFowarding Rule started=========");
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
            vmEsURL = vmEsURL + "/gcp_vminstance/_search";
        }
        logger.debug("========vmEsURL URL after concatenation param {}  =========", vmEsURL);
        boolean isIPfowardRulePassed = false;

        MDC.put("executionId", ruleParam.get("executionId"));
        MDC.put("ruleId", ruleParam.get(PacmanSdkConstants.POLICY_ID));

        if (!StringUtils.isNullOrEmpty(resourceId)) {

            Map<String, Object> mustFilter = new HashMap<>();
            mustFilter.put(PacmanUtils.convertAttributetoKeyword(PacmanRuleConstants.RESOURCE_ID), resourceId);
            mustFilter.put(PacmanRuleConstants.LATEST, true);

            try {
                isIPfowardRulePassed = checkIPForwardRule(vmEsURL, mustFilter);
                if (!isIPfowardRulePassed) {
                    List<LinkedHashMap<String, Object>> issueList = new ArrayList<>();
                    LinkedHashMap<String, Object> issue = new LinkedHashMap<>();

                    annotation = Annotation.buildAnnotation(ruleParam, Annotation.Type.ISSUE);
                    annotation.put(PacmanSdkConstants.DESCRIPTION, "Ensure that IP Forwarding feature is not enabled at the Google Compute Engine instance level for security and compliance reasons and Ensure IP Forwarding feature Disabled for GKE instance. ");
                    annotation.put(PacmanRuleConstants.SEVERITY, severity);
                    annotation.put(PacmanRuleConstants.CATEGORY, category);

                    issue.put(PacmanRuleConstants.VIOLATION_REASON, violationReason);
                    issueList.add(issue);
                    annotation.put("issueDetails", issueList.toString());
                    logger.debug("========violation Reason {} : =========", annotation);
                    return new PolicyResult(PacmanSdkConstants.STATUS_FAILURE, PacmanRuleConstants.FAILURE_MESSAGE, annotation);
                }

            } catch (Exception exception) {
                throw new RuleExecutionFailedExeption(exception.getMessage());
            }
        }

        logger.debug("========Rule  ended with sucess ms=========");
        return new PolicyResult(PacmanSdkConstants.STATUS_SUCCESS, PacmanRuleConstants.SUCCESS_MESSAGE);
    }

    private boolean checkIPForwardRule(String vmEsURL, Map<String, Object> mustFilter) throws Exception {
        logger.debug("========DefaultServiceAccountUsageRule started=========");
        JsonArray hitsJsonArray = GCPUtils.getHitsArrayFromEs(vmEsURL, mustFilter);
        boolean validationResult = false;
        if (hitsJsonArray.size() > 0) {
            JsonObject vmInstanceObject = (JsonObject) ((JsonObject) hitsJsonArray.get(0))
                    .get(PacmanRuleConstants.SOURCE);

            logger.debug("Validating the data item: {}", vmInstanceObject.toString());

            boolean canIPForward=true;
            boolean isGKECluster=false;
            if(vmInstanceObject.get(PacmanRuleConstants.CAN_IP_FORWARD) !=null){
                 canIPForward=vmInstanceObject.get(PacmanRuleConstants.CAN_IP_FORWARD).getAsBoolean();
                violationReason="IP Forward Enabled for  GCE  Instance";


            }
            else {
                logger.info(PacmanRuleConstants.RESOURCE_DATA_NOT_FOUND);
            }
            if(vmInstanceObject.get(PacmanRuleConstants.TAGS).getAsJsonObject()!=null){
                JsonObject tags=vmInstanceObject.get(PacmanRuleConstants.TAGS).getAsJsonObject();
                if(tags.get(PacmanRuleConstants.GKE_CLUSTER_NAME)!=null && tags.get(PacmanRuleConstants.GKE_CLUSTER_NAME).getAsString()==""){
                    violationReason="IP Forward Disabled for GKE Cluster Instance";
                    isGKECluster=true;


                }

            }
            if(canIPForward==false && isGKECluster==false){
                validationResult=true;
            } else if (canIPForward==true && isGKECluster==true) {

                validationResult=true;
            }


        } else {
            logger.info(PacmanRuleConstants.RESOURCE_DATA_NOT_FOUND);
        }

        return validationResult;
    }

    @Override
    public String getHelpText() {
        return "disable ip forwarding for GCE instance and enable for GKE instance";
    }
}
