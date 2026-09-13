package com.loconet.backend.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

/**
 * Provides the PasswordEncoder bean used by UserService.
 *
 * Requires spring-security-crypto on the classpath (see README's dependency
 * note) — you do NOT need the full spring-boot-starter-security / a
 * SecurityFilterChain for this alone, just the crypto module for hashing.
 */
@Configuration
public class PasswordEncoderConfig {

    @Bean

    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
