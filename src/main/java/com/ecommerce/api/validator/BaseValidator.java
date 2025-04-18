package com.ecommerce.api.validator;

import com.ecommerce.api.exception.CustomException;
import com.ecommerce.api.model.BaseEntity;
import org.springframework.validation.Errors;
import org.springframework.validation.ValidationUtils;
import org.springframework.validation.Validator;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;

public abstract class BaseValidator<T extends BaseEntity> implements Validator {

    // Common validation patterns
    protected static final Pattern EMAIL_PATTERN = 
        Pattern.compile("^[A-Za-z0-9+_.-]+@(.+)$");
    
    protected static final Pattern PHONE_PATTERN = 
        Pattern.compile("^\\+?[1-9]\\d{1,14}$");
    
    protected static final Pattern URL_PATTERN = 
        Pattern.compile("^(https?://)?([\\da-z.-]+)\\.([a-z.]{2,6})[/\\w .-]*/?$");
    
    protected static final Pattern USERNAME_PATTERN = 
        Pattern.compile("^[a-zA-Z0-9_-]{3,16}$");
    
    protected static final Pattern PASSWORD_PATTERN = 
        Pattern.compile("^(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z])(?=.*[@#$%^&+=])(?=\\S+$).{8,}$");

    @Override
    public boolean supports(Class<?> clazz) {
        return BaseEntity.class.isAssignableFrom(clazz);
    }

    @Override
    public void validate(Object target, Errors errors) {
        T entity = (T) target;
        
        // Validate common fields
        validateCommonFields(entity, errors);
        
        // Validate specific fields
        validateSpecificFields(entity, errors);
    }

    /**
     * Validate common fields
     */
    protected void validateCommonFields(T entity, Errors errors) {
        // Active status validation
        ValidationUtils.rejectIfEmpty(errors, "active", "field.required", "Active status is required");

        // Created/Updated date validation
        if (entity.getCreatedAt() != null && entity.getCreatedAt().isAfter(LocalDateTime.now())) {
            errors.rejectValue("createdAt", "date.future", "Created date cannot be in the future");
        }

        if (entity.getUpdatedAt() != null && entity.getUpdatedAt().isAfter(LocalDateTime.now())) {
            errors.rejectValue("updatedAt", "date.future", "Updated date cannot be in the future");
        }

        // Version validation
        if (entity.getVersion() != null && entity.getVersion() < 0) {
            errors.rejectValue("version", "version.negative", "Version cannot be negative");
        }
    }

    /**
     * Validate specific fields
     */
    protected abstract void validateSpecificFields(T entity, Errors errors);

    /**
     * Validate email
     */
    protected void validateEmail(String email, Errors errors, String field) {
        if (email != null && !email.isEmpty() && !EMAIL_PATTERN.matcher(email).matches()) {
            errors.rejectValue(field, "email.invalid", "Invalid email format");
        }
    }

    /**
     * Validate phone
     */
    protected void validatePhone(String phone, Errors errors, String field) {
        if (phone != null && !phone.isEmpty() && !PHONE_PATTERN.matcher(phone).matches()) {
            errors.rejectValue(field, "phone.invalid", "Invalid phone format");
        }
    }

    /**
     * Validate URL
     */
    protected void validateUrl(String url, Errors errors, String field) {
        if (url != null && !url.isEmpty() && !URL_PATTERN.matcher(url).matches()) {
            errors.rejectValue(field, "url.invalid", "Invalid URL format");
        }
    }

    /**
     * Validate username
     */
    protected void validateUsername(String username, Errors errors, String field) {
        if (username != null && !username.isEmpty() && !USERNAME_PATTERN.matcher(username).matches()) {
            errors.rejectValue(field, "username.invalid", 
                "Username must be 3-16 characters and contain only letters, numbers, underscore, and hyphen");
        }
    }

    /**
     * Validate password
     */
    protected void validatePassword(String password, Errors errors, String field) {
        if (password != null && !password.isEmpty() && !PASSWORD_PATTERN.matcher(password).matches()) {
            errors.rejectValue(field, "password.invalid", 
                "Password must be at least 8 characters and contain at least one uppercase letter, " +
                "one lowercase letter, one number, and one special character");
        }
    }

    /**
     * Validate string length
     */
    protected void validateStringLength(String value, int minLength, int maxLength, 
                                     Errors errors, String field) {
        if (value != null) {
            if (value.length() < minLength) {
                errors.rejectValue(field, "string.tooShort", 
                    String.format("Must be at least %d characters", minLength));
            }
            if (value.length() > maxLength) {
                errors.rejectValue(field, "string.tooLong", 
                    String.format("Must be at most %d characters", maxLength));
            }
        }
    }

    /**
     * Validate number range
     */
    protected void validateNumberRange(Number value, Number min, Number max, 
                                    Errors errors, String field) {
        if (value != null) {
            if (value.doubleValue() < min.doubleValue()) {
                errors.rejectValue(field, "number.tooSmall", 
                    String.format("Must be at least %s", min));
            }
            if (value.doubleValue() > max.doubleValue()) {
                errors.rejectValue(field, "number.tooLarge", 
                    String.format("Must be at most %s", max));
            }
        }
    }

    /**
     * Validate date range
     */
    protected void validateDateRange(LocalDateTime value, LocalDateTime min, LocalDateTime max, 
                                  Errors errors, String field) {
        if (value != null) {
            if (value.isBefore(min)) {
                errors.rejectValue(field, "date.tooEarly", 
                    String.format("Must be after %s", min));
            }
            if (value.isAfter(max)) {
                errors.rejectValue(field, "date.tooLate", 
                    String.format("Must be before %s", max));
            }
        }
    }

    /**
     * Get validation errors as map
     */
    protected Map<String, String> getValidationErrors(Errors errors) {
        Map<String, String> validationErrors = new HashMap<>();
        errors.getFieldErrors().forEach(error ->
            validationErrors.put(error.getField(), error.getDefaultMessage())
        );
        return validationErrors;
    }

    /**
     * Throw validation exception
     */
    protected void throwValidationException(Errors errors) {
        Map<String, String> validationErrors = getValidationErrors(errors);
        throw new CustomException.ValidationException("Validation failed", validationErrors);
    }

    /**
     * Validate and throw
     */
    public void validateAndThrow(T entity) {
        org.springframework.validation.BeanPropertyBindingResult errors = 
            new org.springframework.validation.BeanPropertyBindingResult(entity, "entity");
        validate(entity, errors);
        if (errors.hasErrors()) {
            throwValidationException(errors);
        }
    }
}
