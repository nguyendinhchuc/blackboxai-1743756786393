package com.ecommerce.api.util;

import com.ecommerce.api.model.BaseEntity;
import com.ecommerce.api.model.Revision;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.ReflectionUtils;

import java.lang.reflect.Field;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
public class RevisionUtils {

    private static final ObjectMapper objectMapper = new ObjectMapper()
        .registerModule(new JavaTimeModule())
        .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

    /**
     * Compare two entities and create changes map
     */
    public static Map<String, Object> compareEntities(BaseEntity oldEntity, BaseEntity newEntity) {
        Map<String, Object> changes = new HashMap<>();
        
        if (oldEntity == null && newEntity != null) {
            // Creation case - capture all non-null fields
            changes.putAll(entityToMap(newEntity));
        } else if (oldEntity != null && newEntity != null) {
            // Update case - capture only changed fields
            Map<String, Object> oldValues = entityToMap(oldEntity);
            Map<String, Object> newValues = entityToMap(newEntity);
            
            for (Map.Entry<String, Object> entry : newValues.entrySet()) {
                String key = entry.getKey();
                Object newValue = entry.getValue();
                Object oldValue = oldValues.get(key);
                
                if (!Objects.equals(newValue, oldValue)) {
                    changes.put(key, Map.of(
                        "old", oldValue,
                        "new", newValue
                    ));
                }
            }
        }
        
        return changes;
    }

    /**
     * Convert entity to map
     */
    public static Map<String, Object> entityToMap(BaseEntity entity) {
        Map<String, Object> map = new HashMap<>();
        
        ReflectionUtils.doWithFields(entity.getClass(), field -> {
            field.setAccessible(true);
            Object value = field.get(entity);
            if (value != null) {
                if (value instanceof BaseEntity) {
                    // For entity references, store only id
                    map.put(field.getName() + "Id", ((BaseEntity) value).getId());
                } else if (value instanceof Collection) {
                    // For collections, store size and ids if elements are entities
                    Collection<?> collection = (Collection<?>) value;
                    map.put(field.getName() + "Size", collection.size());
                    if (!collection.isEmpty() && collection.iterator().next() instanceof BaseEntity) {
                        List<Long> ids = collection.stream()
                            .map(item -> ((BaseEntity) item).getId())
                            .collect(Collectors.toList());
                        map.put(field.getName() + "Ids", ids);
                    }
                } else {
                    map.put(field.getName(), value);
                }
            }
        });
        
        return map;
    }

    /**
     * Convert map to JSON string
     */
    public static String mapToJson(Map<String, Object> map) {
        try {
            return objectMapper.writeValueAsString(map);
        } catch (JsonProcessingException e) {
            log.error("Error converting map to JSON: {}", e.getMessage());
            return "{}";
        }
    }

    /**
     * Convert JSON string to map
     */
    public static Map<String, Object> jsonToMap(String json) {
        try {
            return objectMapper.readValue(json, Map.class);
        } catch (JsonProcessingException e) {
            log.error("Error converting JSON to map: {}", e.getMessage());
            return new HashMap<>();
        }
    }

    /**
     * Format timestamp as LocalDateTime
     */
    public static LocalDateTime timestampToLocalDateTime(long timestamp) {
        return LocalDateTime.ofInstant(
            java.time.Instant.ofEpochMilli(timestamp),
            ZoneId.systemDefault()
        );
    }

    /**
     * Convert LocalDateTime to timestamp
     */
    public static long localDateTimeToTimestamp(LocalDateTime dateTime) {
        return dateTime.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli();
    }

    /**
     * Get field value using reflection
     */
    public static Object getFieldValue(Object object, String fieldName) {
        try {
            Field field = ReflectionUtils.findField(object.getClass(), fieldName);
            if (field != null) {
                field.setAccessible(true);
                return field.get(object);
            }
        } catch (Exception e) {
            log.error("Error getting field value: {}", e.getMessage());
        }
        return null;
    }

    /**
     * Format changes for display
     */
    public static String formatChangesForDisplay(Map<String, Object> changes) {
        StringBuilder builder = new StringBuilder();
        changes.forEach((key, value) -> {
            if (value instanceof Map) {
                Map<String, Object> changeMap = (Map<String, Object>) value;
                builder.append(String.format("%s: %s → %s\n",
                    formatFieldName(key),
                    formatValue(changeMap.get("old")),
                    formatValue(changeMap.get("new"))
                ));
            } else {
                builder.append(String.format("%s: %s\n",
                    formatFieldName(key),
                    formatValue(value)
                ));
            }
        });
        return builder.toString();
    }

    /**
     * Format field name for display
     */
    private static String formatFieldName(String fieldName) {
        return Arrays.stream(fieldName.split("(?=\\p{Upper})"))
            .map(String::toLowerCase)
            .collect(Collectors.joining(" "));
    }

    /**
     * Format value for display
     */
    private static String formatValue(Object value) {
        if (value == null) {
            return "null";
        }
        if (value instanceof LocalDateTime) {
            return ((LocalDateTime) value).toString();
        }
        if (value instanceof Collection) {
            return "[" + ((Collection<?>) value).size() + " items]";
        }
        return value.toString();
    }

    /**
     * Create revision summary
     */
    public static String createRevisionSummary(Revision revision) {
        return String.format("%s %s %s (ID: %d) by %s at %s",
            revision.getRevisionType(),
            revision.getEntityName(),
            revision.getEntityId(),
            revision.getId(),
            revision.getUsername(),
            timestampToLocalDateTime(revision.getTimestamp())
        );
    }

    /**
     * Check if field should be excluded from revision tracking
     */
    public static boolean isFieldExcluded(String fieldName, Set<String> excludedFields) {
        return excludedFields.contains(fieldName) ||
               fieldName.contains("password") ||
               fieldName.contains("secret") ||
               fieldName.contains("token");
    }

    /**
     * Sanitize sensitive data from changes
     */
    public static Map<String, Object> sanitizeChanges(Map<String, Object> changes, Set<String> excludedFields) {
        return changes.entrySet().stream()
            .filter(entry -> !isFieldExcluded(entry.getKey(), excludedFields))
            .collect(Collectors.toMap(
                Map.Entry::getKey,
                Map.Entry::getValue
            ));
    }
}
