package com.tmobile.pacman.api.admin.repository.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

import java.util.List;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class PolicyParamDetails {

    private List<PolicyParams> params;
}
