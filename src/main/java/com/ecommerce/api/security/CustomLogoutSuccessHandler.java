package com.ecommerce.api.security;

import com.ecommerce.api.model.User;
import com.ecommerce.api.service.UserDetailsServiceImpl;
import com.ecommerce.api.service.UserSessionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.logout.SimpleUrlLogoutSuccessHandler;
import org.springframework.stereotype.Component;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.io.IOException;
import java.time.LocalDateTime;

@Component
public class CustomLogoutSuccessHandler extends SimpleUrlLogoutSuccessHandler {

    @Autowired
    private UserSessionService userSessionService;

    @Autowired
    private UserDetailsServiceImpl userDetailsService;

    public CustomLogoutSuccessHandler() {
        // Set default target URL after logout
        setDefaultTargetUrl("/admin/login?logout=true");
    }

    @Override
    public void onLogoutSuccess(
            HttpServletRequest request,
            HttpServletResponse response,
            Authentication authentication) throws IOException, ServletException {

        if (authentication != null && authentication.getPrincipal() != null) {
            try {
                // Get the current user
                User user = userDetailsService.getCurrentUser();
                
                // Get the current session ID
                String sessionId = request.getSession().getId();
                
                // Deactivate the current session
                userSessionService.deactivateSession(sessionId);
                
                // Log the logout activity
                logLogoutActivity(request, user);
                
                // Perform any additional cleanup
                performLogoutCleanup(request, user);
                
            } catch (Exception e) {
                logger.error("Error during logout process", e);
            }
        }

        // Invalidate session
        HttpSession session = request.getSession(false);
        if (session != null) {
            session.invalidate();
        }

        // Clear all cookies
        clearAuthenticationCookies(request, response);

        // Redirect to login page with logout parameter
        String targetUrl = determineTargetUrl(request, response);
        getRedirectStrategy().sendRedirect(request, response, targetUrl);
    }

    /**
     * Log the logout activity
     */
    private void logLogoutActivity(HttpServletRequest request, User user) {
        String ipAddress = getClientIP(request);
        String userAgent = request.getHeader("User-Agent");
        
        // Log the logout event
        logger.info("User {} logged out - IP: {}, User-Agent: {}, Time: {}", 
                   user.getUsername(),
                   ipAddress,
                   userAgent,
                   LocalDateTime.now());
    }

    /**
     * Perform additional cleanup tasks during logout
     */
    private void performLogoutCleanup(HttpServletRequest request, User user) {
        // Update user's last logout time
        user.setLastLogoutAt(LocalDateTime.now());
        userDetailsService.updateUser(user);

        // Clear any remember-me tokens
        request.getCookies();
        if (request.getCookies() != null) {
            for (jakarta.servlet.http.Cookie cookie : request.getCookies()) {
                if (cookie.getName().equals("remember-me")) {
                    cookie.setMaxAge(0);
                    break;
                }
            }
        }
    }

    /**
     * Clear authentication related cookies
     */
    private void clearAuthenticationCookies(HttpServletRequest request, HttpServletResponse response) {
        jakarta.servlet.http.Cookie[] cookies = request.getCookies();
        if (cookies != null) {
            for (jakarta.servlet.http.Cookie cookie : cookies) {
                if (isAuthenticationCookie(cookie.getName())) {
                    cookie.setValue("");
                    cookie.setPath("/");
                    cookie.setMaxAge(0);
                    response.addCookie(cookie);
                }
            }
        }
    }

    /**
     * Check if the cookie is authentication related
     */
    private boolean isAuthenticationCookie(String cookieName) {
        return cookieName.toLowerCase().contains("jwt") ||
               cookieName.toLowerCase().contains("token") ||
               cookieName.toLowerCase().contains("auth") ||
               cookieName.toLowerCase().contains("remember-me") ||
               cookieName.toLowerCase().contains("session");
    }

    /**
     * Get client IP address
     */
    private String getClientIP(HttpServletRequest request) {
        String xfHeader = request.getHeader("X-Forwarded-For");
        if (xfHeader == null || xfHeader.isEmpty()) {
            return request.getRemoteAddr();
        }
        return xfHeader.split(",")[0].trim();
    }

    /**
     * Determine the target URL after logout
     */
    @Override
    protected String determineTargetUrl(HttpServletRequest request, HttpServletResponse response) {
        String targetUrl = super.determineTargetUrl(request, response);
        
        // Add timestamp to prevent caching
        return targetUrl + (targetUrl.contains("?") ? "&" : "?") + "t=" + System.currentTimeMillis();
    }

    /**
     * Handle any errors during logout
     */
    private void handleLogoutError(HttpServletRequest request, Exception e) {
        logger.error("Error during logout", e);
        
        // Store error message in session
        HttpSession session = request.getSession(true);
        session.setAttribute("logoutError", "An error occurred during logout. Please try again.");
    }
}
