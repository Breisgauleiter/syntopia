package com.syntopia.controller;

import com.syntopia.model.OnboardingQuest;
import com.syntopia.dto.ApiResponse;
import com.syntopia.service.OnboardingQuestService;
import com.syntopia.exception.ResourceNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.core.Authentication;
import java.util.Map;
import jakarta.servlet.http.HttpServletRequest;

import java.util.List;

/**
 * REST Controller for managing onboarding quests
 * Provides endpoints for role-specific quest generation and retrieval
 */
@RestController
@RequestMapping("/api/onboarding")
@CrossOrigin(origins = "*")
public class OnboardingController {
    
    @Autowired
    private OnboardingQuestService onboardingQuestService;

    @Autowired
    private com.syntopia.service.QuestService questService;
    
    /**
     * Get all onboarding quests for all roles and levels 1-4
     */
    @GetMapping("/quests")
    public ResponseEntity<?> getAllOnboardingQuests() {
        List<OnboardingQuest> quests = onboardingQuestService.getAll();
        return ResponseEntity.ok(ApiResponse.success(quests));
    }
    
    /**
     * Get onboarding quests for a specific role
     */
    @GetMapping("/quests/role/{role}")
    public ResponseEntity<?> getQuestsByRole(@PathVariable String role) {
    List<OnboardingQuest> roleQuests = onboardingQuestService.getByRoleDisplay(role);

        if (roleQuests.isEmpty()) {
            throw new ResourceNotFoundException("No onboarding quests for role");
        }

        return ResponseEntity.ok(ApiResponse.success(roleQuests));
    }
    
    /**
     * Get onboarding quests for a specific level (1-4)
     */
    @GetMapping("/quests/level/{level}")
    public ResponseEntity<?> getQuestsByLevel(@PathVariable int level) {
        if (level < 1 || level > 4) {
            throw new IllegalArgumentException("Level must be 1-4");
        }

    List<OnboardingQuest> levelQuests = onboardingQuestService.getByLevel(level);

        return ResponseEntity.ok(ApiResponse.success(levelQuests));
    }
    
    /**
     * Get a specific onboarding quest by role and level
     */
    @GetMapping("/quests/role/{role}/level/{level}")
    public ResponseEntity<?> getQuestByRoleAndLevel(
            @PathVariable String role, 
            @PathVariable int level) {
        if (level < 1 || level > 4) {
            throw new IllegalArgumentException("Level must be 1-4");
        }

    OnboardingQuest quest = onboardingQuestService.ensureQuest(role, level);
    return ResponseEntity.ok(ApiResponse.success(quest));
    }

    /**
     * Accept onboarding quest for the authenticated user (marks it ACTIVE)
     */
    @PostMapping("/accept")
    public ResponseEntity<?> acceptOnboardingQuest(
            @RequestParam String role,
            @RequestParam int level,
            Authentication authentication,
            HttpServletRequest request) {
        if (level < 1 || level > 4) {
            throw new IllegalArgumentException("Level must be 1-4");
        }
        String userId = authentication.getName();
        var userQuest = questService.acceptOnboardingQuestForUser(userId, role, level);
        return ResponseEntity.ok(ApiResponse.success("Onboarding quest accepted", userQuest));
    }

