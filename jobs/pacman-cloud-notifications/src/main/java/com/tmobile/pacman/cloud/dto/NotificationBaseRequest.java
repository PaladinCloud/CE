package com.tmobile.pacman.cloud.dto;




import com.tmobile.pacman.cloud.util.Constants;

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

