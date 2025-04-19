package com.ecommerce.api.controller;

import com.ecommerce.api.config.RevisionNotificationConfig;
import com.ecommerce.api.notification.RevisionNotificationService;
import com.ecommerce.api.payload.ApiResponse;
import com.ecommerce.api.security.CustomAuthenticationSuccessHandler;
import com.ecommerce.api.validator.RevisionNotificationValidator;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import java.util.List;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/v1/notifications")
@RequiredArgsConstructor
@Tag(name = "Revision Notifications", description = "APIs for managing revision notifications")
public class RevisionNotificationController {

    private final RevisionNotificationService notificationService;
    private final RevisionNotificationValidator notificationValidator;
    private final RevisionNotificationConfig notificationConfig;

    /**
     * Test email configuration
     */
    @PostMapping("/test-email")
    @PreAuthorize("hasRole('REVISION_ADMIN')")
    @Operation(summary = "Test email configuration")
    public ResponseEntity<ApiResponse<Void>> testEmailConfiguration(
            @Parameter(description = "Test email recipient")
            @RequestParam @Email String recipient) {
        notificationValidator.validateEmailRecipient(recipient);
        notificationService.sendSystemAlert(
                "Test Email",
                "This is a test email from the revision notification system.",
                List.of(recipient)
        );
        return ApiResponse.ok("Test email sent successfully", null);
    }

    /**
     * Update notification settings
     */
    @PutMapping("/settings")
    @PreAuthorize("hasRole('REVISION_ADMIN')")
    @Operation(summary = "Update notification settings")
    public ResponseEntity<ApiResponse<Void>> updateNotificationSettings(
            @Parameter(description = "Notification settings")
            @RequestBody @Valid NotificationSettingsRequest request) {
        // Update email settings
        // notificationConfig.getEmail().setEnabled(request.emailEnabled());
        notificationConfig.setEmailEnabled(request.emailEnabled());
        notificationConfig.getEmail().setFrom(request.emailFrom());

        // Update notification levels
        notificationConfig.getLevels().setError(request.errorEnabled());
        notificationConfig.getLevels().setWarning(request.warningEnabled());
        notificationConfig.getLevels().setInfo(request.infoEnabled());
        notificationConfig.getLevels().setDebug(request.debugEnabled());

        // Validate updated configuration
        // notificationConfig.validate();

        return ApiResponse.ok("Notification settings updated", null);
    }

    /**
     * Get notification settings
     */
    @GetMapping("/settings")
    @PreAuthorize("hasRole('REVISION_ADMIN')")
    @Operation(summary = "Get notification settings")
    public ResponseEntity<NotificationSettingsResponse> getNotificationSettings() {
        return ResponseEntity.ok(new NotificationSettingsResponse(
                notificationConfig.isEmailEnabled(),
                notificationConfig.getEmail().getFrom(),
                notificationConfig.getLevels().isError(),
                notificationConfig.getLevels().isWarning(),
                notificationConfig.getLevels().isInfo(),
                notificationConfig.getLevels().isDebug(),
                notificationConfig.getRecipients().getAdministrators(),
                notificationConfig.getRecipients().getDevelopers(),
                notificationConfig.getRecipients().getManagers(),
                // notificationConfig.getRecipients().getAuditors()
                List.of()
        ));
    }

    /**
     * Update recipients
     */
    @PutMapping("/recipients/{role}")
    @PreAuthorize("hasRole('REVISION_ADMIN')")
    @Operation(summary = "Update recipients for a role")
    public ResponseEntity<ApiResponse<Void>> updateRecipients(
            @Parameter(description = "Role (admin/developer/manager/auditor)")
            @PathVariable @NotBlank String role,
            @Parameter(description = "List of email addresses")
            @RequestBody @NotEmpty List<@Email String> recipients) {
        notificationValidator.validateEmailRecipients(recipients);

        switch (role.toLowerCase()) {
            case "admin" -> notificationConfig.getRecipients().setAdministrators(recipients);
            case "developer" -> notificationConfig.getRecipients().setDevelopers(recipients);
            case "manager" -> notificationConfig.getRecipients().setManagers(recipients);
            case "auditor" -> {
                // No auditors field in RecipientConfig, ignore or handle accordingly
            }
            default -> throw new IllegalArgumentException("Invalid role: " + role);
        }

        return ApiResponse.ok("Recipients updated for role: " + role, null);
    }

    /**
     * Send custom notification
     */
    @PostMapping("/custom")
    @PreAuthorize("hasRole('REVISION_ADMIN')")
    @Operation(summary = "Send custom notification")
    public ResponseEntity<ApiResponse<Void>> sendCustomNotification(
            @Parameter(description = "Custom notification request")
            @RequestBody @Valid CustomNotificationRequest request) {
        notificationValidator.validateEmailRecipients(request.recipients());
        notificationValidator.validateSubject(request.subject());
        notificationValidator.validateContent(request.content());

        notificationService.sendSystemAlert(
                request.subject(),
                request.content(),
                request.recipients()
        );

        return ApiResponse.ok("Custom notification sent", null);
    }

    /**
     * Get notification statistics
     */
    @GetMapping("/stats")
    @PreAuthorize("hasRole('REVISION_ADMIN')")
    @Operation(summary = "Get notification statistics")
    public ResponseEntity<Map<String, Object>> getNotificationStats() {
        return ResponseEntity.ok(Map.of(
                "totalSent", notificationService.getTotalNotificationsSent(),
                "totalErrors", notificationService.getTotalNotificationErrors(),
                "averageDeliveryTime", notificationService.getAverageDeliveryTime(),
                "deliverySuccess", notificationService.getDeliverySuccessRate()
        ));
    }

    /**
     * Notification settings request record
     */
    public record NotificationSettingsRequest(
            boolean emailEnabled,
            @Email String emailFrom,
            boolean errorEnabled,
            boolean warningEnabled,
            boolean infoEnabled,
            boolean debugEnabled
    ) {}

    /**
     * Notification settings response record
     */
    public record NotificationSettingsResponse(
            boolean emailEnabled,
            String emailFrom,
            boolean errorEnabled,
            boolean warningEnabled,
            boolean infoEnabled,
            boolean debugEnabled,
            List<String> adminRecipients,
            List<String> developerRecipients,
            List<String> managerRecipients,
            List<String> auditorRecipients
    ) {}

    /**
     * Custom notification request record
     */
    public record CustomNotificationRequest(
            @NotEmpty List<@Email String> recipients,
            @NotBlank String subject,
            @NotBlank String content
    ) {}
}
