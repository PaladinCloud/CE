package com.tmobile.cloud.awsrules.s3;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;

import com.google.common.collect.HashMultimap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;

import com.nimbusds.oauth2.sdk.util.MapUtils;
import com.tmobile.cloud.awsrules.utils.PacmanUtils;
import com.tmobile.cloud.constants.PacmanRuleConstants;
import com.tmobile.pacman.commons.PacmanSdkConstants;
import com.tmobile.pacman.commons.exception.InvalidInputException;
import com.tmobile.pacman.commons.rule.Annotation;
import com.tmobile.pacman.commons.rule.BaseRule;
import com.tmobile.pacman.commons.rule.PacmanRule;
import com.tmobile.pacman.commons.rule.RuleResult;

@PacmanRule(key = "check-s3-object-level-read-logging-rule", desc = "This rule checks object level logging for s3 buckets", severity = PacmanSdkConstants.SEV_MEDIUM, category = PacmanSdkConstants.SECURITY)
public class S3ObjectLevelReadLoggingRule extends BaseRule {

    private static final Logger logger = LoggerFactory.getLogger(S3ObjectLevelReadLoggingRule.class);

    private static final String CLOUD_TRAIL_URL = "/aws/cloudtrail/_search";
    private static final String CLOUD_TRAIL_EVENT_SELECTOR_URL = "/aws/cloudtrail_eventselector/_search";
    private static final String DATA_RESOURCE_TYPE = "AWS::S3::Object";

    /**
     * The method will get triggered from Rule Engine with following parameters
     *
     * @param ruleParam          *************Following are the Rule Parameters********* <br><br>
     *                           <p>
     *                           ruleKey :check-s3-object-level-read-logging-rule <br><br>
     *                           <p>
     *                           threadsafe : if true , rule will be executed on multiple threads <br><br>
     *                           <p>
     *                           severity : Enter the value of severity <br><br>
     *                           <p>
     *                           ruleCategory : Enter the value of category <br><br>
     * @param resourceAttributes this is a resource in context which needs to be scanned this is provided by execution engine
     */
    @Override
    public RuleResult execute(final Map<String, String> ruleParam, Map<String, String> resourceAttributes) {

        logger.debug("========CheckForS3ObjectLevelReadLogging started=========");

        MDC.put("executionId", ruleParam.get("executionId"));
        MDC.put("ruleId", ruleParam.get(PacmanSdkConstants.RULE_ID));

        if (MapUtils.isNotEmpty(ruleParam) && !PacmanUtils.doesAllHaveValue(ruleParam.get(PacmanRuleConstants.SEVERITY),
                ruleParam.get(PacmanRuleConstants.CATEGORY))) {
            logger.info(PacmanRuleConstants.MISSING_CONFIGURATION);
            throw new InvalidInputException(PacmanRuleConstants.MISSING_CONFIGURATION);
        }

        Optional<String> opt = Optional.ofNullable(resourceAttributes).map(this::checkValidation);

        RuleResult ruleResult = Optional.of(ruleParam).filter(param -> opt.isPresent())
                .map(param -> buildFailureAnnotation(param, opt.get()))
                .orElse(new RuleResult(PacmanSdkConstants.STATUS_SUCCESS, PacmanRuleConstants.SUCCESS_MESSAGE));

        logger.debug("========CheckForS3ObjectLevelReadLogging ended=========");
        return ruleResult;

    }

