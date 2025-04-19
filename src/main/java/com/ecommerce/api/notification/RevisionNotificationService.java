package com.ecommerce.api.notification;

import com.ecommerce.api.cache.RevisionNotificationCache;
import com.ecommerce.api.config.RevisionNotificationConfig;
import com.ecommerce.api.exception.RevisionNotificationException;
import com.ecommerce.api.metrics.RevisionNotificationMetrics;
import com.ecommerce.api.model.Revision;
import com.ecommerce.api.validator.RevisionNotificationValidator;
import io.micrometer.core.instrument.Timer;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.thymeleaf.context.Context;
import org.thymeleaf.spring6.SpringTemplateEngine;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

@Slf4j
@Service
@RequiredArgsConstructor
public class RevisionNotificationService {

    private final JavaMailSender mailSender;
    private final SpringTemplateEngine templateEngine;
    private final RevisionNotificationConfig notificationConfig;
    private final RevisionNotificationValidator notificationValidator;
    private final RevisionNotificationCache notificationCache;
    private final RevisionNotificationMetrics notificationMetrics;

    /**
     * Send revision notification
     */
    @Async
    public CompletableFuture<Void> sendRevisionNotification(Revision revision,
                                                            NotificationType type, List<String> recipients) {
        Timer.Sample timer = notificationMetrics.startNotificationTimer();
        String notificationId = generateNotificationId();

        try {
            log.debug("Sending revision notification: type={}, revision={}", type, revision.getId());
            notificationValidator.validateEmailConfiguration();
            notificationValidator.validateEmailRecipients(recipients);

            // Check rate limit
            if (!notificationCache.checkRateLimit(recipients.get(0), 100, Duration.ofHours(1))) {
                throw RevisionNotificationException.rateLimitExceeded(recipients.get(0));
            }

            // Prepare template data
            Map<String, Object> templateData = prepareRevisionTemplateData(revision, type);
            String subject = generateSubject(type, revision);

            // Process template
            String content = processTemplate("revision-notification", templateData);

            // Send email
            sendEmail(recipients, subject, content, notificationId);

            // Record metrics
            notificationMetrics.recordNotificationSent(type.name(), "INFO");
            timer.stop(notificationMetrics.getNotificationDeliveryTimer());

            log.info("Revision notification sent successfully: id={}, type={}",
                    notificationId, type);
            return CompletableFuture.completedFuture(null);
        } catch (Exception e) {
            handleNotificationError(e, notificationId, type, timer);
            return CompletableFuture.failedFuture(e);
        }
    }

    /**
     * Send system alert
     */
    @Async
    public CompletableFuture<Void> sendSystemAlert(String message, String details,
                                                   List<String> recipients) {
        Timer.Sample timer = notificationMetrics.startNotificationTimer();
        String notificationId = generateNotificationId();

        try {
            log.debug("Sending system alert: message={}", message);
            notificationValidator.validateEmailConfiguration();
            notificationValidator.validateEmailRecipients(recipients);

            // Prepare template data
            Map<String, Object> templateData = Map.of(
                    "message", message,
                    "details", details,
                    "timestamp", LocalDateTime.now()
            );

            // Process template
            String content = processTemplate("system-alert", templateData);

            // Send email
            sendEmail(recipients, "System Alert: " + message, content, notificationId);

            // Record metrics
            notificationMetrics.recordNotificationSent("SYSTEM_ALERT", "WARNING");
            timer.stop(notificationMetrics.getNotificationDeliveryTimer());

            log.info("System alert sent successfully: id={}", notificationId);
            return CompletableFuture.completedFuture(null);
        } catch (Exception e) {
            handleNotificationError(e, notificationId, NotificationType.SYSTEM_ALERT, timer);
            return CompletableFuture.failedFuture(e);
        }
    }

    /**
     * Send error notification
     */
    @Async
    public CompletableFuture<Void> sendErrorNotification(String error, String stackTrace,
                                                         List<String> recipients) {
        Timer.Sample timer = notificationMetrics.startNotificationTimer();
        String notificationId = generateNotificationId();

        try {
            log.debug("Sending error notification: error={}", error);
            notificationValidator.validateEmailConfiguration();
            notificationValidator.validateEmailRecipients(recipients);

            // Prepare template data
            Map<String, Object> templateData = Map.of(
                    "error", error,
                    "stackTrace", stackTrace,
                    "timestamp", LocalDateTime.now()
            );

            // Process template
            String content = processTemplate("error-alert", templateData);

            // Send email
            sendEmail(recipients, "Error Alert: " + error, content, notificationId);

            // Record metrics
            notificationMetrics.recordNotificationSent("ERROR_ALERT", "ERROR");
            timer.stop(notificationMetrics.getNotificationDeliveryTimer());

            log.info("Error notification sent successfully: id={}", notificationId);
            return CompletableFuture.completedFuture(null);
        } catch (Exception e) {
            handleNotificationError(e, notificationId, NotificationType.ERROR_ALERT, timer);
            return CompletableFuture.failedFuture(e);
        }
    }

