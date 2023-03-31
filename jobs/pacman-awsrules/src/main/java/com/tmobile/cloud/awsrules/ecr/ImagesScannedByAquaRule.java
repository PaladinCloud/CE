package com.tmobile.cloud.awsrules.ecr;


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
import java.text.ParseException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;

@PacmanPolicy(key = "check-for-images-scanned-by-aqua", desc = "checks for Images scanned by aqua,if not found then its an issue", severity = PacmanSdkConstants.SEV_HIGH, category = PacmanSdkConstants.GOVERNANCE)
public class ImagesScannedByAquaRule extends BasePolicy {

  private static final Logger logger = LoggerFactory.getLogger(ImagesScannedByAquaRule.class);
  /**
   * The method will get triggered from Rule Engine with following parameters.
   *
   * @param ruleParam          ************* Following are the Rule Parameters********* <br><br>
   *                           <p>
   *                           ruleKey : check-for-images-scanned-by-aqua <br><br>
   *                           <p>
   *                           target : Enter the target days <br><br>
   *                           <p>
   *                           discoveredDaysRange : Enter the discovered days Range <br><br>
   *                           <p>
   *                           esAquaUrl : Enter the Aqua URL <br><br>
   * @param resourceAttributes this is a resource in context which needs to be scanned this is provided by execution engine
   * @return the rule result
   */

  @Override
  public PolicyResult execute(Map<String, String> ruleParam, Map<String, String> resourceAttributes) {
    logger.debug("ImagesScannedByAquaRule execution started .............");
    Annotation annotation = null;
    String imageId = null;
    String category = ruleParam.get(PacmanRuleConstants.CATEGORY);
    String target = ruleParam.get(PacmanRuleConstants.TARGET);
    String discoveryDate = resourceAttributes.get(PacmanRuleConstants.DISCOVEREY_DATE);
    String discoveredDaysRange = ruleParam.get(PacmanRuleConstants.DISCOVERED_DAYS_RANGE);
    if (!StringUtils.isNullOrEmpty(discoveryDate)) {
      discoveryDate = discoveryDate.substring(0, PacmanRuleConstants.FIRST_DISCOVERED_DATE_FORMAT_LENGTH);
    }
    String aquaEsAPI = null;
    String formattedUrl = PacmanUtils.formatUrl(ruleParam, PacmanRuleConstants.ES_AQUA_IMAGE_URL);
    if (!StringUtils.isNullOrEmpty(formattedUrl)) {
      aquaEsAPI = formattedUrl;
    }
    MDC.put("executionId", ruleParam.get("executionId")); // this is the logback Mapped Diagnostic Contex
    MDC.put("ruleId", ruleParam.get(PacmanSdkConstants.POLICY_ID)); // this is the logback Mapped Diagnostic Contex
    if (!PacmanUtils.doesAllHaveValue(category, aquaEsAPI, discoveredDaysRange, target)) {
      logger.info(PacmanRuleConstants.MISSING_CONFIGURATION);
      throw new InvalidInputException(PacmanRuleConstants.MISSING_CONFIGURATION);
    }
    List<LinkedHashMap<String, Object>> issueList = new ArrayList<>();
    Gson gson = new Gson();
    if (resourceAttributes != null) {
      imageId = StringUtils.trim(resourceAttributes.get(PacmanRuleConstants.RESOURCE_ID));
      String imageName = buildImageNameFromResourceId(imageId);
      String entityType = resourceAttributes.get(PacmanRuleConstants.AQUA_ENTITY_TYPE);
      List<JsonObject> vulnerabilityInfoList = new ArrayList<>();
      if (PacmanUtils.calculateLaunchedDuration(discoveryDate) >= Long.parseLong(discoveredDaysRange)) {
        try {
          vulnerabilityInfoList = PacmanUtils.checkImageIdFromElasticSearchForAqua(imageName, aquaEsAPI, "image_name", null);
          if (CollectionUtils.isNullOrEmpty(vulnerabilityInfoList)) {
            annotation = Annotation.buildAnnotation(ruleParam, Annotation.Type.ISSUE);
            annotation.put(PacmanSdkConstants.DESCRIPTION, "" + entityType + " image not scanned  by aqua found!!");
            annotation.put(PacmanRuleConstants.CATEGORY, category);
            LinkedHashMap<String, Object> issue = new LinkedHashMap<>();
            issue.put(PacmanRuleConstants.VIOLATION_REASON, "" + entityType + " image not scanned by aqua found");
            issue.put(PacmanRuleConstants.SOURCE_VERIFIED, "_resourceid," + PacmanRuleConstants.AQUA_LAST_VULN_SCAN);
            issueList.add(issue);
            annotation.put("issueDetails", issueList.toString());
            logger.debug("========ResourceScannedByAquaRule ended with annotation {} : =========", annotation);
            return new PolicyResult(PacmanSdkConstants.STATUS_FAILURE, PacmanRuleConstants.FAILURE_MESSAGE, annotation);
          } else {
            Optional<String> foundAny = vulnerabilityInfoList
                .stream()
                .map(elem -> elem.get("last_found_date").getAsString()+"T00:00:00Z")
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
              annotation.put(PacmanSdkConstants.DESCRIPTION, "" + entityType + " aqua not scanned since "
                  + target + " days!!");
              annotation.put(PacmanRuleConstants.CATEGORY, category);
              LinkedHashMap<String, Object> issue = new LinkedHashMap<>();
              issue.put(PacmanRuleConstants.VIOLATION_REASON, "" + entityType + " image not scanned by aqua found");
              issueList.add(issue);
              annotation.put("issueDetails", issueList.toString());
              logger.debug("========ResourceScannedByAquaRule ended with annotation {} : =========", annotation);
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

