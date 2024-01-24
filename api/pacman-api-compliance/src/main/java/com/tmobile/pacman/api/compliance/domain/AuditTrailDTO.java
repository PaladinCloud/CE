package com.tmobile.pacman.api.compliance.domain;

import lombok.Builder;
import lombok.Value;

import java.util.Map;

@Value
@Builder(setterPrefix = "with", toBuilder = true)
public class AuditTrailDTO {

    String id;
    String assetGroup;
    String targetType;
    String createdBy;
    String docType;
    String target;
    String status;
    Map<String, Object> parentDetailsMap;
    Map<String, String> optionalAuditFields;
}
