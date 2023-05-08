/*******************************************************************************
 * Copyright 2018 T Mobile, Inc. or its affiliates. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License.  You may obtain a copy
 * of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  See the
 * License for the specific language governing permissions and limitations under
 * the License.
 ******************************************************************************/


package com.tmobile.pacman.executor;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

import com.tmobile.pacman.commons.autofix.AutoFixManagerFactory;
import com.tmobile.pacman.commons.autofix.manager.IAutofixManger;
import com.tmobile.pacman.commons.policy.Annotation;
import com.tmobile.pacman.commons.policy.PacmanPolicy;
import com.tmobile.pacman.commons.policy.PolicyResult;
import com.tmobile.pacman.commons.utils.Constants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;

import com.google.common.base.Joiner;
import com.google.common.base.Splitter;
import com.google.common.base.Strings;
import com.google.common.collect.ImmutableMap;
import com.tmobile.pacman.common.PacmanSdkConstants;
import com.tmobile.pacman.dto.IssueException;
import com.tmobile.pacman.integrations.slack.SlackMessageRelay;
import com.tmobile.pacman.publisher.impl.AnnotationPublisher;
import com.tmobile.pacman.reactors.PacEventHandler;
import com.tmobile.pacman.service.ExceptionManager;
import com.tmobile.pacman.service.ExceptionManagerImpl;
import com.tmobile.pacman.util.AuditUtils;
import com.tmobile.pacman.util.CommonUtils;
import com.tmobile.pacman.util.ESUtils;
import com.tmobile.pacman.util.ProgramExitUtils;
import com.tmobile.pacman.util.ReflectionUtils;
import com.tmobile.pacman.util.PolicyExecutionUtils;
import com.tmobile.pacman.util.NotificationUtils;


// TODO: Auto-generated Javadoc
/**
 * This class is responsible for firing the execute method of the rule.
 *
 * @author kkumar
 */
public class PolicyExecutor {

    /** The Constant logger. */
    private static final Logger logger = LoggerFactory.getLogger(PolicyExecutor.class);
    
    /** The is resource filter exists. */
    private Boolean isResourceFilterExists = Boolean.FALSE;
    
    /**  Annotation Publisher *. */
    AnnotationPublisher annotationPublisher;

    /**
     * The main method.
     *
     * @param args the arguments
     */
    public static void main(String[] args) {

        // File f = new File("rule.jar-jar-with-dependencies.jar");
        // URLClassLoader cl = new URLClassLoader(new URL[]{f.toURI().toURL(),
        // null});
        //
        // Class<?> clazz =
        // cl.loadClass("com.tmobile.cloud.awsrules.ec2.CheckForNamingConvention");
        // Method main = clazz.getMethod("main", String[].class);
        // main.invoke(null, new Object[]{args});
        // if(1==1) return;
        
        
        String executionId = UUID.randomUUID().toString(); // this is the unique
        // id for this pass
        // of execution
        
        //check if triggered by event of square one project.
        logger.debug("received input-->" + args[0]);
        if(PacEventHandler.isInvocationSourceAnEvent(args[0]))
        {
            logger.info("input source detected as event, will process event now.");
            new PacEventHandler().handleEvent(executionId,args[0]);
        }else
        {
                try {   logger.info("input source detected as policy, will process policy now.");
                        new PolicyExecutor().run(args, executionId);
                } catch (Exception e) {
                    logger.error("error while in policy method for executionId ->" + executionId, e);
                }
        }
    }

