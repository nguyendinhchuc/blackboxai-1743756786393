package com.ecommerce.api.cache;

import com.ecommerce.api.constant.RevisionConstants;
import com.ecommerce.api.metrics.RevisionMetrics;
import com.ecommerce.api.model.Revision;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.CacheConfig;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

@Slf4j
@Component
@RequiredArgsConstructor
@CacheConfig(cacheNames = RevisionConstants.REVISION_CACHE_NAME)
public class RevisionCache {

    private final CacheManager cacheManager;
    private final RevisionMetrics revisionMetrics;

    /**
     * Get revision by ID
     */
    @Cacheable(key = "'revision_' + #id")
    public Optional<Revision> getRevision(Long id) {
        revisionMetrics.recordCacheMiss("revision");
        return Optional.empty(); // Actual implementation will be provided by cache interceptor
    }

    /**
     * Get revisions by entity
     */
    @Cacheable(key = "'entity_' + #entityName + '_' + #entityId")
    public List<Revision> getRevisionsByEntity(String entityName, Long entityId) {
        revisionMetrics.recordCacheMiss("entity_revisions");
        return List.of(); // Actual implementation will be provided by cache interceptor
    }

    /**
     * Get latest revision by entity
     */
    @Cacheable(key = "'latest_' + #entityName + '_' + #entityId")
    public Optional<Revision> getLatestRevision(String entityName, Long entityId) {
        revisionMetrics.recordCacheMiss("latest_revision");
        return Optional.empty(); // Actual implementation will be provided by cache interceptor
    }

    /**
     * Cache revision
     */
    @Caching(evict = {
        @CacheEvict(key = "'entity_' + #revision.entityName + '_' + #revision.entityId"),
        @CacheEvict(key = "'latest_' + #revision.entityName + '_' + #revision.entityId")
    })
    public void cacheRevision(Revision revision) {
        getCache().put("revision_" + revision.getId(), revision);
        log.debug("Cached revision: {}", revision.getId());
    }

    /**
     * Evict revision from cache
     */
    @Caching(evict = {
        @CacheEvict(key = "'revision_' + #id"),
        @CacheEvict(key = "'entity_' + #entityName + '_' + #entityId"),
        @CacheEvict(key = "'latest_' + #entityName + '_' + #entityId")
    })
    public void evictRevision(Long id, String entityName, Long entityId) {
        log.debug("Evicted revision: {}", id);
    }

    /**
     * Evict all revisions for entity
     */
    public void evictEntityRevisions(String entityName, Long entityId) {
        getCache().evict("entity_" + entityName + "_" + entityId);
        getCache().evict("latest_" + entityName + "_" + entityId);
        log.debug("Evicted all revisions for entity: {} {}", entityName, entityId);
    }

    /**
     * Clear all caches
     */
    @CacheEvict(allEntries = true)
    @Scheduled(fixedRateString = "${cache.evict-all-rate:3600000}") // Default: 1 hour
    public void clearAllCaches() {
        log.info("Cleared all revision caches");
    }

    /**
     * Get cache statistics
     */
    public CacheStatistics getCacheStatistics() {
        org.springframework.cache.Cache cache = getCache();
        if (cache instanceof com.github.benmanes.caffeine.cache.Cache) {
            com.github.benmanes.caffeine.cache.Cache<Object, Object> caffeineCache = 
                (com.github.benmanes.caffeine.cache.Cache<Object, Object>) cache.getNativeCache();
            
            return new CacheStatistics(
                caffeineCache.estimatedSize(),
                caffeineCache.stats().hitCount(),
                caffeineCache.stats().missCount(),
                caffeineCache.stats().evictionCount()
            );
        }
        return new CacheStatistics(0, 0, 0, 0);
    }

    /**
     * Warm up cache
     */
    public void warmUpCache(List<Revision> revisions) {
        log.info("Warming up revision cache with {} revisions", revisions.size());
        revisions.forEach(this::cacheRevision);
    }

    /**
     * Get cache
     */
    private org.springframework.cache.Cache getCache() {
        return cacheManager.getCache(RevisionConstants.REVISION_CACHE_NAME);
    }

    /**
     * Cache statistics record
     */
    public record CacheStatistics(
        long size,
        long hits,
        long misses,
        long evictions
    ) {
        public double getHitRatio() {
            long total = hits + misses;
            return total == 0 ? 0.0 : (double) hits / total;
        }

        public String getHitRatioPercentage() {
            return String.format("%.2f%%", getHitRatio() * 100);
        }
    }

    /**
     * Check if entity is cached
     */
    public boolean isEntityCached(String entityName, Long entityId) {
        return getCache().get("entity_" + entityName + "_" + entityId) != null;
    }

    /**
     * Check if revision is cached
     */
    public boolean isRevisionCached(Long id) {
        return getCache().get("revision_" + id) != null;
    }

    /**
     * Pre-cache entity revisions
     */
    public void preCacheEntityRevisions(String entityName, Long entityId, List<Revision> revisions) {
        String cacheKey = "entity_" + entityName + "_" + entityId;
        getCache().put(cacheKey, revisions);
        
        if (!revisions.isEmpty()) {
            String latestKey = "latest_" + entityName + "_" + entityId;
            getCache().put(latestKey, revisions.get(0));
        }
        
        log.debug("Pre-cached {} revisions for entity: {} {}", revisions.size(), entityName, entityId);
    }

    /**
     * Update cached revision
     */
    @Caching(evict = {
        @CacheEvict(key = "'entity_' + #revision.entityName + '_' + #revision.entityId"),
        @CacheEvict(key = "'latest_' + #revision.entityName + '_' + #revision.entityId")
    })
    public void updateCachedRevision(Revision revision) {
        getCache().put("revision_" + revision.getId(), revision);
        log.debug("Updated cached revision: {}", revision.getId());
    }
}
