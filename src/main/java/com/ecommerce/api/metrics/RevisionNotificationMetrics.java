package com.ecommerce.api.metrics;

import io.micrometer.core.instrument.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import jakarta.annotation.PostConstruct;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

@Slf4j
@Component
@RequiredArgsConstructor
public class RevisionNotificationMetrics {

    private final MeterRegistry meterRegistry;

    private Counter totalNotificationsSent;
    private Counter totalNotificationErrors;
    private Timer notificationDeliveryTimer;
    private AtomicInteger queueSize;
    private AtomicLong oldestMessageAge;
    // Removed notificationsByType and notificationsByLevel fields
    private Timer templateProcessingTimer;
    private Counter recipientCount;

    private static final String METRIC_PREFIX = "revision.notification";

    /**
     * Initialize metrics
     */
    @PostConstruct
    public void init() {
        // Total notifications sent
        totalNotificationsSent = Counter.builder(METRIC_PREFIX + ".sent")
                .description("Total number of notifications sent")
                .register(meterRegistry);

        // Total notification errors
        totalNotificationErrors = Counter.builder(METRIC_PREFIX + ".errors")
                .description("Total number of notification errors")
                .register(meterRegistry);

        // Notification delivery timer
        notificationDeliveryTimer = Timer.builder(METRIC_PREFIX + ".delivery.time")
                .description("Time taken to deliver notifications")
                .register(meterRegistry);

        // Queue size
        queueSize = meterRegistry.gauge(METRIC_PREFIX + ".queue.size",
                new AtomicInteger(0));

        // Oldest message age
        oldestMessageAge = meterRegistry.gauge(METRIC_PREFIX + ".queue.oldest.age",
                new AtomicLong(0));

        // Removed initialization of notificationsByType and notificationsByLevel

        // Template processing timer
        templateProcessingTimer = Timer.builder(METRIC_PREFIX + ".template.processing")
                .description("Time taken to process templates")
                .register(meterRegistry);

        // Recipient count
        recipientCount = Counter.builder(METRIC_PREFIX + ".recipients")
                .description("Number of notification recipients")
                .register(meterRegistry);

        log.info("Notification metrics initialized");
    }

    public Timer getNotificationDeliveryTimer() {
        return notificationDeliveryTimer;
    }

    /**
     * Record notification sent
     */
    public void recordNotificationSent(String type, String level) {
        totalNotificationsSent.increment();
        meterRegistry.counter(METRIC_PREFIX + ".by.type", "type", type).increment();
        meterRegistry.counter(METRIC_PREFIX + ".by.level", "level", level).increment();
    }

    /**
     * Record notification error
     */
    public void recordNotificationError(String type, String errorType) {
        totalNotificationErrors.increment();
        meterRegistry.counter(METRIC_PREFIX + ".error",
                "type", type,
                "error", errorType
        ).increment();
    }

    /**
     * Record delivery time
     */
    public void recordDeliveryTime(long milliseconds) {
        notificationDeliveryTimer.record(milliseconds, TimeUnit.MILLISECONDS);
    }

    /**
     * Update queue metrics
     */
    public void updateQueueMetrics(int size, long oldestAgeMillis) {
        queueSize.set(size);
        oldestMessageAge.set(oldestAgeMillis);
    }

    /**
     * Record template processing time
     */
    public void recordTemplateProcessing(long milliseconds) {
        templateProcessingTimer.record(milliseconds, TimeUnit.MILLISECONDS);
    }

    /**
     * Record recipients
     */
    public void recordRecipients(int count) {
        recipientCount.increment(count);
    }

    /**
     * Get total notifications sent
     */
    public long getTotalNotificationsSent() {
        return (long) totalNotificationsSent.count();
    }

    /**
     * Get total notification errors
     */
    public long getTotalNotificationErrors() {
        return (long) totalNotificationErrors.count();
    }

    /**
     * Get average delivery time
     */
    public double getAverageDeliveryTime() {
        return notificationDeliveryTimer.mean(TimeUnit.MILLISECONDS);
    }

    /**
     * Get current queue size
     */
    public int getCurrentQueueSize() {
        return queueSize.get();
    }

    /**
     * Get oldest message age
     */
    public long getOldestMessageAge() {
        return oldestMessageAge.get();
    }

    /**
     * Get notifications by type
     */
    public long getNotificationsByType(String type) {
        Counter counter = meterRegistry.find(METRIC_PREFIX + ".by.type").tag("type", type).counter();
        return counter == null ? 0 : (long) counter.count();
    }

    /**
     * Get notifications by level
     */
    public long getNotificationsByLevel(String level) {
        Counter counter = meterRegistry.find(METRIC_PREFIX + ".by.level").tag("level", level).counter();
        return counter == null ? 0 : (long) counter.count();
    }

    /**
     * Get average template processing time
     */
    public double getAverageTemplateProcessingTime() {
        return templateProcessingTimer.mean(TimeUnit.MILLISECONDS);
    }

    /**
     * Get total recipients
     */
    public long getTotalRecipients() {
        return (long) recipientCount.count();
    }

    /**
     * Get delivery success rate
     */
    public double getDeliverySuccessRate() {
        long total = getTotalNotificationsSent();
        long errors = getTotalNotificationErrors();
        return total == 0 ? 100.0 : ((total - errors) * 100.0) / total;
    }

    /**
     * Record notification metrics
     */
    public Timer.Sample startNotificationTimer() {
        return Timer.start(meterRegistry);
    }

    /**
     * Stop notification timer
     */
    public void stopNotificationTimer(Timer.Sample sample, String type, String level) {
        sample.stop(Timer.builder(METRIC_PREFIX + ".total")
                .tag("type", type)
                .tag("level", level)
                .register(meterRegistry));
    }

    /**
     * Record notification attempt
     */
    public void recordNotificationAttempt(String type, boolean success, long duration) {
        meterRegistry.timer(METRIC_PREFIX + ".attempt",
                "type", type,
                "success", String.valueOf(success)
        ).record(duration, TimeUnit.MILLISECONDS);
    }

    /**
     * Record retry attempt
     */
    public void recordRetryAttempt(String type, int attempt, boolean success) {
        meterRegistry.counter(METRIC_PREFIX + ".retry",
                "type", type,
                "attempt", String.valueOf(attempt),
                "success", String.valueOf(success)
        ).increment();
    }

    /**
     * Record batch size
     */
    public void recordBatchSize(int size) {
        meterRegistry.summary(METRIC_PREFIX + ".batch.size")
                .record(size);
    }

    /**
     * Record rate limit
     */
    public void recordRateLimit(String type) {
        meterRegistry.counter(METRIC_PREFIX + ".rate.limit",
                "type", type
        ).increment();
    }
}