    /**
     * Run.
     *
     * @param args the args
     * @param executionId the execution id
     * @throws InstantiationException the instantiation exception
     * @throws IllegalAccessException the illegal access exception
     * @throws ClassNotFoundException the class not found exception
     */
    private void run(String[] args, String executionId)
            throws InstantiationException, IllegalAccessException, ClassNotFoundException {
        Map<String, String> policyParam = new HashMap<String, String>();
        String policyParams = "";
        Boolean errorWhileProcessing = Boolean.FALSE;

        Map<String, Object> policyEngineStats = new HashMap<>();
        
        //this is elastic search type to put rule engine stats in
        final String type = CommonUtils.getPropValue(PacmanSdkConstants.STATS_TYPE_NAME_KEY); // "execution-stats";
        final String JOB_ID = CommonUtils.getEnvVariableValue(PacmanSdkConstants.JOB_ID);
        final String mandatoryTags = CommonUtils.getPropValue(PacmanSdkConstants.TAGGING_MANDATORY_TAGS);
        System.setProperty(Constants.RDS_DB_URL,CommonUtils.getPropValue(Constants.RDS_DB_URL));
        System.setProperty(Constants.RDS_USER,CommonUtils.getPropValue(Constants.RDS_USER));
        System.setProperty(Constants.RDS_PWD,CommonUtils.getPropValue(Constants.RDS_PWD));
        System.setProperty(Constants.BASE_ACCOUNT,CommonUtils.getPropValue(Constants.BASE_ACCOUNT));
        System.setProperty(Constants.BASE_REGION,CommonUtils.getPropValue(Constants.BASE_REGION));
        System.setProperty(Constants.BASE_ROLE,CommonUtils.getPropValue(Constants.BASE_ROLE));
        System.setProperty(Constants.SECRET_MANAGER_PATH,CommonUtils.getPropValue(Constants.SECRET_MANAGER_PATH));

        if (args.length > 0 && CommonUtils.buildPolicyUUIDFromJson(args[0]) != null) {
            String policyUUID = CommonUtils.buildPolicyUUIDFromJson(args[0]);
            String policyDetailsUrl = CommonUtils.getEnvVariableValue(PacmanSdkConstants.POLICY_DETAILS_URL);
            policyDetailsUrl += policyUUID;
            String policyDetails = CommonUtils.doHttpGet(policyDetailsUrl);
            if (Strings.isNullOrEmpty(policyDetails)) {
                logger.error(
                        "Policy details for the policyID {} not found ", policyUUID);
                logger.error("exiting now..");
                ProgramExitUtils.exitWithError();
            }
            policyParam = CommonUtils.createPolicyParamMap(policyDetails);

            policyParam.put(PacmanSdkConstants.EXECUTION_ID, executionId);
            policyParam.put(PacmanSdkConstants.TAGGING_MANDATORY_TAGS, mandatoryTags);
//            policyParam.put(PacmanSdkConstants.Role_IDENTIFYING_STRING, PacmanSdkConstants.ROLE_PREFIX +
//                    CommonUtils.getPropValue(PacmanSdkConstants.APPLICATION_PREFIX) + PacmanSdkConstants.ROLE_SUFFIX);
            policyParam.put(PacmanSdkConstants.Role_IDENTIFYING_STRING, PacmanSdkConstants.ROLE_PREFIX +
                    PacmanSdkConstants.INTEGRAION_ROLE);
            if (Strings.isNullOrEmpty(policyParam.get(PacmanSdkConstants.DATA_SOURCE_KEY))) {
                logger.error(
                        "data source is missing, will not be able to figure out the target index to post the policy evaluvation, please check rule configuration");
                logger.error("exiting now..");
                ProgramExitUtils.exitWithError();
            }
            logger.debug("policy Param String " + policyParams);
            logger.debug("target Type :" + policyParam.get(PacmanSdkConstants.TARGET_TYPE));
            logger.debug("policy Key : " + policyParam.get("policyKey"));
        } else {
            logger.debug(
                    "No arguments available for policy execution, unable to identify the policy due to missing arguments");
            logger.debug("atlest policy key is required to identify the policy class");
            logger.debug("returning now.");
            return;
        }
        try{
            setLogLevel(policyParam);
        }catch(Exception e){
            logger.info("no log level found in params , setting to ERROR");
        }
        setMappedDiagnosticContex(executionId, policyParam.get(PacmanSdkConstants.POLICY_ID));
        setUncaughtExceptionHandler();
        logger.debug("uncaught exception handler engaged.");
        setShutDownHook(policyEngineStats);
        logger.debug("shutdown hook engaged.");
        policyEngineStats.put(PacmanSdkConstants.JOB_ID, JOB_ID);
        policyEngineStats.put(PacmanSdkConstants.STATUS_KEY, PacmanSdkConstants.STATUS_RUNNING);
        policyEngineStats.put(PacmanSdkConstants.EXECUTION_ID, executionId);
        policyEngineStats.put(PacmanSdkConstants.POLICY_ID, policyParam.get(PacmanSdkConstants.POLICY_ID));
        long startTime = resetStartTime();
        policyEngineStats.put("startTime", CommonUtils.getCurrentDateStringWithFormat(PacmanSdkConstants.PAC_TIME_ZONE,
                PacmanSdkConstants.DATE_FORMAT));
        // publish the stats once to let ES know rule engine has started.
        ESUtils.publishMetrics(policyEngineStats,type);
        policyEngineStats.put("timeTakenToFindExecutable", CommonUtils.getElapseTimeSince(startTime));
        // get the resources based on Type
        // List<Map<String, String>> resources =
        // getResources(ruleParam.get(PacmanSdkConstants.TARGET_TYPE));
        List<Map<String, String>> resources = new ArrayList<>();
        List<String> userFields = null;
        if (!Strings.isNullOrEmpty(policyParam.get(PacmanSdkConstants.ES_SOURCE_FIELDS_KEY))) {
            userFields = Splitter.on("|").trimResults()
                    .splitToList(policyParam.get(PacmanSdkConstants.ES_SOURCE_FIELDS_KEY));
        }
        String indexName = "".intern();
        startTime = resetStartTime();

        try {
            indexName = CommonUtils.getIndexNameFromRuleParam(policyParam);
            Map<String, String> filter = new HashMap<>();
            if (!Strings.isNullOrEmpty(policyParam.get(PacmanSdkConstants.ACCOUNT_ID)))
                filter.put(ESUtils.createKeyword(PacmanSdkConstants.ACCOUNT_ID),
                        policyParam.get(PacmanSdkConstants.ACCOUNT_ID));
            if (!Strings.isNullOrEmpty(policyParam.get(PacmanSdkConstants.REGION)))
                filter.put(ESUtils.createKeyword(PacmanSdkConstants.REGION), policyParam.get(PacmanSdkConstants.REGION));
            if (!Strings.isNullOrEmpty(policyParam.get(PacmanSdkConstants.RESOURCE_ID)))
                filter.put(ESUtils.createKeyword(PacmanSdkConstants.RESOURCE_ID),
                        policyParam.get(PacmanSdkConstants.RESOURCE_ID));

            if (!filter.isEmpty()) {
                logger.debug("found filters in rule config, resources will be filtered");
                isResourceFilterExists = Boolean.TRUE;
                policyEngineStats.put("resource filter", filter);
            }
            resources = ESUtils.getResourcesFromEs(indexName, policyParam.get(PacmanSdkConstants.TARGET_TYPE), filter,
                    userFields);
            logger.debug("got resources for evaluvation, total resources = " + resources.size());
            policyEngineStats.put("timeTakenToFetchInventory", CommonUtils.getElapseTimeSince(startTime));
            if(resources.isEmpty()){
                logger.info("no resources to evaluvate exiting now");
                ProgramExitUtils.exitSucessfully();
            }
        } catch (Exception e) {
            logger.error(
                    "unable to get inventory for " + indexName + "--" + policyParam.get(PacmanSdkConstants.TARGET_TYPE),
                    e);
            policyEngineStats.put("errorMessage", "unable to fetch inventory");
            policyEngineStats.put("technicalErrorDetails", e.getMessage());
            ProgramExitUtils.exitWithError();
        }

        
        startTime = resetStartTime();

        logger.info("total objects received for policy " + resources.size());
        String policyParamStr = Joiner.on("#").withKeyValueSeparator("=").join(policyParam);
        policyEngineStats.put("timeTakenToGetResources", CommonUtils.getElapseTimeSince(startTime));
        policyEngineStats.put("totalResourcesForThisExecutionCycle", resources.size());
        policyEngineStats.put("policyId", policyParam.get(PacmanSdkConstants.POLICY_ID));
        policyEngineStats.put("policyParams", policyParamStr);
        startTime = System.nanoTime();
        // loop through resources and call rule execute method

        PolicyRunner policyRunner;
        if ("true".equals(policyParam.get(PacmanSdkConstants.RUN_ON_MULTI_THREAD_KEY))) {
            policyRunner = new MultiThreadedPolicyRunner();
        } else {
            policyRunner = new SingleThreadPolicyRunner();
        }

        // collect all resource ids for a post execution check of how many
        // executions returned issues.
        Map<String, Map<String, String>> resourceIdToResourceMap = new HashMap<>();
        resources.stream().forEach(obj -> {
            resourceIdToResourceMap.put(obj.get(PacmanSdkConstants.DOC_ID), obj);
        });
        List<PolicyResult> evaluations = new ArrayList<>();
        List<PolicyResult> missingEvaluations = new ArrayList<>();

        try {
            evaluations = policyRunner.runPolicies(resources, policyParam, executionId);
            policyEngineStats.put("totalEvaluvationsFromPolicyRunner", evaluations.size());
            logger.debug("total evaluations received back from policy Runner" + evaluations.size());
        } catch (Exception e) {
            String msg = "error occured while executing";
            logger.error(msg, e);
            policyEngineStats.put(msg, Strings.isNullOrEmpty(e.getMessage()) ? "" : e.getMessage());
            logger.error("exiting now..", e);
            ProgramExitUtils.exitWithError();
        }

        // if resources size is not equals to number of evaluations then we have
        // some exceptions during evaluation , those will be the intersection of
        // resource and evaluations
        List<String> missingResourceIds = new ArrayList<>();
        // *****************************************************************
        // handle missing evaluation start
        // **************************************************************************************
        if (resources.size() != evaluations.size()) {
            if(policyParam.containsKey(PacmanSdkConstants.POLICY_CONTACT))
            {
                String message = String.format("%s total resource -> %s , total results returned by policy-> %s",policyParam.get(PacmanSdkConstants.POLICY_ID), resources.size(),evaluations.size());
                //send  message about missing evaluations 
                if(notifyPolicyOwner(policyParam.get(PacmanSdkConstants.POLICY_CONTACT),message)){
                    logger.trace(String.format("message sent to %s" ,policyParam.get(PacmanSdkConstants.POLICY_CONTACT)));
                }else{
                    logger.error(String.format("unable to send message to %s" ,policyParam.get(PacmanSdkConstants.POLICY_CONTACT)));
                }
            }
            
            List<String> allEvaluvatedResources = evaluations.stream()
                    .map(obj -> obj.getAnnotation().get(PacmanSdkConstants.DOC_ID)).collect(Collectors.toList());
            logger.debug("all evaluated resource count" + allEvaluvatedResources.size());
            allEvaluvatedResources.stream().forEach(obj -> {
                resourceIdToResourceMap.remove(obj);
            });

            // create all missing evaluations as unknown / unable to execute
            // type annotations
            logger.debug("total potential missing evaluations" + resourceIdToResourceMap.size());
            final Map<String, String> policyParamCopy = ImmutableMap.<String, String>builder().putAll(policyParam).build();
            String policyKey = policyParam.get("policyKey");
            Class<?> policyClass = null;
            policyClass = ReflectionUtils.findAssociateClass(policyKey);
            PacmanPolicy policyAnnotation = policyClass.getAnnotation(PacmanPolicy.class);
            if (resourceIdToResourceMap.size() > 0) {
                resourceIdToResourceMap.values().forEach(obj -> {
                    missingEvaluations.add(new PolicyResult(PacmanSdkConstants.STATUS_UNKNOWN,
                            PacmanSdkConstants.STATUS_UNKNOWN_MESSAGE, PolicyExecutionUtils.buildAnnotation(policyParamCopy,
                                    obj, executionId, Annotation.Type.ISSUE, policyAnnotation)));
                });
                policyEngineStats.put("missingEvaluations", missingEvaluations.size());
                evaluations.addAll(missingEvaluations);
            }
        }

        // *********************************************************************
        // handle missing evaluation end
        // ***********************************************************************************

        logger.info("Elapsed time in minutes for evaluation: " + CommonUtils.getElapseTimeSince(startTime));
        policyEngineStats.put("timeTakenToEvaluvate", CommonUtils.getElapseTimeSince(startTime));
        startTime = System.nanoTime();
        IAutofixManger autoFixManager = AutoFixManagerFactory.getAutofixManager(policyParam.get(PacmanSdkConstants.ASSET_GROUP_KEY));
        // process rule evaluations the annotations based on result
        try {
            if (evaluations.size() > 0) {

                ExceptionManager exceptionManager = new ExceptionManagerImpl();
                Map<String, List<IssueException>> exemptedResourcesForPolicy = exceptionManager.getStickyExceptions(
                        policyParam.get(PacmanSdkConstants.POLICY_ID), policyParam.get(PacmanSdkConstants.TARGET_TYPE));
                Map<String, IssueException> individuallyExcemptedIssues = exceptionManager
                        .getIndividualExceptions(policyParam.get(PacmanSdkConstants.TARGET_TYPE));

                policyEngineStats.putAll(processPolicyEvaluations(resources, evaluations, policyParam,
                        exemptedResourcesForPolicy, individuallyExcemptedIssues));
                try {
                    if (policyParam.containsKey(PacmanSdkConstants.POLICY_PARAM_AUTO_FIX_KEY_NAME) && Boolean
                            .parseBoolean(policyParam.get(PacmanSdkConstants.POLICY_PARAM_AUTO_FIX_KEY_NAME)) == true) {
                        policyEngineStats.putAll(autoFixManager.performAutoFixs(policyParam, exemptedResourcesForPolicy,
                                individuallyExcemptedIssues));
                    }
                } catch (Exception e) {
                    logger.error("unable to signal auto fix manager");
                }
            } else {
                logger.info("no evaluvation to process");
            }
        } catch (Exception e) {
            logger.error("error while processing evaluvations", e);
            policyEngineStats.put("error-while-processing-evaluvations", e.getLocalizedMessage());
            errorWhileProcessing = true;
        }
        policyEngineStats.put("timeTakenToProcessEvaluvations", CommonUtils.getElapseTimeSince(startTime));
        startTime = System.nanoTime();
        policyEngineStats.put("endTime", CommonUtils.getCurrentDateStringWithFormat(PacmanSdkConstants.PAC_TIME_ZONE,
                PacmanSdkConstants.DATE_FORMAT));
        policyEngineStats.put(PacmanSdkConstants.STATUS_KEY, PacmanSdkConstants.STATUS_FINISHED);
        try{
                ESUtils.publishMetrics(policyEngineStats,type);
        }catch(Exception e) {
            logger.error("unable to publish metrices",e);
        }
        if (!errorWhileProcessing)
            ProgramExitUtils.exitSucessfully();
        else
            ProgramExitUtils.exitWithError();
    }

