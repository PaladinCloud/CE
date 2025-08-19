package com.tmobile.pacman.api.asset.repository;

import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

import javax.annotation.PostConstruct;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Repository;
import org.springframework.util.CollectionUtils;

import com.google.common.base.Strings;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.tmobile.pacman.api.asset.AssetConstants;
import com.tmobile.pacman.api.commons.Constants;
import com.tmobile.pacman.api.commons.exception.DataException;
import com.tmobile.pacman.api.commons.repo.ElasticSearchRepository;
import com.tmobile.pacman.api.commons.repo.PacmanRdsRepository;
import com.tmobile.pacman.api.commons.utils.CommonUtils;
import com.tmobile.pacman.api.commons.utils.PacHttpUtils;

import static com.tmobile.pacman.api.commons.Constants.ES_PAGE_SIZE;

/**
 * Implemented class for CloudNotificationsRepository and all its method
 */
@Repository
public class CloudNotificationsRepositoryImpl implements CloudNotificationsRepository {

	@Autowired
	ElasticSearchRepository esRepository;

	@Autowired
	PacmanRdsRepository rdsRepository;

	private static final Log LOGGER = LogFactory.getLog(CloudNotificationsRepositoryImpl.class);
	private static final String _SEARCH = "_search";
	private static final String HITS = "hits";
	private static final String ERROR_IN_US = "error retrieving inventory from ES";
	private static final String DATE_FORMAT = "yyyy-MM-dd HH:mm:ss";
	private final int  ES_PAGE_SIZE=10000;
	@Value("${elastic-search.host}")
	private String esHost;
	@Value("${elastic-search.port}")
	private int esPort;
	private String TYPE = "notification";
	private String INDEX = "notification";
	private String AUTOFIXTYPE = "autofixplan";
	final static String protocol = "http";
	final static String START_TIME = "startTime";
	final static String CLOUD_NOTIFICATIONS = "cloud_notifications";
	final static String GLOBAL_NOTIFICATION_COUNT = "globalNotificationsCount";
	final static String EVENT_ISSUES_COUNT = "evnetIssuesCount";
	final static String EVENT_SCHEDULED_COUNT = "eventscheduledCount";
	final static String EVENT_NOTIFICATION_COUNT = "eventNotificationCount";
	private String esUrl;
	private static final String _SOURCE = "_source";
	private static final String _COUNT = "_count";
	private static final String ESQUERY_RANGE = ",{ \"range\": {\"_loaddate.keyword\": {";
	private static final String ESQUERY_RANGE_CLOSE = "}}}";
	List<Map<String, Object>> notifications = new ArrayList<>();
	String autoFixQuery = "";

	@PostConstruct
	void init() {
		esUrl = protocol + "://" + esHost + ":" + esPort;
	}

	@Override
	public List<Map<String, Object>> getNotifications(String assetGroup, Map<String,List<String>>filter, int size,
													  int from, Map<String,Object> sortFilter, Date startDate, Date endDate) {
		LOGGER.info("Inside getNotifications");
		notifications = new ArrayList<>();
		try {

			getCloudNotifications(INDEX, TYPE, filter, size, from,sortFilter,startDate,endDate).forEach(notification -> {
				notifications.add(notification);
			});

		} catch (Exception e) {
			LOGGER.error("Error in getNotifications", e);
		}

		LOGGER.info("Exiting getNotifications");
		return notifications.stream().distinct().collect(Collectors.toList());
	}

	@Override
	public Map<String, Object> getCloudNotificationDetail(String eventId, String assetGroup) {
		LOGGER.info("Inside getCloudNotificationDetail");
		Map<String, Object> detail = new HashMap<>();
		try {

			detail = getCloudNotificationDetail(INDEX, TYPE, eventId);

		} catch (Exception e) {
			LOGGER.error("Error in getCloudNotificationDetail", e);
		}
		LOGGER.info("Exiting getCloudNotificationDetail");
		return detail;
	}

	@SuppressWarnings({ "deprecation" })
	private List<Map<String, Object>> getAssetsByResourceId(String assetGroupName, String type, String resourceId) {

		List<Map<String, Object>> results = new ArrayList<>();
		String query = "SELECT esIndex, resourceIdVal FROM CloudNotification_mapping WHERE esIndex =\"" + type + "\"";
		try {
			results = rdsRepository.getDataFromPacman(query);
		} catch (Exception exception) {
			LOGGER.error("Error in getAssetsByResourceId for getting parent type ", exception);
		}
		Map<String, Object> mustFilter = new HashMap<>();
		mustFilter.put(CommonUtils.convertAttributetoKeyword((results.get(0).get("resourceIdVal")).toString()),
				resourceId);
		mustFilter.put(Constants.LATEST, Constants.TRUE);
		mustFilter.put(AssetConstants.UNDERSCORE_ENTITY, Constants.TRUE);
		Map<String, Object> mustNotFilter = null;

		List<Map<String, Object>> assets = new ArrayList<>();
		try {
			if (AssetConstants.ALL.equals(type)) {
				try {
					Map<String, Object> mustTermsFilter = new HashMap<>();
					assets = esRepository.getDataFromES(assetGroupName, null, mustFilter, null, null, null,
							mustTermsFilter);
				} catch (Exception e) {
					LOGGER.error(AssetConstants.ERROR_GETASSETSBYAG, e);
				}
			} else {
				assets = esRepository.getDataFromES(assetGroupName, (results.get(0).get("esIndex")).toString(),
						mustFilter, mustNotFilter, null, null, null);
			}
		} catch (Exception e) {
			LOGGER.error(AssetConstants.ERROR_GETASSETSBYAG, e);
		}
		return assets;
	}

