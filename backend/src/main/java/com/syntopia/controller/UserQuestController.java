package com.syntopia.controller;

import com.syntopia.dto.ApiResponse;
import com.syntopia.dto.UserQuestDTO;
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
    public ResponseEntity<?> getAvailableQuestsForUser(Authentication authentication,
                                                       @RequestParam(defaultValue = "0") int page,
                                                       @RequestParam(defaultValue = "50") int size) {
    String userId = authentication.getName();
    List<UserQuest> all = questService.getAvailableQuestsForUserWithProgress(userId);
    int from = Math.min(page * size, all.size());
    int to = Math.min(from + size, all.size());
    List<UserQuestDTO> slice = all.subList(from, to).stream()
        .map(uq -> UserQuestDTO.fromEntity(uq, userId))
        .toList();
    return ResponseEntity.ok(ApiResponse.paginated(slice, page, size, all.size()));
    }

    /**
     * Accept/Start a quest for the authenticated user
     */
    @PostMapping("/{questId}/accept")
    public ResponseEntity<?> acceptQuest(@PathVariable String questId, Authentication authentication) {
    String userId = authentication.getName();
    UserQuest userQuest = questService.acceptUserQuest(userId, questId); // may throw IllegalArgumentException
    UserQuestDTO dto = UserQuestDTO.fromEntity(userQuest, userId);
    return ResponseEntity.ok(ApiResponse.success("Quest accepted successfully", dto));
    }

    /**
     * Complete a quest for the authenticated user
     */
    @PostMapping("/{questId}/complete")
    public ResponseEntity<?> completeQuest(
            @PathVariable String questId, 
            @RequestBody(required = false) Map<String, Object> completionData,
            Authentication authentication) {
        String userId = authentication.getName();
        if (completionData == null) {
            completionData = new HashMap<>();
        }
        UserQuest userQuest = questService.completeUserQuest(userId, questId, completionData); // may throw IllegalArgumentException
        UserQuestDTO dto = UserQuestDTO.fromEntity(userQuest, userId);
        Map<String,Object> payload = new HashMap<>(dto.toMap());
        payload.put("experienceAwarded", userQuest.getExperienceAwarded());
        return ResponseEntity.ok(ApiResponse.success("Quest completed successfully", payload));
    }

    /**
     * Abandon a quest for the authenticated user
     */
    @PostMapping("/{questId}/abandon")
    public ResponseEntity<?> abandonQuest(@PathVariable String questId, Authentication authentication) {
    String userId = authentication.getName();
    UserQuest userQuest = questService.abandonUserQuest(userId, questId);
    UserQuestDTO dto = UserQuestDTO.fromEntity(userQuest, userId);
    return ResponseEntity.ok(ApiResponse.success("Quest abandoned successfully", dto));
    }

    /**
     * Get user's quest statistics and progress
     */
    @GetMapping("/statistics")
    public ResponseEntity<?> getUserQuestStatistics(Authentication authentication) {
    String userId = authentication.getName();
    Map<String, Object> statistics = questService.getUserQuestStatistics(userId);
    return ResponseEntity.ok(ApiResponse.success(statistics));
    }

    /**
     * Update quest progress for the authenticated user
     */
    @PutMapping("/{questId}/progress")
    public ResponseEntity<?> updateQuestProgress(
            @PathVariable String questId,
            @RequestBody Map<String, Object> progressUpdate,
            Authentication authentication) {
    String userId = authentication.getName();
    UserQuest updatedUserQuest = questService.updateQuestProgress(userId, questId, progressUpdate);
    UserQuestDTO dto = UserQuestDTO.fromEntity(updatedUserQuest, userId);
    return ResponseEntity.ok(ApiResponse.success("Progress updated successfully", dto));
    }

    /**
     * Get user's active quests
     */
    @GetMapping("/active")
    public ResponseEntity<?> getActiveQuests(Authentication authentication,
                                             @RequestParam(defaultValue = "0") int page,
                                             @RequestParam(defaultValue = "50") int size) {
    String userId = authentication.getName();
    List<UserQuest> all = questService.getActiveQuestsForUser(userId);
    int from = Math.min(page * size, all.size());
    int to = Math.min(from + size, all.size());
    List<UserQuestDTO> slice = all.subList(from, to).stream().map(uq -> UserQuestDTO.fromEntity(uq, userId)).toList();
    return ResponseEntity.ok(ApiResponse.paginated(slice, page, size, all.size()));
    }

        /**
     * Get user's completed quests
     */
    @GetMapping("/completed")
    public ResponseEntity<?> getCompletedQuests(Authentication authentication,
                                                @RequestParam(defaultValue = "0") int page,
                                                @RequestParam(defaultValue = "50") int size) {
    String userId = authentication.getName();
    List<UserQuest> all = questService.getCompletedQuestsForUser(userId);
    int from = Math.min(page * size, all.size());
    int to = Math.min(from + size, all.size());
    List<UserQuestDTO> slice = all.subList(from, to).stream().map(uq -> UserQuestDTO.fromEntity(uq, userId)).toList();
    return ResponseEntity.ok(ApiResponse.paginated(slice, page, size, all.size()));
    }

    /**
     * Verify a completed quest (transition USER_COMPLETED -> USER_VERIFIED)
     */
    @PostMapping("/{questId}/verify")
    public ResponseEntity<?> verifyQuest(@PathVariable String questId, Authentication authentication) {
    String userId = authentication.getName();
    UserQuest verified = questService.verifyCompletedUserQuest(userId, questId);
    UserQuestDTO dto = UserQuestDTO.fromEntity(verified, userId);
    return ResponseEntity.ok(ApiResponse.success("Quest verified", dto));
    }
}
