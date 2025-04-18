package com.ecommerce.api.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public class RevisionException extends CustomException {

    private final String entityName;
    private final Long entityId;

    /**
     * Constructor for revision not found
     */
    public static RevisionException notFound(Long id) {
        return new RevisionException(
            String.format("Revision not found with ID: %d", id),
            "REVISION_NOT_FOUND",
            HttpStatus.NOT_FOUND
        );
    }

    /**
     * Constructor for entity revision not found
     */
    public static RevisionException entityRevisionNotFound(String entityName, Long entityId) {
        return new RevisionException(
            String.format("No revisions found for %s with ID: %d", entityName, entityId),
            "ENTITY_REVISION_NOT_FOUND",
            HttpStatus.NOT_FOUND,
            entityName,
            entityId
        );
    }

    /**
     * Constructor for invalid revision data
     */
    public static RevisionException invalidData(String message) {
        return new RevisionException(
            message,
            "INVALID_REVISION_DATA",
            HttpStatus.BAD_REQUEST
        );
    }

    /**
     * Constructor for revision processing error
     */
    public static RevisionException processingError(String message, Throwable cause) {
        return new RevisionException(
            message,
            "REVISION_PROCESSING_ERROR",
            HttpStatus.INTERNAL_SERVER_ERROR,
            cause
        );
    }

    /**
     * Constructor for too many revisions
     */
    public static RevisionException tooManyRevisions(String entityName, Long entityId, int maxRevisions) {
        return new RevisionException(
            String.format("Maximum number of revisions (%d) exceeded for %s with ID: %d", 
                maxRevisions, entityName, entityId),
            "TOO_MANY_REVISIONS",
            HttpStatus.BAD_REQUEST,
            entityName,
            entityId
        );
    }

    /**
     * Constructor for invalid revision type
     */
    public static RevisionException invalidRevisionType(String revisionType) {
        return new RevisionException(
            String.format("Invalid revision type: %s", revisionType),
            "INVALID_REVISION_TYPE",
            HttpStatus.BAD_REQUEST
        );
    }

    /**
     * Constructor for compression error
     */
    public static RevisionException compressionError(String message, Throwable cause) {
        return new RevisionException(
            String.format("Error compressing revision data: %s", message),
            "COMPRESSION_ERROR",
            HttpStatus.INTERNAL_SERVER_ERROR,
            cause
        );
    }

    /**
     * Constructor for decompression error
     */
    public static RevisionException decompressionError(String message, Throwable cause) {
        return new RevisionException(
            String.format("Error decompressing revision data: %s", message),
            "DECOMPRESSION_ERROR",
            HttpStatus.INTERNAL_SERVER_ERROR,
            cause
        );
    }

    /**
     * Constructor for invalid changes format
     */
    public static RevisionException invalidChangesFormat(String message) {
        return new RevisionException(
            String.format("Invalid changes format: %s", message),
            "INVALID_CHANGES_FORMAT",
            HttpStatus.BAD_REQUEST
        );
    }

    /**
     * Constructor for unauthorized access
     */
    public static RevisionException unauthorized(String entityName, Long entityId) {
        return new RevisionException(
            String.format("Unauthorized access to revisions for %s with ID: %d", entityName, entityId),
            "UNAUTHORIZED_REVISION_ACCESS",
            HttpStatus.FORBIDDEN,
            entityName,
            entityId
        );
    }

    /**
     * Private constructors
     */
    private RevisionException(String message, String code, HttpStatus status) {
        super(message, code, status);
        this.entityName = null;
        this.entityId = null;
    }

    private RevisionException(String message, String code, HttpStatus status, Throwable cause) {
        super(message, code, status, cause);
        this.entityName = null;
        this.entityId = null;
    }

    private RevisionException(String message, String code, HttpStatus status, 
                            String entityName, Long entityId) {
        super(message, code, status);
        this.entityName = entityName;
        this.entityId = entityId;
    }

    /**
     * Get error details
     */
    @Override
    public Object getErrorDetails() {
        if (entityName != null && entityId != null) {
            return new RevisionErrorDetails(entityName, entityId);
        }
        return super.getErrorDetails();
    }

    /**
     * Revision error details class
     */
    @Getter
    public static class RevisionErrorDetails {
        private final String entityName;
        private final Long entityId;

        public RevisionErrorDetails(String entityName, Long entityId) {
            this.entityName = entityName;
            this.entityId = entityId;
        }
    }
}
