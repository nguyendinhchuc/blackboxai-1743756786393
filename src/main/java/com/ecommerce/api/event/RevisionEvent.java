package com.ecommerce.api.event;

import com.ecommerce.api.model.BaseEntity;
import com.ecommerce.api.model.Revision;
import lombok.Getter;
import org.springframework.context.ApplicationEvent;

import jakarta.servlet.http.HttpServletRequest;
import java.util.Map;

@Getter
public class RevisionEvent extends ApplicationEvent {

    private final String entityName;
    private final Long entityId;
    private final Revision.RevisionType revisionType;
    private final Map<String, Object> changes;
    private final String reason;
    private final HttpServletRequest request;
    private final String username;
    private final BaseEntity entity;
    private final BaseEntity oldEntity;

    /**
     * Create a new RevisionEvent
     */
    public RevisionEvent(Object source,
                        String entityName,
                        Long entityId,
                        Revision.RevisionType revisionType,
                        Map<String, Object> changes,
                        String reason,
                        HttpServletRequest request,
                        String username,
                        BaseEntity entity,
                        BaseEntity oldEntity) {
        super(source);
        this.entityName = entityName;
        this.entityId = entityId;
        this.revisionType = revisionType;
        this.changes = changes;
        this.reason = reason;
        this.request = request;
        this.username = username;
        this.entity = entity;
        this.oldEntity = oldEntity;
    }

    /**
     * Builder for RevisionEvent
     */
    public static RevisionEventBuilder builder() {
        return new RevisionEventBuilder();
    }

    public static class RevisionEventBuilder {
        private Object source;
        private String entityName;
        private Long entityId;
        private Revision.RevisionType revisionType;
        private Map<String, Object> changes;
        private String reason;
        private HttpServletRequest request;
        private String username;
        private BaseEntity entity;
        private BaseEntity oldEntity;

        RevisionEventBuilder() {
        }

        public RevisionEventBuilder source(Object source) {
            this.source = source;
            return this;
        }

        public RevisionEventBuilder entityName(String entityName) {
            this.entityName = entityName;
            return this;
        }

        public RevisionEventBuilder entityId(Long entityId) {
            this.entityId = entityId;
            return this;
        }

        public RevisionEventBuilder revisionType(Revision.RevisionType revisionType) {
            this.revisionType = revisionType;
            return this;
        }

        public RevisionEventBuilder changes(Map<String, Object> changes) {
            this.changes = changes;
            return this;
        }

        public RevisionEventBuilder reason(String reason) {
            this.reason = reason;
            return this;
        }

        public RevisionEventBuilder request(HttpServletRequest request) {
            this.request = request;
            return this;
        }

        public RevisionEventBuilder username(String username) {
            this.username = username;
            return this;
        }

        public RevisionEventBuilder entity(BaseEntity entity) {
            this.entity = entity;
            return this;
        }

        public RevisionEventBuilder oldEntity(BaseEntity oldEntity) {
            this.oldEntity = oldEntity;
            return this;
        }

        public RevisionEvent build() {
            return new RevisionEvent(source, entityName, entityId, revisionType, changes, 
                                   reason, request, username, entity, oldEntity);
        }
    }
}