    /**
     * Send cleanup notification
     */
    @Async
    public CompletableFuture<Void> sendCleanupNotification(Integer deletedCount, String reason,
                                                           List<String> recipients) {
        Timer.Sample timer = notificationMetrics.startNotificationTimer();
        String notificationId = generateNotificationId();

        try {
            log.debug("Sending cleanup notification: deletedCount={}", deletedCount);
            notificationValidator.validateEmailConfiguration();
            notificationValidator.validateEmailRecipients(recipients);

            // Prepare template data
            Map<String, Object> templateData = Map.of(
                    "deletedCount", deletedCount,
                    "reason", reason,
                    "timestamp", LocalDateTime.now()
            );

            // Process template
            String content = processTemplate("cleanup-report", templateData);

            // Send email
            sendEmail(recipients, "Cleanup Report: " + deletedCount + " Revisions Deleted",
                    content, notificationId);

            // Record metrics
            notificationMetrics.recordNotificationSent("CLEANUP_REPORT", "INFO");
            timer.stop(notificationMetrics.getNotificationDeliveryTimer());

            log.info("Cleanup notification sent successfully: id={}", notificationId);
            return CompletableFuture.completedFuture(null);
        } catch (Exception e) {
            handleNotificationError(e, notificationId, NotificationType.CLEANUP_REPORT, timer);
            return CompletableFuture.failedFuture(e);
        }
    }

    /**
     * Process template
     */
    private String processTemplate(String templateName, Map<String, Object> data) {
        try {
            Context context = new Context();
            context.setVariables(data);
            return templateEngine.process(templateName, context);
        } catch (Exception e) {
            throw RevisionNotificationException.templateProcessingFailed(templateName,
                    e.getMessage(), e);
        }
    }

    /**
     * Send email
     */
    private void sendEmail(List<String> recipients, String subject, String content,
                           String notificationId) throws MessagingException {
        MimeMessage message = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

        helper.setFrom(notificationConfig.getEmail().getFrom());
        helper.setTo(recipients.toArray(new String[0]));
        helper.setSubject(subject);
        helper.setText(content, true);

        mailSender.send(message);
        notificationCache.updateDeliveryStatus(notificationId, "DELIVERED", null);
    }

    /**
     * Handle notification error
     */
    private void handleNotificationError(Exception e, String notificationId,
                                         NotificationType type, Timer.Sample timer) {
        log.error("Error sending notification: id={}, type={}, error={}",
                notificationId, type, e.getMessage(), e);

        notificationMetrics.recordNotificationError(type.name(), e.getClass().getSimpleName());
        timer.stop(notificationMetrics.getNotificationDeliveryTimer());

        notificationCache.updateDeliveryStatus(notificationId, "FAILED", e.getMessage());

        if (e instanceof RevisionNotificationException) {
            throw (RevisionNotificationException) e;
        }
        throw RevisionNotificationException.emailSendingFailed(
                notificationId, e.getMessage(), e);
    }

    /**
     * Prepare revision template data
     */
    private Map<String, Object> prepareRevisionTemplateData(Revision revision,
                                                            NotificationType type) {
        Map<String, Object> data = new HashMap<>();
        data.put("revision", revision);
        data.put("type", type);
        data.put("timestamp", LocalDateTime.now());
        data.put("changes", getRevisionChanges(revision));
        return data;
    }

    /**
     * Get revision changes
     */
    private Map<String, Object> getRevisionChanges(Revision revision) {
        // Implementation would extract and format revision changes
        return Map.of();
    }

    /**
     * Generate notification subject
     */
    private String generateSubject(NotificationType type, Revision revision) {
        return switch (type) {
            case REVISION_CREATED -> "New Revision Created: " + revision.getEntityName();
            case REVISION_UPDATED -> "Revision Updated: " + revision.getEntityName();
            case REVISION_DELETED -> "Revision Deleted: " + revision.getEntityName();
            default -> "Revision Notification: " + revision.getEntityName();
        };
    }

    /**
     * Generate notification ID
     */
    private String generateNotificationId() {
        return "notif_" + System.currentTimeMillis() + "_" +
                String.format("%04d", (int) (Math.random() * 10000));
    }

    /**
     * Get notification statistics
     */
    public Map<String, Object> getNotificationStats() {
        return Map.of(
                "totalSent", notificationMetrics.getTotalNotificationsSent(),
                "totalErrors", notificationMetrics.getTotalNotificationErrors(),
                "averageDeliveryTime", notificationMetrics.getAverageDeliveryTime(),
                "successRate", notificationMetrics.getDeliverySuccessRate(),
                "cacheStats", notificationCache.getCacheStatistics()
        );
    }

    public long getTotalNotificationsSent() {
        return notificationMetrics.getTotalNotificationsSent();
    }

    public long getTotalNotificationErrors() {
        return notificationMetrics.getTotalNotificationErrors();
    }

    public double getAverageDeliveryTime() {
        return notificationMetrics.getAverageDeliveryTime();
    }

    public double getDeliverySuccessRate() {
        return notificationMetrics.getDeliverySuccessRate();
    }

    /**
     * Notification type enum
     */
    public enum NotificationType {
        REVISION_CREATED,
        REVISION_UPDATED,
        REVISION_DELETED,
        SYSTEM_ALERT,
        ERROR_ALERT,
        CLEANUP_REPORT
    }
}
