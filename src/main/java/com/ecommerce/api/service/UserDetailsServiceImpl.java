package com.ecommerce.api.service;

import com.ecommerce.api.model.User;
import com.ecommerce.api.model.UserDetailsImpl;
import com.ecommerce.api.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class UserDetailsServiceImpl implements UserDetailsService {

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
}
