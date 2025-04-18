package com.ecommerce.api.aspect;

import com.ecommerce.api.model.BaseEntity;
import com.ecommerce.api.model.Revision;
import com.ecommerce.api.service.RevisionService;
import lombok.RequiredArgsConstructor;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import jakarta.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.Map;

@Aspect
@Component
@RequiredArgsConstructor
public class RevisionAspect {

    private final RevisionService revisionService;

    /**
     * Create revision after entity creation
     */
    @AfterReturning(
        pointcut = "execution(* com.ecommerce.api.service.*.create*(..)) && args(entity)",
        returning = "result"
    )
    public void afterCreate(JoinPoint joinPoint, BaseEntity entity, Object result) {
        if (entity != null && result instanceof BaseEntity) {
            createRevision(
                (BaseEntity) result,
                Revision.RevisionType.INSERT,
                "Entity created",
                createChangesMap(null, (BaseEntity) result)
            );
        }
    }

    /**
     * Create revision after entity update
     */
    @AfterReturning(
        pointcut = "execution(* com.ecommerce.api.service.*.update*(..)) && args(id, entity)",
        returning = "result"
    )
    public void afterUpdate(JoinPoint joinPoint, Long id, BaseEntity entity, Object result) {
        if (entity != null && result instanceof BaseEntity) {
            createRevision(
                (BaseEntity) result,
                Revision.RevisionType.UPDATE,
                "Entity updated",
                createChangesMap(entity, (BaseEntity) result)
            );
        }
    }

    /**
     * Create revision after entity deletion
     */
    @AfterReturning(
        pointcut = "execution(* com.ecommerce.api.service.*.delete*(..)) && args(id)"
    )
    public void afterDelete(JoinPoint joinPoint, Long id) {
        String entityName = extractEntityName(joinPoint);
        if (entityName != null) {
            Map<String, Object> changes = new HashMap<>();
            changes.put("id", id);
            changes.put("deleted", true);

            revisionService.createRevision(
                entityName,
                id,
                Revision.RevisionType.DELETE,
                changes,
                "Entity deleted",
                getCurrentRequest()
            );
        }
    }

    /**
     * Create revision after entity restore
     */
    @AfterReturning(
        pointcut = "execution(* com.ecommerce.api.service.*.restore*(..)) && args(id)",
        returning = "result"
    )
    public void afterRestore(JoinPoint joinPoint, Long id, Object result) {
        if (result instanceof BaseEntity) {
            BaseEntity entity = (BaseEntity) result;
            createRevision(
                entity,
                Revision.RevisionType.UPDATE,
                "Entity restored",
                createRestoreChangesMap(entity)
            );
        }
    }

    /**
     * Create revision
     */
    private void createRevision(BaseEntity entity, Revision.RevisionType type, 
                              String reason, Map<String, Object> changes) {
        revisionService.createRevision(
            entity.getClass().getSimpleName(),
            entity.getId(),
            type,
            changes,
            reason,
            getCurrentRequest()
        );
    }

    /**
     * Create changes map for entity comparison
     */
    private Map<String, Object> createChangesMap(BaseEntity oldEntity, BaseEntity newEntity) {
        Map<String, Object> changes = new HashMap<>();
        
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
                    changes.put(key, new HashMap<String, Object>() {{
                        put("old", oldValue);
                        put("new", newValue);
                    }});
                }
            }
        }
        
        return changes;
    }

    /**
     * Create changes map for entity restore
     */
    private Map<String, Object> createRestoreChangesMap(BaseEntity entity) {
        Map<String, Object> changes = new HashMap<>();
        changes.put("restored", true);
        changes.put("active", true);
        changes.put("deletedAt", null);
        changes.put("deletedBy", null);
        return changes;
    }

    /**
     * Convert entity to map
     */
    private Map<String, Object> entityToMap(BaseEntity entity) {
        Map<String, Object> map = new HashMap<>();
        
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

    /**
     * Extract entity name from join point
     */
    private String extractEntityName(JoinPoint joinPoint) {
        String className = joinPoint.getTarget().getClass().getSimpleName();
        return className.replace("Service", "");
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
}
