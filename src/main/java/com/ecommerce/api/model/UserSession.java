package com.ecommerce.api.model;

import lombok.Data;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Data
@Entity
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "user_sessions")
public class UserSession {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(nullable = false)
    private String sessionId;

    @Column(nullable = false)
    private String ipAddress;

    @Column(nullable = false)
    private String userAgent;

    @Column
    private String browser;

    @Column
    private String platform;

    @Column
    private String deviceType;

    @Column
    private String location;

    @Column(nullable = false)
    private LocalDateTime createdAt;

    @Column(nullable = false)
    private LocalDateTime lastActiveTime;

    @Column(nullable = false)
    private boolean active;

    @Column(nullable = false)
    private boolean currentSession;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        lastActiveTime = LocalDateTime.now();
    }

    public void updateLastActiveTime() {
        this.lastActiveTime = LocalDateTime.now();
    }

    public String getDeviceInfo() {
        return String.format("%s on %s", browser, platform);
    }

    public String getLastActiveTimeFormatted() {
        // TODO: Implement proper time formatting (e.g., "2 hours ago")
        return lastActiveTime.toString();
    }

    public boolean isExpired() {
        // Session expires after 30 minutes of inactivity
        return LocalDateTime.now().minusMinutes(30).isAfter(lastActiveTime);
    }

    public void deactivate() {
        this.active = false;
        this.currentSession = false;
    }

    // Helper method to parse User-Agent string
    public static UserSession createFromRequest(User user, String sessionId, String ipAddress, String userAgent) {
        UserSession session = new UserSession();
        session.setUser(user);
        session.setSessionId(sessionId);
        session.setIpAddress(ipAddress);
        session.setUserAgent(userAgent);
        session.setActive(true);
        session.setCurrentSession(false);

        // Parse user agent to set browser, platform, and device type
        // This is a simple implementation - in production, use a proper User-Agent parser library
        String userAgentLower = userAgent.toLowerCase();
        
        // Set browser
        if (userAgentLower.contains("chrome")) {
            session.setBrowser("Chrome");
        } else if (userAgentLower.contains("firefox")) {
            session.setBrowser("Firefox");
        } else if (userAgentLower.contains("safari")) {
            session.setBrowser("Safari");
        } else if (userAgentLower.contains("edge")) {
            session.setBrowser("Edge");
        } else {
            session.setBrowser("Unknown");
        }

        // Set platform
        if (userAgentLower.contains("windows")) {
            session.setPlatform("Windows");
        } else if (userAgentLower.contains("mac")) {
            session.setPlatform("macOS");
        } else if (userAgentLower.contains("linux")) {
            session.setPlatform("Linux");
        } else if (userAgentLower.contains("android")) {
            session.setPlatform("Android");
        } else if (userAgentLower.contains("iphone") || userAgentLower.contains("ipad")) {
            session.setPlatform("iOS");
        } else {
            session.setPlatform("Unknown");
        }

        // Set device type
        if (userAgentLower.contains("mobile") || userAgentLower.contains("android") || userAgentLower.contains("iphone")) {
            session.setDeviceType("MOBILE");
        } else if (userAgentLower.contains("ipad") || userAgentLower.contains("tablet")) {
            session.setDeviceType("TABLET");
        } else {
            session.setDeviceType("DESKTOP");
        }

        return session;
    }
}
