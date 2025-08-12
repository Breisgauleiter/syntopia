package com.syntopia.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * Data Transfer Objects for Community API endpoints
 */
public class CommunityDTO {

    /**
     * DTO for connection requests
     */
    public static class ConnectionRequestDTO {
    @NotBlank(message = "toUserId is required")
    private String toUserId;
    @NotBlank(message = "connectionType is required")
    @Pattern(regexp = "friend|mentor|collaborator", message = "connectionType must be friend, mentor, or collaborator")
    private String connectionType; // 'friend', 'mentor', 'collaborator'

        public ConnectionRequestDTO() {}

        public ConnectionRequestDTO(String toUserId, String connectionType) {
            this.toUserId = toUserId;
            this.connectionType = connectionType;
        }

        public String getToUserId() { return toUserId; }
        public void setToUserId(String toUserId) { this.toUserId = toUserId; }

        public String getConnectionType() { return connectionType; }
        public void setConnectionType(String connectionType) { this.connectionType = connectionType; }
    }

    /**
     * DTO for connection responses (accept/decline)
     */
    public static class ConnectionResponseDTO {
    @NotBlank(message = "action is required")
    @Pattern(regexp = "accept|decline", message = "action must be 'accept' or 'decline'")
    private String action; // 'accept' or 'decline'

        public ConnectionResponseDTO() {}

        public ConnectionResponseDTO(String action) {
            this.action = action;
        }

        public String getAction() { return action; }
        public void setAction(String action) { this.action = action; }
    }

    /**
     * DTO for connection details with other user info
     */
    public static class ConnectionDTO {
        private String id;
        private String type;
        private String status;
        private LocalDateTime createdAt;
        private Map<String, Object> otherUser;

        public ConnectionDTO() {}

        public String getId() { return id; }
        public void setId(String id) { this.id = id; }

        public String getType() { return type; }
        public void setType(String type) { this.type = type; }

        public String getStatus() { return status; }
        public void setStatus(String status) { this.status = status; }

        public LocalDateTime getCreatedAt() { return createdAt; }
        public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

        public Map<String, Object> getOtherUser() { return otherUser; }
        public void setOtherUser(Map<String, Object> otherUser) { this.otherUser = otherUser; }
    }

    /**
     * DTO for creating new projects
     */
    public static class CreateProjectDTO {
    @NotBlank(message = "title is required")
    private String title;
    @NotBlank(message = "description is required")
    private String description;
        private List<String> tags;
    @Pattern(regexp = "public|private", message = "visibility must be 'public' or 'private'")
    private String visibility; // 'public' or 'private'

        public CreateProjectDTO() {}

        public String getTitle() { return title; }
        public void setTitle(String title) { this.title = title; }

        public String getDescription() { return description; }
        public void setDescription(String description) { this.description = description; }

        public List<String> getTags() { return tags; }
        public void setTags(List<String> tags) { this.tags = tags; }

        public String getVisibility() { return visibility; }
        public void setVisibility(String visibility) { this.visibility = visibility; }
    }

    /**
     * DTO for project details
     */
    public static class ProjectDTO {
        private String id;
        private String title;
        private String description;
        private List<String> tags;
        private String visibility;
        private LocalDateTime createdAt;
        private int membersCount;
        private Map<String, Object> owner;

        public ProjectDTO() {}

        public String getId() { return id; }
        public void setId(String id) { this.id = id; }

        public String getTitle() { return title; }
        public void setTitle(String title) { this.title = title; }

        public String getDescription() { return description; }
        public void setDescription(String description) { this.description = description; }

        public List<String> getTags() { return tags; }
        public void setTags(List<String> tags) { this.tags = tags; }

        public String getVisibility() { return visibility; }
        public void setVisibility(String visibility) { this.visibility = visibility; }

        public LocalDateTime getCreatedAt() { return createdAt; }
        public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

        public int getMembersCount() { return membersCount; }
        public void setMembersCount(int membersCount) { this.membersCount = membersCount; }

        public Map<String, Object> getOwner() { return owner; }
        public void setOwner(Map<String, Object> owner) { this.owner = owner; }
    }

    /**
     * DTO for leaderboard entries
     */
    public static class LeaderboardEntryDTO {
        private Map<String, Object> user;
        private int xp;
        private int completed;
        private int rank;

        public LeaderboardEntryDTO() {}

        public Map<String, Object> getUser() { return user; }
        public void setUser(Map<String, Object> user) { this.user = user; }

        public int getXp() { return xp; }
        public void setXp(int xp) { this.xp = xp; }

        public int getCompleted() { return completed; }
        public void setCompleted(int completed) { this.completed = completed; }

        public int getRank() { return rank; }
        public void setRank(int rank) { this.rank = rank; }
    }

    /**
     * DTO for feed items (discriminated union)
     */
    public static class FeedItemDTO {
        private String type; // 'quest_completed', 'project_created', 'connection_accepted'
        private LocalDateTime timestamp;
        private Map<String, Object> data;

        public FeedItemDTO() {}

        public FeedItemDTO(String type, LocalDateTime timestamp, Map<String, Object> data) {
            this.type = type;
            this.timestamp = timestamp;
            this.data = data;
        }

        public String getType() { return type; }
        public void setType(String type) { this.type = type; }

        public LocalDateTime getTimestamp() { return timestamp; }
        public void setTimestamp(LocalDateTime timestamp) { this.timestamp = timestamp; }

        public Map<String, Object> getData() { return data; }
        public void setData(Map<String, Object> data) { this.data = data; }
    }
}
