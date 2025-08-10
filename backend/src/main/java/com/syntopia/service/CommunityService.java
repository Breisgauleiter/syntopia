package com.syntopia.service;

import com.syntopia.model.User;
import com.syntopia.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Random;
import java.util.stream.Collectors;

/**
 * CommunityService - Business logic for Community Features
 */
@Service
public class CommunityService {

    @Autowired
    private UserRepository userRepository;

    /**
     * Get community activity feed
     */
    public Map<String, Object> getCommunityFeed(String userId, int page, int size) {
        try {
            // Get recent quest completions for activity feed
            List<Map<String, Object>> activities = new ArrayList<>();
            
            // Get all users for demo activity
            List<User> users = new ArrayList<>();
            userRepository.findAll().forEach(users::add);
            
            // Create sample activities from user data
            for (User user : users.subList(0, Math.min(users.size(), 10))) {
                if (!user.getId().equals(userId)) {
                    Map<String, Object> activity = new HashMap<>();
                    activity.put("id", "activity_" + user.getId());
                    activity.put("type", "quest_completion");
                    activity.put("userId", user.getId());
                    activity.put("user", Map.of(
                        "id", user.getId(),
                        "displayName", user.getDisplayName(),
                        "profilePictureUrl", user.getProfilePictureUrl(),
                        "currentLevel", user.getCurrentLevel(),
                        "selectedRole", user.getSelectedRole()
                    ));
                    activity.put("questTitle", "Sample Quest: " + user.getSelectedRole() + " Training");
                    activity.put("questType", user.getSelectedRole() != null ? user.getSelectedRole().toLowerCase() : "general");
                    activity.put("experienceReward", 50);
                    activity.put("timestamp", LocalDateTime.now().minusHours(new Random().nextInt(24)));
                    activities.add(activity);
                }
            }
            
            // Sort by timestamp
            activities.sort((a, b) -> 
                ((LocalDateTime) b.get("timestamp")).compareTo((LocalDateTime) a.get("timestamp"))
            );
            
            // Apply pagination
            int start = page * size;
            int end = Math.min(start + size, activities.size());
            List<Map<String, Object>> paginatedActivities = 
                start < activities.size() ? activities.subList(start, end) : new ArrayList<>();
            
            Map<String, Object> result = new HashMap<>();
            result.put("activities", paginatedActivities);
            result.put("totalCount", activities.size());
            result.put("hasMore", end < activities.size());
            return result;
        } catch (Exception e) {
            throw new RuntimeException("Error loading community feed: " + e.getMessage(), e);
        }
    }

    /**
     * Search and discover users
     */
    public Map<String, Object> getUserDirectory(String searchQuery, String roleFilter, String levelFilter, 
                                               String locationFilter, int page, int size) {
        try {
            List<User> allUsers = new ArrayList<>();
            userRepository.findAll().forEach(allUsers::add);
            
            // Apply filters
            List<User> filteredUsers = allUsers.stream()
                .filter(user -> {
                    // Search query filter
                    if (searchQuery != null && !searchQuery.trim().isEmpty()) {
                        String query = searchQuery.toLowerCase();
                        return user.getDisplayName().toLowerCase().contains(query) ||
                               (user.getUsername() != null && user.getUsername().toLowerCase().contains(query)) ||
                               (user.getBio() != null && user.getBio().toLowerCase().contains(query));
                    }
                    return true;
                })
                .filter(user -> {
                    // Role filter
                    if (roleFilter != null && !roleFilter.equals("all")) {
                        return roleFilter.equals(user.getSelectedRole());
                    }
                    return true;
                })
                .filter(user -> {
                    // Level filter
                    if (levelFilter != null && !levelFilter.equals("all")) {
                        try {
                            int level = Integer.parseInt(levelFilter);
                            return user.getCurrentLevel() == level;
                        } catch (NumberFormatException e) {
                            return true;
                        }
                    }
                    return true;
                })
                .collect(Collectors.toList());
            
            // Apply pagination
            int start = page * size;
            int end = Math.min(start + size, filteredUsers.size());
            List<User> paginatedUsers = 
                start < filteredUsers.size() ? filteredUsers.subList(start, end) : new ArrayList<>();
            
            // Convert to response format
            List<Map<String, Object>> userList = paginatedUsers.stream()
                .map(this::convertUserToMap)
                .collect(Collectors.toList());
            
            Map<String, Object> result = new HashMap<>();
            result.put("users", userList);
            result.put("totalCount", filteredUsers.size());
            result.put("hasMore", end < filteredUsers.size());
            return result;
        } catch (Exception e) {
            throw new RuntimeException("Error searching users: " + e.getMessage(), e);
        }
    }

    /**
     * Get user connections (mock implementation)
     */
    public Map<String, Object> getUserConnections(String userId) {
        try {
            List<Map<String, Object>> pendingRequests = new ArrayList<>();
            List<Map<String, Object>> acceptedConnections = new ArrayList<>();
            
            Map<String, Object> result = new HashMap<>();
            result.put("pending", pendingRequests);
            result.put("accepted", acceptedConnections);
            return result;
        } catch (Exception e) {
            throw new RuntimeException("Error loading user connections: " + e.getMessage(), e);
        }
    }

    /**
     * Join project collaboration (mock implementation)
     */

    /**
     * Join project collaboration (mock implementation)
     */
    public Map<String, Object> joinProjectCollaboration(String userId, String projectId, String role) {
        try {
            Map<String, Object> result = new HashMap<>();
            result.put("success", true);
            result.put("message", "Successfully joined project collaboration");
            return result;
        } catch (Exception e) {
            throw new RuntimeException("Error joining project: " + e.getMessage(), e);
        }
    }

