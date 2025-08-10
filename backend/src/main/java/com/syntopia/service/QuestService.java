package com.syntopia.service;

import com.syntopia.model.Quest;
import com.syntopia.model.User;
import com.syntopia.model.UserQuest;
import com.syntopia.model.OnboardingQuest;
import com.syntopia.repository.QuestRepository;
import com.syntopia.repository.UserRepository;
import com.syntopia.repository.UserQuestRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Quest Service for Syntopia Platform
 * 
 * Handles quest management, progression tracking, and user-quest interactions
 * in the gamified Sacred Geometry system.
 */
@Service
public class QuestService {

    @Autowired
    private QuestRepository questRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private UserService userService;

    @Autowired
    private UserQuestRepository userQuestRepository;

    // ===============================
    // Quest CRUD Operations
    // ===============================

    /**
     * Get all available quests
     */
    public List<Quest> getAllQuests() {
        return (List<Quest>) questRepository.findAll();
    }

    /**
     * Get quest by ID
     */
    public Optional<Quest> getQuestById(String questId) {
        return questRepository.findById(questId);
    }

    /**
     * Create a new quest
     */
    public Quest createQuest(Quest quest) {
        quest.setCreatedAt(LocalDateTime.now());
        quest.setUpdatedAt(LocalDateTime.now());
        quest.setStatus(Quest.QuestStatus.AVAILABLE);
        return questRepository.save(quest);
    }

    /**
     * Update an existing quest
     */
    public Quest updateQuest(String questId, Quest questUpdates) {
        return questRepository.findById(questId)
                .map(existingQuest -> {
                    // Update fields that are allowed to be modified
                    if (questUpdates.getTitle() != null) {
                        existingQuest.setTitle(questUpdates.getTitle());
                    }
                    if (questUpdates.getDescription() != null) {
                        existingQuest.setDescription(questUpdates.getDescription());
                    }
                    if (questUpdates.getExperienceReward() > 0) {
                        existingQuest.setExperienceReward(questUpdates.getExperienceReward());
                    }
                    if (questUpdates.getStatus() != null) {
                        existingQuest.setStatus(questUpdates.getStatus());
                    }
                    if (questUpdates.getDifficulty() != null) {
                        existingQuest.setDifficulty(questUpdates.getDifficulty());
                    }
                    
                    existingQuest.setUpdatedAt(LocalDateTime.now());
                    return questRepository.save(existingQuest);
                })
                .orElseThrow(() -> new IllegalArgumentException("Quest not found: " + questId));
    }

    /**
     * Delete a quest
     */
    public void deleteQuest(String questId) {
        questRepository.deleteById(questId);
    }

    // ===============================
    // User-Quest Relationship Management
    // ===============================

    /**
     * Get available quests for a specific user with their individual progress
     */
    public List<UserQuest> getAvailableQuestsForUserWithProgress(String userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found: " + userId));
        
        // Get all quests the user can access by level and role
        List<Quest> accessibleQuests;
        if (user.getSelectedRole() == null) {
            accessibleQuests = questRepository.findByRequiredLevelLessThanEqualAndStatus(
                    user.getCurrentLevel(), 
                    Quest.QuestStatus.AVAILABLE
            );
        } else {
            accessibleQuests = questRepository.findByRoleAndRequiredLevelLessThanEqualAndStatus(
                    user.getSelectedRole(), 
                    user.getCurrentLevel(), 
                    Quest.QuestStatus.AVAILABLE
            );
        }

        List<UserQuest> userQuests = new ArrayList<>();
        for (Quest quest : accessibleQuests) {
            // Check if user already has this quest relationship
            Optional<UserQuest> existingUserQuest = userQuestRepository.findByUserIdAndQuestId(userId, quest.getId());
            
            UserQuest userQuest;
            if (existingUserQuest.isPresent()) {
                userQuest = existingUserQuest.get();
            } else {
                // Create new UserQuest relationship for available quest
                userQuest = new UserQuest(user, quest);
                userQuest.setStatus(UserQuest.UserQuestStatus.USER_AVAILABLE);
            }
            
            // Quest data is already populated via the @To relationship
            userQuests.add(userQuest);
        }
        
        return userQuests;
    }

