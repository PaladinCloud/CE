package com.tmobile.cloud.awsrules.securitygroup;

import com.tmobile.cloud.awsrules.utils.PacmanUtils;
import com.tmobile.cloud.constants.PacmanRuleConstants;
import com.tmobile.pacman.commons.PacmanSdkConstants;
import com.tmobile.pacman.commons.exception.InvalidInputException;
import com.tmobile.pacman.commons.rule.Annotation;
import com.tmobile.pacman.commons.rule.BaseRule;
import com.tmobile.pacman.commons.rule.PacmanRule;
import com.tmobile.pacman.commons.rule.RuleResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.regex.Pattern;
import java.util.stream.Collectors;


@PacmanRule(key = "check-for-security-group-naming-convention", desc = "check for the naming convention of security groups", severity = PacmanSdkConstants.SEV_MEDIUM, category = PacmanSdkConstants.GOVERNANCE)

/*
*
*
* from : trendmicro
* Default Pattern Format
    security-group-RegionCode-EnvironmentCode-ApplicationCode

   Default Pattern Components
    RegionCode
    (ue1|uw1|uw2|ew1|ec1|an1|an2|as1|as2|se1) for us-east-1, us-west-1, us-west-2, eu-west-1, eu-central-1, ap-northeast-1, ap-northeast-2, ap-southeast-1, ap-southeast-2, sa-east-1.

   EnvironmentCode
    (d|t|s|p) for development, test, staging, production.

   ApplicationCode
    ([a-z0-9\-]+) for applications (e.g. nodejs, mongo) running on the instances associated with the selected security groups.

    Default Pattern Examples
    security-group-us-east-1-p-mongo-elasticsearch
    security-group-ap-northeast-1-p-tomcat
* */

public class SecurityGroupNamingConvention extends BaseRule {

    private static final Logger logger= LoggerFactory.getLogger(SecurityGroupNamingConvention.class);
    //TODO: externalize to config file later
    private static final String SECURITY_GROUP_PREFIX="security-group";
    private static final String[] REGION_CODE={"us-east1", "us-west1","us-east2", "us-west2","eu-west-1", "eu-central-1", "ap-northeast-1", "ap-northeast-2", "ap-southeast-1", "ap-southeast-2", "sa-east-1"};
    private static final String[] ENVIRONMENT_CODE={"d","t","s","p"};
    private static final String APPLICATION_CODE="([a-z0-9\\-]+)";


    @Override
    public RuleResult execute(Map<String, String> ruleParam, Map<String, String> resourceAttributes) {
        logger.info("Executing rule SecurityGroupNamingConvention");
        //String elasticSearchUrl=ruleParam.get(PacmanRuleConstants.ES_URL_PARAM);
        //String pacmanHost= PacmanUtils.getPacmanHost(PacmanRuleConstants.ES_URI);

        logger.debug("Rule parameters:  {}",ruleParam );
        logger.debug("Resource attributes: {}", resourceAttributes);
        String resourceId = null;

        String severity = ruleParam.get(PacmanRuleConstants.SEVERITY);
        String category = ruleParam.get(PacmanRuleConstants.CATEGORY);

        if (!PacmanUtils.doesAllHaveValue(severity, category)) {
            logger.info(PacmanRuleConstants.MISSING_CONFIGURATION);
            throw new InvalidInputException(PacmanRuleConstants.MISSING_CONFIGURATION);
        }

        if(resourceAttributes!=null){
            resourceId=resourceAttributes.get(PacmanRuleConstants.RESOURCE_ID);

            Annotation annotation = null;
            annotation = Annotation.buildAnnotation(ruleParam, Annotation.Type.ISSUE);
            annotation.put(PacmanSdkConstants.DESCRIPTION, "Security group name is not as per convention.");
            annotation.put(PacmanRuleConstants.SEVERITY, severity);
            annotation.put(PacmanRuleConstants.CATEGORY, category);

            logger.debug("Before ValidateSecurityGroup Method.");

            boolean isValidName = validateSecurityGroupName(resourceId);
            logger.debug("After ValidateSecurityGroup Method.");
            logger.debug("Value of isValidName is: " + isValidName);


            if(!isValidName){
                logger.debug("Inside isValidName!");

                List<Map<String,Object>> issueList=new ArrayList<>();
                Map<String,Object> issue=new LinkedHashMap<>();
                issue.put(PacmanRuleConstants.VIOLATION_REASON,
                        "Security group name does not starts with correct prefix");
                issueList.add(issue);
                annotation.put("issueDetails",issueList.toString());
                logger.debug("Returning the failure!");

                return new RuleResult(PacmanSdkConstants.STATUS_FAILURE,PacmanRuleConstants.FAILURE_MESSAGE,annotation);
            }

        }
        logger.debug("Returning the success!");

        return new RuleResult(PacmanSdkConstants.STATUS_SUCCESS,PacmanRuleConstants.SUCCESS_MESSAGE);
    }

