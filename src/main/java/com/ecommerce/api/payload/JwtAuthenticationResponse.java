package com.ecommerce.api.payload;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class JwtAuthenticationResponse {

    private String accessToken;
    private String refreshToken;
    private String tokenType;
    private Long expiresIn;
    private String username;
    private List<String> roles;
    private UserInfo userInfo;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class UserInfo {
        private Long id;
        private String firstName;
        private String lastName;
        private String email;
        private String avatar;
        private Boolean isEmailVerified;
        private Boolean isTwoFactorEnabled;
        private Long lastLoginAt;
        private String lastLoginIp;
        private List<SessionInfo> activeSessions;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SessionInfo {
        private String deviceType;
        private String browser;
        private String operatingSystem;
        private String ipAddress;
        private String location;
        private Long loginTime;
        private Boolean isCurrent;
    }

    public static JwtAuthenticationResponse success(
            String accessToken,
            String refreshToken,
            Long expiresIn,
            String username,
            List<String> roles,
            UserInfo userInfo) {
        
        return JwtAuthenticationResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .tokenType("Bearer")
                .expiresIn(expiresIn)
                .username(username)
                .roles(roles)
                .userInfo(userInfo)
                .build();
    }

    public static JwtAuthenticationResponse refreshToken(
            String accessToken,
            String refreshToken,
            Long expiresIn) {
        
        return JwtAuthenticationResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .tokenType("Bearer")
                .expiresIn(expiresIn)
                .build();
    }

    public static JwtAuthenticationResponse error(String message) {
        return JwtAuthenticationResponse.builder()
                .tokenType("error")
                .build();
    }

    /**
     * Convert to a map for easier JSON serialization
     */
    public java.util.Map<String, Object> toMap() {
        java.util.Map<String, Object> map = new java.util.HashMap<>();
        map.put("access_token", accessToken);
        map.put("refresh_token", refreshToken);
        map.put("token_type", tokenType);
        map.put("expires_in", expiresIn);
        map.put("username", username);
        map.put("roles", roles);
        
        if (userInfo != null) {
            java.util.Map<String, Object> userInfoMap = new java.util.HashMap<>();
            userInfoMap.put("id", userInfo.getId());
            userInfoMap.put("firstName", userInfo.getFirstName());
            userInfoMap.put("lastName", userInfo.getLastName());
            userInfoMap.put("email", userInfo.getEmail());
            userInfoMap.put("avatar", userInfo.getAvatar());
            userInfoMap.put("isEmailVerified", userInfo.getIsEmailVerified());
            userInfoMap.put("isTwoFactorEnabled", userInfo.getIsTwoFactorEnabled());
            userInfoMap.put("lastLoginAt", userInfo.getLastLoginAt());
            userInfoMap.put("lastLoginIp", userInfo.getLastLoginIp());
            
            if (userInfo.getActiveSessions() != null) {
                java.util.List<java.util.Map<String, Object>> sessionsList = new java.util.ArrayList<>();
                for (SessionInfo session : userInfo.getActiveSessions()) {
                    java.util.Map<String, Object> sessionMap = new java.util.HashMap<>();
                    sessionMap.put("deviceType", session.getDeviceType());
                    sessionMap.put("browser", session.getBrowser());
                    sessionMap.put("operatingSystem", session.getOperatingSystem());
                    sessionMap.put("ipAddress", session.getIpAddress());
                    sessionMap.put("location", session.getLocation());
                    sessionMap.put("loginTime", session.getLoginTime());
                    sessionMap.put("isCurrent", session.getIsCurrent());
                    sessionsList.add(sessionMap);
                }
                userInfoMap.put("activeSessions", sessionsList);
            }
            
            map.put("user_info", userInfoMap);
        }
        
        return map;
    }

    /**
     * Create from a map
     */
    public static JwtAuthenticationResponse fromMap(java.util.Map<String, Object> map) {
        JwtAuthenticationResponse response = new JwtAuthenticationResponse();
        response.setAccessToken((String) map.get("access_token"));
        response.setRefreshToken((String) map.get("refresh_token"));
        response.setTokenType((String) map.get("token_type"));
        response.setExpiresIn((Long) map.get("expires_in"));
        response.setUsername((String) map.get("username"));
        response.setRoles((List<String>) map.get("roles"));
        
        @SuppressWarnings("unchecked")
        java.util.Map<String, Object> userInfoMap = (java.util.Map<String, Object>) map.get("user_info");
        if (userInfoMap != null) {
            UserInfo userInfo = new UserInfo();
            userInfo.setId(((Number) userInfoMap.get("id")).longValue());
            userInfo.setFirstName((String) userInfoMap.get("firstName"));
            userInfo.setLastName((String) userInfoMap.get("lastName"));
            userInfo.setEmail((String) userInfoMap.get("email"));
            userInfo.setAvatar((String) userInfoMap.get("avatar"));
            userInfo.setIsEmailVerified((Boolean) userInfoMap.get("isEmailVerified"));
            userInfo.setIsTwoFactorEnabled((Boolean) userInfoMap.get("isTwoFactorEnabled"));
            userInfo.setLastLoginAt(((Number) userInfoMap.get("lastLoginAt")).longValue());
            userInfo.setLastLoginIp((String) userInfoMap.get("lastLoginIp"));
            
            @SuppressWarnings("unchecked")
            List<java.util.Map<String, Object>> sessionsList = 
                (List<java.util.Map<String, Object>>) userInfoMap.get("activeSessions");
            if (sessionsList != null) {
                List<SessionInfo> sessions = new java.util.ArrayList<>();
                for (java.util.Map<String, Object> sessionMap : sessionsList) {
                    SessionInfo session = new SessionInfo();
                    session.setDeviceType((String) sessionMap.get("deviceType"));
                    session.setBrowser((String) sessionMap.get("browser"));
                    session.setOperatingSystem((String) sessionMap.get("operatingSystem"));
                    session.setIpAddress((String) sessionMap.get("ipAddress"));
                    session.setLocation((String) sessionMap.get("location"));
                    session.setLoginTime(((Number) sessionMap.get("loginTime")).longValue());
                    session.setIsCurrent((Boolean) sessionMap.get("isCurrent"));
                    sessions.add(session);
                }
                userInfo.setActiveSessions(sessions);
            }
            
            response.setUserInfo(userInfo);
        }
        
        return response;
    }
}
