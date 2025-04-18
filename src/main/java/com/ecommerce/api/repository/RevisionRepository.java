package com.ecommerce.api.repository;

import com.ecommerce.api.model.Revision;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Date;
import java.util.List;

@Repository
public interface RevisionRepository extends JpaRepository<Revision, Long>, JpaSpecificationExecutor<Revision> {

    /**
     * Find revisions by entity name and id
     */
    @Query("SELECT r FROM Revision r WHERE r.entityName = :entityName AND r.entityId = :entityId ORDER BY r.timestamp DESC")
    List<Revision> findByEntityNameAndEntityId(
        @Param("entityName") String entityName,
        @Param("entityId") Long entityId
    );

    /**
     * Find revisions by entity name and id with pagination
     */
    @Query("SELECT r FROM Revision r WHERE r.entityName = :entityName AND r.entityId = :entityId ORDER BY r.timestamp DESC")
    Page<Revision> findByEntityNameAndEntityId(
        @Param("entityName") String entityName,
        @Param("entityId") Long entityId,
        Pageable pageable
    );

    /**
     * Find revisions by username
     */
    @Query("SELECT r FROM Revision r WHERE r.username = :username ORDER BY r.timestamp DESC")
    List<Revision> findByUsername(@Param("username") String username);

    /**
     * Find revisions by username with pagination
     */
    @Query("SELECT r FROM Revision r WHERE r.username = :username ORDER BY r.timestamp DESC")
    Page<Revision> findByUsername(@Param("username") String username, Pageable pageable);

    /**
     * Find revisions by date range
     */
    @Query("SELECT r FROM Revision r WHERE r.timestamp BETWEEN :startDate AND :endDate ORDER BY r.timestamp DESC")
    List<Revision> findByDateRange(
        @Param("startDate") long startDate,
        @Param("endDate") long endDate
    );

    /**
     * Find revisions by date range with pagination
     */
    @Query("SELECT r FROM Revision r WHERE r.timestamp BETWEEN :startDate AND :endDate ORDER BY r.timestamp DESC")
    Page<Revision> findByDateRange(
        @Param("startDate") long startDate,
        @Param("endDate") long endDate,
        Pageable pageable
    );

    /**
     * Find revisions by revision type
     */
    @Query("SELECT r FROM Revision r WHERE r.revisionType = :revisionType ORDER BY r.timestamp DESC")
    List<Revision> findByRevisionType(@Param("revisionType") Revision.RevisionType revisionType);

    /**
     * Find revisions by revision type with pagination
     */
    @Query("SELECT r FROM Revision r WHERE r.revisionType = :revisionType ORDER BY r.timestamp DESC")
    Page<Revision> findByRevisionType(
        @Param("revisionType") Revision.RevisionType revisionType,
        Pageable pageable
    );

    /**
     * Find latest revision by entity name and id
     */
    @Query("SELECT r FROM Revision r WHERE r.entityName = :entityName AND r.entityId = :entityId " +
           "ORDER BY r.timestamp DESC LIMIT 1")
    Revision findLatestRevision(
        @Param("entityName") String entityName,
        @Param("entityId") Long entityId
    );

    /**
     * Count revisions by entity name and id
     */
    @Query("SELECT COUNT(r) FROM Revision r WHERE r.entityName = :entityName AND r.entityId = :entityId")
    long countByEntityNameAndEntityId(
        @Param("entityName") String entityName,
        @Param("entityId") Long entityId
    );

    /**
     * Count revisions by username
     */
    @Query("SELECT COUNT(r) FROM Revision r WHERE r.username = :username")
    long countByUsername(@Param("username") String username);

    /**
     * Count revisions by revision type
     */
    @Query("SELECT COUNT(r) FROM Revision r WHERE r.revisionType = :revisionType")
    long countByRevisionType(@Param("revisionType") Revision.RevisionType revisionType);

    /**
     * Delete old revisions
     */
    @Query("DELETE FROM Revision r WHERE r.timestamp < :timestamp")
    void deleteOldRevisions(@Param("timestamp") long timestamp);

    /**
     * Find revisions by IP address
     */
    @Query("SELECT r FROM Revision r WHERE r.ipAddress = :ipAddress ORDER BY r.timestamp DESC")
    List<Revision> findByIpAddress(@Param("ipAddress") String ipAddress);

    /**
     * Find revisions by user agent
     */
    @Query("SELECT r FROM Revision r WHERE r.userAgent LIKE %:userAgent% ORDER BY r.timestamp DESC")
    List<Revision> findByUserAgent(@Param("userAgent") String userAgent);

    /**
     * Find revisions containing specific changes
     */
    @Query("SELECT r FROM Revision r WHERE r.changes LIKE %:changePattern% ORDER BY r.timestamp DESC")
    List<Revision> findByChanges(@Param("changePattern") String changePattern);

    /**
     * Find revisions by multiple criteria
     */
    @Query("SELECT r FROM Revision r WHERE " +
           "(:entityName IS NULL OR r.entityName = :entityName) AND " +
           "(:entityId IS NULL OR r.entityId = :entityId) AND " +
           "(:username IS NULL OR r.username = :username) AND " +
           "(:revisionType IS NULL OR r.revisionType = :revisionType) AND " +
           "(:startDate IS NULL OR r.timestamp >= :startDate) AND " +
           "(:endDate IS NULL OR r.timestamp <= :endDate) " +
           "ORDER BY r.timestamp DESC")
    Page<Revision> findByMultipleCriteria(
        @Param("entityName") String entityName,
        @Param("entityId") Long entityId,
        @Param("username") String username,
        @Param("revisionType") Revision.RevisionType revisionType,
        @Param("startDate") Long startDate,
        @Param("endDate") Long endDate,
        Pageable pageable
    );
}
