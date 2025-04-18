package com.ecommerce.api.service;

import com.ecommerce.api.model.User;
import com.ecommerce.api.model.UserSession;
import com.ecommerce.api.repository.UserSessionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.security.core.session.SessionInformation;
import org.springframework.security.core.session.SessionRegistry;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class UserSessionService {

    @Autowired
    private UserSessionRepository userSessionRepository;

    @Autowired
    private SessionRegistry sessionRegistry;

    @Transactional
    public UserSession createSession(User user, HttpServletRequest request) {
        String sessionId = request.getSession().getId();
        String ipAddress = getClientIp(request);
        String userAgent = request.getHeader("User-Agent");

        // Create new session
        UserSession session = UserSession.createFromRequest(user, sessionId, ipAddress, userAgent);
        
        // Set location if available (you might want to use a GeoIP service in production)
        session.setLocation(getLocationFromIp(ipAddress));
        
        // Save the session
        return userSessionRepository.save(session);
    }

    @Transactional
    public void markSessionAsCurrent(UserSession session) {
        // Unmark any existing current sessions for this user
        userSessionRepository.findByUserAndCurrentSessionTrue(session.getUser())
            .forEach(s -> {
                s.setCurrentSession(false);
                userSessionRepository.save(s);
            });
        
        // Mark this session as current
        session.setCurrentSession(true);
        userSessionRepository.save(session);
    }

    @Transactional
    public void updateSessionActivity(String sessionId) {
        Optional<UserSession> sessionOpt = userSessionRepository.findBySessionId(sessionId);
        sessionOpt.ifPresent(session -> {
            session.updateLastActiveTime();
            userSessionRepository.save(session);
        });
    }

    @Transactional
    public void deactivateSession(String sessionId) {
        Optional<UserSession> sessionOpt = userSessionRepository.findBySessionId(sessionId);
        sessionOpt.ifPresent(session -> {
            session.deactivate();
            userSessionRepository.save(session);
        });
    }

    @Transactional
    public void deactivateAllUserSessions(User user, String exceptSessionId) {
        List<UserSession> sessions = userSessionRepository.findByUserAndSessionIdNot(user, exceptSessionId);
        sessions.forEach(session -> {
            session.deactivate();
            userSessionRepository.save(session);
        });
    }

    public List<UserSession> getActiveSessions(User user) {
        return userSessionRepository.findByUserAndActiveTrue(user);
    }

    public Optional<UserSession> getCurrentSession(User user) {
        return userSessionRepository.findByUserAndCurrentSessionTrue(user).stream().findFirst();
    }

    @Scheduled(fixedRate = 300000) // Run every 5 minutes
    @Transactional
    public void cleanupExpiredSessions() {
        LocalDateTime expiryTime = LocalDateTime.now().minusMinutes(30);
        List<UserSession> expiredSessions = userSessionRepository.findByActiveAndLastActiveTimeBefore(true, expiryTime);
        
        expiredSessions.forEach(session -> {
            session.deactivate();
            userSessionRepository.save(session);
        });
    }

    private String getClientIp(HttpServletRequest request) {
        String xForwardedFor = request.getHeader("X-Forwarded-For");
        if (xForwardedFor != null && !xForwardedFor.isEmpty()) {
            return xForwardedFor.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }

    private String getLocationFromIp(String ipAddress) {
        // TODO: Implement GeoIP lookup
        // For now, return null or implement a simple mock
        return null;
    }

    @Transactional
    public void invalidateSession(String sessionId) {
        Optional<UserSession> sessionOpt = userSessionRepository.findBySessionId(sessionId);
        if (sessionOpt.isPresent()) {
            UserSession session = sessionOpt.get();
            session.deactivate();
            userSessionRepository.save(session);

            // Also invalidate Spring Security session
            sessionRegistry.getAllSessions(session.getUser(), false)
                .stream()
                .filter(info -> info.getSessionId().equals(sessionId))
                .forEach(SessionInformation::expireNow);
        }
    }

    @Transactional
    public void invalidateAllUserSessions(User user, String exceptSessionId) {
        // Deactivate all sessions in our database
        deactivateAllUserSessions(user, exceptSessionId);

        // Invalidate Spring Security sessions
        sessionRegistry.getAllSessions(user, false)
            .stream()
            .filter(info -> !info.getSessionId().equals(exceptSessionId))
            .forEach(SessionInformation::expireNow);
    }
}
