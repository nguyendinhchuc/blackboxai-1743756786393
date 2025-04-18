package com.ecommerce.api.scheduler;

import com.ecommerce.api.config.RevisionNotificationConfig;
import com.ecommerce.api.cache.RevisionNotificationCache;
import com.ecommerce.api.constant.RevisionNotificationConstants;
import com.ecommerce.api.event.RevisionNotificationPublisher;
import com.ecommerce.api.metrics.RevisionNotificationMetrics;
import com.ecommerce.api.notification.RevisionNotificationService;
import com.ecommerce.api.service.RevisionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

@Slf4j
@Component
@RequiredArgsConstructor
public class RevisionNotificationScheduler {

    private final RevisionService revisionService;
    private final RevisionNotificationService notificationService;
    private final RevisionNotificationPublisher notificationPublisher;
    private final RevisionNotificationCache notificationCache;
    private final RevisionNotificationConfig notificationConfig;
    private final RevisionNotificationMetrics notificationMetrics;

    private final AtomicInteger notificationCount = new AtomicInteger(0);
    private final AtomicInteger errorCount = new AtomicInteger(0);
    private final AtomicLong lastCleanupTime = new AtomicLong(System.currentTimeMillis());

    /**
     * Monitor notification metrics
     */
    @Scheduled(cron = RevisionNotificationConstants.HOURLY_METRICS_CRON)
    public void monitorMetrics() {
        try {
            log.debug("Running notification metrics monitoring");
            Map<String, Object> stats = notificationService.getNotificationStats();
            checkMetricsThresholds(stats);
            updateMetrics(stats);
            log.debug("Notification metrics monitoring completed");
        } catch (Exception e) {
            log.error("Error monitoring notification metrics: {}", e.getMessage(), e);
            errorCount.incrementAndGet();
        }
    }

    /**
     * Clean up notification cache
     */
    @Scheduled(cron = RevisionNotificationConstants.DAILY_CLEANUP_CRON)
    public void cleanupCache() {
        try {
            log.debug("Running notification cache cleanup");
            notificationCache.clearAllCaches();
            lastCleanupTime.set(System.currentTimeMillis());
            
            // Reset counters after cleanup
            notificationCount.set(0);
            errorCount.set(0);
            
            log.debug("Notification cache cleanup completed");
        } catch (Exception e) {
            log.error("Error cleaning up notification cache: {}", e.getMessage(), e);
            errorCount.incrementAndGet();
        }
    }

    /**
     * Monitor notification queue
     */
    @Scheduled(cron = RevisionNotificationConstants.RATE_LIMIT_CLEANUP_CRON)
    public void monitorNotificationQueue() {
        try {
            int queueSize = notificationMetrics.getCurrentQueueSize();
            Duration oldestMessage = Duration.ofMillis(notificationMetrics.getOldestMessageAge());
            
            if (queueSize > RevisionNotificationConstants.QUEUE_WARNING_SIZE || 
                oldestMessage.toMinutes() > RevisionNotificationConstants.QUEUE_WARNING_AGE_MINUTES) {
                String alert = String.format(
                    "Notification queue alert: Size=%d, Oldest message=%d minutes",
                    queueSize, oldestMessage.toMinutes()
                );
                
                notificationPublisher.publishSystemAlert("Queue Health Warning", alert);
            }
        } catch (Exception e) {
            log.error("Error monitoring notification queue: {}", e.getMessage(), e);
            errorCount.incrementAndGet();
        }
    }

    /**
     * Send weekly summary
     */
    @Scheduled(cron = RevisionNotificationConstants.WEEKLY_SUMMARY_CRON)
    public void sendWeeklySummary() {
        if (!notificationConfig.isEmailEnabled()) {
            log.debug("Email notifications are disabled. Skipping weekly summary.");
            return;
        }

        try {
            Map<String, Object> stats = notificationService.getNotificationStats();
            String summary = formatWeeklySummary(stats);
            
            notificationPublisher.publishSystemAlert(
                "Weekly Notification Summary",
                summary
            );

            log.info("Weekly summary notification sent successfully");
        } catch (Exception e) {
            log.error("Error sending weekly summary: {}", e.getMessage(), e);
            errorCount.incrementAndGet();
        }
    }

    /**
     * Check metrics thresholds
     */
    private void checkMetricsThresholds(Map<String, Object> stats) {
        double successRate = (double) stats.get("successRate");
        if (successRate < RevisionNotificationConstants.SUCCESS_RATE_CRITICAL) {
            notificationPublisher.publishSystemAlert(
                "Success Rate Critical",
                String.format("Current success rate: %.2f%%", successRate)
            );
        } else if (successRate < RevisionNotificationConstants.SUCCESS_RATE_WARNING) {
            notificationPublisher.publishSystemAlert(
                "Success Rate Warning",
                String.format("Current success rate: %.2f%%", successRate)
            );
        }

        double avgDeliveryTime = (double) stats.get("averageDeliveryTime");
        if (avgDeliveryTime > RevisionNotificationConstants.DELIVERY_TIME_CRITICAL) {
            notificationPublisher.publishSystemAlert(
                "Delivery Time Critical",
                String.format("Average delivery time: %.2f ms", avgDeliveryTime)
            );
        } else if (avgDeliveryTime > RevisionNotificationConstants.DELIVERY_TIME_WARNING) {
            notificationPublisher.publishSystemAlert(
                "Delivery Time Warning",
                String.format("Average delivery time: %.2f ms", avgDeliveryTime)
            );
        }
    }

    /**
     * Update metrics
     */
    private void updateMetrics(Map<String, Object> stats) {
        notificationMetrics.recordNotificationSent("TOTAL", "INFO");
        notificationMetrics.recordDeliveryTime((long) stats.get("averageDeliveryTime"));
        notificationMetrics.updateQueueMetrics(
            (int) stats.get("queueSize"),
            (long) stats.get("oldestMessageAge")
        );
    }

    /**
     * Format weekly summary
     */
    private String formatWeeklySummary(Map<String, Object> stats) {
        return String.format("""
            Weekly Notification Statistics:
            - Total Notifications: %d
            - Success Rate: %.2f%%
            - Average Delivery Time: %.2f ms
            - Total Errors: %d
            - Cache Hit Rate: %.2f%%
            """,
            stats.get("totalSent"),
            stats.get("successRate"),
            stats.get("averageDeliveryTime"),
            stats.get("totalErrors"),
            ((Map<String, Object>) stats.get("cacheStats")).get("hitRate")
        );
    }

    /**
     * Get notification count
     */
    public int getNotificationCount() {
        return notificationCount.get();
    }

    /**
     * Get error count
     */
    public int getErrorCount() {
        return errorCount.get();
    }

    /**
     * Get last cleanup time
     */
    public LocalDateTime getLastCleanupTime() {
        return LocalDateTime.now().minusMillis(
            System.currentTimeMillis() - lastCleanupTime.get()
        );
    }

    /**
     * Increment notification count
     */
    public void incrementNotificationCount() {
        notificationCount.incrementAndGet();
    }

    /**
     * Increment error count
     */
    public void incrementErrorCount() {
        errorCount.incrementAndGet();
    }
}
