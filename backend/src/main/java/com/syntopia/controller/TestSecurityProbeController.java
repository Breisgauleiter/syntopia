package com.syntopia.controller;

import org.springframework.context.annotation.Profile;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.syntopia.dto.ApiResponse;

import java.util.HashMap;
import java.util.Map;

/**
 * Test-only controller exposing a secured endpoint to verify 401 handling.
 * Active only under the 'test' Spring profile.
 */
@RestController
@RequestMapping("/api")
@Profile("test")
public class TestSecurityProbeController {

    @GetMapping("/secure-test")
    public ResponseEntity<Map<String, Object>> secureTest() {
        Map<String,Object> data = new HashMap<>();
        data.put("reachable", true);
        return ResponseEntity.ok(ApiResponse.success(data));
    }
}
