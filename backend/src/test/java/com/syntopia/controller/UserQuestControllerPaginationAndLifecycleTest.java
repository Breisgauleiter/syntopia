package com.syntopia.controller;

import com.syntopia.dto.UserQuestDTO;
import com.syntopia.model.Quest;
import com.syntopia.model.User;
import com.syntopia.model.UserQuest;
import static com.syntopia.testutil.TestBuilders.*;
import com.syntopia.service.QuestService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.IntStream;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;

/**
 * Unit tests for pagination responses and lifecycle endpoints (accept -> complete -> abandon/verify).
 * These are plain controller unit tests (no Spring context) to validate ApiResponse structure & transitions.
 */
public class UserQuestControllerPaginationAndLifecycleTest {

    private UserQuestController controllerWith(QuestService service) {
        UserQuestController c = new UserQuestController();
        try {
            var f = UserQuestController.class.getDeclaredField("questService");
            f.setAccessible(true);
            f.set(c, service);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return c;
    }

    private Authentication auth(String userId) {
        Authentication a = Mockito.mock(Authentication.class);
        when(a.getName()).thenReturn(userId);
        return a;
    }

    private Quest buildQuest(int i) { return newQuest("q-"+i,1,null,true,0,10); }
    private UserQuest buildUserQuest(String userId, Quest q, UserQuest.UserQuestStatus status) {
        User u = newUser(userId,5,null);
        UserQuest uq = newUserQuest(u,q,status);
        uq.setLastProgressUpdate(LocalDateTime.now());
        return uq;
    }

    @Nested
    class Pagination {
        @Test
        @DisplayName("/available pagination slices list and includes metadata")
        void availablePagination() {
            QuestService service = Mockito.mock(QuestService.class);
            List<UserQuest> all = new ArrayList<>();
            IntStream.range(0, 23).forEach(i -> all.add(buildUserQuest("user-1", buildQuest(i), UserQuest.UserQuestStatus.USER_AVAILABLE)));
            when(service.getAvailableQuestsForUserWithProgress("user-1")).thenReturn(all);

            UserQuestController controller = controllerWith(service);
            Authentication auth = auth("user-1");

            // page 0 size 10
            ResponseEntity<?> page0 = controller.getAvailableQuestsForUser(auth,0,10);
            Map<String,Object> body0 = castBody(page0);
            assertThat(body0.get("success")).isEqualTo(true);
            assertThat(((List<?>) body0.get("data")).size()).isEqualTo(10);
            Map<String,Object> pagination0 = (Map<String,Object>) body0.get("pagination");
            assertThat(pagination0.get("page")).isEqualTo(0);
            assertThat(pagination0.get("size")).isEqualTo(10);
            assertThat(pagination0.get("total")).isEqualTo(23L);
            assertThat(pagination0.get("totalPages")).isEqualTo(3); // 23 / 10 -> ceil 3
            assertThat(pagination0.get("hasNext")).isEqualTo(true);
            assertThat(pagination0.get("hasPrev")).isEqualTo(false);

            // page 2 (last) size 10 -> only 3 items
            ResponseEntity<?> page2 = controller.getAvailableQuestsForUser(auth,2,10);
            Map<String,Object> body2 = castBody(page2);
            assertThat(((List<?>) body2.get("data")).size()).isEqualTo(3);
            Map<String,Object> pagination2 = (Map<String,Object>) body2.get("pagination");
            assertThat(pagination2.get("page")).isEqualTo(2);
            assertThat(pagination2.get("hasNext")).isEqualTo(false);
            assertThat(pagination2.get("hasPrev")).isEqualTo(true);
        }

        @Test
        @DisplayName("/active pagination metadata correctness")
        void activePagination() {
            QuestService service = Mockito.mock(QuestService.class);
            List<UserQuest> active = new ArrayList<>();
            IntStream.range(0, 5).forEach(i -> active.add(buildUserQuest("user-1", buildQuest(i), UserQuest.UserQuestStatus.USER_ACTIVE)));
            when(service.getActiveQuestsForUser("user-1")).thenReturn(active);

            UserQuestController controller = controllerWith(service);
            Map<String,Object> body = castBody(controller.getActiveQuests(auth("user-1"),0,2));
            Map<String,Object> pagination = (Map<String,Object>) body.get("pagination");
            assertThat(pagination.get("total")).isEqualTo(5L);
            assertThat(pagination.get("totalPages")).isEqualTo(3); // 5 / 2 -> ceil 3
            assertThat(pagination.get("hasNext")).isEqualTo(true);
        }

        @Test
        @DisplayName("/completed pagination metadata correctness including last page state")
        void completedPagination() {
            QuestService service = Mockito.mock(QuestService.class);
            List<UserQuest> completed = new ArrayList<>();
            // 7 completed quests -> with size 3 expect totalPages = 3 (3,3,1)
            IntStream.range(0, 7).forEach(i -> completed.add(buildUserQuest("user-1", buildQuest(i), UserQuest.UserQuestStatus.USER_COMPLETED)));
            when(service.getCompletedQuestsForUser("user-1")).thenReturn(completed);

            UserQuestController controller = controllerWith(service);
            Authentication auth = auth("user-1");

            Map<String,Object> bodyPage0 = castBody(controller.getCompletedQuests(auth,0,3));
            Map<String,Object> pag0 = (Map<String,Object>) bodyPage0.get("pagination");
            assertThat(pag0.get("total")).isEqualTo(7L);
            assertThat(pag0.get("totalPages")).isEqualTo(3);
            assertThat(pag0.get("hasNext")).isEqualTo(true);
            assertThat(((List<?>) bodyPage0.get("data")).size()).isEqualTo(3);

            Map<String,Object> bodyLast = castBody(controller.getCompletedQuests(auth,2,3));
            Map<String,Object> pagLast = (Map<String,Object>) bodyLast.get("pagination");
            assertThat(pagLast.get("page")).isEqualTo(2);
            assertThat(pagLast.get("hasNext")).isEqualTo(false);
            assertThat(pagLast.get("hasPrev")).isEqualTo(true);
            assertThat(((List<?>) bodyLast.get("data")).size()).isEqualTo(1);
        }
    }

    @Nested
    class Lifecycle {
        @Test
        @DisplayName("accept -> complete -> verify happy path")
        void acceptCompleteVerify() {
            QuestService service = Mockito.mock(QuestService.class);
            String userId = "user-1";
            String questId = "quest-xyz";

            Quest quest = buildQuest(99);
            quest.setId(questId);

            UserQuest accepted = buildUserQuest(userId, quest, UserQuest.UserQuestStatus.USER_ACTIVE);
            UserQuest completed = buildUserQuest(userId, quest, UserQuest.UserQuestStatus.USER_COMPLETED);
            completed.setExperienceAwarded(50);
            UserQuest verified = buildUserQuest(userId, quest, UserQuest.UserQuestStatus.USER_VERIFIED);
            verified.setVerified(true);

            when(service.acceptUserQuest(userId, questId)).thenReturn(accepted);
            when(service.completeUserQuest(eq(userId), eq(questId), anyMap())).thenReturn(completed);
            when(service.verifyCompletedUserQuest(userId, questId)).thenReturn(verified);

            UserQuestController controller = controllerWith(service);
            Authentication auth = auth(userId);

            // accept
            Map<String,Object> acceptBody = castBody(controller.acceptQuest(questId, auth));
            UserQuestDTO acceptDto = (UserQuestDTO) acceptBody.get("data");
            assertThat(acceptDto.status.name()).isEqualTo("USER_ACTIVE");

            // complete
            Map<String,Object> completeBody = castBody(controller.completeQuest(questId, new HashMap<>(), auth));
            Map<String,Object> completeData = (Map<String,Object>) completeBody.get("data");
            assertThat(completeData.get("status").toString()).isEqualTo("USER_COMPLETED");
            assertThat(completeData.get("experienceAwarded")).isEqualTo(50L);

            // verify
            Map<String,Object> verifyBody = castBody(controller.verifyQuest(questId, auth));
            UserQuestDTO verifyDto = (UserQuestDTO) verifyBody.get("data");
            assertThat(verifyDto.status.name()).isEqualTo("USER_VERIFIED");
            assertThat(verifyDto.verified).isTrue();
        }

        @Test
        @DisplayName("complete without active state returns error")
        void completeWrongState() {
            QuestService service = Mockito.mock(QuestService.class);
            when(service.completeUserQuest(anyString(), anyString(), anyMap())).thenThrow(new IllegalArgumentException("Quest is not active and cannot be completed"));
            UserQuestController controller = controllerWith(service);
            assertThatThrownBy(() -> controller.completeQuest("q1", new HashMap<>(), auth("user-1")))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("not active");
        }

        @Test
        @DisplayName("abandon without active state returns error")
        void abandonWrongState() {
            QuestService service = Mockito.mock(QuestService.class);
            when(service.abandonUserQuest(anyString(), anyString())).thenThrow(new IllegalArgumentException("Quest is not active and cannot be abandoned"));
            UserQuestController controller = controllerWith(service);
            assertThatThrownBy(() -> controller.abandonQuest("q1", auth("user-1")))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("not active");
        }
    }

    @Nested
    class StatisticsEndpoint {
        @Test
        @DisplayName("/statistics returns quest stats with required keys")
        void statisticsSuccess() {
            QuestService service = Mockito.mock(QuestService.class);
            Map<String,Object> stats = new HashMap<>();
            stats.put("totalCompleted", 7L);
            stats.put("activeQuests", 2L);
            stats.put("completedThisWeek", 3);
            stats.put("recentCompletions", List.of());
            when(service.getUserQuestStatistics("user-1")).thenReturn(stats);

            UserQuestController controller = controllerWith(service);
            Map<String,Object> body = castBody(controller.getUserQuestStatistics(auth("user-1")));
            assertThat(body.get("success")).isEqualTo(true);
            @SuppressWarnings("unchecked") Map<String,Object> data = (Map<String,Object>) body.get("data");
            assertThat(data).containsKeys("totalCompleted","activeQuests","completedThisWeek","recentCompletions");
            assertThat(data.get("totalCompleted")).isEqualTo(7L);
            assertThat(data.get("activeQuests")).isEqualTo(2L);
        }
    }

    @SuppressWarnings("unchecked")
    private Map<String,Object> castBody(ResponseEntity<?> response) {
        assertThat(response.getStatusCode().is2xxSuccessful()).isTrue();
        return (Map<String, Object>) response.getBody();
    }

}
