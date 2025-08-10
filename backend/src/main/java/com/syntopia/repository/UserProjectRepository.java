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
}
