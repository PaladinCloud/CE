package com.tmobile.cloud.gcprules.VPCNetwork;

import com.amazonaws.util.StringUtils;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.tmobile.cloud.awsrules.utils.PacmanUtils;
import com.tmobile.cloud.constants.PacmanRuleConstants;
import com.tmobile.pacman.commons.PacmanSdkConstants;
import com.tmobile.pacman.commons.exception.InvalidInputException;
import com.tmobile.pacman.commons.exception.RuleExecutionFailedExeption;
import com.tmobile.pacman.commons.rule.Annotation;
import com.tmobile.pacman.commons.rule.BaseRule;
import com.tmobile.pacman.commons.rule.PacmanRule;
import com.tmobile.pacman.commons.rule.RuleResult;
import com.tmobile.pacman.commons.utils.CommonUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;

import java.util.*;
import org.apache.commons.lang.ArrayUtils;

import com.tmobile.cloud.gcprules.utils.GCPUtils;

@PacmanRule(key = "check-for-vpc-network-firewall-security", desc = "checks for vpc network public IP address", severity = PacmanSdkConstants.SEV_HIGH, category = PacmanSdkConstants.SECURITY)
public class VPCNetworkRule extends BaseRule {
    private static final Logger logger = LoggerFactory.getLogger(VPCNetworkRule.class);

    @Override
    public RuleResult execute(Map<String, String> ruleParam, Map<String, String> resourceAttributes) {

        Annotation annotation = null;

        String resourceId = ruleParam.get(PacmanRuleConstants.RESOURCE_ID);

        String severity = ruleParam.get(PacmanRuleConstants.SEVERITY);

        String category = ruleParam.get(PacmanRuleConstants.CATEGORY);
        String[] port = ruleParam.get(PacmanRuleConstants.PORT).split(",");

        String description = ruleParam.get(PacmanRuleConstants.DESCRIPTION);
        String violtionReason = ruleParam.get(PacmanRuleConstants.VIOLATION_REASON);
        String vmEsURL = CommonUtils.getEnvVariableValue(PacmanSdkConstants.ES_URI_ENV_VAR_NAME);

        if (Boolean.FALSE.equals(PacmanUtils.doesAllHaveValue(severity, category, vmEsURL))) {
            logger.info(PacmanRuleConstants.MISSING_CONFIGURATION);
            throw new InvalidInputException(PacmanRuleConstants.MISSING_CONFIGURATION);
        }

        if (!StringUtils.isNullOrEmpty(vmEsURL)) {
            vmEsURL = vmEsURL + "/gcp_vpcfirewall/_search";
        }
        logger.debug("========gcp_vpcfirewall URL after concatenation param {}  =========", vmEsURL);

        boolean isVmWithPublicIp = false;

        MDC.put("executionId", ruleParam.get("executionId"));
        MDC.put("ruleId", ruleParam.get(PacmanSdkConstants.RULE_ID));

        if (!StringUtils.isNullOrEmpty(resourceId)) {
            logger.debug("========after url");

            Map<String, Object> mustFilter = new HashMap<>();
            mustFilter.put(PacmanUtils.convertAttributetoKeyword(PacmanRuleConstants.RESOURCE_ID), resourceId);
            mustFilter.put(PacmanRuleConstants.LATEST, true);

            try {
                isVmWithPublicIp = verifyPorts(vmEsURL, mustFilter, port);
                if (!isVmWithPublicIp) {
                    List<LinkedHashMap<String, Object>> issueList = new ArrayList<>();
                    LinkedHashMap<String, Object> issue = new LinkedHashMap<>();

                    annotation = Annotation.buildAnnotation(ruleParam, Annotation.Type.ISSUE);
                    annotation.put(PacmanSdkConstants.DESCRIPTION, description);
                    annotation.put(PacmanRuleConstants.SEVERITY, severity);
                    annotation.put(PacmanRuleConstants.CATEGORY, category);
                    issue.put(PacmanRuleConstants.VIOLATION_REASON, violtionReason);
                    issueList.add(issue);
                    annotation.put("issueDetails", issueList.toString());
                    logger.debug("========EC2WithPublicIPAccess ended with an annotation {} : =========", annotation);
                    return new RuleResult(PacmanSdkConstants.STATUS_FAILURE, PacmanRuleConstants.FAILURE_MESSAGE,
                            annotation);
                }

            } catch (Exception exception) {
                throw new RuleExecutionFailedExeption(exception.getMessage());
            }
        }

        logger.debug("========VMWithPublicIPAccess ended=========");
        return new RuleResult(PacmanSdkConstants.STATUS_SUCCESS, PacmanRuleConstants.SUCCESS_MESSAGE);

    }

