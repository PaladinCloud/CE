package com.tmobile.cloud.awsrules.s3;

import com.google.common.collect.HashMultimap;
import com.nimbusds.oauth2.sdk.util.MapUtils;
import com.tmobile.cloud.awsrules.utils.PacmanUtils;
import com.tmobile.cloud.awsrules.utils.S3PacbotUtils;
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
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;

@PacmanPolicy(key = "check-s3-object-level-read-logging-rule", desc = "This rule checks object level logging for s3 buckets", severity = PacmanSdkConstants.SEV_MEDIUM, category = PacmanSdkConstants.SECURITY)
public class S3ObjectLevelReadLoggingRule extends BasePolicy {

    private static final Logger logger = LoggerFactory.getLogger(S3ObjectLevelReadLoggingRule.class);

    private static final String CLOUD_TRAIL_EVENT_SELECTOR_URL = "/aws/cloudtrail_eventselector/_search";
    private static final String DATA_RESOURCE_TYPE = "AWS::S3::Object";
    private static final List<String> READ_WRITE_TYPES = Arrays.asList("All", "ReadOnly");

    private static PolicyResult buildFailureAnnotation(final Map<String, String> ruleParam, String description) {

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
        return new PolicyResult(PacmanSdkConstants.STATUS_FAILURE, PacmanRuleConstants.FAILURE_MESSAGE, annotation);

    }

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
    public PolicyResult execute(final Map<String, String> ruleParam, Map<String, String> resourceAttributes) {

        logger.debug("========CheckForS3ObjectLevelReadLogging started=========");

        MDC.put("executionId", ruleParam.get("executionId"));
        MDC.put("ruleId", ruleParam.get(PacmanSdkConstants.POLICY_ID));

        if (MapUtils.isNotEmpty(ruleParam) && !PacmanUtils.doesAllHaveValue(ruleParam.get(PacmanRuleConstants.SEVERITY),
                ruleParam.get(PacmanRuleConstants.CATEGORY))) {
            logger.info(PacmanRuleConstants.MISSING_CONFIGURATION);
            throw new InvalidInputException(PacmanRuleConstants.MISSING_CONFIGURATION);
        }

        Optional<String> opt = Optional.ofNullable(resourceAttributes).map(this::checkValidation);

        PolicyResult ruleResult = Optional.of(ruleParam).filter(param -> opt.isPresent())
                .map(param -> buildFailureAnnotation(param, opt.get()))
                .orElse(new PolicyResult(PacmanSdkConstants.STATUS_SUCCESS, PacmanRuleConstants.SUCCESS_MESSAGE));

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

        String bucketName = resourceAttributes.get(PacmanRuleConstants.NAME);
        String accountId = resourceAttributes.get(PacmanRuleConstants.ACCOUNTID);
        String pacmanHost = PacmanUtils.getPacmanHost(PacmanRuleConstants.ES_URI);

        if (!PacmanUtils.doesAllHaveValue(pacmanHost, accountId, bucketName)) {
            logger.info(PacmanRuleConstants.MISSING_CONFIGURATION);
            throw new InvalidInputException(PacmanRuleConstants.MISSING_CONFIGURATION);
        }

        try {
            String esEndPoint = pacmanHost + CLOUD_TRAIL_EVENT_SELECTOR_URL;
            Map<String, Object> mustFilter = new HashMap<>();
            mustFilter.put(PacmanRuleConstants.ACCOUNTID, accountId);
            mustFilter.put(PacmanRuleConstants.DATA_RESOURCE_TYPE, DATA_RESOURCE_TYPE);
            HashMultimap<String, Object> shouldFilter = HashMultimap.create();
            Map<String, Object> mustTermsFilter = new HashMap<>();

            Set<String> resourceValueSet = PacmanUtils.getValueFromElasticSearchAsSet(esEndPoint, mustFilter,
                    shouldFilter, mustTermsFilter, PacmanRuleConstants.DATA_RESOURCE_VALUE, null);

            if (Objects.isNull(resourceValueSet) || resourceValueSet.isEmpty()) {
                return S3PacbotUtils.CLOUD_TRAIL_MSG + accountId + " for s3 bucket: " + bucketName +
                        " for S3ObjectLevelReadLogging";
            }
            List<String> resourceValues = S3PacbotUtils.getValidResourceValue(resourceValueSet, bucketName);
            if (resourceValues.isEmpty()) {
                return S3PacbotUtils.CLOUD_TRAIL_MSG + accountId + " for s3 bucket: " + bucketName
                        + " and resourceValue is not matching for S3ObjectLevelReadLogging";
            }
            return S3PacbotUtils.getCloudTrailUsingResourceValue(resourceValues, esEndPoint, accountId, mustFilter,
                    bucketName, shouldFilter, pacmanHost, mustTermsFilter, READ_WRITE_TYPES);
        } catch (Exception ex) {
            logger.error("Object-level logging for read events is not enabled for S3 bucket" + ex.getMessage(), ex);
            return "Object-level logging for read events is enabled for S3 bucket";
        }
    }

    @Override
    public String getHelpText() {
        return "Checks the S3 buckets Object Level Logging enabled for Read Operations";
    }
}
