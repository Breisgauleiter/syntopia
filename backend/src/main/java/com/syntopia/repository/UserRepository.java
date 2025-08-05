package com.syntopia.repository;

import com.arangodb.springframework.repository.ArangoRepository;
import com.syntopia.model.User;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository interface for User entities
 * 
 * Extends ArangoRepository to provide CRUD operations and custom queries
 * for user management in the TAO architecture.
 */
@Repository
public interface UserRepository extends ArangoRepository<User, String> {

    /**
     * Find user by username
     */
    Optional<User> findByUsername(String username);

    /**
     * Find user by email
     */
    Optional<User> findByEmail(String email);

    /**
     * Find user by GitHub ID
     */
    Optional<User> findByGithubId(String githubId);

    /**
     * Find all users by selected role
     */
    List<User> findBySelectedRole(String selectedRole);

    /**
     * Find users by minimum level
     */
    List<User> findByCurrentLevelGreaterThanEqual(int minLevel);

    /**
     * Find users with GitHub integration
     */
    List<User> findByIsGitHubIntegratedTrue();

    /**
     * Find top contributors by experience points
     */
    List<User> findTop10ByOrderByExperiencePointsDesc();

    /**
     * Check if username exists
     */
    boolean existsByUsername(String username);

    /**
     * Check if email exists
     */
    boolean existsByEmail(String email);
}
