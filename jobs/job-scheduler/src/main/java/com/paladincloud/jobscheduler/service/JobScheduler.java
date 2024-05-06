package com.paladincloud.jobscheduler.service;


import com.amazonaws.auth.BasicSessionCredentials;
import com.amazonaws.services.directory.model.AuthenticationFailedException;
import com.paladincloud.jobscheduler.auth.CredentialProvider;
import com.paladincloud.jobscheduler.config.ConfigUtil;
import com.paladincloud.jobscheduler.schema.jobs_and_rule_scheduling.Event;
import com.paladincloud.jobscheduler.schema.jobs_and_rule_scheduling.marshaller.Marshaller;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.env.Environment;
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
import java.util.Arrays;
import java.util.List;
import java.util.Set;


@Service
public class JobScheduler {

    private static final Logger logger = LoggerFactory.getLogger(JobScheduler.class);

    private static final String EVENT_SOURCE = "paladincloud.jobs-scheduler";
    private static final String EVENT_DETAIL_TYPE = "Paladin Cloud Job Scheduling Event";
    public static final String AZURE_ENABLED = "azure.enabled";
    public static final String GCP_ENABLED = "gcp.enabled";
    public static final String AWS_ENABLED = "aws.enabled";
    public static final String QUALYS_ENABLED = "qualys.enabled";
    public static final String AQUA_ENABLED = "aqua.enabled";
    public static final String TENABLE_ENABLED = "tenable.enabled";
    public static final String CONTRAST_ENABLED = "contrast.enabled";
    public static final String PLUGIN_TYPE_TENABLE = "tenable";
    public static final String PLUGIN_TYPE_QUALYS = "qualys";
    public static final String PLUGIN_TYPE_AQUA = "aqua";
    public static final String PLUGIN_TYPE_CONTRAST = "contrast";
    public static final String REQUEST_ENTRY = "Request entry: {} ";
    protected static final List<String> ALL_CLOUDS_LIST = Arrays.asList("aws","azure","gcp");
    public static final String FAILED_WITH_ERROR_CODE = "Injection failed with Error Code: {} ";
    public static final String EVENT_ID = "Event Id: {} ";
    public static final String CURRENT_MILLISECONDS = "Current milliseconds: {}";
    public static final String PLUGIN_TYPE_GCP = "gcp";
    public static final String PLUGIN_TYPE_REDHAT = "redhat";


    @Autowired
    CredentialProvider credentialProvider;

    @Value("${aws.eventbridge.bus.details}")
    private String awsBusDetails;

    @Value("${gcp.eventbridge.bus.details}")
    private String gcpBusDetails;

    @Value("${azure.eventbridge.bus.details}")
    private String azureBusDetails;

    @Value("${vulnerability.eventbridge.bus.details}")
    private String vulnerabilityBusDetails;


    private boolean azureEnabled;
    private boolean gcpEnabled;

    private boolean awsEnabled;

    private boolean qualysEnabled;
    private boolean aquaEnabled;

    private boolean tenableEnabled;
    

    @Value("${scheduler.total.batches}")
    private String noOfBatches;

    @Value("${base.region}")
    private String region;

    @Value("${base.account}")
    private String baseAccount;

    @Value("${scheduler.role}")
    private String roleName;
    
    @Value("${plugins.in.v1}")
    private String pluginUsingV1;

    @Value("#{'${composite.plugins:}'.split(',')}")
    private Set<String> compositePlugins;
    
    @Autowired
    private DataCollectorSQSService dataCollectorSQSServic;

    @Autowired
    private Environment env;

    @Scheduled(initialDelayString = "${scheduler.collector.initial.delay}", fixedDelayString = "${scheduler.interval}")
    public void scheduleCollectorJobs() {
        // print the current milliseconds
        logger.info("Current milliseconds: {} ", System.currentTimeMillis());
        logger.info("Job Scheduler for collector is running...");

        EventBridgeClient eventBrClient = getEventBridgeClient();
        List<PutEventsRequestEntry> putEventsRequestEntries = new ArrayList<>();

        try {
            ConfigUtil.setConfigProperties();
            azureEnabled = Boolean.parseBoolean(System.getProperty(AZURE_ENABLED));
            awsEnabled = Boolean.parseBoolean(System.getProperty(AWS_ENABLED));
            boolean compositePluginEnabled = isCompositeEnabled();
            if (!compositePluginEnabled && awsEnabled) {
                addCollectorEvent(putEventsRequestEntries, awsBusDetails);
            }
            if (!compositePluginEnabled && azureEnabled) {
                addCollectorEvent(putEventsRequestEntries, azureBusDetails);
            }
            // Sending SQS message to trigger Data-Collector
            String[] plugins = pluginUsingV1.split(",");
            List<String> configuredPlugins = dataCollectorSQSServic.pulginsUsingVersion1AndConfigured(plugins);
            for (String plugin : configuredPlugins) {
                if (compositePluginEnabled && "gcp".equalsIgnoreCase(plugin)) {
                    continue;
                }
                dataCollectorSQSServic.sendSQSMessage(plugin);
            }

            if (!putEventsRequestEntries.isEmpty()) {
					PutEventsRequest eventsRequest = PutEventsRequest.builder().entries(putEventsRequestEntries)
							.build();

					PutEventsResponse result = eventBrClient.putEvents(eventsRequest);

					for (PutEventsResultEntry resultEntry : result.entries()) {
						if (resultEntry.eventId() != null) {
							logger.info(EVENT_ID, resultEntry.eventId());
						} else {
							logger.info(FAILED_WITH_ERROR_CODE, resultEntry.errorCode());
						}
					}
				}

        } catch (EventBridgeException e) {
            logger.error(e.awsErrorDetails().errorMessage());
            System.exit(1);
        } catch (Exception e) {
            logger.error(e.getMessage());
            System.exit(1);
        }
        eventBrClient.close();
    }

