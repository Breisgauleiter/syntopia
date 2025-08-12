package com.syntopia.model;

import com.arangodb.springframework.annotation.Document;
import com.arangodb.springframework.annotation.Field;
import com.fasterxml.jackson.annotation.JsonFormat;
import org.springframework.data.annotation.Id;

import java.time.LocalDateTime;
import java.util.Set;
import java.util.List;
import java.util.Map;
import java.util.HashMap;

/**
 * User Entity for Syntopia Platform - TAO Objects Layer
 * 
 * Represents a user in the TAO (Objects) architecture.
 * Contains authentication, profile information, and community progress.
 */
@Document("users")
public class User {

    @Id
    private String id;

    @Field("_key")
    private String key;

    private String username;
    private String email;
    private String passwordHash; // For password-based authentication
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
    
    // Profile-specific fields for Phase 2
    private String bio; // User biography/description
    private String location; // User location
    private String website; // Personal website URL
    private String twitterHandle; // Twitter / X handle without @
    private String linkedinUrl; // Full LinkedIn profile URL
    private List<String> achievements; // List of achievement IDs
    private Map<String, Object> preferences; // Sacred geometry and UI preferences
    private Map<String, Object> stats; // User statistics (login streak, etc.)
    private List<String> badges; // Badge IDs earned by user
    private String profileTheme; // Selected sacred geometry theme
    private boolean profilePublic; // Whether profile is publicly visible
    private LocalDateTime lastActiveAt; // Last activity timestamp
    
    // Social features
    private int followersCount;
    private int followingCount;
    private int contributionsCount;
    
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime createdAt;
    
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime lastLoginAt;

    // Constructors
    public User() {
        this.createdAt = LocalDateTime.now();
        this.enabled = true;
        this.currentLevel = 1;
        this.experiencePoints = 0;
        this.questsCompleted = 0;
        this.isGitHubIntegrated = false;
    }

    public User(String username, String email) {
        this();
        this.username = username;
        this.email = email;
        this.key = username; // Use username as ArangoDB key
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

    public String getPasswordHash() {
        return passwordHash;
    }

    public void setPasswordHash(String passwordHash) {
        this.passwordHash = passwordHash;
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

    // Profile-specific getters and setters
    public String getBio() {
        return bio;
    }

    public void setBio(String bio) {
        this.bio = bio;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public String getWebsite() {
        return website;
    }

    public void setWebsite(String website) {
        this.website = website;
    }

    public String getTwitterHandle() { return twitterHandle; }
    public void setTwitterHandle(String twitterHandle) { this.twitterHandle = twitterHandle; }

    public String getLinkedinUrl() { return linkedinUrl; }
    public void setLinkedinUrl(String linkedinUrl) { this.linkedinUrl = linkedinUrl; }

    public List<String> getAchievements() {
        return achievements;
    }

    public void setAchievements(List<String> achievements) {
        this.achievements = achievements;
    }

    public Map<String, Object> getPreferences() {
        return preferences;
    }

    public void setPreferences(Map<String, Object> preferences) {
        this.preferences = preferences;
    }

    public Map<String, Object> getStats() {
        return stats;
    }

    public void setStats(Map<String, Object> stats) {
        this.stats = stats;
    }

    public List<String> getBadges() {
        return badges;
    }

    public void setBadges(List<String> badges) {
        this.badges = badges;
    }

    public String getProfileTheme() {
        return profileTheme;
    }

    public void setProfileTheme(String profileTheme) {
        this.profileTheme = profileTheme;
    }

    public boolean isProfilePublic() {
        return profilePublic;
    }

    public void setProfilePublic(boolean profilePublic) {
        this.profilePublic = profilePublic;
    }

    public LocalDateTime getLastActiveAt() {
        return lastActiveAt;
    }

    public void setLastActiveAt(LocalDateTime lastActiveAt) {
        this.lastActiveAt = lastActiveAt;
    }

    public int getFollowersCount() {
        return followersCount;
    }

    public void setFollowersCount(int followersCount) {
        this.followersCount = followersCount;
    }

    public int getFollowingCount() {
        return followingCount;
    }

    public void setFollowingCount(int followingCount) {
        this.followingCount = followingCount;
    }

    public int getContributionsCount() {
        return contributionsCount;
    }

    public void setContributionsCount(int contributionsCount) {
        this.contributionsCount = contributionsCount;
    }

    // Profile picture URL - use avatarUrl for consistency
    public String getProfilePictureUrl() {
        return avatarUrl;
    }

    public void setProfilePictureUrl(String profilePictureUrl) {
        this.avatarUrl = profilePictureUrl;
    }

    // Social links as a map
    public Map<String, String> getSocialLinks() {
        Map<String, String> socialLinks = new HashMap<>();
        if (website != null) socialLinks.put("website", website);
        if (githubId != null) socialLinks.put("github", "https://github.com/" + githubId);
        if (twitterHandle != null) socialLinks.put("twitter", "https://x.com/" + twitterHandle.replace("@", ""));
        if (linkedinUrl != null) socialLinks.put("linkedin", linkedinUrl);
        return socialLinks;
    }

    public void setSocialLinks(Map<String, String> socialLinks) {
        if (socialLinks != null) {
            this.website = socialLinks.get("website");
            String githubUrl = socialLinks.get("github");
            if (githubUrl != null && githubUrl.contains("github.com/")) {
                this.githubId = githubUrl.substring(githubUrl.lastIndexOf("/") + 1);
            }
            String twitterUrl = socialLinks.get("twitter");
            if (twitterUrl != null) {
                if (twitterUrl.contains("x.com/") || twitterUrl.contains("twitter.com/")) {
                    this.twitterHandle = twitterUrl.substring(twitterUrl.lastIndexOf("/") + 1).replace("@", "");
                } else {
                    this.twitterHandle = twitterUrl.replace("@", "");
                }
            }
            String linkedin = socialLinks.get("linkedin");
            if (linkedin != null && linkedin.contains("linkedin.")) {
                this.linkedinUrl = linkedin;
            }
        }
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
