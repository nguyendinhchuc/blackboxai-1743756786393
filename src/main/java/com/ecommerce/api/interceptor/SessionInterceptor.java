package com.ecommerce.api.interceptor;

import com.ecommerce.api.model.User;
import com.ecommerce.api.service.UserDetailsServiceImpl;
import com.ecommerce.api.service.UserSessionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.util.Arrays;
import java.util.List;

@Component
public class SessionInterceptor implements HandlerInterceptor {

    @Autowired
    private UserSessionService userSessionService;

    @Autowired
    private UserDetailsServiceImpl userDetailsService;

    // List of paths that should be excluded from session tracking
    private static final List<String> EXCLUDED_PATHS = Arrays.asList(
        "/static",
        "/css",
        "/js",
        "/images",
        "/favicon.ico",
        "/error",
        "/login",
        "/logout"
    );

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        // Skip session tracking for excluded paths
        if (shouldExclude(request.getRequestURI())) {
            return true;
        }

        // Get current authentication
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        
        if (authentication != null && authentication.isAuthenticated() && 
            !"anonymousUser".equals(authentication.getPrincipal())) {
            
            User currentUser = userDetailsService.getCurrentUser();
            String sessionId = request.getSession().getId();

            // Update session activity
            userSessionService.updateSessionActivity(sessionId);

            // Check for session validity
            if (!isValidSession(request, currentUser)) {
                // If session is invalid, redirect to login page
                response.sendRedirect("/admin/login?expired=true");
                return false;
            }
        }

        return true;
    }

    @Override
    public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler, ModelAndView modelAndView) throws Exception {
        // Add common session-related attributes to the model
        if (modelAndView != null) {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            
            if (authentication != null && authentication.isAuthenticated() && 
                !"anonymousUser".equals(authentication.getPrincipal())) {
                
                User currentUser = userDetailsService.getCurrentUser();
                modelAndView.addObject("currentUser", currentUser);
                
                // Add session information if it's an admin page
                if (request.getRequestURI().startsWith("/admin")) {
                    modelAndView.addObject("activeSessions", 
                        userSessionService.getActiveSessions(currentUser));
                }
            }
        }
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) throws Exception {
        // Clean up any resources if needed
    }

    private boolean shouldExclude(String path) {
        return EXCLUDED_PATHS.stream().anyMatch(path::startsWith);
    }

    private boolean isValidSession(HttpServletRequest request, User user) {
        String sessionId = request.getSession().getId();
        
        // Check if session exists and is active
        return userSessionService.getActiveSessions(user).stream()
            .anyMatch(session -> session.getSessionId().equals(sessionId) && !session.isExpired());
    }

    /**
     * Helper method to get client IP address
     */
    private String getClientIp(HttpServletRequest request) {
        String xForwardedFor = request.getHeader("X-Forwarded-For");
        if (xForwardedFor != null && !xForwardedFor.isEmpty()) {
            // Get first IP in case of multiple proxies
            return xForwardedFor.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }

    /**
     * Helper method to check for suspicious activity
     */
    private boolean isSuspiciousActivity(HttpServletRequest request, User user) {
        String currentIp = getClientIp(request);
        String userAgent = request.getHeader("User-Agent");
        
        // Get active sessions for this user
        List<com.ecommerce.api.model.UserSession> activeSessions = userSessionService.getActiveSessions(user);
        
        // Check for multiple sessions from different locations/devices
        long suspiciousSessions = activeSessions.stream()
            .filter(session -> !session.getIpAddress().equals(currentIp) && 
                             !session.getUserAgent().equals(userAgent))
            .count();
        
        // If there are more than 2 sessions from different locations/devices, consider it suspicious
        return suspiciousSessions > 2;
    }

    /**
     * Helper method to check for concurrent session limit
     */
    private boolean hasExceededSessionLimit(User user) {
        // Get count of active sessions
        long activeSessionCount = userSessionService.getActiveSessions(user).size();
        
        // Maximum allowed concurrent sessions (can be made configurable)
        int maxSessions = 5;
        
        return activeSessionCount > maxSessions;
    }

    /**
     * Helper method to validate session timeout
     */
    private boolean isSessionTimedOut(HttpServletRequest request) {
        long lastAccessedTime = request.getSession().getLastAccessedTime();
        long currentTime = System.currentTimeMillis();
        long sessionTimeout = request.getSession().getMaxInactiveInterval() * 1000L;
        
        return (currentTime - lastAccessedTime) > sessionTimeout;
    }
}
