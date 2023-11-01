package com.tmobile.cloud.awsrules.rds;

import com.amazonaws.util.StringUtils;
import com.tmobile.cloud.awsrules.utils.PacmanUtils;
import com.tmobile.cloud.constants.PacmanRuleConstants;
import com.tmobile.pacman.commons.PacmanSdkConstants;
import com.tmobile.pacman.commons.exception.InvalidInputException;
import com.tmobile.pacman.commons.policy.Annotation;
import com.tmobile.pacman.commons.policy.PacmanPolicy;
import com.tmobile.pacman.commons.policy.PolicyResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@PacmanPolicy(key = "check-for-rds-db-unencrypted", desc = "This rule checks for RDS DB is unencrypted  if yes then it creates an issue", severity = PacmanSdkConstants.SEV_HIGH, category = PacmanSdkConstants.SECURITY)
public class RDSDBUnencryptedRule {

    private static final Logger logger = LoggerFactory.getLogger(RDSDBUnencryptedRule.class);
    /**
     * The method will get triggered from Rule Engine with following parameters
     *
     * @param ruleParam
     *
     **************Following are the Rule Parameters********* <br><br>
     *
     * ruleKey : check-for-rds-db-unencrypted <br><br>
     *
     * severity : Enter the value of severity <br><br>
     *
     * ruleCategory : Enter the value of category <br><br>
     *
     * @param resourceAttributes this is a resource in context which needs to be scanned this is provided by execution engine
     *
     */

    public PolicyResult execute(Map<String, String> ruleParam, Map<String, String> resourceAttributes) {
        logger.debug("========RDSDBUnencryptedPublicAccessRule started=========");

        Annotation annotation = null;
        LinkedHashMap<String, Object> issue = new LinkedHashMap<>();
        List<LinkedHashMap<String,Object>> issueList = new ArrayList<>();

        String severity = ruleParam.get(PacmanRuleConstants.SEVERITY);
        String category = ruleParam.get(PacmanRuleConstants.CATEGORY);
        String dbInstanceIdentifier = resourceAttributes.get(PacmanRuleConstants.DB_INSTANCE_IDENTIFIER);
        String storageEncrypted = resourceAttributes.get(PacmanRuleConstants.STORAGE_ENCRYPTED);
        String description = "Unencrypted rds db instance with publicly accessible ports found !!";

        MDC.put("executionId", ruleParam.get("executionId"));
        MDC.put("ruleId", ruleParam.get(PacmanSdkConstants.POLICY_ID));


        if (!PacmanUtils.doesAllHaveValue(severity, category )) {
            logger.info(PacmanRuleConstants.MISSING_CONFIGURATION);
            throw new InvalidInputException(PacmanRuleConstants.MISSING_CONFIGURATION);
        }

            if (!StringUtils.isNullOrEmpty(storageEncrypted) && !Boolean.parseBoolean(storageEncrypted)) {
                annotation = Annotation.buildAnnotation(ruleParam, Annotation.Type.ISSUE);
                annotation.put(PacmanSdkConstants.DESCRIPTION, description);
                annotation.put(PacmanRuleConstants.SEVERITY, severity);
                annotation.put(PacmanRuleConstants.CATEGORY, category);
                annotation.put(PacmanRuleConstants.RESOURCE_DISPLAY_ID, dbInstanceIdentifier);
                issue.put(PacmanRuleConstants.VIOLATION_REASON, description);
                issueList.add(issue);
                annotation.put("issueDetails", issueList.toString());
                logger.info(description);
                return new PolicyResult(PacmanSdkConstants.STATUS_FAILURE, PacmanRuleConstants.FAILURE_MESSAGE,
                        annotation);
            }
        logger.debug("========RDSDBUnencryptedPublicAccessRule ended=========");
        return new PolicyResult(PacmanSdkConstants.STATUS_SUCCESS, PacmanRuleConstants.SUCCESS_MESSAGE);

    }

    public String getHelpText() {
        return "This rule checks rdsdb has unencrypted public access";
    }

}
