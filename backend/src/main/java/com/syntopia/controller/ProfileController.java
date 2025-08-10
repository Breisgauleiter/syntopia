package com.syntopia.controller;

import com.syntopia.dto.ApiResponse;
import com.syntopia.model.User;
import com.syntopia.service.ProfileService;
import com.syntopia.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * ProfileController - REST API for Profile Management
 * 
 * Leverages existing TAO architecture with user_collaborations and user_projects edges
 * for comprehensive profile and social relationship management.
 * 
 * Endpoints:
 * - GET /api/profile - Get current user's profile
 * - PUT /api/profile - Update profile information
 * - POST /api/profile/avatar - Upload profile avatar
 * - GET /api/profile/achievements - Get user achievements
 * - GET /api/profile/social - Get social connections
 * - GET /api/profile/{userId} - Get public profile by ID
 */
@RestController
@RequestMapping("/api/profile")
public class ProfileController {

    @Autowired
    private ProfileService profileService;

    @Autowired
    private UserService userService;

    /**
     * Get current user's complete profile
     */
    @GetMapping
    public ResponseEntity<Map<String, Object>> getCurrentProfile(HttpServletRequest request) {
        try {
            String email = (String) request.getAttribute("email");
            if (email == null) {
                return ResponseEntity.status(401)
                    .body(ApiResponse.error("Authentication required"));
            }

            Optional<User> userOpt = userService.findByEmail(email);
            if (userOpt.isEmpty()) {
                return ResponseEntity.status(404)
                    .body(ApiResponse.error("User not found"));
            }

            User user = userOpt.get();
            Map<String, Object> profileData = profileService.getCompleteProfile(user.getId());
            
            return ResponseEntity.ok(ApiResponse.success(profileData));

        } catch (Exception e) {
            return ResponseEntity.status(500)
                .body(ApiResponse.error("Failed to retrieve profile", e.getMessage()));
        }
    }

    /**
     * Update user profile information
     */
    @PutMapping
    public ResponseEntity<Map<String, Object>> updateProfile(
            @RequestBody Map<String, Object> profileData, 
            HttpServletRequest request) {
        try {
            String email = (String) request.getAttribute("email");
            if (email == null) {
                return ResponseEntity.status(401)
                    .body(ApiResponse.error("Authentication required"));
            }

            Optional<User> userOpt = userService.findByEmail(email);
            if (userOpt.isEmpty()) {
                return ResponseEntity.status(404)
                    .body(ApiResponse.error("User not found"));
            }

            User user = userOpt.get();
            // Update profile fields
            Map<String, Object> updateResult = profileService.updateProfile(user.getId(), profileData);
            
            return ResponseEntity.ok(ApiResponse.success(
                "Profile updated successfully", 
                updateResult.get("user")
            ));

        } catch (Exception e) {
            return ResponseEntity.status(500)
                .body(ApiResponse.error("Failed to update profile", e.getMessage()));
        }
    }

    /**
     * Upload profile avatar
     */
    @PostMapping("/avatar")
    public ResponseEntity<Map<String, Object>> uploadAvatar(
            @RequestParam("avatar") MultipartFile file,
            HttpServletRequest request) {
        try {
            String email = (String) request.getAttribute("email");
            if (email == null) {
                return ResponseEntity.status(401).body(Map.of("error", "Authentication required"));
            }

            Optional<User> userOpt = userService.findByEmail(email);
            if (userOpt.isEmpty()) {
                return ResponseEntity.status(404).body(Map.of("error", "User not found"));
            }

            User user = userOpt.get();
            // Validate file
            if (file.isEmpty()) {
                return ResponseEntity.status(400).body(Map.of("error", "No file provided"));
            }

            Map<String, Object> uploadResult = profileService.uploadAvatar(user.getId(), file);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Avatar uploaded successfully");
            response.put("avatarUrl", uploadResult.get("avatarUrl"));
            
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("error", "Failed to upload avatar", "details", e.getMessage()));
        }
    }

    /**
     * Get user achievements and badges
     */
    @GetMapping("/achievements")
    public ResponseEntity<Map<String, Object>> getAchievements(HttpServletRequest request) {
        try {
            String email = (String) request.getAttribute("email");
            if (email == null) {
                return ResponseEntity.status(401).body(Map.of("error", "Authentication required"));
            }

            Optional<User> userOpt = userService.findByEmail(email);
            if (userOpt.isEmpty()) {
                return ResponseEntity.status(404).body(Map.of("error", "User not found"));
            }

            User user = userOpt.get();
            Map<String, Object> achievements = profileService.getUserAchievements(user.getId());
            return ResponseEntity.ok(achievements);

        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("error", "Failed to retrieve achievements", "details", e.getMessage()));
        }
    }

    /**
     * Get social connections using TAO user_collaborations edges
     */
    @GetMapping("/social")
    public ResponseEntity<Map<String, Object>> getSocialConnections(HttpServletRequest request) {
        try {
            String email = (String) request.getAttribute("email");
            if (email == null) {
                return ResponseEntity.status(401).body(Map.of("error", "Authentication required"));
            }

            Optional<User> userOpt = userService.findByEmail(email);
            if (userOpt.isEmpty()) {
                return ResponseEntity.status(404).body(Map.of("error", "User not found"));
            }

            User user = userOpt.get();
            Map<String, Object> socialData = profileService.getSocialConnections(user.getId());
            return ResponseEntity.ok(socialData);

        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("error", "Failed to retrieve social connections", "details", e.getMessage()));
        }
    }

    /**
     * Get public profile by user ID
     */
    @GetMapping("/{userId}")
    public ResponseEntity<Map<String, Object>> getPublicProfile(@PathVariable String userId) {
        try {
            Map<String, Object> publicProfile = profileService.getPublicProfile(userId);
            
            if (publicProfile == null) {
                return ResponseEntity.status(404).body(Map.of("error", "User not found"));
            }
            
            return ResponseEntity.ok(publicProfile);

        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("error", "Failed to retrieve public profile", "details", e.getMessage()));
        }
    }

    /**
     * Get profile completion status
     */
    @GetMapping("/completion")
    public ResponseEntity<Map<String, Object>> getProfileCompletion(HttpServletRequest request) {
        try {
            String email = (String) request.getAttribute("email");
            if (email == null) {
                return ResponseEntity.status(401).body(Map.of("error", "Authentication required"));
            }

            Optional<User> userOpt = userService.findByEmail(email);
            if (userOpt.isEmpty()) {
                return ResponseEntity.status(404).body(Map.of("error", "User not found"));
            }

            User user = userOpt.get();
            Map<String, Object> completion = profileService.getProfileCompletion(user.getId());
            return ResponseEntity.ok(completion);

        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("error", "Failed to check profile completion", "details", e.getMessage()));
        }
    }

    /**
     * Search users for connections/collaboration
     */
    @GetMapping("/search")
    public ResponseEntity<Map<String, Object>> searchUsers(
            @RequestParam(required = false) String query,
            @RequestParam(required = false) String role,
            @RequestParam(required = false) Integer minLevel,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        try {
            Map<String, Object> searchResults = profileService.searchUsers(query, role, minLevel, page, size);
            return ResponseEntity.ok(searchResults);

        } catch (Exception e) {
            return ResponseEntity.status(500)
                .body(ApiResponse.error("Search failed", e.getMessage()));
        }
    }
}