    /**
     * @param ruleParam
     */
    private void setLogLevel(Map<String, String> ruleParam) {
        ch.qos.logback.classic.Logger root = (ch.qos.logback.classic.Logger)LoggerFactory.getLogger(Logger.ROOT_LOGGER_NAME);
        root.setLevel(ch.qos.logback.classic.Level.toLevel(ruleParam.get("logLevel"),ch.qos.logback.classic.Level.ERROR));
        
    }

    /**
     * Notify policy owner.
     *
     * @param user the user
     * @param message the message
     * @return true, if successful
     */
    private boolean notifyPolicyOwner(String user, String message) {
        SlackMessageRelay messageRelay = new SlackMessageRelay();
        if(!Strings.isNullOrEmpty(user)){
            return messageRelay.sendMessage(user, message);
        }
        return false;
    }

    /**
     * Sets the mapped diagnostic contex.
     *
     * @param executionId the execution id
     * @param policyId the policy id
     */
    private void setMappedDiagnosticContex(String executionId, String policyId) {
        MDC.put(PacmanSdkConstants.EXECUTION_ID, executionId); // this is the
                                                               // logback Mapped
                                                               // Diagnostic
                                                               // Contex
        MDC.put(PacmanSdkConstants.POLICY_ID, policyId); // this is the logback
                                                     // Mapped Diagnostic Contex
    }