    /**
     * Get onboarding progress summary for authenticated user
     */
    @GetMapping(value = "/progress", produces = "application/json")
    public ResponseEntity<?> getOnboardingProgress(
            Authentication authentication,
            @RequestParam(value = "status", required = false) String statusParam,
            @RequestParam(value = "level", required = false) String levelParam,
            @RequestParam(value = "page", required = false) Integer page,
            @RequestParam(value = "size", required = false) Integer size) {
        String userId = authentication.getName();
        var onboarding = questService.getUserOnboardingUserQuests(userId);

        // Optional filtering by status (supports comma-separated list)
        final java.util.Set<String> allowedStatuses;
        if (statusParam != null && !statusParam.isBlank()) {
            java.util.Set<String> tmp = new java.util.HashSet<>();
            for (String s : statusParam.split(",")) {
                String trimmed = s.trim().toUpperCase();
                try {
                    com.syntopia.model.UserQuest.UserQuestStatus.valueOf(trimmed); // validate
                    tmp.add(trimmed);
                } catch (IllegalArgumentException e) {
                    return ResponseEntity.badRequest().body(ApiResponse.error("Invalid status value: " + s));
                }
            }
            allowedStatuses = java.util.Collections.unmodifiableSet(tmp);
        } else {
            allowedStatuses = null;
        }

        // Optional filtering by level (supports comma-separated list)
        final java.util.Set<Integer> allowedLevels;
        if (levelParam != null && !levelParam.isBlank()) {
            java.util.Set<Integer> tmpLevels = new java.util.HashSet<>();
            for (String l : levelParam.split(",")) {
                try {
                    int val = Integer.parseInt(l.trim());
                    tmpLevels.add(val);
                } catch (NumberFormatException e) {
                    return ResponseEntity.badRequest().body(ApiResponse.error("Invalid level value: " + l));
                }
            }
            allowedLevels = java.util.Collections.unmodifiableSet(tmpLevels);
        } else {
            allowedLevels = null;
        }

        var filtered = onboarding.stream()
                .filter(uq -> allowedStatuses == null || allowedStatuses.contains(uq.getStatus().name()))
                .filter(uq -> allowedLevels == null || allowedLevels.contains(uq.getQuest().getRequiredLevel()))
                .toList();

        long completed = filtered.stream().filter(uq -> uq.getStatus() == com.syntopia.model.UserQuest.UserQuestStatus.USER_COMPLETED || uq.getStatus() == com.syntopia.model.UserQuest.UserQuestStatus.USER_VERIFIED).count();
        long active = filtered.stream().filter(uq -> uq.getStatus() == com.syntopia.model.UserQuest.UserQuestStatus.USER_ACTIVE).count();
        long verified = filtered.stream().filter(uq -> uq.getStatus() == com.syntopia.model.UserQuest.UserQuestStatus.USER_VERIFIED).count();
        long total = filtered.size();
        int maxLevelAchieved = filtered.stream().map(uq -> uq.getQuest().getRequiredLevel()).max(Integer::compareTo).orElse(0);

        List<Map<String,Object>> summaries = filtered.stream().map(uq -> {
            Map<String,Object> m = new java.util.HashMap<>();
            m.put("questId", uq.getQuest().getId());
            m.put("title", uq.getQuest().getTitle());
            m.put("level", uq.getQuest().getRequiredLevel());
            m.put("status", uq.getStatus().name());
            m.put("progress", uq.getProgress());
            return m;
        }).toList();

        // Pagination (optional). If page or size missing -> return all (no pagination metadata)
        List<Map<String,Object>> pagedSummaries = summaries;
        Map<String,Object> pagination = null;
        if (page != null || size != null) {
            int p = page == null ? 0 : Math.max(0, page);
            int s = size == null ? summaries.size() : Math.max(1, size);
            int from = Math.min(p * s, summaries.size());
            int to = Math.min(from + s, summaries.size());
            pagedSummaries = summaries.subList(from, to);
            pagination = new java.util.HashMap<>();
            pagination.put("page", p);
            pagination.put("size", s);
            pagination.put("total", summaries.size()); // total after filters, before pagination
            int totalPages = (int) Math.ceil(summaries.size() / (double) s);
            pagination.put("totalPages", totalPages);
            pagination.put("hasNext", p + 1 < totalPages);
            pagination.put("hasPrev", p > 0);
        }

        Map<String,Object> payload = new java.util.HashMap<>();
        payload.put("total", total);
        payload.put("completed", completed);
        payload.put("verified", verified);
        payload.put("active", active);
        payload.put("progressPercent", total == 0 ? 0 : Math.round((completed * 100.0)/ total));
        payload.put("maxLevelAchieved", maxLevelAchieved);
        payload.put("quests", pagedSummaries);
        if (pagination != null) {
            payload.put("pagination", pagination);
        }

        return ResponseEntity.ok(ApiResponse.success(payload));
    }
    
