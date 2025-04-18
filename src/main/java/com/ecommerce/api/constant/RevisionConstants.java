package com.ecommerce.api.constant;

import java.time.format.DateTimeFormatter;
import java.util.Set;

public final class RevisionConstants {

    private RevisionConstants() {
        // Private constructor to prevent instantiation
    }

    /**
     * Date and time formats
     */
    public static final String DATE_TIME_FORMAT = "yyyy-MM-dd HH:mm:ss";
    public static final DateTimeFormatter DATE_TIME_FORMATTER = 
        DateTimeFormatter.ofPattern(DATE_TIME_FORMAT);
    public static final String DATE_FORMAT = "yyyy-MM-dd";
    public static final DateTimeFormatter DATE_FORMATTER = 
        DateTimeFormatter.ofPattern(DATE_FORMAT);

    /**
     * Default values
     */
    public static final int DEFAULT_PAGE_SIZE = 20;
    public static final int MAX_PAGE_SIZE = 100;
    public static final int DEFAULT_RETENTION_PERIOD = 180; // 6 months
    public static final int DEFAULT_MAX_REVISIONS = 100;
    public static final int DEFAULT_COMPRESSION_THRESHOLD = 1024; // 1KB

    /**
     * Cache configuration
     */
    public static final String REVISION_CACHE_NAME = "revisionCache";
    public static final int REVISION_CACHE_TTL = 3600; // 1 hour
    public static final int REVISION_CACHE_MAX_SIZE = 1000;

    /**
     * Security configuration
     */
    public static final String REVISION_ADMIN_ROLE = "ROLE_REVISION_ADMIN";
    public static final String REVISION_USER_ROLE = "ROLE_REVISION_USER";
    public static final String REVISION_VIEW_ROLE = "ROLE_REVISION_VIEW";

    /**
     * API endpoints
     */
    public static final String API_BASE_PATH = "/api/revisions";
    public static final String API_VERSION = "v1";
    public static final String API_FULL_PATH = API_BASE_PATH + "/" + API_VERSION;

    /**
     * Swagger documentation
     */
    public static final String SWAGGER_TAG = "Revision Management";
    public static final String SWAGGER_DESCRIPTION = 
        "APIs for managing entity revisions and audit trails";

    /**
     * Field exclusions
     */
    public static final Set<String> DEFAULT_EXCLUDED_FIELDS = Set.of(
        "password",
        "secretKey",
        "token",
        "refreshToken",
        "salt",
        "hash",
        "pin",
        "cvv",
        "ssn",
        "creditCard"
    );

    /**
     * Database configuration
     */
    public static final String REVISION_TABLE_NAME = "revisions";
    public static final int USERNAME_MAX_LENGTH = 50;
    public static final int IP_ADDRESS_MAX_LENGTH = 45;
    public static final int USER_AGENT_MAX_LENGTH = 512;
    public static final int REASON_MAX_LENGTH = 1000;
    public static final int ENTITY_NAME_MAX_LENGTH = 255;
    public static final int CHANGES_MAX_LENGTH = 10000;

    /**
     * Audit trail configuration
     */
    public static final String CREATED_BY_FIELD = "createdBy";
    public static final String CREATED_DATE_FIELD = "createdDate";
    public static final String LAST_MODIFIED_BY_FIELD = "lastModifiedBy";
    public static final String LAST_MODIFIED_DATE_FIELD = "lastModifiedDate";
    public static final String DELETED_BY_FIELD = "deletedBy";
    public static final String DELETED_DATE_FIELD = "deletedDate";

    /**
     * Error codes
     */
    public static final String ERROR_REVISION_NOT_FOUND = "REVISION_NOT_FOUND";
    public static final String ERROR_ENTITY_REVISION_NOT_FOUND = "ENTITY_REVISION_NOT_FOUND";
    public static final String ERROR_INVALID_REVISION_DATA = "INVALID_REVISION_DATA";
    public static final String ERROR_PROCESSING_REVISION = "REVISION_PROCESSING_ERROR";
    public static final String ERROR_TOO_MANY_REVISIONS = "TOO_MANY_REVISIONS";
    public static final String ERROR_INVALID_REVISION_TYPE = "INVALID_REVISION_TYPE";
    public static final String ERROR_COMPRESSION = "COMPRESSION_ERROR";
    public static final String ERROR_DECOMPRESSION = "DECOMPRESSION_ERROR";
    public static final String ERROR_INVALID_CHANGES_FORMAT = "INVALID_CHANGES_FORMAT";
    public static final String ERROR_UNAUTHORIZED = "UNAUTHORIZED_REVISION_ACCESS";

    /**
     * Scheduler configuration
     */
    public static final String CLEANUP_SCHEDULE = "0 0 0 * * ?"; // Daily at midnight
    public static final String EXCESS_CLEANUP_SCHEDULE = "0 0 1 * * ?"; // Daily at 1 AM
    public static final String STATS_SCHEDULE = "0 0 2 * * ?"; // Daily at 2 AM
    public static final String COMPRESSION_SCHEDULE = "0 0 3 * * ?"; // Daily at 3 AM

    /**
     * Async configuration
     */
    public static final String ASYNC_THREAD_PREFIX = "RevisionAsync-";
    public static final int ASYNC_CORE_POOL_SIZE = 2;
    public static final int ASYNC_MAX_POOL_SIZE = 5;
    public static final int ASYNC_QUEUE_CAPACITY = 500;
    public static final int ASYNC_TIMEOUT_SECONDS = 60;

    /**
     * Metrics configuration
     */
    public static final String METRIC_PREFIX = "revision";
    public static final String METRIC_CREATED = METRIC_PREFIX + ".created";
    public static final String METRIC_UPDATED = METRIC_PREFIX + ".updated";
    public static final String METRIC_DELETED = METRIC_PREFIX + ".deleted";
    public static final String METRIC_ERRORS = METRIC_PREFIX + ".errors";
    public static final String METRIC_PROCESSING_TIME = METRIC_PREFIX + ".processing_time";

    /**
     * Logging configuration
     */
    public static final String LOG_PREFIX = "[REVISION]";
    public static final String LOG_CREATED = LOG_PREFIX + " Created revision for {} with ID {}";
    public static final String LOG_UPDATED = LOG_PREFIX + " Updated revision for {} with ID {}";
    public static final String LOG_DELETED = LOG_PREFIX + " Deleted revision for {} with ID {}";
    public static final String LOG_ERROR = LOG_PREFIX + " Error processing revision: {}";
}
