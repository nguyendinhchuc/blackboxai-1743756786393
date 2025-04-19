package com.ecommerce.api.event;

import com.ecommerce.api.config.RevisionNotificationConfig;
import com.ecommerce.api.model.Revision;
import lombok.AccessLevel;
import lombok.Getter;
import org.springframework.context.ApplicationEvent;

import java.util.List;
import java.util.Map;

@Getter
public class RevisionNotificationEvent extends ApplicationEvent {

    private final NotificationType type;
    private final Revision revision;
    private final List<String> recipients;
    private final Map<String, Object> additionalData;
    private final RevisionNotificationConfig.NotificationLevel level;
    @Getter(AccessLevel.NONE)
    public final long timestamp;

    /**
     * Constructor for revision-related notifications
     */
    public RevisionNotificationEvent(Object source, NotificationType type, Revision revision, 
            List<String> recipients, RevisionNotificationConfig.NotificationLevel level) {
        this(source, type, revision, recipients, level, Map.of());
    }

    /**
     * Constructor with additional data
     */
    public RevisionNotificationEvent(Object source, NotificationType type, Revision revision,
            List<String> recipients, RevisionNotificationConfig.NotificationLevel level,
            Map<String, Object> additionalData) {
        super(source);
        this.type = type;
        this.revision = revision;
        this.recipients = recipients;
        this.level = level;
        this.additionalData = additionalData;
        this.timestamp = System.currentTimeMillis();
    }

    /**
     * Create revision created event
     */
    public static RevisionNotificationEvent revisionCreated(Object source, Revision revision,
            List<String> recipients) {
        return new RevisionNotificationEvent(
            source,
            NotificationType.REVISION_CREATED,
            revision,
            recipients,
            RevisionNotificationConfig.NotificationLevel.INFO
        );
    }

    /**
     * Create revision updated event
     */
    public static RevisionNotificationEvent revisionUpdated(Object source, Revision revision,
            List<String> recipients) {
        return new RevisionNotificationEvent(
            source,
            NotificationType.REVISION_UPDATED,
            revision,
            recipients,
            RevisionNotificationConfig.NotificationLevel.INFO
        );
    }

    /**
     * Create revision deleted event
     */
    public static RevisionNotificationEvent revisionDeleted(Object source, Revision revision,
            List<String> recipients) {
        return new RevisionNotificationEvent(
            source,
            NotificationType.REVISION_DELETED,
            revision,
            recipients,
            RevisionNotificationConfig.NotificationLevel.WARNING
        );
    }

    /**
     * Create revision error event
     */
    public static RevisionNotificationEvent revisionError(Object source, Revision revision,
            List<String> recipients, String error, String stackTrace) {
        return new RevisionNotificationEvent(
            source,
            NotificationType.REVISION_ERROR,
            revision,
            recipients,
            RevisionNotificationConfig.NotificationLevel.ERROR,
            Map.of(
                "error", error,
                "stackTrace", stackTrace
            )
        );
    }

    /**
     * Create cleanup completed event
     */
    public static RevisionNotificationEvent cleanupCompleted(Object source, 
            List<String> recipients, int deletedCount, String reason) {
        return new RevisionNotificationEvent(
            source,
            NotificationType.CLEANUP_COMPLETED,
            null,
            recipients,
            RevisionNotificationConfig.NotificationLevel.INFO,
            Map.of(
                "deletedCount", deletedCount,
                "reason", reason
            )
        );
    }

    /**
     * Create system alert event
     */
    public static RevisionNotificationEvent systemAlert(Object source, List<String> recipients,
            String message, String details) {
        return new RevisionNotificationEvent(
            source,
            NotificationType.SYSTEM_ALERT,
            null,
            recipients,
            RevisionNotificationConfig.NotificationLevel.WARNING,
            Map.of(
                "message", message,
                "details", details
            )
        );
    }

    /**
     * Create excess revisions event
     */
    public static RevisionNotificationEvent excessRevisions(Object source, Revision revision,
            List<String> recipients, int currentCount, int maxAllowed) {
        return new RevisionNotificationEvent(
            source,
            NotificationType.EXCESS_REVISIONS,
            revision,
            recipients,
            RevisionNotificationConfig.NotificationLevel.WARNING,
            Map.of(
                "currentCount", currentCount,
                "maxAllowed", maxAllowed
            )
        );
    }

    /**
     * Get additional data value
     */
    @SuppressWarnings("unchecked")
    public <T> T getAdditionalData(String key, Class<T> type) {
        Object value = additionalData.get(key);
        if (value != null && type.isInstance(value)) {
            return (T) value;
        }
        return null;
    }

    /**
     * Check if has additional data
     */
    public boolean hasAdditionalData(String key) {
        return additionalData.containsKey(key);
    }

    /**
     * Get event age
     */
    public long getAge() {
        return System.currentTimeMillis() - timestamp;
    }

    /**
     * Notification type enum
     */
    public enum NotificationType {
        REVISION_CREATED,
        REVISION_UPDATED,
        REVISION_DELETED,
        REVISION_ERROR,
        CLEANUP_COMPLETED,
        SYSTEM_ALERT,
        EXCESS_REVISIONS
    }
}
