package com.ecommerce.api.security;

import com.ecommerce.api.service.UserDetailsServiceImpl;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.MalformedJwtException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private static final Logger logger = LoggerFactory.getLogger(JwtAuthenticationFilter.class);

    @Autowired
    private UserDetailsServiceImpl userDetailsService;

    @Autowired
    private JwtTokenProvider jwtTokenProvider;

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain) throws ServletException, IOException {

        try {
            // Get JWT token from request
            String token = getJwtFromRequest(request);

            // If path starts with /api, validate JWT token
            if (request.getServletPath().startsWith("/api") && token != null && !token.isEmpty()) {
                try {
                    if (jwtTokenProvider.validateToken(token)) {
                        // Get username from token
                        String username = jwtTokenProvider.getUsernameFromToken(token);

                        // Load user details
                        UserDetails userDetails = userDetailsService.loadUserByUsername(username);

                        // Create authentication token
                        UsernamePasswordAuthenticationToken authentication =
                                new UsernamePasswordAuthenticationToken(
                                        userDetails, null, userDetails.getAuthorities());

                        authentication.setDetails(
                                new WebAuthenticationDetailsSource().buildDetails(request));

                        // Set authentication in context
                        SecurityContextHolder.getContext().setAuthentication(authentication);
                    }
                } catch (ExpiredJwtException e) {
                    logger.error("JWT token is expired: {}", e.getMessage());
                    handleJwtException(response, "JWT token is expired");
                    return;
                } catch (MalformedJwtException e) {
                    logger.error("Invalid JWT token: {}", e.getMessage());
                    handleJwtException(response, "Invalid JWT token");
                    return;
                } catch (Exception e) {
                    logger.error("Unable to validate JWT token: {}", e.getMessage());
                    handleJwtException(response, "Unable to validate JWT token");
                    return;
                }
            }

            filterChain.doFilter(request, response);
        } catch (Exception e) {
            logger.error("Cannot set user authentication: {}", e.getMessage());
            handleJwtException(response, "Cannot set user authentication");
        }
    }

    private String getJwtFromRequest(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");
        if (bearerToken != null && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);
        }
        return null;
    }

    private void handleJwtException(HttpServletResponse response, String message) throws IOException {
        response.setContentType("application/json");
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.getWriter().write(String.format("{\"error\": \"%s\"}", message));
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String path = request.getServletPath();

        // Skip JWT authentication for these paths
        return path.startsWith("/css/") ||
                path.startsWith("/js/") ||
                path.startsWith("/images/") ||
                path.startsWith("/fonts/") ||
                path.startsWith("/webjars/") ||
                path.equals("/") ||
                path.equals("/home") ||
                path.equals("/about") ||
                path.startsWith("/admin/login") ||
                path.startsWith("/admin/logout") ||
                path.startsWith("/api/auth/") ||
                path.startsWith("/api/public/") ||
                path.startsWith("/swagger-ui/") ||
                path.startsWith("/v3/api-docs/");
    }

    /**
     * Check if request is an API request
     */
    private boolean isApiRequest(HttpServletRequest request) {
        return request.getServletPath().startsWith("/api");
    }

    /**
     * Check if request is for static resources
     */
    private boolean isStaticResourceRequest(HttpServletRequest request) {
        String path = request.getServletPath();
        return path.startsWith("/css/") ||
                path.startsWith("/js/") ||
                path.startsWith("/images/") ||
                path.startsWith("/fonts/") ||
                path.startsWith("/webjars/");
    }

    /**
     * Check if request is for public endpoints
     */
    private boolean isPublicEndpoint(HttpServletRequest request) {
        String path = request.getServletPath();
        return path.equals("/") ||
                path.equals("/home") ||
                path.equals("/about") ||
                path.startsWith("/admin/login") ||
                path.startsWith("/admin/logout") ||
                path.startsWith("/api/auth/") ||
                path.startsWith("/api/public/");
    }

    /**
     * Check if request is for documentation
     */
    private boolean isDocumentationRequest(HttpServletRequest request) {
        String path = request.getServletPath();
        return path.startsWith("/swagger-ui/") ||
                path.startsWith("/v3/api-docs/");
    }
}
