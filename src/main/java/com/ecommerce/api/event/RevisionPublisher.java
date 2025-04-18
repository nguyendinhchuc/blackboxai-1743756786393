package com.ecommerce.api.event;

import com.ecommerce.api.model.BaseEntity;
import com.ecommerce.api.model.Revision;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import jakarta.servlet.http.HttpServletRequest;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class RevisionPublisher {

    private final ApplicationEventPublisher eventPublisher;

    /**
     * Publish revision event
     */
    public void publishRevisionEvent(String entityName,
                                   Long entityId,
                                   Revision.RevisionType revisionType,
                                   Map<String, Object> changes,
                                   String reason,
                                   BaseEntity entity,
                                   BaseEntity oldEntity) {
        HttpServletRequest request = getCurrentRequest();
        String username = getCurrentUsername();

        RevisionEvent event = RevisionEvent.builder()
                .source(this)
                .entityName(entityName)
                .entityId(entityId)
                .revisionType(revisionType)
                .changes(changes)
                .reason(reason)
                .request(request)
                .username(username)
                .entity(entity)
                .oldEntity(oldEntity)
                .build();

        eventPublisher.publishEvent(event);
    }

    /**
     * Publish immediate revision event
     */
    public void publishImmediateRevisionEvent(String entityName,
                                            Long entityId,
                                            Revision.RevisionType revisionType,
                                            Map<String, Object> changes,
                                            String reason,
                                            BaseEntity entity,
                                            BaseEntity oldEntity) {
        RevisionEvent event = RevisionEvent.builder()
                .source(this)
                .entityName(entityName)
                .entityId(entityId)
                .revisionType(revisionType)
                .changes(changes)
                .reason(reason)
                .request(getCurrentRequest())
                .username(getCurrentUsername())
                .entity(entity)
                .oldEntity(oldEntity)
                .build();

        eventPublisher.publishEvent(new RevisionEventListener.ImmediateRevisionEvent(event));
    }

    /**
     * Publish create revision event
     */
    public void publishCreateEvent(BaseEntity entity, Map<String, Object> changes) {
        publishRevisionEvent(
            entity.getClass().getSimpleName(),
            entity.getId(),
            Revision.RevisionType.INSERT,
            changes,
            "Entity created",
            entity,
            null
        );
    }

    /**
     * Publish update revision event
     */
    public void publishUpdateEvent(BaseEntity entity, BaseEntity oldEntity, Map<String, Object> changes) {
        publishRevisionEvent(
            entity.getClass().getSimpleName(),
            entity.getId(),
            Revision.RevisionType.UPDATE,
            changes,
            "Entity updated",
            entity,
            oldEntity
        );
    }

    /**
     * Publish delete revision event
     */
    public void publishDeleteEvent(BaseEntity entity) {
        Map<String, Object> changes = Map.of(
            "id", entity.getId(),
            "deleted", true,
            "deletedAt", entity.getDeletedAt(),
            "deletedBy", entity.getDeletedBy()
        );

        publishRevisionEvent(
            entity.getClass().getSimpleName(),
            entity.getId(),
            Revision.RevisionType.DELETE,
            changes,
            "Entity deleted",
            entity,
            null
        );
    }

    /**
     * Publish restore revision event
     */
    public void publishRestoreEvent(BaseEntity entity) {
        Map<String, Object> changes = Map.of(
            "id", entity.getId(),
            "restored", true,
            "deletedAt", null,
            "deletedBy", null
        );

        publishRevisionEvent(
            entity.getClass().getSimpleName(),
            entity.getId(),
            Revision.RevisionType.UPDATE,
            changes,
            "Entity restored",
            entity,
            null
        );
    }

    /**
     * Get current HTTP request
     */
    private HttpServletRequest getCurrentRequest() {
        ServletRequestAttributes attributes = 
            (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        return attributes != null ? attributes.getRequest() : null;
    }

    /**
     * Get current username
     */
    private String getCurrentUsername() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.isAuthenticated()) {
            return authentication.getName();
        }
        return "system";
    }

    /**
     * Create changes map
     */
    public static Map<String, Object> createChangesMap(BaseEntity oldEntity, BaseEntity newEntity) {
        Map<String, Object> changes = new java.util.HashMap<>();
        
        if (oldEntity == null) {
            // For creation, store all non-null values
            changes.putAll(entityToMap(newEntity));
        } else {
            // For update, store only changed values
            Map<String, Object> oldValues = entityToMap(oldEntity);
            Map<String, Object> newValues = entityToMap(newEntity);
            
            for (Map.Entry<String, Object> entry : newValues.entrySet()) {
                String key = entry.getKey();
                Object newValue = entry.getValue();
                Object oldValue = oldValues.get(key);
                
                if (newValue != null && !newValue.equals(oldValue)) {
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
    private static Map<String, Object> entityToMap(BaseEntity entity) {
        Map<String, Object> map = new java.util.HashMap<>();
        
        // Use reflection to get all fields
        for (java.lang.reflect.Field field : entity.getClass().getDeclaredFields()) {
            field.setAccessible(true);
            try {
                Object value = field.get(entity);
                if (value != null) {
                    map.put(field.getName(), value);
                }
            } catch (IllegalAccessException e) {
                // Ignore inaccessible fields
            }
        }
        
        return map;
    }
}
