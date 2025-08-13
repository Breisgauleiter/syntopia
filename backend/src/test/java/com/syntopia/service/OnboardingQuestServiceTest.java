package com.syntopia.service;

import com.syntopia.model.OnboardingQuest;
import com.syntopia.repository.OnboardingQuestRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.ArrayList;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@SpringBootTest(classes = {OnboardingQuestService.class, OnboardingQuestGenerator.class})
class OnboardingQuestServiceTest {

    @MockBean
    private OnboardingQuestRepository repository;

    @Autowired
    private OnboardingQuestGenerator generator;

    @Autowired
    private OnboardingQuestService service;

    @BeforeEach
    void setup() {
        when(repository.count()).thenReturn(0L);
        when(repository.findAll()).thenReturn(new ArrayList<>());
    }

    @Test
    void seedIfEmpty_savesGeneratedQuests() {
        service.seedIfEmpty();
    verify(repository, times(1)).saveAll(any());
    }

    @Test
    void ensureQuest_createsWhenMissing() {
        String role = "Tech Development";
        int level = 1;
        String id = generator.generateQuestId(role, level);
        when(repository.findAll()).thenReturn(new ArrayList<>());
        when(repository.save(any(OnboardingQuest.class))).thenAnswer(invocation -> invocation.getArgument(0));

        OnboardingQuest quest = service.ensureQuest(role, level);
        assertThat(quest.getId()).isEqualTo(id);
        verify(repository, times(1)).save(any(OnboardingQuest.class));
    }

    @Test
    void getByRoleAndLevel_returnsPresentAfterEnsure() {
        String role = "UX Design";
        int level = 2;
        when(repository.save(any(OnboardingQuest.class))).thenAnswer(invocation -> invocation.getArgument(0));
        service.ensureQuest(role, level);
        assertThat(service.getByRoleAndLevel(role, level)).isPresent();
    }
}
