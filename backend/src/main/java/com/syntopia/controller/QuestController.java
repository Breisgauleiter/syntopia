package com.syntopia.controller;

import com.syntopia.model.Quest;
import com.syntopia.model.User;
import com.syntopia.service.QuestService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Quest Controller for Syntopia Platform
 * 
 * REST API endpoints for quest management, progression, and user interactions
 * in the gamified Sacred Geometry system.
 */
@RestController
@RequestMapping("/api/quests")
@CrossOrigin(origins = {"http://localhost:5173", "http://localhost:3000"})
public class QuestController {

    @Autowired
    private QuestService questService;

    // ===============================
    // Quest CRUD Endpoints
    // ===============================

    /**
     * Get all quests
     */
    @GetMapping
    public ResponseEntity<Map<String, Object>> getAllQuests() {
        try {
            List<Quest> quests = questService.getAllQuests();
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("quests", quests);
            response.put("count", quests.size());
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(createErrorResponse("Failed to retrieve quests: " + e.getMessage()));
        }
    }

    /**
     * Get quest by ID
     */
    @GetMapping("/{questId}")
    public ResponseEntity<Map<String, Object>> getQuestById(@PathVariable String questId) {
        try {
            Optional<Quest> questOpt = questService.getQuestById(questId);
            
            if (questOpt.isPresent()) {
                Map<String, Object> response = new HashMap<>();
                response.put("success", true);
                response.put("quest", questOpt.get());
                
                return ResponseEntity.ok(response);
            } else {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(createErrorResponse("Quest not found"));
            }
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(createErrorResponse("Failed to retrieve quest: " + e.getMessage()));
        }
    }

    /**
     * Create a new quest (Admin only - future implementation)
     */
    @PostMapping
    public ResponseEntity<Map<String, Object>> createQuest(@RequestBody Quest quest) {
        try {
            Quest createdQuest = questService.createQuest(quest);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Quest created successfully");
            response.put("quest", createdQuest);
            
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(createErrorResponse("Failed to create quest: " + e.getMessage()));
        }
    }

