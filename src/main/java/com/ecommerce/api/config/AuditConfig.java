package com.ecommerce.api.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.domain.AuditorAware;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import jakarta.servlet.http.HttpServletRequest;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;

@Configuration
@EnableJpaAuditing(auditorAwareRef = "auditorProvider")
public class AuditConfig {

    @Bean
    public AuditorAware<String> auditorProvider() {
        return new SpringSecurityAuditAwareImpl();
    }

    class SpringSecurityAuditAwareImpl implements AuditorAware<String> {

        @Override
        public Optional<String> getCurrentAuditor() {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

            if (authentication == null || !authentication.isAuthenticated()) {
                return Optional.of("system");
            }

            if (authentication.getPrincipal() instanceof String) {
                return Optional.of((String) authentication.getPrincipal());
            }

            if (authentication.getPrincipal() instanceof UserDetails) {
                return Optional.of(((UserDetails) authentication.getPrincipal()).getUsername());
            }

            return Optional.of(authentication.getName());
        }
    }

    /**
     * Custom auditor resolver that can handle different types of authentication principals
     */
    class CustomAuditorResolver {
        
        public String resolveAuditor(Authentication authentication) {
            if (authentication == null || !authentication.isAuthenticated()) {
                return "system";
            }

            Object principal = authentication.getPrincipal();

            if (principal instanceof String) {
                return (String) principal;
            }

            if (principal instanceof UserDetails) {
                return ((UserDetails) principal).getUsername();
            }

            // Handle custom user principal
            if (principal instanceof com.ecommerce.api.model.User) {
                return ((com.ecommerce.api.model.User) principal).getUsername();
            }

            // Handle OAuth2 authentication
            if (principal instanceof org.springframework.security.oauth2.core.user.OAuth2User) {
                return ((org.springframework.security.oauth2.core.user.OAuth2User) principal)
                    .getAttributeyes("email");
            }
("email");
            }

            return authentication.getName();
        }
    }

    /**
     * Audit event listener
     */
    @Bean
    public org.springframework.data.jpa.domain.support.AuditingEntityListener auditingEntityListener() {
        return new org.springframework.data.jpa.domain.support.AuditingEntityListener();
    }

    /**
     * Custom audit event listener that provides additional auditing functionality
     */
    class CustomAuditEventListener {

        @jakarta.persistence.PrePersist
        public void prePersist(Object entity) {
            if (entity instanceof com.ecommerce.api.model.BaseEntity) {
                com.ecommerce.api.model.BaseEntity baseEntity = (com.ecommerce.api.model.BaseEntity) entity;
                String currentAuditor = auditorProvider().getCurrentAuditor().orElse("system");
                
                if (baseEntity.getCreatedBy() == null) {
                    baseEntity.setCreatedBy(currentAuditor);
                }
                if (baseEntity.getUpdatedBy() == null) {
                    baseEntity.setUpdatedBy(currentAuditor);
                }
            }
        }

        @jakarta.persistence.PreUpdate
        public void preUpdate(Object entity) {
            if (entity instanceof com.ecommerce.api.model.BaseEntity) {
                com.ecommerce.api.model.BaseEntity baseEntity = (com.ecommerce.api.model.BaseEntity) entity;
                String currentAuditor = auditorProvider().getCurrentAuditor().orElse("system");
                
                baseEntity.setUpdatedBy(currentAuditor);
                baseEntity.setUpdatedAt(java.time.LocalDateTime.now());
            }
        }

        @jakarta.persistence.PreRemove
        public void preRemove(Object entity) {
            if (entity instanceof com.ecommerce.api.model.BaseEntity) {
                com.ecommerce.api.model.BaseEntity baseEntity = (com.ecommerce.api.model.BaseEntity) entity;
                String currentAuditor = auditorProvider().getCurrentAuditor().orElse("system");
                
                baseEntity.setDeletedBy(currentAuditor);
                baseEntity.setDeletedAt(java.time.LocalDateTime.now());
            }
        }
    }

    /**
     * Audit revision listener
     */
    @Bean
    public org.hibernate.envers.RevisionListener revisionListener() {
        return new CustomRevisionListener();
    }

    class CustomRevisionListener implements org.hibernate.envers.RevisionListener {
        private static final Logger logger = Logger.getLogger(CustomRevisionListener.class.getName());
        
        @Override
        public void newRevision(Object revisionEntity) {
            if (revisionEntity instanceof com.ecommerce.api.model.Revision) {
                com.ecommerce.api.model.Revision revision = (com.ecommerce.api.model.Revision) revisionEntity;
                String currentAuditor = auditorProvider().getCurrentAuditor().orElse("system");
                
                revision.setUsername(currentAuditor);
                revision.setTimestamp(System.currentTimeMillis());
                
                // Capture IP address and User-Agent from the current request
                try {
                    ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
                    if (attributes != null) {
                        HttpServletRequest request = attributes.getRequest();
                        
                        // Get IP address (check for X-Forwarded-For header first)
                        String ipAddress = request.getHeader("X-Forwarded-For");
                        if (ipAddress == null || ipAddress.isEmpty() || "unknown".equalsIgnoreCase(ipAddress)) {
                            ipAddress = request.getRemoteAddr();
                        } else {
                            // X-Forwarded-For might contain multiple IPs; take the first one
                            ipAddress = ipAddress.split(",")[0].trim();
                        }
                        
                        // Get User-Agent
                        String userAgent = request.getHeader("User-Agent");
                        
                        // Set the values in the revision entity
                        revision.setIpAddress(ipAddress);
                        revision.setUserAgent(userAgent);
                    } else {
                        // No request context available (e.g., background job)
                        revision.setIpAddress("unknown");
                        revision.setUserAgent("unknown");
                    }
                } catch (Exception e) {
                    // Log the exception but don't let it interrupt the audit process
                    logger.log(Level.WARNING, "Failed to capture request details for audit: " + e.getMessage(), e);
                    revision.setIpAddress("unknown");
                    revision.setUserAgent("unknown");
                }
            }
        }
    }
}
