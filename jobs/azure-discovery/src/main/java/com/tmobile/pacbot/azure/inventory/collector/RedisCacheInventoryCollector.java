package com.tmobile.pacbot.azure.inventory.collector;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.microsoft.azure.management.monitor.LocalizableString;
import com.microsoft.azure.management.redis.RedisCache;
import com.tmobile.pacbot.azure.inventory.vo.RedisCacheVH;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.microsoft.azure.PagedList;
import com.microsoft.azure.management.Azure;
import com.microsoft.azure.management.network.Network;
import com.tmobile.pacbot.azure.inventory.auth.AzureCredentialProvider;
import com.tmobile.pacbot.azure.inventory.vo.NetworkVH;
import com.tmobile.pacbot.azure.inventory.vo.SubscriptionVH;
public class RedisCacheInventoryCollector {
    @Autowired
    AzureCredentialProvider azureCredentialProvider;

    private static Logger log = LoggerFactory.getLogger(NetworkInventoryCollector.class);

    public List<RedisCacheVH> fetchRedisCacheDetails(SubscriptionVH subscription) {
        List<RedisCacheVH> redisCacheList = new ArrayList<>();

        Azure azure = azureCredentialProvider.getClient(subscription.getTenant(),subscription.getSubscriptionId());
        PagedList<RedisCache> caches = azure.redisCaches().list();

        for (RedisCache redisCache : caches) {
            RedisCacheVH redisCacheVH = new RedisCacheVH();
            redisCacheVH.setNonSslPort(redisCache.nonSslPort());
            redisCacheVH.setSubscription(subscription.getSubscriptionId());
            redisCacheVH.setSubscriptionName(subscription.getSubscriptionName());
            redisCacheVH.setName(redisCache.name());
            redisCacheVH.setPort(redisCache.port());
            redisCacheList.add(redisCacheVH);
        }
        log.info("Target Type : {}  Total: {} ","redis cache",redisCacheList.size());
        return redisCacheList;
    }
}
