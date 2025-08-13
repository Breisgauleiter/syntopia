package com.syntopia.service;

import com.syntopia.model.Quest;
import com.syntopia.model.User;
import com.syntopia.model.UserQuest;
import static com.syntopia.testutil.TestBuilders.*;
import com.syntopia.repository.QuestRepository;
import com.syntopia.repository.UserQuestRepository;
import com.syntopia.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for QuestService verification workflow.
 */
public class QuestServiceTest {

    @Mock private QuestRepository questRepository;
    @Mock private UserRepository userRepository;
    @Mock private UserQuestRepository userQuestRepository;
    @Mock private UserService userService; // referenced by service

    @InjectMocks private QuestService questService;

    private User user;
    private Quest quest;
    private UserQuest completedUserQuest;

    @BeforeEach
    void init() {
        MockitoAnnotations.openMocks(this);
    user = newUser("user-1",5,null);
    quest = newQuest("quest-1",1,null,true,0,100);

    completedUserQuest = newUserQuest(user, quest, UserQuest.UserQuestStatus.USER_COMPLETED);
    completedUserQuest.setId("uq-1"); // override id for stability
        completedUserQuest.setStatus(UserQuest.UserQuestStatus.USER_COMPLETED);
        completedUserQuest.setCompletedAt(LocalDateTime.now().minusMinutes(10));
    }

    @Nested
    class VerifyCompletedQuest {
        @Test
        @DisplayName("Transitions USER_COMPLETED to USER_VERIFIED and sets verified flag")
        void verifySuccess() {
            when(userQuestRepository.findByUserIdAndQuestId("user-1", "quest-1"))
                .thenReturn(Optional.of(completedUserQuest));
            when(questRepository.findById("quest-1")).thenReturn(Optional.of(quest));
            when(userQuestRepository.save(any(UserQuest.class))).thenAnswer(inv -> inv.getArgument(0));

            UserQuest result = questService.verifyCompletedUserQuest("user-1", "quest-1");

            assertThat(result.getStatus()).isEqualTo(UserQuest.UserQuestStatus.USER_VERIFIED);
            assertThat(result.isVerified()).isTrue();
            assertThat(result.getQuest()).isNotNull();

            ArgumentCaptor<UserQuest> captor = ArgumentCaptor.forClass(UserQuest.class);
            verify(userQuestRepository).save(captor.capture());
            assertThat(captor.getValue().getStatus()).isEqualTo(UserQuest.UserQuestStatus.USER_VERIFIED);
        }

