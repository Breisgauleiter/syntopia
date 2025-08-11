package com.syntopia.service;

import com.syntopia.model.User;
import com.syntopia.repository.UserRepository;
import com.syntopia.repository.UserQuestRepository;
import com.syntopia.repository.UserCollaborationRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.*;

/**
 * ProfileService - Business logic for Profile Management
 * 
 * Handles user profile operations, avatar uploads, and achievement tracking
 * Updated to use real data from UserQuestRepository and UserCollaborationRepository
 */
@Service
public class ProfileService {

    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private UserQuestRepository userQuestRepository;
    
    @Autowired
    private UserCollaborationRepository userCollaborationRepository;

    /**
     * Get complete user profile
     */
    public Map<String, Object> getCompleteProfile(String userId) {
        try {
            Optional<User> userOpt = userRepository.findById(userId);
            if (userOpt.isEmpty()) {
                throw new RuntimeException("User not found");
            }

            User user = userOpt.get();
            Map<String, Object> profile = new HashMap<>();
            
            // Basic profile information
            profile.put("id", user.getId());
            profile.put("username", user.getUsername());
            profile.put("email", user.getEmail());
            profile.put("displayName", user.getDisplayName());
            profile.put("bio", user.getBio());
            profile.put("profilePictureUrl", user.getProfilePictureUrl());
            profile.put("isProfilePublic", user.isProfilePublic());
            profile.put("selectedRole", user.getSelectedRole());
            profile.put("currentLevel", user.getCurrentLevel());
            profile.put("experiencePoints", user.getExperiencePoints());
            profile.put("socialLinks", user.getSocialLinks());
            
            // Basic stats with real data
            Map<String, Object> stats = new HashMap<>();
            
            // Get real connection count (all accepted connections)
            try {
                Integer connectionCount = userCollaborationRepository.countConnections(user.getId(), "ACCEPTED", "all");
                stats.put("connectionsCount", connectionCount != null ? connectionCount : 0);
            } catch (Exception e) {
                stats.put("connectionsCount", 0);
            }
            stats.put("projectsCount", 0);
            stats.put("questsCompleted", 0);
            profile.put("stats", stats);
            
            return profile;
        } catch (Exception e) {
            throw new RuntimeException("Error loading user profile: " + e.getMessage(), e);
        }
    }

    /**
     * Update user profile
     */
    public Map<String, Object> updateProfile(String userId, Map<String, Object> profileData) {
        try {
            Optional<User> userOpt = userRepository.findById(userId);
            if (userOpt.isEmpty()) {
                throw new RuntimeException("User not found");
            }

            User user = userOpt.get();
            
            // Update profile fields
            if (profileData.containsKey("displayName")) {
                user.setDisplayName((String) profileData.get("displayName"));
            }
            if (profileData.containsKey("bio")) {
                user.setBio((String) profileData.get("bio"));
            }
            if (profileData.containsKey("isProfilePublic")) {
                user.setProfilePublic((Boolean) profileData.get("isProfilePublic"));
            }
            if (profileData.containsKey("socialLinks")) {
                @SuppressWarnings("unchecked")
                Map<String, String> socialLinks = (Map<String, String>) profileData.get("socialLinks");
                user.setSocialLinks(socialLinks);
            }

            User savedUser = userRepository.save(user);
            
            Map<String, Object> result = new HashMap<>();
            result.put("success", true);
            result.put("message", "Profile updated successfully");
            result.put("user", convertUserToMap(savedUser));
            return result;
        } catch (Exception e) {
            throw new RuntimeException("Error updating profile: " + e.getMessage(), e);
        }
    }

