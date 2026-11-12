package com.viraj.dmabackend.config;

import com.github.benmanes.caffeine.cache.Caffeine;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.caffeine.CaffeineCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;
import java.util.concurrent.TimeUnit;

@Configuration
@EnableCaching
public class CacheConfig {

    public static final String CACHE_PERMISSIONS = "permissions";
    public static final String CACHE_ROLES = "roles";
    public static final String CACHE_ROLE = "role";
    public static final String CACHE_ROLE_ENTITY = "roleEntity";
    public static final String CACHE_MENU_TREE = "menuTree";

    @Bean
    public CacheManager cacheManager() {
        CaffeineCacheManager cacheManager = new CaffeineCacheManager();
        cacheManager.setCacheNames(List.of(CACHE_PERMISSIONS, CACHE_ROLES, CACHE_ROLE, CACHE_ROLE_ENTITY, CACHE_MENU_TREE));
        cacheManager.setCaffeine(Caffeine.newBuilder()
                .maximumSize(500)
                .expireAfterWrite(1, TimeUnit.HOURS)
                .recordStats());
        return cacheManager;
    }
}
