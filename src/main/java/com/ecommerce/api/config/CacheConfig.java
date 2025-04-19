package com.ecommerce.api.config;

import com.ecommerce.api.security.CustomAuthenticationSuccessHandler;
import com.github.benmanes.caffeine.cache.Caffeine;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.caffeine.CaffeineCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Arrays;
import java.util.concurrent.TimeUnit;

@Configuration
@EnableCaching
public class CacheConfig {
    private static final Logger logger = LoggerFactory.getLogger(CacheConfig.class);

    @Value("${cache.caffeine.spec}")
    private String caffeineSpec;

    /**
     * Cache names for different entities and operations
     */
    public static final String USER_CACHE = "users";
    public static final String SESSION_CACHE = "sessions";
    public static final String PRODUCT_CACHE = "products";
    public static final String CATEGORY_CACHE = "categories";
    public static final String BANNER_CACHE = "banners";
    public static final String AUTH_TOKEN_CACHE = "authTokens";
    public static final String SETTINGS_CACHE = "settings";

    /**
     * Configure Caffeine cache manager
     */
    @Bean
    public CacheManager cacheManager() {
        CaffeineCacheManager cacheManager = new CaffeineCacheManager();
        
        // Set default cache configuration
        cacheManager.setCaffeine(Caffeine.newBuilder()
            .maximumSize(500)
            .expireAfterAccess(10, TimeUnit.MINUTES)
            .recordStats());

        // Register cache names
        cacheManager.setCacheNames(Arrays.asList(
            USER_CACHE,
            SESSION_CACHE,
            PRODUCT_CACHE,
            CATEGORY_CACHE,
            BANNER_CACHE,
            AUTH_TOKEN_CACHE,
            SETTINGS_CACHE
        ));

        // Configure specific caches
        configureCaches(cacheManager);

        return cacheManager;
    }

    /**
     * Configure specific caches with different settings
     */
    private void configureCaches(CaffeineCacheManager cacheManager) {
        // User cache - longer expiration for frequently accessed data
        cacheManager.registerCustomCache(USER_CACHE, Caffeine.newBuilder()
            .maximumSize(1000)
            .expireAfterWrite(30, TimeUnit.MINUTES)
            .recordStats()
            .build());

        // Session cache - shorter expiration for security
        cacheManager.registerCustomCache(SESSION_CACHE, Caffeine.newBuilder()
            .maximumSize(10000)
            .expireAfterWrite(5, TimeUnit.MINUTES)
            .recordStats()
            .build());

        // Product cache - larger size for product catalog
        cacheManager.registerCustomCache(PRODUCT_CACHE, Caffeine.newBuilder()
            .maximumSize(5000)
            .expireAfterWrite(1, TimeUnit.HOURS)
            .recordStats()
            .build());

        // Category cache - small size, infrequently updated
        cacheManager.registerCustomCache(CATEGORY_CACHE, Caffeine.newBuilder()
            .maximumSize(100)
            .expireAfterWrite(2, TimeUnit.HOURS)
            .recordStats()
            .build());

        // Banner cache - small size, frequently accessed
        cacheManager.registerCustomCache(BANNER_CACHE, Caffeine.newBuilder()
            .maximumSize(50)
            .expireAfterWrite(15, TimeUnit.MINUTES)
            .recordStats()
            .build());

        // Auth token cache - security sensitive
        cacheManager.registerCustomCache(AUTH_TOKEN_CACHE, Caffeine.newBuilder()
            .maximumSize(10000)
            .expireAfterWrite(1, TimeUnit.MINUTES)
            .recordStats()
            .build());

        // Settings cache - small size, infrequently updated
        cacheManager.registerCustomCache(SETTINGS_CACHE, Caffeine.newBuilder()
            .maximumSize(100)
            .expireAfterWrite(1, TimeUnit.HOURS)
            .recordStats()
            .build());
    }

    /**
     * Configure cache metrics reporter
     */
//    @Bean
//    public com.github.benmanes.caffeine.cache.stats.CacheStats cacheMetrics(CacheManager cacheManager) {
//        CaffeineCacheManager caffeineCacheManager = (CaffeineCacheManager) cacheManager;
//        return caffeineCacheManager.getCacheNames().stream()
//            .map(caffeineCacheManager::getCache)
//            .map(cache -> ((com.github.benmanes.caffeine.cache.Cache<?, ?>) cache.getNativeCache()).stats())
//            .reduce(new com.github.benmanes.caffeine.cache.stats.CacheStats(0, 0, 0, 0, 0, 0, 0),
//                com.github.benmanes.caffeine.cache.stats.CacheStats::plus);
//    }

    /**
     * Cache key generator for complex keys
     */
    @Bean
    public org.springframework.cache.interceptor.KeyGenerator customKeyGenerator() {
        return (target, method, params) -> {
            StringBuilder key = new StringBuilder();
            key.append(target.getClass().getSimpleName());
            key.append(":");
            key.append(method.getName());
            key.append(":");
            for (Object param : params) {
                key.append(param.toString());
                key.append("_");
            }
            return key.toString();
        };
    }

    /**
     * Cache error handler
     */
    @Bean
    public org.springframework.cache.interceptor.CacheErrorHandler cacheErrorHandler() {
        return new org.springframework.cache.interceptor.SimpleCacheErrorHandler() {
            @Override
            public void handleCacheGetError(RuntimeException exception, org.springframework.cache.Cache cache, Object key) {
                logger.error("Error getting from cache: " + cache.getName(), exception);
                super.handleCacheGetError(exception, cache, key);
            }

            @Override
            public void handleCachePutError(RuntimeException exception, org.springframework.cache.Cache cache, Object key, Object value) {
                logger.error("Error putting in cache: " + cache.getName(), exception);
                super.handleCachePutError(exception, cache, key, value);
            }

            @Override
            public void handleCacheEvictError(RuntimeException exception, org.springframework.cache.Cache cache, Object key) {
                logger.error("Error evicting from cache: " + cache.getName(), exception);
                super.handleCacheEvictError(exception, cache, key);
            }

            @Override
            public void handleCacheClearError(RuntimeException exception, org.springframework.cache.Cache cache) {
                logger.error("Error clearing cache: " + cache.getName(), exception);
                super.handleCacheClearError(exception, cache);
            }
        };
    }
}
