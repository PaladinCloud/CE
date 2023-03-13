package com.tmobile.cloud.awsrules.ecr;


import com.amazonaws.util.StringUtils;
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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
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
    String firstDiscoveredOn = resourceAttributes.get(PacmanRuleConstants.FIRST_DISCOVERED_ON);
    String discoveredDaysRange = ruleParam.get(PacmanRuleConstants.DISCOVERED_DAYS_RANGE);
    if (!StringUtils.isNullOrEmpty(firstDiscoveredOn)) {
      firstDiscoveredOn = firstDiscoveredOn.substring(0, PacmanRuleConstants.FIRST_DISCOVERED_DATE_FORMAT_LENGTH);
    }
    String aquaEsAPI = null;
    String formattedUrl = PacmanUtils.formatUrl(ruleParam, PacmanRuleConstants.ES_AQUA_IMAGE_URL);
    if (!StringUtils.isNullOrEmpty(formattedUrl)) {
      aquaEsAPI = formattedUrl;
    }
    MDC.put("executionId", ruleParam.get("executionId")); // this is the logback Mapped Diagnostic Contex
    MDC.put("ruleId", ruleParam.get(PacmanSdkConstants.POLICY_ID)); // this is the logback Mapped Diagnostic Contex
    if (!PacmanUtils.doesAllHaveValue( category, aquaEsAPI, target, discoveredDaysRange)) {
      logger.info(PacmanRuleConstants.MISSING_CONFIGURATION);
      throw new InvalidInputException(PacmanRuleConstants.MISSING_CONFIGURATION);
    }
    if (resourceAttributes != null) {
      imageId = StringUtils.trim(resourceAttributes.get(PacmanRuleConstants.RESOURCE_ID));
      String imageName = buildImageNameFromResourceId(imageId);
      List<JsonObject> vulnerabilityInfoList = new ArrayList<>();
      if (PacmanUtils.calculateLaunchedDuration(firstDiscoveredOn) > Long.parseLong(discoveredDaysRange)) {
        try {
          vulnerabilityInfoList = PacmanUtils.checkImageIdFromElasticSearchForAqua(imageName, aquaEsAPI, "image_name", target);
        } catch (Exception e) {
          logger.error("unable to determine", e);
          throw new RuleExecutionFailedExeption("unable to determine" + e);
        }
        String highestSeverity = getSeverity(vulnerabilityInfoList);
        JsonObject highestSeverityVulnerability = getHighestSeverityVulnerability(vulnerabilityInfoList, highestSeverity);
        List<Map<String, Object>> issueDetails = buildImageIssueDetails(vulnerabilityInfoList);

        if (highestSeverity != null && highestSeverityVulnerability != null) {
          annotation = Annotation.buildAnnotation(ruleParam, Annotation.Type.ISSUE);
          if(null!=highestSeverityVulnerability && issueDetails!=null){
            annotation.put(PacmanSdkConstants.DESCRIPTION, highestSeverityVulnerability.get("description").getAsString());
            annotation.put(PacmanRuleConstants.SEVERITY, highestSeverity);
            annotation.put(PacmanRuleConstants.CATEGORY, category);
            annotation.put(PacmanRuleConstants.NVD_URL, highestSeverityVulnerability.get("nvd_url").getAsString());
            annotation.put("issueDetails", issueDetails.toString());
            return new PolicyResult(PacmanSdkConstants.STATUS_FAILURE, PacmanRuleConstants.FAILURE_MESSAGE, annotation);
          }
        }
      }
    }
    logger.debug("========ResourceScannedByQualysRule ended=========");
    return new PolicyResult(PacmanSdkConstants.STATUS_SUCCESS, PacmanRuleConstants.SUCCESS_MESSAGE);
  }

  private List<Map<String, Object>> buildImageIssueDetails(List<JsonObject> vulnerabilityInfoList) {

    List<Map<String,Object>> issues = new ArrayList<>();

    for(JsonObject elem : vulnerabilityInfoList){
      Map<String,Object> issue = new HashMap<>();
      issue.putIfAbsent(PacmanRuleConstants.VIOLATION_REASON,elem.get("description").getAsString());
      issue.putIfAbsent(PacmanRuleConstants.AQUA_SEVERITY_CLASSIFICATION, elem.get("aqua_severity_classification").getAsString());
      issue.putIfAbsent(PacmanRuleConstants.AQUA_SEVERITY, elem.get("aqua_severity").getAsString());
      issue.putIfAbsent(PacmanRuleConstants.CVE_NUM, elem.get("name").getAsString());
      issue.putIfAbsent(PacmanRuleConstants.AQUA_SOLUTION, elem.get("solution").getAsString());
      issue.putIfAbsent(PacmanRuleConstants.NVD_URL, elem.get("nvd_url").getAsString());
      issue.putIfAbsent(PacmanRuleConstants.VULNERABLE_IMAGE_DETAILS, elem.get("resource"));
      issues.add(issue);
    }
    return issues;
  }

  private JsonObject getHighestSeverityVulnerability(List<JsonObject> vulnerabilityInfoList, String highestSeverity) {
    return vulnerabilityInfoList.stream().filter(elem -> highestSeverity.equals(elem.get("aqua_severity").getAsString())).findFirst().orElse(null);
  }

  private String getSeverity(List<JsonObject> vulnerabilityInfoList) {
    Set<String> allSeverities = vulnerabilityInfoList.stream().map(vul -> vul.get("aqua_severity").getAsString()).collect(Collectors.toSet());
    if(allSeverities!=null){
       if (allSeverities.contains("critical")){
         return "critical";
       }
       else if (allSeverities.contains("high")){
         return "high";
       }
       else if (allSeverities.contains("medium")){
         return "medium";
       }
       else if (allSeverities.contains("low")){
         return "medium";
       }
       else if (allSeverities.contains("negligible")){
         return "negligible";
       }
    }
    return "negligible";
  }

  private String buildImageNameFromResourceId(String imageId) {
    if(imageId!=null)
     return imageId.split("/")[1].concat(":latest");
    return null;
  }

  @Override
  public String getHelpText() {
    return null;
  }
}

