package com.tmobile.pacman.commons.autofix.manager;

import com.amazonaws.regions.Regions;
import com.amazonaws.util.CollectionUtils;
import com.google.common.base.Splitter;
import com.google.common.base.Strings;
import com.google.common.collect.HashMultimap;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.microsoft.azure.PagedList;
import com.microsoft.azure.management.Azure;
import com.microsoft.azure.management.resources.Subscription;
import com.tmobile.pacman.common.AutoFixAction;
import com.tmobile.pacman.common.PacmanSdkConstants;
import com.tmobile.pacman.commons.AWSService;
import com.tmobile.pacman.commons.autofix.AutoFixPlan;
import com.tmobile.pacman.commons.autofix.FixResult;
import com.tmobile.pacman.commons.autofix.Status;
import com.tmobile.pacman.commons.aws.clients.AWSClientManager;
import com.tmobile.pacman.commons.aws.clients.impl.AWSClientManagerImpl;
import com.tmobile.pacman.commons.azure.clients.AzureCredentialProvider;
import com.tmobile.pacman.commons.config.ConfigUtil;
import com.tmobile.pacman.commons.exception.UnableToCreateClientException;
import com.tmobile.pacman.commons.gcp.clients.GCPCredentialsProvider;
import com.tmobile.pacman.dto.AutoFixTransaction;
import com.tmobile.pacman.dto.IssueException;
import com.tmobile.pacman.dto.ResourceOwner;
import com.tmobile.pacman.integrations.slack.SlackMessageRelay;
import com.tmobile.pacman.publisher.impl.ElasticSearchDataPublisher;
import com.tmobile.pacman.service.ExceptionManager;
import com.tmobile.pacman.service.ExceptionManagerImpl;
import com.tmobile.pacman.service.ResourceOwnerService;
import com.tmobile.pacman.util.CommonUtils;
import com.tmobile.pacman.util.ESUtils;
import com.tmobile.pacman.util.MailUtils;
import com.tmobile.pacman.util.ReflectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public interface IAutofixManger {

    AzureCredentialProvider azureCredentialProvider = new AzureCredentialProvider();
    GCPCredentialsProvider gcpCredentialsProvider=new GCPCredentialsProvider();

    static final Logger logger = LoggerFactory.getLogger(AutoFixManager.class);


    Map<String, String> targetTypeAlias = new HashMap<>();


    default void initializeTargetType() {
        if (targetTypeAlias.isEmpty()) {
            String alias = CommonUtils.getPropValue(PacmanSdkConstants.TARGET_TYPE_ALIAS);
            if (!Strings.isNullOrEmpty(alias)) {
                targetTypeAlias.putAll(Splitter.on(",").withKeyValueSeparator("=").split(alias));
            }
        }
    }

    /**
     * @param ruleParam
     * @param exemptedResourcesForRule
     * @param individuallyExcemptedIssues
     * @return
     * @throws Exception
     */
    default Map<String, Object> performAutoFixs(Map<String, String> ruleParam,
                                               Map<String, List<IssueException>> exemptedResourcesForRule,
                                               Map<String, IssueException> individuallyExcemptedIssues) throws Exception {

        List<Map<String, String>> existingIssues = null;
        String ruleId = ruleParam.get(PacmanSdkConstants.RULE_ID);
        ResourceOwnerService ownerService = new ResourceOwnerService();
        NextStepManager nextStepManager = new NextStepManager();
        ResourceTaggingManager taggingManager = new ResourceTaggingManager();
        AutoFixAction autoFixAction;
        List<FixResult> fixResults = new ArrayList<>();
        AWSService serviceType = null;
        ResourceOwner resourceOwner = null;
        String resourceId = null;
        String targetType = null;
        String parentDocId = null;
        String annotationId = null;
        String exceptionExpiryDate = null;
        Class<?> fixClass = null;
        Object fixObject = null;
        Method executeMethod = null;
        Method backupMethod = null;
        Method isFixCandidateMethod = null;
        Method addDetailsToTransactionLogMethod = null;
        String fixKey = null;
        Map<String, Object> autoFixStats = new HashMap<>();
        List<AutoFixTransaction> autoFixTrans = new ArrayList<>();
        List<AutoFixTransaction> silentautoFixTrans = new ArrayList<>();
        Map<String, Object> clientMap = null;

        Integer resourcesTaggedCounter = 0;
        Integer notificationSentCounter = 0;
        Integer autoFixCounter = 0;
        Integer errorWhileTaggingCounter = 0;
        Integer resourceOwnerNotFoundCounter = 0;
        Integer didNothingCounter = 0;
        Integer backupConfigCounter = 0;
        Integer autoFixPlanCreatedCounter = 0;

        String executionId = ruleParam.get(PacmanSdkConstants.EXECUTION_ID);
        String transactionId = null;

        MDC.put("executionId", executionId);
        MDC.put("ruleId", ruleParam.get(PacmanSdkConstants.RULE_ID));

        String type = "autofix";
        logger.info("autoFixmanager start");

        // check fix exists for rule
        initializeConfigs();
        initializeTargetType();
        try {
            fixKey = ruleParam.get("fixKey");
            fixClass = ReflectionUtils.findFixClass(fixKey);
            fixObject = fixClass.newInstance();
            executeMethod = ReflectionUtils.findAssociatedMethod(fixObject, "executeFix");
            backupMethod = ReflectionUtils.findAssociatedMethod(fixObject, "backupExistingConfigForResource");
            isFixCandidateMethod = findIsFixCandidateMethod(fixObject, isFixCandidateMethod);
            try {
                addDetailsToTransactionLogMethod = ReflectionUtils.findAssociatedMethod(fixObject, "addDetailsToTransactionLog");
            } catch (NoSuchMethodException e) {
                logger.debug("addDetailsToTransactionLog method not implemented", e);
            }
        } catch (Exception e) {
            logger.error(String.format("Please check the rule class complies to implemetation contract, fix key= %s", fixKey), e);
            autoFixStats.put("auto-fix-enabled", true);
            autoFixStats.put("auto-fix-error", "error finding fix class - >" + e.getMessage());
            return autoFixStats;
        }
        try {
            existingIssues = getOpenAndExcepmtedAnnotationForRule(ruleParam);
        } catch (Exception e) {
            logger.error("unable to get open issue for rule {} {}", ruleId, e);
            autoFixStats.put("auto-fix-error", "unable to get open issue for rule" + ruleId + "-- >" + e.getMessage());
            return autoFixStats;
        }
        int count = 0;
        AutoFixPlanManager autoFixPlanManager = new AutoFixPlanManager();
        AutoFixPlan autoFixPlan = null;
        for (Map<String, String> annotation : existingIssues) {
            List<AutoFixTransaction> addDetailsToLogTrans = new ArrayList<>();
            logger.debug("display issue count {}", count++);
            targetType = annotation.get("targetType");
            resourceId = annotation.get("_resourceid");
            parentDocId = annotation.get(PacmanSdkConstants.DOC_ID);
            transactionId = CommonUtils.getUniqueIdForString(resourceId);
            autoFixPlan = null;
            annotationId = annotation.get(PacmanSdkConstants.ES_DOC_ID_KEY); // this
            // will
            // be
            // used
            // to
            // identify
            // the
            // exception
            // for
            // the
            // resource
            //targetType =  getTargetTypeAlias(targetType);
            // commenting this , as alias is only used to create client, so using the function directly below


            serviceType = AWSService.valueOf(getTargetTypeAlias(targetType).toUpperCase());

            // create client
            if (isAccountAllowListedForAutoFix(annotation.get(PacmanSdkConstants.ACCOUNT_ID), ruleParam.get(PacmanSdkConstants.RULE_ID))) {

                clientMap=getClientMap(getTargetTypeAlias(targetType), annotation, CommonUtils.getPropValue(PacmanSdkConstants.AUTO_FIX_ROLE_NAME));
//                if (ruleParam.get("assetGroup").equalsIgnoreCase("aws")) {
//                    clientMap = getAWSClient(getTargetTypeAlias(targetType), annotation, CommonUtils.getPropValue(PacmanSdkConstants.AUTO_FIX_ROLE_NAME));
//                }
//                if (ruleParam.get("assetGroup").equalsIgnoreCase("azure")) {
//                    clientMap = getAzureClientMap(ruleParam.get("accountId"));
//                }

            } else {
                logger.info("Account id is blacklisted {}", annotation.get(PacmanSdkConstants.ACCOUNT_ID));
                continue;
            }
            logger.debug("processing for{} ", resourceId);
            if ((!isResourceTypeExemptedFromCutOfDateCriteria(targetType)
                    && resourceCreatedBeforeCutoffData(resourceId, targetType))
                    || !isresourceIdMatchesCriteria(resourceId) || !isAFixCandidate(isFixCandidateMethod, fixObject, resourceId, targetType, clientMap, ruleParam, annotation)) {
                logger.debug("exempted by various conditions -->{} ", resourceId);
                continue;
            }
            logger.debug("not exempted by conditions --> {} ", resourceId);

            try {
                autoFixPlan = autoFixPlanManager.getAutoFixPalnForResource(resourceId, ruleParam); // find plan associates with resource and issue
            } catch (Exception e) {
                logger.error("no plan found for resource {} {}", e, resourceId);
            }
            if (null != autoFixPlan) {
                logger.debug("got autofix plan with id" + autoFixPlan.getPlanId());
            }


            // if resource is exempted tag the resource
            String issueStatus = annotation.get("issueStatus");

            // find resource owner
            resourceOwner = ownerService.findResourceOwnerByIdAndType(resourceId, serviceType);
            // the following method will also trigger a auto fix plan creation if no action is taken on resource yet
            autoFixAction = nextStepManager.getNextStep(ruleParam, normalizeResourceId(resourceId, serviceType, annotation), resourceId, clientMap, serviceType);
            if (AutoFixAction.UNABLE_TO_DETERMINE == autoFixAction) {
                autoFixTrans.add(new AutoFixTransaction(AutoFixAction.UNABLE_TO_DETERMINE, resourceId, ruleId,
                        executionId, transactionId, "unable to determine the next set of action , not processing for this pass", type, annotation.get("targetType"), annotationId, annotation.get(PacmanSdkConstants.ACCOUNT_ID), annotation.get(PacmanSdkConstants.REGION), parentDocId));
                continue;
            }
            if (PacmanSdkConstants.ISSUE_STATUS_EXEMPTED_VALUE.equals(issueStatus)) {
                try {
                    // get the exception
                    // 1: check if individual exception exists
                    // individuallyExcemptedIssues.get(annotation.get(key));
                    // notify resource owner about exemption if he was already
                    // notified for violation
                    // check the next step
                    exceptionExpiryDate = getMaxExceptionExpiry(annotationId, resourceId, exemptedResourcesForRule,
                            individuallyExcemptedIssues);
                    try {
                        if (autoFixPlan != null && !Status.SUSPENDED.equals(autoFixPlan.getPlanStatus())) // suspend if not already suspended
                            autoFixPlanManager.suspendPlan(autoFixPlan, ruleParam);
                        logger.debug("auto fix plan suspended for plan # --> {}", autoFixPlan.getPlanId());
                        autoFixTrans.add(new AutoFixTransaction(AutoFixAction.SUSPEND_AUTO_FIX_PLAN, resourceId, ruleId, executionId,
                                transactionId, "auto fix plan suspended with id" + autoFixPlan.getPlanId(), type, annotation.get("targetType"), annotationId, annotation.get(PacmanSdkConstants.ACCOUNT_ID), annotation.get(PacmanSdkConstants.REGION), parentDocId));
                    } catch (Exception e) {
                        logger.error("unable to sync plna status , while resource is exempted, will try in next pass", e);
                    }
                    if (AutoFixAction.AUTOFIX_ACTION_FIX == autoFixAction) {
                        Map<String, String> pacTag = createPacTag(exceptionExpiryDate);
                        taggingManager.tagResource(normalizeResourceId(resourceId, serviceType, annotation), clientMap, serviceType, pacTag);
                        autoFixTrans.add(new AutoFixTransaction(AutoFixAction.AUTOFIX_ACTION_TAG, resourceId, ruleId,
                                executionId, transactionId, "resource tagged", type, annotation.get("targetType"), annotationId, annotation.get(PacmanSdkConstants.ACCOUNT_ID), annotation.get(PacmanSdkConstants.REGION), parentDocId));
                        resourcesTaggedCounter++;
                        // this means this resource was exempted after sending
                        // the violation emails and exempted afterwards
                        if (!nextStepManager.isSilentFixEnabledForRule(ruleId) && !MailUtils.sendAutoFixNotification(ruleParam, resourceOwner, targetType, resourceId,
                                exceptionExpiryDate, AutoFixAction.AUTOFIX_ACTION_EXEMPTED, addDetailsToLogTrans, annotation)) {
                            logger.error("unable to send email to {}", resourceOwner);
                        }
                    }
                    // should be removed for deployment
                    // throw new Exception("in case you run it by mistake it
                    // will tag all buckets , hence checking in with this guard
                    // rail");
                    continue;
                } catch (Exception e) {
                    logger.error("error while tagging the resource", e);
                    errorWhileTaggingCounter++;
                    continue;
                }
            } else {
                try {
                    // if issue is not exempted create auto fix plan
                    try {
                        if (null == autoFixPlan && !nextStepManager.isSilentFixEnabledForRule(ruleId)) {
                            autoFixPlan = autoFixPlanManager.createPlan(ruleId, annotation.get(PacmanSdkConstants.ANNOTATION_PK), resourceId,
                                    annotation.get(PacmanSdkConstants.DOC_ID), targetType, NextStepManager.getMaxNotifications(ruleId), NextStepManager.getAutoFixDelay(ruleId));
                            autoFixPlanManager.publishPlan(ruleParam, autoFixPlan);
                            autoFixPlanCreatedCounter++;
                            logger.debug("auto fix plan published with id {} ", autoFixPlan.getPlanId());
                            autoFixTrans.add(new AutoFixTransaction(AutoFixAction.CREATE_AUTO_FIX_PLAN, resourceId, ruleId, executionId,
                                    transactionId, "auto fix plan created with id" + autoFixPlan.getPlanId(), type, annotation.get("targetType"), annotationId, annotation.get(PacmanSdkConstants.ACCOUNT_ID), annotation.get(PacmanSdkConstants.REGION), parentDocId));
                        }
                    } catch (Exception e) {
                        logger.error(String.format("unable to create autofix plan for %s,%s,%s,%s,%s", ruleId, annotation.get(PacmanSdkConstants.ANNOTATION_PK), resourceId, annotation.get(PacmanSdkConstants.DOC_ID), targetType), e);
                    }

                    logger.debug("found the resource Owner {}", resourceOwner);
                    if (Strings.isNullOrEmpty(resourceOwner.getEmailId())
                            && !Strings.isNullOrEmpty(resourceOwner.getName())
                            && resourceOwner.getName().contains("@")) { // case
                        // when
                        // name
                        // contains
                        // email
                        resourceOwner.setEmailId(resourceOwner.getName());
                    }

                    if (!resourceOwner.getEmailId().contains("@")) { // service
                        // account
                        // case, in
                        // this
                        // case it
                        // is a
                        // service
                        // account
                        // name
                        resourceOwner
                                .setEmailId(CommonUtils.getPropValue(PacmanSdkConstants.ORPHAN_RESOURCE_OWNER_EMAIL));
                    }
                } catch (Exception e) {
                    logger.error("unable to find the resource owner for {} and {} ", resourceId, e);
                    resourceOwner = new ResourceOwner("CSO",
                            CommonUtils.getPropValue(PacmanSdkConstants.ORPHAN_RESOURCE_OWNER_EMAIL));
                    resourceOwnerNotFoundCounter++;
                }

                if (AutoFixAction.DO_NOTHING == autoFixAction) {
                    didNothingCounter++;
                    autoFixTrans.add(new AutoFixTransaction(AutoFixAction.DO_NOTHING, resourceId, ruleId, executionId,
                            transactionId, "waiting for 24 hours before fixing the violation", type, annotation.get("targetType"), annotationId, annotation.get(PacmanSdkConstants.ACCOUNT_ID), annotation.get(PacmanSdkConstants.REGION), parentDocId));

                    continue;
                }

                if (AutoFixAction.AUTOFIX_ACTION_EMAIL == autoFixAction
                        && isAccountAllowListedForAutoFix(annotation.get(PacmanSdkConstants.ACCOUNT_ID), ruleId)) {
                    long autofixExpiring = nextStepManager.getAutoFixExpirationTimeInHours(ruleParam.get(PacmanSdkConstants.RULE_ID), resourceId);
                	/*ZonedDateTime zonedDateTime = ZonedDateTime.now(ZoneId.of("America/Los_Angeles")).plusHours(Integer.parseInt(CommonUtils.getPropValue(PacmanSdkConstants.PAC_AUTO_FIX_DELAY_KEY
                            +"."+ ruleParam.get(PacmanSdkConstants.RULE_ID))));*/
                    ZonedDateTime zonedDateTime = ZonedDateTime.now(ZoneId.of("America/Los_Angeles")).plusHours(autofixExpiring);
                    String expiringTime = zonedDateTime.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
                    if (!MailUtils.sendAutoFixNotification(ruleParam, resourceOwner, targetType, resourceId,
                            expiringTime, AutoFixAction.AUTOFIX_ACTION_EMAIL, addDetailsToLogTrans, annotation)) {
                        String msg = String.format("unable to send email to %s for vulnerable resource %s, hence skipping this pass", resourceOwner.toString(), resourceId);
                        logger.error(msg);
                        new SlackMessageRelay().sendMessage(CommonUtils.getPropValue(PacmanSdkConstants.PAC_MONITOR_SLACK_USER), msg);
                        continue; // notification was not sent, skip further
                        // execution
                    }
                    logger.debug("email sent to {}", resourceOwner);
                    autoFixTrans.add(new AutoFixTransaction(AutoFixAction.AUTOFIX_ACTION_EMAIL, resourceId, ruleId, executionId,
                            transactionId, "email sent to " + resourceOwner.getEmailId(), type, annotation.get("targetType"), annotationId, annotation.get(PacmanSdkConstants.ACCOUNT_ID), annotation.get(PacmanSdkConstants.REGION), parentDocId));
                    notificationSentCounter++;
                    try {
                        nextStepManager.postFixAction(resourceId, AutoFixAction.EMAIL);
                        try {
                            if (null == autoFixPlan && !nextStepManager.isSilentFixEnabledForRule(ruleId)) {
                                autoFixPlanManager.synchronizeAndRepublishAutoFixPlan(autoFixPlan, resourceId, ruleParam);
                                autoFixTrans.add(new AutoFixTransaction(AutoFixAction.SYNC_AUTO_FIX_PLAN, resourceId, ruleId, executionId,
                                        transactionId, "auto fix plan synchronized with id" + autoFixPlan.getPlanId(), type, annotation.get("targetType"), annotationId, annotation.get(PacmanSdkConstants.ACCOUNT_ID), annotation.get(PacmanSdkConstants.REGION), parentDocId));
                            }
                        } catch (Exception e) {
                            logger.error("unable to syn plan for {} and {}", resourceId, e);
                        }
                    } catch (Exception e) {
                        logger.error("unable to post email action for {} and {}", resourceId, e);
                    }
                    fixResults.add(new FixResult(PacmanSdkConstants.STATUS_SUCCESS_CODE,
                            String.format("email sent to owner of resource %s", resourceId)));
                    continue;
                } else {
                    if (AutoFixAction.AUTOFIX_ACTION_FIX == autoFixAction) {
                        try {
                            try {
                                backupMethod.invoke(fixObject, resourceId, targetType, clientMap, ruleParam, annotation);
                            } catch (Exception e) {
                                logger.error("unable to backup the configuration for {} and {} and {}", targetType, resourceId, e);
                                continue;
                            }
                            autoFixTrans.add(new AutoFixTransaction(AutoFixAction.AUTOFIX_ACTION_BACKUP, resourceId, ruleId,
                                    executionId, transactionId, "resource aconfig backedup", type, annotation.get("targetType"), annotationId, annotation.get(PacmanSdkConstants.ACCOUNT_ID), annotation.get(PacmanSdkConstants.REGION), parentDocId));
                            backupConfigCounter++;
                            FixResult result = (FixResult) executeMethod.invoke(fixObject, annotation, clientMap, ruleParam);
                            fixResults
                                    .add(result);
                            autoFixTrans.add(new AutoFixTransaction(AutoFixAction.AUTOFIX_ACTION_FIX, resourceId, ruleId,
                                    executionId, transactionId, result.toString(), type, annotation.get("targetType"), annotationId, annotation.get(PacmanSdkConstants.ACCOUNT_ID), annotation.get(PacmanSdkConstants.REGION), parentDocId));
                            if (!nextStepManager.isSilentFixEnabledForRule(ruleId)) {

                                if (null != addDetailsToTransactionLogMethod) {
                                    addDetailsToLogTrans.add((AutoFixTransaction) addDetailsToTransactionLogMethod.invoke(fixObject, annotation));
                                }
                                MailUtils.sendAutoFixNotification(ruleParam, resourceOwner, targetType, resourceId, "",
                                        AutoFixAction.AUTOFIX_ACTION_FIX, addDetailsToLogTrans, annotation);
                                nextStepManager.postFixAction(resourceId, AutoFixAction.AUTOFIX_ACTION_FIX);
                                try {
                                    if (null == autoFixPlan && !nextStepManager.isSilentFixEnabledForRule(ruleId)) {
                                        autoFixPlanManager.synchronizeAndRepublishAutoFixPlan(autoFixPlan, resourceId, ruleParam);
                                        autoFixTrans.add(new AutoFixTransaction(AutoFixAction.SYNC_AUTO_FIX_PLAN, resourceId, ruleId, executionId,
                                                transactionId, "auto fix plan synchronized with id" + autoFixPlan.getPlanId(), type, annotation.get("targetType"), annotationId, annotation.get(PacmanSdkConstants.ACCOUNT_ID), annotation.get(PacmanSdkConstants.REGION), parentDocId));
                                    }
                                } catch (Exception e) {
                                    logger.error(String.format("unable to syn plan for %s", resourceId), e);
                                }
                                logger.debug("autofixed the resource {} and email sent to  {} and plan synchronized",
                                        resourceId, resourceOwner);
                            }
//                                    if(annotation.get("policyId").equalsIgnoreCase("PacMan_ApplicationTagsShouldBeValid_version-1")){
//                                    silentautoFixTrans.add(new AutoFixTransaction(resourceId, ruleId,annotation.get("accountid") , annotation.get("region"),annotation.get("correct_application_tag")));
//                                    }
                            else {
                                logger.error("unable to send email to {}", resourceOwner);

                            }
                            if (nextStepManager.isSilentFixEnabledForRule(ruleId) && null != addDetailsToTransactionLogMethod) {
                                silentautoFixTrans.add((AutoFixTransaction) addDetailsToTransactionLogMethod.invoke(fixObject, annotation));
                            }

                            autoFixCounter++;
                        } catch (Exception e) {
                            logger.error(String.format("unable to execute auto fix for %s  will not fix at this time", resourceId),
                                    e);
                            // continue with next bucket
                            continue;
                        }
                    } else if (AutoFixAction.AUTOFIX_ACTION_EMAIL_REMIND_EXCEPTION_EXPIRY == autoFixAction) {

                        if (!MailUtils.sendAutoFixNotification(ruleParam, resourceOwner, targetType, resourceId, "",
                                AutoFixAction.AUTOFIX_ACTION_EMAIL_REMIND_EXCEPTION_EXPIRY, addDetailsToLogTrans, annotation)) {
                            logger.error("unable to send email to {}", resourceOwner);
                        }
                    }
                }
            } // if issue open

        }// for

        autoFixPlanManager.releaseResourfes();
        //Silent fix send Digest email
        if (!silentautoFixTrans.isEmpty() && nextStepManager.isSilentFixEnabledForRule(ruleId)) {

            MailUtils.sendCommonFixNotification(silentautoFixTrans, ruleParam, resourceOwner, targetType);
        }
        // publish the transactions here
        // if any transaction exists post it
        if (autoFixTrans != null && !autoFixTrans.isEmpty()) {
            ElasticSearchDataPublisher dataPublisher = new ElasticSearchDataPublisher();
            dataPublisher.publishAutoFixTransactions(autoFixTrans, ruleParam);
            dataPublisher.close();
        }

        autoFixStats.put("autoFixCounter", autoFixCounter);
        autoFixStats.put("resourcesTaggedCounter", resourcesTaggedCounter);
        autoFixStats.put("notificationSentCounter", notificationSentCounter);
        autoFixStats.put("errorWhileTagging", errorWhileTaggingCounter);
        autoFixStats.put("resourceOwnerNotFound", resourceOwnerNotFoundCounter);
        autoFixStats.put("didNothingCounter", didNothingCounter);
        autoFixStats.put("backupConfigCounter", backupConfigCounter);
        autoFixStats.put("autoFixPlanCreatedCounter", autoFixPlanCreatedCounter);
        logger.info("autoFixmanager end");
        return autoFixStats;
    }

    Map<String, Object> getClientMap(String targetTypeAlias, Map<String, String> annotation, String autoFixRole) throws Exception;


    /**
     * return the right id needed for operation
     *
     * @param resourceId
     * @param serviceType
     * @param annotation
     * @return
     */
    default String normalizeResourceId(String resourceId, AWSService serviceType, Map<String, String> annotation) {

        switch (serviceType) {

            case ELB_APP:
                return annotation.get(PacmanSdkConstants.APP_ELB_ARN_ATTRIBUTE_NAME);
            default:
                return resourceId;
        }
    }

    /**
     * @param targetType
     * @return
     */
    default String getTargetTypeAlias(String targetType) {
        return targetTypeAlias.get(targetType) == null ? targetType : targetTypeAlias.get(targetType);
    }


    /**
     * find the method isfixCandidate.
     *
     * @param fixObject            the fix object
     * @param isFixCandidateMethod the is fix candidate method
     * @return the method
     */
    default Method findIsFixCandidateMethod(Object fixObject, Method isFixCandidateMethod) {
        try {
            isFixCandidateMethod = ReflectionUtils.findAssociatedMethod(fixObject, "isFixCandidate");
        } catch (Exception e) {
            logger.debug("isFixCandidateMethod not implemented will use the default value true", e);
        }
        return isFixCandidateMethod;
    }

    /**
     * Checks if is a fix candidate.
     *
     * @param isFixCandidateMethod the is fix candidate method
     * @param fixObject            the fix object
     * @param resourceId           the resource id
     * @param targetType           the target type
     * @param clientMap            the client map
     * @param ruleParam            the rule param
     * @param annotation           the annotation
     * @return true, if is a fix candidate
     * @throws IllegalAccessException    the illegal access exception
     * @throws IllegalArgumentException  the illegal argument exception
     * @throws InvocationTargetException the invocation target exception
     */
    default boolean isAFixCandidate(Method isFixCandidateMethod, Object fixObject, String resourceId, String targetType,
                                    Map<String, Object> clientMap, Map<String, String> ruleParam, Map<String, String> annotation) throws IllegalAccessException, IllegalArgumentException, InvocationTargetException {
        try {
            Boolean isFixCandidate = null == isFixCandidateMethod ? true : (Boolean) isFixCandidateMethod.invoke(fixObject, resourceId, targetType, clientMap, ruleParam, annotation);
            logger.debug("is fix candidate ==> {} ", isFixCandidate);
            return isFixCandidate;

        } catch (Exception e) {
            logger.error("error executing is fix candidate ", e);
            return Boolean.FALSE;
        }

    }

    /**
     * Checks if is resource type exempted from cut of date criteria.
     *
     * @param targetType the target type
     * @return true, if is resource type exempted from cut of date criteria
     */
    default boolean isResourceTypeExemptedFromCutOfDateCriteria(String targetType) {

        try {
            List<String> exemptedtypes = Arrays
                    .asList(CommonUtils.getPropValue(PacmanSdkConstants.AUTOFIX_EXEMPTED_TYPES_KEY).split("\\s*,\\s*"));
            return exemptedtypes.contains(targetType);
        } catch (Exception e) {
            logger.error("error in isResourceTypeExemptedFromCutOfDateCriteria {}", e);
            return false;
        }
    }

    /**
     * Gets the max exception expiry.
     *
     * @param annotationId                the annotation id
     * @param resourceId                  the resource id
     * @param exemptedResourcesForRule    the exempted resources for rule
     * @param individuallyExcemptedIssues the individually excempted issues
     * @return the max exception expiry
     * @throws Exception the exception
     */
    default String getMaxExceptionExpiry(String annotationId, String resourceId,
                                         Map<String, List<IssueException>> exemptedResourcesForRule,
                                         Map<String, IssueException> individuallyExcemptedIssues) throws Exception {

        // check if resource exempted using sticky exception
        List<IssueException> issueExceptions = exemptedResourcesForRule.get(resourceId);
        if (!CollectionUtils.isNullOrEmpty(issueExceptions)) {
            // get the max expiry date exception
        }
        // get individual exception details
        IssueException issueException = individuallyExcemptedIssues.get(annotationId);
        if (issueException != null) {
            return issueException.getExpiryDate();
        } else {
            throw new Exception("unable to find expiry date");
        }
    }

    /**
     * This will help testing the auto fix function.
     *
     * @param resourceId the resource id
     * @return true, if is resource id matches criteria
     */
    default boolean isresourceIdMatchesCriteria(String resourceId) {

        Pattern p;
        if (Strings.isNullOrEmpty(resourceId) || Strings
                .isNullOrEmpty(CommonUtils.getPropValue(PacmanSdkConstants.FIX_ONLY_MATCHING_RESOURCE_PATTERN))) {
            // resource with no name, this method has no responsibility to fix
            // this
            return true;
        }
        try {
            p = Pattern.compile(CommonUtils.getPropValue(PacmanSdkConstants.FIX_ONLY_MATCHING_RESOURCE_PATTERN));
        } catch (Exception e) {
            logger.info("no resource filter pattern defined {}", e);
            return true;
        }
        Matcher m = p.matcher(resourceId.toLowerCase());
        return m.find();

    }

    /**
     * Gets the AWS client.
     *
     * @param targetType            the target type
     * @param annotation            the annotation
     * @param ruleIdentifyingString the rule identifying string
     * @return the AWS client
     * @throws Exception the exception
     */
    default Map<String, Object> getAWSClient(String targetType, Map<String, String> annotation,
                                             String ruleIdentifyingString) throws Exception {

        StringBuilder roleArn = new StringBuilder();
        Map<String, Object> clientMap = null;
        roleArn.append(PacmanSdkConstants.ROLE_ARN_PREFIX).append(annotation.get(PacmanSdkConstants.ACCOUNT_ID))
                .append(":").append(ruleIdentifyingString);

        AWSClientManager awsClientManager = new AWSClientManagerImpl();
        try {
            clientMap = awsClientManager.getClient(annotation.get(PacmanSdkConstants.ACCOUNT_ID), roleArn.toString(),
                    AWSService.valueOf(targetType.toUpperCase()), Regions.fromName(
                            annotation.get(PacmanSdkConstants.REGION) == null ? Regions.DEFAULT_REGION.getName()
                                    : annotation.get(PacmanSdkConstants.REGION)),
                    ruleIdentifyingString);
        } catch (UnableToCreateClientException e1) {
            logger.error("unable to create client for account {} and region {} and {}", annotation.get(PacmanSdkConstants.ACCOUNT_ID), annotation.get(PacmanSdkConstants.REGION), e1);
            throw new Exception("unable to create client for account and region");
        }
        return clientMap;
    }

    /**
     * Resource created before cutoff data.
     *
     * @param resourceid   the resourceid
     * @param resourceType the resource type
     * @return true, if successful
     */
    default boolean resourceCreatedBeforeCutoffData(final String resourceid, String resourceType) {

        // Call service to find the resource creation date and check from cutoff
        // date defined in properties file return false if unable to determine
        try {
            return CommonUtils.resourceCreatedBeforeCutoffData(getResourceCreatedDate(resourceid, resourceType));
        } catch (Exception e) {
            // cannot find using heimdall, the fix shall expose a method to get
            // the resource specific creation date, call that method here and
            // get the creation date
            // for now returning true to indicate resource was created befroe
            // cutoff
            logger.error("error {}", e);
            return true;
        }
    }

    /**
     * Gets the resource created date.
     *
     * @param resourceId   the resource id
     * @param resourceType the resource type
     * @return the resource created date
     * @throws Exception the exception
     */
    @SuppressWarnings("unchecked")
    default Date getResourceCreatedDate(final String resourceId, String resourceType) throws Exception {
        String response = "";
        try {
            String url = CommonUtils.getPropValue(PacmanSdkConstants.RESOURCE_CREATIONDATE);
            url = url.concat("?resourceId=").concat(resourceId).concat("&resourceType=").concat(resourceType);
            response = CommonUtils.doHttpGet(url);
            if (!Strings.isNullOrEmpty(response)) {
                Gson serializer = new GsonBuilder().setLenient().create();
                Map<String, Object> resourceDetailsMap = (Map<String, Object>) serializer.fromJson(response,
                        Object.class);
                String resourceCreationDateString = resourceDetailsMap.get("data").toString();
                if ("Resource Not Found".equals(resourceCreationDateString))
                    return new Date();
                return CommonUtils.dateFormat(resourceCreationDateString, PacmanSdkConstants.YYYY_MM_DD_T_HH_MM_SS_Z,
                        PacmanSdkConstants.MM_DD_YYYY);
            }
            throw new Exception("unable to find resource creation date");
        } catch (Exception exception) {
            logger.error("Cannot find resource creation data {} response from service--> {}", response, exception);
            throw exception;
        }
    }

//    /**
//     * Checks if is account allow listed for auto fix.
//     *
//     * @param account the account
//     * @param ruleId the rule id
//     * @return true, if is account allow listed for auto fix
//     */
//    private boolean isAccountAllowListedForAutoFix(String account, String ruleId) {
//        try {
//            String allowlistStr = CommonUtils
//                    .getPropValue(PacmanSdkConstants.AUTOFIX_ALLOWLIST_ACCOUNTS_PREFIX + ruleId);
//            List<String> allowlist = Arrays.asList(allowlistStr.split("\\s*,\\s*"));
//            return allowlist.contains(account);
//        } catch (Exception e) {
//            logger.error(String.format("%s account assumed not allowlisted for autofix for ruleId %s" ,account, ruleId));
//            return Boolean.FALSE;
//        }
//    }


    /**
     * Checks if is account allow listed for auto fix.
     *
     * @param account the account
     * @param ruleId  the rule id
     * @return true, if is account allow listed for auto fix
     */
    default boolean isAccountAllowListedForAutoFix(String account, String ruleId) {
        try {
            String allowlistStr = CommonUtils
                    .getPropValue(PacmanSdkConstants.AUTOFIX_ALLOWLIST_ACCOUNTS_PREFIX + ruleId);
            List<String> allowlist = Arrays.asList(allowlistStr.split("\\s*,\\s*"));
            return allowlist.contains(account);
        } catch (Exception e) {
            logger.error("account is assumed allowlisted for autofix for ruleId {} and {} and {}", account, ruleId, e);
            return Boolean.TRUE; // be defensive , if not able to figure out , assume deny list
        }
    }


    /**
     * Gets the open and excepmted annotation for rule.
     *
     * @param ruleParam the rule param
     * @return the open and excepmted annotation for rule
     * @throws Exception the exception
     */
    default List<Map<String, String>> getOpenAndExcepmtedAnnotationForRule(Map<String, String> ruleParam)
            throws Exception {

        String esUrl = ESUtils.getEsUrl();
        String ruleId = ruleParam.get(PacmanSdkConstants.RULE_ID);
        String indexName = CommonUtils.getIndexNameFromRuleParam(ruleParam);
        String attributeToQuery = ESUtils.convertAttributetoKeyword(PacmanSdkConstants.RULE_ID);
        Map<String, Object> mustFilter = new HashMap<>();
        mustFilter.put(attributeToQuery, ruleId);
        mustFilter.put("type.keyword", "issue");
        HashMultimap<String, Object> shouldFilter = HashMultimap.create();
        shouldFilter.put(ESUtils.convertAttributetoKeyword(PacmanSdkConstants.ISSUE_STATUS_KEY),
                PacmanSdkConstants.STATUS_OPEN);
        shouldFilter.put(ESUtils.convertAttributetoKeyword(PacmanSdkConstants.ISSUE_STATUS_KEY),
                PacmanSdkConstants.STATUS_EXEMPTED);
        List<String> fields = new ArrayList<>();
        Long totalDocs = ESUtils.getTotalDocumentCountForIndexAndType(esUrl, indexName, null, mustFilter, null,
                shouldFilter);
        // get all the issues for this ruleId
        return ESUtils.getDataFromES(esUrl, indexName.toLowerCase(), null,
                mustFilter, null, shouldFilter, fields, 0, totalDocs);
    }

    /**
     * creates a tag for resource.
     *
     * @param exceptionDetails the exception details
     * @return the map
     * @throws Exception the exception
     */
    default Map<String, String> createPacTag(String exceptionDetails) throws Exception {
        String pacTagName = CommonUtils.getPropValue(PacmanSdkConstants.PACMAN_AUTO_FIX_TAG_NAME);
        // String pacTagValue = CommonUtils.encrypt(exceptionDetails,
        // CommonUtils.getPropValue(PacmanSdkConstants.PAC_AUTO_TAG_SALT_KEY));
        String pacTagValue = CommonUtils.encryptB64(exceptionDetails);
        Map<String, String> tagMap = new HashMap<>();
        tagMap.put(pacTagName, pacTagValue);
        return tagMap;
    }


    /**
     * test the code locally.
     *
     * @param args the arguments
     * @throws Exception the exception
     */
    public static void main(String[] args) throws Exception {
        CommonUtils.getPropValue(PacmanSdkConstants.ORPHAN_RESOURCE_OWNER_EMAIL);
        Map<String, String> params = new HashMap<>();
        Arrays.asList(args).stream().forEach(obj -> {
            String[] keyValue = obj.split("[:]");
            params.put(keyValue[0], keyValue[1]);
        });
        try {
            ConfigUtil.setConfigProperties(System.getenv(PacmanSdkConstants.CONFIG_CREDENTIALS),"azure-discovery");
            if (!(params == null || params.isEmpty())) {
                params.forEach((k, v) -> System.setProperty(k, v));
            }
        } catch (Exception e) {
            logger.error("Error fetching config", e);
            //ErrorManageUtil.uploadError("all", "all", "all", "Error fetching config "+ e.getMessage());
            //return ErrorManageUtil.formErrorCode();
        }
        Properties props = System.getProperties();

        Map<String, String> ruleParam = CommonUtils.createParamMap(args[0]);
        ExceptionManager exceptionManager = new ExceptionManagerImpl();
        Map<String, List<IssueException>> excemptedResourcesForRule = exceptionManager.getStickyExceptions(
                ruleParam.get(PacmanSdkConstants.RULE_ID), ruleParam.get(PacmanSdkConstants.TARGET_TYPE));
        Map<String, IssueException> individuallyExcemptedIssues = exceptionManager
                .getIndividualExceptions(ruleParam.get(PacmanSdkConstants.TARGET_TYPE));
        AutoFixManager autoFixManager = new AutoFixManager();
        autoFixManager.performAutoFixs(ruleParam, excemptedResourcesForRule, individuallyExcemptedIssues);

    }

    default Map<String, Object> getAzureClientMap(String subscriptionId) throws Exception {


        Map<String, Object> clientMap = null;
        //String tenantId="db8e92e6-bdc8-4d89-97c9-3e52cbe9d583";
        subscriptionId = "f4d319d8-7eac-4e15-a561-400f7744aa81";
        String tenant = System.getProperty("azure.credentials");
        String tenantStr = tenant.split("tenant:")[1];
        String tenantId = tenantStr.substring(0, tenantStr.indexOf(","));
        Azure.Authenticated azure = azureCredentialProvider.authenticate(tenantId);
        PagedList<Subscription> subscriptions = azure.subscriptions().list();

        //Azure.Authenticated authenticated = azureCredentialProvider.authenticate(tenantId);
        //String subId=authenticated.subscriptions().list().stream().filter(id->id.subscriptionId().equalsIgnoreCase(subscriptionId)).map(Subscription::subscriptionId).findFirst().orElse(null);

        //System.setProperty("azure.credentials", "tenant:db8e92e6-bdc8-4d89-97c9-3e52cbe9d583,clientId:5f07eb9a-7b4e-4f1f-a5ea-4d7fcbfd6c92,secretId:Pqk8Q~~alPOaGRmsd2OQ_mBY.QOAAs~5JnQcTcw9");
        Azure azureClient = azureCredentialProvider.authenticate(tenantId, subscriptionId);
        clientMap = new HashMap<>();
        clientMap.put("client", azureClient);
        return clientMap;
    }

    public abstract void initializeConfigs();
}