	@SuppressWarnings("unchecked")
	public Map<String, Object> getCloudNotificationDetail(String index, String type, String eventId)
			throws DataException {
		Gson gson = new GsonBuilder().create();
		String responseDetails = null;
		StringBuilder requestBody = null;
		List<Map<String, Object>> cloudDetails = null;
		List<String> fieldNames = new ArrayList<>();
		List<String> fieldsToBeSkipped = Arrays.asList(Constants.RESOURCEID, Constants.DOCID,
				AssetConstants.UNDERSCORE_ENTITY, Constants._ID,AssetConstants.UNDERSCORE_LOADDATE, 
				Constants.ES_DOC_PARENT_KEY, Constants.ES_DOC_ROUTING_KEY, AssetConstants.CREATE_TIME,
				AssetConstants.FIRST_DISCOVEREDON, AssetConstants.DISCOVERY_DATE, Constants.LATEST,
				AssetConstants.CREATION_DATE);
		StringBuilder urlToQueryBuffer = new StringBuilder(esUrl).append("/").append(index).append("/").append(_SEARCH);
		String body = "{\"_source\": [\"eventID\",\"eventName\",\"eventCategory\", \"eventCategoryName\", \"eventSource\",\"eventSourceName\",\"eventDescription\",\"_loaddate\",\"payload\"], \"query\":{\"bool\":{\"must\":[{\"term\":{\"eventId.keyword\":\""
				+ eventId + "\"}},{\"term\":{\"latest\":\"true\"}}]}}}";
		requestBody = new StringBuilder(body);
		try {
			responseDetails = PacHttpUtils.doHttpPost(urlToQueryBuffer.toString(), requestBody.toString());
		} catch (Exception e) {
			LOGGER.error(ERROR_IN_US, e);
			throw new DataException(e);
		}
		Map<String, Object> responseMap = (Map<String, Object>) gson.fromJson(responseDetails, Object.class);
		if (responseMap.containsKey(HITS)) {
			Map<String, Object> hits = (Map<String, Object>) responseMap.get(HITS);
			if (hits.containsKey(HITS)) {
				cloudDetails = (List<Map<String, Object>>) hits.get(HITS);
				if (cloudDetails.size() > 0) {
					Map<String, Object> cloudDetail = cloudDetails.get(0);
					Map<String, Object> sourceMap = (Map<String, Object>) cloudDetail.get("_source");
					return formGetListResponse(fieldNames, sourceMap, fieldsToBeSkipped);
				}
			}
		}
		return new HashMap<>();

	}

	private Map<String, Object> formGetListResponse(List<String> fieldNames, Map<String, Object> assetDetail,
			List<String> fieldsToBeSkipped) {
		if (!CollectionUtils.isEmpty(fieldNames)) {
			final List<String> fieldNamesCopy = fieldNames;

			Map<String, Object> asset = new LinkedHashMap<>();
			for (String fieldName : fieldNamesCopy) {
				if (!assetDetail.containsKey(fieldName)) {
					asset.put(fieldName, "");
				} else {
					asset.put(fieldName, assetDetail.get(fieldName));
				}
			}

			return asset;
		} else {
			Map<String, Object> asset = new LinkedHashMap<>();
			asset.put(START_TIME, assetDetail.get(AssetConstants.UNDERSCORE_LOADDATE));
			assetDetail.forEach((key, value) -> {
				if (!fieldsToBeSkipped.contains(key)) {
					asset.put(key, value);
				}
			});

			return asset;
		}
	}

	@Override
	@SuppressWarnings("unchecked")
	public Map<String, Object> getCloudNotificationInfo(String eventArn, boolean globalNotifier, String assetGroup) {
		Map<String, Object> eventMap = new HashMap<>();
		eventMap.put("scheduledChange", "Scheduled Change");
		eventMap.put("accountNotification", "Account Notification");
		eventMap.put("issue", "Issue");

		String index = "";
		String type = "";
		if (globalNotifier) {
			index = INDEX;
			type = TYPE;
		} else {
			index = assetGroup;
			type = TYPE;
		}
		Gson gson = new GsonBuilder().create();
		String responseDetails = null;
		StringBuilder requestBody = null;
		List<Map<String, Object>> cloudDetails = null;
		StringBuilder urlToQueryBuffer = new StringBuilder(esUrl).append("/").append(index).append("/").append(type)
				.append("/").append(_SEARCH);

		String body = "{\"_source\": [\"eventarn\",\"endtime\",\"eventtypecategory\",\"_loaddate\",\"statuscode\",\"eventtypecode\",\"eventregion\",\"latestdescription\", \"type\"], \"query\":{\"bool\":{\"must\":[{\"term\":{\"eventarn.keyword\":\""
				+ eventArn + "\"}},{\"term\":{\"latest\":\"true\"}}]}}}";
		requestBody = new StringBuilder(body);
		try {
			responseDetails = PacHttpUtils.doHttpPost(urlToQueryBuffer.toString(), requestBody.toString());
		} catch (Exception e) {
			LOGGER.error(ERROR_IN_US, e);
			try {
				throw new DataException(e);
			} catch (DataException e1) {
				LOGGER.error("ERROR in getCloudNotificationInfo ", e1);
			}
		}
		Map<String, Object> responseMap = (Map<String, Object>) gson.fromJson(responseDetails, Object.class);
		Map<String, Object> infoMap = new HashMap<String, Object>();
		if (responseMap.containsKey(HITS)) {
			Map<String, Object> hits = (Map<String, Object>) responseMap.get(HITS);
			if (hits.containsKey(HITS)) {
				cloudDetails = (List<Map<String, Object>>) hits.get(HITS);
				for (Map<String, Object> cloudDetail : cloudDetails) {
					Map<String, Object> sourceMap = (Map<String, Object>) cloudDetail.get("_source");
					infoMap.put("event", CommonUtils.capitailizeWord(sourceMap.get("eventtypecode").toString()));
					infoMap.put("status", sourceMap.get("statuscode"));
					infoMap.put("region", sourceMap.get("eventregion"));
					infoMap.put("startTime", sourceMap.get("_loaddate"));
					infoMap.put("endTime", sourceMap.get("endtime"));
					infoMap.put("eventCategory", eventMap.get(sourceMap.get("eventtypecategory")));
					infoMap.put("eventarn", sourceMap.get("eventarn"));
					infoMap.put("latestdescription", eventDescChanges(sourceMap.get("latestdescription").toString()));
				}
			}
		}
		return infoMap;
	}

