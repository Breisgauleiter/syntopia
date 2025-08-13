package com.syntopia.service;

import com.syntopia.model.OnboardingQuest;
import com.syntopia.model.Quest;
import com.syntopia.model.User;
import com.syntopia.model.UserQuest;
import com.syntopia.repository.QuestRepository;
import com.syntopia.repository.UserQuestRepository;
import com.syntopia.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.time.LocalDateTime;
import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.any;

/**
 * Verifies idempotency logic in acceptOnboardingQuestForUser that abandons other active onboarding quests of same level.
 */
public class QuestServiceOnboardingIdempotencyTest {

    @Mock private QuestRepository questRepository;
    @Mock private UserRepository userRepository;
    @Mock private UserQuestRepository userQuestRepository;
    @Mock private UserService userService; // unused here but required by service
    @Mock private OnboardingQuestService onboardingQuestService;

    @InjectMocks private QuestService questService;

    private User user;
    private Quest existingQuestLevel1RoleA;
    private UserQuest existingUserQuestA;

    @BeforeEach
    void setup() {
        MockitoAnnotations.openMocks(this);
        user = new User();
        user.setId("user-1");
        user.setUsername("user-1");
        user.setCurrentLevel(5);

        existingQuestLevel1RoleA = new Quest();
        existingQuestLevel1RoleA.setId("onboarding-tech-development-level-1");
        existingQuestLevel1RoleA.setRequiredLevel(1);
        existingQuestLevel1RoleA.setRole("TECH_DEVELOPMENT");
        existingQuestLevel1RoleA.setStatus(Quest.QuestStatus.AVAILABLE);
        existingQuestLevel1RoleA.setCreatedAt(LocalDateTime.now());
        existingQuestLevel1RoleA.setUpdatedAt(LocalDateTime.now());
        existingQuestLevel1RoleA.getMetadata().put("isOnboardingQuest", true);

        existingUserQuestA = new UserQuest(user, existingQuestLevel1RoleA);
        existingUserQuestA.setId("uq-existing-A");
        existingUserQuestA.markAsStarted();
    }

    @Test
    @DisplayName("Accepting a new onboarding quest of same level abandons previous active onboarding quest")
    void acceptingSecondOnboardingQuestAbandonsFirst() {
        // Stubs
        when(userRepository.findById("user-1")).thenReturn(Optional.of(user));

        // ensureQuest for any role/level returns a generated definition
        when(onboardingQuestService.ensureQuest(anyString(), anyInt())).thenAnswer(inv -> {
            String roleDisplay = inv.getArgument(0);
            int level = inv.getArgument(1);
            OnboardingQuest def = new OnboardingQuest();
            def.setId("onboarding_" + roleDisplay.replaceAll(" ", "").toLowerCase() + "_level_" + level);
            def.setLevel(level);
            def.setTitle(roleDisplay + " L" + level + " Quest");
            def.setDescription("Desc");
            def.setXpReward(100);
            def.setRoleDialect("dialect");
            def.setSteps(List.of("step1"));
            def.setIconType("icon");
            return def;
        });

        // First we simulate existing active quest (role A) already persisted
        when(userQuestRepository.findByUserId("user-1")).thenReturn(List.of(existingUserQuestA));

        // No existing quest of new role/level in repository
        when(questRepository.findByRoleAndRequiredLevelAndStatus(eq("BUSINESS_DEVELOPMENT"), eq(1), eq(Quest.QuestStatus.AVAILABLE)))
                .thenReturn(List.of());

        // Saving new quest assigns id if missing
        when(questRepository.save(any(Quest.class))).thenAnswer(inv -> {
            Quest q = inv.getArgument(0);
            if (q.getId() == null) {
                q.setId("onboarding-business-development-level-1");
            }
            return q;
        });

        // No existing relation for new quest
        when(userQuestRepository.findByUserIdAndQuestId("user-1", "onboarding-business-development-level-1"))
                .thenReturn(Optional.empty());

        // Saving user quests echoes argument (assign id if null)
        when(userQuestRepository.save(any(UserQuest.class))).thenAnswer(inv -> {
            UserQuest uq = inv.getArgument(0);
            if (uq.getId() == null) {
                uq.setId(UUID.randomUUID().toString());
            }
            return uq;
        });

        // Act: accept onboarding quest for a different role (same level 1)
        UserQuest newUserQuest = questService.acceptOnboardingQuestForUser("user-1", "BUSINESS_DEVELOPMENT", 1);

        // Assert new quest active
        assertThat(newUserQuest.getStatus()).isEqualTo(UserQuest.UserQuestStatus.USER_ACTIVE);
        assertThat(newUserQuest.getQuest().getRequiredLevel()).isEqualTo(1);
        assertThat(newUserQuest.getQuest().getMetadata().get("isOnboardingQuest")).isEqualTo(true);

        // The existing one should have been abandoned
        assertThat(existingUserQuestA.getStatus()).isEqualTo(UserQuest.UserQuestStatus.USER_ABANDONED);
    }
}
