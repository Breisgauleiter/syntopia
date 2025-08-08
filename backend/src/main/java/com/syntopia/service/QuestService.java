package com.syntopia.service;

import com.syntopia.model.Quest;
import com.syntopia.model.User;
import com.syntopia.repository.QuestRepository;
import com.syntopia.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
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
    // Quest Discovery & Filtering
    // ===============================

    /**
     * Get available quests for a specific user based on level and role
     */
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
}
