package com.ecommerce.api.event;

import com.ecommerce.api.notification.RevisionNotificationService;
import com.ecommerce.api.scheduler.RevisionNotificationScheduler;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class RevisionNotificationEventListener {

    private final RevisionNotificationService notificationService;
    private final RevisionNotificationScheduler notificationScheduler;

    /**
     * Handle revision notification events
     */
    @Async
    @EventListener
    public void handleRevisionNotificationEvent(RevisionNotificationEvent event) {
        try {
            log.debug("Processing notification event: {}", event.getType());
            notificationScheduler.incrementNotificationCount();

            switch (event.getType()) {
                case REVISION_CREATED -> handleRevisionCreated(event);
                case REVISION_UPDATED -> handleRevisionUpdated(event);
                case REVISION_DELETED -> handleRevisionDeleted(event);
                case REVISION_ERROR -> handleRevisionError(event);
                case CLEANUP_COMPLETED -> handleCleanupCompleted(event);
                case SYSTEM_ALERT -> handleSystemAlert(event);
                case EXCESS_REVISIONS -> handleExcessRevisions(event);
            }

            log.debug("Notification event processed successfully: {}", event.getType());
        } catch (Exception e) {
            log.error("Error processing notification event: {}", e.getMessage(), e);
            notificationScheduler.incrementErrorCount();
            handleNotificationError(event, e);
        }
    }

    /**
     * Handle revision created event
     */
    private void handleRevisionCreated(RevisionNotificationEvent event) {
        notificationService.sendRevisionNotification(
            event.getRevision(),
            RevisionNotificationService.NotificationType.REVISION_CREATED,
            event.getRecipients()
        );
    }

    /**
     * Handle revision updated event
     */
    private void handleRevisionUpdated(RevisionNotificationEvent event) {
        notificationService.sendRevisionNotification(
            event.getRevision(),
            RevisionNotificationService.NotificationType.REVISION_UPDATED,
            event.getRecipients()
        );
    }

    /**
     * Handle revision deleted event
     */
    private void handleRevisionDeleted(RevisionNotificationEvent event) {
        notificationService.sendRevisionNotification(
            event.getRevision(),
            RevisionNotificationService.NotificationType.REVISION_DELETED,
            event.getRecipients()
        );
    }

    /**
     * Handle revision error event
     */
    private void handleRevisionError(RevisionNotificationEvent event) {
        String error = event.getAdditionalData("error", String.class);
        String stackTrace = event.getAdditionalData("stackTrace", String.class);
        
        notificationService.sendErrorNotification(
            error,
            stackTrace,
            event.getRecipients()
        );
    }

    /**
     * Handle cleanup completed event
     */
    private void handleCleanupCompleted(RevisionNotificationEvent event) {
        Integer deletedCount = event.getAdditionalData("deletedCount", Integer.class);
        String reason = event.getAdditionalData("reason", String.class);
        
        notificationService.sendCleanupNotification(
            deletedCount,
            reason,
            event.getRecipients()
        );
    }

    /**
     * Handle system alert event
     */
    private void handleSystemAlert(RevisionNotificationEvent event) {
        String message = event.getAdditionalData("message", String.class);
        String details = event.getAdditionalData("details", String.class);
        
        notificationService.sendSystemAlert(
            message,
            details,
            event.getRecipients()
        );
    }

    /**
     * Handle excess revisions event
     */
    private void handleExcessRevisions(RevisionNotificationEvent event) {
        Integer currentCount = event.getAdditionalData("currentCount", Integer.class);
        Integer maxAllowed = event.getAdditionalData("maxAllowed", Integer.class);
        
        String message = String.format(
            "Excess revisions detected for %s #%d: %d/%d",
            event.getRevision().getEntityName(),
            event.getRevision().getEntityId(),
            currentCount,
            maxAllowed
        );
        
        notificationService.sendSystemAlert(
            "Excess Revisions Alert",
            message,
            event.getRecipients()
        );
    }

    /**
     * Handle notification error
     */
    private void handleNotificationError(RevisionNotificationEvent event, Exception e) {
        try {
            String errorMessage = String.format(
                "Error processing notification event %s: %s",
                event.getType(),
                e.getMessage()
            );
            
            notificationService.sendErrorNotification(
                errorMessage,
                getStackTrace(e),
                event.getRecipients()
            );
        } catch (Exception ex) {
            log.error("Error sending error notification: {}", ex.getMessage(), ex);
        }
    }

    /**
     * Get stack trace as string
     */
    private String getStackTrace(Exception e) {
        StringBuilder sb = new StringBuilder();
        for (StackTraceElement element : e.getStackTrace()) {
            sb.append(element.toString()).append("\n");
        }
        return sb.toString();
    }
}
