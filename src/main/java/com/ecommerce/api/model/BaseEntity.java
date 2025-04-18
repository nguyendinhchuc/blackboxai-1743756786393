package com.ecommerce.api.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedBy;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import jakarta.persistence.*;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@Getter
@Setter
@MappedSuperclass
@EntityListeners(AuditingEntityListener.class)
public abstract class BaseEntity implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @CreatedBy
    @Column(name = "created_by")
    @JsonIgnore
    private String createdBy;

    @LastModifiedBy
    @Column(name = "updated_by")
    @JsonIgnore
    private String updatedBy;

    @Column(name = "deleted_at")
    @JsonIgnore
    private LocalDateTime deletedAt;

    @Column(name = "deleted_by")
    @JsonIgnore
    private String deletedBy;

    @Version
    @Column(name = "version")
    @JsonIgnore
    private Long version;

    @Column(name = "active", nullable = false)
    private Boolean active = true;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
        if (active == null) {
            active = true;
        }
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    /**
     * Soft delete the entity
     */
    public void softDelete(String deletedBy) {
        this.deletedAt = LocalDateTime.now();
        this.deletedBy = deletedBy;
        this.active = false;
    }

    /**
     * Restore a soft-deleted entity
     */
    public void restore(String restoredBy) {
        this.deletedAt = null;
        this.deletedBy = null;
        this.active = true;
        this.updatedBy = restoredBy;
        this.updatedAt = LocalDateTime.now();
    }

    /**
     * Check if entity is deleted
     */
    @JsonIgnore
    public boolean isDeleted() {
        return deletedAt != null;
    }

    /**
     * Check if entity is new
     */
    @JsonIgnore
    public boolean isNew() {
        return id == null;
    }

    /**
     * Get time since creation
     */
    @JsonIgnore
    public long getAgeDays() {
        if (createdAt == null) {
            return 0;
        }
        return java.time.Duration.between(createdAt, LocalDateTime.now()).toDays();
    }

    /**
     * Get time since last update
     */
    @JsonIgnore
    public long getLastUpdateDays() {
        if (updatedAt == null) {
            return 0;
        }
        return java.time.Duration.between(updatedAt, LocalDateTime.now()).toDays();
    }

    /**
     * Get time since deletion
     */
    @JsonIgnore
    public Long getDeletedDays() {
        if (deletedAt == null) {
            return null;
        }
        return java.time.Duration.between(deletedAt, LocalDateTime.now()).toDays();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof BaseEntity)) return false;
        BaseEntity that = (BaseEntity) o;
        return id != null && id.equals(that.getId());
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }

    /**
     * Create a copy of the entity
     */
    public BaseEntity copy() {
        try {
            BaseEntity copy = (BaseEntity) super.clone();
            copy.id = null;
            copy.createdAt = null;
            copy.createdBy = null;
            copy.updatedAt = null;
            copy.updatedBy = null;
            copy.deletedAt = null;
            copy.deletedBy = null;
            copy.version = null;
            return copy;
        } catch (CloneNotSupportedException e) {
            throw new RuntimeException("Failed to clone entity", e);
        }
    }

    /**
     * Convert entity to map
     */
    public Map<String, Object> toMap() {
        Map<String, Object> map = new HashMap<>();
        map.put("id", id);
        map.put("createdAt", createdAt);
        map.put("updatedAt", updatedAt);
        map.put("active", active);
        return map;
    }

    /**
     * Convert map to entity
     */
    public void fromMap(Map<String, Object> map) {
        if (map.containsKey("id")) {
            this.id = Long.valueOf(map.get("id").toString());
        }
        if (map.containsKey("active")) {
            this.active = Boolean.valueOf(map.get("active").toString());
        }
    }
}