    private void putPluginRuleRequestEntries(int batchNo, String busDetails, List<PutEventsRequestEntry> reqEntryList, String pluginType) {
        String detailString = null;
        //plugin-saasdev-all-clouds:2
        //Generate event for all clouds for plugin
        for(String cloudType:ALL_CLOUDS_LIST) {
            String cloudName = pluginType+"-"+cloudType;
            Event event = populateEventForRule(cloudName, batchNo);
            detailString = getMarshalledEvent(detailString, event);
            PutEventsRequestEntry reqEntry = PutEventsRequestEntry.builder().source(EVENT_SOURCE).detailType(EVENT_DETAIL_TYPE).detail(detailString).eventBusName(busDetails.split(":")[0]).build();
            // print the request entry
            logger.info(REQUEST_ENTRY, reqEntry);

            // Add the PutEventsRequestEntry to a putEventsRequestEntries
            reqEntryList.add(reqEntry);
        }
    }

    private void putPluginRuleRequestEntriesForContrast(int batchNo, String busDetails, List<PutEventsRequestEntry> reqEntryList) {
        String detailString = null;
        Event event = populateEventForRule(JobScheduler.PLUGIN_TYPE_CONTRAST, batchNo);
        detailString = getMarshalledEvent(detailString, event);
        PutEventsRequestEntry reqEntry = PutEventsRequestEntry.builder().source(EVENT_SOURCE).detailType(
                EVENT_DETAIL_TYPE).detail(detailString).eventBusName(busDetails.split(":")[0]).build();
        // print the request entry
        logger.info(REQUEST_ENTRY, reqEntry);
        // Add the PutEventsRequestEntry to a putEventsRequestEntries
        reqEntryList.add(reqEntry);
    }

