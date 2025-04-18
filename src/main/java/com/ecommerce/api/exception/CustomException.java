package com.ecommerce.api.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public class CustomException extends RuntimeException {
    
    private final String error;
    private final HttpStatus status;
    private final Object data;

    public CustomException(String message) {
        this(message, "Internal Server Error", HttpStatus.INTERNAL_SERVER_ERROR);
    }

    public CustomException(String message, String error, HttpStatus status) {
        this(message, error, status, null);
    }

    public CustomException(String message, String error, HttpStatus status, Object data) {
        super(message);
        this.error = error;
        this.status = status;
        this.data = data;
    }

    public static class ResourceNotFoundException extends CustomException {
        public ResourceNotFoundException(String message) {
            super(message, "Resource Not Found", HttpStatus.NOT_FOUND);
        }

        public ResourceNotFoundException(String resource, String field, Object value) {
            super(
                String.format("%s not found with %s: '%s'", resource, field, value),
                "Resource Not Found",
                HttpStatus.NOT_FOUND
            );
        }
    }

    public static class BadRequestException extends CustomException {
        public BadRequestException(String message) {
            super(message, "Bad Request", HttpStatus.BAD_REQUEST);
        }

        public BadRequestException(String message, Object data) {
            super(message, "Bad Request", HttpStatus.BAD_REQUEST, data);
        }
    }

    public static class UnauthorizedException extends CustomException {
        public UnauthorizedException(String message) {
            super(message, "Unauthorized", HttpStatus.UNAUTHORIZED);
        }
    }

    public static class ForbiddenException extends CustomException {
        public ForbiddenException(String message) {
            super(message, "Forbidden", HttpStatus.FORBIDDEN);
        }
    }

    public static class ConflictException extends CustomException {
        public ConflictException(String message) {
            super(message, "Conflict", HttpStatus.CONFLICT);
        }
    }

    public static class ValidationException extends CustomException {
        public ValidationException(String message) {
            super(message, "Validation Error", HttpStatus.BAD_REQUEST);
        }

        public ValidationException(String message, Object errors) {
            super(message, "Validation Error", HttpStatus.BAD_REQUEST, errors);
        }
    }

    public static class FileUploadException extends CustomException {
        public FileUploadException(String message) {
            super(message, "File Upload Error", HttpStatus.BAD_REQUEST);
        }
    }

    public static class SessionException extends CustomException {
        public SessionException(String message) {
            super(message, "Session Error", HttpStatus.UNAUTHORIZED);
        }
    }

    public static class AuthenticationException extends CustomException {
        public AuthenticationException(String message) {
            super(message, "Authentication Error", HttpStatus.UNAUTHORIZED);
        }
    }

    public static class TokenException extends CustomException {
        public TokenException(String message) {
            super(message, "Token Error", HttpStatus.UNAUTHORIZED);
        }
    }

    public static class DatabaseException extends CustomException {
        public DatabaseException(String message) {
            super(message, "Database Error", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    public static class ServiceException extends CustomException {
        public ServiceException(String message) {
            super(message, "Service Error", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    public static class ExternalServiceException extends CustomException {
        public ExternalServiceException(String message) {
            super(message, "External Service Error", HttpStatus.SERVICE_UNAVAILABLE);
        }
    }

    public static class TooManyRequestsException extends CustomException {
        public TooManyRequestsException(String message) {
            super(message, "Too Many Requests", HttpStatus.TOO_MANY_REQUESTS);
        }
    }

    public static class OperationNotAllowedException extends CustomException {
        public OperationNotAllowedException(String message) {
            super(message, "Operation Not Allowed", HttpStatus.METHOD_NOT_ALLOWED);
        }
    }

    public static class InvalidStateException extends CustomException {
        public InvalidStateException(String message) {
            super(message, "Invalid State", HttpStatus.CONFLICT);
        }
    }

    public static class DuplicateResourceException extends CustomException {
        public DuplicateResourceException(String message) {
            super(message, "Duplicate Resource", HttpStatus.CONFLICT);
        }
    }

    public static class PaymentException extends CustomException {
        public PaymentException(String message) {
            super(message, "Payment Error", HttpStatus.BAD_REQUEST);
        }
    }

    public static class MailException extends CustomException {
        public MailException(String message) {
            super(message, "Mail Error", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    public static class CacheException extends CustomException {
        public CacheException(String message) {
            super(message, "Cache Error", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}
