package com.tmobile.pacbot.azure.inventory.collector;
import java.util.ArrayList;
import java.util.List;
import com.microsoft.azure.management.redis.RedisCache;
import com.tmobile.pacbot.azure.inventory.vo.RedisCacheVH;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.microsoft.azure.PagedList;
import com.microsoft.azure.management.Azure;
import com.tmobile.pacbot.azure.inventory.auth.AzureCredentialProvider;
import com.tmobile.pacbot.azure.inventory.vo.SubscriptionVH;
@Component
public class RedisCacheInventoryCollector {
    @Autowired
    AzureCredentialProvider azureCredentialProvider;

    private static Logger log = LoggerFactory.getLogger(RedisCacheInventoryCollector.class);

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
            redisCacheVH.setId(redisCache.id());
            redisCacheVH.setRegion(redisCache.regionName());
            redisCacheList.add(redisCacheVH);
        }
        log.info("Target Type : {}  Total: {} ","redis cache",redisCacheList.size());
        return redisCacheList;
    }
}
