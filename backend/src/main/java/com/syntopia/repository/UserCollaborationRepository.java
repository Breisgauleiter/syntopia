package com.syntopia.repository;

import com.arangodb.springframework.repository.ArangoRepository;
import com.arangodb.springframework.annotation.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;

/**
 * Repository for user_collaborations edge collection
 * Manages user connections, mentorship, and collaboration relationships
 */
@Repository
public interface UserCollaborationRepository extends ArangoRepository<Map<String, Object>, String> {

    /**
     * Find all collaborations for a user (both outgoing and incoming)
     */
    @Query("FOR c IN user_collaborations FILTER c._from == CONCAT('users/', @userId) OR c._to == CONCAT('users/', @userId) RETURN c")
    List<Map<String, Object>> findByUserId(@Param("userId") String userId);

    /**
     * Find all outgoing connections from a user
     */
    @Query("FOR c IN user_collaborations FILTER c._from == CONCAT('users/', @userId) RETURN c")
    List<Map<String, Object>> findByFromUserId(@Param("userId") String userId);

    /**
     * Find all incoming connections to a user
     */
    @Query("FOR c IN user_collaborations FILTER c._to == CONCAT('users/', @userId) RETURN c")
    List<Map<String, Object>> findByToUserId(@Param("userId") String userId);

    /**
     * Find specific collaboration between two users
     */
    @Query("FOR c IN user_collaborations FILTER c._from == CONCAT('users/', @fromUserId) AND c._to == CONCAT('users/', @toUserId) RETURN c")
    List<Map<String, Object>> findByFromUserIdAndToUserId(@Param("fromUserId") String fromUserId, @Param("toUserId") String toUserId);

    /**
     * Find collaborations by type
     */
    @Query("FOR c IN user_collaborations FILTER c._from == CONCAT('users/', @userId) AND c.type == @type RETURN c")
    List<Map<String, Object>> findByUserIdAndType(@Param("userId") String userId, @Param("type") String type);

    /**
     * Find collaborations by status
     */
    @Query("FOR c IN user_collaborations FILTER c._from == CONCAT('users/', @userId) AND c.status == @status RETURN c")
    List<Map<String, Object>> findByUserIdAndStatus(@Param("userId") String userId, @Param("status") String status);
}
