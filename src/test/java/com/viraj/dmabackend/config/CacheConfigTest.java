package com.viraj.dmabackend.config;

import org.junit.jupiter.api.Test;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.cache.caffeine.CaffeineCacheManager;

import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;

public class CacheConfigTest {

    @Test
    void cacheManager_CreatesCaffeineCacheManagerWithConfiguredCaches() {
        CacheConfig config = new CacheConfig();
        CacheManager cacheManager = config.cacheManager();

        assertInstanceOf(CaffeineCacheManager.class, cacheManager);
        CaffeineCacheManager caffeineCacheManager = (CaffeineCacheManager) cacheManager;

        Cache roleCache = caffeineCacheManager.getCache(CacheConfig.CACHE_ROLE);
        assertNotNull(roleCache);

        Cache rolesCache = caffeineCacheManager.getCache(CacheConfig.CACHE_ROLES);
        assertNotNull(rolesCache);

        Cache menuTreeCache = caffeineCacheManager.getCache(CacheConfig.CACHE_MENU_TREE);
        assertNotNull(menuTreeCache);

        Cache permissionsCache = caffeineCacheManager.getCache(CacheConfig.CACHE_PERMISSIONS);
        assertNotNull(permissionsCache);
    }
}
