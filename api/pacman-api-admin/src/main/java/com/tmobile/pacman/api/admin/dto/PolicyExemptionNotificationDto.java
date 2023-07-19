package com.tmobile.pacman.api.admin.dto;

import com.tmobile.pacman.api.admin.repository.model.PolicyExemption;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class PolicyExemptionNotificationDto extends PolicyExemption {

    private String summary;

    private String policyName;
}
