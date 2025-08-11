package com.syntopia.controller;

import com.syntopia.service.QuestService;
import com.syntopia.service.OnboardingQuestGenerator;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.core.Authentication;
import org.springframework.test.web.servlet.MockMvc;
import com.syntopia.config.JwtRequestFilter;

import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;

@WebMvcTest(OnboardingController.class)
@AutoConfigureMockMvc(addFilters = false)
class OnboardingControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private QuestService questService;

    @MockBean
    private Authentication authentication;

    @MockBean
    private JwtRequestFilter jwtRequestFilter;

    @MockBean
    private OnboardingQuestGenerator onboardingQuestGenerator;

    @Test
    void acceptOnboardingQuest_returns200() throws Exception {
        when(authentication.getName()).thenReturn("user-1");
        when(questService.acceptOnboardingQuestForUser(anyString(), anyString(), anyInt()))
                .thenReturn(new com.syntopia.model.UserQuest());

    mockMvc.perform(post("/api/onboarding/accept").with(csrf()).with(authentication(authentication))
                        .param("role", "Tech Development")
                        .param("level", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }
}
