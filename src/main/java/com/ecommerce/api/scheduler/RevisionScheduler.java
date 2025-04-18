package com.ecommerce.api.scheduler;

import com.ecommerce.api.config.RevisionConfiguration;
import com.ecommerce.api.model.Revision;
import com.ecommerce.api.repository.RevisionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

@Slf4j
@Component
@RequiredArgsConstructor
public class RevisionScheduler {

    private final RevisionRepository revisionRepository;
    private final RevisionConfiguration revisionConfiguration;

    /**
     * Clean up old revisions based on retention period
     */
    @Scheduled(cron = "${revision.cleanup-schedule:0 0 0 * * ?}")
    @Transactional
    public void cleanupOldRevisions() {
        log.info("Starting revision cleanup task");
        AtomicInteger deletedCount = new AtomicInteger(0);

        try {
            long thresholdTimestamp = revisionConfiguration.getCleanupThresholdTimestamp();
            revisionRepository.deleteOldRevisions(thresholdTimestamp);
            
            log.info("Completed revision cleanup task. Deleted {} old revisions", 
                deletedCount.get());
        } catch (Exception e) {
            log.error("Error during revision cleanup task: {}", e.getMessage(), e);
        }
    }

    /**
     * Clean up excess revisions per entity
     */
    @Scheduled(cron = "${revision.excess-cleanup-schedule:0 0 1 * * ?}")
    @Transactional
    public void cleanupExcessRevisions() {
        log.info("Starting excess revision cleanup task");
        AtomicInteger deletedCount = new AtomicInteger(0);

        try {
            // Get all unique entity names and IDs
            List<Object[]> entityGroups = revisionRepository.findAll()
                .stream()
                .map(revision -> new Object[]{revision.getEntityName(), revision.getEntityId()})
                .distinct()
                .toList();

            // Process each entity group
            entityGroups.forEach(group -> {
                String entityName = (String) group[0];
                Long entityId = (Long) group[1];

                // Get revision count for this entity
                long revisionCount = revisionRepository.countByEntityNameAndEntityId(
                    entityName, entityId);

                // If count exceeds limit, delete oldest excess revisions
                if (revisionConfiguration.hasTooManyRevisions(revisionCount)) {
                    int toDelete = (int) (revisionCount - revisionConfiguration.getMaxRevisionsPerEntity());
                    List<Revision> oldestRevisions = revisionRepository
                        .findByEntityNameAndEntityId(entityName, entityId)
                        .stream()
                        .sorted((r1, r2) -> Long.compare(r1.getTimestamp(), r2.getTimestamp()))
                        .limit(toDelete)
                        .toList();

                    revisionRepository.deleteAll(oldestRevisions);
                    deletedCount.addAndGet(oldestRevisions.size());
                }
            });

            log.info("Completed excess revision cleanup task. Deleted {} excess revisions", 
                deletedCount.get());
        } catch (Exception e) {
            log.error("Error during excess revision cleanup task: {}", e.getMessage(), e);
        }
    }

    /**
     * Generate revision statistics
     */
    @Scheduled(cron = "${revision.stats-schedule:0 0 2 * * ?}")
    @Transactional(readOnly = true)
    public void generateRevisionStatistics() {
        log.info("Starting revision statistics generation");

        try {
            LocalDateTime now = LocalDateTime.now();
            LocalDateTime dayAgo = now.minusDays(1);
            LocalDateTime weekAgo = now.minusWeeks(1);
            LocalDateTime monthAgo = now.minusMonths(1);

            long dayTimestamp = dayAgo.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli();
            long weekTimestamp = weekAgo.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli();
            long monthTimestamp = monthAgo.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli();

            // Get counts for different time periods
            Map<String, Long> stats = Map.of(
                "total", revisionRepository.count(),
                "lastDay", countRevisionsSince(dayTimestamp),
                "lastWeek", countRevisionsSince(weekTimestamp),
                "lastMonth", countRevisionsSince(monthTimestamp)
            );

            // Get counts by revision type
            Map<Revision.RevisionType, Long> typeStats = Map.of(
                Revision.RevisionType.INSERT, 
                    revisionRepository.countByRevisionType(Revision.RevisionType.INSERT),
                Revision.RevisionType.UPDATE, 
                    revisionRepository.countByRevisionType(Revision.RevisionType.UPDATE),
                Revision.RevisionType.DELETE, 
                    revisionRepository.countByRevisionType(Revision.RevisionType.DELETE)
            );

            log.info("Revision Statistics:");
            log.info("Total Revisions: {}", stats.get("total"));
            log.info("Last 24 Hours: {}", stats.get("lastDay"));
            log.info("Last Week: {}", stats.get("lastWeek"));
            log.info("Last Month: {}", stats.get("lastMonth"));
            log.info("By Type: {}", typeStats);

        } catch (Exception e) {
            log.error("Error generating revision statistics: {}", e.getMessage(), e);
        }
    }

    /**
     * Count revisions since timestamp
     */
    private long countRevisionsSince(long timestamp) {
        return revisionRepository.findByDateRange(timestamp, System.currentTimeMillis()).size();
    }

    /**
     * Compress old revisions
     */
    @Scheduled(cron = "${revision.compression-schedule:0 0 3 * * ?}")
    @Transactional
    public void compressOldRevisions() {
        if (!revisionConfiguration.isCompressData()) {
            return;
        }

        log.info("Starting revision compression task");
        AtomicInteger compressedCount = new AtomicInteger(0);

        try {
            // Process revisions in batches
            int pageSize = 100;
            int pageNumber = 0;
            Page<Revision> page;

            do {
                page = revisionRepository.findAll(PageRequest.of(pageNumber, pageSize));
                
                page.getContent().forEach(revision -> {
                    String changes = revision.getChanges();
                    if (changes != null && changes.length() > revisionConfiguration.getCompressionThreshold()) {
                        String compressed = revisionConfiguration.revisionDataProcessor()
                            .processData(changes);
                        revision.setChanges(compressed);
                        revisionRepository.save(revision);
                        compressedCount.incrementAndGet();
                    }
                });

                pageNumber++;
            } while (page.hasNext());

            log.info("Completed revision compression task. Compressed {} revisions", 
                compressedCount.get());
        } catch (Exception e) {
            log.error("Error during revision compression task: {}", e.getMessage(), e);
        }
    }
}
