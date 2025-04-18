package com.ecommerce.api.repository;

import com.ecommerce.api.model.BaseEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.NoRepositoryBean;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@NoRepositoryBean
public interface BaseRepository<T extends BaseEntity> extends JpaRepository<T, Long>, JpaSpecificationExecutor<T> {

    /**
     * Find all active entities
     */
    @Query("SELECT e FROM #{#entityName} e WHERE e.active = true AND e.deletedAt IS NULL")
    List<T> findAllActive();

    /**
     * Find all active entities with pagination
     */
    @Query("SELECT e FROM #{#entityName} e WHERE e.active = true AND e.deletedAt IS NULL")
    Page<T> findAllActive(Pageable pageable);

    /**
     * Find all deleted entities
     */
    @Query("SELECT e FROM #{#entityName} e WHERE e.deletedAt IS NOT NULL")
    List<T> findAllDeleted();

    /**
     * Find all deleted entities with pagination
     */
    @Query("SELECT e FROM #{#entityName} e WHERE e.deletedAt IS NOT NULL")
    Page<T> findAllDeleted(Pageable pageable);

    /**
     * Find by id only if active
     */
    @Query("SELECT e FROM #{#entityName} e WHERE e.id = ?1 AND e.active = true AND e.deletedAt IS NULL")
    Optional<T> findByIdActive(Long id);

    /**
     * Soft delete an entity
     */
    @Modifying
    @Transactional
    @Query("UPDATE #{#entityName} e SET e.deletedAt = ?2, e.deletedBy = ?3, e.active = false WHERE e.id = ?1")
    void softDelete(Long id, LocalDateTime deletedAt, String deletedBy);

    /**
     * Restore a soft-deleted entity
     */
    @Modifying
    @Transactional
    @Query("UPDATE #{#entityName} e SET e.deletedAt = NULL, e.deletedBy = NULL, e.active = true, e.updatedAt = ?2, e.updatedBy = ?3 WHERE e.id = ?1")
    void restore(Long id, LocalDateTime updatedAt, String updatedBy);

    /**
     * Find all by specification and active status
     */
    default Page<T> findAllBySpecificationAndActive(Specification<T> spec, boolean active, Pageable pageable) {
        return findAll(
            Specification.where(spec)
                .and((root, query, cb) -> cb.equal(root.get("active"), active))
                .and((root, query, cb) -> active ? 
                    cb.isNull(root.get("deletedAt")) : 
                    cb.isNotNull(root.get("deletedAt"))),
            pageable
        );
    }

    /**
     * Count active entities
     */
    @Query("SELECT COUNT(e) FROM #{#entityName} e WHERE e.active = true AND e.deletedAt IS NULL")
    long countActive();

    /**
     * Count deleted entities
     */
    @Query("SELECT COUNT(e) FROM #{#entityName} e WHERE e.deletedAt IS NOT NULL")
    long countDeleted();

    /**
     * Find all by created date range
     */
    @Query("SELECT e FROM #{#entityName} e WHERE e.createdAt BETWEEN ?1 AND ?2 AND e.active = true AND e.deletedAt IS NULL")
    List<T> findAllByCreatedAtBetween(LocalDateTime start, LocalDateTime end);

    /**
     * Find all by updated date range
     */
    @Query("SELECT e FROM #{#entityName} e WHERE e.updatedAt BETWEEN ?1 AND ?2 AND e.active = true AND e.deletedAt IS NULL")
    List<T> findAllByUpdatedAtBetween(LocalDateTime start, LocalDateTime end);

    /**
     * Find all by created by
     */
    @Query("SELECT e FROM #{#entityName} e WHERE e.createdBy = ?1 AND e.active = true AND e.deletedAt IS NULL")
    List<T> findAllByCreatedBy(String createdBy);

    /**
     * Find all by updated by
     */
    @Query("SELECT e FROM #{#entityName} e WHERE e.updatedBy = ?1 AND e.active = true AND e.deletedAt IS NULL")
    List<T> findAllByUpdatedBy(String updatedBy);

    /**
     * Find all by deleted by
     */
    @Query("SELECT e FROM #{#entityName} e WHERE e.deletedBy = ?1 AND e.deletedAt IS NOT NULL")
    List<T> findAllByDeletedBy(String deletedBy);

    /**
     * Permanently delete old soft-deleted entities
     */
    @Modifying
    @Transactional
    @Query("DELETE FROM #{#entityName} e WHERE e.deletedAt < ?1")
    void permanentlyDeleteOldEntities(LocalDateTime before);

    /**
     * Update active status
     */
    @Modifying
    @Transactional
    @Query("UPDATE #{#entityName} e SET e.active = ?2, e.updatedAt = ?3, e.updatedBy = ?4 WHERE e.id = ?1")
    void updateActiveStatus(Long id, boolean active, LocalDateTime updatedAt, String updatedBy);

    /**
     * Find all with version greater than
     */
    @Query("SELECT e FROM #{#entityName} e WHERE e.version > ?1 AND e.active = true AND e.deletedAt IS NULL")
    List<T> findAllWithVersionGreaterThan(Long version);

    /**
     * Find latest version
     */
    @Query("SELECT MAX(e.version) FROM #{#entityName} e WHERE e.active = true AND e.deletedAt IS NULL")
    Optional<Long> findLatestVersion();
}
