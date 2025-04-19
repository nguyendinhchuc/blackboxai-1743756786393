package com.ecommerce.api.payload;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ApiResponse<T> {

    private boolean success;
    private String message;
    private T data;
    private Object errors;
    private Map<String, Object> metadata;
    private LocalDateTime timestamp;
    private int status;

    /**
     * Create a success response
     */
    public static <T> ApiResponse<T> success(T data) {
        return ApiResponse.<T>builder()
                .success(true)
                .message("Success")
                .data(data)
                .timestamp(LocalDateTime.now())
                .status(HttpStatus.OK.value())
                .build();
    }

    /**
     * Create a success response with message
     */
    public static <T> ApiResponse<T> success(String message, T data) {
        return ApiResponse.<T>builder()
                .success(true)
                .message(message)
                .data(data)
                .timestamp(LocalDateTime.now())
                .status(HttpStatus.OK.value())
                .build();
    }

    /**
     * Create an error response
     */
    public static <T> ApiResponse<T> error(String message) {
        ApiResponse<T> res = new ApiResponse<>();
        res.setSuccess(false);
        res.setMessage(message);
        res.setTimestamp(LocalDateTime.now());
        res.setStatus(HttpStatus.BAD_REQUEST.value());
        return res;
    }

    /**
     * Create an error response with status
     */
    public static <T> ApiResponse<T> error(String message, HttpStatus status) {
        return ApiResponse.<T>builder()
                .success(false)
                .message(message)
                .timestamp(LocalDateTime.now())
                .status(status.value())
                .build();
    }

    /**
     * Create an error response with validation errors
     */
    public static <T> ApiResponse<T> error(String message, Object errors) {
        return ApiResponse.<T>builder()
                .success(false)
                .message(message)
                .errors(errors)
                .timestamp(LocalDateTime.now())
                .status(HttpStatus.BAD_REQUEST.value())
                .build();
    }

    /**
     * Add metadata to response
     */
    public ApiResponse<T> addMetadata(String key, Object value) {
        if (metadata == null) {
            metadata = new HashMap<>();
        }
        metadata.put(key, value);
        return this;
    }

    /**
     * Convert to ResponseEntity
     */
    public ResponseEntity<ApiResponse<T>> toResponseEntity() {
        return ResponseEntity.status(this.status).body(this);
    }

    /**
     * Create a response entity with data
     */
    public static <T> ResponseEntity<ApiResponse<T>> ok(T data) {
        return success(data).toResponseEntity();
    }

    /**
     * Create a response entity with message and data
     */
    public static <T> ResponseEntity<ApiResponse<T>> ok(String message, T data) {
        return success(message, data).toResponseEntity();
    }

    /**
     * Create a paginated response
     */
    public static <T> ApiResponse<T> paginated(T data, org.springframework.data.domain.Page<?> page) {
        ApiResponse<T> response = success(data);
        response.addMetadata("pagination", new HashMap<String, Object>() {{
            put("page", page.getNumber());
            put("size", page.getSize());
            put("totalElements", page.getTotalElements());
            put("totalPages", page.getTotalPages());
            put("first", page.isFirst());
            put("last", page.isLast());
        }});
        return response;
    }

    /**
     * Create a response with additional metadata
     */
    public static <T> ApiResponse<T> withMetadata(T data, Map<String, Object> metadata) {
        ApiResponse<T> response = success(data);
        response.setMetadata(metadata);
        return response;
    }

    /**
     * Check if response is successful
     */
    public boolean isSuccessful() {
        return this.success && this.status >= 200 && this.status < 300;
    }

    /**
     * Get error message if present
     */
    public String getErrorMessage() {
        if (!this.success && this.message != null) {
            return this.message;
        }
        return null;
    }

    /**
     * Get validation errors if present
     */
    public Map<String, String> getValidationErrors() {
        if (!this.success && this.errors instanceof Map) {
            return (Map<String, String>) this.errors;
        }
        return null;
    }

    /**
     * Create a builder with common fields
     */
    public static <T> ApiResponseBuilder<T> builder() {
        return new ApiResponseBuilder<T>()
                .timestamp(LocalDateTime.now());
    }
}
