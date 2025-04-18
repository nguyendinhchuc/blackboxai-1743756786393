package com.ecommerce.api.service;

import com.ecommerce.api.exception.CustomException;
import com.ecommerce.api.model.BaseEntity;
import com.ecommerce.api.repository.BaseRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public abstract class BaseService<T extends BaseEntity> {

    protected final BaseRepository<T> repository;

    protected BaseService(BaseRepository<T> repository) {
        this.repository = repository;
    }

    /**
     * Find all entities
     */
    public List<T> findAll() {
        return repository.findAllActive();
    }

    /**
     * Find all entities with pagination
     */
    public Page<T> findAll(Pageable pageable) {
        return repository.findAllActive(pageable);
    }

    /**
     * Find all entities with specification and pagination
     */
    public Page<T> findAll(Specification<T> spec, Pageable pageable) {
        return repository.findAllBySpecificationAndActive(spec, true, pageable);
    }

    /**
     * Find by id
     */
    public Optional<T> findById(Long id) {
        return repository.findByIdActive(id);
    }

    /**
     * Find by id or throw exception
     */
    public T findByIdOrThrow(Long id) {
        return findById(id)
            .orElseThrow(() -> new CustomException.ResourceNotFoundException(
                getEntityName(), "id", id));
    }

    /**
     * Save entity
     */
    @Transactional
    public T save(T entity) {
        validateEntity(entity);
        beforeSave(entity);
        T savedEntity = repository.save(entity);
        afterSave(savedEntity);
        return savedEntity;
    }

    /**
     * Update entity
     */
    @Transactional
    public T update(Long id, T entity) {
        T existingEntity = findByIdOrThrow(id);
        validateEntity(entity);
        beforeUpdate(existingEntity, entity);
        updateEntityFields(existingEntity, entity);
        T updatedEntity = repository.save(existingEntity);
        afterUpdate(updatedEntity);
        return updatedEntity;
    }

    /**
     * Delete entity
     */
    @Transactional
    public void delete(Long id) {
        T entity = findByIdOrThrow(id);
        beforeDelete(entity);
        repository.softDelete(id, LocalDateTime.now(), getCurrentUser());
        afterDelete(entity);
    }

    /**
     * Restore entity
     */
    @Transactional
    public T restore(Long id) {
        validateRestore(id);
        beforeRestore(id);
        repository.restore(id, LocalDateTime.now(), getCurrentUser());
        T restoredEntity = findByIdOrThrow(id);
        afterRestore(restoredEntity);
        return restoredEntity;
    }

    /**
     * Count active entities
     */
    public long count() {
        return repository.countActive();
    }

    /**
     * Exists by id
     */
    public boolean existsById(Long id) {
        return repository.findByIdActive(id).isPresent();
    }

    /**
     * Find all by ids
     */
    public List<T> findAllById(Iterable<Long> ids) {
        return repository.findAllById(ids);
    }

    /**
     * Save all entities
     */
    @Transactional
    public List<T> saveAll(Iterable<T> entities) {
        entities.forEach(this::validateEntity);
        entities.forEach(this::beforeSave);
        List<T> savedEntities = repository.saveAll(entities);
        savedEntities.forEach(this::afterSave);
        return savedEntities;
    }

    /**
     * Delete all by ids
     */
    @Transactional
    public void deleteAll(Iterable<Long> ids) {
        ids.forEach(this::delete);
    }

    /**
     * Validate entity before save/update
     */
    protected void validateEntity(T entity) {
        // Override in specific service if needed
    }

    /**
     * Update entity fields
     */
    protected void updateEntityFields(T existingEntity, T newEntity) {
        // Override in specific service to update fields
    }

    /**
     * Get current user
     */
    protected String getCurrentUser() {
        // Override in specific service to get current user
        return "system";
    }

    /**
     * Get entity name
     */
    protected String getEntityName() {
        return "Entity";
    }

    /**
     * Validate restore operation
     */
    protected void validateRestore(Long id) {
        // Override in specific service if needed
    }

    /**
     * Before save hook
     */
    protected void beforeSave(T entity) {
        // Override in specific service if needed
    }

    /**
     * After save hook
     */
    protected void afterSave(T entity) {
        // Override in specific service if needed
    }

    /**
     * Before update hook
     */
    protected void beforeUpdate(T existingEntity, T newEntity) {
        // Override in specific service if needed
    }

    /**
     * After update hook
     */
    protected void afterUpdate(T entity) {
        // Override in specific service if needed
    }

    /**
     * Before delete hook
     */
    protected void beforeDelete(T entity) {
        // Override in specific service if needed
    }

    /**
     * After delete hook
     */
    protected void afterDelete(T entity) {
        // Override in specific service if needed
    }

    /**
     * Before restore hook
     */
    protected void beforeRestore(Long id) {
        // Override in specific service if needed
    }

    /**
     * After restore hook
     */
    protected void afterRestore(T entity) {
        // Override in specific service if needed
    }

    /**
     * Find all deleted entities
     */
    public List<T> findAllDeleted() {
        return repository.findAllDeleted();
    }

    /**
     * Find all deleted entities with pagination
     */
    public Page<T> findAllDeleted(Pageable pageable) {
        return repository.findAllDeleted(pageable);
    }

    /**
     * Find all by created date range
     */
    public List<T> findAllByCreatedAtBetween(LocalDateTime start, LocalDateTime end) {
        return repository.findAllByCreatedAtBetween(start, end);
    }

    /**
     * Find all by updated date range
     */
    public List<T> findAllByUpdatedAtBetween(LocalDateTime start, LocalDateTime end) {
        return repository.findAllByUpdatedAtBetween(start, end);
    }

    /**
     * Find all by created by
     */
    public List<T> findAllByCreatedBy(String createdBy) {
        return repository.findAllByCreatedBy(createdBy);
    }

    /**
     * Find all by updated by
     */
    public List<T> findAllByUpdatedBy(String updatedBy) {
        return repository.findAllByUpdatedBy(updatedBy);
    }

    /**
     * Find all by deleted by
     */
    public List<T> findAllByDeletedBy(String deletedBy) {
        return repository.findAllByDeletedBy(deletedBy);
    }

    /**
     * Permanently delete old entities
     */
    @Transactional
    public void permanentlyDeleteOldEntities(LocalDateTime before) {
        repository.permanentlyDeleteOldEntities(before);
    }

    /**
     * Update active status
     */
    @Transactional
    public void updateActiveStatus(Long id, boolean active) {
        repository.updateActiveStatus(id, active, LocalDateTime.now(), getCurrentUser());
    }
}