    /**
     * Get all available roles
     */
    @GetMapping("/roles")
    public ResponseEntity<?> getAvailableRoles() {
        List<String> roles = List.of(
            "Tech Development",
            "Business Development", 
            "UX Design",
            "Data Science",
            "Legal Advisory",
            "Finance Analysis",
            "Sustainability Lead"
        );
        return ResponseEntity.ok(ApiResponse.success(roles));
    }

    /**
     * ADMIN: Force refresh onboarding quest definition cache.
     */
    @PostMapping("/cache/refresh")
    @org.springframework.security.access.prepost.PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> refreshOnboardingCache() {
        onboardingQuestService.forceRefreshCache();
        return ResponseEntity.ok(ApiResponse.success("Onboarding quest cache refreshed"));
    }
    
    /**
     * Get SYN principles information
     */
    @GetMapping("/syn-principles")
    public ResponseEntity<?> getSynPrinciples() {
        List<SynPrincipleInfo> principles = List.of(
            new SynPrincipleInfo(1, "Syntropie", "Harmonische Ordnung durch Bewusstsein", 
                "Die Kraft der bewussten Struktur und organischen Organisation für maximale Harmonie."),
            new SynPrincipleInfo(2, "Synthese", "Evolution von Idee zu Manifestation", 
                "Der Prozess der Transformation und Integration für kontinuierliche Evolution."),
            new SynPrincipleInfo(3, "Synarchie", "Netzwerk aus Netzwerken", 
                "Organische Führung und kollaborative Governance für dezentrale Innovation."),
            new SynPrincipleInfo(4, "Synchronizität", "Intelligentes Purpose-Matching & AI-Consciousness", 
                "Bedeutungsvolle Verbindungen und bewusste Technologie für positive Transformation.")
        );
        return ResponseEntity.ok(ApiResponse.success(principles));
    }
    
    /**
     * Get Fibonacci XP progression information
     */
    @GetMapping("/experience-levels")
    public ResponseEntity<?> getExperienceLevels() {
        List<ExperienceLevel> levels = List.of(
            new ExperienceLevel(1, 100, "Profile Genesis", "Sacred Profile Setup & Role Selection"),
            new ExperienceLevel(2, 200, "Principle Integration", "SYN-Principles Mastery & Avatar Creation"),
            new ExperienceLevel(3, 300, "Community Formation", "Network Building & Collaboration"),
            new ExperienceLevel(4, 500, "Synchronicity Activation", "GitHub Integration & Quest Synchronization")
        );
        return ResponseEntity.ok(ApiResponse.success(levels));
    }
    
    // Helper method to convert role names to dialects
    
    // Data transfer objects
    public static class SynPrincipleInfo {
        private int level;
        private String name;
        private String subtitle;
        private String description;
        
        public SynPrincipleInfo(int level, String name, String subtitle, String description) {
            this.level = level;
            this.name = name;
            this.subtitle = subtitle;
            this.description = description;
        }
        
        // Getters
        public int getLevel() { return level; }
        public String getName() { return name; }
        public String getSubtitle() { return subtitle; }
        public String getDescription() { return description; }
    }
    
    public static class ExperienceLevel {
        private int level;
        private int xpRequired;
        private String title;
        private String description;
        
        public ExperienceLevel(int level, int xpRequired, String title, String description) {
            this.level = level;
            this.xpRequired = xpRequired;
            this.title = title;
            this.description = description;
        }
        
        // Getters
        public int getLevel() { return level; }
        public int getXpRequired() { return xpRequired; }
        public String getTitle() { return title; }
        public String getDescription() { return description; }
    }
}
