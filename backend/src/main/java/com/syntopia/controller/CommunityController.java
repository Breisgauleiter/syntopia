package com.syntopia.controller;

import com.syntopia.dto.ApiResponse;
import com.syntopia.dto.CommunityDTO;
import com.syntopia.model.User;
import com.syntopia.service.CommunityService;
import com.syntopia.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.Optional;

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

    /**
     * Get community activity feed
     */
    @GetMapping("/feed")
    public ResponseEntity<Map<String, Object>> getCommunityFeed(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
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
            Map<String, Object> feed = communityService.getCommunityFeed(user.getId(), page, size);
            
            return ResponseEntity.ok(ApiResponse.success(feed));

        } catch (Exception e) {
            return ResponseEntity.status(500)
                .body(ApiResponse.error("Failed to retrieve community feed", e.getMessage()));
        }
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

            String levelFilter = minLevel != null ? minLevel.toString() : null;
            Map<String, Object> result = communityService.getUserDirectory(search, role, levelFilter, null, page, size);
            
            return ResponseEntity.ok(ApiResponse.success(result));

        } catch (Exception e) {
            return ResponseEntity.status(500)
                .body(ApiResponse.error("Failed to retrieve user directory", e.getMessage()));
        }
    }

    /**
     * Send connection request to another user
     */
    @PostMapping("/connect")
    public ResponseEntity<Map<String, Object>> sendConnectionRequest(
            @RequestBody CommunityDTO.ConnectionRequestDTO requestData,
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

            User fromUser = userOpt.get();
            
            if (requestData.getToUserId() == null) {
                return ResponseEntity.status(400)
                    .body(ApiResponse.error("Target user ID is required"));
            }
            
            if (requestData.getConnectionType() == null) {
                return ResponseEntity.status(400)
                    .body(ApiResponse.error("Connection type is required"));
            }

            Map<String, Object> result = communityService.sendConnectionRequest(
                fromUser.getId(), 
                requestData.getToUserId(), 
                requestData.getConnectionType()
            );
            
            return ResponseEntity.ok(ApiResponse.success(result));

        } catch (RuntimeException e) {
            if (e.getMessage().contains("already exists")) {
                return ResponseEntity.status(409)
                    .body(ApiResponse.error(e.getMessage()));
            } else if (e.getMessage().contains("not found")) {
                return ResponseEntity.status(404)
                    .body(ApiResponse.error(e.getMessage()));
            } else {
                return ResponseEntity.status(400)
                    .body(ApiResponse.error(e.getMessage()));
            }
        } catch (Exception e) {
            return ResponseEntity.status(500)
                .body(ApiResponse.error("Failed to send connection request", e.getMessage()));
        }
    }

    /**
     * Accept or decline connection request
     */
    @PutMapping("/connect/{connectionId}")
    public ResponseEntity<Map<String, Object>> respondToConnectionRequest(
            @PathVariable String connectionId,
            @RequestBody CommunityDTO.ConnectionResponseDTO responseData,
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

            if (responseData.getAction() == null || 
                (!"accept".equals(responseData.getAction()) && !"decline".equals(responseData.getAction()))) {
                return ResponseEntity.status(400)
                    .body(ApiResponse.error("Invalid action. Must be 'accept' or 'decline'"));
            }

            Map<String, Object> result = communityService.respondToConnectionRequest(
                connectionId, 
                responseData.getAction(), 
                userOpt.get().getId()
            );
            
            return ResponseEntity.ok(ApiResponse.success(result));

        } catch (RuntimeException e) {
            if (e.getMessage().contains("not found")) {
                return ResponseEntity.status(404)
                    .body(ApiResponse.error(e.getMessage()));
            } else if (e.getMessage().contains("unauthorized")) {
                return ResponseEntity.status(403)
                    .body(ApiResponse.error(e.getMessage()));
            } else {
                return ResponseEntity.status(400)
                    .body(ApiResponse.error(e.getMessage()));
            }
        } catch (Exception e) {
            return ResponseEntity.status(500)
                .body(ApiResponse.error("Failed to respond to connection request", e.getMessage()));
        }
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
            Map<String, Object> connections = communityService.getUserConnections(user.getId(), status, direction, page, size);
            
            return ResponseEntity.ok(ApiResponse.success(connections));

        } catch (Exception e) {
            return ResponseEntity.status(500)
                .body(ApiResponse.error("Failed to retrieve connections", e.getMessage()));
        }
    }

    /**
     * Get community projects
     */
    @GetMapping("/projects")
    public ResponseEntity<Map<String, Object>> getCommunityProjects(
            @RequestParam(defaultValue = "false") boolean mine,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
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
            Map<String, Object> result = communityService.getCommunityProjects(user.getId(), mine, page, size);
            
            return ResponseEntity.ok(ApiResponse.success(result));

        } catch (Exception e) {
            return ResponseEntity.status(500)
                .body(ApiResponse.error("Failed to retrieve community projects", e.getMessage()));
        }
    }

    /**
     * Create new community project
     */
    @PostMapping("/projects")
    public ResponseEntity<Map<String, Object>> createCommunityProject(
            @RequestBody CommunityDTO.CreateProjectDTO projectData,
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

            User creator = userOpt.get();
            
            if (projectData.getTitle() == null || projectData.getTitle().trim().isEmpty()) {
                return ResponseEntity.status(400)
                    .body(ApiResponse.error("Project title is required"));
            }
            
            if (projectData.getDescription() == null || projectData.getDescription().trim().isEmpty()) {
                return ResponseEntity.status(400)
                    .body(ApiResponse.error("Project description is required"));
            }
            
            Map<String, Object> result = communityService.createCommunityProject(creator.getId(), projectData);
            
            return ResponseEntity.ok(ApiResponse.success(result));

        } catch (Exception e) {
            return ResponseEntity.status(500)
                .body(ApiResponse.error("Failed to create community project", e.getMessage()));
        }
    }

    /**
     * Get community statistics
     */
    @GetMapping("/stats")
    public ResponseEntity<Map<String, Object>> getCommunityStats(HttpServletRequest request) {
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

            Map<String, Object> stats = communityService.getCommunityStats();
            
            return ResponseEntity.ok(ApiResponse.success(stats));

        } catch (Exception e) {
            return ResponseEntity.status(500)
                .body(ApiResponse.error("Failed to retrieve community statistics", e.getMessage()));
        }
    }

    /**
     * Get leaderboard data
     */
    @GetMapping("/leaderboard")
    public ResponseEntity<Map<String, Object>> getLeaderboard(
            @RequestParam(defaultValue = "all") String window,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        try {
            Map<String, Object> result = communityService.getCommunityLeaderboard(window, page, size);
            
            return ResponseEntity.ok(ApiResponse.success(result));

        } catch (Exception e) {
            return ResponseEntity.status(500)
                .body(ApiResponse.error("Failed to retrieve leaderboard", e.getMessage()));
        }
    }
}