    /**
     * Checks the S3 buckets read operation logs has been enabled on object level.
     *
     * @param resourceAttributes resourceAttributes
     * @return string
     */
    private String checkValidation(Map<String, String> resourceAttributes) {

        String description = null;
        String bucketName = resourceAttributes.get(PacmanRuleConstants.NAME);
        String accountId = resourceAttributes.get(PacmanRuleConstants.ACCOUNTID);
        String pacmanHost = PacmanUtils.getPacmanHost(PacmanRuleConstants.ES_URI);
        List<String> readWriteTypes = Arrays.asList("All", "ReadOnly");
        if (!PacmanUtils.doesAllHaveValue(pacmanHost, accountId, bucketName)) {
            logger.info(PacmanRuleConstants.MISSING_CONFIGURATION);
            throw new InvalidInputException(PacmanRuleConstants.MISSING_CONFIGURATION);
        }

        try {
            String esEndPoint = pacmanHost + CLOUD_TRAIL_EVENT_SELECTOR_URL;
            Map<String, Object> mustFilter = new HashMap<>();
            mustFilter.put(PacmanRuleConstants.ACCOUNTID, accountId);
            mustFilter.put(PacmanRuleConstants.DATA_RESOURCE_TYPE, DATA_RESOURCE_TYPE);
            mustFilter.put(PacmanRuleConstants.DATA_RESOURCE_VALUE, "arn:aws:s3:::" + bucketName + "/");
            HashMultimap<String, Object> shouldFilter = HashMultimap.create();
            Map<String, Object> mustTermsFilter = new HashMap<>();

            Set<String> resultSet = PacmanUtils.getValueFromElasticSearchAsSet(esEndPoint, mustFilter,
                    shouldFilter, mustTermsFilter, "trailarn", null);
            Set<String> typeResultSet = PacmanUtils.getValueFromElasticSearchAsSet(esEndPoint, mustFilter,
                    shouldFilter, mustTermsFilter, "readwritetype", null);

            if (Objects.isNull(resultSet) || resultSet.isEmpty() || Objects.isNull(typeResultSet)
                    || typeResultSet.isEmpty()) {
                return "CloudTrail log with matching conditions does not exists, accountId: " + accountId
                        + " for s3 bucket: " + bucketName;
            }
            String trailFromSearch = resultSet.iterator().next();
            String readTypeFromSearch = typeResultSet.iterator().next();
            if (!readWriteTypes.contains(readTypeFromSearch)) {
                return "CloudTrail log with matching conditions does not exists," +
                        " readwritetype: " + readTypeFromSearch + ", accountId: " + accountId
                        + " for s3 bucket: " + bucketName;
            }

            esEndPoint = pacmanHost + CLOUD_TRAIL_URL;
            mustFilter = new HashMap<>();
            mustFilter.put(PacmanRuleConstants.MULTI_REGION_TRAIL, "true");
            mustFilter.put(PacmanRuleConstants.TRAIL_ARN, trailFromSearch);
            resultSet = PacmanUtils.getValueFromElasticSearchAsSet(esEndPoint, mustFilter,
                    shouldFilter, mustTermsFilter, "trailarn", null);
            if (Objects.isNull(resultSet) || resultSet.isEmpty()) {
                return "CloudTrail log with matching conditions does not exists, isMultiRegionTrail: true"
                        + ", accountId: " + accountId + " for s3 bucket: " + bucketName;
            }


        } catch (Exception ex) {
            logger.error("Object-level logging for read events is not enabled for S3 bucket" + ex.getMessage(), ex);
            description = "Object-level logging for read events is enabled for S3 bucket";
        }

        return description;
    }

    private static RuleResult buildFailureAnnotation(final Map<String, String> ruleParam, String description) {

        Annotation annotation = null;
        LinkedHashMap<String, Object> issue = new LinkedHashMap<>();
        List<LinkedHashMap<String, Object>> issueList = new ArrayList<>();

        annotation = Annotation.buildAnnotation(ruleParam, Annotation.Type.ISSUE);
        annotation.put(PacmanSdkConstants.DESCRIPTION, description);
        annotation.put(PacmanRuleConstants.SEVERITY, ruleParam.get(PacmanRuleConstants.SEVERITY));
        annotation.put(PacmanRuleConstants.CATEGORY, ruleParam.get(PacmanRuleConstants.CATEGORY));
        annotation.put(PacmanRuleConstants.RESOURCE_ID, ruleParam.get(PacmanRuleConstants.RESOURCE_ID));
        issue.put(PacmanRuleConstants.VIOLATION_REASON, description);
        issueList.add(issue);
        annotation.put("issueDetails", issueList.toString());
        logger.debug("========CheckForS3ObjectLevelReadLogging annotation {} :=========", annotation);
        return new RuleResult(PacmanSdkConstants.STATUS_FAILURE, PacmanRuleConstants.FAILURE_MESSAGE, annotation);

    }

    @Override
    public String getHelpText() {
        return "Checks the S3 buckets Object Level Logging enabled for Read Operations";
    }
}
