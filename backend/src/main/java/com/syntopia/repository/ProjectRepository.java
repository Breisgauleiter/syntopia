package com.syntopia.repository;

import com.arangodb.springframework.repository.ArangoRepository;
import com.arangodb.springframework.annotation.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;

/**
 * Repository for projects collection
 * Manages community projects
 */
@Repository
public interface ProjectRepository extends ArangoRepository<Map<String, Object>, String> {

    /**
     * Find projects by status
     */
    @Query("FOR p IN projects FILTER p.status == @status RETURN p")
    List<Map<String, Object>> findByStatus(@Param("status") String status);

    /**
     * Find projects by title (contains search)
     */
    @Query("FOR p IN projects FILTER CONTAINS(LOWER(p.title), LOWER(@title)) RETURN p")
    List<Map<String, Object>> findByTitleContaining(@Param("title") String title);

    /**
     * Find projects created by user
     */
    @Query("FOR p IN projects FILTER p.createdBy == @userId RETURN p")
    List<Map<String, Object>> findByCreatedBy(@Param("userId") String userId);
}
