package com.syntopia.service;

import com.syntopia.model.User;
import com.syntopia.model.UserQuest;
import com.syntopia.repository.UserRepository;
import com.syntopia.repository.UserQuestRepository;
import com.syntopia.repository.UserCollaborationRepository;
import com.syntopia.repository.UserProjectRepository;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(classes = {ProfileService.class})
class ProfileServiceAchievementsTest {

    @MockBean
    private UserRepository userRepository;
    @MockBean
    private UserQuestRepository userQuestRepository;
    @MockBean
    private UserCollaborationRepository userCollaborationRepository;
    @MockBean
    private UserProjectRepository userProjectRepository;

    @Autowired
    private ProfileService profileService;

    @Test
    void achievements_includeProjectAndQuestProgress() {
        User u = new User();
        u.setId("u1");
        u.setUsername("user1");
        u.setCurrentLevel(3); // trigger level achievements
        u.setExperiencePoints(600); // trigger experience collector
        Mockito.when(userRepository.findById("u1")).thenReturn(Optional.of(u));

        Mockito.when(userQuestRepository.countByUserIdAndStatus("u1", UserQuest.UserQuestStatus.USER_COMPLETED)).thenReturn(5L);
        Mockito.when(userQuestRepository.countByUserIdAndStatus("u1", UserQuest.UserQuestStatus.USER_VERIFIED)).thenReturn(0L);
        Mockito.when(userCollaborationRepository.countConnections("u1", "ACCEPTED", "all")).thenReturn(2);
        Mockito.when(userProjectRepository.countProjects("u1", true)).thenReturn(2); // between thresholds

        Map<String,Object> result = profileService.getUserAchievements("u1");
        assertThat(result.get("achievements")).isInstanceOf(java.util.List.class);
        @SuppressWarnings("unchecked")
        java.util.List<Map<String,Object>> list = (java.util.List<Map<String,Object>>) result.get("achievements");

        // basic assertions for newly added project collaborator achievement (in_progress)
        boolean hasCollaboratorProgress = list.stream().anyMatch(a -> a.get("id").equals("project_collaborator") && "in_progress".equals(a.get("status")));
        boolean hasQuestNovice = list.stream().anyMatch(a -> a.get("id").equals("quest_novice") && "completed".equals(a.get("status")));
        boolean hasFirstProject = list.stream().anyMatch(a -> a.get("id").equals("first_project") && "completed".equals(a.get("status")));
        assertThat(hasCollaboratorProgress).isTrue();
        assertThat(hasQuestNovice).isTrue();
        assertThat(hasFirstProject).isTrue();
    }
}