    private boolean validateSecurityGroupName(String resourceId) {

        logger.debug("Validating the SecurityGroupNamingConvention for resource : {}", resourceId );
        String namePrefix=SECURITY_GROUP_PREFIX;
        String[] regions= REGION_CODE;
        String[] env=ENVIRONMENT_CODE;
        String appNamePattern=APPLICATION_CODE;
        logger.debug("Validating the prefix criteria. Expected prefix: {}",namePrefix);
        boolean prefixCheck= resourceId.startsWith(namePrefix);
        if(!prefixCheck){
            logger.debug("Prefix validation failed. Resource doesn't have required prefix");
            return false;
        }
        resourceId = resourceId.substring(namePrefix.length()+1);

        logger.debug("Validating the regions criteria. Expected prefix: {}", Arrays.stream(regions).collect(Collectors.toList()));
        String  matchedRegion = validateValues(resourceId, regions);
        if(matchedRegion==null){
            logger.debug("Region validation failed. Resource doesn't have required region");
            return false;
        }
        resourceId = resourceId.substring(matchedRegion.length()+1);
        logger.debug("Validating the enviornment criteria. Expected env: {}", Arrays.stream(env).collect(Collectors.toList()));
        String matchedEnv=validateValues(resourceId,env);
        if(matchedEnv==null){
            logger.debug("Environment validation failed. Resource doesn't have required environment");
            return false;
        }
        resourceId=resourceId.substring(matchedEnv.length()+1);
        return Pattern.matches(appNamePattern, resourceId);
    }

    private String validateValues(String targetValue, String[] values) {
        return Arrays.stream(values).filter(item->targetValue.startsWith(item)).findFirst().orElse(null);
    }

    @Override
    public String getHelpText() {
        return "This rule checks if the security group names are as per the naming conventions configured";
    }

    public static void main(String[] args) {
        SecurityGroupNamingConvention demo=new SecurityGroupNamingConvention();
        logger.debug("Inside SecurityGroupNamingConvention rule.");
        Map<String, String> ruleParam = new HashMap<>();
        Map<String, String> resourceAttributes=new HashMap<>();

        String resourceId = "security-group-ap-northeast-1-p-tomcat";

        ruleParam.put(PacmanRuleConstants.SEVERITY,"low");
        ruleParam.put(PacmanRuleConstants.CATEGORY,"governance");
        ruleParam.put("_resourceid",resourceId);
        ruleParam.put("ruleId","123");
        ruleParam.put("policyId","123");
        ruleParam.put("policyVersion","1");

        resourceAttributes.put(PacmanRuleConstants.RESOURCE_ID, resourceId);

        RuleResult ruleResult = demo.execute(ruleParam, resourceAttributes);
        logger.debug("Rule execution status: {}",ruleResult.getStatus());
        logger.debug("Description: {}",ruleResult.getDesc());

    }
}
