package com.ecommerce.api.security;

import com.ecommerce.api.model.User;
import com.ecommerce.api.model.UserSession;
import com.ecommerce.api.service.UserDetailsServiceImpl;
import com.ecommerce.api.service.UserSessionService;
import jakarta.servlet.http.Cookie;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.security.web.savedrequest.HttpSessionRequestCache;
import org.springframework.security.web.savedrequest.RequestCache;
import org.springframework.security.web.savedrequest.SavedRequest;
import org.springframework.stereotype.Component;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;

@Component
public class CustomAuthenticationSuccessHandler extends SimpleUrlAuthenticationSuccessHandler {

    private static final Logger logger = LoggerFactory.getLogger(CustomAuthenticationSuccessHandler.class);

    private RequestCache requestCache = new HttpSessionRequestCache();

    @Autowired
    private UserSessionService userSessionService;

    @Autowired
    private UserDetailsServiceImpl userDetailsService;

    @Override
    public void onAuthenticationSuccess(
            HttpServletRequest request,
            HttpServletResponse response,
            Authentication authentication) throws ServletException, IOException {

        // Create and store new session
        try {
            User user = userDetailsService.getCurrentUser();
            UserSession session = userSessionService.createSession(user, request);
            userSessionService.markSessionAsCurrent(session);

            // Generate JWT token
            String jwtToken = userDetailsService.getJwtTokenProvider().generateToken(authentication);

            // Set JWT token in HttpOnly cookie
            Cookie jwtCookie = new Cookie("JWT_TOKEN", jwtToken);
            jwtCookie.setHttpOnly(true);
            jwtCookie.setPath("/");
            jwtCookie.setMaxAge(60 * 60 * 24); // 1 day expiration
            response.addCookie(jwtCookie);

        } catch (Exception e) {
            logger.error("Failed to create user session or set JWT token", e);
        }

        // Handle redirect
        SavedRequest savedRequest = requestCache.getRequest(request, response);

        if (savedRequest == null) {
            // If no saved request exists, redirect based on user role
            if (authentication.getAuthorities().stream()
                    .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"))) {
                getRedirectStrategy().sendRedirect(request, response, "/admin");
            } else {
                getRedirectStrategy().sendRedirect(request, response, "/");
            }
            return;
        }

        // Clear saved request
        requestCache.removeRequest(request, response);

        // Get the target URL
        String targetUrl = savedRequest.getRedirectUrl();

        // Validate the target URL
        if (isValidRedirectUrl(targetUrl)) {
            getRedirectStrategy().sendRedirect(request, response, targetUrl);
        } else {
            // If invalid, redirect to default page
            getRedirectStrategy().sendRedirect(request, response, getDefaultTargetUrl());
        }
    }

    public void setRequestCache(RequestCache requestCache) {
        this.requestCache = requestCache;
    }

    /**
     * Validate the redirect URL to prevent open redirect vulnerability
     */
    private boolean isValidRedirectUrl(String url) {
        if (url == null || url.isEmpty()) {
            return false;
        }

        // Check if it's a relative URL
        if (url.startsWith("/")) {
            // Validate allowed paths
            return isAllowedPath(url);
        }

        // Don't allow external redirects
        return false;
    }

    /**
     * Check if the path is allowed for redirect
     */
    private boolean isAllowedPath(String path) {
        // List of allowed paths
        String[] allowedPaths = {
                "/admin",
                "/admin/dashboard",
                "/admin/products",
                "/admin/categories",
                "/admin/banners",
                "/admin/users",
                "/admin/profile",
                "/admin/sessions",
                "/"
        };

        // Check if path starts with any allowed path
        for (String allowedPath : allowedPaths) {
            if (path.startsWith(allowedPath)) {
                return true;
            }
        }

        return false;
    }

    /**
     * Log login activity
     */
    private void logLoginActivity(HttpServletRequest request, User user) {
        String ipAddress = request.getHeader("X-Forwarded-For");
        if (ipAddress == null || ipAddress.isEmpty()) {
            ipAddress = request.getRemoteAddr();
        }

        String userAgent = request.getHeader("User-Agent");

        // TODO: Implement login activity logging
        logger.info("User {} logged in from IP: {}, User-Agent: {}",
                user.getUsername(), ipAddress, userAgent);
    }

    /**
     * Check for suspicious login activity
     */
    private boolean isSuspiciousLogin(HttpServletRequest request, User user) {
        String ipAddress = request.getHeader("X-Forwarded-For");
        if (ipAddress == null || ipAddress.isEmpty()) {
            ipAddress = request.getRemoteAddr();
        }

        // Get recent sessions for this user
        int recentSessionCount = userSessionService.getActiveSessions(user).size();

        // If there are too many active sessions, consider it suspicious
        if (recentSessionCount > 5) {
            logger.warn("Suspicious login detected for user {} from IP: {}",
                    user.getUsername(), ipAddress);
            return true;
        }

        return false;
    }
}