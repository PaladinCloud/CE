package com.tmobile.pacman.api.admin.domain;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.google.common.collect.Maps;

import java.time.Clock;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Map;
import java.util.UUID;

import static com.tmobile.pacman.api.commons.Constants.DOCID;

public class ActivityLogRequest {

    @JsonFormat(pattern="yyyy-MM-dd HH:mm:ss")
    private LocalDateTime updateTime=LocalDateTime.now(Clock.systemUTC());
    private String user;
    private String object;
    private String objectId;
    private String action;
    private String oldState;
    private String newState;
    private String updateTimeStr;

    public String getUpdateTimeStr() {
        return updateTimeStr;
    }

    public void setUpdateTimeStr(String updateTimeStr) {
        this.updateTimeStr = updateTimeStr;
    }

    public String getUser() {
        return user;
    }

    public void setUser(String user) {
        this.user = user;
    }

    public String getObject() {
        return object;
    }

    public void setObject(String object) {
        this.object = object;
    }

    public String getObjectId() {
        return objectId;
    }

    public void setObjectId(String objectId) {
        this.objectId = objectId;
    }


    public String getNewState() {
        return newState;
    }

    public void setNewState(String newState) {
        this.newState = newState;
    }

    public LocalDateTime getUpdateTime() {
        return updateTime;
    }

    public void setUpdateTime(LocalDateTime updateTime) {
        if(updateTime!=null) this.updateTime = updateTime;
    }

    public String getOldState() {
        return oldState;
    }
    public void setOldState(String oldState) {
        this.oldState = oldState;
    }
    public String getAction() {
        return action;
    }
    public void setAction(String action) {
        this.action = action;
    }
    public Map<String, Object> getActivityLogDetails() {
        Map<String, Object> activityLogDetails = Maps.newHashMap();
        DateTimeFormatter customFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss");
        String loggingTime = this.updateTime.format(customFormatter);
        activityLogDetails.put("updateTime", loggingTime);
        activityLogDetails.put("updateTimeStr", loggingTime.replaceAll("T"," "));
        activityLogDetails.put("user", this.user);
        activityLogDetails.put("object", this.object);
        activityLogDetails.put("objectId", this.objectId);
        activityLogDetails.put("_docid", this.objectId);
        activityLogDetails.put("oldState",this.oldState);
        activityLogDetails.put("newState",this.newState);
        activityLogDetails.put("action",this.action);
        UUID uuid = UUID.randomUUID();
        String strUUID = uuid.toString();
        activityLogDetails.put(DOCID,strUUID);
        return activityLogDetails;
    }

    public String toString(){
        return "object-"+this.object+", objectId"+this.objectId+", action"+this.action+
                ", user"+this.user+", updateTime"+this.updateTime+", oldState"+this.oldState+", newState"+this.newState;
    }
}