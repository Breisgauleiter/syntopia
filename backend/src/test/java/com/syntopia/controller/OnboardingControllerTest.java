package com.syntopia.controller;

import com.syntopia.service.QuestService;
import com.syntopia.service.OnboardingQuestGenerator;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import com.syntopia.config.JwtRequestFilter;

import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;

@WebMvcTest(OnboardingController.class)
@AutoConfigureMockMvc(addFilters = true)
class OnboardingControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private QuestService questService;

    @MockBean
    private JwtRequestFilter jwtRequestFilter;

    @MockBean
    private com.syntopia.config.JwtAuthenticationEntryPoint jwtAuthenticationEntryPoint;

    @MockBean
    private com.syntopia.config.JwtTokenUtil jwtTokenUtil;

    @MockBean
    private com.syntopia.service.UserService userService;

    @MockBean
    private OnboardingQuestGenerator onboardingQuestGenerator;

    @Test
    @WithMockUser(username = "user-1", roles = {"USER"})
    void acceptOnboardingQuest_returns200() throws Exception {
        // Bypass JWT filter
        org.mockito.Mockito.doAnswer(invocation -> {
            jakarta.servlet.FilterChain chain = invocation.getArgument(2);
            jakarta.servlet.http.HttpServletRequest req = invocation.getArgument(0);
            jakarta.servlet.http.HttpServletResponse res = invocation.getArgument(1);
            chain.doFilter(req, res);
            return null;
        }).when(jwtRequestFilter).doFilter(org.mockito.ArgumentMatchers.any(), org.mockito.ArgumentMatchers.any(), org.mockito.ArgumentMatchers.any());

        // Provide a basic UserDetails for Security if needed
        when(userService.loadUserByUsername(org.mockito.ArgumentMatchers.anyString()))
            .thenReturn(org.springframework.security.core.userdetails.User.withUsername("user-1").password("").authorities("ROLE_USER").build());

        when(questService.acceptOnboardingQuestForUser(anyString(), anyString(), anyInt()))
                .thenReturn(new com.syntopia.model.UserQuest());

    mockMvc.perform(post("/api/onboarding/accept").with(csrf())
                        .accept(org.springframework.http.MediaType.APPLICATION_JSON)
                        .param("role", "Tech Development")
                        .param("level", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }
}
