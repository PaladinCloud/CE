package com.tmobile.pacman.cloud;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import com.tmobile.pacman.cloud.dao.RDSDBManager;
import com.tmobile.pacman.cloud.dto.NotificationBaseRequest;
import com.tmobile.pacman.cloud.es.ElasticSearchRepository;
import com.tmobile.pacman.cloud.exception.DataException;
import com.tmobile.pacman.cloud.util.Constants;
import com.tmobile.pacman.cloud.util.Util;

@Component
public class CloudNotificationDataCollectionOrchestrator {

	/** The log. */
	private static final Logger LOGGER = LoggerFactory.getLogger(CloudNotificationDataCollectionOrchestrator.class);

	/** Clound Notification Query */
	private String cloudTargetQuery = "select * FROM CloudNotification_mapping"; // { "EC2" }; , "DIRECTCONNECT", "RDS",
																					// "LAMBDA", "IAM", "VPN",
																					// "CLOUDFRONT", "S3", "REDSHIFT",
																					// "SQS",
																					// "DYNAMODB", "ELASTICCACHE",
																					// "APIGATEWAY",
																					// "VPC", "KMS", "MQ", "CONFIG",
																					// "CLOUDTRAIL" };

	private static final String CURR_DATE = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZ").format(new java.util.Date());

	/** The es type. */
	private static String ES_NOTIFICATION = "notification";

	private static String EVENT_SOURCE_AWS = "aws";
	private static String EVENT_SOURCE_DISPLAY_AWS = "AWS";
	private static String EVENT_CATEGORY = "eventtypecategory";
	private static String EVENT_NAME = "eventtypecode";

	/**
	 * Orchestrate.
	 * 
	 */
	public void orchestrate() {
		ExecutorService executor = Executors.newCachedThreadPool();
		executor.execute(() -> {
			try {
				dataCollection();
			} catch (Exception e) {
				LOGGER.error("Exception in startDataCollection " + Util.getStackTrace(e));
			}
		});
		executor.shutdown();
		while (!executor.isTerminated()) {
		}
	}

	/**
	 * Instantiates a new datacollection orchestrator.
	 *
	 * dataCollection method will iterate the targettypes and stores the
	 * notifications.
	 * 
	 */
	public void dataCollection() {

		try {
			List<Map<String, Object>> countList = new ArrayList<Map<String, Object>>();
			List<Map<String, String>> cloudMappings = RDSDBManager.executeQuery(cloudTargetQuery);
			List<NotificationBaseRequest> cloudNotificationObjs = new ArrayList<>();

			cloudMappings.parallelStream().forEach(cloudMapping -> {

				LOGGER.info("Started Collection for this Target Type**" + cloudMapping.get(Constants.EVENTTYPE));
				List<Map<String, Object>> phdEvents = new ArrayList<Map<String, Object>>();
				try {
					phdEvents = ElasticSearchRepository.getPhdEvents(cloudMapping.get(Constants.EVENTTYPE));
				} catch (DataException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				phdEvents.forEach(event -> {
					if (event.get(Constants.EVENTARN) != "") {
						try {
							String phdEntity = ElasticSearchRepository
									.getPhdEnityByArn(event.get(Constants.EVENTARN).toString());
							// System.out.println("**Entty**"+phdEntity);
							// Get the resources from pacbot
							NotificationBaseRequest notificationReq = new NotificationBaseRequest();
							String eventName = (String) event.getOrDefault(EVENT_NAME, "");
							notificationReq.setEventName(eventName.replaceAll("_", " "));
							notificationReq.setEventSource(EVENT_SOURCE_AWS);
							notificationReq.setEventSourceName(EVENT_SOURCE_DISPLAY_AWS);
							String category = (String) event.get(EVENT_CATEGORY);
							notificationReq.setEventCategory(category);
							try {
								notificationReq.setEventCategoryName(
										Constants.NotificationTypes.valueOf(category.toUpperCase()));
							} catch (Exception e) {
								LOGGER.error("invalid category type found " + category + " excpetion " + e);
							}

							if (phdEntity != null && !"UNKNOWN".equals(phdEntity) && !"AWS_ACCOUNT".equals(phdEntity)) {

								List<Map<String, Object>> resorceDet = ElasticSearchRepository.getPacResourceDet(
										cloudMapping.get(Constants.ESINDEX), cloudMapping.get(Constants.RESOURCEIDKEY),
										cloudMapping.get(Constants.RESOURCEIDVAL), phdEntity);
								if (!resorceDet.isEmpty()) {
									resorceDet.forEach(details -> {
										LOGGER.info("**Target type**" + cloudMapping.get(Constants.RESOURCEIDKEY));
										LOGGER.info(
												"***pac list**" + details.get(cloudMapping.get(Constants.RESOURCEIDVAL))
														+ "**DOCID***" + details.get(Constants._DOCID));
										countList.add(details);

										event.put(Constants._DOCID, details.get(Constants._DOCID));
										event.put(Constants._RESOURCEID,
												details.get(cloudMapping.get(Constants.RESOURCEIDVAL)));
										// event.put(Constants.LATEST, true);
										event.put(Constants.EVENT_ID,
												details.get(cloudMapping.get(Constants.RESOURCEIDVAL)));
										event.put(Constants.TYPE,
												cloudMapping.get(Constants.ESINDEX).toLowerCase().toString());
										notificationReq.setEventId(
												details.get(cloudMapping.get(Constants.RESOURCEIDVAL)).toString());
										notificationReq.setAssetType(
												cloudMapping.get(Constants.ESINDEX).toLowerCase().toString());
										notificationReq.setAssetTypeName(
												cloudMapping.get(Constants.ESINDEX).toLowerCase().toString());
									});
								} else {

									event.put(Constants._ID, event.get(Constants.ACCOUNTID).toString() + ":"
											+ event.get(Constants.EVENTARN).toString());
									notificationReq.setEventId(event.get(Constants.ACCOUNTID).toString() + ":"
											+ event.get(Constants.EVENTARN).toString());

								}

							} else {

								event.put(Constants._ID, event.get(Constants.ACCOUNTID).toString() + ":"
										+ event.get(Constants.EVENTARN).toString());
								notificationReq.setEventId(event.get(Constants.ACCOUNTID).toString() + ":"
										+ event.get(Constants.EVENTARN).toString());

							}
							notificationReq.setPayload(event);
							cloudNotificationObjs.add(notificationReq);
						} catch (DataException e) {
							LOGGER.error("Error in the cloudNotification" + e.getMessage());
						}
					}
				});
			});

			if (!cloudNotificationObjs.isEmpty()) {
				// outputList.forEach(System.err::println);
				LOGGER.info("UPLOADING SECURITYHUB DATA TO ES");

				Util.pushNotificaiton(System.getProperty(Constants.NOTIFICATION_URL), cloudNotificationObjs);
			}
			LOGGER.info("**PHD with resourceID size**" + countList.size());
		} catch (Exception e) {
			LOGGER.error(" FAILED IN SECURITYHUB DATACOLLECTION JOB {}", Util.getStackTrace(e));
		}
	}
}
