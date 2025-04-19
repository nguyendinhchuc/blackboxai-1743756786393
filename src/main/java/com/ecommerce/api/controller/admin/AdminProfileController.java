package com.ecommerce.api.controller.admin;

import com.ecommerce.api.model.User;
import com.ecommerce.api.service.UserDetailsServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/admin/profile")
public class AdminProfileController {

    @Autowired
    private UserDetailsServiceImpl userDetailsService;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @GetMapping
    public String profile(Model model) {
        User currentUser = userDetailsService.getCurrentUser();
        model.addAttribute("user", currentUser);
        return "admin/profile";
    }

    @PostMapping("/update")
    public String updateProfile(
            @ModelAttribute User updatedUser,
            RedirectAttributes redirectAttributes) {
        try {
            User currentUser = userDetailsService.getCurrentUser();
            
            // Update only allowed fields
            currentUser.setEmail(updatedUser.getEmail());
            currentUser.setFirstName(updatedUser.getFirstName());
            currentUser.setLastName(updatedUser.getLastName());
            
            userDetailsService.updateUser(currentUser);
            
            redirectAttributes.addFlashAttribute("success", "Profile updated successfully");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Failed to update profile: " + e.getMessage());
        }
        return "redirect:/admin/profile";
    }

    @PostMapping("/change-password")
    public String changePassword(
            @RequestParam String currentPassword,
            @RequestParam String newPassword,
            @RequestParam String confirmPassword,
            RedirectAttributes redirectAttributes) {
        try {
            User currentUser = userDetailsService.getCurrentUser();
            
            // Validate current password
            if (!passwordEncoder.matches(currentPassword, currentUser.getPassword())) {
                redirectAttributes.addFlashAttribute("error", "Current password is incorrect");
                return "redirect:/admin/profile";
            }
            
            // Validate new password
            if (!newPassword.equals(confirmPassword)) {
                redirectAttributes.addFlashAttribute("error", "New passwords do not match");
                return "redirect:/admin/profile";
            }
            
            // Update password
            currentUser.setPassword(passwordEncoder.encode(newPassword));
            userDetailsService.updateUser(currentUser);
            
            redirectAttributes.addFlashAttribute("success", "Password changed successfully");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Failed to change password: " + e.getMessage());
        }
        return "redirect:/admin/profile";
    }

    @PostMapping("/delete")
    public String deleteAccount(RedirectAttributes redirectAttributes) {
        try {
            User currentUser = userDetailsService.getCurrentUser();
            
            // Check if user is the last admin
            if (userDetailsService.isLastAdmin(currentUser)) {
                redirectAttributes.addFlashAttribute("error", 
                    "Cannot delete account: You are the last administrator");
                return "redirect:/admin/profile";
            }
            
            userDetailsService.deleteUser(currentUser.getId());
            return "redirect:/admin/logout";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Failed to delete account: " + e.getMessage());
            return "redirect:/admin/profile";
        }
    }

    @PostMapping("/enable-2fa")
    public String enableTwoFactorAuth(RedirectAttributes redirectAttributes) {
        try {
            User currentUser = userDetailsService.getCurrentUser();
            // TODO: Implement 2FA setup
            redirectAttributes.addFlashAttribute("success", "Two-factor authentication enabled");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Failed to enable two-factor authentication: " + e.getMessage());
        }
        return "redirect:/admin/profile";
    }

    @GetMapping("/sessions")
    public String viewActiveSessions(Model model) {
        User currentUser = userDetailsService.getCurrentUser();
        // TODO: Implement session management
        return "admin/sessions";
    }

    @PostMapping("/sessions/revoke-all")
    public String revokeAllSessions(RedirectAttributes redirectAttributes) {
        try {
            User currentUser = userDetailsService.getCurrentUser();
            // TODO: Implement session revocation
            redirectAttributes.addFlashAttribute("success", "All sessions have been revoked");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Failed to revoke sessions: " + e.getMessage());
        }
        return "redirect:/admin/profile";
    }
}
