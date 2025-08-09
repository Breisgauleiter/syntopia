package com.syntopia.repository;

import com.syntopia.model.UserQuest;
import com.arangodb.springframework.repository.ArangoRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Repository for UserQuest edge relationships in ArangoDB
 * 
 * Manages user-specific quest progress and status tracking
 */
@Repository
public interface UserQuestRepository extends ArangoRepository<UserQuest, String> {

    // ===============================
    // User-specific Quest Queries
    // ===============================

    /**
     * Find all quests for a specific user
     */
    List<UserQuest> findByUserId(String userId);

    /**
     * Find all users working on a specific quest
     */
    List<UserQuest> findByQuestId(String questId);

    /**
     * Find specific user-quest relationship
     */
    Optional<UserQuest> findByUserIdAndQuestId(String userId, String questId);

    /**
     * Find user's quests by status
     */
    List<UserQuest> findByUserIdAndStatus(String userId, UserQuest.UserQuestStatus status);

    /**
     * Find user's active quests
     */
    default List<UserQuest> findActiveQuestsByUser(String userId) {
        return findByUserIdAndStatus(userId, UserQuest.UserQuestStatus.USER_ACTIVE);
    }

    /**
     * Find user's completed quests
     */
    default List<UserQuest> findCompletedQuestsByUser(String userId) {
        return findByUserIdAndStatus(userId, UserQuest.UserQuestStatus.USER_COMPLETED);
    }

    /**
     * Find user's available quests
     */
    default List<UserQuest> findAvailableQuestsByUser(String userId) {
        return findByUserIdAndStatus(userId, UserQuest.UserQuestStatus.USER_AVAILABLE);
    }

    // ===============================
    // Quest-specific User Queries
    // ===============================

    /**
     * Find all users who completed a specific quest
     */
    default List<UserQuest> findUsersWhoCompletedQuest(String questId) {
        return findByQuestIdAndStatus(questId, UserQuest.UserQuestStatus.USER_COMPLETED);
    }

    /**
     * Find all users currently working on a quest
     */
    default List<UserQuest> findUsersWorkingOnQuest(String questId) {
        return findByQuestIdAndStatus(questId, UserQuest.UserQuestStatus.USER_ACTIVE);
    }

    /**
     * Find quest relationships by quest and status
     */
    List<UserQuest> findByQuestIdAndStatus(String questId, UserQuest.UserQuestStatus status);

    // ===============================
    // Statistics & Analytics
    // ===============================

    /**
     * Count completed quests for a user
     */
    long countByUserIdAndStatus(String userId, UserQuest.UserQuestStatus status);

    /**
     * Count how many users completed a specific quest
     */
    long countByQuestIdAndStatus(String questId, UserQuest.UserQuestStatus status);

    /**
     * Find recently completed quests for a user
     */
    List<UserQuest> findByUserIdAndStatusAndCompletedAtAfter(
            String userId, 
            UserQuest.UserQuestStatus status, 
            LocalDateTime after
    );

    /**
     * Find quests with progress above threshold
     */
    List<UserQuest> findByUserIdAndProgressGreaterThan(String userId, int progressThreshold);

    // ===============================
    // GitHub Quest Specific
    // ===============================

    /**
     * Find user quests that need verification (GitHub quests)
     */
    List<UserQuest> findByStatusAndIsVerifiedFalse(UserQuest.UserQuestStatus status);

    /**
     * Find GitHub quests by pull request URL
     */
    Optional<UserQuest> findByGithubPullRequestUrl(String githubPullRequestUrl);

    /**
     * Find GitHub quests by commit SHA
     */
    List<UserQuest> findByGithubCommitSha(String githubCommitSha);

    // ===============================
    // Pagination Support
    // ===============================

    /**
     * Find user's quests with pagination
     */
    Page<UserQuest> findByUserId(String userId, Pageable pageable);

    /**
     * Find user's quests by status with pagination
     */
    Page<UserQuest> findByUserIdAndStatus(String userId, UserQuest.UserQuestStatus status, Pageable pageable);

    // ===============================
    // Custom AQL Queries (if needed)
    // ===============================

    // Example custom query - can be added later if needed:
    // @Query("FOR userQuest IN user_quests FILTER userQuest.userId == @userId AND userQuest.progress >= @minProgress RETURN userQuest")
    // List<UserQuest> findByUserIdAndMinProgress(@Param("userId") String userId, @Param("minProgress") int minProgress);
}