    /**
     * Get community leaderboard
     */
    public Map<String, Object> getCommunityLeaderboard(String period, int page, int size) {
        try {
            List<User> users = new ArrayList<>();
            userRepository.findAll().forEach(users::add);
            
            // Create leaderboard entries with calculated scores
            List<Map<String, Object>> leaderboardEntries = users.stream()
                .map(user -> {
                    Map<String, Object> entry = new HashMap<>();
                    entry.put("userId", user.getId());
                    entry.put("user", Map.of(
                        "id", user.getId(),
                        "displayName", user.getDisplayName(),
                        "profilePictureUrl", user.getProfilePictureUrl(),
                        "currentLevel", user.getCurrentLevel(),
                        "selectedRole", user.getSelectedRole()
                    ));
                    
                    // Calculate score based on experience and level
                    long score = user.getExperiencePoints() + (user.getCurrentLevel() * 100L);
                    entry.put("score", score);
                    
                    return entry;
                })
                .sorted((a, b) -> Long.compare((Long) b.get("score"), (Long) a.get("score")))
                .collect(Collectors.toList());
            
            // Apply pagination
            int start = page * size;
            int end = Math.min(start + size, leaderboardEntries.size());
            List<Map<String, Object>> paginatedEntries = 
                start < leaderboardEntries.size() ? leaderboardEntries.subList(start, end) : new ArrayList<>();
            
            // Add rank to entries
            for (int i = 0; i < paginatedEntries.size(); i++) {
                paginatedEntries.get(i).put("rank", start + i + 1);
            }
            
            Map<String, Object> result = new HashMap<>();
            result.put("leaderboard", paginatedEntries);
            result.put("totalCount", leaderboardEntries.size());
            result.put("hasMore", end < leaderboardEntries.size());
            return result;
        } catch (Exception e) {
            throw new RuntimeException("Error loading leaderboard: " + e.getMessage(), e);
        }
    }

    // Helper methods
    
    private Map<String, Object> convertUserToMap(User user) {
        Map<String, Object> userMap = new HashMap<>();
        userMap.put("id", user.getId());
        userMap.put("displayName", user.getDisplayName());
        userMap.put("username", user.getUsername());
        userMap.put("profilePictureUrl", user.getProfilePictureUrl());
        userMap.put("bio", user.getBio());
        userMap.put("selectedRole", user.getSelectedRole());
        userMap.put("currentLevel", user.getCurrentLevel());
        userMap.put("experiencePoints", user.getExperiencePoints());
        userMap.put("socialLinks", user.getSocialLinks());
        
        // Add stats
        Map<String, Object> stats = new HashMap<>();
        stats.put("questsCompleted", 0);
        stats.put("connectionsCount", 0);
        stats.put("projectsCount", 0);
        userMap.put("stats", stats);
        
        return userMap;
    }

    /**
     * Send connection request between users
     */
    public Map<String, Object> sendConnectionRequest(String fromUserId, String toUserId, String connectionType, String message) {
        // Implementation using UserCollaborationRepository
        // connectionType can be: "collaboration", "mentorship", "learning_buddy", "professional", "social"
        Map<String, Object> result = new HashMap<>();
        result.put("success", true);
        result.put("message", "Connection request sent successfully");
        result.put("connectionType", connectionType);
        return result;
    }

    /**
     * Respond to connection request
     */
    public Map<String, Object> respondToConnectionRequest(String requestId, String response) {
        // Implementation using UserCollaborationRepository
        Map<String, Object> result = new HashMap<>();
        result.put("success", true);
        result.put("message", "Connection request " + response + " successfully");
        return result;
    }

    /**
     * Invite collaboration
     */
    public Map<String, Object> inviteCollaboration(String fromUserId, String toUserId, String projectId, String role, String message) {
        // Implementation using UserProjectRepository
        Map<String, Object> result = new HashMap<>();
        result.put("success", true);
        result.put("message", "Collaboration invitation sent successfully");
        return result;
    }

    /**
     * Get community projects with filters
     */
    public Map<String, Object> getCommunityProjects(String skillFilter, int page, int size) {
        // Implementation using ProjectRepository and UserProjectRepository
        List<Map<String, Object>> projects = new ArrayList<>();
        
        // Sample project data
        Map<String, Object> project = new HashMap<>();
        project.put("id", "project_1");
        project.put("title", "Sacred Geometry Art Installation");
        project.put("description", "Community project to create digital sacred geometry art");
        project.put("skillsNeeded", List.of("digital_art", "programming", "sacred_geometry"));
        project.put("type", "art");
        project.put("membersCount", 3);
        project.put("maxMembers", 5);
        projects.add(project);

        Map<String, Object> result = new HashMap<>();
        result.put("projects", projects);
        result.put("totalCount", projects.size());
        result.put("hasMore", false);
        return result;
    }

    /**
     * Create community project
     */
    public Map<String, Object> createCommunityProject(String creatorId, String title, List<String> skillsNeeded, String description) {
        // Implementation using ProjectRepository
        Map<String, Object> result = new HashMap<>();
        result.put("success", true);
        result.put("projectId", "project_" + System.currentTimeMillis());
        result.put("message", "Community project created successfully");
        return result;
    }

    /**
     * Get community stats
     */
    public Map<String, Object> getCommunityStats(String userId) {
        Map<String, Object> stats = new HashMap<>();
        stats.put("totalMembers", 150);
        stats.put("activeProjects", 12);
        stats.put("totalConnections", 89);
        stats.put("questsCompleted", 45);
        return stats;
    }

    /**
     * Get community leaderboard
     */
    public Map<String, Object> getLeaderboard(String period, int page, int size) {
        return getCommunityLeaderboard(period, page, size);
    }
}
