package com.syntopia.controller;

import com.syntopia.service.OnboardingQuestService;
import com.syntopia.service.OnboardingQuestGenerator;
import com.syntopia.service.QuestService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;

/**
 * Focused tests for the cache refresh endpoint with role-based access control.
 */
@WebMvcTest(OnboardingController.class)
@AutoConfigureMockMvc(addFilters = true)
class OnboardingControllerAdminCacheRefreshTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private QuestService questService;

    @MockBean
    private OnboardingQuestGenerator onboardingQuestGenerator;

    @MockBean
    private OnboardingQuestService onboardingQuestService;

    @MockBean
    private com.syntopia.config.JwtRequestFilter jwtRequestFilter;

    @MockBean
    private com.syntopia.config.JwtAuthenticationEntryPoint jwtAuthenticationEntryPoint;

    @MockBean
    private com.syntopia.config.JwtTokenUtil jwtTokenUtil;

    @MockBean
    private com.syntopia.service.UserService userService;

    private void bypassJwt(String... authorities) throws Exception {
        org.mockito.Mockito.doAnswer(invocation -> {
            jakarta.servlet.FilterChain chain = invocation.getArgument(2);
            jakarta.servlet.http.HttpServletRequest req = invocation.getArgument(0);
            jakarta.servlet.http.HttpServletResponse res = invocation.getArgument(1);
            chain.doFilter(req, res);
            return null;
        }).when(jwtRequestFilter).doFilter(org.mockito.ArgumentMatchers.any(), org.mockito.ArgumentMatchers.any(), org.mockito.ArgumentMatchers.any());

        // Provide user details reflecting supplied authorities (default to ROLE_USER)
        String[] auths = (authorities == null || authorities.length == 0) ? new String[]{"ROLE_USER"} : authorities;
        org.mockito.Mockito.when(userService.loadUserByUsername(org.mockito.ArgumentMatchers.anyString()))
            .then(invocation -> org.springframework.security.core.userdetails.User
                .withUsername(invocation.getArgument(0))
                .password("")
                .authorities(auths)
                .build());
    }

    @Test
    @WithMockUser(username = "admin-x", roles = {"ADMIN"})
    void cacheRefresh_withAdminRole_returns200() throws Exception {
    bypassJwt("ROLE_ADMIN");
        mockMvc.perform(post("/api/onboarding/cache/refresh").with(csrf()))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.message").value("Onboarding quest cache refreshed"));
        org.mockito.Mockito.verify(onboardingQuestService).forceRefreshCache();
    }

}
