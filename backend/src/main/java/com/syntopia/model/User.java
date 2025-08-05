package com.syntopia.model;

import com.arangodb.springframework.annotation.Document;
import com.arangodb.springframework.annotation.Key;
import com.fasterxml.jackson.annotation.JsonFormat;
import org.springframework.data.annotation.Id;

import java.time.LocalDateTime;
import java.util.Set;

/**
 * User Entity for Syntopia Platform - TAO Objects Layer
 * 
 * Represents a user in the TAO (Objects) architecture.
 * Contains authentication, profile information, and Sacred Geometry progress.
 */
@Document("users")
public class User {

    @Id
    private String id;

    @Key
    private String key;

    private String username;
    private String email;
    private String githubId;
    private String displayName;
    private String avatarUrl;
    private Set<String> roles;
    private boolean enabled;
    
    // Syntopia-specific fields
    private String selectedRole; // One of the 7 initial roles
    private int currentLevel; // SCL (Syntopia Contribution Level) 1-25
    private long experiencePoints;
    private int questsCompleted;
    private boolean isGitHubIntegrated;
    
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createdAt;
    
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime lastLoginAt;

    // Constructors
    public User() {
        this.createdAt = LocalDateTime.now();
        this.enabled = true;
        this.currentLevel = 1;
        this.experiencePoints = 0L;
        this.questsCompleted = 0;
        this.isGitHubIntegrated = false;
    }

    public User(String username, String email) {
        this();
        this.username = username;
        this.email = email;
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

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getGithubId() {
        return githubId;
    }

    public void setGithubId(String githubId) {
        this.githubId = githubId;
    }

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public String getAvatarUrl() {
        return avatarUrl;
    }

    public void setAvatarUrl(String avatarUrl) {
        this.avatarUrl = avatarUrl;
    }

    public Set<String> getRoles() {
        return roles;
    }

    public void setRoles(Set<String> roles) {
        this.roles = roles;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public String getSelectedRole() {
        return selectedRole;
    }

    public void setSelectedRole(String selectedRole) {
        this.selectedRole = selectedRole;
    }

    public int getCurrentLevel() {
        return currentLevel;
    }

    public void setCurrentLevel(int currentLevel) {
        this.currentLevel = currentLevel;
    }

    public long getExperiencePoints() {
        return experiencePoints;
    }

    public void setExperiencePoints(long experiencePoints) {
        this.experiencePoints = experiencePoints;
    }

    public int getQuestsCompleted() {
        return questsCompleted;
    }

    public void setQuestsCompleted(int questsCompleted) {
        this.questsCompleted = questsCompleted;
    }

    public boolean isGitHubIntegrated() {
        return isGitHubIntegrated;
    }

    public void setGitHubIntegrated(boolean gitHubIntegrated) {
        isGitHubIntegrated = gitHubIntegrated;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getLastLoginAt() {
        return lastLoginAt;
    }

    public void setLastLoginAt(LocalDateTime lastLoginAt) {
        this.lastLoginAt = lastLoginAt;
    }

    @Override
    public String toString() {
        return "User{" +
                "id='" + id + '\'' +
                ", username='" + username + '\'' +
                ", email='" + email + '\'' +
                ", displayName='" + displayName + '\'' +
                ", selectedRole='" + selectedRole + '\'' +
                ", currentLevel=" + currentLevel +
                ", enabled=" + enabled +
                '}';
    }
}
