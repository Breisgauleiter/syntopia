package com.syntopia.service;

import com.syntopia.model.Quest;
import com.syntopia.model.User;
import com.syntopia.model.UserQuest;
import com.syntopia.repository.QuestRepository;
import com.syntopia.repository.UserQuestRepository;
import com.syntopia.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@SpringBootTest(classes = {QuestService.class})
class QuestServiceOnboardingProgressTest {

    @MockBean
    private QuestRepository questRepository;
    @MockBean
    private UserRepository userRepository;
    @MockBean
    private UserQuestRepository userQuestRepository;
    @MockBean
    private UserService userService;
    @MockBean
    private OnboardingQuestGenerator onboardingQuestGenerator;
    @MockBean
    private OnboardingQuestService onboardingQuestService;

    @Autowired
    private QuestService questService;

    @Test
    void getUserOnboardingUserQuests_filtersByMetadataFlag() {
        String userId = "u1";
        Quest onboardingQuest = new Quest();
        onboardingQuest.setId("q1");
        onboardingQuest.getMetadata().put("isOnboardingQuest", true);
        onboardingQuest.setRequiredLevel(1);

        User user = new User();
        user.setId(userId);
        when(userRepository.findById(userId)).thenReturn(java.util.Optional.of(user));

        UserQuest uq = new UserQuest(user, onboardingQuest);
        uq.setQuest(onboardingQuest);
        uq.markAsStarted();

        when(userQuestRepository.findByUserId(userId)).thenReturn(List.of(uq));

        var result = questService.getUserOnboardingUserQuests(userId);
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getQuest().getId()).isEqualTo("q1");
    }
}
