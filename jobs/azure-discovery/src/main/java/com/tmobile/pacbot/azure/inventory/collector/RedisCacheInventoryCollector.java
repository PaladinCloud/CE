package com.tmobile.pacbot.azure.inventory.collector;

import com.microsoft.azure.PagedList;
import com.microsoft.azure.management.Azure;
import com.microsoft.azure.management.redis.RedisCache;
import com.tmobile.pacbot.azure.inventory.auth.AzureCredentialProvider;
import com.tmobile.pacbot.azure.inventory.vo.AzureVH;
import com.tmobile.pacbot.azure.inventory.vo.RedisCacheVH;
import com.tmobile.pacbot.azure.inventory.vo.SubscriptionVH;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Component
public class RedisCacheInventoryCollector implements Collector {
    private static final Logger log = LoggerFactory.getLogger(RedisCacheInventoryCollector.class);
    @Autowired
    AzureCredentialProvider azureCredentialProvider;

    @Override
    public List<? extends AzureVH> collect() {
        throw new UnsupportedOperationException();
    }

    public List<RedisCacheVH> collect(SubscriptionVH subscription) {
        List<RedisCacheVH> redisCacheList = new ArrayList<>();

        Azure azure = azureCredentialProvider.getClient(subscription.getTenant(), subscription.getSubscriptionId());
        PagedList<RedisCache> caches = azure.redisCaches().list();

        for (RedisCache redisCache : caches) {
            RedisCacheVH redisCacheVH = new RedisCacheVH();
            redisCacheVH.setNonSslPort(redisCache.nonSslPort());
            redisCacheVH.setSubscription(subscription.getSubscriptionId());
            redisCacheVH.setSubscriptionName(subscription.getSubscriptionName());
            redisCacheVH.setName(redisCache.name());
            redisCacheVH.setPort(redisCache.port());
            redisCacheVH.setId(redisCache.id());
            redisCacheVH.setRegion(Util.getRegionValue(subscription, redisCache.regionName()));
            redisCacheVH.setResourceGroupName(redisCache.resourceGroupName());
            redisCacheVH.setTags(redisCache.tags());
            redisCacheList.add(redisCacheVH);
        }

        log.info("Target Type : {}  Total: {} ", "redis cache", redisCacheList.size());
        return redisCacheList;
    }

    @Override
    public List<? extends AzureVH> collect(SubscriptionVH subscription, Map<String, Map<String, String>> tagMap) {
        throw new UnsupportedOperationException();
    }
}
