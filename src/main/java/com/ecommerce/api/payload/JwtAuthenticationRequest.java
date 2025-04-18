package com.ecommerce.api.payload;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class JwtAuthenticationRequest {

    @NotBlank(message = "Username cannot be blank")
    @Size(min = 3, max = 50, message = "Username must be between 3 and 50 characters")
    private String username;

    @NotBlank(message = "Password cannot be blank")
    @Size(min = 6, max = 100, message = "Password must be between 6 and 100 characters")
    private String password;

    private Boolean rememberMe;

    private String refreshToken;

    private String deviceInfo;

    private String ipAddress;

    private String location;

    private TwoFactorAuthInfo twoFactorAuth;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TwoFactorAuthInfo {
        private String code;
        private String method; // "authenticator", "sms", "email"
        private Boolean remember; // Remember device for 2FA
    }

    /**
     * Create a login request
     */
    public static JwtAuthenticationRequest createLoginRequest(
            String username,
            String password,
            Boolean rememberMe,
            String deviceInfo,
            String ipAddress) {
        
        return JwtAuthenticationRequest.builder()
                .username(username)
                .password(password)
                .rememberMe(rememberMe)
                .deviceInfo(deviceInfo)
                .ipAddress(ipAddress)
                .build();
    }

    /**
     * Create a refresh token request
     */
    public static JwtAuthenticationRequest createRefreshTokenRequest(String refreshToken) {
        return JwtAuthenticationRequest.builder()
                .refreshToken(refreshToken)
                .build();
    }

    /**
     * Create a 2FA verification request
     */
    public static JwtAuthenticationRequest createTwoFactorRequest(
            String username,
            String code,
            String method,
            Boolean remember) {
        
        TwoFactorAuthInfo twoFactorAuth = TwoFactorAuthInfo.builder()
                .code(code)
                .method(method)
                .remember(remember)
                .build();

        return JwtAuthenticationRequest.builder()
                .username(username)
                .twoFactorAuth(twoFactorAuth)
                .build();
    }

    /**
     * Validate the request based on type
     */
    public boolean isValid() {
        if (isLoginRequest()) {
            return username != null && !username.isEmpty() &&
                   password != null && !password.isEmpty();
        }
        
        if (isRefreshTokenRequest()) {
            return refreshToken != null && !refreshToken.isEmpty();
        }
        
        if (isTwoFactorRequest()) {
            return username != null && !username.isEmpty() &&
                   twoFactorAuth != null &&
                   twoFactorAuth.getCode() != null && !twoFactorAuth.getCode().isEmpty() &&
                   twoFactorAuth.getMethod() != null && !twoFactorAuth.getMethod().isEmpty();
        }
        
        return false;
    }

    /**
     * Check if this is a login request
     */
    public boolean isLoginRequest() {
        return username != null && password != null;
    }

    /**
     * Check if this is a refresh token request
     */
    public boolean isRefreshTokenRequest() {
        return refreshToken != null;
    }

    /**
     * Check if this is a 2FA verification request
     */
    public boolean isTwoFactorRequest() {
        return username != null && twoFactorAuth != null;
    }

    /**
     * Get device type from device info
     */
    public String getDeviceType() {
        if (deviceInfo == null) {
            return "unknown";
        }
        
        String info = deviceInfo.toLowerCase();
        if (info.contains("mobile") || info.contains("android") || info.contains("iphone")) {
            return "mobile";
        } else if (info.contains("tablet") || info.contains("ipad")) {
            return "tablet";
        } else {
            return "desktop";
        }
    }

    /**
     * Get browser info from device info
     */
    public String getBrowser() {
        if (deviceInfo == null) {
            return "unknown";
        }

        String info = deviceInfo.toLowerCase();
        if (info.contains("chrome")) {
            return "Chrome";
        } else if (info.contains("firefox")) {
            return "Firefox";
        } else if (info.contains("safari")) {
            return "Safari";
        } else if (info.contains("edge")) {
            return "Edge";
        } else if (info.contains("opera")) {
            return "Opera";
        } else {
            return "Unknown";
        }
    }

    /**
     * Get operating system from device info
     */
    public String getOperatingSystem() {
        if (deviceInfo == null) {
            return "unknown";
        }

        String info = deviceInfo.toLowerCase();
        if (info.contains("windows")) {
            return "Windows";
        } else if (info.contains("mac")) {
            return "macOS";
        } else if (info.contains("linux")) {
            return "Linux";
        } else if (info.contains("android")) {
            return "Android";
        } else if (info.contains("ios")) {
            return "iOS";
        } else {
            return "Unknown";
        }
    }
}
