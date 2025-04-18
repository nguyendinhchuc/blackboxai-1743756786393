package com.ecommerce.api.event;

import com.ecommerce.api.config.RevisionNotificationConfig;
import com.ecommerce.api.model.Revision;
import com.ecommerce.api.validator.RevisionNotificationValidator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

@Slf4j
@Component
@RequiredArgsConstructor
public class RevisionNotificationPublisher {

    private final ApplicationEventPublisher eventPublisher;
    private final RevisionNotificationConfig notificationConfig;
    private final RevisionNotificationValidator notificationValidator;

    /**
     * Publish revision created event
     */
    public void publishRevisionCreated(Revision revision) {
        if (!notificationConfig.isEmailEnabled()) {
            log.debug("Email notifications are disabled. Skipping revision created event.");
            return;
        }

        try {
            List<String> recipients = notificationValidator.validateAndGetRecipients(
                RevisionNotificationConfig.NotificationLevel.INFO
            );

            RevisionNotificationEvent event = RevisionNotificationEvent.revisionCreated(
                this, revision, recipients
            );

            eventPublisher.publishEvent(event);
            log.debug("Published revision created event for revision {}", revision.getId());
        } catch (Exception e) {
            log.error("Error publishing revision created event: {}", e.getMessage(), e);
        }
    }

    /**
     * Publish revision updated event
     */
    public void publishRevisionUpdated(Revision revision) {
        if (!notificationConfig.isEmailEnabled()) {
            log.debug("Email notifications are disabled. Skipping revision updated event.");
            return;
        }

        try {
            List<String> recipients = notificationValidator.validateAndGetRecipients(
                RevisionNotificationConfig.NotificationLevel.INFO
            );

            RevisionNotificationEvent event = RevisionNotificationEvent.revisionUpdated(
                this, revision, recipients
            );

            eventPublisher.publishEvent(event);
            log.debug("Published revision updated event for revision {}", revision.getId());
        } catch (Exception e) {
            log.error("Error publishing revision updated event: {}", e.getMessage(), e);
        }
    }

    /**
     * Publish revision deleted event
     */
    public void publishRevisionDeleted(Revision revision) {
        if (!notificationConfig.isEmailEnabled()) {
            log.debug("Email notifications are disabled. Skipping revision deleted event.");
            return;
        }

        try {
            List<String> recipients = notificationValidator.validateAndGetRecipients(
                RevisionNotificationConfig.NotificationLevel.WARNING
            );

            RevisionNotificationEvent event = RevisionNotificationEvent.revisionDeleted(
                this, revision, recipients
            );

            eventPublisher.publishEvent(event);
            log.debug("Published revision deleted event for revision {}", revision.getId());
        } catch (Exception e) {
            log.error("Error publishing revision deleted event: {}", e.getMessage(), e);
        }
    }

    /**
     * Publish revision error event
     */
    public void publishRevisionError(Revision revision, String error, String stackTrace) {
        if (!notificationConfig.isEmailEnabled()) {
            log.debug("Email notifications are disabled. Skipping revision error event.");
            return;
        }

        try {
            List<String> recipients = notificationValidator.validateAndGetRecipients(
                RevisionNotificationConfig.NotificationLevel.ERROR
            );

            RevisionNotificationEvent event = RevisionNotificationEvent.revisionError(
                this, revision, recipients, error, stackTrace
            );

            eventPublisher.publishEvent(event);
            log.debug("Published revision error event for revision {}", revision.getId());
        } catch (Exception e) {
            log.error("Error publishing revision error event: {}", e.getMessage(), e);
        }
    }

    /**
     * Publish cleanup completed event
     */
    public void publishCleanupCompleted(int deletedCount, String reason) {
        if (!notificationConfig.isEmailEnabled()) {
            log.debug("Email notifications are disabled. Skipping cleanup completed event.");
            return;
        }

        try {
            List<String> recipients = notificationValidator.validateAndGetRecipients(
                RevisionNotificationConfig.NotificationLevel.INFO
            );

            RevisionNotificationEvent event = RevisionNotificationEvent.cleanupCompleted(
                this, recipients, deletedCount, reason
            );

            eventPublisher.publishEvent(event);
            log.debug("Published cleanup completed event: {} revisions deleted", deletedCount);
        } catch (Exception e) {
            log.error("Error publishing cleanup completed event: {}", e.getMessage(), e);
        }
    }

    /**
     * Publish system alert event
     */
    public void publishSystemAlert(String message, String details) {
        if (!notificationConfig.isEmailEnabled()) {
            log.debug("Email notifications are disabled. Skipping system alert event.");
            return;
        }

        try {
            List<String> recipients = notificationValidator.validateAndGetRecipients(
                RevisionNotificationConfig.NotificationLevel.WARNING
            );

            RevisionNotificationEvent event = RevisionNotificationEvent.systemAlert(
                this, recipients, message, details
            );

            eventPublisher.publishEvent(event);
            log.debug("Published system alert event: {}", message);
        } catch (Exception e) {
            log.error("Error publishing system alert event: {}", e.getMessage(), e);
        }
    }

    /**
     * Publish excess revisions event
     */
    public void publishExcessRevisions(Revision revision, int currentCount, int maxAllowed) {
        if (!notificationConfig.isEmailEnabled()) {
            log.debug("Email notifications are disabled. Skipping excess revisions event.");
            return;
        }

        try {
            List<String> recipients = notificationValidator.validateAndGetRecipients(
                RevisionNotificationConfig.NotificationLevel.WARNING
            );

            RevisionNotificationEvent event = RevisionNotificationEvent.excessRevisions(
                this, revision, recipients, currentCount, maxAllowed
            );

            eventPublisher.publishEvent(event);
            log.debug("Published excess revisions event for entity {}", 
                revision.getEntityName());
        } catch (Exception e) {
            log.error("Error publishing excess revisions event: {}", e.getMessage(), e);
        }
    }

    /**
     * Publish custom notification event
     */
    public void publishCustomNotification(String subject, String content, 
            List<String> recipients, Map<String, Object> additionalData) {
        if (!notificationConfig.isEmailEnabled()) {
            log.debug("Email notifications are disabled. Skipping custom notification event.");
            return;
        }

        try {
            notificationValidator.validateEmailRecipients(recipients);
            notificationValidator.validateSubject(subject);
            notificationValidator.validateContent(content);

            RevisionNotificationEvent event = new RevisionNotificationEvent(
                this,
                RevisionNotificationEvent.NotificationType.SYSTEM_ALERT,
                null,
                recipients,
                RevisionNotificationConfig.NotificationLevel.INFO,
                Map.of(
                    "message", subject,
                    "details", content,
                    "additionalData", additionalData
                )
            );

            eventPublisher.publishEvent(event);
            log.debug("Published custom notification event: {}", subject);
        } catch (Exception e) {
            log.error("Error publishing custom notification event: {}", e.getMessage(), e);
        }
    }
}