    /**
     * Reset start time.
     *
     * @return the long
     */
    private long resetStartTime() {
        return System.nanoTime();
    }

    /**
     * Sets the shut down hook.
     *
     * @param ruleEngineStats the rule engine stats
     */
    private void setShutDownHook(Map<String, Object> ruleEngineStats) {
        // final Thread mainThread = Thread.currentThread();
        Runtime.getRuntime().addShutdownHook(new Thread(new ShutDownHook(ruleEngineStats)));
    }

    /**
     * Process rule evaluations.
     *
     * @param resources the resources
     * @param evaluations the evaluations
     * @param policyParam the rule param
     * @param exemptedResourcesForPolicy the exempted resources for rule
     * @param individuallyExcemptedIssues the individually excempted issues
     * @return the map
     * @throws Exception the exception
     */
    private Map<String, Object> processPolicyEvaluations(List<Map<String, String>> resources,
            List<PolicyResult> evaluations, Map<String, String> policyParam,
            Map<String, List<IssueException>> exemptedResourcesForPolicy,
            Map<String, IssueException> individuallyExcemptedIssues) throws Exception {

        Map<String, Object> metrics = new HashMap();
        metrics.put("totalResourcesEvalauetd", evaluations.size());
        String evalDate = CommonUtils.getCurrentDateStringWithFormat(PacmanSdkConstants.PAC_TIME_ZONE,
                PacmanSdkConstants.DATE_FORMAT);
        Annotation annotation = null;
        annotationPublisher = new AnnotationPublisher();
        long exemptionCounter = 0;
        try {

            metrics.put("max-exemptible-resource-count", exemptedResourcesForPolicy.size());

            metrics.put("individual-exception-count-for-this-policy", individuallyExcemptedIssues.size());
        } catch (Exception e) {
            logger.error("unable to fetch exceptions", e);
        }
        Status status;
        int issueFoundCounter = 0;
        //Pre populate the existing issues
        annotationPublisher.populateExistingIssuesForType(policyParam);
        
        for (PolicyResult result : evaluations) {
            annotation = result.getAnnotation();
            if (PacmanSdkConstants.STATUS_SUCCESS.equals(result.getStatus())) {
                annotation.put(PacmanSdkConstants.REASON_TO_CLOSE_KEY, result.getDesc());
                annotationPublisher.submitToClose(annotation);
                // closeIssue(annotation); // close issue
            } else { // publish the issue to ES
                if (PacmanSdkConstants.STATUS_FAILURE.equals(result.getStatus())) {

                    status = adjustStatus(PacmanSdkConstants.STATUS_OPEN, exemptedResourcesForPolicy,
                            individuallyExcemptedIssues, annotation);
                    annotation.put(PacmanSdkConstants.ISSUE_STATUS_KEY, status.getStatus());

                    // if exempted add additional details
                    if (PacmanSdkConstants.STATUS_EXEMPTED.equals(status.getStatus())) {
                        exemptionCounter++;
                        annotation.put(PacmanSdkConstants.EXEMPTION_EXPIRING_ON, status.getExemptionExpiryDate());
                        annotation.put(PacmanSdkConstants.REASON_TO_EXEMPT_KEY, status.getReason());
                        annotation.put(PacmanSdkConstants.EXEMPTION_ID, status.getExceptionId());
                    }
                }
                if (PacmanSdkConstants.STATUS_UNKNOWN.equals(result.getStatus())) {
                    annotation.put(PacmanSdkConstants.ISSUE_STATUS_KEY, PacmanSdkConstants.STATUS_UNKNOWN);
                    annotation.put(PacmanSdkConstants.STATUS_REASON,
                            PacmanSdkConstants.STATUS_UNABLE_TO_DETERMINE);
                }

                annotation.put(PacmanSdkConstants.DATA_SOURCE_KEY, policyParam.get(PacmanSdkConstants.DATA_SOURCE_KEY));
                // add created date if not an existing issue
                if(!annotationPublisher.getExistingIssuesMapWithAnnotationIdAsKey().containsKey(CommonUtils.getUniqueAnnotationId(annotation))){
                    annotation.put(PacmanSdkConstants.CREATED_DATE, evalDate);
                }
                annotation.put(PacmanSdkConstants.MODIFIED_DATE, evalDate);
                // annotationPublisher.publishAnnotationToEs(annotation);
                annotationPublisher.submitToPublish(annotation);
                issueFoundCounter++;
                logger.info("submitted annotaiton to publisher");
            }

        }
        metrics.put("totalExemptionAppliedForThisRun", exemptionCounter);
        annotationPublisher.setRuleParam(ImmutableMap.<String, String>builder().putAll(policyParam).build());
        // annotation will contain the last annotation processed above
        
        if (!isResourceFilterExists) {
            annotationPublisher.setExistingResources(resources); // if resources
                                                                 // are not
                                                                 // filtered
                                                                 // then no need
                                                                 // to make
                                                                 // another
                                                                 // call.
        }
        // this will be used for closing issues if resources are filtered
        // already this will prevent actual issues to close
        else {
            annotationPublisher
                    .setExistingResources(ESUtils.getResourcesFromEs(CommonUtils.getIndexNameFromRuleParam(policyParam),
                            policyParam.get(PacmanSdkConstants.TARGET_TYPE), null, null));
        }
        annotationPublisher.publish();
        metrics.put("total-issues-found", issueFoundCounter);
        List<Annotation> closedIssues = annotationPublisher.processClosureEx();
        NotificationUtils.triggerNotificationsForViolations(annotationPublisher.getBulkUploadBucket(), annotationPublisher.getExistingIssuesMapWithAnnotationIdAsKey(), true);
        NotificationUtils.triggerNotificationsForViolations(annotationPublisher.getClouserBucket(), annotationPublisher.getExistingIssuesMapWithAnnotationIdAsKey(), false);

        Integer danglisngIssues = annotationPublisher.closeDanglingIssues(annotation);
        metrics.put("dangling-issues-closed", danglisngIssues);
        metrics.put("total-issues-closed", closedIssues.size() + danglisngIssues);
        AuditUtils.postAuditTrail(annotationPublisher.getBulkUploadBucket(), PacmanSdkConstants.STATUS_OPEN);
        AuditUtils.postAuditTrail(closedIssues, PacmanSdkConstants.STATUS_CLOSE);
        return metrics;
    }

