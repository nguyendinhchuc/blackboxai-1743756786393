package com.ecommerce.api.constant;

import java.time.format.DateTimeFormatter;

public final class RevisionNotificationConstants {

    private RevisionNotificationConstants() {
        // Private constructor to prevent instantiation
    }

    // Cache Names
    public static final String CACHE_TEMPLATES = "notificationTemplates";
    public static final String CACHE_RECIPIENTS = "notificationRecipients";
    public static final String CACHE_STATS = "notificationStats";
    public static final String CACHE_DELIVERY = "notificationDelivery";
    public static final String CACHE_RATE_LIMITS = "notificationRateLimits";

    // Cache Configuration
    public static final long CACHE_TIMEOUT_SECONDS = 7200;
    public static final int CACHE_MAX_SIZE = 2000;
    public static final String CACHE_SPEC = String.format(
        "maximumSize=%d,expireAfterWrite=%ds,recordStats",
        CACHE_MAX_SIZE, CACHE_TIMEOUT_SECONDS
    );

    // Template Cache Configuration
    public static final int TEMPLATE_CACHE_SIZE = 100;
    public static final long TEMPLATE_CACHE_TIMEOUT = 3600;

    // Template Configuration
    public static final String TEMPLATE_BASE_PATH = "templates/notification/";
    public static final String TEMPLATE_SUFFIX = ".html";
    public static final String TEMPLATE_ENCODING = "UTF-8";
    public static final String TEMPLATE_CONTENT_TYPE = "text/html";
    
    // Template Names
    public static final String TEMPLATE_REVISION = "revision-notification";
    public static final String TEMPLATE_SYSTEM = "system-alert";
    public static final String TEMPLATE_ERROR = "error-alert";
    public static final String TEMPLATE_CLEANUP = "cleanup-report";

    // Email Configuration
    public static final String DEFAULT_EMAIL_HOST = "smtp.gmail.com";
    public static final int DEFAULT_EMAIL_PORT = 587;
    public static final String DEFAULT_EMAIL_PROTOCOL = "smtp";
    public static final int DEFAULT_EMAIL_TIMEOUT = 5000;
    public static final String DEFAULT_SENDER_NAME = "System Notifications";
    public static final String DEFAULT_REPLY_TO = "no-reply@example.com";

    // Rate Limiting
    public static final int MAX_REQUESTS_PER_HOUR = 1000;
    public static final int MAX_RECIPIENTS_PER_EMAIL = 50;
    public static final int MAX_RETRY_ATTEMPTS = 3;
    public static final long RETRY_DELAY_MS = 5000;

    // Content Limits
    public static final int MAX_SUBJECT_LENGTH = 255;
    public static final int MAX_CONTENT_LENGTH = 10000;
    public static final int MAX_STACK_TRACE_LENGTH = 5000;

    // Metrics Configuration
    public static final String METRIC_PREFIX = "revision.notification";
    public static final String METRIC_NOTIFICATIONS_SENT = METRIC_PREFIX + ".sent";
    public static final String METRIC_NOTIFICATIONS_FAILED = METRIC_PREFIX + ".failed";
    public static final String METRIC_DELIVERY_TIME = METRIC_PREFIX + ".delivery.time";
    public static final String METRIC_QUEUE_SIZE = METRIC_PREFIX + ".queue.size";
    public static final String METRIC_QUEUE_AGE = METRIC_PREFIX + ".queue.age";
    public static final String METRIC_CACHE_HITS = METRIC_PREFIX + ".cache.hits";
    public static final String METRIC_CACHE_MISSES = METRIC_PREFIX + ".cache.misses";
    public static final String METRIC_TEMPLATE_PROCESSING = METRIC_PREFIX + ".template.processing";

    // Notification Types and Levels
    public static final String TYPE_REVISION = "REVISION";
    public static final String TYPE_SYSTEM = "SYSTEM";
    public static final String TYPE_ERROR = "ERROR";
    public static final String TYPE_CLEANUP = "CLEANUP";
    
    public static final String LEVEL_INFO = "INFO";
    public static final String LEVEL_WARNING = "WARNING";
    public static final String LEVEL_ERROR = "ERROR";
    public static final String LEVEL_CRITICAL = "CRITICAL";

    // Date Time Formats
    public static final DateTimeFormatter DATE_FORMATTER = 
        DateTimeFormatter.ofPattern("yyyy-MM-dd");
    public static final DateTimeFormatter DATE_TIME_FORMATTER = 
        DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    public static final DateTimeFormatter TIME_FORMATTER = 
        DateTimeFormatter.ofPattern("HH:mm:ss");

