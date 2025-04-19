package com.ecommerce.api.controller;

import com.ecommerce.api.model.User;
import com.ecommerce.api.model.UserSession;
import com.ecommerce.api.payload.JwtAuthenticationRequest;
import com.ecommerce.api.payload.JwtAuthenticationResponse;
import com.ecommerce.api.security.JwtTokenProvider;
import com.ecommerce.api.service.UserDetailsServiceImpl;
import com.ecommerce.api.service.UserSessionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private JwtTokenProvider tokenProvider;

    @Autowired
    private UserDetailsServiceImpl userDetailsService;

    @Autowired
    private UserSessionService userSessionService;

    @PostMapping("/login")
    public ResponseEntity<?> authenticateUser(
            @Valid @RequestBody JwtAuthenticationRequest loginRequest,
            HttpServletRequest request) {
        try {
            // Authenticate user
            Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                    loginRequest.getUsername(),
                    loginRequest.getPassword()
                )
            );

            // Set authentication in context
            SecurityContextHolder.getContext().setAuthentication(authentication);

            // Get current user
            User user = userDetailsService.getCurrentUser();

            // Create session
            UserSession session = userSessionService.createSession(user, request);
            userSessionService.markSessionAsCurrent(session);

            // Generate tokens
            String accessToken = tokenProvider.generateToken(authentication);
            String refreshToken = tokenProvider.generateRefreshToken(authentication);

            // Get user roles
            List<String> roles = authentication.getAuthorities().stream()
                    .map(GrantedAuthority::getAuthority)
                    .collect(Collectors.toList());

            // Create user info
            JwtAuthenticationResponse.UserInfo userInfo = JwtAuthenticationResponse.UserInfo.builder()
                    .id(user.getId())
                    .firstName(user.getFirstName())
                    .lastName(user.getLastName())
                    .email(user.getEmail())
                    .avatar(user.getAvatar())
                    .isEmailVerified(user.getIsEmailVerified())
                    .isTwoFactorEnabled(user.isTwoFactorEnabled())
                    .lastLoginAt(System.currentTimeMillis())
                    .lastLoginIp(request.getRemoteAddr())
                    .build();

            // Return response
            return ResponseEntity.ok(JwtAuthenticationResponse.success(
                accessToken,
                refreshToken,
                tokenProvider.getExpirationDateFromToken(accessToken).getTime(),
                user.getUsername(),
                roles,
                userInfo
            ));

        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(JwtAuthenticationResponse.error(e.getMessage()));
        }
    }

    @PostMapping("/refresh")
    public ResponseEntity<?> refreshToken(
            @Valid @RequestBody JwtAuthenticationRequest refreshRequest) {
        try {
            String refreshToken = refreshRequest.getRefreshToken();

            // Validate refresh token
            if (!tokenProvider.validateToken(refreshToken)) {
                return ResponseEntity.badRequest()
                        .body(JwtAuthenticationResponse.error("Invalid refresh token"));
            }

            // Get username from refresh token
            String username = tokenProvider.getUsernameFromToken(refreshToken);

            // Load user details
            org.springframework.security.core.userdetails.UserDetails userDetails = 
                userDetailsService.loadUserByUsername(username);

            // Create authentication
            UsernamePasswordAuthenticationToken authentication = 
                new UsernamePasswordAuthenticationToken(
                    userDetails, null, userDetails.getAuthorities());

            // Generate new tokens
            String newAccessToken = tokenProvider.generateToken(authentication);
            String newRefreshToken = tokenProvider.generateRefreshToken(authentication);

            return ResponseEntity.ok(JwtAuthenticationResponse.refreshToken(
                newAccessToken,
                newRefreshToken,
                tokenProvider.getExpirationDateFromToken(newAccessToken).getTime()
            ));

        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(JwtAuthenticationResponse.error(e.getMessage()));
        }
    }

    @PostMapping("/logout")
    public ResponseEntity<?> logout(HttpServletRequest request) {
        try {
            // Get current session
            String sessionId = request.getSession().getId();
            
            // Deactivate session
            userSessionService.deactivateSession(sessionId);

            return ResponseEntity.ok().build();

        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @GetMapping("/validate")
    public ResponseEntity<?> validateToken(
            @RequestParam String token) {
        try {
            if (tokenProvider.validateToken(token)) {
                return ResponseEntity.ok().build();
            } else {
                return ResponseEntity.badRequest().build();
            }
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @GetMapping("/user")
    public ResponseEntity<?> getCurrentUser() {
        try {
            User user = userDetailsService.getCurrentUser();
            return ResponseEntity.ok(user);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PostMapping("/verify-2fa")
    public ResponseEntity<?> verifyTwoFactor(
            @Valid @RequestBody JwtAuthenticationRequest twoFactorRequest) {
        try {
            // Implement 2FA verification logic here
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
}
