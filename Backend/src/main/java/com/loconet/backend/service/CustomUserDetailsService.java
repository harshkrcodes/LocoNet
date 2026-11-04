package com.loconet.backend.service;

import com.loconet.backend.repository.UserRepository;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class CustomUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;

    public CustomUserDetailsService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        // Yeh line Spring Security ko memory chhod kar PostgreSQL se email verify karne par majboor karegi
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("User nahi mila: " + email));
    }
}