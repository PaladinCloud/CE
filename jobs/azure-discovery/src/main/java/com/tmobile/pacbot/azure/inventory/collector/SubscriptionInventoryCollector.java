package com.tmobile.pacbot.azure.inventory.collector;

import com.tmobile.pacbot.azure.inventory.vo.SubscriptionVH;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class SubscriptionInventoryCollector {

    private static final Logger log = LoggerFactory.getLogger(SubscriptionInventoryCollector.class);

    public List<SubscriptionVH> fetchSubscriptions(SubscriptionVH subscription) {

        List<SubscriptionVH> subscriptionList = new ArrayList<>();

        SubscriptionVH subscriptionVH = new SubscriptionVH();
        subscriptionVH.setTenant(subscription.getTenant());
        subscriptionVH.setSubscriptionId(subscription.getSubscriptionId());
        subscriptionVH.setId(subscription.getSubscriptionId());
        subscriptionVH.setSubscriptionName(subscription.getSubscriptionName());
        subscriptionVH.setSubscription(subscription.getSubscriptionId());
        subscriptionList.add(subscriptionVH);

        log.info("Size of subscriptions: {}", subscriptionList.size());
        return subscriptionList;
    }
}

