package com.tmobile.pacman.api.admin.controller;

import com.google.common.base.Strings;
import com.tmobile.pacman.api.admin.domain.NotificationPrefsRequest;
import com.tmobile.pacman.api.admin.service.NotificationSettings;
import com.tmobile.pacman.api.commons.utils.ResponseUtils;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;
import java.util.List;


import static com.tmobile.pacman.api.admin.common.AdminConstants.UNEXPECTED_ERROR_OCCURRED;

@Api(value = "/notifications")
@RestController
@PreAuthorize("@securityService.hasPermission(authentication, 'notification-preferences') or #oauth2.hasScope('API_OPERATION/READ')")
@RequestMapping("/notifications")
public class NotificationsController {
    private static final Logger log = LoggerFactory.getLogger(NotificationsController.class);
    @Autowired
    NotificationSettings notificationSettings;

    @ApiOperation(httpMethod = "GET", value = "API to get Notification settings",  produces = MediaType.APPLICATION_JSON_VALUE)
    @RequestMapping(path = "/preferences", method = RequestMethod.GET,  produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Object> getNotificationSettings() {
        try {
            return ResponseUtils.buildSucessResponse(notificationSettings.getNotificationSettings());
        } catch (Exception exception) {
            log.error(UNEXPECTED_ERROR_OCCURRED, exception);
            return ResponseUtils.buildFailureResponse(new Exception(exception.getMessage()), null, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
    @ApiOperation(httpMethod = "POST", value = "API to update notification settings",  consumes = MediaType.APPLICATION_JSON_VALUE)
    @RequestMapping(path = "/preferences", method = RequestMethod.POST,  consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Object> updateNotificationSettings(@RequestBody List<NotificationPrefsRequest> notificationPreferencesList) {
        try {
            for(NotificationPrefsRequest notificationPrefsRequest : notificationPreferencesList){
                if(Strings.isNullOrEmpty(notificationPrefsRequest.getNotificationType())){
                    return ResponseUtils.buildFailureResponse(new Exception("notificationType not provided."), null, HttpStatus.INTERNAL_SERVER_ERROR);
                }
                if(Strings.isNullOrEmpty(notificationPrefsRequest.getNotificationChannelName())){
                    return ResponseUtils.buildFailureResponse(new Exception("notificationChannelName not provided."), null, HttpStatus.INTERNAL_SERVER_ERROR);
                }
                if(Strings.isNullOrEmpty(notificationPrefsRequest.getAddOrRemove())){
                    return ResponseUtils.buildFailureResponse(new Exception("addOrRemove not provided for "+notificationPrefsRequest.getNotificationType()+", "+notificationPrefsRequest.getNotificationChannelName()+" combination."), null, HttpStatus.INTERNAL_SERVER_ERROR);
                }
                if(!Arrays.asList("add","remove").contains(notificationPrefsRequest.getAddOrRemove())){
                    return ResponseUtils.buildFailureResponse(new Exception("Invalid value for addOrRemove. It should be one of 'add'/'remove'."), null, HttpStatus.INTERNAL_SERVER_ERROR);
                }
                if(notificationPrefsRequest.getUpdatedBy()==null){
                    return ResponseUtils.buildFailureResponse(new Exception("updatedBy should be provided for "+notificationPrefsRequest.getNotificationType()+", "+notificationPrefsRequest.getNotificationChannelName()+" combination."), null, HttpStatus.INTERNAL_SERVER_ERROR);
                }
            }
            notificationSettings.updateNotificationSettings(notificationPreferencesList);
            return ResponseUtils.buildSucessResponse("Successfully updated!!");
        } catch (Exception exception) {
            log.error(UNEXPECTED_ERROR_OCCURRED, exception);
            return ResponseUtils.buildFailureResponse(new Exception(exception.getMessage()), null, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @ApiOperation(httpMethod = "GET", value = "API to get Notification preferences and configurations",  produces = MediaType.APPLICATION_JSON_VALUE)
    @RequestMapping(path = "/preferences-and-configs", method = RequestMethod.GET,  produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Object> getNotificationConfigs() {
        try {
            return ResponseUtils.buildSucessResponse(notificationSettings.getNotificationSettingsAndConfigs());
        } catch (Exception exception) {
            log.error(UNEXPECTED_ERROR_OCCURRED, exception);
            return ResponseUtils.buildFailureResponse(new Exception(exception.getMessage()), null, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

}
