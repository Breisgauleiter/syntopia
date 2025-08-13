package com.syntopia.model;

import com.arangodb.springframework.annotation.Edge;
import com.arangodb.springframework.annotation.From;
import com.arangodb.springframework.annotation.To;
import com.fasterxml.jackson.annotation.JsonFormat;
import org.springframework.data.annotation.Id;

import java.time.LocalDateTime;

/**
 * UserCollaboration Edge Entity for Syntopia Platform
 * 
 * Represents collaboration relationships between users (mentorship, partnerships, etc.)
 * following the TAO architecture pattern.
 */
@Edge("user_collaborations")
public class UserCollaboration {

    @Id
    private String id;

    @From
    private User fromUser; // The user who initiates or mentors

    @To  
    private User toUser; // The user being mentored or collaborated with

    private CollaborationType type; // MENTOR, COLLABORATOR, WITNESS
    private CollaborationStatus status; // ACTIVE, COMPLETED, PAUSED, ENDED

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime startedAt;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime endedAt;

    private String notes; // Optional collaboration notes
    private int experienceShared; // Experience points shared through collaboration

    /**
     * Collaboration type enum
     */
    public enum CollaborationType {
        MENTOR,      // One user mentors another
        COLLABORATOR, // Peer-to-peer collaboration
        WITNESS      // One user witnesses another's journey
    }

    /**
     * Collaboration status enum
     */
    public enum CollaborationStatus {
        ACTIVE,    // Currently active collaboration
        COMPLETED, // Successfully completed collaboration
        PAUSED,    // Temporarily paused
        ENDED      // Ended (may not have been completed)
    }

    // Constructors
    public UserCollaboration() {}

    public UserCollaboration(User fromUser, User toUser, CollaborationType type) {
        this.fromUser = fromUser;
        this.toUser = toUser;
        this.type = type;
        this.status = CollaborationStatus.ACTIVE;
        this.startedAt = LocalDateTime.now();
    }

    // Getters and Setters
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public User getFromUser() {
        return fromUser;
    }

    public void setFromUser(User fromUser) {
        this.fromUser = fromUser;
    }

    public User getToUser() {
        return toUser;
    }

    public void setToUser(User toUser) {
        this.toUser = toUser;
    }

    public CollaborationType getType() {
        return type;
    }

    public void setType(CollaborationType type) {
        this.type = type;
    }

    public CollaborationStatus getStatus() {
        return status;
    }

    public void setStatus(CollaborationStatus status) {
        this.status = status;
    }

    public LocalDateTime getStartedAt() {
        return startedAt;
    }

    public void setStartedAt(LocalDateTime startedAt) {
        this.startedAt = startedAt;
    }

    public LocalDateTime getEndedAt() {
        return endedAt;
    }

    public void setEndedAt(LocalDateTime endedAt) {
        this.endedAt = endedAt;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    public int getExperienceShared() {
        return experienceShared;
    }

    public void setExperienceShared(int experienceShared) {
        this.experienceShared = experienceShared;
    }

    // Utility methods
    public void endCollaboration() {
        this.status = CollaborationStatus.ENDED;
        this.endedAt = LocalDateTime.now();
    }

    public void completeCollaboration(int experienceAwarded) {
        this.status = CollaborationStatus.COMPLETED;
        this.endedAt = LocalDateTime.now();
        this.experienceShared = experienceAwarded;
    }

    @Override
    public String toString() {
        return "UserCollaboration{" +
                "id='" + id + '\'' +
                ", fromUser=" + (fromUser != null ? fromUser.getId() : "null") +
                ", toUser=" + (toUser != null ? toUser.getId() : "null") +
                ", type=" + type +
                ", status=" + status +
                ", startedAt=" + startedAt +
                '}';
    }
}
