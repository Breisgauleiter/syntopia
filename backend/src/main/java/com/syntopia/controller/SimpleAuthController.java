package com.syntopia.controller;

import com.syntopia.model.User;
import com.syntopia.service.UserService;
import com.syntopia.config.JwtTokenUtil;
import com.syntopia.dto.ApiResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import com.syntopia.security.Roles;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

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
    public ResponseEntity<?> register(@Valid @RequestBody RegisterRequest request) {
        // Check if user already exists
        Optional<User> existingUser = userService.findByUsername(request.getUsername().trim());
        if (existingUser.isPresent()) {
            throw new IllegalArgumentException("Username already exists");
        }

        Optional<User> existingEmail = userService.findByEmail(request.getEmail().trim());
        if (existingEmail.isPresent()) {
            throw new IllegalArgumentException("Email already exists");
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

        Map<String,Object> data = new HashMap<>();
        data.put("user", createUserResponse(user));
        return ResponseEntity.ok(ApiResponse.success("User registered successfully", data));
    }

    /**
     * Login user (simplified)
     */
    @PostMapping("/login")
    public ResponseEntity<?> login(@Valid @RequestBody LoginRequest request) {
        // Validate input - accept either username or email
        String loginIdentifier = null;
        if (request.getUsername() != null && !request.getUsername().trim().isEmpty()) {
            loginIdentifier = request.getUsername().trim();
        } else if (request.getEmail() != null && !request.getEmail().trim().isEmpty()) {
            loginIdentifier = request.getEmail().trim();
        } else {
            throw new IllegalArgumentException("Username or email is required");
        }

        if (request.getPassword() == null || request.getPassword().isEmpty()) {
            throw new IllegalArgumentException("Password is required");
        }

        // Find user by username or email
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
            throw new SecurityException("Invalid credentials");
        }

        User user = userOpt.get();

        // Proper password verification using BCrypt
        boolean passwordMatches = passwordEncoder.matches(request.getPassword(), user.getPasswordHash());
        if (!passwordMatches) {
            throw new SecurityException("Invalid credentials");
        }

        // Update last login
        user.setLastLoginAt(java.time.LocalDateTime.now());
        userService.save(user);

        // Generate JWT tokens
        String accessToken = jwtTokenUtil.generateToken(user.getUsername());
        String refreshToken = jwtTokenUtil.generateRefreshToken(user.getUsername());

        Map<String,Object> data = new HashMap<>();
        data.put("user", createUserResponse(user));
        data.put("token", accessToken);
        data.put("refreshToken", refreshToken);
        return ResponseEntity.ok(ApiResponse.success("Login successful", data));
    }

    // Helper methods
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
        @NotBlank(message = "Username is required")
        private String username;
        @NotBlank(message = "Email is required")
        @Email(message = "Email must be valid")
        private String email;
        @NotBlank(message = "Password is required")
        @Size(min = 6, message = "Password must be at least 6 characters")
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
        @Email(message = "Email must be valid")
        private String email;
        @NotBlank(message = "Password is required")
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
     * Get current user information
     * @param authHeader Authorization header with JWT token
     * @return Current user data
     */
    @GetMapping("/me")
    public ResponseEntity<?> getCurrentUser(Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new SecurityException("Authentication required");
        }
        String username = authentication.getName();
        User user = userService.findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));
        return ResponseEntity.ok(ApiResponse.success(Map.of("user", createUserResponse(user))));
    }

    /**
     * Update user profile (selected role, display name, etc.)
     */
    @PutMapping("/profile")
    public ResponseEntity<?> updateProfile(@RequestBody UpdateProfileRequest request, Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new SecurityException("Authentication required");
        }
        String username = authentication.getName();
        User user = userService.findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));
        
        // Update fields if provided
        if (request.getSelectedRole() != null) {
            user.setSelectedRole(request.getSelectedRole());
        }
        if (request.getDisplayName() != null && !request.getDisplayName().trim().isEmpty()) {
            user.setDisplayName(request.getDisplayName().trim());
        }
        if (request.getAvatarUrl() != null) {
            user.setAvatarUrl(request.getAvatarUrl());
        }
        
        // Save updated user
        userService.save(user);
        
        // Return updated user data
        Map<String,Object> data = new HashMap<>();
    data.put("user", createUserResponse(user));
    return ResponseEntity.ok(ApiResponse.success("Profile updated successfully", data));
    }

    /**
     * Logout endpoint
     */
    @PostMapping("/logout")
    public ResponseEntity<?> logout() {
        return ResponseEntity.ok(ApiResponse.success("Logged out successfully"));
    }

    /**
     * Promote user to admin (Temporary setup endpoint)
     * WARNING: This should be removed in production!
     */
    @PostMapping("/promote-admin/{username}")
    @PreAuthorize("hasRole('" + Roles.ADMIN + "')")
    public ResponseEntity<?> promoteToAdmin(@PathVariable String username) {
        Optional<User> userOptional = userService.findByUsername(username);
        if (!userOptional.isPresent()) {
            throw new IllegalArgumentException("User not found");
        }
        
        User user = userOptional.get();
        userService.addRoleToUser(user.getId(), "ROLE_ADMIN");
        
        return ResponseEntity.ok(ApiResponse.success("User promoted to admin successfully", Map.of("username", username)));
    }

    // Inner classes for request/response objects
    
    /**
     * Update Profile Request DTO
     */
    public static class UpdateProfileRequest {
        private String selectedRole;
        private String displayName;
        private String avatarUrl;

        public String getSelectedRole() {
            return selectedRole;
        }

        public void setSelectedRole(String selectedRole) {
            this.selectedRole = selectedRole;
        }

        public String getDisplayName() {
            return displayName;
        }

        public void setDisplayName(String displayName) {
            this.displayName = displayName;
        }

        public String getAvatarUrl() {
            return avatarUrl;
        }

        public void setAvatarUrl(String avatarUrl) {
            this.avatarUrl = avatarUrl;
        }
    }
}
