package com.ecommerce.api.model;

import lombok.Getter;
import lombok.Setter;
import org.hibernate.envers.RevisionEntity;
import org.hibernate.envers.RevisionNumber;
import org.hibernate.envers.RevisionTimestamp;

import jakarta.persistence.*;
import java.io.Serializable;
import java.text.DateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@Entity
@Table(name = "revisions")
@RevisionEntity(com.ecommerce.api.config.AuditConfig.CustomRevisionListener.class)
@Getter
@Setter
public class Revision implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @RevisionNumber
    private Long id;

    @RevisionTimestamp
    private long timestamp;

    @Column(name = "username", nullable = false)
    private String username;

    @Column(name = "ip_address")
    private String ipAddress;

    @Column(name = "user_agent")
    private String userAgent;

    @Column(name = "revision_type")
    @Enumerated(EnumType.STRING)
    private RevisionType revisionType;

    @Column(name = "entity_name")
    private String entityName;

    @Column(name = "entity_id")
    private Long entityId;

    @Column(name = "changes", columnDefinition = "TEXT")
    private String changes;

    @Column(name = "reason")
    private String reason;

    /**
     * Get revision date
     */
    @Transient
    public Date getRevisionDate() {
        return new Date(timestamp);
    }

    /**
     * Get formatted revision date
     */
    @Transient
    public String getFormattedRevisionDate() {
        return DateFormat.getDateTimeInstance().format(getRevisionDate());
    }

    /**
     * Get changes as map
     */
    @Transient
    public Map<String, Object> getChangesAsMap() {
        if (changes == null || changes.isEmpty()) {
            return new HashMap<>();
        }
        try {
            return new com.fasterxml.jackson.databind.ObjectMapper().readValue(
                changes, 
                new com.fasterxml.jackson.core.type.TypeReference<Map<String, Object>>() {}
            );
        } catch (Exception e) {
            return new HashMap<>();
        }
    }

    /**
     * Set changes from map
     */
    public void setChangesFromMap(Map<String, Object> changesMap) {
        if (changesMap == null) {
            this.changes = null;
            return;
        }
        try {
            this.changes = new com.fasterxml.jackson.databind.ObjectMapper()
                .writeValueAsString(changesMap);
        } catch (Exception e) {
            this.changes = null;
        }
    }

    /**
     * Revision type enum
     */
    public enum RevisionType {
        INSERT,
        UPDATE,
        DELETE
    }

    /**
     * Convert to DTO
     */
    public com.ecommerce.api.dto.RevisionDTO toDTO() {
        return com.ecommerce.api.dto.RevisionDTO.builder()
                .id(id)
                .timestamp(timestamp)
                .username(username)
                .ipAddress(ipAddress)
                .userAgent(userAgent)
                .revisionType(revisionType)
                .entityName(entityName)
                .entityId(entityId)
                .changes(getChangesAsMap())
                .reason(reason)
                .build();
    }

    /**
     * Create from DTO
     */
    public static Revision fromDTO(com.ecommerce.api.dto.RevisionDTO dto) {
        Revision revision = new Revision();
        revision.setId(dto.getId());
        revision.setTimestamp(dto.getTimestamp());
        revision.setUsername(dto.getUsername());
        revision.setIpAddress(dto.getIpAddress());
        revision.setUserAgent(dto.getUserAgent());
        revision.setRevisionType(dto.getRevisionType());
        revision.setEntityName(dto.getEntityName());
        revision.setEntityId(dto.getEntityId());
        revision.setChangesFromMap(dto.getChanges());
        revision.setReason(dto.getReason());
        return revision;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Revision)) return false;
        Revision that = (Revision) o;
        return id != null && id.equals(that.getId());
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
}
