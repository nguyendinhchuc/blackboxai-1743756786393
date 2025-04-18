package com.ecommerce.api.validator;

import com.ecommerce.api.model.Revision;
import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Map;

@Component
public class RevisionValidator extends BaseValidator<Revision> {

    @Override
    protected void validateSpecificFields(Revision revision, Errors errors) {
        // Validate entity name
        if (revision.getEntityName() == null || revision.getEntityName().trim().isEmpty()) {
            errors.rejectValue("entityName", "field.required", "Entity name is required");
        } else if (revision.getEntityName().length() > 255) {
            errors.rejectValue("entityName", "field.maxLength", 
                "Entity name cannot be longer than 255 characters");
        }

        // Validate entity ID
        if (revision.getEntityId() == null) {
            errors.rejectValue("entityId", "field.required", "Entity ID is required");
        } else if (revision.getEntityId() <= 0) {
            errors.rejectValue("entityId", "field.positive", "Entity ID must be positive");
        }

        // Validate revision type
        if (revision.getRevisionType() == null) {
            errors.rejectValue("revisionType", "field.required", "Revision type is required");
        }

        // Validate timestamp
        if (revision.getTimestamp() <= 0) {
            errors.rejectValue("timestamp", "field.required", "Timestamp is required");
        } else {
            LocalDateTime revisionDate = LocalDateTime.ofInstant(
                Instant.ofEpochMilli(revision.getTimestamp()), 
                ZoneId.systemDefault()
            );
            if (revisionDate.isAfter(LocalDateTime.now())) {
                errors.rejectValue("timestamp", "field.future", 
                    "Timestamp cannot be in the future");
            }
        }

        // Validate username
        if (revision.getUsername() == null || revision.getUsername().trim().isEmpty()) {
            errors.rejectValue("username", "field.required", "Username is required");
        } else {
            validateUsername(revision.getUsername(), errors, "username");
        }

        // Validate IP address
        if (revision.getIpAddress() != null && !revision.getIpAddress().trim().isEmpty()) {
            validateIpAddress(revision.getIpAddress(), errors);
        }

        // Validate user agent
        if (revision.getUserAgent() != null && revision.getUserAgent().length() > 512) {
            errors.rejectValue("userAgent", "field.maxLength", 
                "User agent cannot be longer than 512 characters");
        }

        // Validate changes
        validateChanges(revision, errors);

        // Validate reason
        if (revision.getReason() != null && revision.getReason().length() > 1000) {
            errors.rejectValue("reason", "field.maxLength", 
                "Reason cannot be longer than 1000 characters");
        }
    }

    /**
     * Validate IP address format
     */
    private void validateIpAddress(String ipAddress, Errors errors) {
        String ipv4Pattern = "^((25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)\\.){3}" +
                           "(25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)$";
        String ipv6Pattern = "^([0-9a-fA-F]{1,4}:){7}[0-9a-fA-F]{1,4}$";

        if (!ipAddress.matches(ipv4Pattern) && !ipAddress.matches(ipv6Pattern)) {
            errors.rejectValue("ipAddress", "field.invalid", "Invalid IP address format");
        }
    }

    /**
     * Validate changes map
     */
    private void validateChanges(Revision revision, Errors errors) {
        Map<String, Object> changes = revision.getChangesAsMap();
        
        if (changes == null || changes.isEmpty()) {
            errors.rejectValue("changes", "field.required", "Changes are required");
            return;
        }

        // Validate maximum size of changes
        String changesString = revision.getChanges();
        if (changesString != null && changesString.length() > 10000) {
            errors.rejectValue("changes", "field.maxLength", 
                "Changes cannot be longer than 10000 characters");
        }

        // Validate change values
        for (Map.Entry<String, Object> entry : changes.entrySet()) {
            String key = entry.getKey();
            Object value = entry.getValue();

            // Validate key
            if (key == null || key.trim().isEmpty()) {
                errors.rejectValue("changes", "field.invalid", 
                    "Change key cannot be empty");
                continue;
            }

            if (key.length() > 255) {
                errors.rejectValue("changes", "field.maxLength", 
                    "Change key cannot be longer than 255 characters: " + key);
                continue;
            }

            // Validate value
            if (value != null) {
                String valueStr = value.toString();
                if (valueStr.length() > 1000) {
                    errors.rejectValue("changes", "field.maxLength", 
                        "Change value cannot be longer than 1000 characters: " + key);
                }
            }
        }
    }

    /**
     * Validate revision for specific entity type
     */
    public void validateForEntityType(Revision revision, Class<?> entityType, Errors errors) {
        // Validate entity name matches
        String expectedEntityName = entityType.getSimpleName();
        if (!expectedEntityName.equals(revision.getEntityName())) {
            errors.rejectValue("entityName", "field.mismatch", 
                "Entity name must match the entity type: " + expectedEntityName);
        }

        // Additional entity-specific validations can be added here
    }

    /**
     * Validate revision sequence
     */
    public void validateRevisionSequence(Revision revision, Revision previousRevision, Errors errors) {
        if (previousRevision != null) {
            // Validate timestamp sequence
            if (revision.getTimestamp() <= previousRevision.getTimestamp()) {
                errors.rejectValue("timestamp", "field.sequence", 
                    "Revision timestamp must be after the previous revision");
            }

            // Validate version sequence if applicable
            if (revision.getEntityName().equals(previousRevision.getEntityName()) && 
                revision.getEntityId().equals(previousRevision.getEntityId())) {
                // Additional sequence validations can be added here
            }
        }
    }
}
