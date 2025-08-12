package com.syntopia.controller;

import com.syntopia.dto.ApiResponse;
import com.syntopia.dto.CommunityDTO;
import com.syntopia.model.User;
import com.syntopia.exception.ResourceNotFoundException;
import com.syntopia.service.CommunityService;
import com.syntopia.service.UserService;
import jakarta.validation.Valid;
import org.springframework.security.core.Authentication;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * CommunityController - REST API for Community Features
 * 
 * Updated to use real ArangoDB-backed data with proper DTOs and validation
 */
@RestController
@RequestMapping("/api/community")
public class CommunityController {

    @Autowired
    private CommunityService communityService;

    @Autowired
    private UserService userService;

    private User requireUser(Authentication authentication) {
        String username = authentication.getName();
        return userService.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
    }

    /**
     * Get community activity feed
     */
    @GetMapping("/feed")
    public ResponseEntity<Map<String, Object>> getCommunityFeed(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            Authentication authentication) {
        User user = requireUser(authentication);
        Map<String, Object> feed = communityService.getCommunityFeed(user.getId(), page, size);
        return ResponseEntity.ok(ApiResponse.success(feed));
    }

    /**
     * Get user directory for discovering connections
     */
    @GetMapping("/users")
    public ResponseEntity<Map<String, Object>> getUserDirectory(
            @RequestParam(required = false) String search,
            @RequestParam(required = false) String role,
            @RequestParam(required = false) Integer minLevel,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            Authentication authentication) {
        String levelFilter = minLevel != null ? minLevel.toString() : null;
        Map<String, Object> result = communityService.getUserDirectory(search, role, levelFilter, null, page, size);
        return ResponseEntity.ok(ApiResponse.success(result));
    }

    /**
     * Send connection request to another user
     */
    @PostMapping("/connect")
    public ResponseEntity<Map<String, Object>> sendConnectionRequest(
            @Valid @RequestBody CommunityDTO.ConnectionRequestDTO requestData,
            Authentication authentication) {
    User fromUser = requireUser(authentication);

        Map<String, Object> result = communityService.sendConnectionRequest(
            fromUser.getId(),
            requestData.getToUserId(),
            requestData.getConnectionType()
        );
        return ResponseEntity.ok(ApiResponse.success(result));
    }

    /**
     * Accept or decline connection request
     */
    @PutMapping("/connect/{connectionId}")
    public ResponseEntity<Map<String, Object>> respondToConnectionRequest(
            @PathVariable String connectionId,
            @Valid @RequestBody CommunityDTO.ConnectionResponseDTO responseData,
            Authentication authentication) {
        User user = requireUser(authentication);
        Map<String, Object> result = communityService.respondToConnectionRequest(
            connectionId,
            responseData.getAction(),
            user.getId()
        );
        return ResponseEntity.ok(ApiResponse.success(result));
    }

    /**
     * Cancel an outgoing pending connection request
     */
    @DeleteMapping("/connect/{connectionId}")
    public ResponseEntity<Map<String, Object>> cancelPendingConnection(
            @PathVariable String connectionId,
            Authentication authentication) {
        User user = requireUser(authentication);
        Map<String, Object> result = communityService.cancelPendingConnection(connectionId, user.getId());
        return ResponseEntity.ok(ApiResponse.success(result));
    }

    /**
     * Get user's connections
     */
    @GetMapping("/connections")
    public ResponseEntity<Map<String, Object>> getConnections(
            @RequestParam(required = false) String status,
            @RequestParam(defaultValue = "all") String direction,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            Authentication authentication) {
    User user = requireUser(authentication);
        Map<String, Object> connections = communityService.getUserConnections(user.getId(), status, direction, page, size);
        return ResponseEntity.ok(ApiResponse.success(connections));
    }

    /**
     * Get community projects
     */
    @GetMapping("/projects")
    public ResponseEntity<Map<String, Object>> getCommunityProjects(
            @RequestParam(defaultValue = "false") boolean mine,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            Authentication authentication) {
    User user = requireUser(authentication);
        Map<String, Object> result = communityService.getCommunityProjects(user.getId(), mine, page, size);
        return ResponseEntity.ok(ApiResponse.success(result));
    }

    /**
     * Create new community project
     */
    @PostMapping("/projects")
    public ResponseEntity<Map<String, Object>> createCommunityProject(
            @Valid @RequestBody CommunityDTO.CreateProjectDTO projectData,
            Authentication authentication) {
    User creator = requireUser(authentication);
        Map<String, Object> result = communityService.createCommunityProject(creator.getId(), projectData);
        return ResponseEntity.ok(ApiResponse.success(result));
    }

    /**
     * Get community statistics
     */
    @GetMapping("/stats")
    public ResponseEntity<Map<String, Object>> getCommunityStats(Authentication authentication) {
        Map<String, Object> stats = communityService.getCommunityStats();
        return ResponseEntity.ok(ApiResponse.success(stats));
    }

    /**
     * Get leaderboard data
     */
    @GetMapping("/leaderboard")
    public ResponseEntity<Map<String, Object>> getLeaderboard(
            @RequestParam(defaultValue = "all") String window,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        Map<String, Object> result = communityService.getCommunityLeaderboard(window, page, size);
        return ResponseEntity.ok(ApiResponse.success(result));
    }
}
