package com.syntopia;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.security.servlet.UserDetailsServiceAutoConfiguration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

/**
 * Main Application Class for Syntopia Backend
 * 
 * Syntopia is a platform for exploring sacred geometry through interactive visualizations.
 * This backend service provides authentication, data management, and API endpoints
 * for the frontend Vue.js application.
 */
@SpringBootApplication(exclude = {UserDetailsServiceAutoConfiguration.class})
@EnableConfigurationProperties
public class SyntopiaApplication {

    public static void main(String[] args) {
        SpringApplication.run(SyntopiaApplication.class, args);
    }
}
