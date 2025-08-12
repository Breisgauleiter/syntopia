package com.syntopia.service;

import com.syntopia.model.User;
import com.syntopia.repository.UserRepository;
import com.syntopia.repository.UserQuestRepository;
import com.syntopia.repository.UserCollaborationRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class ProfileServiceTest {

    @Mock
    private UserRepository userRepository;
    @Mock
    private UserQuestRepository userQuestRepository;
    @Mock
    private UserCollaborationRepository userCollaborationRepository;

    @InjectMocks
    private ProfileService profileService;

    private User user;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        user = new User();
        user.setId("u1");
        user.setUsername("testuser");
        user.setDisplayName("Test User");
        user.setCurrentLevel(3);
        user.setExperiencePoints(750);
    }

    @Test
    void testGetUserAchievementsProgressFields() {
        when(userRepository.findById("u1")).thenReturn(Optional.of(user));
        when(userQuestRepository.countByUserIdAndStatus(eq("u1"), any())).thenReturn(7L); // 7 quests completed
        when(userCollaborationRepository.countConnections("u1", "ACCEPTED", "all")).thenReturn(3); // 3 connections

        Map<String, Object> result = profileService.getUserAchievements("u1");
        assertNotNull(result);
        @SuppressWarnings("unchecked")
        var achievements = (java.util.List<Map<String,Object>>) result.get("achievements");
        assertTrue(achievements.stream().anyMatch(a -> "quest_adept".equals(a.get("id")) && a.containsKey("progress") && (int)a.get("goal") == 10));
        assertTrue(achievements.stream().anyMatch(a -> "quest_master".equals(a.get("id")) && a.containsKey("goal")));
        assertTrue(achievements.stream().anyMatch(a -> "community_builder".equals(a.get("id"))));
    }

    @Test
    void testUpdateProfileSocialLinks() {
        when(userRepository.findById("u1")).thenReturn(Optional.of(user));
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Map<String,Object> input = Map.of(
            "socialLinks", Map.of(
                "twitter", "https://x.com/syntopia",
                "github", "https://github.com/syntopia",
                "linkedin", "https://www.linkedin.com/in/syntopia/"
            )
        );
        Map<String,Object> updated = profileService.updateProfile("u1", input);
        assertNotNull(updated.get("user"));
        @SuppressWarnings("unchecked")
        Map<String,Object> userMap = (Map<String,Object>) updated.get("user");
        @SuppressWarnings("unchecked")
        Map<String,String> social = (Map<String,String>) userMap.get("socialLinks");
        assertEquals("https://x.com/syntopia", social.get("twitter"));
        assertEquals("https://github.com/syntopia", social.get("github"));
        assertEquals("https://www.linkedin.com/in/syntopia/", social.get("linkedin"));
    }
}
