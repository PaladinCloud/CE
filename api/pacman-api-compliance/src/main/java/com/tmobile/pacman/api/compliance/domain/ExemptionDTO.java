package com.tmobile.pacman.api.compliance.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Data
@AllArgsConstructor
@Builder
public class ExemptionDTO {

    private String status;
    private String reasonToExempt;
    private String exemptionExpiringOn;
    private String exemptionRaisedExpiringOn;
    private String exemptionRaisedBy;
    private String exemptionRaisedOn;
    private String exemptionRevokedBy;
    private String exemptionRevokedOn;
    private String exemptionCancelledBy;
    private String exemptionCancelledOn;
    private String exemptionApprovedBy;
    private String exemptionApprovedOn;
}
