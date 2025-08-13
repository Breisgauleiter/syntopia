package com.syntopia.model;

import com.arangodb.springframework.annotation.Document;
import com.arangodb.springframework.annotation.Field;
import com.fasterxml.jackson.annotation.JsonFormat;
import org.springframework.data.annotation.Id;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Document("onboarding_quests")
public class OnboardingQuest {

    @Id
    private String id;

    @Field("_key")
    private String key;

    private String title;
    private String description;
    private int level;
    private int xpReward;
    private String synPrinciple;
    private String roleDialect;
    private List<String> steps;
    private String iconType;
    private boolean profileRequired;
    private Map<String, Object> metadata;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime createdAt;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime updatedAt;

    public OnboardingQuest() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    // Getters and Setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getKey() { return key; }
    public void setKey(String key) { this.key = key; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public int getLevel() { return level; }
    public void setLevel(int level) { this.level = level; }

    public int getXpReward() { return xpReward; }
    public void setXpReward(int xpReward) { this.xpReward = xpReward; }

    public String getSynPrinciple() { return synPrinciple; }
    public void setSynPrinciple(String synPrinciple) { this.synPrinciple = synPrinciple; }

    public String getRoleDialect() { return roleDialect; }
    public void setRoleDialect(String roleDialect) { this.roleDialect = roleDialect; }

    public List<String> getSteps() { return steps; }
    public void setSteps(List<String> steps) { this.steps = steps; }

    public String getIconType() { return iconType; }
    public void setIconType(String iconType) { this.iconType = iconType; }

    public boolean isProfileRequired() { return profileRequired; }
    public void setProfileRequired(boolean profileRequired) { this.profileRequired = profileRequired; }

    public Map<String, Object> getMetadata() { return metadata; }
    public void setMetadata(Map<String, Object> metadata) { this.metadata = metadata; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}
