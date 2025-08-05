package com.syntopia.controller;

import com.syntopia.model.User;
import com.syntopia.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * Simple Authentication Controller for Syntopia Platform
 * 
 * Basic user registration and login for the worldwide community platform.
 */
@RestController
@RequestMapping("/api/auth")
@CrossOrigin(origins = {"http://localhost:5173", "http://localhost:3000"})
public class SimpleAuthController {

    @Autowired
    private UserService userService;

    /**
     * Register a new user
     */
    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody RegisterRequest request) {
        try {
            // Validate input
            if (request.getUsername() == null || request.getUsername().trim().isEmpty()) {
                return ResponseEntity.badRequest().body(createErrorResponse("Username is required"));
            }
            if (request.getEmail() == null || request.getEmail().trim().isEmpty()) {
                return ResponseEntity.badRequest().body(createErrorResponse("Email is required"));
            }
            if (request.getPassword() == null || request.getPassword().length() < 6) {
                return ResponseEntity.badRequest().body(createErrorResponse("Password must be at least 6 characters"));
            }

            // Check if user already exists
            Optional<User> existingUser = userService.findByUsername(request.getUsername().trim());
            if (existingUser.isPresent()) {
                return ResponseEntity.badRequest().body(createErrorResponse("Username already exists"));
            }

            Optional<User> existingEmail = userService.findByEmail(request.getEmail().trim());
            if (existingEmail.isPresent()) {
                return ResponseEntity.badRequest().body(createErrorResponse("Email already exists"));
            }

            // Create user
            User user = userService.createUser(
                request.getUsername().trim(),
                request.getEmail().trim(),
                request.getDisplayName() != null ? request.getDisplayName().trim() : request.getUsername().trim()
            );

            // For now, store password as plain text (TODO: add encryption later)
            user.setPasswordHash(request.getPassword());
            user = userService.save(user);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "User registered successfully");
            response.put("user", createUserResponse(user));

            return ResponseEntity.ok(response);

        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(createErrorResponse(e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(createErrorResponse("Registration failed: " + e.getMessage()));
        }
    }

    /**
     * Login user (simplified)
     */
    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest request) {
        try {
            // Validate input
            if (request.getUsername() == null || request.getUsername().trim().isEmpty()) {
                return ResponseEntity.badRequest().body(createErrorResponse("Username is required"));
            }
            if (request.getPassword() == null || request.getPassword().isEmpty()) {
                return ResponseEntity.badRequest().body(createErrorResponse("Password is required"));
            }

            // Find user
            Optional<User> userOpt = userService.findByUsername(request.getUsername().trim());
            if (!userOpt.isPresent()) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(createErrorResponse("Invalid credentials"));
            }

            User user = userOpt.get();

            // Simple password check (TODO: add proper hashing later)
            if (!request.getPassword().equals(user.getPasswordHash())) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(createErrorResponse("Invalid credentials"));
            }

            // Update last login
            user.setLastLoginAt(java.time.LocalDateTime.now());
            userService.save(user);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Login successful");
            response.put("user", createUserResponse(user));

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(createErrorResponse("Login failed: " + e.getMessage()));
        }
    }

    // Helper methods
    private Map<String, Object> createErrorResponse(String message) {
        Map<String, Object> response = new HashMap<>();
        response.put("success", false);
        response.put("error", message);
        return response;
    }

    private Map<String, Object> createUserResponse(User user) {
        Map<String, Object> userResponse = new HashMap<>();
        userResponse.put("id", user.getId());
        userResponse.put("username", user.getUsername());
        userResponse.put("email", user.getEmail());
        userResponse.put("displayName", user.getDisplayName());
        userResponse.put("level", user.getCurrentLevel());
        userResponse.put("experience", user.getExperiencePoints());
        userResponse.put("createdAt", user.getCreatedAt());
        userResponse.put("lastLoginAt", user.getLastLoginAt());
        return userResponse;
    }

    // Request DTOs
    public static class RegisterRequest {
        private String username;
        private String email;
        private String password;
        private String displayName;

        // Getters and setters
        public String getUsername() { return username; }
        public void setUsername(String username) { this.username = username; }

        public String getEmail() { return email; }
        public void setEmail(String email) { this.email = email; }

        public String getPassword() { return password; }
        public void setPassword(String password) { this.password = password; }

        public String getDisplayName() { return displayName; }
        public void setDisplayName(String displayName) { this.displayName = displayName; }
    }

    public static class LoginRequest {
        private String username;
        private String password;

        // Getters and setters
        public String getUsername() { return username; }
        public void setUsername(String username) { this.username = username; }

        public String getPassword() { return password; }
        public void setPassword(String password) { this.password = password; }
    }
}