    // Notification Status and Headers
    public static final String STATUS_PENDING = "PENDING";
    public static final String STATUS_DELIVERED = "DELIVERED";
    public static final String STATUS_FAILED = "FAILED";
    public static final String STATUS_RETRYING = "RETRYING";
    public static final String STATUS_CANCELLED = "CANCELLED";
    
    public static final String HEADER_NOTIFICATION_ID = "X-Notification-ID";
    public static final String HEADER_NOTIFICATION_TYPE = "X-Notification-Type";
    public static final String HEADER_NOTIFICATION_TIMESTAMP = "X-Notification-Timestamp";

    // Error Codes and Messages
    public static final String ERROR_TEMPLATE_NOT_FOUND = "NOTIFICATION_TEMPLATE_NOT_FOUND";
    public static final String ERROR_TEMPLATE_PROCESSING = "NOTIFICATION_TEMPLATE_PROCESSING_FAILED";
    public static final String ERROR_INVALID_EMAIL = "NOTIFICATION_INVALID_EMAIL";
    public static final String ERROR_SENDING_FAILED = "NOTIFICATION_SENDING_FAILED";
    public static final String ERROR_RATE_LIMIT = "NOTIFICATION_RATE_LIMIT_EXCEEDED";
    public static final String ERROR_QUEUE_FULL = "NOTIFICATION_QUEUE_FULL";
    public static final String ERROR_MAX_RETRIES = "NOTIFICATION_MAX_RETRIES_EXCEEDED";
    public static final String ERROR_INVALID_DATA = "NOTIFICATION_INVALID_DATA";

    // Subject Prefixes
    public static final String SUBJECT_NEW_REVISION = "New Revision Created: %s";
    public static final String SUBJECT_UPDATED_REVISION = "Revision Updated: %s";
    public static final String SUBJECT_DELETED_REVISION = "Revision Deleted: %s";
    public static final String SUBJECT_SYSTEM_ALERT = "System Alert: %s";
    public static final String SUBJECT_ERROR_ALERT = "Error Alert: %s";
    public static final String SUBJECT_CLEANUP_REPORT = "Cleanup Report: %d Revisions Deleted";

    // Notification ID Format
    public static final String NOTIFICATION_ID_FORMAT = "notif_%d_%04d";

    // Scheduler Cron Expressions
    public static final String DAILY_CLEANUP_CRON = "0 0 0 * * ?";  // Daily at midnight
    public static final String WEEKLY_SUMMARY_CRON = "0 0 0 ? * MON";  // Every Monday
    public static final String HOURLY_METRICS_CRON = "0 0 * * * ?";  // Every hour
    public static final String RATE_LIMIT_CLEANUP_CRON = "0 */5 * * * ?";  // Every 5 minutes

    // Queue Configuration
    public static final int QUEUE_WARNING_SIZE = 800;
    public static final long QUEUE_WARNING_AGE_MINUTES = 15;
    public static final int QUEUE_CRITICAL_SIZE = 900;
    public static final long QUEUE_CRITICAL_AGE_MINUTES = 30;

    // Memory Thresholds (in MB)
    public static final int MEMORY_WARNING_THRESHOLD = 1024;
    public static final int MEMORY_CRITICAL_THRESHOLD = 1536;

    // Performance Thresholds (in ms)
    public static final long DELIVERY_TIME_WARNING = 1000;
    public static final long DELIVERY_TIME_CRITICAL = 2000;
    public static final long TEMPLATE_PROCESSING_WARNING = 500;
    public static final long TEMPLATE_PROCESSING_CRITICAL = 1000;
    public static final long QUEUE_PROCESSING_WARNING = 2000;
    public static final long QUEUE_PROCESSING_CRITICAL = 5000;

    // Success Rate Thresholds (in percentage)
    public static final double SUCCESS_RATE_WARNING = 95.0;
    public static final double SUCCESS_RATE_CRITICAL = 90.0;

    // Batch Processing
    public static final int DEFAULT_BATCH_SIZE = 200;
    public static final int MAX_BATCH_SIZE = 2000;
    public static final long BATCH_TIMEOUT_MS = 30000;
    public static final int MIN_BATCH_SIZE = 50;
    public static final long MIN_BATCH_WAIT_MS = 1000;

    // Thread Pool Configuration
    public static final int CORE_POOL_SIZE = 4;
    public static final int MAX_POOL_SIZE = 8;
    public static final int QUEUE_CAPACITY = 1000;
    public static final String THREAD_NAME_PREFIX = "NotificationAsync-";
    public static final int THREAD_TIMEOUT_SECONDS = 300;
    public static final int THREAD_KEEP_ALIVE_SECONDS = 60;
}
