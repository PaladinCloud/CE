package com.tmobile.pacman.commons.dto;

import com.tmobile.pacman.commons.utils.Constants;

import java.util.UUID;

import static com.tmobile.pacman.commons.utils.Constants.EVENT_SOURCE;
import static com.tmobile.pacman.commons.utils.Constants.EVENT_SOURCE_NAME;

public class NotificationBaseRequest {
    private String eventId;
    private String eventName;
    private Constants.NotificationTypes eventCategory;
    private String eventCategoryName;
    private String eventSource;
    private String eventSourceName;
    private String eventDescription;
    private String subject;
    private Object payload;

    public NotificationBaseRequest(){

        this.eventId=UUID.randomUUID().toString();
        this.setEventSource(EVENT_SOURCE);
        this.setEventSourceName(EVENT_SOURCE_NAME);
    }

    public String getEventId() {
        return eventId;
    }

    public void setEventId(String eventId) {
        this.eventId = eventId;
    }

    public String getEventName() {
        return eventName;
    }

    public void setEventName(String eventName) {
        this.eventName = eventName;
    }



    public String getEventCategoryName() {
        return eventCategoryName;
    }

    public void setEventCategoryName(String eventCategoryName) {
        this.eventCategoryName = eventCategoryName;
    }

    public String getEventSource() {
        return eventSource;
    }

    public void setEventSource(String eventSource) {
        this.eventSource = eventSource;
    }

    public String getEventSourceName() {
        return eventSourceName;
    }

    public void setEventSourceName(String eventSourceName) {
        this.eventSourceName = eventSourceName;
    }

    public String getEventDescription() {
        return eventDescription;
    }

    public void setEventDescription(String eventDescription) {
        this.eventDescription = eventDescription;
    }

    public String getSubject() {
        return subject;
    }

    public void setSubject(String subject) {
        this.subject = subject;
    }

    public Object getPayload() {
        return payload;
    }

    public void setPayload(Object payload) {
        this.payload = payload;
    }

    public Constants.NotificationTypes getEventCategory() {
        return eventCategory;
    }

    public void setEventCategory(Constants.NotificationTypes eventCategory) {
        this.eventCategory = eventCategory;
    }
}
