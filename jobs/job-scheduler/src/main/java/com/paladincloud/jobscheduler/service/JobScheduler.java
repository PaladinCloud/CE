package com.paladincloud.jobscheduler.service;


import com.amazonaws.auth.BasicSessionCredentials;
import com.amazonaws.services.directory.model.AuthenticationFailedException;
import com.paladincloud.jobscheduler.auth.CredentialProvider;
import com.paladincloud.jobscheduler.schema.jobs_and_rule_scheduling.Event;
import com.paladincloud.jobscheduler.schema.jobs_and_rule_scheduling.marshaller.Marshaller;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.auth.credentials.AwsSessionCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.eventbridge.EventBridgeClient;
import software.amazon.awssdk.services.eventbridge.model.*;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;


@Service
public class JobScheduler {

    private static final Logger logger = LoggerFactory.getLogger(JobScheduler.class);

    private static final String EVENT_SOURCE = "paladincloud.jobs-scheduler";
    private static final String EVENT_DETAIL_TYPE = "Paladin Cloud Job Scheduling Event";

    @Autowired
    CredentialProvider credentialProvider;

    @Value("${aws.eventbridge.bus.details}")
    private String awsBusDetails;

    @Value("${gcp.eventbridge.bus.details}")
    private String gcpBusDetails;

    @Value("${azure.eventbridge.bus.details}")
    private String azureBusDetails;

    @Value("${azure.enabled}")
    private boolean azureEnabled;

    @Value("${gcp.enabled}")
    private boolean gcpEnabled;

    @Value("${scheduler.total.batches}")
    private String noOfBatches;

    @Value("${base.region}")
    private String region;

    @Value("${base.account}")
    private String baseAccount;

    @Value("${scheduler.role}")
    private String roleName;

    @Scheduled(initialDelayString = "${scheduler.collector.initial.delay}", fixedDelayString = "${scheduler.interval}")
    public void scheduleCollectorJobs() {
        // print the current milliseconds
        logger.info("Current milliseconds: {} ", System.currentTimeMillis());
        logger.info("Job Scheduler for collector is running...");

        EventBridgeClient eventBrClient = getEventBridgeClient();
        List<PutEventsRequestEntry> putEventsRequestEntries = new ArrayList<>();

        try {
            addCollectorEvent(putEventsRequestEntries, awsBusDetails);

            if (azureEnabled) {
                addCollectorEvent(putEventsRequestEntries, azureBusDetails);
            }
            if (gcpEnabled) {
                addCollectorEvent(putEventsRequestEntries, gcpBusDetails);
            }

            PutEventsRequest eventsRequest = PutEventsRequest.builder().entries(putEventsRequestEntries).build();

            PutEventsResponse result = eventBrClient.putEvents(eventsRequest);

            for (PutEventsResultEntry resultEntry : result.entries()) {
                if (resultEntry.eventId() != null) {
                    logger.info("Event Id: {} ", resultEntry.eventId());
                } else {
                    logger.info("Injection failed with Error Code: {} ", resultEntry.errorCode());
                }
            }

        } catch (EventBridgeException e) {
            logger.error(e.awsErrorDetails().errorMessage());
            System.exit(1);
        }
        eventBrClient.close();
    }

