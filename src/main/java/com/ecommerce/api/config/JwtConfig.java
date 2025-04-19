package com.ecommerce.api.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.validation.annotation.Validated;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import java.time.Duration;

@Configuration
//@ConfigurationProperties(prefix = "app.jwt")
@Validated
public class JwtConfig {

    /**
     * Secret key used for signing JWT tokens
     */
    @NotBlank
    private String secret;

    /**
     * Access token expiration time
     */
    @NotNull
    @Positive
    private Duration expiration = Duration.ofHours(1);

    /**
     * Refresh token expiration time
     */
    @NotNull
    @Positive
    private Duration refreshExpiration = Duration.ofDays(7);

    /**
     * Token prefix in Authorization header
     */
    @NotBlank
    private String tokenPrefix = "Bearer ";

    /**
     * Header name for Authorization
     */
    @NotBlank
    private String headerString = "Authorization";

    /**
     * Path for authentication endpoint
     */
    @NotBlank
    private String authEndpoint = "/api/auth";

    /**
     * Issuer name for JWT tokens
     */
    @NotBlank
    private String issuer = "ecommerce-api";

    /**
     * Audience for JWT tokens
     */
    @NotBlank
    private String audience = "ecommerce-web";

    /**
     * Token type
     */
    @NotBlank
    private String tokenType = "JWT";

    /**
     * Maximum token refresh attempts
     */
    @NotNull
    @Positive
    private Integer maxRefreshAttempts = 3;

    /**
     * Whether to include roles in JWT claims
     */
    private boolean includeRoles = true;

    /**
     * Whether to include permissions in JWT claims
     */
    private boolean includePermissions = true;

    // Getters and Setters

    public String getSecret() {
        return secret;
    }

    public void setSecret(String secret) {
        this.secret = secret;
    }

    public Duration getExpiration() {
        return expiration;
    }

    public void setExpiration(Duration expiration) {
        this.expiration = expiration;
    }

    public Duration getRefreshExpiration() {
        return refreshExpiration;
    }

    public void setRefreshExpiration(Duration refreshExpiration) {
        this.refreshExpiration = refreshExpiration;
    }

    public String getTokenPrefix() {
        return tokenPrefix;
    }

    public void setTokenPrefix(String tokenPrefix) {
        this.tokenPrefix = tokenPrefix;
    }

    public String getHeaderString() {
        return headerString;
    }

    public void setHeaderString(String headerString) {
        this.headerString = headerString;
    }

    public String getAuthEndpoint() {
        return authEndpoint;
    }

    public void setAuthEndpoint(String authEndpoint) {
        this.authEndpoint = authEndpoint;
    }

    public String getIssuer() {
        return issuer;
    }

    public void setIssuer(String issuer) {
        this.issuer = issuer;
    }

    public String getAudience() {
        return audience;
    }

    public void setAudience(String audience) {
        this.audience = audience;
    }

    public String getTokenType() {
        return tokenType;
    }

    public void setTokenType(String tokenType) {
        this.tokenType = tokenType;
    }

    public Integer getMaxRefreshAttempts() {
        return maxRefreshAttempts;
    }

    public void setMaxRefreshAttempts(Integer maxRefreshAttempts) {
        this.maxRefreshAttempts = maxRefreshAttempts;
    }

    public boolean isIncludeRoles() {
        return includeRoles;
    }

    public void setIncludeRoles(boolean includeRoles) {
        this.includeRoles = includeRoles;
    }

    public boolean isIncludePermissions() {
        return includePermissions;
    }

    public void setIncludePermissions(boolean includePermissions) {
        this.includePermissions = includePermissions;
    }

    /**
     * Get expiration time in milliseconds
     */
    public int getExpirationInMs() {
        return (int) expiration.toMillis();
    }

    /**
     * Get refresh expiration time in milliseconds
     */
    public int getRefreshExpirationInMs() {
        return (int) refreshExpiration.toMillis();
    }

    /**
     * Get full authentication path
     */
    public String getAuthPath() {
        return authEndpoint + "/login";
    }

    /**
     * Get full refresh token path
     */
    public String getRefreshPath() {
        return authEndpoint + "/refresh";
    }

    /**
     * Get authorization header with token prefix
     */
    public String getAuthorizationHeader(String token) {
        return tokenPrefix + token;
    }

    /**
     * Extract token from authorization header
     */
    public String extractToken(String authorizationHeader) {
        if (authorizationHeader != null && authorizationHeader.startsWith(tokenPrefix)) {
            return authorizationHeader.substring(tokenPrefix.length());
        }
        return null;
    }
}
