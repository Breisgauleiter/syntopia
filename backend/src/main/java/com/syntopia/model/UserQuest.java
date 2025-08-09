package com.syntopia.model;

import com.arangodb.springframework.annotation.Edge;
import com.arangodb.springframework.annotation.From;
import com.arangodb.springframework.annotation.To;
import com.fasterxml.jackson.annotation.JsonFormat;
import org.springframework.data.annotation.Id;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * UserQuest Edge Entity for Syntopia Platform
 * 
 * Represents the relationship between a User and a Quest, tracking individual progress.
 * This allows multiple users to work on the same quest independently.
 */
@Edge("user_quests")
public class UserQuest {

    @Id
    private String id;

    @From
    private String userId; // Points to User document

    @To  
    private String questId; // Points to Quest document

    private UserQuestStatus status; // USER_AVAILABLE, USER_ACTIVE, USER_COMPLETED, USER_ABANDONED

    // Include quest data for convenience (not stored in database, populated by service)
    private transient Quest quest;
    private int progress; // Progress percentage (0-100)
    private Map<String, Object> progressData; // Flexible progress tracking data

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime acceptedAt;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime startedAt;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime completedAt;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime abandonedAt;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime lastProgressUpdate;

    // Experience awarded for this specific completion
    private long experienceAwarded;

    // Completion details
    private String completionNotes;
    private Map<String, Object> completionData; // Flexible completion data (GitHub PR link, etc.)

    // Quest type specific data
    private String githubPullRequestUrl; // For GitHub quests
    private String githubCommitSha; // For GitHub quests
    private boolean isVerified; // If completion has been verified

    /**
     * Status enum for individual user quest progress
     */
    public enum UserQuestStatus {
        USER_AVAILABLE,  // User can start this quest
        USER_ACTIVE,     // User is currently working on this quest
        USER_COMPLETED,  // User has completed this quest
        USER_ABANDONED,  // User abandoned this quest
        USER_VERIFIED    // Quest completion has been verified (for GitHub quests)
    }

    // Constructors
    public UserQuest() {}

    public UserQuest(String userId, String questId) {
        this.userId = userId;
        this.questId = questId;
        this.status = UserQuestStatus.USER_AVAILABLE;
        this.progress = 0;
        this.acceptedAt = LocalDateTime.now();
    }

    // Getters and Setters
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getQuestId() {
        return questId;
    }

    public void setQuestId(String questId) {
        this.questId = questId;
    }

    public UserQuestStatus getStatus() {
        return status;
    }

    public void setStatus(UserQuestStatus status) {
        this.status = status;
    }

    public int getProgress() {
        return progress;
    }

    public void setProgress(int progress) {
        this.progress = Math.max(0, Math.min(100, progress)); // Clamp 0-100
    }

    public Map<String, Object> getProgressData() {
        return progressData;
    }

    public void setProgressData(Map<String, Object> progressData) {
        this.progressData = progressData;
    }

    public LocalDateTime getAcceptedAt() {
        return acceptedAt;
    }

    public void setAcceptedAt(LocalDateTime acceptedAt) {
        this.acceptedAt = acceptedAt;
    }

    public LocalDateTime getStartedAt() {
        return startedAt;
    }

    public void setStartedAt(LocalDateTime startedAt) {
        this.startedAt = startedAt;
    }

    public LocalDateTime getCompletedAt() {
        return completedAt;
    }

    public void setCompletedAt(LocalDateTime completedAt) {
        this.completedAt = completedAt;
    }

    public LocalDateTime getAbandonedAt() {
        return abandonedAt;
    }

    public void setAbandonedAt(LocalDateTime abandonedAt) {
        this.abandonedAt = abandonedAt;
    }

    public LocalDateTime getLastProgressUpdate() {
        return lastProgressUpdate;
    }

    public void setLastProgressUpdate(LocalDateTime lastProgressUpdate) {
        this.lastProgressUpdate = lastProgressUpdate;
    }

    public long getExperienceAwarded() {
        return experienceAwarded;
    }

    public void setExperienceAwarded(long experienceAwarded) {
        this.experienceAwarded = experienceAwarded;
    }

    public String getCompletionNotes() {
        return completionNotes;
    }

    public void setCompletionNotes(String completionNotes) {
        this.completionNotes = completionNotes;
    }

    public Map<String, Object> getCompletionData() {
        return completionData;
    }

    public void setCompletionData(Map<String, Object> completionData) {
        this.completionData = completionData;
    }

    public String getGithubPullRequestUrl() {
        return githubPullRequestUrl;
    }

    public void setGithubPullRequestUrl(String githubPullRequestUrl) {
        this.githubPullRequestUrl = githubPullRequestUrl;
    }

    public String getGithubCommitSha() {
        return githubCommitSha;
    }

    public void setGithubCommitSha(String githubCommitSha) {
        this.githubCommitSha = githubCommitSha;
    }

    public boolean isVerified() {
        return isVerified;
    }

    public void setVerified(boolean verified) {
        isVerified = verified;
    }

    public Quest getQuest() {
        return quest;
    }

    public void setQuest(Quest quest) {
        this.quest = quest;
    }

    // Utility methods
    public boolean isCompleted() {
        return status == UserQuestStatus.USER_COMPLETED || status == UserQuestStatus.USER_VERIFIED;
    }

    public boolean isActive() {
        return status == UserQuestStatus.USER_ACTIVE;
    }

    public boolean isAvailable() {
        return status == UserQuestStatus.USER_AVAILABLE;
    }

    public void markAsStarted() {
        this.status = UserQuestStatus.USER_ACTIVE;
        this.startedAt = LocalDateTime.now();
        this.lastProgressUpdate = LocalDateTime.now();
    }

    public void markAsCompleted(long experienceAwarded) {
        this.status = UserQuestStatus.USER_COMPLETED;
        this.completedAt = LocalDateTime.now();
        this.lastProgressUpdate = LocalDateTime.now();
        this.progress = 100;
        this.experienceAwarded = experienceAwarded;
    }

    public void markAsAbandoned() {
        this.status = UserQuestStatus.USER_ABANDONED;
        this.abandonedAt = LocalDateTime.now();
        this.lastProgressUpdate = LocalDateTime.now();
    }

    @Override
    public String toString() {
        return "UserQuest{" +
                "id='" + id + '\'' +
                ", userId='" + userId + '\'' +
                ", questId='" + questId + '\'' +
                ", status=" + status +
                ", progress=" + progress +
                ", completedAt=" + completedAt +
                '}';
    }
}
