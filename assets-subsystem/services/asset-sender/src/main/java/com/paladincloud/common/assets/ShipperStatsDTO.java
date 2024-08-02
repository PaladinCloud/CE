package com.paladincloud.common.assets;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonFormat.Shape;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.time.ZonedDateTime;
import java.util.Date;

public record ShipperStatsDTO(
    @JsonProperty("datasouree")
    String dataSource,
    @JsonProperty("docType")
    String docType,
    // The format is "yyyy-MM-dd'T'HH:mm:ssZ"
    @JsonFormat(shape = Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ssZ")
    @JsonProperty("start_time")
    ZonedDateTime startTime,
    // The format is "yyyy-MM-dd'T'HH:mm:ssZ"
    @JsonFormat(shape = Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ssZ")
    @JsonProperty("end_time")
    ZonedDateTime endTime,
    @JsonProperty("total_docs")
    long totalDocumentCount,
    @JsonProperty("uploaded_docs")
    long uploadedDocumentCount,
    @JsonProperty("newly_discovered")
    long newlyDiscoveredCount
) {}
