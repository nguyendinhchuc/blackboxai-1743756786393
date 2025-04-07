package com.ecommerce.api.service;

import com.ecommerce.api.model.Role;
import com.ecommerce.api.model.RoleEnum;
import com.ecommerce.api.model.User;
import com.ecommerce.api.model.UserDetailsImpl;
import com.ecommerce.api.payload.request.LoginRequest;
import com.ecommerce.api.payload.request.SignupRequest;
import com.ecommerce.api.payload.response.JwtResponse;
import com.ecommerce.api.repository.RoleRepository;
import com.ecommerce.api.repository.UserRepository;
import com.ecommerce.api.security.JwtUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AuthService {
    private final AuthenticationManager authenticationManager;
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtils jwtUtils;

    @Autowired
    public AuthService(AuthenticationManager authenticationManager,
                     UserRepository userRepository,
                     RoleRepository roleRepository,
                     PasswordEncoder passwordEncoder,
                     JwtUtils jwtUtils) {
        this.authenticationManager = authenticationManager;
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtUtils = jwtUtils;
    }

    public JwtResponse authenticateUser(LoginRequest loginRequest) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(loginRequest.getUsername(), loginRequest.getPassword()));

        SecurityContextHolder.getContext().setAuthentication(authentication);
        String jwt = jwtUtils.generateJwtToken(authentication);

        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
        String role = userDetails.getAuthorities().stream()
                .findFirst()
                .map(item -> item.getAuthority())
                .orElse(RoleEnum.ROLE_USER.name());

        return new JwtResponse(jwt,
                             userDetails.getId(),
                             userDetails.getUsername(),
                             userDetails.getEmail(),
                             role);
    }

    public void registerUser(SignupRequest signUpRequest) {
        if (userRepository.existsByUsername(signUpRequest.getUsername())) {
            throw new RuntimeException("Error: Username is already taken!");
        }

        if (userRepository.existsByEmail(signUpRequest.getEmail())) {
            throw new RuntimeException("Error: Email is already in use!");
        }

        // Get role from request or use default ROLE_USER
        Role role;
        if (signUpRequest.getRole() != null) {
            try {
                RoleEnum roleEnum = RoleEnum.valueOf(signUpRequest.getRole());
                role = roleRepository.findByName(roleEnum)
                    .orElseThrow(() -> new RuntimeException("Error: Role is not found."));
            } catch (IllegalArgumentException e) {
                throw new RuntimeException("Error: Invalid role specified.");
            }
        } else {
            role = roleRepository.findByName(RoleEnum.ROLE_USER)
                .orElseThrow(() -> new RuntimeException("Error: Default role not found."));
        }

        User user = new User(signUpRequest.getUsername(), 
                           signUpRequest.getEmail(),
                           passwordEncoder.encode(signUpRequest.getPassword()),
                           signUpRequest.getTenantId(),
                           role);

        userRepository.save(user);
    }
}