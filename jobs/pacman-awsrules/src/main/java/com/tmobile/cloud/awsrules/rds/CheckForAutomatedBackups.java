package com.tmobile.cloud.awsrules.rds;

import com.nimbusds.oauth2.sdk.util.MapUtils;
import com.tmobile.cloud.awsrules.utils.PacmanUtils;
import com.tmobile.cloud.constants.PacmanRuleConstants;
import com.tmobile.pacman.commons.PacmanSdkConstants;
import com.tmobile.pacman.commons.exception.InvalidInputException;
import com.tmobile.pacman.commons.policy.Annotation;
import com.tmobile.pacman.commons.policy.BasePolicy;
import com.tmobile.pacman.commons.policy.PacmanPolicy;
import com.tmobile.pacman.commons.policy.PolicyResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

@PacmanPolicy(key = "check-automated-backups-in-rds-instance", desc = "This rule checks if Automated Backups are enabled for RDS DB Instances", severity = PacmanSdkConstants.SEV_HIGH, category = PacmanSdkConstants.SECURITY)
public class CheckForAutomatedBackups extends BasePolicy {

    private static final Logger logger = LoggerFactory.getLogger(CheckForAutomatedBackups.class);

    private static final String CONFIGURATION_VALUES = " - accountId - %s - storageType - %s";
    private static final String BACKUP_RETENTION_PERIOD = "0";

    private static PolicyResult buildFailureAnnotation(final Map<String, String> ruleParam, String description) {
        LinkedHashMap<String, Object> issue = new LinkedHashMap<>();
        List<LinkedHashMap<String, Object>> issueList = new ArrayList<>();
        Annotation annotation = Annotation.buildAnnotation(ruleParam, Annotation.Type.ISSUE);
        annotation.put(PacmanSdkConstants.DESCRIPTION, description);
        annotation.put(PacmanRuleConstants.SEVERITY, ruleParam.get(PacmanRuleConstants.SEVERITY));
        annotation.put(PacmanRuleConstants.CATEGORY, ruleParam.get(PacmanRuleConstants.CATEGORY));
        annotation.put(PacmanRuleConstants.RESOURCE_ID, ruleParam.get(PacmanRuleConstants.RESOURCE_ID));
        issue.put(PacmanRuleConstants.VIOLATION_REASON, description);
        issueList.add(issue);
        annotation.put("issueDetails", issueList.toString());
        logger.debug("========CheckForAutomatedBackups annotation {} :=========", annotation);
        return new PolicyResult(PacmanSdkConstants.STATUS_FAILURE, PacmanRuleConstants.FAILURE_MESSAGE, annotation);
    }

    /**
     * The method will get triggered from Rule Engine with following parameters <br>
     * Following are the Rule Parameters <br>
     * ruleKey :check-automated-backups-in-rds-instance <br>
     * threadsafe : if true , rule will be executed on multiple threads <br>
     * severity : Enter the value of severity <br>
     * ruleCategory : Enter the value of category <br>
     *
     * @param ruleParam          the rule param
     * @param resourceAttributes this is a resource in context which needs to be scanned this is provided by execution engine
     * @return ruleResult
     */
    @Override
    public PolicyResult execute(Map<String, String> ruleParam, Map<String, String> resourceAttributes) {
        logger.debug("========CheckForAutomatedBackups started=========");
        MDC.put("executionId", ruleParam.get("executionId"));
        MDC.put("ruleId", ruleParam.get(PacmanSdkConstants.POLICY_ID));
        if (MapUtils.isNotEmpty(ruleParam) && !PacmanUtils.doesAllHaveValue(ruleParam.get(PacmanRuleConstants.SEVERITY),
                ruleParam.get(PacmanRuleConstants.CATEGORY), resourceAttributes.get(PacmanRuleConstants.ACCOUNTID))) {
            logger.info(PacmanRuleConstants.MISSING_CONFIGURATION);
            throw new InvalidInputException(PacmanRuleConstants.MISSING_CONFIGURATION);
        }
        Optional<String> opt = Optional.ofNullable(resourceAttributes)
                .map(this::checkValidation);
        PolicyResult ruleResult = Optional.of(ruleParam).filter(param -> opt.isPresent())
                .map(param -> buildFailureAnnotation(param, opt.get()))
                .orElse(new PolicyResult(PacmanSdkConstants.STATUS_SUCCESS, PacmanRuleConstants.SUCCESS_MESSAGE));
        logger.debug("========CheckForAutomatedBackups ended=========");
        return ruleResult;
    }

    private String checkValidation(Map<String, String> resource) {
        String accountId = resource.get(PacmanRuleConstants.ACCOUNTID);
        String backupRetentionPeriod = resource.get(PacmanRuleConstants.BACKUP_RETENTION_PERIOD);
        String configValues = String.format(CONFIGURATION_VALUES, accountId, backupRetentionPeriod);

        if (Objects.isNull(backupRetentionPeriod) || backupRetentionPeriod.isEmpty() || Objects.isNull(accountId)
                || accountId.isEmpty()) {
            String missingConfiguration = PacmanRuleConstants.MISSING_CONFIGURATION + configValues;
            logger.info(missingConfiguration);
            throw new InvalidInputException(missingConfiguration);
        }
        if (backupRetentionPeriod.equalsIgnoreCase(BACKUP_RETENTION_PERIOD)) {
            return "Automated backup is not enabled in RDS DB instance- " + configValues;
        }
        logger.debug("========CheckForAutomatedBackups " + configValues);
        return null;
    }

    @Override
    public String getHelpText() {
        return "This rule checks if Automated Backups are enabled for RDS DB Instances";
    }
}