    @Scheduled(initialDelayString = "${vulnerability.collector.initial.delay}", fixedDelayString = "${vulnerability.interval}")
    public void schedulePluginCollectorJobs() {
        // print the current milliseconds
        logger.info("Current milliseconds: {} ", System.currentTimeMillis());
        logger.info("Job Scheduler for custom plugin is running...");

        EventBridgeClient eventBrClient = getEventBridgeClient();
        List<PutEventsRequestEntry> putEventsRequestEntries = new ArrayList<>();

        try {
            ConfigUtil.setConfigProperties();

            qualysEnabled=Boolean.parseBoolean(System.getProperty(QUALYS_ENABLED));
            aquaEnabled=Boolean.parseBoolean(System.getProperty(AQUA_ENABLED));
            tenableEnabled=Boolean.parseBoolean(System.getProperty(TENABLE_ENABLED));
            if (qualysEnabled) {
                addPluginCollectorEvent(putEventsRequestEntries, vulnerabilityBusDetails, PLUGIN_TYPE_QUALYS);
            }
            if (aquaEnabled) {
                addPluginCollectorEvent(putEventsRequestEntries, vulnerabilityBusDetails, PLUGIN_TYPE_AQUA);
            }
            if (tenableEnabled) {
                addPluginCollectorEvent(putEventsRequestEntries, vulnerabilityBusDetails, PLUGIN_TYPE_TENABLE);
            }

            // check if events to put is > 0
            if (!putEventsRequestEntries.isEmpty()) {
                PutEventsRequest eventsRequest = PutEventsRequest.builder().entries(putEventsRequestEntries).build();
                PutEventsResponse result = eventBrClient.putEvents(eventsRequest);

                for (PutEventsResultEntry resultEntry : result.entries()) {
                    if (resultEntry.eventId() != null) {
                        logger.info(EVENT_ID, resultEntry.eventId());
                    } else {
                        logger.info(FAILED_WITH_ERROR_CODE, resultEntry.errorCode());
                    }
                }
            }
        } catch (EventBridgeException e) {
            logger.error(e.awsErrorDetails().errorMessage());
            System.exit(1);
        } catch (Exception e) {
            logger.error(e.getMessage());
            System.exit(1);
        }
        eventBrClient.close();
    }
    @Scheduled(initialDelayString =  "${vulnerability.shipper.initial.delay}", fixedDelayString = "${vulnerability.interval}")
    public void schedulePluginShipperJobs() {
        // print the current milliseconds
        logger.info(CURRENT_MILLISECONDS, System.currentTimeMillis());
        logger.info("Job Scheduler for plugin shipper is running...");

        EventBridgeClient eventBrClient = getEventBridgeClient();
        List<PutEventsRequestEntry> putEventsRequestEntries = new ArrayList<>();

        try {
            ConfigUtil.setConfigProperties();
            qualysEnabled = Boolean.parseBoolean(System.getProperty(QUALYS_ENABLED));
            if (qualysEnabled) {
                addPluginShipperEvent(putEventsRequestEntries, vulnerabilityBusDetails, PLUGIN_TYPE_QUALYS);
            }
            if (!putEventsRequestEntries.isEmpty()) {
                PutEventsRequest eventsRequest = PutEventsRequest.builder().entries(putEventsRequestEntries).build();

                PutEventsResponse result = eventBrClient.putEvents(eventsRequest);

                for (PutEventsResultEntry resultEntry : result.entries()) {
                    if (resultEntry.eventId() != null) {
                        logger.info(EVENT_ID, resultEntry.eventId());
                    } else {
                        logger.info(FAILED_WITH_ERROR_CODE, resultEntry.errorCode());
                    }
                }
            }
        } catch (EventBridgeException e) {
            logger.error(e.awsErrorDetails().errorMessage());
            System.exit(1);
        } catch (Exception e) {
            logger.error(e.getMessage());
            System.exit(1);
        }
        eventBrClient.close();
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
            logger.info(REQUEST_ENTRY, reqEntry);
        }
    }

    private void addPluginCollectorEvent(List<PutEventsRequestEntry> putEventsRequestEntries, String busDetails, String pluginType) {
        String detailString = null;
        //For custom plugins like qualys, aqua, tenable- "aqua-saasdev-aws_gcp_azure:145"
        // populate events for each event bus
        String[] busDetailsArray = busDetails.split(",");
        for (String busDetail : busDetailsArray) {
            String customBusDetail = busDetail.split(":")[0];
            for(String cloudType:ALL_CLOUDS_LIST){
                String cloudName = pluginType+"-"+cloudType;
                Event event = populateEventForCollector(cloudName);
                detailString = getMarshalledEvent(detailString, event);
                PutEventsRequestEntry reqEntry = PutEventsRequestEntry.builder().source(EVENT_SOURCE).detailType(EVENT_DETAIL_TYPE).detail(detailString).eventBusName(customBusDetail).build();

                // Add the PutEventsRequestEntry to a putEventsRequestEntries
                putEventsRequestEntries.add(reqEntry);

                // print the request entry
                logger.info(REQUEST_ENTRY, reqEntry);
            }
        }
    }
    private void addPluginShipperEvent(List<PutEventsRequestEntry> putEventsRequestEntries, String busDetails, String pluginType) {
        String detailString = null;

        // populate events for each event bus
        String[] busDetailsArray = busDetails.split(",");
        for (String busDetail : busDetailsArray) {
            String customBusDetail = busDetail.split(":")[0];
            for(String cloudType:ALL_CLOUDS_LIST) {
                String cloudName = pluginType+"-"+cloudType;
                Event event = populateEventForShipper(cloudName);

                detailString = getMarshalledEvent(detailString, event);
                PutEventsRequestEntry reqEntry = PutEventsRequestEntry.builder().source(EVENT_SOURCE).detailType(EVENT_DETAIL_TYPE).detail(detailString).eventBusName(customBusDetail).build();

                // Add the PutEventsRequestEntry to a putEventsRequestEntries
                putEventsRequestEntries.add(reqEntry);

                // print the request entry
                logger.info(REQUEST_ENTRY, reqEntry);
            }
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

    public boolean isCompositeEnabled() {
        for (String plugin : compositePlugins) {
            String isEnabledKey = plugin + ".enabled";
            String isEnabledValue = env.getProperty(isEnabledKey);
            if (isEnabledValue != null && (isEnabledValue.equalsIgnoreCase("true") ||
                    isEnabledValue.equalsIgnoreCase("1"))) {
                return true;
            }
        }
        return false;
    }
}
