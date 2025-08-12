package com.syntopia.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import com.syntopia.dto.ApiResponse;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * Main API Controller for Syntopia Platform
 * 
 * Provides basic endpoints for health checks and platform information.
 */
@RestController
@RequestMapping("/api")
@CrossOrigin(origins = {"http://localhost:5173", "http://localhost:3000"})
public class SyntopiaController {

    /**
     * Health check endpoint
     */
    @GetMapping("/health")
    public ResponseEntity<Map<String, Object>> health() {
        Map<String, Object> data = new HashMap<>();
        data.put("status", "UP");
        data.put("timestamp", LocalDateTime.now());
        data.put("service", "Syntopia Backend");
        data.put("version", "1.0.0-SNAPSHOT");
        return ResponseEntity.ok(ApiResponse.success(data));
    }

    /**
     * Welcome endpoint
     */
    @GetMapping("/")
    public ResponseEntity<Map<String, Object>> welcome() {
        Map<String, Object> data = new HashMap<>();
        data.put("message", "Welcome to Syntopia - Sacred Geometry Platform");
        data.put("description", "Explore the divine mathematics of creation through interactive visualizations");
        data.put("timestamp", LocalDateTime.now());
        data.put("endpoints", Map.of(
                "health", "/api/health",
                "auth", "/api/auth/*",
                "geometry", "/api/geometry/*",
                "users", "/api/users/*"
        ));
        return ResponseEntity.ok(ApiResponse.success(data));
    }

    /**
     * API information endpoint
     */
    @GetMapping("/info")
    public ResponseEntity<Map<String, Object>> info() {
        Map<String, Object> data = new HashMap<>();
        data.put("name", "Syntopia Backend API");
        data.put("version", "1.0.0-SNAPSHOT");
        data.put("description", "Backend service for Syntopia platform");
        data.put("features", new String[]{
                "JWT Authentication",
                "OAuth2 GitHub Integration",
                "ArangoDB TAO Architecture",
                "Sacred Geometry Data Management",
                "Real-time Visualization Support"
        });
        data.put("timestamp", LocalDateTime.now());
        return ResponseEntity.ok(ApiResponse.success(data));
    }

}
