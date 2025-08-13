package com.syntopia.controller;

import com.syntopia.model.Quest;
import com.syntopia.model.UserQuest;
import com.syntopia.service.QuestService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;

import java.time.LocalDateTime;
import java.util.Map;

import com.syntopia.dto.UserQuestDTO;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

/**
 * Plain unit test for UserQuestController verify endpoint logic (no MVC stack required).
 */
class UserQuestControllerTest {

    @Test
    @DisplayName("verify endpoint returns success payload with USER_VERIFIED status")
    void verifyQuestEndpoint() {
        // Arrange controller with mocked service
        QuestService questService = Mockito.mock(QuestService.class);
        UserQuestController controller = new UserQuestController();
        // inject mock via reflection (field is package-private autowired)
        try {
            var f = UserQuestController.class.getDeclaredField("questService");
            f.setAccessible(true);
            f.set(controller, questService);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        UserQuest uq = new UserQuest();
        uq.setId("uq-1");
        uq.setStatus(UserQuest.UserQuestStatus.USER_VERIFIED);
        uq.setLastProgressUpdate(LocalDateTime.now());
        uq.setVerified(true);
        Quest quest = new Quest();
        quest.setId("q1");
        quest.setTitle("Quest Title");
        uq.setQuest(quest);
        when(questService.verifyCompletedUserQuest(anyString(), anyString())).thenReturn(uq);

        Authentication auth = Mockito.mock(Authentication.class);
        when(auth.getName()).thenReturn("user-1");

        // Act
        ResponseEntity<?> response = controller.verifyQuest("q1", auth);

        // Assert
        assertThat(response.getStatusCode().is2xxSuccessful()).isTrue();
        @SuppressWarnings("unchecked")
        Map<String, Object> body = (Map<String, Object>) response.getBody();
        assertThat(body).isNotNull();
        assertThat(body.get("success")).isEqualTo(true);
    Object dataObj = body.get("data");
    assertThat(dataObj).isInstanceOf(UserQuestDTO.class);
    UserQuestDTO dto = (UserQuestDTO) dataObj;
    assertThat(dto.status.name()).isEqualTo("USER_VERIFIED");
    assertThat(dto.verified).isTrue();
    }
}
