package com.ecommerce.api.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public class RevisionNotificationException extends RuntimeException {

    private final HttpStatus status;
    private final String code;
    private final String details;

    private RevisionNotificationException(String message, String code, 
            String details, HttpStatus status, Throwable cause) {
        super(message, cause);
        this.code = code;
        this.details = details;
        this.status = status;
    }

    /**
     * Template not found exception
     */
    public static RevisionNotificationException templateNotFound(String templateName) {
        return new RevisionNotificationException(
            String.format("Template not found: %s", templateName),
            "NOTIFICATION_TEMPLATE_NOT_FOUND",
            String.format("The notification template '%s' could not be found", templateName),
            HttpStatus.NOT_FOUND,
            null
        );
    }

    /**
     * Template processing failed exception
     */
    public static RevisionNotificationException templateProcessingFailed(
            String templateName, String reason, Throwable cause) {
        return new RevisionNotificationException(
            String.format("Failed to process template %s: %s", templateName, reason),
            "NOTIFICATION_TEMPLATE_PROCESSING_FAILED",
            String.format("Error processing template '%s': %s", templateName, reason),
            HttpStatus.INTERNAL_SERVER_ERROR,
            cause
        );
    }

    /**
     * Invalid email configuration exception
     */
    public static RevisionNotificationException invalidEmailConfiguration(String reason) {
        return new RevisionNotificationException(
            String.format("Invalid email configuration: %s", reason),
            "NOTIFICATION_INVALID_EMAIL_CONFIG",
            String.format("The email configuration is invalid: %s", reason),
            HttpStatus.INTERNAL_SERVER_ERROR,
            null
        );
    }

    /**
     * Invalid email recipient exception
     */
    public static RevisionNotificationException invalidEmailRecipient(String recipient) {
        return new RevisionNotificationException(
            String.format("Invalid email recipient: %s", recipient),
            "NOTIFICATION_INVALID_RECIPIENT",
            String.format("The email recipient '%s' is invalid", recipient),
            HttpStatus.BAD_REQUEST,
            null
        );
    }

    /**
     * Email sending failed exception
     */
    public static RevisionNotificationException emailSendingFailed(
            String notificationId, String reason, Throwable cause) {
        return new RevisionNotificationException(
            String.format("Failed to send email notification %s: %s", notificationId, reason),
            "NOTIFICATION_SENDING_FAILED",
            String.format("Error sending notification '%s': %s", notificationId, reason),
            HttpStatus.INTERNAL_SERVER_ERROR,
            cause
        );
    }

    /**
     * Rate limit exceeded exception
     */
    public static RevisionNotificationException rateLimitExceeded(String recipient) {
        return new RevisionNotificationException(
            String.format("Rate limit exceeded for recipient: %s", recipient),
            "NOTIFICATION_RATE_LIMIT_EXCEEDED",
            String.format("Too many notifications sent to recipient '%s'", recipient),
            HttpStatus.TOO_MANY_REQUESTS,
            null
        );
    }

    /**
     * Invalid notification type exception
     */
    public static RevisionNotificationException invalidNotificationType(String type) {
        return new RevisionNotificationException(
            String.format("Invalid notification type: %s", type),
            "NOTIFICATION_INVALID_TYPE",
            String.format("The notification type '%s' is not supported", type),
            HttpStatus.BAD_REQUEST,
            null
        );
    }

    /**
     * Invalid notification content exception
     */
    public static RevisionNotificationException invalidContent(String reason) {
        return new RevisionNotificationException(
            String.format("Invalid notification content: %s", reason),
            "NOTIFICATION_INVALID_CONTENT",
            String.format("The notification content is invalid: %s", reason),
            HttpStatus.BAD_REQUEST,
            null
        );
    }

    /**
     * Queue full exception
     */
    public static RevisionNotificationException queueFull(int queueSize) {
        return new RevisionNotificationException(
            String.format("Notification queue is full (size: %d)", queueSize),
            "NOTIFICATION_QUEUE_FULL",
            String.format("The notification queue has reached its maximum size of %d", queueSize),
            HttpStatus.SERVICE_UNAVAILABLE,
            null
        );
    }

    /**
     * Maximum retries exceeded exception
     */
    public static RevisionNotificationException maxRetriesExceeded(
            String notificationId, int maxRetries) {
        return new RevisionNotificationException(
            String.format("Maximum retries exceeded for notification: %s", notificationId),
            "NOTIFICATION_MAX_RETRIES_EXCEEDED",
            String.format("Failed to send notification '%s' after %d attempts", 
                notificationId, maxRetries),
            HttpStatus.INTERNAL_SERVER_ERROR,
            null
        );
    }

    /**
     * Invalid template data exception
     */
    public static RevisionNotificationException invalidTemplateData(
            String templateName, String reason) {
        return new RevisionNotificationException(
            String.format("Invalid template data for %s: %s", templateName, reason),
            "NOTIFICATION_INVALID_TEMPLATE_DATA",
            String.format("The data provided for template '%s' is invalid: %s", 
                templateName, reason),
            HttpStatus.BAD_REQUEST,
            null
        );
    }

    /**
     * Service unavailable exception
     */
    public static RevisionNotificationException serviceUnavailable(String reason) {
        return new RevisionNotificationException(
            String.format("Notification service unavailable: %s", reason),
            "NOTIFICATION_SERVICE_UNAVAILABLE",
            String.format("The notification service is currently unavailable: %s", reason),
            HttpStatus.SERVICE_UNAVAILABLE,
            null
        );
    }

    /**
     * Configuration error exception
     */
    public static RevisionNotificationException configurationError(String reason) {
        return new RevisionNotificationException(
            String.format("Notification configuration error: %s", reason),
            "NOTIFICATION_CONFIG_ERROR",
            String.format("There is an error in the notification configuration: %s", reason),
            HttpStatus.INTERNAL_SERVER_ERROR,
            null
        );
    }

    /**
     * Permission denied exception
     */
    public static RevisionNotificationException permissionDenied(String action) {
        return new RevisionNotificationException(
            String.format("Permission denied: %s", action),
            "NOTIFICATION_PERMISSION_DENIED",
            String.format("You do not have permission to perform this action: %s", action),
            HttpStatus.FORBIDDEN,
            null
        );
    }

    /**
     * Resource not found exception
     */
    public static RevisionNotificationException resourceNotFound(
            String resourceType, String resourceId) {
        return new RevisionNotificationException(
            String.format("%s not found: %s", resourceType, resourceId),
            "NOTIFICATION_RESOURCE_NOT_FOUND",
            String.format("The requested %s '%s' could not be found", 
                resourceType, resourceId),
            HttpStatus.NOT_FOUND,
            null
        );
    }
}
