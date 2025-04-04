package com.ecommerce.api.controller;

import com.ecommerce.api.payload.request.LoginRequest;
import com.ecommerce.api.payload.request.SignupRequest;
import com.ecommerce.api.payload.response.JwtResponse;
import com.ecommerce.api.payload.response.MessageResponse;
import com.ecommerce.api.service.AuthService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping("/test")
public class TestController {

    @GetMapping
    public ResponseEntity<?> authenticateUser() {
        return ResponseEntity.ok(new MessageResponse("User registered successfully!"));
    }
}