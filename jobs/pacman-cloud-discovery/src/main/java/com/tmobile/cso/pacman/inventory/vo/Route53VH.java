package com.tmobile.cso.pacman.inventory.vo;

import java.util.Date;
import java.util.List;

public class Route53VH {

    private String domainName;

    private Date expirationDate;

    private Boolean registrantPrivacy;

    private Boolean autoRenew;

    private String statusList;

    private List<Route53HostedZoneVH> route53HostedZoneVHList;

    public void setDomainName(String domainName) {
        this.domainName = domainName;
    }

    public void setExpirationDate(Date expirationDate) {
        this.expirationDate = expirationDate;
    }

    public void setRegistrantPrivacy(Boolean registrantPrivacy) {
        this.registrantPrivacy = registrantPrivacy;
    }

    public void setAutoRenew(Boolean autoRenew) {
        this.autoRenew = autoRenew;
    }

    public void setStatusList(String statusList) {
        this.statusList = statusList;
    }

    public void setRoute53HostedZoneVHList(List<Route53HostedZoneVH> route53HostedZoneVHList) {
        this.route53HostedZoneVHList = route53HostedZoneVHList;
    }
}