	private String eventDescChanges(String description) {

		description = description.replace("*", "<h6>*");
		description = description.replace("?[NL]", "?</h6>");
		if (description.indexOf("https://console.aws.amazon.com/ec2/v2/home?region=us-west-2#Events") > 0) {
			description = description.replace("https://console.aws.amazon.com/ec2/v2/home?region=us-west-2#Events",
					"<a href =\"https://console.aws.amazon.com/ec2/v2/home?region=us-west-2#Events\" target=\"_blank\">https://console.aws.amazon.com/ec2/v2/home?region=us-west-2#Events</a>");
		}
		if (description.indexOf(
				"https://docs.aws.amazon.com/AWSEC2/latest/UserGuide/monitoring-instances-status-check_sched.html#schedevents_actions_reboot") > 0) {
			description = description.replace(
					"https://docs.aws.amazon.com/AWSEC2/latest/UserGuide/monitoring-instances-status-check_sched.html#schedevents_actions_reboot",
					"<a href =\"https://docs.aws.amazon.com/AWSEC2/latest/UserGuide/monitoring-instances-status-check_sched.html#schedevents_actions_reboot\" target=\"_blank\">https://docs.aws.amazon.com/AWSEC2/latest/UserGuide/monitoring-instances-status-check_sched.html#schedevents_actions_reboot</a>");
		}
		if (description.indexOf(
				"at https://docs.aws.amazon.com/AWSEC2/latest/UserGuide/monitoring-instances-status-check_sched.html") > 0) {
			description = description.replace(
					"at https://docs.aws.amazon.com/AWSEC2/latest/UserGuide/monitoring-instances-status-check_sched.html",
					"<a href =\"https://docs.aws.amazon.com/AWSEC2/latest/UserGuide/monitoring-instances-status-check_sched.html\" target=\"_blank\">https://docs.aws.amazon.com/AWSEC2/latest/UserGuide/monitoring-instances-status-check_sched.html</a>");
		}
		if (description.indexOf("https://docs.aws.amazon.com/AWSEC2/latest/UserGuide/Stop_Start.html") > 0) {
			description = description.replace("https://docs.aws.amazon.com/AWSEC2/latest/UserGuide/Stop_Start.html",
					"<a href =\"https://docs.aws.amazon.com/AWSEC2/latest/UserGuide/Stop_Start.html\" target=\"_blank\">https://docs.aws.amazon.com/AWSEC2/latest/UserGuide/Stop_Start.html</a>");
		}
		if (description.indexOf("https://console.aws.amazon.com/ec2/v2/home?region=us-east-1#Events") > 0) {
			description = description.replace("https://console.aws.amazon.com/ec2/v2/home?region=us-east-1#Events",
					"<a href =\"https://console.aws.amazon.com/ec2/v2/home?region=us-east-1#Events\" target=\"_blank\">https://console.aws.amazon.com/ec2/v2/home?region=us-east-1#Events</a>");
		}
		if (description.indexOf("[3] https://github.com/awslabs/aws-vpn-migration-scripts") > 0) {
			description = description.replace("[3] https://github.com/awslabs/aws-vpn-migration-scripts",
					"<a href =\"https://github.com/awslabs/aws-vpn-migration-scripts\" target=\"_blank\">https://github.com/awslabs/aws-vpn-migration-scripts</a>");
		}
		if (description.indexOf("http://docs.aws.amazon.com/AWSEC2/latest/UserGuide/instance-retirement.html") > 0) {
			description = description.replace(
					" http://docs.aws.amazon.com/AWSEC2/latest/UserGuide/instance-retirement.html",
					"<a href =\" http://docs.aws.amazon.com/AWSEC2/latest/UserGuide/instance-retirement.html\" target=\"_blank\">http://docs.aws.amazon.com/AWSEC2/latest/UserGuide/instance-retirement.html</a>");
		}
		if (description.indexOf("(https://aws.amazon.com/support)") > 0) {
			description = description.replace("(https://aws.amazon.com/support)",
					"<a href =\"https://aws.amazon.com/support\" target=\"_blank\">https://aws.amazon.com/support</a>");
		}
		if (description.indexOf("(http://aws.amazon.com/support)") > 0) {
			description = description.replace("(http://aws.amazon.com/support)",
					"<a href =\"http://aws.amazon.com/support\" target=\"_blank\">http://aws.amazon.com/support</a>");
		}
		if (description.indexOf("http://aws.amazon.com/support") > 0) {
			description = description.replace("http://aws.amazon.com/support",
					"<a href =\"http://aws.amazon.com/support\" target=\"_blank\">http://aws.amazon.com/support</a>");
		}
		if (description.indexOf("[1] https://console.aws.amazon.com") > 0) {
			description = description.replace("[1] https://console.aws.amazon.com",
					"<a href =\"https://console.aws.amazon.com\" target=\"_blank\">https://console.aws.amazon.com</a>");
		}
		if (description.indexOf("http://aws.amazon.com/architecture") > 0) {
			description = description.replace("http://aws.amazon.com/architecture",
					"<a href =\"http://aws.amazon.com/architecture\" target=\"_blank\">http://aws.amazon.com/architecture</a>");
		}
		if (description.indexOf("https://aws.amazon.com/support") > 0) {
			description = description.replace("https://aws.amazon.com/support",
					"<a href =\"https://aws.amazon.com/support\" target=\"_blank\">https://aws.amazon.com/support</a>");
		}
		if (description.indexOf("https://aws.amazon.com/premiumsupport/knowledge-center/migrate-classic-vpn-new") > 0) {
			description = description.replace(
					"https://aws.amazon.com/premiumsupport/knowledge-center/migrate-classic-vpn-new",
					"<a href =\"https://aws.amazon.com/premiumsupport/knowledge-center/migrate-classic-vpn-new\" target=\"_blank\">https://aws.amazon.com/premiumsupport/knowledge-center/migrate-classic-vpn-new</a>");
		}
		if (description.indexOf(
				"https://docs.aws.amazon.com/amazon-mq/latest/developer-guide/using-amazon-mq-securely.html#amazon-mq-vpc-security-groups") > 0) {
			description = description.replace(
					"https://docs.aws.amazon.com/amazon-mq/latest/developer-guide/using-amazon-mq-securely.html#amazon-mq-vpc-security-groups",
					"<a href =\"https://docs.aws.amazon.com/amazon-mq/latest/developer-guide/using-amazon-mq-securely.html#amazon-mq-vpc-security-groups\" target=\"_blank\">https://docs.aws.amazon.com/amazon-mq/latest/developer-guide/using-amazon-mq-securely.html#amazon-mq-vpc-security-groups</a>");
		}
		if (description.indexOf(
				"[3] http://activemq.apache.org/security-advisories.data/CVE-2019-0222-announcement.txt") > 0) {
			description = description.replace(
					"[3] http://activemq.apache.org/security-advisories.data/CVE-2019-0222-announcement.txt",
					"<a href =\"http://activemq.apache.org/security-advisories.data/CVE-2019-0222-announcement.txt\" target=\"_blank\">http://activemq.apache.org/security-advisories.data/CVE-2019-0222-announcement.txt</a>");
		}
		if (description.indexOf(
				"https://docs.aws.amazon.com/amazon-mq/latest/developer-guide/amazon-mq-editing-broker-preferences.html") > 0) {
			description = description.replace(
					"https://docs.aws.amazon.com/amazon-mq/latest/developer-guide/amazon-mq-editing-broker-preferences.html",
					"<a href =\"https://docs.aws.amazon.com/amazon-mq/latest/developer-guide/amazon-mq-editing-broker-preferences.html\" target=\"_blank\">https://docs.aws.amazon.com/amazon-mq/latest/developer-guide/amazon-mq-editing-broker-preferences.html</a>");
		}
		if (description.indexOf("https://docs.aws.amazon.com/vpn/latest/s2svpn/VPC_VPN.html#vpn-categories") > 0) {
			description = description.replace(
					"https://docs.aws.amazon.com/vpn/latest/s2svpn/VPC_VPN.html#vpn-categories",
					"<a href =\"https://docs.aws.amazon.com/vpn/latest/s2svpn/VPC_VPN.html#vpn-categories\" target=\"_blank\">https://docs.aws.amazon.com/vpn/latest/s2svpn/VPC_VPN.html#vpn-categories</a>");
		}
		if (description.indexOf("https://nodejs.org/en/blog/release/v6.9.0/") > 0) {
			description = description.replace("https://nodejs.org/en/blog/release/v6.9.0/",
					"<a href =\"https://nodejs.org/en/blog/release/v6.9.0\" target=\"_blank\">https://nodejs.org/en/blog/release/v6.9.0/</a>");
		}
		if (description.indexOf("https://docs.aws.amazon.com/lambda/latest/dg/runtime-support-policy.html") > 0) {
			description = description.replace(
					"https://docs.aws.amazon.com/lambda/latest/dg/runtime-support-policy.html",
					"<a href =\"https://docs.aws.amazon.com/lambda/latest/dg/runtime-support-policy.html\" target=\"_blank\">https://docs.aws.amazon.com/lambda/latest/dg/runtime-support-policy.html</a>");
		}
		if (description.indexOf(
				"https://aws.amazon.com/blogs/compute/node-js-8-10-runtime-now-available-in-aws-lambda/") > 0) {
			description = description.replace(
					"https://aws.amazon.com/blogs/compute/node-js-8-10-runtime-now-available-in-aws-lambda/",
					"<a href =\"https://aws.amazon.com/blogs/compute/node-js-8-10-runtime-now-available-in-aws-lambda\" target=\"_blank\">https://aws.amazon.com/blogs/compute/node-js-8-10-runtime-now-available-in-aws-lambda</a>");
		}
		if (description.indexOf(
				"https://aws.amazon.com/blogs/compute/upcoming-updates-to-the-aws-lambda-execution-environment/") > 0) {
			description = description.replace(
					"https://aws.amazon.com/blogs/compute/upcoming-updates-to-the-aws-lambda-execution-environment/",
					"<a href =\"https://aws.amazon.com/blogs/compute/upcoming-updates-to-the-aws-lambda-execution-environment\" target=\"_blank\">https://aws.amazon.com/blogs/compute/upcoming-updates-to-the-aws-lambda-execution-environment</a>");
		}
		if (description.indexOf(
				"https://aws.amazon.com/blogs/compute/updated-timeframe-for-the-upcoming-aws-lambda-and-aws-lambdaedge-execution-environment-update/") > 0) {
			description = description.replace(
					"https://aws.amazon.com/blogs/compute/updated-timeframe-for-the-upcoming-aws-lambda-and-aws-lambdaedge-execution-environment-update/",
					"<a href =\"https://aws.amazon.com/blogs/compute/updated-timeframe-for-the-upcoming-aws-lambda-and-aws-lambdaedge-execution-environment-update\" target=\"_blank\">https://aws.amazon.com/blogs/compute/updated-timeframe-for-the-upcoming-aws-lambda-and-aws-lambdaedge-execution-environment-update</a>");
		}

		description = description.replace("[1]", "").replace("[2]", "").replace("[3]", "").replace("[4]", "")
				.replace("[NL][NL]", "\n\n").replace("(https:", "<a href=\"https:").replace("[NL]", "\n")
				.replace(" [3]", "");
		return description;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.tmobile.pacman.api.compliance.repository.
	 * CloudNotificationsRepositoryImpl# getGlobalNotifications(java.lang.String)
	 */
	@SuppressWarnings("unchecked")
	public List<Map<String, Object>> getCloudNotifications(String index, String type, Map<String,List<String>>filter,
			int size, int from,Map<String,Object> sortFilter, Date startDate, Date endDate) throws DataException {
		Map<String, Object> eventMap = new HashMap<>();
		int docSize=0;
		eventMap.put("scheduledChange", "Scheduled Change");
		eventMap.put("accountNotification", "Account Notification");
		eventMap.put("issue", "Issue");
		List<Map<String, Object>> notificationsList = new ArrayList<Map<String, Object>>();
		try {
			String body = "";
			String eventCategory = filterkey(filter, AssetConstants.EVENTCATEGORY);

			String eventSource = filterkey(filter, AssetConstants.EVENTSOURCE);

			String eventName = filterkey(filter, AssetConstants.EVENTNAME);
			Map<String, Object> requestBody = new HashMap<String, Object>();
			body = "{\"size\":<size>,\"_source\":[\"eventId\",\"eventName\",\"eventCategory\",\"eventCategoryName\",\"eventSource\",\"eventSourceName\",\"_loaddate\"],"
					+ "\"query\":{\"bool\":{\"must\":[{\"term\":{\"latest\":\"true\"}},{\"term\":{\"docType.keyword\":\"notification\"}}";
			if (!Strings.isNullOrEmpty(eventSource)) {
				body = body + ",{\"terms\":{\"eventSourceName.keyword\":" + eventSource + "}}";
			}
			if (!Strings.isNullOrEmpty(eventCategory)) {
				body = body + ",{\"terms\":{\"eventCategoryName.keyword\":" + eventCategory + "}}";
			}
			if (!Strings.isNullOrEmpty(eventName)) {
				body = body + ",{\"terms\":{\"eventName.keyword\":" + eventName + "}}";
			}

			/*
			gte stands for Greater than or equal to
			 */
			String gte = null;

			/*
			lte stands for Less than or equal to
			 */
			String lte = null;

			if ( startDate!= null) {
				gte = "\"gte\": \"" + new SimpleDateFormat(DATE_FORMAT).format(startDate) + "\"";
			}
			if ( endDate != null) {
				lte = "\"lte\": \"" + new SimpleDateFormat(DATE_FORMAT).format(endDate) + "\"";
			}

			if (gte != null && lte != null) {
				body= body + (ESQUERY_RANGE + gte + "," + lte + ESQUERY_RANGE_CLOSE);
			} else if (gte != null) {
				body= body +(ESQUERY_RANGE + gte + ESQUERY_RANGE_CLOSE);
			} else if(lte != null) {
				body= body + (ESQUERY_RANGE + lte + ESQUERY_RANGE_CLOSE);
			}

			body = body + "]}},";

			List<Map<String, Object>> list = new ArrayList<Map<String, Object>>();
			if(null != sortFilter && !sortFilter.isEmpty()) {
				Map<String, Object> sortMap = new HashMap<>();
				String fieldName = (String) sortFilter.get("fieldName");
				if(fieldName!=null) {
					String sortOrder = sortFilter.get("order") != null ? (String) sortFilter.get("order") : "asc";
					Map<String, Object> sortOrderMap = new HashMap<>();
					sortOrderMap.put("order", sortOrder);
					sortMap.put(fieldName, sortOrderMap);
					list.add(sortMap);
				}
			}
			else if(null == sortFilter || sortFilter.isEmpty() || !sortFilter.get("fieldName").toString().equals("_loaddate.keyword"))
			{
				Map<String, Object> sortMap = new HashMap<>();
				Map<String, Object> sortOrderMap = new HashMap<>();
				sortOrderMap.put("order", "desc");
				sortMap.put("_loaddate.keyword", sortOrderMap);
				list.add(sortMap);
			}
			if (!list.isEmpty()) {
				requestBody.put("sort", list);
			}
			Gson serializer = new GsonBuilder().create();
			String request = serializer.toJson(requestBody).substring(1);
			body=body+request;
			String requestForCount = body.replace("\"size\":<size>,","");
			requestForCount=requestForCount.replace("\"_source\":[\"eventId\",\"eventName\",\"eventCategory\",\"eventCategoryName\",\"eventSource\",\"eventSourceName\",\"_loaddate\"],","");
			requestForCount = requestForCount.replace(","+request.substring(0,request.length()-1),"");
			if((from+size)<ES_PAGE_SIZE)
				body=body.replace("<size>",String.valueOf(size+from));
			else
				body=body.replace("<size>",String.valueOf(ES_PAGE_SIZE));

			notificationsList=esRepository.fetchCloudNotifications(index,"",body,requestForCount,from,size,docSize);
		} catch (Exception e) {
			LOGGER.error("Error in getCloudNotifications", e);
		}
		return notificationsList.stream().distinct().collect(Collectors.toList());
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.tmobile.pacman.api.compliance.repository.
	 * CloudNotificationsRepositoryImpl# getGlobalNotifications(java.lang.String)
	 */
	@SuppressWarnings("unchecked")
	public Map<String, Object> getCloudEventArn(String index, String type, int size, int from) throws DataException {

		Gson gson = new GsonBuilder().create();
		String responseDetails = null;
		StringBuilder requestBody = null;
		StringBuilder urlToQueryBuffer = new StringBuilder(esUrl).append("/").append(index).append("/").append(_SEARCH);

		String body = "{\"query\":{\"bool\":{\"must\":[{ \"term\": { \"docType\": \"<docType>\" } },{\"match\":{\"latest\":true}}]}},\"aggs\":{\"name\":{\"terms\":{\"field\":\"eventId.keyword\",\"size\":1000}}},"
				+ "\"sort\":[{\"_loaddate.keyword\":{\"order\":\"desc\"}}]}";
		body = body.replace("<docType>", type);
		requestBody = new StringBuilder(body);
		try {
			responseDetails = PacHttpUtils.doHttpPost(urlToQueryBuffer.toString(), requestBody.toString());
		} catch (Exception e) {
			LOGGER.error(ERROR_IN_US, e);
			throw new DataException(e);
		}
		Map<String, Object> response = (Map<String, Object>) gson.fromJson(responseDetails, Map.class);
		Map<String, Object> aggregations = (Map<String, Object>) response.get(Constants.AGGREGATIONS);
		Map<String, Object> name = (Map<String, Object>) aggregations.get("name");
		List<Map<String, Object>> buckets = (List<Map<String, Object>>) name.get(Constants.BUCKETS);

		return buckets.parallelStream().filter(buket -> buket.get("doc_count") != null)
				.collect(Collectors.toMap(buket -> buket.get("key").toString(), buket -> buket.get("doc_count"),
						(oldValue, newValue) -> newValue));
	}

	@SuppressWarnings("unchecked")
	private long getTotalDocCount(String index, String requestBody) {
		StringBuilder urlToQuery = new StringBuilder(esUrl).append("/").append(index).append("/").append(_SEARCH);
		String responseDetails = null;
		Gson gson = new GsonBuilder().create();
		try {
			responseDetails = PacHttpUtils.doHttpPost(urlToQuery.toString(), requestBody);
			Map<String, Object> response = (Map<String, Object>) gson.fromJson(responseDetails, Map.class);
			Map<String, Object> aggregations = (Map<String, Object>) response.get(Constants.AGGREGATIONS);
			Map<String, Object> name = (Map<String, Object>) aggregations.get("name");
			List<Map<String, Object>> buckets = (List<Map<String, Object>>) name.get(Constants.BUCKETS);
			return (long) (buckets.size());
		} catch (Exception e) {
			LOGGER.error("Error in getTotalDocCount", e);
			return 0;
		}
	}

	/**
	 * Gets the type count.
	 *
	 * @param indexName the index name
	 * @return the type count
	 */
	private int getAutoFixSummary(String indexName, String request) {

		StringBuilder urlToQuery = new StringBuilder(esUrl).append("/").append(indexName).append("/").append(_COUNT).append("?filter_path=count");
		String responseDetails = null;
		try {
			responseDetails = PacHttpUtils.doHttpPost(urlToQuery.toString(), request);
			JsonParser jsonParser = new JsonParser();
			JsonObject resultJson = (JsonObject) jsonParser.parse(responseDetails);
			return resultJson.get("count").getAsInt();
		} catch (Exception e) {
			LOGGER.error("Error in getTotalDocCount", e);
			return 0;
		}
	}

	@SuppressWarnings("unchecked")
	public List<Map<String, Object>> getAutofixProjections(String index, String type, Map<String, String> filter)
			throws DataException {
		List<Map<String, Object>> autofixPlanList = new ArrayList<Map<String, Object>>();
		try {
			StringBuilder urlToQuery = new StringBuilder(esUrl).append("/").append(index).append("/").append(_SEARCH);
			String body = "";
			body = "{\"size\":10000,\"query\": {\"bool\": {\"must\": [ { \"term\": { \"docType\": \"<type>\" } } ] }} ,\"_source\":[\"docId\",\"planItems\",\"policyId\",\"issueId\",\"resourceId\",\"resourceType\"]}";
			body = body.replace("<type>", type);
			Gson gson = new GsonBuilder().create();
			String responseDetails = null;
			try {
				responseDetails = PacHttpUtils.doHttpPost(urlToQuery.toString(), new StringBuilder(body).toString());
			} catch (Exception e) {
				LOGGER.error("Error in getAutofixProjections", e);
			}
			Map<String, Object> response = (Map<String, Object>) gson.fromJson(responseDetails, Object.class);
			if (response.containsKey(HITS)) {
				Map<String, Object> hits = (Map<String, Object>) response.get(HITS);
				if (hits.containsKey(HITS)) {
					List<Map<String, Object>> hitDetails = (List<Map<String, Object>>) hits.get(HITS);
					if (!hitDetails.isEmpty()) {
						for (Map<String, Object> hitDetail : hitDetails) {
							Map<String, Object> sources = (Map<String, Object>) hitDetail.get(_SOURCE);
							Map<String, Object> notifcation = new LinkedHashMap<String, Object>();
							notifcation.put("event", "Aws " + sources.get("resourceType") + " Autofix");
							notifcation.put("eventCategory", "Autofix");
							notifcation.put("eventarn", sources.get("resourceId"));
							Object planStatus = sources.get("planStatus");
							List<Map<String, Object>> planitems = (List<Map<String, Object>>) sources.get("planItems");

							if (planitems != null && !planitems.isEmpty()) {

								notifcation.put("startTime", planitems.get(0).get("plannedActionTime"));
								notifcation.put("endTime",
										planitems.get(planitems.size() - 1).get("plannedActionTime"));

								if (planStatus == null) {
									planStatus = planitems.get(planitems.size() - 1).get("status");
								}

							}
							if (planStatus != null) {
								notifcation.put("status", planStatus.toString().toLowerCase());

							} else {
								notifcation.put("status", "unknown");
							}
							notifcation.put("affectedResources", 1);
							autofixPlanList.add(notifcation);
						}
					}
				}
			}
			Comparator<Map<String, Object>> comp = (m1, m2) -> LocalDate
					.parse(m2.get("endTime").toString().substring(0, 10), DateTimeFormatter.ISO_DATE).compareTo(
							LocalDate.parse(m1.get("endTime").toString().substring(0, 10), DateTimeFormatter.ISO_DATE));
			Collections.sort(autofixPlanList, comp);
		} catch (Exception e) {
			LOGGER.error("Error in getAutofixProjections", e);
		}
		return autofixPlanList;
	}

	@SuppressWarnings("unchecked")
	public Map<String, Object> getAutofixProjectionDetail(String ag, Map<String, String> filter) {
		Map<String, Object> autofixPlanDet = new LinkedHashMap<String, Object>();
		autoFixQuery = "";
		try {
			StringBuilder urlToQuery = new StringBuilder(esUrl).append("/").append(ag).append("/").append(_SEARCH);
			filter.entrySet().forEach(autofix -> {
				autoFixQuery = "{\"size\":1,\"_source\":[\"docId\",\"planItems\",\"policyId\",\"issueId\",\"resourceId\",\"resourceType\"],"
						+ "\"query\":{\"bool\":{\"must\":[{\"match\":{\"docType.keyword\":\"autofixplan\"}},"
						+ "{\"match\":{\""+ autofix.getKey() +".keyword\":\""+ autofix.getValue() + "\"}}]}}}";
			});
			Gson gson = new GsonBuilder().create();
			String responseDetails = null;
			try {
				responseDetails = PacHttpUtils.doHttpPost(urlToQuery.toString(),
						new StringBuilder(autoFixQuery).toString());
			} catch (Exception e) {
				LOGGER.error("Error in getAutofixProjectionDetail", e);
			}
			Map<String, Object> response = (Map<String, Object>) gson.fromJson(responseDetails, Object.class);
			if (response.containsKey(HITS)) {
				Map<String, Object> hits = (Map<String, Object>) response.get(HITS);
				if (hits.containsKey(HITS)) {
					List<Map<String, Object>> hitDetails = (List<Map<String, Object>>) hits.get(HITS);
					if (!hitDetails.isEmpty()) {
						for (Map<String, Object> hitDetail : hitDetails) {
							Map<String, Object> sources = (Map<String, Object>) hitDetail.get(_SOURCE);
							autofixPlanDet.put("event", "Aws " + sources.get("resourceType") + " Autofix");

							autofixPlanDet.put("eventCategory", "Autofix");
							autofixPlanDet.put("eventarn", sources.get("resourceId"));
							autofixPlanDet.put("planItems", sources.get("planItems"));
							autofixPlanDet.put("issueId", sources.get("issueId"));
							autofixPlanDet.put("resourceId", sources.get("resourceId"));
							autofixPlanDet.put("policyId", sources.get("policyId"));
							autofixPlanDet.put("resourceType", sources.get("resourceType"));
							List<Map<String, Object>> planitems = (List<Map<String, Object>>) sources.get("planItems");
							if (planitems != null && planitems.size() > 0) {
								planitems.get(0).entrySet().forEach(item -> {
									if ("plannedActionTime".equalsIgnoreCase(item.getKey())) {
										autofixPlanDet.put("startTime", item.getValue());
									}
									if ("status".equalsIgnoreCase(item.getKey())) {
										autofixPlanDet.put("status", item.getValue().toString().toLowerCase());
									}
								});
								planitems.get(planitems.size() - 1).entrySet().forEach(item -> {
									if ("plannedActionTime".equalsIgnoreCase(item.getKey())) {
										autofixPlanDet.put("endTime", item.getValue());
									}
									if ("status".equalsIgnoreCase(item.getKey())) {
										autofixPlanDet.put("status", item.getValue().toString().toLowerCase());
									}
								});
							}
							List<Map<String, Object>> ruleDetails = new ArrayList<Map<String, Object>>();
							try {
								ruleDetails = rdsRepository.getDataFromPacman(
										"SELECT policyDisplayName, policyId FROM cf_PolicyTable WHERE policyId =\""
												+ sources.get("policyId") + "\"");
								autofixPlanDet.put("Name", ruleDetails.get(0).get("policyDisplayName"));
							} catch (Exception exception) {
								LOGGER.error("Error in getAutofixProjectionDetail for getting rule displayName ",
										exception);
							}
							try {
								autofixPlanDet.put("policyDescription",
										rdsRepository.queryForString(
												"select policyDesc from cf_PolicyTable WHERE policyId =\""
														+ ruleDetails.get(0).get("policyId") + "\""));
							} catch (Exception exception) {
								LOGGER.error("Error in getAutofixProjectionDetail for getting policy description ",
										exception);
							}
						}
					}
				}
			}
		} catch (Exception e) {
			LOGGER.error("Error in getAutofixProjectionDetail", e);
		}
		return autofixPlanDet;
	}

	private String filterkey( Map<String, List<String>> filter, String keyText) {
		String searchterm = "";
		if (filter.containsKey(keyText) && StringUtils.isNotBlank(filter.get(keyText).toString())) {
			searchterm = "[";
			List<String>splitted = filter.get(keyText);
			for (String _categoryList : splitted) {
				searchterm = searchterm + "\"" + _categoryList + "\",";
			}
			searchterm = StringUtils.substring(searchterm, 0, searchterm.length() - 1);
			searchterm = searchterm + "]";
		}
		return searchterm;
	}

	@Override
	@SuppressWarnings("unchecked")
	public List<Map<String, Object>> getCloudNotificationsSummary(String assetGroup, boolean globalNotifier,
			String resourceId, String eventStatus) {
		LOGGER.info("Inside getCloudNotificationsSummary");
		List<Map<String, Object>> summaryList = new ArrayList<>();
		try {
			Map<String, Object> countMap = new HashMap<>();
			if (globalNotifier && Strings.isNullOrEmpty(resourceId) && Strings.isNullOrEmpty(resourceId)) {
				countMap.put(GLOBAL_NOTIFICATION_COUNT, getTotalDocCount(CLOUD_NOTIFICATIONS,
						"{\"query\":{\"bool\":{\"must\":[{\"term\": { \"docType\": \"cloud_notification\"}},{ \"term\": { \"docType\": \"cloud_notification\"}},{\"match\":{\"latest\":true}}]}},\"aggs\":{\"name\":{\"terms\":{\"field\":\"eventarn.keyword\",\"size\":1000}}}}"));
				countMap.put(EVENT_ISSUES_COUNT, getTotalDocCount(CLOUD_NOTIFICATIONS,
						"{\"size\":0,\"query\":{\"bool\":{\"must\":[{\"term\": { \"docType\": \"cloud_notification\"}},{ \"term\": { \"docType\": \"cloud_notification\"}},{\"match\":{\"latest\":true}},{\"match\": {\"eventtypecategory.keyword\": \"issue\"}}]}},\"aggs\":{\"name\":{\"terms\":{\"field\":\"eventarn.keyword\",\"size\":1000}}}}"));
				countMap.put(EVENT_SCHEDULED_COUNT, getTotalDocCount(CLOUD_NOTIFICATIONS,
						"{\"size\":0,\"query\":{\"bool\":{\"must\":[{\"term\": { \"docType\": \"cloud_notification\"}},{ \"term\": { \"docType\": \"cloud_notification\"}},{\"match\":{\"latest\":true}},{\"match\": {\"eventtypecategory.keyword\": \"scheduledChange\"}}]}},\"aggs\":{\"name\":{\"terms\":{\"field\":\"eventarn.keyword\",\"size\":1000}}}}"));
				countMap.put(EVENT_NOTIFICATION_COUNT, getTotalDocCount(CLOUD_NOTIFICATIONS,
						"{\"size\":0,\"query\":{\"bool\":{\"must\":[{\"term\": { \"docType\": \"cloud_notification\"}},{ \"term\": { \"docType\": \"cloud_notification\"}},{\"match\":{\"latest\":true}},{\"match\": {\"eventtypecategory.keyword\": \"accountNotification\"}}]}},\"aggs\":{\"name\":{\"terms\":{\"field\":\"eventarn.keyword\",\"size\":1000}}}}"));
				countMap.put("autofixCount", 0);
				summaryList.add(countMap);
			} else {
				if (!Strings.isNullOrEmpty(resourceId) && !Strings.isNullOrEmpty(resourceId) && !globalNotifier) {
					String body = "{\"size\": 1,\"_source\":\"eventtypecategory\",\"query\":{\"bool\":{\"must\":[{\"term\":{\"docType\":\""
							+ TYPE +"\"}},{\"term\":{\"_resourceid.keyword\":\""
							+ resourceId + "\"}},{\"term\":{\"statuscode.keyword\":\"" + eventStatus + "\"}}]}}}";
					StringBuilder urlToQuery = new StringBuilder(esUrl).append("/").append(assetGroup).append("/")
							.append(TYPE).append("/").append(_SEARCH);
					Gson gson = new GsonBuilder().create();
					String responseDetails = null;
					try {
						responseDetails = PacHttpUtils.doHttpPost(urlToQuery.toString(),
								new StringBuilder(body).toString());
					} catch (Exception e) {
						LOGGER.error("Error in getAutofixProjectionDetail", e);
					}
					Map<String, Object> response = (Map<String, Object>) gson.fromJson(responseDetails, Object.class);
					if (response.containsKey(HITS)) {
						Map<String, Object> hits = (Map<String, Object>) response.get(HITS);
						if (hits.containsKey(HITS)) {
							List<Map<String, Object>> hitDetails = (List<Map<String, Object>>) hits.get(HITS);
							if (!hitDetails.isEmpty()) {
								for (Map<String, Object> hitDetail : hitDetails) {
									Map<String, Object> sources = (Map<String, Object>) hitDetail.get(_SOURCE);
									String eventType = sources.get("eventtypecategory").toString();
									switch (eventType) {
									case "scheduledChange":
										countMap.put(GLOBAL_NOTIFICATION_COUNT, 0);
										countMap.put(EVENT_ISSUES_COUNT, 0);
										countMap.put(EVENT_SCHEDULED_COUNT, sources.size());
										countMap.put(EVENT_NOTIFICATION_COUNT, 0);
										countMap.put("autofixCount", 0);
										summaryList.add(countMap);
										break;
									case "issue":
										countMap.put(GLOBAL_NOTIFICATION_COUNT, 0);
										countMap.put(EVENT_ISSUES_COUNT, sources.size());
										countMap.put(EVENT_SCHEDULED_COUNT, 0);
										countMap.put(EVENT_NOTIFICATION_COUNT, 0);
										countMap.put("autofixCount", 0);
										summaryList.add(countMap);
										break;
									case "accountNotification":
										countMap.put(GLOBAL_NOTIFICATION_COUNT, 0);
										countMap.put(EVENT_ISSUES_COUNT, 0);
										countMap.put(EVENT_SCHEDULED_COUNT, 0);
										countMap.put(EVENT_NOTIFICATION_COUNT, sources.size());
										countMap.put("autofixCount", 0);
										summaryList.add(countMap);
										break;
									default:
									}
								}
							}
						}
					}
				} else if (!Strings.isNullOrEmpty(resourceId) && !Strings.isNullOrEmpty(resourceId) && globalNotifier) {
					countMap.put(GLOBAL_NOTIFICATION_COUNT, 0);
					countMap.put(EVENT_ISSUES_COUNT, 0);
					countMap.put(EVENT_SCHEDULED_COUNT, 0);
					countMap.put(EVENT_NOTIFICATION_COUNT, 0);
					countMap.put("autofixCount", 0);
					summaryList.add(countMap);
				} else {
					countMap.put(GLOBAL_NOTIFICATION_COUNT, getTotalDocCount(CLOUD_NOTIFICATIONS,
							"{\"query\":{\"bool\":{\"must\":[{\"term\": { \"docType\": \"cloud_notification\"}},{\"match\":{\"latest\":true}}]}},\"aggs\":{\"name\":{\"terms\":{\"field\":\"eventarn.keyword\",\"size\":1000}}}}"));
					countMap.put(EVENT_ISSUES_COUNT, getTotalDocCount(assetGroup,
							"{\"size\":0,\"query\":{\"bool\":{\"must\":[{\"term\": { \"docType\": \"cloud_notification\"}},{\"match\":{\"latest\":true}},{\"match\": {\"eventtypecategory.keyword\": \"issue\"}}]}},\"aggs\":{\"name\":{\"terms\":{\"field\":\"eventarn.keyword\",\"size\":1000}}}}"));
					countMap.put(EVENT_SCHEDULED_COUNT, getTotalDocCount(assetGroup,
							"{\"size\":0,\"query\":{\"bool\":{\"must\":[{\"term\": { \"docType\": \"cloud_notification\"}},{\"match\":{\"latest\":true}},{\"match\": {\"eventtypecategory.keyword\": \"scheduledChange\"}}]}},\"aggs\":{\"name\":{\"terms\":{\"field\":\"eventarn.keyword\",\"size\":1000}}}}"));
					countMap.put(EVENT_NOTIFICATION_COUNT, getTotalDocCount(assetGroup,
							"{\"size\":0,\"query\":{\"bool\":{\"must\":[{\"term\": { \"docType\": \"cloud_notification\"}},{\"match\":{\"latest\":true}},{\"match\": {\"eventtypecategory.keyword\": \"accountNotification\"}}]}},\"aggs\":{\"name\":{\"terms\":{\"field\":\"eventarn.keyword\",\"size\":1000}}}}"));
					countMap.put("autofixCount", getAutoFixSummary(assetGroup, "{  \"query\": {\"bool\": {\"must\": [ { \"term\": { \"docType\": \"autofixplan\" } } ] } } }"));
					summaryList.add(countMap);
				}
			}
		} catch (Exception e) {
			LOGGER.error("Error in getCloudNotificationsSummary", e);
		}
		LOGGER.info("Exiting getCloudNotificationsSummary");
		return summaryList;
	}
}
