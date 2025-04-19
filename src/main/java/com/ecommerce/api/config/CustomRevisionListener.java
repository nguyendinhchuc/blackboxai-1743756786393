package com.ecommerce.api.config;

import com.ecommerce.api.model.Revision;
import jakarta.servlet.http.HttpServletRequest;
import org.hibernate.envers.RevisionListener;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.util.logging.Level;
import java.util.logging.Logger;

public class CustomRevisionListener implements RevisionListener {

    private static final Logger logger = Logger.getLogger(CustomRevisionListener.class.getName());

    @Override
    public void newRevision(Object revisionEntity) {
        if (revisionEntity instanceof Revision) {
            Revision revision = (Revision) revisionEntity;

            // Since auditorProvider is not static, we cannot access it here directly.
            // We will set username as "system" or implement a way to inject auditor info if needed.
            revision.setUsername("system");
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
