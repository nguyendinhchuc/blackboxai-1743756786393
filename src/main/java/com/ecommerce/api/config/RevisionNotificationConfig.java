package com.ecommerce.api.config;

import com.ecommerce.api.constant.RevisionNotificationConstants;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.validation.annotation.Validated;

import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import java.util.List;

@Data
@Validated
@Configuration
@ConfigurationProperties(prefix = "revision.notification")
public class RevisionNotificationConfig {

    @NotNull
    private Boolean emailEnabled = true;

    @Valid
    private EmailConfig email = new EmailConfig();

    @Valid
    private RecipientConfig recipients = new RecipientConfig();

    @Valid
    private RateLimitConfig rateLimit = new RateLimitConfig();

    @Valid
    private RetryConfig retry = new RetryConfig();

    @Valid
    private QueueConfig queue = new QueueConfig();

    @Valid
    private TemplateConfig template = new TemplateConfig();

    /**
     * Email configuration
     */
    @Data
    public static class EmailConfig {
        @NotBlank
        private String host = RevisionNotificationConstants.DEFAULT_EMAIL_HOST;

        @Min(1)
        @Max(65535)
        private Integer port = RevisionNotificationConstants.DEFAULT_EMAIL_PORT;

        @NotBlank
        @Email
        private String from = "noreply@example.com";

        private String username;
        private String password;

        @NotBlank
        private String protocol = RevisionNotificationConstants.DEFAULT_EMAIL_PROTOCOL;

        private Boolean auth = true;
        private Boolean startTlsEnabled = true;
        private Boolean startTlsRequired = true;

        @Min(1000)
        private Integer connectionTimeout = RevisionNotificationConstants.DEFAULT_EMAIL_TIMEOUT;

        @Min(1000)
        private Integer timeout = RevisionNotificationConstants.DEFAULT_EMAIL_TIMEOUT;

        @Min(1000)
        private Integer writeTimeout = RevisionNotificationConstants.DEFAULT_EMAIL_TIMEOUT;
    }

    /**
     * Recipient configuration
     */
    @Data
    public static class RecipientConfig {
        @NotEmpty
        private List<@Email String> users;

        @NotEmpty
        private List<@Email String> managers;

        @NotEmpty
        private List<@Email String> developers;

        @NotEmpty
        private List<@Email String> administrators;
    }

    /**
     * Rate limit configuration
     */
    @Data
    public static class RateLimitConfig {
        @Min(1)
        private Integer maxRequestsPerHour = RevisionNotificationConstants.MAX_REQUESTS_PER_HOUR;

        @Min(1)
        private Integer windowMinutes = 60;

        private Boolean enabled = true;
    }

    /**
     * Retry configuration
     */
    @Data
    public static class RetryConfig {
        @Min(0)
        private Integer maxRetries = RevisionNotificationConstants.MAX_RETRY_ATTEMPTS;

        @Min(100)
        private Long delayMs = RevisionNotificationConstants.RETRY_DELAY_MS;

        private Boolean enabled = true;
    }

    /**
     * Queue configuration
     */
    @Data
    public static class QueueConfig {
        @Min(1)
        private Integer size = RevisionNotificationConstants.QUEUE_CAPACITY;

        @Min(1)
        private Integer warningThreshold = RevisionNotificationConstants.QUEUE_WARNING_SIZE;

        @Min(1)
        private Long warningAgeMinutes = RevisionNotificationConstants.QUEUE_WARNING_AGE_MINUTES;
    }

    /**
     * Template configuration
     */
    @Data
    public static class TemplateConfig {
        @NotBlank
        private String basePath = RevisionNotificationConstants.TEMPLATE_BASE_PATH;

        @NotBlank
        private String suffix = RevisionNotificationConstants.TEMPLATE_SUFFIX;

        @Min(1)
        private Integer cacheSize = RevisionNotificationConstants.CACHE_MAX_SIZE;

        @Min(1)
        private Long cacheTimeoutSeconds = RevisionNotificationConstants.CACHE_TIMEOUT_SECONDS;
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

    /**
     * Notification level enum
     */
    public enum NotificationLevel {
        INFO,
        WARNING,
        ERROR,
        CRITICAL
    }

    /**
     * Get cache specification
     */
    public String getCacheSpec() {
        return String.format("maximumSize=%d,expireAfterWrite=%ds",
            template.getCacheSize(),
            template.getCacheTimeoutSeconds()
        );
    }

    /**
     * Get template path
     */
    public String getTemplatePath(String templateName) {
        return template.getBasePath() + templateName + template.getSuffix();
    }

    /**
     * Check if rate limiting is enabled
     */
    public boolean isRateLimitEnabled() {
        return emailEnabled && rateLimit.getEnabled();
    }

    /**
     * Check if retry is enabled
     */
    public boolean isRetryEnabled() {
        return emailEnabled && retry.getEnabled();
    }

    /**
     * Get recipients for notification level
     */
    public List<String> getRecipientsForLevel(NotificationLevel level) {
        return switch (level) {
            case INFO -> recipients.getUsers();
            case WARNING -> recipients.getManagers();
            case ERROR -> recipients.getDevelopers();
            case CRITICAL -> recipients.getAdministrators();
        };
    }

    /**
     * Get notification level for type
     */
    public NotificationLevel getLevelForType(NotificationType type) {
        return switch (type) {
            case REVISION_CREATED, REVISION_UPDATED -> NotificationLevel.INFO;
            case REVISION_DELETED, CLEANUP_REPORT -> NotificationLevel.WARNING;
            case ERROR_ALERT -> NotificationLevel.ERROR;
            case SYSTEM_ALERT -> NotificationLevel.CRITICAL;
        };
    }
}
