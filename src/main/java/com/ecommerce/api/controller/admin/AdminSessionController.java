package com.ecommerce.api.controller.admin;

import com.ecommerce.api.model.User;
import com.ecommerce.api.model.UserSession;
import com.ecommerce.api.service.UserDetailsServiceImpl;
import com.ecommerce.api.service.UserSessionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import jakarta.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.Optional;

@Controller
@RequestMapping("/admin/sessions")
@PreAuthorize("hasRole('ADMIN')")
public class AdminSessionController {

    @Autowired
    private UserSessionService userSessionService;

    @Autowired
    private UserDetailsServiceImpl userDetailsService;

    @GetMapping
    public String viewSessions(Model model, HttpServletRequest request) {
        User currentUser = userDetailsService.getCurrentUser();
        
        // Get all active sessions for the current user
        List<UserSession> activeSessions = userSessionService.getActiveSessions(currentUser);
        
        // Get the current session
        Optional<UserSession> currentSession = userSessionService.getCurrentSession(currentUser);
        
        // If there's no current session marked, create one
        if (currentSession.isEmpty()) {
            UserSession newSession = userSessionService.createSession(currentUser, request);
            userSessionService.markSessionAsCurrent(newSession);
            currentSession = Optional.of(newSession);
        }

        model.addAttribute("sessions", activeSessions);
        currentSession.ifPresent(session -> model.addAttribute("currentSession", session));

        return "admin/sessions";
    }

    @PostMapping("/revoke")
    public String revokeSession(
            @RequestParam String sessionId,
            RedirectAttributes redirectAttributes) {
        try {
            User currentUser = userDetailsService.getCurrentUser();
            Optional<UserSession> currentSession = userSessionService.getCurrentSession(currentUser);
            
            // Prevent revoking current session through this endpoint
            if (currentSession.isPresent() && currentSession.get().getSessionId().equals(sessionId)) {
                redirectAttributes.addFlashAttribute("error", 
                    "Cannot revoke current session. Use 'Sign Out' instead.");
                return "redirect:/admin/sessions";
            }

            userSessionService.invalidateSession(sessionId);
            redirectAttributes.addFlashAttribute("success", "Session revoked successfully");
            
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", 
                "Failed to revoke session: " + e.getMessage());
        }
        
        return "redirect:/admin/sessions";
    }

    @PostMapping("/revoke-all")
    public String revokeAllSessions(
            HttpServletRequest request,
            RedirectAttributes redirectAttributes) {
        try {
            User currentUser = userDetailsService.getCurrentUser();
            String currentSessionId = request.getSession().getId();
            
            userSessionService.invalidateAllUserSessions(currentUser, currentSessionId);
            redirectAttributes.addFlashAttribute("success", 
                "All other sessions have been revoked successfully");
            
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", 
                "Failed to revoke sessions: " + e.getMessage());
        }
        
        return "redirect:/admin/sessions";
    }

    @GetMapping("/current")
    @ResponseBody
    public ResponseEntity<?> getCurrentSession() {
        try {
            User currentUser = userDetailsService.getCurrentUser();
            Optional<UserSession> currentSession = userSessionService.getCurrentSession(currentUser);
            
            if (currentSession.isPresent()) {
                return ResponseEntity.ok(currentSession.get());
            } else {
                return ResponseEntity.notFound().build();
            }
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Failed to get current session: " + e.getMessage());
        }
    }

    @GetMapping("/active")
    @ResponseBody
    public ResponseEntity<?> getActiveSessions() {
        try {
            User currentUser = userDetailsService.getCurrentUser();
            List<UserSession> activeSessions = userSessionService.getActiveSessions(currentUser);
            return ResponseEntity.ok(activeSessions);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Failed to get active sessions: " + e.getMessage());
        }
    }

    @PostMapping("/refresh")
    @ResponseBody
    public ResponseEntity<?> refreshSession(HttpServletRequest request) {
        try {
            String sessionId = request.getSession().getId();
            userSessionService.updateSessionActivity(sessionId);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Failed to refresh session: " + e.getMessage());
        }
    }

    @ExceptionHandler(Exception.class)
    public String handleError(Exception e, RedirectAttributes redirectAttributes) {
        redirectAttributes.addFlashAttribute("error", "An error occurred: " + e.getMessage());
        return "redirect:/admin/sessions";
    }
}
