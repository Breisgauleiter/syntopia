package com.syntopia.controller;

import com.syntopia.dto.ApiResponse;
import com.syntopia.model.User;
import com.syntopia.service.CommunityService;
import com.syntopia.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * CommunityController - REST API for Community Features
 * 
 * Leverages existing TAO architecture with user_collaborations and user_projects edges
 * for social interactions, connections, and collaborative projects.
 * 
 * Endpoints:
 * - GET /api/community/feed - Get community activity feed
 * - GET /api/community/users - Get user directory for connections
 * - POST /api/community/connect - Send connection request
 * - PUT /api/community/connect/{requestId} - Accept/decline connection request
 * - GET /api/community/connections - Get user's connections
 * - POST /api/community/collaborate - Invite user to collaborate on project
 * - GET /api/community/projects - Get community projects
 * - POST /api/community/projects - Create new community project
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
                return ResponseEntity.status(401).body(Map.of("error", "Authentication required"));
            }

            Optional<User> userOpt = userService.findByEmail(email);
            if (userOpt.isEmpty()) {
                return ResponseEntity.status(404).body(Map.of("error", "User not found"));
            }

            String levelFilter = minLevel != null ? minLevel.toString() : null;
            Map<String, Object> result = communityService.getUserDirectory(search, role, levelFilter, null, page, size);
            return ResponseEntity.ok(result);

        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("error", "Failed to retrieve user directory", "details", e.getMessage()));
        }
    }

    /**
     * Send connection request to another user
     */
    @PostMapping("/connect")
    public ResponseEntity<Map<String, Object>> sendConnectionRequest(
            @RequestBody Map<String, Object> requestData,
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

            User fromUser = userOpt.get();
            String toUserId = (String) requestData.get("toUserId");
            String connectionType = (String) requestData.get("connectionType"); // "collaboration", "mentorship", "learning_buddy", "professional", "social"
            String message = (String) requestData.get("message");

            if (toUserId == null) {
                return ResponseEntity.status(400).body(Map.of("error", "Target user ID is required"));
            }

            if (connectionType == null) {
                connectionType = "social"; // Default to social connection
            }

            Map<String, Object> result = communityService.sendConnectionRequest(fromUser.getId(), toUserId, connectionType, message);
            return ResponseEntity.ok(result);

        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("error", "Failed to send connection request", "details", e.getMessage()));
        }
    }

    /**
     * Accept or decline connection request
     */
    @PutMapping("/connect/{requestId}")
    public ResponseEntity<Map<String, Object>> respondToConnectionRequest(
            @PathVariable String requestId,
            @RequestBody Map<String, Object> responseData,
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

            String action = (String) responseData.get("action"); // "ACCEPT" or "DECLINE"

            if (action == null || (!action.equals("ACCEPT") && !action.equals("DECLINE"))) {
                return ResponseEntity.status(400).body(Map.of("error", "Invalid action. Must be ACCEPT or DECLINE"));
            }

            Map<String, Object> result = communityService.respondToConnectionRequest(requestId, action);
            return ResponseEntity.ok(result);

        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("error", "Failed to respond to connection request", "details", e.getMessage()));
        }
    }

    /**
     * Get user's connections and connection requests
     */
    @GetMapping("/connections")
    public ResponseEntity<Map<String, Object>> getConnections(HttpServletRequest request) {
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
            Map<String, Object> connections = communityService.getUserConnections(user.getId());
            return ResponseEntity.ok(connections);

        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("error", "Failed to retrieve connections", "details", e.getMessage()));
        }
    }

    /**
     * Invite user to collaborate on a project
     */
    @PostMapping("/collaborate")
    public ResponseEntity<Map<String, Object>> inviteCollaboration(
            @RequestBody Map<String, Object> inviteData,
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

            User fromUser = userOpt.get();
            String toUserId = (String) inviteData.get("toUserId");
            String projectId = (String) inviteData.get("projectId");
            String role = (String) inviteData.get("role");
            String message = (String) inviteData.get("message");

            if (toUserId == null || projectId == null) {
                return ResponseEntity.status(400).body(Map.of("error", "Target user ID and project ID are required"));
            }

            Map<String, Object> result = communityService.inviteCollaboration(fromUser.getId(), toUserId, projectId, role, message);
            return ResponseEntity.ok(result);

        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("error", "Failed to send collaboration invite", "details", e.getMessage()));
        }
    }

    /**
     * Get community projects
     */
    @GetMapping("/projects")
    public ResponseEntity<List<Map<String, Object>>> getCommunityProjects(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) String search,
            @RequestParam(required = false) String status) {
        try {
            Map<String, Object> result = communityService.getCommunityProjects(search, page, size);
            @SuppressWarnings("unchecked")
            List<Map<String, Object>> projects = (List<Map<String, Object>>) result.get("projects");
            return ResponseEntity.ok(projects);

        } catch (Exception e) {
            return ResponseEntity.status(500).body(List.of(Map.of("error", "Failed to retrieve community projects", "details", e.getMessage())));
        }
    }

    /**
     * Create new community project
     */
    @PostMapping("/projects")
    public ResponseEntity<Map<String, Object>> createCommunityProject(
            @RequestBody Map<String, Object> projectData,
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

            User creator = userOpt.get();
            
            String title = (String) projectData.get("title");
            String description = (String) projectData.get("description");
            @SuppressWarnings("unchecked")
            List<String> skillsNeeded = (List<String>) projectData.get("skillsNeeded");
            
            if (title == null || description == null) {
                return ResponseEntity.status(400).body(Map.of("error", "Title and description are required"));
            }
            
            if (skillsNeeded == null) {
                skillsNeeded = new ArrayList<>();
            }
            
            Map<String, Object> result = communityService.createCommunityProject(creator.getId(), title, skillsNeeded, description);
            return ResponseEntity.ok(result);

        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("error", "Failed to create community project", "details", e.getMessage()));
        }
    }

    /**
     * Get user activity statistics
     */
    @GetMapping("/stats")
    public ResponseEntity<Map<String, Object>> getCommunityStats(HttpServletRequest request) {
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
            Map<String, Object> stats = communityService.getCommunityStats(user.getId());
            return ResponseEntity.ok(stats);

        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("error", "Failed to retrieve community statistics", "details", e.getMessage()));
        }
    }

    /**
     * Get leaderboard data
     */
    @GetMapping("/leaderboard")
    public ResponseEntity<List<Map<String, Object>>> getLeaderboard(
            @RequestParam(defaultValue = "experience") String type,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "50") int size) {
        try {
            Map<String, Object> result = communityService.getLeaderboard(type, page, size);
            @SuppressWarnings("unchecked")
            List<Map<String, Object>> leaderboard = (List<Map<String, Object>>) result.get("leaderboard");
            return ResponseEntity.ok(leaderboard);

        } catch (Exception e) {
            return ResponseEntity.status(500).body(List.of(Map.of("error", "Failed to retrieve leaderboard", "details", e.getMessage())));
        }
    }
}
