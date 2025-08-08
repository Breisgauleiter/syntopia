package com.syntopia.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

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
        Map<String, Object> response = new HashMap<>();
        response.put("status", "UP");
        response.put("timestamp", LocalDateTime.now());
        response.put("service", "Syntopia Backend");
        response.put("version", "1.0.0-SNAPSHOT");
        return ResponseEntity.ok(response);
    }

    /**
     * Welcome endpoint
     */
    @GetMapping("/")
    public ResponseEntity<Map<String, Object>> welcome() {
        Map<String, Object> response = new HashMap<>();
        response.put("message", "Welcome to Syntopia - Sacred Geometry Platform");
        response.put("description", "Explore the divine mathematics of creation through interactive visualizations");
        response.put("timestamp", LocalDateTime.now());
        response.put("endpoints", Map.of(
                "health", "/api/health",
                "auth", "/api/auth/*",
                "geometry", "/api/geometry/*",
                "users", "/api/users/*"
        ));
        return ResponseEntity.ok(response);
    }

    /**
     * API information endpoint
     */
    @GetMapping("/info")
    public ResponseEntity<Map<String, Object>> info() {
        Map<String, Object> response = new HashMap<>();
        response.put("name", "Syntopia Backend API");
        response.put("version", "1.0.0-SNAPSHOT");
        response.put("description", "Backend service for Syntopia platform");
        response.put("features", new String[]{
                "JWT Authentication",
                "OAuth2 GitHub Integration",
                "ArangoDB TAO Architecture",
                "Sacred Geometry Data Management",
                "Real-time Visualization Support"
        });
        response.put("timestamp", LocalDateTime.now());
        return ResponseEntity.ok(response);
    }
}
