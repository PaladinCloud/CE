/**
 * 
 */
package com.tmobile.pacman.api.asset.controller;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

import com.tmobile.pacman.api.asset.domain.NotificationRequest;
import com.tmobile.pacman.api.commons.utils.ThreadLocalUtil;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.google.common.base.Strings;
import com.tmobile.pacman.api.asset.AssetConstants;
import com.tmobile.pacman.api.asset.domain.Request;
import com.tmobile.pacman.api.asset.domain.ResponseWithCount;
import com.tmobile.pacman.api.asset.service.CloudNotificationService;
import com.tmobile.pacman.api.commons.Constants;
import com.tmobile.pacman.api.commons.utils.CommonUtils;
import com.tmobile.pacman.api.commons.utils.ResponseUtils;

import io.swagger.annotations.ApiOperation;

/**
 * The controller layer which has methods to return list of cloud notifications.
 *
 */
@RestController
@PreAuthorize("@securityService.hasPermission(authentication, 'readonly') or #oauth2.hasScope('API_OPERATION/READ')")
@CrossOrigin
public class CloudNotificationsController {
	
	@Autowired
	CloudNotificationService cloudService;

	private static final Log LOGGER = LogFactory.getLog(AssetListController.class);
	
	 /**
		 * Fetches the Cloud Notifications for the rule id passed in the filter.
		 *
		 * @param request This request expects assetGroup and ruleId as mandatory
		 * attributes. API returns all the CIS assets associated with the
		 * assetGroup with matching filters.
		 * 
		 * @return cloud Notifications by asset group.
		 */

		@ApiOperation(httpMethod = "POST", value = "Get the list of  Cloud Notifications by a asset Group. Mandatory Filter -'Global Notifications'")
		@PostMapping(value = "/v1/cloud/notifications")
		public ResponseEntity<Object> getlistOfCloudNotifications(@RequestBody(required = true) NotificationRequest request ) {

			String assetGroup = request.getAg();
			if (Strings.isNullOrEmpty(assetGroup)) {
				return ResponseUtils.buildFailureResponse(new Exception(Constants.ASSET_MANDATORY));
			}

			int from = request.getFrom();
			int size = request.getSize();
			if (from < 0) {
				return ResponseUtils.buildFailureResponse(new Exception(AssetConstants.ERROR_FROM_NEGATIVE));
			}

			Date startDate=null;
			Date endDate=null;

			String searchText = request.getSearchtext();
			Map<String,List<String>> filter = request.getFilter();
			Map<String,Object> sortFilter=request.getSortFilter();

			if(filter.get("_loaddate")!=null) {
				List<String> dateRangeList = filter.get("_loaddate");
				StringBuilder dateRange= new StringBuilder();
				for(String range:dateRangeList){
					dateRange.append(range);
				}
				String[] dates = dateRange.toString().split(" - ");
				SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
				try {
					startDate = dateFormat.parse(dates[0]);
					endDate = dateFormat.parse(dates[1]);
				} catch (ParseException e) {
					LOGGER.error("Error in Date Format");
				}
			}
			List<Map<String, Object>> masterList;
			
			try {
				masterList = cloudService.getNotifications(assetGroup, filter, size, from,sortFilter,startDate,endDate);
			} catch (Exception e) {
				LOGGER.error("Error in getlistOfCloudNotifications ", e);
				return ResponseUtils.buildFailureResponse(e);
			}
			int count = ThreadLocalUtil.count.get();
			ThreadLocalUtil.count.remove();
			return formResponseWithCount(masterList, from, size, searchText,count);
		}

		 /**
	     * Method returns the list with count based on the from and size.
	     * 
	     * @param masterList
	     * @param from
	     * @param size
	     * @param searchText
	     * 
	     * @return ResponseEntity 
	     */
	    @SuppressWarnings("unchecked")
	    private ResponseEntity<Object> formResponseWithCount(List<Map<String, Object>> masterList, int from, int size,
	            String searchText,long totalDocumentCount) {
	        try {
	            List<Map<String, Object>> masterDetailList = (List<Map<String, Object>>) CommonUtils
	                    .filterMatchingCollectionElements(masterList, searchText, true);
	            if (masterDetailList.isEmpty()) {
	                return ResponseUtils
	                        .buildSucessResponse(new ResponseWithCount(new ArrayList<Map<String, Object>>(), 0));
	            }
	            return ResponseUtils.buildSucessResponse(new ResponseWithCount(masterDetailList, totalDocumentCount));
	        } catch (Exception e) {
	            LOGGER.error("Exception in formResponseWithCount ",e);
	            return ResponseUtils.buildFailureResponse(e);
	        }
	    }
	    
	    @GetMapping(value = "/v1/cloud/notifications/summary")
	    public ResponseEntity<Object> getCloudNotificationsSummary(@RequestParam(name = "ag", required = true) String assetGroup , 
	    		@RequestParam(name = "global", required = true) boolean globalNotifier,
	    		@RequestParam(name = "resourceId", required = false) String resourceId,
	    		@RequestParam(name = "eventStatus", required = false) String eventStatus) {
			try {
				return ResponseUtils.buildSucessResponse(cloudService.getCloudNotificationsSummary(assetGroup, globalNotifier, resourceId, eventStatus));
			} catch (Exception e) {
				LOGGER.error("Error in getCloudNotificationsSummary "+ e);
				return ResponseUtils.buildFailureResponse(e);
			}
	    }
	    
	    @GetMapping(value = "/v1/cloud/notifications/detail")
	    public ResponseEntity<Object> getCloudNotificationDetail(@RequestParam(name = "eventId", required = true) String eventArn, 
	    		@RequestParam(name = "ag", required = true) String assetGroup) {
			try {
				return ResponseUtils.buildSucessResponse(cloudService.getCloudNotificationDetail(eventArn, assetGroup));
			} catch (Exception e) {
				LOGGER.error("Error in getCloudNotificationDetail "+ e);
				return ResponseUtils.buildFailureResponse(e);
			}
	    }
	    
	    @GetMapping(value = "/v1/cloud/notifications/info")
	    public ResponseEntity<Object> getCloudNotificationInfo(@RequestParam(name = "eventArn", required = true) String eventArn, 
	    		@RequestParam(name = "global", required = true) boolean globalNotifier,
	    		@RequestParam(name = "ag", required = true) String assetGroup) {
			try {
				return ResponseUtils.buildSucessResponse(cloudService.getCloudNotificationInfo(eventArn,globalNotifier, assetGroup));
			} catch (Exception e) {
				LOGGER.error("Error in getCloudNotificationInfo "+ e);
				return ResponseUtils.buildFailureResponse(e);
			}
	    }
	    
	    @ApiOperation(httpMethod = "POST", value = "Autofix plan details")
		@PostMapping(value = "/v1/autofix/notifications/detail")
	    public ResponseEntity<Object> getAutofixProjectionDetail(@RequestBody(required = true) Request request) {
			try {
				String assetGroup = request.getAg();
				Map<String, String> filter = request.getFilter();
				if (Strings.isNullOrEmpty(assetGroup)) {
					return ResponseUtils.buildFailureResponse(new Exception(Constants.ASSET_MANDATORY));
				}
				if (filter.isEmpty()) {
					return ResponseUtils.buildFailureResponse(new Exception(Constants.FILTER_MANDATORY));
				}
				return ResponseUtils.buildSucessResponse(cloudService.getAutofixProjectionDetail(assetGroup, filter));
			} catch (Exception e) {
				LOGGER.error("Error in getAutofixProjectionDetail "+ e);
				return ResponseUtils.buildFailureResponse(e);
			}
	    }

}
