package com.paladincloud.notification_log.dto;




import com.paladincloud.notification_log.common.*;

import lombok.Data;

import java.util.UUID;


@Data
public class NotificationBaseRequest {
    private String eventId;
    private String eventName;
    private String eventCategory;
    private Constants.NotificationTypes eventCategoryName;
    private String eventSource;
    private String eventSourceName;
    private String eventDescription;
    private String subject;
    private String assetType;
    private String assetTypeName;
    private Object payload;

    public NotificationBaseRequest(){

        this.eventId=UUID.randomUUID().toString();
       
    }

   
	
    
}

