package com.ecommerce.api.validator;

import com.ecommerce.api.config.RevisionNotificationConfig;
import com.ecommerce.api.constant.RevisionNotificationConstants;
import com.ecommerce.api.exception.RevisionNotificationException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.regex.Pattern;

@Slf4j
@Component
@RequiredArgsConstructor
public class RevisionNotificationValidator {

    private final RevisionNotificationConfig notificationConfig;

    // Email validation pattern
    private static final Pattern EMAIL_PATTERN = Pattern.compile(
            "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$"
    );

    /**
     * Validate email configuration
     */
    public void validateEmailConfiguration() {
        if (!notificationConfig.isEmailEnabled()) {
            throw RevisionNotificationException.configurationError("Email notifications are disabled");
        }

        RevisionNotificationConfig.EmailConfig emailConfig = notificationConfig.getEmail();
        if (emailConfig == null) {
            throw RevisionNotificationException.configurationError("Email configuration is missing");
        }

        if (StringUtils.isBlank(emailConfig.getHost())) {
            throw RevisionNotificationException.configurationError("SMTP host is not configured");
        }

        if (emailConfig.getPort() <= 0) {
            throw RevisionNotificationException.configurationError("Invalid SMTP port configured");
        }

        if (StringUtils.isBlank(emailConfig.getFrom())) {
            throw RevisionNotificationException.configurationError("From address is not configured");
        }

        validateEmailAddress(emailConfig.getFrom());
    }

    /**
     * Validate email recipients
     */
    public void validateEmailRecipients(List<String> recipients) {
        if (recipients == null || recipients.isEmpty()) {
            throw RevisionNotificationException.invalidEmailRecipient("Recipients list is empty");
        }

        if (recipients.size() > RevisionNotificationConstants.MAX_RECIPIENTS_PER_EMAIL) {
            throw RevisionNotificationException.invalidEmailRecipient(
                    String.format("Too many recipients (max: %d)",
                            RevisionNotificationConstants.MAX_RECIPIENTS_PER_EMAIL)
            );
        }

        recipients.forEach(this::validateEmailAddress);
    }

    /**
     * Validate email address
     */
    public void validateEmailAddress(String email) {
        if (StringUtils.isBlank(email)) {
            throw RevisionNotificationException.invalidEmailRecipient("Email address is blank");
        }

        if (!EMAIL_PATTERN.matcher(email).matches()) {
            throw RevisionNotificationException.invalidEmailRecipient(
                    String.format("Invalid email address format: %s", email)
            );
        }
    }

    /**
     * Validate notification subject
     */
    public void validateSubject(String subject) {
        if (StringUtils.isBlank(subject)) {
            throw RevisionNotificationException.invalidContent("Subject is blank");
        }

        if (subject.length() > RevisionNotificationConstants.MAX_SUBJECT_LENGTH) {
            throw RevisionNotificationException.invalidContent(
                    String.format("Subject exceeds maximum length of %d characters",
                            RevisionNotificationConstants.MAX_SUBJECT_LENGTH)
            );
        }
    }

    /**
     * Validate a single email recipient
     */
    public void validateEmailRecipient(String recipient) {
        if (StringUtils.isBlank(recipient)) {
            throw RevisionNotificationException.invalidEmailRecipient("Email recipient is blank");
        }

        if (!EMAIL_PATTERN.matcher(recipient).matches()) {
            throw RevisionNotificationException.invalidEmailRecipient(
                    String.format("Invalid email recipient format: %s", recipient)
            );
        }
    }

    /**
     * Validate notification content
     */
    public void validateContent(String content) {
        if (StringUtils.isBlank(content)) {
            throw RevisionNotificationException.invalidContent("Content is blank");
        }

        if (content.length() > RevisionNotificationConstants.MAX_CONTENT_LENGTH) {
            throw RevisionNotificationException.invalidContent(
                    String.format("Content exceeds maximum length of %d characters",
                            RevisionNotificationConstants.MAX_CONTENT_LENGTH)
            );
        }
    }

    /**
     * Validate stack trace
     */
    public void validateStackTrace(String stackTrace) {
        if (StringUtils.isNotBlank(stackTrace) &&
                stackTrace.length() > RevisionNotificationConstants.MAX_STACK_TRACE_LENGTH) {
            throw RevisionNotificationException.invalidContent(
                    String.format("Stack trace exceeds maximum length of %d characters",
                            RevisionNotificationConstants.MAX_STACK_TRACE_LENGTH)
            );
        }
    }

    /**
     * Validate and get recipients for notification level
     */
    public List<String> validateAndGetRecipients(
            RevisionNotificationConfig.NotificationLevel level) {
        List<String> recipients = switch (level) {
            case INFO -> notificationConfig.getRecipients().getUsers();
            case WARNING -> notificationConfig.getRecipients().getManagers();
            case ERROR -> notificationConfig.getRecipients().getDevelopers();
            case CRITICAL -> notificationConfig.getRecipients().getAdministrators();
        };

        validateEmailRecipients(recipients);
        return recipients;
    }

    /**
     * Validate template name
     */
    public void validateTemplateName(String templateName) {
        if (StringUtils.isBlank(templateName)) {
            throw RevisionNotificationException.templateNotFound("Template name is blank");
        }

        if (!templateName.matches("^[a-zA-Z0-9-]+$")) {
            throw RevisionNotificationException.invalidContent(
                    "Template name contains invalid characters"
            );
        }
    }

    /**
     * Validate template data
     */
    public void validateTemplateData(String templateName, Object data) {
        if (data == null) {
            throw RevisionNotificationException.invalidTemplateData(
                    templateName, "Template data is null"
            );
        }
    }

    /**
     * Validate notification type
     */
    public void validateNotificationType(String type) {
        if (StringUtils.isBlank(type)) {
            throw RevisionNotificationException.invalidNotificationType("Type is blank");
        }

        try {
            RevisionNotificationConfig.NotificationType.valueOf(type);
        } catch (IllegalArgumentException e) {
            throw RevisionNotificationException.invalidNotificationType(
                    String.format("Invalid notification type: %s", type)
            );
        }
    }

    /**
     * Validate rate limit configuration
     */
    public void validateRateLimitConfig(int maxRequests, int windowMinutes) {
        if (maxRequests <= 0) {
            throw RevisionNotificationException.configurationError(
                    "Maximum requests must be greater than 0"
            );
        }

        if (windowMinutes <= 0) {
            throw RevisionNotificationException.configurationError(
                    "Rate limit window must be greater than 0 minutes"
            );
        }
    }

    /**
     * Validate retry configuration
     */
    public void validateRetryConfig(int maxRetries, int delaySeconds) {
        if (maxRetries < 0) {
            throw RevisionNotificationException.configurationError(
                    "Maximum retries cannot be negative"
            );
        }

        if (delaySeconds <= 0) {
            throw RevisionNotificationException.configurationError(
                    "Retry delay must be greater than 0 seconds"
            );
        }
    }

    /**
     * Validate queue configuration
     */
    public void validateQueueConfig(int queueSize) {
        if (queueSize <= 0) {
            throw RevisionNotificationException.configurationError(
                    "Queue size must be greater than 0"
            );
        }
    }
}
