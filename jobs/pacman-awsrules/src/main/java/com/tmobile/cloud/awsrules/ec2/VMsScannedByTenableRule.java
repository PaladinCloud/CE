package com.tmobile.cloud.awsrules.ec2;


import com.amazonaws.util.CollectionUtils;
import com.amazonaws.util.StringUtils;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.tmobile.cloud.awsrules.utils.PacmanUtils;
import com.tmobile.cloud.constants.PacmanRuleConstants;
import com.tmobile.pacman.commons.PacmanSdkConstants;
import com.tmobile.pacman.commons.exception.InvalidInputException;
import com.tmobile.pacman.commons.exception.RuleExecutionFailedExeption;
import com.tmobile.pacman.commons.policy.Annotation;
import com.tmobile.pacman.commons.policy.BasePolicy;
import com.tmobile.pacman.commons.policy.PacmanPolicy;
import com.tmobile.pacman.commons.policy.PolicyResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;

import java.text.ParseException;
import java.util.*;

@PacmanPolicy(key = "check-for-vms-scanned-by-tenable", desc = "checks for VMs scanned by tenable ,if not found then its an issue", severity = PacmanSdkConstants.SEV_HIGH, category = PacmanSdkConstants.GOVERNANCE)
public class VMsScannedByTenableRule extends BasePolicy {

  private static final Logger logger = LoggerFactory.getLogger(VMsScannedByTenableRule.class);
  /**
   * The method will get triggered from Rule Engine with following parameters.
   *
   * @param ruleParam          ************* Following are the Rule Parameters********* <br><br>
   *                           <p>
   *                           ruleKey : check-for-vms-scanned-by-tenable <br><br>
   *                           <p>
   *                           target : Enter the target days <br><br>
   *                           <p>
   *                           discoveredDaysRange : Enter the discovered days Range <br><br>
   *                           <p>
   *                           esTenableUrl : Enter the Tenable URL <br><br>
   * @param resourceAttributes this is a resource in context which needs to be scanned this is provided by execution engine
   * @return the rule result
   */

  @Override
  public PolicyResult execute(Map<String, String> ruleParam, Map<String, String> resourceAttributes) {
    logger.debug("VMsScannedByTenableRule execution started .............");
    Annotation annotation = null;
    String instanceID = null;
    String category = ruleParam.get(PacmanRuleConstants.CATEGORY);
    String target = ruleParam.get(PacmanRuleConstants.TARGET);
    String discoveryDate = resourceAttributes.get(PacmanRuleConstants.DISCOVEREY_DATE);
    String discoveredDaysRange = ruleParam.get(PacmanRuleConstants.DISCOVERED_DAYS_RANGE);
    if (!StringUtils.isNullOrEmpty(discoveryDate)) {
      discoveryDate = discoveryDate.substring(0, PacmanRuleConstants.FIRST_DISCOVERED_DATE_FORMAT_LENGTH);
    }
    String tenableEsAPI = null;
    String formattedUrl = PacmanUtils.formatUrl(ruleParam, PacmanRuleConstants.ES_TENABLE_VM_URL);
    if (!StringUtils.isNullOrEmpty(formattedUrl)) {
      tenableEsAPI = formattedUrl;
    }
    MDC.put("executionId", ruleParam.get("executionId")); // this is the logback Mapped Diagnostic Contex
    MDC.put("ruleId", ruleParam.get(PacmanSdkConstants.POLICY_ID)); // this is the logback Mapped Diagnostic Contex
    if (!PacmanUtils.doesAllHaveValue(category, tenableEsAPI, discoveredDaysRange, target)) {
      logger.info(PacmanRuleConstants.MISSING_CONFIGURATION);
      throw new InvalidInputException(PacmanRuleConstants.MISSING_CONFIGURATION);
    }
    List<LinkedHashMap<String, Object>> issueList = new ArrayList<>();
    Gson gson = new Gson();
    if (resourceAttributes != null) {
      instanceID = StringUtils.trim(resourceAttributes.get(PacmanRuleConstants.RESOURCE_ID));
      String entityType = resourceAttributes.get(PacmanRuleConstants.ENTITY_TYPE);
      List<JsonObject> vulnerabilityInfoList = new ArrayList<>();
      if (PacmanUtils.calculateLaunchedDuration(discoveryDate) >= 0) {
        try {
          vulnerabilityInfoList = PacmanUtils.checkInstanceIdFromElasticSearchForTenable(instanceID, tenableEsAPI, "asset.instanceId", null);
          if (CollectionUtils.isNullOrEmpty(vulnerabilityInfoList)) {
            annotation = Annotation.buildAnnotation(ruleParam, Annotation.Type.ISSUE);
            annotation.put(PacmanSdkConstants.DESCRIPTION, "" + entityType + " image not scanned  by tenable found!!");
            annotation.put(PacmanRuleConstants.CATEGORY, category);
            LinkedHashMap<String, Object> issue = new LinkedHashMap<>();
            issue.put(PacmanRuleConstants.VIOLATION_REASON, "" + entityType + " image not scanned by tenable found");
            issueList.add(issue);
            annotation.put("issueDetails", issueList.toString());
            logger.debug("========ResourceScannedByTenableRule ended with annotation {} : =========", annotation);
            return new PolicyResult(PacmanSdkConstants.STATUS_FAILURE, PacmanRuleConstants.FAILURE_MESSAGE, annotation);
          } else {
            Optional<String> foundAny = vulnerabilityInfoList
                .stream()
                .map(elem -> elem.get("last_found").getAsString())
                .filter(lastVulnScan -> {
                  try {
                    return PacmanUtils.calculateDuration(lastVulnScan) < Long.parseLong(target);
                  } catch (ParseException e) {
                    logger.error("Exception while parsing last scan date",e);
                  }
                  return false;
                }).findAny();
            if (!foundAny.isPresent()) {
              annotation = Annotation.buildAnnotation(ruleParam, Annotation.Type.ISSUE);
              annotation.put(PacmanSdkConstants.DESCRIPTION, "" + entityType + " tenable not scanned since "
                  + target + " days!!");
              annotation.put(PacmanRuleConstants.CATEGORY, category);
              LinkedHashMap<String, Object> issue = new LinkedHashMap<>();
              issue.put(PacmanRuleConstants.VIOLATION_REASON, "" + entityType + " image not scanned by tenable found");
              issueList.add(issue);
              annotation.put("issueDetails", issueList.toString());
              logger.debug("========ResourceScannedByTenableRule ended with annotation {} : =========", annotation);
              return new PolicyResult(PacmanSdkConstants.STATUS_FAILURE, PacmanRuleConstants.FAILURE_MESSAGE, annotation);
            }
          }
        } catch (Exception e) {
          logger.error("unable to determine", e);
          throw new RuleExecutionFailedExeption("unable to determine" + e);
        }
      }
    }
    return null;
  }

  private String buildImageNameFromResourceId(String imageId) {
    if(imageId!=null && imageId.split("/").length>1)
      return imageId.split("/")[1].concat(":latest");
    return null;
  }

  @Override
  public String getHelpText() {
    return null;
  }
}

