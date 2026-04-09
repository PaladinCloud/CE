package com.tmobile.pacman.api.compliance.dto;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class NotificationPayload {

    private String url;
    private String status;
    private String intiatedBy;
    private String intiatedDate;
    private Object searchFilter;

}
