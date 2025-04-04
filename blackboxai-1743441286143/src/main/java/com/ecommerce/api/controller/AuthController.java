package com.ecommerce.api.controller;

import com.ecommerce.api.payload.request.LoginRequest;
import com.ecommerce.api.payload.request.SignupRequest;
import com.ecommerce.api.payload.response.JwtResponse;
import com.ecommerce.api.payload.response.MessageResponse;
import com.ecommerce.api.security.JwtUtils;
import com.ecommerce.api.service.AuthService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;

@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping("/api/auth")
public class AuthController {
    @Autowired
    private JwtUtils jwtUtils;

    @Autowired
    private AuthService authService;

    @PostMapping("/signin")
    public ResponseEntity<?> authenticateUser(@Valid @RequestBody LoginRequest loginRequest) {
        JwtResponse jwtResponse = authService.authenticateUser(loginRequest);
        return ResponseEntity.ok(jwtResponse);
    }

    @PostMapping("/signup")
    public ResponseEntity<?> registerUser(@Valid @RequestBody SignupRequest signUpRequest) {
        authService.registerUser(signUpRequest);
        return ResponseEntity.ok(new MessageResponse("User registered successfully!"));
    }

    @CrossOrigin(origins = "https://y7lfq3-8000.csb.app")
    @GetMapping("/verify")
    public ResponseEntity<?> verifyToken(@RequestHeader("Authorization") String authHeader) {
        try {
            if (authHeader != null && authHeader.startsWith("Bearer ")) {
                String token = authHeader.substring(7);
                boolean isValid = jwtUtils.validateJwtToken(token);
                
                if (isValid) {
                    String username = jwtUtils.getUserNameFromJwtToken(token);
                    return ResponseEntity.ok(new MessageResponse("Token is valid for user: " + username));
                }
            }
            return ResponseEntity.badRequest().body(new MessageResponse("Invalid token"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new MessageResponse("Error verifying token: " + e.getMessage()));
        }
    }

}