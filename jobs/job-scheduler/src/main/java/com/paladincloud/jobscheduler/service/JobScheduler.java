package com.paladincloud.jobscheduler.service;


import com.paladincloud.jobscheduler.schema.jobs_and_rule_scheduling.Event;
import com.paladincloud.jobscheduler.schema.jobs_and_rule_scheduling.marshaller.Marshaller;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
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

    @Value("${aws.eventbridge.bus.details}")
    private String awsBusDetails;

    @Value("${gcp.eventbridge.bus.details}")
    private String gcpBusDetails;

    @Value("${azure.eventbridge.bus.details}")
    private String azureBusDetails;

    @Value("${azure_enabled}")
    private boolean azureEnabled;

    @Value("${gcp_enabled}")
    private boolean gcpEnabled;

    @Value("${no_of_rules_per_batch}")
    private String noOfRulesPerBatch;

    @Scheduled(initialDelayString = "${job.schedule.initialDelay}", fixedDelayString = "${job.schedule.interval}")
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

            PutEventsRequest eventsRequest = PutEventsRequest.builder()
                    .entries(putEventsRequestEntries)
                    .build();

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

    @Scheduled(initialDelayString = "${job.schedule.initialDelay.shipper}", fixedDelayString = "${job.schedule.interval}")
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

            PutEventsRequest eventsRequest = PutEventsRequest.builder()
                    .entries(putEventsRequestEntries)
                    .build();

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

    @Scheduled(initialDelayString = "${job.schedule.initialDelay.rules}", fixedDelayString = "${job.schedule.interval}")
    public void scheduleRules() {
        // print the current milliseconds
        logger.info("Current milliseconds: {}", System.currentTimeMillis());
        logger.info("Job Scheduler for rules is running...");

        EventBridgeClient eventBrClient = getEventBridgeClient();
        List<PutEventsRequestEntry> putEventsRequestEntries = new ArrayList<>();

        try {
            addRuleEvent(putEventsRequestEntries, awsBusDetails);
            if (gcpEnabled) {
                addRuleEvent(putEventsRequestEntries, gcpBusDetails);
            }
            if (azureEnabled) {
                addRuleEvent(putEventsRequestEntries, azureBusDetails);
            }

            PutEventsRequest eventsRequest = PutEventsRequest.builder()
                    .entries(putEventsRequestEntries)
                    .build();

            PutEventsResponse result = eventBrClient.putEvents(eventsRequest);

            for (PutEventsResultEntry resultEntry : result.entries()) {
                if (resultEntry.eventId() != null) {
                    logger.info("Event Id: {} ", resultEntry.eventId());
                } else {
                    logger.info("Injection failed with Error Code: {}", resultEntry.errorCode());
                }
            }

        } catch (EventBridgeException e) {
            logger.error(e.awsErrorDetails().errorMessage());
            System.exit(1);
        }
        eventBrClient.close();
    }

    private void addRuleEvent(List<PutEventsRequestEntry> putEventsRequestEntries, String busDetails) {
        String detailString = null;

        // populate events for each event bus
        String[] busDetailsArray = busDetails.split(",");
        for (String busDetail : busDetailsArray) {
            int noOfBatches = (int) Math.ceil(Double.parseDouble(busDetail.split(":")[1]) / Double.parseDouble(noOfRulesPerBatch));
            for (int i = 0; i < noOfBatches; i++) {
                Event event = populateEventForRule(busDetail.split(":")[0], i);

                detailString = getMarshalledEvent(detailString, event);
                PutEventsRequestEntry reqEntry = PutEventsRequestEntry.builder()
                        .source(EVENT_SOURCE)
                        .detailType(EVENT_DETAIL_TYPE)
                        .detail(detailString)
                        .eventBusName(busDetail.split(":")[0])
                        .build();

                // Add the PutEventsRequestEntry to a putEventsRequestEntries
                putEventsRequestEntries.add(reqEntry);

                // print the request entry
                logger.info("Request entry: {} ", reqEntry);
            }
        }
    }

    private void addShipperEvent(List<PutEventsRequestEntry> putEventsRequestEntries, String busDetails) {
        String detailString = null;

        // populate events for each event bus
        String[] busDetailsArray = busDetails.split(",");
        for (String busDetail : busDetailsArray) {
            Event event = populateEventForShipper(busDetail.split(":")[0]);

            detailString = getMarshalledEvent(detailString, event);
            PutEventsRequestEntry reqEntry = PutEventsRequestEntry.builder()
                    .source(EVENT_SOURCE)
                    .detailType(EVENT_DETAIL_TYPE)
                    .detail(detailString)
                    .eventBusName(busDetail.split(":")[0])
                    .build();

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
            Event event = populateEventForCollector(busDetail.split(":")[0]);
            detailString = getMarshalledEvent(detailString, event);
            PutEventsRequestEntry reqEntry = PutEventsRequestEntry.builder()
                    .source(EVENT_SOURCE)
                    .detailType(EVENT_DETAIL_TYPE)
                    .detail(detailString)
                    .eventBusName(busDetail.split(":")[0])
                    .build();

            // Add the PutEventsRequestEntry to a putEventsRequestEntries
            putEventsRequestEntries.add(reqEntry);

            // print the request entry
            logger.info("Request entry: {} ", reqEntry);
        }
    }

    private EventBridgeClient getEventBridgeClient() {
        Region region = Region.US_EAST_1;

        AwsBasicCredentials awsCreds = AwsBasicCredentials.create("", "");
        return EventBridgeClient.builder()
                .region(region)
                .credentialsProvider(StaticCredentialsProvider.create(awsCreds))
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