    @Scheduled(initialDelayString = "${scheduler.shipper.initial.delay}", fixedDelayString = "${scheduler.interval}")
    public void scheduleShipperJobs() {
        // print the current milliseconds
        logger.info("Current milliseconds: {}", System.currentTimeMillis());
        logger.info("Job Scheduler for shipper is running...");

        EventBridgeClient eventBrClient = getEventBridgeClient();
        List<PutEventsRequestEntry> putEventsRequestEntries = new ArrayList<>();

        try {
            addShipperEvent(putEventsRequestEntries, awsBusDetails);
            if (gcpEnabled) {
                addShipperEvent(putEventsRequestEntries, gcpBusDetails);
            }
            if (azureEnabled) {
                addShipperEvent(putEventsRequestEntries, azureBusDetails);
            }

            PutEventsRequest eventsRequest = PutEventsRequest.builder().entries(putEventsRequestEntries).build();

            PutEventsResponse result = eventBrClient.putEvents(eventsRequest);

            for (PutEventsResultEntry resultEntry : result.entries()) {
                if (resultEntry.eventId() != null) {
                    logger.info("Event Id: {} ", resultEntry.eventId());
                } else {
                    logger.info("Injection failed with Error Code: {} ", resultEntry.errorCode());
                }
            }

        } catch (EventBridgeException e) {
            logger.error(e.awsErrorDetails().errorMessage());
            System.exit(1);
        }
        eventBrClient.close();
    }

