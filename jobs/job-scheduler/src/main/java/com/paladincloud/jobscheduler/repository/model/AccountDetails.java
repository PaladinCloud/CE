package com.paladincloud.jobscheduler.repository.model;

import javax.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Setter
@Getter
@Table(name = "cf_Accounts", uniqueConstraints = @UniqueConstraint(columnNames = "accountId"))
public class AccountDetails {

    @Id
    @Column(name = "accountId", unique = true, nullable = false)
    private String accountId;
    private String accountName;
    private String assets;
    private String violations;
    private String accountStatus;
    private String platform;
    private String createdBy;
    private String createdTime;
}