    /**
     * Adjust the status of issue based on exception.
     *
     * @param status the status
     * @param excemptedResourcesForPolicy the excempted resources for policy
     * @param individuallyExcemptedIssues the individually excempted issues
     * @param annotation the annotation
     * @return the status
     */
    private Status adjustStatus(String status, Map<String, List<IssueException>> excemptedResourcesForPolicy,
            Map<String, IssueException> individuallyExcemptedIssues, Annotation annotation) {

        List<IssueException> stickyExceptions = excemptedResourcesForPolicy
                .get(annotation.get(PacmanSdkConstants.RESOURCE_ID));
        IssueException exception;
        if (null != stickyExceptions) {
            // get the exemption with min expiry date and create the status for
            // now taking from 0 index
            exception = stickyExceptions.get(0);
            return new Status(PacmanSdkConstants.STATUS_EXEMPTED, exception.getExceptionReason(), exception.getId(),
                    exception.getExpiryDate());
        } else // check individual exception
        {
            exception = individuallyExcemptedIssues.get(CommonUtils.getUniqueAnnotationId(annotation));
            if (null != exception) {
                return new Status(PacmanSdkConstants.STATUS_EXEMPTED, exception.getExceptionReason(), exception.getId(),
                        exception.getExpiryDate());
            } else {
                return new Status(status); // return the same status as input
            }
        }
    }

