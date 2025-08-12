package com.syntopia.controller;

import com.syntopia.model.OnboardingQuest;
import com.syntopia.dto.ApiResponse;
import com.syntopia.service.OnboardingQuestGenerator;
import com.syntopia.exception.ResourceNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.core.Authentication;
import jakarta.servlet.http.HttpServletRequest;

import java.util.List;
import java.util.stream.Collectors;

/**
 * REST Controller for managing onboarding quests
 * Provides endpoints for role-specific quest generation and retrieval
 */
@RestController
@RequestMapping("/api/onboarding")
@CrossOrigin(origins = "*")
public class OnboardingController {
    
    @Autowired
    private OnboardingQuestGenerator questGenerator;

    @Autowired
    private com.syntopia.service.QuestService questService;
    
    /**
     * Get all onboarding quests for all roles and levels 1-4
     */
    @GetMapping("/quests")
    public ResponseEntity<?> getAllOnboardingQuests() {
    List<OnboardingQuest> quests = questGenerator.generateAllOnboardingQuests();
    return ResponseEntity.ok(ApiResponse.success(quests));
    }
    
    /**
     * Get onboarding quests for a specific role
     */
    @GetMapping("/quests/role/{role}")
    public ResponseEntity<?> getQuestsByRole(@PathVariable String role) {
        List<OnboardingQuest> allQuests = questGenerator.generateAllOnboardingQuests();
        List<OnboardingQuest> roleQuests = allQuests.stream()
            .filter(quest -> quest.getRoleDialect().equals(getRoleDialect(role)))
            .collect(Collectors.toList());

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

        List<OnboardingQuest> allQuests = questGenerator.generateAllOnboardingQuests();
        List<OnboardingQuest> levelQuests = allQuests.stream()
            .filter(quest -> quest.getLevel() == level)
            .collect(Collectors.toList());

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

        OnboardingQuest quest = questGenerator.generateQuestForRoleAndLevel(role, level);
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
    private String getRoleDialect(String role) {
        switch (role) {
            case "Tech Development":
                return "Codesmith";
            case "Business Development":
                return "Visionary";
            case "UX Design":
                return "Creator";
            case "Data Science":
                return "Analyst";
            case "Legal Advisory":
                return "Guardian";
            case "Finance Analysis":
                return "Steward";
            case "Sustainability Lead":
                return "Planetary Steward";
            default:
                return "Sacred Practitioner";
        }
    }
    
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
