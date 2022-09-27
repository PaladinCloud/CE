package com.tmobile.cloud.gcprules.vminstance;

import com.amazonaws.util.StringUtils;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.tmobile.cloud.awsrules.utils.PacmanUtils;
import com.tmobile.cloud.constants.PacmanRuleConstants;
import com.tmobile.cloud.gcprules.utils.GCPUtils;
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

@PacmanRule(key = "check-for-serial-port-enable-key", desc = "checks if instance-level serial port enable key is disabled", severity = PacmanSdkConstants.SEV_MEDIUM, category = PacmanSdkConstants.SECURITY)
public class VMInstanceSerialPortDisableRule extends BaseRule {
    private static final Logger logger = LoggerFactory.getLogger(VMInstanceSerialPortDisableRule.class);

    private static final String keyValue="serial-port-enable";

    @Override
    public RuleResult execute(Map<String, String> ruleParam, Map<String, String> resourceAttributes) {
        logger.debug("========VMInstanceSerialPortDisableRule started=========");
        Annotation annotation = null;

        String resourceId = ruleParam.get(PacmanRuleConstants.RESOURCE_ID);
        String severity = ruleParam.get(PacmanRuleConstants.SEVERITY);
        String category = ruleParam.get(PacmanRuleConstants.CATEGORY);

        String vmEsURL = CommonUtils.getEnvVariableValue(PacmanSdkConstants.ES_URI_ENV_VAR_NAME);

        if (Boolean.FALSE.equals(PacmanUtils.doesAllHaveValue(severity, category, vmEsURL))) {
            logger.info("{},{},{}",severity,category,vmEsURL);
            logger.info(PacmanRuleConstants.MISSING_CONFIGURATION);
            throw new InvalidInputException(PacmanRuleConstants.MISSING_CONFIGURATION);
        }

        if (!StringUtils.isNullOrEmpty(vmEsURL)) {
            vmEsURL = vmEsURL + "/gcp_vminstance/_search";
        }
        logger.debug("========vmEsURL URL after concatenation param {}  =========", vmEsURL);

        boolean isSerialPortDisable = false;

        MDC.put("executionId", ruleParam.get("executionId"));
        MDC.put("ruleId", ruleParam.get(PacmanSdkConstants.RULE_ID));

        if (!StringUtils.isNullOrEmpty(resourceId)) {

            Map<String, Object> mustFilter = new HashMap<>();
            mustFilter.put(PacmanUtils.convertAttributetoKeyword(PacmanRuleConstants.RESOURCE_ID), resourceId);
            mustFilter.put(PacmanRuleConstants.LATEST, true);

            try {
                isSerialPortDisable = verifyIsSerialPortDisable(vmEsURL, mustFilter);
                if (!isSerialPortDisable) {
                    List<LinkedHashMap<String, Object>> issueList = new ArrayList<>();
                    LinkedHashMap<String, Object> issue = new LinkedHashMap<>();

                    annotation = Annotation.buildAnnotation(ruleParam, Annotation.Type.ISSUE);
                    annotation.put(PacmanSdkConstants.DESCRIPTION, "serial-port-enable key is enabled");
                    annotation.put(PacmanRuleConstants.SEVERITY, severity);
                    annotation.put(PacmanRuleConstants.CATEGORY, category);

                    issue.put(PacmanRuleConstants.VIOLATION_REASON, "serial-port-enable key is enabled");
                    issueList.add(issue);
                    annotation.put("issueDetails", issueList.toString());
                    logger.debug("========VMInstanceSerialPortDisableRule ended with an annotation {} : =========", annotation);
                    return new RuleResult(PacmanSdkConstants.STATUS_FAILURE, PacmanRuleConstants.FAILURE_MESSAGE, annotation);
                }

            } catch (Exception exception) {
                throw new RuleExecutionFailedExeption(exception.getMessage());
            }
        }

        logger.debug("========VMInstanceSerialPortDisableRule ended=========");
        return new RuleResult(PacmanSdkConstants.STATUS_SUCCESS, PacmanRuleConstants.SUCCESS_MESSAGE);

    }

    private boolean verifyIsSerialPortDisable(String vmEsURL, Map<String, Object> mustFilter) throws Exception {
        logger.debug("========verifyIsBlockProjectWideSSHEnable started=========");
        JsonArray hitsJsonArray = GCPUtils.getHitsArrayFromEs(vmEsURL, mustFilter);
        boolean validationResult = true;
        if (hitsJsonArray.size() > 0) {
            JsonObject vmInstanceObject = (JsonObject) ((JsonObject) hitsJsonArray.get(0))
                    .get(PacmanRuleConstants.SOURCE);

            logger.debug("Validating the data item: {}", vmInstanceObject);
            JsonArray items=vmInstanceObject.getAsJsonObject().get(PacmanRuleConstants.ITEMS).getAsJsonArray();

            if(!items.isEmpty()){
                for(int i=0;i<items.size();i++){
                    JsonObject item=((JsonObject) items.get(i));

                    String key = item.getAsJsonObject()
                            .get(PacmanRuleConstants.KEY).getAsString();

                    Boolean value=item.getAsJsonObject()
                            .get(PacmanRuleConstants.VALUE).getAsBoolean();

                    if(key.equalsIgnoreCase(keyValue) && value) {
                        validationResult = false;
                        break;
                    }
                }
            }
            else {
                logger.info(PacmanRuleConstants.RESOURCE_DATA_NOT_FOUND);
            }

        } else {
            logger.info(PacmanRuleConstants.RESOURCE_DATA_NOT_FOUND);
        }

        return validationResult;
    }

    @Override
    public String getHelpText() {
        return "This rule checks if Enable connecting to serial ports configuration setting is disabled for all your production Google Compute Engine instances.";
    }
}