        @Test
        @DisplayName("Rejects verification when status not USER_COMPLETED")
        void verifyWrongState() {
            completedUserQuest.setStatus(UserQuest.UserQuestStatus.USER_ACTIVE);
            when(userQuestRepository.findByUserIdAndQuestId("user-1", "quest-1"))
                .thenReturn(Optional.of(completedUserQuest));

            assertThatThrownBy(() -> questService.verifyCompletedUserQuest("user-1", "quest-1"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("USER_COMPLETED");
            verify(userQuestRepository, never()).save(any());
        }

        @Test
        @DisplayName("Throws when user quest relation missing")
        void verifyMissingRelation() {
            when(userQuestRepository.findByUserIdAndQuestId("user-1", "quest-1"))
                .thenReturn(Optional.empty());
            assertThatThrownBy(() -> questService.verifyCompletedUserQuest("user-1", "quest-1"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("not found");
        }
    }

    @Nested
    class AcceptUserQuestEdgeCases {
        @Test
        @DisplayName("Rejects accept when user level too low")
        void levelTooLow() {
            user.setCurrentLevel(1);
            quest.setRequiredLevel(5);
            when(userRepository.findById("user-1")).thenReturn(Optional.of(user));
            when(questRepository.findById("quest-1")).thenReturn(Optional.of(quest));

            assertThatThrownBy(() -> questService.acceptUserQuest("user-1", "quest-1"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("level");
        }

        @Test
        @DisplayName("Rejects accept when role mismatch and quest role not All")
        void roleMismatch() {
            user.setSelectedRole("DEVELOPER");
            quest.setRole("DESIGNER");
            when(userRepository.findById("user-1")).thenReturn(Optional.of(user));
            when(questRepository.findById("quest-1")).thenReturn(Optional.of(quest));

            assertThatThrownBy(() -> questService.acceptUserQuest("user-1", "quest-1"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("role");
        }

        @Test
        @DisplayName("Rejects accept when quest completion limit reached (non-reusable or max reached)")
        void limitReached() {
            // Non-reusable quest scenario
            quest.setReusable(false); // default non-reusable
            quest.setMaxCompletions(1);
            quest.setCurrentCompletions(1);
            when(userRepository.findById("user-1")).thenReturn(Optional.of(user));
            when(questRepository.findById("quest-1")).thenReturn(Optional.of(quest));

            assertThatThrownBy(() -> questService.acceptUserQuest("user-1", "quest-1"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("maximum completion limit");
        }

        @Test
        @DisplayName("Allows reuse when quest is reusable and previously completed")
        void allowsReuseWhenReusable() {
            quest.setReusable(true);
            quest.setMaxCompletions(0); // unlimited
            when(userRepository.findById("user-1")).thenReturn(Optional.of(user));
            when(questRepository.findById("quest-1")).thenReturn(Optional.of(quest));

            // Existing completed relation
            UserQuest existing = new UserQuest(user, quest);
            existing.setId("uq-1");
            existing.markAsCompleted(quest.getExperienceReward());
            when(userQuestRepository.findByUserIdAndQuestId("user-1", "quest-1")).thenReturn(Optional.of(existing));
            when(userQuestRepository.save(any(UserQuest.class))).thenAnswer(inv -> inv.getArgument(0));

            UserQuest restarted = questService.acceptUserQuest("user-1", "quest-1");
            assertThat(restarted.getStatus()).isEqualTo(UserQuest.UserQuestStatus.USER_ACTIVE);
            assertThat(restarted.getProgress()).isEqualTo(0); // reset
        }

        @Test
        @DisplayName("Rejects reuse when quest is not reusable and already completed")
        void rejectsReuseWhenNotReusable() {
            quest.setReusable(false);
            when(userRepository.findById("user-1")).thenReturn(Optional.of(user));
            when(questRepository.findById("quest-1")).thenReturn(Optional.of(quest));

            UserQuest existing = new UserQuest(user, quest);
            existing.setId("uq-1");
            existing.markAsCompleted(quest.getExperienceReward());
            when(userQuestRepository.findByUserIdAndQuestId("user-1", "quest-1")).thenReturn(Optional.of(existing));

            assertThatThrownBy(() -> questService.acceptUserQuest("user-1", "quest-1"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("maximum completion limit");
        }
    }

    @Nested
    class UpdateQuestProgressEdgeCases {
        private UserQuest active;

        @BeforeEach
        void setupActive() {
            active = new UserQuest(user, quest);
            active.setId("uq-progress-1");
            active.markAsStarted();
        }

        @Test
        @DisplayName("Rejects negative progress values")
        void negativeProgress() {
            when(userQuestRepository.findByUserIdAndQuestId("user-1","quest-1"))
                .thenReturn(Optional.of(active));
            assertThatThrownBy(() -> questService.updateQuestProgress("user-1","quest-1", Map.of("progress", -5)))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("cannot be negative");
            verify(userQuestRepository, never()).save(any());
        }

        @Test
        @DisplayName("Caps progress values above 100 to 100")
        void capAboveHundred() {
            when(userQuestRepository.findByUserIdAndQuestId("user-1","quest-1"))
                .thenReturn(Optional.of(active));
            when(questRepository.findById("quest-1")).thenReturn(Optional.of(quest));
            when(userQuestRepository.save(any(UserQuest.class))).thenAnswer(inv -> inv.getArgument(0));

            UserQuest result = questService.updateQuestProgress("user-1","quest-1", Map.of("progress", 150));
            assertThat(result.getProgress()).isEqualTo(100);
        }

        @Test
        @DisplayName("Rejects update when quest not active")
        void notActive() {
            UserQuest notActive = new UserQuest(user, quest);
            notActive.setId("uq-na-1");
            notActive.setStatus(UserQuest.UserQuestStatus.USER_AVAILABLE);
            when(userQuestRepository.findByUserIdAndQuestId("user-1","quest-1"))
                .thenReturn(Optional.of(notActive));
            assertThatThrownBy(() -> questService.updateQuestProgress("user-1","quest-1", Map.of("progress", 10)))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("not active");
        }

        @Test
        @DisplayName("Applies progressData and notes")
        void appliesProgressDataAndNotes() {
            when(userQuestRepository.findByUserIdAndQuestId("user-1","quest-1"))
                .thenReturn(Optional.of(active));
            when(questRepository.findById("quest-1")).thenReturn(Optional.of(quest));
            when(userQuestRepository.save(any(UserQuest.class))).thenAnswer(inv -> inv.getArgument(0));

            Map<String,Object> update = new java.util.HashMap<>();
            update.put("progress", 55);
            update.put("progressData", Map.of("milestone", "half"));
            update.put("notes", "Good progress");

            UserQuest result = questService.updateQuestProgress("user-1","quest-1", update);
            assertThat(result.getProgress()).isEqualTo(55);
            assertThat(result.getProgressData()).isNotNull();
            assertThat(result.getProgressData().get("milestone")).isEqualTo("half");
            assertThat(result.getCompletionNotes()).isEqualTo("Good progress");
        }
    }

    @Nested
    class AbandonUserQuest {
        private UserQuest active;

        @BeforeEach
        void setupActive() {
            active = newUserQuest(user, quest, UserQuest.UserQuestStatus.USER_ACTIVE);
            active.setId("uq-abandon-1");
        }

        @Test
        @DisplayName("Abandons an active quest and sets status + quest reference")
        void abandonSuccess() {
            when(userQuestRepository.findByUserIdAndQuestId("user-1","quest-1"))
                .thenReturn(Optional.of(active));
            when(questRepository.findById("quest-1")).thenReturn(Optional.of(quest));
            when(userQuestRepository.save(any(UserQuest.class))).thenAnswer(inv -> inv.getArgument(0));

            UserQuest result = questService.abandonUserQuest("user-1","quest-1");

            assertThat(result.getStatus()).isEqualTo(UserQuest.UserQuestStatus.USER_ABANDONED);
            assertThat(result.getQuest()).isNotNull();
            assertThat(result.getAbandonedAt()).isNotNull();
        }

        @Test
        @DisplayName("Rejects abandon when quest not active")
        void abandonWrongState() {
            UserQuest completed = newUserQuest(user, quest, UserQuest.UserQuestStatus.USER_COMPLETED);
            completed.setId("uq-abandon-2");
            when(userQuestRepository.findByUserIdAndQuestId("user-1","quest-1"))
                .thenReturn(Optional.of(completed));

            assertThatThrownBy(() -> questService.abandonUserQuest("user-1","quest-1"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("not active");
        }
    }

    @Nested
    class UserQuestStatistics {
        @Test
        @DisplayName("Returns map with totalCompleted, activeQuests, completedThisWeek, recentCompletions")
        void statsSuccess() {
            String userId = "user-1";
            // counts
            when(userQuestRepository.countByUserIdAndStatus(userId, UserQuest.UserQuestStatus.USER_COMPLETED))
                .thenReturn(5L);
            when(userQuestRepository.countByUserIdAndStatus(userId, UserQuest.UserQuestStatus.USER_ACTIVE))
                .thenReturn(2L);
            // recent completions list
            UserQuest recent1 = newUserQuest(user, quest, UserQuest.UserQuestStatus.USER_COMPLETED);
            recent1.setId("uq-stat-1");
            UserQuest recent2 = newUserQuest(user, quest, UserQuest.UserQuestStatus.USER_COMPLETED);
            recent2.setId("uq-stat-2");
            when(userQuestRepository.findByUserIdAndStatusAndCompletedAtAfter(
                eq(userId), eq(UserQuest.UserQuestStatus.USER_COMPLETED), any(LocalDateTime.class)))
                .thenReturn(java.util.List.of(recent1, recent2));

            Map<String,Object> stats = questService.getUserQuestStatistics(userId);

            assertThat(stats).containsKeys("totalCompleted","activeQuests","completedThisWeek","recentCompletions");
            assertThat(stats.get("totalCompleted")).isEqualTo(5L);
            assertThat(stats.get("activeQuests")).isEqualTo(2L);
            assertThat(stats.get("completedThisWeek")).isEqualTo(2);
            assertThat((java.util.List<?>) stats.get("recentCompletions")).hasSize(2);
        }
    }

    @Nested
    class QuestLifecycleHappyPath {
        @Test
        @DisplayName("Progress update then completion awards XP, increments quest completions, then verify")
        void fullLifecycle() {
            // Arrange active quest relationship
            UserQuest active = newUserQuest(user, quest, UserQuest.UserQuestStatus.USER_ACTIVE);
            active.setId("uq-life-1");

            // Stubbing sequence for updateProgress -> complete -> verify
            // Calls to findByUserIdAndQuestId in order: updateProgress (active), complete (active), verify (completed)
            when(userQuestRepository.findByUserIdAndQuestId("user-1","quest-1"))
                .thenReturn(Optional.of(active)) // for updateProgress
                .thenReturn(Optional.of(active)) // for complete
                .thenAnswer(inv -> {
                    // Simulate that after completion status is USER_COMPLETED
                    if (active.getStatus() == UserQuest.UserQuestStatus.USER_COMPLETED) {
                        return Optional.of(active);
                    }
                    return Optional.of(active);
                });
            when(questRepository.findById("quest-1")).thenReturn(Optional.of(quest));
            when(userQuestRepository.save(any(UserQuest.class))).thenAnswer(inv -> inv.getArgument(0));
            when(questRepository.save(any(Quest.class))).thenAnswer(inv -> inv.getArgument(0));

            // Act 1: progress update
            UserQuest afterProgress = questService.updateQuestProgress("user-1","quest-1", Map.of("progress", 40));
            assertThat(afterProgress.getProgress()).isEqualTo(40);

            // Act 2: complete quest
            int initialCompletions = quest.getCurrentCompletions();
            UserQuest afterComplete = questService.completeUserQuest("user-1","quest-1", Map.of("evidence","url"));
            assertThat(afterComplete.getStatus()).isEqualTo(UserQuest.UserQuestStatus.USER_COMPLETED);
            assertThat(afterComplete.getProgress()).isEqualTo(100);
            assertThat(quest.getCurrentCompletions()).isEqualTo(initialCompletions + 1);
            verify(userService).completeQuest("user-1", quest.getExperienceReward());

            // Adjust stubbing for verify call to return completed relation
            when(userQuestRepository.findByUserIdAndQuestId("user-1","quest-1"))
                .thenReturn(Optional.of(afterComplete));

            // Act 3: verify quest
            UserQuest afterVerify = questService.verifyCompletedUserQuest("user-1","quest-1");
            assertThat(afterVerify.getStatus()).isEqualTo(UserQuest.UserQuestStatus.USER_VERIFIED);
            assertThat(afterVerify.isVerified()).isTrue();

            // Verify saves invoked expected times (>=3) without being overly strict on exact counts
            verify(userQuestRepository, atLeast(3)).save(any(UserQuest.class));
            verify(questRepository, atLeastOnce()).save(any(Quest.class));
        }
    }
}