    /**
     * in case any rule throws exception and it reaches main, this will make
     * sure the VM is terminated gracefully close all clients here.
     */
    private void setUncaughtExceptionHandler() {
        Thread.currentThread().setUncaughtExceptionHandler(new PolicyEngineUncaughtExceptionHandler());
    }

    /**
     * The Class Status.
     */
    static class Status {
        
        /** The status. */
        String status;
        
        /** The reason. */
        String reason;
        
        /** The exemption id. */
        String exemptionId;
        
        /** The exemption expiry date. */
        String exemptionExpiryDate;

        /**
         * Instantiates a new status.
         *
         * @param status the status
         * @param reason the reason
         * @param exemptionId the exemption id
         * @param exemptionExpiryDate the exemption expiry date
         */
        public Status(String status, String reason, String exemptionId, String exemptionExpiryDate) {
            super();
            this.status = status;
            this.reason = reason;
            this.exemptionId = exemptionId;
            this.exemptionExpiryDate = exemptionExpiryDate;
        }

        /**
         * Instantiates a new status.
         *
         * @param status the status
         */
        public Status(String status) {
            this.status = status;
        }

        /**
         * Gets the status.
         *
         * @return the status
         */
        public String getStatus() {
            return status;
        }

        /**
         * Sets the status.
         *
         * @param status the new status
         */
        public void setStatus(String status) {
            this.status = status;
        }

