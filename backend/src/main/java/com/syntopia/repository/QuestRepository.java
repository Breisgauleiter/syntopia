package com.syntopia.repository;

import com.arangodb.springframework.repository.ArangoRepository;
import com.syntopia.model.Quest;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repository interface for Quest entities
 * 
 * Provides CRUD operations and custom queries for quest management
 * in the gamified Syntopia system.
 */
@Repository
public interface QuestRepository extends ArangoRepository<Quest, String> {

    /**
     * Find quests by role
     */
    List<Quest> findByRole(String role);

    /**
     * Find quests by required level
     */
    List<Quest> findByRequiredLevelLessThanEqual(int userLevel);

    /**
     * Find quests by status
     */
    List<Quest> findByStatus(Quest.QuestStatus status);

    /**
     * Find available quests by level and status (for users without selected role)
     */
    List<Quest> findByRequiredLevelLessThanEqualAndStatus(int userLevel, Quest.QuestStatus status);

    /**
     * Find quests by type
     */
    List<Quest> findByType(Quest.QuestType type);

    /**
     * Find quests by difficulty
     */
    List<Quest> findByDifficulty(Quest.QuestDifficulty difficulty);

    /**
     * Find available quests for a specific role and level
     */
    List<Quest> findByRoleAndRequiredLevelLessThanEqualAndStatus(
            String role, int userLevel, Quest.QuestStatus status);

    /**
     * Find a quest by exact role, exact required level and status
     */
    List<Quest> findByRoleAndRequiredLevelAndStatus(
            String role, int level, Quest.QuestStatus status);

    /**
     * Find GitHub quests
     */
    List<Quest> findByTypeAndGithubRepositoryNotNull(Quest.QuestType type);

    /**
     * Find quests containing specific geometry patterns
     */
    List<Quest> findByGeometryPatternsContaining(String pattern);

    /**
     * Count quests by status
     */
    long countByStatus(Quest.QuestStatus status);

    /**
     * Count quests by type
     */
    long countByType(Quest.QuestType type);

    /**
     * Count quests by difficulty
     */
    long countByDifficulty(Quest.QuestDifficulty difficulty);
}