    /**
     * Update an existing quest (Admin only - future implementation)
     */
    @PutMapping("/{questId}")
    public ResponseEntity<Map<String, Object>> updateQuest(@PathVariable String questId, 
                                                          @RequestBody Quest questUpdates) {
        try {
            Quest updatedQuest = questService.updateQuest(questId, questUpdates);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Quest updated successfully");
            response.put("quest", updatedQuest);
            
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(createErrorResponse(e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(createErrorResponse("Failed to update quest: " + e.getMessage()));
        }
    }

    /**
     * Delete a quest (Admin only - future implementation)
     */
    @DeleteMapping("/{questId}")
    public ResponseEntity<Map<String, Object>> deleteQuest(@PathVariable String questId) {
        try {
            questService.deleteQuest(questId);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Quest deleted successfully");
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(createErrorResponse("Failed to delete quest: " + e.getMessage()));
        }
    }

    // ===============================
    // Quest Discovery Endpoints
    // ===============================

    /**
     * Get available quests for a specific user
     */
    @GetMapping("/available/{userId}")
    public ResponseEntity<Map<String, Object>> getAvailableQuests(@PathVariable String userId) {
        try {
            List<Quest> availableQuests = questService.getAvailableQuestsForUser(userId);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("quests", availableQuests);
            response.put("count", availableQuests.size());
            
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(createErrorResponse(e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(createErrorResponse("Failed to retrieve available quests: " + e.getMessage()));
        }
    }

    /**
     * Get quests by role
     */
    @GetMapping("/role/{role}")
    public ResponseEntity<Map<String, Object>> getQuestsByRole(@PathVariable String role) {
        try {
            List<Quest> roleQuests = questService.getQuestsByRole(role);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("quests", roleQuests);
            response.put("count", roleQuests.size());
            response.put("role", role);
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(createErrorResponse("Failed to retrieve quests by role: " + e.getMessage()));
        }
    }

    /**
     * Get quests by type
     */
    @GetMapping("/type/{type}")
    public ResponseEntity<Map<String, Object>> getQuestsByType(@PathVariable Quest.QuestType type) {
        try {
            List<Quest> typeQuests = questService.getQuestsByType(type);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("quests", typeQuests);
            response.put("count", typeQuests.size());
            response.put("type", type);
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(createErrorResponse("Failed to retrieve quests by type: " + e.getMessage()));
        }
    }

    /**
     * Get quests by difficulty
     */
    @GetMapping("/difficulty/{difficulty}")
    public ResponseEntity<Map<String, Object>> getQuestsByDifficulty(@PathVariable Quest.QuestDifficulty difficulty) {
        try {
            List<Quest> difficultyQuests = questService.getQuestsByDifficulty(difficulty);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("quests", difficultyQuests);
            response.put("count", difficultyQuests.size());
            response.put("difficulty", difficulty);
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(createErrorResponse("Failed to retrieve quests by difficulty: " + e.getMessage()));
        }
    }

    /**
     * Get GitHub integration quests
     */
    @GetMapping("/github")
    public ResponseEntity<Map<String, Object>> getGitHubQuests() {
        try {
            List<Quest> githubQuests = questService.getGitHubQuests();
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("quests", githubQuests);
            response.put("count", githubQuests.size());
            response.put("type", "GitHub Integration");
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(createErrorResponse("Failed to retrieve GitHub quests: " + e.getMessage()));
        }
    }

    /**
     * Get quests by geometry pattern
     */
    @GetMapping("/pattern/{pattern}")
    public ResponseEntity<Map<String, Object>> getQuestsByGeometryPattern(@PathVariable String pattern) {
        try {
            List<Quest> patternQuests = questService.getQuestsByGeometryPattern(pattern);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("quests", patternQuests);
            response.put("count", patternQuests.size());
            response.put("pattern", pattern);
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(createErrorResponse("Failed to retrieve quests by pattern: " + e.getMessage()));
        }
    }

    // ===============================
    // Quest Progression Endpoints
    // ===============================

    /**
     * Accept/Start a quest
     */
    @PostMapping("/{questId}/accept")
    public ResponseEntity<Map<String, Object>> acceptQuest(@PathVariable String questId, 
                                                          @RequestParam String userId) {
        try {
            User user = questService.acceptQuest(userId, questId);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Quest accepted successfully");
            response.put("user", createUserResponse(user));
            
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(createErrorResponse(e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(createErrorResponse("Failed to accept quest: " + e.getMessage()));
        }
    }

    /**
     * Complete a quest
     */
    @PostMapping("/{questId}/complete")
    public ResponseEntity<Map<String, Object>> completeQuest(@PathVariable String questId, 
                                                            @RequestParam String userId) {
        try {
            User user = questService.completeQuest(userId, questId);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Quest completed successfully! Experience points awarded.");
            response.put("user", createUserResponse(user));
            
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(createErrorResponse(e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(createErrorResponse("Failed to complete quest: " + e.getMessage()));
        }
    }

    /**
     * Abandon a quest
     */
    @PostMapping("/{questId}/abandon")
    public ResponseEntity<Map<String, Object>> abandonQuest(@PathVariable String questId, 
                                                           @RequestParam String userId) {
        try {
            User user = questService.abandonQuest(userId, questId);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Quest abandoned. You can re-accept it later.");
            response.put("user", createUserResponse(user));
            
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(createErrorResponse(e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(createErrorResponse("Failed to abandon quest: " + e.getMessage()));
        }
    }

    // ===============================
    // Quest Analytics Endpoints
    // ===============================

    /**
     * Get quest statistics
     */
    @GetMapping("/stats")
    public ResponseEntity<Map<String, Object>> getQuestStatistics() {
        try {
            Map<String, Object> stats = new HashMap<>();
            stats.put("total", questService.getAllQuests().size());
            stats.put("available", questService.getAvailableQuestCount());
            stats.put("active", questService.getActiveQuestCount());
            stats.put("completed", questService.getCompletedQuestCount());
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("statistics", stats);
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(createErrorResponse("Failed to retrieve quest statistics: " + e.getMessage()));
        }
    }

    // ===============================
    // GitHub Integration Endpoints
    // ===============================

    /**
     * Create quest from GitHub issue (Admin/System use)
     */
    @PostMapping("/github")
    public ResponseEntity<Map<String, Object>> createGitHubQuest(@RequestBody GitHubQuestRequest request) {
        try {
            Quest githubQuest = questService.createGitHubQuest(
                    request.getTitle(),
                    request.getDescription(),
                    request.getGithubIssueUrl(),
                    request.getRepository(),
                    request.getIssueNumber(),
                    request.getRequiredLevel()
            );
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "GitHub quest created successfully");
            response.put("quest", githubQuest);
            
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(createErrorResponse("Failed to create GitHub quest: " + e.getMessage()));
        }
    }

    // ===============================
    // Helper Methods
    // ===============================

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
        userResponse.put("currentLevel", user.getCurrentLevel());
        userResponse.put("experiencePoints", user.getExperiencePoints());
        userResponse.put("questsCompleted", user.getQuestsCompleted());
        userResponse.put("selectedRole", user.getSelectedRole());
        return userResponse;
    }

    // ===============================
    // Request DTOs
    // ===============================

    public static class GitHubQuestRequest {
        private String title;
        private String description;
        private String githubIssueUrl;
        private String repository;
        private int issueNumber;
        private int requiredLevel;

        // Getters and setters
        public String getTitle() { return title; }
        public void setTitle(String title) { this.title = title; }

        public String getDescription() { return description; }
        public void setDescription(String description) { this.description = description; }

        public String getGithubIssueUrl() { return githubIssueUrl; }
        public void setGithubIssueUrl(String githubIssueUrl) { this.githubIssueUrl = githubIssueUrl; }

        public String getRepository() { return repository; }
        public void setRepository(String repository) { this.repository = repository; }

        public int getIssueNumber() { return issueNumber; }
        public void setIssueNumber(int issueNumber) { this.issueNumber = issueNumber; }

        public int getRequiredLevel() { return requiredLevel; }
        public void setRequiredLevel(int requiredLevel) { this.requiredLevel = requiredLevel; }
    }
}
