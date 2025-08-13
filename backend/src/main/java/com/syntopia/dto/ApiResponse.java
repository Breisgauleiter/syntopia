package com.syntopia.dto;

import java.util.HashMap;
import java.util.Map;

/**
 * Standardized API Response wrapper for consistent response patterns
 * across all Syntopia controllers.
 * 
 * Provides unified success/error response structure to match Quest system patterns.
 */
public class ApiResponse {
    
    /**
     * Create a successful response with data
     */
    public static Map<String, Object> success(Object data) {
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("data", data);
        return response;
    }
    
    /**
     * Create a successful response with data and custom message
     */
    public static Map<String, Object> success(String message, Object data) {
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("message", message);
        response.put("data", data);
        return response;
    }
    
    /**
     * Create a successful response with just a message
     */
    public static Map<String, Object> success(String message) {
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("message", message);
        return response;
    }
    
    /**
     * Create an error response
     */
    public static Map<String, Object> error(String message) {
        Map<String, Object> response = new HashMap<>();
        response.put("success", false);
        response.put("error", message);
        return response;
    }
    
    /**
     * Create an error response with additional details
     */
    public static Map<String, Object> error(String message, String details) {
        Map<String, Object> response = new HashMap<>();
        response.put("success", false);
        response.put("error", message);
        response.put("details", details);
        return response;
    }

    /**
     * Create an error response with additional structured details
     */
    public static Map<String, Object> error(String message, Object details) {
        Map<String, Object> response = new HashMap<>();
        response.put("success", false);
        response.put("error", message);
        response.put("details", details);
        return response;
    }
    
    /**
     * Create a paginated response (for lists with pagination)
     */
    public static Map<String, Object> paginated(Object data, int page, int size, long total) {
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("data", data);
        
        Map<String, Object> pagination = new HashMap<>();
        pagination.put("page", page);
        pagination.put("size", size);
        pagination.put("total", total);
        pagination.put("totalPages", (int) Math.ceil((double) total / size));
        pagination.put("hasNext", (page + 1) * size < total);
        pagination.put("hasPrev", page > 0);
        
        response.put("pagination", pagination);
        return response;
    }
}
