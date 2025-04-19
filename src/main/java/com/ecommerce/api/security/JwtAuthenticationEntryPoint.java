package com.ecommerce.api.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

@Component
public class JwtAuthenticationEntryPoint implements AuthenticationEntryPoint {

    private static final Logger logger = LoggerFactory.getLogger(JwtAuthenticationEntryPoint.class);

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public void commence(
            HttpServletRequest request,
            HttpServletResponse response,
            AuthenticationException authException) throws IOException, ServletException {

        // Check if it's an API request
        if (isApiRequest(request)) {
            handleApiUnauthorized(request, response, authException);
        } else {
            handleWebUnauthorized(request, response, authException);
        }
    }

    /**
     * Handle unauthorized API requests
     */
    private void handleApiUnauthorized(
            HttpServletRequest request,
            HttpServletResponse response,
            AuthenticationException authException) throws IOException {

        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);

        Map<String, Object> body = new HashMap<>();
        body.put("status", HttpServletResponse.SC_UNAUTHORIZED);
        body.put("error", "Unauthorized");
        body.put("message", authException.getMessage());
        body.put("path", request.getServletPath());
        body.put("timestamp", System.currentTimeMillis());

        objectMapper.writeValue(response.getOutputStream(), body);
    }

    /**
     * Handle unauthorized web requests
     */
    private void handleWebUnauthorized(
            HttpServletRequest request,
            HttpServletResponse response,
            AuthenticationException authException) throws IOException {

        // Store the original request URL in session
        String targetUrl = request.getRequestURI();
        if (request.getQueryString() != null) {
            targetUrl += "?" + request.getQueryString();
        }
        request.getSession().setAttribute("SPRING_SECURITY_SAVED_REQUEST", targetUrl);

        // Redirect to login page with error message
        String loginUrl = "/admin/login";
        String errorParam = "?error=true";
        String redirectUrl = loginUrl + errorParam;

        // Add custom error message if available
        String errorMessage = authException.getMessage();
        if (errorMessage != null && !errorMessage.isEmpty()) {
            redirectUrl += "&message=" + errorMessage;
        }

        response.sendRedirect(redirectUrl);
    }

    /**
     * Check if the request is an API request
     */
    private boolean isApiRequest(HttpServletRequest request) {
        String path = request.getServletPath();
        return path.startsWith("/api/");
    }

    /**
     * Get error details
     */
    private Map<String, Object> getErrorDetails(
            HttpServletRequest request,
            AuthenticationException authException) {

        Map<String, Object> details = new HashMap<>();
        details.put("timestamp", System.currentTimeMillis());
        details.put("status", HttpServletResponse.SC_UNAUTHORIZED);
        details.put("error", "Unauthorized");
        details.put("message", authException.getMessage());
        details.put("path", request.getServletPath());

        // Add request details
        details.put("method", request.getMethod());
        details.put("remoteAddr", request.getRemoteAddr());
        details.put("userAgent", request.getHeader("User-Agent"));

        // Add session information if available
        if (request.getSession(false) != null) {
            details.put("sessionId", request.getSession().getId());
        }

        return details;
    }

    /**
     * Log authentication failure
     */
    private void logAuthenticationFailure(
            HttpServletRequest request,
            AuthenticationException authException) {

        String path = request.getServletPath();
        String remoteAddr = request.getRemoteAddr();
        String userAgent = request.getHeader("User-Agent");
        String errorMessage = authException.getMessage();

        // Log the authentication failure using SLF4J
        logger.warn("Authentication failure - Path: {}, IP: {}, User-Agent: {}, Error: {}",
                path, remoteAddr, userAgent, errorMessage);
    }

    /**
     * Check if request accepts JSON response
     */
    private boolean isJsonResponse(HttpServletRequest request) {
        String accept = request.getHeader("Accept");
        return accept != null && accept.contains(MediaType.APPLICATION_JSON_VALUE);
    }

    /**
     * Check if it's an AJAX request
     */
    private boolean isAjaxRequest(HttpServletRequest request) {
        String requestedWith = request.getHeader("X-Requested-With");
        return "XMLHttpRequest".equals(requestedWith);
    }
}