    @Scheduled(initialDelayString = "${scheduler.rules.initial.delay}", fixedDelayString = "${scheduler.interval}")
    public void scheduleRules() {
        // print the current milliseconds
        logger.info("Current milliseconds: {}", System.currentTimeMillis());
        logger.info("Job Scheduler for rules is running...");

        EventBridgeClient eventBrClient = getEventBridgeClient();

        // busdetails e.g- aws.eventbridge.bus.details=paladincloud-aws:aws:289
        // azure.eventbridge.bus.details=paladincloud-azure:azure:102
        // gcp.eventbridge.bus.details=paladincloud-gcp:gcp:30
        try {
            int totBatches = Integer.parseInt(this.noOfBatches);
            logger.info("No of batches: {}", noOfBatches);

            for (int i = 0; i < totBatches; i++) {
                List<PutEventsRequestEntry> putEventsRequestEntries = new ArrayList<>();
                // add event for aws rules
                putRuleEventIntoRequestEntry(i, awsBusDetails, putEventsRequestEntries);

                // add event for azure rules
                if (azureEnabled) {
                    putRuleEventIntoRequestEntry(i, azureBusDetails, putEventsRequestEntries);
                }

                // add event for gcp rules
                if (gcpEnabled) {
                    putRuleEventIntoRequestEntry(i, gcpBusDetails, putEventsRequestEntries);
                }

                PutEventsRequest eventsRequest = PutEventsRequest.builder().entries(putEventsRequestEntries).build();
                PutEventsResponse result = eventBrClient.putEvents(eventsRequest);

                for (PutEventsResultEntry resultEntry : result.entries()) {
                    if (resultEntry.eventId() != null) {
                        logger.info("Event Id: {} ", resultEntry.eventId());
                    } else {
                        logger.info("Injection failed with Error Code: {}", resultEntry.errorCode());
                    }
                }
                //Delay of 1 min between each batch
                Thread.sleep(1000 * 60);
            }

        } catch (EventBridgeException e) {
            logger.error(e.awsErrorDetails().errorMessage());
            System.exit(1);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        eventBrClient.close();
    }

    private void putRuleEventIntoRequestEntry(int batchNo, String busDetails, List<PutEventsRequestEntry> reqEntryList) {

        String detailString = null;
        String cloudName = busDetails.split(":")[0].split("-")[1];
        Event event = populateEventForRule(cloudName, batchNo);
        detailString = getMarshalledEvent(detailString, event);
        PutEventsRequestEntry reqEntry = PutEventsRequestEntry.builder().source(EVENT_SOURCE).detailType(EVENT_DETAIL_TYPE).detail(detailString).eventBusName(busDetails.split(":")[0]).build();
        // print the request entry
        logger.info("Request entry: {} ", reqEntry);

        // Add the PutEventsRequestEntry to a putEventsRequestEntries
        reqEntryList.add(reqEntry);
    }

    private void addShipperEvent(List<PutEventsRequestEntry> putEventsRequestEntries, String busDetails) {
        String detailString = null;

        // populate events for each event bus
        String[] busDetailsArray = busDetails.split(",");
        for (String busDetail : busDetailsArray) {
            String cloudName = busDetail.split(":")[0].split("-")[1];
            Event event = populateEventForShipper(cloudName);

            detailString = getMarshalledEvent(detailString, event);
            PutEventsRequestEntry reqEntry = PutEventsRequestEntry.builder().source(EVENT_SOURCE).detailType(EVENT_DETAIL_TYPE).detail(detailString).eventBusName(busDetail.split(":")[0]).build();

            // Add the PutEventsRequestEntry to a putEventsRequestEntries
            putEventsRequestEntries.add(reqEntry);

            // print the request entry
            logger.info("Request entry: {} ", reqEntry);

        }
    }

    private void addCollectorEvent(List<PutEventsRequestEntry> putEventsRequestEntries, String busDetails) {
        String detailString = null;

        // populate events for each event bus
        String[] busDetailsArray = busDetails.split(",");
        for (String busDetail : busDetailsArray) {
            String cloudName = busDetail.split(":")[0].split("-")[1];
            Event event = populateEventForCollector(cloudName);
            detailString = getMarshalledEvent(detailString, event);
            PutEventsRequestEntry reqEntry = PutEventsRequestEntry.builder().source(EVENT_SOURCE).detailType(EVENT_DETAIL_TYPE).detail(detailString).eventBusName(busDetail.split(":")[0]).build();

            // Add the PutEventsRequestEntry to a putEventsRequestEntries
            putEventsRequestEntries.add(reqEntry);

            // print the request entry
            logger.info("Request entry: {} ", reqEntry);
        }
    }

    private EventBridgeClient getEventBridgeClient() {
        Region reg = Region.of(region);
        BasicSessionCredentials tempCredentials = null;
        try {
            tempCredentials = credentialProvider.getCredentials(this.baseAccount, roleName);
        } catch (Exception e) {
            logger.error("{\"errcode\":\"NO_CRED\" , \"account\":\"" + this.baseAccount + "\", \"Message\":\"Error getting credentials for account " + this.baseAccount + "\" , \"cause\":\"" + e.getMessage() + "\"}");
//            ErrorManageUtil.uploadError(accountId, "all", "all", e.getMessage());
        }
        if (tempCredentials == null) {
            throw new AuthenticationFailedException("can not get the temp credentials!!");
        }
        return EventBridgeClient.builder()
                .region(reg)
                .credentialsProvider(StaticCredentialsProvider
                        .create(AwsSessionCredentials
                                .create(tempCredentials.getAWSAccessKeyId(), tempCredentials.getAWSSecretKey(), tempCredentials.getSessionToken())))
                .build();
    }

    private String getMarshalledEvent(String detailString, Event event) {
        try {
            detailString = Marshaller.marshal(event);
        } catch (IOException e) {
            //Failed to serialise the event as a JSON formatted string. Let's quit.
            e.printStackTrace();
            System.exit(1);
        }
        return detailString;
    }

    private Event populateEventForCollector(String cloudType) {
        Event event = new Event();
        event.setBatchNo(BigDecimal.valueOf(1));
        event.setCloudName(cloudType);
        event.setSubmitJob(true);
        event.setIsRule(false);
        event.setIsCollector(true);
        event.setIsShipper(false);
        return event;
    }

    private Event populateEventForShipper(String cloudType) {
        Event event = new Event();
        event.setBatchNo(BigDecimal.valueOf(1));
        event.setCloudName(cloudType);
        event.setSubmitJob(true);
        event.setIsRule(false);
        event.setIsCollector(false);
        event.setIsShipper(true);
        return event;
    }

    private Event populateEventForRule(String cloudType, int batchNo) {
        Event event = new Event();
        event.setBatchNo(BigDecimal.valueOf(batchNo));
        event.setCloudName(cloudType);
        event.setSubmitJob(true);
        event.setIsRule(true);
        event.setIsCollector(false);
        event.setIsShipper(false);

        return event;
    }
}