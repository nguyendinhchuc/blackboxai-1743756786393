package com.ecommerce.api.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.ecommerce.api.model.User;
import com.ecommerce.api.model.UserDetailsImpl;
import com.ecommerce.api.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class UserDetailsServiceImpl implements UserDetailsService {

    private static final Logger logger = LoggerFactory.getLogger(UserDetailsServiceImpl.class);

    @Autowired
    private UserRepository userRepository;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found with username: " + username));

        // Ensure role is loaded
        if (user.getRole() != null) {
            user.getRole().getName(); // Force load the role
        }

        return UserDetailsImpl.build(user);
    }

    public User getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            return null;
        }
        Object principal = authentication.getPrincipal();
        if (principal instanceof UserDetailsImpl) {
            return ((UserDetailsImpl) principal).getUser();
        }
        return null;
    }

    public User updateUser(User user) {
        try {
            return userRepository.save(user);
        } catch (Exception e) {
            logger.error("Error updating user: " + e.getMessage(), e);
            throw new RuntimeException("Failed to update user", e);
        }
    }

    public void deleteUser(Long userId) {
        try {
            userRepository.deleteById(userId);
        } catch (Exception e) {
            logger.error("Error deleting user with id " + userId + ": " + e.getMessage(), e);
            throw new RuntimeException("Failed to delete user with id " + userId, e);
        }
    }

    public boolean isLastAdmin(User currentUser) {
        if (currentUser == null || currentUser.getRole() == null) {
            return false;
        }
        if (!currentUser.getRole().getName().equals(com.ecommerce.api.model.RoleEnum.ROLE_ADMIN)) {
            return false;
        }
        long adminCount = userRepository.countByRole_Name(com.ecommerce.api.model.RoleEnum.ROLE_ADMIN);
        return adminCount == 1;
    }
}
