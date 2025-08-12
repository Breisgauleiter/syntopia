package com.syntopia.controller;

import com.syntopia.model.Quest;
import com.syntopia.dto.ApiResponse;
import com.syntopia.model.UserQuest;
import com.syntopia.service.QuestService;
import com.syntopia.exception.ResourceNotFoundException;
import com.syntopia.security.Roles;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;

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
        List<Quest> quests = questService.getAllQuests();
        Map<String,Object> payload = new HashMap<>();
        payload.put("quests", quests);
        payload.put("count", quests.size());
        return ResponseEntity.ok(ApiResponse.success(payload));
    }

    /**
     * Get quest by ID
     */
    @GetMapping("/{questId}")
    public ResponseEntity<Map<String, Object>> getQuestById(@PathVariable String questId) {
        Optional<Quest> questOpt = questService.getQuestById(questId);
        if (questOpt.isEmpty()) {
            throw new ResourceNotFoundException("Quest not found");
        }
        return ResponseEntity.ok(ApiResponse.success(Map.of("quest", questOpt.get())));
    }

    /**
     * Create a new quest (Admin only - future implementation)
     */
    @PostMapping
    @PreAuthorize("hasRole('" + Roles.ADMIN + "')")
    public ResponseEntity<Map<String, Object>> createQuest(@RequestBody Quest quest) {
        Quest createdQuest = questService.createQuest(quest);
        return ResponseEntity.status(HttpStatus.CREATED)
            .body(ApiResponse.success("Quest created successfully", Map.of("quest", createdQuest)));
    }

    /**
     * Update an existing quest (Admin only - future implementation)
     */
    @PutMapping("/{questId}")
    @PreAuthorize("hasRole('" + Roles.ADMIN + "')")
    public ResponseEntity<Map<String, Object>> updateQuest(@PathVariable String questId, 
                              @RequestBody Quest questUpdates) {
        Quest updatedQuest = questService.updateQuest(questId, questUpdates);
        return ResponseEntity.ok(ApiResponse.success("Quest updated successfully", Map.of("quest", updatedQuest)));
    }

    /**
     * Delete a quest (Admin only - future implementation)
     */
    @DeleteMapping("/{questId}")
    @PreAuthorize("hasRole('" + Roles.ADMIN + "')")
    public ResponseEntity<Map<String, Object>> deleteQuest(@PathVariable String questId) {
        questService.deleteQuest(questId);
        return ResponseEntity.ok(ApiResponse.success("Quest deleted successfully"));
    }

    // ===============================
    // Quest Discovery Endpoints
    // ===============================

    /**
     * Get available quests for a specific user
     */
    @GetMapping("/available/{userId}")
    public ResponseEntity<Map<String, Object>> getAvailableQuests(@PathVariable String userId) {
        List<UserQuest> availableQuests = questService.getAvailableQuestsForUserWithProgress(userId);
        Map<String,Object> payload = new HashMap<>();
        payload.put("quests", availableQuests);
        payload.put("count", availableQuests.size());
        return ResponseEntity.ok(ApiResponse.success(payload));
    }

    /**
     * Get quests by role
     */
    @GetMapping("/role/{role}")
    public ResponseEntity<Map<String, Object>> getQuestsByRole(@PathVariable String role) {
        List<Quest> roleQuests = questService.getQuestsByRole(role);
        Map<String,Object> payload = new HashMap<>();
        payload.put("quests", roleQuests);
        payload.put("count", roleQuests.size());
        payload.put("role", role);
        return ResponseEntity.ok(ApiResponse.success(payload));
    }

    /**
     * Get quests by type
     */
    @GetMapping("/type/{type}")
    public ResponseEntity<Map<String, Object>> getQuestsByType(@PathVariable Quest.QuestType type) {
        List<Quest> typeQuests = questService.getQuestsByType(type);
        Map<String,Object> payload = new HashMap<>();
        payload.put("quests", typeQuests);
        payload.put("count", typeQuests.size());
        payload.put("type", type);
        return ResponseEntity.ok(ApiResponse.success(payload));
    }

    /**
     * Get quests by difficulty
     */
    @GetMapping("/difficulty/{difficulty}")
    public ResponseEntity<Map<String, Object>> getQuestsByDifficulty(@PathVariable Quest.QuestDifficulty difficulty) {
        List<Quest> difficultyQuests = questService.getQuestsByDifficulty(difficulty);
        Map<String,Object> payload = new HashMap<>();
        payload.put("quests", difficultyQuests);
        payload.put("count", difficultyQuests.size());
        payload.put("difficulty", difficulty);
        return ResponseEntity.ok(ApiResponse.success(payload));
    }

    /**
     * Get GitHub integration quests
     */
    @GetMapping("/github")
    public ResponseEntity<Map<String, Object>> getGitHubQuests() {
        List<Quest> githubQuests = questService.getGitHubQuests();
        Map<String,Object> payload = new HashMap<>();
        payload.put("quests", githubQuests);
        payload.put("count", githubQuests.size());
        payload.put("type", "GitHub Integration");
        return ResponseEntity.ok(ApiResponse.success(payload));
    }

    /**
     * Get quests by geometry pattern
     */
    @GetMapping("/pattern/{pattern}")
    public ResponseEntity<Map<String, Object>> getQuestsByGeometryPattern(@PathVariable String pattern) {
        List<Quest> patternQuests = questService.getQuestsByGeometryPattern(pattern);
        Map<String,Object> payload = new HashMap<>();
        payload.put("quests", patternQuests);
        payload.put("count", patternQuests.size());
        payload.put("pattern", pattern);
        return ResponseEntity.ok(ApiResponse.success(payload));
    }

    // (Deprecated quest progression endpoints removed; use /api/user-quests/* endpoints instead.)

    // ===============================
    // Quest Analytics Endpoints
    // ===============================

    /**
     * Get quest statistics
     */
    @GetMapping("/stats")
    public ResponseEntity<Map<String, Object>> getQuestStatistics() {
        Map<String, Object> stats = new HashMap<>();
        stats.put("total", questService.getAllQuests().size());
        stats.put("available", questService.getAvailableQuestCount());
        stats.put("active", questService.getActiveQuestCount());
        stats.put("completed", questService.getCompletedQuestCount());
        return ResponseEntity.ok(ApiResponse.success(Map.of("statistics", stats)));
    }

    // ===============================
    // GitHub Integration Endpoints
    // ===============================

    /**
     * Create quest from GitHub issue (Admin/System use)
     */
    @PostMapping("/github")
    @PreAuthorize("hasRole('" + Roles.ADMIN + "')")
    public ResponseEntity<Map<String, Object>> createGitHubQuest(@Valid @RequestBody GitHubQuestRequest request) {
        Quest githubQuest = questService.createGitHubQuest(
                request.getTitle(),
                request.getDescription(),
                request.getGithubIssueUrl(),
                request.getRepository(),
                request.getIssueNumber(),
                request.getRequiredLevel()
        );
        return ResponseEntity.status(HttpStatus.CREATED)
            .body(ApiResponse.success("GitHub quest created successfully", Map.of("quest", githubQuest)));
    }

    // ===============================
    // Helper Methods
    // ===============================

    // Removed legacy createErrorResponse & createUserResponse helpers after ApiResponse migration

    // Removed legacy createUserQuestResponse helper (no longer needed).

    // ===============================
    // Request DTOs
    // ===============================

    public static class GitHubQuestRequest {
        @NotBlank(message = "Title is required")
        private String title;
        @NotBlank(message = "Description is required")
        private String description;
        @NotBlank(message = "GitHub issue URL is required")
        @Pattern(regexp = "https://github.com/.+/.+/issues/\\d+", message = "Must be a valid GitHub issue URL")
        private String githubIssueUrl;
        @NotBlank(message = "Repository identifier is required")
        private String repository;
        @Min(value = 1, message = "Issue number must be positive")
        private int issueNumber;
        @Min(value = 1, message = "Required level must be at least 1")
        @Max(value = 25, message = "Required level cannot exceed 25")
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

    // ===============================
    // Admin/Setup Endpoints
    // ===============================

    /**
     * Seed onboarding quests into the database
     * This endpoint should be called once during setup to populate
     * the database with the onboarding quests
     * ADMIN ONLY
     */
    @PostMapping("/seed-onboarding")
    @PreAuthorize("hasRole('" + Roles.ADMIN + "')")
    public ResponseEntity<Map<String, Object>> seedOnboardingQuests() {
        List<Quest> seededQuests = questService.seedOnboardingQuests();
        Map<String, Object> payload = new HashMap<>();
        payload.put("questsSeeded", seededQuests.size());
        payload.put("quests", seededQuests);
        return ResponseEntity.ok(ApiResponse.success("Onboarding quests seeded successfully", payload));
    }
}
