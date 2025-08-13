package com.syntopia.controller;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import com.syntopia.config.JwtRequestFilter;
import com.syntopia.service.QuestService;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;

/**
 * Validates @Valid constraints for GitHub quest creation endpoint.
 */
@WebMvcTest(QuestController.class)
@AutoConfigureMockMvc(addFilters = true)
class QuestControllerValidationTest {

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
    private com.syntopia.security.AuthenticationUserDetailsService authenticationUserDetailsService;

    @MockBean
    private com.syntopia.service.UserService userService;

    @Test
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    void createGitHubQuest_validationFails_badUrlAndMissingFields() throws Exception {
    // Bypass JWT filter
    org.mockito.Mockito.doAnswer(invocation -> { var c = invocation.getArgument(2, jakarta.servlet.FilterChain.class); c.doFilter(invocation.getArgument(0), invocation.getArgument(1)); return null; })
        .when(jwtRequestFilter).doFilter(org.mockito.ArgumentMatchers.any(), org.mockito.ArgumentMatchers.any(), org.mockito.ArgumentMatchers.any());

    String invalidJson = "{" +
        "\"title\":\"\"," +
        "\"description\":\"\"," +
        "\"githubIssueUrl\":\"https://example.com/not-valid\"," +
        "\"repository\":\"\"," +
        "\"issueNumber\":0," +
        "\"requiredLevel\":30" +
        "}";

    mockMvc.perform(post("/api/quests/github").with(csrf())
            .contentType(MediaType.APPLICATION_JSON)
            .content(invalidJson))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.success").value(false))
        .andExpect(jsonPath("$.details.validationErrors.title").exists())
        .andExpect(jsonPath("$.details.validationErrors.description").exists())
        .andExpect(jsonPath("$.details.validationErrors.githubIssueUrl").exists())
        .andExpect(jsonPath("$.details.validationErrors.repository").exists())
        .andExpect(jsonPath("$.details.validationErrors.issueNumber").exists())
        .andExpect(jsonPath("$.details.validationErrors.requiredLevel").exists());
    }
}
