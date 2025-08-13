package com.syntopia.model;

import com.arangodb.springframework.annotation.Document;
import com.arangodb.springframework.annotation.Field;
import com.fasterxml.jackson.annotation.JsonFormat;
import org.springframework.data.annotation.Id;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * Quest Entity for Syntopia Platform - TAO Objects Layer
 * 
 * Represents a quest in the gamified system.
 * Quests can be platform quests (Level 1-3) or GitHub issues (Level 4+).
 */
@Document("quests")
public class Quest {

    @Id
    private String id;

    @Field("_key")
    private String key;

    private String title;
    private String description;
    private String role; // For which role this quest is designed
    private int requiredLevel; // Minimum level required to accept this quest
    private long experienceReward;
    private QuestType type; // PLATFORM, GITHUB_ISSUE, COMMUNITY
    private QuestStatus status; // AVAILABLE, ACTIVE, COMPLETED, ARCHIVED
    private QuestDifficulty difficulty; // BEGINNER, INTERMEDIATE, ADVANCED, EXPERT

    // GitHub Integration
    private String githubIssueUrl;
    private String githubRepository;
    private int githubIssueNumber;

    // Sacred Geometry aspects
    private List<String> geometryPatterns; // Which patterns this quest explores
    private Map<String, Object> geometryData; // Specific geometry parameters

    // Completion tracking
    private String completionCriteria;
    private List<String> completionSteps;
    private boolean isAutoValidated;

    // Additional metadata for flexible quest data
    private Map<String, Object> metadata;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createdAt;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime updatedAt;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    // Quest categorization and reusability
    private QuestCategory category; // INDIVIDUAL, SHARED, UNIQUE
    private boolean isReusable; // Can multiple users complete this quest?
    private int maxCompletions; // Maximum number of completions (0 = unlimited)
    private int currentCompletions; // Current number of completions

    private LocalDateTime dueDate;

    // Constructors
    public Quest() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
        this.status = QuestStatus.AVAILABLE;
        this.isAutoValidated = false;
    this.metadata = new java.util.HashMap<>();
    }

    public Quest(String title, String description, String role, int requiredLevel) {
        this();
        this.title = title;
        this.description = description;
        this.role = role;
        this.requiredLevel = requiredLevel;
    }

    // Enums
    public enum QuestCategory {
        INDIVIDUAL,  // Each user gets their own instance (onboarding quests)
        SHARED,      // Multiple users can work on the same quest (learning quests)
        UNIQUE       // Only one user can complete this quest (GitHub issues)
    }

    public enum QuestType {
        PLATFORM,
        GITHUB_ISSUE,
        COMMUNITY,
        SACRED_GEOMETRY,
        ONBOARDING,
        LEARNING,
        SKILL_BUILDING,
        NETWORKING,
        INTEGRATION,
        CONTRIBUTION
    }

    public enum QuestStatus {
        AVAILABLE,
        ACTIVE,
        COMPLETED,
        ARCHIVED,
        SUSPENDED
    }

    public enum QuestDifficulty {
        BEGINNER,
        INTERMEDIATE,
        ADVANCED,
        EXPERT
    }

    // Getters and Setters
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public int getRequiredLevel() {
        return requiredLevel;
    }

    public void setRequiredLevel(int requiredLevel) {
        this.requiredLevel = requiredLevel;
    }

    public long getExperienceReward() {
        return experienceReward;
    }

    public void setExperienceReward(long experienceReward) {
        this.experienceReward = experienceReward;
    }

    public QuestType getType() {
        return type;
    }

    public void setType(QuestType type) {
        this.type = type;
    }

    public QuestStatus getStatus() {
        return status;
    }

    public void setStatus(QuestStatus status) {
        this.status = status;
    }

    public QuestDifficulty getDifficulty() {
        return difficulty;
    }

    public void setDifficulty(QuestDifficulty difficulty) {
        this.difficulty = difficulty;
    }

    public String getGithubIssueUrl() {
        return githubIssueUrl;
    }

    public void setGithubIssueUrl(String githubIssueUrl) {
        this.githubIssueUrl = githubIssueUrl;
    }

    public String getGithubRepository() {
        return githubRepository;
    }

    public void setGithubRepository(String githubRepository) {
        this.githubRepository = githubRepository;
    }

    public int getGithubIssueNumber() {
        return githubIssueNumber;
    }

    public void setGithubIssueNumber(int githubIssueNumber) {
        this.githubIssueNumber = githubIssueNumber;
    }

    public List<String> getGeometryPatterns() {
        return geometryPatterns;
    }

    public void setGeometryPatterns(List<String> geometryPatterns) {
        this.geometryPatterns = geometryPatterns;
    }

    public Map<String, Object> getGeometryData() {
        return geometryData;
    }

    public void setGeometryData(Map<String, Object> geometryData) {
        this.geometryData = geometryData;
    }

    public String getCompletionCriteria() {
        return completionCriteria;
    }

    public void setCompletionCriteria(String completionCriteria) {
        this.completionCriteria = completionCriteria;
    }

    public List<String> getCompletionSteps() {
        return completionSteps;
    }

    public void setCompletionSteps(List<String> completionSteps) {
        this.completionSteps = completionSteps;
    }

    public boolean isAutoValidated() {
        return isAutoValidated;
    }

    public void setAutoValidated(boolean autoValidated) {
        isAutoValidated = autoValidated;
    }

    public Map<String, Object> getMetadata() {
        if (metadata == null) {
            metadata = new java.util.HashMap<>();
        }
        return metadata;
    }

    public void setMetadata(Map<String, Object> metadata) {
        this.metadata = metadata;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    public LocalDateTime getDueDate() {
        return dueDate;
    }

    public void setDueDate(LocalDateTime dueDate) {
        this.dueDate = dueDate;
    }

    public QuestCategory getCategory() {
        return category;
    }

    public void setCategory(QuestCategory category) {
        this.category = category;
    }

    public boolean isReusable() {
        return isReusable;
    }

    public void setReusable(boolean reusable) {
        isReusable = reusable;
    }

    public int getMaxCompletions() {
        return maxCompletions;
    }

    public void setMaxCompletions(int maxCompletions) {
        this.maxCompletions = maxCompletions;
    }

    public int getCurrentCompletions() {
        return currentCompletions;
    }

    public void setCurrentCompletions(int currentCompletions) {
        this.currentCompletions = currentCompletions;
    }

    // Utility methods for quest availability
    public boolean canBeCompletedByMoreUsers() {
        return isReusable && (maxCompletions == 0 || currentCompletions < maxCompletions);
    }

    public boolean isLimitReached() {
        return !isReusable || (maxCompletions > 0 && currentCompletions >= maxCompletions);
    }

    @Override
    public String toString() {
        return "Quest{" +
                "id='" + id + '\'' +
                ", title='" + title + '\'' +
                ", role='" + role + '\'' +
                ", requiredLevel=" + requiredLevel +
                ", type=" + type +
                ", status=" + status +
                ", difficulty=" + difficulty +
                '}';
    }
}
