package com.ecommerce.api.repository;

import com.ecommerce.api.model.User;
import com.ecommerce.api.model.UserSession;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface UserSessionRepository extends JpaRepository<UserSession, Long> {

    /**
     * Find a session by its session ID
     */
    Optional<UserSession> findBySessionId(String sessionId);

    /**
     * Find all active sessions for a user
     */
    List<UserSession> findByUserAndActiveTrue(User user);

    /**
     * Find all sessions for a user except a specific session ID
     */
    List<UserSession> findByUserAndSessionIdNot(User user, String sessionId);

    /**
     * Find the current session for a user
     */
    List<UserSession> findByUserAndCurrentSessionTrue(User user);

    /**
     * Find active sessions that haven't been updated since a specific time
     */
    List<UserSession> findByActiveAndLastActiveTimeBefore(boolean active, LocalDateTime time);

    /**
     * Count active sessions for a user
     */
    long countByUserAndActiveTrue(User user);

    /**
     * Find all active sessions for a user ordered by last activity
     */
    @Query("SELECT s FROM UserSession s WHERE s.user = :user AND s.active = true ORDER BY s.lastActiveTime DESC")
    List<UserSession> findActiveSessionsOrderedByLastActivity(@Param("user") User user);

    /**
     * Find all sessions for a user within a date range
     */
    @Query("SELECT s FROM UserSession s WHERE s.user = :user AND s.createdAt BETWEEN :startDate AND :endDate")
    List<UserSession> findSessionsInDateRange(
        @Param("user") User user,
        @Param("startDate") LocalDateTime startDate,
        @Param("endDate") LocalDateTime endDate
    );

    /**
     * Find sessions by IP address
     */
    List<UserSession> findByIpAddress(String ipAddress);

    /**
     * Find sessions by device type
     */
    List<UserSession> findByUserAndDeviceType(User user, String deviceType);

    /**
     * Find sessions by browser
     */
    List<UserSession> findByUserAndBrowser(User user, String browser);

    /**
     * Find sessions by platform
     */
    List<UserSession> findByUserAndPlatform(User user, String platform);

    /**
     * Delete expired sessions
     */
    @Query("DELETE FROM UserSession s WHERE s.active = false AND s.lastActiveTime < :expiryTime")
    void deleteExpiredSessions(@Param("expiryTime") LocalDateTime expiryTime);

    /**
     * Find suspicious sessions (multiple sessions from same IP with different users)
     */
    @Query("SELECT s FROM UserSession s WHERE s.ipAddress IN " +
           "(SELECT s2.ipAddress FROM UserSession s2 GROUP BY s2.ipAddress HAVING COUNT(DISTINCT s2.user) > 1)")
    List<UserSession> findSuspiciousSessions();

    /**
     * Find concurrent active sessions for a user
     */
    @Query("SELECT COUNT(s) FROM UserSession s WHERE s.user = :user AND s.active = true")
    long countConcurrentSessions(@Param("user") User user);

    /**
     * Deactivate all sessions for a user except the current one
     */
    @Query("UPDATE UserSession s SET s.active = false WHERE s.user = :user AND s.sessionId != :currentSessionId")
    void deactivateOtherSessions(@Param("user") User user, @Param("currentSessionId") String currentSessionId);

    /**
     * Find the most recent session for a user
     */
    Optional<UserSession> findFirstByUserOrderByCreatedAtDesc(User user);
}
