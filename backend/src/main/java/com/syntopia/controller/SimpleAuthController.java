package com.syntopia.controller;

import com.syntopia.model.User;
import com.syntopia.service.UserService;
import com.syntopia.config.JwtTokenUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
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

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtTokenUtil jwtTokenUtil;

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

            // Hash password with BCrypt
            String hashedPassword = passwordEncoder.encode(request.getPassword());
            user.setPasswordHash(hashedPassword);
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
            // Validate input - accept either username or email
            String loginIdentifier = null;
            if (request.getUsername() != null && !request.getUsername().trim().isEmpty()) {
                loginIdentifier = request.getUsername().trim();
            } else if (request.getEmail() != null && !request.getEmail().trim().isEmpty()) {
                loginIdentifier = request.getEmail().trim();
            } else {
                return ResponseEntity.badRequest().body(createErrorResponse("Username or email is required"));
            }
            
            if (request.getPassword() == null || request.getPassword().isEmpty()) {
                return ResponseEntity.badRequest().body(createErrorResponse("Password is required"));
            }

            // Find user by username or email
            System.out.println("Login attempt for: " + loginIdentifier);
            Optional<User> userOpt;
            if (loginIdentifier.contains("@")) {
                // Looks like email
                userOpt = userService.findByEmail(loginIdentifier);
            } else {
                // Looks like username
                userOpt = userService.findByUsername(loginIdentifier);
            }
            
            // If not found with first method, try the other method
            if (!userOpt.isPresent()) {
                if (loginIdentifier.contains("@")) {
                    userOpt = userService.findByUsername(loginIdentifier);
                } else {
                    userOpt = userService.findByEmail(loginIdentifier);
                }
            }
            
            if (!userOpt.isPresent()) {
                System.out.println("User not found: " + loginIdentifier);
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(createErrorResponse("Invalid credentials"));
            }

            User user = userOpt.get();
            System.out.println("User found: " + user.getUsername() + ", password hash: " + user.getPasswordHash());

            // Proper password verification using BCrypt
            boolean passwordMatches = passwordEncoder.matches(request.getPassword(), user.getPasswordHash());
            System.out.println("Password matches: " + passwordMatches);
            if (!passwordMatches) {
                System.out.println("Password mismatch for user: " + user.getUsername());
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(createErrorResponse("Invalid credentials"));
            }

            // Update last login
            user.setLastLoginAt(java.time.LocalDateTime.now());
            userService.save(user);

            // Generate JWT tokens
            String accessToken = jwtTokenUtil.generateToken(user.getUsername());
            String refreshToken = jwtTokenUtil.generateRefreshToken(user.getUsername());

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Login successful");
            response.put("user", createUserResponse(user));
            response.put("token", accessToken);
            response.put("refreshToken", refreshToken);

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
        userResponse.put("selectedRole", user.getSelectedRole());
        userResponse.put("currentLevel", user.getCurrentLevel());
        userResponse.put("experiencePoints", user.getExperiencePoints());
        userResponse.put("questsCompleted", user.getQuestsCompleted());
        userResponse.put("isGitHubIntegrated", user.isGitHubIntegrated());
        userResponse.put("avatarUrl", user.getAvatarUrl());
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
        private String email;
        private String password;

        // Getters and setters
        public String getUsername() { return username; }
        public void setUsername(String username) { this.username = username; }

        public String getEmail() { return email; }
        public void setEmail(String email) { this.email = email; }

        public String getPassword() { return password; }
        public void setPassword(String password) { this.password = password; }
    }

    /**
     * Get current user info
     */
    @GetMapping("/me")
    public ResponseEntity<?> getCurrentUser(@RequestHeader("Authorization") String authHeader) {
        try {
            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(createErrorResponse("No valid authorization token provided"));
            }

            String token = authHeader.substring(7);
            // Here you would validate the token and extract user info
            // For now, return a simple response
            Map<String, Object> response = new HashMap<>();
            response.put("message", "User info endpoint");
            response.put("token", "received");
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(createErrorResponse("Error retrieving user information"));
        }
    }

    /**
     * Logout endpoint
     */
    @PostMapping("/logout")
    public ResponseEntity<?> logout() {
        try {
            Map<String, Object> response = new HashMap<>();
            response.put("message", "Logged out successfully");
            response.put("success", true);
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(createErrorResponse("Error during logout"));
        }
    }
}
