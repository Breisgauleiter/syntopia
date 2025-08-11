package com.syntopia.service;

import com.syntopia.dto.CommunityDTO;
import com.syntopia.model.User;
import com.syntopia.repository.UserRepository;
import com.syntopia.repository.UserCollaborationRepository;
import com.syntopia.repository.UserProjectRepository;
import com.syntopia.repository.UserQuestRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * CommunityService - Business logic for Community Features
 * Now with real ArangoDB-backed data instead of mocks
 */
@Service
public class CommunityService {

    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private UserCollaborationRepository userCollaborationRepository;
    
    @Autowired
    private UserProjectRepository userProjectRepository;
    
    @Autowired
    private UserQuestRepository userQuestRepository;

    /**
     * Get community activity feed with real data from DB
     */
    public Map<String, Object> getCommunityFeed(String userId, int page, int size) {
        try {
            List<Map<String, Object>> feedItems = new ArrayList<>();
            
            // Get recent quest completions (last 7 days)
            List<Map<String, Object>> questCompletions = userQuestRepository.findRecentCompletedQuests(7, size / 3);
            feedItems.addAll(questCompletions);
            
            // Get recent projects (last 7 days)
            List<Map<String, Object>> newProjects = userProjectRepository.findRecentProjects(7, size / 3);
            feedItems.addAll(newProjects);
            
            // Get recent connections (last 7 days)
            List<Map<String, Object>> newConnections = userCollaborationRepository.findRecentAcceptedConnections(7, size / 3);
            feedItems.addAll(newConnections);
            
            // Sort all items by timestamp descending
            feedItems.sort((a, b) -> {
                LocalDateTime timeA = (LocalDateTime) a.get("timestamp");
                LocalDateTime timeB = (LocalDateTime) b.get("timestamp");
                return timeB.compareTo(timeA);
            });
            
            // Apply pagination
            int start = page * size;
            int end = Math.min(start + size, feedItems.size());
            List<Map<String, Object>> paginatedItems = 
                start < feedItems.size() ? feedItems.subList(start, end) : new ArrayList<>();
            
            Map<String, Object> result = new HashMap<>();
            result.put("activities", paginatedItems);
            result.put("totalCount", feedItems.size());
            result.put("hasMore", end < feedItems.size());
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
     * Get user connections with real data
     */
    public Map<String, Object> getUserConnections(String userId, String status, String direction, int page, int size) {
        try {
            int offset = page * size;
            List<Map<String, Object>> connections = userCollaborationRepository.findConnectionsWithUserDetails(
                userId, status, direction, offset, size);
            
            Integer totalCount = userCollaborationRepository.countConnections(userId, status, direction);
            
            Map<String, Object> result = new HashMap<>();
            result.put("connections", connections);
            result.put("totalCount", totalCount != null ? totalCount : 0);
            result.put("hasMore", (offset + size) < (totalCount != null ? totalCount : 0));
            return result;
        } catch (Exception e) {
            throw new RuntimeException("Error loading user connections: " + e.getMessage(), e);
        }
    }

    /**
     * Send connection request with real persistence
     */
    public Map<String, Object> sendConnectionRequest(String fromUserId, String toUserId, String connectionType) {
        try {
            // Validate users exist
            Optional<User> fromUser = userRepository.findById(fromUserId);
            Optional<User> toUser = userRepository.findById(toUserId);
            
            if (fromUser.isEmpty() || toUser.isEmpty()) {
                throw new RuntimeException("One or both users not found");
            }
            
            // Prevent self-connection
            if (fromUserId.equals(toUserId)) {
                throw new RuntimeException("Cannot connect to yourself");
            }
            
            // Check if connection already exists
            List<Map<String, Object>> existing = userCollaborationRepository.findExistingConnection(fromUserId, toUserId);
            if (!existing.isEmpty()) {
                throw new RuntimeException("Connection already exists between these users");
            }
            
            // Create the connection
            Map<String, Object> connection = userCollaborationRepository.createConnection(fromUserId, toUserId, connectionType);
            
            return connection;
        } catch (Exception e) {
            throw new RuntimeException("Error sending connection request: " + e.getMessage(), e);
        }
    }

    /**
     * Respond to connection request (accept/decline)
     */
    public Map<String, Object> respondToConnectionRequest(String connectionId, String action, String respondingUserId) {
        try {
            // Validate action
            if (!"accept".equals(action) && !"decline".equals(action)) {
                throw new RuntimeException("Invalid action. Must be 'accept' or 'decline'");
            }
            
            String status = "accept".equals(action) ? "ACCEPTED" : "DECLINED";
            Map<String, Object> updatedConnection = userCollaborationRepository.updateConnectionStatus(connectionId, status);
            
            return updatedConnection;
        } catch (Exception e) {
            throw new RuntimeException("Error responding to connection request: " + e.getMessage(), e);
        }
    }

    /**
     * Get community projects with real data
     */
    public Map<String, Object> getCommunityProjects(String userId, boolean mine, int page, int size) {
        try {
            int offset = page * size;
            List<Map<String, Object>> projects = userProjectRepository.listProjectsWithDetails(userId, mine, offset, size);
            Integer totalCount = userProjectRepository.countProjects(userId, mine);
            
            Map<String, Object> result = new HashMap<>();
            result.put("projects", projects);
            result.put("totalCount", totalCount != null ? totalCount : 0);
            result.put("hasMore", (offset + size) < (totalCount != null ? totalCount : 0));
            return result;
        } catch (Exception e) {
            throw new RuntimeException("Error loading community projects: " + e.getMessage(), e);
        }
    }

    /**
     * Create new community project with real persistence
     */
    public Map<String, Object> createCommunityProject(String ownerId, CommunityDTO.CreateProjectDTO projectData) {
        try {
            // Validate required fields
            if (projectData.getTitle() == null || projectData.getTitle().trim().isEmpty()) {
                throw new RuntimeException("Project title is required");
            }
            if (projectData.getDescription() == null || projectData.getDescription().trim().isEmpty()) {
                throw new RuntimeException("Project description is required");
            }
            
            // Set defaults
            String visibility = projectData.getVisibility() != null ? projectData.getVisibility() : "public";
            String[] tags = projectData.getTags() != null ? 
                projectData.getTags().toArray(new String[0]) : new String[0];
            
            // Create project with owner
            Map<String, Object> project = userProjectRepository.createProjectWithOwner(
                projectData.getTitle(), 
                projectData.getDescription(), 
                tags, 
                visibility, 
                ownerId
            );
            
            return project;
        } catch (Exception e) {
            throw new RuntimeException("Error creating community project: " + e.getMessage(), e);
        }
    }

    /**
     * Get community leaderboard with real aggregated data
     */
    public Map<String, Object> getCommunityLeaderboard(String window, int page, int size) {
        try {
            Integer windowDays = null;
            if ("week".equals(window)) {
                windowDays = 7;
            } else if ("month".equals(window)) {
                windowDays = 30;
            }
            
            int offset = page * size;
            List<Map<String, Object>> leaderboard = userQuestRepository.getLeaderboard(windowDays, offset, size);
            
            // Add rank to each entry
            for (int i = 0; i < leaderboard.size(); i++) {
                leaderboard.get(i).put("rank", offset + i + 1);
            }
            
            Map<String, Object> result = new HashMap<>();
            result.put("leaderboard", leaderboard);
            result.put("hasMore", leaderboard.size() == size); // Simple approximation
            return result;
        } catch (Exception e) {
            throw new RuntimeException("Error loading community leaderboard: " + e.getMessage(), e);
        }
    }

    /**
     * Get community stats with real data
     */
    public Map<String, Object> getCommunityStats() {
        try {
            // Count users
            long totalUsers = userRepository.count();
            
            // Count active users (users with quest activity in last 7 days)
            List<Map<String, Object>> recentActivity = userQuestRepository.findRecentCompletedQuests(7, 1000);
            long activeUsers7d = recentActivity.stream()
                .map(activity -> ((Map<String, Object>) activity.get("user")).get("id"))
                .distinct()
                .count();
            
            // Count projects
            Integer totalProjects = userProjectRepository.countProjects(null, false);
            
            // Count connections
            Integer totalConnections = userCollaborationRepository.countConnections(null, "ACCEPTED", "all");
            
            // Count completed quests
            Integer questsCompleted = userQuestRepository.countCompletedQuests();
            
            Map<String, Object> stats = new HashMap<>();
            stats.put("users", totalUsers);
            stats.put("activeUsers7d", activeUsers7d);
            stats.put("projects", totalProjects != null ? totalProjects : 0);
            stats.put("connections", totalConnections != null ? totalConnections : 0);
            stats.put("questsCompleted", questsCompleted != null ? questsCompleted : 0);
            
            return stats;
        } catch (Exception e) {
            throw new RuntimeException("Error loading community stats: " + e.getMessage(), e);
        }
    }

    /**
     * Convert User entity to Map for API responses
     */
    private Map<String, Object> convertUserToMap(User user) {
        Map<String, Object> userMap = new HashMap<>();
        userMap.put("id", user.getId());
        userMap.put("username", user.getUsername());
        userMap.put("displayName", user.getDisplayName());
        userMap.put("profilePictureUrl", user.getProfilePictureUrl());
        userMap.put("currentLevel", user.getCurrentLevel());
        userMap.put("experiencePoints", user.getExperiencePoints());
        userMap.put("selectedRole", user.getSelectedRole());
        userMap.put("bio", user.getBio());
        userMap.put("location", user.getLocation());
        return userMap;
    }
}