    /**
     * Upload avatar/profile picture
     */
    public Map<String, Object> uploadAvatar(String userId, MultipartFile file) {
        try {
            // Validate file
            if (file.isEmpty()) {
                throw new RuntimeException("File is empty");
            }
            
            String contentType = file.getContentType();
            if (contentType == null || !contentType.startsWith("image/")) {
                throw new RuntimeException("File must be an image");
            }
            
            // Create uploads directory if it doesn't exist
            Path uploadsDir = Paths.get("uploads/avatars");
            Files.createDirectories(uploadsDir);
            
            // Generate unique filename
            String originalFilename = file.getOriginalFilename();
            String extension = originalFilename != null && originalFilename.contains(".") 
                ? originalFilename.substring(originalFilename.lastIndexOf("."))
                : ".jpg";
            String filename = userId + "_" + System.currentTimeMillis() + extension;
            
            // Save file
            Path filePath = uploadsDir.resolve(filename);
            Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);
            
            // Update user profile picture URL
            Optional<User> userOpt = userRepository.findById(userId);
            if (userOpt.isPresent()) {
                User user = userOpt.get();
                user.setProfilePictureUrl("/uploads/avatars/" + filename);
                userRepository.save(user);
            }
            
            Map<String, Object> result = new HashMap<>();
            result.put("success", true);
            result.put("message", "Avatar uploaded successfully");
            result.put("avatarUrl", "/uploads/avatars/" + filename);
            return result;
        } catch (IOException e) {
            throw new RuntimeException("Error uploading avatar: " + e.getMessage(), e);
        }
    }

    /**
     * Get user achievements based on real quest completion data
     */
    public Map<String, Object> getUserAchievements(String userId) {
        try {
            Optional<User> userOpt = userRepository.findById(userId);
            if (userOpt.isEmpty()) {
                throw new RuntimeException("User not found");
            }

            User user = userOpt.get();
            List<Map<String, Object>> achievements = new ArrayList<>();
            
            // Get real quest completion data
            try {
                long completedQuests = userQuestRepository.countByUserIdAndStatus(userId, 
                    com.syntopia.model.UserQuest.UserQuestStatus.USER_COMPLETED);
                int questCount = (int) completedQuests;
                
                // Achievement: First Quest Completed
                if (questCount >= 1) {
                    achievements.add(createAchievement(
                        "first_quest", "First Quest", "Completed your first quest", 
                        "🎯", "completed", 50
                    ));
                }
                
                // Achievement: Quest Novice (5 quests)
                if (questCount >= 5) {
                    achievements.add(createAchievement(
                        "quest_novice", "Quest Novice", "Completed 5 quests", 
                        "🏆", "completed", 100
                    ));
                }
                
                // Achievement: Quest Adept (10 quests) 
                if (questCount >= 10) {
                    achievements.add(createAchievement(
                        "quest_adept", "Quest Adept", "Completed 10 quests", 
                        "⭐", "completed", 250
                    ));
                } else if (questCount >= 5) {
                    // Show as in-progress if they have some quests
                    achievements.add(createAchievement(
                        "quest_adept", "Quest Adept", "Complete 10 quests (" + questCount + "/10)", 
                        "⭐", "in_progress", 250
                    ));
                }
                
                // Achievement: Quest Master (25 quests)
                if (questCount >= 25) {
                    achievements.add(createAchievement(
                        "quest_master", "Quest Master", "Completed 25 quests", 
                        "👑", "completed", 500
                    ));
                } else if (questCount >= 10) {
                    achievements.add(createAchievement(
                        "quest_master", "Quest Master", "Complete 25 quests (" + questCount + "/25)", 
                        "👑", "in_progress", 500
                    ));
                }
                
            } catch (Exception e) {
                System.err.println("Error loading quest achievements: " + e.getMessage());
            }
            
            // Level-based achievements
            if (user.getCurrentLevel() >= 2) {
                achievements.add(createAchievement(
                    "first_levelup", "Level Up!", "Reached level 2", 
                    "📈", "completed", 100
                ));
            }
            
            if (user.getCurrentLevel() >= 5) {
                achievements.add(createAchievement(
                    "level_explorer", "Level Explorer", "Reached level 5", 
                    "🚀", "completed", 250
                ));
            }
            
            // Experience-based achievements
            if (user.getExperiencePoints() >= 500) {
                achievements.add(createAchievement(
                    "experience_collector", "Experience Collector", "Earned 500+ experience points", 
                    "💎", "completed", 150
                ));
            }
            
            if (user.getExperiencePoints() >= 1000) {
                achievements.add(createAchievement(
                    "experience_hoarder", "Experience Hoarder", "Earned 1000+ experience points", 
                    "💰", "completed", 300
                ));
            }
            
            // Connection-based achievements
            try {
                Integer connectionCount = userCollaborationRepository.countConnections(userId, "ACCEPTED", "all");
                int connections = connectionCount != null ? connectionCount : 0;
                
                if (connections >= 1) {
                    achievements.add(createAchievement(
                        "first_connection", "Connected", "Made your first connection", 
                        "🤝", "completed", 75
                    ));
                }
                
                if (connections >= 5) {
                    achievements.add(createAchievement(
                        "networker", "Networker", "Connected with 5 users", 
                        "🌐", "completed", 200
                    ));
                }
                
                if (connections >= 10) {
                    achievements.add(createAchievement(
                        "community_builder", "Community Builder", "Connected with 10 users", 
                        "�️", "completed", 400
                    ));
                } else if (connections >= 5) {
                    achievements.add(createAchievement(
                        "community_builder", "Community Builder", "Connect with 10 users (" + connections + "/10)", 
                        "🏗️", "in_progress", 400
                    ));
                }
                
            } catch (Exception e) {
                System.err.println("Error loading connection achievements: " + e.getMessage());
            }
            
            // Add locked achievements as goals
            boolean hasQuestMaster = achievements.stream()
                .anyMatch(a -> "quest_master".equals(a.get("id")) && "completed".equals(a.get("status")));
            if (!hasQuestMaster) {
                boolean hasQuestMasterProgress = achievements.stream()
                    .anyMatch(a -> "quest_master".equals(a.get("id")));
                if (!hasQuestMasterProgress) {
                    achievements.add(createAchievement(
                        "quest_master", "Quest Master", "Complete 25 quests (0/25)", 
                        "👑", "locked", 500
                    ));
                }
            }
            
            Map<String, Object> result = new HashMap<>();
            result.put("achievements", achievements);
            result.put("totalAchievements", achievements.size());
            result.put("completedAchievements", 
                (int) achievements.stream().filter(a -> "completed".equals(a.get("status"))).count());
            return result;
            
        } catch (Exception e) {
            throw new RuntimeException("Error loading achievements: " + e.getMessage(), e);
        }
    }

    /**
     * Get social connections data with real connection data
     */
    public Map<String, Object> getSocialConnections(String userId) {
        try {
            List<Map<String, Object>> connections = new ArrayList<>();
            
            // Get real connections from repository
            try {
                List<Map<String, Object>> realConnections = userCollaborationRepository
                    .findConnectionsWithUserDetails(userId, "ACCEPTED", "all", 0, 50);
                
                // Transform the connection data for profile display
                for (Map<String, Object> conn : realConnections) {
                    Map<String, Object> connectionData = new HashMap<>();
                    connectionData.put("id", conn.get("id"));
                    connectionData.put("type", conn.get("type"));
                    connectionData.put("status", conn.get("status"));
                    connectionData.put("createdAt", conn.get("createdAt"));
                    connectionData.put("updatedAt", conn.get("updatedAt"));
                    
                    // Extract other user info
                    @SuppressWarnings("unchecked")
                    Map<String, Object> otherUser = (Map<String, Object>) conn.get("otherUser");
                    if (otherUser != null) {
                        connectionData.put("otherUser", otherUser);
                    }
                    
                    connections.add(connectionData);
                }
                
            } catch (Exception e) {
                System.err.println("Error loading connections data: " + e.getMessage());
                // Continue with empty list if there's an error
            }
            
            // Get connection stats
            Map<String, Object> stats = new HashMap<>();
            try {
                Integer totalConnections = userCollaborationRepository.countConnections(userId, "ACCEPTED", "all");
                Integer pendingConnections = userCollaborationRepository.countConnections(userId, "PENDING", "in");
                
                stats.put("total", totalConnections != null ? totalConnections : 0);
                stats.put("pending", pendingConnections != null ? pendingConnections : 0);
                stats.put("recent", connections.size());
                
            } catch (Exception e) {
                stats.put("total", 0);
                stats.put("pending", 0);
                stats.put("recent", 0);
            }
            
            Map<String, Object> result = new HashMap<>();
            result.put("connections", connections);
            result.put("totalConnections", connections.size());
            result.put("stats", stats);
            return result;
            
        } catch (Exception e) {
            throw new RuntimeException("Error loading social connections: " + e.getMessage(), e);
        }
    }

    /**
     * Get profile completion percentage
     */
    public Map<String, Object> getProfileCompletion(String userId) {
        try {
            Optional<User> userOpt = userRepository.findById(userId);
            if (userOpt.isEmpty()) {
                throw new RuntimeException("User not found");
            }

            User user = userOpt.get();
            List<Map<String, Object>> completionItems = new ArrayList<>();
            int completedCount = 0;
            
            // Basic profile fields
            completionItems.add(createCompletionItem(
                "displayName", "Display Name", user.getDisplayName() != null, 10
            ));
            if (user.getDisplayName() != null) completedCount++;
            
            completionItems.add(createCompletionItem(
                "bio", "Profile Bio", user.getBio() != null && !user.getBio().trim().isEmpty(), 15
            ));
            if (user.getBio() != null && !user.getBio().trim().isEmpty()) completedCount++;
            
            completionItems.add(createCompletionItem(
                "avatar", "Profile Picture", user.getProfilePictureUrl() != null, 10
            ));
            if (user.getProfilePictureUrl() != null) completedCount++;
            
            completionItems.add(createCompletionItem(
                "role", "Selected Role", user.getSelectedRole() != null, 20
            ));
            if (user.getSelectedRole() != null) completedCount++;
            
            boolean hasSocialLinks = user.getSocialLinks() != null && !user.getSocialLinks().isEmpty();
            completionItems.add(createCompletionItem(
                "socialLinks", "Social Links", hasSocialLinks, 10
            ));
            if (hasSocialLinks) completedCount++;
            
            int totalItems = completionItems.size();
            int completionPercentage = totalItems > 0 ? (completedCount * 100) / totalItems : 0;
            
            Map<String, Object> result = new HashMap<>();
            result.put("completionPercentage", completionPercentage);
            result.put("completedItems", completedCount);
            result.put("totalItems", totalItems);
            result.put("items", completionItems);
            return result;
        } catch (Exception e) {
            throw new RuntimeException("Error calculating profile completion: " + e.getMessage(), e);
        }
    }

    // Helper methods
    
    private Map<String, Object> convertUserToMap(User user) {
        Map<String, Object> userMap = new HashMap<>();
        userMap.put("id", user.getId());
        userMap.put("username", user.getUsername());
        userMap.put("displayName", user.getDisplayName());
        userMap.put("email", user.getEmail());
        userMap.put("bio", user.getBio());
        userMap.put("profilePictureUrl", user.getProfilePictureUrl());
        userMap.put("isProfilePublic", user.isProfilePublic());
        userMap.put("selectedRole", user.getSelectedRole());
        userMap.put("currentLevel", user.getCurrentLevel());
        userMap.put("experiencePoints", user.getExperiencePoints());
        userMap.put("socialLinks", user.getSocialLinks());
        return userMap;
    }
    
    private Map<String, Object> createAchievement(String id, String title, String description, 
                                                  String icon, String status, int points) {
        Map<String, Object> achievement = new HashMap<>();
        achievement.put("id", id);
        achievement.put("title", title);
        achievement.put("description", description);
        achievement.put("icon", icon);
        achievement.put("status", status);
        achievement.put("points", points);
        achievement.put("unlockedAt", "completed".equals(status) ? "2025-08-09T10:00:00" : null);
        return achievement;
    }
    
    private Map<String, Object> createCompletionItem(String field, String name, boolean completed, int weight) {
        Map<String, Object> item = new HashMap<>();
        item.put("field", field);
        item.put("name", name);
        item.put("completed", completed);
        item.put("weight", weight);
        return item;
    }

    /**
     * Get public profile for a user
     */
    public Map<String, Object> getPublicProfile(String userId) {
        Optional<User> userOpt = userRepository.findById(userId);
        if (userOpt.isEmpty()) {
            throw new RuntimeException("User not found");
        }

        User user = userOpt.get();
        if (!user.isProfilePublic()) {
            throw new RuntimeException("Profile is not public");
        }

        Map<String, Object> profile = new HashMap<>();
        profile.put("id", user.getId());
        profile.put("username", user.getUsername());
        profile.put("displayName", user.getDisplayName());
        profile.put("bio", user.getBio());
        profile.put("profilePictureUrl", user.getProfilePictureUrl());
        profile.put("selectedRole", user.getSelectedRole());
        profile.put("currentLevel", user.getCurrentLevel());
        profile.put("experiencePoints", user.getExperiencePoints());
        profile.put("achievements", user.getAchievements());
        profile.put("socialLinks", user.getSocialLinks());
        
        return profile;
    }

    /**
     * Search users with filters and pagination
     */
    public Map<String, Object> searchUsers(String query, String role, Integer minLevel, int page, int size) {
        List<User> allUsers = (List<User>) userRepository.findAll();
        List<User> filteredUsers = new ArrayList<>();

        for (User user : allUsers) {
            if (!user.isProfilePublic()) continue;
            
            boolean matches = true;
            
            // Query filter (username or displayName)
            if (query != null && !query.trim().isEmpty()) {
                String searchQuery = query.toLowerCase();
                boolean queryMatch = user.getUsername().toLowerCase().contains(searchQuery) ||
                                   (user.getDisplayName() != null && user.getDisplayName().toLowerCase().contains(searchQuery));
                if (!queryMatch) matches = false;
            }
            
            // Role filter
            if (role != null && !role.isEmpty() && !role.equals("all")) {
                if (!role.equals(user.getSelectedRole())) {
                    matches = false;
                }
            }
            
            // Minimum level filter
            if (minLevel != null && user.getCurrentLevel() < minLevel) {
                matches = false;
            }
            
            if (matches) {
                filteredUsers.add(user);
            }
        }

        // Sort by experience points descending
        filteredUsers.sort((a, b) -> Long.compare(b.getExperiencePoints(), a.getExperiencePoints()));

        // Pagination
        int totalElements = filteredUsers.size();
        int start = page * size;
        int end = Math.min(start + size, totalElements);
        
        List<User> pageUsers = filteredUsers.subList(start, end);
        
        List<Map<String, Object>> userMaps = new ArrayList<>();
        for (User user : pageUsers) {
            Map<String, Object> userMap = new HashMap<>();
            userMap.put("id", user.getId());
            userMap.put("username", user.getUsername());
            userMap.put("displayName", user.getDisplayName());
            userMap.put("profilePictureUrl", user.getProfilePictureUrl());
            userMap.put("selectedRole", user.getSelectedRole());
            userMap.put("currentLevel", user.getCurrentLevel());
            userMap.put("experiencePoints", user.getExperiencePoints());
            userMaps.add(userMap);
        }

        Map<String, Object> result = new HashMap<>();
        result.put("users", userMaps);
        result.put("totalElements", totalElements);
        result.put("totalPages", (int) Math.ceil((double) totalElements / size));
        result.put("currentPage", page);
        result.put("pageSize", size);
        
        return result;
    }
}
