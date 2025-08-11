package com.syntopia.repository;

import com.arangodb.springframework.repository.ArangoRepository;
import com.arangodb.springframework.annotation.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;

/**
 * Repository for user_projects edge collection
 * Manages user participation in community projects
 */
@Repository
public interface UserProjectRepository extends ArangoRepository<Map<String, Object>, String> {

    /**
     * Find all projects for a user
     */
    @Query("FOR p IN user_projects FILTER p._from == CONCAT('users/', @userId) RETURN p")
    List<Map<String, Object>> findByUserId(@Param("userId") String userId);

    /**
     * Find all users for a project
     */
    @Query("FOR p IN user_projects FILTER p._to == CONCAT('projects/', @projectId) RETURN p")
    List<Map<String, Object>> findByProjectId(@Param("projectId") String projectId);

    /**
     * Find user-project relationship
     */
    @Query("FOR p IN user_projects FILTER p._from == CONCAT('users/', @userId) AND p._to == CONCAT('projects/', @projectId) RETURN p")
    List<Map<String, Object>> findByUserIdAndProjectId(@Param("userId") String userId, @Param("projectId") String projectId);

    /**
     * Find projects by user role
     */
    @Query("FOR p IN user_projects FILTER p._from == CONCAT('users/', @userId) AND p.role == @role RETURN p")
    List<Map<String, Object>> findByUserIdAndRole(@Param("userId") String userId, @Param("role") String role);

    /**
     * Create new project with owner relationship
     */
    @Query("""
        INSERT {
            name: @title,
            description: @description,
            tags: @tags,
            visibility: @visibility,
            status: 'ACTIVE',
            createdAt: DATE_NOW()
        } INTO projects
        LET project = NEW
        
        INSERT {
            _from: CONCAT('users/', @ownerId),
            _to: project._id,
            role: 'owner',
            createdAt: DATE_NOW()
        } INTO user_projects
        
        RETURN project
    """)
    Map<String, Object> createProjectWithOwner(@Param("title") String title, @Param("description") String description, @Param("tags") String[] tags, @Param("visibility") String visibility, @Param("ownerId") String ownerId);

    /**
     * Add member to project
     */
    @Query("INSERT { _from: CONCAT('users/', @userId), _to: CONCAT('projects/', @projectId), role: @role, createdAt: DATE_NOW() } INTO user_projects RETURN NEW")
    Map<String, Object> addMember(@Param("userId") String userId, @Param("projectId") String projectId, @Param("role") String role);

    /**
     * List projects with member counts and owner details
     */
    @Query("""
        FOR p IN projects
                // When mine == true, only include projects where the user has a membership edge
                // Use a subquery to check membership instead of invalid lambda syntax
                FILTER (
                    @mine == false OR (
                        LENGTH(
                            FOR up IN user_projects
                                FILTER (@userId != null AND up._from == CONCAT('users/', @userId) AND up._to == p._id)
                                LIMIT 1
                                RETURN 1
                        ) > 0
                    )
                )
        
        LET memberCount = LENGTH(FOR up IN user_projects FILTER up._to == p._id RETURN 1)
        LET ownerEdge = FIRST(FOR up IN user_projects FILTER up._to == p._id AND up.role == 'owner' RETURN up)
        LET owner = DOCUMENT(ownerEdge._from)
        
        SORT p.createdAt DESC
        LIMIT @offset, @limit
        
        RETURN {
            id: p._key,
            title: p.name,
            description: p.description,
            tags: p.tags,
            visibility: p.visibility,
            createdAt: p.createdAt,
            membersCount: memberCount,
            owner: {
                id: owner._key,
                displayName: owner.displayName,
                avatarUrl: owner.profilePictureUrl
            }
        }
    """)
    List<Map<String, Object>> listProjectsWithDetails(@Param("userId") String userId, @Param("mine") boolean mine, @Param("offset") int offset, @Param("limit") int limit);

    /**
     * Count projects for pagination
     */
    @Query("""
        FOR p IN projects
                FILTER (
                    @mine == false OR (
                        LENGTH(
                            FOR up IN user_projects
                                FILTER (@userId != null AND up._from == CONCAT('users/', @userId) AND up._to == p._id)
                                LIMIT 1
                                RETURN 1
                        ) > 0
                    )
                )
        COLLECT WITH COUNT INTO total
        RETURN total
    """)
    Integer countProjects(@Param("userId") String userId, @Param("mine") boolean mine);

    /**
     * Get recent projects for feed
     */
    @Query("""
        FOR p IN projects
        FILTER p.createdAt >= DATE_SUBTRACT(DATE_NOW(), @windowDays, 'day')
        
        LET ownerEdge = FIRST(FOR up IN user_projects FILTER up._to == p._id AND up.role == 'owner' RETURN up)
        LET owner = DOCUMENT(ownerEdge._from)
        
        SORT p.createdAt DESC
        LIMIT @limit
        
        RETURN {
            type: 'project_created',
            timestamp: p.createdAt,
            project: { id: p._key, name: p.name },
            owner: { id: owner._key, displayName: owner.displayName }
        }
    """)
    List<Map<String, Object>> findRecentProjects(@Param("windowDays") int windowDays, @Param("limit") int limit);
}
