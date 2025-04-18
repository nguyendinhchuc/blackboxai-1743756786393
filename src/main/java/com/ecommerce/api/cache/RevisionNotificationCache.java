package com.ecommerce.api.cache;

import com.ecommerce.api.model.Revision;
import com.ecommerce.api.notification.RevisionNotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.CacheConfig;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Component
@RequiredArgsConstructor
@CacheConfig(cacheNames = "revisionNotifications")
public class RevisionNotificationCache {

    private final CacheManager cacheManager;
    private static final String TEMPLATE_CACHE = "notificationTemplates";
    private static final String RECIPIENT_CACHE = "notificationRecipients";
    private static final String STATS_CACHE = "notificationStats";
    private static final String DELIVERY_CACHE = "notificationDelivery";

    // In-memory cache for rate limiting
    private final Map<String, RateLimitInfo> rateLimitCache = new ConcurrentHashMap<>();

    /**
     * Cache notification template
     */
    @Cacheable(value = TEMPLATE_CACHE, key = "#templateName")
    public String getTemplate(String templateName) {
        log.debug("Loading template from source: {}", templateName);
        // Template loading logic would be implemented here
        return null;
    }

    /**
     * Cache notification recipients
     */
    @Cacheable(value = RECIPIENT_CACHE, key = "#type")
    public List<String> getRecipients(RevisionNotificationService.NotificationType type) {
        log.debug("Loading recipients for type: {}", type);
        // Recipient loading logic would be implemented here
        return null;
    }

    /**
     * Cache notification statistics
     */
    @Cacheable(value = STATS_CACHE, key = "#revision.id")
    public Map<String, Object> getNotificationStats(Revision revision) {
        log.debug("Loading notification stats for revision: {}", revision.getId());
        // Stats loading logic would be implemented here
        return null;
    }

    /**
     * Cache delivery status
     */
    @Cacheable(value = DELIVERY_CACHE, key = "#notificationId")
    public DeliveryStatus getDeliveryStatus(String notificationId) {
        log.debug("Loading delivery status for notification: {}", notificationId);
        // Delivery status loading logic would be implemented here
        return null;
    }

    /**
     * Clear template cache
     */
    @CacheEvict(value = TEMPLATE_CACHE, allEntries = true)
    public void clearTemplateCache() {
        log.debug("Clearing template cache");
    }

    /**
     * Clear recipient cache
     */
    @CacheEvict(value = RECIPIENT_CACHE, allEntries = true)
    public void clearRecipientCache() {
        log.debug("Clearing recipient cache");
    }

    /**
     * Clear stats cache
     */
    @CacheEvict(value = STATS_CACHE, allEntries = true)
    public void clearStatsCache() {
        log.debug("Clearing stats cache");
    }

    /**
     * Clear delivery cache
     */
    @CacheEvict(value = DELIVERY_CACHE, allEntries = true)
    public void clearDeliveryCache() {
        log.debug("Clearing delivery cache");
    }

    /**
     * Clear all caches
     */
    @Scheduled(cron = "0 0 0 * * ?") // Daily at midnight
    public void clearAllCaches() {
        log.info("Performing scheduled cache cleanup");
        clearTemplateCache();
        clearRecipientCache();
        clearStatsCache();
        clearDeliveryCache();
        cleanupRateLimitCache();
    }

    /**
     * Check rate limit
     */
    public boolean checkRateLimit(String recipient, int maxRequests, Duration window) {
        RateLimitInfo info = rateLimitCache.computeIfAbsent(recipient,
            k -> new RateLimitInfo(maxRequests));

        return info.checkAndIncrement(window);
    }

    /**
     * Clean up rate limit cache
     */
    private void cleanupRateLimitCache() {
        LocalDateTime now = LocalDateTime.now();
        rateLimitCache.entrySet().removeIf(entry ->
            Duration.between(entry.getValue().getLastRequest(), now).toHours() > 24);
    }

    /**
     * Rate limit info record
     */
    private static class RateLimitInfo {
        private final int maxRequests;
        private int requestCount;
        private LocalDateTime lastRequest;
        private LocalDateTime windowStart;

        public RateLimitInfo(int maxRequests) {
            this.maxRequests = maxRequests;
            this.requestCount = 0;
            this.lastRequest = LocalDateTime.now();
            this.windowStart = this.lastRequest;
        }

        public synchronized boolean checkAndIncrement(Duration window) {
            LocalDateTime now = LocalDateTime.now();
            if (Duration.between(windowStart, now).compareTo(window) > 0) {
                // Reset window
                windowStart = now;
                requestCount = 0;
            }

            if (requestCount >= maxRequests) {
                return false;
            }

            requestCount++;
            lastRequest = now;
            return true;
        }

        public LocalDateTime getLastRequest() {
            return lastRequest;
        }
    }

    /**
     * Delivery status record
     */
    public record DeliveryStatus(
        String notificationId,
        String recipient,
        RevisionNotificationService.NotificationType type,
        LocalDateTime sentTime,
        LocalDateTime deliveredTime,
        String status,
        String errorMessage
    ) {}

    /**
     * Cache metrics
     */
    public Map<String, Object> getCacheMetrics() {
        return Map.of(
            "templateCacheSize", getCacheSize(TEMPLATE_CACHE),
            "recipientCacheSize", getCacheSize(RECIPIENT_CACHE),
            "statsCacheSize", getCacheSize(STATS_CACHE),
            "deliveryCacheSize", getCacheSize(DELIVERY_CACHE),
            "rateLimitCacheSize", rateLimitCache.size()
        );
    }

    /**
     * Get cache size
     */
    private long getCacheSize(String cacheName) {
        return cacheManager.getCache(cacheName)
            .getNativeCache()
            .toString()
            .lines()
            .count();
    }

    /**
     * Warm up cache
     */
    public void warmUpCache() {
        log.info("Warming up notification cache");
        // Pre-load frequently used templates
        getTemplate("revision-notification");
        getTemplate("system-alert");
        getTemplate("cleanup-report");
        getTemplate("error-alert");

        // Pre-load recipient lists
        for (RevisionNotificationService.NotificationType type : 
                RevisionNotificationService.NotificationType.values()) {
            getRecipients(type);
        }

        log.info("Cache warm-up completed");
    }

    /**
     * Update delivery status
     */
    public void updateDeliveryStatus(String notificationId, String status, 
            String errorMessage) {
        DeliveryStatus current = getDeliveryStatus(notificationId);
        if (current != null) {
            DeliveryStatus updated = new DeliveryStatus(
                current.notificationId(),
                current.recipient(),
                current.type(),
                current.sentTime(),
                LocalDateTime.now(),
                status,
                errorMessage
            );
            cacheManager.getCache(DELIVERY_CACHE).put(notificationId, updated);
        }
    }

    /**
     * Get cache statistics
     */
    public Map<String, Object> getCacheStatistics() {
        return Map.of(
            "hitCount", getCacheHitCount(),
            "missCount", getCacheMissCount(),
            "evictionCount", getCacheEvictionCount(),
            "averageGetTime", getCacheAverageGetTime()
        );
    }

    private long getCacheHitCount() {
        return 0; // Implementation depends on cache provider
    }

    private long getCacheMissCount() {
        return 0; // Implementation depends on cache provider
    }

    private long getCacheEvictionCount() {
        return 0; // Implementation depends on cache provider
    }

    private double getCacheAverageGetTime() {
        return 0.0; // Implementation depends on cache provider
    }
}
