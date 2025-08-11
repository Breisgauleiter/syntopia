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

    /**
     * Create a new connection with status PENDING
     */
    @Query("INSERT { _from: CONCAT('users/', @fromUserId), _to: CONCAT('users/', @toUserId), type: @type, status: 'PENDING', createdAt: DATE_NOW(), updatedAt: DATE_NOW() } INTO user_collaborations RETURN NEW")
    Map<String, Object> createConnection(@Param("fromUserId") String fromUserId, @Param("toUserId") String toUserId, @Param("type") String type);

    /**
     * Update connection status (for accept/decline)
     */
    @Query("UPDATE @edgeId WITH { status: @status, updatedAt: DATE_NOW() } IN user_collaborations RETURN NEW")
    Map<String, Object> updateConnectionStatus(@Param("edgeId") String edgeId, @Param("status") String status);

    /**
     * Find connections with other user details and filtering
     */
    @Query("""
        FOR c IN user_collaborations
        FILTER (c._from == CONCAT('users/', @userId) OR c._to == CONCAT('users/', @userId))
        AND (@status == null OR c.status == @status)
        AND (@direction == 'all' OR 
             (@direction == 'out' AND c._from == CONCAT('users/', @userId)) OR
             (@direction == 'in' AND c._to == CONCAT('users/', @userId)))
        
        LET otherUserId = c._from == CONCAT('users/', @userId) ? c._to : c._from
        LET otherUser = DOCUMENT(otherUserId)
        
        SORT c.createdAt DESC
        LIMIT @offset, @limit
        
        RETURN {
            id: c._id,
            type: c.type,
            status: c.status,
            createdAt: c.createdAt,
            otherUser: {
                id: otherUser._key,
                displayName: otherUser.displayName,
                avatarUrl: otherUser.profilePictureUrl,
                currentLevel: otherUser.currentLevel,
                selectedRole: otherUser.selectedRole
            }
        }
    """)
    List<Map<String, Object>> findConnectionsWithUserDetails(@Param("userId") String userId, @Param("status") String status, @Param("direction") String direction, @Param("offset") int offset, @Param("limit") int limit);

    /**
     * Count connections for pagination
     */
    @Query("""
        FOR c IN user_collaborations
        FILTER (c._from == CONCAT('users/', @userId) OR c._to == CONCAT('users/', @userId))
        AND (@status == null OR c.status == @status)
        AND (@direction == 'all' OR 
             (@direction == 'out' AND c._from == CONCAT('users/', @userId)) OR
             (@direction == 'in' AND c._to == CONCAT('users/', @userId)))
        COLLECT WITH COUNT INTO total
        RETURN total
    """)
    Integer countConnections(@Param("userId") String userId, @Param("status") String status, @Param("direction") String direction);

    /**
     * Check if connection already exists between two users
     */
    @Query("FOR c IN user_collaborations FILTER (c._from == CONCAT('users/', @userId1) AND c._to == CONCAT('users/', @userId2)) OR (c._from == CONCAT('users/', @userId2) AND c._to == CONCAT('users/', @userId1)) RETURN c")
    List<Map<String, Object>> findExistingConnection(@Param("userId1") String userId1, @Param("userId2") String userId2);

    /**
     * Get recent accepted connections for feed
     */
    @Query("""
        FOR c IN user_collaborations
        FILTER c.status == 'ACCEPTED' AND c.updatedAt >= DATE_SUBTRACT(DATE_NOW(), @windowDays, 'day')
        
        LET fromUser = DOCUMENT(c._from)
        LET toUser = DOCUMENT(c._to)
        
        SORT c.updatedAt DESC
        LIMIT @limit
        
        RETURN {
            type: 'connection_accepted',
            timestamp: c.updatedAt,
            from: { id: fromUser._key, displayName: fromUser.displayName },
            to: { id: toUser._key, displayName: toUser.displayName }
        }
    """)
    List<Map<String, Object>> findRecentAcceptedConnections(@Param("windowDays") int windowDays, @Param("limit") int limit);
}
