package com.syntopia.controller;

import com.syntopia.dto.ApiResponse;
import com.syntopia.model.User;
import com.syntopia.service.ProfileService;
import com.syntopia.service.UserService;
import org.springframework.security.core.Authentication;
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
    public ResponseEntity<Map<String, Object>> getCurrentProfile(Authentication authentication) {
        String username = authentication.getName();
        Optional<User> userOpt = userService.findByUsername(username);
        if (userOpt.isEmpty()) {
            throw new IllegalArgumentException("User not found");
        }

        User user = userOpt.get();
        Map<String, Object> profileData = profileService.getCompleteProfile(user.getId());
        return ResponseEntity.ok(ApiResponse.success(profileData));
    }

    /**
     * Update user profile information
     */
    @PutMapping
    public ResponseEntity<Map<String, Object>> updateProfile(
            @RequestBody Map<String, Object> profileData,
            Authentication authentication) {
        String username = authentication.getName();
        Optional<User> userOpt = userService.findByUsername(username);
        if (userOpt.isEmpty()) {
            throw new IllegalArgumentException("User not found");
        }

        User user = userOpt.get();
        Map<String, Object> updateResult = profileService.updateProfile(user.getId(), profileData);
        return ResponseEntity.ok(ApiResponse.success(
            "Profile updated successfully",
            updateResult.get("user")
        ));
    }

    /**
     * Upload profile avatar
     */
    @PostMapping("/avatar")
    public ResponseEntity<Map<String, Object>> uploadAvatar(
            @RequestParam("avatar") MultipartFile file,
            Authentication authentication) {
        String username = authentication.getName();
        Optional<User> userOpt = userService.findByUsername(username);
        if (userOpt.isEmpty()) {
            throw new IllegalArgumentException("User not found");
        }

        if (file.isEmpty()) {
            throw new IllegalArgumentException("No file provided");
        }

        User user = userOpt.get();
        Map<String, Object> uploadResult = profileService.uploadAvatar(user.getId(), file);

        Map<String,Object> payload = new HashMap<>();
        payload.put("avatarUrl", uploadResult.get("avatarUrl"));
        return ResponseEntity.ok(ApiResponse.success("Avatar uploaded successfully", payload));
    }

    /**
     * Get user achievements and badges
     */
    @GetMapping("/achievements")
    public ResponseEntity<Map<String, Object>> getAchievements(Authentication authentication) {
        String username = authentication.getName();
        Optional<User> userOpt = userService.findByUsername(username);
        if (userOpt.isEmpty()) {
            throw new IllegalArgumentException("User not found");
        }

        User user = userOpt.get();
        Map<String, Object> achievements = profileService.getUserAchievements(user.getId());
        return ResponseEntity.ok(ApiResponse.success(achievements));
    }

    /**
     * Get social connections using TAO user_collaborations edges
     */
    @GetMapping("/social")
    public ResponseEntity<Map<String, Object>> getSocialConnections(Authentication authentication) {
        String username = authentication.getName();
        Optional<User> userOpt = userService.findByUsername(username);
        if (userOpt.isEmpty()) {
            throw new IllegalArgumentException("User not found");
        }

        User user = userOpt.get();
        Map<String, Object> socialData = profileService.getSocialConnections(user.getId());
        return ResponseEntity.ok(ApiResponse.success(socialData));
    }

    /**
     * Get public profile by user ID
     */
    @GetMapping("/{userId}")
    public ResponseEntity<Map<String, Object>> getPublicProfile(@PathVariable String userId) {
        Map<String, Object> publicProfile = profileService.getPublicProfile(userId);
        if (publicProfile == null) {
            throw new IllegalArgumentException("User not found");
        }
        return ResponseEntity.ok(ApiResponse.success(publicProfile));
    }

    /**
     * Get profile completion status
     */
    @GetMapping("/completion")
    public ResponseEntity<Map<String, Object>> getProfileCompletion(Authentication authentication) {
        String username = authentication.getName();
        Optional<User> userOpt = userService.findByUsername(username);
        if (userOpt.isEmpty()) {
            throw new IllegalArgumentException("User not found");
        }

        User user = userOpt.get();
        Map<String, Object> completion = profileService.getProfileCompletion(user.getId());
        return ResponseEntity.ok(ApiResponse.success(completion));
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
        Map<String, Object> searchResults = profileService.searchUsers(query, role, minLevel, page, size);
        return ResponseEntity.ok(ApiResponse.success(searchResults));
    }
}
