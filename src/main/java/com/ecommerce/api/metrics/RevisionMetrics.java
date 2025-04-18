package com.ecommerce.api.metrics;

import com.ecommerce.api.constant.RevisionConstants;
import com.ecommerce.api.model.Revision;
import io.micrometer.core.instrument.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import jakarta.annotation.PostConstruct;
import java.util.concurrent.TimeUnit;

@Component
@RequiredArgsConstructor
public class RevisionMetrics {

    private final MeterRegistry meterRegistry;

    private Counter revisionsCreatedCounter;
    private Counter revisionsUpdatedCounter;
    private Counter revisionsDeletedCounter;
    private Counter revisionErrorsCounter;
    private Timer revisionProcessingTimer;
    private DistributionSummary revisionSizeHistogram;
    private Gauge revisionQueueSizeGauge;

    /**
     * Initialize metrics
     */
    @PostConstruct
    public void initMetrics() {
        // Counters
        revisionsCreatedCounter = Counter.builder(RevisionConstants.METRIC_CREATED)
            .description("Number of revisions created")
            .tag("type", "created")
            .register(meterRegistry);

        revisionsUpdatedCounter = Counter.builder(RevisionConstants.METRIC_UPDATED)
            .description("Number of revisions updated")
            .tag("type", "updated")
            .register(meterRegistry);

        revisionsDeletedCounter = Counter.builder(RevisionConstants.METRIC_DELETED)
            .description("Number of revisions deleted")
            .tag("type", "deleted")
            .register(meterRegistry);

        revisionErrorsCounter = Counter.builder(RevisionConstants.METRIC_ERRORS)
            .description("Number of revision processing errors")
            .tag("type", "error")
            .register(meterRegistry);

        // Timer
        revisionProcessingTimer = Timer.builder(RevisionConstants.METRIC_PROCESSING_TIME)
            .description("Time taken to process revisions")
            .tag("type", "processing")
            .register(meterRegistry);

        // Histogram
        revisionSizeHistogram = DistributionSummary.builder("revision.size")
            .description("Size distribution of revision changes")
            .baseUnit("bytes")
            .scale(100)
            .register(meterRegistry);

        // Queue size gauge
        revisionQueueSizeGauge = Gauge.builder("revision.queue.size", this, RevisionMetrics::getQueueSize)
            .description("Current size of revision processing queue")
            .register(meterRegistry);
    }

    /**
     * Record revision creation
     */
    public void recordRevisionCreated(Revision revision) {
        revisionsCreatedCounter.increment();
        recordRevisionSize(revision);
        recordEntityMetric(revision, "created");
    }

    /**
     * Record revision update
     */
    public void recordRevisionUpdated(Revision revision) {
        revisionsUpdatedCounter.increment();
        recordRevisionSize(revision);
        recordEntityMetric(revision, "updated");
    }

    /**
     * Record revision deletion
     */
    public void recordRevisionDeleted(Revision revision) {
        revisionsDeletedCounter.increment();
        recordEntityMetric(revision, "deleted");
    }

    /**
     * Record revision error
     */
    public void recordRevisionError(String entityName, String errorType) {
        revisionErrorsCounter.increment();
        meterRegistry.counter("revision.errors",
            "entity", entityName,
            "error", errorType
        ).increment();
    }

    /**
     * Record revision processing time
     */
    public Timer.Sample startRevisionProcessing() {
        return Timer.start(meterRegistry);
    }

    /**
     * Stop revision processing timer
     */
    public void stopRevisionProcessing(Timer.Sample sample, String entityName) {
        sample.stop(Timer.builder(RevisionConstants.METRIC_PROCESSING_TIME)
            .tag("entity", entityName)
            .register(meterRegistry));
    }

    /**
     * Record revision size
     */
    private void recordRevisionSize(Revision revision) {
        if (revision.getChanges() != null) {
            revisionSizeHistogram.record(revision.getChanges().length());
        }
    }

    /**
     * Record entity-specific metric
     */
    private void recordEntityMetric(Revision revision, String action) {
        meterRegistry.counter("revision.entity",
            "entity", revision.getEntityName(),
            "action", action
        ).increment();
    }

    /**
     * Get queue size (placeholder implementation)
     */
    private double getQueueSize() {
        // Implement actual queue size measurement
        return 0.0;
    }

    /**
     * Record revision compression ratio
     */
    public void recordCompressionRatio(double ratio) {
        meterRegistry.gauge("revision.compression.ratio", ratio);
    }

    /**
     * Record cleanup metrics
     */
    public void recordCleanup(int deletedCount, String reason) {
        meterRegistry.counter("revision.cleanup",
            "reason", reason
        ).increment(deletedCount);
    }

    /**
     * Record revision validation
     */
    public void recordValidation(boolean success, String entityName) {
        meterRegistry.counter("revision.validation",
            "entity", entityName,
            "result", success ? "success" : "failure"
        ).increment();
    }

    /**
     * Record cache metrics
     */
    public void recordCacheHit(String cacheType) {
        meterRegistry.counter("revision.cache.hits",
            "type", cacheType
        ).increment();
    }

    public void recordCacheMiss(String cacheType) {
        meterRegistry.counter("revision.cache.misses",
            "type", cacheType
        ).increment();
    }

    /**
     * Record user metrics
     */
    public void recordUserAction(String username, String action) {
        meterRegistry.counter("revision.user.actions",
            "user", username,
            "action", action
        ).increment();
    }

    /**
     * Record API metrics
     */
    public void recordApiCall(String endpoint, String method, int statusCode) {
        meterRegistry.counter("revision.api.calls",
            "endpoint", endpoint,
            "method", method,
            "status", String.valueOf(statusCode)
        ).increment();
    }

    /**
     * Record performance metrics
     */
    public void recordQueryPerformance(String queryType, long durationMs) {
        meterRegistry.timer("revision.query.performance",
            "type", queryType
        ).record(durationMs, TimeUnit.MILLISECONDS);
    }
}