    private boolean verifyPorts(String vmEsURL, Map<String, Object> mustFilter, String[] ports) throws Exception {
        logger.debug("========verifyports  started=========");
        JsonArray hitsJsonArray = GCPUtils.getHitsArrayFromEs(vmEsURL, mustFilter);
        boolean validationResult = true;
        if (hitsJsonArray.size() > 0) {
            logger.debug("========verifyports hit array=========");

            JsonObject vpcFirewall = (JsonObject) ((JsonObject) hitsJsonArray.get(0))
                    .get(PacmanRuleConstants.SOURCE);

            logger.debug("Validating the data item: {}", vpcFirewall.toString());

            boolean isDisabled = vpcFirewall.getAsJsonObject().get(PacmanRuleConstants.DISABLED).getAsBoolean();
            String direction = vpcFirewall.getAsJsonObject().get(PacmanRuleConstants.DIRECTION).getAsString();
            JsonArray sourceRanges = vpcFirewall.getAsJsonObject().get(PacmanRuleConstants.SOURCERANGES)
                    .getAsJsonArray();
            JsonArray allow = vpcFirewall.getAsJsonObject().get(PacmanRuleConstants.ALLOW.toLowerCase())
                    .getAsJsonArray();

            if (!isDisabled && direction.equalsIgnoreCase(PacmanRuleConstants.INGRESS)
                    && validateSourceRanges(sourceRanges)) {
                for (JsonElement jsonElement : allow) {
                    String protocol = jsonElement.getAsJsonObject().get(PacmanRuleConstants.PROTOCOL).getAsString();
                    JsonArray jsonports = jsonElement.getAsJsonObject().get(PacmanRuleConstants.PORTS).getAsJsonArray();
                    if (checkports(ports, protocol, jsonports)) {
                        validationResult = false;
                    }

                }

            }

        } else {
            logger.info(PacmanRuleConstants.RESOURCE_DATA_NOT_FOUND);
        }

        return validationResult;
    }

    boolean validateSourceRanges(JsonArray sourceRanges) {
        logger.debug("*********validate source Ranges {}", sourceRanges);

        for (JsonElement jsonElement : sourceRanges) {
            String sourcerange = jsonElement.toString().replaceAll("^\"|\"$", "");

            if (sourcerange.equals("0.0.0.0/0")) {

                return true;
            }
        }

        return false;
    }

    boolean checkports(String[] allparamsPorts, String protocol, JsonArray jsonports) {
        logger.debug("======== all paramsProtocol========= {}", allparamsPorts);

        for (String paramsPorts : allparamsPorts) {
            String paramsProtocol = paramsPorts.split(":")[0];
            String paramsPort = paramsPorts.split(":")[1];
            logger.debug("========check ports========= {}", paramsPort);
            logger.debug("========paramsProtocol========= {}", paramsProtocol);

            logger.debug("========paramsPort========= {}", paramsPort);
            logger.debug("========Protocol========= {}", protocol);
            if (paramsProtocol.equalsIgnoreCase(PacmanRuleConstants.ICMP)) {
                return true;
            }

            if (paramsProtocol.equalsIgnoreCase(protocol)) {
                logger.debug("========Protocol inside if========= {}", protocol);

                for (JsonElement jsonElement : jsonports) {
                    logger.debug("========check ports Jsonele ========= {}", jsonElement);
                    logger.debug("========check ports params port ========= {}", paramsPort);

                    if (jsonElement.toString().replaceAll("^\"|\"$", "").equalsIgnoreCase(paramsPort)) {
                        return true;
                    }

                }
            }

        }
        return false;

    }

    @Override
    public String getHelpText() {
        return "check public Access to VPC Fire wall";
    }

}