    /**
     * Start/Accept a quest for a user using UserQuest relationship
     */
    public UserQuest acceptUserQuest(String userId, String questId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found: " + userId));
        
        Quest quest = questRepository.findById(questId)
                .orElseThrow(() -> new IllegalArgumentException("Quest not found: " + questId));

        // Validate user can accept this quest
        if (user.getCurrentLevel() < quest.getRequiredLevel()) {
            throw new IllegalArgumentException("User level " + user.getCurrentLevel() + 
                    " is too low for quest requiring level " + quest.getRequiredLevel());
        }

        // Role validation
        if (quest.getRole() != null && 
            !quest.getRole().equals("All") && 
            !quest.getRole().equals(user.getSelectedRole())) {
            throw new IllegalArgumentException("Quest is not available for role: " + user.getSelectedRole());
        }

        // Check if quest has completion limits (for unique quests like GitHub issues)
        if (quest.isLimitReached()) {
            throw new IllegalArgumentException("Quest has reached maximum completion limit");
        }

        // Check if user already has this quest
        Optional<UserQuest> existingUserQuest = userQuestRepository.findByUserIdAndQuestId(userId, questId);
        
        UserQuest userQuest;
        if (existingUserQuest.isPresent()) {
            userQuest = existingUserQuest.get();
            if (userQuest.getStatus() == UserQuest.UserQuestStatus.USER_ACTIVE) {
                throw new IllegalArgumentException("User is already working on this quest");
            }
            if (userQuest.isCompleted()) {
                if (!quest.isReusable()) {
                    throw new IllegalArgumentException("Quest has already been completed and is not reusable");
                }
                // Reset for re-attempt
                userQuest.setStatus(UserQuest.UserQuestStatus.USER_ACTIVE);
                userQuest.setProgress(0);
                userQuest.setStartedAt(LocalDateTime.now());
            } else {
                userQuest.markAsStarted();
            }
        } else {
            // Create new UserQuest relationship
            userQuest = new UserQuest(user, quest);
            userQuest.markAsStarted();
        }

        UserQuest savedUserQuest = userQuestRepository.save(userQuest);
        
        // Quest data is already populated via the @To relationship
        
        return savedUserQuest;
    }

    /**
     * Complete a quest for a user using UserQuest relationship
     */
    public UserQuest completeUserQuest(String userId, String questId, Map<String, Object> completionData) {
        UserQuest userQuest = userQuestRepository.findByUserIdAndQuestId(userId, questId)
                .orElseThrow(() -> new IllegalArgumentException("User quest relationship not found"));

        if (!userQuest.isActive()) {
            throw new IllegalArgumentException("Quest is not active and cannot be completed");
        }

        Quest quest = questRepository.findById(questId)
                .orElseThrow(() -> new IllegalArgumentException("Quest not found: " + questId));

        // Mark UserQuest as completed
        userQuest.markAsCompleted(quest.getExperienceReward());
        userQuest.setCompletionData(completionData);

        // Update quest completion count
        quest.setCurrentCompletions(quest.getCurrentCompletions() + 1);
        questRepository.save(quest);

        // Award experience points to user
        userService.completeQuest(userId, quest.getExperienceReward());

        UserQuest savedUserQuest = userQuestRepository.save(userQuest);
        
        // Load quest data for frontend
        savedUserQuest.setQuest(quest);
        
        return savedUserQuest;
    }

    /**
     * Abandon a quest for a user
     */
    public UserQuest abandonUserQuest(String userId, String questId) {
        UserQuest userQuest = userQuestRepository.findByUserIdAndQuestId(userId, questId)
                .orElseThrow(() -> new IllegalArgumentException("User quest relationship not found"));

        if (!userQuest.isActive()) {
            throw new IllegalArgumentException("Quest is not active and cannot be abandoned");
        }

        userQuest.markAsAbandoned();
        UserQuest savedUserQuest = userQuestRepository.save(userQuest);
        
        // Load quest data for frontend  
        Quest quest = questRepository.findById(questId)
                .orElseThrow(() -> new IllegalArgumentException("Quest not found: " + questId));
        savedUserQuest.setQuest(quest);
        
        return savedUserQuest;
    }

