package com.tmobile.pacman.api.admin.repository.model;

import javax.persistence.*;

@Entity
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

    public String getAccountId() {
        return accountId;
    }

    public void setAccountId(String accountId) {
        this.accountId = accountId;
    }

    public String getAccountName() {
        return accountName;
    }

    public void setAccountName(String accountName) {
        this.accountName = accountName;
    }

    public String getAssets() {
        return assets;
    }

    public void setAssets(String assets) {
        this.assets = assets;
    }

    public String getViolations() {
        return violations;
    }

    public void setViolations(String violations) {
        this.violations = violations;
    }

    public String getAccountStatus() {
        return accountStatus;
    }

    public void setAccountStatus(String accountStatus) {
        this.accountStatus = accountStatus;
    }

    public String getPlatform() {
        return platform;
    }

    public void setPlatform(String platform) {
        this.platform = platform;
    }
}
