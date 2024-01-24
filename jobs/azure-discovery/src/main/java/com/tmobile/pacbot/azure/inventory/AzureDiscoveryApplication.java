package com.tmobile.pacbot.azure.inventory;

import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.concurrent.ConcurrentMapCacheManager;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

import java.util.Map;

@Configuration
@EnableCaching
@ComponentScan({"com.tmobile.pacbot.azure.inventory", "com.tmobile.pacman.commons.database", "com.tmobile.pacman.commons.secrets"})
public class AzureDiscoveryApplication {

    public static Map<String, Object> collect() {
        ApplicationContext context = new AnnotationConfigApplicationContext(AzureDiscoveryApplication.class);
        AzureFetchOrchestrator orchestrator = context.getBean(AzureFetchOrchestrator.class);

        return orchestrator.orchestrate();
    }

    @Bean
    public CacheManager cacheManager() {
        return new ConcurrentMapCacheManager("PolicyDefinitionVH");
    }
}