    /**
     * Get user's quest progress and statistics
     */
    public Map<String, Object> getUserQuestStatistics(String userId) {
        Map<String, Object> stats = new HashMap<>();
        
        long completedCount = userQuestRepository.countByUserIdAndStatus(userId, UserQuest.UserQuestStatus.USER_COMPLETED);
        long activeCount = userQuestRepository.countByUserIdAndStatus(userId, UserQuest.UserQuestStatus.USER_ACTIVE);
        
        List<UserQuest> recentCompletions = userQuestRepository.findByUserIdAndStatusAndCompletedAtAfter(
            userId, 
            UserQuest.UserQuestStatus.USER_COMPLETED,
            LocalDateTime.now().minusDays(7)
        );

        stats.put("totalCompleted", completedCount);
        stats.put("activeQuests", activeCount);
        stats.put("completedThisWeek", recentCompletions.size());
        stats.put("recentCompletions", recentCompletions);
        
        return stats;
    }

    // ===============================
    // Quest Discovery & Filtering (Updated)
    // ===============================

    /**
     * Get available quests for a specific user based on level and role
     * @deprecated Use getAvailableQuestsForUserWithProgress instead
     */
    @Deprecated
    public List<Quest> getAvailableQuestsForUser(String userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found: " + userId));
        
        // If user has no selected role, return all available quests they can access by level
        if (user.getSelectedRole() == null) {
            return questRepository.findByRequiredLevelLessThanEqualAndStatus(
                    user.getCurrentLevel(), 
                    Quest.QuestStatus.AVAILABLE
            );
        }
        
        return questRepository.findByRoleAndRequiredLevelLessThanEqualAndStatus(
                user.getSelectedRole(), 
                user.getCurrentLevel(), 
                Quest.QuestStatus.AVAILABLE
        );
    }

    /**
     * Get quests by role
     */
    public List<Quest> getQuestsByRole(String role) {
        return questRepository.findByRole(role);
    }

    /**
     * Get quests by type
     */
    public List<Quest> getQuestsByType(Quest.QuestType type) {
        return questRepository.findByType(type);
    }

    /**
     * Get quests by difficulty
     */
    public List<Quest> getQuestsByDifficulty(Quest.QuestDifficulty difficulty) {
        return questRepository.findByDifficulty(difficulty);
    }

    /**
     * Get quests by required level (user can access)
     */
    public List<Quest> getQuestsByMaxLevel(int userLevel) {
        return questRepository.findByRequiredLevelLessThanEqual(userLevel);
    }

    /**
     * Get GitHub integration quests (Level 4+)
     */
    public List<Quest> getGitHubQuests() {
        return questRepository.findByTypeAndGithubRepositoryNotNull(Quest.QuestType.GITHUB_ISSUE);
    }

    /**
     * Get quests containing specific geometry patterns
     */
    public List<Quest> getQuestsByGeometryPattern(String pattern) {
        return questRepository.findByGeometryPatternsContaining(pattern);
    }

    // ===============================
    // Quest Progression & Completion
    // ===============================

    /**
     * Start/Accept a quest for a user
     */
    public User acceptQuest(String userId, String questId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found: " + userId));
        
        Quest quest = questRepository.findById(questId)
                .orElseThrow(() -> new IllegalArgumentException("Quest not found: " + questId));

        // Validate user can accept this quest
        if (user.getCurrentLevel() < quest.getRequiredLevel()) {
            throw new IllegalArgumentException("User level " + user.getCurrentLevel() + 
                    " is too low for quest requiring level " + quest.getRequiredLevel());
        }

        // Role validation - allow if quest has no role requirement OR matches user role OR quest is for "All"
        if (quest.getRole() != null && 
            !quest.getRole().equals("All") && 
            !quest.getRole().equals(user.getSelectedRole())) {
            throw new IllegalArgumentException("Quest is not available for role: " + user.getSelectedRole());
        }

        if (quest.getStatus() != Quest.QuestStatus.AVAILABLE) {
            throw new IllegalArgumentException("Quest is not available for acceptance");
        }

        // TODO: Create UserQuest edge relationship in ArangoDB
        // For now, we'll track this in a simple way
        // In future: use ArangoDB edge collection "user_quests"
        
        quest.setStatus(Quest.QuestStatus.ACTIVE);
        quest.setUpdatedAt(LocalDateTime.now());
        questRepository.save(quest);

        return user;
    }

