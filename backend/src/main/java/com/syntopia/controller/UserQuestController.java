package com.syntopia.controller;

import com.syntopia.model.UserQuest;
import com.syntopia.service.QuestService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * User Quest Controller for Syntopia Platform
 * 
 * Handles user-specific quest interactions and progress tracking
 */
@RestController
@RequestMapping("/api/user-quests")
@CrossOrigin(origins = "*")
public class UserQuestController {

    @Autowired
    private QuestService questService;

    /**
     * Get all available quests for the authenticated user with their progress
     */
    @GetMapping("/available")
    public ResponseEntity<List<UserQuest>> getAvailableQuestsForUser(Authentication authentication) {
        try {
            String userId = authentication.getName(); // Get user ID from JWT
            List<UserQuest> userQuests = questService.getAvailableQuestsForUserWithProgress(userId);
            return ResponseEntity.ok(userQuests);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * Accept/Start a quest for the authenticated user
     */
    @PostMapping("/{questId}/accept")
    public ResponseEntity<Map<String, Object>> acceptQuest(@PathVariable String questId, Authentication authentication) {
        try {
            String userId = authentication.getName();
            UserQuest userQuest = questService.acceptUserQuest(userId, questId);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Quest accepted successfully");
            response.put("userQuest", userQuest);
            
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(errorResponse);
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", "Failed to accept quest");
            return ResponseEntity.internalServerError().body(errorResponse);
        }
    }

    /**
     * Complete a quest for the authenticated user
     */
    @PostMapping("/{questId}/complete")
    public ResponseEntity<Map<String, Object>> completeQuest(
            @PathVariable String questId, 
            @RequestBody(required = false) Map<String, Object> completionData,
            Authentication authentication) {
        try {
            String userId = authentication.getName();
            
            if (completionData == null) {
                completionData = new HashMap<>();
            }
            
            UserQuest userQuest = questService.completeUserQuest(userId, questId, completionData);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Quest completed successfully");
            response.put("userQuest", userQuest);
            response.put("experienceAwarded", userQuest.getExperienceAwarded());
            
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(errorResponse);
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", "Failed to complete quest");
            return ResponseEntity.internalServerError().body(errorResponse);
        }
    }

    /**
     * Abandon a quest for the authenticated user
     */
    @PostMapping("/{questId}/abandon")
    public ResponseEntity<Map<String, Object>> abandonQuest(@PathVariable String questId, Authentication authentication) {
        try {
            String userId = authentication.getName();
            UserQuest userQuest = questService.abandonUserQuest(userId, questId);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Quest abandoned successfully");
            response.put("userQuest", userQuest);
            
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(errorResponse);
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", "Failed to abandon quest");
            return ResponseEntity.internalServerError().body(errorResponse);
        }
    }

    /**
     * Get user's quest statistics and progress
     */
    @GetMapping("/statistics")
    public ResponseEntity<Map<String, Object>> getUserQuestStatistics(Authentication authentication) {
        try {
            String userId = authentication.getName();
            Map<String, Object> statistics = questService.getUserQuestStatistics(userId);
            return ResponseEntity.ok(statistics);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * Update quest progress for the authenticated user
     */
    @PutMapping("/{questId}/progress")
    public ResponseEntity<Map<String, Object>> updateQuestProgress(
            @PathVariable String questId,
            @RequestBody Map<String, Object> progressUpdate,
            Authentication authentication) {
        try {
            String userId = authentication.getName();
            UserQuest updatedUserQuest = questService.updateQuestProgress(userId, questId, progressUpdate);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("userQuest", updatedUserQuest);
            response.put("message", "Progress updated successfully");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", "Failed to update progress: " + e.getMessage());
            return ResponseEntity.badRequest().body(errorResponse);
        }
    }

    /**
     * Get user's active quests
     */
    @GetMapping("/active")
    public ResponseEntity<List<UserQuest>> getActiveQuests(Authentication authentication) {
        try {
            String userId = authentication.getName();
            List<UserQuest> activeQuests = questService.getActiveQuestsForUser(userId);
            return ResponseEntity.ok(activeQuests);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

        /**
     * Get user's completed quests
     */
    @GetMapping("/completed")
    public ResponseEntity<List<UserQuest>> getCompletedQuests(Authentication authentication) {
        try {
            String userId = authentication.getName();
            List<UserQuest> completedQuests = questService.getCompletedQuestsForUser(userId);
            return ResponseEntity.ok(completedQuests);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }
}
