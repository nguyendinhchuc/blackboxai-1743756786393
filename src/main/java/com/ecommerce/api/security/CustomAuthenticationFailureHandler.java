package com.ecommerce.api.security;

import com.ecommerce.api.service.UserDetailsServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationFailureHandler;
import org.springframework.stereotype.Component;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class CustomAuthenticationFailureHandler extends SimpleUrlAuthenticationFailureHandler {

    private static final int MAX_ATTEMPTS = 5;
    private static final long LOCK_TIME_DURATION = 15 * 60 * 1000; // 15 minutes in milliseconds

    // Store failed attempts: key = username or IP, value = Map of attempt count and lockout timestamp
    private final Map<String, Map<String, Object>> failedAttempts = new ConcurrentHashMap<>();

    @Autowired
    private UserDetailsServiceImpl userDetailsService;

    public CustomAuthenticationFailureHandler() {
        setDefaultFailureUrl("/admin/login?error=true");
    }

    @Override
    public void onAuthenticationFailure(
            HttpServletRequest request,
            HttpServletResponse response,
            AuthenticationException exception) throws IOException, ServletException {

        String username = request.getParameter("username");
        String ipAddress = getClientIP(request);
        
        // Handle the failure based on the type of authentication exception
        String errorMessage = determineErrorMessage(exception);
        
        // Check if account is locked
        if (isAccountLocked(username, ipAddress)) {
            long lockTimeRemaining = getRemainingLockTime(username, ipAddress);
            if (lockTimeRemaining > 0) {
                errorMessage = String.format(
                    "Account is locked due to multiple failed attempts. Please try again in %d minutes.",
                    lockTimeRemaining / 60000
                );
                super.setDefaultFailureUrl("/admin/login?error=locked&lockTime=" + lockTimeRemaining);
            } else {
                // Reset failed attempts if lock time has expired
                resetFailedAttempts(username, ipAddress);
            }
        } else {
            // Increment failed attempts
            incrementFailedAttempts(username, ipAddress);
            
            // Check if account should be locked
            if (getFailedAttempts(username, ipAddress) >= MAX_ATTEMPTS) {
                lockAccount(username, ipAddress);
                errorMessage = "Account has been locked due to multiple failed attempts. Please try again later.";
                super.setDefaultFailureUrl("/admin/login?error=locked");
            }
        }

        // Log the failed attempt
        logFailedAttempt(request, username, ipAddress, errorMessage);

        // Add error message to session
        request.getSession().setAttribute("SPRING_SECURITY_LAST_EXCEPTION", exception);
        request.getSession().setAttribute("errorMessage", errorMessage);

        super.onAuthenticationFailure(request, response, exception);
    }

    private String getClientIP(HttpServletRequest request) {
        String xfHeader = request.getHeader("X-Forwarded-For");
        if (xfHeader == null || xfHeader.isEmpty()) {
            return request.getRemoteAddr();
        }
        return xfHeader.split(",")[0].trim();
    }

    private String determineErrorMessage(AuthenticationException exception) {
        String message = exception.getMessage();
        if (message == null) {
            return "Invalid username or password";
        }
        
        // Customize error message based on exception type
        if (message.contains("Bad credentials")) {
            return "Invalid username or password";
        } else if (message.contains("locked")) {
            return "Account is locked";
        } else if (message.contains("disabled")) {
            return "Account is disabled";
        } else if (message.contains("expired")) {
            return "Account has expired";
        }
        
        return message;
    }

    private synchronized void incrementFailedAttempts(String username, String ipAddress) {
        String key = username != null ? username : ipAddress;
        Map<String, Object> attempts = failedAttempts.computeIfAbsent(key, k -> new HashMap<>());
        int currentAttempts = (int) attempts.getOrDefault("count", 0);
        attempts.put("count", currentAttempts + 1);
        attempts.put("lastFailure", System.currentTimeMillis());
    }

    private synchronized int getFailedAttempts(String username, String ipAddress) {
        String key = username != null ? username : ipAddress;
        Map<String, Object> attempts = failedAttempts.get(key);
        return attempts != null ? (int) attempts.getOrDefault("count", 0) : 0;
    }

    private synchronized void lockAccount(String username, String ipAddress) {
        String key = username != null ? username : ipAddress;
        Map<String, Object> attempts = failedAttempts.computeIfAbsent(key, k -> new HashMap<>());
        attempts.put("lockTime", System.currentTimeMillis());
    }

    private synchronized boolean isAccountLocked(String username, String ipAddress) {
        String key = username != null ? username : ipAddress;
        Map<String, Object> attempts = failedAttempts.get(key);
        if (attempts != null && attempts.containsKey("lockTime")) {
            long lockTime = (long) attempts.get("lockTime");
            return (System.currentTimeMillis() - lockTime) < LOCK_TIME_DURATION;
        }
        return false;
    }

    private synchronized long getRemainingLockTime(String username, String ipAddress) {
        String key = username != null ? username : ipAddress;
        Map<String, Object> attempts = failedAttempts.get(key);
        if (attempts != null && attempts.containsKey("lockTime")) {
            long lockTime = (long) attempts.get("lockTime");
            long remainingTime = LOCK_TIME_DURATION - (System.currentTimeMillis() - lockTime);
            return Math.max(0, remainingTime);
        }
        return 0;
    }

    private synchronized void resetFailedAttempts(String username, String ipAddress) {
        String key = username != null ? username : ipAddress;
        failedAttempts.remove(key);
    }

    private void logFailedAttempt(HttpServletRequest request, String username, String ipAddress, String errorMessage) {
        String userAgent = request.getHeader("User-Agent");
        
        // Log the failed attempt (you might want to save this to a database in production)
        logger.warn("Failed login attempt - Username: {}, IP: {}, User-Agent: {}, Error: {}", 
                   username != null ? username : "unknown",
                   ipAddress,
                   userAgent,
                   errorMessage);
    }

    /**
     * Check if there are suspicious patterns in failed attempts
     */
    private boolean isSuspiciousActivity(String username, String ipAddress) {
        // Check for rapid successive attempts
        Map<String, Object> attempts = failedAttempts.get(username != null ? username : ipAddress);
        if (attempts != null && attempts.containsKey("lastFailure")) {
            long lastFailure = (long) attempts.get("lastFailure");
            long timeBetweenAttempts = System.currentTimeMillis() - lastFailure;
            
            // If attempts are too rapid (less than 1 second apart)
            if (timeBetweenAttempts < 1000) {
                return true;
            }
        }
        
        return false;
    }

    /**
     * Clean up old entries in the failedAttempts map
     */
    public void cleanupFailedAttempts() {
        long now = System.currentTimeMillis();
        failedAttempts.entrySet().removeIf(entry -> {
            Map<String, Object> attempts = entry.getValue();
            if (attempts.containsKey("lockTime")) {
                long lockTime = (long) attempts.get("lockTime");
                return (now - lockTime) > LOCK_TIME_DURATION;
            }
            if (attempts.containsKey("lastFailure")) {
                long lastFailure = (long) attempts.get("lastFailure");
                return (now - lastFailure) > LOCK_TIME_DURATION;
            }
            return true;
        });
    }
}