        /**
         * Gets the reason.
         *
         * @return the reason
         */
        public String getReason() {
            return reason;
        }

        /**
         * Sets the reason.
         *
         * @param reason the new reason
         */
        public void setReason(String reason) {
            this.reason = reason;
        }

        /**
         * Gets the exception id.
         *
         * @return the exception id
         */
        public String getExceptionId() {
            return exemptionId;
        }

        /**
         * Sets the exception id.
         *
         * @param exceptionId the new exception id
         */
        public void setExceptionId(String exceptionId) {
            this.exemptionId = exceptionId;
        }

        /**
         * Gets the exemption id.
         *
         * @return the exemption id
         */
        public String getExemptionId() {
            return exemptionId;
        }

        /**
         * Sets the exemption id.
         *
         * @param exemptionId the new exemption id
         */
        public void setExemptionId(String exemptionId) {
            this.exemptionId = exemptionId;
        }

        /**
         * Gets the exemption expiry date.
         *
         * @return the exemption expiry date
         */
        public String getExemptionExpiryDate() {
            return exemptionExpiryDate;
        }

        /**
         * Sets the exemption expiry date.
         *
         * @param exemptionExpiryDate the new exemption expiry date
         */
        public void setExemptionExpiryDate(String exemptionExpiryDate) {
            this.exemptionExpiryDate = exemptionExpiryDate;
        }
    }

}