    /**
     * Complete a quest for a user
     */
    public User completeQuest(String userId, String questId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found: " + userId));
        
        Quest quest = questRepository.findById(questId)
                .orElseThrow(() -> new IllegalArgumentException("Quest not found: " + questId));

        if (quest.getStatus() != Quest.QuestStatus.ACTIVE) {
            throw new IllegalArgumentException("Quest is not active and cannot be completed");
        }

        // Mark quest as completed
        quest.setStatus(Quest.QuestStatus.COMPLETED);
        quest.setUpdatedAt(LocalDateTime.now());
        questRepository.save(quest);

        // Award experience points to user
        return userService.completeQuest(userId, quest.getExperienceReward());
    }

    /**
     * Abandon/Cancel a quest for a user
     */
    public User abandonQuest(String userId, String questId) {
        // Verify user exists and get user for return
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found: " + userId));
        
        Quest quest = questRepository.findById(questId)
                .orElseThrow(() -> new IllegalArgumentException("Quest not found: " + questId));

        if (quest.getStatus() != Quest.QuestStatus.ACTIVE) {
            throw new IllegalArgumentException("Quest is not active and cannot be abandoned");
        }

        // Reset quest to available status
        quest.setStatus(Quest.QuestStatus.AVAILABLE);
        quest.setUpdatedAt(LocalDateTime.now());
        questRepository.save(quest);

        return user;
    }

    // ===============================
    // Quest Analytics & Statistics
    // ===============================

    /**
     * Get quest completion statistics
     */
    public long getCompletedQuestCount() {
        return questRepository.countByStatus(Quest.QuestStatus.COMPLETED);
    }

    /**
     * Get active quest count
     */
    public long getActiveQuestCount() {
        return questRepository.countByStatus(Quest.QuestStatus.ACTIVE);
    }

    /**
     * Get available quest count
     */
    public long getAvailableQuestCount() {
        return questRepository.countByStatus(Quest.QuestStatus.AVAILABLE);
    }

    // ===============================
    // GitHub Integration Methods
    // ===============================

    /**
     * Create quest from GitHub issue
     */
    public Quest createGitHubQuest(String title, String description, String githubIssueUrl, 
                                   String repository, int issueNumber, int requiredLevel) {
        Quest quest = new Quest();
        quest.setTitle(title);
        quest.setDescription(description);
        quest.setType(Quest.QuestType.GITHUB_ISSUE);
        quest.setGithubIssueUrl(githubIssueUrl);
        quest.setGithubRepository(repository);
        quest.setGithubIssueNumber(issueNumber);
        quest.setRequiredLevel(requiredLevel);
        quest.setRole("All"); // GitHub quests available to all roles
        quest.setExperienceReward(calculateGitHubQuestReward(requiredLevel));
        quest.setDifficulty(mapLevelToDifficulty(requiredLevel));
        
        // Set GitHub quest category and reusability
        quest.setCategory(Quest.QuestCategory.UNIQUE); // Only one user can complete GitHub issues
        quest.setReusable(false); // GitHub issues are unique, can't be completed multiple times
        quest.setMaxCompletions(1); // Maximum 1 completion
        quest.setCurrentCompletions(0);
        
        return createQuest(quest);
    }

    /**
     * Calculate experience reward for GitHub quests based on level
     */
    private long calculateGitHubQuestReward(int requiredLevel) {
        return switch (requiredLevel) {
            case 1, 2, 3 -> 100;
            case 4, 5, 6 -> 250;
            case 7, 8, 9 -> 500;
            case 10, 11, 12 -> 750;
            default -> 1000;
        };
    }

    /**
     * Map required level to quest difficulty
     */
    private Quest.QuestDifficulty mapLevelToDifficulty(int requiredLevel) {
        return switch (requiredLevel) {
            case 1, 2, 3, 4 -> Quest.QuestDifficulty.BEGINNER;
            case 5, 6, 7, 8 -> Quest.QuestDifficulty.INTERMEDIATE;
            case 9, 10, 11, 12 -> Quest.QuestDifficulty.ADVANCED;
            default -> Quest.QuestDifficulty.EXPERT;
        };
    }

    /**
     * Map onboarding level to specific quest type for proper categorization
     */
    private Quest.QuestType mapLevelToQuestType(int level) {
        return switch (level) {
            case 1 -> Quest.QuestType.LEARNING;     // Profile setup & basic understanding
            case 2 -> Quest.QuestType.SKILL_BUILDING; // SYN principles & skill development
            case 3 -> Quest.QuestType.NETWORKING;   // Community building & connections
            case 4 -> Quest.QuestType.INTEGRATION;  // GitHub integration & advanced features
            default -> Quest.QuestType.ONBOARDING; // Fallback
        };
    }

