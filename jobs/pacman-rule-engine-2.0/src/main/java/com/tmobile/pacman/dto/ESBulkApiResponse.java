package com.tmobile.pacman.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

import java.util.List;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class ESBulkApiResponse {
    private int took;
    private boolean errors;
    private List<Item> items;
}
