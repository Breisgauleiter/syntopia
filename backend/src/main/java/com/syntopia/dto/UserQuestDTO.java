package com.syntopia.dto;

import com.syntopia.model.Quest;
import com.syntopia.model.UserQuest;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * Data Transfer Object for UserQuest ensuring presence of userId & questId.
 */
public class UserQuestDTO {
    public String userQuestId;
    public String userId;
    public String questId;
    public UserQuest.UserQuestStatus status;
    public int progress;
    public Map<String, Object> progressData;
    public LocalDateTime acceptedAt;
    public LocalDateTime startedAt;
    public LocalDateTime completedAt;
    public LocalDateTime abandonedAt;
    public LocalDateTime lastProgressUpdate;
    public long experienceAwarded;
    public String completionNotes;
    public Map<String, Object> completionData;
    public boolean verified;
    public Quest quest; // Embedded quest summary (can be trimmed later)

    public static UserQuestDTO fromEntity(UserQuest entity, String userIdFallback) {
        UserQuestDTO dto = new UserQuestDTO();
        dto.userQuestId = entity.getId();
        dto.userId = entity.getUser() != null ? entity.getUser().getId() : userIdFallback;
        Quest q = entity.getQuest();
        dto.quest = q;
        dto.questId = q != null ? q.getId() : null;
        dto.status = entity.getStatus();
        dto.progress = entity.getProgress();
        dto.progressData = entity.getProgressData();
        dto.acceptedAt = entity.getAcceptedAt();
        dto.startedAt = entity.getStartedAt();
        dto.completedAt = entity.getCompletedAt();
        dto.abandonedAt = entity.getAbandonedAt();
        dto.lastProgressUpdate = entity.getLastProgressUpdate();
        dto.experienceAwarded = entity.getExperienceAwarded();
        dto.completionNotes = entity.getCompletionNotes();
        dto.completionData = entity.getCompletionData();
        dto.verified = entity.isVerified();
        return dto;
    }

    public Map<String, Object> toMap() {
        Map<String, Object> map = new HashMap<>();
        map.put("userQuestId", userQuestId);
        map.put("userId", userId);
        map.put("questId", questId);
        map.put("status", status);
        map.put("progress", progress);
        map.put("progressData", progressData);
        map.put("acceptedAt", acceptedAt);
        map.put("startedAt", startedAt);
        map.put("completedAt", completedAt);
        map.put("abandonedAt", abandonedAt);
        map.put("lastProgressUpdate", lastProgressUpdate);
        map.put("experienceAwarded", experienceAwarded);
        map.put("completionNotes", completionNotes);
        map.put("completionData", completionData);
        map.put("verified", verified);
        map.put("quest", quest);
        return map;
    }
}
