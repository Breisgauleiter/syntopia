package com.syntopia.model;

/**
 * Experience Level System based on Fibonacci progression
 * Each level requires progressively more experience following the sacred mathematical pattern
 */
public class ExperienceLevels {
    
    /**
     * Fibonacci-based experience requirements for levels 1-8
     * Formula: fibonacci(n) * 100
     */
    public static final int[] LEVEL_XP_REQUIREMENTS = {
        100,   // Level 1: 1 * 100 = 100 XP
        200,   // Level 2: 2 * 100 = 200 XP  
        300,   // Level 3: 3 * 100 = 300 XP
        500,   // Level 4: 5 * 100 = 500 XP
        800,   // Level 5: 8 * 100 = 800 XP
        1300,  // Level 6: 13 * 100 = 1300 XP
        2100,  // Level 7: 21 * 100 = 2100 XP
        3400   // Level 8: 34 * 100 = 3400 XP
    };
    
    /**
     * Total cumulative experience needed to reach each level
     */
    public static final int[] CUMULATIVE_XP_FOR_LEVEL = {
        0,     // Level 0 (starting)
        100,   // Level 1: 100 total
        300,   // Level 2: 100 + 200 = 300 total
        600,   // Level 3: 300 + 300 = 600 total
        1100,  // Level 4: 600 + 500 = 1100 total (GitHub integration unlocked)
        1900,  // Level 5: 1100 + 800 = 1900 total
        3200,  // Level 6: 1900 + 1300 = 3200 total
        5300,  // Level 7: 3200 + 2100 = 5300 total
        8700   // Level 8: 5300 + 3400 = 8700 total
    };
    
    /**
     * Calculate the user's current level based on total experience
     */
    public static int calculateLevel(int totalExperience) {
        for (int level = CUMULATIVE_XP_FOR_LEVEL.length - 1; level >= 0; level--) {
            if (totalExperience >= CUMULATIVE_XP_FOR_LEVEL[level]) {
                return level;
            }
        }
        return 0; // Should never happen, but safe default
    }
    
    /**
     * Calculate experience needed for next level
     */
    public static int experienceToNextLevel(int totalExperience) {
        int currentLevel = calculateLevel(totalExperience);
        if (currentLevel >= CUMULATIVE_XP_FOR_LEVEL.length - 1) {
            return 0; // Max level reached
        }
        return CUMULATIVE_XP_FOR_LEVEL[currentLevel + 1] - totalExperience;
    }
    
    /**
     * Get experience requirement for a specific level
     */
    public static int getXpRequirementForLevel(int level) {
        if (level < 1 || level > LEVEL_XP_REQUIREMENTS.length) {
            return 0;
        }
        return LEVEL_XP_REQUIREMENTS[level - 1];
    }
    
    /**
     * Check if GitHub integration should be unlocked (Level 4+)
     */
    public static boolean isGitHubIntegrationUnlocked(int totalExperience) {
        return calculateLevel(totalExperience) >= 4;
    }
}