    // ===============================
    // Onboarding Quest Integration
    // ===============================

    @Autowired
    private OnboardingQuestGenerator onboardingQuestGenerator;

    /**
     * Seed onboarding quests into the regular quest database
     * This makes onboarding quests appear in the main quest list
     */
    public List<Quest> seedOnboardingQuests() {
        List<Quest> seededQuests = new ArrayList<>();
        String[] roles = {
            "TECH_DEVELOPMENT", "BUSINESS_DEVELOPMENT", "UX_DESIGN", 
            "DATA_SCIENCE", "LEGAL_ADVISORY", "FINANCE_ANALYSIS", "SUSTAINABILITY_LEAD"
        };

        for (String role : roles) {
            for (int level = 1; level <= 4; level++) {
                try {
                    // Check if quest already exists
                    String questId = "onboarding-" + role.toLowerCase().replace(" ", "-") + "-level-" + level;
                    if (questRepository.findById(questId).isPresent()) {
                        continue; // Skip if already exists
                    }

                    // Generate onboarding quest
                    String displayRole = mapRoleToDisplayName(role);
                    OnboardingQuest onboardingQuest = onboardingQuestGenerator.generateQuestForRoleAndLevel(displayRole, level);
                    
                    // Convert to regular Quest
                    Quest quest = convertOnboardingQuestToQuest(onboardingQuest, role, level);
                    quest.setId(questId);
                    
                    // Save to database
                    Quest savedQuest = questRepository.save(quest);
                    seededQuests.add(savedQuest);
                } catch (Exception e) {
                    System.err.println("Error seeding onboarding quest for " + role + " level " + level + ": " + e.getMessage());
                }
            }
        }
        return seededQuests;
    }

    /**
     * Convert OnboardingQuest to regular Quest for database storage
     */
    private Quest convertOnboardingQuestToQuest(OnboardingQuest onboardingQuest, String role, int level) {
        Quest quest = new Quest();
        quest.setTitle(onboardingQuest.getTitle());
        quest.setDescription(onboardingQuest.getDescription());
        quest.setType(mapLevelToQuestType(level)); // Use level-specific quest type
        quest.setDifficulty(mapLevelToDifficulty(level));
        quest.setRequiredLevel(level);
        quest.setExperienceReward(onboardingQuest.getXpReward());
        quest.setRole(role);
        quest.setStatus(Quest.QuestStatus.AVAILABLE);
        quest.setCreatedAt(LocalDateTime.now());
        quest.setUpdatedAt(LocalDateTime.now());
        
        // Set quest category and reusability
        quest.setCategory(Quest.QuestCategory.INDIVIDUAL); // Each user gets their own progress
        quest.setReusable(true); // Multiple users can complete onboarding quests
        quest.setMaxCompletions(0); // Unlimited completions for onboarding
        quest.setCurrentCompletions(0);
        
        // Add onboarding-specific metadata
        quest.getMetadata().put("synPrinciple", onboardingQuest.getSynPrinciple());
        quest.getMetadata().put("roleDialect", onboardingQuest.getRoleDialect());
        quest.getMetadata().put("iconType", onboardingQuest.getIconType());
        quest.getMetadata().put("profileRequired", onboardingQuest.isProfileRequired());
        quest.getMetadata().put("steps", onboardingQuest.getSteps());
        quest.getMetadata().put("isOnboardingQuest", true);
        
        return quest;
    }

    /**
     * Map enum role values to display names for OnboardingQuestGenerator
     */
    private String mapRoleToDisplayName(String enumRole) {
        switch (enumRole) {
            case "TECH_DEVELOPMENT":
                return "Tech Development";
            case "BUSINESS_DEVELOPMENT":
                return "Business Development";
            case "UX_DESIGN":
                return "UX Design";
            case "DATA_SCIENCE":
                return "Data Science";
            case "LEGAL_ADVISORY":
                return "Legal Advisory";
            case "FINANCE_ANALYSIS":
                return "Finance Analysis";
            case "SUSTAINABILITY_LEAD":
                return "Sustainability Lead";
            default:
                return enumRole; // fallback
        }
    }
}
